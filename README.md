# 校园智慧服务平台

基于 Spring Boot + Vue 3 的校园智慧服务平台，提供课程管理、选课抢课、成绩管理、作业管理、通知公告、教室管理、报修工单等功能。

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3 + Vue Router + Pinia + Element Plus + ECharts + Axios |
| 后端 | Spring Boot 3.1.5 + MyBatis-Plus + JWT + Redis |
| 数据库 | MySQL 8.0 + Redis |
| 构建工具 | Vite 5 + Maven |
| 压测工具 | JMeter 5.6.3 |

## 功能模块

- **用户管理**：管理员/教师/学生三角色，登录认证（JWT），个人信息管理
- **课程管理**：课程增删改查，教师关联课程
- **选课系统**：学生选课/退课，Redis 防超卖并发控制
- **成绩管理**：成绩录入/查询/修改，修改日志记录
- **作业管理**：作业发布/提交/批改
- **通知公告**：通知发布/阅读状态追踪
- **教室管理**：教室信息维护，类型/容量/设施管理
- **报修工单**：报修提交/处理/状态跟踪
- **数据看板**：ECharts 可视化统计
- **文件上传**：通用文件上传下载

## 项目结构

```
graduation_project/
├── campus-backend/                # 后端 (Spring Boot)
│   ├── sql/init.sql               # 数据库初始化脚本（建库+建表+数据）
│   ├── redis/redis.conf           # Redis 配置文件
│   ├── src/main/java/com/campus/
│   │   ├── controller/            # 控制层
│   │   ├── service/               # 业务层
│   │   ├── mapper/                # 数据访问层
│   │   ├── entity/                # 实体类
│   │   ├── dto/                   # 请求传输对象
│   │   ├── vo/                    # 视图传输对象
│   │   ├── config/                # 配置类
│   │   ├── interceptor/           # 拦截器
│   │   ├── aspect/                # 切面
│   │   ├── annotation/            # 自定义注解
│   │   ├── common/                # 通用返回类
│   │   ├── exception/             # 异常处理
│   │   └── util/                  # 工具类
│   └── src/main/resources/
│       ├── application.yml        # 主配置
│       └── application-dev.yml    # 开发环境配置
├── campus-frontend/               # 前端 (Vue 3)
│   ├── src/
│   │   ├── api/                   # 接口请求
│   │   ├── views/                 # 页面组件
│   │   ├── layout/                # 布局组件
│   │   ├── router/                # 路由配置
│   │   ├── stores/                # 状态管理
│   │   └── utils/                 # 工具函数
│   └── vite.config.js
└── jmeter-course-selection-concurrent-test.jmx  # JMeter 并发选课压测脚本
```

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- Node.js 16+
- MySQL 8.0+
- Redis

### 1. 初始化数据库

```sql
-- 登录 MySQL 后执行
source campus-backend/sql/init.sql;
```

> `init.sql` 包含建库、建表和全部初始数据，执行后自动创建 `campus` 数据库。

### 2. 启动 Redis

```bash
# 使用项目自带的配置文件启动
redis-server campus-backend/redis/redis.conf
```

> Redis 为纯内存缓存，无需导入数据。应用启动后会自动写入选课库存缓存和看板数据缓存。

### 3. 启动后端

修改 `campus-backend/src/main/resources/application-dev.yml` 中的数据库和 Redis 连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/campus?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: 你的密码
  data:
    redis:
      host: localhost
      port: 6379
```

然后启动：

```bash
cd campus-backend
mvn spring-boot:run
```

后端运行在 http://localhost:8080

### 4. 启动前端

```bash
cd campus-frontend
npm install
npm run dev
```

前端运行在 http://localhost:5173

### 5. 默认账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | 123456 |
| 教师 | teacher1 | 123456 |
| 学生 | student1 | 123456 |

## 并发选课压测

使用 JMeter 打开 `jmeter-course-selection-concurrent-test.jmx`，模拟 10 个学生同时抢 1 门课程的 3 个名额，验证 Redis 分布式锁防超卖效果。

## Redis 在项目中的作用

| 场景 | 说明 |
|------|------|
| 选课库存缓存 | 课程剩余名额缓存到 Redis，选课时原子扣减，防止超卖 |
| 分布式锁 | `setIfAbsent` 实现选课互斥，避免并发重复选课 |
| 看板数据缓存 | 仪表盘统计结果缓存 2-5 分钟，减少数据库查询压力 |
