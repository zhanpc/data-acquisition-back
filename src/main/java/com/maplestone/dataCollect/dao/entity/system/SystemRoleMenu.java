package com.maplestone.dataCollect.dao.entity.system;

import com.maplestone.dataCollect.dao.BaseEntity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 系统角色菜单关联
 * </p>
 *
 * @author hmx
 * @since 2021-09-23
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(name = "SystemRoleMenu", description = "系统角色菜单关联")
public class SystemRoleMenu extends BaseEntity {

    /**
     * 角色id
     */
    @Schema(name = "systemRoleId", description = "角色id")
    private String systemRoleId;

    /**
     * 菜单id
     */
    @Schema(name = "systemMenuId", description = "菜单id")
    private String systemMenuId;

}
