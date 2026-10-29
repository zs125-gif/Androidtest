package com.example.digitalmall.model;

import java.io.Serializable;

/**
 * 搜索结果项：一条商品记录 + 它所属店铺的上下文。
 * <p>
 * 店铺详情页的商品列表只需要 Product，但搜索结果可能来自任意店铺，
 * 点进去时必须知道商品属于哪家店（购物车按店铺隔离、详情页要取店铺主题色），
 * 所以这里把店铺信息一起打包，避免回查。
 */
public class SearchItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private Product product;
    private int storeId;
    private String storeName;
    private String themeColor;
    private String categoryName;

    public SearchItem() {
    }

    public SearchItem(Product product, int storeId, String storeName, String themeColor,
                      String categoryName) {
        this.product = product;
        this.storeId = storeId;
        this.storeName = storeName;
        this.themeColor = themeColor;
        this.categoryName = categoryName;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getStoreId() {
        return storeId;
    }

    public void setStoreId(int storeId) {
        this.storeId = storeId;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getThemeColor() {
        return themeColor;
    }

    public void setThemeColor(String themeColor) {
        this.themeColor = themeColor;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    /**
     * 参与关键词匹配的全部文本（商品名、副标题、描述、参数、店铺名）。
     * 统一转成小写，调用方按 token 逐个 contains 即可实现「空格分词、全部命中」。
     */
    public String searchableText() {
        StringBuilder builder = new StringBuilder();
        if (product != null) {
            builder.append(safe(product.getName())).append(' ')
                    .append(safe(product.getSubtitle())).append(' ')
                    .append(safe(product.getDescription())).append(' ');
            if (product.getParams() != null) {
                for (String param : product.getParams()) {
                    builder.append(param).append(' ');
                }
            }
        }
        if (storeName != null) {
            builder.append(storeName).append(' ');
        }
        if (categoryName != null) {
            builder.append(categoryName);
        }
        return builder.toString().toLowerCase();
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }
}
