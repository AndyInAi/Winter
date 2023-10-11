
### 安装

	apt install -y gdebi fonts-noto-cjk

	if [ ! -f ~/google-chrome-beta.deb ]; then wget  https://dl.google.com/linux/direct/google-chrome-beta_current_amd64.deb -O ~/google-chrome-beta.deb; fi

	if [ -f ~/google-chrome-beta.deb ]; then gdebi -n ~/google-chrome-beta.deb ; fi


### 测试

	google-chrome --headless --no-sandbox --no-pdf-header-footer --print-to-pdf="/root/sohu.pdf" "https://www.sohu.com/"


