
### 条件

	# 安装时需要确保 K8S 集群正在运行，并且挂载了 GlusterFS 集群文件系统

	# 相关文档： K8S 集群安装配置.md、GlusterFS 集群安装配置.md

	# 在各个主机检查 GlusterFS 集群文件系统

	df -h /mnt/gluster-gv0

	# 正常输出

	Filesystem      Size  Used Avail Use% Mounted on
	gfs:/gv0         48T  835G   48T   2% /mnt/gluster-gv0


#### 以下操作均在 K8S 第一个节点，即 Master 节点 192.168.1.21 执行


### 配置

	# 生成启动脚本，要求 Spring Boot 应用 demo.jar 已放在 /root/demo.jar

	(
		mkdir -p /mnt/gluster-gv0/k8s/spring-boot
		
		cp -f /root/demo.jar /mnt/gluster-gv0/k8s/spring-boot/

		echo '#!/bin/bash
			#sleep 88888
			echo "
				deb http://mirrors.tuna.tsinghua.edu.cn/ubuntu/ jammy main restricted universe multiverse
				deb http://mirrors.tuna.tsinghua.edu.cn/ubuntu/ jammy-updates main restricted universe multiverse
				deb http://mirrors.tuna.tsinghua.edu.cn/ubuntu/ jammy-backports main restricted universe multiverse
				deb http://mirrors.tuna.tsinghua.edu.cn/ubuntu/ jammy-security main restricted universe multiverse
			" > /etc/apt/sources.list
			apt update -y
			apt install -y openjdk-17-jdk-headless
			cp -f /mnt/spring-boot/demo.jar /root/
			cd /root/
			java -jar demo.jar
		'> /mnt/gluster-gv0/k8s/spring-boot/start 
		
		chmod +x /mnt/gluster-gv0/k8s/spring-boot/start
	)


### 部署

	# 副本：16 个

	# 端口：30002
	
	(
		mkdir -p /mnt/gluster-gv0/k8s/spring-boot;

		kubectl get namespace winter > /dev/null 2>&1 || kubectl create namespace winter;

		echo '
                        apiVersion: apps/v1
                        kind: Deployment
                        metadata:
                          labels:
                            app: spring-boot
                          name: spring-boot
                          namespace: winter
                        spec:
                          replicas: 16
                          selector:
                            matchLabels:
                              app: spring-boot
                          template:
                            metadata:
                              labels:
                                app: spring-boot
                            spec:
                              volumes:
                                - name: k8s
                                  hostPath:
                                    path: /mnt/gluster-gv0/k8s
                              containers:
                                - image: ubuntu
                                  imagePullPolicy: IfNotPresent
                                  name: spring-boot
                                  volumeMounts:
                                    - name: k8s
                                      mountPath: /mnt
                                  command: ["/mnt/spring-boot/start"]
		' > ~/spring-boot.yml;

		echo '
			apiVersion: v1
			kind: Service
			metadata:
			  name: spring-boot
			  namespace: winter
			spec:
			  selector:
			    app: spring-boot
			  type: LoadBalancer
			  ports:
			    - nodePort: 30002
			      port: 8080
			      protocol: TCP
			      targetPort: 8080
			  sessionAffinity: ClientIP
		' > spring-boot-port.yml;
		sed -i 's/\t/        /g' ~/spring-boot.yml;
		sed -i 's/\t/        /g' ~/spring-boot-port.yml;
		
		kubectl apply -f ~/spring-boot.yml;
		kubectl apply -f ~/spring-boot-port.yml;
	)


### 查看

	kubectl get pod,svc,node -n winter -o wide


### 测试

	curl http://192.168.1.22:30002/hello
	curl http://192.168.1.23:30002/hello
	curl http://192.168.1.24:30002/hello

	# 使用浏览器打开

	http://192.168.1.24:30002/hello
	http://192.168.1.23:30002/hello
	http://192.168.1.22:30002/hello


### 扩缩

	# 静态扩缩，指定 pod 数量，如 24

	kubectl scale --replicas=24 -f ~/spring-boot.yml

	# 动态扩缩，指定最小和最大 pod 数量

	kubectl autoscale  --max=32 --min=16 -f ~/spring-boot.yml
	


