package com.example.taskscheduler.service;

import com.example.taskscheduler.model.Task;
import com.example.taskscheduler.model.TaskStatus;
import com.example.taskscheduler.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class WorkerServiceTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public TaskRunner testTaskRunner() {
            return new TaskRunner() {
                @Override
                public String execute(Task task) throws Exception {
                    return "Test result for " + task.getTaskType();
                }
            };
        }
    }

    @Autowired
    private WorkerService workerService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
    }

    @Test
    void testPollAndExecuteSuccess() {
        Task task = taskService.submitTask("EXPORT_DATA", "{\"format\":\"csv\"}");

        workerService.pollAndExecute("worker-1");

        Task updated = taskRepository.findById(task.getId()).orElseThrow();
        assertEquals(TaskStatus.SUCCESS, updated.getStatus());
        assertEquals("worker-1", updated.getWorkerName());
        assertNotNull(updated.getStartedAt());
        assertNotNull(updated.getCompletedAt());
        assertNotNull(updated.getResult());
    }

    @Test
    void testPollNoTasks() {
        workerService.pollAndExecute("worker-1");

        assertEquals(0, taskRepository.count());
    }

    @Test
    void testWorkerPicksOldestTask() {
        Task first = taskService.submitTask("EXPORT_DATA", "{\"id\":1}");
        Task second = taskService.submitTask("GENERATE_REPORT", "{\"id\":2}");

        workerService.pollAndExecute("worker-1");

        Task firstUpdated = taskRepository.findById(first.getId()).orElseThrow();
        Task secondUpdated = taskRepository.findById(second.getId()).orElseThrow();

        assertEquals(TaskStatus.SUCCESS, firstUpdated.getStatus());
        assertEquals(TaskStatus.PENDING, secondUpdated.getStatus());
    }

    @Test
    void testStuckTaskRecovery() {
        Task task = taskService.submitTask("EXPORT_DATA", "{\"format\":\"csv\"}");
        task.setStatus(TaskStatus.RUNNING);
        task.setStartedAt(LocalDateTime.now().minusSeconds(60));
        task.setWorkerName("crashed-worker");
        taskRepository.save(task);

        int recovered = taskService.recoverStuckTasks(30);

        Task updated = taskRepository.findById(task.getId()).orElseThrow();
        assertEquals(1, recovered);
        assertEquals(TaskStatus.PENDING, updated.getStatus());
        assertNull(updated.getStartedAt());
        assertNull(updated.getWorkerName());
    }

    @Test
    void testMultipleWorkersProcessDifferentTasks() {
        Task t1 = taskService.submitTask("EXPORT_DATA", "{}");
        Task t2 = taskService.submitTask("GENERATE_REPORT", "{}");

        workerService.pollAndExecute("worker-1");
        workerService.pollAndExecute("worker-2");

        Task t1Updated = taskRepository.findById(t1.getId()).orElseThrow();
        Task t2Updated = taskRepository.findById(t2.getId()).orElseThrow();

        assertEquals(TaskStatus.SUCCESS, t1Updated.getStatus());
        assertEquals(TaskStatus.SUCCESS, t2Updated.getStatus());
        assertEquals("worker-1", t1Updated.getWorkerName());
        assertEquals("worker-2", t2Updated.getWorkerName());
    }
}
