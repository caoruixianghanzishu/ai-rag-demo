# AI-RAG 智能知识库问答系统（V1.0）

## 一、项目背景

我是 Java 后端开发工程师，目前正在学习 AI 应用开发。

本项目目标不是实现复杂 AI Agent，而是实现一个可以用于简历展示和面试演示的 AI 知识库问答系统。

要求：

* 技术方案简单
* 代码结构规范
* 容易理解
* 可以快速落地
* 后续能够扩展 RAG、向量数据库、Agent 等能力

---

## 二、项目目标

实现一个基于 Spring Boot + Spring AI + DeepSeek 的智能知识库问答系统。

用户能够：

1. 上传知识库文档
2. 提问问题
3. 系统检索知识库
4. 调用 DeepSeek 生成答案
5. 支持流式输出
6. 支持多轮对话
7. 保存聊天记录

---

## 三、技术栈

后端：

* Java 17
* Spring Boot 3.4+
* Spring AI
* MyBatis Plus
* MySQL 8
* Redis

前端：

* Vue3
* Element Plus

AI模型：

* DeepSeek Chat API

构建工具：

* Maven

---

## 四、项目架构

用户提问

↓

Spring Boot

↓

知识库检索

↓

DeepSeek API

↓

返回答案

↓

保存聊天记录

---

## 五、V1功能范围

### 1. AI聊天

用户输入：

你好

系统调用 DeepSeek

返回结果

功能要求：

* 普通聊天
* 单轮对话

---

### 2. 流式输出

使用：

SSE

接口：

GET /api/chat/stream

要求：

* 实时返回内容
* 边生成边显示

---

### 3. 多轮对话

使用 Redis 保存上下文

数据结构：

sessionId

messages

要求：

* 支持连续聊天
* 记住上一轮内容

示例：

用户：

奖学金申请条件是什么？

AI：

......

用户：

申请时间是什么时候？

AI：

根据上下文继续回答

---

### 4. 聊天记录

数据库表：

ai_chat_session

ai_chat_message

功能：

* 创建会话
* 查询会话列表
* 查看历史聊天

---

### 5. 知识库管理

数据库表：

knowledge_doc

字段：

id

title

content

create_time

功能：

* 新增知识内容
* 编辑知识内容
* 删除知识内容
* 查询知识内容

暂不实现文件上传

直接录入文本

---

### 6. 简易RAG

用户提问：

奖学金申请条件

系统流程：

Step1

查询 knowledge_doc

Step2

找到相关内容

Step3

拼接 Prompt

Step4

调用 DeepSeek

Prompt格式：

你是一名高校学工系统智能助手。

请根据以下知识库内容回答问题。

知识库内容：

{{content}}

用户问题：

{{question}}

如果知识库没有相关内容，请明确说明未找到相关资料。

---

## 六、数据库设计

### ai_chat_session

id bigint

session_name varchar(200)

create_time datetime

---

### ai_chat_message

id bigint

session_id bigint

role varchar(20)

content longtext

create_time datetime

---

### knowledge_doc

id bigint

title varchar(200)

content longtext

create_time datetime

---

## 七、接口设计

### 聊天接口

POST

/api/chat

参数：

{
"sessionId":"1",
"message":"奖学金申请条件"
}

返回：

{
"answer":"xxxx"
}

---

### 流式聊天

GET

/api/chat/stream

参数：

message

sessionId

返回：

text/event-stream

---

### 知识库管理

POST /knowledge/add

PUT /knowledge/update

DELETE /knowledge/delete

GET /knowledge/list

GET /knowledge/detail

---

### 会话管理

GET /session/list

GET /session/history

---

## 八、代码结构

com.ai.demo

controller

service

service.impl

mapper

entity

dto

vo

config

ai

rag

util

---

## 九、开发要求

必须：

* 使用 Spring AI
* 使用 DeepSeek API
* 使用 MyBatis Plus
* 使用 Redis
* 使用 SSE

不要：

* LangChain4j
* Kafka
* Elasticsearch
* Milvus
* Docker
* Kubernetes
* Agent
* MCP

保持项目简单。

---

## 十、后续扩展方向

V2：

支持 PDF 上传

支持 Word 上传

支持 MinIO

V3：

接入 Embedding

接入向量数据库

Milvus

Chroma

PGVector

V4：

Agent能力

Function Calling

MCP

工具调用
