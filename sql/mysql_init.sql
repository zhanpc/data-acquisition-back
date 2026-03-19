-- 数据采集系统 MySQL 配置数据库初始化脚本

-- 创建数据库
CREATE DATABASE IF NOT EXISTS iot_config CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE iot_config;

-- 场站配置表
CREATE TABLE IF NOT EXISTS station_config (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    station_id INT UNIQUE NOT NULL COMMENT '场站ID',
    station_name VARCHAR(100) NOT NULL COMMENT '场站名称',
    protocol VARCHAR(20) NOT NULL COMMENT '协议类型: IEC104/MODBUS_TCP/MODBUS_RTU',
    host VARCHAR(50) NOT NULL COMMENT '设备IP地址',
    port INT NOT NULL COMMENT '端口号',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-离线 1-在线',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_status (status),
    INDEX idx_protocol (protocol)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='场站配置表';

-- 测点配置表
CREATE TABLE IF NOT EXISTS point_config (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    station_id INT NOT NULL COMMENT '场站ID',
    point_id INT NOT NULL COMMENT '测点ID',
    point_name VARCHAR(100) NOT NULL COMMENT '测点名称',
    data_type VARCHAR(20) NOT NULL COMMENT '数据类型: FLOAT/INT/BOOL',
    unit VARCHAR(20) COMMENT '单位',
    table_name VARCHAR(100) NOT NULL COMMENT 'TDengine表名',
    ioa INT COMMENT 'IEC104信息对象地址',
    address INT COMMENT 'Modbus寄存器地址',
    register_type VARCHAR(20) COMMENT 'Modbus寄存器类型: HOLDING/INPUT/COIL/DISCRETE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_station_point (station_id, point_id),
    INDEX idx_station (station_id),
    INDEX idx_table_name (table_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测点配置表';

-- 告警规则表
CREATE TABLE IF NOT EXISTS alarm_rule (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    station_id INT NOT NULL COMMENT '场站ID',
    point_id INT NOT NULL COMMENT '测点ID',
    upper_limit DOUBLE COMMENT '上限值',
    lower_limit DOUBLE COMMENT '下限值',
    alarm_level VARCHAR(20) COMMENT '告警级别: INFO/WARNING/ERROR/CRITICAL',
    enabled TINYINT DEFAULT 1 COMMENT '是否启用: 0-禁用 1-启用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_station_point (station_id, point_id),
    INDEX idx_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='告警规则表';

-- 告警历史表
CREATE TABLE IF NOT EXISTS alarm_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    station_id INT NOT NULL COMMENT '场站ID',
    point_id INT NOT NULL COMMENT '测点ID',
    alarm_time TIMESTAMP NOT NULL COMMENT '告警时间',
    alarm_value DOUBLE COMMENT '告警值',
    alarm_level VARCHAR(20) COMMENT '告警级别',
    alarm_message TEXT COMMENT '告警消息',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_station_time (station_id, alarm_time),
    INDEX idx_time (alarm_time),
    INDEX idx_level (alarm_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='告警历史表';

-- 插入测试数据
INSERT INTO station_config (station_id, station_name, protocol, host, port, status) VALUES
(1, '测试场站001', 'IEC104', '192.168.1.100', 2404, 1),
(2, '测试场站002', 'MODBUS_TCP', '192.168.1.101', 502, 1);

INSERT INTO point_config (station_id, point_id, point_name, data_type, unit, table_name, ioa) VALUES
(1, 1, '电压', 'FLOAT', 'V', 'station_001_iec', 1001),
(1, 2, '电流', 'FLOAT', 'A', 'station_001_iec', 1002),
(1, 3, '功率', 'FLOAT', 'kW', 'station_001_iec', 1003);

INSERT INTO point_config (station_id, point_id, point_name, data_type, unit, table_name, address, register_type) VALUES
(2, 1, '温度', 'FLOAT', '℃', 'station_002_modbus', 0, 'HOLDING'),
(2, 2, '湿度', 'FLOAT', '%', 'station_002_modbus', 1, 'HOLDING'),
(2, 3, '压力', 'FLOAT', 'Pa', 'station_002_modbus', 2, 'HOLDING');

INSERT INTO alarm_rule (station_id, point_id, upper_limit, lower_limit, alarm_level, enabled) VALUES
(1, 1, 250.0, 200.0, 'WARNING', 1),
(1, 2, 100.0, 0.0, 'ERROR', 1),
(2, 1, 50.0, -10.0, 'WARNING', 1);
