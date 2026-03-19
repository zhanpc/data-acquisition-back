package com.maplestone.dataCollect.service.impl.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.maplestone.dataCollect.common.utils.MD5Utils;
import com.maplestone.dataCollect.common.utils.MybatisPlusUtils;
import com.maplestone.dataCollect.dao.entity.system.SystemRoleUser;
import com.maplestone.dataCollect.dao.entity.system.SystemUser;
import com.maplestone.dataCollect.dao.mapper.system.SystemUserMapper;
import com.maplestone.dataCollect.pojo.dto.BaseDTO;
import com.maplestone.dataCollect.pojo.dto.UpdatePasswordDTO;
import com.maplestone.dataCollect.service.BaseIService;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 表服务实现类
 */
@Service
public class SystemUserService extends BaseIService<SystemUserMapper, SystemUser> {

    @Autowired
    private SystemRoleUserService systemRoleUserService;
    @Resource
    private SystemUserMapper systemUserMapper;

    /**
     * 根据用户名称查询 用户信息
     *
     * @param userName
     * @return
     */
    public SystemUser getUserByUserName(String userName) {
        return getUserByUserName(userName, "1");
    }

    public SystemUser getUserByUserName(String userName, String enabled) {
        LambdaQueryWrapper<SystemUser> queryWrapper = new LambdaQueryWrapper();
        if (StringUtils.isNotBlank(enabled)) {
            queryWrapper.eq(SystemUser::getEnabled, enabled);
        }
        queryWrapper.eq(SystemUser::getUserName, userName);
        return systemUserMapper.selectOne(queryWrapper);
    }

    public int countUserByUserName(String userName) {
        SystemUser systemUser = getUserByUserName(userName, null);
        return systemUser == null ? 0 : 1;
    }

    /**
     * 分页查询用户信息列表
     *
     * @param baseDTO
     * @return
     */
    public IPage<SystemUser> listUserByPage(BaseDTO baseDTO) {
        return systemUserMapper.findUserHasPage(MybatisPlusUtils.getPage(baseDTO), baseDTO.getLike());
    }

    /**
     * 查出全部用户列表
     *
     * @return
     */
    public List<SystemUser> listAll() {
        LambdaQueryWrapper<SystemUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ne(SystemUser::getUserName, "superadmin");
        queryWrapper.orderByDesc(SystemUser::getCreatedTime);
        return this.list(queryWrapper);
    }

    /**
     * 修改用户的密码
     *
     * @param passwordDTO
     * @return
     */
    public boolean updateUserPwd(UpdatePasswordDTO passwordDTO) {
        SystemUser oldSystemUser = this.findById(passwordDTO.getId());
        String userName = oldSystemUser.getUserName();
        // 判断两次新密码是否一致
        String oneNewPassword = passwordDTO.getOneNewPassword();
        if (!oneNewPassword.equals(passwordDTO.getTwoNewPassword())) {
            throw new IllegalArgumentException("新密码输入不一致，请重新输入~");
        }
        String md5Password = MD5Utils.encrypt(userName, oneNewPassword);
        SystemUser systemUser = new SystemUser();
        systemUser.setId(passwordDTO.getId());
        systemUser.setPassword(md5Password);
        return this.updateActionBase(systemUser);
    }

    /**
     * 分页查询角色已关联用户
     */
    public IPage<SystemUser> findByRoleIdHasPage(BaseDTO baseDTO, String roleId) {
        MPJLambdaWrapper<SystemUser> mpjLambdaWrapper = new MPJLambdaWrapper<>();
        mpjLambdaWrapper.selectAll(SystemUser.class)
                .leftJoin(SystemRoleUser.class, SystemRoleUser::getSystemUserId, SystemUser::getId);
        mpjLambdaWrapper.eq(SystemRoleUser::getSystemRoleId, roleId);
        if (StringUtils.isNotEmpty(baseDTO.getLike())) {
            mpjLambdaWrapper.and(wrapper -> wrapper.like(SystemUser::getNickName, baseDTO.getLike())
                    .or().like(SystemUser::getUserName, baseDTO.getLike()));
        }
        mpjLambdaWrapper.last(" ORDER BY convert(nick_name using gbk) asc");
        IPage<SystemUser> page = baseMapper.selectJoinPage(MybatisPlusUtils.getPage(baseDTO), SystemUser.class,
                mpjLambdaWrapper);
        // systemUserMapper.findByRoleIdHasPage(MybatisPlusUtils.getPage(baseDTO),
        // roleId, baseDTO.getLike())
        return page;
    }

    /**
     * 分页查询未关联角色的用户
     */
    public IPage<SystemUser> findUserNoRoleHasPage(BaseDTO baseDTO, String roleId) {
        return systemUserMapper.findUserNoRoleHasPage(MybatisPlusUtils.getPage(baseDTO), roleId, baseDTO.getLike());
    }

    /**
     * 删除用户
     *
     * @param id
     * @return
     */
    public boolean deleteById(String id) {
        // 先去删除用户关联的角色
        systemRoleUserService.deleteByUser(id);
        // 删除用户
        return this.removeById(id);
    }

}
