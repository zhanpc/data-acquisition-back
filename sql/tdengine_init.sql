-- 数据采集系统 TDengine 时序数据库初始化脚本

-- 创建数据库
CREATE DATABASE IF NOT EXISTS iot_data 
    KEEP 365          -- 保留1年
    DURATION 30       -- 热数据30天
    BUFFER 256        -- 写缓存256MB
    PAGES 256;        -- 元数据缓存

USE iot_data;

-- IEC104 超级表
CREATE STABLE IF NOT EXISTS iec104_data (
    ts TIMESTAMP,
    value DOUBLE,
    quality TINYINT,
    cot SMALLINT
) TAGS (
    station_id INT,
    point_id INT,
    point_name NCHAR(100),
    coa INT,
    ioa INT,
    type_id TINYINT
);

-- Modbus 超级表
CREATE STABLE IF NOT EXISTS modbus_data (
    ts TIMESTAMP,
    value DOUBLE,
    quality TINYINT
) TAGS (
    station_id INT,
    point_id INT,
    point_name NCHAR(100),
    slave_id TINYINT,
    address INT,
    register_type NCHAR(20)
);

-- 为测试场站创建子表
CREATE TABLE IF NOT EXISTS station_001_iec USING iec104_data 
TAGS (1, 0, '场站001', 1, 0, 0);

CREATE TABLE IF NOT EXISTS station_002_modbus USING modbus_data 
TAGS (2, 0, '场站002', 1, 0, 'HOLDING');
