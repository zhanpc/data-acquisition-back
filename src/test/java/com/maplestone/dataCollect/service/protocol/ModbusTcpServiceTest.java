package com.maplestone.dataCollect.service.protocol;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Modbus TCP 测试类
 */
@Slf4j
@SpringBootTest
public class ModbusTcpServiceTest {

    @Autowired
    private ModbusTcpService modbusTcpService;

    private static final String host = "192.168.3.222";
    /**
     * 测试连接
     */
    @Test
    public void testConnect() {
        String connectionId = "test-modbus-tcp";
        int port = 502;
        Integer stationId = 1;
        boolean connected = modbusTcpService.connect(connectionId, stationId,host, port);
        log.info("连接结果: {}", connected ? "成功" : "失败");

        if (connected) {
            modbusTcpService.disconnect(connectionId);
        }
    }

    /**
     * 测试读取保持寄存器 03 Holding Register
     */
    @Test
    public void testReadHoldingRegisters() {
        String connectionId = "test-modbus-tcp";
        int port = 502;
        Integer stationId = 1;
        try {
            // 连接设备
            boolean connected = modbusTcpService.connect(connectionId,stationId, host, port);
            if (!connected) {
                log.error("连接失败");
                return;
            }

            // 读取寄存器
            int unitId = 1;       // 单元ID (从站地址)
            int address = 0;      // 起始地址
            int quantity = 10;    // 读取数量

            int[] values = modbusTcpService.readHoldingRegisters(connectionId, unitId, address, quantity);

            log.info("读取到的数据:");
            for (int i = 0; i < values.length; i++) {
                log.info("  寄存器[{}] = {}", address + i, values[i]);
            }

        } catch (Exception e) {
            log.error("读取失败: {}", e.getMessage(), e);
        } finally {
            modbusTcpService.disconnect(connectionId);
        }
    }

    /**
     * 测试读取输入寄存器 04 Input Register
     */
    @Test
    public void testReadInputRegisters() {
        String connectionId = "test-modbus-tcp";
        int port = 502;
        Integer stationId = 1;
        try {
            boolean connected = modbusTcpService.connect(connectionId,stationId, host, port);
            if (!connected) {
                log.error("连接失败");
                return;
            }

            int unitId = 1;
            int address = 0;
            int quantity = 5;

            int[] values = modbusTcpService.readInputRegisters(connectionId, unitId, address, quantity);

            log.info("读取输入寄存器:");
            for (int i = 0; i < values.length; i++) {
                log.info("  寄存器[{}] = {}", address + i, values[i]);
            }

        } catch (Exception e) {
            log.error("读取失败: {}", e.getMessage(), e);
        } finally {
            modbusTcpService.disconnect(connectionId);
        }
    }

    /**
     * 测试读取线圈 01 Coil Status
     */
    @Test
    public void testReadCoils() {
        String connectionId = "test-modbus-tcp";
        int port = 502;
        Integer stationId = 1;
        try {
            boolean connected = modbusTcpService.connect(connectionId,stationId, host, port);
            if (!connected) {
                log.error("连接失败");
                return;
            }

            int unitId = 1;
            int address = 0;
            int quantity = 8;

            boolean[] values = modbusTcpService.readCoils(connectionId, unitId, address, quantity);

            log.info("读取线圈状态:");
            for (int i = 0; i < values.length; i++) {
                log.info("  线圈[{}] = {}", address + i, values[i]);
            }

        } catch (Exception e) {
            log.error("读取失败: {}", e.getMessage(), e);
        } finally {
            modbusTcpService.disconnect(connectionId);
        }
    }

    /**
     * 测试读取离散输入 02 Input Status
     */
    @Test
    public void testReadDiscreteInputs() {
        String connectionId = "test-modbus-tcp";
        int port = 502;
        Integer stationId = 1;
        try {
            boolean connected = modbusTcpService.connect(connectionId, stationId,host, port);
            if (!connected) {
                log.error("连接失败");
                return;
            }

            int unitId = 1;
            int address = 0;
            int quantity = 8;

            boolean[] values = modbusTcpService.readDiscreteInputs(connectionId, unitId, address, quantity);

            log.info("读取离散输入:");
            for (int i = 0; i < values.length; i++) {
                log.info("  输入[{}] = {}", address + i, values[i]);
            }

        } catch (Exception e) {
            log.error("读取失败: {}", e.getMessage(), e);
        } finally {
            modbusTcpService.disconnect(connectionId);
        }
    }

    /**
     * 测试写单个寄存器
     */
    @Test
    public void testWriteSingleRegister() {
        String connectionId = "test-modbus-tcp";
        int port = 502;
        Integer stationId = 1;
        try {
            boolean connected = modbusTcpService.connect(connectionId, stationId,host, port);
            if (!connected) {
                log.error("连接失败");
                return;
            }

            int unitId = 1;
            int address = 0;
            int value = 1234;

            modbusTcpService.writeSingleRegister(connectionId, unitId, address, value);
            log.info("写入成功: 寄存器[{}] = {}", address, value);

            // 读取验证
            int[] readValues = modbusTcpService.readHoldingRegisters(connectionId, unitId, address, 1);
            log.info("验证读取: 寄存器[{}] = {}", address, readValues[0]);

        } catch (Exception e) {
            log.error("操作失败: {}", e.getMessage(), e);
        } finally {
            modbusTcpService.disconnect(connectionId);
        }
    }

    /**
     * 测试写单个线圈
     */
    @Test
    public void testWriteSingleCoil() {
        String connectionId = "test-modbus-tcp";
        int port = 502;
        Integer stationId = 1;
        try {
            boolean connected = modbusTcpService.connect(connectionId, stationId,host, port);
            if (!connected) {
                log.error("连接失败");
                return;
            }

            int unitId = 1;
            int address = 0;
            boolean value = true;

            modbusTcpService.writeSingleCoil(connectionId, unitId, address, value);
            log.info("写入成功: 线圈[{}] = {}", address, value);

            // 读取验证
            boolean[] readValues = modbusTcpService.readCoils(connectionId, unitId, address, 1);
            log.info("验证读取: 线圈[{}] = {}", address, readValues[0]);

        } catch (Exception e) {
            log.error("操作失败: {}", e.getMessage(), e);
        } finally {
            modbusTcpService.disconnect(connectionId);
        }
    }

    /**
     * 测试写多个寄存器
     */
    @Test
    public void testWriteMultipleRegisters() {
        String connectionId = "test-modbus-tcp";
        int port = 502;
        Integer stationId = 1;
        try {
            boolean connected = modbusTcpService.connect(connectionId, stationId,host, port);
            if (!connected) {
                log.error("连接失败");
                return;
            }

            int unitId = 1;
            int address = 0;
            int[] values = {100, 200, 300, 400, 500};

            modbusTcpService.writeMultipleRegisters(connectionId, unitId, address, values);
            log.info("写入成功: 起始地址={}, 数量={}", address, values.length);

            // 读取验证
            int[] readValues = modbusTcpService.readHoldingRegisters(connectionId, unitId, address, values.length);
            log.info("验证读取:");
            for (int i = 0; i < readValues.length; i++) {
                log.info("  寄存器[{}] = {}", address + i, readValues[i]);
            }

        } catch (Exception e) {
            log.error("操作失败: {}", e.getMessage(), e);
        } finally {
            modbusTcpService.disconnect(connectionId);
        }
    }
}