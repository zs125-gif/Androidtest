package com.example.digitalmall.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

/**
 * 搜索历史仓库（SharedPreferences）。
 * <p>
 * 只存关键词字符串，最新的排在最前面，最多保留 {@link #MAX_SIZE} 条，
 * 重复搜索同一个词时把它提到最前而不是存两份。
 */
public class SearchHistory {

    private static final String SP_NAME = "search_history";
    private static final String KEY_WORDS = "keywords";
    private static final String SEPARATOR = "\n";
    private static final int MAX_SIZE = 8;

    private SearchHistory() {
    }

    public static List<String> get(Context context) {
        List<String> result = new ArrayList<>();
        String raw = prefs(context).getString(KEY_WORDS, "");
        if (raw == null) {
            return result;
        }
        for (String item : raw.split(SEPARATOR)) {
            String word = item.trim();
            if (!word.isEmpty() && !result.contains(word)) {
                result.add(word);
            }
        }
        return result;
    }

    public static void add(Context context, String keyword) {
        String word = keyword == null ? "" : keyword.trim();
        if (word.isEmpty()) {
            return;
        }
        List<String> list = get(context);
        list.remove(word);
        list.add(0, word);
        while (list.size() > MAX_SIZE) {
            list.remove(list.size() - 1);
        }
        prefs(context).edit().putString(KEY_WORDS, join(list)).apply();
    }

    public static void clear(Context context) {
        prefs(context).edit().remove(KEY_WORDS).apply();
    }

    private static String join(List<String> list) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                builder.append(SEPARATOR);
            }
            builder.append(list.get(i));
        }
        return builder.toString();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
    }
}
