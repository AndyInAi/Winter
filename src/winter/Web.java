package winter;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.kafka.common.Uuid;
import org.json.simple.JSONObject;

public class Web {

	/**
	 * PDF 根目录
	 */
	public static String PDF_DIR = "/mnt/gluster-gv0/pdf";

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
	@SuppressWarnings({})
	public static void main(String[] args) {

		Web web = new Web();

		// web.elasticImportReview();

		web.redisImportReview();
		
	}

	public Database db = null;

	public Elastic elastic = null;

	public Redis redis = null;

	public Random random = null;

	public Web() {

		db = new Database();

		elastic = new Elastic();

		redis = new Redis();

		random = new Random();

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
	 * 把 MariaDB 数据库 t_review 表里的部分数据，导出到 ElasticSearch
	 */
	@SuppressWarnings({ "rawtypes" })
	public boolean elasticImportReview() {

		String sql = "SELECT * FROM t_review LIMIT 1000";

		ArrayList rows = null;

		try {

			rows = db.select(sql);

		} catch (SQLException ex) {

			ex.printStackTrace();

			return false;

		}

		int size = rows.size();

		System.out.println("把 MariaDB 数据库 t_review 表里的部分数据，导出到 ElasticSearch ......");

		for (int i = 0; i < size; i++) {

			HashMap row = (HashMap) rows.get(i);

			boolean ok = elastic.insert("t_review", (long) row.get("ID"), "review", (String) row.get("REVIEW"));

			System.out.print('#');

			if (!ok) {

				return false;

			}

		}

		System.out.println("\n导出完成");

		return true;

	}

	public String genToken() {

		return DigestUtils.md5Hex(random.nextInt(1, Integer.MAX_VALUE) + "-" + random.nextInt(1, Integer.MAX_VALUE));

	}

	/**
	 * 获取自己的用户信息
	 * 
	 * @param request
	 * @param session
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public String getMyUserInfo(HttpServletRequest request, HttpSession session) {

		long user_id = (session.getAttribute("id") == null ? 0 : (long) session.getAttribute("id"));

		HashMap result = new HashMap();

		result.put("login", false);

		result.put("result", true);

		if (user_id < 1) {

			return JSONObject.toJSONString(result);

		}

		HashMap map = null;

		try {

			map = db.get("t_user", "id", user_id);

		} catch (SQLException ex) {

			ex.printStackTrace();

			result.put("result", false);

			return JSONObject.toJSONString(result);

		}

		if (map == null) {

			return JSONObject.toJSONString(result);

		}

		result.put("login", true);

		result.put("id", map.get("ID"));

		result.put("name", map.get("NAME"));

		result.put("nick", map.get("NICK"));

		return JSONObject.toJSONString(result);

	}

	/**
	 * 获取用户列表
	 * 
	 * @param request
	 * @param session
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String listUser(HttpServletRequest request, HttpSession session) {

		HashMap result = new HashMap();

		if (session.getAttribute("id") == null) {

			result.put("login", false);

			return JSONObject.toJSONString(result);

		}

		result.put("login", true);

		String sql = "SELECT id, name, nick, SUBSTRING(create_time, 3, 14) create_time FROM t_user ORDER BY  id DESC LIMIT 20";

		ArrayList rows = null;

		try {

			rows = db.select(sql);

		} catch (SQLException ex) {

			ex.printStackTrace();

			result.put("result", false);

			return JSONObject.toJSONString(result);

		}

		result.put("result", true);

		result.put("users", rows);

		return JSONObject.toJSONString(result);

	}

	
	/**
	 * 生成 PDF 文件
	 * 
	 * @param request
	 * @param session
	 * @return
	 */
	public String makePDF(HttpServletRequest request, HttpSession session) {		
					
		return makePDF(request.getParameter("url"));		
		
	}
	
	/**
	 * 生成 PDF 文件
	 * 
	 * @param url 生成 pdf 文件的 URL
	 * @return
	 */
	public String makePDF(String url) {

		if (url == null || (url = url.trim()).length() == 0) {

			return null;

		}

		String uuid = Uuid.randomUuid().toString();

		OutputStream bos = null;

		try {

			bos = new BufferedOutputStream(new FileOutputStream(new File(PDF_DIR + "/taks", uuid)));

			bos.write(url.getBytes());

		} catch (Exception ex) {

			ex.printStackTrace();

			return null;

		} finally {

			close(bos);

		}

		File pdf = new File(PDF_DIR + "/out", uuid + ".pdf");

		for (int i = 0; i < 4; i++) {

			sleep(random.nextInt(500, 3000));

			if (pdf.exists()) {

				return "/pdf/out/" + uuid + ".pdf";

			}

		}

		return null;

	}

	/**
	 * 把 MariaDB 数据库 t_review 表里的部分数据，导出到 Redis
	 */
	@SuppressWarnings({ "rawtypes" })
	public boolean redisImportReview() {

		String sql = "SELECT * FROM t_review LIMIT 1000";

		ArrayList rows = null;

		try {

			rows = db.select(sql);

		} catch (SQLException ex) {

			ex.printStackTrace();

			return false;

		}

		int size = rows.size();

		System.out.println("把 MariaDB 数据库 t_review 表里的部分数据，导出到 Redis ......");

		for (int i = 0; i < size; i++) {

			HashMap row = (HashMap) rows.get(i);

			redis.set("review:" + row.get("ID"), (String) row.get("REVIEW"));

			System.out.print('#');

		}

		System.out.println("\n导出完成");

		return true;

	}

	/**
	 * 登录
	 * 
	 * @param request
	 * @param session
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String signIn(HttpServletRequest request, HttpSession session) {

		HashMap result = new HashMap();

		result.put("result", false);

		String name = request.getParameter("name");

		String password = request.getParameter("password");

		if (name == null || password == null || (name = name.trim()).length() == 0 || (password = password.trim()).length() == 0) {

			return JSONObject.toJSONString(result);

		}

		HashMap row = null;

		try {

			row = db.get("t_user", "name", name);

		} catch (SQLException ex) {

			ex.printStackTrace();

		}

		if (row != null) {

			String passwordMD5 = DigestUtils.md5Hex(password);

			String _password = (String) row.get("PASSWORD");

			if (_password.equals(passwordMD5)) {

				session.setAttribute("id", row.get("ID"));

				session.setAttribute("name", row.get("NAME"));

				result.put("id", row.get("ID"));

				result.put("name", row.get("NAME"));

				result.put("result", true);

			}

		}

		return JSONObject.toJSONString(result);

	}

	/**
	 * 退出登录
	 * 
	 * @param request
	 * @param session
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public String signOut(HttpServletRequest request, HttpSession session) {

		session.invalidate();

		HashMap result = new HashMap();

		result.put("login", false);

		result.put("result", true);

		return JSONObject.toJSONString(result);

	}

	/**
	 * 注册
	 * 
	 * @param request
	 * @param session
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String signUp(HttpServletRequest request, HttpSession session) {

		HashMap result = new HashMap();

		result.put("result", false);

		String name = request.getParameter("name");

		String password = request.getParameter("password");

		if (name == null || password == null || (name = name.trim()).length() == 0 || (password = password.trim()).length() == 0) {

			return JSONObject.toJSONString(result);

		}

		HashMap row = null;

		try {

			row = db.get("t_user", "name", name);

		} catch (SQLException ex) {

			ex.printStackTrace();

		}

		if (row != null) {

			result.put("info", "用户名已存在");

			return JSONObject.toJSONString(result);

		}

		row = new HashMap();

		row.put("name", name);

		row.put("password", DigestUtils.md5Hex(password));

		row.put("create_time", new Timestamp(System.currentTimeMillis()));

		try {

			db.insert("t_user", row);

		} catch (SQLException ex) {

			ex.printStackTrace();

			result.put("info", "系统错误");

			return JSONObject.toJSONString(result);

		}

		result.put("result", true);

		return JSONObject.toJSONString(result);

	}

	/**
	 * 休眠
	 * 
	 * @param ms
	 */
	public void sleep(long ms) {

		try {

			Thread.sleep(random.nextInt(1000));

		} catch (InterruptedException ex) {

			ex.printStackTrace();

		}

	}

}
