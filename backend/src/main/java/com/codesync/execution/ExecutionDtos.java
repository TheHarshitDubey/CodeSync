package com.codesync.execution;

import jakarta.validation.constraints.NotBlank;

public class ExecutionDtos {

    public record ExecuteRequest(
            @NotBlank String code,
            @NotBlank String language, // "java" | "python" | "javascript"
            String stdin
    ) {}

    public record ExecuteResponse(
            String stdout,
            String stderr,
            String status,
            Double timeSeconds
    ) {}
}
