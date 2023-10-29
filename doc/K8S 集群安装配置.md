
## K8S 集群安装配置


### 每个节点执行


#### 下载安装

		mkdir -p  /usr/local/lib/systemd/system/ /opt/cni/bin /etc/containerd /etc/apt/keyrings

		wget https://github.com/containerd/containerd/releases/download/v1.6.24/containerd-1.6.24-linux-amd64.tar.gz -O containerd-1.6.24-linux-amd64.tar.gz

		wget https://raw.githubusercontent.com/containerd/containerd/main/containerd.service -O /usr/local/lib/systemd/system/containerd.service

		wget https://github.com/opencontainers/runc/releases/download/v1.1.9/runc.amd64 -O runc.amd64

		wget https://github.com/containernetworking/plugins/releases/download/v1.3.0/cni-plugins-linux-amd64-v1.3.0.tgz -O cni-plugins-linux-amd64-v1.3.0.tgz

		curl -fsSL https://pkgs.k8s.io/core:/stable:/v1.28/deb/Release.key | gpg --dearmor -o /etc/apt/keyrings/kubernetes-apt-keyring.gpg

		wget https://raw.githubusercontent.com/flannel-io/flannel/master/Documentation/kube-flannel.yml -O kube-flannel.yml

		chmod -R 755 /etc/apt/keyrings

		echo 'deb [signed-by=/etc/apt/keyrings/kubernetes-apt-keyring.gpg] https://pkgs.k8s.io/core:/stable:/v1.28/deb/ /' | sudo tee /etc/apt/sources.list.d/kubernetes.list

		apt-get update -y

		apt install -y --allow-change-held-packages kubelet kubeadm kubectl

		apt-mark hold kubelet kubeadm kubectl

		tar Cxzvf /usr/local containerd-1.6.24-linux-amd64.tar.gz

		install -m 755 runc.amd64 /usr/local/sbin/runc

		tar Cxzvf /opt/cni/bin cni-plugins-linux-amd64-v1.3.0.tgz

		apt install -y ipset ipvsadm apt-transport-https ca-certificates curl

		kubectl version --client


#### 系统配置

		cat <<EOF | sudo tee /etc/modules-load.d/k8s.conf
		overlay
		br_netfilter
		ip_vs
		ip_vs_rr
		ip_vs_wrr
		ip_vs_sh
		nf_conntrack 
		EOF

		modprobe overlay
		modprobe br_netfilter
		modprobe ip_vs
		modprobe ip_vs_rr
		modprobe ip_vs_wrr
		modprobe ip_vs_sh
		modprobe nf_conntrack 

		cat <<EOF | sudo tee /etc/sysctl.d/k8s.conf
		net.bridge.bridge-nf-call-iptables  = 1
		net.bridge.bridge-nf-call-ip6tables = 1
		net.ipv4.ip_forward                 = 1
		EOF

		sysctl --system

		systemctl daemon-reload

		systemctl enable --now containerd

		containerd config default > /etc/containerd/config.toml

		sed -i 's/sandbox_image .*/sandbox_image = "registry.aliyuncs.com\/google_containers\/pause:3.9"/g' /etc/containerd/config.toml

		if [ "`grep 'SystemdCgroup = true' /etc/containerd/config.toml`" == "" ]; then sed -i "s/SystemdCgroup = false/SystemdCgroup = true/g" /etc/containerd/config.toml; fi

		systemctl restart containerd

		systemctl enable kubelet --now

		nodes=4; # 节点数

		for ((i=1; i<=$nodes; i++)); do 

			ip="192.168.1.$((20+$i)) " # 注意保留一个空格；节点 IP 这里以 192.168.1.21 开始

			host="$ip k8s$i"

			if [ "`grep \"^$ip\" /etc/hosts`" == "" ]; then echo "$host" >> /etc/hosts; else sed -i "s/^$ip.*$/$host/g" /etc/hosts; fi

		done


