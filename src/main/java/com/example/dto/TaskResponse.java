package com.example.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record TaskResponse(
        UUID id,
        String title,
        String description,
        boolean completed,
        LocalDateTime createdAt,
        LocalDateTime deadline
) {
}
