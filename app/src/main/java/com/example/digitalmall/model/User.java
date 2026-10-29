package com.example.digitalmall.model;

import java.io.Serializable;

/**
 * 用户实体，对应 users 表的一行。
 */
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String username;
    private String nickname;
    private String phone;

    public User() {
    }

    public User(String username, String nickname, String phone) {
        this.username = username;
        this.nickname = nickname;
        this.phone = phone;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    /** 头像占位文字：昵称首字 */
    public String getAvatarText() {
        String source = nickname == null || nickname.isEmpty() ? username : nickname;
        if (source == null || source.isEmpty()) {
            return "?";
        }
        return source.substring(0, 1).toUpperCase();
    }

    /** 手机号脱敏：138****8000 */
    public String getMaskedPhone() {
        if (phone == null || phone.length() < 7) {
            return phone == null ? "" : phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
