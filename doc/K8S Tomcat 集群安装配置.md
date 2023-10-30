
### 条件

	# 需要 K8S 集群准备就绪正在运行

	<https://github.com/AndyInAi/Winter/blob/main/doc/K8S%20%E9%9B%86%E7%BE%A4%E5%AE%89%E8%A3%85%E9%85%8D%E7%BD%AE.md>


### 部署

	# 在 K8S 第一个节点，即 Master 节点 192.168.1.21 执行

	# 副本：16 个

	# 端口：30001
	
	mkdir -p /mnt/gluster-gv0/k8s/webapps-1/ROOT

	(
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
				- name: webapps-1
				  hostPath:
				    path: /mnt/gluster-gv0/k8s/webapps-1
			      containers:
				- image: tomcat
				  imagePullPolicy: Always
				  name: tomcat
				  volumeMounts:
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

