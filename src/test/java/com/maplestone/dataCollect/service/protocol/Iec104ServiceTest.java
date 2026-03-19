package com.maplestone.dataCollect.service.protocol;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

/**
 * IEC 104 测试类
 */
@Slf4j
@SpringBootTest
public class Iec104ServiceTest {

    @Autowired
    private Iec104Service iec104Service;
    private static final String host = "127.0.0.1";
    /**
     * 测试连接
     */
    @Test
    public void testConnect() {
        String connectionId = "test-iec104";
        int port = 2404;
        Integer stationId = 1;
        boolean connected = iec104Service.connect(connectionId, stationId, host, port);
        log.info("连接结果: {}", connected ? "成功" : "失败");

        if (connected) {
            // 等待一段时间接收数据
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            iec104Service.disconnect(connectionId);
        }
    }

    /**
     * 测试总召唤
     */
    @Test
    public void testGeneralInterrogation() {
        String connectionId = "test-iec104";
        int port = 2404;
        Integer stationId = 1;
        try {
            boolean connected = iec104Service.connect(connectionId,stationId, host, port);
            if (!connected) {
                log.error("连接失败");
                return;
            }

            // 等待连接稳定
            Thread.sleep(1000);

            // 发送总召唤
            int commonAddress = 1;
            iec104Service.generalInterrogation(connectionId, commonAddress);
            log.info("总召唤命令已发送");

            // 等待接收数据
            Thread.sleep(5000);

        } catch (IOException | InterruptedException e) {
            log.error("操作失败: {}", e.getMessage());
        } finally {
            iec104Service.disconnect(connectionId);
        }
    }

    /**
     * 测试读取数据
     */
    @Test
    public void testReadData() {
        String connectionId = "test-iec104";
        int port = 2404;
        Integer stationId = 1;
        try {
            boolean connected = iec104Service.connect(connectionId, stationId,host, port);
            if (!connected) {
                log.error("连接失败");
                return;
            }

            // 发送总召唤获取数据
            int commonAddress = 1;
            iec104Service.generalInterrogation(connectionId, commonAddress);
            log.info("总召唤命令已发送，数据将通过回调自动处理并发送到Kafka");

            // 等待数据接收（数据会通过事件监听器自动处理）
            Thread.sleep(3000);

        } catch (Exception e) {
            log.error("操作失败: {}", e.getMessage(), e);
        } finally {
            iec104Service.disconnect(connectionId);
        }
    }

    /**
     * 测试发送单点命令
     */
    @Test
    public void testSendSingleCommand() {
        String connectionId = "test-iec104";
        int port = 2404;
        Integer stationId = 1;
        try {
            boolean connected = iec104Service.connect(connectionId, stationId,host, port);
            if (!connected) {
                log.error("连接失败");
                return;
            }

            // 等待连接稳定
            Thread.sleep(1000);

            // 发送单点命令
            int commonAddress = 1;
            int ioa = 1;
            boolean value = true;

            iec104Service.sendSingleCommand(connectionId, commonAddress, ioa, value);
            log.info("单点命令已发送: IOA[{}] = {}", ioa, value);

            // 等待命令执行
            Thread.sleep(2000);

        } catch (IOException | InterruptedException e) {
            log.error("操作失败: {}", e.getMessage());
        } finally {
            iec104Service.disconnect(connectionId);
        }
    }

    /**
     * 测试发送设定值命令
     */
    @Test
    public void testSendSetpointCommand() {
        String connectionId = "test-iec104";
        int port = 2404;
        Integer stationId = 1;
        try {
            boolean connected = iec104Service.connect(connectionId, stationId,host, port);
            if (!connected) {
                log.error("连接失败");
                return;
            }

            // 等待连接稳定
            Thread.sleep(1000);

            // 发送设定值命令
            int commonAddress = 1;
            int ioa = 2;
            float value = 123.45f;

            iec104Service.sendSetpointCommand(connectionId, commonAddress, ioa, value);
            log.info("设定值命令已发送: IOA[{}] = {}", ioa, value);

            // 等待命令执行
            Thread.sleep(2000);

        } catch (IOException | InterruptedException e) {
            log.error("操作失败: {}", e.getMessage());
        } finally {
            iec104Service.disconnect(connectionId);
        }
    }

    /**
     * 综合测试：连接、总召唤、读取、写入
     */
    @Test
    public void testComprehensive() {
        String connectionId = "test-iec104-comprehensive";
        int port = 2404;
        Integer stationId = 1;
        try {
            // 1. 连接
            log.info("=== 步骤1: 连接设备 ===");
            boolean connected = iec104Service.connect(connectionId,stationId, host, port);
            if (!connected) {
                log.error("连接失败");
                return;
            }
            Thread.sleep(1000);

            // 2. 总召唤
            log.info("=== 步骤2: 发送总召唤 ===");
            int commonAddress = 1;
            iec104Service.generalInterrogation(connectionId, commonAddress);
            log.info("总召唤命令已发送，数据将通过回调自动处理并发送到Kafka");
            Thread.sleep(3000);

            // 3. 发送控制命令
            log.info("=== 步骤3: 发送控制命令 ===");
            iec104Service.sendSingleCommand(connectionId, commonAddress, 2, true);
            Thread.sleep(1000);

            log.info("=== 测试完成 ===");

        } catch (Exception e) {
            log.error("测试失败: {}", e.getMessage(), e);
        } finally {
            iec104Service.disconnect(connectionId);
        }
    }
}