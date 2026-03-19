package com.maplestone.dataCollect.common.constant;

/**
 * @author hmx
 * @Description:
 * @date 2021/10/28 10:46
 */

public interface RedisConst {

    // 项目目录
    String FOLDER = "weldmap:";
    // token存放目录
    String TOKEN_KEY = FOLDER + "token:";

    Long TOKEN_EXPIRE = 7800L;

    String SCHEDULE_VIEW = FOLDER + "scheduleView";
    String PREVIEW = FOLDER + "preview";
}
