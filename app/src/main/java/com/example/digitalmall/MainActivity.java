package com.example.digitalmall;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.digitalmall.adapter.StoreAdapter;
import com.example.digitalmall.model.Store;
import com.example.digitalmall.net.Api;
import com.example.digitalmall.net.HttpUtil;
import com.example.digitalmall.net.JsonParser;
import com.example.digitalmall.util.RawData;
import com.example.digitalmall.util.UIUtil;

import java.util.List;
import java.util.Locale;

/**
 * 首页：品牌店铺列表。
 * <p>
 * 对应教材 15.4「店铺界面」：异步访问网络 -> 解析 JSON -> Handler 通知主线程刷新 GridView。
 */
public class MainActivity extends AppCompatActivity {

    private static final int MSG_STORE_LOADED = 0x11;

    private GridView gridStore;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private TextView tvStoreCount;

    private StoreAdapter adapter;

    /** 子线程解析完数据后，用 Handler 回到主线程刷新界面（教材知识点） */
    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == MSG_STORE_LOADED) {
                progressBar.setVisibility(View.GONE);
                adapter.notifyDataSetChanged();
                int count = adapter.getCount();
                tvEmpty.setVisibility(count == 0 ? View.VISIBLE : View.GONE);
                if (count == 0) {
                    tvEmpty.setText(R.string.empty_store_list);
                }
                tvStoreCount.setText(String.format(Locale.CHINA, "共 %d 家", count));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gridStore = findViewById(R.id.grid_store);
        progressBar = findViewById(R.id.progress_bar);
        tvEmpty = findViewById(R.id.tv_empty);
        tvStoreCount = findViewById(R.id.tv_store_count);

        adapter = new StoreAdapter(this);
        gridStore.setAdapter(adapter);
        gridStore.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Store store = adapter.getItem(position);
                // 店铺详情页在下一个 PR 里接入，这里先做点击反馈
                UIUtil.toast(MainActivity.this, "即将进入：" + store.getName());
            }
        });

        loadStoreList();
    }

    private void loadStoreList() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        Api.getStoreList(new HttpUtil.Callback() {
            @Override
            public void onSuccess(String body) {
                try {
                    adapter.setData(JsonParser.parseStores(body));
                    handler.sendEmptyMessage(MSG_STORE_LOADED);
                } catch (Exception e) {
                    loadOfflineData();
                }
            }

            @Override
            public void onFailure(String message) {
                loadOfflineData();
            }
        });
    }

    /** 服务器不可用时读取 res/raw 下的离线数据，保证界面不空白 */
    private void loadOfflineData() {
        try {
            List<Store> stores = JsonParser.parseStores(RawData.storeListJson(MainActivity.this));
            adapter.setData(stores);
            UIUtil.toast(MainActivity.this, getString(R.string.net_error));
        } catch (Exception e) {
            adapter.setData(null);
            UIUtil.toast(MainActivity.this, getString(R.string.parse_error));
        }
        handler.sendEmptyMessage(MSG_STORE_LOADED);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
