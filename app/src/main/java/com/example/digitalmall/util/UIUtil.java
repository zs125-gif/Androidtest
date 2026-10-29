package com.example.digitalmall.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.digitalmall.R;

/**
 * 通用 UI 工具：dp 换算、颜色解析、封面占位背景、商品图标映射。
 */
public class UIUtil {

    private UIUtil() {
    }

    public static int dp(Context context, float value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    public static void toast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    /** 解析 #RRGGBB 颜色，失败时返回默认色 */
    public static int parseColor(String hex, int defaultColor) {
        if (hex == null || hex.trim().isEmpty()) {
            return defaultColor;
        }
        try {
            return Color.parseColor(hex.trim());
        } catch (Exception e) {
            return defaultColor;
        }
    }

    /** 把颜色向白色靠拢，用于做渐变的浅色端 */
    public static int lighten(int color, float ratio) {
        int r = (int) (Color.red(color) + (255 - Color.red(color)) * ratio);
        int g = (int) (Color.green(color) + (255 - Color.green(color)) * ratio);
        int b = (int) (Color.blue(color) + (255 - Color.blue(color)) * ratio);
        return Color.rgb(r, g, b);
    }

    /**
     * 生成商品/店铺封面占位背景：主题色渐变 + 圆角。
     * 项目没有真实商品图，用品牌色渐变 + 品类图标来保证界面观感统一。
     */
    public static GradientDrawable coverDrawable(Context context, String hexColor, float radiusDp) {
        int base = parseColor(hexColor, context.getColor(R.color.cover_blue));
        GradientDrawable drawable = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{lighten(base, 0.42f), base});
        drawable.setCornerRadius(dp(context, radiusDp));
        return drawable;
    }

    /**
     * 封面图片名 -> 本地图片资源。
     * <p>
     * 数据源（store_list.json / store_detail.json）里的 cover 字段可以直接写资源名，
     * 例如 "pic_phone"；如果写的是 http 开头的网络地址则返回 0，交给 ImageLoader 异步下载。
     */
    public static int picRes(String cover) {
        if (cover == null || cover.trim().isEmpty()) {
            return 0;
        }
        String name = cover.trim();
        if (name.startsWith("http://") || name.startsWith("https://") || name.startsWith("file:")) {
            return 0;
        }
        switch (name) {
            case "pic_phone":
                return R.drawable.pic_phone;
            case "pic_laptop":
                return R.drawable.pic_laptop;
            case "pic_tablet":
                return R.drawable.pic_tablet;
            case "pic_headset":
                return R.drawable.pic_headset;
            case "pic_watch":
                return R.drawable.pic_watch;
            case "pic_charger":
                return R.drawable.pic_charger;
            case "pic_store_1":
                return R.drawable.pic_store_1;
            case "pic_store_2":
                return R.drawable.pic_store_2;
            case "pic_store_3":
                return R.drawable.pic_store_3;
            case "pic_store_4":
                return R.drawable.pic_store_4;
            case "pic_store_5":
                return R.drawable.pic_store_5;
            default:
                return 0;
        }
    }

    /**
     * 统一处理封面：优先使用真实图片，没有图片时退回「品牌色渐变 + 品类图标」。
     * <p>
     * ListView 会复用条目，所以两个分支都要显式清理上一次的设置，否则会串图。
     */
    public static void bindCover(Context context, ImageView ivCover, ImageView ivIcon,
                                 String cover, String icon, String themeColor, float radiusDp) {
        int res = picRes(cover);
        if (res != 0) {
            // 白底 + 圆角，配合布局里的 clipToOutline 把图片裁成圆角
            GradientDrawable white = new GradientDrawable();
            white.setColor(Color.WHITE);
            white.setCornerRadius(dp(context, radiusDp));
            ivCover.setBackground(white);
            ivCover.setImageResource(res);
            ivCover.setScaleType(ImageView.ScaleType.CENTER_CROP);
            if (ivIcon != null) {
                ivIcon.setVisibility(View.GONE);
            }
        } else {
            if (ivIcon != null) {
                ivIcon.setVisibility(View.VISIBLE);
                ivIcon.setImageResource(iconRes(icon));
            }
            ivCover.setImageDrawable(null);
            ivCover.setBackground(coverDrawable(context, themeColor, radiusDp));
            ImageLoader.load(cover, ivCover, 0);
        }
    }

    /** 商品品类字符串 -> 图标资源 */
    public static int iconRes(String icon) {        if (icon == null) {
            return R.drawable.ic_store;
        }
        switch (icon) {
            case "phone":
                return R.drawable.ic_phone;
            case "laptop":
                return R.drawable.ic_laptop;
            case "tablet":
                return R.drawable.ic_tablet;
            case "headset":
                return R.drawable.ic_headset;
            case "watch":
                return R.drawable.ic_watch;
            case "charger":
                return R.drawable.ic_charger;
            default:
                return R.drawable.ic_store;
        }
    }
}
