package com.example.digitalmall.model;

/**
 * 购物车条目：一个商品 + 购买数量。
 */
public class CartItem {

    private final Product product;
    private int count;

    public CartItem(Product product, int count) {
        this.product = product;
        this.count = count;
    }

    public Product getProduct() {
        return product;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    /** 该条目的金额合计 */
    public double getSubtotal() {
        return product.getPrice() * count;
    }

    public String getSubtotalText() {
        return String.format("¥%.2f", getSubtotal());
    }
}
