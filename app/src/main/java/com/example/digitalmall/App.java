package com.example.digitalmall;

import android.app.Application;

import com.example.digitalmall.db.UserDao;
import com.example.digitalmall.util.CartManager;

/**
 * 全局入口：
 * 1) 把 Context 交给购物车管理器（购物车数据落在 SQLite）；
 * 2) 首次启动时预置一个演示账号，方便直接体验登录流程。
 */
public class App extends Application {

    private static final String DEMO_USER = "demo";
    private static final String DEMO_PASSWORD = "123456";
    private static final String DEMO_PHONE = "13800008888";
    private static final String DEMO_NICKNAME = "数码达人";

    @Override
    public void onCreate() {
        super.onCreate();
        CartManager.getInstance().init(this);
        seedDemoUser();
    }

    /** 首次安装时写入演示账号：demo / 123456 */
    private void seedDemoUser() {
        try {
            if (!UserDao.exists(this, DEMO_USER)) {
                UserDao.register(this, DEMO_USER, DEMO_PASSWORD, DEMO_PHONE, DEMO_NICKNAME);
            }
        } catch (Exception ignored) {
            // 预置失败不影响正常使用，用户仍可自行注册
        }
    }
}
