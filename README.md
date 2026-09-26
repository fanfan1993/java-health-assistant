# AI 心理健康助手（AI Mental Health Assistant）

一站式心理健康服务平台：**AI 情绪陪伴（SSE 流式对话）+ 专业心理量表测评 + 情绪日记 + 数据可视化洞察 + 管理后台**。

前后端分离架构：后端 SpringBoot 3 + Spring AI + MyBatis-Plus + MySQL/Redis + JWT；前端 React 19 + TypeScript + Vite + Ant Design 5 + ECharts。

> 无 MySQL、无 AI API Key 也能一键跑通全部功能（H2 内存库 + 内置 Mock 流式模型）。

```
mental-health-assistant/
├── backend/     # Spring Boot 3 + Spring AI 后端服务（端口 8080）
└── frontend/    # React 19 + Vite 前端（端口 5173，/api 代理到后端）
```

## 功能总览

| 模块 | 说明 |
| --- | --- |
| 认证注册 | JWT 令牌、BCrypt 密码加密、USER / ADMIN 双角色权限 |
| AI 心灵伙伴 | SSE 流式对话，打字机逐字渲染、多轮上下文、会话管理 |
| 心理测评 | 内置 SDS 抑郁 / SAS 焦虑 / PSS 压力 / PSQI 睡眠 4 套量表共 55 题，正反向计分、标准分换算、分级判定与建议 |
| 情绪日记 | 心情 1-5 级 + 标签 + 内容，分页管理、乐观更新 |
| 数据看板 | 14 天心情趋势、情绪分布玫瑰图、测评轨迹、平均心情、连续打卡天数 |
| 管理后台 | 用户列表、平台总览（仅 ADMIN） |

**演示账号**：`admin / admin123`（管理员，可见全部页面）。

## 快速启动

### 方式一：H2 内存库（零依赖，推荐体验用）

```bash
# 1. 启动后端（自动建表 + 内置 admin 账号，重启数据清空）
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=h2

# 2. 启动前端（另开终端）
cd frontend
npm install --registry=https://registry.npmmirror.com
npm run dev
```

打开 http://localhost:5173 即可使用。

### 方式二：MySQL（持久化，推荐开发用）

```bash
# 1. 初始化数据库
mysql -uroot -p < backend/src/main/resources/schema.sql

# 2. 按需修改 backend/src/main/resources/application.yml
#    MySQL 连接（默认 localhost:3306/mind_assistant，root/123456）
#    Redis 连接（默认 localhost:6379）
#    jwt.secret（生产环境务必替换为长随机字符串）
#    手动执行 data.sql 中的 INSERT 创建 admin 账号

# 3. 启动后端与前端（同方式一，后端去掉 profiles 参数）
cd backend && mvn spring-boot:run
cd frontend && npm run dev
```

### 接入真实大模型（可选）

支持任何 OpenAI 兼容接口（DeepSeek、通义、Kimi、OpenAI 等）：

```bash
export AI_API_KEY=sk-你的真实key
export AI_BASE_URL=https://api.deepseek.com   # DeepSeek 示例
export AI_MODEL=deepseek-chat
cd backend && mvn spring-boot:run
```

> 未配置 Key（或仍为 `sk-xxx` 占位）时自动切换内置 Mock 流式模型，逐词延迟输出预置共情回复，全部功能可完整联调。

---

# 后端（backend/）

## 技术栈

| 分类 | 技术 |
| --- | --- |
| 语言/框架 | Java 17、Spring Boot 3.3.5 |
| AI | Spring AI 1.0.0-M4（兼容 DeepSeek 等 OpenAI 兼容接口） |
| ORM | MyBatis-Plus 3.5.7 |
| 数据库 | MySQL 8（可选 H2 内存库 profile） |
| 缓存 | Redis |
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

---

# 前端（frontend/）

## 技术栈

- **React 19** + **TypeScript（strict）** + **Vite 6**
- **react-router v7**：集中式路由配置 + 懒加载 + 路由守卫
- **zustand v5**（persist 持久化 token / 主题）
- **antd v5** + @ant-design/icons
- **echarts v5** + 自封装 `useECharts` hook
- **axios 封装**：统一 Result 解包、token 注入、错误提示、401 跳登录
- **SASS**：设计变量 / mixins / 嵌套，治愈系紫青渐变（#6C5CE7 → #00CEC9）
- 无富文本、无额外动画库（全部 CSS3 animation/transition）

