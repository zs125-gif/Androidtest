package com.example.digitalmall;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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
 * 首页 Fragment：品牌店铺列表。
 * <p>
 * 对应教材 15.4「店铺界面」：异步访问网络 -> 解析 JSON -> Handler 通知主线程刷新 GridView。
 * 用 Fragment 承载是为了满足「Activity / Fragment 组织页面结构」的要求：
 * 页面容器 MainActivity 负责导航，具体业务页面各自由 Fragment 实现，职责清晰。
 */
public class HomeFragment extends Fragment {

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
            if (msg.what == MSG_STORE_LOADED && isAdded()) {
                progressBar.setVisibility(View.GONE);
                adapter.notifyDataSetChanged();
                int count = adapter.getCount();
                tvEmpty.setVisibility(count == 0 ? View.VISIBLE : View.GONE);
                if (count == 0) {
                    tvEmpty.setText(R.string.empty_store_list);
                }
                tvStoreCount.setText(String.format(Locale.CHINA, "共 %d 家", count));
                // 数据到位后做一次淡入，避免列表"啪"地一下出现
                gridStore.setAlpha(0f);
                gridStore.animate().alpha(1f).setDuration(320).start();
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        gridStore = view.findViewById(R.id.grid_store);
        progressBar = view.findViewById(R.id.progress_bar);
        tvEmpty = view.findViewById(R.id.tv_empty);
        tvStoreCount = view.findViewById(R.id.tv_store_count);

        adapter = new StoreAdapter(requireContext());
        gridStore.setAdapter(adapter);
        gridStore.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View item, int position, long id) {
                Store store = adapter.getItem(position);
                Intent intent = new Intent(getActivity(), StoreDetailActivity.class);
                intent.putExtra(StoreDetailActivity.EXTRA_STORE_ID, store.getId());
                intent.putExtra(StoreDetailActivity.EXTRA_STORE_NAME, store.getName());
                intent.putExtra(StoreDetailActivity.EXTRA_STORE_NOTICE, store.getNotice());
                intent.putExtra(StoreDetailActivity.EXTRA_THEME_COLOR, store.getThemeColor());
                intent.putExtra(StoreDetailActivity.EXTRA_DELIVERY_FEE, store.getDeliveryFee());
                startActivity(intent);
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
                    // JSON 结构异常 -> 降级到离线数据，避免白屏
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
        if (!isAdded()) {
            return;
        }
        try {
            List<Store> stores = JsonParser.parseStores(RawData.storeListJson(requireContext()));
            adapter.setData(stores);
            UIUtil.toast(requireContext(), getString(R.string.net_error));
        } catch (Exception e) {
            adapter.setData(null);
            UIUtil.toast(requireContext(), getString(R.string.parse_error));
        }
        handler.sendEmptyMessage(MSG_STORE_LOADED);
    }

    @Override
    public void onDestroyView() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroyView();
    }
}
