# 数码商城 DigitalMall

一个模仿教材《Android 移动应用基础教程（Android Studio 第 3 版）》第 15 章
「综合项目——网上订餐」结构改写的 **Android 电商类 App**，主题换成 **数码商城**。

> 原书项目是「网上订餐」，本项目按同样的模块拆分方式改成了「数码商城」：
> 店铺列表 → 店铺详情（分类 + 商品 + 购物车）→ 商品详情 → 订单确认 → 扫码支付。
> 知识点与原书完全一致：`HttpURLConnection` 异步访问网络、JSON 解析、
> `Handler` 消息通信、`ListView/GridView + BaseAdapter`、`Intent` 传值、购物车逻辑。

## 界面与模块

| # | 界面 | Activity | 对应原书章节 |
| --- | --- | --- | --- |
| 1 | 品牌店铺列表 | `MainActivity` | 15.4 店铺界面 |
| 2 | 店铺详情（左分类 + 右商品 + 底部购物车） | `StoreDetailActivity` | 15.5 店铺详情业务 |
| 3 | 商品详情 | `ProductDetailActivity` | 15.6 菜品详情业务 |
| 4 | 订单确认 | `OrderActivity` | 15.7 订单业务 |
| 5 | 扫码支付 | `PayActivity` | 15.7 支付对话框 |

## 技术栈

- 语言：Java 17
- 编译：`compileSdk 36` / `minSdk 26` / `targetSdk 36`
- 构建：AGP 9.5.0 + Gradle 9.8.2（需 JDK 17+，Android Studio 2024.3 及以上）
- 依赖：`appcompat`、`constraintlayout`、`cardview`
- 数据：本地/远程 JSON + `HttpURLConnection` + `org.json`

## 目录结构

```
DigitalMall/
├── app/src/main/
│   ├── java/com/example/digitalmall/
│   │   ├── MainActivity.java            首页：店铺列表
│   │   ├── adapter/                     BaseAdapter 系列
│   │   ├── model/                       Store / Product / Category / CartItem
│   │   ├── net/                         HttpUtil / Api / JsonParser
│   │   └── util/                        CartManager / ImageLoader / RawData / UIUtil
│   ├── res/layout/                      各页面布局
│   ├── res/drawable/                    纯矢量图标与形状（不依赖位图）
│   └── res/raw/*.json                   离线兜底数据
├── server/                              静态数据服务（Tomcat 或 python）
└── tools/gen_data.py                    演示数据生成脚本
```

## 怎么跑起来

1. **导入工程**：Android Studio 打开 `DigitalMall` 目录，等待 Gradle Sync 完成（首次会下载 Gradle 9.8.2 与依赖）。
2. **启动数据服务**（可选）：
   ```bash
   cd server
   python serve.py          # 或者把两个 json 丢进 Tomcat 的 webapps/ROOT
   ```
3. **运行 App**：选一个 API 26+ 的模拟器点 Run。
   - 服务器开着 → 数据来自 `http://10.0.2.2:8080/`
   - 服务器没开 → 自动降级读 `res/raw` 里的同名 JSON，并提示「已切换为离线数据」
4. **真机调试**：把 `net/Api.java` 中的 `HOST` 改成电脑的局域网 IP。

## 关于图片

为了不往仓库里塞位图，店铺/商品封面统一用「品牌色渐变 + 品类矢量图标」生成占位图
（见 `util/UIUtil.coverDrawable()`）。`ImageLoader` 会尝试加载 JSON 里的 `cover` 字段，
拿到图片就替换占位图，拿不到就保持占位图，不会出现白块。

## 开发过程（每页一个 PR）

本项目按页面拆成多个 PR 迭代开发，分支与 PR 一一对应：

| PR | 分支 | 内容 |
| --- | --- | --- |
| — | `main` | 工程骨架、主题资源、网络与数据基类、演示数据（直接提交到主干） |
| #1 | `feature/p1-store-list` | 首页：品牌店铺列表 |
| #2 | `feature/p2-store-detail` | 店铺详情：分类导航 + 商品列表 + 购物车 |
| #3 | `feature/p3-product-detail` | 商品详情页 |
| #4 | `feature/p4-order` | 订单确认页 |
| #5 | `feature/p5-payment` | 扫码支付页 |

仓库地址：https://github.com/zs125-gif/Androidtest
