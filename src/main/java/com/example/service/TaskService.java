package com.example.service;

import com.example.dto.CreateTaskRequest;
import com.example.dto.TaskResponse;
import com.example.dto.UpdateTaskRequest;

import java.util.List;
import java.util.UUID;

public interface TaskService {
    TaskResponse create(CreateTaskRequest req);
    TaskResponse get(UUID id);
    List<TaskResponse> list();
    TaskResponse update(UUID id, UpdateTaskRequest req);
    void delete(UUID id);
}
