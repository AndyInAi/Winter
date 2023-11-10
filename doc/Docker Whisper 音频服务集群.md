


### 安装

	(
		export DEBIAN_FRONTEND=noninteractive ; apt update -y; apt install -y docker.io

		curl -fsSL https://nvidia.github.io/libnvidia-container/gpgkey |  gpg --dearmor -o /usr/share/keyrings/nvidia-container-toolkit-keyring.gpg \
		  && curl -s -L https://nvidia.github.io/libnvidia-container/stable/deb/nvidia-container-toolkit.list | \
		    sed 's#deb https://#deb [signed-by=/usr/share/keyrings/nvidia-container-toolkit-keyring.gpg] https://#g' | \
		     tee /etc/apt/sources.list.d/nvidia-container-toolkit.list \
		  && \
		     apt-get update
		
		apt-get install -y nvidia-container-toolkit

		nvidia-ctk runtime configure --runtime=docker

		systemctl restart docker
		
		mkdir -p ~/.cache/whisper;
	)


### 打开容器

	docker run --runtime=nvidia --gpus all -it ubuntu

	# 打开一个新终端查看容器 ID

	# docker ps

	输出例子：

	CONTAINER ID   IMAGE     COMMAND       CREATED          STATUS          PORTS     NAMES
	039d10c9f406   ubuntu    "/bin/bash"   39 minutes ago   Up 39 minutes             unruffled_thompson

	# 039d10c9f406 即为正在运行的容器ID，每隔一段时间切回此终端提交保存，直到容器安装配置完成

	docker commit 039d10c9f406 ubuntu:whisper


### 容器内安装

	(
		echo '
			deb http://mirrors.tuna.tsinghua.edu.cn/ubuntu/ jammy main restricted universe multiverse
			deb http://mirrors.tuna.tsinghua.edu.cn/ubuntu/ jammy-updates main restricted universe multiverse
			deb http://mirrors.tuna.tsinghua.edu.cn/ubuntu/ jammy-backports main restricted universe multiverse
			deb http://mirrors.tuna.tsinghua.edu.cn/ubuntu/ jammy-security main restricted universe multiverse
		' > /etc/apt/sources.list;

		(export DEBIAN_FRONTEND=noninteractive ; apt update -y; apt install -y python3-pip git ffmpeg wget)

		mkdir -p ~/.pip ~/.cache/whisper

		echo '
				[global]
				index-url = https://pypi.tuna.tsinghua.edu.cn/simple
				[install]
				trusted-host=pypi.tuna.tsinghua.edu.cn
		' > ~/.pip/pip.conf;

		pip cache purge

		pip install -U pip

		pip install setuptools-rust

		pip install git+https://github.com/openai/whisper.git 

		pip cache purge
	)

	# 容器内安装完成，切回上一个终端提交保存

	docker commit 039d10c9f406 ubuntu:whisper

	# 然后退出容器


### 测试

	# 在 /root/ 目录下准备一个 test.mp4 文件

	# 打开容器
	docker run --runtime=nvidia --gpus all -v /root:/mnt -v /root/.cache/whisper:/root/.cache/whisper -w /mnt -it ubuntu:whisper 

	# 执行测试
	whisper test.mp4 --model medium --language Chinese


