
### 条件

	# 安装时需要确保 K8S 集群正在运行，并且挂载了 GlusterFS 集群文件系统

	# 相关文档： K8S 集群安装配置.md、GlusterFS 集群安装配置.md

	# 在各个主机检查 GlusterFS 集群文件系统

	df -h /mnt/gluster-gv0

	# 正常输出

	Filesystem      Size  Used Avail Use% Mounted on
	gfs:/gv0         48T  835G   48T   2% /mnt/gluster-gv0


### 配置

	# 在 K8S 第一个节点，即 Master 节点 192.168.1.21 执行

	# 生成服务脚本 /mnt/gluster-gv0/k8s/pdf/tasks

	(
		mkdir -p /mnt/gluster-gv0/k8s/pdf/{task,lock,out,over}
	
		echo '#!/bin/bash

			dir="/home/blessuser/Downloads"

			cmd="google-chrome --headless --no-sandbox --no-pdf-header-footer"

			while true; do

				for i in `ls -tr ${dir}/task`; do

					if [ ! -f ${dir}/lock/${i} ] && [ ! -f ${dir}/out/${i}.pdf ]; then

						touch ${dir}/lock/${i}

						$cmd --print-to-pdf="${dir}/out/${i}.pdf" "`cat ${dir}/task/${i} |tr -d [:cntrl:][:space:]`"

						mv -f ${dir}/task/${i} ${dir}/over/${i}

						rm -f ${dir}/lock/${i}

						echo task ${i} done;

					fi
				done

				sleep 3

			done

		' > /mnt/gluster-gv0/k8s/pdf/tasks;
		
		chmod +x /mnt/gluster-gv0/k8s/pdf/tasks; 
	)

	# 生成启动服务脚本 /mnt/gluster-gv0/k8s/pdf/start-tasks

	(
		echo '#!/bin/bash

			nohup /home/blessuser/Downloads/tasks a3b8fe74-78ae-11ee-a7e4-00155d010b84 > /home/blessuser/tasks.log 2>&1 &

		' > /mnt/gluster-gv0/k8s/pdf/start-tasks; 
		
		chmod +x /mnt/gluster-gv0/k8s/pdf/start-tasks;
	)


### 部署

	# 在 K8S 第一个节点，即 Master 节点 192.168.1.21 执行

	# 副本：16 个
	
	(
		mkdir -p /mnt/gluster-gv0/k8s/pdf;

		chmod -R 777 /mnt/gluster-gv0/k8s/pdf;

		kubectl get namespace winter || kubectl create namespace winter;

		echo '
			apiVersion: apps/v1
			kind: Deployment
			metadata:
			  labels:
			    app: pdf
			  name: pdf
			  namespace: winter
			spec:
			  replicas: 16
			  selector:
			    matchLabels:
			      app: pdf
			  template:
			    metadata:
			      labels:
				app: pdf
			    spec:
			      volumes:
				- name: pdf
				  hostPath:
				    path: /mnt/gluster-gv0/k8s/pdf
			      containers:
				- image: browserless/chrome
				  imagePullPolicy: IfNotPresent
				  name: pdf
				  volumeMounts:
					- name: pdf
					  mountPath: /home/blessuser/Downloads
				  lifecycle:
					postStart:
					  exec:
					    command: ["/home/blessuser/Downloads/start-tasks"]
		' > ~/pdf.yml;

		sed -i 's/\t/        /g' ~/pdf.yml;
	)

	kubectl apply -f ~/pdf.yml;


### 查看

	kubectl get pod,svc,node -n winter -o wide


### 测试

	apt install -y uuid;

q	_uuid=`uuid -v 4`; 

	echo https://www.zhipin.com/ > /mnt/gluster-gv0/k8s/pdf/task/$_uuid

	# 几秒后查看结果

	echo; ll -h /mnt/gluster-gv0/k8s/pdf/out/$_uuid.pdf; file /mnt/gluster-gv0/k8s/pdf/out/$_uuid.pdf; echo


### 扩缩

	# 静态扩缩，指定 pod 数量，如 24

	kubectl scale --replicas=24 -f ~/pdf.yml

	# 动态扩缩，指定最小和最大 pod 数量

	kubectl autoscale  --max=32 --min=16 -f ~/pdf.yml
	


