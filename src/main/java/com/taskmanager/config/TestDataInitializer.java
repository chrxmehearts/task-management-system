package com.taskmanager.config;

import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TestDataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.existsByUsername("test")) {
            log.info("Test account already exists, skipping seed");
            return;
        }

        User testUser = userRepository.save(
                User.builder()
                        .username("test")
                        .email("test@test.com")
                        .password(passwordEncoder.encode("test"))
                        .role("ROLE_USER")
                        .build()
        );

        LocalDate today = LocalDate.now();

        List<Task> tasks = List.of(
                Task.builder().user(testUser).title("Set up project repository").description("Initialise Git repo, add .gitignore, push initial commit to remote.").status("DONE").priority("HIGH").dueDate(today.minusDays(10)).build(),
                Task.builder().user(testUser).title("Write project README").description("Document setup steps, tech stack, and environment variables.").status("DONE").priority("MEDIUM").dueDate(today.minusDays(7)).build(),
                Task.builder().user(testUser).title("Design database schema").description("Create ERD for users, tasks, comments, and labels tables.").status("DONE").priority("HIGH").dueDate(today.minusDays(5)).build(),
                Task.builder().user(testUser).title("Implement JWT authentication").description("Add login and register endpoints secured with JWT tokens.").status("DONE").priority("HIGH").dueDate(today.minusDays(3)).build(),
                Task.builder().user(testUser).title("Create Flyway migrations").description("Write V1 and V2 SQL migration scripts for the initial schema.").status("DONE").priority("MEDIUM").dueDate(today.minusDays(2)).build(),
                Task.builder().user(testUser).title("Build task CRUD API").description("REST endpoints for creating, reading, updating, and deleting tasks.").status("IN_PROGRESS").priority("HIGH").dueDate(today.plusDays(1)).build(),
                Task.builder().user(testUser).title("Add task filtering and sorting").description("Allow filtering tasks by status and priority; support sort by due date.").status("IN_PROGRESS").priority("MEDIUM").dueDate(today.plusDays(3)).build(),
                Task.builder().user(testUser).title("Write unit tests for services").description("Cover AuthService, TaskService, and UserService with JUnit 5 tests.").status("IN_PROGRESS").priority("HIGH").dueDate(today.plusDays(2)).build(),
                Task.builder().user(testUser).title("Integrate Swagger UI").description("Expose OpenAPI docs at /swagger-ui.html with request/response examples.").status("IN_PROGRESS").priority("LOW").dueDate(today.plusDays(4)).build(),
                Task.builder().user(testUser).title("Implement pagination on task list").description("Add page and size query params to GET /api/tasks.").status("TODO").priority("MEDIUM").dueDate(today.plusDays(5)).build(),
                Task.builder().user(testUser).title("Add task labels / tags").description("Allow users to attach colour-coded labels to tasks for grouping.").status("TODO").priority("LOW").dueDate(today.plusDays(7)).build(),
                Task.builder().user(testUser).title("Send email notifications").description("Notify users via email when a task is approaching its due date.").status("TODO").priority("MEDIUM").dueDate(today.plusDays(9)).build(),
                Task.builder().user(testUser).title("Set up CI/CD pipeline").description("Configure GitHub Actions to build, test, and deploy on every push to main.").status("TODO").priority("HIGH").dueDate(today.plusDays(6)).build(),
                Task.builder().user(testUser).title("Add user profile endpoint").description("GET /api/users/me returns current user details; PATCH allows updates.").status("TODO").priority("LOW").dueDate(today.plusDays(10)).build(),
                Task.builder().user(testUser).title("Implement task comments").description("Allow users to leave timestamped comments on any task they own.").status("TODO").priority("LOW").dueDate(today.plusDays(14)).build(),
                Task.builder().user(testUser).title("Performance profiling").description("Run load tests with k6 and identify slow DB queries to optimise.").status("TODO").priority("MEDIUM").dueDate(today.plusDays(20)).build(),
                Task.builder().user(testUser).title("Overdue: security audit").description("Review OWASP Top-10 checklist and fix any identified vulnerabilities.").status("TODO").priority("HIGH").dueDate(today.minusDays(1)).build(),
                Task.builder().user(testUser).title("Overdue: update dependencies").description("Bump Spring Boot, jjwt, and MapStruct to their latest stable versions.").status("TODO").priority("MEDIUM").dueDate(today.minusDays(4)).build()
        );

        taskRepository.saveAll(tasks);
        log.info("Test account seeded: username=test, email=test@test.com, tasks={}", tasks.size());
    }
}
