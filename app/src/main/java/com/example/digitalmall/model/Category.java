package com.example.digitalmall.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 商品分类，对应店铺详情页左侧的分类列表（手机 / 平板 / 穿戴…）。
 */
public class Category {

    private String name;
    private List<Product> products = new ArrayList<>();

    public Category() {
    }

    public Category(String name, List<Product> products) {
        this.name = name;
        this.products = products;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products == null ? new ArrayList<Product>() : products;
    }

    @Override
    public String toString() {
        return name;
    }
}
