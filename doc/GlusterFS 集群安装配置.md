

### 准备硬盘

#### 以 sdb 为例，必须为新硬盘或已清除所有分区

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


### 安装

	apt install -y glusterfs-server


### 启动

	systemctl --now enable glusterd ; service glusterd status


### 配置

	NODES="192.168.1.81 192.168.1.82 192.168.1.83 192.168.1.84" # 4 个节点 IP 列表，对应主机名 gfs1 - gfs4

	no=0; for i in $NODES; do no=$(($no+1)) ; ip="$i " ;  host="$ip gfs$no" ;  if [ "`grep \"^$ip\" /etc/hosts`" == "" ]; then echo "$host" >> /etc/hosts; else sed -i "s/^$ip.*$/$host/g" /etc/hosts; fi ; done


### 配置，需要所有节点执行完成以上操作后继续

	# 所有节点执行
	_hostname=`hostname`; for ((i=1; i<=4; i++)) do if [ ! "$_hostname" == "gfs$i" ]; then gluster peer probe gfs$i; fi;  done; gluster pool list

	# 任一节点执行
	gluster volume create gv0 replica 4 gfs1:/data/brick1/gv0 gfs2:/data/brick1/gv0 gfs3:/data/brick1/gv0 gfs4:/data/brick1/gv0 force

	gluster volume set gv0 performance.cache-size 1GB

### 启动存储

	gluster volume start gv0 ; gluster volume info gv0


### 测试

	# 任一节点执行
	
	apt install -y glusterfs-server

	mkdir -p /mnt/gluster-gv0

	mount -t glusterfs gfs1:/gv0 /mnt/gluster-gv0
	
	# 复制 10 个文件到存储
	for i in `seq -w 1 10`; do cp  /var/log/dmesg /mnt/gluster-gv0/copy-test-$i; done

	# 查看结果

	ls -lA /mnt/gluster-gv0/copy*

	# 在其它节点查看结果

	ls -lA /data/brick1/gv0/copy*
	

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
		server gfs4 192.168.1.84:24007

	' > /etc/haproxy/haproxy.cfg;)

	systemctl --now enable haproxy

	systemctl status haproxy

	
### 客户端安装配置

	apt install -y glusterfs-client

	ip="192.168.1.80" # 负载均衡服务器；主机名 gfs

	host="$ip gfs" ;  if [ "`grep \"^$ip \" /etc/hosts`" == "" ]; then echo "$host" >> /etc/hosts; else sed -i "s/^$ip .*$/$host/g" /etc/hosts; fi
	
	mkdir -p /mnt/gluster-gv0

	mount -t glusterfs gfs1:/gv0 /mnt/gluster-gv0




