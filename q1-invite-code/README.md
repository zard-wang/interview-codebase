# 邀请码系统（Invite Code System）

一个基于 Spring Boot 的邀请码管理服务。支持创建活动、批量生成邀请码、验证和兑换邀请码。

## 技术栈

- Java 17
- Spring Boot 3.3.6
- Spring Data JPA
- H2 内存数据库
- Maven

## 项目结构

```
src/main/java/com/interview/invitecode/
  entity/          Campaign, InviteCode, Redemption, CodeStatus
  dto/             Request/Response DTOs
  repository/      Spring Data JPA repositories
  service/         InviteCodeService, NotificationService
  controller/      REST API endpoints

src/test/java/com/interview/invitecode/
  InviteCodeServiceTest.java
```

## 业务规则

- 每个邀请码属于一个活动（Campaign），有**使用次数上限**（`maxRedemptions`）
- 每个邀请码有**过期时间**（`expiresAt`），到期当天即视为已过期，不可使用
- 同一用户对同一个邀请码只能兑换**一次**
- 已禁用的邀请码不能验证或兑换

## API

| Method | Path | 说明 |
|--------|------|------|
| POST | `/api/campaigns` | 创建活动 |
| POST | `/api/campaigns/{id}/codes/generate` | 批量生成邀请码 |
| GET | `/api/codes/{code}` | 查询邀请码详情 |
| POST | `/api/codes/{code}/validate` | 验证邀请码是否可用 |
| POST | `/api/codes/{code}/redeem` | 兑换邀请码 |
| GET | `/api/codes/{code}/redemptions` | 查询兑换记录 |

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
