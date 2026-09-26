# TestPilot AI 面试讲解稿

这份讲解稿按 3 分钟设计，适用于前端、全栈或软件工程岗位。不要逐字背诵；记住“问题—方案—实现—结果—边界”五段结构即可。

## 30 秒开场

> 我做的项目叫 TestPilot AI，是一个 AI Agent 驱动的软件测试与缺陷分析平台。它和普通测试管理后台最大的区别是，首页不是统计看板，也不是聊天框，而是 Agent 的实时测试工作区。用户描述测试目标后，Agent 会制定计划、调用 API 或浏览器工具执行测试，并把失败结果关联到响应、日志、Schema 和 Trace，最终生成缺陷和测试报告。

展示动作：停留在登录页，指向 `T-102 · AUTHENTICATION TEST` 和 `500 ERROR`，然后点击“进入产品演示”。

## 第 1 分钟：为什么这样设计

> 我最先解决的问题是 AI 执行过程不可见。如果只做一个聊天框，用户只知道模型给出了结论，却不知道它测了什么、调用了什么工具、为什么判断失败。所以我把产品结构设计成 Agent First 和 Evidence First：中央区域展示 Goal、Plan、当前步骤和 Activity Stream，右侧展示环境、覆盖率与证据。用户可以沿着失败事件直接进入 Trace。

> 视觉上我没有采用传统后台的 KPI 卡片矩阵，也减少了大面积渐变和发光效果。整体使用紧凑的三栏 Developer Tool 布局，让主工作区占最大面积，颜色只承担通过、失败、执行中和风险这些状态含义。

展示动作：指出 Workspace 的 Mission、Current Execution、Plan 和 Activity。

## 第 2 分钟：核心技术实现

> 前端使用 Vue 3、TypeScript、Vite、Pinia、Vue Router、Element Plus 和 Vue Flow。Pinia Store 同时管理当前项目、任务、步骤和 SSE 事件。真实联调时，前端通过 REST 创建任务，通过 SSE 接收 Plan、Step、Tool Call、Assertion 和 Failure Analysis 等事件；连接断开时会做有限次数的指数退避重连。

> 后端使用 Java 21 和 Spring Boot。Agent Runtime 由 Planner、状态机、Executor、Failure Analyzer、Tool Registry 和 Trace Recorder 组成。Tool Registry 中实现了 HTTP、Browser、Database、Log、Knowledge、Requirement、TestData 和 Bug 等工具。执行失败后，系统会将工具结果和断言写入 Trace，再由失败分析模块组织证据。

> 为了保证项目可以在没有数据库和模型 Key 的情况下稳定展示，我另外实现了前端 Demo Runtime。它使用确定性的 ShopSphere 场景模拟步骤推进、暂停、重放和失败事件；将 `VITE_DEMO_MODE` 设置为 `false` 后，再切换到真实 API 和 SSE。

展示动作：打开 Trace，指向节点类型、失败路径和右侧 Evidence。

## 第 3 分钟：最有价值的细节

> 我认为项目里最有价值的细节是把 Fact、Evidence 和 AI Hypothesis 分开。例如登录接口收到 500 是事实，DataIntegrityViolationException 和 username varchar(255) 是证据，而“DTO 可能缺少最大长度校验”只是 AI 推测。界面会显示置信度和未确认状态，避免用户误以为模型已经确认根因。

> UI Testing 里的 Locator Self-Healing 也不是简单提示修复成功，而是展示原 Locator、失败原因、候选 Locator、Text、Role、Confidence 和恢复结果。临时重试成功后仍需要人工确认，才会永久修改测试资产。

> 最终报告不只展示通过率，而是回答测了什么、哪里有风险、缺什么覆盖以及下一步应该做什么。AI Evaluation 页面还可以在相同数据集上比较 Agent 版本，并通过完成率、证据关联率、无依据结论率和覆盖率门禁决定是否发布。

展示动作：快速切换 UI Testing、Bug Center 和 Report。

## 15 秒收尾

> 这个项目让我完整实践了 AI Agent 产品设计、实时状态管理、复杂工具型 UI、证据可视化和前后端 Agent 架构。当前版本定位是可运行的展示与技术验证，不会把预设评测数据描述成真实生产 Benchmark；如果继续商业化，我会优先补充多租户权限、任务队列、审计与真实端到端评测集。

