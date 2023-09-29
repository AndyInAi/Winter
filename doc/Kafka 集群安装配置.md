
### 安装配置 

		sudo apt install -y openjdk-11-jdk

		wget https://dlcdn.apache.org/kafka/3.5.0/kafka_2.13-3.5.0.tgz -O ~/kafka_2.13-3.5.0.tgz

		tar xvzf ~/kafka_2.13-3.5.0.tgz

		ln -s ~/kafka_2.13-3.5.0 ~/kafka

		cd ~/kafka

		if [ "`grep ^plugin.path= config/connect-standalone.properties`" == "" ]; then echo "plugin.path=libs/connect-file-3.5.0.jar" >> config/connect-standalone.properties; else sed -i "s/^plugin.path=.*$/plugin.path=libs\/connect-file-3.5.0.jar/g" config/connect-standalone.properties; fi

		echo "nohup bin/zookeeper-server-start.sh config/zookeeper.properties > zookeeper.log 2>&1 &" > zk ; chmod +x zk

		echo "ps aux | grep -v grep  | grep org.apache.zookeeper.server.quorum.QuorumPeerMain | awk '{print \$2}'" > zk_pid ; chmod +x zk_pid

		echo "nohup bin/kafka-server-start.sh config/server.properties > server.log 2>&1 &" > kf ; chmod +x kf

		echo "ps aux | grep -v grep  | grep kafka.Kafka | awk '{print \$2}'" > kf_pid ; chmod +x kf_pid

		echo 'cd ~/kafka; zk_pid="`./zk_pid`"; if [ "$zk_pid" == "" ]; then ./zk; else echo Zookeeper PID $zk_pid running ...; fi' > start
		
		echo 'cd ~/kafka; kf_pid="`./kf_pid`"; if [ "$kf_pid" == "" ]; then ./kf; else echo Kafka PID $kf_pid running ...; fi' >> start ; chmod +x start

		echo 'cd ~/kafka; zk_pid="`./zk_pid`"; if [ ! "$zk_pid" == "" ]; then kill -9 $zk_pid; fi' > stop

		echo 'cd ~/kafka; kf_pid="`./kf_pid`"; if [ ! "$kf_pid" == "" ]; then kill -9 $kf_pid; fi' >> stop ; chmod +x stop


### 运行服务

		~/kafka/start


### 停止服务

		~/kafka/stop


### 测试

#### 创建 topic

		cd ~/kafka; bin/kafka-topics.sh --create --topic quickstart-events --bootstrap-server localhost:9092

#### 查看 topic

		cd ~/kafka; bin/kafka-topics.sh --describe --topic quickstart-events --bootstrap-server localhost:9092

#### 生产

		cd ~/kafka; bin/kafka-console-producer.sh --topic quickstart-events --bootstrap-server localhost:9092

#### 消费

		cd ~/kafka; bin/kafka-console-consumer.sh --topic quickstart-events --from-beginning --bootstrap-server localhost:9092

#### 批量生产

		cd ~/kafka; echo -e "foo\nbar" > test.txt

		cd ~/kafka; bin/connect-standalone.sh config/connect-standalone.properties config/connect-file-source.properties config/connect-file-sink.properties

		cd ~/kafka; more test.sink.txt


