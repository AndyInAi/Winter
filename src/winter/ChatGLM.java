package winter;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class ChatGLM {

	public static String CHATGLM_URL = "http://192.168.1.60/";

	public static int CONN_TIME_OUT = 6000;

	public static int READ_TIME_OUT = 60000;

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

		System.out.println("Start ...");

		ChatGLM chatGLM = new ChatGLM();

		JSONObject o = chatGLM.chat("你好，我叫石大大，我的大学初恋女友林青霞嫁人了，请帮我给她写一封信");

		System.out.println("response:\n" + o.toJSONString());

	}

	/**
	 * 会话请求
	 * 
	 * @param prompt
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public JSONObject chat(String prompt)
	{

		String _prompt = (prompt == null) ? "" : prompt.trim();

		if (_prompt.length() == 0) {

			System.out.println("_prompt: " + _prompt);

			return null;

		}

		JSONObject req = new JSONObject();

		req.put("prompt", _prompt);

		req.put("history", new JSONArray());

		HttpURLConnection conn = null;

		InputStream bis = null;

		OutputStream bos = null;

		try {

			conn = (HttpURLConnection) getURL().openConnection();

			conn.setRequestMethod("POST");

			conn.setRequestProperty("Content-Type", "application/json");

			conn.setDoOutput(true);

			conn.setConnectTimeout(CONN_TIME_OUT);

			conn.setReadTimeout(READ_TIME_OUT);

			conn.connect();

			bos = conn.getOutputStream();

			bos.write(req.toJSONString().getBytes());

			bos.flush();

			int code = conn.getResponseCode();

			if (code != 200) {

				System.out.println("code: " + code);

				return null;

			}

			bis = conn.getInputStream();

			byte[] bytes = bis.readAllBytes();

			JSONParser p = new JSONParser();

			JSONObject j = (JSONObject) p.parse(new String(bytes));

			return j;

		} catch (Exception ex) {

			ex.printStackTrace();

			return null;

		} finally {

			close(bos);

			close(bis);

			disconnect(conn);

		}

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
	 * 根据 path 生成 url
	 * 
	 * @param path
	 * @return
	 */
	private URL getURL()
	{

		try {

			return new URL(CHATGLM_URL);

		} catch (MalformedURLException ex) {

			ex.printStackTrace();

		}

		return null;

	}

	/**
	 * 测试
	 */
	public void test()
	{

	}

}
