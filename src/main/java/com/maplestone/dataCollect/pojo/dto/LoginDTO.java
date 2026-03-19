package com.maplestone.dataCollect.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

/**
 * @description: 登录信息
 * @Author hmx
 * @CreateTime 2021-06-22 9:50
 */

@Validated
@Data
@NoArgsConstructor
public class LoginDTO {

    @Schema(name = "code")
    private String code;

    @NotBlank(message = "用户名不能为空")
    @Schema(name = "userName", description = "用户名")
    private String userName;

    @NotBlank(message = "密码不能为空")
    @Schema(name = "password", description = "密码")
    private String password;
}
