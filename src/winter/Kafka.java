
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
import org.apache.kafka.common.errors.TopicExistsException;

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
	 * 192.168.1.234 kk4<br/>
	 * 
	 */
	public static String BOOTSTRAP_SERVERS_CONFIG = "192.168.1.231:9092, 192.168.1.232:9092, 192.168.1.233:9092, 192.168.1.234:9092";

	public static int NUM_PARTITIONS = 3;

	/**
	 * 打印信息
	 * 
	 * @param o
	 */
	public static void log(Object o)
	{

		String time = (new Timestamp(System.currentTimeMillis())).toString().substring(0, 19);

		System.out.println("[" + time + "] " + o.toString());

	}

	/**
	 * 测试
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{

		Kafka kf = new Kafka();

		log(kf.createTopic("test_topic666"));

	}

	/**
	 * 构造方法
	 */
	private Kafka()
	{
	}

	/**
	 * 创建 Topic
	 * 
	 * @param numPartitions
	 * @param topicNames
	 * @return
	 */
	public boolean createTopic(int numPartitions, String... topicNames)
	{

		return createTopics(BOOTSTRAP_SERVERS_CONFIG, numPartitions, topicNames);

	}

	/**
	 * 创建 Topic
	 * 
	 * @param numPartitions
	 * @param topicName
	 * @return
	 */
	public boolean createTopic(int numPartitions, String topicName)
	{

		return createTopics(BOOTSTRAP_SERVERS_CONFIG, numPartitions, new String[] { topicName });

	}

	/**
	 * 创建 Topic
	 * 
	 * @param topicName
	 * @return
	 */
	public boolean createTopic(String topicName)
	{

		return createTopics(BOOTSTRAP_SERVERS_CONFIG, NUM_PARTITIONS, new String[] { topicName });

	}

	/**
	 * 创建 Topic
	 * 
	 * @param topicNames
	 * @return
	 */
	public boolean createTopic(String... topicNames)
	{

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
	public boolean createTopic(String bootstrapServers, int numPartitions, String topicName)
	{

		return createTopics(bootstrapServers, numPartitions, new String[] { topicName });

	}

	/**
	 * 创建 Topic
	 * 
	 * @param bootstrapServers
	 * @param numPartitions
	 * @param topicNames
	 * @return
	 */
	public boolean createTopics(String bootstrapServers, int numPartitions, String... topicNames)
	{

		if (bootstrapServers == null || (bootstrapServers = bootstrapServers.trim()).length() == 0 || numPartitions < 1 || topicNames == null || topicNames.length == 0) {

			return false;

		}

		Properties props = new Properties();

		props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

		props.put(AdminClientConfig.CLIENT_ID_CONFIG, "client-" + UUID.randomUUID());

		try (Admin admin = Admin.create(props)) {

			// create topics in a retry loop
			while (true) {

				// use default RF to avoid NOT_ENOUGH_REPLICAS error with minISR > 1
				short replicationFactor = -1;

				List<NewTopic> newTopics = Arrays.stream(topicNames).map(name -> new NewTopic(name, numPartitions, replicationFactor)).collect(Collectors.toList());

				try {

					admin.createTopics(newTopics).all().get();

					log("Created topics: " + Arrays.toString(topicNames));

					return true;

				} catch (ExecutionException ex) {

					if (!(ex.getCause() instanceof TopicExistsException)) {

						ex.printStackTrace();

					}

					TimeUnit.MILLISECONDS.sleep(1_000);

				}

				return false;

			}

		} catch (Throwable e) {

			e.printStackTrace();

			return false;

		}

	}

}
