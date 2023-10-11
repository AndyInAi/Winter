
### 安装

	apt install -y gdebi fonts-noto-cjk uuid

	if [ ! -f ~/google-chrome-beta.deb ]; then wget  https://dl.google.com/linux/direct/google-chrome-beta_current_amd64.deb -O ~/google-chrome-beta.deb; fi

	if [ -f ~/google-chrome-beta.deb ]; then gdebi -n ~/google-chrome-beta.deb ; fi

	NODES="192.168.1.91 192.168.1.92 192.168.1.93 192.168.1.94" # 4 个节点 IP 列表，对应主机名 chrome1 - chrome4

	no=0; for i in $NODES; do no=$(($no+1)) ; ip="$i " ;  host="$ip chrome$no" ;  if [ "`grep \"^$ip\" /etc/hosts`" == "" ]; then echo "$host" >> /etc/hosts; else sed -i "s/^$ip.*$/$host/g" /etc/hosts; fi ; done


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

	mount -a && mkdir -p /mnt/gluster-gv0/pdf/{task,lock,out,over} && mount

	
### 配置

	# 服务脚本 ~/tasks

	# 开始
	(echo "

		if [ ! \"\$1\" == \"b61bdb9c-4e85-4c68-a054-856caf6320e2\" ]; then exit 0; fi

		rm -f ~/tasks-kill

		dir=\"/mnt/gluster-gv0/pdf\"

		cmd=\"google-chrome --headless --no-sandbox --no-pdf-header-footer\"

		while true; do

			for i in \`ls -tr \${dir}/task\`; do

				if [ -f ~/tasks-kill ]; then rm -f ~/tasks-kill; exit 0; fi

				if [ ! -f \${dir}/lock/\${i} ] && [ ! -f \${dir}/out/\${i}.pdf ]; then

					touch \${dir}/lock/\${i}

					\$cmd --print-to-pdf=\"\${dir}/out/\${i}.pdf\" \"\`cat \${dir}/task/\${i} |tr -d [:cntrl:][:space:]\`\"

					mv -f \${dir}/task/\${i} \${dir}/over/\${i}

					rm -f \${dir}/lock/\${i}

					echo task \${i} done;

				fi
			done

			sleep 3

			if [ -f ~/tasks-kill ]; then rm -f ~/tasks-kill; exit 0; fi

		done

	" > ~/tasks; chmod +x ~/tasks; )
	# 结束

	# 启动服务脚本 ~/start-tasks

	# 开始
	(echo "

		pid=\"\`ps aux |grep 'b61bdb9c-4e85-4c68-a054-856caf6320e2' | grep -v grep | awk '{print \$2}'\`\"

		if [ ! \"\$pid\" == \"\" ]; then

			echo; echo Tasks PID \$pid is running ...; echo; exit 0

		fi

		nohup ~/tasks b61bdb9c-4e85-4c68-a054-856caf6320e2 > ~/tasks.log 2>&1 &

	" > ~/start-tasks; chmod +x ~/start-tasks; )
	# 结束

	# 停止服务脚本 ~/start-tasks

	# 开始
	(echo "

		pid=\"\`ps aux |grep 'b61bdb9c-4e85-4c68-a054-856caf6320e2' | grep -v grep | awk '{print \$2}'\`\"

		if [ ! \"\$pid\" == \"\" ]; then

			touch ~/tasks-kill

		fi

	" > ~/stop-tasks; chmod +x ~/stop-tasks; )
	# 结束


### 启动服务

	~/start-tasks


### 停止服务

	~/stop-tasks


### 测试

	echo "https://www.sohu.com/" > /mnt/gluster-gv0/pdf/task/`uuid`

	# 几秒钟后查看结果

	ll /mnt/gluster-gv0/pdf/out/