### 主节点初始化

		export KUBE_PROXY_MODE=ipvs

		echo "export KUBE_PROXY_MODE=ipvs" >> /etc/profile

		(
			kubeadm config print init-defaults > kubeadm-init.yml
			sed -i "s/ttl:.*$/ttl: 0s/g" kubeadm-init.yml
			sed -i "s/advertiseAddress.*/advertiseAddress: `ifconfig |grep inet |grep -v 127.0 |awk '{print $2}' | tail -n 1`/g" kubeadm-init.yml
			sed -i "s/name:.*/name: `hostname`/g" kubeadm-init.yml
			sed -i "s/imageRepository.*/imageRepository: registry.aliyuncs.com\/google_containers/g" kubeadm-init.yml
			if [ "`grep 'podSubnet' kubeadm-init.yml`" == "" ]; then sed -i "s/serviceSubnet.*/serviceSubnet: 10.96.0.0\/12\n  podSubnet: 10.244.0.0\/16/g" kubeadm-init.yml; fi
			if [ "`grep 'pod-network-cidr' kubeadm-init.yml`" == "" ]; then echo "
				pod-network-cidr: '10.244.0.0/16'
				---
				apiVersion: kubeproxy.config.k8s.io/v1alpha1
				kind: KubeProxyConfiguration
				mode: ipvs
				---
				kind: KubeletConfiguration
				apiVersion: kubelet.config.k8s.io/v1beta1
				cgroupDriver: systemd
			" >> kubeadm-init.yml; fi
			sed -i "s/^\t*//g" kubeadm-init.yml
		)

		kubeadm init   --config=kubeadm-init.yml   --ignore-preflight-errors=all

		(
			mkdir -p $HOME/.kube
			cp -f /etc/kubernetes/admin.conf $HOME/.kube/config
			chown $(id -u):$(id -g) $HOME/.kube/config
		)

		export KUBECONFIG=/etc/kubernetes/admin.conf

		echo "export KUBECONFIG=/etc/kubernetes/admin.conf" >> /etc/profile

		kubectl apply -f kube-flannel.yml

		kubectl get nodes


### 新节点加入集群

		kubeadm join 192.168.1.21:6443 --token abcdef.0123456789abcdef \
			--discovery-token-ca-cert-hash sha256:297dd450baa2d4381d9c29425529fefae18d1ddf2d9f0c838000cbda11d2d188


### 主节点设置 role，在全部节点加入集群后执行

		kubectl label no k8s1 kubernetes.io/role=master

		nodes=4; # 节点数

		for ((i=2; i<=$nodes; i++)); do 

			kubectl label no k8s$i kubernetes.io/role=node

		done


### 主节点生成日常操作脚本命令


#### List pod, svc, node

	# ~/kl 

	(
		echo '#!/bin/bash

		kubectl get pod,svc,node -n winter -o wide

		' > ~/kl; chmod +x ~/kl; sed -i 's/^\t*//g' ~/kl;
	)


#### Pod shell

	# ~/ks 

	(
		echo '#!/bin/bash

		if [ "$1" == "" ]; then echo -e \\nPod shell\\n\\nuseage: \\n\\t$0 pod\\n; exit 0; fi

		kubectl exec -it $1 -n winter -- bash

		' > ~/ks; chmod +x ~/ks; sed -i 's/^\t*//g' ~/ks;
	)


#### Create deployment

	# ~/kd 

	(
		echo '#!/bin/bash

			if [ "$2" == "" ]; then echo -e \\nCreate deployment \\n\\nuseage: \\n\\t$0 image-name deploy-name\\n; exit 0; fi
			
			kubectl create deployment $2 --image=$1 -n winter
		
		' > ~/kd; chmod +x ~/kd; sed -i 's/^\t*//g' ~/kd;
	)


#### Expose port

	# ~/kp 

	(
		echo '#!/bin/bash

		if [ "$3" == "" ]; then echo -e \\nExpose port\\n\\nuseage: \\n\\t$0 deploy-name port target-port\\n; exit 0; fi

		kubectl expose deployment $1 --port=$2 --target-port=$3 --type=NodePort --name $1 --namespace=winter

		' > ~/kp; chmod +x ~/kp; sed -i 's/^\t*//g' ~/kp;
	)