## React 19 特性使用

- `useActionState`：登录/注册表单（form action + pending）
- `useOptimistic`：日记删除/新增乐观更新
- `useTransition`：导航、测评提交、日记操作等非阻塞过渡（含 async transition）
- `use(Promise)`：仪表盘统计、测评量表、管理后台数据（Suspense 边界）
- `use(Context)`：主题切换按钮直接 `use(ThemeContext)` 读取
- Context 直接作 Provider：`<ThemeContext value={...}>`
- `useId`：登录/注册表单字段关联
- ref 直接作为 props 传递（不再需要 forwardRef）：GlassCard

## 页面

| 路由 | 说明 |
| --- | --- |
| /login /register | 登录/注册（渐变玻璃拟态 + 光斑动画） |
| / | 主布局（渐变玻璃侧边栏 + 顶部栏） |
| /dashboard | 仪表盘（数字滚动统计卡 + 心情趋势渐变面积图 + 情绪玫瑰图 + 测评记录） |
| /chat | AI 心灵伙伴（SSE 流式对话 + 打字机效果） |
| /assessment | 心理测评（量表列表 → 答题 → 环形分数结果页） |
| /journal | 情绪日记（卡片 + 抽屉新建 + 乐观删除） |
| /profile | 个人中心 |
| /admin | 管理后台（仅 ADMIN 角色可见） |

## 目录结构

> 规范：**一个组件/页面一个文件夹**，组件本体 + 私有样式 + `index.ts` 统一出口，外部引用路径保持 `@/components/Xxx` / `@/pages/Xxx` 不变。

```
src/
├── api/                  # request.ts axios 封装 + auth/chat/assessment/journal/stats/user
├── components/           # 通用组件（每个组件独立文件夹）
│   ├── GlassCard/        #   GlassCard.tsx + index.ts
│   ├── GradientButton/   #   GradientButton.tsx + GradientButton.scss + index.ts
│   ├── AnimatedNumber/
│   ├── PageTransition/
│   ├── AppLogo/
│   ├── LoadingScreen/
│   ├── ProtectedRoute/
│   └── ErrorBoundary/
├── context/              # ThemeContext
├── hooks/                # useSSE / useECharts / useCountUp / useDebounce / useDocumentTitle
├── pages/                # 页面（每页独立文件夹，页面级样式就近存放）
│   ├── Login/            #   Login.tsx + index.ts（样式共用 styles/auth.scss）
│   ├── Register/
│   ├── Layout/
│   ├── Dashboard/        #   Dashboard.tsx + Dashboard.scss + index.ts
│   ├── Chat/
│   ├── Assessment/
│   ├── Journal/
│   ├── Profile/
│   └── Admin/
├── router/               # index.tsx 集中式路由配置（lazy + 守卫）
├── store/                # useAuthStore / useThemeStore / useChatStore（zustand + persist）
├── styles/               # _variables / _mixins / global.scss / auth.scss（登录注册共用）
└── types/                # API 类型集中定义（UserInfo / Result<T> / Scale / Overview 等）
```

## 常用命令

```bash
npm install --registry=https://registry.npmmirror.com
npm run dev      # http://localhost:5173，/api 代理到后端 8080
npm run build    # tsc --noEmit && vite build
npm run preview
```

---

## 常见问题

- **登录提示 401？** 确认后端已启动且前端 `/api` 代理指向正确端口（`vite.config.ts` 中默认 8080，可用环境变量 `BACKEND_PORT` 覆盖）。
- **AI 对话是固定话术？** 未配置真实 API Key 时走内置 Mock 流式模型；按「接入真实大模型」配置后即为真实模型回复。
- **H2 模式重启后数据没了？** H2 为内存库设计，持久化请使用 MySQL 方式启动。
- **量表提交报「答案数量与题目数量不符」？** `answers` 传每题选中选项的**下标数组**，长度须等于题目数。
