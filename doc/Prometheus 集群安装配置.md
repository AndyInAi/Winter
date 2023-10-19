

### 安装

	apt install -y net-tools

	if [ ! -f ~/prometheus-2.47.2.linux-amd64.tar.gz ]; then wget -O ~/prometheus-2.47.2.linux-amd64.tar.gz https://mirrors.tuna.tsinghua.edu.cn/github-release/prometheus/prometheus/LatestRelease/prometheus-2.47.2.linux-amd64.tar.gz; fi

	if [ ! -d ~/prometheus-2.47.2.linux-amd64 ]; then tar -xvzf ~/prometheus-2.47.2.linux-amd64.tar.gz; fi

	if [ ! -d ~/p8s ]; then ln -s ~/prometheus-2.47.2.linux-amd64 ~/p8s; fi


### 配置

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


### MariaDB 监控配置

	# 开始
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
	)
	# 结束


#### 重启

	~/stop-p8s; ~/start-p8s 

### 结果

![image](https://github.com/AndyInAi/Winter/blob/main/img/p8s/p8s-mariadb.png)


### 未完待续

