package com.taskmanager.mapper;

import com.taskmanager.dto.request.TaskCreateDto;
import com.taskmanager.dto.response.TaskResponseDto;
import com.taskmanager.entity.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TaskMapper {

    @Mapping(source = "user.id", target = "userId")
    TaskResponseDto toResponseDto(Task task);

    Task toEntity(TaskCreateDto taskCreateDto);
}