## 1 分钟压缩版

> TestPilot AI 是一个 AI Agent 驱动的软件测试平台。用户描述目标后，Agent 会规划并调用 API 或浏览器工具执行测试。前端使用 Vue 3、TypeScript、Pinia 和 Vue Flow，把 Goal、Plan、工具调用、断言、失败分析和 Trace 实时展示出来；后端使用 Spring Boot 构建 Agent Planner、状态机、Executor、Tool Registry 和 SSE 事件流。

> 项目最重要的设计是 Evidence First：500 响应是事实，日志和 Schema 是证据，DTO 缺少长度校验则明确标记为 AI Hypothesis。UI Locator 自愈也会展示候选、置信度和人工确认过程。为了保证面试现场稳定，我实现了离线 Demo Runtime，同时保留真实 REST 和 SSE 联调模式。

> 当前项目覆盖 Workspace、API/UI Testing、Self-Healing、Bug、Report、Trace 和 Agent Evaluation。下一步商业化需要补充多租户、任务队列、审计和真实基准评测。

## 高频追问与回答

### 为什么不用聊天框作为首页？

> 测试任务具有长时间、多步骤和多工具调用特征。聊天记录适合表达语言交互，但不适合快速判断当前步骤、失败节点、运行环境和证据完整性，所以我使用任务工作区与 Activity Stream，输入框只承担目标和命令输入。

### Demo Runtime 会不会显得项目是假的？

> Demo Runtime 是展示适配层，不是用来替代真实后端。真实后端已经有 Agent Task、Planner、状态机、Tool Registry、Trace 和 SSE 接口。默认演示使用确定性数据，是为了避免现场受到模型额度、数据库或目标系统状态影响；README 会明确说明两种模式的边界。

### 如何保证 AI 根因分析可信？

> 不把模型结论直接当事实。系统先记录响应、断言、日志和 Schema，再让 Agent 基于证据形成带置信度的 Hypothesis；界面标注“尚未确认”，并提供 Trace 供人工检查。生产化还应增加代码检索验证、评测集和人工反馈闭环。

### Locator Self-Healing 如何避免误修复？

> 使用 DOM 属性、文本、Role 和上下文生成候选并计算置信度。超过阈值的候选可以临时重试，但永久更新测试资产需要人工确认。这样把运行恢复和资产变更分开。

### SSE 为什么需要重连？

> Agent 任务可能持续数分钟，网络波动会让客户端丢失实时事件。Store 在任务仍处于运行状态时进行有限次数的指数退避重连；任务完成、失败或取消后立即关闭连接，避免无意义重试。

### 为什么使用 Vue Flow？

> Trace 包含 Agent、Tool、Decision、Error 等不同节点和分支关系，普通列表不容易表达因果路径。Vue Flow 可以展示拓扑关系，同时把节点详情放在右侧面板，避免图中承载过多文字。

### 项目目前最大的不足是什么？

> 展示层已经完整，但真实端到端验证仍不足。优先级最高的是建立可重复的真实评测数据集、补充 Playwright 集成测试和任务队列；其次才是多租户、审计和成本治理。Element Plus 公共 Chunk 也需要继续拆分。

### 你个人完成了什么？

> 我负责前端信息架构、Design System 和核心页面实现，完成 Workspace、API/UI Testing、Self-Healing、Evidence-Based Bug、Trace、Report 和 Evaluation；实现中英文切换、响应式布局、Demo Runtime、任务重放、SSE 状态处理和浏览器冒烟测试。同时梳理了后端 Agent Runtime 与工具系统的接口关系，并完善了 Docker 和项目文档。

## 面试表达注意事项

- 不要说“系统已经实现全自动根因定位”，应说“生成有证据支持、待确认的根因推测”。
- 不要把演示评测分数称为真实 Benchmark。
- 不要声称当前系统已经达到生产级多租户或高并发。
- 不要连续罗列框架名；每个技术点要对应一个具体问题。
- 优先展示 FAIL、Evidence 和 Trace，这是项目与普通后台差异最大的部分。
- 如果被问到数据真实性，主动解释 Demo Runtime 与联调模式的边界。

## 相关材料

- [项目 README](../README.md)
- [演示指南](DEMO_GUIDE.md)
- [架构说明](ARCHITECTURE.md)
- [API 参考](API.md)
