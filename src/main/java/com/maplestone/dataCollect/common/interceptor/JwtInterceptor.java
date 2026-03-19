package com.maplestone.dataCollect.common.interceptor;

import com.alibaba.fastjson.JSON;
import com.maplestone.dataCollect.common.constant.RedisConst;
import com.maplestone.dataCollect.common.constant.SecurityConst;
import com.maplestone.dataCollect.common.scurity.JwtContext;
import com.maplestone.dataCollect.common.scurity.JwtUtil;
import com.maplestone.dataCollect.common.utils.DateUtils;
import com.maplestone.dataCollect.common.utils.HttpUtils;
import com.maplestone.dataCollect.common.utils.RedisUtil;
import com.maplestone.dataCollect.controller.system.LoginController;
import com.maplestone.dataCollect.dao.entity.system.SystemUser;
import com.maplestone.dataCollect.pojo.RspVo;
import com.maplestone.dataCollect.pojo.dto.LoginDTO;
import com.maplestone.dataCollect.service.impl.system.SystemUserService;

import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.SignatureException;
import java.util.*;

/**
 * @description: jwt认证拦截
 * @Author hmx
 * @CreateTime 2021-06-25 9:47
 */

@Slf4j
@Component
public class JwtInterceptor implements HandlerInterceptor {

    private String anonUrl = "/error,/doc.html,/swagger-resources,/login" +
            ",/api/dataCollect/v1/pc/system/login" +
            ",/api/dataCollect/v1/pc/modelComponent/getElementInfo" +
            ",/api/dataCollect/v1/pc/person/getUserLocation" +
            ",/api/dataCollect/v1/pc/uploadData/addUploadData" +
            ",/api/dataCollect/v1/pc/uploadData/addUploadDataList" +
            ",/api/dataCollect/v1/pc/drawArea/getDrawnAreaCoordinates" +
            ",/api/dataCollect/v1/pc/bigScreen/getAreaUserList" +
            ",/api/dataCollect/v1/pc/bigScreen/getDashboardData" +
            ",/api/dataCollect/v1/pc/person/getUserLocationHistory" +
            ",/api/dataCollect/v1/pc/paramConf/getDataFrequency" +
            ",/api/dataCollect/v1/pc/dataMng/getDataBus" +
            ",/api/dataCollect/v1/pc/dataTypeData/getData" +
            ",/" +
            ",/simpleWord" +
            ",/csrf";
    private String filterUrl = "/test,/download,/bimface,/api-docs,/swagger-ui,/simpleWord";

    @Autowired
    private SystemUserService systemUserService;
    @Autowired
    private LoginController loginController;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws SignatureException {
        String uri = request.getRequestURI();
        // 包含以下路径的都不拦截
        String[] anonUrls = anonUrl.split(",");
        List<String> anonUrlList = Arrays.asList(anonUrls);
        if (anonUrlList.contains(uri)) {
            return true;
        }
        List<String> filterUris = Arrays.asList(filterUrl.split(","));
        for (String filterUri : filterUris) {
            if (uri.contains(filterUri)) {
                return true;
            }
        }

        // 获取token
        String token = request.getHeader(SecurityConst.TOKEN_HEADER);
        if (StringUtils.isBlank(token)) {
            token = request.getParameter(SecurityConst.TOKEN_HEADER);
        }
        if (StringUtils.isBlank(token)) {
            // 可能不是直接添加的token参数
            token = request.getHeader("Authorization");
            if (StringUtils.isNotBlank(token)) {
                token = token.replace("Bearer ", "");
            }
        }
        if (StringUtils.isBlank(token)) {
            // 最后从body中尝试获取
            try {
                String body = HttpUtils.getBody(request);
                Map<String, String> map = JSON.parseObject(body, Map.class);
                token = map.get(SecurityConst.TOKEN_HEADER);
            } catch (Exception e) {
                log.error("{}", e);
            }
        }
        if (StringUtils.isBlank(token)) {
            throw new SignatureException(SecurityConst.TOKEN_HEADER + "不能为空");
            // 2025/03/06 免登录 如果没有token后端自己塞
            // LoginDTO loginDTO = new LoginDTO();
            // loginDTO.setUserName("admin");
            // loginDTO.setPassword("123456");
            // RspVo rspVo = loginController.login(loginDTO);
            // Map<String,Object> data = (Map)rspVo.getData();
            // token = data.get("token").toString();
        }
        String id = null;
        // 判断token是否超时
        Claims claims = JwtUtil.getTokenClaim(token);
        if (null == claims || JwtUtil.isTokenExpired(claims.getExpiration())) {
            if (RedisUtil.isEmpty(RedisConst.TOKEN_KEY + token)) {
                throw new SignatureException("登录过期，请重新登录");
            }
            Map<String, Object> tokenMap = (Map<String, Object>) RedisUtil.get(RedisConst.TOKEN_KEY + token);
            String time = tokenMap.get("time").toString();
            long secDifference = DateUtils.getSecDifference(new Date(),
                    DateUtils.stringToDate(time, "yyyy-MM-dd HH:mm:ss"));
            if (secDifference > SecurityConst.TOKEN_EXPIRE) {
                throw new SignatureException("登录过期，请重新登录");
            }
            id = tokenMap.get("id").toString();
        } else {
            id = claims.getSubject();
        }
        if (StringUtils.isNotBlank(id)) {
            // 延长过期时间
            Map<String, Object> tokenMap = new HashMap<>();
            tokenMap.put("id", id);
            tokenMap.put("time", DateUtils.getSystemTime());
            RedisUtil.set(RedisConst.TOKEN_KEY + token, tokenMap, RedisConst.TOKEN_EXPIRE);
        }

        // 去查找用户信息 存放在localThread内
        SystemUser byId = systemUserService.getById(id);
        if (byId == null) {
            throw new SignatureException("身份信息有误，请重新登录");
        }
        JwtContext.setUser(byId);
        return true;
    }

}
