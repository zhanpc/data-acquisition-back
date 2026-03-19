package com.maplestone.dataCollect.service.protocol;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maplestone.dataCollect.cache.ConfigCache;
import com.maplestone.dataCollect.dao.entity.StationConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 应用启动时自动建立所有场站连接
 * Order=2，在 ConfigService(Order=1) 加载配置后执行
 */
@Slf4j
@Component
@Order(2)
public class ConnectionInitializer implements CommandLineRunner {

    @Autowired
    private ConfigCache configCache;

    @Autowired
    private DataAcquisitionService dataAcquisitionService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void run(String... args) throws Exception {
        log.info("开始自动建立场站连接...");
        Map<Integer, StationConfig> stationConfigs = configCache.getAllStationConfigs();

        for (StationConfig config : stationConfigs.values()) {
            String connectionId = "station_" + config.getStationId();
            String protocol = config.getProtocol();
            Map<String, Object> params = buildParams(config);

            boolean success = dataAcquisitionService.connect(connectionId, protocol, config.getStationId(), params);
            if (success) {
                log.info("场站连接成功: stationId={}, protocol={}", config.getStationId(), protocol);
            } else {
                log.warn("场站连接失败: stationId={}, protocol={}", config.getStationId(), protocol);
            }
        }

        log.info("场站连接初始化完成，共处理 {} 个场站", stationConfigs.size());
    }

    private Map<String, Object> buildParams(StationConfig config) {
        String protocol = config.getProtocol();

        if ("MODBUS_RTU".equals(protocol)) {
            if (config.getExtraParams() != null && !config.getExtraParams().isEmpty()) {
                try {
                    return objectMapper.readValue(config.getExtraParams(), new TypeReference<Map<String, Object>>() {});
                } catch (Exception e) {
                    log.error("解析 extraParams 失败: stationId={}, error={}", config.getStationId(), e.getMessage());
                }
            }
            return new HashMap<>();
        }

        // MODBUS_TCP / IEC_104
        Map<String, Object> params = new HashMap<>();
        params.put("host", config.getHost());
        params.put("port", config.getPort());
        return params;
    }
}
