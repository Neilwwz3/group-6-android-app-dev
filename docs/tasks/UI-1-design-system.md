# UI-1：视觉规范和通用组件

| 项目 | 内容 |
| --- | --- |
| 负责 | 李同学（ketianli082-stack） |
| 分支 | `feature/ui-1-design-system` |
| 截止 | 11 月 6 日（11 月 7 日开会时一起看） |
| 前置 | 先完成 T0 |

## 目标

把效果图整理成全组统一使用的视觉规范，并写成 Android 资源文件。以后所有页面都只用这里定义的颜色、字号、间距和样式，保证整个 App 是一个风格。规范以 [Material Design 3](https://m3.material.io/) 为准，项目已经在用 Material 3 主题。

## 这次不做

- 不做具体页面（首页、片库等）。页面等核心需求定了以后另外派。
- 不做暗色模式，App 固定用浅色主题。
- 不写 Java 代码。
- 不加字体文件或第三方库，字体用系统默认。

## 要交付的

1. **颜色**：`res/values/colors.xml`
   - 品牌主色（效果图里的蓝色）和它的浅色背景
   - 页面背景、卡片背景、分割线
   - 文字：主要、次要、提示
   - 四种观看状态各一组（文字色 + 浅底色）：想看、在看、已看完、弃坑
   - 评分星星的颜色
   - 色值从效果图里取。命名按用途，例如 `color_primary`、`status_watching_bg`，不要用 `blue_1` 这种按颜色起的名字。
2. **主题**：`res/values/themes.xml`
   - 父主题改为 `Theme.Material3.Light.NoActionBar`
   - 把上面的颜色接到 Material 3 的主题属性：`colorPrimary`、`colorOnPrimary`、`colorPrimaryContainer`、`colorSurface`、`colorOnSurface`、`android:colorBackground` 等
   - 删除 `res/values-night/themes.xml`，暗色模式也用浅色配色
3. **字号、间距、圆角**：新建 `res/values/dimens.xml`
   - 字号 5 级：大标题、页面标题、卡片标题、正文、说明文字（单位 sp）
   - 间距：4 / 8 / 12 / 16 / 24 dp
   - 圆角：卡片、徽章、按钮
4. **通用样式**：新建 `res/values/styles.xml`，至少包括：
   - 文字样式：`TextAppearance.Group6.Headline`、`.Title`、`.CardTitle`、`.Body`、`.Caption`
   - 卡片：`Widget.Group6.Card`
   - 状态徽章 4 种：`Widget.Group6.StatusBadge.Want`、`.Watching`、`.Watched`、`.Dropped`（需要的圆角背景放在 `res/drawable/`）
   - 筛选标签：`Widget.Group6.FilterChip`
   - 只读的小号评分星级：`Widget.Group6.RatingBar.Small`
   - 底部导航：`Widget.Group6.BottomNavigation`（选中是主色，未选中是灰色）
   - 悬浮按钮：`Widget.Group6.Fab`
5. **组件样例页**：新建 `res/layout/design_sample.xml`
   - 一个可以滚动的页面，把上面所有颜色、文字样式和组件摆出来
   - 再放一张示例影视卡片：海报占位、标题、“年份 · 类型 · 题材”、评分星级、状态徽章
   - 底部导航放 4 个占位项即可，页签名称等核心需求定了再改
   - 不用写 Java。在 Android Studio 里打开这个布局，切到 **Split** 或 **Design** 预览就能看到效果
6. **规范说明**：新建 `docs/design/design-system.md`
   - 用表格列出每个颜色、尺寸、样式的资源名和用途
   - 附上样例页的截图
   - 其他组员和他们的 AI 会照这份说明使用样式，所以要写清楚“什么情况用哪个”

## 可以改

- `res/values/colors.xml`、`res/values/themes.xml`、`res/values/strings.xml`（只为样例页添加文字）
- 新建 `res/values/dimens.xml`、`res/values/styles.xml`
- 删除 `res/values-night/themes.xml`
- `res/drawable/` 下新建文件
- 新建 `res/layout/design_sample.xml`
- 新建 `docs/design/` 下的文件

## 不要改

- 任何 Java 文件、`AndroidManifest.xml`、所有 Gradle 文件
- `res/layout/activity_main.xml`

## 验收标准

1. App 能正常编译，在模拟器上能运行。
2. `colors.xml` 包含“要交付的”第 1 条列出的全部颜色，并且按用途命名。
3. 样例页预览里能看到：5 级文字样式、卡片、4 种状态徽章、筛选标签（选中和未选中两种状态）、只读评分星级、底部导航、悬浮按钮、示例影视卡片。
4. 所有布局文件里没有写死的色值（搜不到 `#` 开头的颜色）。
5. `docs/design/design-system.md` 写清了每个资源的名字和用途。

## 自测

1. 打开 `design_sample.xml`，切到 Split 预览，截一张完整的图（太长可以分两张）。
2. 在 `res/layout/` 里搜索 `#`，确认没有写死的颜色。
3. 运行 App，截一张模拟器的图。
4. 把截图按验收标准的编号贴进 PR。

## 给 AI 的提示（可以直接复制）

> 请先阅读仓库根目录的 AGENTS.md 和 docs/tasks/UI-1-design-system.md。这是一个 Java + XML + Material 3 的 Android 项目。请根据我附上的效果图，按任务卡生成 colors.xml、themes.xml、dimens.xml、styles.xml、design_sample.xml 和 docs/design/design-system.md。不要写 Java 代码，不要改任务卡“可以改”以外的文件。每一步告诉我改了哪些文件。
