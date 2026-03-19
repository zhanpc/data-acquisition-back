package com.maplestone.dataCollect.dao.entity.system;

import com.baomidou.mybatisplus.annotation.TableField;
import com.maplestone.dataCollect.dao.BaseEntity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * <p>
 * 系统角色
 * </p>
 *
 * @author hmx
 * @since 2021-09-23
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(name = "SystemRole", description = "系统角色")
public class SystemRole extends BaseEntity {

    /**
     * 角色名称
     */
    @Schema(name = "roleName", description = "角色名称")
    private String roleName;

    /**
     * 描述
     */
    @Schema(name = "description", description = "描述")
    private String description;

    /**
     * 角色拥有得页面权限
     */
    @TableField(exist = false)
    private List<SystemMenu> jurisdiction;

}
