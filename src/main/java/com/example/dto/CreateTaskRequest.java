package com.example.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public record CreateTaskRequest(
        @NotBlank String title,
        String description,
        LocalDateTime deadline
) {
}
