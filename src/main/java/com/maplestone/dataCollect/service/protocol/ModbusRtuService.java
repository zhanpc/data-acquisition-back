package com.maplestone.dataCollect.service.protocol;

import com.maplestone.dataCollect.cache.ConfigCache;
import com.maplestone.dataCollect.dao.entity.PointConfig;
import com.maplestone.dataCollect.kafka.DataProducer;
import com.maplestone.dataCollect.pojo.entity.DataPoint;
import lombok.extern.slf4j.Slf4j;
import net.wimpi.modbus.io.ModbusSerialTransaction;
import net.wimpi.modbus.msg.*;
import net.wimpi.modbus.net.SerialConnection;
import net.wimpi.modbus.procimg.InputRegister;
import net.wimpi.modbus.procimg.Register;
import net.wimpi.modbus.util.SerialParameters;
import net.wimpi.modbus.util.BitVector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Modbus RTU 数据采集服务
 */
@Slf4j
@Service
public class ModbusRtuService implements ProtocolHandler {

    @Autowired
    private ConfigCache configCache;

    @Autowired
    private DataProducer dataProducer;

    private final Map<String, SerialConnection> connectionMap = new ConcurrentHashMap<>();
    private final Map<String, Integer> connectionStationMap = new ConcurrentHashMap<>();

    @Override
    public String getProtocolType() {
        return "MODBUS_RTU";
    }

    @Override
    public boolean connect(String connectionId, Integer stationId, Map<String, Object> params) {
        String portName = (String) params.get("portName");
        int baudRate = ((Number) params.get("baudRate")).intValue();
        int dataBits = params.containsKey("dataBits") ? ((Number) params.get("dataBits")).intValue() : 8;
        int stopBits = params.containsKey("stopBits") ? ((Number) params.get("stopBits")).intValue() : 1;
        String parity = params.containsKey("parity") ? (String) params.get("parity") : "None";
        return connect(connectionId, stationId, portName, baudRate, dataBits, stopBits, parity);
    }

    /**
     * 连接 Modbus RTU 设备
     * @param connectionId 连接ID
     * @param portName 串口名称 (如 COM1, /dev/ttyS0)
     * @param baudRate 波特率
     * @param dataBits 数据位 (5-8)
     * @param stopBits 停止位 (1, 2)
     * @param parity 校验位 ("None", "Even", "Odd")
     */
    public boolean connect(String connectionId, Integer stationId, String portName, int baudRate,
                          int dataBits, int stopBits, String parity) {
        connectionStationMap.put(connectionId, stationId);
        try {
            SerialParameters params = new SerialParameters();
            params.setPortName(portName);
            params.setBaudRate(baudRate);
            params.setDatabits(dataBits);
            params.setStopbits(stopBits);
            params.setParity(parity);
            params.setEncoding("rtu");
            params.setEcho(false);

            SerialConnection connection = new net.wimpi.modbus.net.SerialConnection(params);
            connection.open();

            connectionMap.put(connectionId, connection);
            log.info("Modbus RTU 连接成功: port={}, baudRate={}", portName, baudRate);
            return true;
        } catch (Exception e) {
            log.error("Modbus RTU 连接失败: {}", e.getMessage());
            connectionStationMap.remove(connectionId);
            return false;
        }
    }

    /**
     * 读取保持寄存器
     */
    public int[] readHoldingRegisters(String connectionId, int unitId, int address, int quantity) throws Exception {
        SerialConnection connection = connectionMap.get(connectionId);
        if (connection == null) {
            throw new IllegalStateException("连接不存在: " + connectionId);
        }

        ReadMultipleRegistersRequest request = new ReadMultipleRegistersRequest(address, quantity);
        request.setUnitID(unitId);

        ModbusSerialTransaction transaction = new ModbusSerialTransaction(connection);
        transaction.setRequest(request);
        transaction.execute();

        ReadMultipleRegistersResponse response = (ReadMultipleRegistersResponse) transaction.getResponse();
        Register[] registers = response.getRegisters();

        int[] result = new int[registers.length];
        for (int i = 0; i < registers.length; i++) {
            result[i] = registers[i].getValue();
        }

        processRegisterData(connectionId, unitId, address, result, "HOLDING");

        log.info("读取保持寄存器成功: unitId={}, address={}, quantity={}", unitId, address, quantity);
        return result;
    }

