
### 条件

	# 安装时需要确保 K8S 集群正在运行，并且挂载了 GlusterFS 集群文件系统

	# 相关文档： K8S 集群安装配置.md、GlusterFS 集群安装配置.md

	# 在各个主机检查 GlusterFS 集群文件系统

	df -h /mnt/gluster-gv0

	# 正常输出

	Filesystem      Size  Used Avail Use% Mounted on
	gfs:/gv0         48T  835G   48T   2% /mnt/gluster-gv0


#### 以下操作均在 K8S 第一个节点，即 Master 节点 192.168.1.21 执行


### 编译

	(
		export DEBIAN_FRONTEND=noninteractive
		
		apt update -y

		apt install -y openjdk-17-jdk-headless gcc make libssl-dev

		if [ ! -f ~/resin-4.0.66.tar.gz ]; then wget -O ~/resin-4.0.66.tar.gz https://caucho.com/download/resin-4.0.66.tar.gz ; fi
		
		if [ ! -d ~/resin-4.0.66 ]; then tar --overwrite -xvzf ~/resin-4.0.66.tar.gz ; fi

		if [ -d ~/resin-4.0.66 ]; then 
		
			cd ~/resin-4.0.66 && ./configure --prefix=/root/resin-4 && make -j `nproc`&& make install; 
			
			if [ "`grep ^web_admin_enable ~/resin-4/conf/resin.properties`" == "" ]; then echo "web_admin_enable : false" >> ~/resin-4/conf/resin.properties; else sed -i "s/^web_admin_enable.*$/web_admin_enable : false/g" ~/resin-4/conf/resin.properties; fi
			if [ "`grep ^resin_doc ~/resin-4/conf/resin.properties`" == "" ]; then echo "resin_doc : false" >> ~/resin-4/conf/resin.properties; else sed -i "s/^resin_doc.*$/resin_doc : false/g" ~/resin-4/conf/resin.properties; fi

		fi
	)


### 配置

	# 生成启动脚本，要求 web 应用已放置在根目录 /mnt/gluster-gv0/k8s/resin/webapps/ROOT

	(
		mkdir -p /mnt/gluster-gv0/k8s/resin/webapps/ROOT

		# 生成测试页面
		echo "<h1>Hello Resin <%= new java.util.Date() %></h1>" > /mnt/gluster-gv0/k8s/resin/webapps/ROOT/hello.jsp

		if [ -d ~/resin-4 ]; then mkdir -p /mnt/gluster-gv0/k8s/resin; cp -r -f ~/resin-4 /mnt/gluster-gv0/k8s/resin/ ; fi

		echo '#!/bin/bash
			
			echo "
				deb http://mirrors.tuna.tsinghua.edu.cn/ubuntu/ jammy main restricted universe multiverse
				deb http://mirrors.tuna.tsinghua.edu.cn/ubuntu/ jammy-updates main restricted universe multiverse
				deb http://mirrors.tuna.tsinghua.edu.cn/ubuntu/ jammy-backports main restricted universe multiverse
				deb http://mirrors.tuna.tsinghua.edu.cn/ubuntu/ jammy-security main restricted universe multiverse
			" > /etc/apt/sources.list

			export DEBIAN_FRONTEND=noninteractive
			apt update -y
			apt install -y openjdk-17-jdk-headless
			
			cp -r -f /mnt/resin/resin-4 /root/
			cp -f -r /mnt/resin/webapps/ROOT /root/resin-4/webapps/

			cd /root/resin-4
			bin/resinctl console

		'> /mnt/gluster-gv0/k8s/resin/start
		
		chmod +x /mnt/gluster-gv0/k8s/resin/start
	)


### 部署

	# 副本：16 个

	# 端口：30003
	
	(
		mkdir -p /mnt/gluster-gv0/k8s/resin;

		kubectl get namespace winter > /dev/null 2>&1 || kubectl create namespace winter;

		echo '
                        apiVersion: apps/v1
                        kind: Deployment
                        metadata:
                          labels:
                            app: resin
                          name: resin
                          namespace: winter
                        spec:
                          replicas: 1
                          selector:
                            matchLabels:
                              app: resin
                          template:
                            metadata:
                              labels:
                                app: resin
                            spec:
                              volumes:
                                - name: k8s
                                  hostPath:
                                    path: /mnt/gluster-gv0/k8s
                              containers:
                                - image: ubuntu
                                  imagePullPolicy: IfNotPresent
                                  name: resin
                                  volumeMounts:
                                    - name: k8s
                                      mountPath: /mnt
                                  command: ["/mnt/resin/start"]
		' > ~/resin.yml;

		echo '
			apiVersion: v1
			kind: Service
			metadata:
			  name: resin
			  namespace: winter
			spec:
			  selector:
			    app: resin
			  type: LoadBalancer
			  ports:
			    - nodePort: 30003
			      port: 8080
			      protocol: TCP
			      targetPort: 8080
			  sessionAffinity: ClientIP
		' > resin-port.yml;
		sed -i 's/\t/        /g' ~/resin.yml;
		sed -i 's/\t/        /g' ~/resin-port.yml;
		
		kubectl apply -f ~/resin.yml;
		kubectl apply -f ~/resin-port.yml;
	)


### 查看

	kubectl get pod,svc,node -n winter -o wide


### 测试	

	curl http://192.168.1.22:30003/hello.jsp
	curl http://192.168.1.23:30003/hello.jsp
	curl http://192.168.1.24:30003/hello.jsp

	# 使用浏览器打开

	http://192.168.1.24:30003/hello.jsp
	http://192.168.1.23:30003/hello.jsp
	http://192.168.1.22:30003/hello.jsp


### 扩缩

	# 静态扩缩，指定 pod 数量，如 24

	kubectl scale --replicas=24 -f ~/resin.yml

	# 动态扩缩，指定最小和最大 pod 数量

	kubectl autoscale  --max=32 --min=16 -f ~/resin.yml
	


