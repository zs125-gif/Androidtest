package com.example.digitalmall.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.digitalmall.model.User;

/**
 * 登录态管理（课程设计第 4 项技术要求：SharedPreferences 保存登录状态与用户信息）。
 * <p>
 * 只把「昵称、账号、手机号」这类展示信息存 SharedPreferences；
 * 密码不落盘，校验一律走 users 表（SQLite）。
 */
public class SessionManager {

    private static final String PREF_NAME = "digitalmall_session";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_NICKNAME = "nickname";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_LOGIN_TIME = "login_time";

    private SessionManager() {
    }

    private static SharedPreferences pref(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /** 登录成功后写入登录态 */
    public static void save(Context context, User user) {
        pref(context).edit()
                .putString(KEY_USERNAME, user.getUsername())
                .putString(KEY_NICKNAME, user.getNickname())
                .putString(KEY_PHONE, user.getPhone())
                .putLong(KEY_LOGIN_TIME, System.currentTimeMillis())
                .apply();
    }

    public static boolean isLogin(Context context) {
        return pref(context).getString(KEY_USERNAME, null) != null;
    }

    public static String getUsername(Context context) {
        return pref(context).getString(KEY_USERNAME, null);
    }

    public static String getNickname(Context context) {
        String nickname = pref(context).getString(KEY_NICKNAME, null);
        if (nickname == null || nickname.isEmpty()) {
            return getUsername(context);
        }
        return nickname;
    }

    public static String getPhone(Context context) {
        return pref(context).getString(KEY_PHONE, "");
    }

    public static long getLoginTime(Context context) {
        return pref(context).getLong(KEY_LOGIN_TIME, 0L);
    }

    /** 组装成 User 对象，方便个人中心统一展示 */
    public static User currentUser(Context context) {
        if (!isLogin(context)) {
            return null;
        }
        return new User(getUsername(context), getNickname(context), getPhone(context));
    }

    /** 退出登录：只清登录态，SQLite 里的订单和购物车保留 */
    public static void logout(Context context) {
        pref(context).edit().clear().apply();
    }
}
