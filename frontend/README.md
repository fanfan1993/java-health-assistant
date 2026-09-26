# AI 心理健康助手 - 前端（React 版）

源课程为 Vue3 版，本仓库为 React 19 改造版前端。

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

## 启动

```bash
npm install --registry=https://registry.npmmirror.com
npm run dev      # http://localhost:5173，/api 代理到后端 8080（BACKEND_PORT 可覆盖）
npm run build    # tsc --noEmit && vite build
npm run preview
```

## 页面

| 路由               | 说明                                      |
| ---------------- | --------------------------------------- |
| /login /register | 登录/注册（渐变玻璃拟态 + 光斑动画）                    |
| /                | 主布局（渐变玻璃侧边栏 + 顶部栏）                      |
| /dashboard       | 仪表盘（数字滚动统计卡 + 心情趋势渐变面积图 + 情绪玫瑰图 + 测评记录） |
| /chat            | AI 心灵伙伴（SSE 流式对话 + 打字机效果）               |
| /assessment      | 心理测评（量表列表 → 答题 → 环形分数结果页）               |
| /journal         | 情绪日记（卡片 + 抽屉新建 + 乐观删除）                  |
| /profile         | 个人中心                                    |
| /admin           | 管理后台（仅 ADMIN 角色可见）                      |

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

## 后端接口

`baseURL: /api`，响应统一 `{ code, message, data }`，完整接口契约见后端 [../README.md](../README.md)（SSE 流：`GET /chat/stream?sessionId=&message=&token=`，`data:` 载荷兼容 JSON `{"delta":"..."}` 与纯文本，`[DONE]` 结束）。
