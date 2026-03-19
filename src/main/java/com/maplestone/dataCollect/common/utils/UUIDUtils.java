package com.maplestone.dataCollect.common.utils;

import java.util.UUID;

/**
 * @description:
 * @Author hmx
 * @CreateTime 2021-04-21 14:17
 */

public class UUIDUtils {

    public static String getUUID() {
        UUID uuid = UUID.randomUUID();
        String str = uuid.toString().replaceAll("-", "");
        return str.toUpperCase();
    }

    public static Integer getUUIDInOrderId() {
        Integer orderId = UUID.randomUUID().toString().hashCode();
        orderId = orderId < 0 ? -orderId : orderId; // String.hashCode() 值会为空
        return orderId;
    }
}
