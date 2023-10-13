
# Winter
一个百倍开发速度，每天轻松处理十亿请求的 Java Web 架构，告别垃圾类，让开发人员 99% 的精力用于业务。


### 快速预览


## MariaDB

#### 增加记录
	
	row = new HashMap();

	row.put("name", name);

	row.put("password", DigestUtils.md5Hex(password));

	row.put("create_time", new Timestamp(System.currentTimeMillis()));

	db.insert("t_user", row);

#### 删除记录

	db.delete("t_user", "id", 2);

#### 修改记录

	db.update("t_user", "nick", "石大大", "id", 2);

#### 查询记录

	String sql = "SELECT id, name, nick, SUBSTRING(create_time, 3, 14) create_time FROM t_user ORDER BY  id DESC LIMIT 20";

	ArrayList rows = db.select(sql);

	result.put("result", true);

	result.put("users", rows);

	return JSONObject.toJSONString(result);

##### 查询结果

	{
	  "result": true,
	  "login": true,
	  "users": [
	    {
	      "NICK": "石大大2000000",
	      "ID": 2000000,
	      "CREATE_TIME": "23-09-13 20:30",
	      "NAME": "andy2000000"
	    },
	    {
	      "NICK": "石大大1999999",
	      "ID": 1999999,
	      "CREATE_TIME": "23-09-13 20:30",
	      "NAME": "andy1999999"
	    },
	    {
	      "NICK": "石大大1999981",
	      "ID": 1999981,
	      "CREATE_TIME": "23-09-13 20:30",
	      "NAME": "andy1999981"
	    }
	  ]
	}	


## Redis

#### 设置值

	redis.set("hello", "石大大");

#### 取值

	redis.get("hello");

#### 设置 Map

	hash = new HashMap();

	hash.put("id", "2");

	hash.put("name", "Andy");

	hash.put("nick", "石大大");

	redis.hset("session:2", hash);

#### 取 Map

	redis.hgetAll("session:2");

#### 取 Map 结果

	{nick=石大大, name=Andy, id=2}

#### 取 Map 并转换为 JSON 字符串

	redis.hgetJson("session:2");

#### 取 Map 并转换为 JSON 字符串结果

	{"nick":"石大大","name":"Andy","id":"2"}


## ElasticSearch

#### 增加记录
	
	elastic.insert("t_review", 9999, "review", "Hello Movie!");

#### 删除记录

	elastic.delete("t_review", 9999);

#### 获取记录

	elastic.get("t_review", 9999);

#### 修改记录

	elastic.insert("t_review", 9999, "review", "Hello Movie World!");

#### 搜索记录

	elastic.search("t_review", "review", "Movie");

##### 搜索结果

	[
	    {
		"_index": "t_review",
		"_source": {
		    "review": "Hello Movie!"
		},
		"_id": "9999",
		"_score": 0.2876821
	    }
	]


## Kafka

#### 发送消息

		Kafka kf = new Kafka();

		String topic = "test_topic_888";

		RecordMetadata meta = kf.write(topic, topic + " Hello " + new java.util.Date());

#### 接收消息

	ConsumerRecords<Integer, String> records = kf.read(topic);

	for (ConsumerRecord<Integer, String> record : records) {

		System.out.println(record);

	}


## MariaDB & Redis

#### 从 MariaDB 导出数据到 Redis

	String sql = "SELECT * FROM t_review LIMIT 1000";

	ArrayList rows = db.select(sql);

	int size = rows.size();

	for (int i = 0; i < size; i++) {

		HashMap row = (HashMap) rows.get(i);

		redis.set("review:" + row.get("ID"), (String) row.get("REVIEW"));
	
	}


## MariaDB & ElasticSearch

#### 从 MariaDB 导出数据到 ElasticSearch

	String sql = "SELECT * FROM t_review LIMIT 1000";

	ArrayList rows = db.select(sql);

	int size = rows.size();

	for (int i = 0; i < size; i++) {

		HashMap row = (HashMap) rows.get(i);

		elastic.insert("t_review", (long) row.get("ID"), "review", (String) row.get("REVIEW"));
	
	}


