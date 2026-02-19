package com.taskmanager.controller;

import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.UserRepository;
import com.taskmanager.security.JwtTokenProvider;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class StatsController {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/stats")
    public String statsPage(HttpSession session, Model model) {
        Long userId = getUserIdFromSession(session);
        if (userId == null) return "redirect:/login";

        List<Task> tasks = taskRepository.findAllByUserId(userId);
        LocalDate today = LocalDate.now();

        long todo       = count(tasks, "TODO",        null);
        long inProgress = count(tasks, "IN_PROGRESS", null);
        long done       = count(tasks, "DONE",        null);

        long high   = countPriority(tasks, "HIGH");
        long medium = countPriority(tasks, "MEDIUM");
        long low    = countPriority(tasks, "LOW");

        long highDone       = count(tasks, "DONE",        "HIGH");
        long highInProgress = count(tasks, "IN_PROGRESS", "HIGH");
        long highTodo       = count(tasks, "TODO",        "HIGH");

        long mediumDone       = count(tasks, "DONE",        "MEDIUM");
        long mediumInProgress = count(tasks, "IN_PROGRESS", "MEDIUM");
        long mediumTodo       = count(tasks, "TODO",        "MEDIUM");

        long lowDone       = count(tasks, "DONE",        "LOW");
        long lowInProgress = count(tasks, "IN_PROGRESS", "LOW");
        long lowTodo       = count(tasks, "TODO",        "LOW");

        long overdue  = tasks.stream().filter(t -> t.getDueDate() != null && t.getDueDate().isBefore(today)                                               && !"DONE".equals(t.getStatus())).count();
        long dueToday = tasks.stream().filter(t -> t.getDueDate() != null && t.getDueDate().isEqual(today)                                               && !"DONE".equals(t.getStatus())).count();
        long dueSoon  = tasks.stream().filter(t -> t.getDueDate() != null && t.getDueDate().isAfter(today)   && t.getDueDate().isBefore(today.plusDays(4)) && !"DONE".equals(t.getStatus())).count();
        long onTrack  = tasks.stream().filter(t -> t.getDueDate() != null && t.getDueDate().isAfter(today.plusDays(3))                                   && !"DONE".equals(t.getStatus())).count();
        long noDate   = tasks.stream().filter(t -> t.getDueDate() == null && !"DONE".equals(t.getStatus())).count();

        int total          = tasks.size();
        int completionRate = total == 0 ? 0 : (int) (done * 100 / total);

        int completionComponent    = total == 0 ? 0 : (int) (done * 40 / total);
        int highPriorityComponent  = high == 0  ? 25 : (int) (highDone * 25 / high);
        int overdueComponent       = (int) Math.max(0, 20 - overdue * 4);
        int activeComponent        = (int) Math.min(15, inProgress * 3);
        int productivityScore      = Math.min(100, completionComponent + highPriorityComponent + overdueComponent + activeComponent);

        model.addAttribute("total",          total);
        model.addAttribute("todo",           todo);
        model.addAttribute("inProgress",     inProgress);
        model.addAttribute("done",           done);
        model.addAttribute("high",           high);
        model.addAttribute("medium",         medium);
        model.addAttribute("low",            low);
        model.addAttribute("highDone",       highDone);
        model.addAttribute("highInProgress", highInProgress);
        model.addAttribute("highTodo",       highTodo);
        model.addAttribute("mediumDone",       mediumDone);
        model.addAttribute("mediumInProgress", mediumInProgress);
        model.addAttribute("mediumTodo",       mediumTodo);
        model.addAttribute("lowDone",       lowDone);
        model.addAttribute("lowInProgress", lowInProgress);
        model.addAttribute("lowTodo",       lowTodo);
        model.addAttribute("overdue",    overdue);
        model.addAttribute("dueToday",   dueToday);
        model.addAttribute("dueSoon",    dueSoon);
        model.addAttribute("onTrack",    onTrack);
        model.addAttribute("noDate",     noDate);
        model.addAttribute("completionRate",       completionRate);
        model.addAttribute("productivityScore",    productivityScore);
        model.addAttribute("scoreColor",           scoreColor(productivityScore));
        model.addAttribute("scoreGrade",           scoreGrade(productivityScore));
        model.addAttribute("scoreGradeClass",      scoreGradeClass(productivityScore));
        model.addAttribute("scoreMessage",         scoreMessage(productivityScore));
        model.addAttribute("completionComponent",   completionComponent);
        model.addAttribute("highPriorityComponent", highPriorityComponent);
        model.addAttribute("overdueComponent",      overdueComponent);
        model.addAttribute("activeComponent",       activeComponent);

        return "stats";
    }

    private long count(List<Task> tasks, String status, String priority) {
        return tasks.stream()
                .filter(t -> status.equals(t.getStatus()))
                .filter(t -> priority == null || priority.equals(t.getPriority()))
                .count();
    }

    private long countPriority(List<Task> tasks, String priority) {
        return tasks.stream().filter(t -> priority.equals(t.getPriority())).count();
    }

    private String scoreColor(int score) {
        if (score >= 85) return "#4ADE80";
        if (score >= 70) return "#60A5FA";
        if (score >= 40) return "#FBBF24";
        return "#F87171";
    }

    private String scoreGrade(int score) {
        if (score >= 90) return "A+";
        if (score >= 80) return "A";
        if (score >= 70) return "B";
        if (score >= 60) return "C";
        if (score >= 50) return "D";
        return "F";
    }

    private String scoreGradeClass(int score) {
        if (score >= 80) return "grade-a";
        if (score >= 70) return "grade-b";
        if (score >= 50) return "grade-c";
        return "grade-f";
    }

    private String scoreMessage(int score) {
        if (score >= 90) return "Outstanding! Your task management is excellent.";
        if (score >= 80) return "Great work! You're staying on top of your workload.";
        if (score >= 70) return "Good momentum! Keep tackling those high-priority tasks.";
        if (score >= 60) return "Making progress! Clear overdue items to boost your score.";
        if (score >= 40) return "Needs attention. Focus on high-priority and overdue tasks.";
        return "Time to regroup. Start by clearing overdue and high-priority tasks.";
    }

    private Long getUserIdFromSession(HttpSession session) {
        String token = (String) session.getAttribute("jwt_token");
        if (token == null) return null;
        try {
            String username = jwtTokenProvider.extractUsername(token);
            return userRepository.findByUsername(username).map(User::getId).orElse(null);
        } catch (Exception e) {
            log.error("Failed to extract user from session: {}", e.getMessage());
            return null;
        }
    }
}
