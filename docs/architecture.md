# 架构草案：数据库与页面划分

> **状态：草案 v0（统筹起草，11 月 4 日核心需求确定后修订）**
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
| `androidx.navigation:navigation-fragment` / `navigation-ui` | 底部导航和页面跳转 | 2.10.2 |

统计图表**不引入图表库**，用一个自绘的简单柱状图 View 实现。

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

### 整体结构

- **一个 Activity + 多个 Fragment**：`MainActivity` 只放底部导航栏和 `NavHostFragment`。
- 底部导航 4 个页签，用 Navigation 组件的 `nav_graph.xml` 管理跳转。
- 二级页面（作品详情、编辑作品、记一笔）也是 Fragment，从页签页跳过去，返回键回到原页签。

```
MainActivity
└── BottomNavigationView
    ├── 首页      HomeFragment
    ├── 媒体库    LibraryFragment ──→ 作品详情 ──→ 编辑作品
    ├── 统计      StatsFragment
    └── 设置      SettingsFragment
（任意页面）──→ 记一笔 AddRecordFragment
```

### 四个大页面

#### 首页 `ui/home/`

打开 App 第一眼看到的页面，回答“我最近在看什么”。

- 继续观看：状态为“在看”的作品，横向滚动卡片，剧集显示“看到第几集 / 共几集”
- 最近记录：最近 5 条观看记录
- 本月概览：本月看了几部、共多少小时、当前连续观影天数
- 想看清单：随机或最新的几部“想看”
- 悬浮按钮：记一笔
- 没有数据时显示空状态，引导去设置页“载入示例数据”或去媒体库添加

#### 媒体库 `ui/library/`

所有作品的完整列表，是数据最多的页面。

- 顶部搜索框（按中文名 / 原名）
- 筛选标签：全部 / 想看 / 在看 / 已看完 / 弃坑；电影 / 剧集
- 排序：最近更新 / 评分 / 年份
- 列表卡片：海报、标题、“年份 · 类型 · 题材”、评分星级、状态徽章（用 UI-1 的组件）
- 悬浮按钮：添加作品
- 点卡片进入**作品详情**：基本信息、修改状态和评分、观看记录列表、编辑 / 删除

#### 统计 `ui/stats/`

只读页面，全部数据来自 `watch_record` 和 `media_item`。

- 总览数字：总作品数、已看完数、总观看时长
- 月度柱状图：最近 12 个月每月观看次数或时长
- 题材分布：各题材数量占比
- 评分分布：0–10 分各区间的作品数
- 连续观影：最长连续天数、当前连续天数

#### 设置 `ui/settings/`

普通布局实现，不引入 Preference 库。

- 数据：载入示例数据、清空所有数据（二次确认）
- 导出 / 导入 JSON 备份（可选，看进度）
- 关于：版本号、小组成员

### 文件命名

| 页面 | Fragment | 布局 | ViewModel |
| --- | --- | --- | --- |
| 首页 | `HomeFragment` | `fragment_home.xml` | `HomeViewModel` |
| 媒体库 | `LibraryFragment` | `fragment_library.xml`、`item_media.xml` | `LibraryViewModel` |
| 作品详情 | `MediaDetailFragment` | `fragment_media_detail.xml`、`item_watch_record.xml` | `MediaDetailViewModel` |
| 编辑作品 | `EditMediaFragment` | `fragment_edit_media.xml` | `EditMediaViewModel` |
| 记一笔 | `AddRecordFragment` | `fragment_add_record.xml` | `AddRecordViewModel` |
| 统计 | `StatsFragment` | `fragment_stats.xml` | `StatsViewModel` |
| 设置 | `SettingsFragment` | `fragment_settings.xml` | `SettingsViewModel` |

## 3. 实施顺序与分工（建议，待 11 月 7 日会议确认）

| 编号 | 内容 | 负责 | 前置 |
| --- | --- | --- | --- |
| ARCH-1 | 加依赖；建好 Room 数据库、Entity、DAO、Repository | 统筹 | 无 |
| ARCH-2 | `MainActivity` + 底部导航 + `nav_graph.xml` + 4 个空白 Fragment | 统筹 | ARCH-1 |
| D-2 | 把 D-1 的 `library.json` 载入数据库（设置页按钮调用） | 杨同学 | D-1、ARCH-1 |
| P-1 | 首页 | 李同学 | UI-1、ARCH-2 |
| P-2 | 媒体库 + 作品详情 + 编辑作品 | 杨同学 | ARCH-2、D-2 |
| P-3 | 统计页 + 自绘柱状图 | 待定 | ARCH-2、D-2 |
| P-4 | 设置页（载入示例数据、清空数据、关于） | 待定 | ARCH-2、D-2 |
| P-5 | 记一笔 | 待定 | ARCH-2 |

好友功能和 Jellyfin 导入已在 11 月 2 日决定不做。

ARCH-1 和 ARCH-2 先合并进 `dev`，其他人在这个骨架上各自只改自己的 `ui/<功能名>/` 包和对应布局，互不冲突。
