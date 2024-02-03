# spring-ai-extension

### 项目简介

**Spring-AI-extension**是一个扩展库，旨在无缝集成国内运营商LLM等模型。该项目是以Spring AI为基础结构为准，该库的是在此基础上实现或扩充国内的大模型REST API实现，以配合Spring AI的使用。

##功能特性

**支持通义千问**

## 安装与配置

### 安装步骤

#### 1.下载源码

通过Git下载到本地
~~~ bash

git clone https://github.com/XYWENJIE/spring-ai-dashscope.git

~~~

#### 2.构建并安装
在项目根目录下执行maven构建命名:
~~~ bash
cd spring-ai-extension
mvn clean install
~~~

这将并以项目并安装本地Maven仓库

引入依赖
在你的Spring项目中添加依赖:
~~~xml
<dependency>
	<groupId>org.xywenjie.spring-ai-extension</groupId>
	<artifactId>spring-ai-dashscope</artifactId>
	<version>0.8.0-SNAPSHOT</version>
</dependency>
~~~

#### 联系

有任何疑问可以提交issue或邮箱联系:xywenjie@outlook.com