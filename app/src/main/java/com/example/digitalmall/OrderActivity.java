package com.example.digitalmall;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.digitalmall.model.CartItem;
import com.example.digitalmall.util.CartManager;
import com.example.digitalmall.util.UIUtil;

import java.util.List;
import java.util.Locale;

/**
 * 订单确认页。
 * <p>
 * 对应教材 15.7「实现订单功能业务」：收货地址、商品清单、小计、配送费、合计、去支付。
 */
public class OrderActivity extends AppCompatActivity {

    public static final String EXTRA_STORE_ID = "extra_store_id";
    public static final String EXTRA_STORE_NAME = "extra_store_name";
    public static final String EXTRA_DELIVERY_FEE = "extra_delivery_fee";
    public static final String EXTRA_THEME_COLOR = "extra_theme_color";

    private int storeId;
    private String storeName;
    private String themeColor;
    private double deliveryFee;

    private double totalPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        Intent intent = getIntent();
        storeId = intent.getIntExtra(EXTRA_STORE_ID, 0);
        storeName = intent.getStringExtra(EXTRA_STORE_NAME);
        themeColor = intent.getStringExtra(EXTRA_THEME_COLOR);
        deliveryFee = intent.getDoubleExtra(EXTRA_DELIVERY_FEE, 0);

        List<CartItem> items = CartManager.getInstance().getItems(storeId);
        if (items == null || items.isEmpty()) {
            UIUtil.toast(this, getString(R.string.cart_empty_tip));
            finish();
            return;
        }

        findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ((TextView) findViewById(R.id.tv_receiver)).setText(R.string.receiver_name);
        ((TextView) findViewById(R.id.tv_phone)).setText(R.string.receiver_phone);
        ((TextView) findViewById(R.id.tv_address)).setText(R.string.receiver_address);
        ((TextView) findViewById(R.id.tv_store_name)).setText(storeName);

        int count = 0;
        for (CartItem item : items) {
            count += item.getCount();
        }
        ((TextView) findViewById(R.id.tv_item_count)).setText(
                String.format(Locale.CHINA, "共 %d 件", count));

        bindItems(items);
        bindAmount(items);

        findViewById(R.id.btn_pay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goPay();
            }
        });
    }

    /** 商品清单用动态 inflate 的方式填充，避免 ScrollView 里嵌套 ListView 的滑动冲突 */
    private void bindItems(List<CartItem> items) {
        LinearLayout container = findViewById(R.id.layout_items);
        LayoutInflater inflater = LayoutInflater.from(this);
        container.removeAllViews();

        for (CartItem item : items) {
            View row = inflater.inflate(R.layout.item_order_product, container, false);

            ImageView ivCover = row.findViewById(R.id.iv_cover);
            ivCover.setBackground(UIUtil.coverDrawable(this, themeColor, 8f));
            ImageView ivIcon = row.findViewById(R.id.iv_icon);
            ivIcon.setImageResource(UIUtil.iconRes(item.getProduct().getIcon()));

            ((TextView) row.findViewById(R.id.tv_name)).setText(item.getProduct().getName());
            ((TextView) row.findViewById(R.id.tv_subtitle)).setText(item.getProduct().getSubtitle());
            ((TextView) row.findViewById(R.id.tv_price)).setText(item.getSubtotalText());
            ((TextView) row.findViewById(R.id.tv_count)).setText(
                    String.format(Locale.CHINA, "×%d", item.getCount()));

            container.addView(row);
        }
    }

    private void bindAmount(List<CartItem> items) {
        double subtotal = 0;
        for (CartItem item : items) {
            subtotal += item.getSubtotal();
        }
        totalPrice = subtotal + deliveryFee;

        String subtotalText = String.format(Locale.CHINA, "¥%.2f", subtotal);
        String feeText = deliveryFee <= 0 ? getString(R.string.free_delivery)
                : String.format(Locale.CHINA, "¥%.2f", deliveryFee);
        String totalText = String.format(Locale.CHINA, "¥%.2f", totalPrice);

        ((TextView) findViewById(R.id.tv_subtotal)).setText(subtotalText);
        ((TextView) findViewById(R.id.tv_delivery_fee)).setText(feeText);
        ((TextView) findViewById(R.id.tv_total)).setText(totalText);
        ((TextView) findViewById(R.id.tv_total_bottom)).setText(totalText);
    }

    private void goPay() {
        // 收银台页面在下一个 PR 接入
        UIUtil.toast(this, String.format(Locale.CHINA, "应付 ¥%.2f，正在打开收银台…", totalPrice));
    }
}
