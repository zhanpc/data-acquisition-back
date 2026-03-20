package com.maplestone.dataCollect.service.config;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.maplestone.dataCollect.dao.entity.StationConfig;
import com.maplestone.dataCollect.dao.mapper.StationConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 场站运行态回写服务。
 * status 仍表示配置启停，运行态使用 connectStatus / reconnectFailCount 等字段表达。
 */
@Slf4j
@Service
public class StationRuntimeStatusService {

    public static final int CONNECT_STATUS_DISCONNECTED = 0;
    public static final int CONNECT_STATUS_CONNECTED = 1;
    public static final int CONNECT_STATUS_RECONNECT_FAILED = 2;

    @Autowired
    private StationConfigMapper stationConfigMapper;

    public void markConnectSuccess(Integer stationId) {
        UpdateWrapper<StationConfig> wrapper = new UpdateWrapper<>();
        wrapper.eq("station_id", stationId)
                .set("connect_status", CONNECT_STATUS_CONNECTED)
                .set("reconnect_fail_count", 0)
                .set("last_error", null)
                .set("last_connect_time", new Date())
                .set("last_retry_time", new Date());
        stationConfigMapper.update(null, wrapper);
    }

    public void markConnectFailure(Integer stationId, String errorMessage) {
        UpdateWrapper<StationConfig> wrapper = new UpdateWrapper<>();
        wrapper.eq("station_id", stationId)
                .set("connect_status", CONNECT_STATUS_DISCONNECTED)
                .set("last_error", truncate(errorMessage))
                .set("last_retry_time", new Date());
        stationConfigMapper.update(null, wrapper);
    }

    public void markReconnectAttempt(Integer stationId, int failCount, String errorMessage, boolean reachedLimit) {
        UpdateWrapper<StationConfig> wrapper = new UpdateWrapper<>();
        wrapper.eq("station_id", stationId)
                .set("connect_status", reachedLimit ? CONNECT_STATUS_RECONNECT_FAILED : CONNECT_STATUS_DISCONNECTED)
                .set("reconnect_fail_count", failCount)
                .set("last_error", truncate(errorMessage))
                .set("last_retry_time", new Date());
        stationConfigMapper.update(null, wrapper);
    }

    public void resetReconnectStatus(Integer stationId) {
        UpdateWrapper<StationConfig> wrapper = new UpdateWrapper<>();
        wrapper.eq("station_id", stationId)
                .set("connect_status", CONNECT_STATUS_DISCONNECTED)
                .set("reconnect_fail_count", 0)
                .set("last_error", null);
        stationConfigMapper.update(null, wrapper);
    }

    private String truncate(String value) {
        if (value == null) {
            return null;
        }
        return value.length() <= 500 ? value : value.substring(0, 500);
    }
}
