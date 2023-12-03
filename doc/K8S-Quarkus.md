 
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

	# 准备 Quarkus 应用，以 Winter-quarkus 为例，复制到 /mnt/gluster-gv0/k8s/quarkus 目录下

	# 如果访问 GitHub.com 需要代理在此设置
	# export http_proxy=http://192.168.1.109:3128/
	# export https_proxy=http://192.168.1.109:3128/

	(
		if [ ! -d ~/Winter-quarkus ]; then cd ~/ ; git clone https://github.com/AndyInAi/Winter-quarkus.git; fi ;

		cd  ~/Winter-quarkus && tar xzf quarkus-app.tar.gz

		mkdir -p /mnt/gluster-gv0/k8s/quarkus ;

		cp -f -r ~/Winter-quarkus /mnt/gluster-gv0/k8s/quarkus/ ;
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

			mkdir -p /mnt/gluster-gv0/k8s/quarkus

			cp -f -r /mnt/quarkus/Winter-quarkus ~/

			cd ~/Winter-quarkus && java -jar quarkus-app/quarkus-run.jar
			
			# sleep 88888

		'> /mnt/gluster-gv0/k8s/quarkus/start 
		
		chmod +x /mnt/gluster-gv0/k8s/quarkus/start
	)


### 部署

	# 副本：8 个

	# 端口：30014
	
	(
		mkdir -p /mnt/gluster-gv0/k8s/quarkus;

		kubectl get namespace winter > /dev/null 2>&1 || kubectl create namespace winter;

		echo '
                        apiVersion: apps/v1
                        kind: Deployment
                        metadata:
                          labels:
                            app: quarkus
                          name: quarkus
                          namespace: winter
                        spec:
                          replicas: 8
                          selector:
                            matchLabels:
                              app: quarkus
                          template:
                            metadata:
                              labels:
                                app: quarkus
                            spec:
                              volumes:
                                - name: k8s
                                  hostPath:
                                    path: /mnt/gluster-gv0/k8s
                              containers:
                                - image: ubuntu
                                  imagePullPolicy: IfNotPresent
                                  name: quarkus
                                  volumeMounts:
                                    - name: k8s
                                      mountPath: /mnt
                                  command: ["/mnt/quarkus/start"]
		' > ~/quarkus.yml;

		echo '
			apiVersion: v1
			kind: Service
			metadata:
			  name: quarkus
			  namespace: winter
			spec:
			  selector:
			    app: quarkus
			  type: LoadBalancer
			  ports:
			    - nodePort: 30014
			      port: 8080
			      protocol: TCP
			      targetPort: 8080
			  sessionAffinity: ClientIP
		' > quarkus-port.yml;
		sed -i 's/\t/        /g' ~/quarkus.yml;
		sed -i 's/\t/        /g' ~/quarkus-port.yml;
		
		kubectl apply -f ~/quarkus.yml;
		kubectl apply -f ~/quarkus-port.yml;
	)


### 查看

	kubectl get pod,svc,node -n winter -o wide


### 测试

	curl http://192.168.1.22:30014/
	curl http://192.168.1.23:30014/
	curl http://192.168.1.24:30014/

	# 使用浏览器打开

	http://192.168.1.24:30014/
	http://192.168.1.23:30014/
	http://192.168.1.22:30014/


### 扩缩

	# 静态扩缩，指定 pod 数量，如 12

	kubectl scale --replicas=12 -f ~/quarkus.yml

	# 动态扩缩，指定最小和最大 pod 数量

	kubectl autoscale  --max=16 --min=4 -f ~/quarkus.yml
	


