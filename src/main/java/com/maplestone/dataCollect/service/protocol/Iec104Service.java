package com.maplestone.dataCollect.service.protocol;

import com.maplestone.dataCollect.cache.ConfigCache;
import com.maplestone.dataCollect.dao.entity.PointConfig;
import com.maplestone.dataCollect.kafka.DataProducer;
import com.maplestone.dataCollect.pojo.entity.DataPoint;
import com.maplestone.dataCollect.service.config.StationRuntimeStatusService;
import lombok.extern.slf4j.Slf4j;
import org.openmuc.j60870.*;
import org.openmuc.j60870.ie.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

/**
 * IEC 60870-5-104 数据采集服务
 */
@Slf4j
@Service
public class Iec104Service implements ProtocolHandler {

    private static final String PARAM_HOST = "host";
    private static final String PARAM_PORT = "port";
    private static final String PARAM_COMMON_ADDRESS = "commonAddress";
    private static final String PARAM_COA = "coa";

    @Autowired
    private ConfigCache configCache;

    @Autowired
    private DataProducer dataProducer;

    @Autowired
    private StationRuntimeStatusService stationRuntimeStatusService;

    @Value("${data-acquisition.iec104.auto-general-interrogation:true}")
    private boolean autoGeneralInterrogation;

    @Value("${data-acquisition.iec104.general-interrogation-delay-ms:2000}")
    private long generalInterrogationDelayMs;

    @Value("${data-acquisition.iec104.reconnect-delay-ms:5000}")
    private long reconnectDelayMs;

    private final Map<String, Connection> connectionMap = new ConcurrentHashMap<>();
    private final Map<String, Integer> connectionStationMap = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Object>> connectionParamMap = new ConcurrentHashMap<>();
    private final Set<String> reconnectingConnections = ConcurrentHashMap.newKeySet();
    private final Set<String> manualDisconnectConnections = ConcurrentHashMap.newKeySet();
    private final ScheduledExecutorService reconnectExecutor = Executors.newSingleThreadScheduledExecutor();

    private volatile boolean shuttingDown = false;

    @Override
    public String getProtocolType() {
        return "IEC_104";
    }

    @Override
    public boolean connect(String connectionId, Integer stationId, Map<String, Object> params) {
        Map<String, Object> safeParams = new HashMap<>(params);
        connectionParamMap.put(connectionId, safeParams);
        connectionStationMap.put(connectionId, stationId);
        manualDisconnectConnections.remove(connectionId);
        reconnectingConnections.remove(connectionId);
        return doConnect(connectionId, stationId, safeParams);
    }

    /**
     * 连接 IEC 104 设备
     */
    public boolean connect(String connectionId, Integer stationId, String host, int port) {
        Map<String, Object> params = new HashMap<>();
        params.put(PARAM_HOST, host);
        params.put(PARAM_PORT, port);
        return connect(connectionId, stationId, params);
    }

    private boolean doConnect(String connectionId, Integer stationId, Map<String, Object> params) {
        try {
            String host = (String) params.get(PARAM_HOST);
            int port = ((Number) params.get(PARAM_PORT)).intValue();
            InetAddress address = InetAddress.getByName(host);

            Connection connection = new ClientConnectionBuilder(address)
                    .setPort(port)
                    .setConnectionEventListener(new ConnectionEventListener() {
                        @Override
                        public void newASdu(Connection conn, ASdu aSdu) {
                            handleAsdu(connectionId, aSdu);
                        }

                        @Override
                        public void connectionClosed(Connection conn, IOException e) {
                            connectionMap.remove(connectionId, conn);
                            log.warn("IEC 104 连接关闭: {}", connectionId);
                            if (e != null) {
                                log.error("连接关闭异常: {}", e.getMessage());
                            }
                            scheduleReconnect(connectionId);
                        }

                        @Override
                        public void dataTransferStateChanged(Connection conn, boolean stopped) {
                            log.debug("IEC 104 数据传输状态变化: connectionId={}, stopped={}", connectionId, stopped);
                        }
                    })
                    .build();

            connectionMap.put(connectionId, connection);

            // 启动数据传输
            connection.startDataTransfer();
            triggerGeneralInterrogation(connectionId, stationId, params);
            stationRuntimeStatusService.markConnectSuccess(stationId);

            log.info("IEC 104 连接成功: {}:{}", host, port);
            return true;
        } catch (Exception e) {
            stationRuntimeStatusService.markConnectFailure(stationId, e.getMessage());
            log.error("IEC 104 连接失败: {}", e.getMessage());
            scheduleReconnect(connectionId);
            return false;
        }
    }

