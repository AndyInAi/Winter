package winter;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.json.simple.JSONObject;

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

		redis.set("hello", "石大大");

		redis.get("hello");

		System.out.println(redis.get("hello"));

		Map<String, String> hash = new HashMap<>();

		hash.put("id", "2");

		hash.put("name", "Andy");

		hash.put("nick", "石大大");

		redis.hset("session:2", hash);

		Map<String, String> map = redis.hgetAll("session:2");

		System.out.println(map);

		String json = redis.hgetJson("session:2");

		System.out.println(json);

		// dis.test();

	}

	/**
	 * 关闭连接
	 * 
	 * @param redis
	 */
	public void closeRedis(JedisCluster redis)
	{

		if (POOL.size() > IDLE) {

			redis.close();

		} else {

			POOL.add(redis);

		}

	}

	/**
	 * 删除
	 * 
	 * @param key
	 */
	public void del(String key)
	{

		JedisCluster redis = getRedis();

		redis.del(key);

		closeRedis(redis);

	}

	/**
	 * 取值
	 * 
	 * @param key
	 * @return
	 */
	public String get(String key)
	{

		JedisCluster redis = getRedis();

		String value = redis.get(key);

		closeRedis(redis);

		return value;

	}

	/**
	 * 获取连接
	 * 
	 * @return
	 */
	public JedisCluster getRedis()
	{

		if (POOL.size() < IDLE) {

			return new JedisCluster(new HostAndPort(HOST, PORT));

		} else {

			return POOL.remove(0);

		}

	}

	/**
	 * 取 Map 字段值
	 * 
	 * @param key
	 * @param field
	 * @return
	 */
	public String hget(String key, String field)
	{

		JedisCluster redis = getRedis();

		String value = redis.hget(key, field);

		closeRedis(redis);

		return value;

	}

	/**
	 * 取 Map
	 * 
	 * @param key
	 * @return
	 */
	public Map<String, String> hgetAll(String key)
	{

		JedisCluster redis = getRedis();

		Map<String, String> map = redis.hgetAll(key);

		closeRedis(redis);

		return map;

	}

	/**
	 * 取 Map 并转换为 JSON 字符串
	 * 
	 * @param key
	 * @return
	 */
	public String hgetJson(String key)
	{

		JedisCluster redis = getRedis();

		String json = JSONObject.toJSONString(redis.hgetAll(key));

		closeRedis(redis);

		return json;

	}

	/**
	 * 设置 Map
	 * 
	 * @param key
	 * @param map
	 */
	public void hset(String key, Map<String, String> map)
	{

		JedisCluster redis = getRedis();

		redis.hset(key, map);

		closeRedis(redis);

	}

	/**
	 * 设置 Map 字段值
	 * 
	 * @param key
	 * @param field
	 * @param value
	 */
	public void hsetnx(String key, String field, String value)
	{

		JedisCluster redis = getRedis();

		redis.hsetnx(key, field, value);

		closeRedis(redis);

	}

	/**
	 * 设置值
	 * 
	 * @param key
	 * @param value
	 */
	public void set(String key, String value)
	{

		JedisCluster redis = getRedis();

		redis.set(key, value);

		closeRedis(redis);

	}

	/**
	 * 测试
	 */
	public void test()
	{

		System.out.println("Test start ...");

		long start = System.currentTimeMillis();

		int times = 10000;

		for (int i = 0; i < times; i++) {

			test2();

		}

		System.out.println("Time: " + (System.currentTimeMillis() - start));

	}

	/**
	 * 测试
	 */
	public void test2()
	{

		set("hello", "石大大");

		get("hello");

		// System.out.println(get("hello"));

		Map<String, String> hash = new HashMap<>();

		hash.put("name", "Andy");

		hash.put("nick", "石大大");

		hset("session:2", hash);

		hgetAll("session:2");

		// System.out.println(hgetAll("session:2"));

	}

}