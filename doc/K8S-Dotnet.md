 
### 条件

	# 安装时需要确保 K8S 集群正在运行，并且挂载了 GlusterFS 集群文件系统

	# 相关文档： K8S 集群安装配置.md、GlusterFS 集群安装配置.md

	# 在各个主机检查 GlusterFS 集群文件系统

	df -h /mnt/gluster-gv0

	# 正常输出

	Filesystem      Size  Used Avail Use% Mounted on
	gfs:/gv0         48T  835G   48T   2% /mnt/gluster-gv0


#### 以下操作均在 K8S 第一个节点，即 Master 节点 192.168.1.21 执行


### 准备

	# 准备 Dotnet 应用，以 Winter-dotnet 为例，复制到 /mnt/gluster-gv0/k8s/dotnet 目录下

	# 如果访问 GitHub.com 需要代理在此设置
	# export http_proxy=http://192.168.1.109:3128/
	# export https_proxy=http://192.168.1.109:3128/

	(
		if [ ! -d ~/Winter-dotnet ]; then cd ~/ ; git clone https://github.com/AndyInAi/Winter-dotnet.git; fi

		mkdir -p /mnt/gluster-gv0/k8s/dotnet

		cp -f -r ~/Winter-dotnet /mnt/gluster-gv0/k8s/dotnet/
	)


### 配置

	# 生成启动脚本

	(
		mkdir -p /mnt/gluster-gv0/k8s/dotnet

		echo '#!/bin/bash

			echo "
				deb http://mirrors.tuna.tsinghua.edu.cn/ubuntu/ jammy main restricted universe multiverse
				deb http://mirrors.tuna.tsinghua.edu.cn/ubuntu/ jammy-updates main restricted universe multiverse
				deb http://mirrors.tuna.tsinghua.edu.cn/ubuntu/ jammy-backports main restricted universe multiverse
				deb http://mirrors.tuna.tsinghua.edu.cn/ubuntu/ jammy-security main restricted universe multiverse
			" > /etc/apt/sources.list

			export DEBIAN_FRONTEND=noninteractive
			
			apt update -y

			apt install -y wget
			
			wget https://packages.microsoft.com/config/ubuntu/22.04/packages-microsoft-prod.deb -O ~/packages-microsoft-prod.deb

			dpkg -i ~/packages-microsoft-prod.deb

			apt update -y

			apt install -y dotnet-sdk-8.0

			cp -f -r /mnt/dotnet/Winter-dotnet ~/

			cd ~/Winter-dotnet

			dotnet run --urls http://0.0.0.0:8080

			# sleep 88888

		'> /mnt/gluster-gv0/k8s/dotnet/start 
		
		chmod +x /mnt/gluster-gv0/k8s/dotnet/start
	)


### 部署

	# 副本：8 个

	# 端口：30008
	
	(
		mkdir -p /mnt/gluster-gv0/k8s/dotnet;

		kubectl get namespace winter > /dev/null 2>&1 || kubectl create namespace winter;

		echo '
                        apiVersion: apps/v1
                        kind: Deployment
                        metadata:
                          labels:
                            app: dotnet
                          name: dotnet
                          namespace: winter
                        spec:
                          replicas: 8
                          selector:
                            matchLabels:
                              app: dotnet
                          template:
                            metadata:
                              labels:
                                app: dotnet
                            spec:
                              volumes:
                                - name: k8s
                                  hostPath:
                                    path: /mnt/gluster-gv0/k8s
                              containers:
                                - image: ubuntu
                                  imagePullPolicy: IfNotPresent
                                  name: dotnet
                                  volumeMounts:
                                    - name: k8s
                                      mountPath: /mnt
                                  command: ["/mnt/dotnet/start"]
		' > ~/dotnet.yml;

		echo '
			apiVersion: v1
			kind: Service
			metadata:
			  name: dotnet
			  namespace: winter
			spec:
			  selector:
			    app: dotnet
			  type: LoadBalancer
			  ports:
			    - nodePort: 30008
			      port: 8080
			      protocol: TCP
			      targetPort: 8080
			  sessionAffinity: ClientIP
		' > dotnet-port.yml;
		sed -i 's/\t/        /g' ~/dotnet.yml;
		sed -i 's/\t/        /g' ~/dotnet-port.yml;
		
		kubectl apply -f ~/dotnet.yml;
		kubectl apply -f ~/dotnet-port.yml;
	)


### 查看

	kubectl get pod,svc,node -n winter -o wide


### 测试

	curl http://192.168.1.22:30008/
	curl http://192.168.1.23:30008/
	curl http://192.168.1.24:30008/

	# 使用浏览器打开

	http://192.168.1.24:30008/
	http://192.168.1.23:30008/
	http://192.168.1.22:30008/


### 扩缩

	# 静态扩缩，指定 pod 数量，如 12

	kubectl scale --replicas=12 -f ~/dotnet.yml

	# 动态扩缩，指定最小和最大 pod 数量

	kubectl autoscale  --max=16 --min=4 -f ~/dotnet.yml
	


