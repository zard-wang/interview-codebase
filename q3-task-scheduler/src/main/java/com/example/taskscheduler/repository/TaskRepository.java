package com.example.taskscheduler.repository;

import com.example.taskscheduler.model.Task;
import com.example.taskscheduler.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByStatus(TaskStatus status);

    List<Task> findByStatusAndStartedAtBefore(TaskStatus status, LocalDateTime before);
}
