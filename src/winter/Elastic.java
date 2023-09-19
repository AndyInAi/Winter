package winter;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Elastic {

	public static String ELASTIC_URL = "http://192.168.1.220:9200";

	public static int TIME_OUT = 3000;

	public static void main(String[] args)
	{

		Elastic elastic = new Elastic();

		elastic.test();

	}

	private void close(Closeable o)
	{

		if (o != null) {

			try {

				o.close();

			} catch (IOException ex) {

				ex.printStackTrace();

			}

		}

	}

	/**
	 * 创建索引
	 * 
	 * @param indexName
	 * @return
	 */
	public boolean createIndex(String indexName)
	{

		if (indexName == null) {

			return false;

		}

		String _indexName = indexName.trim().toLowerCase();

		if (!_indexName.matches("[0-9a-z][0-9a-z_]*")) {

			return false;

		}

		HttpURLConnection conn = null;

		BufferedInputStream bis = null;

		try {

			conn = (HttpURLConnection) getURL("/" + _indexName).openConnection();

			conn.setRequestMethod("PUT");

			conn.setConnectTimeout(TIME_OUT);

			conn.setReadTimeout(TIME_OUT);

			conn.connect();

			if (conn.getResponseCode() != 200) {

				return false;

			}

			bis = new BufferedInputStream(conn.getInputStream());

			byte[] bytes = bis.readAllBytes();

			JSONParser p = new JSONParser();

			JSONObject j = (JSONObject) p.parse(new String(bytes));

			return j.get("acknowledged") != null && j.get("shards_acknowledged") != null && (boolean) j.get("acknowledged") && (boolean) j.get("shards_acknowledged");

		} catch (Exception ex) {

			ex.printStackTrace();

			return false;

		} finally {

			close(bis);

			disconnect(conn);

		}

	}

	/**
	 * 删除记录
	 * 
	 * @param indexName
	 * @param id
	 * @return
	 */
	public boolean delete(String indexName, long id)
	{

		return delete(indexName, String.valueOf(id));

	}

	/**
	 * 删除记录
	 * 
	 * @param indexName
	 * @return
	 */
	public boolean delete(String indexName, String id)
	{

		if (indexName == null || id == null) {

			return false;

		}

		String _indexName = indexName.trim().toLowerCase();

		if (!_indexName.matches("[0-9a-z][0-9a-z_]*")) {

			return false;

		}

		String _id = id.trim().toLowerCase();

		if (!_id.matches("[0-9a-z][0-9a-z_]*")) {

			return false;

		}

		HttpURLConnection conn = null;

		BufferedInputStream bis = null;

		try {

			conn = (HttpURLConnection) getURL("/" + _indexName + "/_doc/" + id).openConnection();

			conn.setRequestMethod("DELETE");

			conn.setConnectTimeout(TIME_OUT);

			conn.setReadTimeout(TIME_OUT);

			conn.connect();

			if (conn.getResponseCode() != 200) {

				return false;

			}

			bis = new BufferedInputStream(conn.getInputStream());

			byte[] bytes = bis.readAllBytes();

			JSONParser p = new JSONParser();

			JSONObject j = (JSONObject) p.parse(new String(bytes));

			String result = (String) j.get("result");

			return result != null && result.equals("deleted");

		} catch (Exception ex) {

			ex.printStackTrace();

			return false;

		} finally {

			close(bis);

			disconnect(conn);

		}

	}

	/**
	 * 删除索引
	 * 
	 * @param indexName
	 * @return
	 */
	public boolean deleteIndex(String indexName)
	{

		if (indexName == null) {

			return false;

		}

		String _indexName = indexName.trim().toLowerCase();

		if (!_indexName.matches("[0-9a-z][0-9a-z_]*")) {

			return false;

		}

		HttpURLConnection conn = null;

		BufferedInputStream bis = null;

		try {

			conn = (HttpURLConnection) getURL("/" + _indexName).openConnection();

			conn.setRequestMethod("DELETE");

			conn.setConnectTimeout(TIME_OUT);

			conn.setReadTimeout(TIME_OUT);

			conn.connect();

			if (conn.getResponseCode() != 200) {

				return false;

			}

			bis = new BufferedInputStream(conn.getInputStream());

			byte[] bytes = bis.readAllBytes();

			JSONParser p = new JSONParser();

			JSONObject j = (JSONObject) p.parse(new String(bytes));

			return j.get("acknowledged") != null && (boolean) j.get("acknowledged");

		} catch (Exception ex) {

			ex.printStackTrace();

			return false;

		} finally {

			close(bis);

			disconnect(conn);

		}

	}

	/**
	 * 关闭连接
	 * 
	 * @param conn
	 */
	private void disconnect(HttpURLConnection conn)
	{

		if (conn != null) {

			conn.disconnect();

		}

	}

	/**
	 * 获取记录
	 * 
	 * @param indexName
	 * @param id
	 * @return
	 */
	public JSONObject get(String indexName, long id)
	{

		return get(indexName, String.valueOf(id));

	}

	/**
	 * 获取记录
	 * 
	 * @param indexName
	 * @param id
	 * @return
	 */
	public JSONObject get(String indexName, String id)
	{

		if (indexName == null) {

			return null;

		}

		String _indexName = indexName.trim().toLowerCase();

		if (!_indexName.matches("[0-9a-z][0-9a-z_]*")) {

			return null;

		}

		String _id = id.trim().toLowerCase();

		if (!_id.matches("[0-9a-z][0-9a-z_]*")) {

			return null;

		}

		HttpURLConnection conn = null;

		BufferedInputStream bis = null;

		try {

			conn = (HttpURLConnection) getURL("/" + _indexName + "/_doc/" + id).openConnection();

			conn.setConnectTimeout(TIME_OUT);

			conn.setReadTimeout(TIME_OUT);

			conn.connect();

			if (conn.getResponseCode() != 200) {

				return null;

			}

			bis = new BufferedInputStream(conn.getInputStream());

			byte[] bytes = bis.readAllBytes();

			JSONParser p = new JSONParser();

			return (JSONObject) p.parse(new String(bytes));

		} catch (Exception ex) {

			ex.printStackTrace();

			return null;

		} finally {

			close(bis);

			disconnect(conn);

		}

	}

	/**
	 * 集群状态
	 * 
	 * @return
	 */
	public JSONObject getHealth()
	{

		HttpURLConnection conn = null;

		BufferedInputStream bis = null;

		try {

			conn = (HttpURLConnection) getURL("/_cluster/health").openConnection();

			conn.setConnectTimeout(TIME_OUT);

			conn.setReadTimeout(TIME_OUT);

			conn.connect();

			if (conn.getResponseCode() != 200) {

				return null;

			}

			bis = new BufferedInputStream(conn.getInputStream());

			byte[] bytes = bis.readAllBytes();

			JSONParser p = new JSONParser();

			return (JSONObject) p.parse(new String(bytes));

		} catch (Exception ex) {

			ex.printStackTrace();

			return null;

		} finally {

			close(bis);

			disconnect(conn);

		}

	}

	/**
	 * 索引详情
	 * 
	 * @param indexName
	 * @return
	 */
	public JSONObject getIndex(String indexName)
	{

		if (indexName == null) {

			return null;

		}

		String _indexName = indexName.trim().toLowerCase();

		if (!_indexName.matches("[0-9a-z][0-9a-z_]*")) {

			return null;

		}

		HttpURLConnection conn = null;

		BufferedInputStream bis = null;

		try {

			conn = (HttpURLConnection) getURL("/" + _indexName).openConnection();

			conn.setConnectTimeout(TIME_OUT);

			conn.setReadTimeout(TIME_OUT);

			conn.connect();

			if (conn.getResponseCode() != 200) {

				return null;

			}

			bis = new BufferedInputStream(conn.getInputStream());

			byte[] bytes = bis.readAllBytes();

			JSONParser p = new JSONParser();

			return (JSONObject) p.parse(new String(bytes));

		} catch (Exception ex) {

			ex.printStackTrace();

			return null;

		} finally {

			close(bis);

			disconnect(conn);

		}

	}

	/**
	 * 根据 path 生成 url
	 * 
	 * @param path
	 * @return
	 */
	private URL getURL(String path)
	{

		try {

			return new URL(ELASTIC_URL + path + "?format=json");

		} catch (MalformedURLException ex) {

			ex.printStackTrace();

		}

		return null;

	}

	/**
	 * 添加记录
	 * 
	 * @param indexName
	 * @param id
	 * @param key1
	 * @param value1
	 * @return
	 */
	public boolean insert(String indexName, long id, String key1, String value1)
	{

		return insert(indexName, String.valueOf(id), key1, value1);

	}

	/**
	 * 添加记录
	 * 
	 * @param indexName
	 * @param id
	 * @param key1
	 * @param value1
	 * @param key2
	 * @param value2
	 * @return
	 */
	public boolean insert(String indexName, long id, String key1, String value1, String key2, String value2)
	{

		return insert(indexName, String.valueOf(id), key1, value1, key2, value2);

	}

	/**
	 * 添加记录
	 * 
	 * @param indexName
	 * @param id
	 * @param row
	 * @return
	 */
	public boolean insert(String indexName, String id, JSONObject row)
	{

		if (indexName == null || id == null || row == null) {

			return false;

		}

		String _indexName = indexName.trim().toLowerCase();

		if (!_indexName.matches("[0-9a-z][0-9a-z_]*")) {

			return false;

		}

		String _id = id.trim().toLowerCase();

		if (!_id.matches("[0-9a-z][0-9a-z_]*")) {

			return false;

		}

		HttpURLConnection conn = null;

		InputStream bis = null;

		OutputStream bos = null;

		try {

			conn = (HttpURLConnection) getURL("/" + _indexName + "/_doc/" + id).openConnection();

			conn.setRequestMethod("POST");

			conn.setRequestProperty("Content-Type", "application/json");

			conn.setDoOutput(true);

			conn.setConnectTimeout(TIME_OUT);

			conn.setReadTimeout(TIME_OUT);

			conn.connect();

			bos = conn.getOutputStream();

			bos.write(row.toJSONString().getBytes());

			bos.flush();

			int code = conn.getResponseCode();

			if (code != 200 && code != 201) {

				return false;

			}

			bis = conn.getInputStream();

			byte[] bytes = bis.readAllBytes();

			JSONParser p = new JSONParser();

			JSONObject j = (JSONObject) p.parse(new String(bytes));

			String result = (String) j.get("result");

			return result != null && (result.equals("created") || result.equals("updated"));

		} catch (Exception ex) {

			ex.printStackTrace();

			return false;

		} finally {

			close(bos);

			close(bis);

			disconnect(conn);

		}

	}

	/**
	 * 添加记录
	 * 
	 * @param indexName
	 * @param id
	 * @param key1
	 * @param value1
	 * @return
	 */
	public boolean insert(String indexName, String id, String key1, String value1)
	{

		return insert(indexName, id, key1, value1, null, null);

	}

	/**
	 * 添加记录
	 * 
	 * @param indexName
	 * @param id
	 * @param key1
	 * @param value1
	 * @param key2
	 * @param value2
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public boolean insert(String indexName, String id, String key1, String value1, String key2, String value2)
	{

		if (key1 == null || value1 == null) {

			return false;

		}

		String _key1 = key1.trim();

		if (!_key1.matches(".+")) {

			return false;

		}

		String _value1 = value1.trim();

		JSONObject row = new JSONObject();

		row.put(key1, _value1);

		if (key2 != null && value1 != null) {

			String _key2 = key2.trim();

			String _value2 = value2.trim();

			if (_key2.matches(".+")) {

				row.put(key2, _value2);

			}

		}

		return insert(indexName, id, row);

	}

	/**
	 * 记录列表
	 * 
	 * @param indexName
	 * @param row
	 * @return
	 */
	public JSONArray list(String indexName)
	{

		if (indexName == null) {

			return new JSONArray();

		}

		String _indexName = indexName.trim().toLowerCase();

		if (!_indexName.matches("[0-9a-z][0-9a-z_]*")) {

			return new JSONArray();

		}

		HttpURLConnection conn = null;

		InputStream bis = null;

		try {

			conn = (HttpURLConnection) getURL("/" + _indexName + "/_search").openConnection();

			conn.setConnectTimeout(TIME_OUT);

			conn.setReadTimeout(TIME_OUT);

			conn.connect();

			int code = conn.getResponseCode();

			if (code != 200) {

				return new JSONArray();

			}

			bis = conn.getInputStream();

			byte[] bytes = bis.readAllBytes();

			JSONParser p = new JSONParser();

			JSONObject j = (JSONObject) p.parse(new String(bytes));

			return (JSONArray) ((JSONObject) j.get("hits")).get("hits");

		} catch (Exception ex) {

			ex.printStackTrace();

			return new JSONArray();

		} finally {

			close(bis);

			disconnect(conn);

		}

	}

	/**
	 * 索引列表
	 * 
	 * @return
	 */
	public JSONArray listIndex()
	{

		HttpURLConnection conn = null;

		BufferedInputStream bis = null;

		try {

			conn = (HttpURLConnection) getURL("/_cat/indices").openConnection();

			conn.setConnectTimeout(TIME_OUT);

			conn.setReadTimeout(TIME_OUT);

			conn.connect();

			if (conn.getResponseCode() != 200) {

				return new JSONArray();

			}

			bis = new BufferedInputStream(conn.getInputStream());

			byte[] bytes = bis.readAllBytes();

			JSONParser p = new JSONParser();

			return (JSONArray) p.parse(new String(bytes));

		} catch (Exception ex) {

			ex.printStackTrace();

			return new JSONArray();

		} finally {

			close(bis);

			disconnect(conn);

		}

	}

	/**
	 * 搜索记录
	 * 
	 * @param indexName
	 * @param row
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public JSONArray search(String indexName, JSONObject row)
	{

		if (indexName == null || row == null) {

			return new JSONArray();

		}

		String _indexName = indexName.trim().toLowerCase();

		if (!_indexName.matches("[0-9a-z][0-9a-z_]*")) {

			return new JSONArray();

		}

		HttpURLConnection conn = null;

		InputStream bis = null;

		OutputStream bos = null;

		try {

			conn = (HttpURLConnection) getURL("/" + _indexName + "/_search").openConnection();

			conn.setRequestMethod("POST");

			conn.setRequestProperty("Content-Type", "application/json");

			conn.setDoOutput(true);

			conn.setConnectTimeout(TIME_OUT);

			conn.setReadTimeout(TIME_OUT);

			conn.connect();

			bos = conn.getOutputStream();

			JSONObject match = new JSONObject();

			match.put("match", row);

			JSONObject search = new JSONObject();

			search.put("query", match);

			bos.write(search.toJSONString().getBytes());

			bos.flush();

			int code = conn.getResponseCode();

			if (code != 200) {

				return new JSONArray();

			}

			bis = conn.getInputStream();

			byte[] bytes = bis.readAllBytes();

			JSONParser p = new JSONParser();

			JSONObject j = (JSONObject) p.parse(new String(bytes));

			return (JSONArray) ((JSONObject) j.get("hits")).get("hits");

		} catch (Exception ex) {

			ex.printStackTrace();

			return new JSONArray();

		} finally {

			close(bos);

			close(bis);

			disconnect(conn);

		}

	}

	/**
	 * 搜索记录
	 * 
	 * @param indexName
	 * @param key1
	 * @param value1
	 * @param key2
	 * @param value2
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public JSONArray search(String indexName, String key1, String value1)
	{

		if (key1 == null || value1 == null) {

			return new JSONArray();

		}

		String _key1 = key1.trim();

		if (!_key1.matches(".+")) {

			return new JSONArray();

		}

		String _value1 = value1.trim();

		JSONObject row = new JSONObject();

		row.put(_key1, _value1);

		return search(indexName, row);

	}

	/**
	 * 测试
	 */
	public void test()
	{

		String indexName = "review";

		System.out.println(insert(indexName, 2, "title", "Hello World 2", "content", "Hello Movie 2"));

		System.out.println(get(indexName, 2));

		System.out.println(list(indexName));

//
//		System.out.println(delete(review, 1));
//
//		System.out.println(get(review, 1));

//		System.out.println(getIndex(review));
//
//		System.out.println(deleteIndex(review));

//
//		System.out.println(createIndex(review));
//
//		System.out.println(getIndex(review));
//
//		System.out.println(getIndex(review));
//
//		System.out.println(listIndex());
//
		// System.out.println(insert(review, "1", "title", "Hello World", "content", "Hello Movie"));
//
//		System.out.println(search(review, "content", "Movie"));
//
//		System.out.println(search(review, "title", "Movie"));

	}

}
