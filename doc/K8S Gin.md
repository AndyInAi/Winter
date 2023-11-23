 
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

	# 编译 Gin 应用，以 Winter-gin 为例，编译后复制到 /mnt/gluster-gv0/k8s/gin 目录下

	(
	    echo '
		[source.crates-io]
		replace-with = "mirror"
		
		[source.mirror]
		registry = "http://mirrors4.tuna.tsinghua.edu.cn/git/crates.io-index.git"
	    ' > ~/.cargo/config

		echo "
			deb http://mirrors.tuna.tsinghua.edu.cn/ubuntu/ jammy main restricted universe multiverse
			deb http://mirrors.tuna.tsinghua.edu.cn/ubuntu/ jammy-updates main restricted universe multiverse
			deb http://mirrors.tuna.tsinghua.edu.cn/ubuntu/ jammy-backports main restricted universe multiverse
			deb http://mirrors.tuna.tsinghua.edu.cn/ubuntu/ jammy-security main restricted universe multiverse
		" > /etc/apt/sources.list

		export DEBIAN_FRONTEND=noninteractive
		apt update -y
		apt install -y golang-go git wget
	)

	# 如果访问 GitHub.com 需要代理在此设置
	# export http_proxy=http://192.168.1.109:3128/
	# export https_proxy=http://192.168.1.109:3128/
	
	(
		if [ ! -d ~/Winter-gin]; then
		    cd ~/ ;
		    git clone https://github.com/AndyInAi/Winter-gin.git
		fi
		  
		cd ~/Winter-gin
		  
		go mod init Winter-gin
		  
		go get github.com/gin-gonic/gin

		go build .

		cd ~/

		mkdir -p /mnt/gluster-gv0/k8s/gin

		cp -f  ~/Winter-gin/Winter-gin /mnt/gluster-gv0/k8s/gin/
	)


### 配置

	# 生成启动脚本

	(
		mkdir -p /mnt/gluster-gv0/k8s/gin

		echo '#!/bin/bash

			cp -f /mnt/gin/Winter-gin ~/
			
			chmod +x ~/Winter-gin

			~/Winter-gin

		'> /mnt/gluster-gv0/k8s/gin/start 
		
		chmod +x /mnt/gluster-gv0/k8s/gin/start
	)


### 部署

	# 副本：16 个

	# 端口：30004
	
	(
		mkdir -p /mnt/gluster-gv0/k8s/gin;

		kubectl get namespace winter > /dev/null 2>&1 || kubectl create namespace winter;

		echo '
                        apiVersion: apps/v1
                        kind: Deployment
                        metadata:
                          labels:
                            app: gin
                          name: gin
                          namespace: winter
                        spec:
                          replicas: 1
                          selector:
                            matchLabels:
                              app: gin
                          template:
                            metadata:
                              labels:
                                app: gin
                            spec:
                              volumes:
                                - name: k8s
                                  hostPath:
                                    path: /mnt/gluster-gv0/k8s
                              containers:
                                - image: ubuntu
                                  imagePullPolicy: IfNotPresent
                                  name: gin
                                  volumeMounts:
                                    - name: k8s
                                      mountPath: /mnt
                                  command: ["/mnt/gin/start"]
		' > ~/gin.yml;

		echo '
			apiVersion: v1
			kind: Service
			metadata:
			  name: gin
			  namespace: winter
			spec:
			  selector:
			    app: gin
			  type: LoadBalancer
			  ports:
			    - nodePort: 30004
			      port: 8080
			      protocol: TCP
			      targetPort: 8080
			  sessionAffinity: ClientIP
		' > gin-port.yml;
		sed -i 's/\t/        /g' ~/gin.yml;
		sed -i 's/\t/        /g' ~/gin-port.yml;
		
		kubectl apply -f ~/gin.yml;
		kubectl apply -f ~/gin-port.yml;
	)


### 查看

	kubectl get pod,svc,node -n winter -o wide


### 测试

	curl http://192.168.1.22:30004/ping
	curl http://192.168.1.23:30004/ping
	curl http://192.168.1.24:30004/ping

	# 使用浏览器打开

	http://192.168.1.24:30004/ping
	http://192.168.1.23:30004/ping
	http://192.168.1.22:30004/ping


### 扩缩

	# 静态扩缩，指定 pod 数量，如 24

	kubectl scale --replicas=24 -f ~/gin.yml

	# 动态扩缩，指定最小和最大 pod 数量

	kubectl autoscale  --max=32 --min=16 -f ~/gin.yml
	


