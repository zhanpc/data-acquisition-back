package com.maplestone.dataCollect.common.exception;

import com.alibaba.fastjson.JSONObject;
import com.maplestone.dataCollect.common.aspect.RequestWrapper;
import com.maplestone.dataCollect.common.constant.HttpConst;
import com.maplestone.dataCollect.common.utils.DateUtils;
import com.maplestone.dataCollect.pojo.RspVo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ValidationException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.SignatureException;

/**
 * 全局异常捕获处理
 * 
 * @author hmx
 * @date 2019/06/25 14:00
 *
 */

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 所有验证框架异常捕获处理
     * 
     * @return
     */
    @ResponseBody
    @ExceptionHandler(value = { BindException.class, MethodArgumentNotValidException.class,
            ServletRequestBindingException.class, ValidationException.class })
    public RspVo validationExceptionHandler(HttpServletRequest request, Exception exception) {
        log.error("框架异常捕获处理:");
        String msg = "系统繁忙，请稍后重试...";
        BindingResult bindResult = null;
        if (exception instanceof BindException) {
            bindResult = ((BindException) exception).getBindingResult();
        } else if (exception instanceof MethodArgumentNotValidException) {
            bindResult = ((MethodArgumentNotValidException) exception).getBindingResult();
        } else if (exception instanceof ServletRequestBindingException) {
            msg = exception.getMessage();
        } else if (exception instanceof ValidationException) {
            msg = exception.getMessage();
        }
        if (bindResult != null && bindResult.hasErrors()) {
            msg = bindResult.getAllErrors().get(0).getDefaultMessage();
            if (msg.contains("NumberFormatException")) {
                msg = "参数类型错误！";
            }
        }
        log.error(msg);
        dealException(request, exception);
        return RspVo.getIntervalServerResponseJoMsg(msg);
    }

    /**
     * jwt 权限认证异常捕获
     * 
     * @param request
     * @param exception
     * @return
     */
    @ResponseBody
    @ExceptionHandler(value = { SignatureException.class })
    public RspVo authorizationException(HttpServletRequest request, SignatureException exception) {
        log.error("jwt权限认证异常:");
        dealException(request, exception);
        return RspVo.getFailureResponseJoMsg(exception.getMessage(), HttpConst.UNAUTHORIZED);
    }

    /**
     * 自定义异常的捕获
     * 
     * @param request
     * @param exception
     * @return
     */
    @ResponseBody
    @ExceptionHandler(value = BusinessException.class)
    public RspVo businessExceptionHandler(HttpServletRequest request, BusinessException exception) {
        log.error("发生业务异常:");
        dealException(request, exception);
        return RspVo.getIntervalServerResponseJoMsg(exception.getMessage());
    }

    /**
     * 未知的异常捕获处理
     * 
     * @param exception
     * @return
     */
    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public RspVo allUnknowExceptionHandler(HttpServletRequest request, Exception exception) {
        log.error("发生未知异常:");
        dealException(request, exception);
        return RspVo.getIntervalServerResponseJoMsg(exception.getMessage());
    }

    /**
     * 异常处理
     * 
     * @param request
     * @param exception
     */
    private void dealException(HttpServletRequest request, Exception exception) {
        String params = null;
        String body = null;
        try {
            RequestWrapper requestWrapper = new RequestWrapper(request);
            body = requestWrapper.getBody(requestWrapper);
            // GET中的请求参数或者地址栏携带的参数
            params = request.getParameterMap() == null ? "" : JSONObject.toJSONString(request.getParameterMap());
        } catch (IOException e) {
            log.error("{}", e);
        }
        // 日志打印
        String error = logError(request, params, body, exception);
        log.error(error);
    }

    /**
     * 打印异常信息和 请求信息
     * 
     * @param request
     * @param exception
     * @return
     */
    private String logError(HttpServletRequest request, String params, String body, Exception exception) {
        StringWriter sw = new StringWriter();
        sw.append(String.format("\nDate:{%s};\n", DateUtils.getSystemTime()));
        sw.append(String.format("url:{%s}产生错误;\n", request.getRequestURI()));
        sw.append(String.format("请求IP:{%s};\n", request.getRemoteAddr()));
        sw.append(String.format("type:{%s};\n", request.getMethod()));
        sw.append(String.format("请求参数:{%s};\n", params));
        sw.append(String.format("body:{%s};\n", body));
        exception.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

}