    /**
     * 处理接收到的 ASDU
     */
    private void handleAsdu(String connectionId, ASdu aSdu) {
        try {
            Integer stationId = connectionStationMap.get(connectionId);
            if (stationId == null) {
                log.warn("未找到连接对应的场站ID: {}", connectionId);
                return;
            }

            int coa = aSdu.getCommonAddress();
            InformationObject[] ios = aSdu.getInformationObjects();

            for (InformationObject io : ios) {
                int ioa = io.getInformationObjectAddress();
                PointConfig pointConfig = configCache.getPointConfigByIoa(stationId, ioa);

                if (pointConfig == null) {
                    log.debug("未找到测点配置: station={}, ioa={}", stationId, ioa);
                    continue;
                }

                double value = extractValue(io);
                
                DataPoint dataPoint = DataPoint.builder()
                        .stationId(stationId)
                        .pointId(pointConfig.getPointId())
                        .pointName(pointConfig.getPointName())
                        .timestamp(System.currentTimeMillis())
                        .value(value)
                        .quality(1)
                        .tableName(pointConfig.getTableName())
                        .coa(coa)
                        .ioa(ioa)
                        .typeId(aSdu.getTypeIdentification().getId())
                        .build();

                dataProducer.send(dataPoint);
            }

            log.debug("处理 ASDU: connectionId={}, COA={}, 类型={}, 数据点数={}",
                    connectionId, coa, aSdu.getTypeIdentification(), ios.length);

        } catch (Exception e) {
            log.error("处理 ASDU 失败: {}", e.getMessage(), e);
        }
    }

    private double extractValue(InformationObject io) {
        InformationElement[][] elements = io.getInformationElements();
        if (elements == null || elements.length == 0 || elements[0].length == 0) {
            return 0.0;
        }

        InformationElement element = elements[0][0];
        if (element instanceof IeShortFloat) {
            return ((IeShortFloat) element).getValue();
        } else if (element instanceof IeSinglePointWithQuality) {
            return ((IeSinglePointWithQuality) element).isOn() ? 1.0 : 0.0;
        } else if (element instanceof IeDoublePointWithQuality) {
            IeDoublePointWithQuality.DoublePointInformation dpi = 
                ((IeDoublePointWithQuality) element).getDoublePointInformation();
            return dpi == IeDoublePointWithQuality.DoublePointInformation.ON ? 1.0 : 0.0;
        }
        return 0.0;
    }

    /**
     * 总召唤 (General Interrogation)
     */
    public void generalInterrogation(String connectionId, int commonAddress) throws IOException {
        Connection connection = connectionMap.get(connectionId);
        if (connection == null) {
            throw new IllegalStateException("连接不存在: " + connectionId);
        }

        connection.interrogation(commonAddress, CauseOfTransmission.ACTIVATION, new IeQualifierOfInterrogation(20));
        log.info("发送总召唤命令: connectionId={}, COA={}", connectionId, commonAddress);
    }

    private void triggerGeneralInterrogation(String connectionId, Integer stationId, Map<String, Object> params) {
        if (!autoGeneralInterrogation) {
            return;
        }

        int commonAddress = resolveCommonAddress(stationId, params);
        reconnectExecutor.schedule(() -> {
            try {
                generalInterrogation(connectionId, commonAddress);
            } catch (Exception e) {
                log.error("自动总召失败: connectionId={}, COA={}, error={}", connectionId, commonAddress, e.getMessage());
            }
        }, Math.max(generalInterrogationDelayMs, 0L), TimeUnit.MILLISECONDS);
    }