    /**
     * 读取输入寄存器
     */
    public int[] readInputRegisters(String connectionId, int unitId, int address, int quantity) throws Exception {
        SerialConnection connection = connectionMap.get(connectionId);
        if (connection == null) {
            throw new IllegalStateException("连接不存在: " + connectionId);
        }

        ReadInputRegistersRequest request = new ReadInputRegistersRequest(address, quantity);
        request.setUnitID(unitId);

        ModbusSerialTransaction transaction = new ModbusSerialTransaction(connection);
        transaction.setRequest(request);
        transaction.execute();

        ReadInputRegistersResponse response = (ReadInputRegistersResponse) transaction.getResponse();
        InputRegister[] registers = response.getRegisters();

        int[] result = new int[registers.length];
        for (int i = 0; i < registers.length; i++) {
            result[i] = registers[i].getValue();
        }

        processRegisterData(connectionId, unitId, address, result, "INPUT");

        log.info("读取输入寄存器成功: unitId={}, address={}, quantity={}", unitId, address, quantity);
        return result;
    }

    /**
     * 读取线圈状态
     */
    public boolean[] readCoils(String connectionId, int unitId, int address, int quantity) throws Exception {
        SerialConnection connection = connectionMap.get(connectionId);
        if (connection == null) {
            throw new IllegalStateException("连接不存在: " + connectionId);
        }

        ReadCoilsRequest request = new ReadCoilsRequest(address, quantity);
        request.setUnitID(unitId);

        ModbusSerialTransaction transaction = new ModbusSerialTransaction(connection);
        transaction.setRequest(request);
        transaction.execute();

        ReadCoilsResponse response = (ReadCoilsResponse) transaction.getResponse();
        BitVector coils = response.getCoils();

        boolean[] result = new boolean[quantity];
        for (int i = 0; i < quantity; i++) {
            result[i] = coils.getBit(i);
        }

        processBooleanData(connectionId, unitId, address, result, "COIL");

        log.info("读取线圈状态成功: unitId={}, address={}, quantity={}", unitId, address, quantity);
        return result;
    }

    /**
     * 读取离散输入
     */
    public boolean[] readDiscreteInputs(String connectionId, int unitId, int address, int quantity) throws Exception {
        SerialConnection connection = connectionMap.get(connectionId);
        if (connection == null) {
            throw new IllegalStateException("连接不存在: " + connectionId);
        }

        ReadInputDiscretesRequest request = new ReadInputDiscretesRequest(address, quantity);
        request.setUnitID(unitId);

        ModbusSerialTransaction transaction = new ModbusSerialTransaction(connection);
        transaction.setRequest(request);
        transaction.execute();

        ReadInputDiscretesResponse response = (ReadInputDiscretesResponse) transaction.getResponse();
        BitVector inputs = response.getDiscretes();

        boolean[] result = new boolean[quantity];
        for (int i = 0; i < quantity; i++) {
            result[i] = inputs.getBit(i);
        }

        processBooleanData(connectionId, unitId, address, result, "DISCRETE");

        log.info("读取离散输入成功: unitId={}, address={}, quantity={}", unitId, address, quantity);
        return result;
    }

    private void processRegisterData(String connectionId, int unitId, int startAddress, int[] values, String registerType) {
        Integer stationId = connectionStationMap.get(connectionId);
        if (stationId == null) {
            return;
        }

        for (int i = 0; i < values.length; i++) {
            int address = startAddress + i;
            PointConfig pointConfig = configCache.getPointConfigByAddressAndRegisterType(stationId, address, registerType);

            if (pointConfig == null) {
                continue;
            }

            sendDataPoint(stationId, pointConfig, unitId, address, (double) values[i], registerType);
        }
    }

