package com.maplestone.dataCollect.dao.mapper.system;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.yulichang.base.MPJBaseMapper;
import com.maplestone.dataCollect.dao.entity.system.SystemUser;

import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 系统用户 Mapper 接口
 * </p>
 *
 * @author hmx
 * @since 2021-09-23
 */
public interface SystemUserMapper extends MPJBaseMapper<SystemUser> {

    IPage<SystemUser> findByRoleIdHasPage(IPage<SystemUser> page, @Param("roleId") String roleId,
            @Param("like") String like);

    IPage<SystemUser> findUserNoRoleHasPage(IPage<SystemUser> page, @Param("roleId") String roleId,
            @Param("like") String like);

    IPage<SystemUser> findUserHasPage(IPage<SystemUser> page, @Param("like") String like);

}
