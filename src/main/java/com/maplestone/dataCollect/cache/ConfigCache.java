package com.maplestone.dataCollect.cache;

import com.maplestone.dataCollect.dao.entity.PointConfig;
import com.maplestone.dataCollect.dao.entity.StationConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 配置缓存 - 内存缓存，避免频繁访问数据库
 */
@Slf4j
@Component
public class ConfigCache {

    private final Map<Integer, StationConfig> stationConfigMap = new ConcurrentHashMap<>();
    
    private final Map<String, PointConfig> pointConfigMap = new ConcurrentHashMap<>();

    public void loadStationConfigs(List<StationConfig> configs) {
        stationConfigMap.clear();
        for (StationConfig config : configs) {
            stationConfigMap.put(config.getStationId(), config);
        }
        log.info("加载场站配置到缓存: {} 个", configs.size());
    }

    public void loadPointConfigs(List<PointConfig> configs) {
        pointConfigMap.clear();
        for (PointConfig config : configs) {
            String key = buildPointKey(config.getStationId(), config.getPointId());
            pointConfigMap.put(key, config);
        }
        log.info("加载测点配置到缓存: {} 个", configs.size());
    }

    public StationConfig getStationConfig(Integer stationId) {
        return stationConfigMap.get(stationId);
    }

    public PointConfig getPointConfig(Integer stationId, Integer pointId) {
        String key = buildPointKey(stationId, pointId);
        return pointConfigMap.get(key);
    }

    public PointConfig getPointConfigByIoa(Integer stationId, Integer ioa) {
        return pointConfigMap.values().stream()
                .filter(config -> config.getStationId().equals(stationId) && 
                                  config.getIoa() != null && 
                                  config.getIoa().equals(ioa))
                .findFirst()
                .orElse(null);
    }

    public PointConfig getPointConfigByAddress(Integer stationId, Integer address) {
        return pointConfigMap.values().stream()
                .filter(config -> config.getStationId().equals(stationId) && 
                                  config.getAddress() != null && 
                                  config.getAddress().equals(address))
                .findFirst()
                .orElse(null);
    }

    public PointConfig getPointConfigByAddressAndRegisterType(Integer stationId, Integer address, String registerType) {
        return pointConfigMap.values().stream()
                .filter(config -> config.getStationId().equals(stationId)
                        && config.getAddress() != null
                        && config.getAddress().equals(address)
                        && registerTypeMatches(config.getRegisterType(), registerType))
                .findFirst()
                .orElse(null);
    }

    public List<PointConfig> getPointConfigsByStation(Integer stationId) {
        return pointConfigMap.values().stream()
                .filter(config -> config.getStationId().equals(stationId))
                .collect(Collectors.toList());
    }

    public Map<Integer, StationConfig> getAllStationConfigs() {
        return new ConcurrentHashMap<>(stationConfigMap);
    }

    public void clear() {
        stationConfigMap.clear();
        pointConfigMap.clear();
        log.info("清空配置缓存");
    }

    private String buildPointKey(Integer stationId, Integer pointId) {
        return stationId + "_" + pointId;
    }

    private boolean registerTypeMatches(String source, String target) {
        if (source == null || target == null) {
            return source == null && target == null;
        }
        return source.trim().equalsIgnoreCase(target.trim());
    }
}
