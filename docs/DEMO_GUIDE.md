# TestPilot AI 演示指南

这份指南用于简历项目展示、面试讲解和作品集录屏。目标是在 3–5 分钟内让观看者理解：TestPilot AI 不只是生成测试内容，而是让 Agent 真实地规划、执行、观察失败并组织证据。

## 最快开始

### 1. 启动前端

```bash
cd frontend
npm install
npm run dev
```

### 2. 打开登录页

访问：

```text
http://localhost:5173/login?lang=zh
```

### 3. 进入演示

点击“进入产品演示”。不需要填写账号，也不需要启动后端。系统会进入 ShopSphere 项目并自动打开“3 分钟产品导览”。

## 推荐讲解路线

### 第 1 段：产品定位（30 秒）

停留在登录页，说明：

> TestPilot AI 是一个 AI Agent 驱动的软件测试与缺陷分析平台。用户描述测试目标后，Agent 会规划测试、调用 API 或浏览器工具执行，并把失败结果关联到可复现证据。

指出登录页中的执行预览：

```text
T-102 · AUTHENTICATION TEST
POST /api/auth/login
500 ERROR
```

### 第 2 段：Agent 实时执行（60 秒）

进入 `/workspace`，重点展示：

1. Goal：用户原始测试目标。
2. Plan：Agent 生成的执行计划和步骤状态。
3. Current Execution：当前正在检查的边界条件。
4. Activity Stream：HTTP、Tool、Assertion 和 Agent 事件。
5. Context：环境、覆盖率、结果和证据。

可以点击重放按钮，让任务从第一步重新开始；也可以演示暂停与继续。

推荐讲解：

> 这里不是聊天记录。每个 Agent 行为都有明确步骤、工具类型、时间和执行结果，失败以后会进入证据关联阶段。

### 第 3 段：失败证据与 Trace（60 秒）

在工作台点击“查看证据”或“打开 Trace”。

重点展示：

- HTTP Response：`500 Internal Server Error`
- Application Log：`DataIntegrityViolationException`
- Schema：`username varchar(255)`
- Input：用户名长度 500
- AI Hypothesis：DTO 可能缺少最大长度校验
- 未确认说明：该推测还需要人工或代码检查确认

推荐讲解：

> 平台不会把模型推测直接展示为已确认根因。事实、证据和 AI Hypothesis 使用不同的视觉层级，降低错误归因风险。

### 第 4 段：UI Self-Healing（45 秒）

打开 `/ui-testing`，展示：

- 原始 Locator：`#login-btn`
- Element Not Found
- 候选 Locator：`button[type="submit"]`
- 文本与 Role 证据
- 94% Confidence
- 临时恢复与永久更新分开
- 永久修改需要人工确认

推荐讲解：

> Self-Healing 不是弹出“修复成功”，而是完整呈现失败、候选、证据、置信度和最终决策。

### 第 5 段：缺陷与报告（45 秒）

打开 `/bugs`，说明 Fact、Evidence 和 AI Hypothesis 的区别；随后打开 `/reports`，展示：

- Overall Risk
- Key Findings
- Coverage Gaps
- Evidence-Based Bugs
- Agent Recommendations

最后可以打开 `/evaluation`，展示 Agent v1.3 与基线版本的质量门禁对比。

## 3 分钟压缩版本

如果演示时间很短，只展示：

```text
Login 产品定位（20 秒）
→ Workspace 执行过程（60 秒）
→ Trace 证据链（60 秒）
→ Report 决策结果（40 秒）
```

不要在短演示里逐个介绍菜单。

## 演示前检查清单

- Node.js 20+ 可用。
- `frontend/node_modules` 已安装。
- `npm run build` 成功。
- `npm run test:smoke` 成功。
- 浏览器缩放为 100%。
- 推荐窗口至少 1440×900。
- `frontend/.env.local` 未将 `VITE_DEMO_MODE` 设置为 `false`。
- 首次打开使用 `/login?lang=zh`。

## 常见问题

### 页面没有演示数据

确认 `frontend/.env.local` 中为：

```dotenv
VITE_DEMO_MODE=true
```

修改环境变量后需要重新启动 Vite。

### 页面要求登录

确认：

```dotenv
VITE_AUTH_REQUIRED=false
```

或者从登录页点击“进入产品演示”。

### 5173 端口被占用

Vite 会自动选择下一个可用端口。请使用终端中 `Local:` 后显示的地址，例如：

```text
http://127.0.0.1:5174/
```

### Headless Chrome 偶尔没有生成截图

`npm run test:smoke` 依赖本机 Chrome 或 Edge。若某次显示 `0 bytes`，但构建没有错误，可先关闭残留的 Headless Chrome 进程并重新运行。连续失败时再检查对应页面。

### 如何重新打开导览

进入系统后，点击顶部工具栏的“演示导览”。在 1200px 以下它会收缩为 `?` 图标。

## 演示边界

演示时建议主动说明：

- 默认展示数据是确定性的前端场景，用来保证离线演示稳定。
- 后端包含 Agent Runtime、Tool Registry、SSE、JWT 和数据持久化结构。
- 真实 UI 自动化需要后端 Playwright Tool 与目标测试环境。
- Evaluation 页面演示的是评测产品形态，不代表已完成大规模模型基准测试。

这会让项目陈述更可信，也能体现你理解原型、演示系统与生产系统之间的差异。

## 下一步阅读

- [返回项目 README](../README.md)
- [查看架构说明](ARCHITECTURE.md)
- [查看 API 参考](API.md)
- [查看面试讲解稿](INTERVIEW_SCRIPT.md)
