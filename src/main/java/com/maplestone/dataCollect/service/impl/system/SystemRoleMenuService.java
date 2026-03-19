package com.maplestone.dataCollect.service.impl.system;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.maplestone.dataCollect.common.scurity.JwtContext;
import com.maplestone.dataCollect.common.utils.TreeUtils;
import com.maplestone.dataCollect.dao.BaseEntity;
import com.maplestone.dataCollect.dao.entity.system.SystemMenu;
import com.maplestone.dataCollect.dao.entity.system.SystemRoleMenu;
import com.maplestone.dataCollect.dao.entity.system.SystemUser;
import com.maplestone.dataCollect.dao.mapper.system.SystemRoleMenuMapper;
import com.maplestone.dataCollect.pojo.dto.BindDTO;
import com.maplestone.dataCollect.service.BaseIService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 表服务实现类
 */
@Service
public class SystemRoleMenuService extends BaseIService<SystemRoleMenuMapper, SystemRoleMenu> {

    @Autowired
    private SystemMenuService systemMenuService;

    /**
     * 获取所有的菜单 并匹配角色是否已包含菜单
     * 
     * @param id
     * @param type
     * @return
     */
    public Map<String, Object> listMenuByRole(String id, Integer type) {
        Map<String, Object> dataMap = new HashMap<>();
        // 系统所有菜单
        List<SystemMenu> systemMenus = systemMenuService.listMenuAll(type);
        JSONArray array = JSONArray.parseArray(JSON.toJSONString(systemMenus));
        JSONArray jsonArray = TreeUtils.listToTree(array, "id", "pid", "children");
        List<String> lastIds = new ArrayList<>();
        lastIds = TreeUtils.getTreeLastId(lastIds, jsonArray);

        // 这个角色已包含的菜单
        List<SystemMenu> roleMenus = systemMenuService.findMenuByRoleIdAndType(id, type);
        List<String> checkList = new ArrayList<>();
        if (roleMenus != null && roleMenus.size() > 0) {
            checkList = roleMenus.stream().map(BaseEntity::getId).collect(Collectors.toList());
        }
        checkList.retainAll(lastIds);
        dataMap.put("menuList", jsonArray);
        dataMap.put("checkList", checkList);
        return dataMap;
    }

    /**
     * 给角色添加菜单 先删除原来的再重新添加
     * 
     * @param roleMenuDTO
     * @return
     */
    public boolean addMenu(BindDTO roleMenuDTO, Integer type) {
        List<String> menuIds = roleMenuDTO.getIdList();
        String id = roleMenuDTO.getId();
        deleteRoleMenuByType(id, type);
        List<SystemRoleMenu> roleMenuList = new ArrayList<>();
        for (String menuId : menuIds) {
            SystemRoleMenu systemRoleMenu = new SystemRoleMenu();
            systemRoleMenu.setSystemRoleId(id);
            systemRoleMenu.setSystemMenuId(menuId);
            roleMenuList.add(systemRoleMenu);
        }
        SystemUser user = (SystemUser) JwtContext.getUser();
        return this.saveBatchActionBase(roleMenuList, user.getUserName());
    }

    /** 删除角色下的菜单 */
    public void deleteRoleMenuByType(String id, Integer type) {
        List<String> menuList = systemMenuService.findMenuIdByType(type);
        if (menuList == null || menuList.size() == 0) {
            return;
        }
        LambdaQueryWrapper<SystemRoleMenu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SystemRoleMenu::getSystemRoleId, id);
        queryWrapper.in(SystemRoleMenu::getSystemMenuId, menuList);
        this.remove(queryWrapper);
    }

}
