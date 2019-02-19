# 版本信息

FROM centos:latest
MAINTAINER locutus "JohnDoeo"



# OS环境配置

RUN yum install -y wget



# 安装JDK

RUN mkdir /var/tmp/jdk

RUN wget --no-check-certificate --no-cookies --header "Cookie: oraclelicense=accept-securebackup-cookie"  -P /var/tmp/jdk http://download.oracle.com/otn-pub/java/jdk/8u201-b09/42970487e3af4f5aa5bca3f542482c60/jdk-8u201-linux-x64.tar.gz
RUN tar xzf /var/tmp/jdk/jdk-8u201-linux-x64.tar.gz -C /var/tmp/jdk && rm -rf /var/tmp/jdk/jdk-8u201-linux-x64.tar.gz



#设置环境变量

ENV JAVA_HOME /var/tmp/jdk/jdk1.8.0_201

ENV PATH $PATH:$JAVA_HOME/bin

#开启内部服务端口

EXPOSE 10001

#将springboot的jar包添加进来

ADD weblog-1.0.jar weblog.jar

RUN sh -c 'touch /weblog.jar'

ENV JAVA_OPTS=""

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /weblog.jar" ]
