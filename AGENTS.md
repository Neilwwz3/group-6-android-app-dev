# AGENTS.md：给 AI 编程助手的项目规则

本文件写给 AI 编程助手（Codex、Claude Code、Cursor、Copilot、ChatGPT 等），也写给使用它们的组员。

**开始写任何代码之前，先读完本文件和要做的那张任务卡（`docs/tasks/` 下）。** 用网页版 AI 的同学，请把本文件和任务卡一起贴进对话。

如果任务卡和本文件有冲突，或者有任何不确定的地方，**停下来，让使用者去问统筹（Neilwwz3）**，不要自己猜。

## 项目是什么

- COMP7506 第 6 组的 Android 课程项目，暂定名 MovieTracker：本地优先的个人观影记录与评分 App。
- 核心需求还在调研，11 月 4 日确定。在此之前不要添加任务卡以外的功能。

## 技术栈（不许更换）

- 只用 **Java 11**。不要写 Kotlin 源文件，不要用 Jetpack Compose。
- 界面用 **XML 布局 + Material Components**，主题基于 Material 3。
- minSdk 24，targetSdk / compileSdk 37。
- 构建脚本是 Kotlin DSL（`*.gradle.kts`），依赖版本统一写在 `gradle/libs.versions.toml`。

## 包结构

基础包名：`hk.hkucs.group_6_android_app_dev`

```
ui/<功能名>/       Activity、Fragment、Adapter，以及这个功能自己的 ViewModel
data/db/           Room 数据库、Entity、DAO（只有统筹能改）
data/repository/   Repository：界面层访问数据的唯一入口
util/              小工具类
```

骨架还在搭建，具体路径以任务卡为准。

## 架构规则

- MVVM：Activity / Fragment 只负责显示和接收操作；数据经 ViewModel → Repository 取得。
- 每个功能一个自己的 ViewModel，不要把所有东西塞进一个全局 ViewModel。
- 数据库读写和网络请求不能放在主线程。
- 不要自己新建数据库表，也不要修改 Entity 或 DAO。需要新的数据或字段时，让使用者去问统筹。

## 不许改的共享文件（除非任务卡明确允许）

- `build.gradle.kts`、`app/build.gradle.kts`、`settings.gradle.kts`、`gradle.properties`、`gradle/` 目录
- `app/src/main/AndroidManifest.xml`
- `data/db/` 下的所有文件
- `MainActivity.java` 和导航结构
- `res/values/themes.xml`、`colors.xml`、`dimens.xml`、`styles.xml`（由 UI 负责人维护）
- `README.md`、`AGENTS.md`、`CLAUDE.md`、`.github/`、`docs/tasks/`

## 依赖

- 不要添加任何第三方库，不要修改任何版本号。确实需要时，让使用者去问统筹。

## 只做任务卡上的事

- 只改任务卡“可以改”里列出的文件。需要改其他文件时，先停下来问。
- 不要顺手重构、改名、重新格式化或删除别人的代码。
- 每次回答时列出你新建和修改了哪些文件。

## 界面语言：一律英文（已定，不再讨论）

用户在 App 里能看到的所有内容都用英文，没有例外：

- 页面文字、按钮、标题、提示、空状态、Toast、对话框、通知、错误信息
- 数据里会显示出来的值：示例数据的标题、简介、标签、观后感，题材词（`Genres.ALL`），状态和类型的显示名
- 日期和数字的格式：用英文格式，代码里格式化时指定 `Locale.US`，例如 `Nov 19, 2026`
- App 名称：MovieTracker

所有界面文字写进 `strings.xml`，不要写死在布局或 Java 里。不要添加中文翻译（不建 `values-zh/`）。

不受这条限制的：代码注释、项目文档（`docs/`、`README.md`、任务说明）、PR 描述和讨论，用中文或英文都可以。

旧文档或旧 PR 里如果还有“界面用中文”的说法，一律以本节为准。

## 代码和资源规范

- 界面上显示的文字一律写进 `res/values/strings.xml`，用英文（见上一节），不要写死在布局或 Java 里。
- 颜色、字号、间距使用主题属性或 `colors.xml` / `dimens.xml` 里的资源，不要在布局里写死色值。
- 类名用大驼峰（`LibraryFragment`），资源名用小写加下划线（`fragment_library.xml`、`item_media.xml`）。
- 标识符用英文；注释可以用中文。

## 隐私与版权

- 代码、测试、文档、截图和示例里不得出现真实的服务器地址、账号、密码或令牌。
- 不要使用网上下载的电影海报，不要复制豆瓣、TMDB 等网站的简介原文。

## Git 规则

- 不要直接提交到 `dev` 或 `main`，不要 force push。所有 PR 都提到 `dev`，不要向 `main` 提 PR。
- 新建分支前，先切到 `dev` 并拉取最新代码，再从 `dev` 新建分支。
- 分支名：`feature/<任务编号>-<简短英文名>`，例如 `feature/ui-1-design-system`。
- 提交信息：`<任务编号>: <做了什么>`，例如 `UI-1: add status badge styles`。
- 不要提交 `local.properties`、`build/` 目录、APK、密钥文件。

## 提交 PR 之前

1. 在 Android Studio 里能成功编译（Build → Make Project），在模拟器上能运行。
2. 按任务卡的“自测”一节逐条检查并截图。
3. 按 PR 模板填写说明。PR 提交后 GitHub 会自动编译一次；出现红色叉号说明编译失败，需要修好。
