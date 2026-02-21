package com.taskmanager.controller;

import com.taskmanager.dto.request.TaskCreateDto;
import com.taskmanager.dto.request.TaskUpdateDto;
import com.taskmanager.dto.response.TaskResponseDto;
import com.taskmanager.entity.User;
import com.taskmanager.repository.UserRepository;
import com.taskmanager.security.JwtTokenProvider;
import com.taskmanager.service.TaskService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
    public String listTasks(HttpSession session, Model model,
                            HttpServletRequest request, HttpServletResponse response) {
        Long userId = getUserIdFromSession(session);
        if (userId == null) return handleUnauthorized(request, response);

        List<TaskResponseDto> tasks = taskService.getAllTasks(userId);
        model.addAttribute("tasks", tasks);
        return "task-list :: taskList";
    }

    @PostMapping("/create")
    public String createTask(@RequestParam("title") String title,
                             @RequestParam(name = "description", required = false) String description,
                             @RequestParam(name = "priority", required = false) String priority,
                             @RequestParam(name = "status", required = false) String status,
                             @RequestParam(name = "dueDate", required = false) String dueDate,
                             HttpSession session, Model model,
                             HttpServletRequest request, HttpServletResponse response) {
        Long userId = getUserIdFromSession(session);
        if (userId == null) return handleUnauthorized(request, response);

        TaskCreateDto dto = TaskCreateDto.builder()
                .title(title)
                .description(description)
                .priority(priority != null && !priority.isBlank() ? priority : "MEDIUM")
                .status(status != null && !status.isBlank() ? status : "TODO")
                .dueDate(dueDate != null && !dueDate.isBlank() ? LocalDate.parse(dueDate) : null)
                .build();

        taskService.createTask(dto, userId);

        List<TaskResponseDto> tasks = taskService.getAllTasks(userId);
        model.addAttribute("tasks", tasks);
        return "task-list :: taskList";
    }

    @PostMapping("/update")
    public String updateTask(@RequestParam("taskId") Long taskId,
                             @RequestParam(name = "title", required = false) String title,
                             @RequestParam(name = "description", required = false) String description,
                             @RequestParam(name = "status", required = false) String status,
                             @RequestParam(name = "priority", required = false) String priority,
                             @RequestParam(name = "dueDate", required = false) String dueDate,
                             HttpSession session, Model model,
                             HttpServletRequest request, HttpServletResponse response) {
        Long userId = getUserIdFromSession(session);
        if (userId == null) return handleUnauthorized(request, response);

        TaskUpdateDto updateDto = TaskUpdateDto.builder()
                .title(title)
                .description(description)
                .status(status)
                .priority(priority)
                .dueDate(dueDate != null && !dueDate.isBlank() ? LocalDate.parse(dueDate) : null)
                .build();

        taskService.updateTask(taskId, updateDto, userId);

        List<TaskResponseDto> tasks = taskService.getAllTasks(userId);
        model.addAttribute("tasks", tasks);
        return "task-list :: taskList";
    }

    @DeleteMapping("/{id}")
    public String deleteTask(@PathVariable("id") Long id, HttpSession session, Model model,
                             HttpServletRequest request, HttpServletResponse response) {
        Long userId = getUserIdFromSession(session);
        if (userId == null) return handleUnauthorized(request, response);

        taskService.deleteTask(id, userId);

        List<TaskResponseDto> tasks = taskService.getAllTasks(userId);
        model.addAttribute("tasks", tasks);
        return "task-list :: taskList";
    }

    @PostMapping("/{id}/done")
    public String markDone(@PathVariable("id") Long id, HttpSession session, Model model,
                           HttpServletRequest request, HttpServletResponse response) {
        Long userId = getUserIdFromSession(session);
        if (userId == null) return handleUnauthorized(request, response);

        TaskUpdateDto updateDto = TaskUpdateDto.builder().status("DONE").build();
        taskService.updateTask(id, updateDto, userId);

        List<TaskResponseDto> tasks = taskService.getAllTasks(userId);
        model.addAttribute("tasks", tasks);
        return "task-list :: taskList";
    }

    @PostMapping("/{id}/status")
    @ResponseBody
    public ResponseEntity<Void> moveTask(@PathVariable("id") Long id,
                                         @RequestParam("status") String status,
                                         HttpSession session) {
        Long userId = getUserIdFromSession(session);
        if (userId == null) return ResponseEntity.status(401).build();
        taskService.updateTask(id, TaskUpdateDto.builder().status(status).build(), userId);
        return ResponseEntity.noContent().build();
    }

    private String handleUnauthorized(HttpServletRequest request, HttpServletResponse response) {
        if ("true".equals(request.getHeader("HX-Request"))) {
            response.setHeader("HX-Redirect", "/login");
            return "fragments :: empty";
        }
        return "redirect:/login";
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
