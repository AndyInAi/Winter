 
## 人工智能会话集群安装配置


### 添加普通用户 chat

		adduser chat

### 切换到用户 chat 操作

		su - chat

### 下载安装

		git clone https://github.com/THUDM/ChatGLM2-6B

		cd ChatGLM2-6B

		pip install -r requirements.txt

		pip install gradio fastapi uvicorn

### 启动 API 服务 ；第一次启动将自动下载必需文件

		python api.py


### 负载均衡安装配置 IP 192.168.1.60

		apt install -y nginx

		if [ "`grep '#include \/etc\/nginx\/sites-enabled\/' /etc/nginx/nginx.conf`" == "" ]; then echo ok; sed -i "s/include \/etc\/nginx\/sites-enabled\//#include \/etc\/nginx\/sites-enabled\//g" /etc/nginx/nginx.conf; fi

		# vi /etc/nginx/conf.d/chat.conf

		    upstream chat {

			ip_hash;

			server 192.168.1.61:8000;
			server 192.168.1.62:8000;
			server 192.168.1.63:8000;
			server 192.168.1.64:8000;
			server 192.168.1.65:8000;
			server 192.168.1.66:8000;
			server 192.168.1.67:8000;
			server 192.168.1.68:8000;

		    }

		    server {

			listen 80;

			location / {

				proxy_pass http://chat;

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


### 程序调用

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
