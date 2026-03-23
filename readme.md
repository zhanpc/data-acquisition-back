# 数据采集后端复盘手册

## 1. 文档用途

这个 `readme.md` 用于记录当前后端代码的整体结构、关键技术选型、各模块实现方式，以及每次后端代码变更后的复盘记录，方便后续快速回顾项目演进过程。

它和 `AGENTS.md` 不冲突：

- `AGENTS.md` 约束的是协作规则、交付要求、文档同步要求。
- `readme.md` 侧重的是项目本身的代码结构说明和变更复盘沉淀。

后续建议执行规则：

- 每次后端代码改动后，至少更新本文档中的“最近更新记录”。
- 如果改动影响了接口、配置行为、实现状态、阶段计划、风险或验收判断，还要同步更新 `dev-plan-new.md`。
- 如果本次改动非常小，也建议至少在“最近更新记录”中留痕，方便后续追溯。

## 2. 当前项目定位

当前项目是一个基于 Spring Boot 的工业数据采集后端，围绕“场站配置加载 -> 协议建连 -> 数据采集 -> WAL 落盘 -> Kafka 转发 -> TDengine 存储”构建核心链路，同时保留了系统管理能力（登录、用户、角色、菜单）。

## 3. 核心链路

当前主链路可以概括为：

`MySQL 配置库 -> ConfigService/ConfigCache -> ConnectionInitializer -> 各协议处理器 -> DataProducer -> WAL -> Kafka -> DataConsumer -> TDengine`

说明：

- MySQL 负责保存场站、测点、告警等基础配置。
- 启动时由 `ConfigService` 加载配置到 `ConfigCache`。
- `ConnectionInitializer` 在配置加载后自动建立场站连接。
- 协议层通过 `ProtocolHandler` 统一抽象，当前接入 IEC 104、Modbus TCP、Modbus RTU。
- 采集结果统一收敛为 `DataPoint`。
- `DataProducer` 先写 WAL，再发 Kafka。
- `DataConsumer` 从 Kafka 批量消费后写入 TDengine。

## 4. 关键技术

| 分类 | 当前技术 |
| --- | --- |
| 基础框架 | Spring Boot 2.2.x |
| 调度能力 | Spring Scheduling |
| 数据访问 | MyBatis-Plus、MyBatis-Plus-Join |
| 配置库 | MySQL |
| 时序库 | TDengine |
| 消息通道 | Kafka |
| 缓存/登录态 | Redis + Jedis |
| 协议接入 | IEC 104（j60870）、Modbus（jamod） |
| 安全认证 | JWT |
| 接口文档 | Springdoc OpenAPI |
| 序列化/工具 | Jackson、Fastjson、Hutool、Apache Commons |
| 构建方式 | Maven |

## 5. 代码结构总览

### 5.1 目录概览

- `src/main/java/com/maplestone/dataCollect`
  后端主代码目录。
- `src/main/resources/config`
  多环境配置与日志配置。
- `sql`
  MySQL、TDengine 初始化脚本。
- `src/test/java`
  协议相关测试与生成器代码。

### 5.2 Java 包职责

#### `controller`

负责提供 REST 接口。当前已明显落地的是系统管理接口，位于 `controller/system`，包括：

- `LoginController`
- `UserController`
- `RoleController`
- `MenuController`

#### `service`

负责业务逻辑，当前可分为三类：

- `service/config`
  负责配置加载、缓存刷新、运行状态维护。
- `service/protocol`
  负责协议连接、采集、轮询、重连、统一协议分发。
- `service/storage`
  负责 TDengine 写入与时序查询。

#### `kafka`

负责采集数据消息化处理：

- `DataProducer`
  先写 WAL，再投递 Kafka。
- `DataConsumer`
  批量消费 Kafka 数据并写入 TDengine。

#### `wal`

- `WalManager`
  负责本地落盘、启动恢复、消息发送成功后的清理。

#### `cache`

- `ConfigCache`
  负责缓存场站配置和测点配置，减少运行期频繁查库。

#### `dao`

负责实体与 Mapper：

- `dao/entity`
  业务实体，如 `StationConfig`、`PointConfig`、`AlarmRule`。
- `dao/entity/system`
  系统管理相关实体。
- `dao/mapper`
  对应 MyBatis Mapper。

#### `common`

放置公共基础设施：

- `config`
  Web、Redis、Swagger、MyBatis 等配置。
- `interceptor`
  JWT、MyBatis 等拦截器。
- `exception`
  统一异常处理。
- `utils`
  常用工具类。
- `constant`
  系统常量。

#### `pojo`

承载 DTO、VO、统一响应对象和采集数据对象。

## 6. 重点模块怎么实现

### 6.1 启动与调度

- 启动类是 `DataCollectApplication`。
- 开启了 `@EnableScheduling` 和 `@EnableAsync`。
- 项目具备定时轮询和异步处理基础能力。

### 6.2 配置加载与缓存

- `ConfigService` 实现了 `CommandLineRunner`，应用启动时自动从 MySQL 加载启用状态的场站配置和测点配置。
- 配置统一进入 `ConfigCache`，供协议层、初始化逻辑和轮询逻辑复用。
- `refreshCache()` 提供了重新加载缓存的基础入口。

