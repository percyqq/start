	"http://hub-mirror.c.163.com",
    "https://registry.docker-cn.com"
	
	
	云里雾里的docker ping通方法
https://www.cnblogs.com/brock0624/p/9788710.html	
route -p add 172.17.0.0 MASK 255.255.255.240 10.0.75.2
	
	
docker inspect 36afde543eb5 | grep IPAddress
//将主机的8081端口映射到宿主机的8080端口
iptables -t nat -A  DOCKER -p tcp --dport 8081 -j DNAT --to-destination 172.17.0.2:8080


docker 映射文件从宿主机到容器,端口号映射
//将宿主机的81端口映射到容器的80端口
//将宿主机的/develop/data卷，映射到容器的/data卷
docker run -i -t -p 81:80 -v /develop/data:/data  centos /bin/bash



docker 进入一个运行的容器
	docker ps
	docker exec -it docker exec -it 02af1357cb80 /bin/bash /bin/bash

切换到root用户：
	su - root
	修改下密码
	passwd root		==> 6*
	
	
ubuntu最小化的包更新一下apt
	apt-get update
	apt install curl
	apt-get install vim
	
	永久显示行号： 命令行输入下面的命令编辑:
	vim ~/.vimrc
	内容: set nu

	###   ifconfig
	apt-get install net-tools
		
	###  ping
	apt-get install iputils-ping
		
	####  ip
	apt-get install iproute2
	


安装ssh服务
docker run -i -t ubuntu /bin/bash #创建一个容器，-t是临时终端。

进入ubuntu后，安装openssh
apt-get install openssh-server #安装ssh

#需要修改/etc/sshd/sshd_config文件中内容
PermitRootLogin yes

UsePAM no
修改ubuntu的root用户密码，以便以后ssh登陆：

sudo passwd root

service ssh stop


3、制作新的镜像		
cd D:/u	
	docker build -t ubuntu/ssh:v1.1 .


到此，我们需要把这个带有ssh服务的容器提交成一个镜像，方便以后在这个基础上各种改造：
docker commit <container id> <image name>
 docker push ~~
	docker commit 02af1357cb80 ubuntu/sshd:v1					
   
   docker rmi 删除旧镜像
   
   启动新镜像
   docker run -it --name ubuntu_ssh -p 10122:22  ubuntu/sshd:v1 /bin/bash
   docker run -it --name ubuntu_ssh -p 1022:22 -p 127.0.0.1:6379:6379 ubuntu/dev:v1.1
   
   
 
 进入镜像启动sshd：
 netstat -lnutp|grep 22
/usr/sbin/sshd -D &

  docker exec -it 02af1357cb80 /bin/bash ubuntu:sshd



windows10保存镜像报错如下
	Error processing tar file(exit status 1): archive/tar: invalid tar header

I meet the same error on my windows10, in powershell, when use:
	docker save image > tmp.tar
	docker load < tmp.tar
	
then I tryed:	解决方案如下：
	docker save image -o tmp.tar
	docker load -i tmp.tar
no error anymore.

保存镜像为文件 .如果要讲镜像保存为本地文件，可以使用Docker save命令。
命令格式：
docker save -o 要保存的文件名  要保存的镜像
docker save -o ubuntu.tar ubuntu/sshd:v1
docker save eccdfeb798f3 -o ubuntu.tar

docker load -i ubuntu.tar




3、制作新的镜像
到此，我们需要把这个带有ssh服务的容器提交成一个镜像，方便以后在这个基础上各种改造：
docker commit <container id> <image name>

当结束后，我们使用 exit 来退出，现在我们的容器已经被我们改变了，使用 docker commit 命令来提交更新后的副本。
docker commit -m "Added json gem" -a "Docker Newbee" 0b2616b0e5a8 ouruser/sinatra:v2
docker run -t -i ouruser/sinatra:v2 /bin/bash
docker run --name imagedb -p 3307:3306 -e MYSQL_ROOT_PASSWORD=123456 -idt gatewaydb:0.0.1 
			--character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci

docker logs -f imagedb 


 docker inspect docker exec -it 02af1357cb80 /bin/bash | grep IPAddress	
 inet 172.17.0.2  netmask 255.255.0.0  broadcast 172.17.255.255
  
  //将主机的8081端口映射到宿主机的8080端口
iptables -t nat -A  DOCKER -p tcp --dport 8081 -j DNAT --to-destination 172.17.0.2:8080
  

下载开发版本
https://hub.docker.com/editions/community/docker-ce-desktop-windows

国内 docker 仓库镜像对比
https://ieevee.com/tech/2016/09/28/docker-mirror.html


https://blog.sina.cn/dpool/blog/s/blog_558e87580102zgxh.html


Windows 10升级1809版本后，发现Hyper-V不能用了，管理器里是一片空白，看服务Hyper-V 主机计算服务没有启动，
手动启动的话失败，报错，代码1053. 自己尝试修复，
也百度了很久，没弄好，后来终于在微软的英文网站上找到了答案，分享如下。
1, Open 'Window Security'
打开“WIndows安全中心 ”
2, Open 'App & Browser control'
打开“应用和浏览器控制”
3, Click 'Exploit protection settings' at the bottom
点击'Exploit protection settings' (在最下面）
4, Switch to 'Program settings' tab
切换到“程序设置”
5, Locate 'C:\WINDOWS\System32\vmcompute.exe' in the list and expand it
找到 'C:\WINDOWS\System32\vmcompute.exe'并展开
6, Click 'Edit'
点击“编辑”
7, Scroll down to 'Code flow guard (CFG)' and uncheck 'Override system settings'
找到'控制流保护（CFG）”并把“替代系统设置”前的勾去掉。
8, Start vmcompute from powershell 'net start vmcompute
打开命令行窗口运行net start vmcompute启动Hyper-V主机计算服务（也可以在计算机管理里启动这个服务和虚拟机管理服务）
原文链接：
https://social.technet.microsoft.com/Forums/en-US/ee5b1d6b-09e2-49f3-a52c-820aafc316f9/hyperv-doesnt-work-after-upgrade-to-windows-10-1809?forum=win10itprovirt&prof=required

