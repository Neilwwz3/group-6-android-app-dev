# 数据接口约定

页面怎么读写数据，以本文件为准。由统筹在 ARCH-1（数据层）里实现，实现前后方法名不变。

**规则：** 页面只能通过自己的 ViewModel 调用 `MediaRepository`，不要直接使用 DAO 或数据库。缺数据、缺方法时，在群里找统筹，不要自己加。

## 拿到 Repository

ViewModel 继承 `AndroidViewModel`，在构造函数里：

```java
MediaRepository repo = MediaRepository.get(application);
```

- 名字以 `observe` 开头的方法返回 `LiveData`：数据一变，界面自动刷新。
- 写操作在后台线程执行，调用后立即返回；需要知道何时完成的，传回调，回调在主线程执行，可以直接更新界面。

## 常量

| 类 | 值 | 界面上显示 |
| --- | --- | --- |
| `MediaStatus.WANT` | `"want"` | 想看 |
| `MediaStatus.WATCHING` | `"watching"` | 在看 |
| `MediaStatus.WATCHED` | `"watched"` | 已看完 |
| `MediaStatus.DROPPED` | `"dropped"` | 弃坑 |
| `MediaType.MOVIE` | `"movie"` | 电影 |
| `MediaType.SERIES` | `"series"` | 剧集 |
| `Genres.ALL` | 剧情、喜剧、动作、科幻、动画、悬疑、犯罪、爱情、恐怖、纪录、奇幻、战争 | 题材只能从这里选 |

显示用的中文写在 `strings.xml`（`status_want` 等），由 ARCH-1 提供。

## 数据类

**`MediaItem`：一部电影或剧集**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `String` | 新建时留空，保存时自动生成 |
| `title` / `originalTitle` | `String` | 中文名必填 / 原名可空 |
| `year` | `Integer` | 可空 |
| `type` | `String` | `MediaType` 之一 |
| `genres` / `directors` / `cast` / `tags` | `List<String>` | 不会是 null，没有就是空列表 |
| `overview` | `String` | 简介 |
| `runtimeMinutes` | `Integer` | 电影片长 / 剧集单集时长，可空 |
| `totalEpisodes` | `Integer` | 剧集总集数；电影为 null |
| `poster` | `String` | 海报文件名，可空；为空时界面显示占位图 |
| `status` | `String` | `MediaStatus` 之一 |
| `rating` | `Float` | 0–10，步长 1（即半星）；null 表示未评分 |

**`WatchRecord`：一次观看**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `long` | 自动生成 |
| `mediaId` | `String` | 属于哪部作品 |
| `date` | `String` | `YYYY-MM-DD` |
| `episodes` | `List<Integer>` | 剧集这次看了哪几集；电影为空列表 |
| `minutes` | `Integer` | 这次看了多久，可空 |
| `rating` | `Float` | 这次的评分，可空 |
| `note` | `String` | 一句观后感，可为空字符串 |

**`RecordWithMedia`：** 一条记录加上它所属作品的标题、海报和类型。字段：`record`（`WatchRecord`）、`mediaTitle`、`mediaPoster`、`mediaType`。

**`StatsSummary`：** `mediaCount`（作品数）、`watchedCount`（已看完数）、`totalMinutes`（总时长，分钟）、`averageRating`（平均分，`Float`，可空）、`recordCount`（记录数）。

**`GenreCount`：** `genre`、`count`。

## MediaRepository 的方法

### 作品

| 方法 | 返回 | 谁用 |
| --- | --- | --- |
| `observeAllMedia()` | `LiveData<List<MediaItem>>`，最近更新的在前 | 片库（搜索、筛选、排序在 ViewModel 里做） |
| `observeMedia(String id)` | `LiveData<MediaItem>`；作品被删除后变成 null | 作品详情、编辑作品、记一笔 |
| `observeMediaByStatus(String status)` | `LiveData<List<MediaItem>>` | 首页“继续观看” |
| `saveMedia(MediaItem item, Consumer<String> onSaved)` | 新建或更新；回调给出作品 id，可传 null | 添加 / 编辑作品 |
| `updateStatus(String id, String status)` | — | 作品详情 |
| `updateRating(String id, Float rating)` | —；rating 可为 null | 作品详情 |
| `deleteMedia(String id, Runnable onDone)` | 连同它的观看记录一起删除 | 作品详情 |

### 观看记录

| 方法 | 返回 | 谁用 |
| --- | --- | --- |
| `observeRecords(String mediaId)` | `LiveData<List<WatchRecord>>`，最新的在前 | 作品详情 |
| `observeRecentRecords(int limit)` | `LiveData<List<RecordWithMedia>>` | 首页“最近记录” |
| `observeTimeline()` | `LiveData<List<RecordWithMedia>>`，全部记录，最新的在前 | 日记时间线 |
| `observeDailyCounts(String fromDate, String toDate)` | `LiveData<Map<String, Integer>>`：日期 → 当天记录数，没有记录的日期不出现 | 日记热力图 |
| `addRecord(WatchRecord record, Runnable onDone)` | 保存记录。如果作品原来是“想看”：电影自动改为“已看完”，剧集自动改为“在看” | 记一笔 |

### 统计

| 方法 | 返回 | 谁用 |
| --- | --- | --- |
| `observeSummary()` | `LiveData<StatsSummary>`，全部数据 | 我的 |
| `observeMonthSummary(String yearMonth)` | `LiveData<StatsSummary>`，例如 `"2026-11"`，只算这个月的记录 | 首页“本月概览” |
| `observeGenreCounts()` | `LiveData<List<GenreCount>>`，数量多的在前 | 我的·题材环形图 |

### 示例数据

| 方法 | 作用 |
| --- | --- |
| `SampleDataLoader.load(Context context, Runnable onDone)` | 清空现有数据，再载入 `assets/sample/library.json` |
| `MediaRepository.get(context).clearAll(Runnable onDone)` | 清空所有作品和记录 |