### 6.3 场站初始化建连

- `ConnectionInitializer` 也是启动阶段执行组件。
- 它依赖 `ConfigCache` 中已经加载好的场站配置。
- 启动后会遍历场站，构造 `connectionId`，并调用 `DataAcquisitionService.connect(...)` 建立连接。
- 对于 RTU 一类需要扩展参数的协议，会从 `extraParams` 中解析额外连接参数。

### 6.4 协议抽象与扩展方式

- `ProtocolHandler` 定义了统一接口：获取协议类型、建立连接、断开单连接、断开全部连接。
- `DataAcquisitionService` 在启动时自动收集所有 `ProtocolHandler` 实现，并按 `protocolType` 建立映射。
- 这样新增协议时，原则上只需要新增一个实现类并接入 Spring，而不需要在中心调度处写死分支。

这套设计的价值是：

- 协议扩展成本较低。
- 连接管理入口统一。
- 应用关闭时可以统一执行资源回收。

### 6.5 Modbus 自动轮询与重连

- `ModbusPollScheduler` 通过 `@Scheduled` 定时执行。
- 它会从 `ConfigCache` 遍历 Modbus 场站和测点。
- 根据测点的 `registerType` 和 `address` 选择读取保持寄存器、输入寄存器、线圈或离散输入。
- 当连接异常时，会调用 `DataAcquisitionService.disconnect(...)` 标记断链，并按配置进行自动重连。
- `extraParams` 中的 `unitId/slaveId` 会参与 Modbus 从站寻址。

当前实现特点：

- 已经具备基础自动采集能力。
- 已具备失败断链和定时重连机制。
- 还没有做到寄存器区间合并、频率分层调度等更高阶优化。

### 6.6 数据落盘与消息链路

- 所有采集结果统一使用 `DataPoint` 表示。
- `DataProducer` 发送前先调用 `WalManager.write(...)` 写本地 WAL。
- Kafka 发送成功后删除对应 WAL 文件。
- 如果应用启动时发现历史 WAL，会执行恢复补发。

这个设计的意义是：

- Kafka 暂时异常时，采集数据不会立刻丢失。
- 应用重启后可以尝试恢复未成功发送的数据。

### 6.7 TDengine 写入与查询

- `TDengineService.batchInsert(...)` 会先按 `tableName` 分组。
- 根据 `DataPoint` 是否包含 Modbus 特征字段，选择不同写入 SQL。
- 当前已提供最新值、历史值、聚合查询的基础方法。

当前限制：

- 查询逻辑仍偏向 IEC 104 表结构。
- 多协议统一查询模型还不完整。

### 6.8 系统管理与认证

- `LoginController` 负责登录和当前用户信息查询。
- 登录成功后生成 JWT，并把登录态续期信息写入 Redis。
- `JwtInterceptor` 负责对请求做 token 解析、续期、用户上下文写入。
- 当前系统管理模块已具备基础后台能力，但和采集业务管理模块还不是一回事。

## 7. 配置与脚本

### 7.1 配置文件

- `src/main/resources/config/application.yml`
  基础配置入口。
- `src/main/resources/config/application-*.yml`
  多环境配置。
- `src/main/resources/config/logback.xml`
  日志配置。

### 7.2 初始化脚本

- `sql/mysql_init.sql`
  MySQL 配置库初始化脚本。
- `sql/tdengine_init.sql`
  TDengine 初始化脚本。

## 8. 测试现状

当前测试主要以协议联调和基础集成测试为主，包括：

- `Iec104ServiceTest`
- `ModbusTcpServiceTest`
- `BeaconManageApplicationTests`

现状判断：

- 已有基础测试样例。
- 还缺少更稳定、可重复执行、弱依赖外部设备环境的自动化测试体系。

## 9. 每次后端改动后怎么更新这份文档

建议每次后端代码变动后，至少检查以下几项：

1. 这次改动影响了哪个模块。
2. 主链路有没有变化。
3. 是否引入了新的关键技术、框架、中间件或配置项。
4. 是否改变了接口、内部类型、数据库结构、消息结构。
5. 是否需要同步更新 `dev-plan-new.md`。

建议更新顺序：

1. 先更新“最近更新记录”。
2. 如果改动影响了模块实现方式，再更新第 5、6、7 节对应内容。
3. 如果影响项目交付状态、计划、风险、验收标准，再同步更新 `dev-plan-new.md`。

## 10. 最近更新记录

### 2026-03-23

- 新增本 `readme.md`，用于沉淀后端代码结构、关键技术、模块实现方式和后续变更复盘记录。
- 本次改动仅新增说明文档，不涉及后端业务代码、接口、配置行为和交付状态变更。
- 本次无需更新 `dev-plan-new.md`。

## 11. 后续记录模板

后续每次后端改动后，可以按下面模板追加：

```md
### YYYY-MM-DD

- 改动摘要：
- 影响模块：
- 涉及文件：
- 核心实现：
- 关键技术/组件：
- 对主链路的影响：
- 是否涉及接口/配置/数据结构变化：
- 是否需要同步更新 dev-plan-new.md：
- 风险与后续待办：
```
