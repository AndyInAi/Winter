package winter;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.digest.DigestUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;
import org.json.simple.parser.JSONParser;

public class Web {

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
	@SuppressWarnings({})
	public static void main(String[] args)
	{

		Web web = new Web();

		web.elasticImportReview();

	}

	public Database db = null;

	public Elastic elastic = null;

	Random random = null;

	public Web()
	{

		db = new Database();

		elastic = new Elastic();

		random = new Random();

	}

	/**
	 * 把 MariaDB 数据库 t_review 表里的部分数据，导出到 ElasticSearch
	 */
	@SuppressWarnings({ "rawtypes" })
	public boolean elasticImportReview()
	{

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

	public String genToken()
	{

		return DigestUtils.md5Hex(random.nextInt(1, Integer.MAX_VALUE) + "-" + random.nextInt(1, Integer.MAX_VALUE));

	}

	/**
	 * 获取用户列表
	 * 
	 * @param request
	 * @param session
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String listUser(HttpServletRequest request, HttpSession session)
	{

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
	 * 登录
	 * 
	 * @param request
	 * @param session
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String signIn(HttpServletRequest request, HttpSession session)
	{

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
	 * 注册
	 * 
	 * @param request
	 * @param session
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String signUp(HttpServletRequest request, HttpSession session)
	{

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

}
