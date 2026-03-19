package com.maplestone.dataCollect.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @description: 前端路由菜单格式实体类
 * @Author hmx
 * @CreateTime 2021-06-25 12:43
 */

@Data
@Schema(name = "RouterMetaDTO", description = "前端路由菜单格式实体类")
public class RouterMetaDTO {
    @Schema(name = "title", description = "标题")
    private String title;
    @Schema(name = "icon", description = "图标")
    private String icon;
    @Schema(name = "noCache", description = "是否关闭")
    private Boolean noClosable;
    @Schema(name = "levelHidden", description = "是否隐藏")
    private Boolean levelHidden;
}
