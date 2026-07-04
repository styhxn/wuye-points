package com.wuye.points.util;

import cn.hutool.crypto.digest.BCrypt;

/**
 * 密码工具类
 */
public class PasswordUtil {

    /**
     * 生成BCrypt哈希值
     */
    public static String hashPassword(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }

    /**
     * 验证密码
     */
    public static boolean checkPassword(String rawPassword, String hashedPassword) {
        return BCrypt.checkpw(rawPassword, hashedPassword);
    }

    /**
     * 主方法 - 用于生成密码哈希值
     * 用法: java PasswordUtil <password>
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("用法: java PasswordUtil <password>");
            System.out.println("示例: java PasswordUtil admin123456");
            return;
        }
        String rawPassword = args[0];
        String hashed = hashPassword(rawPassword);
        System.out.println("原始密码: " + rawPassword);
        System.out.println("BCrypt哈希: " + hashed);
        System.out.println("\nSQL语句:");
        System.out.println("UPDATE residents SET password = '" + hashed + "' WHERE phone = '管理员手机号';");
    }
}
