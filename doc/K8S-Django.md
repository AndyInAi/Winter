 
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

	# 准备 Django 应用，以 Winter_django 为例，复制到 /mnt/gluster-gv0/k8s/django 目录下

	# 如果访问 GitHub.com 需要代理在此设置
	# export http_proxy=http://192.168.1.109:3128/
	# export https_proxy=http://192.168.1.109:3128/

	(
		if [ ! -d ~/Winter_django ]; then cd ~/ ; git clone https://github.com/AndyInAi/Winter_django.git; fi

		mkdir -p /mnt/gluster-gv0/k8s/django

		cp -f -r ~/Winter_django /mnt/gluster-gv0/k8s/django/
	)


### 配置

	# 生成启动脚本

	(
		mkdir -p /mnt/gluster-gv0/k8s/django

		echo '#!/bin/bash

			echo "
				deb http://mirrors.tuna.tsinghua.edu.cn/ubuntu/ jammy main restricted universe multiverse
				deb http://mirrors.tuna.tsinghua.edu.cn/ubuntu/ jammy-updates main restricted universe multiverse
				deb http://mirrors.tuna.tsinghua.edu.cn/ubuntu/ jammy-backports main restricted universe multiverse
				deb http://mirrors.tuna.tsinghua.edu.cn/ubuntu/ jammy-security main restricted universe multiverse
			" > /etc/apt/sources.list

			export DEBIAN_FRONTEND=noninteractive
			apt update -y

			python3 -V > /dev/null 2>&1 || apt install -y python3

			pip3 -V > /dev/null 2>&1 || apt install -y python3-pip

			pip3 config set global.index-url https://pypi.tuna.tsinghua.edu.cn/simple

			python3 -m django --version > /dev/null 2>&1 || pip3 install django tzdata
			
			cp -f -r /mnt/django/Winter_django ~/

			cd ~/Winter_django

			python3 manage.py runserver 0.0.0.0:8080

			# sleep 88888

		'> /mnt/gluster-gv0/k8s/django/start 
		
		chmod +x /mnt/gluster-gv0/k8s/django/start
	)


### 部署

	# 副本：8 个

	# 端口：30006
	
	(
		mkdir -p /mnt/gluster-gv0/k8s/django;

		kubectl get namespace winter > /dev/null 2>&1 || kubectl create namespace winter;

		echo '
                        apiVersion: apps/v1
                        kind: Deployment
                        metadata:
                          labels:
                            app: django
                          name: django
                          namespace: winter
                        spec:
                          replicas: 8
                          selector:
                            matchLabels:
                              app: django
                          template:
                            metadata:
                              labels:
                                app: django
                            spec:
                              volumes:
                                - name: k8s
                                  hostPath:
                                    path: /mnt/gluster-gv0/k8s
                              containers:
                                - image: ubuntu
                                  imagePullPolicy: IfNotPresent
                                  name: django
                                  volumeMounts:
                                    - name: k8s
                                      mountPath: /mnt
                                  command: ["/mnt/django/start"]
		' > ~/django.yml;

		echo '
			apiVersion: v1
			kind: Service
			metadata:
			  name: django
			  namespace: winter
			spec:
			  selector:
			    app: django
			  type: LoadBalancer
			  ports:
			    - nodePort: 30006
			      port: 8080
			      protocol: TCP
			      targetPort: 8080
			  sessionAffinity: ClientIP
		' > django-port.yml;
		sed -i 's/\t/        /g' ~/django.yml;
		sed -i 's/\t/        /g' ~/django-port.yml;
		
		kubectl apply -f ~/django.yml;
		kubectl apply -f ~/django-port.yml;
	)


### 查看

	kubectl get pod,svc,node -n winter -o wide


### 测试

	curl http://192.168.1.22:30006/
	curl http://192.168.1.23:30006/
	curl http://192.168.1.24:30006/

	# 使用浏览器打开

	http://192.168.1.24:30006/
	http://192.168.1.23:30006/
	http://192.168.1.22:30006/


### 扩缩

	# 静态扩缩，指定 pod 数量，如 12

	kubectl scale --replicas=12 -f ~/django.yml

	# 动态扩缩，指定最小和最大 pod 数量

	kubectl autoscale  --max=16 --min=4 -f ~/django.yml
	


