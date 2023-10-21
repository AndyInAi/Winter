

### 安装

	apt install -y net-tools

	if [ ! -f ~/prometheus-2.47.2.linux-amd64.tar.gz ]; then wget -O ~/prometheus-2.47.2.linux-amd64.tar.gz https://mirrors.tuna.tsinghua.edu.cn/github-release/prometheus/prometheus/LatestRelease/prometheus-2.47.2.linux-amd64.tar.gz; fi

	if [ ! -d ~/prometheus-2.47.2.linux-amd64 ]; then tar -xvzf ~/prometheus-2.47.2.linux-amd64.tar.gz; fi

	if [ ! -d ~/p8s ]; then ln -s ~/prometheus-2.47.2.linux-amd64 ~/p8s; fi


### 配置

	# 开始
	(
		NODES="192.168.1.211 192.168.1.212 192.168.1.213 192.168.1.214"; NAME="p8s" # 4 个节点 IP 列表，对应主机名 p8s1 - p8s4
		no=0; for i in $NODES; do no=$(($no+1)) ; ip="$i " ;  host="$ip ${NAME}$no" ;  if [ "`grep \"^$ip\" /etc/hosts`" == "" ]; then echo "$host" >> /etc/hosts; else sed -i "s/^$ip.*$/$host/g" /etc/hosts; fi ; done
	)
	# 结束

	(cd ~ ; ip=`ifconfig |grep inet |grep -v inet6 |grep -v 127.0.0.1 |awk '{print $2}' |head -n 1`; echo $ip; sed -i "s/\".*:9090\"/\"$ip:9090\"/g" ./p8s/prometheus.yml)

	# 生成启动脚本 ~/start-p8s
	# 开始
	(
		echo "
			
			pid=\"\`ps aux | grep prometheus | grep -v grep | awk '{print \$2}'\`\"

			if [ ! \"\${pid}\" == \"\" ]; then echo; echo \"Prometheus PID \${pid} is running ...\"; echo; exit 0; fi

			(
				cd ~ ;
				./p8s/prometheus \\
				--config.file=./p8s/prometheus.yml \\
				--storage.tsdb.no-lockfile \\
				--storage.remote.read-concurrent-limit=0 \\
				--storage.remote.read-max-bytes-in-frame=1048576 \\
				--rules.alert.resend-delay=1m \\
				--alertmanager.notification-queue-capacity=10000 \\
				--query.lookback-delta=5m \\
				--query.timeout=2m \\
				--query.max-concurrency=200 \\
				--query.max-samples=50000000 \\
				--enable-feature=memory-snapshot-on-shutdown \\
				--web.enable-admin-api \\
				> ~/p8s.log 2>&1 &
			)

		" > ~/start-p8s;

		chmod +x ~/start-p8s;
	)
	# 结束

	# 生成停止脚本 ~/stop-p8s
	# 开始
	(
		echo "
			
			pid=\"\`ps aux | grep prometheus | grep -v grep | awk '{print \$2}'\`\"

			if [ ! \"\${pid}\" == \"\" ]; then kill -9 \${pid}; echo; echo \"Prometheus PID \${pid} stoped\"; echo; exit 0; fi

		" > ~/stop-p8s;
		chmod +x ~/stop-p8s;
	)
	# 结束


### 启动

	~/start-p8s

	# 查看启动信息

	cat ~/p8s.log


### 停止

	~/stop-p8s


### 负载均衡节点安装配置
	
	# 实现高可用高并发高流量热备要求

	# hostname: p8s
	# ip: 192.168.1.210

	apt install -y nginx

	if [ "`grep '#include \/etc\/nginx\/sites-enabled\/' /etc/nginx/nginx.conf`" == "" ]; then echo ok; sed -i "s/include \/etc\/nginx\/sites-enabled\//#include \/etc\/nginx\/sites-enabled\//g" /etc/nginx/nginx.conf; fi

	systemctl --now enable nginx

	# 开始
	(
		echo '
		    upstream p8s {
			ip_hash;
			server 192.168.1.211:9090;
			server 192.168.1.212:9090;
			server 192.168.1.213:9090;
			server 192.168.1.214:9090;
		    }
		    server {
			listen 80;
			location / {
				proxy_pass http://p8s;
				proxy_http_version	1.1;
				proxy_set_header	Host $host;
				proxy_set_header	X-Real-IP  $remote_addr;
				proxy_set_header	X-Forwarded-For $proxy_add_x_forwarded_for;
				proxy_set_header	Upgrade $http_upgrade;
				proxy_set_header	Connection "upgrade";
			}
		    }
		' > /etc/nginx/conf.d/p8s.conf ; 
	)
	# 结束

	systemctl restart nginx


### MariaDB 监控配置

	(
		cd ~;
		if [ "`grep '\- job_name: \"mysql\"' ./p8s/prometheus.yml`" == "" ]; then
			echo '
				  - job_name: "mysql"
				    static_configs:
				      - targets: ["192.168.1.201:9104"]
				      - targets: ["192.168.1.202:9104"]
				      - targets: ["192.168.1.203:9104"]
				      - targets: ["192.168.1.204:9104"]
			'>> ./p8s/prometheus.yml;
			sed -i "s/^\t*//g" ./p8s/prometheus.yml;
		fi
		~/stop-p8s; ~/start-p8s;
	)

	# 截图

![image](https://github.com/AndyInAi/Winter/blob/main/img/p8s/p8s-1.png)


### Redis 监控配置

	(
		cd ~;
		if [ "`grep '\- job_name: \"redis\"' ./p8s/prometheus.yml`" == "" ]; then
			echo '
				  - job_name: "redis"
				    static_configs:
				      - targets: ["192.168.1.241:9121"]
				      - targets: ["192.168.1.242:9121"]
				      - targets: ["192.168.1.243:9121"]
				      - targets: ["192.168.1.244:9121"]
				      - targets: ["192.168.1.245:9121"]
				      - targets: ["192.168.1.246:9121"]
				      - targets: ["192.168.1.247:9121"]
				      - targets: ["192.168.1.248:9121"]
				      - targets: ["192.168.1.249:9121"]
			'>> ./p8s/prometheus.yml;
			sed -i "s/^\t*//g" ./p8s/prometheus.yml;
		fi
		~/stop-p8s; ~/start-p8s;
	)

	# 截图

![image](https://github.com/AndyInAi/Winter/blob/main/img/p8s/p8s-2.png)


### Resin 监控配置

	(
		~/stop-p8s;
		cd ~;
		if [ "`grep '\- job_name: \"resin\"' ./p8s/prometheus.yml`" == "" ]; then
			echo '
			  - job_name: "resin"
			    metrics_path: /probe
			    params:
			      module: [http_2xx]
			    static_configs:
			      - targets:
			        - http://192.168.1.181
			        - http://192.168.1.182
			        - http://192.168.1.183
			        - http://192.168.1.184
			    relabel_configs:
			      - source_labels: [__address__]
			        target_label: __param_target
			      - source_labels: [__param_target]
			        target_label: instance
			      - target_label: __address__
			        replacement: 192.168.1.181:9115
			'>> ./p8s/prometheus.yml;
			sed -i "s/^\t*//g" ./p8s/prometheus.yml;
		fi
		~/start-p8s;
	)

	# 截图

![image](https://github.com/AndyInAi/Winter/blob/main/img/p8s/p8s-3.png)


#### 重启

	~/stop-p8s; ~/start-p8s 


