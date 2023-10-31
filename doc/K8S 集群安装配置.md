
## K8S 集群安装配置


### 准备

	# 9 台主机：第 1 台作为主节点，其余 8 台作为工作节点

	# IP 地址： 主节点 192.168.1.21 工作节点 192.168.1.22 - 29

	# 每台主机配置：
		12 核以上 CPU
		16GB 以上内存 		
		2TB 以上企业级 NVME 硬盘
		10Gb 以上网卡

	# 配置每台主机 /etc/hosts 

	# 主机名 k8s1 - 9 对应 IP 地址 192.168.1.21 - 29

	(
		hosts=9; # 9 台主机

		for ((i=1; i<=$hosts; i++)); do 

			ip="192.168.1.$((20+$i)) " # 注意保留一个空格；主机 IP 这里以 192.168.1.21 开始

			host="$ip k8s$i"

			if [ "`grep \"^$ip\" /etc/hosts`" == "" ]; then echo "$host" >> /etc/hosts; else sed -i "s/^$ip.*$/$host/g" /etc/hosts; fi

		done
	)	


#### 下载安装

	# 在每个主机执行

	# 保证网络能正常访问 GitHub.com

	(
		mkdir -p  /usr/local/lib/systemd/system/ /opt/cni/bin /etc/containerd /etc/apt/keyrings

		wget https://github.com/containerd/containerd/releases/download/v1.6.24/containerd-1.6.24-linux-amd64.tar.gz -O ~/containerd-1.6.24-linux-amd64.tar.gz

		wget https://raw.githubusercontent.com/containerd/containerd/main/containerd.service -O /usr/local/lib/systemd/system/containerd.service

		wget https://github.com/opencontainers/runc/releases/download/v1.1.9/runc.amd64 -O ~/runc.amd64

		wget https://github.com/containernetworking/plugins/releases/download/v1.3.0/cni-plugins-linux-amd64-v1.3.0.tgz -O ~/cni-plugins-linux-amd64-v1.3.0.tgz

		curl -fsSL https://pkgs.k8s.io/core:/stable:/v1.28/deb/Release.key | gpg --dearmor -o /etc/apt/keyrings/kubernetes-apt-keyring.gpg

		wget https://raw.githubusercontent.com/flannel-io/flannel/master/Documentation/kube-flannel.yml -O ~/kube-flannel.yml
	)

	(
		chmod -R 755 /etc/apt/keyrings

		echo 'deb [signed-by=/etc/apt/keyrings/kubernetes-apt-keyring.gpg] https://pkgs.k8s.io/core:/stable:/v1.28/deb/ /' > /etc/apt/sources.list.d/kubernetes.list

		export DEBIAN_FRONTEND=noninteractive

		apt-get update -y

		apt install -y --allow-change-held-packages kubelet kubeadm kubectl

		apt-mark hold kubelet kubeadm kubectl

		tar Cxzvf /usr/local ~/containerd-1.6.24-linux-amd64.tar.gz

		install -m 755 ~/runc.amd64 /usr/local/sbin/runc

		tar Cxzvf /opt/cni/bin ~/cni-plugins-linux-amd64-v1.3.0.tgz

		apt install -y ipset ipvsadm apt-transport-https ca-certificates curl

		kubectl version --client
	)


#### 系统配置

	(
		modprobe overlay
		modprobe br_netfilter
		modprobe ip_vs
		modprobe ip_vs_rr
		modprobe ip_vs_wrr
		modprobe ip_vs_sh
		modprobe nf_conntrack 

		echo '
			overlay
			br_netfilter
			ip_vs
			ip_vs_rr
			ip_vs_wrr
			ip_vs_sh
			nf_conntrack 
		' > /etc/modules-load.d/k8s.conf;

		echo '
			net.bridge.bridge-nf-call-iptables  = 1
			net.bridge.bridge-nf-call-ip6tables = 1
			net.ipv4.ip_forward                 = 1
		' > /etc/sysctl.d/k8s.conf;

		sysctl --system

		systemctl daemon-reload

		systemctl enable --now containerd
	)

	(
		containerd config default > /etc/containerd/config.toml

		sed -i 's/sandbox_image .*/sandbox_image = "registry.aliyuncs.com\/google_containers\/pause:3.9"/g' /etc/containerd/config.toml

		if [ "`grep 'SystemdCgroup = true' /etc/containerd/config.toml`" == "" ]; then sed -i "s/SystemdCgroup = false/SystemdCgroup = true/g" /etc/containerd/config.toml; fi

		systemctl restart containerd

		systemctl enable kubelet --now
	)


