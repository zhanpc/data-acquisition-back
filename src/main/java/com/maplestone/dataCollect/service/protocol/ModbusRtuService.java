package com.maplestone.dataCollect.service.protocol;

import lombok.extern.slf4j.Slf4j;
import net.wimpi.modbus.io.ModbusSerialTransaction;
import net.wimpi.modbus.msg.*;
import net.wimpi.modbus.net.SerialConnection;
import net.wimpi.modbus.procimg.InputRegister;
import net.wimpi.modbus.procimg.Register;
import net.wimpi.modbus.util.SerialParameters;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Modbus RTU 数据采集服务
 */
@Slf4j
@Service
public class ModbusRtuService implements ProtocolHandler {

    private Map<String, SerialConnection> connectionMap = new HashMap<>();

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
        return connect(connectionId, portName, baudRate, dataBits, stopBits, parity);
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
    public boolean connect(String connectionId, String portName, int baudRate,
                          int dataBits, int stopBits, String parity) {
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

        log.info("读取保持寄存器成功: unitId={}, address={}, quantity={}", unitId, address, quantity);
        return result;
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
     * 断开连接
     */
    public void disconnect(String connectionId) {
        SerialConnection connection = connectionMap.get(connectionId);
        if (connection != null) {
            connection.close();
            connectionMap.remove(connectionId);
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
    }
}