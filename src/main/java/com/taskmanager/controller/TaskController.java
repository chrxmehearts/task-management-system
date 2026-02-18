package com.taskmanager.controller;

import com.taskmanager.dto.request.TaskCreateDto;
import com.taskmanager.dto.request.TaskUpdateDto;
import com.taskmanager.dto.response.TaskResponseDto;
import com.taskmanager.entity.User;
import com.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    public ResponseEntity<List<TaskResponseDto>> getAllTasks() {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(taskService.getAllTasks(userId));
    }

    @PostMapping
    public ResponseEntity<TaskResponseDto> createTask(@Valid @RequestBody TaskCreateDto request) {
        Long userId = getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.createTask(request, userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDto> getTaskById(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(taskService.getTaskById(id, userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponseDto> updateTask(@PathVariable Long id,
                                                       @Valid @RequestBody TaskUpdateDto request) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(taskService.updateTask(id, request, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        taskService.deleteTask(id, userId);
        return ResponseEntity.noContent().build();
    }

    private Long getCurrentUserId() {
        return ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
    }
}
