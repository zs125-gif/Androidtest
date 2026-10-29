package com.example.digitalmall;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.digitalmall.adapter.OrderAdapter;
import com.example.digitalmall.db.OrderDao;
import com.example.digitalmall.model.Order;
import com.example.digitalmall.util.SessionManager;

import java.util.List;

/**
 * 我的订单：从 SQLite 的 orders 表读当前登录用户的订单，ListView 展示。
 * 这是「SQLite 保存订单数据」这条要求的可见出口。
 */
public class OrderListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_list);

        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ListView listView = findViewById(R.id.lv_orders);
        TextView tvEmpty = findViewById(R.id.tv_empty);

        String username = SessionManager.getUsername(this);
        List<Order> orders = OrderDao.listByUser(this, username);

        if (orders.isEmpty()) {
            listView.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            listView.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
            listView.setAdapter(new OrderAdapter(this, orders));
        }
    }
}
