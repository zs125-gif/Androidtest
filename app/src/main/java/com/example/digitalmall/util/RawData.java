package com.example.digitalmall.util;

import android.content.Context;

import com.example.digitalmall.R;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * 读取 res/raw 下的本地 JSON。
 * <p>
 * 作用：模拟器连不上电脑上的 Tomcat 时，界面依然能正常展示（离线兜底数据），
 * 保证课堂演示不会因为网络问题卡住。
 */
public class RawData {

    private RawData() {
    }

    public static String storeListJson(Context context) {
        return read(context, R.raw.store_list);
    }

    public static String storeDetailJson(Context context) {
        return read(context, R.raw.store_detail);
    }

    private static String read(Context context, int rawResId) {
        StringBuilder builder = new StringBuilder();
        InputStream inputStream = null;
        BufferedReader reader = null;
        try {
            inputStream = context.getResources().openRawResource(rawResId);
            reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        } catch (Exception e) {
            return "{}";
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Exception ignored) {
            }
        }
        return builder.toString();
    }
}
