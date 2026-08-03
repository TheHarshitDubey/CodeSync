package com.codesync.execution;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.codesync.execution.ExecutionDtos.*;

@RestController
@RequestMapping("/api/execute")
@RequiredArgsConstructor
public class ExecutionController {

    private final Judge0Client judge0Client;

    @PostMapping
    public ResponseEntity<ExecuteResponse> execute(@Valid @RequestBody ExecuteRequest request) {
        return ResponseEntity.ok(judge0Client.execute(request));
    }
}
