package com.maplestone.dataCollect.service.impl.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.maplestone.dataCollect.common.constant.SystemConst;
import com.maplestone.dataCollect.common.utils.MybatisPlusUtils;
import com.maplestone.dataCollect.dao.entity.system.SystemMenu;
import com.maplestone.dataCollect.dao.entity.system.SystemRole;
import com.maplestone.dataCollect.dao.mapper.system.SystemRoleMapper;
import com.maplestone.dataCollect.pojo.dto.BaseDTO;
import com.maplestone.dataCollect.service.BaseIService;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 表服务实现类
 */
@Service
public class SystemRoleService extends BaseIService<SystemRoleMapper, SystemRole> {

    @Autowired
    private SystemRoleMenuService systemRoleMenuService;
    @Autowired
    private SystemRoleUserService systemRoleUserService;
    @Autowired
    private SystemRoleMapper systemRoleMapper;
    @Resource
    private SystemMenuService systemMenuService;

    /**
     * 分页查询角色列表
     * 
     * @param baseDTO
     * @return
     */
    public IPage<SystemRole> listRoleByPage(BaseDTO baseDTO) {
        // LambdaQueryWrapper<SystemRole> queryWrapper = new LambdaQueryWrapper<>();
        // queryWrapper.ne(SystemRole::getRoleName,"超级管理员");
        // queryWrapper.orderByDesc(SystemRole::getCreatedTime);
        // if (StringUtils.isNotBlank(baseDTO.getLike())){
        // queryWrapper.like(SystemRole::getRoleName, baseDTO.getLike());
        // }
        // return systemRoleMapper.selectPage(MybatisPlusUtils.getPage(baseDTO),
        // queryWrapper);
        LambdaQueryWrapper<SystemRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ne(SystemRole::getRoleName, "超级管理员");
        queryWrapper.orderByDesc(SystemRole::getCreatedTime);
        if (StringUtils.isNotBlank(baseDTO.getLike())) {
            queryWrapper.like(SystemRole::getRoleName, baseDTO.getLike());
        }
        IPage<SystemRole> systemRoleIPage = systemRoleMapper.selectPage(MybatisPlusUtils.getPage(baseDTO),
                queryWrapper);

        List<SystemMenu> menuList = systemMenuService.selectListAndRoleId();
        Map<String, List<SystemMenu>> collect = menuList.stream().collect(Collectors.groupingBy(SystemMenu::getRoleId));
        systemRoleIPage.getRecords().forEach(t -> {
            if (collect.containsKey(t.getId())) {
                t.setJurisdiction(collect.get(t.getId()));
            }
        });
        return systemRoleIPage;
    }

    /**
     * 查询所有的角色列表
     * 
     * @return
     */
    public List<SystemRole> listRoleAll() {
        LambdaQueryWrapper<SystemRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ne(SystemRole::getRoleName, "超级管理员");
        return this.list(queryWrapper);
    }

    /** 统计角色名称数量 */
    public int countRoleName(String roleName) {
        LambdaQueryWrapper<SystemRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SystemRole::getRoleName, roleName);
        return this.count(queryWrapper);
    }

    /**
     * 删除角色
     * 
     * @param id
     * @return
     */
    public boolean deleteById(String id) {
        // 先去删除关联的菜单
        systemRoleMenuService.deleteRoleMenuByType(id, SystemConst.PC_MENU);
        // 删除关联的用户
        systemRoleUserService.unbindAllUser(id);
        // 删除角色
        return this.removeById(id);
    }

}
