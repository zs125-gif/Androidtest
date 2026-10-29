# 静态数据服务

App 通过 HTTP 拉取 JSON 数据，模拟教材里「Tomcat 提供数据」的做法。
两种启动方式任选一种，端口固定 **8080**，因为客户端写死了 `http://10.0.2.2:8080/`。

## 方式一：Python 一键启动（推荐，无需装 Tomcat）

```bash
cd server
python serve.py
```

## 方式二：Tomcat

1. 安装 Tomcat 10，把 `store_list.json`、`store_detail.json` 复制到
   `TOMCAT_HOME/webapps/ROOT/` 目录下；
2. 启动 Tomcat；
3. 浏览器访问 <http://localhost:8080/store_list.json> 能返回 JSON 即成功。

## 接口说明

| 文件 | 说明 |
| --- | --- |
| `store_list.json` | 首页店铺列表：店名、月销量、评分、配送费/时长、公告、优惠、主题色 |
| `store_detail.json` | 所有店铺的分类与商品：分类名 + 商品（名称、副标题、价格、原价、销量、库存、参数、图文描述） |

返回结构示例：

```json
{
  "code": 200,
  "storeList": [
    { "id": 1, "name": "星辰数码旗舰店", "monthSales": 3286, "rating": 4.9, ... }
  ]
}
```

> 数据由 `tools/gen_data.py` 生成，同时输出到 `server/` 和 `app/src/main/res/raw/`。
> 修改数据请改脚本后重新执行，避免两份文件不一致：

```bash
python tools/gen_data.py
```

## 关于 10.0.2.2

`10.0.2.2` 是 Android 模拟器访问宿主机 `localhost` 的固定地址。
真机调试时，把 `net/Api.java` 里的 `HOST` 改成电脑的局域网 IP（如 `192.168.1.10`），
并确保手机和电脑在同一个 Wi-Fi 下。

## 没有服务器也能跑

App 内置了离线兜底：网络请求失败时会自动读取 `res/raw` 下的同名 JSON，
并弹一个 Toast 提示「已切换为离线数据」，界面依然完整可用。
