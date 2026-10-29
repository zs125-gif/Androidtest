package com.example.digitalmall.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 商品实体类，对应 store_detail.json 中 products 数组的一个元素。
 */
public class Product {

    private int id;
    private String name;
    private String subtitle;
    private double price;
    private double originPrice;
    private int sales;
    private int stock;
    private String icon;
    private String cover;
    private String description;
    private List<String> params = new ArrayList<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getOriginPrice() {
        return originPrice;
    }

    public void setOriginPrice(double originPrice) {
        this.originPrice = originPrice;
    }

    public int getSales() {
        return sales;
    }

    public void setSales(int sales) {
        this.sales = sales;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getParams() {
        return params;
    }

    public void setParams(List<String> params) {
        this.params = params == null ? new ArrayList<String>() : params;
    }

    /** 价格文案，保留两位小数，如「¥4999.00」 */
    public String getPriceText() {
        return String.format("¥%.2f", price);
    }

    public String getSalesText() {
        return "月销 " + sales;
    }
}
