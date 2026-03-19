package com.maplestone.dataCollect.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @description: 文件类
 * @Author hmx
 * @CreateTime 2021-06-25 14:09
 */

@Data
@Schema(name = "FileDocumentVO", description = "文件类")
public class FileDocumentVO {

    /**
     * 文件路径
     */
    @Schema(name = "filePath", description = "文件路径")
    private String filePath;

    /**
     * 原文件名称
     */
    @Schema(name = "originalName", description = "原文件名称")
    private String originalName;

    /**
     * 新文件名称
     */
    @Schema(name = "presentName", description = "新文件名称")
    private String presentName;

    /**
     * 大小
     */
    @Schema(name = "size", description = "大小")
    private String size;

    /**
     * 文件格式
     */
    @Schema(name = "fileFormat", description = "文件格式")
    private String fileFormat;

    /**
     * 1-是压缩包 0-否
     */
    @Schema(name = "isPackage", description = "1-是压缩包 0-否")
    private Integer isPackage;

}
