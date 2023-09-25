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

public class SDWebUi {

	public static String SDWEBUI_URL = "http://127.0.0.1:7860/sdapi/v1/txt2img";

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

		SDWebUi sdWebUi = new SDWebUi();

		JSONObject o = sdWebUi.text2img("8k, high detail, sea, beach,   girl, detailed face", "logo, text", 3);

		System.out.println("response:\n" + o.toJSONString());

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

			return new URL(SDWEBUI_URL);

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

	/**
	 * 文本生成图像
	 * 
	 * @param prompt          提示词
	 * @param negative_prompt 屏蔽提示词
	 * @param count           生成图像数量
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public JSONObject text2img(String prompt, String negative_prompt, int count)
	{

		String _prompt = (prompt == null) ? "" : prompt.trim();

		String _negative_prompt = negative_prompt == null ? "" : negative_prompt.trim();

		if (_prompt.length() == 0) {

			return null;

		}

		int _count = count > 9 ? 9 : count < 1 ? 1 : count;

		JSONObject req = new JSONObject();

		req.put("prompt", _prompt);
		req.put("negative_prompt", _negative_prompt);
		req.put("styles", new JSONArray());
		req.put("seed", -1);
		req.put("subseed", -1);
		req.put("subseed_strength", 0);
		req.put("seed_resize_from_h", -1);
		req.put("seed_resize_from_w", -1);
		req.put("sampler_name", "");
		req.put("batch_size", _count);
		req.put("n_iter", 1);
		req.put("steps", 50);
		req.put("cfg_scale", 7);
		req.put("width", 512);
		req.put("height", 512);
		req.put("restore_faces", true);
		req.put("tiling", true);
		req.put("do_not_save_samples", false);
		req.put("do_not_save_grid", false);
		req.put("eta", 0);
		req.put("denoising_strength", 0);
		req.put("s_min_uncond", 0);
		req.put("s_churn", 0);
		req.put("s_tmax", 0);
		req.put("s_tmin", 0);
		req.put("s_noise", 0);
		req.put("override_settings", new JSONObject());
		req.put("override_settings_restore_afterwards", true);
		req.put("refiner_checkpoint", "");
		req.put("refiner_switch_at", 0);
		req.put("disable_extra_networks", false);
		req.put("comments", new JSONObject());
		req.put("enable_hr", false);
		req.put("firstphase_width", 0);
		req.put("firstphase_height", 0);
		req.put("hr_scale", 2);
		req.put("hr_upscaler", "");
		req.put("hr_second_pass_steps", 0);
		req.put("hr_resize_x", 0);
		req.put("hr_resize_y", 0);
		req.put("hr_checkpoint_name", "majicmixRealistic_betterV2V25");
		req.put("hr_sampler_name", "");
		req.put("hr_prompt", "");
		req.put("hr_negative_prompt", "");
		req.put("sampler_index", "DPM++ 2M Karras");
		req.put("script_name", "");
		req.put("script_args", new JSONArray());
		req.put("send_images", true);
		req.put("save_images", false);
		req.put("alwayson_scripts", new JSONObject());

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
