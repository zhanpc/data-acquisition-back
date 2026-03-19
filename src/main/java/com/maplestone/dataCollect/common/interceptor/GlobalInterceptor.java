package com.maplestone.dataCollect.common.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.maplestone.dataCollect.common.aspect.RequestWrapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class GlobalInterceptor implements HandlerInterceptor {

    /**
     * 在请求处理之前进行调用（Controller方法调用之前）
     * 
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        return true;// 返回 true-拦截通过，false-拦截不通过
    }

    /**
     * 请求处理之后进行调用，但是在视图被渲染之前（Controller方法调用之后）
     * 
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            @Nullable ModelAndView modelAndView) throws Exception {
        RequestWrapper requestWrapper = new RequestWrapper(request);
        String body = requestWrapper.getBody(requestWrapper);
        // GET中的请求参数或者地址栏携带的参数
        String params = request.getParameterMap() == null ? "" : JSONObject.toJSONString(request.getParameterMap());
        String requestURI = requestWrapper.getRequestURI();
        log.info("requestURI:" + requestURI);
        log.info("请求参数:" + params + body);

        // GET中的请求参数或者地址栏携带的参数
        // String params=request.getParameterMap()==null?"":
        // JSONObject.toJSONString(request.getParameterMap());
        // String requestURI = request.getRequestURI();
        // log.info("requestURI:"+requestURI);
        // log.info("请求参数:"+params);
    }

    /**
     * 在整个请求结束之后被调用，也就是在DispatcherServlet 渲染了对应的视图之后执行（主要是用于进行资源清理工作）
     * 
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
            @Nullable Exception ex) throws Exception {
    }
}
