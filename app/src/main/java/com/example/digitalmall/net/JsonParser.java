package com.example.digitalmall.net;

import com.example.digitalmall.model.Category;
import com.example.digitalmall.model.Product;
import com.example.digitalmall.model.Store;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * JSON 解析工具：把服务器返回的字符串转成实体对象（对应教材 13 章的 JSON 解析）。
 */
public class JsonParser {

    private JsonParser() {
    }

    /** 解析 store_list.json */
    public static List<Store> parseStores(String json) throws JSONException {
        List<Store> stores = new ArrayList<>();
        JSONObject root = new JSONObject(json);
        JSONArray array = root.optJSONArray("storeList");
        if (array == null) {
            return stores;
        }
        for (int i = 0; i < array.length(); i++) {
            JSONObject item = array.optJSONObject(i);
            if (item == null) {
                continue;
            }
            Store store = new Store();
            store.setId(item.optInt("id"));
            store.setName(item.optString("name"));
            store.setSlogan(item.optString("slogan"));
            store.setMonthSales(item.optInt("monthSales"));
            store.setRating(item.optDouble("rating", 5.0));
            store.setDeliveryFee(item.optDouble("deliveryFee", 0));
            store.setDeliveryTime(item.optString("deliveryTime"));
            store.setNotice(item.optString("notice"));
            store.setPromotion(item.optString("promotion"));
            store.setCover(item.optString("cover"));
            store.setIcon(item.optString("icon"));
            store.setThemeColor(item.optString("themeColor", "#1B6BFF"));
            stores.add(store);
        }
        return stores;
    }

    /** 解析 store_detail.json，取出指定店铺的分类列表 */
    public static List<Category> parseCategories(String json, int storeId) throws JSONException {
        List<Category> categories = new ArrayList<>();
        JSONObject root = new JSONObject(json);
        JSONArray storeArray = root.optJSONArray("storeList");
        if (storeArray == null) {
            return categories;
        }
        for (int i = 0; i < storeArray.length(); i++) {
            JSONObject storeObj = storeArray.optJSONObject(i);
            if (storeObj == null || storeObj.optInt("id") != storeId) {
                continue;
            }
            JSONArray categoryArray = storeObj.optJSONArray("categories");
            if (categoryArray == null) {
                break;
            }
            for (int j = 0; j < categoryArray.length(); j++) {
                JSONObject categoryObj = categoryArray.optJSONObject(j);
                if (categoryObj == null) {
                    continue;
                }
                Category category = new Category();
                category.setName(categoryObj.optString("name"));
                category.setProducts(parseProducts(categoryObj.optJSONArray("products")));
                categories.add(category);
            }
            break;
        }
        return categories;
    }

    private static List<Product> parseProducts(JSONArray array) {
        List<Product> products = new ArrayList<>();
        if (array == null) {
            return products;
        }
        for (int i = 0; i < array.length(); i++) {
            JSONObject item = array.optJSONObject(i);
            if (item == null) {
                continue;
            }
            Product product = new Product();
            product.setId(item.optInt("id"));
            product.setName(item.optString("name"));
            product.setSubtitle(item.optString("subtitle"));
            product.setPrice(item.optDouble("price", 0));
            product.setOriginPrice(item.optDouble("originPrice", 0));
            product.setSales(item.optInt("sales"));
            product.setStock(item.optInt("stock"));
            product.setIcon(item.optString("icon"));
            product.setCover(item.optString("cover"));
            product.setDescription(item.optString("description"));

            List<String> params = new ArrayList<>();
            JSONArray paramArray = item.optJSONArray("params");
            if (paramArray != null) {
                for (int k = 0; k < paramArray.length(); k++) {
                    params.add(paramArray.optString(k));
                }
            }
            product.setParams(params);
            products.add(product);
        }
        return products;
    }
}
