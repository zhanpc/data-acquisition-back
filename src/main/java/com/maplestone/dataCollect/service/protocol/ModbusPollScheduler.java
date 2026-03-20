package com.maplestone.dataCollect.service.protocol;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maplestone.dataCollect.cache.ConfigCache;
import com.maplestone.dataCollect.dao.entity.PointConfig;
import com.maplestone.dataCollect.dao.entity.StationConfig;
import com.maplestone.dataCollect.service.config.StationRuntimeStatusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 按数据库测点配置自动轮询 Modbus TCP 数据。
 */
@Slf4j
@Component
public class ModbusPollScheduler {

    private static final String CONNECTION_ID_PREFIX = "station_";

    @Autowired
    private ConfigCache configCache;

    @Autowired
    private DataAcquisitionService dataAcquisitionService;

    @Autowired
    private ModbusTcpService modbusTcpService;

    @Autowired
    private ModbusRtuService modbusRtuService;

    @Autowired
    private StationRuntimeStatusService stationRuntimeStatusService;

    @Value("${data-acquisition.modbus.enabled:true}")
    private boolean enabled;

    @Value("${data-acquisition.modbus.reconnect-enabled:true}")
    private boolean reconnectEnabled;

    @Value("${data-acquisition.modbus.max-reconnect-attempts:5}")
    private int maxReconnectAttempts;

    private final Map<Integer, Integer> reconnectAttempts = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Scheduled(
            initialDelayString = "${data-acquisition.modbus.initial-delay-ms:10000}",
            fixedDelayString = "${data-acquisition.modbus.poll-interval-ms:5000}"
    )
    public void pollStations() {
        if (!enabled) {
            return;
        }

        for (StationConfig stationConfig : configCache.getAllStationConfigs().values()) {
            if (!isModbusProtocol(stationConfig.getProtocol())) {
                continue;
            }

            String connectionId = buildConnectionId(stationConfig.getStationId());
            String protocol = dataAcquisitionService.getConnectionProtocol(connectionId);
            if (!isModbusProtocol(protocol)) {
                continue;
            }

            reconnectAttempts.remove(stationConfig.getStationId());

            List<PointConfig> pointConfigs = configCache.getPointConfigsByStation(stationConfig.getStationId());
            if (pointConfigs.isEmpty()) {
                continue;
            }

            int unitId = resolveUnitId(stationConfig);
            for (PointConfig pointConfig : pointConfigs) {
                pollPoint(connectionId, protocol, unitId, stationConfig, pointConfig);
            }
        }
    }

    @Scheduled(
            initialDelayString = "${data-acquisition.modbus.reconnect-initial-delay-ms:10000}",
            fixedDelayString = "${data-acquisition.modbus.reconnect-interval-ms:5000}"
    )
    public void reconnectStations() {
        if (!enabled || !reconnectEnabled) {
            return;
        }

        for (StationConfig stationConfig : configCache.getAllStationConfigs().values()) {
            if (!isModbusProtocol(stationConfig.getProtocol())) {
                continue;
            }

            String connectionId = buildConnectionId(stationConfig.getStationId());
            String protocol = dataAcquisitionService.getConnectionProtocol(connectionId);
            if (isModbusProtocol(protocol)) {
                reconnectAttempts.remove(stationConfig.getStationId());
                continue;
            }

            attemptReconnect(stationConfig);
        }
    }

    private void attemptReconnect(StationConfig stationConfig) {
        if (!reconnectEnabled) {
            return;
        }

        int stationId = stationConfig.getStationId();
        int currentAttempts = reconnectAttempts.getOrDefault(stationId, 0);
        if (currentAttempts >= maxReconnectAttempts) {
            return;
        }

        int nextAttempt = currentAttempts + 1;
        reconnectAttempts.put(stationId, nextAttempt);

        String connectionId = buildConnectionId(stationId);
        boolean success = dataAcquisitionService.connect(
                connectionId,
                stationConfig.getProtocol(),
                stationId,
                buildParams(stationConfig)
        );

        if (success) {
            reconnectAttempts.remove(stationId);
            stationRuntimeStatusService.markConnectSuccess(stationId);
            log.info("Modbus 自动重连成功: stationId={}, protocol={}, attempt={}",
                    stationId, stationConfig.getProtocol(), nextAttempt);
            return;
        }

        boolean reachedLimit = nextAttempt >= maxReconnectAttempts;
        stationRuntimeStatusService.markReconnectAttempt(
                stationId,
                nextAttempt,
                "自动重连失败",
                reachedLimit
        );

        log.warn("Modbus 自动重连失败: stationId={}, protocol={}, attempt={}/{}",
                stationId, stationConfig.getProtocol(), nextAttempt, maxReconnectAttempts);

        if (reachedLimit) {
            log.error("Modbus 自动重连已达到上限，停止重连: stationId={}, protocol={}, maxAttempts={}",
                    stationId, stationConfig.getProtocol(), maxReconnectAttempts);
        }
    }

