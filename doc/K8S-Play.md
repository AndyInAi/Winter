 
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

	# 准备 Play 应用，以 Winter-play 为例，复制到 /mnt/gluster-gv0/k8s/play 目录下

	# 如果访问 GitHub.com 需要代理在此设置
	# export http_proxy=http://192.168.1.109:3128/
	# export https_proxy=http://192.168.1.109:3128/

	(
		if [ ! -d ~/Winter-play ]; then cd ~/ ; git clone https://github.com/AndyInAi/Winter-play.git; fi

		mkdir -p /mnt/gluster-gv0/k8s/play

		cp -f -r ~/Winter-play /mnt/gluster-gv0/k8s/play/
	)


### 配置

	# 生成启动脚本

	(
		mkdir -p /mnt/gluster-gv0/k8s/play

		echo '#!/bin/bash

			echo "
			  deb http://mirrors.tuna.tsinghua.edu.cn/ubuntu/ jammy main restricted universe multiverse
			  deb http://mirrors.tuna.tsinghua.edu.cn/ubuntu/ jammy-updates main restricted universe multiverse
			  deb http://mirrors.tuna.tsinghua.edu.cn/ubuntu/ jammy-backports main restricted universe multiverse
			  deb http://mirrors.tuna.tsinghua.edu.cn/ubuntu/ jammy-security main restricted universe multiverse
			" > /etc/apt/sources.list
	
			apt -y update

			apt install -y curl gnupg openjdk-17-jdk-headless

			echo "deb https://repo.scala-sbt.org/scalasbt/debian all main" > /etc/apt/sources.list.d/sbt.list

			echo "deb https://repo.scala-sbt.org/scalasbt/debian /" > /etc/apt/sources.list.d/sbt_old.list

			curl -sL \
			     "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2EE0EA64E40A89B84B2DF73499E82A75642AC823" \
			     | apt-key add

			export DEBIAN_FRONTEND=noninteractive
			
			apt -y update

			apt -y install sbt

			cp -f -r /mnt/play/Winter-play ~/

			cd ~/Winter-play

			sbt run -Dhttp.port=8080

			# sleep 88888

		'> /mnt/gluster-gv0/k8s/play/start 
		
		chmod +x /mnt/gluster-gv0/k8s/play/start
	)


### 部署

	# 副本：8 个

	# 端口：30010
	
	(
		mkdir -p /mnt/gluster-gv0/k8s/play;

		kubectl get namespace winter > /dev/null 2>&1 || kubectl create namespace winter;

		echo '
                        apiVersion: apps/v1
                        kind: Deployment
                        metadata:
                          labels:
                            app: play
                          name: play
                          namespace: winter
                        spec:
                          replicas: 8
                          selector:
                            matchLabels:
                              app: play
                          template:
                            metadata:
                              labels:
                                app: play
                            spec:
                              volumes:
                                - name: k8s
                                  hostPath:
                                    path: /mnt/gluster-gv0/k8s
                              containers:
                                - image: ubuntu
                                  imagePullPolicy: IfNotPresent
                                  name: play
                                  volumeMounts:
                                    - name: k8s
                                      mountPath: /mnt
                                  command: ["/mnt/play/start"]
		' > ~/play.yml;

		echo '
			apiVersion: v1
			kind: Service
			metadata:
			  name: play
			  namespace: winter
			spec:
			  selector:
			    app: play
			  type: LoadBalancer
			  ports:
			    - nodePort: 30010
			      port: 8080
			      protocol: TCP
			      targetPort: 8080
			  sessionAffinity: ClientIP
		' > play-port.yml;
		sed -i 's/\t/        /g' ~/play.yml;
		sed -i 's/\t/        /g' ~/play-port.yml;
		
		kubectl apply -f ~/play.yml;
		kubectl apply -f ~/play-port.yml;
	)


### 查看

	kubectl get pod,svc,node -n winter -o wide


### 测试

	curl http://192.168.1.22:30010/
	curl http://192.168.1.23:30010/
	curl http://192.168.1.24:30010/

	# 使用浏览器打开

	http://192.168.1.24:30010/
	http://192.168.1.23:30010/
	http://192.168.1.22:30010/


### 扩缩

	# 静态扩缩，指定 pod 数量，如 12

	kubectl scale --replicas=12 -f ~/play.yml

	# 动态扩缩，指定最小和最大 pod 数量

	kubectl autoscale  --max=16 --min=4 -f ~/play.yml
	


