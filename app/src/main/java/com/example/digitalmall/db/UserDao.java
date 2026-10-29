package com.example.digitalmall.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;

import com.example.digitalmall.model.User;
import com.example.digitalmall.util.PasswordUtil;

/**
 * users 表的读写（注册、登录校验、按用户名查询）。
 */
public class UserDao {

    private UserDao() {
    }

    /**
     * 注册一个新用户。
     *
     * @return 新用户 id；用户名已存在时返回 -1
     */
    public static long register(Context context, String username, String password,
                                String phone, String nickname) {
        SQLiteDatabase db = DBHelper.getInstance(context).getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password", PasswordUtil.hash(password));
        values.put("phone", phone);
        values.put("nickname", nickname);
        values.put("created", System.currentTimeMillis());
        try {
            return db.insertOrThrow(DBHelper.TABLE_USER, null, values);
        } catch (SQLiteConstraintException e) {
            // username 上有 UNIQUE 约束，重复注册会走到这里
            return -1;
        }
    }

    public static boolean exists(Context context, String username) {
        SQLiteDatabase db = DBHelper.getInstance(context).getReadableDatabase();
        Cursor cursor = db.query(DBHelper.TABLE_USER, new String[]{"_id"},
                "username=?", new String[]{username}, null, null, null);
        try {
            return cursor.moveToFirst();
        } finally {
            cursor.close();
        }
    }

    /**
     * 校验用户名密码。
     *
     * @return 校验通过返回用户对象，否则返回 null
     */
    public static User login(Context context, String username, String password) {
        SQLiteDatabase db = DBHelper.getInstance(context).getReadableDatabase();
        Cursor cursor = db.query(DBHelper.TABLE_USER,
                new String[]{"_id", "username", "password", "phone", "nickname"},
                "username=?", new String[]{username}, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                String saved = cursor.getString(cursor.getColumnIndexOrThrow("password"));
                if (PasswordUtil.matches(password, saved)) {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
                    // 老版本存的明文密码，登录成功后顺手升级成加盐哈希
                    if (PasswordUtil.isLegacy(saved)) {
                        upgradePassword(context, id, password);
                    }
                    User user = new User();
                    user.setId(id);
                    user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow("username")));
                    user.setPhone(cursor.getString(cursor.getColumnIndexOrThrow("phone")));
                    user.setNickname(cursor.getString(cursor.getColumnIndexOrThrow("nickname")));
                    return user;
                }
            }
        } finally {
            cursor.close();
        }
        return null;
    }

    /** 把指定用户的密码改成加盐哈希（用于明文老数据升级） */
    private static void upgradePassword(Context context, int id, String plainPassword) {
        SQLiteDatabase db = DBHelper.getInstance(context).getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("password", PasswordUtil.hash(plainPassword));
        db.update(DBHelper.TABLE_USER, values, "_id=?", new String[]{String.valueOf(id)});
    }
}
