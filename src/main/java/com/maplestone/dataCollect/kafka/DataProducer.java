package com.maplestone.dataCollect.kafka;

import com.maplestone.dataCollect.pojo.entity.DataPoint;
import com.maplestone.dataCollect.wal.WalManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * Kafka 数据生产者
 * 负责将采集到的数据发送到 Kafka
 */
@Slf4j
@Component
public class DataProducer {

    private static final String TOPIC = "iot-data";

    @Autowired
    private KafkaTemplate<String, DataPoint> kafkaTemplate;

    @Autowired
    private WalManager walManager;

    @PostConstruct
    public void init() {
        log.info("Kafka 数据生产者初始化完成");
        recoverFromWal();
    }

    public void send(DataPoint dataPoint) {
        String walFilePath = walManager.write(dataPoint);

        String key = dataPoint.getStationId() + "_" + dataPoint.getPointId();
        ListenableFuture<SendResult<String, DataPoint>> future = kafkaTemplate.send(TOPIC, key, dataPoint);

        future.addCallback(new ListenableFutureCallback<SendResult<String, DataPoint>>() {
            @Override
            public void onSuccess(SendResult<String, DataPoint> result) {
                walManager.delete(walFilePath);
                log.debug("数据发送成功: station={}, point={}, value={}", 
                    dataPoint.getStationId(), dataPoint.getPointId(), dataPoint.getValue());
            }

            @Override
            public void onFailure(Throwable ex) {
                log.error("数据发送失败: station={}, point={}, 错误: {}", 
                    dataPoint.getStationId(), dataPoint.getPointId(), ex.getMessage());
            }
        });
    }

    private void recoverFromWal() {
        List<DataPoint> dataPoints = walManager.recover();
        if (dataPoints.isEmpty()) {
            return;
        }

        log.info("开始恢复 WAL 数据: {} 条", dataPoints.size());
        for (DataPoint dataPoint : dataPoints) {
            send(dataPoint);
        }
    }
}
