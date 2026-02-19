# MVP Build Prompt — Task Management System

You are working on a Java 17 + Spring Boot 3.4.1 backend project.
Read CLAUDE.md first for all conventions, naming rules, and constraints before writing any code.

## Your Mission
Build a complete working MVP backend. Follow the exact order below.
After each phase, run `./mvnw test` and fix any errors before proceeding.
Use `git commit` after each phase with a descriptive message.

---

## Phase 1 — Database Migrations

Create `src/main/resources/db/migration/V1__create_initial_tables.sql`:
```sql
CREATE TABLE users (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    email       VARCHAR(100) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    role        VARCHAR(20)  NOT NULL DEFAULT 'ROLE_USER',
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tasks (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(255) NOT NULL,
    description TEXT,
    status      VARCHAR(20)  NOT NULL DEFAULT 'TODO',
    priority    VARCHAR(20)  NOT NULL DEFAULT 'MEDIUM',
    due_date    DATE,
    user_id     BIGINT       NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tasks_user FOREIGN KEY (user_id) REFERENCES users(id)
);
```

---

## Phase 2 — Entities

Create `User.java` entity in `com.taskmanager.entity`:
- Fields: id (Long), username, email, password, role, createdAt
- Annotations: `@Entity`, `@Table(name = "users")`, `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`
- `@CreationTimestamp` on `createdAt`
- Implement `UserDetails` from Spring Security

Create `Task.java` entity in `com.taskmanager.entity`:
- Fields: id (Long), title, description, status (String), priority (String), dueDate (LocalDate), user (ManyToOne → User), createdAt
- Annotations: `@Entity`, `@Table(name = "tasks")`, `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`
- `@ManyToOne(fetch = FetchType.LAZY)` + `@JoinColumn(name = "user_id")` on user field

---

## Phase 3 — Repositories

Create `UserRepository.java` in `com.taskmanager.repository`:
- Extends `JpaRepository<User, Long>`
- Methods: `findByUsername(String username)`, `findByEmail(String email)`, `existsByUsername(String username)`, `existsByEmail(String email)`

Create `TaskRepository.java` in `com.taskmanager.repository`:
- Extends `JpaRepository<Task, Long>`
- Methods: `findAllByUserId(Long userId)`, `findByIdAndUserId(Long id, Long userId)`

---

## Phase 4 — DTOs

Create in `com.taskmanager.dto.request`:
- `RegisterRequestDto`: username, email, password — all with `@NotBlank` validation
- `LoginRequestDto`: username, password — both with `@NotBlank`
- `TaskCreateDto`: title (`@NotBlank`), description, status, priority, dueDate (LocalDate)
- `TaskUpdateDto`: same fields as TaskCreateDto, all optional

Create in `com.taskmanager.dto.response`:
- `AuthResponseDto`: token (String), username (String)
- `UserResponseDto`: id, username, email, role
- `TaskResponseDto`: id, title, description, status, priority, dueDate, createdAt, userId

All DTOs use `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor` from Lombok.

---

## Phase 5 — MapStruct Mappers

Create `UserMapper.java` interface in `com.taskmanager.mapper`:
- `@Mapper(componentModel = "spring")`
- `UserResponseDto toResponseDto(User user)`

Create `TaskMapper.java` interface in `com.taskmanager.mapper`:
- `@Mapper(componentModel = "spring")`
- `TaskResponseDto toResponseDto(Task task)`
- `@Mapping(source = "user.id", target = "userId")` on that method
- `Task toEntity(TaskCreateDto taskCreateDto)`

---

## Phase 6 — JWT Security

Create `JwtTokenProvider.java` in `com.taskmanager.security`:
- `@Component`, `@Slf4j`
- Inject jwt.secret and jwt.expiration from application.yml via `@Value`
- `generateToken(UserDetails userDetails) → String`
- `extractUsername(String token) → String`
- `isTokenValid(String token, UserDetails userDetails) → boolean`
- Use `io.jsonwebtoken` (jjwt 0.12.6) API: `Jwts.builder()`, `Jwts.parserBuilder()`
- Key created with `Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8))`

Create `JwtAuthFilter.java` in `com.taskmanager.security`:
- Extends `OncePerRequestFilter`
- Extract Bearer token from `Authorization` header
- Validate and set `UsernamePasswordAuthenticationToken` in SecurityContext
- Use `@RequiredArgsConstructor`, `@Slf4j`

Create `UserDetailsServiceImpl.java` in `com.taskmanager.security`:
- Implements `UserDetailsService`
- Loads user by username from `UserRepository`
- Throws `UsernameNotFoundException` if not found

---

## Phase 7 — Security Configuration

