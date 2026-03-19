package com.maplestone.dataCollect.common.filter;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.maplestone.dataCollect.common.aspect.RequestWrapper;
import com.maplestone.dataCollect.common.aspect.ResponseWrapper;

import java.io.IOException;

/**
 * 全局过滤器
 *
 * @author hmx on 2019/7/17 0017
 */

@Slf4j
public class GlobalFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        log.info("---- Processing of request input into trim ----");
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
        RequestWrapper requestWrapper = new RequestWrapper(httpServletRequest);
        ResponseWrapper responseWrapper = new ResponseWrapper(httpServletResponse);

        filterChain.doFilter(requestWrapper, responseWrapper);

        byte[] bytes = responseWrapper.getBytes();

        // 将数据 再写到 response 中
        servletResponse.getOutputStream().write(bytes);
        servletResponse.getOutputStream().flush();
        servletResponse.getOutputStream().close();
        // filterChain.doFilter(httpServletRequest, httpServletResponse);
    }

    @Override
    public void destroy() {

    }

}
