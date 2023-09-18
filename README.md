
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


### MariaDB 集群安装配置 

<https://github.com/AndyInAi/Winter/blob/main/doc/MariaDB%20%E9%9B%86%E7%BE%A4%E5%AE%89%E8%A3%85%E9%85%8D%E7%BD%AE.txt>


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


### Redis 集群安装配置

<https://github.com/AndyInAi/Winter/blob/main/doc/Redis%20%E9%9B%86%E7%BE%A4%E5%AE%89%E8%A3%85%E9%85%8D%E7%BD%AE.txt>


### ElasticSearch 集群安装配置
<https://github.com/AndyInAi/Winter/blob/main/doc/ElasticSearch%20%E9%9B%86%E7%BE%A4%E5%AE%89%E8%A3%85%E9%85%8D%E7%BD%AE.txt>


### Resin 集群安装配置
<https://github.com/AndyInAi/Winter/blob/main/doc/Resin%20%E9%9B%86%E7%BE%A4%E5%AE%89%E8%A3%85%E9%85%8D%E7%BD%AE.txt>


