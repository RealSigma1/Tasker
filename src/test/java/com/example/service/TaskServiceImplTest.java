package com.example.service;

import com.example.domain.Task;
import com.example.domain.TaskRepository;
import com.example.dto.CreateTaskRequest;
import com.example.dto.TaskResponse;
import com.example.dto.UpdateTaskRequest;
import com.example.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TaskServiceImplTest {

    private TaskRepository repo;
    private Clock clock;
    private TaskServiceImpl service;

    @BeforeEach
    void setUp() {
        repo = mock(TaskRepository.class);

        clock = Clock.fixed(Instant.parse("2025-12-24T00:00:00Z"), ZoneId.systemDefault());

        service = new TaskServiceImpl(repo, clock);
    }

    @Test
    void create_createsTaskAndReturnsResponse() {
        LocalDateTime dl = LocalDateTime.parse("2025-12-31T12:00:00");

        Task saved = new Task();
        UUID id = UUID.randomUUID();
        saved.setId(id);
        saved.setTitle("Title");
        saved.setDescription("Desc");
        saved.setCompleted(false);
        saved.setCreatedAt(LocalDateTime.now(clock));
        saved.setDeadline(dl);

        when(repo.save(any(Task.class))).thenReturn(saved);

        TaskResponse resp = service.create(new CreateTaskRequest("Title", "Desc", dl));

        assertEquals(id, resp.id());
        assertEquals("Title", resp.title());
        assertEquals("Desc", resp.description());
        assertFalse(resp.completed());
        assertEquals(LocalDateTime.now(clock), resp.createdAt());
        assertEquals(dl, resp.deadline());

        ArgumentCaptor<Task> cap = ArgumentCaptor.forClass(Task.class);
        verify(repo).save(cap.capture());
        Task toSave = cap.getValue();
        assertEquals("Title", toSave.getTitle());
        assertEquals("Desc", toSave.getDescription());
        assertFalse(toSave.isCompleted());
        assertEquals(LocalDateTime.now(clock), toSave.getCreatedAt());
        assertEquals(dl, toSave.getDeadline());
    }

    @Test
    void get_returnsTask() {
        UUID id = UUID.randomUUID();

        Task t = new Task();
        t.setId(id);
        t.setTitle("A");
        t.setDescription("B");
        t.setCompleted(false);
        t.setCreatedAt(LocalDateTime.parse("2025-12-20T10:00:00"));
        t.setDeadline(null);

        when(repo.findById(id)).thenReturn(Optional.of(t));

        TaskResponse resp = service.get(id);

        assertEquals(id, resp.id());
        assertEquals("A", resp.title());
        assertEquals("B", resp.description());
        assertEquals(LocalDateTime.parse("2025-12-20T10:00:00"), resp.createdAt());
        assertNull(resp.deadline());
    }

    @Test
    void list_returnsAll() {
        Task t1 = new Task();
        t1.setId(UUID.randomUUID());
        t1.setTitle("T1");
        t1.setDescription("D1");
        t1.setCompleted(false);
        t1.setCreatedAt(LocalDateTime.parse("2025-12-20T10:00:00"));
        t1.setDeadline(null);

        Task t2 = new Task();
        t2.setId(UUID.randomUUID());
        t2.setTitle("T2");
        t2.setDescription("D2");
        t2.setCompleted(true);
        t2.setCreatedAt(LocalDateTime.parse("2025-12-21T10:00:00"));
        t2.setDeadline(LocalDateTime.parse("2025-12-31T00:00:00"));

        when(repo.findAll()).thenReturn(List.of(t1, t2));

        List<TaskResponse> res = service.list();

        assertEquals(2, res.size());
        assertEquals("T1", res.get(0).title());
        assertEquals("T2", res.get(1).title());
        assertEquals(LocalDateTime.parse("2025-12-31T00:00:00"), res.get(1).deadline());
    }

    @Test
    void update_updatesFields() {
        UUID id = UUID.randomUUID();

        Task existing = new Task();
        existing.setId(id);
        existing.setTitle("Old");
        existing.setDescription("OldDesc");
        existing.setCompleted(false);
        existing.setCreatedAt(LocalDateTime.parse("2025-12-20T10:00:00"));
        existing.setDeadline(null);

        when(repo.findById(id)).thenReturn(Optional.of(existing));
        when(repo.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        LocalDateTime dl = LocalDateTime.parse("2026-01-01T09:30:00");
        TaskResponse resp = service.update(id, new UpdateTaskRequest("New", null, Boolean.TRUE, dl));

        assertEquals("New", resp.title());
        assertEquals("OldDesc", resp.description());
        assertTrue(resp.completed());
        assertEquals(dl, resp.deadline());
    }

    @Test
    void delete_throwsIfNotExists() {
        UUID id = UUID.randomUUID();
        when(repo.existsById(id)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> service.delete(id));
    }
}
