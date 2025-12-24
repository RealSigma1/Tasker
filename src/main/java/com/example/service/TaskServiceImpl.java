package com.example.service;

import com.example.domain.Task;
import com.example.domain.TaskRepository;
import com.example.dto.CreateTaskRequest;
import com.example.dto.TaskResponse;
import com.example.dto.UpdateTaskRequest;
import com.example.exceptions.BadRequestException;
import com.example.exceptions.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository repo;
    private final Clock clock;

    public TaskServiceImpl(TaskRepository repo, Clock clock) {
        this.repo = repo;
        this.clock = clock;
    }

    @Override
    @Transactional
    public TaskResponse create(CreateTaskRequest req) {
        String title = trim(req.title());
        if (title.isEmpty()) {
            throw new BadRequestException("title must not be blank");
        }

        String description = trim(req.description());
        LocalDateTime now = LocalDateTime.now(clock);

        Task t = new Task();
        t.setTitle(title);
        t.setDescription(description);
        t.setCompleted(false);
        t.setCreatedAt(now);
        t.setDeadline(req.deadline());

        Task saved = repo.save(t);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse get(UUID id) {
        Task t = repo.findById(id).orElseThrow(() -> new NotFoundException("task not found"));
        return toResponse(t);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> list() {
        return repo.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public TaskResponse update(UUID id, UpdateTaskRequest req) {
        Task t = repo.findById(id).orElseThrow(() -> new NotFoundException("task not found"));

        if (req.title() != null) {
            String title = trim(req.title());
            if (title.isEmpty()) {
                throw new BadRequestException("title must not be blank");
            }
            t.setTitle(title);
        }

        if (req.description() != null) {
            t.setDescription(trim(req.description()));
        }

        if (req.completed() != null) {
            t.setCompleted(req.completed());
        }

        if (req.deadline() != null) {
            t.setDeadline(req.deadline());
        }

        Task saved = repo.save(t);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (!repo.existsById(id)) {
            throw new NotFoundException("task not found");
        }
        repo.deleteById(id);
    }

    private TaskResponse toResponse(Task t) {
        return new TaskResponse(
                t.getId(),
                t.getTitle(),
                t.getDescription(),
                t.isCompleted(),
                t.getCreatedAt(),
                t.getDeadline()
        );
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
