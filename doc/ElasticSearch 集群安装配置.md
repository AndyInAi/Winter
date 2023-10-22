

### 安装

	# 每个节点执行

	wget -qO - https://artifacts.elastic.co/GPG-KEY-elasticsearch | gpg --dearmor -o /usr/share/keyrings/elasticsearch-keyring.gpg

	echo "deb [signed-by=/usr/share/keyrings/elasticsearch-keyring.gpg] https://mirrors.tuna.tsinghua.edu.cn/elasticstack/8.x/apt/ stable main" | tee /etc/apt/sources.list.d/elastic-8.x.list

	apt-get update -y && apt-get install -y elasticsearch

	(
		NODES="192.168.1.221 192.168.1.222 192.168.1.223 192.168.1.224"; NAME="es" # 4 个节点 IP 列表，对应主机名 es1 - es4
		no=0; for i in $NODES; do no=$(($no+1)) ; ip="$i " ;  host="$ip ${NAME}$no" ;  if [ "`grep \"^$ip\" /etc/hosts`" == "" ]; then echo "$host" >> /etc/hosts; else sed -i "s/^$ip.*$/$host/g" /etc/hosts; fi ; done
	)


### 配置

	# 每个节点执行

	# 开始
	(
		export nodes='["192.168.1.221", "192.168.1.222", "192.168.1.223", "192.168.1.224"]' # 4 个节点，IP 192.168.1.221 - 4

		sed -i 's/xpack.security.enabled: true/xpack.security.enabled: false/g' /etc/elasticsearch/elasticsearch.yml

		if [ "`grep ^cluster.initial_master_nodes /etc/elasticsearch/elasticsearch.yml`" == "" ]; then echo "cluster.initial_master_nodes: $nodes" >> /etc/elasticsearch/elasticsearch.yml; else sed -i "s/^cluster.initial_master_nodes.*$/cluster.initial_master_nodes: $nodes/g" /etc/elasticsearch/elasticsearch.yml; fi

		if [ "`grep ^discovery.seed_hosts /etc/elasticsearch/elasticsearch.yml`" == "" ]; then echo "discovery.seed_hosts: $nodes" >> /etc/elasticsearch/elasticsearch.yml; else sed -i "s/^discovery.seed_hosts.*$/discovery.seed_hosts: $nodes/g" /etc/elasticsearch/elasticsearch.yml; fi

		if [ "`grep ^node.name /etc/elasticsearch/elasticsearch.yml`" == "" ]; then echo "node.name: `hostname`" >> /etc/elasticsearch/elasticsearch.yml; else sed -i "s/^node.name.*$/node.name: `hostname`/g" /etc/elasticsearch/elasticsearch.yml; fi

		if [ "`grep ^transport.host /etc/elasticsearch/elasticsearch.yml`" == "" ]; then echo "transport.host: 0.0.0.0" >> /etc/elasticsearch/elasticsearch.yml; else sed -i "s/^transport.host.*$/transport.host: 0.0.0.0/g" /etc/elasticsearch/elasticsearch.yml; fi

		# rm -rf /var/lib/elasticsearch; mkdir -p /var/lib/elasticsearch ; chown -R elasticsearch:elasticsearch /var/lib/elasticsearch

		systemctl daemon-reload

		systemctl enable elasticsearch

		systemctl restart elasticsearch
	)
	# 结束


### 测试

	# 任一节点执行

	systemctl status elasticsearch

	curl -X GET "localhost:9200/?pretty"

	curl -XGET http://localhost:9200/_cat/health?v=true


### 负载均衡节点安装配置 

	# 节点 IP 192.168.1.220

	apt install haproxy -y

	#开始
	(
		echo '
			listen winter_elasticsearch_cluster

				mode tcp
				balance source
				bind 0.0.0.0:9200
				
				server es1 192.168.1.221:9200
				server es2 192.168.1.222:9200
				server es3 192.168.1.223:9200
				server es4 192.168.1.224:9200
		' > /etc/haproxy/haproxy.cfg;
	)
	#结束

	systemctl daemon-reload

	systemctl enable haproxy

	systemctl restart haproxy


### Prometheus 监控安装配置 (可选)

	# 在每个节点执行


#### 安装

	export DEBIAN_FRONTEND=noninteractive; apt install -y prometheus-elasticsearch-exporter ; systemctl --now enable prometheus-elasticsearch-exporter


#### 停止
	
	systemctl stop prometheus-elasticsearch-exporter;


#### 启动

	systemctl start prometheus-elasticsearch-exporter;  systemctl status prometheus-elasticsearch-exporter


#### 截图

![image](https://github.com/AndyInAi/Winter/blob/main/img/p8s/p8s-4.png)


