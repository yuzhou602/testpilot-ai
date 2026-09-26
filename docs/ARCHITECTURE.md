# TestPilot AI 架构说明

![TestPilot AI 系统架构](diagrams/testpilot-architecture.svg)

该图同时提供 [Mermaid 源文件](diagrams/testpilot-architecture.mmd) 和 [可编辑 Excalidraw 文件](diagrams/testpilot-architecture.excalidraw)。

TestPilot AI 使用 Vue 3 前端呈现 Agent 测试工作流，使用 Spring Boot 后端提供认证、项目数据、Agent Runtime、工具调用、Trace、SSE 和持久化能力。默认展示模式可以完全离线运行；联调模式则连接真实后端。

## 系统边界

```text
┌───────────────────────────────────────────────────────────────┐
│ Vue 3 Frontend                                                │
│                                                               │
│ Login / Projects / Workspace / API / UI / Bugs / Reports      │
│ Requirements / Test Cases / Trace / Evaluation / Settings     │
│                                                               │
│ Pinia State ─ Demo Runtime ─ REST Client ─ SSE Client          │
└──────────────────────────────┬────────────────────────────────┘
                               │ REST + SSE
┌──────────────────────────────▼────────────────────────────────┐
│ Spring Boot Backend                                           │
│                                                               │
│ Controller → Service → Repository                             │
│                    │                                          │
│                    ├─ Agent Planner / State Machine / Executor │
│                    ├─ Tool Registry                           │
│                    ├─ Failure Analyzer                        │
│                    └─ Trace Recorder / Evaluation             │
└───────────────┬──────────────────┬─────────────────┬──────────┘
                │                  │                 │
          MySQL / Redis       LLM Provider     Target System
                                              API / Browser / DB
```

## 前端模块

### 页面层

页面位于 `frontend/src/views`：

- `workspace`：Agent Goal、Plan、Activity 和 Context。
- `apitesting`：API Tree、请求编辑、响应、断言和 AI 建议。
- `uitesting`：浏览器截图、测试步骤、Locator 检查与 Self-Healing。
- `trace`：使用 Vue Flow 展示 Agent 节点与失败路径。
- `requirements`、`testcases`、`bugs`、`reports`：测试资产与结果。
- `evaluation`：Agent 版本比较与质量门禁。
- `Settings.vue`：环境、Agent 权限、模型路由与证据策略。

### 组件层

`frontend/src/components/agent` 提供：

- `AgentComposer`：测试目标输入和快捷场景。
- `AgentPlan`：紧凑执行计划。
- `AgentActivity`：工具、HTTP、断言和系统事件流。

`frontend/src/components/common` 提供状态、风险、覆盖率、JSON、Tool Call、语言切换和演示导览等通用组件。

### 状态与运行模式

`frontend/src/stores/app.ts` 是主要状态入口，维护：

- 当前项目与任务；
- Agent Steps 与 SSE Events；
- 执行、暂停、取消和重放状态；
- Demo Runtime 与真实 API 的切换；
- Theme 与运行时连接状态。

`frontend/src/config/runtime.ts` 使用 `VITE_DEMO_MODE` 判断模式。默认值不是 `false` 时启用展示模式。

## 后端模块

后端包根目录为 `com.testpilot`。

| 模块 | 职责 |
|---|---|
| `agent` | 任务、步骤、规划、执行、状态机、Trace、Memory、RAG 与 Evaluation |
| `agent.tool` | HTTP、Browser、Database、Log、Knowledge、Requirement、TestData、Bug 工具 |
| `auth` / `security` | 用户、注册登录、JWT 和请求过滤 |
| `project` | 测试项目与环境入口 |
| `api` | OpenAPI 导入与 API Definition |
| `requirement` | 需求与业务规则 |
| `testcase` | 测试用例与场景 |
| `bug` | 结构化缺陷和证据 |
| `report` | 测试报告读取 |
| `execution` | 测试执行记录 |

## Agent 执行数据流

