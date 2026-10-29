package com.example.digitalmall.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.digitalmall.R;
import com.example.digitalmall.model.Product;
import com.example.digitalmall.model.SearchItem;
import com.example.digitalmall.util.CartManager;
import com.example.digitalmall.util.UIUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 搜索结果适配器。
 * <p>
 * 和店铺详情页的商品列表相比，这里多了两件事：
 * 1. 商品名里命中的关键词要标成品牌色（Spannable 富文本）；
 * 2. 结果来自不同店铺，条目上要标出商品属于哪家店。
 */
public class SearchAdapter extends BaseAdapter {

    public interface OnSearchActionListener {
        void onItemClick(SearchItem item);

        void onAddToCart(SearchItem item);
    }

    private final Context context;
    private final LayoutInflater inflater;
    private final List<SearchItem> data = new ArrayList<>();
    private final List<String> keywords = new ArrayList<>();

    private final int highlightColor;
    private final int hintColor;
    private final int defaultStoreColor;

    private OnSearchActionListener listener;

    public SearchAdapter(Context context) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.highlightColor = context.getColor(R.color.brand_primary);
        this.hintColor = context.getColor(R.color.text_hint);
        this.defaultStoreColor = context.getColor(R.color.brand_primary);
    }

    public void setOnSearchActionListener(OnSearchActionListener listener) {
        this.listener = listener;
    }

    public void setData(List<SearchItem> items, String keyword) {
        data.clear();
        if (items != null) {
            data.addAll(items);
        }
        keywords.clear();
        if (keyword != null) {
            for (String token : keyword.toLowerCase(Locale.ROOT).split("\\s+")) {
                if (!token.isEmpty()) {
                    keywords.add(token);
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public SearchItem getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        Product product = data.get(position).getProduct();
        return product == null ? position : product.getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_search_result, parent, false);
            holder = new ViewHolder(convertView);
            holder.tvOriginPrice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final SearchItem item = getItem(position);
        final Product product = item.getProduct();
        if (product == null) {
            return convertView;
        }

        UIUtil.bindCover(context, holder.ivCover, holder.ivIcon,
                product.getCover(), product.getIcon(), item.getThemeColor(), 8f);

        holder.tvName.setText(highlight(product.getName()));
        holder.tvStoreLine.setText(buildStoreLine(item, product));
        holder.tvPrice.setText(product.getPriceText());
        holder.tvOriginPrice.setText(String.format(Locale.CHINA, "¥%.2f", product.getOriginPrice()));

        int count = CartManager.getInstance().getCountOf(item.getStoreId(), product.getId());
        if (count > 0) {
            holder.tvCount.setVisibility(View.VISIBLE);
            holder.tvCount.setText(String.format(Locale.CHINA, "已选 %d", count));
        } else {
            holder.tvCount.setVisibility(View.GONE);
        }

        holder.btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onAddToCart(item);
                }
            }
        });

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(item);
                }
            }
        });

        return convertView;
    }

    /** 商品名里的关键词标品牌色，其余保持原色 */
    private CharSequence highlight(String text) {
        if (text == null) {
            return "";
        }
        SpannableString spannable = new SpannableString(text);
        String lower = text.toLowerCase(Locale.ROOT);
        for (String token : keywords) {
            int from = 0;
            while (true) {
                int index = lower.indexOf(token, from);
                if (index < 0) {
                    break;
                }
                spannable.setSpan(new ForegroundColorSpan(highlightColor),
                        index, index + token.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                from = index + token.length();
            }
        }
        return spannable;
    }

    /** 「店铺名 · 商品副标题」，店铺名用品牌色，副标题灰色 */
    private CharSequence buildStoreLine(SearchItem item, Product product) {
        String storeName = item.getStoreName() == null ? "" : item.getStoreName();
        String subtitle = product.getSubtitle() == null ? "" : product.getSubtitle();
        String line = subtitle.isEmpty() ? storeName : storeName + " · " + subtitle;
        SpannableString spannable = new SpannableString(line);
        spannable.setSpan(new ForegroundColorSpan(
                        UIUtil.parseColor(item.getThemeColor(), defaultStoreColor)),
                0, storeName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new ForegroundColorSpan(hintColor),
                storeName.length(), line.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    private static class ViewHolder {
        private final ImageView ivCover;
        private final ImageView ivIcon;
        private final TextView tvName;
        private final TextView tvStoreLine;
        private final TextView tvPrice;
        private final TextView tvOriginPrice;
        private final TextView tvCount;
        private final TextView btnAdd;

        ViewHolder(View itemView) {
            ivCover = itemView.findViewById(R.id.iv_cover);
            ivIcon = itemView.findViewById(R.id.iv_icon);
            tvName = itemView.findViewById(R.id.tv_name);
            tvStoreLine = itemView.findViewById(R.id.tv_store_line);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvOriginPrice = itemView.findViewById(R.id.tv_origin_price);
            tvCount = itemView.findViewById(R.id.tv_count);
            btnAdd = itemView.findViewById(R.id.btn_add);
        }
    }
}
