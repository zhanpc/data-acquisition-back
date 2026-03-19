package com.maplestone.dataCollect.service.storage;

import com.maplestone.dataCollect.pojo.entity.DataPoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * TDengine 存储服务
 * 负责时序数据的批量写入和查询
 */
@Slf4j
@Service
public class TDengineService {

    @Autowired
    @Qualifier("tdengineJdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    public void batchInsert(List<DataPoint> dataPoints) {
        if (dataPoints == null || dataPoints.isEmpty()) {
            return;
        }

        Map<String, List<DataPoint>> grouped = dataPoints.stream()
                .collect(Collectors.groupingBy(DataPoint::getTableName));

        for (Map.Entry<String, List<DataPoint>> entry : grouped.entrySet()) {
            String tableName = entry.getKey();
            List<DataPoint> points = entry.getValue();

            insertToTable(tableName, points);
        }

        log.info("批量写入 TDengine: {} 条数据", dataPoints.size());
    }

    private void insertToTable(String tableName, List<DataPoint> dataPoints) {
        DataPoint sample = dataPoints.get(0);
        if (isModbusPoint(sample)) {
            insertModbusPoints(tableName, dataPoints);
            return;
        }
        insertIec104Points(tableName, dataPoints);
    }

    private boolean isModbusPoint(DataPoint dataPoint) {
        return dataPoint.getAddress() != null
                || dataPoint.getRegisterType() != null
                || dataPoint.getSlaveId() != null;
    }

    private void insertIec104Points(String tableName, List<DataPoint> dataPoints) {
        String sql = "INSERT INTO " + tableName + " (ts, `value`, quality, coa, type_id) VALUES (?, ?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                DataPoint dp = dataPoints.get(i);
                ps.setTimestamp(1, new Timestamp(dp.getTimestamp()));
                ps.setDouble(2, dp.getValue());
                ps.setInt(3, dp.getQuality() != null ? dp.getQuality() : 0);
                ps.setInt(4, dp.getCoa() != null ? dp.getCoa() : 0);
                ps.setInt(5, dp.getTypeId() != null ? dp.getTypeId() : 0);
            }

            @Override
            public int getBatchSize() {
                return dataPoints.size();
            }
        });
    }

    private void insertModbusPoints(String tableName, List<DataPoint> dataPoints) {
        String sql = "INSERT INTO " + tableName + " (ts, `value`, quality, slave_id) VALUES (?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                DataPoint dp = dataPoints.get(i);
                ps.setTimestamp(1, new Timestamp(dp.getTimestamp()));
                ps.setDouble(2, dp.getValue());
                ps.setInt(3, dp.getQuality() != null ? dp.getQuality() : 0);
                ps.setInt(4, dp.getSlaveId() != null ? dp.getSlaveId() : 1);
            }

            @Override
            public int getBatchSize() {
                return dataPoints.size();
            }
        });
    }

    public Map<String, Object> queryLatestData(Integer stationId, Integer pointId) {
        String sql = "SELECT ts, `value`, quality FROM iec104_data WHERE station_id = ? AND point_id = ? ORDER BY ts DESC LIMIT 1";
        
        try {
            return jdbcTemplate.queryForMap(sql, stationId, pointId);
        } catch (Exception e) {
            log.error("查询最新数据失败: station={}, point={}, 错误: {}", stationId, pointId, e.getMessage());
            return null;
        }
    }

    public List<Map<String, Object>> queryHistoryData(Integer stationId, Integer pointId, Long startTime, Long endTime) {
        String sql = "SELECT ts, `value`, quality FROM iec104_data WHERE station_id = ? AND point_id = ? AND ts >= ? AND ts <= ? ORDER BY ts";
        
        try {
            return jdbcTemplate.queryForList(sql, stationId, pointId, new Timestamp(startTime), new Timestamp(endTime));
        } catch (Exception e) {
            log.error("查询历史数据失败: station={}, point={}, 错误: {}", stationId, pointId, e.getMessage());
            return null;
        }
    }

    public List<Map<String, Object>> queryAggregateData(Integer stationId, Integer pointId, String interval) {
        String sql = "SELECT _wstart AS ts, AVG(`value`) AS avg_value, MAX(`value`) AS max_value, MIN(`value`) AS min_value " +
                     "FROM iec104_data WHERE station_id = ? AND point_id = ? " +
                     "INTERVAL(" + interval + ") ORDER BY ts";
        
        try {
            return jdbcTemplate.queryForList(sql, stationId, pointId);
        } catch (Exception e) {
            log.error("查询聚合数据失败: station={}, point={}, 错误: {}", stationId, pointId, e.getMessage());
            return null;
        }
    }
}
