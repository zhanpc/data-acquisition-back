package com.maplestone.dataCollect.service.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.maplestone.dataCollect.cache.ConfigCache;
import com.maplestone.dataCollect.dao.entity.PointConfig;
import com.maplestone.dataCollect.dao.entity.StationConfig;
import com.maplestone.dataCollect.dao.mapper.PointConfigMapper;
import com.maplestone.dataCollect.dao.mapper.StationConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 配置服务 - 负责加载和管理配置
 */
@Slf4j
@Service
@Order(1)
public class ConfigService implements CommandLineRunner {

    @Autowired
    private StationConfigMapper stationConfigMapper;

    @Autowired
    private PointConfigMapper pointConfigMapper;

    @Autowired
    private ConfigCache configCache;

    @Override
    public void run(String... args) throws Exception {
        log.info("应用启动，开始加载配置到缓存...");
        loadAllConfigs();
    }

    public void loadAllConfigs() {
        loadStationConfigs();
        loadPointConfigs();
    }

    public void loadStationConfigs() {
        QueryWrapper<StationConfig> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);
        List<StationConfig> configs = stationConfigMapper.selectList(wrapper);
        configCache.loadStationConfigs(configs);
    }

    public void loadPointConfigs() {
        List<PointConfig> configs = pointConfigMapper.selectList(null);
        configCache.loadPointConfigs(configs);
    }

    public void refreshCache() {
        log.info("刷新配置缓存...");
        configCache.clear();
        loadAllConfigs();
    }

    public StationConfig getStationConfig(Integer stationId) {
        return configCache.getStationConfig(stationId);
    }

    public PointConfig getPointConfig(Integer stationId, Integer pointId) {
        return configCache.getPointConfig(stationId, pointId);
    }
}
