# Spring AI Extension

### 项目简介

**Spring-AI-extension**是一款细致入微的增强库，它的中心使命是为国内领先的大型语言模型（LLM）提供流畅的整合方案。该项目立足于强大的Spring AI基础架构，致力于深化拓展和丰富此生态体系。通过对接和封装国内领先的大模型RESTful API接口，Spring-AI-extension能与Spring AI平台紧密高效地协同工作，使开发者能在充分利用Spring AI框架的同时，轻松接入国内运营商提供的高性能大模型服务。借助该扩展库，开发者的应用不仅能获得更智能的体验，用户体验也将得到显著提升。

##功能特性

**支持阿里云的（DashScope）灵积服务通义千问和通义万相**

提示：如果要本地通义千问模型可以使用官方的Ollama模块进行

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
	<version>0.8.1</version>
</dependency>
~~~

//TODO 未完成
可以在Spring Boot的文件application.properties中配置
~~~properties
spring.ai.dashscope.api-key=你的api-key
~~~

#### 联系

有任何疑问可以提交issue或邮箱联系:xywenjie@outlook.com

#### 未来发展
本人单位用阿里云，优先阿里云的灵积服务，未来会添加智谱AI、百度的千帆和腾讯云混元大模型等功能