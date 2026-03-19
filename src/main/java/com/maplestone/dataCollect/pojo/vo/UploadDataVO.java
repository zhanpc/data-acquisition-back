package com.maplestone.dataCollect.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(name = "UploadDataVO", description = "旅客人员列表VO")
public class UploadDataVO {

    @Schema(name = "userId", description = "用户id")
    private String userId;

    @Schema(name = "reportTime", description = "上报时间")
    private Date reportTime;

    @Schema(name = "isActive", description = "是否活跃")
    private Boolean isActive;

    // @Schema(name = "inCounts", description = "进入区域次数")
    // private int inCounts;
    //
    // @Schema(name = "outCounts", description = "离开区域次数")
    // private int outCounts;

    @Schema(name = "stopArea", description = "停留区域")
    private String stopArea;

    @Schema(name = "stopDrawArea", description = "区域类型")
    private String stopDrawArea;

    @Schema(name = "status", description = "状态")
    private String status;

}