## 人工智能绘画

	SDWebUi sdWebUi = new SDWebUi();

	JSONObject o = sdWebUi.text2img("8k, high detail, sea, beach, girl, detailed face", "logo, text", 9);

##### 生成结果

![image](https://github.com/AndyInAi/Winter/blob/main/img/00000-1430822278.png)
![image](https://github.com/AndyInAi/Winter/blob/main/img/00002-1430822280.png)
![image](https://github.com/AndyInAi/Winter/blob/main/img/00003-1430822281.png)
![image](https://github.com/AndyInAi/Winter/blob/main/img/00004-1430822282.png)
![image](https://github.com/AndyInAi/Winter/blob/main/img/00005-1430822283.png)
![image](https://github.com/AndyInAi/Winter/blob/main/img/00006-1430822284.png)
![image](https://github.com/AndyInAi/Winter/blob/main/img/00007-1430822285.png)
![image](https://github.com/AndyInAi/Winter/blob/main/img/00008-1430822286.png)

	sdWebUi.text2img("8k, high detail, sea, beach, moon, diamond, girl, detailed face", "logo, text", 9);

##### 生成结果

![image](https://github.com/AndyInAi/Winter/blob/main/img/00077-3078859337.png)
![image](https://github.com/AndyInAi/Winter/blob/main/img/00078-3078859338.png)
![image](https://github.com/AndyInAi/Winter/blob/main/img/00079-3078859339.png)
![image](https://github.com/AndyInAi/Winter/blob/main/img/00080-3078859340.png)
![image](https://github.com/AndyInAi/Winter/blob/main/img/00081-3078859341.png)
![image](https://github.com/AndyInAi/Winter/blob/main/img/00082-3078859342.png)
![image](https://github.com/AndyInAi/Winter/blob/main/img/00083-3078859343.png)
![image](https://github.com/AndyInAi/Winter/blob/main/img/00084-3078859344.png)
![image](https://github.com/AndyInAi/Winter/blob/main/img/00085-3078859345.png)


## 人工智能会话

	ChatGLM chatGLM = new ChatGLM();

	JSONObject o = chatGLM.chat("你好，我叫石大大，我的大学初恋女友林青霞嫁人了，请帮我给她写一封信");

#### 会话结果

{
    "response": "亲爱的林青霞,\n\n我希望这封信能够传达出我真挚的祝福和感激之情。当我听到你的婚讯时,我感到非常惊讶和高兴。我知道你一直是我心中最珍贵的人,我们的初恋更是我心中一段美好的回忆。\n\n我从未忘记我们曾经一起度过的美好时光,那些无忧无虑的夜晚,以及你对我的爱和关怀。如今,我已经步入了成熟和稳定的婚姻生活,开始了新的生 命旅程。我也深刻理解到婚姻和爱情的责任和挑战,需要我们共同去面对和承担。\n\n我希望你和你的丈夫能够幸福美满,相互扶持,共同迎接未来的挑战。我也希望你能够在家庭和工作中继续保持你的热情和才华,继续实现你的梦想和理想。\n\n再次祝福你,希望你一切安好。\n\n爱你\n\n石大大",
    "history": [
    ],
    "time": "2023-09-27 11:29:02",
    "status": 200
}

#### 会话结果 response 内容

	亲爱的林青霞,

	我希望这封信能够传达出我真挚的祝福和感激之情。当我听到你的婚讯时,我感到非常惊讶和高兴。

	我知道你一直是我心中最珍贵的人,我们的初恋更是我心中一段美好的回忆。

	我从未忘记我们曾经一起度过的美好时光,那些无忧无虑的夜晚,以及你对我的爱和关怀。如今,我已

	经步入了成熟和稳定的婚姻生活,开始了新的生 命旅程。我也深刻理解到婚姻和爱情的责任和挑战,

	需要我们共同去面对和承担。

	我希望你和你的丈夫能够幸福美满,相互扶持,共同迎接未来的挑战。我也希望你能够在家庭和工作

	中继续保持你的热情和才华,继续实现你的梦想和理想。

	再次祝福你,希望你一切安好。

	爱你

	石大大


***


### 服务端开发

	一个方法 + 一个 JSP 文件

##### 以获取自己的用户信息为例：

#### 1) 在 Web.java 内创建一个方法，实现功能
	
	// 参数包括 HttpServletRequest 和 HttpSession
	
	// 返回 JSON 字符串，key 至少包括：login 表示是否登录，result 表示是否执行正常

	/**
	 * 获取自己的用户信息
	 * 
	 * @param request
	 * @param session
	 * @return
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
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

#### 2) 创建一个 JSP 文件 /get_my_userinfo.jsp，实例化 Web.java ，调用方法，返回 JSON

	<%@ page contentType="application/json; charset=utf-8"  %>
	<jsp:useBean id="webBean" scope="session" class="winter.Web" />
	<%= webBean.signIn(request, session) %>


### 开发完了？

### 对，完了！


***


### 客户端开发

##### 以获取自己的用户信息为例：

	<script src="/js/jquery.js" type="text/javascript"></script>

	<script type="text/javascript">

		$(document).ready(function(e) {
		
			$.getJSON("/get_my_userinfo.jsp?r=" + Math.random() , function(response) {
				
				if ( response.login == false ) {
					
					window.location = "/sign-in/?r=" + Math.random();
				
					return false;
						
				}
				
				$("#my_name").text(response.name);
				
			});
			
		});

	</script>


### 又开发完了？

### 对，又完了！


***


### 开发一个每天生成 100 万个 PDF 文件的系统；计划 3 个高级软件工程师 996 开发 3 个月

	//  一个方法

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

	// 一个 JSP 文件 /make_pdf.jsp

	<%@ page contentType="application/json; charset=utf-8"  %>
	<jsp:useBean id="webBean" scope="session" class="winter.Web" />
	<%= webBean.makePDF(request, session) %>


***


### 又又开发完了？

### 对，又又完了！


***


### 准备测试数据

#### 创建数据库 winter 及表 t_user 

	CREATE DATABASE winter /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci */;

	USE winter;

	-- DROP TABLE IF EXISTS winter.t_user;

	CREATE TABLE t_user (
	  id int(10) unsigned NOT NULL AUTO_INCREMENT,
	  name varchar(20) NOT NULL,
	  create_time timestamp NOT NULL DEFAULT current_timestamp(),
	  password varchar(32) NOT NULL,
	  nick varchar(20) DEFAULT NULL,
	  intro varchar(1000) DEFAULT NULL,
	  PRIMARY KEY (id)
	) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

	-- 增加一个测试用户 andy 密码为 winter

	INSERT INTO t_user (id,name,create_time,password,nick,intro) VALUES  (1,'andy','2023-09-13 20:30:40','f6432274349b5cb93433f8ed886a3f37','石大大',NULL);

#### 增加一百万个测试用户

<https://github.com/AndyInAi/Winter/blob/main/bash/t_user_1m>

	cd bash

	bash t_user_1m

#### 增加 5 万个测试文本数据

<https://github.com/AndyInAi/Winter/tree/main/sql>

#### 解压
	gzip -d winter_t_review.sql.gz

#### 导入 MariaDB 数据库 winter，表名为 t_review
	time mysql -e "\. winter_t_review.sql" -h localhost -u root -pwinter winter


### MariaDB 集群安装配置 

<https://github.com/AndyInAi/Winter/blob/main/doc/MariaDB%20%E9%9B%86%E7%BE%A4%E5%AE%89%E8%A3%85%E9%85%8D%E7%BD%AE.txt>


### Redis 集群安装配置

<https://github.com/AndyInAi/Winter/blob/main/doc/Redis%20%E9%9B%86%E7%BE%A4%E5%AE%89%E8%A3%85%E9%85%8D%E7%BD%AE.txt>


### ElasticSearch 集群安装配置
<https://github.com/AndyInAi/Winter/blob/main/doc/ElasticSearch%20%E9%9B%86%E7%BE%A4%E5%AE%89%E8%A3%85%E9%85%8D%E7%BD%AE.txt>


### Resin 集群安装配置
<https://github.com/AndyInAi/Winter/blob/main/doc/Resin%20%E9%9B%86%E7%BE%A4%E5%AE%89%E8%A3%85%E9%85%8D%E7%BD%AE.md>


### K8S 集群安装配置
<https://github.com/AndyInAi/Winter/blob/main/doc/K8S%20%E9%9B%86%E7%BE%A4%E5%AE%89%E8%A3%85%E9%85%8D%E7%BD%AE.md>


### 人工智能绘画集群安装配置
<https://github.com/AndyInAi/Winter/blob/main/doc/%E4%BA%BA%E5%B7%A5%E6%99%BA%E8%83%BD%E7%BB%98%E7%94%BB%E9%9B%86%E7%BE%A4%E5%AE%89%E8%A3%85%E9%85%8D%E7%BD%AE.md>


### 人工智能会话集群安装配置
<https://github.com/AndyInAi/Winter/blob/main/doc/%E4%BA%BA%E5%B7%A5%E6%99%BA%E8%83%BD%E4%BC%9A%E8%AF%9D%E9%9B%86%E7%BE%A4%E5%AE%89%E8%A3%85%E9%85%8D%E7%BD%AE.md>


### 人工智能语音集群安装配置
<https://github.com/AndyInAi/Winter/blob/main/doc/%E4%BA%BA%E5%B7%A5%E6%99%BA%E8%83%BD%E8%AF%AD%E9%9F%B3%E9%9B%86%E7%BE%A4%E5%AE%89%E8%A3%85%E9%85%8D%E7%BD%AE.md>


### Kafka 集群安装配置
<https://github.com/AndyInAi/Winter/blob/main/doc/Kafka%20%E9%9B%86%E7%BE%A4%E5%AE%89%E8%A3%85%E9%85%8D%E7%BD%AE.md>


### Flink 集群安装配置
<https://github.com/AndyInAi/Winter/blob/main/doc/Flink%20%E9%9B%86%E7%BE%A4%E5%AE%89%E8%A3%85%E9%85%8D%E7%BD%AE.md>


### GlusterFS 集群安装配置
<https://github.com/AndyInAi/Winter/blob/main/doc/GlusterFS%20%E9%9B%86%E7%BE%A4%E5%AE%89%E8%A3%85%E9%85%8D%E7%BD%AE.md>


### PDF 文件生成服务集群安装配置
<https://github.com/AndyInAi/Winter/blob/main/doc/PDF%20%E6%96%87%E4%BB%B6%E7%94%9F%E6%88%90%E6%9C%8D%E5%8A%A1%E9%9B%86%E7%BE%A4%E5%AE%89%E8%A3%85%E9%85%8D%E7%BD%AE.md>


### FFmpeg 集群安装配置
<https://github.com/AndyInAi/Winter/blob/main/doc/FFmpeg%20%E9%9B%86%E7%BE%A4%E5%AE%89%E8%A3%85%E9%85%8D%E7%BD%AE.md>


### 全部代码在 Ubuntu 22.04.3 LTS 测试通过

	PRETTY_NAME="Ubuntu 22.04.3 LTS"
	NAME="Ubuntu"
	VERSION_ID="22.04"
	VERSION="22.04.3 LTS (Jammy Jellyfish)"
	VERSION_CODENAME=jammy
	ID=ubuntu
	ID_LIKE=debian
	HOME_URL="https://www.ubuntu.com/"
	SUPPORT_URL="https://help.ubuntu.com/"
	BUG_REPORT_URL="https://bugs.launchpad.net/ubuntu/"
	PRIVACY_POLICY_URL="https://www.ubuntu.com/legal/terms-and-policies/privacy-policy"
	UBUNTU_CODENAME=jammy


