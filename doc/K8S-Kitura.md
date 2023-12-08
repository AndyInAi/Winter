
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

	# 准备 Kitura 应用，以 Winter-kitura 为例，复制到 /mnt/gluster-gv0/k8s/kitura 目录下

	(
		echo "
			deb http://mirrors.tuna.tsinghua.edu.cn/ubuntu/ jammy main restricted universe multiverse
			deb http://mirrors.tuna.tsinghua.edu.cn/ubuntu/ jammy-updates main restricted universe multiverse
			deb http://mirrors.tuna.tsinghua.edu.cn/ubuntu/ jammy-backports main restricted universe multiverse
			deb http://mirrors.tuna.tsinghua.edu.cn/ubuntu/ jammy-security main restricted universe multiverse
		" > /etc/apt/sources.list

		export DEBIAN_FRONTEND=noninteractive
		
		apt update -y

		apt install -y git xz-utils
	)

	# 如果访问 GitHub.com 需要代理在此设置
	# export http_proxy=http://192.168.1.109:3128/
	# export https_proxy=http://192.168.1.109:3128/

	(
		if [ ! -d ~/Winter-kitura ]; then cd ~/ ; git clone https://github.com/AndyInAi/Winter-kitura.git; fi ;

		cd ~/Winter-kitura && xz -d -k Winter-kitura.xz

		chmod +x ~/Winter-kitura/Winter-kitura

		mkdir -p /mnt/gluster-gv0/k8s/kitura ;

		cp -f -r ~/Winter-kitura /mnt/gluster-gv0/k8s/kitura/ ;
	)


### 配置

	# 生成启动脚本

	(
		mkdir -p /mnt/gluster-gv0/k8s/kitura

		echo '#!/bin/bash

			echo "
				deb http://mirrors.tuna.tsinghua.edu.cn/ubuntu/ jammy main restricted universe multiverse
				deb http://mirrors.tuna.tsinghua.edu.cn/ubuntu/ jammy-updates main restricted universe multiverse
				deb http://mirrors.tuna.tsinghua.edu.cn/ubuntu/ jammy-backports main restricted universe multiverse
				deb http://mirrors.tuna.tsinghua.edu.cn/ubuntu/ jammy-security main restricted universe multiverse
			" > /etc/apt/sources.list

			export DEBIAN_FRONTEND=noninteractive
			
			apt update -y

			apt install -y curl

			cp -f -r /mnt/kitura/Winter-kitura ~/

			chmod +x ~/Winter-kitura/Winter-kitura

			~/Winter-kitura/Winter-kitura
			
			sleep 88888

		'> /mnt/gluster-gv0/k8s/kitura/start 
		
		chmod +x /mnt/gluster-gv0/k8s/kitura/start
	)


### 部署

	# 副本：8 个

	# 端口：30019
	
	(
		mkdir -p /mnt/gluster-gv0/k8s/kitura;

		kubectl get namespace winter > /dev/null 2>&1 || kubectl create namespace winter;

		echo '
                        apiVersion: apps/v1
                        kind: Deployment
                        metadata:
                          labels:
                            app: kitura
                          name: kitura
                          namespace: winter
                        spec:
                          replicas: 1
                          selector:
                            matchLabels:
                              app: kitura
                          template:
                            metadata:
                              labels:
                                app: kitura
                            spec:
                              volumes:
                                - name: k8s
                                  hostPath:
                                    path: /mnt/gluster-gv0/k8s
                              containers:
                                - image: ubuntu
                                  imagePullPolicy: IfNotPresent
                                  name: kitura
                                  volumeMounts:
                                    - name: k8s
                                      mountPath: /mnt
                                  command: ["/mnt/kitura/start"]
		' > ~/kitura.yml;

		echo '
			apiVersion: v1
			kind: Service
			metadata:
			  name: kitura
			  namespace: winter
			spec:
			  selector:
			    app: kitura
			  type: LoadBalancer
			  ports:
			    - nodePort: 30019
			      port: 8080
			      protocol: TCP
			      targetPort: 8080
			  sessionAffinity: ClientIP
		' > kitura-port.yml;
		sed -i 's/\t/        /g' ~/kitura.yml;
		sed -i 's/\t/        /g' ~/kitura-port.yml;
		
		kubectl apply -f ~/kitura.yml;
		kubectl apply -f ~/kitura-port.yml;
	)


### 查看

	kubectl get pod,svc,node -n winter -o wide


### 测试

	curl http://192.168.1.22:30019/
	curl http://192.168.1.23:30019/
	curl http://192.168.1.24:30019/

	# 使用浏览器打开

	http://192.168.1.24:30019/
	http://192.168.1.23:30019/
	http://192.168.1.22:30019/


### 扩缩

	# 静态扩缩，指定 pod 数量，如 12

	kubectl scale --replicas=12 -f ~/kitura.yml

	# 动态扩缩，指定最小和最大 pod 数量

	kubectl autoscale  --max=16 --min=4 -f ~/kitura.yml
	


