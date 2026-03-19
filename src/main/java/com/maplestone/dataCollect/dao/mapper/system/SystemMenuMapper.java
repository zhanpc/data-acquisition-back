package com.maplestone.dataCollect.dao.mapper.system;

import com.github.yulichang.base.MPJBaseMapper;
import com.maplestone.dataCollect.dao.entity.system.SystemMenu;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 系统菜单 Mapper 接口
 * </p>
 *
 * @author hmx
 * @since 2021-09-23
 */
public interface SystemMenuMapper extends MPJBaseMapper<SystemMenu> {

    List<SystemMenu> listNavMenu(@Param("userId") String userId, @Param("type") Integer type);

    List<SystemMenu> findMenuByRoleIdAndType(@Param("roleId") String roleId, @Param("type") Integer type);

    List<SystemMenu> selectListAndRoleId();
}
