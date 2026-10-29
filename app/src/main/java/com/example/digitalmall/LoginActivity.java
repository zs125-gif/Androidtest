package com.example.digitalmall;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.digitalmall.db.UserDao;
import com.example.digitalmall.model.User;
import com.example.digitalmall.util.SessionManager;
import com.example.digitalmall.util.UIUtil;

/**
 * 登录页：账号密码校验走 SQLite 的 users 表，登录态写入 SharedPreferences。
 */
public class LoginActivity extends AppCompatActivity {

    private static final int REQ_REGISTER = 0x3001;

    private EditText etUsername;
    private EditText etPassword;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);

        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ((TextView) findViewById(R.id.btn_login)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doLogin();
            }
        });

        findViewById(R.id.btn_register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(LoginActivity.this, RegisterActivity.class), REQ_REGISTER);
            }
        });

        String preset = SessionManager.getUsername(this);
        if (preset != null) {
            etUsername.setText(preset);
        }
    }

    private void doLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            UIUtil.toast(this, getString(R.string.err_username_empty));
            etUsername.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            UIUtil.toast(this, getString(R.string.err_password_empty));
            etPassword.requestFocus();
            return;
        }

        User user = UserDao.login(this, username, password);
        if (user == null) {
            // 账号不存在与密码错误给同一句提示，避免暴露账号是否注册过
            UIUtil.toast(this, getString(R.string.login_failed));
            return;
        }

        SessionManager.save(this, user);
        UIUtil.toast(this, getString(R.string.login_success));
        setResult(Activity.RESULT_OK);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_REGISTER && resultCode == Activity.RESULT_OK) {
            // 注册成功后已自动登录，直接把结果透传给个人中心
            setResult(Activity.RESULT_OK);
            finish();
        }
    }
}
