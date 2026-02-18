package com.taskmanager.controller;

import com.taskmanager.dto.request.TaskCreateDto;
import com.taskmanager.dto.request.TaskUpdateDto;
import com.taskmanager.dto.response.TaskResponseDto;
import com.taskmanager.entity.User;
import com.taskmanager.repository.UserRepository;
import com.taskmanager.security.JwtTokenProvider;
import com.taskmanager.service.TaskService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/ui/tasks")
@RequiredArgsConstructor
@Slf4j
public class UiTaskController {

    private final TaskService taskService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @GetMapping("/list")
    public String listTasks(HttpSession session, Model model) {
        Long userId = getUserIdFromSession(session);
        if (userId == null) return "redirect:/login";

        List<TaskResponseDto> tasks = taskService.getAllTasks(userId);
        model.addAttribute("tasks", tasks);
        return "dashboard :: taskList";
    }

    @PostMapping("/create")
    public String createTask(@RequestParam String title,
                             @RequestParam(required = false) String description,
                             @RequestParam(required = false) String priority,
                             @RequestParam(required = false) String status,
                             @RequestParam(required = false) String dueDate,
                             HttpSession session, Model model) {
        Long userId = getUserIdFromSession(session);
        if (userId == null) return "redirect:/login";

        TaskCreateDto request = TaskCreateDto.builder()
                .title(title)
                .description(description)
                .priority(priority != null && !priority.isBlank() ? priority : "MEDIUM")
                .status(status != null && !status.isBlank() ? status : "TODO")
                .dueDate(dueDate != null && !dueDate.isBlank() ? LocalDate.parse(dueDate) : null)
                .build();

        taskService.createTask(request, userId);

        List<TaskResponseDto> tasks = taskService.getAllTasks(userId);
        model.addAttribute("tasks", tasks);
        return "dashboard :: taskList";
    }

    @DeleteMapping("/{id}")
    public String deleteTask(@PathVariable Long id, HttpSession session, Model model) {
        Long userId = getUserIdFromSession(session);
        if (userId == null) return "redirect:/login";

        taskService.deleteTask(id, userId);

        List<TaskResponseDto> tasks = taskService.getAllTasks(userId);
        model.addAttribute("tasks", tasks);
        return "dashboard :: taskList";
    }

    @PostMapping("/{id}/done")
    public String markDone(@PathVariable Long id, HttpSession session, Model model) {
        Long userId = getUserIdFromSession(session);
        if (userId == null) return "redirect:/login";

        TaskUpdateDto updateDto = TaskUpdateDto.builder().status("DONE").build();
        taskService.updateTask(id, updateDto, userId);

        List<TaskResponseDto> tasks = taskService.getAllTasks(userId);
        model.addAttribute("tasks", tasks);
        return "dashboard :: taskList";
    }

    private Long getUserIdFromSession(HttpSession session) {
        String token = (String) session.getAttribute("jwt_token");
        if (token == null) return null;

        try {
            String username = jwtTokenProvider.extractUsername(token);
            return userRepository.findByUsername(username)
                    .map(User::getId)
                    .orElse(null);
        } catch (Exception e) {
            log.error("Failed to extract user from session JWT: {}", e.getMessage());
            return null;
        }
    }
}
