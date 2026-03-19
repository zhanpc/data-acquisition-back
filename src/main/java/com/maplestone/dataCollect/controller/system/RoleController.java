package com.maplestone.dataCollect.controller.system;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.maplestone.dataCollect.common.constant.ApiConst;
import com.maplestone.dataCollect.common.constant.HttpConst;
import com.maplestone.dataCollect.common.constant.SystemConst;
import com.maplestone.dataCollect.dao.entity.system.SystemRole;
import com.maplestone.dataCollect.dao.entity.system.SystemUser;
import com.maplestone.dataCollect.pojo.RspVo;
import com.maplestone.dataCollect.pojo.dto.BaseDTO;
import com.maplestone.dataCollect.pojo.dto.BindDTO;
import com.maplestone.dataCollect.service.impl.system.SystemRoleMenuService;
import com.maplestone.dataCollect.service.impl.system.SystemRoleService;
import com.maplestone.dataCollect.service.impl.system.SystemRoleUserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

/**
 * @description: 用户角色管理接口
 * @Author hmx
 * @CreateTime 2021-06-22 19:14
 */

@Slf4j
@Validated
@RequestMapping(ApiConst.PC + "/system/role")
@RestController
@Tag(name = "角色管理接口")
public class RoleController {

    @Autowired
    private SystemRoleService systemRoleService;
    @Autowired
    private SystemRoleMenuService systemRoleMenuService;
    @Autowired
    private SystemRoleUserService systemRoleUserService;

    /**
     * 查询角色列表 分页
     *
     * @param baseDTO
     * @return
     */
    @Operation(summary = "查询角色列表 分页")
    @Parameter(name = "baseDTO", description = "分页参数")
    @PostMapping("/list")
    public RspVo listByPage(BaseDTO baseDTO) {
        IPage<SystemRole> systemRoleIPage = systemRoleService.listRoleByPage(baseDTO);
        return RspVo.getSuccessResponseJoData(systemRoleIPage);
    }

    /**
     * 查询角色列表不分页
     *
     * @return
     */
    @Operation(summary = "查询角色列表不分页")
    @GetMapping("/listAll")
    public RspVo listAll() {
        List<SystemRole> systemRoles = systemRoleService.listRoleAll();
        return RspVo.getSuccessResponseJoData(systemRoles);
    }

    /**
     * 根据id获取单个角色信息
     *
     * @param id
     * @return
     */
    @Operation(summary = "根据id获取单个角色信息")
    @Parameter(name = "id", description = "角色id", required = true)
    @GetMapping("/get")
    public RspVo getById(
            @NotBlank(message = "id not be null") @RequestParam String id) {
        SystemRole systemRole = systemRoleService.findById(id);
        return RspVo.getSuccessResponseJoData(systemRole);
    }

    /**
     * 添加角色
     *
     * @param systemRole
     * @return
     */
    @Operation(summary = "添加角色")
    @Parameter(name = "systemRole", description = "角色信息", required = true)
    @PostMapping("/add")
    public RspVo add(@RequestBody SystemRole systemRole) {
        Assert.hasText(systemRole.getRoleName(), "角色名称不能为空~");
        int count = systemRoleService.countRoleName(systemRole.getRoleName());
        if (count > 0) {
            return RspVo.getFailureResponseJoMsg("角色名称重复,请重新输入~");
        }
        boolean result = systemRoleService.saveActionBase(systemRole);
        return RspVo.getStatusJoMsg(result, HttpConst.ADD_SUCCESS, HttpConst.ADD_FAILED);
    }

    /**
     * 根据id删除角色
     *
     * @param id
     * @return
     */
    @Operation(summary = "根据id删除角色")
    @Parameter(name = "id", description = "角色id", required = true)
    @DeleteMapping("/del")
    public RspVo deleteById(
            @NotBlank(message = "id not be null") @RequestParam String id) {
        boolean result = systemRoleService.deleteById(id);
        return RspVo.getStatusJoMsg(result, HttpConst.DELETE_SUCCESS, HttpConst.DELETE_FAILED);
    }

