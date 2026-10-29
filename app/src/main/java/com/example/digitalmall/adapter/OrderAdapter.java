package com.example.digitalmall.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.digitalmall.R;
import com.example.digitalmall.model.Order;

import java.util.List;
import java.util.Locale;

/**
 * 我的订单列表适配器（ListView + ViewHolder 复用）。
 */
public class OrderAdapter extends BaseAdapter {

    private final LayoutInflater inflater;
    private final List<Order> data;

    public OrderAdapter(Context context, List<Order> data) {
        this.inflater = LayoutInflater.from(context);
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Order getItem(int position) {
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
            convertView = inflater.inflate(R.layout.item_order, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Order order = getItem(position);
        holder.tvStoreName.setText(order.getStoreName());
        holder.tvStatus.setText(order.getStatusText());
        holder.tvSummary.setText(String.format(Locale.CHINA, "共 %d 件 · %s",
                order.getItemCount(),
                order.getSummary() == null ? "" : order.getSummary()));
        holder.tvOrderNo.setText(String.format(Locale.CHINA, "%s  %s", order.getOrderNo(), order.getTimeText()));
        holder.tvTotal.setText(order.getTotalText());
        return convertView;
    }

    private static class ViewHolder {
        private final TextView tvStoreName;
        private final TextView tvStatus;
        private final TextView tvSummary;
        private final TextView tvOrderNo;
        private final TextView tvTotal;

        ViewHolder(View itemView) {
            tvStoreName = itemView.findViewById(R.id.tv_store_name);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvSummary = itemView.findViewById(R.id.tv_summary);
            tvOrderNo = itemView.findViewById(R.id.tv_order_no);
            tvTotal = itemView.findViewById(R.id.tv_total);
        }
    }
}
