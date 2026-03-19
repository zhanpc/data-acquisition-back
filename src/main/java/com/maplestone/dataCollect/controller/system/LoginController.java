package com.maplestone.dataCollect.controller.system;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.maplestone.dataCollect.common.constant.ApiConst;
import com.maplestone.dataCollect.common.constant.RedisConst;
import com.maplestone.dataCollect.common.scurity.JwtContext;
import com.maplestone.dataCollect.common.scurity.JwtUtil;
import com.maplestone.dataCollect.common.utils.DateUtils;
import com.maplestone.dataCollect.common.utils.MD5Utils;
import com.maplestone.dataCollect.common.utils.RedisUtil;
import com.maplestone.dataCollect.dao.entity.system.SystemUser;
import com.maplestone.dataCollect.pojo.RspVo;
import com.maplestone.dataCollect.pojo.dto.LoginDTO;
import com.maplestone.dataCollect.service.impl.system.SystemUserService;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * @description: 登录接口
 * @Author hmx
 * @CreateTime 2021-06-22 9:41
 */

@Slf4j
@Validated
@RequestMapping(ApiConst.PC + "/system")
@RestController
@Tag(name = "登录接口")
public class LoginController {

    @Autowired
    private SystemUserService systemUserService;

    /**
     * 用户登录接口
     *
     * @param loginDTO
     * @return
     */
    @Operation(summary = "用户登录接口")
    @Parameter(name = "loginDTO", description = "用户登录信息")
    @PostMapping("/login")
    public RspVo login(
            @Valid @RequestBody LoginDTO loginDTO) {
        String userName = loginDTO.getUserName();
        String md5Password = MD5Utils.encrypt(userName, loginDTO.getPassword());
        SystemUser systemUser = systemUserService.getUserByUserName(userName);
        if (systemUser == null) {
            return RspVo.getFailureResponseJoMsg("账号不存在");
        } else if (!systemUser.getPassword().equals(md5Password)) {
            return RspVo.getFailureResponseJoMsg("密码错误");
        }
        try {
            String token = JwtUtil.createToken(systemUser.getId());
            Map<String, Object> map = new HashMap<>();
            map.put("token", token);
            // 缓存到redis中
            Map<String, Object> tokenMap = new HashMap<>();
            tokenMap.put("id", systemUser.getId());
            tokenMap.put("time", DateUtils.getSystemTime());
            RedisUtil.set(RedisConst.TOKEN_KEY + token, tokenMap, RedisConst.TOKEN_EXPIRE);
            return RspVo.getSuccessResponseJoData(map);
        } catch (Exception e) {
            return RspVo.getFailureResponseJoMsg("用户名或密码错误");
        }
    }

    /**
     * 获取当前登录用户
     *
     * @return
     */
    @Operation(summary = "获取当前登录用户")
    @GetMapping("/userInfo")
    public RspVo findCurrentUser() {
        SystemUser user = (SystemUser) JwtContext.getUser();
        return RspVo.getSuccessResponseJoData(user);
    }

}
