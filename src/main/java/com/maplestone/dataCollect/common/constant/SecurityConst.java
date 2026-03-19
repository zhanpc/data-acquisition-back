package com.maplestone.dataCollect.common.constant;

public interface SecurityConst {

    // 自定义密钥
    String TOKEN_SECRET = "weldmap1234567";
    // 过期时间
    int TOKEN_EXPIRE = 7200;
    // 名称
    String TOKEN_HEADER = "user-token";

    String LOGIN_URL = "/login";

    String SUCCESS_URL = "/index";

    String UNAUTHORIZED_URL = "/403";

}