    private int resolveCommonAddress(Integer stationId, Map<String, Object> params) {
        Object commonAddress = params.get(PARAM_COMMON_ADDRESS);
        if (commonAddress instanceof Number) {
            return ((Number) commonAddress).intValue();
        }

        Object coa = params.get(PARAM_COA);
        if (coa instanceof Number) {
            return ((Number) coa).intValue();
        }

        return stationId != null ? stationId : 1;
    }

    private void scheduleReconnect(String connectionId) {
        if (shuttingDown || manualDisconnectConnections.contains(connectionId)) {
            return;
        }

        Integer stationId = connectionStationMap.get(connectionId);
        Map<String, Object> params = connectionParamMap.get(connectionId);
        if (stationId == null || params == null) {
            return;
        }

        if (!reconnectingConnections.add(connectionId)) {
            return;
        }

        reconnectExecutor.schedule(() -> {
            try {
                if (shuttingDown || manualDisconnectConnections.contains(connectionId)) {
                    return;
                }

                log.info("开始重连 IEC 104: connectionId={}", connectionId);
                boolean success = doConnect(connectionId, stationId, new HashMap<>(params));
                if (!success && !shuttingDown && !manualDisconnectConnections.contains(connectionId)) {
                    reconnectingConnections.remove(connectionId);
                    scheduleReconnect(connectionId);
                    return;
                }
            } finally {
                reconnectingConnections.remove(connectionId);
            }
        }, Math.max(reconnectDelayMs, 1000L), TimeUnit.MILLISECONDS);
    }


    /**
     * 发送单点命令
     */
    public void sendSingleCommand(String connectionId, int commonAddress, int ioa, boolean value) throws IOException {
        Connection connection = connectionMap.get(connectionId);
        if (connection == null) {
            throw new IllegalStateException("连接不存在: " + connectionId);
        }

        IeSingleCommand command = new IeSingleCommand(value, 0, false);

        connection.send(new ASdu(ASduType.C_SC_NA_1, false, CauseOfTransmission.ACTIVATION,
                false, false, 0, commonAddress,
                new InformationObject(ioa, command)));

        log.info("发送单点命令: connectionId={}, COA={}, IOA={}, 值={}", connectionId, commonAddress, ioa, value);
    }

    /**
     * 发送设定值命令 (短浮点数)
     */
    public void sendSetpointCommand(String connectionId, int commonAddress, int ioa, float value) throws IOException {
        Connection connection = connectionMap.get(connectionId);
        if (connection == null) {
            throw new IllegalStateException("连接不存在: " + connectionId);
        }

        IeShortFloat setpoint = new IeShortFloat(value);

        connection.send(new ASdu(ASduType.C_SE_NC_1, false, CauseOfTransmission.ACTIVATION,
                false, false, 0, commonAddress,
                new InformationObject(ioa, setpoint)));

        log.info("发送设定值命令: connectionId={}, COA={}, IOA={}, 值={}", connectionId, commonAddress, ioa, value);
    }

    /**
     * 断开连接
     */
    public void disconnect(String connectionId) {
        manualDisconnectConnections.add(connectionId);
        reconnectingConnections.remove(connectionId);
        Connection connection = connectionMap.get(connectionId);
        if (connection != null) {
            connection.close();
            connectionMap.remove(connectionId);
        }
        connectionStationMap.remove(connectionId);
        connectionParamMap.remove(connectionId);
        log.info("IEC 104 连接已断开: {}", connectionId);
    }

    /**
     * 断开所有连接
     */
    public void disconnectAll() {
        shuttingDown = true;
        manualDisconnectConnections.addAll(connectionMap.keySet());
        connectionMap.forEach((id, connection) -> {
            connection.close();
            log.info("IEC 104 连接已断开: {}", id);
        });
        connectionMap.clear();
        connectionStationMap.clear();
        connectionParamMap.clear();
        reconnectingConnections.clear();
        manualDisconnectConnections.clear();
    }

    @PreDestroy
    public void shutdownExecutor() {
        shuttingDown = true;
        reconnectExecutor.shutdownNow();
    }
}
