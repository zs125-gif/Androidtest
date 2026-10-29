package com.example.digitalmall.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;
import android.widget.ImageView;

import com.example.digitalmall.R;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 极简图片加载器：内存缓存 + 线程池下载 + 复用防错位。
 * <p>
 * 服务器上没有真实图片时会一直加载失败，这里记录了「已失败的 host」，
 * 失败一次后同 host 直接跳过，避免列表滑动时反复等超时。
 */
public class ImageLoader {

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(3);
    private static final Set<String> FAILED_HOSTS = Collections.synchronizedSet(new HashSet<String>());

    private static final LruCache<String, Bitmap> CACHE;

    static {
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        int cacheSize = maxMemory / 8;
        CACHE = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount() / 1024;
            }
        };
    }

    private ImageLoader() {
    }

    /**
     * @param placeholderRes 加载中/失败时显示的占位图，传 0 表示不显示占位图（只保留背景）
     */
    public static void load(final String url, final ImageView imageView, final int placeholderRes) {
        imageView.setTag(R.id.tag_image_url, url);
        if (url == null || url.trim().isEmpty()) {
            showPlaceholder(imageView, placeholderRes);
            return;
        }
        String host = hostOf(url);
        if (FAILED_HOSTS.contains(host)) {
            showPlaceholder(imageView, placeholderRes);
            return;
        }
        Bitmap cached = CACHE.get(url);
        if (cached != null) {
            imageView.setImageBitmap(cached);
            return;
        }
        showPlaceholder(imageView, placeholderRes);
        EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                final Bitmap bitmap = download(url);
                if (bitmap == null) {
                    FAILED_HOSTS.add(hostOf(url));
                    return;
                }
                CACHE.put(url, bitmap);
                imageView.post(new Runnable() {
                    @Override
                    public void run() {
                        if (url.equals(imageView.getTag(R.id.tag_image_url))) {
                            imageView.setImageBitmap(bitmap);
                        }
                    }
                });
            }
        });
    }

    private static void showPlaceholder(ImageView imageView, int placeholderRes) {
        if (placeholderRes > 0) {
            imageView.setImageResource(placeholderRes);
        } else {
            imageView.setImageDrawable(null);
        }
    }

    private static String hostOf(String url) {
        try {
            return new URL(url).getHost();
        } catch (Exception e) {
            return "";
        }
    }

    private static Bitmap download(String url) {
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            connection.setRequestMethod("GET");
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                inputStream = connection.getInputStream();
                return BitmapFactory.decodeStream(inputStream);
            }
        } catch (Exception ignored) {
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Exception ignored) {
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }
}
