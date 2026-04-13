package com.example.taskscheduler.service;

import com.example.taskscheduler.model.Task;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class TaskRunner {

    private final Random random = new Random();

    public String execute(Task task) throws Exception {
        int sleepTime = 100 + random.nextInt(401);
        Thread.sleep(sleepTime);

        if (random.nextDouble() < 0.2) {
            throw new RuntimeException("Task execution failed for type: " + task.getTaskType());
        }

        return "Completed " + task.getTaskType() + " with payload hash " + task.getPayload().hashCode();
    }
}
