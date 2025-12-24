package com.example.dto;

import java.time.LocalDateTime;

public record UpdateTaskRequest(
        String title,
        String description,
        Boolean completed,
        LocalDateTime deadline
) {
}
