package com.maplestone.dataCollect.controller.system;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.maplestone.dataCollect.common.constant.ApiConst;
import com.maplestone.dataCollect.common.constant.HttpConst;
import com.maplestone.dataCollect.common.scurity.JwtContext;
import com.maplestone.dataCollect.common.utils.MD5Utils;
import com.maplestone.dataCollect.dao.entity.system.SystemUser;
import com.maplestone.dataCollect.pojo.RspVo;
import com.maplestone.dataCollect.pojo.dto.BaseDTO;
import com.maplestone.dataCollect.pojo.dto.UpdatePasswordDTO;
import com.maplestone.dataCollect.service.impl.system.SystemUserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @description: 系统用户管理接口
 * @Author hmx
 * @CreateTime 2020-11-09 14:33
 */

@Slf4j
@Validated
@RequestMapping(ApiConst.PC + "/system/user")
@RestController
@Tag(name = "用户管理接口")
public class UserController {

    @Autowired
    private SystemUserService systemUserService;

    /**
     * 查询用户列表 分页
     *
     * @param baseDTO
     * @return
     */
    @Operation(summary = "查询用户列表 分页")
    @Parameter(name = "baseDTO", description = "分页信息")
    @PostMapping("/list")
    public RspVo listByPage(BaseDTO baseDTO) {
        IPage<SystemUser> systemUserIPage = systemUserService.listUserByPage(baseDTO);
        return RspVo.getSuccessResponseJoData(systemUserIPage);
    }

    /**
     * 查出全部用户列表 不分页
     *
     * @return
     */
    @Operation(summary = "查出全部用户列表 不分页")
    @PostMapping("/listAll")
    public RspVo listAll() {
        List<SystemUser> userList = systemUserService.listAll();
        return RspVo.getSuccessResponseJoData(userList);
    }

    /**
     * 根据id获取单个用户信息
     *
     * @param id
     * @return
     */
    @Operation(summary = "根据id获取单个用户信息")
    @Parameter(name = "id", description = "用户id", required = true)
    @GetMapping("/get")
    public RspVo getById(
            @NotBlank(message = "id not be null") @RequestParam String id) {
        SystemUser systemUser = systemUserService.findById(id);
        return RspVo.getSuccessResponseJoData(systemUser);
    }

    /**
     * 添加用户
     *
     * @param user
     * @return
     */
    @Operation(summary = "添加用户")
    @Parameter(name = "user", description = "用户信息")
    @PostMapping("/add")
    public RspVo add(@RequestBody SystemUser user) {
        Assert.hasText(user.getUserName(), "用户名不能为空~");
        Assert.hasText(user.getPassword(), "密码不能为空~");
        Assert.hasText(user.getNickName(), "姓名不能为空~");
        int count = systemUserService.countUserByUserName(user.getUserName());
        if (count > 0) {
            return RspVo.getFailureResponseJoMsg("账号重复,请重新输入~");
        }
        user.setPassword(MD5Utils.encrypt(user.getUserName(), user.getPassword()));
        boolean result = systemUserService.saveActionBase(user);
        return RspVo.getStatusJoMsg(result, HttpConst.ADD_SUCCESS, HttpConst.ADD_FAILED);
    }

    /**
     * 根据id删除用户
     *
     * @param id
     * @return
     */
    @Operation(summary = "根据id删除用户")
    @Parameter(name = "id", description = "用户id", required = true)
    @DeleteMapping("/del")
    public RspVo deleteById(
            @NotBlank(message = "id not be null") @RequestParam String id) {
        boolean result = systemUserService.deleteById(id);
        return RspVo.getStatusJoMsg(result, HttpConst.DELETE_SUCCESS, HttpConst.DELETE_FAILED);
    }

    /**
     * 根据id修改用户信息
     *
     * @param user
     * @return
     */
    @Operation(summary = "根据id修改用户信息")
    @Parameter(name = "user", description = "用户信息")
    @PostMapping("/update")
    public RspVo updateById(@RequestBody SystemUser user) {
        Assert.hasText(user.getId(), "id not be null");
        SystemUser oldSystemUser = systemUserService.findById(user.getId());
        // 当密码不为空的时候进行加密
        if (StringUtils.isNotBlank(user.getPassword())) {
            user.setPassword(MD5Utils.encrypt(oldSystemUser.getUserName(), user.getPassword()));
        }
        // 不允许修改用户名
        user.setUserName(null);
        boolean result = systemUserService.updateActionBase(user);
        return RspVo.getStatusJoMsg(result, HttpConst.UPDATE_SUCCESS, HttpConst.UPDATE_FAILED);
    }

    /**
     * 更新用户是否启用
     *
     * @param id
     * @param enabled
     * @return
     */
    @Operation(summary = "更新用户是否启用")
    @Parameter(name = "id", description = "用户id", required = true)
    @Parameter(name = "enabled", description = "是否启用", required = true)
    @PostMapping("/setEnabled")
    public RspVo setEnabled(
            @NotBlank(message = "id not be null") @RequestParam String id,
            @NotNull(message = "enabled not be null") @RequestParam Boolean enabled) {
        SystemUser systemUser = new SystemUser();
        systemUser.setId(id);
        systemUser.setEnabled(enabled);
        boolean result = systemUserService.updateActionBase(systemUser);
        return RspVo.getStatusJoMsg(result, HttpConst.UPDATE_SUCCESS, HttpConst.UPDATE_FAILED);
    }

    /**
     * 重置用户密码
     *
     * @param passwordDTO
     * @return
     */
    @Operation(summary = "重置用户密码")
    @Parameter(name = "passwordDTO", description = "用户信息")
    @PostMapping("/resetPwd")
    public RspVo resetPwd(
            @Valid @RequestBody UpdatePasswordDTO passwordDTO) {
        Assert.hasText(passwordDTO.getId(), "id not be null");
        boolean result = systemUserService.updateUserPwd(passwordDTO);
        return RspVo.getStatusJoMsg(result, HttpConst.UPDATE_SUCCESS, HttpConst.UPDATE_FAILED);
    }

    /**
     * 修改用户密码
     *
     * @param passwordDTO
     * @return
     */
    @Operation(summary = "修改用户密码")
    @Parameter(name = "passwordDTO", description = "用户信息")
    @PostMapping("/updatePwd")
    public RspVo updatePwd(
            @Valid @RequestBody UpdatePasswordDTO passwordDTO) {
        Assert.hasText(passwordDTO.getOldPassword(), "oldPassword not be null");
        SystemUser user = (SystemUser) JwtContext.getUser();
        passwordDTO.setId(user.getId());
        SystemUser oldSystemUser = systemUserService.findById(passwordDTO.getId());
        String userName = oldSystemUser.getUserName();
        // 加密并判断旧密码
        String md5Password = MD5Utils.encrypt(userName, passwordDTO.getOldPassword());
        if (!md5Password.equals(oldSystemUser.getPassword())) {
            throw new IllegalArgumentException("旧密码不正确，请重新输入~");
        }
        boolean result = systemUserService.updateUserPwd(passwordDTO);
        return RspVo.getStatusJoMsg(result, HttpConst.UPDATE_SUCCESS, HttpConst.UPDATE_FAILED);
    }

}
