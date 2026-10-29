package com.example.digitalmall;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

/**
 * 主容器 Activity：底部两个 Tab（首页 / 我的），内容由 Fragment 承载。
 * <p>
 * 这样「Activity 负责导航、Fragment 负责页面内容」，符合课程设计第 1 项技术要求。
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG_HOME = "tab_home";
    private static final String TAG_MINE = "tab_mine";

    private ImageView ivTabHome;
    private ImageView ivTabMine;
    private TextView tvTabHome;
    private TextView tvTabMine;

    private Fragment homeFragment;
    private Fragment mineFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ivTabHome = findViewById(R.id.iv_tab_home);
        ivTabMine = findViewById(R.id.iv_tab_mine);
        tvTabHome = findViewById(R.id.tv_tab_home);
        tvTabMine = findViewById(R.id.tv_tab_mine);

        FragmentManager fm = getSupportFragmentManager();
        if (savedInstanceState == null) {
            homeFragment = new HomeFragment();
            mineFragment = new MineFragment();
            // 两个 Fragment 一次性 add，之后用 hide/show 切换，避免重建丢失列表状态
            fm.beginTransaction()
                    .add(R.id.fragment_container, homeFragment, TAG_HOME)
                    .add(R.id.fragment_container, mineFragment, TAG_MINE)
                    .hide(mineFragment)
                    .commit();
        } else {
            homeFragment = fm.findFragmentByTag(TAG_HOME);
            mineFragment = fm.findFragmentByTag(TAG_MINE);
        }

        findViewById(R.id.tab_home).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToHome();
            }
        });
        findViewById(R.id.tab_mine).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToMine();
            }
        });

        updateTabStyle(true);
    }

    public void switchToHome() {
        if (homeFragment == null || mineFragment == null) {
            return;
        }
        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        tx.show(homeFragment).hide(mineFragment).commit();
        updateTabStyle(true);
    }

    public void switchToMine() {
        if (homeFragment == null || mineFragment == null) {
            return;
        }
        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        tx.show(mineFragment).hide(homeFragment).commit();
        updateTabStyle(false);
    }

    /** 选中态：图标与文字都用品牌蓝 */
    private void updateTabStyle(boolean homeSelected) {
        int active = getColor(R.color.brand_primary);
        int normal = getColor(R.color.text_hint);
        ivTabHome.setColorFilter(homeSelected ? active : normal);
        tvTabHome.setTextColor(homeSelected ? active : normal);
        ivTabMine.setColorFilter(homeSelected ? normal : active);
        tvTabMine.setTextColor(homeSelected ? normal : active);
    }

    @Override
    public void onBackPressed() {
        if (mineFragment != null && mineFragment.isVisible()) {
            switchToHome();
            return;
        }
        super.onBackPressed();
    }
}
