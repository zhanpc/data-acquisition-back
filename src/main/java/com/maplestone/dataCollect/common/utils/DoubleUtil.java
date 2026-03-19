package com.maplestone.dataCollect.common.utils;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * @author ZhangYaoWen
 * @date 2021年05月08日 15:29
 * @description
 */
public class DoubleUtil {

    /**
     * double加法
     * 
     * @param a
     * @param b
     * @return
     */
    public static Double add(Double a, Double b) {
        BigDecimal b1 = new BigDecimal(a);
        BigDecimal b2 = new BigDecimal(b);
        return b1.add(b2).doubleValue();
    }

    /**
     * double减法
     * 
     * @param a
     * @param b
     * @param setPrecision 设置精度
     * @return
     */
    public static Double subtraction(Double a, Double b, Integer setPrecision) {
        BigDecimal b1 = new BigDecimal(a);
        BigDecimal b2 = new BigDecimal(b);
        return b1.subtract(b2, new MathContext(setPrecision)).doubleValue();
    }

    /**
     * double乘法 结果保留两位小数
     * 
     * @param a
     * @param b
     * @return
     */
    public static Double multiplication(Double a, Double b) {
        BigDecimal b1 = new BigDecimal(a);
        BigDecimal b2 = new BigDecimal(b);
        return b1.multiply(b2).doubleValue();
    }

    /**
     * double除法
     * 
     * @param a
     * @param b
     * @param accurate 结果保留位数
     * @return
     */
    public static Double division(Double a, Double b, Integer accurate) {
        if (a == 0.0) {
            return 0.00;
        }
        if (accurate < 0) {
            throw new RuntimeException("精确度必须是正整数或零");
        }
        BigDecimal b1 = new BigDecimal(a);
        BigDecimal b2 = new BigDecimal(b);
        return b1.divide(b2, accurate, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * double截取小数位 四舍五入
     * 
     * @param a
     * @param scale accurate 小数点后留几位
     * @return
     */
    public static Double divisionRounding(Double a, Integer scale) {
        if (scale < 0) {
            throw new RuntimeException("精确度必须是正整数或零");
        }
        BigDecimal b = new BigDecimal(a);
        BigDecimal one = new BigDecimal("1");
        return b.divide(one, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static String subZero(Double d) {
        return BigDecimal.valueOf(d).stripTrailingZeros().toPlainString();
    }
}
