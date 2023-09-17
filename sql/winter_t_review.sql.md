
### 使用 winter_t_review.sql.gz 说明

#### 解压
		gzip -d winter_t_review.sql.gz

#### 导入 MariaDB 数据库 winter，表名为 t_review
		time mysql -e "\. winter_t_review.sql" -h localhost -u root -pwinter winter

