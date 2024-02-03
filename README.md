# spring-ai-extension

### Introduction

**Spring-AI-extension**A meticulously designed extension library, its core objective is to provide a seamless integration solution for the leading large language model (LLM) in China. Built upon the robust foundation of the Spring AI infrastructure, this project aims to deeply expand and enrich this ecosystem by implementing and encapsulating RESTful API interfaces of the country’s top-tier large models, thereby achieving close and efficient collaboration with the Spring AI platform. Developers can leverage this extension library to easily invoke high-performance LLM services provided by domestic operators while fully utilizing the Spring AI framework, further enhancing the application’s intelligence level and user experience.

## 依赖

[Spring AI](https://github.com/spring-projects/spring-ai)

##功能特性

**Support QWen Model**

## Installation and Configuration

### Installation Steps

#### 1.Download source code

Download to local via Git
~~~ bash

git clone https://github.com/XYWENJIE/spring-ai-dashscope.git

~~~

#### 2.Build and Install
Execute Maven build commands in the root directory of the project:
~~~ bash
cd spring-ai-extension
mvn clean install
~~~

This process will involve integrating the project and installing artifacts into the local Maven repository.

Introduce dependencies
In your Spring project, you can include dependencies, using Alibaba Cloud’s Model Service product ‘dashscope’ for illustration purposes.:
~~~xml
<dependency>
	<groupId>org.xywenjie.spring-ai-extension</groupId>
	<artifactId>spring-ai-dashscope</artifactId>
	<version>0.8.0-SNAPSHOT</version>
</dependency>
~~~

#### contact

For any inquiries, please feel free to submit an issue or reach out to us at xywenjie@outlook.com.

TODO

 - [X] Alibaba Cloud’s DashScope
 - [X] Qwen Chat Model
 - [X] Qwen Image Model
 - [ ] Stable Diffusion Model 