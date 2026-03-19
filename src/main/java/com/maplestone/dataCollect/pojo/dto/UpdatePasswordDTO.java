package com.maplestone.dataCollect.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

/**
 * @description:
 * @Author ChenLei
 * @CreateTime 2020-11-09 15:50
 */

@Validated
@Data
public class UpdatePasswordDTO {

    /**
     * 用户id
     */
    @Schema(name = "id", description = "用户id")
    private String id;

    /**
     * 旧密码
     */
    // @NotBlank(message = "旧密码不能为空")
    @Schema(name = "oldPassword", description = "旧密码")
    private String oldPassword;

    /**
     * 第一次新密码
     */
    @NotBlank(message = "新密码不能为空")
    @Schema(name = "oneNewPassword", description = "新密码")
    private String oneNewPassword;

    /**
     * 第二次新密码
     */
    @NotBlank(message = "确认新密码不能为空")
    @Schema(name = "twoNewPassword", description = "确认新密码")
    private String twoNewPassword;

}
