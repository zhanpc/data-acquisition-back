package com.maplestone.dataCollect.dao.entity.system;

import com.baomidou.mybatisplus.annotation.TableField;
import com.maplestone.dataCollect.dao.BaseEntity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 系统菜单
 * </p>
 *
 * @author hmx
 * @since 2021-09-23
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(name = "SystemMenu", description = "系统菜单")
public class SystemMenu extends BaseEntity {

    /**
     * 父级Id
     */
    @Schema(name = "pid", description = "父级Id")
    private String pid;

    /**
     * 菜单名称
     */
    @Schema(name = "name", description = "菜单名称")
    private String name;

    /**
     * 菜单路径
     */
    @Schema(name = "path", description = "菜单路径")
    private String path;

    /**
     * 图标
     */
    @Schema(name = "icon", description = "图标")
    private String icon;

    /**
     * 菜单类型 (1-PC菜单, 2-app菜单)
     */
    @Schema(name = "type", description = "菜单类型 (1-PC菜单, 2-app菜单)")
    private Integer type;

    /**
     * 是否启用(1-启用)
     */
    @Schema(name = "enabled", description = "是否启用(1-启用)")
    private Boolean enabled;

    /**
     * 排序 (数值越小越靠前)
     */
    @Schema(name = "sort", description = "排序 (数值越小越靠前)")
    private Integer sort;

    /**
     * 描述
     */
    @Schema(name = "description", description = "描述")
    private String description;

    /**
     * 前端路由名称
     */
    @Schema(name = "routerName", description = "前端路由名称")
    private String routerName;

    /**
     * 前端组件
     */
    @Schema(name = "component", description = "前端组件")
    private String component;

    /**
     * 前端跳转路径
     */
    @Schema(name = "redirect", description = "前端跳转路径")
    private String redirect;

    /**
     * 前端路由是否缓存
     */
    @Schema(name = "noCache", description = "前端路由是否缓存")
    private Boolean noClosable;

    /**
     * 前端..
     */
    @Schema(name = "affix", description = "前端..")
    private Boolean levelHidden;

    /**
     * 角色id
     */
    @TableField(exist = false)
    private String roleId;

}
