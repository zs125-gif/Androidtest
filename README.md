# 数码商城 DigitalMall

一个模仿教材《Android 移动应用基础教程（Android Studio 第 3 版）》第 15 章
「综合项目——网上订餐」结构改写的 **Android 电商类 App**，主题换成 **数码商城**。

> 原书项目是「网上订餐」，本项目按同样的模块拆分方式改成了「数码商城」：
> 店铺列表 → 店铺详情（分类 + 商品 + 购物车）→ 商品详情 → 订单确认 → 扫码支付，
> 并补齐课程设计要求的账号体系（登录 / 注册 / 个人中心 / 我的订单）。

## 界面与模块

| # | 界面 | 承载类 | 对应原书章节 |
| --- | --- | --- | --- |
| 1 | 品牌店铺列表（底部导航「首页」） | `MainActivity` + `HomeFragment` | 15.4 店铺界面 |
| 2 | 店铺详情（左分类 + 右商品 + 底部购物车） | `StoreDetailActivity` | 15.5 店铺详情业务 |
| 3 | 商品详情 | `ProductDetailActivity` | 15.6 菜品详情业务 |
| 4 | 订单确认 | `OrderActivity` | 15.7 订单业务 |
| 5 | 扫码支付 | `PayActivity` | 15.7 支付对话框 |
| 6 | 登录 | `LoginActivity` | 课程设计新增 |
| 7 | 注册 | `RegisterActivity` | 课程设计新增 |
| 8 | 个人中心（底部导航「我的」） | `MineFragment` | 课程设计新增 |
| 9 | 我的订单（SQLite 历史订单） | `OrderListActivity` | 课程设计新增 |

## 七项核心技术点对照

| # | 技术要求 | 本项目落地位置 |
| --- | --- | --- |
| 1 | **Activity / Fragment** 组织页面结构 | `MainActivity` 只做导航容器，内容由 `HomeFragment` / `MineFragment` 承载；另有 7 个功能 Activity 各自职责单一 |
| 2 | **ListView / RecyclerView** 展示列表数据 | 首页 `GridView`（店铺）、店铺详情 `ListView`（商品）、我的订单 `ListView`，全部 `BaseAdapter` + ViewHolder 复用 |
| 3 | **Intent** 页面跳转与数据传递 | 店铺 id/名称/主题色/配送费用 `putExtra` 传递；`Product` 实现 `Serializable` 直接传对象；`startActivityForResult` 回传登录结果 |
| 4 | **SharedPreferences** 保存登录状态与用户信息 | `util/SessionManager`：`digitalmall_session` 存储 username / nickname / phone / login_time，退出登录只清这里 |
| 5 | **SQLite** 保存购物车、订单等业务数据 | `db/DBHelper` 建 `cart` / `orders` / `users` 三表；`CartManager` 读写购物车、`OrderDao` 落单与查询、`UserDao` 账号校验 |
| 6 | **JSON 数据解析** 本地或远程数据源 | `HttpUtil`（HttpURLConnection 异步）+ `org.json` 解析 `store_list.json` / `store_detail.json`；服务器不可用时降级读 `res/raw` 同名文件 |
| 7 | **异常处理与加载提示** | 加载中 `ProgressBar`、空列表文案、网络失败自动降级并 Toast 提示、JSON 结构异常兜底为空列表、表单逐项校验提示、清空购物车与退出登录二次确认 |

另外几处评分点也有对应实现：

- **图片加载**：`util/ImageLoader` 内存缓存 + 线程池异步下载；封面优先用本地真实图，`cover` 写成 http 地址时自动走网络
- **动画**：首页数据加载完成后列表淡入 320ms（`ViewPropertyAnimator`）
- **持久化**：购物车与订单落在 SQLite，杀进程重进数据仍在

## 技术栈

- 语言：Java 17
- 编译：`compileSdk 36` / `minSdk 26` / `targetSdk 36`
- 构建：AGP 9.3.0 + Gradle 9.5.0（需 JDK 17+，Android Studio 2026 / Ladybug 及以上）
- 依赖：`appcompat`、`constraintlayout`、`cardview`
- 数据：远程 JSON（`HttpURLConnection`）+ 本地 `res/raw` 兜底 + SQLite + SharedPreferences

