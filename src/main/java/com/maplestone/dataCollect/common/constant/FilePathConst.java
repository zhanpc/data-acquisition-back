package com.maplestone.dataCollect.common.constant;

import java.io.File;

public interface FilePathConst {

    /**
     * 默认文件存储路径
     */
    String PATH_FILE = File.separator + "file" + File.separator;
    /**
     * 录音文件路径
     */
    String PATH_RECORD_FILE = File.separator + "file" + File.separator + "record" + File.separator;

    /**
     * 微信图片路径
     */
    String PATH_WEIXIN_IMGS = File.separator + "file" + File.separator + "weixin" + File.separator + "imgs"
            + File.separator;
    /**
     * 文档
     */
    String PATH_UPLOAD_DOCUMENT = File.separator + "file" + File.separator + "document" + File.separator;
    /**
     * 人员图片
     */
    String PATH_UPLOAD_PERSON = File.separator + "file" + File.separator + "person" + File.separator;
    /**
     * 预案文件
     */
    String PATH_UPLOAD_EMCYPLAN = File.separator + "file" + File.separator + "emcy" + File.separator + "document"
            + File.separator;

    /**
     * 文档
     */
    String PATH_UPLOAD_MANAGEMENT = File.separator + "file" + File.separator + "management" + File.separator;

    /**
     * 首页图片
     */
    String PATH_UPLOAD_HOME_PICTURE = File.separator + "file" + File.separator + "home" + File.separator + "picture"
            + File.separator;
    /**
     * 下载
     */
    String PATH_DOWNLOAD = File.separator + "download" + File.separator;
    /**
     * 上传
     */
    String PATH_UPLOADS = File.separator + "uploads" + File.separator;
    /**
     * 图片路径
     */
    String PATH_UPLOAD_UNIVERSAL_PICTURE = File.separator + "file" + File.separator + "picture" + File.separator
            + "universal" + File.separator;
    /**
     * 百度编辑器文件路径
     */
    String PATH_UPLOAD_UEDITOR_FILE = File.separator + "file" + File.separator + "ueditor" + File.separator;

    /**
     * 点（.）
     */
    String PATH_DOT = ".";

    String SUFFIX = "pdf";

}
