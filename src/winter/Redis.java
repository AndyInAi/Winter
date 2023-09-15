package winter;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

public class Redis {

	public final static String HOST = "192.168.1.240";

	public final static int PORT = 6379;

	public static Vector<JedisCluster> POOL = new Vector<JedisCluster>();

	public static int IDLE = 3;

	public static void main(String[] args)
	{

		Redis redis = new Redis();

		// System.out.println(redis.get("hello"));

		redis.test();

	}

	public void closeRedis(JedisCluster redis)
	{

		if (POOL.size() > IDLE) {

			redis.close();

		} else {

			POOL.add(redis);

		}

	}

	public String get(String key)
	{

		JedisCluster redis = getRedis();

		String value = redis.get(key);

		closeRedis(redis);

		return value;

	}

	public JedisCluster getRedis()
	{

		if (POOL.size() < IDLE) {

			return new JedisCluster(new HostAndPort(HOST, PORT));

		} else {

			return POOL.remove(0);

		}

	}

	public String hget(String key, String field)
	{

		JedisCluster redis = getRedis();

		String value = redis.hget(key, field);

		closeRedis(redis);

		return value;

	}

	public Map<String, String> hgetAll(String key)
	{

		JedisCluster redis = getRedis();

		Map<String, String> map = redis.hgetAll(key);

		closeRedis(redis);

		return map;

	}

	public void hset(String key, Map<String, String> map)
	{

		JedisCluster redis = getRedis();

		redis.hset(key, map);

		closeRedis(redis);

	}

	public void set(String key, String value)
	{

		JedisCluster redis = getRedis();

		redis.set(key, value);

		closeRedis(redis);

	}

	public void test()
	{

		long start = System.currentTimeMillis();

		for (int i = 0; i < 10000; i++) {

			test2();

		}

		System.out.println(System.currentTimeMillis() - start);

	}

	public void test2()
	{

		set("foo", "bar");

		System.out.println(get("foo"));

		Map<String, String> hash = new HashMap<>();

		hash.put("name", "John");
		hash.put("surname", "Smith");
		hash.put("company", "Redis");
		hash.put("age", "29");

		hset("user-session:123", hash);

		System.out.println(hgetAll("user-session:123"));

	}

}