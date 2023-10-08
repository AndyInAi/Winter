package winter;

import java.io.Closeable;
import java.io.File;
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

public class SDWebUi {

	public static String SDWEBUI_URL = "http://192.168.1.10/sdapi/v1/txt2img";

	public static int CONN_TIME_OUT = 6000;

	public static int READ_TIME_OUT = 60000;

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

		log("Start ...");

		SDWebUi sdWebUi = new SDWebUi();

		JSONObject txt2img = sdWebUi.txt2Img("8k, high detail, sea, beach, girl, detailed face", "logo, text", 2);

		File[] files = sdWebUi.saveImg(txt2img);

		log(files.length);

		for (int i = 0; i < files.length; i++) {

			log(files[i].getAbsolutePath());

		}

	}

	public Image image = null;

	public SDWebUi() {

		image = new Image();

	}

	/**
	 * 关闭
	 * 
	 * @param o
	 */
	private void close(Closeable o) {

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
	private void disconnect(HttpURLConnection conn) {

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
	private URL getURL() {

		try {

			return new URL(SDWEBUI_URL);

		} catch (MalformedURLException ex) {

			ex.printStackTrace();

		}

		return null;

	}

	/**
	 * 把文本生成图像返回的 JSON 对象，其中的图像 base64 编码转换并保存为图像文件
	 * 
	 * @param txt2img
	 * @return
	 */
	public File[] saveImg(JSONObject txt2img) {

		if (txt2img == null) {

			return null;

		}

		Object images = txt2img.get("images");

		if (images == null || !(images instanceof JSONArray)) {

			return null;

		}

		JSONArray _images = (JSONArray) images;

		int size = _images.size();

		File[] files = new File[size];

		for (int i = 0; i < size; i++) {

			files[i] = image.base64ToImage((String) _images.get(i));

		}

		return files;

	}

	/**
	 * 文本生成图像
	 * 
	 * @param prompt
	 *            提示词
	 * @param negative_prompt
	 *            屏蔽提示词
	 * @param count
	 *            生成图像数量
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public JSONObject txt2Img(String prompt, String negative_prompt, int count) {

		String _prompt = (prompt == null) ? "" : prompt.trim();

		String _negative_prompt = negative_prompt == null ? "" : negative_prompt.trim();

		if (_prompt.length() == 0) {

			return null;

		}

		int _count = count > 9 ? 9 : count < 1 ? 1 : count;

		JSONObject req = new JSONObject();

		req.put("prompt", _prompt);

		req.put("negative_prompt", _negative_prompt);

		req.put("batch_size", _count);

		req.put("steps", 50);

		HttpURLConnection conn = null;

		InputStream bis = null;

		OutputStream bos = null;

		try {

			conn = (HttpURLConnection) getURL().openConnection();

			conn.setRequestMethod("POST");

			conn.setRequestProperty("Content-Type", "application/json");

			conn.setDoOutput(true);

			conn.setConnectTimeout(CONN_TIME_OUT);

			conn.setReadTimeout(READ_TIME_OUT * count);

			conn.connect();

			bos = conn.getOutputStream();

			bos.write(req.toJSONString().getBytes());

			bos.flush();

			int code = conn.getResponseCode();

			if (code != 200) {

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

}
