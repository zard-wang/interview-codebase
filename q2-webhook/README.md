# Webhook 分发服务（Webhook Dispatch Service）

一个基于 Spring Boot 的 Webhook 分发服务。接收内部事件，向已配置的订阅方 URL 发送 HTTP POST 回调，支持失败重试和投递日志。

## 技术栈

- Java 17
- Spring Boot 3.3.6
- Spring Data JPA
- H2 内存数据库
- Maven

## 项目结构

```
src/main/java/com/webhook/
  ├── WebhookDispatchApplication.java
  ├── controller/
  │   ├── SubscriptionController.java
  │   ├── WebhookController.java
  │   ├── DeliveryController.java
  │   └── DispatchRequest.java
  ├── model/
  │   ├── Subscription.java
  │   ├── DeliveryLog.java
  │   ├── DeliveryStatus.java
  │   └── HttpResult.java
  ├── repository/
  │   ├── SubscriptionRepository.java
  │   └── DeliveryLogRepository.java
  └── service/
      ├── WebhookHttpClient.java
      ├── DefaultWebhookHttpClient.java
      ├── DispatchService.java
      ├── SubscriptionService.java
      └── DeliveryLogService.java
```

## 业务规则

- 每个订阅方（Subscription）配置接收 URL、事件类型过滤和启用/禁用状态
- 事件分发时，只向**已启用且事件类型匹配**的订阅方投递
- 投递失败时会**自动重试**，重试耗尽后标记为 EXHAUSTED
- 每次投递的状态和尝试次数记录在投递日志中

## API

| Method | Path | 说明 |
|--------|------|------|
| POST | `/api/subscriptions` | 创建订阅 |
| GET | `/api/subscriptions` | 查询订阅列表 |
| GET | `/api/subscriptions/{id}` | 查询订阅详情 |
| PUT | `/api/subscriptions/{id}/enable` | 启用订阅 |
| PUT | `/api/subscriptions/{id}/disable` | 禁用订阅 |
| POST | `/api/webhooks/dispatch` | 分发事件 |
| GET | `/api/deliveries` | 查询投递日志 |
| GET | `/api/deliveries/{id}` | 查询投递详情 |

## 运行方式

```bash
# 运行测试
mvn test

# 启动服务
mvn spring-boot:run
```

无需外部依赖，H2 内存数据库自动启动。

## 你的任务

1. 运行 `mvn test`，观察哪个测试失败
2. 定位并修复导致测试失败的 Bug
3. 审查代码库，找出并修复其他你能发现的问题
4. 完成后，按面试官给出的需求开发新功能
