# Task Management System — Claude Context

## Project Overview
Advanced task management system with JWT auth, REST API, and AI integration.
Java 17 + Spring Boot 3.4.x + H2 (dev) / MySQL (prod) + React frontend (planned).

## Tech Stack
- **Backend**: Java 17, Spring Boot 3.4.1, Spring Security 6, Spring Data JPA
- **Database**: H2 in-memory (dev), Flyway migrations
- **Auth**: JWT via jjwt 0.12.6
- **Mapping**: MapStruct 1.6.3
- **Boilerplate**: Lombok
- **Docs**: SpringDoc OpenAPI 2.7.0 → /swagger-ui.html
- **Build**: Maven

## Commands
- Run app: `./mvnw spring-boot:run`
- Build: `./mvnw clean package`
- Test: `./mvnw test`
- Single test: `./mvnw test -Dtest=UserServiceImplTest`
- H2 Console: http://localhost:8080/h2-console (JDBC: `jdbc:h2:mem:taskdb`, user: `sa`, pass: empty)
- Swagger UI: http://localhost:8080/swagger-ui.html

## Package Structure
```
com.taskmanager
├── config/          ← SecurityConfig, SwaggerConfig, JwtConfig
├── controller/      ← AuthController, UserController, TaskController
├── dto/
│   ├── request/     ← LoginRequestDto, RegisterRequestDto, TaskCreateDto
│   └── response/    ← AuthResponseDto, UserResponseDto, TaskResponseDto
├── entity/          ← User, Task (no Entity suffix)
├── exception/       ← GlobalExceptionHandler, ResourceNotFoundException
├── mapper/          ← UserMapper, TaskMapper (MapStruct interfaces)
├── repository/      ← UserRepository, TaskRepository
├── security/        ← JwtTokenProvider, JwtAuthFilter, UserDetailsServiceImpl
└── service/
    ├── AuthService, UserService, TaskService (interfaces)
    └── impl/        ← AuthServiceImpl, UserServiceImpl, TaskServiceImpl
```

## Naming Conventions
- Entities: `User`, `Task` (no suffix)
- Repositories: `UserRepository`, `TaskRepository`
- Service interfaces: `UserService`, `TaskService`
- Service impls: `UserServiceImpl`, `TaskServiceImpl`
- DTOs: `UserResponseDto`, `TaskCreateDto`, `LoginRequestDto`
- MapStruct mappers: `UserMapper`, `TaskMapper`
- Controllers: `UserController`, `TaskController`, `AuthController`

## Code Style Rules
- ALWAYS use constructor injection via Lombok `@RequiredArgsConstructor` — never `@Autowired`
- ALWAYS use `@Slf4j` for logging — never `System.out.println`
- ALWAYS return `ResponseEntity<T>` from controllers
- ALWAYS use DTOs — never expose entities directly from controllers
- ALWAYS use MapStruct for entity ↔ DTO conversion — never manual mapping
- Use `@Builder` + `@NoArgsConstructor` + `@AllArgsConstructor` on entities
- Use `@Data` on DTOs
- Use `Optional` from repositories, never return null from services
- Service layer throws custom exceptions, controller layer never handles business logic
- Do not use any comments in code (comments free) it`s illegal for you

## Security Rules
- JWT secret must be min 256-bit (32+ chars)
- Whitelist in SecurityConfig: `/api/auth/**`, `/h2-console/**`, `/swagger-ui/**`, `/v3/api-docs/**`, `/actuator/health`
- H2 console requires: `headers().frameOptions().disable()`
- Passwords always encoded with `BCryptPasswordEncoder`

## Database
- Flyway migrations in: `src/main/resources/db/migration/`
- Naming: `V1__create_initial_tables.sql`, `V2__add_task_comments.sql`
- H2 dialect: `org.hibernate.dialect.H2Dialect`
- `ddl-auto: validate` — Flyway manages schema, NOT Hibernate

## Entity Relationships
- `User` (1) → (N) `Task` via `user_id` FK
- Task statuses: `TODO`, `IN_PROGRESS`, `DONE`
- Task priorities: `LOW`, `MEDIUM`, `HIGH`
- User roles: `ROLE_USER`, `ROLE_ADMIN`

## Error Handling
- `GlobalExceptionHandler` with `@RestControllerAdvice`
- `ResourceNotFoundException extends RuntimeException` → returns 404
- `BadCredentialsException` → returns 401
- Standard error response body: `{ timestamp, status, message, path }`

## Git Workflow
- Feature branches: `feature/task-crud`, `feature/jwt-auth`
- Never commit to `main` directly
- Commit format: `feat: add JWT authentication filter`

## What NOT to do
- Never use field injection `@Autowired`
- Never expose JPA entities in API responses
- Never store plain-text passwords
- Never put business logic in controllers
- Never use `ddl-auto: create` or `update` — use Flyway only
- Never delete existing comments in code that were added manually