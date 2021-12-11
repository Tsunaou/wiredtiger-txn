# Centos + wiredtiger镜像制作
docker pull ikcow/my_wiredtiger:v3

## 1.镜像加速

参考https://www.cnblogs.com/djlsunshine/p/11375323.html

cd /etc/docker/

touch daemon.json

```
{
"registry-mirrors": ["https://almtd3fa.mirror.aliyuncs.com"] 
}
```

systemctl daemon-reload

systemctl restart docker.service

## 2.镜像制作

参考https://zhuanlan.zhihu.com/p/122380334

docker pull centos

docker run -it centos /bin/bash

### 2.1.下载语言包

dnf install glibc-langpack-en

### 2.2.下载相关依赖

yum install zlib-devel bzip2 bzip2-devel readline-devel sqlite sqlite-devel openssl-devel xz xz-devel libffi-devel wget gcc make gcc-c++ libtool pcre swig

### 2.3.安装python

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

### 2.4.下载基础工具

yum install git autoconf automake

python3 -m pip install scons

### 2.5.安装java

yum install java

vi /etc/profile

```
export JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.312.b07-2.el8_5.x86_64
export JRE_HOME=$JAVA_HOME/jre
export CLASSPATH=.:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar
export PATH=$JAVA_HOME/bin:$JRE_HOME/bin:$PATH
```

. /etc/profile

yum install java-devel

### 2.6.下载wiredtiger

cd /home

mkdir env

cd env

git clone -b mongodb-4.2 git://github.com/wiredtiger/wiredtiger.git

cd wiredtiger

sh autogen.sh

./configure --enable-java --enable-python && make

make install

### 2.7. 验证

cd /usr/local/lib/

```
/usr/local/lib/libwiredtiger-3.3.0.so: ELF 64-bit LSB shared object, x86-64, version 1 (SYSV), dynamically linked, BuildID[sha1]=c01907e68fba73511db967e17d6f04c4ee2b9130, with debug_info, not stripped
/usr/local/lib/libwiredtiger.a:        current ar archive
/usr/local/lib/libwiredtiger.la:       libtool library file, ASCII text
/usr/local/lib/libwiredtiger.so:       symbolic link to libwiredtiger-3.3.0.so
```

cd /usr/local/share/java/wiredtiger-3.3.0/

```
/usr/local/share/java/wiredtiger-3.3.0/libwiredtiger_java.a:        current ar archive
/usr/local/share/java/wiredtiger-3.3.0/libwiredtiger_java.la:       libtool library file, ASCII text
/usr/local/share/java/wiredtiger-3.3.0/libwiredtiger_java.so:       symbolic link to libwiredtiger_java.so.0.0.0
/usr/local/share/java/wiredtiger-3.3.0/libwiredtiger_java.so.0:     symbolic link to libwiredtiger_java.so.0.0.0
/usr/local/share/java/wiredtiger-3.3.0/libwiredtiger_java.so.0.0.0: ELF 64-bit LSB shared object, x86-64, version 1 (SYSV), dynamically linked, BuildID[sha1]=5ff5cc829d2b57e8d8b53351fbe1b12f78352af1, with debug_info, not stripped
/usr/local/share/java/wiredtiger-3.3.0/wiredtiger.jar:              Java archive data (JAR)
```

/usr/local/bin/wt

## 3.镜像上传

docker commit -m "commit message" -a "authorName" containerID hubName/imageName:tagName

docker login

docker push hubName/imageName:tagName