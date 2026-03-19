package com.maplestone.dataCollect.dao.entity.system;

import com.maplestone.dataCollect.dao.BaseEntity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 系统用户角色关联
 * </p>
 *
 * @author hmx
 * @since 2021-09-23
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(name = "SystemRoleUser", description = "系统用户角色关联")
public class SystemRoleUser extends BaseEntity {

    /**
     * 用户id
     */
    @Schema(name = "systemUserId", description = "用户id")
    private String systemUserId;

    /**
     * 角色id
     */
    @Schema(name = "systemRoleId", description = "角色id")
    private String systemRoleId;

}
