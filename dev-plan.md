# 数据采集系统开发计划

> 基于 design-enhanced.md 设计文档，梳理当前实现状态并制定优先级开发计划。
> 原则：**优先跑通主体业务链路**，OPC协议和Web界面后续迭代。

---

## 一、当前实现状态

### 已完成

| 模块 | 文件 | 说明 |
|------|------|------|
| IEC104 协议 | `Iec104Service.java` | 连接管理、ASDU处理、总召唤、遥控命令 |
| Modbus TCP | `ModbusTcpService.java` | 读写保持寄存器、输入寄存器、线圈 |
| Kafka 生产者 | `DataProducer.java` | 发送数据 + WAL备份 + 启动恢复 |
| Kafka 消费者 | `DataConsumer.java` | 批量消费(1000条)、手动提交、写TDengine |
| WAL 管理器 | `WalManager.java` | 写/读/恢复/清理，按日期目录组织 |
| TDengine 存储 | `TDengineService.java` | 批量写入、最新值查询、历史查询、聚合查询 |
| 配置缓存 | `ConfigCache.java` | 内存缓存场站/测点配置，按IOA/地址索引 |
| 数据模型 | `DataPoint.java` | 统一数据模型（含协议特定字段） |
| 数据库初始化 | `mysql_init.sql` | 场站配置、测点配置、告警规则、告警历史表 |
| 系统权限 | system 模块 | 登录、菜单、角色、用户管理（JWT） |

### 部分完成（需补全）

| 模块 | 问题 |
|------|------|
| Modbus RTU | 仅骨架代码（约50行），未实现串口通信逻辑 |
| PointConfig 实体 | 缺少 pointType、collectFreq、scaleFactor、offset、upperLimit、lowerLimit 等字段 |
| StationConfig 实体 | 缺少协议特定参数字段 |
| Kafka 架构 | 单 Topic，设计要求双通道（实时 + 历史补传） |

### 未实现

| 模块 | 说明 |
|------|------|
| OPC UA/DA 协议 | 无任何实现 |
| 配置热重载 | 无 Redis Pub/Sub 配置变更通知 |
| 边缘数据处理 | 无质量校验、阈值监控、异常检测 |
| 告警服务 | 表结构存在，无主动监控服务 |
| 远程控制 | 无命令队列、执行反馈、审计日志 |
| 数据压缩归档 | 无 LZ4 压缩，无 HDD 冷归档 |
| 双通道断点续传 | WAL 存在但无实时/历史双通道分离 |
| Web 管理界面 | 无前端实现 |
| BIM 集成 | 控制器目录为空 |

---

## 二、开发计划（按优先级）

### P0 — 主体业务链路跑通（当前冲刺）

> 目标：IEC104 + Modbus TCP 数据能完整流转：采集 → Kafka → TDengine，配置可从数据库加载。

#### P0-1 修复 PointConfig / StationConfig 实体字段缺失

**问题**：当前实体缺少采集频率、数据类型、量程等关键字段，导致采集逻辑无法按配置运行。

**任务**：
- `PointConfig` 补充字段：`pointType`(遥测/遥信/遥控/遥调)、`collectFreq`(采集频率秒)、`scaleFactor`、`offset`、`upperLimit`、`lowerLimit`、`protocol`、`timeout`、`retryCount`
- `StationConfig` 补充字段：`protocol`、`reconnectInterval`、`maxRetry`
- 同步更新 `mysql_init.sql` 和 MyBatis mapper

**优先级**：P0 | 预估工作量：0.5天

---

#### P0-2 ConfigService 实现 — 从数据库加载配置到缓存

**问题**：`ConfigCache` 已有内存结构，但 `ConfigService` 未实现从 MySQL 加载并填充缓存的逻辑。

**任务**：
- 实现 `ConfigService.loadAll()` — 启动时加载全部场站和测点配置到 `ConfigCache`
- 实现 `ConfigService.reloadStation(stationId)` — 单场站配置刷新
- 在 `DataCollectApplication` 启动后触发 `loadAll()`

**优先级**：P0 | 预估工作量：0.5天

---

#### P0-3 DataAcquisitionService 实现 — 协议调度主循环

**问题**：`DataAcquisitionService.java` 存在但未实现核心调度逻辑（根据配置启动对应协议服务、按频率轮询采集）。

