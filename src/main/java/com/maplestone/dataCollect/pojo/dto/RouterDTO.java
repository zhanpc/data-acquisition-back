package com.maplestone.dataCollect.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @description: 前端路由菜单格式实体类
 * @Author hmx
 * @CreateTime 2021-06-25 12:42
 */

@Data
@NoArgsConstructor
@Schema(name = "RouterDTO", description = "前端路由菜单格式实体类")
public class RouterDTO {

    @Schema(name = "pid", description = "父级id")
    private String pid;
    @Schema(name = "id", description = "id")
    private String id;
    @Schema(name = "path", description = "路径")
    private String path;
    @Schema(name = "name", description = "名称")
    private String name;
    @Schema(name = "component", description = "组件")
    private String component;
    @Schema(name = "redirect", description = "重定向")
    private String redirect;
    @Schema(name = "hidden")
    private RouterMetaDTO meta;
    @Schema(name = "children", description = "子集")
    private List<RouterDTO> children = new ArrayList<>();
}
