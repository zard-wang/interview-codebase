package com.example.taskscheduler.service;

import com.example.taskscheduler.model.Task;
import com.example.taskscheduler.model.TaskStatus;
import com.example.taskscheduler.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public Task submitTask(String taskType, String payload) {
        Task task = new Task(taskType, payload);
        return taskRepository.save(task);
    }

    public Optional<Task> getTask(Long id) {
        return taskRepository.findById(id);
    }

    public List<Task> listTasks(TaskStatus status) {
        if (status != null) {
            return taskRepository.findByStatus(status);
        }
        return taskRepository.findAll();
    }

    public Task retryTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + id));

        if (task.getStatus() != TaskStatus.FAILED) {
            throw new IllegalStateException("Only FAILED tasks can be retried, current status: " + task.getStatus());
        }

        task.setStatus(TaskStatus.PENDING);
        task.setResult(null);
        task.setStartedAt(null);
        task.setCompletedAt(null);
        task.setWorkerName(null);
        return taskRepository.save(task);
    }

    public int recoverStuckTasks(int timeoutSeconds) {
        LocalDateTime cutoff = LocalDateTime.now().minusSeconds(timeoutSeconds);
        List<Task> stuckTasks = taskRepository.findByStatusAndStartedAtBefore(TaskStatus.FAILED, cutoff);
        for (Task task : stuckTasks) {
            task.setStatus(TaskStatus.PENDING);
            task.setStartedAt(null);
            task.setWorkerName(null);
            taskRepository.save(task);
        }
        return stuckTasks.size();
    }
}
