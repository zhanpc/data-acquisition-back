-- 数据采集系统 TDengine 时序数据库初始化脚本
-- 推荐模型：
-- 1. 超级表按协议区分
-- 2. 子表按“单测点”创建，避免一个场站多个测点共享同一子表
-- 3. table_name 与 MySQL point_config.table_name 一一对应

-- 创建数据库
CREATE DATABASE IF NOT EXISTS iot_data KEEP 365 DURATION 30 BUFFER 256 PAGES 256;

USE iot_data;

-- IEC104 超级表
CREATE STABLE IF NOT EXISTS iec104_data (ts TIMESTAMP, `value` DOUBLE, quality TINYINT, coa INT, type_id SMALLINT) TAGS (station_id INT, point_id INT, point_name NCHAR(100), ioa INT);

-- Modbus 超级表
CREATE STABLE IF NOT EXISTS modbus_data (ts TIMESTAMP, `value` DOUBLE, quality TINYINT, slave_id INT) TAGS (station_id INT, point_id INT, point_name NCHAR(100), address INT, register_type NCHAR(20));

-- IEC104 示例测点子表
CREATE TABLE IF NOT EXISTS pt_1_1 USING iec104_data TAGS (1, 1, '电压', 1001);

CREATE TABLE IF NOT EXISTS pt_1_2 USING iec104_data TAGS (1, 2, '电流', 1002);

CREATE TABLE IF NOT EXISTS pt_1_3 USING iec104_data TAGS (1, 3, '功率', 1003);

-- Modbus 示例测点子表
CREATE TABLE IF NOT EXISTS pt_2_1 USING modbus_data TAGS (2, 1, '温度', 0, 'HOLDING');

CREATE TABLE IF NOT EXISTS pt_2_2 USING modbus_data TAGS (2, 2, '湿度', 1, 'HOLDING');

CREATE TABLE IF NOT EXISTS pt_2_3 USING modbus_data TAGS (2, 3, '压力', 2, 'HOLDING');

CREATE TABLE IF NOT EXISTS pt_3_1 USING modbus_data TAGS (3, 1, '流量', 0, 'HOLDING');
