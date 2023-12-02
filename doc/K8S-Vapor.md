 
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

	# 准备 Vapor 应用，以 Winter-vapor 为例，复制到 /mnt/gluster-gv0/k8s/vapor 目录下

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
		if [ ! -d ~/Winter-vapor ]; then cd ~/ ; git clone https://github.com/AndyInAi/Winter-vapor.git; fi ;

		cd ~/Winter-vapor

		xz -d -k App.xz

		chmod +x App

		mkdir -p /mnt/gluster-gv0/k8s/vapor ;

		cp -f -r ~/Winter-vapor /mnt/gluster-gv0/k8s/vapor/ ;
	)


### 配置

	# 生成启动脚本

	(
		mkdir -p /mnt/gluster-gv0/k8s/vapor

		echo '#!/bin/bash

			cp -f -r /mnt/vapor/Winter-vapor ~/

			chmod +x ~/Winter-vapor/App

			~/Winter-vapor/App serve -b 0.0.0.0:8080
			
			# sleep 88888

		'> /mnt/gluster-gv0/k8s/vapor/start 
		
		chmod +x /mnt/gluster-gv0/k8s/vapor/start
	)


### 部署

	# 副本：8 个

	# 端口：30012
	
	(
		mkdir -p /mnt/gluster-gv0/k8s/vapor;

		kubectl get namespace winter > /dev/null 2>&1 || kubectl create namespace winter;

		echo '
                        apiVersion: apps/v1
                        kind: Deployment
                        metadata:
                          labels:
                            app: vapor
                          name: vapor
                          namespace: winter
                        spec:
                          replicas: 8
                          selector:
                            matchLabels:
                              app: vapor
                          template:
                            metadata:
                              labels:
                                app: vapor
                            spec:
                              volumes:
                                - name: k8s
                                  hostPath:
                                    path: /mnt/gluster-gv0/k8s
                              containers:
                                - image: ubuntu
                                  imagePullPolicy: IfNotPresent
                                  name: vapor
                                  volumeMounts:
                                    - name: k8s
                                      mountPath: /mnt
                                  command: ["/mnt/vapor/start"]
		' > ~/vapor.yml;

		echo '
			apiVersion: v1
			kind: Service
			metadata:
			  name: vapor
			  namespace: winter
			spec:
			  selector:
			    app: vapor
			  type: LoadBalancer
			  ports:
			    - nodePort: 30012
			      port: 8080
			      protocol: TCP
			      targetPort: 8080
			  sessionAffinity: ClientIP
		' > vapor-port.yml;
		sed -i 's/\t/        /g' ~/vapor.yml;
		sed -i 's/\t/        /g' ~/vapor-port.yml;
		
		kubectl apply -f ~/vapor.yml;
		kubectl apply -f ~/vapor-port.yml;
	)


### 查看

	kubectl get pod,svc,node -n winter -o wide


### 测试

	curl http://192.168.1.22:30012/hello
	curl http://192.168.1.23:30012/hello
	curl http://192.168.1.24:30012/hello

	# 使用浏览器打开

	http://192.168.1.24:30012/hello
	http://192.168.1.23:30012/hello
	http://192.168.1.22:30012/hello


### 扩缩

	# 静态扩缩，指定 pod 数量，如 12

	kubectl scale --replicas=12 -f ~/vapor.yml

	# 动态扩缩，指定最小和最大 pod 数量

	kubectl autoscale  --max=16 --min=4 -f ~/vapor.yml
	


