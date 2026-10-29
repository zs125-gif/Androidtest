package com.example.digitalmall.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.digitalmall.R;
import com.example.digitalmall.model.Category;

import java.util.ArrayList;
import java.util.List;

/**
 * 店铺详情页左侧分类适配器。
 */
public class CategoryAdapter extends BaseAdapter {

    private final Context context;
    private final LayoutInflater inflater;
    private final List<Category> data = new ArrayList<>();
    private int selectedIndex = 0;

    public CategoryAdapter(Context context) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    public void setData(List<Category> categories) {
        data.clear();
        if (categories != null) {
            data.addAll(categories);
        }
        selectedIndex = 0;
        notifyDataSetChanged();
    }

    public void setSelectedIndex(int index) {
        if (index < 0 || index >= data.size() || index == selectedIndex) {
            return;
        }
        selectedIndex = index;
        notifyDataSetChanged();
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Category getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_category, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        boolean selected = position == selectedIndex;
        convertView.setSelected(selected);
        holder.indicator.setVisibility(selected ? View.VISIBLE : View.INVISIBLE);
        holder.tvCategory.setText(data.get(position).getName());
        holder.tvCategory.setTextColor(context.getColor(
                selected ? R.color.brand_primary : R.color.text_secondary));
        holder.tvCategory.setTypeface(selected ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        return convertView;
    }

    private static class ViewHolder {
        private final View indicator;
        private final TextView tvCategory;

        ViewHolder(View itemView) {
            indicator = itemView.findViewById(R.id.indicator);
            tvCategory = itemView.findViewById(R.id.tv_category);
        }
    }
}