### 初始化

	# 仅需要在第一台主机，即 Master 主机执行

	(
		export KUBE_PROXY_MODE=ipvs

		echo "export KUBE_PROXY_MODE=ipvs" >> /etc/profile
	)

	(
		kubeadm config print init-defaults > kubeadm-init.yml
		
		sed -i "s/ttl:.*$/ttl: 0s/g" kubeadm-init.yml
		sed -i "s/advertiseAddress.*/advertiseAddress: `ifconfig |grep inet |grep -v 127.0 |awk '{print $2}' | tail -n 1`/g" kubeadm-init.yml
		sed -i "s/name:.*/name: `hostname`/g" kubeadm-init.yml
		sed -i "s/imageRepository.*/imageRepository: registry.aliyuncs.com\/google_containers/g" kubeadm-init.yml
		
		if [ "`grep 'podSubnet' kubeadm-init.yml`" == "" ]; then sed -i "s/serviceSubnet.*/serviceSubnet: 10.96.0.0\/12\n  podSubnet: 10.244.0.0\/16/g" kubeadm-init.yml; fi
		
		if [ "`grep 'pod-network-cidr' kubeadm-init.yml`" == "" ]; then 
			echo "
				pod-network-cidr: '10.244.0.0/16'
				---
				apiVersion: kubeproxy.config.k8s.io/v1alpha1
				kind: KubeProxyConfiguration
				mode: ipvs
				---
				kind: KubeletConfiguration
				apiVersion: kubelet.config.k8s.io/v1beta1
				cgroupDriver: systemd
			" >> kubeadm-init.yml; 
		fi
		
		sed -i "s/^\t*//g" kubeadm-init.yml
	)

	kubeadm init --config=kubeadm-init.yml --ignore-preflight-errors=all

	(
		mkdir -p ~/.kube
		cp -f /etc/kubernetes/admin.conf ~/.kube/config
		chown $(id -u):$(id -g) ~/.kube/config
	)

	(
		export KUBECONFIG=/etc/kubernetes/admin.conf

		echo "export KUBECONFIG=/etc/kubernetes/admin.conf" >> /etc/profile

		kubectl apply -f ~/kube-flannel.yml

		kubectl get nodes
	)



### 加入集群

	# 除了第一台主机，即主节点外的主机执行，--discovery-token-ca-cert-hash 以主节点初始化结果为准

	kubeadm join 192.168.1.21:6443 --token abcdef.0123456789abcdef \
		--discovery-token-ca-cert-hash sha256:51f5189f8292ad983626949d9ba7504104d3fa58e1d68d9517dc8dbdcc17e9ff



### 设置 role

	# 全部主机加入集群后，在第一台主机，即主节点执行

	(
		hosts=9; # 9 台主机

		kubectl label no k8s1 kubernetes.io/role=master

		for ((i=2; i<=$hosts; i++)); do 

			kubectl label no k8s$i kubernetes.io/role=node

		done
	)


### GlusterFS 集群客户端安装配置

	# 在每个主机执行

	# 安装时需要确保 GlusterFS 集群正在运行

	# 相关文档： GlusterFS 集群安装配置.md

	(
		export DEBIAN_FRONTEND=noninteractive;
		
		apt install -y glusterfs-client
	)

	(
		ip="192.168.1.80" # GlusterFS 集群负载均衡主机 gfs

		host="$ip gfs" ;  if [ "`grep \"^$ip \" /etc/hosts`" == "" ]; then echo "$host" >> /etc/hosts; else sed -i "s/^$ip .*$/$host/g" /etc/hosts; fi

		NODES="192.168.1.81 192.168.1.82 192.168.1.83 192.168.1.84 192.168.1.85 192.168.1.86 192.168.1.87 192.168.1.88 192.168.1.89" # 9 个GlusterFS 集群主机 IP 列表，对应主机名 gfs1 - gfs9

		no=0; for i in $NODES; do no=$(($no+1)) ; ip="$i " ;  host="$ip gfs$no" ;  if [ "`grep \"^$ip\" /etc/hosts`" == "" ]; then echo "$host" >> /etc/hosts; else sed -i "s/^$ip.*$/$host/g" /etc/hosts; fi ; done
		
		umount /mnt/gluster-gv0 > /dev/null 2>&1

		mkdir -p /mnt/gluster-gv0

		if [ "`grep ^gfs:/gv0 /etc/fstab`" == "" ]; then echo 'gfs:/gv0 /mnt/gluster-gv0 glusterfs defaults,_netdev 0 0' >> /etc/fstab; fi

		mount -a

		mkdir -p /mnt/gluster-gv0/k8s

		ll /mnt/gluster-gv0/
	)


### 生成常用管理命令

	# 在第一台主机，即主节点执行


	# List pod, svc, node

	# ~/kl 

	(
		echo '#!/bin/bash

		kubectl get pod,svc,node -n winter -o wide

		' > ~/kl; chmod +x ~/kl; sed -i 's/^\t*//g' ~/kl;
	)


	# Pod shell

	# ~/ks 

	(
		echo '#!/bin/bash

		if [ "$1" == "" ]; then echo -e \\nPod shell\\n\\nuseage: \\n\\t$0 pod\\n; exit 0; fi

		kubectl exec -it $1 -n winter -- bash

		' > ~/ks; chmod +x ~/ks; sed -i 's/^\t*//g' ~/ks;
	)


	# Create deployment

	# ~/kd 

	(
		echo '#!/bin/bash

			if [ "$3" == "" ]; then echo -e \\nCreate deployment \\n\\nuseage: \\n\\t$0 image-name deploy-name replicas\\n; exit 0; fi
			
			kubectl create deployment $2 --image=$1  --replicas=$3 -n winter
		
		' > ~/kd; chmod +x ~/kd; sed -i 's/^\t*//g' ~/kd;
	)


	# Expose port

	# ~/kp 

	(
		echo '#!/bin/bash

		if [ "$3" == "" ]; then echo -e \\nExpose port\\n\\nuseage: \\n\\t$0 deploy-name port target-port\\n; exit 0; fi

		kubectl expose deployment $1 --port=$2 --target-port=$3 --type=LoadBalancer --name $1 --namespace=winter

		' > ~/kp; chmod +x ~/kp; sed -i 's/^\t*//g' ~/kp;
	)


