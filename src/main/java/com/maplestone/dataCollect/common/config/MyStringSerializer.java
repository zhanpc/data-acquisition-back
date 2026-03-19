package com.maplestone.dataCollect.common.config;

import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.serializer.SerializeWriter;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * @BelongsProject: steelbridge-vpa-back
 * @BelongsPackage: com.maplestone.steelbridge.common.config
 * @Author: zhanpc
 * @CreateTime: 2023-05-09 15:00
 * @Description: 解决swagger3无法打开得问题
 */
public class MyStringSerializer implements ObjectSerializer {
    public static final MyStringSerializer instance = new MyStringSerializer();

    @Override
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features)
            throws IOException {
        SerializeWriter out = serializer.getWriter();
        out.write(object.toString());
    }
}
