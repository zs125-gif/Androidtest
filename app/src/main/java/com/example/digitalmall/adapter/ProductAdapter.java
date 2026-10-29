package com.example.digitalmall.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.digitalmall.R;
import com.example.digitalmall.model.Product;
import com.example.digitalmall.util.CartManager;
import com.example.digitalmall.util.UIUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 店铺详情页右侧商品适配器。
 * <p>
 * 右侧列表只显示「当前选中分类」下的商品，所以这里是每次切换分类就整体 setData。
 */
public class ProductAdapter extends BaseAdapter {

    public interface OnProductActionListener {
        void onProductClick(Product product);

        void onAddToCart(Product product);
    }

    private final Context context;
    private final LayoutInflater inflater;
    private final int storeId;
    private final String themeColor;
    private final List<Product> data = new ArrayList<>();

    private OnProductActionListener listener;

    public ProductAdapter(Context context, int storeId, String themeColor) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.storeId = storeId;
        this.themeColor = themeColor;
    }

    public void setOnProductActionListener(OnProductActionListener listener) {
        this.listener = listener;
    }

    public void setData(List<Product> products) {
        data.clear();
        if (products != null) {
            data.addAll(products);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Product getItem(int position) {
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
            convertView = inflater.inflate(R.layout.item_product, parent, false);
            holder = new ViewHolder(convertView);
            holder.tvOriginPrice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Product product = getItem(position);

        // 商品图：真实图片优先，没有图片再退回品牌色渐变 + 品类图标
        UIUtil.bindCover(context, holder.ivCover, holder.ivIcon,
                product.getCover(), product.getIcon(), themeColor, 8f);

        holder.tvName.setText(product.getName());
        holder.tvSubtitle.setText(product.getSubtitle());
        holder.tvSales.setText(String.format(Locale.CHINA, "%s · 库存 %d",
                product.getSalesText(), product.getStock()));
        holder.tvPrice.setText(product.getPriceText());
        holder.tvOriginPrice.setText(String.format(Locale.CHINA, "¥%.2f", product.getOriginPrice()));

        int count = CartManager.getInstance().getCountOf(storeId, product.getId());
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
                    listener.onAddToCart(product);
                }
            }
        });

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onProductClick(product);
                }
            }
        });

        return convertView;
    }

    private static class ViewHolder {
        private final ImageView ivCover;
        private final ImageView ivIcon;
        private final TextView tvName;
        private final TextView tvSubtitle;
        private final TextView tvSales;
        private final TextView tvPrice;
        private final TextView tvOriginPrice;
        private final TextView tvCount;
        private final TextView btnAdd;

        ViewHolder(View itemView) {
            ivCover = itemView.findViewById(R.id.iv_cover);
            ivIcon = itemView.findViewById(R.id.iv_icon);
            tvName = itemView.findViewById(R.id.tv_name);
            tvSubtitle = itemView.findViewById(R.id.tv_subtitle);
            tvSales = itemView.findViewById(R.id.tv_sales);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvOriginPrice = itemView.findViewById(R.id.tv_origin_price);
            tvCount = itemView.findViewById(R.id.tv_count);
            btnAdd = itemView.findViewById(R.id.btn_add);
        }
    }
}
