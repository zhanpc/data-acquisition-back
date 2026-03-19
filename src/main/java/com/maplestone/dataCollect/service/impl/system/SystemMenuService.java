package com.maplestone.dataCollect.service.impl.system;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.maplestone.dataCollect.common.utils.TreeUtils;
import com.maplestone.dataCollect.dao.BaseEntity;
import com.maplestone.dataCollect.dao.entity.system.SystemMenu;
import com.maplestone.dataCollect.dao.mapper.system.SystemMenuMapper;
import com.maplestone.dataCollect.pojo.dto.RouterDTO;
import com.maplestone.dataCollect.pojo.dto.RouterMetaDTO;
import com.maplestone.dataCollect.service.BaseIService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 表服务实现类
 */
@Service
public class SystemMenuService extends BaseIService<SystemMenuMapper, SystemMenu> {

    @Autowired
    private SystemMenuMapper systemMenuMapper;

    /**
     * 根据用户id 查询权限下的菜单路由列表
     * 
     * @param userId
     * @param type
     * @return
     */
    public List<RouterDTO> listRouter(String userId, Integer type) {
        List<SystemMenu> menuList = systemMenuMapper.listNavMenu(userId, type);
        if (menuList == null || menuList.size() == 0) {
            return new ArrayList<>();
        }
        // 将菜单列表转换成前端需要的格式
        List<RouterDTO> routerDTOList = menuList.stream().map(this::convertRouterDTO).collect(Collectors.toList());
        // 去转换成树型结构
        JSONArray array = JSONArray.parseArray(JSON.toJSONString(routerDTOList));
        JSONArray jsonArray = TreeUtils.listToTree(array, "id", "pid", "children");
        return JSONObject.parseArray(jsonArray.toJSONString(), RouterDTO.class);
    }

    /** menu转router */
    public RouterDTO convertRouterDTO(SystemMenu menu) {
        RouterDTO routerDTO = new RouterDTO();
        routerDTO.setPid(menu.getPid());
        routerDTO.setId(menu.getId());
        routerDTO.setPath(menu.getPath());
        routerDTO.setName(menu.getRouterName());
        routerDTO.setComponent(menu.getComponent());
        routerDTO.setRedirect(menu.getRedirect());

        RouterMetaDTO routerMetaDTO = new RouterMetaDTO();
        routerMetaDTO.setTitle(menu.getName());
        routerMetaDTO.setIcon(menu.getIcon());
        routerMetaDTO.setNoClosable(menu.getNoClosable());
        routerMetaDTO.setLevelHidden(menu.getLevelHidden());
        routerDTO.setMeta(routerMetaDTO);
        return routerDTO;
    }

    /** 直接查出系统所有菜单 */
    public List<SystemMenu> listMenuAll(Integer type) {
        LambdaQueryWrapper<SystemMenu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SystemMenu::getType, type);
        queryWrapper.eq(SystemMenu::getEnabled, 1);
        queryWrapper.orderByAsc(SystemMenu::getSort);
        List<SystemMenu> menuList = systemMenuMapper.selectList(queryWrapper);
        return menuList;
    }

    /** 根据roleId查询全部菜单 */
    public List<SystemMenu> findMenuByRoleIdAndType(String roleId, Integer type) {
        List<SystemMenu> menuList = systemMenuMapper.findMenuByRoleIdAndType(roleId, type);
        return menuList;
    }

    /** 查出所有菜单的id */
    public List<String> findMenuIdByType(Integer type) {
        List<SystemMenu> systemMenus = listMenuAll(type);
        if (systemMenus != null && systemMenus.size() > 0) {
            List<String> menuIds = systemMenus.stream().map(BaseEntity::getId).collect(Collectors.toList());
            return menuIds;
        }
        return null;
    }

    /**
     * 查出所有角色对应得菜单权限
     */
    public List<SystemMenu> selectListAndRoleId() {
        return systemMenuMapper.selectListAndRoleId();
    }

}
