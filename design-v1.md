# 数据采集系统架构设计文档

## 项目概述

基于IEC 60870-5-104和Modbus协议的工业数据采集系统，支持大规模场站接入和海量时序数据存储。

### 硬件配置
- **CPU**: 国产化CPU，16核
- **内存**: 128GB
- **存储**: 
  - 2×1TB SSD (RAID1) - 热数据存储
  - 2×8TB HDD (RAID5) - 冷数据归档
- **网络**: 6个千兆电口，2个千兆光口
- **电源**: 冗余电源
- **管理**: 1个IPMI管理网口

### 数据规模
- **测试阶段**: 2个场站，2TB/年
- **扩展目标**: 20个场站，20TB/年
- **最终目标**: 200个场站，200TB/年

---

## 系统架构

### 整体架构图

```
┌─────────────────────────────────────────────────────────────┐
│                        采集层                                │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌──────────────┐              ┌──────────────┐           │
│  │  IEC 104     │              │   Modbus     │           │
│  │  Service     │              │   Service    │           │
│  │              │              │              │           │
│  │ - 连接管理    │              │ - TCP/RTU    │           │
│  │ - 数据解析    │              │ - 寄存器读取  │           │
│  │ - 总召唤      │              │ - 线圈控制    │           │
│  └──────┬───────┘              └──────┬───────┘           │
│         │                             │                    │
│         └──────────┬──────────────────┘                    │
│                    ▼                                        │
│         ┌────────────────────┐                             │
│         │  ConfigCache       │                             │
│         │  (内存配置缓存)     │                             │
│         │  - 场站配置         │                             │
│         │  - 测点映射         │                             │
│         │  - 告警规则         │                             │
│         └────────┬───────────┘                             │
│                  ▼                                          │
│         ┌────────────────────┐                             │
│         │  DataProducer      │                             │
│         │  - 本地WAL备份      │                             │
│         │  - Kafka生产者      │                             │
│         └────────┬───────────┘                             │
└──────────────────┼─────────────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────────────┐
│                      消息队列层                              │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│              ┌──────────────────────┐                       │
│              │      Kafka           │                       │
│              │  Topic: iot-data     │                       │
│              │                      │                       │
│              │  配置:                │                       │
│              │  - acks=all          │                       │
│              │  - 副本数=1(测试)     │                       │
│              │  - 保留7天            │                       │
│              │  - 压缩: snappy       │                       │
│              └──────────┬───────────┘                       │
└─────────────────────────┼───────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│                      消费层                                  │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│         ┌─────────────────────────┐                         │
│         │   DataConsumer          │                         │
│         │   - 批量消费(1000条/批)  │                         │
│         │   - 手动提交offset       │                         │
│         │   - 异常重试             │                         │
│         └──────────┬──────────────┘                         │
└────────────────────┼────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                      存储层                                  │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌──────────────────────┐      ┌──────────────────────┐   │
│  │     TDengine         │      │       MySQL          │   │
│  │   (时序数据库)        │      │    (配置数据库)       │   │
│  ├──────────────────────┤      ├──────────────────────┤   │
│  │ 存储内容:             │      │ 存储内容:             │   │
│  │ • 测点实时值          │      │ • 场站配置            │   │
│  │ • 历史曲线数据        │      │ • 测点配置            │   │
│  │ • 统计聚合数据        │      │ • 告警规则            │   │
│  │ • 告警事件记录        │      │ • 用户权限            │   │
│  │                      │      │ • 告警历史            │   │
│  ├──────────────────────┤      ├──────────────────────┤   │
│  │ 特性:                 │      │ 特性:                 │   │
│  │ • 10:1压缩比          │      │ • 关系查询            │   │
│  │ • 自动分区            │      │ • 事务支持            │   │
│  │ • TTL管理(1年)        │      │ • 永久存储            │   │
│  │ • 超级表设计          │      │ • 配置管理            │   │
│  │                      │      │                      │   │
│  ├──────────────────────┤      ├──────────────────────┤   │
│  │ 数据量: TB级          │      │ 数据量: GB级          │   │
│  │ 查询: 时间范围        │      │ 查询: JOIN/GROUP BY   │   │
│  │ 位置: SSD(热) + HDD(冷)│     │ 位置: SSD             │   │
│  └──────────────────────┘      └──────────────────────┘   │
│           ▲                              ▲                 │
│           │                              │                 │
│           └──────────┬───────────────────┘                 │
└──────────────────────┼─────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                      应用层                                  │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌────────────────────────────────────────────────┐        │
│  │         Spring Boot REST API                   │        │
│  ├────────────────────────────────────────────────┤        │
│  │ • 实时数据查询 (TDengine)                       │        │
│  │ • 历史曲线查询 (TDengine)                       │        │
│  │ • 统计分析 (TDengine聚合)                       │        │
│  │ • 配置管理 (MySQL CRUD)                         │        │
│  │ • 告警管理 (MySQL + TDengine)                   │        │
│  │ • 用户认证 (MySQL + JWT)                        │        │
│  └────────────────────────────────────────────────┘        │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## 数据流详解

### 1. 实时数据采集流程

```
步骤1: 设备数据采集
IEC104/Modbus设备 → 采集服务接收原始报文

