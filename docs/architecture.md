# 架构草案：数据库与页面划分

> **状态：v1（11 月 5 日按投票结果更新：主题包 A“观影日记”）**
> 本文件定下两件事：本地数据库用什么、App 有哪几个大页面。具体字段和页面细节会随需求调整，但不会推翻这两项决定。

## 1. 数据库：Room

### 决定

| 数据 | 存在哪里 |
| --- | --- |
| 影视条目、观看记录 | **Room**（androidx.room 2.8.x，基于 SQLite） |
| 设置项（是否已载入示例数据等） | `SharedPreferences` |
| 海报图片 | App 私有目录的文件，数据库里只存文件名 |

### 为什么是 Room

- Android 官方 Jetpack 组件，不算“第三方库”，课程里讲得通，资料也最多。
- 本地优先、无需联网，正好对应 MovieTracker 的定位。
- SQL 在编译时检查，写错字段名直接编译失败，比手写 `SQLiteOpenHelper` 少出错。
- DAO 可以直接返回 `LiveData`，数据一改，首页、片库、统计会自动刷新，适合 MVVM。
- 支持 Java（`annotationProcessor`），符合“只用 Java”的规则。

不选的方案：手写 SQLite（样板代码多、容易写错）；Realm / ObjectBox（第三方库，没必要）；DataStore（主要面向 Kotlin 协程，Java 里用起来别扭，设置项用 `SharedPreferences` 就够了）。

**注意：** 固定用 Room **2.x**。Room 3 面向 Kotlin / KSP，不适合本项目。

### 要加的依赖（统筹负责加）

| 库 | 用途 | 版本 |
| --- | --- | --- |
| `androidx.room:room-runtime` + `room-compiler`（annotationProcessor） | 数据库 | 2.8.5 |
| `androidx.lifecycle:lifecycle-viewmodel` / `lifecycle-livedata` | MVVM | 2.11.0 |
| `androidx.fragment:fragment` | 页签页面（李同学的 UI 分支已加） | 1.9.1 |

统计图表**不引入图表库**：评分控件、热力图、环形图都是自绘 View。

### 表结构（v1 草案）

字段参考 D-1 示例数据格式，保证示例数据能直接存进来。

**`media_item`：一部电影或剧集**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | TEXT 主键 | 示例数据用“原名-年份”，手动添加的用 UUID |
| `title` / `original_title` | TEXT | 中文名 / 原名 |
| `year` | INTEGER | 首映年份，可空 |
| `type` | TEXT | `movie` / `series` |
| `genres` / `directors` / `cast` / `tags` | TEXT | 字符串列表，用 TypeConverter 存成一列 |
| `overview` | TEXT | 简介 |
| `runtime_minutes` | INTEGER | 电影片长 / 剧集单集时长 |
| `total_episodes` | INTEGER | 剧集总集数；电影为空 |
| `poster` | TEXT | 海报文件名，可空 |
| `status` | TEXT | `want` / `watching` / `watched` / `dropped` |
| `rating` | REAL | 当前评分 0–10，可空 |
| `source` | TEXT | `manual` / `sample` |
| `tmdb_id` / `imdb_id` | TEXT | 外部编号，可空，预留给以后去重 |
| `created_at` / `updated_at` | INTEGER | 毫秒时间戳 |

**`watch_record`：一次观看记录**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | INTEGER 自增主键 | |
| `media_id` | TEXT | 外键 → `media_item.id`，删除作品时一起删除；建索引 |
| `date` | TEXT | `YYYY-MM-DD`，方便按月份分组统计 |
| `episodes` | TEXT | 剧集本次看了哪几集（整数列表）；电影为空 |
| `minutes` | INTEGER | 本次观看时长 |
| `rating` | REAL | 本次评分，可空 |
| `note` | TEXT | 一句观后感 |
| `created_at` | INTEGER | 毫秒时间戳 |

列表字段先不拆成单独的表：数据量只有几十到几百条，统计题材分布时在 Java 里汇总就够了，表越少越不容易出错。

### 代码位置

```
data/db/
  AppDatabase.java          单例，version = 1，exportSchema = true
  Converters.java           List<String> / List<Integer> ↔ TEXT
  entity/MediaItem.java
  entity/WatchRecord.java
  dao/MediaDao.java         增删改、按状态/类型查、按标题搜索，返回 LiveData
  dao/WatchRecordDao.java   某作品的记录、某时间段的记录、按月汇总
data/repository/
  MediaRepository.java      界面访问作品和记录的唯一入口，写操作放后台线程
  SettingsRepository.java   包装 SharedPreferences
```

规则（和 `AGENTS.md` 一致）：

