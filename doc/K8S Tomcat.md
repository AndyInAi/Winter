 
### 条件

	# 安装时需要确保 K8S 集群正在运行，并且挂载了 GlusterFS 集群文件系统

	# 相关文档： K8S 集群安装配置.md、GlusterFS 集群安装配置.md

	# 在各个主机检查 GlusterFS 集群文件系统

	df -h /mnt/gluster-gv0

	# 正常输出

	Filesystem      Size  Used Avail Use% Mounted on
	gfs:/gv0         48T  835G   48T   2% /mnt/gluster-gv0


### 部署

	# 在 K8S 第一个节点，即 Master 节点 192.168.1.21 执行

	# 副本：16 个

	# 端口：30001
	
	(
		mkdir -p /mnt/gluster-gv0/k8s/webapps-1/ROOT;

		kubectl get namespace winter > /dev/null 2>&1 || kubectl create namespace winter;

		echo '
                        apiVersion: apps/v1
                        kind: Deployment
                        metadata:
                          labels:
                            app: tomcat
                          name: tomcat
                          namespace: winter
                        spec:
                          replicas: 16
                          selector:
                            matchLabels:
                              app: tomcat
                          template:
                            metadata:
                              labels:
                                app: tomcat
                            spec:
                              volumes:
                                - name: k8s
                                  hostPath:
                                    path: /mnt/gluster-gv0/k8s
                                - name: webapps-1
                                  hostPath:
                                    path: /mnt/gluster-gv0/k8s/webapps-1
                              containers:
                                - image: tomcat
                                  imagePullPolicy: IfNotPresent
                                  name: tomcat
                                  volumeMounts:
                                    - name: k8s
                                      mountPath: /mnt
                                    - name: webapps-1
                                      mountPath: /usr/local/tomcat/webapps
		' > ~/tomcat.yml;

		echo '
			apiVersion: v1
			kind: Service
			metadata:
			  name: tomcat
			  namespace: winter
			spec:
			  selector:
			    app: tomcat
			  type: LoadBalancer
			  ports:
			    - nodePort: 30001
			      port: 8080
			      protocol: TCP
			      targetPort: 8080
			  sessionAffinity: ClientIP
		' > tomcat-port.yml;
		sed -i 's/\t/        /g' ~/tomcat.yml;
		sed -i 's/\t/        /g' ~/tomcat-port.yml;
		
		kubectl apply -f ~/tomcat.yml;
		kubectl apply -f ~/tomcat-port.yml;
	)


### 查看

	kubectl get pod,svc,node -n winter -o wide


### 测试
	
	echo "<h1>Hello Tomcat <%= new java.util.Date() %></h1>" > /mnt/gluster-gv0/k8s/webapps-1/ROOT/hello.jsp

	curl http://192.168.1.22:30001/hello.jsp
	curl http://192.168.1.23:30001/hello.jsp
	curl http://192.168.1.24:30001/hello.jsp

	# 使用浏览器打开

	http://192.168.1.24:30001/hello.jsp
	http://192.168.1.23:30001/hello.jsp
	http://192.168.1.22:30001/hello.jsp


### 扩缩

	# 静态扩缩，指定 pod 数量，如 24

	kubectl scale --replicas=24 -f ~/tomcat.yml

	# 动态扩缩，指定最小和最大 pod 数量

	kubectl autoscale  --max=32 --min=16 -f ~/tomcat.yml
	


