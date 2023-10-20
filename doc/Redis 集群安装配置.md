

### 安装配置

	# 共 9 个节点，3 主 6 从，在每一个节点执行

	(
		NODES="192.168.1.241 192.168.1.242 192.168.1.243 192.168.1.244 192.168.1.245 192.168.1.246 192.168.1.247 192.168.1.248 192.168.1.249"; NAME="redis" # 9 个节点 IP 列表，对应主机名 redis1 - redis9
		no=0; for i in $NODES; do no=$(($no+1)) ; ip="$i " ;  host="$ip ${NAME}$no" ;  if [ "`grep \"^$ip\" /etc/hosts`" == "" ]; then echo "$host" >> /etc/hosts; else sed -i "s/^$ip.*$/$host/g" /etc/hosts; fi ; done
	)

	apt install -y redis net-tools

	if [ "`grep ^bind /etc/redis/redis.conf`" == "" ]; then echo "bind 0.0.0.0" >> /etc/redis/redis.conf; else sed -i "s/^bind.*$/bind 0.0.0.0/g" /etc/redis/redis.conf; fi

	if [ "`grep ^cluster-enabled /etc/redis/redis.conf`" == "" ]; then echo "cluster-enabled yes" >> /etc/redis/redis.conf; else sed -i "s/^cluster-enabled.*$/cluster-enabled yes/g" /etc/redis/redis.conf; fi

	if [ "`grep ^cluster-node-timeout /etc/redis/redis.conf`" == "" ]; then echo "cluster-node-timeout 5000" >> /etc/redis/redis.conf; else sed -i "s/^cluster-node-timeout.*$/cluster-node-timeout 5000/g" /etc/redis/redis.conf; fi

	if [ "`grep ^appendonly /etc/redis/redis.conf`" == "" ]; then echo "appendonly yes" >> /etc/redis/redis.conf; else sed -i "s/^appendonly.*$/appendonly yes/g" /etc/redis/redis.conf; fi

	if [ "`grep ^cluster-config-file /etc/redis/redis.conf`" == "" ]; then echo "cluster-config-file nodes-6379.conf" >> /etc/redis/redis.conf; else sed -i "s/^cluster-config-file.*$/cluster-config-file nodes-6379.conf/g" /etc/redis/redis.conf; fi

	systemctl restart redis


### 集群配置

	#在第一个节点执行

	redis-cli --cluster create 192.168.1.241:6379 192.168.1.242:6379 192.168.1.243:6379 192.168.1.244:6379 192.168.1.245:6379 192.168.1.246:6379 192.168.1.247:6379 192.168.1.248:6379 192.168.1.249:6379 --cluster-replicas 2


### 负载均衡

	apt install haproxy -y

	# 开始
	(
		echo "
			listen winter_redis_cluster

				mode tcp
				balance source
				bind 0.0.0.0:6379
				
				server redis1 192.168.1.241:6379
				server redis2 192.168.1.242:6379
				server redis3 192.168.1.243:6379
		" > /etc/haproxy/haproxy.cfg
	)
	# 结束

	systemctl enable haproxy

	systemctl restart haproxy


### 在第一个节点重启所有节点

	for ((i=9; i>0; i--)); do ssh 192.168.1.24$i "sync && reboot" ; done


### 在第一个节点关闭所有节点

	for ((i=9; i>0; i--)); do ssh 192.168.1.24$i "sync && halt -p" ; done


### Prometheus 监控安装配置 (可选)

	# 每个节点执行


#### 安装

	export DEBIAN_FRONTEND=noninteractive; apt install -y prometheus-redis-exporter ; systemctl --now enable prometheus-redis-exporter


#### 启动

	systemctl start prometheus-redis-exporter; 	systemctl status prometheus-redis-exporter


#### 截图

	![image](https://github.com/AndyInAi/Winter/blob/main/img/p8s/p8s-2.png)

