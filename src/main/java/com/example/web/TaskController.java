package com.example.web;

import com.example.dto.CreateTaskRequest;
import com.example.dto.TaskResponse;
import com.example.dto.UpdateTaskRequest;
import com.example.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService service;

    public TaskController(TaskService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody CreateTaskRequest req) {
        TaskResponse created = service.create(req);
        return ResponseEntity
                .created(URI.create("/api/tasks/" + created.id()))
                .body(created);
    }

    @GetMapping("/{id}")
    public TaskResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @GetMapping
    public List<TaskResponse> list() {
        return service.list();
    }

    @PutMapping("/{id}")
    public TaskResponse update(@PathVariable UUID id, @RequestBody UpdateTaskRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