    private void processBooleanData(String connectionId, int unitId, int startAddress, boolean[] values, String registerType) {
        Integer stationId = connectionStationMap.get(connectionId);
        if (stationId == null) {
            return;
        }

        for (int i = 0; i < values.length; i++) {
            int address = startAddress + i;
            PointConfig pointConfig = configCache.getPointConfigByAddressAndRegisterType(stationId, address, registerType);

            if (pointConfig == null) {
                continue;
            }

            sendDataPoint(stationId, pointConfig, unitId, address, values[i] ? 1D : 0D, registerType);
        }
    }

    private void sendDataPoint(Integer stationId, PointConfig pointConfig, int unitId, int address, double value, String registerType) {
        DataPoint dataPoint = DataPoint.builder()
                .stationId(stationId)
                .pointId(pointConfig.getPointId())
                .pointName(pointConfig.getPointName())
                .timestamp(System.currentTimeMillis())
                .value(value)
                .quality(1)
                .tableName(pointConfig.getTableName())
                .slaveId(unitId)
                .address(address)
                .registerType(registerType)
                .build();

        dataProducer.send(dataPoint);
    }

    /**
     * 写单个保持寄存器
     */
    public void writeSingleRegister(String connectionId, int unitId, int address, int value) throws Exception {
        SerialConnection connection = connectionMap.get(connectionId);
        if (connection == null) {
            throw new IllegalStateException("连接不存在: " + connectionId);
        }

        WriteSingleRegisterRequest request = new WriteSingleRegisterRequest(address, new net.wimpi.modbus.procimg.SimpleRegister(value));
        request.setUnitID(unitId);

        ModbusSerialTransaction transaction = new ModbusSerialTransaction(connection);
        transaction.setRequest(request);
        transaction.execute();

        log.info("写单个寄存器成功: unitId={}, address={}, value={}", unitId, address, value);
    }

    /**
     * 写单个线圈
     */
    public void writeSingleCoil(String connectionId, int unitId, int address, boolean value) throws Exception {
        SerialConnection connection = connectionMap.get(connectionId);
        if (connection == null) {
            throw new IllegalStateException("连接不存在: " + connectionId);
        }

        WriteCoilRequest request = new WriteCoilRequest(address, value);
        request.setUnitID(unitId);

        ModbusSerialTransaction transaction = new ModbusSerialTransaction(connection);
        transaction.setRequest(request);
        transaction.execute();

        log.info("写单个线圈成功: unitId={}, address={}, value={}", unitId, address, value);
    }

    /**
     * 写多个保持寄存器
     */
    public void writeMultipleRegisters(String connectionId, int unitId, int address, int[] values) throws Exception {
        SerialConnection connection = connectionMap.get(connectionId);
        if (connection == null) {
            throw new IllegalStateException("连接不存在: " + connectionId);
        }

        Register[] registers = new Register[values.length];
        for (int i = 0; i < values.length; i++) {
            registers[i] = new net.wimpi.modbus.procimg.SimpleRegister(values[i]);
        }

        WriteMultipleRegistersRequest request = new WriteMultipleRegistersRequest(address, registers);
        request.setUnitID(unitId);

        ModbusSerialTransaction transaction = new ModbusSerialTransaction(connection);
        transaction.setRequest(request);
        transaction.execute();

        log.info("写多个寄存器成功: unitId={}, address={}, quantity={}", unitId, address, values.length);
    }

    /**
     * 断开连接
     */
    public void disconnect(String connectionId) {
        SerialConnection connection = connectionMap.get(connectionId);
        if (connection != null) {
            connection.close();
            connectionMap.remove(connectionId);
            connectionStationMap.remove(connectionId);
            log.info("Modbus RTU 连接已断开: {}", connectionId);
        }
    }

    /**
     * 断开所有连接
     */
    public void disconnectAll() {
        connectionMap.forEach((id, connection) -> {
            connection.close();
            log.info("Modbus RTU 连接已断开: {}", id);
        });
        connectionMap.clear();
        connectionStationMap.clear();
    }
}
