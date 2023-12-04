 
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

	# 准备 Ntex 应用，以 Winter-ntex 为例，复制到 /mnt/gluster-gv0/k8s/ntex 目录下

	# 如果访问 GitHub.com 需要代理在此设置
	# export http_proxy=http://192.168.1.109:3128/
	# export https_proxy=http://192.168.1.109:3128/

	(
		if [ ! -d ~/Winter-ntex ]; then cd ~/ ; git clone https://github.com/AndyInAi/Winter-ntex.git; fi ;

		mkdir -p /mnt/gluster-gv0/k8s/ntex ;

		cp -f -r ~/Winter-ntex /mnt/gluster-gv0/k8s/ntex/ ;
	)


### 配置

	# 生成启动脚本

	(
		echo '#!/bin/bash

			mkdir -p /mnt/gluster-gv0/k8s/ntex

			cp -f -r /mnt/ntex/Winter-ntex ~/

			cd ~/Winter-ntex && chmod +x ./Winter-ntex && ./Winter-ntex
			
			sleep 88888

		'> /mnt/gluster-gv0/k8s/ntex/start 
		
		chmod +x /mnt/gluster-gv0/k8s/ntex/start
	)


### 部署

	# 副本：8 个

	# 端口：30016
	
	(
		mkdir -p /mnt/gluster-gv0/k8s/ntex;

		kubectl get namespace winter > /dev/null 2>&1 || kubectl create namespace winter;

		echo '
                        apiVersion: apps/v1
                        kind: Deployment
                        metadata:
                          labels:
                            app: ntex
                          name: ntex
                          namespace: winter
                        spec:
                          replicas: 1
                          selector:
                            matchLabels:
                              app: ntex
                          template:
                            metadata:
                              labels:
                                app: ntex
                            spec:
                              volumes:
                                - name: k8s
                                  hostPath:
                                    path: /mnt/gluster-gv0/k8s
                              containers:
                                - image: ubuntu
                                  imagePullPolicy: IfNotPresent
                                  name: ntex
                                  volumeMounts:
                                    - name: k8s
                                      mountPath: /mnt
                                  command: ["/mnt/ntex/start"]
		' > ~/ntex.yml;

		echo '
			apiVersion: v1
			kind: Service
			metadata:
			  name: ntex
			  namespace: winter
			spec:
			  selector:
			    app: ntex
			  type: LoadBalancer
			  ports:
			    - nodePort: 30016
			      port: 8080
			      protocol: TCP
			      targetPort: 8080
			  sessionAffinity: ClientIP
		' > ntex-port.yml;
		sed -i 's/\t/        /g' ~/ntex.yml;
		sed -i 's/\t/        /g' ~/ntex-port.yml;
		
		kubectl apply -f ~/ntex.yml;
		kubectl apply -f ~/ntex-port.yml;
	)


### 查看

	kubectl get pod,svc,node -n winter -o wide


### 测试

	curl http://192.168.1.22:30016/
	curl http://192.168.1.23:30016/
	curl http://192.168.1.24:30016/

	# 使用浏览器打开

	http://192.168.1.24:30016/
	http://192.168.1.23:30016/
	http://192.168.1.22:30016/


### 扩缩

	# 静态扩缩，指定 pod 数量，如 12

	kubectl scale --replicas=12 -f ~/ntex.yml

	# 动态扩缩，指定最小和最大 pod 数量

	kubectl autoscale  --max=16 --min=4 -f ~/ntex.yml
	


