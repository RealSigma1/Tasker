package com.example.testsupport;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class ClockTestConfig {

    @Bean
    @Primary
    public Clock clock() {
        return Clock.fixed(Instant.parse("2025-12-20T00:00:00Z"), ZoneOffset.UTC);
    }
}
