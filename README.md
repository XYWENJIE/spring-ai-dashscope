<p align="left">
	<a href="README-zh.md">中文</a>|English
</p>

# Spring AI Extension

### Introduction

The **Spring AI Extension** is a meticulously designed extension library, whose core mission is to provide a seamless integration solution for domestic leading large language models (LLM). Built on the strong Spring AI infrastructure, the project aims to deeply expand and enrich this ecosystem by implementing and encapsulating the RESTful API interfaces of the top domestic large models, thus achieving tight and efficient collaboration with the Spring AI platform. Developers can leverage this extension library to fully utilize the Spring AI framework while easily accessing high-performance large model services provided by domestic operators, further enhancing the intelligence level and user experience of their applications.

## Dependencies

[Spring AI](https://github.com/spring-projects/spring-ai)

## Functional Features

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
	<version>0.7.0-SNAPSHOT</version>
</dependency>
~~~

#### contact

For any inquiries, please feel free to submit an issue or reach out to us at xywenjie@outlook.com.

TODO

 - [X] Alibaba Cloud’s DashScope
 - [X] Qwen Chat Model
 - [X] Qwen Image Model
 - [ ] Stable Diffusion Model 