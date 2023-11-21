 
### 条件

	# 安装时需要确保 K8S 集群正在运行，并且挂载了 GlusterFS 集群文件系统

	# 相关文档： K8S 集群安装配置.md、GlusterFS 集群安装配置.md

	# 在各个主机检查 GlusterFS 集群文件系统

	df -h /mnt/gluster-gv0

	# 正常输出

	Filesystem      Size  Used Avail Use% Mounted on
	gfs:/gv0         48T  835G   48T   2% /mnt/gluster-gv0


#### 以下操作均在 K8S 第一个节点，即 Master 节点 192.168.1.21 执行


### 安装

	(export DEBIAN_FRONTEND=noninteractive && apt update -y && apt install -y xz-utils uuid)

	mkdir -p /mnt/gluster-gv0/k8s/mp4/{upload,task,lock,out,over}

	(if [ ! -f ~/ffmpeg-release-amd64-static.tar.xz ]; then wget -O ~/ffmpeg-release-amd64-static.tar.xz https://johnvansickle.com/ffmpeg/releases/ffmpeg-release-amd64-static.tar.xz ; fi)
	
	(if [ ! -d ~/ffmpeg-6.0-amd64-static ]; then xz -d -k -c  ~/ffmpeg-release-amd64-static.tar.xz | tar --overwrite -xvf - ; fi)

	(if [ -f ~/ffmpeg-6.0-amd64-static/ffmpeg ] ; then cp -f ~/ffmpeg-6.0-amd64-static/ffmpeg /mnt/gluster-gv0/k8s/mp4/ ; cp -f ~/ffmpeg-6.0-amd64-static/ffprobe /mnt/gluster-gv0/k8s/mp4/ ; cp -f ~/ffmpeg-6.0-amd64-static/qt-faststart /mnt/gluster-gv0/k8s/mp4/ ; fi)


### 配置


	# 生成服务脚本 /mnt/gluster-gv0/k8s/mp4/tasks

	(
		mkdir -p /mnt/gluster-gv0/k8s/mp4/{upload,task,lock,out,over}

		echo '#!/bin/bash

			if [ ! "$1" == "16a20c49-7501-47c8-8352-ae2aec19ea8d" ]; then exit 0; fi

			dir="/mnt/mp4"

			cmd="/mnt/mp4/ffmpeg -y -v 0  -i"

			opts="-c:v libx264 -c:a aac -b:a 320K"

			while true; do

				for i in `ls -tr ${dir}/task`; do

					if [ ! -f ${dir}/lock/${i} ] && [ ! -f ${dir}/out/${i}.mp4 ]; then

						touch ${dir}/lock/${i}

						$cmd ${dir}/task/${i}  ${opts} /tmp/${i}.mp4
						
						if [ -f /tmp/${i}.mp4 ]; then 

							/mnt/mp4/ffmpeg -y -v 0 -i /tmp/${i}.mp4  -t 10 -c copy /tmp/${i}-review.mp4

							/mnt/mp4/ffmpeg -y -v 0 -i /tmp/${i}-review.mp4  -t 9 -vf fps=1 /tmp/${i}-%d.png

							mv -f /tmp/${i}* ${dir}/out/

						fi

						mv -f ${dir}/task/${i} ${dir}/over/${i}

						rm -f ${dir}/lock/${i}

						echo task ${i} done;

					fi

				done

				sleep 3

			done

		' > /mnt/gluster-gv0/k8s/mp4/tasks;

		chmod +x /mnt/gluster-gv0/k8s/mp4/tasks; 
	)

	# 启动服务脚本 /mnt/gluster-gv0/k8s/mp4/start-tasks

	(
		echo '#!/bin/bash

			nohup /mnt/mp4/tasks 16a20c49-7501-47c8-8352-ae2aec19ea8d > /root/tasks.log 2>&1

		' > /mnt/gluster-gv0/k8s/mp4/start-tasks; 

		chmod +x /mnt/gluster-gv0/k8s/mp4/start-tasks; 
	)


### 部署

	# 在 K8S 第一个节点，即 Master 节点 192.168.1.21 执行

	# 副本：16 个
	
	(
		mkdir -p /mnt/gluster-gv0/k8s/mp4;

		chmod -R 777 /mnt/gluster-gv0/k8s/mp4;

		kubectl get namespace winter > /dev/null 2>&1 || kubectl create namespace winter;

		echo '
                        apiVersion: apps/v1
                        kind: Deployment
                        metadata:
                          labels:
                            app: mp4
                          name: mp4
                          namespace: winter
                        spec:
                          replicas: 16
                          selector:
                            matchLabels:
                              app: mp4
                          template:
                            metadata:
                              labels:
                                app: mp4
                            spec:
                              volumes:
                                - name: k8s
                                  hostPath:
                                    path: /mnt/gluster-gv0/k8s
                              containers:
                                - image: ubuntu
                                  imagePullPolicy: IfNotPresent
                                  name: mp4
                                  volumeMounts:
                                        - name: k8s
                                          mountPath: /mnt
                                  command: ["/mnt/mp4/start-tasks"]
		' > ~/mp4.yml;

		sed -i 's/\t/        /g' ~/mp4.yml;
	)

	kubectl apply -f ~/mp4.yml;


### 查看

	kubectl get pod,svc,node -n winter -o wide


### 测试

	# 复制或上传一个视频文件 test.mkv 到目录 /mnt/gluster-gv0/k8s/mp4/upload/ 后执行

	(cd /mnt/gluster-gv0/k8s/mp4/task && ln -s ../upload/test.mkv `uuid`)

	# 根据源视频文件大小，需要几十秒钟至几分钟完成，查看结果

	ll -t /mnt/gluster-gv0/k8s/mp4/out/

	# 输出例子，包括一个标准 mp4 文件、一个 10 秒 mp4 文件、# 最多 9 个 png 图片

	48aa87b8-6921-4e75-869d-7db2868384f3.mp4

	48aa87b8-6921-4e75-869d-7db2868384f3-review.mp4

	48aa87b8-6921-4e75-869d-7db2868384f3-1.png
	48aa87b8-6921-4e75-869d-7db2868384f3-2.png
	48aa87b8-6921-4e75-869d-7db2868384f3-3.png
	48aa87b8-6921-4e75-869d-7db2868384f3-4.png
	48aa87b8-6921-4e75-869d-7db2868384f3-5.png
	48aa87b8-6921-4e75-869d-7db2868384f3-6.png
	48aa87b8-6921-4e75-869d-7db2868384f3-7.png
	48aa87b8-6921-4e75-869d-7db2868384f3-8.png
	48aa87b8-6921-4e75-869d-7db2868384f3-9.png


### 扩缩

	# 静态扩缩，指定 pod 数量，如 24

	kubectl scale --replicas=24 -f ~/mp4.yml

	# 动态扩缩，指定最小和最大 pod 数量

	kubectl autoscale  --max=32 --min=16 -f ~/mp4.yml
	


