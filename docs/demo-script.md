# 演示脚本（G6）

11 月 19 日课上演示和 1–2 分钟介绍视频都照这份脚本走。操作说明用中文，讲解词用英文，可以直接念。

## 演示前准备

| 事项 | 做法 |
| --- | --- |
| 版本 | 安装 `dev` 上标签 `demo-v1` 构建的 debug APK，11 月 17 日冻结后不再换 |
| 设备 | 模拟器或真机均可，系统语言设为英文（系统弹窗和日期选择器才会是英文），日期用真实日期 |
| 数据 | 打开 App → **Profile** → **Load sample data** → 确认。演示前就载入好，不要在现场等 |
| 干扰 | 关掉通知、开勿扰；模拟器窗口放大到观众看得清 |
| 备份 | 提前把主线完整录一遍屏，现场出问题就播放录屏 |

示例数据：30 部作品，63 条观看记录，时间从 2025 年 12 月到 2026 年 11 月 11 日；1 月 5 日到 12 日连续 8 天有记录；5 部剧集“在看”，8 部作品“想看”。热力图四档深浅都有：2 月 22 日最深（一天 4 条），1 月 11 日、11 月 7 日次之（3 条）。

## 主线（约 2 分钟，介绍视频照这个拍）

| # | 页面 | 操作 | 讲解词 | 看点 |
| --- | --- | --- | --- | --- |
| 1 | Home | 停留 3 秒；点月份左边的 ‹ 切到 October，再点 › 回到 November | "MovieTracker is a private, local-first diary for everything you watch. Home shows what you're in the middle of, your latest entries and this month at a glance." | 本月概览、Continue watching、Recent entries |
| 2 | Library | 搜索框输入 `matrix`，再清空；点 **Want to Watch** 筛选；点开 **The Matrix** | "The library keeps every title with its status. Search and filters find anything in a second." | 搜索、状态筛选、海报卡片 |
| 3 | 作品详情 | 点 **Add record** → 日期保持今天，时长 `136`，评分 `9`，短评 `Still holds up.` → **Save** | "Logging a watch takes a few seconds: date, time, a 0-to-10 rating and a short note." | 记一笔表单 |
| 4 | 作品详情 | 返回后停留 2 秒 | "The entry is saved, and the film moves from Want to Watch to Watched automatically." | 新记录出现；状态徽章变成 Watched |
| 5 | Diary | 指一下热力图上今天的格子；点 2 月 22 日那个最深的格子 | "The diary turns a year of viewing into a heatmap: the darker the cell, the more you watched that day. Tap any day and the timeline jumps straight to it." | 热力图、点格子跳转时间线 |
| 6 | Profile | 停留 3 秒 | "Profile sums it all up: titles, time watched, average rating and your taste by genre. Everything stays on your phone, with no account." | 总量卡片、题材环形图 |

## 可选扩展（课上时间多时）

1. **添加作品：** Library → **Add a title** → 填一部示例数据里没有的电影（例如 `Dune: Part Two`，2024，Movie，Sci-Fi）→ 保存 → 在片库里找到它。
2. **编辑和删除：** 在刚加的作品详情里点 **Edit** 改年份；再点 **Delete** 并确认，回到片库它已消失。
3. **空状态：** Profile → **Clear all data** → 依次切到 Home、Library、Diary、Profile，展示每页的空状态提示 → 再 **Load sample data** 恢复。

## G6 验收清单

演示版本冻结前，由负责人以外的组员在模拟器上逐条操作。

- [ ] 清空数据后，Home、Library、Diary、Profile 四页都有空状态提示，作品详情的观看记录区也有提示
- [ ] 载入示例数据后，按主线 1–6 步走完，App 不崩溃
- [ ] 按可选扩展 1–3 步走完，App 不崩溃
- [ ] 主线第 4 步：The Matrix 的状态自动变成 Watched，Profile 里的 Watched 数加 1
- [ ] 所有界面文字和日期都是英文
- [ ] 横竖屏切换、按返回键、切到后台再切回来，页面内容不丢、不崩溃
