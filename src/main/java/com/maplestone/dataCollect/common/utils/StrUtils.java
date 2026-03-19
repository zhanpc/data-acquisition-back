package com.maplestone.dataCollect.common.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @description: 字符串操作工具类
 * @Author hmx
 * @CreateTime 2021-06-22 9:29
 */

public class StrUtils {

    /**
     * 数字正则表达式
     */
    private static final String REGEX_INT = "^[-+]?\\d*$";
    /**
     * 浮点正则表达式
     */
    private static final String REGEX_DOUBLE = "^[-+]?\\d*[.]\\d*$";

    private static final String REGEX_CHINESE = "[\u4e00-\u9fa5]";

    private static Pattern linePattern = Pattern.compile("_(\\w)");

    private static Pattern humpPattern = Pattern.compile("[A-Z]");

    /**
     * 截取编码中有含义部分
     * 
     * @param code
     * @return
     */
    public static String getLikeCode(String code) {
        String[] codes = code.split("\\.");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(codes[0]);
        int m = 0;
        for (int i = codes.length - 1; i > 0; i--) {
            if ("00".equals(codes[i])) {
                m++;
            } else {
                break;
            }
        }
        for (int i = 1; i < codes.length - m; i++) {
            stringBuilder.append(".");
            if (!isInteger(codes[i])) {
                stringBuilder.append(codes[i].replace("0", ""));
            } else {
                stringBuilder.append(codes[i]);
            }
        }
        String result = stringBuilder.toString();
        if (result.endsWith("-00")) {
            result = result.replace("00", "");
        }
        return result;
    }

    /**
     * 判断字符串是否为数字
     */
    public static boolean isInteger(String value) {
        if (StringUtils.isBlank(value)) {
            return false;
        }
        return Pattern.compile(REGEX_INT).matcher(value).matches();
    }

    /**
     * 判断字符串是否为浮点
     */
    public static boolean isDouble(String value) {
        if (StringUtils.isBlank(value)) {
            return false;
        }
        return Pattern.compile(REGEX_DOUBLE).matcher(value).matches();
    }

    /**
     * 判断字符串中是否包含中文
     */
    public static boolean isContainChinese(String value) {
        if (StringUtils.isBlank(value)) {
            return false;
        }
        return Pattern.compile(REGEX_CHINESE).matcher(value).find();
    }

    /**
     * 校验String是否全是中文
     */
    public static boolean isChinese(String name) {
        boolean res = true;
        char[] cTemp = name.toCharArray();
        for (int i = 0; i < name.length(); i++) {
            if (isChinese(cTemp[i])) {
                res = false;
                break;
            }
        }
        return res;
    }

    /**
     * 判定输入的是否是汉字
     */
    public static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        return ub != Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                && ub != Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                && ub != Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                && ub != Character.UnicodeBlock.GENERAL_PUNCTUATION
                && ub != Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                && ub != Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS;
    }

    /**
     * 过滤掉中文
     */
    public static String filterChinese(String str) {
        String result = str;
        if (isContainChinese(str)) {
            StringBuilder sb = new StringBuilder();
            char[] charArray = str.toCharArray();
            for (char c : charArray) {
                if (isChinese(c)) {
                    sb.append(c);
                }
            }
            result = sb.toString();
        }
        return result;
    }

    /** 下划线转驼峰 */
    public static String lineToHump(String str) {
        str = str.toLowerCase();
        Matcher matcher = linePattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(1).toUpperCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /** 驼峰转下划线 */
    public static String humpToLine(String str) {
        Matcher matcher = humpPattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "_" + matcher.group(0).toLowerCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /** 字符串首字母转大写 */
    public static String captureName(String str) {
        // 进行字母的ascii编码前移，效率要高于截取字符串进行转换的操作
        char[] cs = str.toCharArray();
        cs[0] -= 32;
        return String.valueOf(cs);
    }

    /**
     * 生成编号 前缀+当前时间戳+随机几位编码
     * prefix 前缀
     * length 随机几位编码
     * 
     * @return
     */
    public static String genCode(String prefix, int length) {
        // 到毫秒的时间戳
        String currTime = DateUtils.dateToString(new Date(), "yyMMddHHmmssSSS");
        // 随机数
        String strRandom = buildRandom(length) + "";

        // 自定义 前缀+时间戳+随机数
        String code = prefix + currTime + strRandom;
        return code;
    }

    /**
     * 取出一个指定长度大小的随机正整数.
     * 
     * @param length int 设定所取出随机数的长度。length小于11
     * @return int 返回生成的随机数。
     */
    public static int buildRandom(int length) {
        int num = 1;
        double random = Math.random();
        if (random < 0.1) {
            random = random + 0.1;
        }
        for (int i = 0; i < length; i++) {
            num = num * 10;
        }
        return (int) ((random * num));
    }

    /**
     * 数字前面补零
     * 
     * @param number
     * @param length
     * @return
     */
    public static String addZero(int number, int length) {
        int num = length - (number + "").length();
        String str = number + "";
        for (int i = num; i > 0; i--) {
            str = "0" + str;
        }
        return str;
    }

}
