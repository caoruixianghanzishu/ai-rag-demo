# AI-RAG 智能知识库问答系统

基于 Spring Boot + Spring AI + DeepSeek 的智能知识库问答系统（V1.0）。

## 技术栈

- Java 17
- Spring Boot 3.4
- Spring AI
- MyBatis Plus
- MySQL 8
- Redis
- Maven
- Vue 3 + Element Plus（前端）

## 项目结构

```
com.ai.demo
├── controller      # 接口层
├── service         # 业务接口
├── service.impl    # 业务实现
├── mapper          # 数据访问
├── entity          # 数据库实体
├── dto             # 请求对象
├── vo              # 响应对象
├── config          # 配置类
├── ai              # AI 调用封装
├── rag             # 简易 RAG
└── util            # 工具类
```

## 快速开始

### 1. 环境准备

- JDK 17+
- Maven 3.8+
- MySQL 8
- Redis

### 2. 初始化数据库

```bash
mysql -u root -p < src/main/resources/db/schema.sql
```

### 3. 配置

修改 `src/main/resources/application.yml` 中的数据库、Redis 连接信息。

设置 DeepSeek API Key（环境变量或配置文件）：

```bash
# Windows PowerShell
$env:DEEPSEEK_API_KEY="your-api-key"

# Linux / macOS
export DEEPSEEK_API_KEY=your-api-key
```

### 4. 启动

```bash
mvn spring-boot:run
```

服务默认运行在 `http://localhost:8080`

### 5. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端默认运行在 `http://localhost:5173`，已通过 Vite 代理转发 API 请求到后端。

## API 接口

### 聊天

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/chat` | 普通聊天（含 RAG） |
| GET  | `/api/chat/stream?message=xxx&sessionId=1` | SSE 流式聊天 |

### 知识库

| 方法 | 路径 | 说明 |
|------|------|------|
| POST   | `/knowledge/add` | 新增知识 |
| PUT    | `/knowledge/update` | 编辑知识 |
| DELETE | `/knowledge/delete?id=1` | 删除知识 |
| GET    | `/knowledge/list` | 知识列表 |
| GET    | `/knowledge/detail?id=1` | 知识详情 |

### 会话

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/session/list` | 会话列表 |
| GET | `/session/history?sessionId=1` | 历史消息 |

## 功能说明

- **AI 聊天**：调用 DeepSeek 生成回答
- **流式输出**：SSE 实时返回生成内容
- **多轮对话**：Redis 缓存上下文，支持连续对话
- **聊天记录**：MySQL 持久化会话与消息
- **知识库管理**：文本录入、增删改查
- **简易 RAG**：关键词匹配知识库，拼接 Prompt 后调用 AI

## 后续扩展

- V2：文件上传（PDF/Word）、MinIO
- V3：Embedding + 向量数据库
- V4：Agent、Function Calling、MCP
