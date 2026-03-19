package com.maplestone.dataCollect.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "uploadDataDTO", description = "数据上报实体类")
public class UploadDataDTO {

    private Integer rssi;

    private String uuid;

    private String major;

    private String minor;

    /**
     * 用户id
     */
    @Schema(name = "userId", description = "用户id")
    private String userId;

    /**
     * 上报时间戳
     */
    @Schema(name = "timestamp", description = "上报时间戳")
    private Long time;

    /**
     * 0 进入上报的数据 1 离开上报的数据
     */
    @Schema(name = "status", description = "0 进入上报的数据 1 离开上报的数据")
    private Integer type;
}
