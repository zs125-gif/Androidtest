package com.example.digitalmall.net;

/**
 * 服务器接口地址集中管理。
 * <p>
 * 10.0.2.2 是 Android 模拟器访问「宿主机 localhost」的固定 IP，
 * 所以手机端不用改代码，只要把 Tomcat（或其他静态服务器）跑在电脑 8080 端口即可。
 * 真机调试时把 HOST 换成电脑的局域网 IP，例如 192.168.1.10。
 */
public class Api {

    public static final String HOST = "10.0.2.2";
    public static final int PORT = 8080;
    public static final String BASE_URL = "http://" + HOST + ":" + PORT + "/";

    /** 店铺列表数据 */
    public static final String PATH_STORE_LIST = "store_list.json";
    /** 店铺详情（分类 + 商品）数据 */
    public static final String PATH_STORE_DETAIL = "store_detail.json";

    private Api() {
    }

    /** 拉取首页店铺列表 */
    public static void getStoreList(HttpUtil.Callback callback) {
        HttpUtil.get(BASE_URL + PATH_STORE_LIST, callback);
    }

    /** 拉取所有店铺的分类与商品数据（数据量小，一次拉全，按店铺 id 过滤） */
    public static void getStoreDetail(HttpUtil.Callback callback) {
        HttpUtil.get(BASE_URL + PATH_STORE_DETAIL, callback);
    }
}
