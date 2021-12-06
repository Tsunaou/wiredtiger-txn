# Centos + wiredtiger镜像制作
当前镜像 无java与pthon API docker

pull ikcow/my_wiredtiger:v1
## 镜像加速
参考https://www.cnblogs.com/djlsunshine/p/11375323.html

cd /etc/docker/

touch daemon.json

{   "registry-mirrors": ["https://almtd3fa.mirror.aliyuncs.com"] }

systemctl daemon-reload

systemctl restart docker.service
## 镜像制作
参考https://zhuanlan.zhihu.com/p/122380334

docker pull centos

docker run -it centos /bin/bash
###下载语言包
dnf install glibc-langpack-en
###下载相关依赖
yum install zlib-devel bzip2 bzip2-devel readline-devel sqlite sqlite-devel openssl-devel xz xz-devel libffi-devel
###安装pthon
参考https://www.cnblogs.com/ech2o/p/11748464.html与https://www.icode9.com/content-1-147240.html

cd /usr/local

wget http://npm.taobao.org/mirrors/python/3.8.0/Python-3.8.0.tgz

tar -xzf Python-3.8.0.tgz

mkdir python3

cd Python-3.8.0

./configure --prefix=/usr/local/python3 --with-ssl

make

make install

ln -s /usr/local/python3/bin/python3 /usr/bin/python3

ln -s /usr/local/python3/bin/pip3 /usr/bin/pip3
###下载基础工具
yum install git autoconf automake wget gcc libtool make gcc-c++

python3 -m pip install scons

yum install pcre

yum install swig
###下载wiredtiger
git clone -b mongodb-4.2 git://github.com/wiredtiger/wiredtiger.git

cd wiredtiger

sh autogen.sh

//遇到问题yum install java java-devel并配置java环境

./configure --enable-java --enable-python && make
##镜像上传
docker commit -m "commit message" -a "authorName" containerID hubName/imageName:tagName

docker login

docker push hubName/imageName:tagName