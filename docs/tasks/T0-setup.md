# T0：搭好开发环境，提交第一个 PR

| 项目 | 内容 |
| --- | --- |
| 负责 | 李同学、杨同学、郭同学（每人各做一遍） |
| 分支 | `feature/t0-<你的 GitHub 用户名>` |
| 截止 | 11 月 4 日 |
| 卡住了 | 在群里找统筹，附截图 |

## 为什么做

确认你的电脑能编译运行这个项目，并完整走一遍“建分支 → 提交 → 推送 → 开 PR”。以后每个任务都是这个流程。

可以把这张卡贴给 AI，让它一步步陪你做。

## 步骤

### 1. 接受仓库邀请

打开 GitHub 通知或注册邮箱里的邀请邮件，点 **Accept invitation**。找不到的话，打开 https://github.com/Neilwwz3/group-6-android-app-dev/invitations 。

### 2. 安装 Android Studio

- 从 https://developer.android.com/studio 下载最新稳定版，安装时全部用默认选项。
- 第一次打开会下载 Android SDK，等它完成。
- 不用另外装 JDK，Android Studio 自带。

### 3. 克隆项目

1. 在 Android Studio 欢迎页点 **Clone Repository**。如果已经打开了别的项目，用 **File → New → Project from Version Control**。
2. URL 填 `https://github.com/Neilwwz3/group-6-android-app-dev.git`。保存位置选一个**路径里没有中文和空格**的文件夹，点 **Clone**。
3. 弹窗要求登录 GitHub 时，按提示在浏览器里登录。
4. 问是否信任项目时，选 **Trust Project**。

### 4. 等待 Gradle 同步

- 窗口底部的进度条走完、Build 窗口没有红字，就是同步成功。第一次可能要 5–15 分钟。
- 如果提示缺少 SDK Platform 37（例如 “Failed to find target”），点提示里的安装链接，装完会自动重新同步。

### 5. 建模拟器并运行

1. 打开右侧的 **Device Manager** → 点 **+** → **Create Virtual Device**。
2. 选 **Pixel 8** → 系统镜像选列表里最新的版本（第一次要点下载）→ **Finish**。
3. 在顶部的设备下拉框选这台模拟器，点绿色的 **▶ Run**。
4. 模拟器里出现 App、屏幕上显示 “Hello World!”，就成功了。**截一张图**。

### 6. 新建分支

1. 点窗口顶部工具栏里的当前分支名 `main`（旧版界面在右下角）。
2. 选 **New Branch**，名称填 `feature/t0-<你的 GitHub 用户名>`，勾选 **Checkout branch**，点 **Create**。
3. 确认工具栏显示的分支名已经变成新分支。

### 7. 新建你的成员卡

1. 左侧项目面板顶部的下拉框从 **Android** 切换成 **Project**，才能看到 `docs` 文件夹。
2. 在 `docs/members/` 下新建两个文件（文件夹不存在就新建）：
   - `<你的 GitHub 用户名>.md`：复制下面的模板，逐项填写
   - `<你的 GitHub 用户名>.png`：第 5 步的截图

```markdown
# <称呼，例如 李同学>

- GitHub：
- 擅长或做过的（例如网页前端、Android、UI 设计、写文档、剪视频）：
- 11 月 19 日前每周大约能投入几小时：
- 平时用的 AI 工具（Codex、ChatGPT、Claude、Cursor 等）：
- Android Studio 版本（Help → About）：
- 模拟器型号和系统版本：
- 搭环境时遇到的问题（没有就写“无”）：

![运行截图](<你的 GitHub 用户名>.png)
```

### 8. 提交并推送

1. 打开左侧的 **Commit** 面板（或菜单 **Git → Commit**）。
2. **只勾选刚才新建的两个文件**，其他文件一律不勾。
3. 提交信息填 `T0: add member card for <你的 GitHub 用户名>`。
4. 点 **Commit and Push**，在弹出的窗口里再点 **Push**。

### 9. 在 GitHub 上开 PR

1. 打开仓库网页，顶部会出现黄色提示条 **Compare & pull request**，点它。
2. 确认是从你的分支合并到 `main`。
3. 按自动带出的模板填写，截图一栏贴上第 5 步的截图。
4. 点 **Create pull request**。几分钟后，PR 页面会显示自动编译的结果：绿色对勾表示通过。
5. **不要自己点合并**，等统筹审批。

## 验收标准

1. PR 里只有 `docs/members/` 下的两个新文件。
2. 成员卡每一项都填了。
3. 附了模拟器运行成功的截图。
4. 自动编译显示绿色对勾。

## 常见问题

- **Gradle 同步很慢或报网络错误**：检查代理或 VPN，然后用 **File → Sync Project with Gradle Files** 重试。
- **模拟器启动不了**：把报错截图发到群里。通常是电脑没有开启 CPU 虚拟化。
- **提交面板里出现很多不认识的文件**：不要勾选，只勾你的两个文件。
- **推送时报权限错误**：确认第 1 步已经接受了邀请。
