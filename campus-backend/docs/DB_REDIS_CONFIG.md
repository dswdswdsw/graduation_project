# 数据库与 Redis 连接配置说明

## 当前状态

项目默认以 **default** 配置文件启动，**不会**建立任何数据库或 Redis 连接。
所有数据源和 Redis 的自动装配类已被排除，项目可以独立运行。

## 如何启用数据库和 Redis 连接

### 方式一：修改 application.yml（推荐）

编辑 `src/main/resources/application.yml`，将 `profiles.active` 改为 `dev`：

```yaml
spring:
  profiles:
    active: dev    # 从 default 改为 dev
```

### 方式二：启动时指定参数

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

或在 IDE 中添加 VM options：
```
-Dspring.profiles.active=dev
```

### 方式三：环境变量

```bash
set SPRING_PROFILES_ACTIVE=dev
mvn spring-boot:run
```

## 启用前需要做的准备

### 1. MySQL 数据库

- 确保 MySQL 服务已启动（默认端口 3306）
- 创建数据库：
```sql
CREATE DATABASE campus DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```
- 执行初始化脚本：`sql/init.sql`
- 修改 `application-dev.yml` 中的连接参数：

| 参数 | 说明 | 默认值 |
|------|------|--------|
| `spring.datasource.url` | JDBC 连接地址 | `jdbc:mysql://localhost:3306/campus` |
| `spring.datasource.username` | 用户名 | `root` |
| `spring.datasource.password` | 密码 | `root` |

### 2. Redis

- 确保 Redis 服务已启动（默认端口 6379）
- 如需密码认证，在 `application-dev.yml` 中设置 `spring.data.redis.password`
- 修改连接参数：

| 参数 | 说明 | 默认值 |
|------|------|--------|
| `spring.data.redis.host` | 主机地址 | `localhost` |
| `spring.data.redis.port` | 端口 | `6379` |
| `spring.data.redis.password` | 密码 | 空 |

## 连接测试功能

启用 dev profile 后，每次启动时会自动执行连接测试：

1. **数据库测试** — 执行 `SELECT 1` 验证连接可用性
2. **Redis 测试** — 写入/读取/删除一个测试 key 验证读写正常

日志输出示例：
```
========================================
  智慧校园服务平台 - 连接测试
  测试时间: 2026-04-20T10:30:00
========================================
[数据库] 连接成功! 测试查询结果: 1
[Redis] 连接成功! 读写测试通过
========================================
  所有连接测试完成，系统启动成功！
========================================
```

## 技术实现原理

### 为什么默认不激活？

在 `application.yml` 中通过 `spring.autoconfigure.exclude` 排除了以下自动配置类：

| 排除的自动配置 | 作用 |
|---------------|------|
| `DataSourceAutoConfiguration` | 禁止自动创建数据源 |
| `RedisAutoConfiguration` | 禁止自动创建 Redis 连接工厂 |
| `MybatisPlusAutoConfiguration` | 禁止 MyBatis-Plus 自动配置 |

同时，以下配置类使用 `@ConditionalOnProperty` 条件注解：

| 类名 | 激活条件 |
|------|---------|
| `MybatisPlusConfig` | `spring.profiles.active=dev` |
| `MyMetaObjectHandler` | `spring.profiles.active=dev` |
| `RedisConfig` | `spring.profiles.active=dev` |
| `ConnectionTestRunner` | `spring.profiles.active=dev` |

这样确保了：
- **default 模式**：零依赖启动，适合前端联调和演示
- **dev 模式**：完整功能，包含 DB 和 Redis 支持
