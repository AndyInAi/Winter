 
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

	# 准备 Winter Boot 应用，以 Winter-boot 为例，复制到 /mnt/gluster-gv0/k8s/boot 目录下

	# 如果访问 GitHub.com 需要代理在此设置
	# export http_proxy=http://192.168.1.109:3128/
	# export https_proxy=http://192.168.1.109:3128/

	(
		if [ ! -d ~/Winter-boot ]; then cd ~/ ; git clone https://github.com/AndyInAi/Winter-boot.git; fi ;

		mkdir -p /mnt/gluster-gv0/k8s/boot ;

		cp -f -r ~/Winter-boot /mnt/gluster-gv0/k8s/boot/ ;
	)


### 配置

	# 生成启动脚本

	(
		echo '#!/bin/bash

			mkdir -p /usr/local/tomcat/webapps/ROOT

			cp -f -r /mnt/boot/Winter-boot/www/* /usr/local/tomcat/webapps/ROOT

			cd /usr/local/tomcat && ./bin/catalina.sh run
			
			# sleep 88888

		'> /mnt/gluster-gv0/k8s/boot/start 
		
		chmod +x /mnt/gluster-gv0/k8s/boot/start
	)


### 部署

	# 副本：8 个

	# 端口：30018
	
	(
		mkdir -p /mnt/gluster-gv0/k8s/boot;

		kubectl get namespace winter > /dev/null 2>&1 || kubectl create namespace winter;

		echo '
                        apiVersion: apps/v1
                        kind: Deployment
                        metadata:
                          labels:
                            app: boot
                          name: boot
                          namespace: winter
                        spec:
                          replicas: 8
                          selector:
                            matchLabels:
                              app: boot
                          template:
                            metadata:
                              labels:
                                app: boot
                            spec:
                              volumes:
                                - name: k8s
                                  hostPath:
                                    path: /mnt/gluster-gv0/k8s
                              containers:
                                - image: tomcat
                                  imagePullPolicy: IfNotPresent
                                  name: boot
                                  volumeMounts:
                                    - name: k8s
                                      mountPath: /mnt
                                  command: ["/mnt/boot/start"]
		' > ~/boot.yml;

		echo '
			apiVersion: v1
			kind: Service
			metadata:
			  name: boot
			  namespace: winter
			spec:
			  selector:
			    app: boot
			  type: LoadBalancer
			  ports:
			    - nodePort: 30018
			      port: 8080
			      protocol: TCP
			      targetPort: 8080
			  sessionAffinity: ClientIP
		' > boot-port.yml;
		sed -i 's/\t/        /g' ~/boot.yml;
		sed -i 's/\t/        /g' ~/boot-port.yml;
		
		kubectl apply -f ~/boot.yml;
		kubectl apply -f ~/boot-port.yml;
	)


### 查看

	kubectl get pod,svc,node -n winter -o wide


### 测试

	curl http://192.168.1.22:30018/
	curl http://192.168.1.23:30018/
	curl http://192.168.1.24:30018/

	# 使用浏览器打开

	http://192.168.1.24:30018/
	http://192.168.1.23:30018/
	http://192.168.1.22:30018/


### 扩缩

	# 静态扩缩，指定 pod 数量，如 12

	kubectl scale --replicas=12 -f ~/boot.yml

	# 动态扩缩，指定最小和最大 pod 数量

	kubectl autoscale  --max=16 --min=4 -f ~/boot.yml
	


