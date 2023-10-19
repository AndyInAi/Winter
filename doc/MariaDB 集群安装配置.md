

### 安装 

	# 每个节点执行

	apt install -y mariadb-server mariadb-backup


### 配置 

	# 每个节点执行

	# 开始
	(
		echo '
			[mysqld]
			binlog_format=ROW
			default-storage-engine=innodb
			innodb_autoinc_lock_mode=2
			bind-address=0.0.0.0
			wsrep_on=ON
			wsrep_provider=/usr/lib/galera/libgalera_smm.so

			wsrep_cluster_name="my_cluster" # 集群名称
			# wsrep_cluster_address="gcomm://" # 第一次运行可能要使用空IP地址列表
			wsrep_cluster_address="gcomm://192.168.1.201,192.168.1.202,192.168.1.203,192.168.1.204" # 节点列表的IP地址

			# Galera Synchronization Configuration
			wsrep_sst_method=rsync

			# 各个节点配置不同之处
			# Galera Node Configuration
			wsrep_node_address="" #当前节点IP地址
			wsrep_node_name="" #当前节点名称
		' > /etc/mysql/conf.d/galera.cnf;

		sed -i "s/^wsrep_node_address=.*/wsrep_node_address=`ifconfig | grep inet | grep -v inet6 | grep -v 127.0.0.1 |awk '{print $2}' | head -n 1`/g" /etc/mysql/conf.d/galera.cnf;
		sed -i "s/^wsrep_node_name=.*/wsrep_node_name=`hostname`/g" /etc/mysql/conf.d/galera.cnf;
		sed -i 's/127.0.0.1/0.0.0.0/g' /etc/mysql/mariadb.conf.d/50-server.cnf;
		
		systemctl --now enable mysql;
		
		mysql -e "grant all privileges on *.* to root@'%' identified by 'winter'";
		mysql -e "flush privileges";
		
		systemctl restart mysql;
	)
	#结束


### 第一个节点首次运行 

	galera_new_cluster
	
	#查看状态
	mysql -e "SHOW STATUS LIKE 'wsrep_cluster_size'"


### 在第 1 个节点生成启动集群脚本 ~/start-cluster

	(
		echo '
			echo
			echo 启动第1个节点 ......
			echo galera_new_cluster
			if [ "`grep ^safe_to_bootstrap /var/lib/mysql/grastate.dat`" == "" ]; then echo "safe_to_bootstrap: 1" >> /var/lib/mysql/grastate.dat; else sed -i "s/^safe_to_bootstrap.*$/safe_to_bootstrap: 1/g" /var/lib/mysql/grastate.dat; fi
			galera_new_cluster

			for((i=2;i<=4;i++));
			do
			    echo
			    echo 启动第 $i 个节点 ......
			    echo ssh 192.168.1.20$i service mysql restart
			    ssh 192.168.1.20$i service mysql restart
			done
			echo
			echo 启动完毕
			echo
		' > ~/start-cluster;
		sed -i '1 i #!/bin/bash' ~/start-cluster;
	)


### 在第 1 个节点生成启动集群

	~/start-cluster


### 负载均衡节点安装配置 

	# 节点 IP: 192.168.1.200

	apt install haproxy -y

	# 开始
	(
		echo '
			listen winter_mariadb_cluster

				mode tcp
				balance source
				bind 0.0.0.0:3306
				
				server db1 192.168.1.201:3306
				server db2 192.168.1.202:3306
				server db3 192.168.1.203:3306
				server db4 192.168.1.204:3306
		' > /etc/haproxy/haproxy.cfg;
	)
	# 结束

	systemctl --now enable haproxy


### Prometheus 监控安装配置 (可选)

	# 每个节点执行

#### 安装

	apt install -y prometheus-mysqld-exporter

	systemctl enable prometheus-mysqld-exporter

#### 配置

	systemctl stop prometheus-mysqld-exporter

	mysql  -u root -pwinter -e "INSTALL SONAME 'query_response_time'; SET GLOBAL query_response_time_stats=ON "

	if [ "`grep ^query_response_time_stats /etc/mysql/mariadb.conf.d/50-server.cnf`" == "" ]; then echo query_response_time_stats=ON >> /etc/mysql/mariadb.conf.d/50-server.cnf; fi

	(ip=`ifconfig |grep inet |grep -v inet6 |grep -v 127.0.0.1 |awk '{print $2}' |head -n 1`; echo -e "[client]\nhost=${ip} \nuser=root \npassword=winter" > /root/.my.cnf)

	sed -i '/^User=.*/d' /lib/systemd/system/prometheus-mysqld-exporter.service
		
	sed -i "s/ARGS=.*/ARGS=\"--config.my-cnf=\/root\/.my.cnf\ --log.level=info\"/g" /etc/default/prometheus-mysqld-exporter
	
	systemctl daemon-reload

#### 启动

	systemctl restart prometheus-mysqld-exporter
	
	systemctl status prometheus-mysqld-exporter

### 结果

![image](https://github.com/AndyInAi/Winter/blob/main/img/00000-1430822278.png)

