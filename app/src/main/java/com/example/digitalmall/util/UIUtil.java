package com.example.digitalmall.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
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

    /** 商品品类字符串 -> 图标资源 */
    public static int iconRes(String icon) {
        if (icon == null) {
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
