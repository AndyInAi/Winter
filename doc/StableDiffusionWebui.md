 
## 人工智能绘画集群安装配置


### 添加普通用户 sd

		adduser sd


### 切换到用户 sd 操作

		su - sd


### 下载文件

		wget -q https://raw.githubusercontent.com/AUTOMATIC1111/stable-diffusion-webui/master/webui.sh

		chmod +x webui.sh


### 安装依赖

		sudo apt install -y wget git python3 python3-venv libgl1 libglib2.0-0 libtcmalloc-minimal4


### 启动 stable-diffusion-webui ；第一次启动将自动进行安装，安装完成后先退出

		./webui.sh --listen  --api --xformers

		没有 GPU 执行 ./webui.sh --listen  --skip-torch-cuda-test --precision full --no-half --use-cpu `nproc` --api 


### 下载 codeformer 

		mkdir -p /home/sd/stable-diffusion-webui/models/Codeformer

		wget https://github.com/sczhou/CodeFormer/releases/download/v0.1.0/codeformer.pth -O /home/sd/stable-diffusion-webui/models/Codeformer/codeformer-v0.1.0.pth


### 负载均衡安装配置

		apt install -y nginx

		if [ "`grep '#include \/etc\/nginx\/sites-enabled\/' /etc/nginx/nginx.conf`" == "" ]; then echo ok; sed -i "s/include \/etc\/nginx\/sites-enabled\//#include \/etc\/nginx\/sites-enabled\//g" /etc/nginx/nginx.conf; fi

		# vi /etc/nginx/conf.d/sd.conf

		    upstream sd {

			ip_hash;

			server 192.168.1.11:7860;
			server 192.168.1.12:7860;
			server 192.168.1.13:7860;
			server 192.168.1.14:7860;
			server 192.168.1.15:7860;
			server 192.168.1.16:7860;
			server 192.168.1.17:7860;
			server 192.168.1.18:7860;

		    }

		    server {

			listen 80;

			location / {

				proxy_pass http://sd;

				proxy_http_version	1.1;
				proxy_set_header	Host $host;
				proxy_set_header	X-Real-IP  $remote_addr;
				proxy_set_header	X-Forwarded-For $proxy_add_x_forwarded_for;
				proxy_set_header	Upgrade $http_upgrade;
				proxy_set_header	Connection "upgrade";

			}

		    }


		# 重启 nginx

		# systemctl restart nginx


### 交互使用

		http://192.168.1.10/


### 程序调用接口说明 

		http://192.168.1.10/docs


### 程序调用

		SDWebUi sdWebUi = new SDWebUi();

		JSONObject o = sdWebUi.text2img("8k, high detail, sea, beach, girl, detailed face", "logo, text", 3);

##### 生成结果

![image](https://github.com/AndyInAi/Winter/blob/main/img/00000-1430822278.png)
![image](https://github.com/AndyInAi/Winter/blob/main/img/00002-1430822280.png)
![image](https://github.com/AndyInAi/Winter/blob/main/img/00003-1430822281.png)
![image](https://github.com/AndyInAi/Winter/blob/main/img/00004-1430822282.png)
![image](https://github.com/AndyInAi/Winter/blob/main/img/00005-1430822283.png)
![image](https://github.com/AndyInAi/Winter/blob/main/img/00006-1430822284.png)
![image](https://github.com/AndyInAi/Winter/blob/main/img/00007-1430822285.png)
![image](https://github.com/AndyInAi/Winter/blob/main/img/00008-1430822286.png)


