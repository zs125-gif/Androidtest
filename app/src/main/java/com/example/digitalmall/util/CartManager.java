package com.example.digitalmall.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.digitalmall.db.DBHelper;
import com.example.digitalmall.model.CartItem;
import com.example.digitalmall.model.Product;

import java.util.ArrayList;
import java.util.List;

/**
 * 购物车管理器（单例）。
 * <p>
 * 数据落在 SQLite 的 cart 表里：按店铺隔离（store_id），同一店铺同一商品（product_id）唯一。
 * 这样杀掉进程再进来，购物车还在——这是内存 Map 做不到的，也是课程设计「SQLite 保存购物车」的落点。
 * <p>
 * 表里冗余存了商品名称、价格、图片等快照字段，所以购物车条目的展示不依赖商品接口是否还能拉到。
 */
public class CartManager {

    private static volatile CartManager sInstance;

    private Context appContext;

    private CartManager() {
    }

    public static CartManager getInstance() {
        if (sInstance == null) {
            synchronized (CartManager.class) {
                if (sInstance == null) {
                    sInstance = new CartManager();
                }
            }
        }
        return sInstance;
    }

    /** 在 Application 启动时注入 Context，之后各页面直接 getInstance() 使用 */
    public void init(Context context) {
        this.appContext = context.getApplicationContext();
    }

    private SQLiteDatabase db() {
        if (appContext == null) {
            throw new IllegalStateException("CartManager 未初始化，请在 Application 中调用 init()");
        }
        return DBHelper.getInstance(appContext).getWritableDatabase();
    }

    /** 加入购物车：已存在则数量 +1 */
    public void add(int storeId, Product product) {
        if (product == null) {
            return;
        }
        SQLiteDatabase db = db();
        int current = getCountOf(storeId, product.getId());
        if (current > 0) {
            ContentValues values = new ContentValues();
            values.put("count", current + 1);
            db.update(DBHelper.TABLE_CART, values, "store_id=? AND product_id=?",
                    new String[]{String.valueOf(storeId), String.valueOf(product.getId())});
            return;
        }
        ContentValues values = new ContentValues();
        values.put("store_id", storeId);
        values.put("product_id", product.getId());
        values.put("name", product.getName());
        values.put("subtitle", product.getSubtitle());
        values.put("price", product.getPrice());
        values.put("origin_price", product.getOriginPrice());
        values.put("icon", product.getIcon());
        values.put("cover", product.getCover());
        values.put("stock", product.getStock());
        values.put("sales", product.getSales());
        values.put("count", 1);
        values.put("created", System.currentTimeMillis());
        db.insert(DBHelper.TABLE_CART, null, values);
    }

    /** 数量 -1，减到 0 时移除该条目 */
    public void decrease(int storeId, Product product) {
        if (product == null) {
            return;
        }
        int current = getCountOf(storeId, product.getId());
        if (current <= 0) {
            return;
        }
        if (current == 1) {
            remove(storeId, product.getId());
            return;
        }
        ContentValues values = new ContentValues();
        values.put("count", current - 1);
        db().update(DBHelper.TABLE_CART, values, "store_id=? AND product_id=?",
                new String[]{String.valueOf(storeId), String.valueOf(product.getId())});
    }

    public void remove(int storeId, int productId) {
        db().delete(DBHelper.TABLE_CART, "store_id=? AND product_id=?",
                new String[]{String.valueOf(storeId), String.valueOf(productId)});
    }

    public void clear(int storeId) {
        db().delete(DBHelper.TABLE_CART, "store_id=?", new String[]{String.valueOf(storeId)});
    }

    /** 当前店铺购物车条目（按加入顺序） */
    public List<CartItem> getItems(int storeId) {
        List<CartItem> items = new ArrayList<>();
        Cursor cursor = db().query(DBHelper.TABLE_CART, null, "store_id=?",
                new String[]{String.valueOf(storeId)}, null, null, "_id ASC");
        try {
            while (cursor.moveToNext()) {
                Product product = readProduct(cursor);
                int count = cursor.getInt(cursor.getColumnIndexOrThrow("count"));
                items.add(new CartItem(product, count));
            }
        } finally {
            cursor.close();
        }
        return items;
    }

    /** 从游标里还原一个商品对象（购物车只用来展示与结算，拿不到详情页字段也能用） */
    private Product readProduct(Cursor cursor) {
        Product product = new Product();
        product.setId(cursor.getInt(cursor.getColumnIndexOrThrow("product_id")));
        product.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
        product.setSubtitle(cursor.getString(cursor.getColumnIndexOrThrow("subtitle")));
        product.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow("price")));
        product.setOriginPrice(cursor.getDouble(cursor.getColumnIndexOrThrow("origin_price")));
        product.setIcon(cursor.getString(cursor.getColumnIndexOrThrow("icon")));
        product.setCover(cursor.getString(cursor.getColumnIndexOrThrow("cover")));
        product.setStock(cursor.getInt(cursor.getColumnIndexOrThrow("stock")));
        product.setSales(cursor.getInt(cursor.getColumnIndexOrThrow("sales")));
        return product;
    }

    /** 当前店铺购物车中某商品的数量，没有则返回 0 */
    public int getCountOf(int storeId, int productId) {
        Cursor cursor = db().query(DBHelper.TABLE_CART, new String[]{"count"},
                "store_id=? AND product_id=?",
                new String[]{String.valueOf(storeId), String.valueOf(productId)}, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        } finally {
            cursor.close();
        }
        return 0;
    }

    /** 当前店铺商品总件数 */
    public int getTotalCount(int storeId) {
        Cursor cursor = db().rawQuery("SELECT IFNULL(SUM(count),0) FROM " + DBHelper.TABLE_CART
                + " WHERE store_id=?", new String[]{String.valueOf(storeId)});
        try {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        } finally {
            cursor.close();
        }
        return 0;
    }

    /** 当前店铺合计金额 */
    public double getTotalPrice(int storeId) {
        Cursor cursor = db().rawQuery("SELECT IFNULL(SUM(price*count),0) FROM " + DBHelper.TABLE_CART
                + " WHERE store_id=?", new String[]{String.valueOf(storeId)});
        try {
            if (cursor.moveToFirst()) {
                return cursor.getDouble(0);
            }
        } finally {
            cursor.close();
        }
        return 0;
    }

    public boolean isEmpty(int storeId) {
        return getTotalCount(storeId) == 0;
    }
}
