package com.example.taskscheduler.service;

import com.example.taskscheduler.model.Task;
import com.example.taskscheduler.model.TaskStatus;
import com.example.taskscheduler.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TaskServiceTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
    }

    @Test
    void testSubmitTask() {
        Task task = taskService.submitTask("EXPORT_DATA", "{\"format\":\"csv\"}");

        assertNotNull(task.getId());
        assertEquals("EXPORT_DATA", task.getTaskType());
        assertEquals("{\"format\":\"csv\"}", task.getPayload());
        assertEquals(TaskStatus.PENDING, task.getStatus());
        assertNotNull(task.getCreatedAt());
        assertNull(task.getStartedAt());
        assertNull(task.getCompletedAt());
        assertNull(task.getWorkerName());
        assertNull(task.getResult());
    }

    @Test
    void testGetTask() {
        Task created = taskService.submitTask("GENERATE_REPORT", "{\"type\":\"monthly\"}");

        Optional<Task> found = taskService.getTask(created.getId());

        assertTrue(found.isPresent());
        assertEquals(created.getId(), found.get().getId());
        assertEquals("GENERATE_REPORT", found.get().getTaskType());
        assertEquals("{\"type\":\"monthly\"}", found.get().getPayload());
    }

    @Test
    void testGetTaskNotFound() {
        Optional<Task> found = taskService.getTask(9999L);
        assertTrue(found.isEmpty());
    }

    @Test
    void testListAllTasks() {
        taskService.submitTask("EXPORT_DATA", "{}");
        taskService.submitTask("GENERATE_REPORT", "{}");

        List<Task> tasks = taskService.listTasks(null);
        assertEquals(2, tasks.size());
    }

    @Test
    void testListTasksByStatus() {
        Task t1 = taskService.submitTask("EXPORT_DATA", "{}");
        Task t2 = taskService.submitTask("GENERATE_REPORT", "{}");

        t2.setStatus(TaskStatus.RUNNING);
        taskRepository.save(t2);

        List<Task> pendingTasks = taskService.listTasks(TaskStatus.PENDING);
        assertEquals(1, pendingTasks.size());
        assertEquals(t1.getId(), pendingTasks.get(0).getId());
    }

    @Test
    void testRetryFailedTask() {
        Task task = taskService.submitTask("EXPORT_DATA", "{\"format\":\"csv\"}");
        task.setStatus(TaskStatus.FAILED);
        task.setResult("Error: connection timeout");
        task.setWorkerName("worker-1");
        taskRepository.save(task);

        Task retried = taskService.retryTask(task.getId());

        assertEquals(TaskStatus.PENDING, retried.getStatus());
        assertNull(retried.getResult());
        assertNull(retried.getStartedAt());
        assertNull(retried.getCompletedAt());
        assertNull(retried.getWorkerName());
    }

    @Test
    void testRetryNonFailedTaskThrows() {
        Task task = taskService.submitTask("EXPORT_DATA", "{}");

        assertThrows(IllegalStateException.class, () -> taskService.retryTask(task.getId()));
    }

    @Test
    void testRetryNonExistentTaskThrows() {
        assertThrows(IllegalArgumentException.class, () -> taskService.retryTask(9999L));
    }
}