    /**
     * 根据id修改角色信息
     *
     * @param systemRole
     * @return
     */
    @Operation(summary = "根据id修改角色信息")
    @Parameter(name = "systemRole", description = "角色信息")
    @PostMapping("/update")
    public RspVo updateById(@RequestBody SystemRole systemRole) {
        Assert.hasText(systemRole.getId(), "id not be null");
        SystemRole oldSystemRole = systemRoleService.findById(systemRole.getId());
        if (StringUtils.isNotBlank(systemRole.getRoleName())
                && !oldSystemRole.getRoleName().equals(systemRole.getRoleName())) {
            // 修改了名称
            int count = systemRoleService.countRoleName(systemRole.getRoleName());
            if (count > 0) {
                return RspVo.getFailureResponseJoMsg("角色名称重复,请重新输入~");
            }
        }
        boolean result = systemRoleService.updateActionBase(systemRole);
        return RspVo.getStatusJoMsg(result, HttpConst.UPDATE_SUCCESS, HttpConst.UPDATE_FAILED);
    }

    /**
     * 获取所有的菜单 并匹配角色是否已包含菜单
     *
     * @param id
     * @return
     */
    @Operation(summary = "获取所有的菜单 并匹配角色是否已包含菜单")
    @Parameter(name = "id", description = "角色id", required = true)
    @GetMapping("/listMenu")
    public RspVo listMenu(
            @NotBlank(message = "id not be null") @RequestParam String id) {
        Map<String, Object> dataMap = systemRoleMenuService.listMenuByRole(id, SystemConst.PC_MENU);
        return RspVo.getSuccessResponseJoData(dataMap);
    }

    /**
     * 给角色添加菜单
     *
     * @param roleMenuDTO
     * @return
     */
    @Operation(summary = "给角色添加菜单")
    @Parameter(name = "roleMenuDTO", description = "角色菜单信息")
    @PostMapping("/addMenu")
    public RspVo addMenu(
            @RequestBody BindDTO roleMenuDTO) {
        Assert.hasText(roleMenuDTO.getId(), "id not be null");
        boolean result = systemRoleMenuService.addMenu(roleMenuDTO, SystemConst.PC_MENU);
        return RspVo.getStatusJoMsg(result, HttpConst.ADD_SUCCESS, HttpConst.ADD_FAILED);
    }

    /**
     * 根据角色id-查询角色已绑定的用户-带分页
     *
     * @param baseDTO
     * @param id
     * @return
     */
    @Operation(summary = "根据角色id-查询角色已绑定的用户-带分页")
    @Parameter(name = "baseDTO", description = "分页信息")
    @Parameter(name = "id", description = "角色id")
    @PostMapping("/listUserBind")
    public RspVo listUserBind(BaseDTO baseDTO, @RequestParam String id) {
        IPage<SystemUser> systemUserIPage = systemRoleUserService.listUserBind(baseDTO, id);
        return RspVo.getSuccessResponseJoData(systemUserIPage);
    }

    /**
     * 根据角色id-查询角色未绑定的用户-带分页
     *
     * @param baseDTO
     * @param id
     * @return
     */
    @Operation(summary = "根据角色id-查询角色未绑定的用户-带分页")
    @Parameter(name = "baseDTO", description = "分页信息")
    @Parameter(name = "id", description = "角色id")
    @PostMapping("/listUserNotBind")
    public RspVo listUserNotBind(BaseDTO baseDTO, @RequestParam String id) {
        IPage<SystemUser> systemUserIPage = systemRoleUserService.listUserNotBind(baseDTO, id);
        return RspVo.getSuccessResponseJoData(systemUserIPage);
    }

    /**
     * 角色绑定用户
     *
     * @param dto
     * @return
     */
    @Operation(summary = "角色绑定用户")
    @Parameter(name = "dto", description = "绑定信息")
    @PostMapping("/bindUser")
    public RspVo bindUser(@RequestBody BindDTO dto) {
        Assert.hasText(dto.getId(), "id not be null");
        boolean result = systemRoleUserService.bindUser(dto);
        return RspVo.getStatusJoMsg(result, HttpConst.LOCK_SUCCESS, HttpConst.LOCK_FAILED);
    }

    /**
     * 角色解绑用户
     *
     * @param dto
     * @return
     */
    @Operation(summary = "角色解绑用户")
    @Parameter(name = "dto", description = "解绑信息")
    @PostMapping("/unbindUser")
    public RspVo unbindUser(@RequestBody BindDTO dto) {
        Assert.hasText(dto.getId(), "id not be null");
        boolean result = systemRoleUserService.unbindUser(dto);
        return RspVo.getStatusJoMsg(result, HttpConst.UNLOCK_SUCCESS, HttpConst.UNLOCK_FAILED);
    }

}
