 
### 条件

	# 安装时需要确保 K8S 集群正在运行，并且挂载了 GlusterFS 集群文件系统

	# 相关文档： K8S.md、GlusterFS.md

	# 在各个主机检查 GlusterFS 集群文件系统

	df -h /mnt/gluster-gv0

	# 正常输出

	Filesystem      Size  Used Avail Use% Mounted on
	gfs:/gv0         48T  835G   48T   2% /mnt/gluster-gv0


#### 以下操作均在 K8S 第一个节点，即 Master 节点 192.168.1.21 执行


### 准备

	# 准备 Spring 应用，以 Winter-spring 为例，复制到 /mnt/gluster-gv0/k8s/spring 目录下

	# 如果访问 GitHub.com 需要代理在此设置
	# export http_proxy=http://192.168.1.109:3128/
	# export https_proxy=http://192.168.1.109:3128/

	(
		if [ ! -d ~/Winter-spring ]; then cd ~/ ; git clone https://github.com/AndyInAi/Winter-spring.git; fi ;

		mkdir -p /mnt/gluster-gv0/k8s/spring ;

		cp -f -r ~/Winter-spring /mnt/gluster-gv0/k8s/spring/ ;
	)


### 配置

	# 生成启动脚本

	(
		echo '#!/bin/bash

			echo "
			  deb http://mirrors.tuna.tsinghua.edu.cn/ubuntu/ jammy main restricted universe multiverse
			  deb http://mirrors.tuna.tsinghua.edu.cn/ubuntu/ jammy-updates main restricted universe multiverse
			  deb http://mirrors.tuna.tsinghua.edu.cn/ubuntu/ jammy-backports main restricted universe multiverse
			  deb http://mirrors.tuna.tsinghua.edu.cn/ubuntu/ jammy-security main restricted universe multiverse
			" > /etc/apt/sources.list

			export DEBIAN_FRONTEND=noninteractive

			apt update -y

			apt install -y openjdk-21-jdk-headless

			mkdir -p /mnt/gluster-gv0/k8s/spring

			cp -f -r /mnt/spring/Winter-spring ~/

			cd ~/Winter-spring && java -jar Winter-spring-1.0.jar
			
			# sleep 88888

		'> /mnt/gluster-gv0/k8s/spring/start 
		
		chmod +x /mnt/gluster-gv0/k8s/spring/start
	)


### 部署

	# 副本：8 个

	# 端口：30015
	
	(
		mkdir -p /mnt/gluster-gv0/k8s/spring;

		kubectl get namespace winter > /dev/null 2>&1 || kubectl create namespace winter;

		echo '
                        apiVersion: apps/v1
                        kind: Deployment
                        metadata:
                          labels:
                            app: spring
                          name: spring
                          namespace: winter
                        spec:
                          replicas: 8
                          selector:
                            matchLabels:
                              app: spring
                          template:
                            metadata:
                              labels:
                                app: spring
                            spec:
                              volumes:
                                - name: k8s
                                  hostPath:
                                    path: /mnt/gluster-gv0/k8s
                              containers:
                                - image: ubuntu
                                  imagePullPolicy: IfNotPresent
                                  name: spring
                                  volumeMounts:
                                    - name: k8s
                                      mountPath: /mnt
                                  command: ["/mnt/spring/start"]
		' > ~/spring.yml;

		echo '
			apiVersion: v1
			kind: Service
			metadata:
			  name: spring
			  namespace: winter
			spec:
			  selector:
			    app: spring
			  type: LoadBalancer
			  ports:
			    - nodePort: 30015
			      port: 8080
			      protocol: TCP
			      targetPort: 8080
			  sessionAffinity: ClientIP
		' > spring-port.yml;
		sed -i 's/\t/        /g' ~/spring.yml;
		sed -i 's/\t/        /g' ~/spring-port.yml;
		
		kubectl apply -f ~/spring.yml;
		kubectl apply -f ~/spring-port.yml;
	)


### 查看

	kubectl get pod,svc,node -n winter -o wide


### 测试

	curl http://192.168.1.22:30015/
	curl http://192.168.1.23:30015/
	curl http://192.168.1.24:30015/

	# 使用浏览器打开

	http://192.168.1.24:30015/
	http://192.168.1.23:30015/
	http://192.168.1.22:30015/


### 扩缩

	# 静态扩缩，指定 pod 数量，如 12

	kubectl scale --replicas=12 -f ~/spring.yml

	# 动态扩缩，指定最小和最大 pod 数量

	kubectl autoscale  --max=16 --min=4 -f ~/spring.yml
	


