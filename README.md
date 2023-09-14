
# Winter
一个百倍开发速度，每天轻松处理十亿请求的 Java Web 架构，告别垃圾类，让开发人员 99% 的精力用于业务。

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

-- 用户密码 123456


INSERT INTO t_user (id,name,create_time,password,nick,intro) VALUES  (1,'andy','2023-09-13 20:30:40','f6432274349b5cb93433f8ed886a3f37','石大大',NULL);

#### 生成一百万个用户记录

<https://github.com/AndyInAi/Winter/blob/main/bash/t_user_1m>

cd bash

bash t_user_1m

