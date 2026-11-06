# group-6-android-app-dev

COMP7506 智能手机应用开发，第 6 组课程项目（香港大学，2026）。

**暂定名：** MovieTracker，一款本地优先的个人观影记录与评分 App。

**功能：** 媒体库（添加、编辑、搜索、按状态筛选）、0–10 分评分和观看记录、首页概览、统计（总量和题材分布），以及主题包“观影日记”（全年观影热力图 + 按日期分组的时间线）。数据全部保存在手机本地，不需要账号。

**当前状态：** 第二波开发中，目标是 11 月 17 日冻结演示版本。进度见 [docs/tasks](docs/tasks/README.md)。

## 重要日期

| 日期 | 事项 |
| --- | --- |
| 2026 年 11 月 19 日（周四） | 课上演示 |
| 2026 年 11 月 30 日（周一）23:59 | 最终提交：文档、源代码和本 README、1–2 分钟介绍视频 |

## 成员与分工

| 成员 | 负责 |
| --- | --- |
| 王同学（Neilwwz3） | 统筹：调研、设计决策、数据层（ARCH-1）；G4 观影日记、G6 演示版本 |
| 李同学（ketianli082-stack） | UI 与视觉设计；G1 骨架、G5 首页和我的 |
| 杨同学（bfzzzzzz） | G2 管理片库、G6 演示版本 |
| 郭同学（v92663940-del） | G3 记录观看、G5 首页和我的 |

各目标的验收方式和截止日期见 [docs/tasks](docs/tasks/README.md)。

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

### 两个分支

| 分支 | 用途 | 谁往里合并 |
| --- | --- | --- |
| `dev`（默认分支） | 日常开发和 11 月 19 日的课上演示 | 所有组员通过 PR 合并 |
| `main` | 最终交付版本 | 只有统筹：11 月 19 日之后逐个模块审核、测试，再从 `dev` 合并过来 |

所有 PR 都提到 `dev`，不要向 `main` 提 PR。两个分支都受保护，不能直接推送。

### 互相审核

每个 PR 需要同时满足两个条件才能合并：自动编译通过，加上一位组员批准。批准之后由提交 PR 的人自己点合并。

| PR 作者 | 审核人 |
| --- | --- |
| 李同学 | 王同学 |
| 杨同学 | 郭同学 |
| 郭同学 | 李同学 |
| 王同学 | 杨同学 |

审核人要在模拟器上按交付目标的验收方式逐条试一遍：有问题就在 PR 里留言，没问题再点 Approve。改动共享文件（数据库、导航、主题）的 PR 还要统筹看过。

### 其他规则

- 一件事一个分支（`feature/<目标编号>-<简短英文名>`，例如 `feature/g2-library-list`），一个 PR；PR 标题以目标编号开头，例如 `G2: add library search`。
- 每天开工前先切到 `dev` 并拉取最新代码，再从 `dev` 新建分支。
- 只改自己负责的页面和文件。要改共享代码（数据模型、数据库、导航、主题）先问统筹。
- 添加第三方库前先问统筹，批准后统一写进 `gradle/libs.versions.toml`。
- 使用 AI 编程助手时，先让它读 [`AGENTS.md`](AGENTS.md)、[`docs/architecture.md`](docs/architecture.md)、[`docs/data-api.md`](docs/data-api.md) 和你负责的交付目标。
- 不要提交 `local.properties`、密钥文件和编译产物，`.gitignore` 已经排除了这些。

## 许可

[MIT](LICENSE)
