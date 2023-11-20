
# Winter
一个开箱即用的 Web 集群架构



### GlusterFS 集群安装配置
<https://github.com/AndyInAi/Winter/blob/main/doc/GlusterFS%20%E9%9B%86%E7%BE%A4%E5%AE%89%E8%A3%85%E9%85%8D%E7%BD%AE.md>


### K8S 集群安装配置
https://github.com/AndyInAi/Winter/blob/main/doc/K8S%20%E9%9B%86%E7%BE%A4%E5%AE%89%E8%A3%85%E9%85%8D%E7%BD%AE.md


### K8S Spring Boot 集群安装配置
<https://github.com/AndyInAi/Winter/blob/main/doc/K8S%20Spring%20Boot%20%E9%9B%86%E7%BE%A4%E5%AE%89%E8%A3%85%E9%85%8D%E7%BD%AE.md>


### K8S Tomcat 集群安装配置
https://github.com/AndyInAi/Winter/blob/main/doc/K8S%20Tomcat%20%E9%9B%86%E7%BE%A4%E5%AE%89%E8%A3%85%E9%85%8D%E7%BD%AE.md


### K8S Resin 集群安装配置
<https://github.com/AndyInAi/Winter/blob/main/doc/K8S%20Resin%20%E9%9B%86%E7%BE%A4%E5%AE%89%E8%A3%85%E9%85%8D%E7%BD%AE.md>


### K8S PDF 文件生成服务集群安装配置
<https://github.com/AndyInAi/Winter/blob/main/doc/K8S%20PDF%20%E6%96%87%E4%BB%B6%E7%94%9F%E6%88%90%E6%9C%8D%E5%8A%A1%E9%9B%86%E7%BE%A4%E5%AE%89%E8%A3%85%E9%85%8D%E7%BD%AE.md>


### K8S FFmpeg 视频服务集群安装配置
<https://github.com/AndyInAi/Winter/blob/main/doc/K8S%20FFmpeg%20%E8%A7%86%E9%A2%91%E6%9C%8D%E5%8A%A1%E9%9B%86%E7%BE%A4%E5%AE%89%E8%A3%85%E9%85%8D%E7%BD%AE.md>


### Docker Whisper 音频服务集群安装配置
<https://github.com/AndyInAi/Winter/blob/main/doc/Docker%20Whisper%20%E9%9F%B3%E9%A2%91%E6%9C%8D%E5%8A%A1%E9%9B%86%E7%BE%A4.md>


### MariaDB 集群安装配置
<https://github.com/AndyInAi/Winter/blob/main/doc/MariaDB%20%E9%9B%86%E7%BE%A4%E5%AE%89%E8%A3%85%E9%85%8D%E7%BD%AE.md>


### Prometheus 集群安装配置
<https://github.com/AndyInAi/Winter/blob/main/doc/Prometheus%20%E9%9B%86%E7%BE%A4%E5%AE%89%E8%A3%85%E9%85%8D%E7%BD%AE.md>


### 人工智能绘画集群安装配置
<https://github.com/AndyInAi/Winter/blob/main/doc/%E4%BA%BA%E5%B7%A5%E6%99%BA%E8%83%BD%E7%BB%98%E7%94%BB%E9%9B%86%E7%BE%A4%E5%AE%89%E8%A3%85%E9%85%8D%E7%BD%AE.md>


### 人工智能会话集群安装配置
<https://github.com/AndyInAi/Winter/blob/main/doc/%E4%BA%BA%E5%B7%A5%E6%99%BA%E8%83%BD%E4%BC%9A%E8%AF%9D%E9%9B%86%E7%BE%A4%E5%AE%89%E8%A3%85%E9%85%8D%E7%BD%AE.md>


### Redis 集群安装配置

<https://github.com/AndyInAi/Winter/blob/main/doc/Redis%20%E9%9B%86%E7%BE%A4%E5%AE%89%E8%A3%85%E9%85%8D%E7%BD%AE.md>


### ElasticSearch 集群安装配置
<https://github.com/AndyInAi/Winter/blob/main/doc/ElasticSearch%20%E9%9B%86%E7%BE%A4%E5%AE%89%E8%A3%85%E9%85%8D%E7%BD%AE.md>


### Kafka 集群安装配置
<https://github.com/AndyInAi/Winter/blob/main/doc/Kafka%20%E9%9B%86%E7%BE%A4%E5%AE%89%E8%A3%85%E9%85%8D%E7%BD%AE.md>



### 开发测试


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

	JSONObject o = sdWebUi.txt2img("8k, high detail, sea, beach, girl, detailed face", "logo, text", 9);


## 人工智能会话

	ChatGLM chatGLM = new ChatGLM();

	JSONObject o = chatGLM.chat("你好，怎么做才能赚到一个亿呢？");


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


***


### 生成 PDF

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


### 视频处理

	Web web = new Web();

	web.makeMP4("test.mkv");

	# 执行成功生成的文件包括标准视频 .mp4、预览视频 -review.mp4、截图 1.png 至 9.png 等文件


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


