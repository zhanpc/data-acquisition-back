package com.maplestone.dataCollect.service.protocol;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据采集管理服务
 * 通过 Spring 自动发现所有 ProtocolHandler 实现，无需硬编码协议依赖
 */
@Slf4j
@Service
public class DataAcquisitionService {

    private final Map<String, ProtocolHandler> handlers;
    private final Map<String, String> connectionProtocolMap = new HashMap<>();

    public DataAcquisitionService(List<ProtocolHandler> handlerList) {
        this.handlers = handlerList.stream()
                .collect(Collectors.toMap(ProtocolHandler::getProtocolType, h -> h));
        log.info("已注册协议处理器: {}", this.handlers.keySet());
    }

    /**
     * 统一连接入口
     */
    public boolean connect(String connectionId, String protocolType, Integer stationId, Map<String, Object> params) {
        ProtocolHandler handler = handlers.get(protocolType);
        if (handler == null) {
            log.error("未找到协议处理器: {}", protocolType);
            return false;
        }
        boolean result = handler.connect(connectionId, stationId, params);
        if (result) {
            connectionProtocolMap.put(connectionId, protocolType);
        }
        return result;
    }

    /**
     * 断开指定连接
     */
    public void disconnect(String connectionId) {
        String protocolType = connectionProtocolMap.get(connectionId);
        if (protocolType == null) {
            log.warn("连接不存在: {}", connectionId);
            return;
        }
        ProtocolHandler handler = handlers.get(protocolType);
        if (handler != null) {
            handler.disconnect(connectionId);
        }
        connectionProtocolMap.remove(connectionId);
        log.info("连接已断开: {} ({})", connectionId, protocolType);
    }

    /**
     * 获取连接对应的协议类型
     */
    public String getConnectionProtocol(String connectionId) {
        return connectionProtocolMap.get(connectionId);
    }

    /**
     * 获取所有连接
     */
    public Map<String, String> getAllConnections() {
        return new HashMap<>(connectionProtocolMap);
    }

    /**
     * 应用关闭时断开所有连接
     */
    @PreDestroy
    public void cleanup() {
        log.info("正在关闭所有数采连接...");
        handlers.values().forEach(ProtocolHandler::disconnectAll);
        connectionProtocolMap.clear();
        log.info("所有数采连接已关闭");
    }
}
