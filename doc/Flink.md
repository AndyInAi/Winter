

### 安装

	apt install -y openjdk-11-jdk

	if [ ! -f ~/flink-1.17.1-bin-scala_2.12.tgz ]; then wget https://mirrors.tuna.tsinghua.edu.cn/apache/flink/flink-1.17.1/flink-1.17.1-bin-scala_2.12.tgz -O ~/flink-1.17.1-bin-scala_2.12.tgz ; fi

	if [ ! -d ~/flink-1.17.1 ]; then tar xvzf ~/flink-1.17.1-bin-scala_2.12.tgz; fi

	if [ ! -s ~/flink ]; then ln -s ~/flink-1.17.1 ~/flink ; fi


### 配置 

	# master ip 192.168.1.71
	# worker ip 192.168.1.72-4

	echo "192.168.1.71:8081" > ~/flink/conf/masters

	(echo '
		192.168.1.72
		192.168.1.73
		192.168.1.74
	'> ~/flink/conf/workers;
	sed -i "s/\t//g" ~/flink/conf/workers;
	sed -i '/^$/d' ~/flink/conf/workers;
	)

	sed -i "s/^.*taskmanager.numberOfTaskSlots.*$/taskmanager.numberOfTaskSlots: 64/" ~/flink/conf/flink-conf.yaml

	sed -i "s/^.*parallelism.default.*$/parallelism.default: 64/" ~/flink/conf/flink-conf.yaml

	sed -i "s/: localhost$/: 0.0.0.0/" ~/flink/conf/flink-conf.yaml

	sed -i "s/jobmanager.rpc.address: 0.0.0.0/jobmanager.rpc.address: 192.168.1.71/" ~/flink/conf/flink-conf.yaml

	sed -i "s/^.*taskmanager.memory.network.min.*$/taskmanager.memory.network.min: 1gb/" ~/flink/conf/flink-conf.yaml

	sed -i "s/^.*taskmanager.memory.network.max.*$/taskmanager.memory.network.max: 2gb/" ~/flink/conf/flink-conf.yaml

	sed -i "s/^.*taskmanager.memory.network.fraction.*$/taskmanager.memory.network.fraction: 0.4/" ~/flink/conf/flink-conf.yaml

	sed -i "s/^jobmanager.memory.process.size.*$/jobmanager.memory.process.size: 4g/" ~/flink/conf/flink-conf.yaml

	sed -i "s/^taskmanager.memory.process.size.*$/taskmanager.memory.process.size: 6g/" ~/flink/conf/flink-conf.yaml


### 配置 master 免密码登录 worker 
	
 	# 在 master 192.168.1.71 执行

	ssh-copy-id -f 192.168.1.72
	ssh-copy-id -f 192.168.1.73
	ssh-copy-id -f 192.168.1.74


### 运行	

#### 启动

	# 在 master 192.168.1.71 执行
 
	(cd ~/flink; bin/start-cluster.sh)

#### 停止

	# 在 master 192.168.1.71 执行
 
	(cd ~/flink; bin/stop-cluster.sh)

#### 重启

	# 在 master 192.168.1.71 执行
 
	(cd ~/flink; bin/stop-cluster.sh && bin/start-cluster.sh)


### 测试 

	# 在 master 192.168.1.71 执行

	(cd ~/flink; bin/flink run examples/streaming/WordCount.jar)

#### 
	
 	# 在 worker 192.168.1.72-74 执行

	(cd ~/flink; tail log/flink-*-taskexecutor-*.out)
	

### 监控

	http://192.168.1.71:8081/#/overview
	

