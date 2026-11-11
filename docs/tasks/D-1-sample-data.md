# D-1：示例影视数据

| 项目 | 内容 |
| --- | --- |
| 负责 | 杨同学（bfzzzzzz） |
| 分支 | `feature/d-1-sample-data` |
| 截止 | 11 月 6 日 |
| 前置 | 先完成 T0 |

## 目标

准备一份约 30 部影视的示例数据，用在课上演示、介绍视频和开发测试里。助教和演示现场都没有真实的观影记录，App 要能一键载入这份数据，看起来像一个真实用户用了一年。

这次只准备数据文件。把数据载入 App 的功能，之后另外派人做。

## 要交付的

1. `app/src/main/assets/sample/library.json`：数据文件，格式见下。
2. `app/src/main/assets/sample/posters/`：自制的占位海报（可选，见“海报”一节）。
3. `docs/sample-data.md`：说明这份数据的构成：多少部电影、多少部剧集，状态和评分怎么分布。

## 数据格式

```json
{
  "version": 1,
  "items": [
    {
      "id": "interstellar-2014",
      "title": "Interstellar",
      "originalTitle": "Interstellar",
      "year": 2014,
      "type": "movie",
      "genres": ["Sci-Fi", "Drama"],
      "directors": ["Christopher Nolan"],
      "cast": ["Matthew McConaughey", "Anne Hathaway"],
      "overview": "Write one or two sentences in your own words.",
      "runtimeMinutes": 169,
      "totalEpisodes": null,
      "poster": null,
      "status": "watched",
      "rating": 9.5,
      "tags": ["mind-bending", "rewatched"],
      "records": [
        { "date": "2026-03-14", "episodes": null, "minutes": 169, "rating": 9.0, "note": "First watch. The second half blew me away." },
        { "date": "2026-09-02", "episodes": null, "minutes": 169, "rating": 9.5, "note": "Rewatched it and liked it even more." }
      ]
    }
  ]
}
```

| 字段 | 说明 |
| --- | --- |
| `id` | 英文小写加连字符，不能重复，建议用“原名-年份” |
| `title` / `originalTitle` | 英文标题 / 原名（非英语作品填原文名，英语作品可与 title 相同） |
| `year` | 首映年份 |
| `type` | `"movie"` 电影，或 `"series"` 剧集 |
| `genres` | 1–3 个，只能从这些词里选：Drama, Comedy, Action, Sci-Fi, Animation, Mystery, Crime, Romance, Horror, Documentary, Fantasy, War |
| `directors` / `cast` | 导演 / 主演（主演最多 4 个）；剧集填主创 |
| `overview` | 一两句英文简介，**自己写**，不要复制豆瓣或 TMDB 的原文 |
| `runtimeMinutes` | 电影填片长；剧集填单集平均时长 |
| `totalEpisodes` | 剧集的总集数；电影填 `null` |
| `poster` | 海报文件名，例如 `"interstellar-2014.png"`；没有就填 `null` |
| `status` | `"want"` 想看、`"watching"` 在看、`"watched"` 已看完、`"dropped"` 弃坑 |
| `rating` | 现在的评分，0–10，最多一位小数；没打分填 `null` |
| `tags` | 0–3 个自定义标签 |
| `records` | 观看记录；状态是“想看”的作品填空数组 `[]` |
| `records[].date` | 观看日期，格式 `YYYY-MM-DD` |
| `records[].episodes` | 剧集这次看了哪几集，例如 `[5, 6]`；电影填 `null` |
| `records[].minutes` | 这次看了多少分钟 |
| `records[].rating` | 这次看完的评分，可以是 `null` |
| `records[].note` | 一句观后感，可以是空字符串 `""` |

## 数据要求（验收标准）

1. 一共 30 部左右：约 20 部电影、10 部剧集。
2. 状态分布：想看约 8 部、在看约 5 部、已看完约 14 部、弃坑约 3 部。
3. 观看记录的日期分布在 2025 年 11 月到 2026 年 10 月，每个月都有记录，有几个月明显多一些（这样统计图才好看）。
4. 至少有一段连续 7 天、每天都有观看记录（用来演示“连续观影”）。
5. 评分有高有低：至少 2 部 9.5 分以上，至少 2 部低于 5 分，大部分在 6–9 分之间。
6. 至少 3 部作品有 2 次以上观看记录，而且每次评分不同（演示“重看后改分”）。
7. “在看”的剧集，记录里看过的集数少于 `totalEpisodes`；“已看完”的剧集，所有集都看过。
8. 题材至少覆盖 6 种。
9. 片名、年份、导演、演员、时长、集数都要准确。AI 生成的信息经常出错，**至少抽查 10 部**。
10. JSON 格式正确：在 Android Studio 里打开 `library.json`，右侧没有红色错误标记。

## 海报（可选）

官方海报有版权，介绍视频还会公开，所以**不要用网上下载的海报**。两种做法任选一种：

- 不放海报：`poster` 填 `null`，App 会显示占位图。
- 自己做简单的封面：纯色或渐变背景加片名文字，400×600 像素的 PNG，文件名和 `poster` 字段一致。

## 可以改

- 新建 `app/src/main/assets/sample/` 下的文件
- 新建 `docs/sample-data.md`

## 不要改

- 任何 Java 文件、`res/` 下的资源文件、所有 Gradle 文件

## 自测

1. 在 Android Studio 里打开 `library.json`，确认没有红色错误标记。
2. 让 AI 按“数据要求”逐条核对，并统计：电影和剧集各多少部、每种状态各多少部、每个月有几条记录。把统计结果贴进 PR。
3. 写下你抽查了哪 10 部，以及改正了哪些错误。
4. 编译运行一次 App，确认没有受影响。

## 给 AI 的提示（可以直接复制）

> 请先阅读仓库根目录的 AGENTS.md 和 docs/tasks/D-1-sample-data.md。帮我按任务卡的格式生成 library.json。片名、年份、导演、演员、时长、集数要准确，不确定的请标出来让我核对；简介用你自己的话写；观看记录按“数据要求”分布。不要改任务卡“可以改”以外的文件。
