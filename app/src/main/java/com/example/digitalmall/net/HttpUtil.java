package com.example.digitalmall.net;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 简易网络请求工具类（对应教材 14 章的 HttpURLConnection 异步访问网络）。
 * <p>
 * 请求在子线程执行，结果通过 {@link Callback} 回调；回调发生在子线程，
 * 需要更新 UI 时统一用 {@link #runOnUiThread(Runnable)} 或者调用方自己 post 到主线程的 Handler。
 */
public class HttpUtil {

    private static final String TAG = "HttpUtil";
    private static final int CONNECT_TIMEOUT = 5000;
    private static final int READ_TIMEOUT = 8000;

    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    private HttpUtil() {
    }

    public interface Callback {
        /** 请求成功，body 为服务器返回的完整字符串 */
        void onSuccess(String body);

        /** 请求失败，message 为失败原因 */
        void onFailure(String message);
    }

    public static void get(final String address, final Callback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(address);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(CONNECT_TIMEOUT);
                    connection.setReadTimeout(READ_TIMEOUT);
                    connection.setRequestProperty("Accept", "application/json");

                    int code = connection.getResponseCode();
                    if (code == HttpURLConnection.HTTP_OK) {
                        String body = readStream(connection.getInputStream());
                        postSuccess(callback, body);
                    } else {
                        postFailure(callback, "服务器返回状态码：" + code);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "请求失败: " + address, e);
                    postFailure(callback, e.getClass().getSimpleName() + ": " + e.getMessage());
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        }, "http-worker").start();
    }

    private static String readStream(InputStream inputStream) throws Exception {
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
        reader.close();
        return builder.toString();
    }

    private static void postSuccess(final Callback callback, final String body) {
        MAIN_HANDLER.post(new Runnable() {
            @Override
            public void run() {
                callback.onSuccess(body);
            }
        });
    }

    private static void postFailure(final Callback callback, final String message) {
        MAIN_HANDLER.post(new Runnable() {
            @Override
            public void run() {
                callback.onFailure(message);
            }
        });
    }

    /** 把一段代码切回主线程执行 */
    public static void runOnUiThread(Runnable runnable) {
        MAIN_HANDLER.post(runnable);
    }
}