## 目录结构

```
DigitalMall/
├── app/src/main/
│   ├── java/com/example/digitalmall/
│   │   ├── MainActivity.java            导航容器（首页 / 我的）
│   │   ├── HomeFragment.java            首页：店铺列表 + JSON 加载
│   │   ├── MineFragment.java            个人中心：登录态 + 订单笔数
│   │   ├── StoreDetailActivity.java     店铺详情
│   │   ├── ProductDetailActivity.java   商品详情
│   │   ├── OrderActivity.java           确认订单
│   │   ├── PayActivity.java             收银台（支付成功写订单）
│   │   ├── LoginActivity.java           登录
│   │   ├── RegisterActivity.java        注册
│   │   ├── OrderListActivity.java       我的订单
│   │   ├── App.java                     Application：初始化购物车 + 预置演示账号
│   │   ├── db/                          DBHelper / UserDao / OrderDao
│   │   ├── adapter/                     Store / Product / Category / Order 适配器
│   │   ├── model/                       Store / Product / Category / CartItem / User / Order
│   │   ├── net/                         HttpUtil / Api / JsonParser
│   │   └── util/                        CartManager / SessionManager / ImageLoader / RawData / UIUtil
│   ├── res/layout/                      各页面布局（8 个 Activity + 2 个 Fragment + 列表项）
│   ├── res/drawable/                    矢量图标与形状
│   ├── res/drawable-nodpi/              真实商品图与店铺封面（11 张 JPEG）
│   └── res/raw/*.json                   离线兜底数据
├── server/                              静态数据服务（Tomcat 或 python）
└── tools/gen_data.py                    演示数据生成脚本
```

## 怎么跑起来

1. **导入工程**：Android Studio 打开 `DigitalMall` 目录，等待 Gradle Sync 完成。
2. **启动数据服务**（可选）：
   ```bash
   cd server
   python serve.py          # 或者把两个 json 丢进 Tomcat 的 webapps/ROOT
   ```
3. **运行 App**：选一个 API 26+ 的模拟器点 Run。
   - 服务器开着 → 数据来自 `http://10.0.2.2:8080/`
   - 服务器没开 → 自动降级读 `res/raw` 里的同名 JSON，并提示「已切换为离线数据」
4. **真机调试**：把 `net/Api.java` 中的 `HOST` 改成电脑的局域网 IP。

### 演示账号

首次启动会自动写入一个演示账号（见 `App.seedDemoUser()`）：

```
账号：demo
密码：123456
```

也可以直接注册新账号，注册成功会自动登录。

## 关于图片

`res/drawable-nodpi/` 下有 11 张实拍图：6 张商品白底图（手机 / 笔记本 / 平板 / 耳机 / 手表 / 充电器）
和 5 张店铺场景封面。数据里用 `"cover": "pic_phone"` 这样的资源名引用，
`UIUtil.picRes()` 负责映射到 `R.drawable.*`。

> 说明：这些图是为课程演示自制的示意图（生成后裁切、去水印、压到 900×340 / 640×640），
> 不含任何真实品牌与商标，也不用于商业用途。

没有配图的商品仍会走 `UIUtil.coverDrawable()` 的「品牌色渐变 + 品类图标」兜底，
`ImageLoader` 也会尝试加载 `cover` 里的 http 地址，两条路都不通才显示渐变底色，不会出现白块。

## 开发过程（每页 / 每个功能一个 PR）

| PR | 内容 |
| --- | --- |
| — | 工程骨架、主题资源、网络与数据基类、演示数据（直接提交到主干） |
| #1 | 首页：品牌店铺列表 |
| #2 | 店铺详情：分类导航 + 商品列表 + 购物车 |
| #3 | 商品详情页 |
| #4 | 订单确认页 |
| #5 | 扫码支付页 |
| #6 | 真实商品图与店铺封面（替换矢量色块） |
| #7 | SQLite 持久化购物车与订单 + 我的订单页 |
| #8 | 登录页与注册页（SharedPreferences + users 表） |
| #9 | 个人中心 + 底部导航（Fragment 组织页面结构） |

仓库地址：https://github.com/zs125-gif/Androidtest
