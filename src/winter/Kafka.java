
package winter;

import static java.util.Collections.singleton;

import java.sql.Timestamp;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.TopicExistsException;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
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

		System.out.println("[" + time + "] " + (o == null ? null : o.toString()));

	}
	/**
	 * 测试
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		Kafka kf = new Kafka();

		String topic = "test_topic_888";

		log("发送：");

		RecordMetadata meta = kf.write(topic, topic + " send test " + new java.util.Date());

		if (meta == null) {

			log(meta);

		} else {

			log(meta.topic());

			log(meta.partition());

		}

		log("接收：");

		ConsumerRecords<Integer, String> records = kf.read(topic);

		if (records == null) {

			log(records);

		} else {

			for (ConsumerRecord<Integer, String> record : records) {

				log(record);

			}

		}

	}

	/**
	 * 构造方法
	 */
	private Kafka() {

	}

	/**
	 * 实例化消费者
	 * 
	 * @return
	 */
	public KafkaConsumer<Integer, String> createKafkaConsumer() {

		Properties props = new Properties();

		// bootstrap server config is required for consumer to connect to brokers
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS_CONFIG);

		// client id is not required, but it's good to track the source of requests beyond just ip/port
		// by allowing a logical application name to be included in server-side request logging
		props.put(ConsumerConfig.CLIENT_ID_CONFIG, "client-" + UUID.randomUUID());

		// consumer group id is required when we use subscribe(topics) for group management
		props.put(ConsumerConfig.GROUP_ID_CONFIG, "group-" + UUID.randomUUID());

		// key and value are just byte arrays, so we need to set appropriate deserializers
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class);

		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

		// // sets the reset offset policy in case of invalid or no offset
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

		return new KafkaConsumer<>(props);

	}

	/**
	 * 实例化生产者
	 * 
	 * @return
	 */
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
	 * 接收消息
	 * 
	 * @param topic
	 * @return
	 */
	public ConsumerRecords<Integer, String> read(String topic) {

		KafkaConsumer<Integer, String> consumer = createKafkaConsumer();

		consumer.subscribe(singleton(topic));

		log("Subscribed to: " + topic);

		ConsumerRecords<Integer, String> records = null;

		ConsumerRecords<Integer, String> max_records = null;

		int max = 0;

		try {

			for (int i = 0; i < 8; i++) {

				// if required, poll updates partition assignment and invokes the configured rebalance listener
				// then tries to fetch records sequentially using the last committed offset or auto.offset.reset policy
				// returns immediately if there are records or times out returning an empty record set
				// the next poll must be called within session.timeout.ms to avoid group rebalance
				records = consumer.poll(Duration.ofSeconds(1));

				int size = records.count();

				if (size > 0 && size == max) {

					max_records = records;

					break;

				}

				if (size > max) {

					max_records = records;

					max = size;

				}

				// TimeUnit.MILLISECONDS.sleep(1_000);

			}

		} catch (Exception ex) {

			ex.printStackTrace();

		} finally {

			consumer.close();

		}

		return max_records;

	}

	/**
	 * 发送消息
	 * 
	 * @param topic
	 * @param message
	 * @return
	 */
	public RecordMetadata write(String topic, String message) {

		KafkaProducer<Integer, String> producer = createKafkaProducer();

		try {

			return producer.send(new ProducerRecord<>(topic, message)).get();

		} catch (Exception ex) {

			ex.printStackTrace();

		} finally {

			producer.close();

		}

		return null;

	}

}
