package com.maplestone.dataCollect.common.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

/**
 * @description: md5 加密
 * @Author hmx
 * @CreateTime 2021-06-23 11:59
 */
public class MD5Utils {

    public static String encrypt(String password) {
        return DigestUtils.md5DigestAsHex(password.getBytes());
    }

    public static String encrypt(String userName, String password) {
        if (StringUtils.isNotBlank(userName) && StringUtils.isNotBlank(password)) {
            return DigestUtils.md5DigestAsHex((userName + password).getBytes());
        }
        return "";
    }

    public static void main(String[] args) {
        String encrypt = encrypt("superadmin", "dwfs#2021");
        System.out.println(encrypt);
    }
}
