# AI 心理健康助手 - 后端（Spring Boot 3 + Spring AI）

教学级企业项目后端：JWT 认证、AI 流式心理对话（SSE）、心理测评（SDS/SAS/PSS/PSQI）、心情日记、数据统计与管理端。

## 技术栈

| 分类 | 技术 |
| --- | --- |
| 语言/框架 | Java 17、Spring Boot 3.3.5 |
| AI | Spring AI 1.0.0-M4（spring-ai-openai-spring-boot-starter，兼容 DeepSeek 等 OpenAI 兼容接口） |
| ORM | MyBatis-Plus 3.5.7 |
| 数据库 | MySQL 8（可选 H2 内存库 profile） |
| 缓存 | Redis（spring-boot-starter-data-redis） |
| 认证 | java-jwt 4.4.0（HS256，7 天过期） |
| 工具 | Lombok、Hutool 5.8.27（含 BCrypt）、jakarta validation |

## 目录结构（经典分层架构）

```text
com.mind.assistant
├── controller      # 接口层：Auth/Chat/Assessment/Mood/Stats/Admin
├── service         # 业务层：认证、SSE 流式对话、测评计分、统计聚合、量表库
├── mapper          # 数据访问层：MyBatis-Plus Mapper
├── entity          # 数据库实体：User/ChatSession/ChatMessage/AssessmentRecord/MoodJournal
├── dto             # 请求/传输对象：Login/Register/Submit/MoodJournalDTO、Scale 系列
├── vo              # 视图对象：UserVO/OverviewVO（不含敏感字段）
├── config          # 工程配置：MyBatis-Plus、Web(CORS+拦截器)、Redis
├── security        # 安全认证：JwtUtil、JwtInterceptor、UserContext(ThreadLocal)
├── exception       # 异常处理：BizException、GlobalExceptionHandler
├── common          # 通用返回：Result、ResultCode
└── ai              # AI 能力：AiConfig(ChatClient 装配)、MockStreamingChatModel(无Key兜底)
```

## 快速启动

### 1. 初始化数据库（MySQL 方式，推荐）

```bash
mysql -uroot -p < src/main/resources/schema.sql
```

### 2. 修改配置

编辑 `src/main/resources/application.yml`：

- MySQL：默认 `localhost:3306/mind_assistant`，账号 `root/123456`，按需修改
- Redis：默认 `localhost:6379`
- JWT：`jwt.secret` 生产环境务必替换为长随机字符串

### 3. 启动

```bash
mvn spring-boot:run
```

服务运行在 http://localhost:8080 。

> **无 API Key 也能跑**：`spring.ai.openai.api-key` 默认是 `sk-xxx` 占位符，
> 项目检测到占位/为空时会自动启用内置 Mock 流式模型（逐词延迟输出预置共情回复），
> 全部功能可完整联调。

### 4. 无 MySQL 时使用 H2 内存库

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```

会自动用 `schema.sql` + `data.sql` 初始化（重启数据清空）。

## 配置真实 AI Key

支持任何 OpenAI 兼容接口（OpenAI、DeepSeek、通义、Kimi 等）：

方式一：环境变量（推荐）

```bash
export AI_API_KEY=sk-你的真实key
export AI_BASE_URL=https://api.deepseek.com   # DeepSeek 示例
export AI_MODEL=deepseek-chat
mvn spring-boot:run
```

方式二：直接改 `application.yml` 中 `spring.ai.openai.api-key`（不要以 `sk-xxx` 开头即视为真实 key）。

## 内置管理员

`data.sql` 提供了一个 admin 账号：用户名 `admin`，密码 `admin123`（BCrypt 密文）。MySQL 环境请手动执行该 INSERT。

## 接口清单

统一返回 `Result<T>`：`{code, message, data}`；鉴权请求头 `Authorization: Bearer <token>`（SSE 接口额外支持 `?token=`）。

### 认证

| 方法 | 路径 | 说明 | 鉴权 |
| --- | --- | --- | --- |
| POST | /api/auth/register | 注册 `{username,password,nickname}`，返回 `{token,user}` | 否 |
| POST | /api/auth/login | 登录 `{username,password}`，返回 `{token,user}` | 否 |
| GET | /api/user/me | 当前用户信息 | 是 |

### AI 对话

| 方法 | 路径 | 说明 | 鉴权 |
| --- | --- | --- | --- |
| GET | /api/chat/sessions | 会话列表（按更新时间倒序） | 是 |
| POST | /api/chat/session | 新建会话 `{title?}`，默认「新的对话」 | 是 |
| DELETE | /api/chat/session/{id} | 删除会话（校验归属） | 是 |
| GET | /api/chat/session/{id}/messages | 会话消息列表 | 是 |
| GET | /api/chat/stream?sessionId=&message= | SSE 流式对话（事件 `message`，data 为 `{delta:"..."}`，结束发 `[DONE]`） | 是（支持 ?token=） |

### 心理测评

| 方法 | 路径 | 说明 | 鉴权 |
| --- | --- | --- | --- |
| GET | /api/assessment/scales | 内置量表列表（含题目与选项分值） | 是 |
| POST | /api/assessment/submit | 提交作答 `{scaleId, answers:[选项下标]}`，返回得分/等级/建议/逐题明细 | 是 |
| GET | /api/assessment/records | 测评历史 | 是 |

### 心情日记

| 方法 | 路径 | 说明 | 鉴权 |
| --- | --- | --- | --- |
| GET | /api/mood/journal?page=&size= | 分页日记列表 | 是 |
| POST | /api/mood/journal | 新建日记 `{content,mood(1-5),tags:[...]}` | 是 |
| DELETE | /api/mood/journal/{id} | 删除日记（校验归属） | 是 |

### 统计

| 方法 | 路径 | 说明 | 鉴权 |
| --- | --- | --- | --- |
| GET | /api/stats/overview | 首页总览（14 天心情趋势、心情分布、最近 5 条测评、对话/日记数、平均心情、连续记录天数） | 是 |

### 管理端（仅 ADMIN）

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | /api/admin/users | 用户列表 |
| GET | /api/admin/overview | 平台总览 `{userCount,chatCount,journalCount,assessmentCount}` |

## SSE 前端联调示例

```js
const es = new EventSource(
  `/api/chat/stream?sessionId=1&message=你好&token=${token}`);
es.addEventListener('message', e => {
  if (e.data === '[DONE]') { es.close(); return; }
  const { delta } = JSON.parse(e.data);
  appendToChat(delta);
});
```

> 完整的项目介绍、快速启动、前端说明见根目录 [../README.md](../README.md)。
