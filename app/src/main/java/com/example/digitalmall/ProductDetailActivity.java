package com.example.digitalmall;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.digitalmall.model.Product;
import com.example.digitalmall.util.CartManager;
import com.example.digitalmall.util.ImageLoader;
import com.example.digitalmall.util.UIUtil;

import java.util.List;
import java.util.Locale;

/**
 * 商品详情页。
 * <p>
 * 对应教材 15.6「实现菜品详情功能业务」：展示名称、价格、参数、图文详情，并支持加入购物车。
 */
public class ProductDetailActivity extends AppCompatActivity {

    public static final String EXTRA_PRODUCT = "extra_product";
    public static final String EXTRA_STORE_ID = "extra_store_id";
    public static final String EXTRA_THEME_COLOR = "extra_theme_color";

    private Product product;
    private int storeId;
    private int quantity = 1;

    private TextView tvQuantity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        Intent intent = getIntent();
        storeId = intent.getIntExtra(EXTRA_STORE_ID, 0);
        String themeColor = intent.getStringExtra(EXTRA_THEME_COLOR);
        product = (Product) intent.getSerializableExtra(EXTRA_PRODUCT);
        if (product == null) {
            UIUtil.toast(this, getString(R.string.product_missing));
            finish();
            return;
        }

        findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        bindProduct(themeColor);
        buildParams();
        initStepper();

        findViewById(R.id.btn_add_cart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addToCart();
            }
        });
    }

    private void bindProduct(String themeColor) {
        ImageView ivCover = findViewById(R.id.iv_cover);
        ivCover.setBackground(UIUtil.coverDrawable(this, themeColor, 10f));
        ImageLoader.load(product.getCover(), ivCover, 0);

        ImageView ivIcon = findViewById(R.id.iv_icon);
        ivIcon.setImageResource(UIUtil.iconRes(product.getIcon()));

        ((TextView) findViewById(R.id.tv_name)).setText(product.getName());
        ((TextView) findViewById(R.id.tv_subtitle)).setText(product.getSubtitle());
        ((TextView) findViewById(R.id.tv_price)).setText(product.getPriceText());

        TextView tvOrigin = findViewById(R.id.tv_origin_price);
        tvOrigin.setText(String.format(Locale.CHINA, "原价 ¥%.2f", product.getOriginPrice()));
        tvOrigin.getPaint().setFlags(android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);

        ((TextView) findViewById(R.id.tv_sales)).setText(product.getSalesText());
        ((TextView) findViewById(R.id.tv_stock)).setText(
                String.format(Locale.CHINA, "现货 %d 件", product.getStock()));

        TextView tvDescription = findViewById(R.id.tv_description);
        String description = product.getDescription();
        tvDescription.setText(TextUtils.isEmpty(description) ? getString(R.string.description_empty) : description);
    }

    /** 规格参数是变长列表，这里用代码动态添加 TextView */
    private void buildParams() {
        LinearLayout container = findViewById(R.id.layout_params);
        container.removeAllViews();

        List<String> params = product.getParams();
        if (params == null || params.isEmpty()) {
            container.setVisibility(View.GONE);
            return;
        }
        container.setVisibility(View.VISIBLE);
        for (String param : params) {
            TextView textView = new TextView(this);
            textView.setText("· " + param);
            textView.setTextSize(12);
            textView.setTextColor(getColor(R.color.text_secondary));
            textView.setGravity(Gravity.START);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.topMargin = UIUtil.dp(this, 6);
            textView.setLayoutParams(lp);
            container.addView(textView);
        }
    }

    private void initStepper() {
        tvQuantity = findViewById(R.id.tv_quantity);
        tvQuantity.setText(String.valueOf(quantity));

        findViewById(R.id.btn_minus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quantity > 1) {
                    quantity--;
                    tvQuantity.setText(String.valueOf(quantity));
                }
            }
        });

        findViewById(R.id.btn_plus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quantity < product.getStock()) {
                    quantity++;
                    tvQuantity.setText(String.valueOf(quantity));
                } else {
                    UIUtil.toast(ProductDetailActivity.this, getString(R.string.stock_not_enough));
                }
            }
        });
    }

    private void addToCart() {
        for (int i = 0; i < quantity; i++) {
            CartManager.getInstance().add(storeId, product);
        }
        UIUtil.toast(this, String.format(Locale.CHINA, "已加入购物车：%s × %d", product.getName(), quantity));
        finish();
    }
}
