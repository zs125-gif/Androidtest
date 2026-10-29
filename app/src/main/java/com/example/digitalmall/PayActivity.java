package com.example.digitalmall;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.digitalmall.db.OrderDao;
import com.example.digitalmall.model.CartItem;
import com.example.digitalmall.util.CartManager;
import com.example.digitalmall.util.SessionManager;
import com.example.digitalmall.util.UIUtil;

import java.util.List;
import java.util.Locale;

/**
 * 收银台 / 扫码支付页。
 * <p>
 * 对应教材 15.7 的「去支付 → 二维码支付」：展示应付金额与二维码，
 * 点击「我已完成支付」后延迟回调模拟支付结果，并清空当前店铺购物车。
 */
public class PayActivity extends AppCompatActivity {

    public static final String EXTRA_STORE_ID = "extra_store_id";
    public static final String EXTRA_STORE_NAME = "extra_store_name";
    public static final String EXTRA_AMOUNT = "extra_amount";

    private int storeId;
    private String storeName;
    private double amount;

    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);

        Intent intent = getIntent();
        storeId = intent.getIntExtra(EXTRA_STORE_ID, 0);
        storeName = intent.getStringExtra(EXTRA_STORE_NAME);
        amount = intent.getDoubleExtra(EXTRA_AMOUNT, 0);

        ((TextView) findViewById(R.id.tv_amount)).setText(String.format(Locale.CHINA, "¥%.2f", amount));
        ((TextView) findViewById(R.id.tv_store_name)).setText(storeName == null ? "" : storeName);

        findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.btn_paid).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                simulatePay();
            }
        });

        findViewById(R.id.btn_back_home).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToHome();
            }
        });
    }

    /** 真实项目里这里应该轮询订单状态；本项目用 800ms 延迟模拟支付成功回调 */
    private void simulatePay() {
        UIUtil.toast(this, getString(R.string.paying));
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                showSuccess();
            }
        }, 800);
    }

    private void showSuccess() {
        // 先把购物车快照下来，再清空——订单要落库，清空后就查不到了
        List<CartItem> items = CartManager.getInstance().getItems(storeId);
        int itemCount = CartManager.getInstance().getTotalCount(storeId);

        StringBuilder summary = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            CartItem item = items.get(i);
            if (i > 0) {
                summary.append("、");
            }
            summary.append(item.getProduct().getName()).append("×").append(item.getCount());
        }

        String username = SessionManager.getUsername(this);
        if (username == null) {
            username = "guest";
        }
        // 支付成功 -> 订单写入 SQLite（课程设计第 5 项技术要求）
        OrderDao.insert(this, username, storeId,
                storeName == null ? "" : storeName, amount, itemCount, summary.toString());

        CartManager.getInstance().clear(storeId);

        findViewById(R.id.layout_pay_content).setVisibility(View.GONE);
        findViewById(R.id.layout_pay_success).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.tv_paid_amount)).setText(
                String.format(Locale.CHINA, "实付 ¥%.2f", amount));
    }

    private void backToHome() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
