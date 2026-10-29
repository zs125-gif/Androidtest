package com.example.digitalmall;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.digitalmall.db.OrderDao;
import com.example.digitalmall.model.User;
import com.example.digitalmall.util.SessionManager;
import com.example.digitalmall.util.UIUtil;

import java.util.Locale;

/**
 * 个人中心 Fragment。
 * <p>
 * 登录态读 SharedPreferences（{@link SessionManager}），订单笔数查 SQLite（{@link OrderDao}），
 * 两个技术要求在这里汇合。
 */
public class MineFragment extends Fragment {

    public static final int REQ_LOGIN = 0x2001;

    private TextView tvAvatar;
    private TextView tvNickname;
    private TextView tvPhone;
    private TextView btnLogin;
    private TextView tvOrderCount;
    private TextView btnLogout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mine, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvAvatar = view.findViewById(R.id.tv_avatar);
        tvNickname = view.findViewById(R.id.tv_nickname);
        tvPhone = view.findViewById(R.id.tv_phone);
        btnLogin = view.findViewById(R.id.btn_login);
        tvOrderCount = view.findViewById(R.id.tv_order_count);
        btnLogout = view.findViewById(R.id.btn_logout);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(getActivity(), LoginActivity.class), REQ_LOGIN);
            }
        });

        view.findViewById(R.id.row_orders).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!SessionManager.isLogin(requireContext())) {
                    UIUtil.toast(requireContext(), getString(R.string.need_login));
                    startActivityForResult(new Intent(getActivity(), LoginActivity.class), REQ_LOGIN);
                    return;
                }
                startActivity(new Intent(getActivity(), OrderListActivity.class));
            }
        });

        view.findViewById(R.id.row_cart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 购物车归属具体店铺，这里回到首页让用户选店铺
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).switchToHome();
                    UIUtil.toast(requireContext(), "请选择店铺查看购物车");
                }
            }
        });

        view.findViewById(R.id.row_about).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(requireContext())
                        .setTitle(R.string.about)
                        .setMessage(R.string.about_content)
                        .setPositiveButton(R.string.confirm, null)
                        .show();
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(requireContext())
                        .setTitle(R.string.logout)
                        .setMessage(R.string.logout_confirm)
                        .setNegativeButton(R.string.cancel, null)
                        .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SessionManager.logout(requireContext());
                                // 退出登录只清登录态，SQLite 里的购物车与订单保留
                                UIUtil.toast(requireContext(), getString(R.string.logout_done));
                                refresh();
                            }
                        })
                        .show();
            }
        });

        refresh();
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    /** 根据登录态刷新头部与按钮显隐 */
    private void refresh() {
        if (!isAdded()) {
            return;
        }
        User user = SessionManager.currentUser(requireContext());
        if (user == null) {
            tvAvatar.setText("?");
            tvNickname.setText(R.string.not_login);
            // 手机屏窄，长文案会和右侧按钮抢位置被省略号截断，拆成主副两行
            tvPhone.setVisibility(View.VISIBLE);
            tvPhone.setText(R.string.not_login_sub);
            btnLogin.setVisibility(View.VISIBLE);
            btnLogout.setVisibility(View.GONE);
            tvOrderCount.setText(String.format(Locale.CHINA, "共 %d 笔", 0));
            return;
        }
        tvAvatar.setText(user.getAvatarText());
        tvNickname.setText(String.format(getString(R.string.welcome_back), user.getNickname()));
        String phone = user.getMaskedPhone();
        if (phone == null || phone.isEmpty()) {
            tvPhone.setVisibility(View.GONE);
        } else {
            tvPhone.setVisibility(View.VISIBLE);
            tvPhone.setText(phone);
        }
        btnLogin.setVisibility(View.GONE);
        btnLogout.setVisibility(View.VISIBLE);

        int orderCount = OrderDao.countByUser(requireContext(), user.getUsername());
        tvOrderCount.setText(String.format(Locale.CHINA, "共 %d 笔", orderCount));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_LOGIN && resultCode == android.app.Activity.RESULT_OK) {
            refresh();
        }
    }
}
