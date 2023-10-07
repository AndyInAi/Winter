

### 安装配置

	# hostname: resin1 - resin4
	# ip: 192.168.1.181 - 192.168.1.184

	export DEBIAN_FRONTEND=noninteractive && apt install -y default-jdk gcc make libssl-dev

	if [ ! -f ~/resin-4.0.66.tar.gz ]; then wget -O ~/resin-4.0.66.tar.gz https://caucho.com/download/resin-4.0.66.tar.gz ; fi
	
	if [ ! -d ~/resin-4.0.66 ]; then tar --overwrite -xvzf ~/resin-4.0.66.tar.gz ; fi

	(
		if [ -d ~/resin-4.0.66 ]; then
			
			cd ~/resin-4.0.66 && ./configure && make && make install

			if [ "`grep ^web_admin_enable /etc/resin/resin.properties`" == "" ]; then echo "web_admin_enable : false" >> /etc/resin/resin.properties; else sed -i "s/^web_admin_enable.*$/web_admin_enable : false/g" /etc/resin/resin.properties; fi

			if [ "`grep ^resin_doc /etc/resin/resin.properties`" == "" ]; then echo "resin_doc : false" >> /etc/resin/resin.properties; else sed -i "s/^resin_doc.*$/resin_doc : false/g" /etc/resin/resin.properties; fi

			if [ "`grep ^app.http /etc/resin/resin.properties`" == "" ]; then echo "app.http : 80" >> /etc/resin/resin.properties; else sed -i "s/^app.http.*$/app.http : 80/g" /etc/resin/resin.properties; fi

			if [ "`grep ^web.http /etc/resin/resin.properties`" == "" ]; then echo "web.http : 80" >> /etc/resin/resin.properties; else sed -i "s/^web.http.*$/web.http : 80/g" /etc/resin/resin.properties; fi
		fi
	)
	
	update-rc.d -f resin defaults ; service resin start ; systemctl status resin

	NODES="192.168.1.181 192.168.1.182 192.168.1.183 192.168.1.184" # 4 个节点 IP 列表，对应主机名 resin1 - resin4

	no=0; for i in $NODES; do no=$(($no+1)) ; ip="$i " ;  host="$ip resin$no" ;  if [ "`grep \"^$ip\" /etc/hosts`" == "" ]; then echo "$host" >> /etc/hosts; else sed -i "s/^$ip.*$/$host/g" /etc/hosts; fi ; done
	

### 停止

	systemctl stop resin


### GlusterFS 集群客户端安装配置

	# 需要 GlusterFS 集群安装配置已经完成
	# <https://github.com/AndyInAi/Winter/blob/main/doc/GlusterFS%20%E9%9B%86%E7%BE%A4%E5%AE%89%E8%A3%85%E9%85%8D%E7%BD%AE.md>

	apt install -y glusterfs-client

	ip="192.168.1.80" # 负载均衡服务器；主机名 gfs

	host="$ip gfs" ;  if [ "`grep \"^$ip \" /etc/hosts`" == "" ]; then echo "$host" >> /etc/hosts; else sed -i "s/^$ip .*$/$host/g" /etc/hosts; fi

	NODES="192.168.1.81 192.168.1.82 192.168.1.83 192.168.1.84 192.168.1.85 192.168.1.86 192.168.1.87 192.168.1.88 192.168.1.89" # 9 个节点 IP 列表，对应主机名 gfs1 - gfs9

	no=0; for i in $NODES; do no=$(($no+1)) ; ip="$i " ;  host="$ip gfs$no" ;  if [ "`grep \"^$ip\" /etc/hosts`" == "" ]; then echo "$host" >> /etc/hosts; else sed -i "s/^$ip.*$/$host/g" /etc/hosts; fi ; done
	
	umount /mnt/gluster-gv0

	mkdir -p /mnt/gluster-gv0

	if [ "`grep ^gfs:/gv0 /etc/fstab`" == "" ]; then echo 'gfs:/gv0 /mnt/gluster-gv0 glusterfs defaults 0 0' >> /etc/fstab; fi

	mount -a && mount

	ln -s /mnt/gluster-gv0 /var/resin/webapps/ROOT/gfs


### 同步，所有节点完成以上步骤后执行

	# 从第一个节点 192.168.1.181 同步 /var/resin/webapps/ROOT 目录到全部集群节点，排除 GlusterFS 集群存储文件目录 gfs，以及临时目录

	for ((i=181; i<=184; i++)); do echo; echo sync 192.168.1.${i} ......; echo; rsync -avzh  --exclude=ROOT/gfs --exclude=ROOT/WEB-INF/tmp/ --exclude=ROOT/WEB-INF/work/ /var/resin/webapps/ROOT 192.168.1.${i}://var/resin/webapps/; echo; done


### 负载均衡节点安装配置 

	# hostname: resin  
	# ip: 192.168.1.180 

	apt install -y nginx

	if [ "`grep '#include \/etc\/nginx\/sites-enabled\/' /etc/nginx/nginx.conf`" == "" ]; then echo ok; sed -i "s/include \/etc\/nginx\/sites-enabled\//#include \/etc\/nginx\/sites-enabled\//g" /etc/nginx/nginx.conf; fi

	systemctl --now enable nginx

	(
		echo '
		    upstream resin {
			ip_hash;
			server 192.168.1.181;
			server 192.168.1.182;
			server 192.168.1.183;
			server 192.168.1.184;
		    }
		    server {
			listen 80;
			location / {
				proxy_pass http://resin;
				proxy_http_version	1.1;
				proxy_set_header	Host $host;
				proxy_set_header	X-Real-IP  $remote_addr;
				proxy_set_header	X-Forwarded-For $proxy_add_x_forwarded_for;
				proxy_set_header	Upgrade $http_upgrade;
				proxy_set_header	Connection "upgrade";
			}
		    }
		' > /etc/nginx/conf.d/resin.conf ; 
	)

	systemctl restart nginx