步骤2: 数据解析
采集服务 → 解析协议 → 提取测点数据

步骤3: 配置映射
查询内存缓存 → 获取测点配置（不访问MySQL）

步骤4: 数据转换
原始数据 + 配置信息 → DataPoint对象

步骤5: 本地备份
写入本地WAL文件（防止进程崩溃）

步骤6: 发送消息
DataPoint → Kafka (acks=all, 确保不丢失)

步骤7: 批量消费
Kafka Consumer → 批量拉取1000条

步骤8: 持久化存储
批量写入TDengine → 提交Kafka offset

步骤9: 清理备份
删除已成功写入的WAL文件
```

### 2. 配置管理流程

```
初始化阶段:
管理员 → MySQL录入配置 → 采集服务启动 → 加载到内存缓存

运行阶段:
采集服务 → 仅读取内存缓存 → 不访问MySQL

配置变更:
管理员 → 修改MySQL → 发送刷新信号 → 重新加载缓存
```

### 3. 查询流程

```
实时数据查询:
前端 → API → 从TDengine查询最新值 → 返回

历史曲线查询:
前端 → API → MySQL获取测点配置 → TDengine查询历史数据 → 组合返回

配置查询:
前端 → API → MySQL查询 → 返回
```

---

## 核心组件设计

### 1. 数据模型

#### DataPoint (数据点实体)
```java
public class DataPoint {
    private Integer stationId;      // 场站ID
    private Integer pointId;        // 测点ID
    private String pointName;       // 测点名称
    private Long timestamp;         // 时间戳
    private Double value;           // 测点值
    private Integer quality;        // 数据质量
    private String tableName;       // TDengine表名
    
    // IEC104特有字段
    private Integer coa;            // 公共地址
    private Integer ioa;            // 信息对象地址
    private Integer typeId;         // 类型标识
    
    // Modbus特有字段
    private Integer slaveId;        // 从站地址
    private Integer address;        // 寄存器地址
    private String registerType;    // 寄存器类型
}
```

#### StationConfig (场站配置)
```java
public class StationConfig {
    private Integer id;
    private Integer stationId;
    private String stationName;
    private String protocol;        // IEC104/MODBUS_TCP/MODBUS_RTU
    private String host;
    private Integer port;
    private Integer status;         // 0-离线 1-在线
    private Date createdAt;
}
```

#### PointConfig (测点配置)
```java
public class PointConfig {
    private Integer id;
    private Integer stationId;
    private Integer pointId;
    private String pointName;
    private String dataType;        // FLOAT/INT/BOOL
    private String unit;            // 单位
    private String tableName;       // TDengine表名
    private Integer ioa;            // IEC104信息对象地址
    private Integer address;        // Modbus寄存器地址
}
```

### 2. TDengine表结构

```sql
-- 创建数据库
CREATE DATABASE iot_data 
    KEEP 365          -- 保留1年
    DURATION 30       -- 热数据30天
    BUFFER 256        -- 写缓存256MB
    PAGES 256;        -- 元数据缓存

USE iot_data;

