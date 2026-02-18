package com.taskmanager.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskCreateDto {

    @NotBlank
    private String title;

    private String description;

    private String status;

    private String priority;

    private LocalDate dueDate;
}
