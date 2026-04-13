# 任务调度服务（Task Scheduler Service）

一个基于 Spring Boot 的异步任务调度服务。用户提交任务，Worker 竞争拉取并执行，支持查询任务状态和执行结果。

## 技术栈

- Java 17
- Spring Boot 3.3.6
- Spring Data JPA
- H2 内存数据库
- Maven

## 项目结构

```
src/main/java/com/example/taskscheduler/
  model/
    Task.java              - 任务实体
    TaskStatus.java        - PENDING / RUNNING / SUCCESS / FAILED
  repository/
    TaskRepository.java
  service/
    TaskService.java       - 任务管理（提交、查询、重试、恢复）
    WorkerService.java     - Worker 拉取和执行任务
    TaskRunner.java        - 模拟任务执行
  controller/
    TaskController.java    - REST API

src/test/java/com/example/taskscheduler/service/
    TaskServiceTest.java
    WorkerServiceTest.java
```

## 业务规则

- 用户提交任务后状态为 PENDING，等待 Worker 拉取
- Worker 拉取任务后状态变为 RUNNING，执行完成变为 SUCCESS 或 FAILED
- 失败的任务可以手动重试（重置为 PENDING）
- 系统应能检测并恢复**卡住的任务**（长时间处于 RUNNING 的任务）

## API

| Method | Path | 说明 |
|--------|------|------|
| POST | `/api/tasks` | 提交新任务 |
| GET | `/api/tasks/{id}` | 查询任务状态和结果 |
| GET | `/api/tasks` | 任务列表（可选 `?status=` 过滤） |
| POST | `/api/tasks/{id}/retry` | 重试失败的任务 |
| POST | `/api/tasks/worker/poll` | Worker 拉取并执行一个任务 |

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
