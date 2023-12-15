
## 存储服务器 RAID10 安装配置


### 硬件准备
	
	24 块 22T 企业级硬盘；设置硬盘控制器位置分别为 1 - 24
	
	如果硬盘存在分区，首先要手工删除全部分区；此操作将删除所有数据


### 安装软件

	yum install mdadm -y


### 生成分区命令文件

	(
		echo 'g
			n
			
			
			
			w' > ~/fdisk.txt
		sed -i "s/\t*//g" ~/fdisk.txt
	)


### 硬盘分区和格式化

	# dmsetup ls

	# dmsetup remove 

	(
		sd='/dev/sdb /dev/sdc /dev/sdd /dev/sde /dev/sdf /dev/sdg /dev/sdh /dev/sdi /dev/sdj /dev/sdk /dev/sdl /dev/sdm /dev/sdn /dev/sdo /dev/sdp /dev/sdq /dev/sdr /dev/sds /dev/sdt /dev/sdu /dev/sdv /dev/sdw /dev/sdx /dev/sdy'

		for i in $sd; do
			echo $i
			fdisk $i < ~/fdisk.txt;
		done
	)


### 创建 RAID10 硬盘

	(
		sd1='/dev/sdb1 /dev/sdc1 /dev/sdd1 /dev/sde1 /dev/sdf1 /dev/sdg1 /dev/sdh1 /dev/sdi1 /dev/sdj1 /dev/sdk1 /dev/sdl1 /dev/sdm1 /dev/sdn1 /dev/sdo1 /dev/sdp1 /dev/sdq1 /dev/sdr1 /dev/sds1 /dev/sdt1 /dev/sdu1 /dev/sdv1 /dev/sdw1 /dev/sdx1 /dev/sdy1'

		mdadm --create /dev/md0 --name=md0 --raid-devices=24 --level=10  $sd1

		mkfs.xfs -f -i size=512 /dev/md0
	)


### 挂载 RAID10 硬盘

	(
		mkdir -p /md0

		if [ "$(grep '^/dev/md0' /etc/fstab)" = "" ]; then
			echo '/dev/md0 /md0 xfs defaults 1 2' >> /etc/fstab
			mount /md0
		fi
	)


### 查看结果

	df -h

	cat /proc/mdstat



