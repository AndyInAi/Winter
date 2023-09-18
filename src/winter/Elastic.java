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

	private void disconnect(HttpURLConnection conn)
	{

		if (conn != null) {

			conn.disconnect();

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
	 * 添加文本数据
	 * 
	 * @param indexName
	 * @param id
	 * @param text
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public boolean insertText(String indexName, String id, String text)
	{

		if (indexName == null || id == null || text == null) {

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

		String _text = text.trim();

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

			JSONObject row = new JSONObject();

			row.put("text", _text);

			bos.write(row.toJSONString().getBytes());

			bos.flush();

			int code = conn.getResponseCode();

			if (code != 200) {

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

			if (bis != null) {

				try {

					bis.close();

				} catch (IOException ex) {

					ex.printStackTrace();

				}

			}

			if (conn != null) {

				conn.disconnect();

			}

		}

	}

	/**
	 * 测试
	 */
	public void test()
	{

		String review = "review";

//		 System.out.println(deleteIndex(index_test));

//		System.out.println(getIndex(review));
//
//		System.out.println(createIndex(review));
//
//		System.out.println(getIndex(review));
//
//		System.out.println(listIndex());

		System.out.println(insertText(review, "1", "Hello World"));

	}

}
