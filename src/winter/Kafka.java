
package winter;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.errors.TopicExistsException;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

public class Kafka {

	/**
	 * 注意：
	 * 
	 * 运行此程序的开发电脑或服务器，必须把所有 kafka 服务器的主机名和IP地址对
	 * 
	 * 添加到 /etc/hosts 或 C:\Windows\System32\drivers\etc\hosts 文件
	 * 
	 * 例如：
	 * 
	 * 192.168.1.231 kk1 <br/>
	 * 192.168.1.232 kk2 <br/>
	 * 192.168.1.233 kk3 <br/>
	 * 192.168.1.234 kk4 <br/>
	 * 
	 */
	public static String BOOTSTRAP_SERVERS_CONFIG = "192.168.1.231:9092, 192.168.1.232:9092, 192.168.1.233:9092, 192.168.1.234:9092";

	public static int NUM_PARTITIONS = 3;

	/**
	 * 打印信息
	 * 
	 * @param o
	 */
	public static void log(Object o) {

		String time = (new Timestamp(System.currentTimeMillis())).toString().substring(0, 19);

		System.out.println("[" + time + "] " + o.toString());

	}

	/**
	 * 测试
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		Kafka kf = new Kafka();

		String topic = "test_topic_888";

		RecordMetadata meta = kf.syncSend(topic, topic + " send test " + new java.util.Date());

		if (meta == null) {

			log(meta);

		} else {

			log(meta.topic());

			log(meta.partition());

		}

	}

	public KafkaProducer<Integer, String> producer = null;

	/**
	 * 构造方法
	 */
	private Kafka() {

		producer = createKafkaProducer();

	}

	public KafkaProducer<Integer, String> createKafkaProducer() {

		Properties props = new Properties();

		// bootstrap server config is required for producer to connect to
		// brokers
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS_CONFIG);

		// client id is not required, but it's good to track the source of requests beyond just ip/port by allowing a logical application name to be included in server-side request logging
		props.put(ProducerConfig.CLIENT_ID_CONFIG, "client-" + UUID.randomUUID());

		// key and value are just byte arrays, so we need to set appropriate
		// serializers
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class);

		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

		// enable duplicates protection at the partition level
		props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

		return new KafkaProducer<>(props);

	}

	/**
	 * 创建 Topic
	 * 
	 * @param numPartitions
	 * @param topicNames
	 * @return
	 */
	public boolean createTopic(int numPartitions, String... topicNames) {

		return createTopics(BOOTSTRAP_SERVERS_CONFIG, numPartitions, topicNames);

	}

	/**
	 * 创建 Topic
	 * 
	 * @param numPartitions
	 * @param topicName
	 * @return
	 */
	public boolean createTopic(int numPartitions, String topicName) {

		return createTopics(BOOTSTRAP_SERVERS_CONFIG, numPartitions, new String[]{topicName});

	}

	/**
	 * 创建 Topic
	 * 
	 * @param topicName
	 * @return
	 */
	public boolean createTopic(String topicName) {

		return createTopics(BOOTSTRAP_SERVERS_CONFIG, NUM_PARTITIONS, new String[]{topicName});

	}

	/**
	 * 创建 Topic
	 * 
	 * @param topicNames
	 * @return
	 */
	public boolean createTopic(String... topicNames) {

		return createTopics(BOOTSTRAP_SERVERS_CONFIG, NUM_PARTITIONS, topicNames);

	}

	/**
	 * 创建 Topic
	 * 
	 * @param bootstrapServers
	 * @param numPartitions
	 * @param topicName
	 * @return
	 */
	public boolean createTopic(String bootstrapServers, int numPartitions, String topicName) {

		return createTopics(bootstrapServers, numPartitions, new String[]{topicName});

	}

	/**
	 * 创建 Topic
	 * 
	 * @param bootstrapServers
	 * @param numPartitions
	 * @param topicNames
	 * @return
	 */
	public boolean createTopics(String bootstrapServers, int numPartitions, String... topicNames) {

		if (bootstrapServers == null || (bootstrapServers = bootstrapServers.trim()).length() == 0 || numPartitions < 1 || topicNames == null || topicNames.length == 0) {

			return false;

		}

		Properties props = new Properties();

		props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

		props.put(AdminClientConfig.CLIENT_ID_CONFIG, "client-" + UUID.randomUUID());

		try (Admin admin = Admin.create(props)) {

			for (int i = 0; i < 30; i++) {

				short replicationFactor = -1;

				List<NewTopic> newTopics = Arrays.stream(topicNames).map(name -> new NewTopic(name, numPartitions, replicationFactor)).collect(Collectors.toList());

				try {

					admin.createTopics(newTopics).all().get();

					log("Created topics: " + Arrays.toString(topicNames));

					return true;

				} catch (ExecutionException ex) {

					if (ex.getCause() instanceof TopicExistsException) {

						return true;

					}

					ex.printStackTrace();

					TimeUnit.MILLISECONDS.sleep(1_000);

				}

			}

		} catch (Throwable e) {

			e.printStackTrace();

		}

		return false;

	}

	/**
	 * 发送消息
	 * 
	 * @param topic
	 * @param message
	 * @return
	 */
	public RecordMetadata syncSend(String topic, String message) {

		try {

			return producer.send(new ProducerRecord<>(topic, message)).get();

		} catch (Exception ex) {

			ex.printStackTrace();

		}

		return null;

	}

}
