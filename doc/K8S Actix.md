 
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

	# 编译 Actix 应用，以 Winter-actix 为例，编译后复制到 /mnt/gluster-gv0/k8s/actix 目录下

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
		apt install -y cargo wget git
	)

	(
		if [ ! -d ~/Winter-actix ]; then cd ~/ ; git clone https://github.com/AndyInAi/Winter-actix.git; fi

		cd ~/Winter-actix

		cargo build --release

		mkdir -p /mnt/gluster-gv0/k8s/actix

		cp -f ./target/release/Winter-actix /mnt/gluster-gv0/k8s/actix/
	)


### 配置

	# 生成启动脚本

	(
		mkdir -p /mnt/gluster-gv0/k8s/actix

		echo '#!/bin/bash

			cp -f /mnt/actix/Winter-actix ~/
			
			chmod +x ~/Winter-actix

			~/Winter-actix

		'> /mnt/gluster-gv0/k8s/actix/start 
		
		chmod +x /mnt/gluster-gv0/k8s/actix/start
	)


### 部署

	# 副本：16 个

	# 端口：30005
	
	(
		mkdir -p /mnt/gluster-gv0/k8s/actix;

		kubectl get namespace winter > /dev/null 2>&1 || kubectl create namespace winter;

		echo '
                        apiVersion: apps/v1
                        kind: Deployment
                        metadata:
                          labels:
                            app: actix
                          name: actix
                          namespace: winter
                        spec:
                          replicas: 16
                          selector:
                            matchLabels:
                              app: actix
                          template:
                            metadata:
                              labels:
                                app: actix
                            spec:
                              volumes:
                                - name: k8s
                                  hostPath:
                                    path: /mnt/gluster-gv0/k8s
                              containers:
                                - image: ubuntu
                                  imagePullPolicy: IfNotPresent
                                  name: actix
                                  volumeMounts:
                                    - name: k8s
                                      mountPath: /mnt
                                  command: ["/mnt/actix/start"]
		' > ~/actix.yml;

		echo '
			apiVersion: v1
			kind: Service
			metadata:
			  name: actix
			  namespace: winter
			spec:
			  selector:
			    app: actix
			  type: LoadBalancer
			  ports:
			    - nodePort: 30005
			      port: 8080
			      protocol: TCP
			      targetPort: 8080
			  sessionAffinity: ClientIP
		' > actix-port.yml;
		sed -i 's/\t/        /g' ~/actix.yml;
		sed -i 's/\t/        /g' ~/actix-port.yml;
		
		kubectl apply -f ~/actix.yml;
		kubectl apply -f ~/actix-port.yml;
	)


### 查看

	kubectl get pod,svc,node -n winter -o wide


### 测试

	curl http://192.168.1.22:30005/
	curl http://192.168.1.23:30005/
	curl http://192.168.1.24:30005/

	# 使用浏览器打开

	http://192.168.1.24:30005/
	http://192.168.1.23:30005/
	http://192.168.1.22:30005/


### 扩缩

	# 静态扩缩，指定 pod 数量，如 24

	kubectl scale --replicas=24 -f ~/actix.yml

	# 动态扩缩，指定最小和最大 pod 数量

	kubectl autoscale  --max=32 --min=16 -f ~/actix.yml
	


