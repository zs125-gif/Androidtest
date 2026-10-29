package com.example.digitalmall;

import android.app.Activity;
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
 * 注册页：校验通过后写入 users 表，并直接完成登录（写 SharedPreferences）。
 */
public class RegisterActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etPhone;
    private EditText etNickname;
    private EditText etPassword;
    private EditText etPassword2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etUsername = findViewById(R.id.et_username);
        etPhone = findViewById(R.id.et_phone);
        etNickname = findViewById(R.id.et_nickname);
        etPassword = findViewById(R.id.et_password);
        etPassword2 = findViewById(R.id.et_password2);

        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ((TextView) findViewById(R.id.btn_register)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doRegister();
            }
        });

        findViewById(R.id.btn_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 回到登录页：直接关掉自己，让上一个 LoginActivity 露出
                finish();
            }
        });
    }

    private void doRegister() {
        String username = etUsername.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String nickname = etNickname.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String password2 = etPassword2.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            UIUtil.toast(this, getString(R.string.err_username_empty));
            etUsername.requestFocus();
            return;
        }
        if (username.length() < 3) {
            UIUtil.toast(this, getString(R.string.err_username_short));
            etUsername.requestFocus();
            return;
        }
        if (phone.length() != 11 || !TextUtils.isDigitsOnly(phone)) {
            UIUtil.toast(this, getString(R.string.err_phone_invalid));
            etPhone.requestFocus();
            return;
        }
        if (password.length() < 6) {
            UIUtil.toast(this, getString(R.string.err_password_short));
            etPassword.requestFocus();
            return;
        }
        if (!password.equals(password2)) {
            UIUtil.toast(this, getString(R.string.err_password_diff));
            etPassword2.requestFocus();
            return;
        }
        if (UserDao.exists(this, username)) {
            UIUtil.toast(this, getString(R.string.err_user_exists));
            etUsername.requestFocus();
            return;
        }

        String finalNickname = TextUtils.isEmpty(nickname) ? username : nickname;
        long id = UserDao.register(this, username, password, phone, finalNickname);
        if (id == -1) {
            UIUtil.toast(this, getString(R.string.register_failed));
            return;
        }

        // 注册成功直接登录，省掉用户再输一次密码
        SessionManager.save(this, new User(username, finalNickname, phone));
        UIUtil.toast(this, getString(R.string.register_success));
        setResult(Activity.RESULT_OK);
        finish();
    }
}
