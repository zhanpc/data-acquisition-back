package com.maplestone.dataCollect.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @description: 通用关联实体类
 * @Author hmx
 * @CreateTime 2021-06-23 11:59
 */

@Data
@Schema(name = "BindDTO", description = "通用关联实体类")
public class BindDTO {

    @Schema(name = "id", description = "目标id")
    private String id;

    @Schema(name = "idList", description = "要绑定的id集合")
    private List<String> idList;

}
