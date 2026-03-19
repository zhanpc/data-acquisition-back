package com.maplestone.dataCollect.common.interceptor;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.springframework.stereotype.Component;

import com.maplestone.dataCollect.common.scurity.JwtContext;
import com.maplestone.dataCollect.dao.entity.system.SystemUser;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.Properties;

@Component
@Intercepts({ @Signature(type = Executor.class, method = "update", args = { MappedStatement.class, Object.class }) })
public class MybatisInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        SystemUser user = (SystemUser) JwtContext.getUser();
        String userName = user == null ? null : user.getUserName();
        // String userName=null;
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        // 注解中method的值
        String methodName = invocation.getMethod().getName();
        // sql类型
        SqlCommandType sqlCommandType = mappedStatement.getSqlCommandType();
        if ("update".equals(methodName)) {
            Date currentDate = new Date();
            // 对有要求的字段填值
            if (SqlCommandType.INSERT.equals(sqlCommandType)) {
                Object object = invocation.getArgs()[1];
                // Field[] fields = object.getClass().getDeclaredFields();
                // 因为需要注入的字段是父类的 所以此处获取父类属性
                Field[] fields = object.getClass().getSuperclass().getDeclaredFields();
                for (Field field : fields) {
                    if (field.getName().equals("createdTime")) {
                        field.setAccessible(true);
                        field.set(object, currentDate);
                    }
                    if (field.getName().equals("createdUser") && StringUtils.isNotBlank(userName)) {
                        field.setAccessible(true);
                        field.set(object, userName);
                    }
                    if (field.getName().equals("lastModifiedTime")) {
                        field.setAccessible(true);
                        field.set(object, currentDate);
                    }
                    if (field.getName().equals("lastModifiedUser") && StringUtils.isNotBlank(userName)) {
                        field.setAccessible(true);
                        field.set(object, userName);
                    }
                }
            } else if (SqlCommandType.UPDATE.equals(sqlCommandType)) {
                Object parameter = invocation.getArgs()[1];
                Field[] fields;
                if (parameter instanceof MapperMethod.ParamMap) {
                    MapperMethod.ParamMap<?> p = (MapperMethod.ParamMap<?>) parameter;
                    if (p.containsKey("et")) {
                        parameter = p.get("et");
                    } else {
                        parameter = p.get("param1");
                    }
                    if (parameter == null) {
                        return invocation.proceed();
                    }
                    // 因为需要注入的字段是父类的 所以此处获取父类属性
                    fields = parameter.getClass().getSuperclass().getDeclaredFields();
                } else {
                    // 因为需要注入的字段是父类的 所以此处获取父类属性
                    fields = parameter.getClass().getSuperclass().getDeclaredFields();
                }
                for (Field field : fields) {
                    if (field.getName().equals("lastModifiedTime")) {
                        field.setAccessible(true);
                        field.set(parameter, currentDate);
                    }
                    if (field.getName().equals("lastModifiedUser") && StringUtils.isNotBlank(userName)) {
                        field.setAccessible(true);
                        field.set(parameter, userName);
                    }
                }
            }
        }
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object o) {
        return Plugin.wrap(o, this);
    }

    @Override
    public void setProperties(Properties properties) {

    }
}