- `data/db/` 只有统筹能改。需要新字段、新查询，先找统筹。
- Fragment 不直接碰 DAO，一律 ViewModel → Repository。
- 写数据库统一用 Repository 里的后台线程池，不许 `allowMainThreadQueries()`。
- 演示前改表结构时，开发阶段允许清空重建（`fallbackToDestructiveMigration`），因为示例数据可以一键重新载入；正式提交前再评估是否需要写 Migration。

## 2. 页面划分

> 11 月 5 日更新：采用李同学 `UI` 分支里已经做好的四个页签，把“观影计划”换成主题包 A 的“日记”，统计和设置并入“我的”。

### 整体结构

- `MainActivity` 放底部导航栏，4 个页签各是一个 Fragment，手动切换（李同学的 `UI` 分支已实现，不使用 Navigation 组件）。
- 二级页面（作品详情、添加 / 编辑作品、记一笔）用独立的 Activity：别人通过它们的 `newIntent(...)` 静态方法打开，返回键自然回到原页面。这样每个人的页面互不影响，也不需要共同维护导航图。
- 所有 Activity 在 G1（骨架）里一次性注册进 `AndroidManifest.xml`，之后谁都不用再改它。

```
MainActivity（底部导航）
├── 首页   HomeFragment     ──→ 作品详情
├── 片库   LibraryFragment  ──→ 作品详情 ──→ 添加 / 编辑作品
│                                     └──→ 记一笔
├── 日记   DiaryFragment    ──→ 作品详情
└── 我的   ProfileFragment（统计 + 设置）
```

### 四个页签

#### 首页 `ui/home/`

- 继续观看：状态为“在看”的作品，横向滚动卡片
- 最近记录：最近 5 条观看记录
- 本月概览：本月看了几次、共多少小时
- 没有数据时显示空状态，提示去“我的”载入示例数据

#### 片库 `ui/library/`

- 顶部搜索框（按中文名 / 原名）
- 筛选：全部 / 想看 / 在看 / 已看完 / 弃坑
- 列表卡片：海报、标题、“年份 · 类型 · 题材”、评分星级、状态徽章
- 悬浮按钮：添加作品
- 点卡片进入作品详情

#### 日记 `ui/diary/`（主题包 A）

- 上半部分：最近一年的观影热力图，每格一天，颜色越深当天记录越多；点一格，下方时间线跳到那一天
- 下半部分：观影时间线，按日期分组的记录卡片，最新的在最上面

#### 我的 `ui/profile/`

- 统计：总量卡片（作品数、已看完、总时长、平均分）、题材分布环形图
- 设置：载入示例数据、清空所有数据（都要二次确认）、关于
- 演示之后再加：月度观影柱状图

### 二级页面

| 页面 | 包 | 作用 |
| --- | --- | --- |
| 作品详情 | `ui/detail/` | 基本信息、切换状态、评分、观看记录列表、编辑、删除、记一笔入口 |
| 添加 / 编辑作品 | `ui/edit/` | 同一个页面：没传作品 id 是添加，传了是编辑 |
| 记一笔 | `ui/record/` | 日期、时长、当次评分、短评，保存后返回 |

### 通用组件 `ui/components/`

| 组件 | 用在哪里 |
| --- | --- |
| `RatingView`：0–10 分评分控件，5 颗星，支持半星，可设为只读 | 作品详情、记一笔、片库卡片 |
| `HeatmapView`：最近一年的观影热力图 | 日记 |
| `DonutChartView`：环形图 | 我的 |

### 文件命名

| 页面 | 类 | 布局 | ViewModel |
| --- | --- | --- | --- |
| 首页 | `HomeFragment` | `fragment_home.xml` | `HomeViewModel` |
| 片库 | `LibraryFragment` | `fragment_library.xml`、`item_media.xml` | `LibraryViewModel` |
| 日记 | `DiaryFragment` | `fragment_diary.xml`、`item_diary_record.xml` | `DiaryViewModel` |
| 我的 | `ProfileFragment` | `fragment_profile.xml` | `ProfileViewModel` |
| 作品详情 | `MediaDetailActivity` | `activity_media_detail.xml`、`item_watch_record.xml` | `MediaDetailViewModel` |
| 添加 / 编辑作品 | `EditMediaActivity` | `activity_edit_media.xml` | `EditMediaViewModel` |
| 记一笔 | `AddRecordActivity` | `activity_add_record.xml` | `AddRecordViewModel` |

页面读写数据的方法见 [`docs/data-api.md`](data-api.md)。

## 3. 实施顺序与分工

见 [`docs/tasks/README.md`](tasks/README.md) 的第二波交付目标。好友功能和 Jellyfin 导入已在 11 月 2 日决定不做。
