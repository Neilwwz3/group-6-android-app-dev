# group-6-android-app-dev

COMP7506 智能手机应用开发，第 6 组课程项目（香港大学，2026）。

**暂定名：** MovieTracker，一款本地优先的个人观影记录与评分 App。

**当前状态：** 正在调研核心用户需求（11 月 2 日至 4 日），确定后在这里补充功能说明和其余分工。

## 重要日期

| 日期 | 事项 |
| --- | --- |
| 2026 年 11 月 19 日（周四） | 课上演示 |
| 2026 年 11 月 30 日（周一）23:59 | 最终提交：文档、源代码和本 README、1–2 分钟介绍视频 |

## 成员与分工

| 成员 | 分工 |
| --- | --- |
| Neilwwz3 | 调研与统筹：用户调研、设计决策、任务分配、代码审查 |
| 李同学（ketianli082-stack） | UI 与视觉设计 |
| 杨同学（bfzzzzzz） | 待定 |
| 郭同学 | 待定 |

当前任务见 [docs/tasks](docs/tasks/README.md)。

## 技术栈

| 项目 | 版本 |
| --- | --- |
| 语言 | Java 11，XML 布局 |
| 构建 | Gradle 9.6.0（Wrapper）、Android Gradle Plugin 9.4.1、Kotlin DSL 构建脚本 |
| SDK | minSdk 24 / targetSdk 37 / compileSdk 37 |
| 界面 | AppCompat、Material Components（Material 3）、ConstraintLayout |

依赖版本统一写在 [`gradle/libs.versions.toml`](gradle/libs.versions.toml)。

## 编译和运行

1. 克隆仓库：
   ```bash
   git clone https://github.com/Neilwwz3/group-6-android-app-dev.git
   ```
2. 用 Android Studio 打开这个文件夹（**File > Open**），等待 Gradle 同步完成。Android Studio 会自动生成记录 SDK 路径的 `local.properties`，不要提交它。
3. 选择模拟器或真机，点 **Run**。

命令行编译（Windows 上用 `gradlew.bat`）：

```bash
./gradlew assembleDebug   # 生成 debug APK，位于 app/build/outputs/apk/debug/
```

## 协作规则

- `main` 分支受保护，任何人都不能直接推送；所有改动通过 Pull Request 提交，由统筹审批后合并。
- 每个 PR 会自动编译一次，编译不通过不能合并。
- 一个任务一个分支（`feature/<任务编号>-<简短英文名>`），一个 PR。
- 每天开工前先拉取最新的 `main`。
- 只改任务卡允许改的文件。要改共享代码（数据模型、数据库、导航、主题）先问统筹。
- 添加第三方库前先问统筹，批准后统一写进 `gradle/libs.versions.toml`。
- 使用 AI 编程助手时，先让它读 [`AGENTS.md`](AGENTS.md) 和对应的任务卡。
- 不要提交 `local.properties`、密钥文件和编译产物，`.gitignore` 已经排除了这些。

## 许可

[MIT](LICENSE)