**任务**：
- 读取 `ConfigCache` 中所有场站配置
- 按协议类型分发到 `Iec104Service` / `ModbusTcpService`
- 实现基于 `collectFreq` 的定时采集调度（`ScheduledExecutorService`）
- 采集结果统一封装为 `DataPoint` 发送到 `DataProducer`

**优先级**：P0 | 预估工作量：1天

---

#### P0-4 端到端联调验证

**任务**：
- 用 `mysql_init.sql` 中的测试数据（2个场站）启动系统
- 验证：IEC104 连接 → 数据采集 → Kafka 发送 → 消费写入 TDengine → 查询验证
- 验证 WAL 恢复：模拟 Kafka 不可用，重启后数据补发

**优先级**：P0 | 预估工作量：0.5天

---

### P1 — 稳定性与数据完整性（第二阶段）

#### P1-1 Modbus RTU 补全

**任务**：基于 `jamod` 完成串口通信实现，对齐 Modbus TCP 的功能集（读写寄存器/线圈）。

**优先级**：P1 | 预估工作量：1天

---

#### P1-2 双通道 Kafka 架构

**问题**：设计要求实时通道（小批量100条、低延迟）和历史补传通道（大批量10K条）分离。

**任务**：
- 新增 Kafka Topic：`iot-realtime`（当前数据）、`iot-history`（WAL补传数据）
- `DataProducer` 按数据来源路由到不同 Topic
- `DataConsumer` 分别配置两个消费者，不同批次大小和提交策略

**优先级**：P1 | 预估工作量：1天

---

#### P1-3 配置热重载

**任务**：
- Redis Pub/Sub 订阅配置变更事件
- 收到事件后调用 `ConfigService.reloadStation()` 刷新缓存
- 协议服务感知配置变更后重连或调整采集频率

**优先级**：P1 | 预估工作量：1天

---

#### P1-4 边缘数据处理

**任务**：
- 数据质量校验：超量程、无效值过滤
- 阈值告警：对比 `PointConfig.upperLimit/lowerLimit`，触发写入 `alarm_history`
- 告警去重：同一测点告警状态未恢复时不重复告警

**优先级**：P1 | 预估工作量：1.5天

---

### P2 — 功能完善（第三阶段）

#### P2-1 远程控制命令下发

**任务**：
- 命令 API（遥控/遥调）
- 命令队列 + 执行状态跟踪
- IEC104 单命令/设定值命令实现（`Iec104Service` 已有基础）
- 审计日志写入

**优先级**：P2 | 预估工作量：2天

---

#### P2-2 数据压缩与冷归档

**任务**：
- WAL 文件超过7天后 LZ4 压缩
- 超过30天归档到 HDD 路径
- TDengine TTL 策略配置（热数据90天，冷数据1年）

**优先级**：P2 | 预估工作量：1天

---

#### P2-3 采集状态监控 API

**任务**：
- 场站连接状态查询接口
- 采集点最新值批量查询接口
- 简单的健康检查端点（供运维监控）

**优先级**：P2 | 预估工作量：1天

---

### P3 — 后续迭代（暂不排期）

| 功能 | 说明 |
|------|------|
| OPC UA/DA 协议 | 接入 OPC 设备，约3-5天工作量 |
| Web 管理界面 | 场站配置、测点管理、实时监控、告警查看 |
| BIM 集成 | BimFace SDK 已引入，待需求明确 |
| 多通道数据融合 | 多数据源优先级融合，适用于转发站场景 |

---

## 三、里程碑

| 里程碑 | 包含内容 | 目标 |
|--------|----------|------|
| M1 主链路跑通 | P0-1 ~ P0-4 | IEC104+Modbus TCP 数据完整流转 |
| M2 生产就绪 | P1-1 ~ P1-4 | 稳定采集、数据不丢失、告警可用 |
| M3 功能完整 | P2-1 ~ P2-3 | 远控、归档、监控 API |
| M4 扩展协议 | P3 | OPC、Web界面、BIM |

---

## 四、技术债务备忘

- `PointConfig` / `StationConfig` 实体字段需与数据库同步（当前有偏差）
- Kafka 消费者当前无死信队列，消费失败数据会丢失
- `WalManager` 无文件大小上限，极端情况下可能撑满磁盘
- Spring Boot 版本 2.2.6 较旧，依赖升级需评估兼容性
