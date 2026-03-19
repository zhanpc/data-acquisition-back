package com.maplestone.dataCollect.dao.entity.system;

import com.baomidou.mybatisplus.annotation.TableField;
import com.maplestone.dataCollect.dao.BaseEntity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 系统用户
 * </p>
 *
 * @author hmx
 * @since 2021-09-23
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class SystemUser extends BaseEntity {

    /**
     * 用户名
     */
    @Schema(name = "userName", description = "用户名")
    private String userName;

    /**
     * 密码
     */
    @Schema(name = "password", description = "密码")
    private String password;

    /**
     * 昵称
     */
    @Schema(name = "nickName", description = "昵称")
    private String nickName;

    /**
     * 性别 (1: 男，2: 女)
     */
    @Schema(name = "sex", description = "性别 (1: 男，2: 女)")
    private Boolean sex;

    /**
     * 手机号码
     */
    @Schema(name = "phone", description = "手机号码")
    private String phone;

    /**
     * 邮箱
     */
    @Schema(name = "email", description = "邮箱")
    private String email;

    /**
     * 是否启用
     */
    @Schema(name = "enabled", description = "是否启用")
    private Boolean enabled;

    /**
     * 单位id
     */
    @Schema(name = "organizationUnitId", description = "单位id")
    private String organizationUnitId;

    /**
     * 单位名称
     */
    @Schema(name = "UnitName", description = "单位名称")
    @TableField(exist = false)
    private String UnitName;

}
