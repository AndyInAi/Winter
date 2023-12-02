 
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

	# 准备 Oatpp 应用，以 Winter-oatpp 为例，复制到 /mnt/gluster-gv0/k8s/oatpp 目录下

	# 如果访问 GitHub.com 需要代理在此设置
	# export http_proxy=http://192.168.1.109:3128/
	# export https_proxy=http://192.168.1.109:3128/

	(
		if [ ! -d ~/Winter-oatpp ]; then cd ~/ ; git clone https://github.com/AndyInAi/Winter-oatpp.git; fi ;

		chmod +x ~/Winter-oatpp/Winter-oatpp

		mkdir -p /mnt/gluster-gv0/k8s/oatpp ;

		cp -f -r ~/Winter-oatpp /mnt/gluster-gv0/k8s/oatpp/ ;
	)


### 配置

	# 生成启动脚本

	(
		mkdir -p /mnt/gluster-gv0/k8s/oatpp

		echo '#!/bin/bash

			cp -f -r /mnt/oatpp/Winter-oatpp ~/

			chmod +x ~/Winter-oatpp/Winter-oatpp

			~/Winter-oatpp/Winter-oatpp
			
			# sleep 88888

		'> /mnt/gluster-gv0/k8s/oatpp/start 
		
		chmod +x /mnt/gluster-gv0/k8s/oatpp/start
	)


### 部署

	# 副本：8 个

	# 端口：30013
	
	(
		mkdir -p /mnt/gluster-gv0/k8s/oatpp;

		kubectl get namespace winter > /dev/null 2>&1 || kubectl create namespace winter;

		echo '
                        apiVersion: apps/v1
                        kind: Deployment
                        metadata:
                          labels:
                            app: oatpp
                          name: oatpp
                          namespace: winter
                        spec:
                          replicas: 8
                          selector:
                            matchLabels:
                              app: oatpp
                          template:
                            metadata:
                              labels:
                                app: oatpp
                            spec:
                              volumes:
                                - name: k8s
                                  hostPath:
                                    path: /mnt/gluster-gv0/k8s
                              containers:
                                - image: ubuntu
                                  imagePullPolicy: IfNotPresent
                                  name: oatpp
                                  volumeMounts:
                                    - name: k8s
                                      mountPath: /mnt
                                  command: ["/mnt/oatpp/start"]
		' > ~/oatpp.yml;

		echo '
			apiVersion: v1
			kind: Service
			metadata:
			  name: oatpp
			  namespace: winter
			spec:
			  selector:
			    app: oatpp
			  type: LoadBalancer
			  ports:
			    - nodePort: 30013
			      port: 8080
			      protocol: TCP
			      targetPort: 8080
			  sessionAffinity: ClientIP
		' > oatpp-port.yml;
		sed -i 's/\t/        /g' ~/oatpp.yml;
		sed -i 's/\t/        /g' ~/oatpp-port.yml;
		
		kubectl apply -f ~/oatpp.yml;
		kubectl apply -f ~/oatpp-port.yml;
	)


### 查看

	kubectl get pod,svc,node -n winter -o wide


### 测试

	curl http://192.168.1.22:30013/
	curl http://192.168.1.23:30013/
	curl http://192.168.1.24:30013/

	# 使用浏览器打开

	http://192.168.1.24:30013/
	http://192.168.1.23:30013/
	http://192.168.1.22:30013/


### 扩缩

	# 静态扩缩，指定 pod 数量，如 12

	kubectl scale --replicas=12 -f ~/oatpp.yml

	# 动态扩缩，指定最小和最大 pod 数量

	kubectl autoscale  --max=16 --min=4 -f ~/oatpp.yml
	


