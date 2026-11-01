# J-1：Jellyfin 客户端（数据部分）

| 项目 | 内容 |
| --- | --- |
| 负责 | 郭同学 |
| 分支 | `feature/j-1-jellyfin-client` |
| 截止 | 11 月 6 日 |
| 前置 | 先完成 T0 |
| 参考 | 1.0 版的 `JellyfinClient.java`（向写 1.0 版的同学要）；《MovieTracker 修订建议》修订四 |

## 先说明

是否保留 Jellyfin 导入，11 月 4 日确定（大概率保留）。这张卡只写和界面无关的客户端代码，就算最后不用，这段代码也能独立存在。界面和联调放到第二波。

## 目标

写一个纯 Java 的 Jellyfin 客户端：给定服务器地址、用户名和密码，能登录，并拉取这个用户的电影和剧集列表，包括是否看过、TMDB / IMDb 编号、有没有海报。

只用 Android 自带的 `HttpURLConnection` 和 `org.json`，不引入第三方库。

## 要写的类

全部放在 `hk.hkucs.group_6_android_app_dev.data.jellyfin` 包里。

| 类 | 作用 |
| --- | --- |
| `JellyfinClient` | 对外的入口，方法见下 |
| `JellyfinSession` | 登录结果：服务器地址、服务器 ID、用户 ID、访问令牌 |
| `JellyfinItem` | 一部电影或剧集：Jellyfin ID、类型、标题、原标题、年份、题材、导演、演员、简介、时长（分钟）、是否看完、播放进度百分比、TMDB 编号、IMDb 编号、有没有海报 |
| `JellyfinEpisode` | 一集：ID、季号、集号、标题、时长（分钟）、是否看完 |
| `JellyfinException` | 出错时抛出，带一条给用户看的中文提示 |
| `ServerUrls` | 地址规范化和 http / https 判断。只写静态方法，方便单元测试 |

## JellyfinClient 的方法

```java
public JellyfinClient(String deviceId);  // deviceId 由调用方传入

public JellyfinSession login(String serverUrl, String username, String password) throws JellyfinException;
public List<JellyfinItem> fetchLibrary(JellyfinSession session) throws JellyfinException;
public List<JellyfinEpisode> fetchEpisodes(JellyfinSession session, String seriesId) throws JellyfinException;
public String posterUrl(JellyfinSession session, String itemId, int maxWidth);
```

所有方法都是同步的（会阻塞），**由调用方放到后台线程**。客户端里不要自己开线程。

## 必须满足的规则

1. **地址处理**（写在 `ServerUrls` 里）：
   - 用户没写协议时，自动补上 `https://`
   - 支持带端口的地址，例如 `https://media.example.com:8920`
   - 去掉结尾多余的 `/`
   - `http://` 只允许局域网和本机地址：`localhost`、`127.x.x.x`、`10.x.x.x`、`192.168.x.x`、`172.16.x.x`–`172.31.x.x`。其他 `http://` 地址抛出 `JellyfinException`，提示“公网服务器请使用 https 地址”
2. **登录**：`POST /Users/AuthenticateByName`，请求体包含用户名和密码。请求头使用 Jellyfin 的标准格式：
   `Authorization: MediaBrowser Client="MovieTracker", Device="Android", DeviceId="<deviceId>", Version="1.0", Token="<登录后填入>"`
3. **服务器 ID**：`GET /System/Info/Public` 返回的 `Id` 字段，存进 `JellyfinSession`。
4. **片库**：
   - 接口：较新的服务器用 `GET /Items?userId={userId}`，旧版用 `GET /Users/{userId}/Items`。在 PR 里写明用的是哪个，以及测试服务器的版本
   - 参数必须包括 `Recursive=true`、`IncludeItemTypes=Movie,Series`，以及 `Fields=ProviderIds,Genres,People,Overview,ProductionYear`
   - 分页：每页 `Limit=100`，用 `StartIndex` 一页页翻，直到取完。**不设总数上限**
   - 结果里只保留类型是 Movie 和 Series 的条目
   - 如果服务器对某个 `Fields` 值报错，去掉它，并在 PR 里说明
5. **字段对应**：
   - `UserData.Played` → 是否看完；`UserData.PlayedPercentage` → 播放进度
   - `ProviderIds.Tmdb`、`ProviderIds.Imdb` → TMDB、IMDb 编号；没有就存 `null`
   - `RunTimeTicks / 600000000` → 分钟
   - `People` 里 `Type` 为 `Director` 的是导演，`Actor` 的是演员（演员最多取 5 个）
   - `ImageTags` 里没有 `Primary`，就表示没有海报
6. **剧集分集**：`GET /Shows/{seriesId}/Episodes?userId={userId}`，读取季号（`ParentIndexNumber`）、集号（`IndexNumber`）、标题、时长和是否看完。
7. **出错处理**：网络不通、地址错误、用户名或密码错误（401）、服务器返回异常时，都抛 `JellyfinException`，消息用中文、普通人能看懂，例如“用户名或密码错误”“连接不上服务器，请检查地址”。
8. **不保存密码**；日志里不打印密码和令牌。
9. 代码、测试、PR 截图里**不能出现真实的服务器地址和账号**，示例统一用 `media.example.com`。

## 单元测试（必须）

新建 `app/src/test/java/hk/hkucs/group_6_android_app_dev/data/jellyfin/ServerUrlsTest.java`，用 JUnit 测试 `ServerUrls`，至少覆盖：

| 输入 | 期望 |
| --- | --- |
| `media.example.com` | 变成 `https://media.example.com` |
| `https://media.example.com:8920/` | 变成 `https://media.example.com:8920` |
| `http://192.168.1.10:8096` | 允许 |
| `http://localhost:8096` | 允许 |
| `http://172.20.0.5:8096` | 允许 |
| `http://172.40.0.5:8096` | 拒绝 |
| `http://media.example.com` | 拒绝 |

在 Android Studio 里右键这个测试文件 → **Run 'ServerUrlsTest'**，全部通过后截图。

注意：这类本地单元测试里不能使用 `org.json`（会报 “not mocked” 错误），所以 `ServerUrls` 不要用到它。

## 可以改

- 新建 `data/jellyfin/` 包下的 Java 文件
- 新建 `ServerUrlsTest.java`

## 不要改

- `AndroidManifest.xml`（网络权限在第二波联调时统一加）
- 所有 Gradle 文件、任何资源文件、其他包里的代码

## 验收标准

1. 上面列出的 6 个类都在，`JellyfinClient` 的方法签名和任务卡一致。
2. 满足“必须满足的规则”第 1–9 条。
3. `ServerUrlsTest` 全部通过。
4. App 能正常编译，自动编译显示绿色对勾。

## 自测

1. 运行 `ServerUrlsTest`，截图。
2. 在 PR 里逐条写明“必须满足的规则”第 1–9 条分别在哪个类、哪个方法里实现。
3. 搜索整个改动，确认没有真实的服务器地址、账号和密码。
4. 可选：如果你会用 Docker，可以在自己电脑上起一个测试用的 Jellyfin 试一下。测试代码不要提交。

## 给 AI 的提示（可以直接复制）

> 请先阅读仓库根目录的 AGENTS.md 和 docs/tasks/J-1-jellyfin-client.md。这是一个 Java 11 的 Android 项目，不能用 Kotlin，不能加第三方库。请按任务卡在 data/jellyfin 包里写 Jellyfin 客户端，只用 HttpURLConnection 和 org.json，并写好 ServerUrlsTest 单元测试。不要改任务卡“可以改”以外的文件。每一步告诉我改了哪些文件。
