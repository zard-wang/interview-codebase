package com.example.taskscheduler.service;

import com.example.taskscheduler.model.Task;
import com.example.taskscheduler.model.TaskStatus;
import com.example.taskscheduler.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WorkerService {

    private final TaskRepository taskRepository;
    private final TaskRunner taskRunner;

    public WorkerService(TaskRepository taskRepository, TaskRunner taskRunner) {
        this.taskRepository = taskRepository;
        this.taskRunner = taskRunner;
    }

    public void pollAndExecute(String workerName) {
        List<Task> pending = taskRepository.findByStatus(TaskStatus.PENDING);
        if (pending.isEmpty()) {
            return;
        }

        Task task = pending.get(0);
        task.setStatus(TaskStatus.RUNNING);
        task.setStartedAt(LocalDateTime.now());
        task.setWorkerName(workerName);
        taskRepository.save(task);

        try {
            String result = taskRunner.execute(task);
            task.setStatus(TaskStatus.SUCCESS);
            task.setResult(result);
        } catch (Exception e) {
            task.setStatus(TaskStatus.FAILED);
            task.setResult("Error: " + e.getMessage());
        }
        task.setCompletedAt(LocalDateTime.now());
        taskRepository.save(task);
    }
}
