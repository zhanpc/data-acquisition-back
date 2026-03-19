package com.maplestone.dataCollect.controller.system;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.maplestone.dataCollect.common.constant.ApiConst;
import com.maplestone.dataCollect.common.constant.SystemConst;
import com.maplestone.dataCollect.common.scurity.JwtContext;
import com.maplestone.dataCollect.dao.entity.system.SystemUser;
import com.maplestone.dataCollect.pojo.RspVo;
import com.maplestone.dataCollect.pojo.dto.RouterDTO;
import com.maplestone.dataCollect.service.impl.system.SystemMenuService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description: 菜单管理接口
 * @Author hmx
 * @CreateTime 2021-06-22 18:25
 */

@Slf4j
@Validated
@RequestMapping(ApiConst.PC + "/system/menu")
@RestController
@Tag(name = "菜单管理接口")
public class MenuController {

    @Autowired
    private SystemMenuService systemMenuService;

    /**
     * 获取当前登录用户权限下的菜单列表 为前端提供
     *
     * @param
     * @return
     */
    @Operation(summary = "获取当前登录用户权限下的菜单列表")
    @GetMapping("/listRouter")
    public RspVo listRouter() {
        SystemUser user = (SystemUser) JwtContext.getUser();
        List<RouterDTO> routerDTOList = systemMenuService.listRouter(user.getId(), SystemConst.PC_MENU);
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("list", routerDTOList);
        return RspVo.getSuccessResponseJoData(dataMap);
    }

}
