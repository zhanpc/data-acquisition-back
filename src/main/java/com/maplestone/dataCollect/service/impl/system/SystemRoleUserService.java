package com.maplestone.dataCollect.service.impl.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.maplestone.dataCollect.common.scurity.JwtContext;
import com.maplestone.dataCollect.dao.entity.system.SystemRoleUser;
import com.maplestone.dataCollect.dao.entity.system.SystemUser;
import com.maplestone.dataCollect.dao.mapper.system.SystemRoleUserMapper;
import com.maplestone.dataCollect.pojo.dto.BaseDTO;
import com.maplestone.dataCollect.pojo.dto.BindDTO;
import com.maplestone.dataCollect.service.BaseIService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 表服务实现类
 */
@Service
public class SystemRoleUserService extends BaseIService<SystemRoleUserMapper, SystemRoleUser> {

    @Autowired
    private SystemUserService systemUserService;
    @Autowired
    private SystemRoleUserMapper systemRoleUserMapper;

    /**
     * 获取角色已绑定的用户
     * 
     * @param baseDTO
     * @param id
     * @return
     */
    public IPage<SystemUser> listUserBind(BaseDTO baseDTO, String id) {
        IPage<SystemUser> systemUserList = systemUserService.findByRoleIdHasPage(baseDTO, id);
        return systemUserList;
    }

    /**
     * 获取角色没绑定的用户
     * 
     * @param baseDTO
     * @param id
     * @return
     */
    public IPage<SystemUser> listUserNotBind(BaseDTO baseDTO, String id) {
        IPage<SystemUser> systemUserList = systemUserService.findUserNoRoleHasPage(baseDTO, id);
        return systemUserList;
    }

    /**
     * 角色绑定用户
     * 
     * @param dto
     * @return
     */
    public boolean bindUser(BindDTO dto) {
        List<SystemRoleUser> roleUsers = new ArrayList<>();
        for (String userId : dto.getIdList()) {
            SystemRoleUser roleUser = new SystemRoleUser();
            roleUser.setSystemUserId(userId);
            roleUser.setSystemRoleId(dto.getId());
            roleUsers.add(roleUser);
        }
        SystemUser user = (SystemUser) JwtContext.getUser();
        return this.saveBatchActionBase(roleUsers, user.getUserName());
    }

    /**
     * 角色解绑用户
     * 
     * @param dto
     * @return
     */
    public boolean unbindUser(BindDTO dto) {
        List<String> userIdList = dto.getIdList();
        for (String userId : userIdList) {
            LambdaQueryWrapper<SystemRoleUser> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SystemRoleUser::getSystemUserId, userId);
            queryWrapper.eq(SystemRoleUser::getSystemRoleId, dto.getId());
            systemRoleUserMapper.delete(queryWrapper);
        }
        return true;
    }

    public boolean unbindAllUser(String roleId) {
        LambdaQueryWrapper<SystemRoleUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SystemRoleUser::getSystemRoleId, roleId);
        systemRoleUserMapper.delete(queryWrapper);
        return true;
    }

    /** 用户被删除自动解绑 */
    public void deleteByUser(String userId) {
        LambdaQueryWrapper<SystemRoleUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SystemRoleUser::getSystemUserId, userId);
        systemRoleUserMapper.delete(queryWrapper);
    }

}
