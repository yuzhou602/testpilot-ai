# TestPilot AI 工作台设计审查

审查日期：2026-09-13
页面：`/workspace?lang=zh`
类型：APP UI / Developer Tool
验证尺寸：1440×900、1366×768、1024×768

## 结论

工作台已经形成明确的 Agent First 布局：任务列表负责切换，中央区域呈现目标、当前动作、执行计划与实时活动，右侧只承载证据和环境上下文。三种目标尺寸均无横向溢出，1024px 下会自动折叠全局导航并隐藏右侧上下文。

## 已修复问题

### FINDING-001：低高度窗口中的失败摘要被裁切

- 影响：高
- 观察：1366×768 下，失败摘要的两个操作按钮超过容器有效高度，第二个按钮与下方执行计划发生视觉冲突。
- 修复：为低于 800px 的视口保留足够的执行摘要高度，并隐藏非必要的解释段落。
- 状态：已验证
- 文件：`frontend/src/views/workspace/Workspace.vue`
- 修复前：[截图](screenshots/workspace-1366-before.png)
- 修复后：[截图](screenshots/workspace-1366-after.png)

### FINDING-002：1024px 断点隐藏了不存在的元素

- 影响：中
- 观察：样式试图隐藏 `.current-result`，但真实操作区类名是 `.current-actions`。第三列只能依靠父容器裁切消失，响应式意图不明确。
- 修复：在 1024px 及以下明确隐藏 `.current-actions`，让当前任务摘要稳定保持两列。
- 状态：已验证
- 文件：`frontend/src/views/workspace/Workspace.vue`
- 修复前：[截图](screenshots/workspace-1024-before.png)
- 修复后：[截图](screenshots/workspace-1024-after.png)

### FINDING-003：浏览器请求缺少产品图标

- 影响：润色
- 观察：页面首次打开出现一个 404 请求，浏览器标签页也缺少 TestPilot 品牌标识。
- 修复：增加克制的测试探针 SVG 图标并在 HTML 中声明 favicon。
- 状态：已验证，复测控制台错误和失败请求均为 0
- 文件：`frontend/index.html`、`frontend/public/testpilot-mark.svg`

## 设计自检

| 检查项 | 结果 |
|---|---|
| 首屏能否识别软件测试产品 | 是 |
| Agent 当前动作是否可见 | 是 |
| FAIL 是否能快速定位 | 是，红色只用于失败证据和状态 |
| 主工作区是否占据最大面积 | 是 |
| 是否退化成 KPI 卡片后台 | 否 |
| 卡片是否过多 | 否，主要使用连续面板和工具栏 |
| 颜色是否承担语义 | 是 |
| 1024px 是否出现横向溢出 | 否 |
| 控制台是否存在错误 | 否 |

## 评分

- 设计完成度：8.2/10 → 8.7/10
- AI 模板感：2.0/10 → 1.5/10，越低越好
- 响应式可靠性：8.0/10 → 9.0/10

## 尚未纳入本轮

- 小于 768px 只保证查看任务与报告，不承诺完整执行 API/UI 测试。
- Element Plus vendor 包仍超过 Vite 的 500 kB 提示阈值；属于加载性能问题，不影响本轮界面正确性。
- 项目当前不是 Git 仓库，因此无法按照审查流程为每项修复建立独立提交。