-- IEC104超级表
CREATE STABLE iec104_data (
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

-- Modbus超级表
CREATE STABLE modbus_data (
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

-- 为每个场站创建子表
CREATE TABLE station_001_iec USING iec104_data 
TAGS (1, 0, '场站001', 1, 0, 0);

CREATE TABLE station_002_iec USING iec104_data 
TAGS (2, 0, '场站002', 1, 0, 0);
```

### 3. MySQL表结构

```sql
-- 场站配置表
CREATE TABLE station_config (
    id INT PRIMARY KEY AUTO_INCREMENT,
    station_id INT UNIQUE NOT NULL,
    station_name VARCHAR(100) NOT NULL,
    protocol VARCHAR(20) NOT NULL,
    host VARCHAR(50) NOT NULL,
    port INT NOT NULL,
    status TINYINT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 测点配置表
CREATE TABLE point_config (
    id INT PRIMARY KEY AUTO_INCREMENT,
    station_id INT NOT NULL,
    point_id INT NOT NULL,
    point_name VARCHAR(100) NOT NULL,
    data_type VARCHAR(20) NOT NULL,
    unit VARCHAR(20),
    table_name VARCHAR(100) NOT NULL,
    ioa INT,
    address INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_station_point (station_id, point_id),
    INDEX idx_station (station_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 告警规则表
CREATE TABLE alarm_rule (
    id INT PRIMARY KEY AUTO_INCREMENT,
    station_id INT NOT NULL,
    point_id INT NOT NULL,
    upper_limit DOUBLE,
    lower_limit DOUBLE,
    alarm_level VARCHAR(20),
    enabled TINYINT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_station_point (station_id, point_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 告警历史表
CREATE TABLE alarm_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    station_id INT NOT NULL,
    point_id INT NOT NULL,
    alarm_time TIMESTAMP NOT NULL,
    alarm_value DOUBLE,
    alarm_level VARCHAR(20),
    alarm_message TEXT,
    INDEX idx_station_time (station_id, alarm_time),
    INDEX idx_time (alarm_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

## 数据不丢失保障机制

### 三层保障

```
第一层: 本地WAL (Write-Ahead Log)
采集数据 → 先写本地文件 → 再发送Kafka
作用: 防止进程崩溃导致数据丢失

第二层: Kafka持久化
生产者配置:
- acks=1 
- retries=10 (最多重试10次)
- retry.backoff.ms=100 (重试间隔100ms)
- enable.idempotence=true (幂等性)
- max.in.flight.requests.per.connection=5 (配合幂等性使用)

Broker配置:
- replication.factor=1 (测试阶段)
- log.flush.interval.messages=1 (立即刷盘)
- unclean.leader.election=false (禁止脏选举)

第三层: 消费者手动提交
消费者配置:
- enable.auto.commit=false (手动提交)
- 写入TDengine成功后才提交offset
- 失败自动重试
```

### 故障恢复

| 故障场景 | 恢复机制 | 数据丢失 |
|---------|---------|---------|
| 采集服务崩溃 | 从WAL恢复未发送数据 | 0 |
| Kafka崩溃 | 数据已持久化到磁盘 | 0 |
| 消费者崩溃 | offset未提交，重新消费 | 0 |
| 断电 | WAL + Kafka持久化 | 0 |
| 网络抖动 | 自动重试机制 | 0 |

---

## 性能指标

### 吞吐量

| 阶段 | 场站数 | 数据点/秒 | 数据量/天 | 压缩后 |
|------|--------|----------|----------|--------|
| 测试 | 2 | 2,000 | 5.5GB | 550MB |
| 扩展 | 20 | 20,000 | 55GB | 5.5GB |
| 生产 | 200 | 200,000 | 550GB | 55GB |

### 延迟

| 环节 | 延迟 |
|------|------|
| 数据采集 | <10ms |
| 配置查询(内存) | <1ms |
| WAL写入 | ~2ms |
| Kafka发送 | ~5ms |
| TDengine写入 | ~10ms |
| **端到端总延迟** | **<30ms** |

### 资源占用

| 组件 | CPU | 内存 | 磁盘 |
|------|-----|------|------|
| 采集服务 | 2核 | 8GB | - |
| Kafka | 2核 | 4GB | 100GB(SSD) |
| TDengine | 8核 | 64GB | 500GB(SSD) + 14TB(HDD) |
| MySQL | 2核 | 8GB | 50GB(SSD) |
| Redis | 2核 | 16GB | - |
| **总计** | 16核 | 100GB | - |

---

## 磁盘分区规划

### SSD RAID1 (1TB可用)

```
/dev/sda1  100GB   /                  # 系统
/dev/sda2  100GB   /var/lib/mysql     # MySQL数据
/dev/sda3  500GB   /data/tdengine     # TDengine热数据
/dev/sda4  100GB   /data/kafka        # Kafka日志
/dev/sda5  100GB   /data/redis        # Redis持久化
/dev/sda6  100GB   /data/wal          # 本地WAL备份
```

### HDD RAID5 (约14TB可用)

```
/dev/sdb1  14TB    /archive           # TDengine冷数据归档
```

---

## 部署方案

### Docker Compose部署

```yaml
version: '3'
services:
  zookeeper:
    image: wurstmeister/zookeeper
    ports:
      - "2181:2181"
    volumes:
      - /data/zookeeper:/data
    restart: always
      
  kafka:
    image: wurstmeister/kafka
    ports:
      - "9092:9092"
    environment:
      KAFKA_ADVERTISED_HOST_NAME: localhost
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_LOG_DIRS: /kafka/logs
      KAFKA_LOG_RETENTION_HOURS: 168
      KAFKA_AUTO_CREATE_TOPICS_ENABLE: 'true'
      KAFKA_DEFAULT_REPLICATION_FACTOR: 1
      KAFKA_MIN_INSYNC_REPLICAS: 1
      KAFKA_LOG_FLUSH_INTERVAL_MESSAGES: 1
    volumes:
      - /data/kafka:/kafka/logs
    depends_on:
      - zookeeper
    restart: always
      
  tdengine:
    image: tdengine/tdengine:latest
    ports:
      - "6030:6030"
      - "6041:6041"
    environment:
      TZ: Asia/Shanghai
    volumes:
      - /data/tdengine:/var/lib/taos
      - /archive:/var/lib/taos/archive
    restart: always
      
  mysql:
    image: mysql:8.0
    ports:
      - "3306:3306"
    environment:
      MYSQL_ROOT_PASSWORD: your_password
      MYSQL_DATABASE: iot_config
      TZ: Asia/Shanghai
    volumes:
      - /var/lib/mysql:/var/lib/mysql
    restart: always
      
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    command: redis-server --appendonly yes --maxmemory 16gb
    volumes:
      - /data/redis:/data
    restart: always
```

---

## 监控告警

### 关键监控指标

**采集层**
- 连接状态: 在线场站数/总场站数
- 采集速率: 实际点数/秒 vs 预期点数/秒
- 数据延迟: 采集时间戳 vs 当前时间

**消息队列层**
- Kafka消费延迟: lag监控
- 消息积压: 队列深度
- 生产者成功率: 发送成功/总发送

**存储层**
- TDengine写入速率: 点/秒
- 磁盘使用率: SSD <80%, HDD <70%
- 查询响应时间: P99 <100ms

**系统层**
- CPU使用率: <70%
- 内存使用率: <80%
- 网络流量: 入/出带宽

### 告警规则

| 指标 | 阈值 | 级别 |
|------|------|------|
| 场站离线 | 任意场站 | 紧急 |
| 采集速率下降 | <80%预期 | 警告 |
| 数据延迟 | >1秒 | 警告 |
| Kafka消费延迟 | >10000条 | 严重 |
| 磁盘使用率 | >85% | 警告 |
| 内存使用率 | >90% | 严重 |

---

## 扩展路线图

### 阶段1: 测试验证 (2个场站)
- 单机部署
- Kafka单节点
- TDengine单节点
- 验证数据不丢失
- 性能基准测试

### 阶段2: 小规模生产 (20个场站)
- 保持单机部署
- 监控资源使用
- 优化批量写入
- 完善告警规则

### 阶段3: 大规模生产 (200个场站)
- Kafka集群 (3-5节点)
- TDengine集群 (3节点)
- 采集服务集群 (5节点)
- 负载均衡
- 高可用部署

---

## 技术栈总结

| 层次 | 技术选型 | 版本 | 作用 |
|------|---------|------|------|
| 采集协议 | j60870 | 1.7.2 | IEC 104协议 |
| 采集协议 | jamod | 1.2 | Modbus协议 |
| 应用框架 | Spring Boot | 2.2.6 | 后端框架 |
| 消息队列 | Kafka | 3.x | 数据缓冲 |
| 时序数据库 | TDengine | 3.x | 时序数据存储 |
| 关系数据库 | MySQL | 8.0 | 配置存储 |
| 缓存 | Redis | 7.x | 实时缓存 |
| ORM | MyBatis-Plus | 3.3.1 | 数据访问 |
| 通信 | Netty | 4.1.90 | 网络通信 |

---

## 总结

### 核心设计原则

1. **数据不丢失**: WAL + Kafka + 手动提交三层保障
2. **高性能**: 内存缓存 + 批量写入 + 时序数据库
3. **可扩展**: 从2个场站平滑扩展到200个场站
4. **低延迟**: 端到端延迟<30ms
5. **易运维**: 单机部署，配置简单

### 关键特性

- ✅ 支持IEC 104和Modbus协议
- ✅ 数据零丢失保障
- ✅ 10:1数据压缩比
- ✅ 配置与数据分离
- ✅ 自动故障恢复
- ✅ 完善的监控告警
- ✅ 国产化硬件适配

---

**文档版本**: v1.0  
**更新日期**: 2026-03-16  
**维护人**: 开发团队
