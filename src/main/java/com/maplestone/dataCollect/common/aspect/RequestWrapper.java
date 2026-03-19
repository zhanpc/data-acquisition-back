package com.maplestone.dataCollect.common.aspect;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StreamUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

/**
 * 请求读取
 *
 * @author hmx on 2019/7/17 0017
 */
@Slf4j
public class RequestWrapper extends HttpServletRequestWrapper {

    // 用于将流保存下来
    private byte[] requestBody;
    private String body;
    /** 保存处理后的参数 所有参数的Map集合 */
    private Map<String, String[]> parameterMap = new HashMap<String, String[]>();

    public RequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        this.parameterMap.putAll(request.getParameterMap());
        // 转换去空格
        this.modifyParameterValues();
        requestBody = StreamUtils.copyToByteArray(request.getInputStream());
    }

    public String getBody(HttpServletRequest requestWrapper) {
        StringBuffer sb = new StringBuffer();
        InputStream is = null;
        try {
            is = requestWrapper.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, Charset.forName("UTF-8"));
            BufferedReader br = new BufferedReader(isr);
            String s = "";
            while ((s = br.readLine()) != null) {
                sb.append(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        body = sb.toString();
        return body;
    }

    /**
     * 将parameter的值去除空格后重写回去
     */
    public void modifyParameterValues() {
        Set<String> set = parameterMap.keySet();
        Iterator<String> it = set.iterator();
        while (it.hasNext()) {
            String key = it.next();
            String[] values = parameterMap.get(key);
            values[0] = values[0].trim();
            if ("null".equals(values[0]) || "undefined".equals(values[0])) {
                values[0] = "";
            }
            parameterMap.put(key, values);
        }
    }

    // 重写几个HttpServletRequestWrapper中的方法
    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(this.getInputStream()));
    }

    @Override
    public ServletInputStream getInputStream() {

        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(requestBody);
        ServletInputStream servletInputStream = new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
            }

            @Override
            public int read() throws IOException {
                return byteArrayInputStream.read();
            }
        };
        return servletInputStream;

    }

    /**
     * 获取所有参数名
     *
     * @return 返回所有参数名
     */
    @Override
    public Enumeration<String> getParameterNames() {
        Vector<String> vector = new Vector<String>(parameterMap.keySet());
        return vector.elements();
    }

    /**
     * 获取指定参数名的值，如果有重复的参数名，则返回第一个的值 接收一般变量 ，如text类型
     *
     * @param name
     *             指定参数名
     * @return 指定参数名的值
     */
    @Override
    public String getParameter(String name) {
        String[] results = parameterMap.get(name);
        if (results == null || results.length <= 0) {
            return null;
        }
        return results[0];
    }

    /**
     * 获取指定参数名的所有值的数组，如：checkbox的所有数据
     * 接收数组变量 ，如checkobx类型
     */
    @Override
    public String[] getParameterValues(String name) {
        String[] results = parameterMap.get(name);
        if (results == null || results.length <= 0) {
            return null;
        }
        return results;

    }

}