    private void pollPoint(String connectionId, String protocol, int unitId, StationConfig stationConfig, PointConfig pointConfig) {
        if (pointConfig.getAddress() == null || pointConfig.getRegisterType() == null) {
            return;
        }

        try {
            String registerType = pointConfig.getRegisterType().trim().toUpperCase();
            int address = pointConfig.getAddress();

            switch (registerType) {
                case "HOLDING":
                    readHoldingRegisters(protocol, connectionId, unitId, address);
                    break;
                case "INPUT":
                    readInputRegisters(protocol, connectionId, unitId, address);
                    break;
                case "COIL":
                    readCoils(protocol, connectionId, unitId, address);
                    break;
                case "DISCRETE":
                    readDiscreteInputs(protocol, connectionId, unitId, address);
                    break;
                default:
                    log.warn("跳过未知寄存器类型: stationId={}, pointId={}, registerType={}",
                            pointConfig.getStationId(), pointConfig.getPointId(), pointConfig.getRegisterType());
                    break;
            }
        } catch (Exception e) {
            handlePollingFailure(connectionId, stationConfig, e);
            log.error("轮询 Modbus 测点失败: stationId={}, pointId={}, address={}, registerType={}, error={}",
                    pointConfig.getStationId(),
                    pointConfig.getPointId(),
                    pointConfig.getAddress(),
                    pointConfig.getRegisterType(),
                    e.getMessage());
        }
    }

    private void handlePollingFailure(String connectionId, StationConfig stationConfig, Exception e) {
        Integer stationId = stationConfig.getStationId();
        stationRuntimeStatusService.markConnectFailure(stationId, e.getMessage());

        String currentProtocol = dataAcquisitionService.getConnectionProtocol(connectionId);
        if (isModbusProtocol(currentProtocol)) {
            dataAcquisitionService.disconnect(connectionId);
            log.warn("Modbus 连接已标记失效并断开，等待自动重连: stationId={}, protocol={}, error={}",
                    stationId, stationConfig.getProtocol(), e.getMessage());
        }
    }

    private void readHoldingRegisters(String protocol, String connectionId, int unitId, int address) throws Exception {
        if ("MODBUS_RTU".equalsIgnoreCase(protocol)) {
            modbusRtuService.readHoldingRegisters(connectionId, unitId, address, 1);
            return;
        }
        modbusTcpService.readHoldingRegisters(connectionId, unitId, address, 1);
    }

    private void readInputRegisters(String protocol, String connectionId, int unitId, int address) throws Exception {
        if ("MODBUS_RTU".equalsIgnoreCase(protocol)) {
            modbusRtuService.readInputRegisters(connectionId, unitId, address, 1);
            return;
        }
        modbusTcpService.readInputRegisters(connectionId, unitId, address, 1);
    }

    private void readCoils(String protocol, String connectionId, int unitId, int address) throws Exception {
        if ("MODBUS_RTU".equalsIgnoreCase(protocol)) {
            modbusRtuService.readCoils(connectionId, unitId, address, 1);
            return;
        }
        modbusTcpService.readCoils(connectionId, unitId, address, 1);
    }

    private void readDiscreteInputs(String protocol, String connectionId, int unitId, int address) throws Exception {
        if ("MODBUS_RTU".equalsIgnoreCase(protocol)) {
            modbusRtuService.readDiscreteInputs(connectionId, unitId, address, 1);
            return;
        }
        modbusTcpService.readDiscreteInputs(connectionId, unitId, address, 1);
    }

    private int resolveUnitId(StationConfig stationConfig) {
        Map<String, Object> extraParams = parseExtraParams(stationConfig.getExtraParams());
        Object unitId = extraParams.get("unitId");
        if (unitId instanceof Number) {
            return ((Number) unitId).intValue();
        }

        Object slaveId = extraParams.get("slaveId");
        if (slaveId instanceof Number) {
            return ((Number) slaveId).intValue();
        }

        return 1;
    }

    private Map<String, Object> parseExtraParams(String extraParams) {
        if (extraParams == null || extraParams.trim().isEmpty()) {
            return Collections.emptyMap();
        }

        try {
            return objectMapper.readValue(extraParams, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.warn("解析场站扩展参数失败，将使用默认 unitId=1: error={}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    private boolean isModbusProtocol(String protocol) {
        return "MODBUS_TCP".equalsIgnoreCase(protocol) || "MODBUS_RTU".equalsIgnoreCase(protocol);
    }

    private String buildConnectionId(Integer stationId) {
        return CONNECTION_ID_PREFIX + stationId;
    }

    private Map<String, Object> buildParams(StationConfig config) {
        Map<String, Object> params = new java.util.HashMap<>(parseExtraParams(config.getExtraParams()));
        if (!"MODBUS_RTU".equalsIgnoreCase(config.getProtocol())) {
            params.put("host", config.getHost());
            params.put("port", config.getPort());
        }
        return params;
    }
}
