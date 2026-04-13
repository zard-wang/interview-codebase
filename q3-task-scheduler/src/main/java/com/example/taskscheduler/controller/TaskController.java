package com.example.taskscheduler.controller;

import com.example.taskscheduler.model.Task;
import com.example.taskscheduler.model.TaskStatus;
import com.example.taskscheduler.service.TaskService;
import com.example.taskscheduler.service.WorkerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;
    private final WorkerService workerService;

    public TaskController(TaskService taskService, WorkerService workerService) {
        this.taskService = taskService;
        this.workerService = workerService;
    }

    @PostMapping
    public ResponseEntity<Task> submitTask(@RequestBody Map<String, String> request) {
        String taskType = request.get("taskType");
        String payload = request.get("payload");

        if (taskType == null || taskType.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        if (payload == null) {
            payload = "{}";
        }

        Task task = taskService.submitTask(taskType, payload);
        return ResponseEntity.status(HttpStatus.CREATED).body(task);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getTask(@PathVariable Long id) {
        return taskService.getTask(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Task>> listTasks(
            @RequestParam(required = false) TaskStatus status) {
        List<Task> tasks = taskService.listTasks(status);
        return ResponseEntity.ok(tasks);
    }

    @PostMapping("/{id}/retry")
    public ResponseEntity<Task> retryTask(@PathVariable Long id) {
        try {
            Task task = taskService.retryTask(id);
            return ResponseEntity.ok(task);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/worker/poll")
    public ResponseEntity<Void> workerPoll(@RequestParam String workerName) {
        workerService.pollAndExecute(workerName);
        return ResponseEntity.ok().build();
    }
}