Create `SecurityConfig.java` in `com.taskmanager.config`:
- `@Configuration`, `@EnableWebSecurity`, `@RequiredArgsConstructor`
- `SecurityFilterChain` bean with:
    - CSRF disabled
    - Frame options disabled (for H2 console)
    - Whitelisted: `/api/auth/**`, `/h2-console/**`, `/swagger-ui/**`, `/swagger-ui.html`, `/v3/api-docs/**`, `/actuator/health`
    - All other requests: authenticated
    - Session: STATELESS
    - Add `JwtAuthFilter` before `UsernamePasswordAuthenticationFilter`
- `PasswordEncoder` bean: `BCryptPasswordEncoder`
- `AuthenticationManager` bean from `AuthenticationConfiguration`

---

## Phase 8 — Exception Handling

Create `ResourceNotFoundException.java` in `com.taskmanager.exception`:
- Extends `RuntimeException`
- Constructor with message

Create `ErrorResponseDto.java` in `com.taskmanager.dto.response`:
- Fields: timestamp (LocalDateTime), status (int), message (String), path (String)

Create `GlobalExceptionHandler.java` in `com.taskmanager.exception`:
- `@RestControllerAdvice`, `@Slf4j`
- Handle `ResourceNotFoundException` → 404
- Handle `MethodArgumentNotValidException` → 400 (extract field errors)
- Handle `BadCredentialsException` → 401
- Handle generic `Exception` → 500
- All return `ResponseEntity<ErrorResponseDto>`

---

## Phase 9 — Services

Create `AuthService.java` interface + `AuthServiceImpl.java` in `com.taskmanager.service`:
- `register(RegisterRequestDto request) → UserResponseDto`
    - Check username/email uniqueness, throw if duplicate
    - Encode password with BCrypt
    - Save user, return mapped DTO
- `login(LoginRequestDto request) → AuthResponseDto`
    - Authenticate via `AuthenticationManager`
    - Generate JWT token
    - Return `AuthResponseDto` with token + username

Create `TaskService.java` interface + `TaskServiceImpl.java`:
- `createTask(TaskCreateDto request, Long userId) → TaskResponseDto`
- `getAllTasks(Long userId) → List<TaskResponseDto>`
- `getTaskById(Long taskId, Long userId) → TaskResponseDto`
    - Throws `ResourceNotFoundException` if not found or doesn't belong to user
- `updateTask(Long taskId, TaskUpdateDto request, Long userId) → TaskResponseDto`
- `deleteTask(Long taskId, Long userId)`

---

## Phase 10 — Controllers

Create `AuthController.java` in `com.taskmanager.controller`:
- `@RestController`, `@RequestMapping("/api/auth")`, `@RequiredArgsConstructor`
- `POST /api/auth/register` → `ResponseEntity<UserResponseDto>`
- `POST /api/auth/login` → `ResponseEntity<AuthResponseDto>`
- Use `@Valid` on request body params

Create `TaskController.java` in `com.taskmanager.controller`:
- `@RestController`, `@RequestMapping("/api/tasks")`, `@RequiredArgsConstructor`
- Extract authenticated user id via helper: `((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId()`
- `GET /api/tasks` → `ResponseEntity<List<TaskResponseDto>>`
- `POST /api/tasks` → `ResponseEntity<TaskResponseDto>` (201 Created)
- `GET /api/tasks/{id}` → `ResponseEntity<TaskResponseDto>`
- `PUT /api/tasks/{id}` → `ResponseEntity<TaskResponseDto>`
- `DELETE /api/tasks/{id}` → `ResponseEntity<Void>` (204 No Content)

---

## Phase 11 — Swagger Config

Create `SwaggerConfig.java` in `com.taskmanager.config`:
- `@Configuration`
- `OpenAPI` bean with:
    - Title: "Task Management System API"
    - Version: "1.0"
    - Security scheme: `bearerAuth` (Bearer, JWT)
    - Apply security globally

---

## Phase 12 — Verification

After all phases are complete:
1. Run `./mvnw clean package` — must pass with no errors
2. Run the app: `./mvnw spring-boot:run`
3. Verify H2 console accessible: http://localhost:8080/h2-console
4. Verify Swagger UI accessible: http://localhost:8080/swagger-ui.html
5. Test flow via Swagger:
    - POST /api/auth/register with `{"username":"testuser","email":"test@test.com","password":"password123"}`
    - POST /api/auth/login → copy the token
    - Click "Authorize" in Swagger → paste `Bearer <token>`
    - POST /api/tasks → create a task
    - GET /api/tasks → verify task is returned
6. Run all tests: `./mvnw test`
7. Git commit: `feat: complete MVP backend implementation`

---

## Important Reminders
- Read CLAUDE.md before starting
- Use constructor injection only (`@RequiredArgsConstructor`)
- Never expose entities from controllers — always use DTOs + MapStruct
- Never use `ddl-auto: create` — Flyway manages schema
- Add `// Added: <reason>` comment above every new method or class
- If a test fails, fix it before moving to the next phase
- Keep all existing comments in any file you modify