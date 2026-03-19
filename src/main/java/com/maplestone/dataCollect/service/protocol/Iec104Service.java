package com.maplestone.dataCollect.service.protocol;

import com.maplestone.dataCollect.cache.ConfigCache;
import com.maplestone.dataCollect.dao.entity.PointConfig;
import com.maplestone.dataCollect.kafka.DataProducer;
import com.maplestone.dataCollect.pojo.entity.DataPoint;
import lombok.extern.slf4j.Slf4j;
import org.openmuc.j60870.*;
import org.openmuc.j60870.ie.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * IEC 60870-5-104 数据采集服务
 */
@Slf4j
@Service
public class Iec104Service implements ProtocolHandler {

    @Autowired
    private ConfigCache configCache;

    @Autowired
    private DataProducer dataProducer;

    private Map<String, Connection> connectionMap = new HashMap<>();
    private Map<String, Integer> connectionStationMap = new HashMap<>();

    @Override
    public String getProtocolType() {
        return "IEC_104";
    }

    @Override
    public boolean connect(String connectionId, Integer stationId, Map<String, Object> params) {
        String host = (String) params.get("host");
        int port = ((Number) params.get("port")).intValue();
        return connect(connectionId, stationId, host, port);
    }

    /**
     * 连接 IEC 104 设备
     */
    public boolean connect(String connectionId, Integer stationId, String host, int port) {
        connectionStationMap.put(connectionId, stationId);
        try {
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
                            log.warn("IEC 104 连接关闭: {}", connectionId);
                            if (e != null) {
                                log.error("连接关闭异常: {}", e.getMessage());
                            }
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

            log.info("IEC 104 连接成功: {}:{}", host, port);
            return true;
        } catch (IOException e) {
            log.error("IEC 104 连接失败: {}", e.getMessage());
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
        Connection connection = connectionMap.get(connectionId);
        if (connection != null) {
            connection.close();
            connectionMap.remove(connectionId);
            connectionStationMap.remove(connectionId);
            log.info("IEC 104 连接已断开: {}", connectionId);
        }
    }

    /**
     * 断开所有连接
     */
    public void disconnectAll() {
        connectionMap.forEach((id, connection) -> {
            connection.close();
            log.info("IEC 104 连接已断开: {}", id);
        });
        connectionMap.clear();
        connectionStationMap.clear();
    }
}