# 使用ubuntu:22.04镜像作为基础镜像
FROM ubuntu:22.04
# FROM dockerproxy.cn/library/ubuntu:22.04

# 设置时区为 Asia/Shanghai
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
ENV CMD_ALLOW_PDF_EXPORT=true
ENV QT_QPA_PLATFORM=
ENV OPENSSL_CONF=/etc/ssl

# 安装PhantomJS依赖和JDK 17
RUN apt-get update && apt-get install -y \
    fontconfig \
    xfonts-utils \
    fonts-wqy-microhei \
    libfreetype6 \
    libx11-6 \
    libxext6 \
    libxrender1 \
    openssl \
    libssl-dev \
    openjdk-17-jdk \
    && rm -rf /var/lib/apt/lists/* 

# 设置环境变量
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
ENV PATH=$PATH:$JAVA_HOME/bin

# 验证Java安装
RUN java -version && javac -version
	
RUN mkdir /usr/share/fonts/win

COPY genImage/win-fonts /usr/share/fonts/win/

RUN cd /usr/share/fonts/win

RUN mkfontscale

RUN mkfontdir

RUN fc-cache -fv

# 设置字体环境变量
ENV LANG C.UTF-8
ENV LC_ALL C.UTF-8

COPY genImage/phantomjs/server/ /app/data/phantomjs/
COPY genImage/phantomjs/echarts-convert/ /app/data/echarts-convert/
COPY genImage/phantomjs/temp/ /app/data/temp/


# 设置工作目录为 /app
WORKDIR /app

# 复制 JAR 包
COPY genImage/phantomjs-service.jar .

# 启动命令
ENTRYPOINT ["java", "-jar", "phantomjs-service.jar"]
