package com.maplestone.dataCollect.kafka;

import com.maplestone.dataCollect.pojo.entity.DataPoint;
import com.maplestone.dataCollect.service.storage.TDengineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Kafka 数据消费者
 * 负责从 Kafka 批量消费数据并写入 TDengine
 */
@Slf4j
@Component
public class DataConsumer {

    @Autowired
    private TDengineService tdengineService;

    @KafkaListener(topics = "iot-data", containerFactory = "kafkaListenerContainerFactory")
    public void consume(List<DataPoint> dataPoints, Acknowledgment acknowledgment) {
        try {
            if (dataPoints == null || dataPoints.isEmpty()) {
                acknowledgment.acknowledge();
                return;
            }

            log.info("消费 Kafka 数据: {} 条", dataPoints.size());

            tdengineService.batchInsert(dataPoints);

            acknowledgment.acknowledge();

            log.debug("数据写入 TDengine 成功，已提交 offset");
        } catch (Exception e) {
            log.error("消费数据失败: {}", e.getMessage(), e);
        }
    }
}
