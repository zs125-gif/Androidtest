package com.example.digitalmall.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.digitalmall.R;
import com.example.digitalmall.model.Store;
import com.example.digitalmall.util.ImageLoader;
import com.example.digitalmall.util.UIUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 首页店铺列表适配器（对应教材里的 shopAdapter，继承 BaseAdapter + ViewHolder 复用）。
 */
public class StoreAdapter extends BaseAdapter {

    private final Context context;
    private final LayoutInflater inflater;
    private final List<Store> data = new ArrayList<>();

    public StoreAdapter(Context context) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    public void setData(List<Store> stores) {
        data.clear();
        if (stores != null) {
            data.addAll(stores);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Store getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return data.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_store, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.bind(getItem(position));
        return convertView;
    }

    private class ViewHolder {

        private final ImageView ivCover;
        private final ImageView ivIcon;
        private final TextView tvPromotion;
        private final TextView tvName;
        private final TextView tvSlogan;
        private final TextView tvRating;
        private final TextView tvSales;
        private final TextView tvDelivery;
        private final TextView tvDeliveryTime;

        ViewHolder(View itemView) {
            ivCover = itemView.findViewById(R.id.iv_cover);
            ivIcon = itemView.findViewById(R.id.iv_icon);
            tvPromotion = itemView.findViewById(R.id.tv_promotion);
            tvName = itemView.findViewById(R.id.tv_name);
            tvSlogan = itemView.findViewById(R.id.tv_slogan);
            tvRating = itemView.findViewById(R.id.tv_rating);
            tvSales = itemView.findViewById(R.id.tv_sales);
            tvDelivery = itemView.findViewById(R.id.tv_delivery);
            tvDeliveryTime = itemView.findViewById(R.id.tv_delivery_time);
        }

        void bind(Store store) {
            // 封面：先用品牌色渐变占位，能拿到网络图片再覆盖
            ivCover.setBackground(UIUtil.coverDrawable(context, store.getThemeColor(), 8f));
            ivIcon.setImageResource(UIUtil.iconRes(store.getIcon()));
            ImageLoader.load(store.getCover(), ivCover, 0);

            tvName.setText(store.getName());
            tvSlogan.setText(store.getSlogan());
            tvRating.setText(String.format(Locale.CHINA, "★ %.1f", store.getRating()));
            tvSales.setText(store.getMonthSalesText());
            tvDeliveryTime.setText(store.getDeliveryTime());
            tvDelivery.setText(String.format(Locale.CHINA, "配送费 ¥%.0f", store.getDeliveryFee()));

            if (store.getPromotion() == null || store.getPromotion().isEmpty()) {
                tvPromotion.setVisibility(View.GONE);
            } else {
                tvPromotion.setVisibility(View.VISIBLE);
                tvPromotion.setText(store.getPromotion());
            }
        }
    }
}
