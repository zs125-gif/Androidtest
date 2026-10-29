package com.example.digitalmall.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * SQLite 数据库帮助类（课程设计第 5 项技术要求：用 SQLite 保存购物车、订单等业务数据）。
 * <p>
 * 三张表：
 * <ul>
 *     <li>cart —— 购物车，按 (store_id, product_id) 唯一，冗余存商品快照，避免商品下架后购物车失效；</li>
 *     <li>orders —— 订单，支付成功后落库，供「我的订单」查询；</li>
 *     <li>users —— 注册用户，登录时校验。</li>
 * </ul>
 * 采用单例 + applicationContext，避免多处 new 造成连接泄漏。
 */
public class DBHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "digitalmall.db";
    public static final int DB_VERSION = 1;

    public static final String TABLE_CART = "cart";
    public static final String TABLE_ORDER = "orders";
    public static final String TABLE_USER = "users";

    private static volatile DBHelper sInstance;

    private DBHelper(Context context) {
        super(context.getApplicationContext(), DB_NAME, null, DB_VERSION);
    }

    public static DBHelper getInstance(Context context) {
        if (sInstance == null) {
            synchronized (DBHelper.class) {
                if (sInstance == null) {
                    sInstance = new DBHelper(context);
                }
            }
        }
        return sInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_CART + " ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "store_id INTEGER NOT NULL,"
                + "product_id INTEGER NOT NULL,"
                + "name TEXT,"
                + "subtitle TEXT,"
                + "price REAL,"
                + "origin_price REAL,"
                + "icon TEXT,"
                + "cover TEXT,"
                + "stock INTEGER,"
                + "sales INTEGER,"
                + "count INTEGER NOT NULL DEFAULT 1,"
                + "created INTEGER"
                + ")");
        // 同一店铺同一商品只保留一行，加购时做 +1
        db.execSQL("CREATE UNIQUE INDEX uk_cart ON " + TABLE_CART + "(store_id, product_id)");

        db.execSQL("CREATE TABLE " + TABLE_ORDER + " ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "order_no TEXT NOT NULL,"
                + "username TEXT,"
                + "store_id INTEGER,"
                + "store_name TEXT,"
                + "total REAL,"
                + "item_count INTEGER,"
                + "summary TEXT,"
                + "status INTEGER NOT NULL DEFAULT 1,"
                + "created INTEGER"
                + ")");

        db.execSQL("CREATE TABLE " + TABLE_USER + " ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "username TEXT NOT NULL UNIQUE,"
                + "password TEXT NOT NULL,"
                + "phone TEXT,"
                + "nickname TEXT,"
                + "created INTEGER"
                + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 课程设计阶段直接重建，正式项目应做数据迁移
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CART);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        onCreate(db);
    }
}
