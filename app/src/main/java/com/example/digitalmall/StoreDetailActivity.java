package com.example.digitalmall;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.digitalmall.adapter.CategoryAdapter;
import com.example.digitalmall.adapter.ProductAdapter;
import com.example.digitalmall.model.Category;
import com.example.digitalmall.model.Product;
import com.example.digitalmall.net.Api;
import com.example.digitalmall.net.HttpUtil;
import com.example.digitalmall.net.JsonParser;
import com.example.digitalmall.util.CartManager;
import com.example.digitalmall.util.RawData;
import com.example.digitalmall.util.UIUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 店铺详情页：左侧分类 + 右侧商品 + 底部购物车。
 * <p>
 * 对应教材 15.5「实现店铺详情功能业务」：分栏列表、加入购物车、清空购物车。
 */
public class StoreDetailActivity extends AppCompatActivity implements ProductAdapter.OnProductActionListener {

    private static final String TAG = "StoreDetailActivity";

    public static final String EXTRA_STORE_ID = "extra_store_id";
    public static final String EXTRA_STORE_NAME = "extra_store_name";
    public static final String EXTRA_STORE_NOTICE = "extra_store_notice";
    public static final String EXTRA_THEME_COLOR = "extra_theme_color";
    public static final String EXTRA_DELIVERY_FEE = "extra_delivery_fee";

    private int storeId;
    private String storeName;
    private String themeColor;
    private double deliveryFee;

    private final List<Category> categories = new ArrayList<>();

    private CategoryAdapter categoryAdapter;
    private ProductAdapter productAdapter;

    private ListView lvCategory;
    private ListView lvProduct;
    private ProgressBar progressBar;
    private TextView tvCartCount;
    private TextView tvTotal;
    private TextView tvCartHint;

    private final CartManager cartManager = CartManager.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_detail);

        readExtras();
        initViews();
        updateCartBar();
        loadStoreDetail();
    }

    private void readExtras() {
        Intent intent = getIntent();
        storeId = intent.getIntExtra(EXTRA_STORE_ID, 0);
        storeName = intent.getStringExtra(EXTRA_STORE_NAME);
        themeColor = intent.getStringExtra(EXTRA_THEME_COLOR);
        deliveryFee = intent.getDoubleExtra(EXTRA_DELIVERY_FEE, 0);
        String notice = intent.getStringExtra(EXTRA_STORE_NOTICE);
        if (notice == null || notice.isEmpty()) {
            notice = getString(R.string.store_notice_default);
        }
        ((TextView) findViewById(R.id.tv_notice)).setText(notice);
    }

    private void initViews() {
        TextView tvStoreName = findViewById(R.id.tv_store_name);
        TextView tvStoreSub = findViewById(R.id.tv_store_sub);
        tvStoreName.setText(storeName);
        tvStoreSub.setText(String.format(Locale.CHINA, "配送费 ¥%.0f · 由本店发货", deliveryFee));

        findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.tv_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmClearCart();
            }
        });

        findViewById(R.id.btn_settle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goSettle();
            }
        });

        tvCartCount = findViewById(R.id.tv_cart_count);
        tvTotal = findViewById(R.id.tv_total);
        tvCartHint = findViewById(R.id.tv_cart_hint);
        progressBar = findViewById(R.id.progress_bar);

        categoryAdapter = new CategoryAdapter(this);
        lvCategory = findViewById(R.id.lv_category);
        lvCategory.setAdapter(categoryAdapter);
        lvCategory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                categoryAdapter.setSelectedIndex(position);
                showProducts(position);
            }
        });

        productAdapter = new ProductAdapter(this, storeId, themeColor);
        productAdapter.setOnProductActionListener(this);
        lvProduct = findViewById(R.id.lv_product);
        lvProduct.setAdapter(productAdapter);
    }

    private void loadStoreDetail() {
        progressBar.setVisibility(View.VISIBLE);
        Api.getStoreDetail(new HttpUtil.Callback() {
            @Override
            public void onSuccess(String body) {
                try {
                    applyCategories(JsonParser.parseCategories(body, storeId));
                } catch (Exception e) {
                    applyCategories(parseOffline());
                }
            }

            @Override
            public void onFailure(String message) {
                // 离线数据与服务器数据一致，静默降级即可，不打扰用户
                applyCategories(parseOffline());
                Log.i(TAG, "服务器不可用，已静默切换到内置离线数据（店铺详情）：" + message);
            }
        });
    }

    private List<Category> parseOffline() {
        try {
            return JsonParser.parseCategories(RawData.storeDetailJson(this), storeId);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private void applyCategories(List<Category> list) {
        categories.clear();
        if (list != null) {
            categories.addAll(list);
        }
        progressBar.setVisibility(View.GONE);
        categoryAdapter.setData(categories);
        if (categories.isEmpty()) {
            productAdapter.setData(null);
            UIUtil.toast(this, getString(R.string.empty_product_list));
            return;
        }
        showProducts(0);
    }

    private void showProducts(int index) {
        if (index < 0 || index >= categories.size()) {
            return;
        }
        productAdapter.setData(categories.get(index).getProducts());
        lvProduct.setSelection(0);
    }

    /** 刷新底部购物车条 */
    private void updateCartBar() {
        int count = cartManager.getTotalCount(storeId);
        double total = cartManager.getTotalPrice(storeId);

        tvTotal.setText(String.format(Locale.CHINA, "¥%.2f", total));
        if (count > 0) {
            tvCartCount.setVisibility(View.VISIBLE);
            tvCartCount.setText(String.valueOf(count));
            tvCartHint.setText(String.format(Locale.CHINA, "已选 %d 件 · 配送费 ¥%.0f", count, deliveryFee));
        } else {
            tvCartCount.setVisibility(View.GONE);
            tvCartHint.setText(R.string.cart_empty);
        }
    }

    private void confirmClearCart() {
        if (cartManager.isEmpty(storeId)) {
            UIUtil.toast(this, getString(R.string.cart_empty));
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.clear_cart_title)
                .setMessage(R.string.clear_cart_message)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        cartManager.clear(storeId);
                        productAdapter.notifyDataSetChanged();
                        updateCartBar();
                        UIUtil.toast(StoreDetailActivity.this, getString(R.string.cart_cleared));
                    }
                })
                .show();
    }

    private void goSettle() {
        if (cartManager.isEmpty(storeId)) {
            UIUtil.toast(this, getString(R.string.cart_empty_tip));
            return;
        }
        Intent intent = new Intent(this, OrderActivity.class);
        intent.putExtra(OrderActivity.EXTRA_STORE_ID, storeId);
        intent.putExtra(OrderActivity.EXTRA_STORE_NAME, storeName);
        intent.putExtra(OrderActivity.EXTRA_DELIVERY_FEE, deliveryFee);
        intent.putExtra(OrderActivity.EXTRA_THEME_COLOR, themeColor);
        startActivity(intent);
    }

    @Override
    public void onProductClick(Product product) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra(ProductDetailActivity.EXTRA_PRODUCT, product);
        intent.putExtra(ProductDetailActivity.EXTRA_STORE_ID, storeId);
        intent.putExtra(ProductDetailActivity.EXTRA_THEME_COLOR, themeColor);
        startActivity(intent);
    }

    @Override
    public void onAddToCart(Product product) {
        cartManager.add(storeId, product);
        productAdapter.notifyDataSetChanged();
        updateCartBar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 从商品详情页返回时，购物车可能已变化
        if (productAdapter != null) {
            productAdapter.notifyDataSetChanged();
            updateCartBar();
        }
    }
}
