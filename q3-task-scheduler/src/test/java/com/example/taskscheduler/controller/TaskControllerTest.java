package com.example.taskscheduler.controller;

import com.example.taskscheduler.model.Task;
import com.example.taskscheduler.model.TaskStatus;
import com.example.taskscheduler.repository.TaskRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
    }

    @Test
    void testSubmitTask() throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of("taskType", "EXPORT_DATA", "payload", "{\"format\":\"csv\"}"));

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.taskType").value("EXPORT_DATA"))
                .andExpect(jsonPath("$.payload").value("{\"format\":\"csv\"}"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.startedAt").isEmpty())
                .andExpect(jsonPath("$.completedAt").isEmpty())
                .andExpect(jsonPath("$.workerName").isEmpty());
    }

    @Test
    void testSubmitTaskMissingType() throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of("payload", "{}"));

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSubmitTaskDefaultPayload() throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of("taskType", "GENERATE_REPORT"));

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.payload").value("{}"));
    }

    @Test
    void testGetTask() throws Exception {
        Task task = new Task("EXPORT_DATA", "{\"format\":\"csv\"}");
        task = taskRepository.save(task);

        mockMvc.perform(get("/api/tasks/{id}", task.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(task.getId()))
                .andExpect(jsonPath("$.taskType").value("EXPORT_DATA"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void testGetTaskNotFound() throws Exception {
        mockMvc.perform(get("/api/tasks/{id}", 9999))
                .andExpect(status().isNotFound());
    }

    @Test
    void testListAllTasks() throws Exception {
        taskRepository.save(new Task("EXPORT_DATA", "{}"));
        taskRepository.save(new Task("GENERATE_REPORT", "{}"));

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void testListTasksByStatus() throws Exception {
        taskRepository.save(new Task("EXPORT_DATA", "{}"));
        Task running = new Task("GENERATE_REPORT", "{}");
        running.setStatus(TaskStatus.RUNNING);
        taskRepository.save(running);

        mockMvc.perform(get("/api/tasks").param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].taskType").value("EXPORT_DATA"));
    }

    @Test
    void testRetryFailedTask() throws Exception {
        Task task = new Task("EXPORT_DATA", "{}");
        task.setStatus(TaskStatus.FAILED);
        task.setResult("Error: timeout");
        task = taskRepository.save(task);

        mockMvc.perform(post("/api/tasks/{id}/retry", task.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.result").isEmpty());
    }

    @Test
    void testRetryNonFailedTask() throws Exception {
        Task task = new Task("EXPORT_DATA", "{}");
        task = taskRepository.save(task);

        mockMvc.perform(post("/api/tasks/{id}/retry", task.getId()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRetryNonExistentTask() throws Exception {
        mockMvc.perform(post("/api/tasks/{id}/retry", 9999))
                .andExpect(status().isNotFound());
    }
}
