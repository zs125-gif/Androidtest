package com.example.digitalmall.util;

import com.example.digitalmall.model.CartItem;
import com.example.digitalmall.model.Product;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 购物车管理器（单例）。
 * <p>
 * 购物车按店铺隔离：每个店铺 id 对应一个「商品 id -> CartItem」的有序 Map，
 * 这样在不同店铺之间切换时不会互相污染，也方便结算时只结算当前店铺的商品。
 */
public class CartManager {

    private static volatile CartManager sInstance;

    private final Map<Integer, LinkedHashMap<Integer, CartItem>> carts = new HashMap<>();

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

    private LinkedHashMap<Integer, CartItem> cartOf(int storeId) {
        LinkedHashMap<Integer, CartItem> cart = carts.get(storeId);
        if (cart == null) {
            cart = new LinkedHashMap<>();
            carts.put(storeId, cart);
        }
        return cart;
    }

    /** 加入购物车：已存在则数量 +1 */
    public void add(int storeId, Product product) {
        if (product == null) {
            return;
        }
        LinkedHashMap<Integer, CartItem> cart = cartOf(storeId);
        CartItem item = cart.get(product.getId());
        if (item == null) {
            cart.put(product.getId(), new CartItem(product, 1));
        } else {
            item.setCount(item.getCount() + 1);
        }
    }

    /** 数量 -1，减到 0 时移除该条目 */
    public void decrease(int storeId, Product product) {
        LinkedHashMap<Integer, CartItem> cart = cartOf(storeId);
        CartItem item = cart.get(product.getId());
        if (item == null) {
            return;
        }
        if (item.getCount() <= 1) {
            cart.remove(product.getId());
        } else {
            item.setCount(item.getCount() - 1);
        }
    }

    public void remove(int storeId, int productId) {
        cartOf(storeId).remove(productId);
    }

    public void clear(int storeId) {
        cartOf(storeId).clear();
    }

    /** 当前店铺购物车条目（按加入顺序） */
    public List<CartItem> getItems(int storeId) {
        return new ArrayList<>(cartOf(storeId).values());
    }

    /** 当前店铺购物车中某商品的数量，没有则返回 0 */
    public int getCountOf(int storeId, int productId) {
        CartItem item = cartOf(storeId).get(productId);
        return item == null ? 0 : item.getCount();
    }

    /** 当前店铺商品总件数 */
    public int getTotalCount(int storeId) {
        int total = 0;
        for (CartItem item : cartOf(storeId).values()) {
            total += item.getCount();
        }
        return total;
    }

    /** 当前店铺合计金额 */
    public double getTotalPrice(int storeId) {
        double total = 0;
        for (CartItem item : cartOf(storeId).values()) {
            total += item.getSubtotal();
        }
        return total;
    }

    public boolean isEmpty(int storeId) {
        return cartOf(storeId).isEmpty();
    }
}
