
### 安装

	(export DEBIAN_FRONTEND=noninteractive && apt update -y && apt install -y xz-utils uuid)

	(if [ ! -f ~/ffmpeg-release-amd64-static.tar.xz ]; then wget -O ~/ffmpeg-release-amd64-static.tar.xz https://johnvansickle.com/ffmpeg/releases/ffmpeg-release-amd64-static.tar.xz ; fi)
	
	(if [ ! -d ~/ffmpeg-6.0-amd64-static ]; then xz -d -k -c  ~/ffmpeg-release-amd64-static.tar.xz | tar --overwrite -xvf - ; fi)

	(if [ -f ~/ffmpeg-6.0-amd64-static/ffmpeg ] ; then cp -f ~/ffmpeg-6.0-amd64-static/ffmpeg /usr/bin/ ; cp -f ~/ffmpeg-6.0-amd64-static/ffprobe /usr/bin/ ; cp -f ~/ffmpeg-6.0-amd64-static/qt-faststart /usr/bin/ ; fi)

	(
		NODES="192.168.1.171 192.168.1.172 192.168.1.173 192.168.1.174" # 4 个节点 IP 列表，对应主机名 ffmpeg1 - ffmpeg4

		no=0; for i in $NODES; do no=$(($no+1)) ; ip="$i " ;  host="$ip ffmpeg$no" ;  if [ "`grep \"^$ip\" /etc/hosts`" == "" ]; then echo "$host" >> /etc/hosts; else sed -i "s/^$ip.*$/$host/g" /etc/hosts; fi ; done
	)


### GlusterFS 集群客户端安装配置

	# 需要 GlusterFS 集群安装配置已经完成
	# https://github.com/AndyInAi/Winter/blob/main/doc/GlusterFS%20%E9%9B%86%E7%BE%A4%E5%AE%89%E8%A3%85%E9%85%8D%E7%BD%AE.md

	apt install -y glusterfs-client

	ip="192.168.1.80" # 负载均衡服务器；主机名 gfs

	host="$ip gfs" ;  if [ "`grep \"^$ip \" /etc/hosts`" == "" ]; then echo "$host" >> /etc/hosts; else sed -i "s/^$ip .*$/$host/g" /etc/hosts; fi

	NODES="192.168.1.81 192.168.1.82 192.168.1.83 192.168.1.84 192.168.1.85 192.168.1.86 192.168.1.87 192.168.1.88 192.168.1.89" # 9 个节点 IP 列表，对应主机名 gfs1 - gfs9

	no=0; for i in $NODES; do no=$(($no+1)) ; ip="$i " ;  host="$ip gfs$no" ;  if [ "`grep \"^$ip\" /etc/hosts`" == "" ]; then echo "$host" >> /etc/hosts; else sed -i "s/^$ip.*$/$host/g" /etc/hosts; fi ; done
	
	umount /mnt/gluster-gv0

	mkdir -p /mnt/gluster-gv0

	if [ "`grep ^gfs:/gv0 /etc/fstab`" == "" ]; then echo 'gfs:/gv0 /mnt/gluster-gv0 glusterfs defaults,_netdev 0 0' >> /etc/fstab; fi

	mount -a && mkdir -p /mnt/gluster-gv0/mp4/{upload,task,lock,out,over} && mount


### 配置

	# 服务脚本 ~/tasks

	# 开始
	(echo "

		if [ ! \"\$1\" == \"9f954200-69c0-11ee-8de6-00155d010b78\" ]; then exit 0; fi

		rm -f ~/tasks-kill

		dir=\"/mnt/gluster-gv0/mp4\"

		cmd=\"ffmpeg -y  -i\"

		opts=\"-c:v libx264 -fpsmax 5M -c:a aac -b:a 384K\"

		while true; do

			for i in \`ls -tr \${dir}/task\`; do

				if [ -f ~/tasks-kill ]; then rm -f ~/tasks-kill; exit 0; fi

				if [ ! -f \${dir}/lock/\${i} ] && [ ! -f \${dir}/out/\${i}.mp4 ]; then

					touch \${dir}/lock/\${i}

					\$cmd \${dir}/task/\${i}  \${opts} /dev/shm/\${i}.mp4
					
					if [ -f /dev/shm/\${i}.mp4 ]; then 

						mv -f /dev/shm/\${i}.mp4 \${dir}/out/

					fi

					mv -f \${dir}/task/\${i} \${dir}/over/\${i}

					rm -f \${dir}/lock/\${i}

					echo task \${i} done;

				fi
			done

			sleep 2

			if [ -f ~/tasks-kill ]; then rm -f ~/tasks-kill; exit 0; fi

		done

	" > ~/tasks; sed -i '1 i #!/bin/bash' ~/tasks; chmod +x ~/tasks; )
	# 结束

	# 启动服务脚本 ~/start-tasks

	# 开始
	(echo "

		pid=\"\`ps aux |grep '9f954200-69c0-11ee-8de6-00155d010b78' | grep -v grep | awk '{print \$2}'\`\"

		if [ ! \"\$pid\" == \"\" ]; then

			echo; echo Tasks PID \$pid is running ...; echo; exit 0

		fi

		nohup ~/tasks 9f954200-69c0-11ee-8de6-00155d010b78 > ~/tasks.log 2>&1 &

	" > ~/start-tasks; sed -i '1 i #!/bin/bash' ~/start-tasks; chmod +x ~/start-tasks; )
	# 结束

	# 停止服务脚本 ~/start-tasks

	# 开始
	(echo "

		pid=\"\`ps aux |grep '9f954200-69c0-11ee-8de6-00155d010b78' | grep -v grep | awk '{print \$2}'\`\"

		if [ ! \"\$pid\" == \"\" ]; then

			touch ~/tasks-kill

		fi

	" > ~/stop-tasks; sed -i '1 i #!/bin/bash' ~/stop-tasks; chmod +x ~/stop-tasks; )
	# 结束


### 启动服务

	~/start-tasks


### 停止服务

	~/stop-tasks


### 测试

	# 准备一个视频文件 test.mkv 放在目录 /mnt/gluster-gv0/mp4/upload/ 后执行

	ln -s /mnt/gluster-gv0/mp4/upload/test.mkv /mnt/gluster-gv0/mp4/task/`uuid -v4`

	# 几十秒钟或几分钟后查看结果，正常情况下会生成一个 mp4 文件

	ll -t /mnt/gluster-gv0/mp4/out/


