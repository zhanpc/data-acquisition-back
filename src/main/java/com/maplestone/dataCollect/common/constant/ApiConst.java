package com.maplestone.dataCollect.common.constant;

/**
 * @author HeYongXian
 */
public interface ApiConst {
    String PREFIX = "/api";
    String VERSION = "/v1";
    String PROJECT = "/dataCollect";
    String PC_MODE = "/pc";
    String WX_MODE = "/wx";

    String PC = PREFIX + PROJECT + VERSION + PC_MODE;
    String WX = PREFIX + PROJECT + VERSION + WX_MODE;

    String PC_TAG = "PC-";
    String WX_TAG = "WX-";
}
