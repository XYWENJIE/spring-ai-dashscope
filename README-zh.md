# spring-ai-extension

### 项目简介

**Spring-AI-extension**是一款精心设计的扩展库，其核心目标是为国内领先的大型语言模型（LLM）提供无缝集成方案。该项目以强大的Spring AI基础架构为依托，旨在深度拓展和丰富这一生态体系，通过实现并封装国内顶尖大模型的RESTful API接口，从而与Spring AI平台达成紧密且高效的协同工作。开发者借此扩展库能够在充分利用Spring AI框架的同时，轻松调用国内运营商所提供的高性能大模型服务，进一步提升应用的智能化水平及用户体验。

##功能特性

**支持通义千问**

**支持通义万相**

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
在你的Spring项目中添加依赖，这里以阿里云的模型服务产品“灵积”为例:
~~~xml
<dependency>
	<groupId>org.xywenjie.spring-ai-extension</groupId>
	<artifactId>spring-ai-dashscope</artifactId>
	<version>0.8.0-SNAPSHOT</version>
</dependency>
~~~

#### 联系

有任何疑问可以提交issue或邮箱联系:xywenjie@outlook.com