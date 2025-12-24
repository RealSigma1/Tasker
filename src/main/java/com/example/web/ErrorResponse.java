package com.example.web;

import java.time.Instant;

public record ErrorResponse(
        Instant timestamp,
        int status,
        String error
) {
}
