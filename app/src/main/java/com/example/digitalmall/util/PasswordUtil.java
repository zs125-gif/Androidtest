package com.example.digitalmall.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * 密码加盐哈希工具。
 * <p>
 * 存储格式：{@code v1$<saltHex>$<sha256Hex>}，其中 sha256 的输入是「盐 + 明文密码」。
 * 老版本（课程设计前期）直接存明文，登录时用 {@link #isLegacy(String)} 识别并就地升级为哈希。
 */
public final class PasswordUtil {

    private static final String PREFIX = "v1$";
    private static final int SALT_BYTES = 16;

    private PasswordUtil() {
    }

    /** 生成带随机盐的密码哈希串 */
    public static String hash(String password) {
        byte[] salt = new byte[SALT_BYTES];
        new SecureRandom().nextBytes(salt);
        String saltHex = toHex(salt);
        return PREFIX + saltHex + "$" + sha256(saltHex + password);
    }

    /** 校验明文密码是否与库中存的值匹配（同时兼容老的明文记录） */
    public static boolean matches(String password, String stored) {
        if (password == null || stored == null) {
            return false;
        }
        if (!stored.startsWith(PREFIX)) {
            // 老数据：明文直接比较
            return stored.equals(password);
        }
        int split = stored.indexOf('$', PREFIX.length());
        if (split < 0) {
            return false;
        }
        String saltHex = stored.substring(PREFIX.length(), split);
        String expected = stored.substring(split + 1);
        return constantTimeEquals(expected, sha256(saltHex + password));
    }

    /** 是否是尚未升级的明文密码记录 */
    public static boolean isLegacy(String stored) {
        return stored != null && !stored.startsWith(PREFIX);
    }

    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return toHex(digest.digest(input.getBytes("UTF-8")));
        } catch (NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            // 算法必然存在，编码也必然支持，这里兜底成空串，调用方按不匹配处理
            return "";
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            int v = b & 0xFF;
            if (v < 16) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(v));
        }
        return sb.toString();
    }

    /** 定长比较，避免因提前返回泄露时序信息 */
    private static boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        }
        int diff = 0;
        for (int i = 0; i < a.length(); i++) {
            diff |= a.charAt(i) ^ b.charAt(i);
        }
        return diff == 0;
    }
}
