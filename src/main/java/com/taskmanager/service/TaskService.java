package com.taskmanager.service;

import com.taskmanager.dto.request.TaskCreateDto;
import com.taskmanager.dto.request.TaskUpdateDto;
import com.taskmanager.dto.response.TaskResponseDto;

import java.util.List;

public interface TaskService {

    TaskResponseDto createTask(TaskCreateDto request, Long userId);

    List<TaskResponseDto> getAllTasks(Long userId);

    TaskResponseDto getTaskById(Long taskId, Long userId);

    TaskResponseDto updateTask(Long taskId, TaskUpdateDto request, Long userId);

    void deleteTask(Long taskId, Long userId);
}
