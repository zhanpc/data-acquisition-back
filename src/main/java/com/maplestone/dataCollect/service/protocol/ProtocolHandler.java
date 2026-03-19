package com.maplestone.dataCollect.service.protocol;

import java.util.Map;

/**
 * 协议处理器统一接口
 * 各协议服务实现此接口，DataAcquisitionService 通过 Spring 自动发现所有实现
 */
public interface ProtocolHandler {

    /** 协议类型标识，与 StationConfig.protocol 字段对应 */
    String getProtocolType();

    /** 建立连接，params 包含协议所需的所有参数 */
    boolean connect(String connectionId, Integer stationId, Map<String, Object> params);

    /** 断开单个连接 */
    void disconnect(String connectionId);

    /** 断开所有连接（应用关闭时调用）*/
    void disconnectAll();
}