```text
POST /api/agent/tasks
        ↓
创建 AgentTask
        ↓
POST /api/agent/tasks/{id}/execute
        ↓
AgentPlanner 生成 AgentPlan
        ↓
AgentStateMachine 推进状态
        ↓
AgentExecutor / StepExecutor
        ↓
AgentToolRegistry 解析工具名称
        ↓
HTTP / Browser / Database / Log / Knowledge / ...
        ↓
Tool Result + Assertion
        ↓
FailureAnalyzer（失败时）
        ↓
AgentTraceRecorder 保存执行链
        ↓
SSE 推送给前端 Activity Stream
```

## Agent 状态

`AgentTaskStatus` 与运行时状态机共同表达任务生命周期。前端重点处理：

```text
CREATED
  → ANALYZING
  → PLANNING
  → READY
  → RUNNING
  → ANALYZING_FAILURE / REPLANNING / WAITING_USER
  → COMPLETED / FAILED / CANCELLED
```

状态颜色只表示语义：绿色通过、红色失败、蓝色或钢蓝色执行中、黄色警告。

## 实时事件

前端通过 `/api/agent/tasks/{id}/stream` 订阅 SSE。主要事件包括：

- `TASK_STARTED`
- `PLAN_CREATED`
- `STEP_STARTED`
- `STEP_COMPLETED`
- `STEP_FAILED`
- `TOOL_CALL`
- `TOOL_RESULT`
- `ASSERTION`
- `FAILURE_ANALYSIS`
- `WAITING_USER`
- `TASK_COMPLETED`
- `TASK_FAILED`

连接断开时，Store 使用有限次数的指数退避重连；任务结束或取消后主动关闭连接。

## 展示模式与联调模式

### 展示模式

```dotenv
VITE_DEMO_MODE=true
VITE_AUTH_REQUIRED=false
```

优点：

- 不依赖外部服务；
- 执行结果稳定；
- 适合截图、录屏和面试；
- 可以重放 Agent Steps 与 Events。

代价：

- 请求、Trace 和评测指标来自预设场景；
- 不能证明真实目标系统已经被执行；
- 浏览器 Self-Healing 是交互式产品演示，而非浏览器实时录像。

### 联调模式

```dotenv
VITE_DEMO_MODE=false
VITE_AUTH_REQUIRED=true
```

优点：

- 使用真实项目、任务和测试数据；
- SSE 反映真实 Agent 事件；
- 可以连接模型和目标测试环境。

代价：

- 需要数据库、Redis、模型服务和测试环境；
- 执行结果会受网络、模型和目标系统状态影响；
- 需要处理凭据、数据隔离和操作审批。

## Evidence First 设计

失败信息分成三层：

1. **Fact**：已观察到的状态码、断言、日志或 DOM 行为。
2. **Evidence**：支撑事实的请求响应、日志、Schema、截图和 Trace 节点。
3. **AI Hypothesis**：Agent 根据证据生成的可能原因，带置信度并标注未确认。

这种设计牺牲了“一句话给出答案”的速度，但减少了模型推测被误认为事实的风险。

## Self-Healing 决策

```text
Original Locator Failed
        ↓
收集 DOM / Text / Role / Visual Evidence
        ↓
生成 Candidate Locator
        ↓
计算 Confidence
        ↓
临时重试
        ↓
恢复成功
        ↓
人工确认后才永久更新
```

临时恢复与永久修改分开，是为了避免一次偶然匹配污染后续测试资产。

## 安全边界

- JWT 保护需要认证的后端接口。
- 数据库 Tool 应限制为只读或受审批操作。
- Settings 中的 Agent Policy 表达危险工具审批、证据脱敏和 Locator 阈值。
- `.env.example` 中的值只用于本地示例；真实部署必须使用密钥管理服务。
- AI Hypothesis 不能替代人工确认。

## 已知工程边界

- Element Plus 公共 Chunk 当前较大，后续可进一步按需拆分。
- 前端冒烟测试验证页面可渲染，但不是完整端到端测试。
- 展示模式的新项目与设置只保存在浏览器状态。
- 商业化仍需补充多租户、权限矩阵、审计、限流、队列调度和数据保留合规。

## 相关文档

- [返回 README](../README.md)
- [演示指南](DEMO_GUIDE.md)
- [面试讲解稿](INTERVIEW_SCRIPT.md)
- [API 参考](API.md)
