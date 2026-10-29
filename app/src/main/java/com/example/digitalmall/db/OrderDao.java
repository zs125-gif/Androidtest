package com.example.digitalmall.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.digitalmall.model.Order;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * orders 表的读写：支付成功后落单、个人中心查「我的订单」。
 */
public class OrderDao {

    private OrderDao() {
    }

    /** 生成订单号：DM + 时间戳 + 3 位随机数 */
    private static String buildOrderNo() {
        String time = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA).format(new Date());
        int tail = (int) (Math.random() * 900) + 100;
        return "DM" + time + tail;
    }

    /**
     * 支付成功后写入一条订单。
     *
     * @return 订单号，写入失败返回 null
     */
    public static String insert(Context context, String username, int storeId, String storeName,
                                double total, int itemCount, String summary) {
        SQLiteDatabase db = DBHelper.getInstance(context).getWritableDatabase();
        String orderNo = buildOrderNo();
        ContentValues values = new ContentValues();
        values.put("order_no", orderNo);
        values.put("username", username);
        values.put("store_id", storeId);
        values.put("store_name", storeName);
        values.put("total", total);
        values.put("item_count", itemCount);
        values.put("summary", summary);
        values.put("status", 1);
        values.put("created", System.currentTimeMillis());
        long rowId = db.insert(DBHelper.TABLE_ORDER, null, values);
        return rowId == -1 ? null : orderNo;
    }

    /** 某个用户的历史订单，按时间倒序 */
    public static List<Order> listByUser(Context context, String username) {
        List<Order> list = new ArrayList<>();
        SQLiteDatabase db = DBHelper.getInstance(context).getReadableDatabase();
        String selection = username == null ? null : "username=?";
        String[] args = username == null ? null : new String[]{username};
        Cursor cursor = db.query(DBHelper.TABLE_ORDER, null, selection, args,
                null, null, "_id DESC");
        try {
            while (cursor.moveToNext()) {
                Order order = new Order();
                order.setId(cursor.getInt(cursor.getColumnIndexOrThrow("_id")));
                order.setOrderNo(cursor.getString(cursor.getColumnIndexOrThrow("order_no")));
                order.setUsername(cursor.getString(cursor.getColumnIndexOrThrow("username")));
                order.setStoreId(cursor.getInt(cursor.getColumnIndexOrThrow("store_id")));
                order.setStoreName(cursor.getString(cursor.getColumnIndexOrThrow("store_name")));
                order.setTotal(cursor.getDouble(cursor.getColumnIndexOrThrow("total")));
                order.setItemCount(cursor.getInt(cursor.getColumnIndexOrThrow("item_count")));
                order.setSummary(cursor.getString(cursor.getColumnIndexOrThrow("summary")));
                order.setStatus(cursor.getInt(cursor.getColumnIndexOrThrow("status")));
                order.setCreated(cursor.getLong(cursor.getColumnIndexOrThrow("created")));
                list.add(order);
            }
        } finally {
            cursor.close();
        }
        return list;
    }

    public static int countByUser(Context context, String username) {
        SQLiteDatabase db = DBHelper.getInstance(context).getReadableDatabase();
        String selection = username == null ? null : "username=?";
        String[] args = username == null ? null : new String[]{username};
        Cursor cursor = db.query(DBHelper.TABLE_ORDER, new String[]{"COUNT(*)"},
                selection, args, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        } finally {
            cursor.close();
        }
        return 0;
    }
}
