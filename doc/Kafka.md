 
### 安装

	sudo apt install -y openjdk-11-jdk net-tools

	wget https://dlcdn.apache.org/kafka/3.5.0/kafka_2.13-3.5.0.tgz -O ~/kafka_2.13-3.5.0.tgz

	tar xvzf ~/kafka_2.13-3.5.0.tgz

	ln -s ~/kafka_2.13-3.5.0 ~/kafka


### 配置 

	NODES="192.168.1.231 192.168.1.232 192.168.1.233 192.168.1.234" # IP 列表，对应主机名 kk1 - kk4
	CONNECT=""; for i in $NODES; do export CONNECT="${CONNECT}${i}:2181,";  done ; export CONNECT="${CONNECT}127.0.0.1:2181"	
	BROKER_ID="`ifconfig | grep inet | grep -v 127.0.0.1 | tail -n 1 | awk '{print $2}' |sed 's/\./ /g' | awk '{print $4}'`"

	cd ~/kafka

	# rm -f -r /var/{kafka-logs,zookeeper/version-2}
	mkdir -p /var/{kafka-logs,zookeeper}
	echo ${BROKER_ID} > /var/zookeeper/myid
	no=0; for i in $NODES; do no=$(($no+1)) ; ip="$i " ;  host="$ip kk$no" ;  if [ "`grep \"^$ip\" /etc/hosts`" == "" ]; then echo "$host" >> /etc/hosts; else sed -i "s/^$ip.*$/$host/g" /etc/hosts; fi ; done

	if [ "`grep ^dataDir= config/zookeeper.properties`" == "" ]; then echo "dataDir=/var/zookeeper" >> config/zookeeper.properties; else sed -i "s/^dataDir=.*$/dataDir=\/var\/zookeeper/g" config/zookeeper.properties; fi
	if [ "`grep ^initLimit= config/zookeeper.properties`" == "" ]; then echo "initLimit=90" >> config/zookeeper.properties; else sed -i "s/^initLimit=.*$/initLimit=90/g" config/zookeeper.properties; fi
	if [ "`grep ^syncLimit= config/zookeeper.properties`" == "" ]; then echo "syncLimit=90" >> config/zookeeper.properties; else sed -i "s/^syncLimit=.*$/syncLimit=90/g" config/zookeeper.properties; fi
	if [ "`grep ^tickTime= config/zookeeper.properties`" == "" ]; then echo "tickTime=2000" >> config/zookeeper.properties; else sed -i "s/^tickTime=.*$/tickTime=2000/g" config/zookeeper.properties; fi
	sed -i 's/^server\.[0-9]*=.*//g' config/zookeeper.properties
	for i in $NODES; do no=`echo $i | sed 's/\./ /g' | awk '{print $4}'` ; server="server.${no}=${i}:2888:3888" ;  if [ "`grep \"^server.${no}=\" config/zookeeper.properties`" == "" ]; then echo "${server}" >> config/zookeeper.properties; else sed -i "s/^server.${no}=.*$/$server/g" config/zookeeper.properties; fi ; done
	sed -i '/^$/d' config/zookeeper.properties

	if [ "`grep ^log.dirs= config/server.properties`" == "" ]; then echo "log.dirs=/var/kafka-logs" >> config/server.properties; else sed -i "s/^log.dirs=.*$/log.dirs=\/var\/kafka-logs/g" config/server.properties; fi
	if [ "`grep ^broker.id= config/server.properties`" == "" ]; then echo "broker.id=$BROKER_ID" >> config/server.properties; else sed -i "s/^broker.id=.*$/broker.id=$BROKER_ID/g" config/server.properties; fi
	if [ "`grep ^zookeeper.connect= config/server.properties`" == "" ]; then echo "zookeeper.connect=$CONNECT" >> config/server.properties; else sed -i "s/^zookeeper.connect=.*$/zookeeper.connect=$CONNECT/g" config/server.properties; fi
	if [ "`grep ^num.recovery.threads.per.data.dir= config/server.properties`" == "" ]; then echo "num.recovery.threads.per.data.dir=3" >> config/server.properties; else sed -i "s/^num.recovery.threads.per.data.dir=.*$/num.recovery.threads.per.data.dir=3/g" config/server.properties; fi
	if [ "`grep ^offsets.topic.replication.factor= config/server.properties`" == "" ]; then echo "offsets.topic.replication.factor=3" >> config/server.properties; else sed -i "s/^offsets.topic.replication.factor=.*$/offsets.topic.replication.factor=3/g" config/server.properties; fi
	if [ "`grep ^transaction.state.log.replication.factor= config/server.properties`" == "" ]; then echo "transaction.state.log.replication.factor=3" >> config/server.properties; else sed -i "s/^transaction.state.log.replication.factor=.*$/transaction.state.log.replication.factor=3/g" config/server.properties; fi
	if [ "`grep ^transaction.state.log.min.isr= config/server.properties`" == "" ]; then echo "transaction.state.log.min.isr=3" >> config/server.properties; else sed -i "s/^transaction.state.log.min.isr=.*$/transaction.state.log.min.isr=3/g" config/server.properties; fi
	if [ "`grep ^zookeeper.connection.timeout.ms= config/server.properties`" == "" ]; then echo "zookeeper.connection.timeout.ms=180000" >> config/server.properties; else sed -i "s/^zookeeper.connection.timeout.ms=.*$/zookeeper.connection.timeout.ms=180000/g" config/server.properties; fi
	if [ "`grep ^group.initial.rebalance.delay.ms= config/server.properties`" == "" ]; then echo "group.initial.rebalance.delay.ms=3" >> config/server.properties; else sed -i "s/^group.initial.rebalance.delay.ms=.*$/group.initial.rebalance.delay.ms=3/g" config/server.properties; fi
	if [ "`grep ^num.partitions= config/server.properties`" == "" ]; then echo "num.partitions=3" >> config/server.properties; else sed -i "s/^num.partitions=.*$/num.partitions=3/g" config/server.properties; fi

	if [ "`grep ^plugin.path= config/connect-standalone.properties`" == "" ]; then echo "plugin.path=libs/connect-file-3.5.0.jar" >> config/connect-standalone.properties; else sed -i "s/^plugin.path=.*$/plugin.path=libs\/connect-file-3.5.0.jar/g" config/connect-standalone.properties; fi

	echo "ZK_PID=\"\`ps aux | grep -v grep  | grep org.apache.zookeeper.server.quorum.QuorumPeerMain | awk '{print \$2}'\`\" " > start
	echo "KF_PID=\"\`ps aux | grep -v grep  | grep kafka.Kafka | awk '{print \$2}'\`\" " >> start
	echo "cd ~/kafka; " >> start
	echo "if [ \"\$ZK_PID\" == \"\" ]; then " >> start
	echo "        export KAFKA_HEAP_OPTS=\"-Xmx512M -Xms512M\" ; bin/zookeeper-server-start.sh -daemon config/zookeeper.properties " >> start
	echo "else " >> start
	echo "        echo Zookeeper PID \$ZK_PID running ...; " >> start
	echo "fi " >> start
	echo "if [ \"\$KF_PID\" == \"\" ]; then " >> start
	echo "        (sleep 8 ; export KAFKA_HEAP_OPTS=\"-Xmx6G -Xms6G\" ; bin/kafka-server-start.sh -daemon config/server.properties) & " >> start
	echo "else " >> start
	echo "        echo Kafka PID \$KF_PID running ... " >> start
	echo "fi " >> start
	chmod +x start

	echo 'cd ~/kafka; bin/kafka-server-stop.sh ' > stop
	echo 'bin/zookeeper-server-stop.sh ' >> stop
	chmod +x stop


### 运行服务

	~/kafka/start


### 停止服务

	~/kafka/stop


### 测试

#### 创建 topic；在 192.168.1.231

	cd ~/kafka; bin/kafka-topics.sh --create --topic quickstart-events --bootstrap-server localhost:9092

#### 查看 topic；在 192.168.1.232

	cd ~/kafka; bin/kafka-topics.sh --describe --topic quickstart-events --bootstrap-server localhost:9092

#### 生产；在 192.168.1.233

	cd ~/kafka; bin/kafka-console-producer.sh --topic quickstart-events --bootstrap-server localhost:9092

#### 消费；在 192.168.1.234

	cd ~/kafka; bin/kafka-console-consumer.sh --topic quickstart-events --from-beginning --bootstrap-server localhost:9092

#### 批量生产

	cd ~/kafka; echo -e "foo\nbar" > test.txt

	cd ~/kafka; bin/connect-standalone.sh config/connect-standalone.properties config/connect-file-source.properties config/connect-file-sink.properties

	cd ~/kafka; more test.sink.txt

