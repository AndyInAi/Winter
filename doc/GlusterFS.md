
### 准备


#### 主机

	# 9 台主机；主机名 gfs1 - gfs9 对应 IP 地址 192.168.1.81 - 89

	# 每台主机配置：
		8 核以上 CPU
		16GB 以上内存 		
		1TB 以上企业级 NVME 硬盘安装系统
		8TB 以上企业级 NVME 硬盘或 16TB 以上企业级机械硬盘作为存储；如使用 RAID10 需要 4 块以上硬盘
		10Gb 以上网卡

	# 配置每台主机 /etc/hosts 
		
		(
			# 9 个节点 IP 列表，对应主机名 gfs1 - gfs9
			NODES="192.168.1.81 192.168.1.82 192.168.1.83 192.168.1.84 192.168.1.85 192.168.1.86 192.168.1.87 192.168.1.88 192.168.1.89"

			no=0; for i in $NODES; do no=$(($no+1)) ; ip="$i " ;  host="$ip gfs$no" ;  if [ "`grep \"^$ip\" /etc/hosts`" == "" ]; then echo "$host" >> /etc/hosts; else sed -i "s/^$ip.*$/$host/g" /etc/hosts; fi ; done
		)

主机可以为物理主机，也可以为云服务器，例如：[阿里云 ecs.c6.2xlarge](https://www.aliyun.com/product/ecs?source=5176.11533457&userCode=1gbajwso)


#### 存储

	# 所有节点执行

	# 以 sdb 为例，必须为新硬盘或已清除所有分区

	# 不要删除中间三个空行
	
	(echo 'g
		n
		
		
		
		w' > ~/fdisk.txt;
	sed -i "s/\t//g"  ~/fdisk.txt;
	fdisk /dev/sdb < ~/fdisk.txt;)

	mkfs.xfs -i size=512 /dev/sdb1

	mkdir -p /data/brick1
	
	if [ "`grep ^/dev/sdb1 /etc/fstab`" == "" ]; then echo '/dev/sdb1 /data/brick1 xfs defaults 1 2' >> /etc/fstab; fi
	
	mount -a && mount


#### 存储 (RAID10)

可选。RAID10 安装配置 https://github.com/AndyInAi/Winter/blob/main/doc/RAID10.md


### 安装

	# 所有节点执行

	apt install -y glusterfs-server tuned

	systemctl --now enable glusterd 
	
	service glusterd status

	systemctl enable --now tuned
	tuned-adm profile throughput-performance


### 配置
	
需要所有节点执行完成以上操作后继续

	# 所有节点执行

	_hostname=`hostname`; for ((i=1; i<=9; i++)) do if [ ! "$_hostname" == "gfs$i" ]; then gluster peer probe gfs$i; fi;  done; gluster pool list


	# 任一节点执行；每个文件块 3 个副本，随机分布在 9 个节点其中的 3 个

	gluster volume create gv0 replica 3 gfs1:/data/brick1/gv0 gfs2:/data/brick1/gv0 gfs3:/data/brick1/gv0 gfs4:/data/brick1/gv0 gfs5:/data/brick1/gv0 gfs6:/data/brick1/gv0 gfs7:/data/brick1/gv0 gfs8:/data/brick1/gv0 gfs9:/data/brick1/gv0 force

	gluster volume start gv0
	
	gluster volume info gv0


	# 所有节点执行

	(
		gluster volume set gv0 performance.cache-size 1GB && \
		gluster volume set gv0 client.event-threads 3 && \
		gluster volume set gv0 server.event-threads 3 && \
		gluster volume set gv0 performance.io-thread-count 24 && \
		gluster volume set gv0 server.outstanding-rpc-limit 96 && \
		gluster volume set gv0 config.global-threading on && \
		gluster volume set gv0 nl-cache-positive-entry on && \
		gluster volume set gv0 performance.iot-pass-through on && \
		gluster volume set gv0 performance.io-cache on && \
		gluster volume set gv0 performance.io-thread-count 8 && \
		gluster volume set gv0 performance.parallel-readdir on && \
		gluster volume set gv0 performance.qr-cache-timeout 600 && \
		gluster volume set gv0 performance.readdir-ahead on && \
		gluster volume set gv0 performance.write-behind-window-size 128MB
	)

### 挂载存储

	# 所有节点执行

	umount /mnt/gluster-gv0

	mkdir -p /mnt/gluster-gv0

	if [ "`grep ^localhost:/gv0 /etc/fstab`" == "" ]; then echo 'localhost:/gv0 /mnt/gluster-gv0 glusterfs defaults,_netdev 0 0' >> /etc/fstab; fi

	mount -a && mount


### 测试

	# 任一节点执行；复制 10 个文件到存储
	for i in `seq -w 1 10`; do cp  /var/log/dmesg /mnt/gluster-gv0/copy-test-$i; done

	# 任一节点查看结果

	ls -lA /mnt/gluster-gv0/copy*
	

### 负载均衡服务器安装配置 IP 192.168.1.80

	apt install haproxy -y

	(echo '
	listen winter_gfs_cluster

		mode tcp
		balance source
		bind 0.0.0.0:24007
		
		server gfs1 192.168.1.81:24007
		server gfs2 192.168.1.82:24007
		server gfs3 192.168.1.83:24007

	' > /etc/haproxy/haproxy.cfg;)

	systemctl --now enable haproxy

	systemctl status haproxy

	
### 客户端安装配置

	apt install -y glusterfs-client

	ip="192.168.1.80" # 负载均衡服务器；主机名 gfs

	host="$ip gfs" ;  if [ "`grep \"^$ip \" /etc/hosts`" == "" ]; then echo "$host" >> /etc/hosts; else sed -i "s/^$ip .*$/$host/g" /etc/hosts; fi

	NODES="192.168.1.81 192.168.1.82 192.168.1.83 192.168.1.84 192.168.1.85 192.168.1.86 192.168.1.87 192.168.1.88 192.168.1.89" # 9 个节点 IP 列表，对应主机名 gfs1 - gfs9

	no=0; for i in $NODES; do no=$(($no+1)) ; ip="$i " ;  host="$ip gfs$no" ;  if [ "`grep \"^$ip\" /etc/hosts`" == "" ]; then echo "$host" >> /etc/hosts; else sed -i "s/^$ip.*$/$host/g" /etc/hosts; fi ; done
	
	umount /mnt/gluster-gv0

	mkdir -p /mnt/gluster-gv0

	if [ "`grep ^gfs:/gv0 /etc/fstab`" == "" ]; then echo 'gfs:/gv0 /mnt/gluster-gv0 glusterfs defaults,_netdev 0 0' >> /etc/fstab; fi

	mount -a && mount


