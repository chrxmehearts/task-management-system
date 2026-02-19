# UI Build Prompt — Task Management System Frontend

You are building a production-quality web UI for a Spring Boot 3.4.x + Java 17 application.
Read CLAUDE.md first before writing a single line of code.

## Your Research Step (MANDATORY — do this before writing any code)

Before writing HTML/CSS, use the web_search tool to:
1. Search "task management dashboard UI design 2025 dribbble" and study top results
2. Search "modern login page UI design inspiration 2025" and study top results
3. Search "linear app UI design system" for color/typography inspiration
4. Search "htmx spring boot thymeleaf example 2025" for implementation patterns

Extract from your research:
- Color palette (primary, background, sidebar, card colors)
- Typography style (font family, weights, sizes)
- Card design patterns (shadows, border-radius, spacing)
- Layout structure (sidebar width, content padding, grid)

Apply what you find. Do NOT invent a generic design — adapt real inspiration.

---

## Tech Stack for UI

- **Thymeleaf** — server-side templates (already in pom.xml)
- **HTMX 2.x** — partial page updates without full reload (via CDN)
- **Alpine.js 3.x** — lightweight JS reactivity for modals/dropdowns (via CDN)
- **Inter font** — via Google Fonts CDN
- No React, no npm, no build tools needed

Add to pom.xml before starting (these are the only new dependencies):
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
<dependency>
    <groupId>org.thymeleaf.extras</groupId>
    <artifactId>thymeleaf-extras-springsecurity6</artifactId>
</dependency>
<dependency>
    <groupId>org.webjars</groupId>
    <artifactId>webjars-locator-core</artifactId>
</dependency>
```

---

## Phase 1 — Security & Controller Setup

### 1.1 — Update SecurityConfig

In `SecurityConfig.java`, update the whitelist to include all UI routes.
Add these to the permitAll() list:
`/`, `/login`, `/register`, `/dashboard`, `/css/**`, `/js/**`, `/webjars/**`, `/images/**`

Also ensure H2 console still works:
`/h2-console/**`

Keep all existing JWT API security rules intact — the REST API under `/api/**`
must still require Bearer token auth. The UI pages are separate from the API.

### 1.2 — Create PageController

Create `PageController.java` in `com.taskmanager.controller`:
```java
// Added: serves HTML pages for Thymeleaf UI
@Controller
@RequiredArgsConstructor
@Slf4j
public class PageController {

    @GetMapping("/")
    public String indexRedirect() {
        return "redirect:/dashboard";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @GetMapping("/dashboard")
    public String dashboardPage() {
        return "dashboard";
    }
}
```

### 1.3 — Create UiAuthController (handles form submissions for UI)

Create `UiAuthController.java` in `com.taskmanager.controller`:

This controller handles HTML form POST (not JSON API). It:
- POST /ui/register → calls AuthService.register(), stores JWT in HttpSession, redirects to /dashboard
- POST /ui/login → calls AuthService.login(), stores JWT + username in HttpSession, redirects to /dashboard
- POST /ui/logout → invalidates HttpSession, redirects to /login

Store in session:
- `session.setAttribute("jwt_token", token)`
- `session.setAttribute("username", username)`

On error, redirect back with query param: `redirect:/login?error=Invalid credentials`

### 1.4 — Create UiTaskController (handles HTMX partial requests)

Create `UiTaskController.java` in `com.taskmanager.controller`:

This controller handles HTMX requests for task operations. It reads JWT from HttpSession.

Methods:
- `GET /ui/tasks/list` → returns fragment `dashboard :: taskList` with all tasks in model
- `POST /ui/tasks/create` → creates task via TaskService, returns fragment `dashboard :: taskList`
- `DELETE /ui/tasks/{id}` → deletes task via TaskService, returns fragment `dashboard :: taskList`
- `POST /ui/tasks/{id}/done` → updates task status to DONE, returns fragment `dashboard :: taskCard` with updated card

Extract userId from session JWT using JwtTokenProvider.
Redirect to /login if session has no JWT.

---

## Phase 2 — Base Layout Template

Create `src/main/resources/templates/layout.html` — the shared HTML shell:

Requirements:
- Google Fonts: Inter (weights 400, 500, 600, 700) — CDN link in head
- HTMX 2.x — CDN script: `https://unpkg.com/htmx.org@2.0.3`
- Alpine.js 3.x — CDN script: `https://unpkg.com/alpinejs@3.x.x/dist/cdn.min.js` with defer
- Thymeleaf layout fragment: `th:fragment="page(title, content)"`
- Meta viewport, charset, favicon placeholder
- CSS custom properties (variables) for the design system you researched

The layout injects `th:replace` content blocks for page-specific content.

---

## Phase 3 — Global CSS Design System

Create `src/main/resources/static/css/app.css`.

**IMPORTANT**: Before writing this file, you must have completed the web research in the Research Step above. The CSS must reflect real design inspiration, not a generic template.

The CSS must include:

**Design tokens as CSS custom properties:**
```css
:root {
  /* Apply colors from your research — these are placeholders */
  --color-primary: /* from research */;
  --color-primary-hover: /* from research */;
  --color-bg: /* from research */;
  --color-surface: /* from research */;
  --color-sidebar: /* from research */;
  --color-text: /* from research */;
  --color-text-muted: /* from research */;
  --color-border: /* from research */;
  --radius-sm: 6px;
  --radius-md: 10px;
  --radius-lg: 16px;
  --shadow-card: /* from research */;
  --shadow-modal: /* from research */;
  --font-family: 'Inter', system-ui, sans-serif;
}
```

**Required component styles** (all values from your design research):
- Reset and base body styles
- `.auth-wrapper` — centered full-height layout for login/register
- `.auth-card` — floating card with shadow, branding area, form container
- `.form-group` — label + input pairs with focus states
- `.btn-primary`, `.btn-secondary`, `.btn-danger`, `.btn-sm` — button variants
- `.dashboard-layout` — flex container: sidebar + main content area
- `.sidebar` — fixed-width sidebar with logo, nav items, user avatar, logout
- `.sidebar-nav-item` — hover and active states
- `.main-content` — scrollable main area with padding
- `.page-header` — title row with action button
- `.tasks-grid` — responsive CSS Grid for task cards
- `.task-card` — card with left border color based on priority class
- `.task-card.priority-HIGH`, `.priority-MEDIUM`, `.priority-LOW` — border colors
- `.badge` — pill badge for status and priority
- `.badge-status-TODO`, `.badge-status-IN_PROGRESS`, `.badge-status-DONE`
- `.modal-overlay`, `.modal` — centered modal with backdrop
- `.empty-state` — centered empty state with icon placeholder
- `.alert-error`, `.alert-success` — flash message styles
- `.htmx-indicator` — loading spinner shown during HTMX requests
- Smooth transitions on interactive elements (0.15s ease)
- Hover lift effect on task cards (translateY + shadow)

---

## Phase 4 — Login Page

Create `src/main/resources/templates/login.html`:

Design requirements (apply your research here):
- Full-height centered layout
- Left side: branding panel with app name, tagline, decorative element (or single card if minimal design)
- Right side (or center if minimal): login card
- Logo/app name at top of card: "TaskFlow"
- Subtitle: "Welcome back. Sign in to continue."
- Fields: Username, Password
- "Sign In" primary button (full width)
- Link: "Don't have an account? Create one →" → /register
- Error message area (shown when `?error` query param present): `th:if="${param.error}"`
- HTMX is NOT needed here — plain HTML form POST to `/ui/login`
- `action="/ui/login" method="post"` on the form

Thymeleaf specifics:
- `th:action="@{/ui/login}"` on form
- CSRF token: `th:name="${_csrf.parameterName}" th:value="${_csrf.token}"` hidden input
- Error display: `th:if="${param.error}"` on error div

---

## Phase 5 — Register Page

Create `src/main/resources/templates/register.html`:

Same visual style as login page (consistent design language).

Fields: Username, Email, Password, Confirm Password
- Client-side validation with Alpine.js: check passwords match before submit
- `action="/ui/register" method="post"`
- Error display: `th:if="${param.error}"`
- Link back to login: "Already have an account? Sign in →"

Thymeleaf specifics same as login page (CSRF token required).

---

## Phase 6 — Dashboard Page

Create `src/main/resources/templates/dashboard.html`:

This is the main page. It uses HTMX for dynamic updates without page reload.

**Layout**: Sidebar + Main Content (2-column flex layout)

**Sidebar contains**:
- App logo/name at top
- Navigation items: "My Tasks" (active), "Statistics" (placeholder, disabled)
- At bottom: username from session `${session.username}`, logout button
- Logout: `<form action="/ui/logout" method="post">` with CSRF token

**Main content contains**:
- Page header: "My Tasks" title + "New Task" button
- Stats row: 3 cards showing counts — Total Tasks, In Progress, Completed (counts computed in controller)
- Task filter tabs: ALL / TODO / IN_PROGRESS / DONE (Alpine.js state, filters cards client-side)
- Task grid: `div id="task-list"` — this is the HTMX swap target

**New Task button** opens a modal:
```html
<!-- Alpine.js controls modal open/close -->
<div x-data="{ open: false }">
  <button @click="open = true" class="btn-primary">+ New Task</button>
  
  <div class="modal-overlay" :class="{ 'open': open }" @click.self="open = false">
    <div class="modal">
      <h3>Create New Task</h3>
      <form hx-post="/ui/tasks/create"
            hx-target="#task-list"
            hx-swap="innerHTML"
            @htmx:after-request="open = false">
        <!-- title, description, priority, status, dueDate fields -->
        <!-- submit button -->
      </form>
    </div>
  </div>
</div>
```

**Task list fragment** (named `taskList` for HTMX targeting):
```html
<div th:fragment="taskList" id="task-list" class="tasks-grid">
  <!-- th:each task : ${tasks} -->
  <!-- each card calls taskCard fragment -->
  
  <!-- empty state if no tasks -->
  <div th:if="${#lists.isEmpty(tasks)}" class="empty-state">
    <p>No tasks yet. Create your first one!</p>
  </div>
</div>
```

**Task card fragment** (named `taskCard`):
```html
<div th:fragment="taskCard(task)" 
     class="task-card"
     th:classappend="'priority-' + ${task.priority}">
  <h3 th:text="${task.title}"></h3>
  <p class="task-desc" th:text="${task.description}"></p>
  <div class="task-meta">
    <span class="badge" th:classappend="'badge-status-' + ${task.status}" th:text="${task.status}"></span>
    <span class="badge" th:classappend="'badge-priority-' + ${task.priority}" th:text="${task.priority}"></span>
    <span class="task-due" th:if="${task.dueDate}" th:text="${task.dueDate}"></span>
  </div>
  <div class="task-actions">
    <!-- Mark as Done button -->
    <button th:if="${task.status != 'DONE'}"
            hx-post="${'/ui/tasks/' + task.id + '/done'}"
            hx-target="#task-list"
            hx-swap="innerHTML"
            class="btn-sm btn-done">✓ Done</button>
    <!-- Delete button with confirmation -->
    <button hx-delete="${'/ui/tasks/' + task.id}"
            hx-target="#task-list"
            hx-swap="innerHTML"
            hx-confirm="Delete this task?"
            class="btn-sm btn-danger">Delete</button>
  </div>
</div>
```

**HTMX configuration** on dashboard body:
```html
<body hx-headers='{"X-XSRF-TOKEN": "[[${_csrf.token}]]"}'>
```
This sends CSRF token with every HTMX request automatically.

**Initial task load** via HTMX on page load:
```html
<div id="task-list"
     hx-get="/ui/tasks/list"
     hx-trigger="load"
     hx-swap="innerHTML">
  <div class="loading">Loading tasks...</div>
</div>
```

---

## Phase 7 — HTMX + CSRF Configuration

In `SecurityConfig.java`, add CSRF token repository configuration so HTMX requests work:
```java
// Added: CSRF configuration for HTMX compatibility
.csrf(csrf -> csrf
    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
    .csrfTokenRequestHandler(new XorCsrfTokenRequestAttributeHandler())
)
```

Also add a `CsrfTokenRequestAttributeHandler` filter or ensure the CSRF token
is available in the Thymeleaf model via a filter.

Add `CsrfFilter` awareness: create `CsrfTokenResponseHeaderBindingFilter.java` in `com.taskmanager.security`:
```java
// Added: makes CSRF token available to Thymeleaf templates
@Component
public class CsrfTokenResponseHeaderBindingFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            response.setHeader("X-CSRF-TOKEN", csrfToken.getToken());
        }
        filterChain.doFilter(request, response);
    }
}
```

---

## Phase 8 — Verification

After all phases are complete, verify in this order:

1. `./mvnw clean package` — must compile with zero errors
2. `./mvnw spring-boot:run` — app starts on port 8080
3. Open `http://localhost:8080` — should redirect to `/dashboard` → then to `/login` (no session)
4. Test register flow:
    - Go to `http://localhost:8080/register`
    - Fill in username, email, password → submit
    - Should redirect to `/dashboard` with empty task list
5. Test login flow:
    - Go to `http://localhost:8080/login`
    - Login with registered user → redirected to `/dashboard`
6. Test task creation:
    - Click "New Task" button → modal opens
    - Fill in title, priority → submit
    - Task appears in grid WITHOUT full page reload (HTMX partial update)
7. Test task actions:
    - Click "Done" on a task → status badge updates, no page reload
    - Click "Delete" → confirmation shown, task removed, no page reload
8. Test logout:
    - Click logout → session cleared → redirected to `/login`
9. Test that REST API still works:
    - `POST http://localhost:8080/api/auth/login` with JSON → still returns JWT token
    - Swagger UI at `http://localhost:8080/swagger-ui.html` still works
10. Git commit: `feat: add Thymeleaf + HTMX web UI`

---

## Key Rules for This Task

- Research designs FIRST, write code SECOND
- Every HTML page must use the Inter font
- Every interactive element must have a hover/focus state
- HTMX swap target must always be `#task-list` for task operations
- CSRF token must be present on ALL POST/DELETE forms and HTMX requests
- Session-based auth for UI is completely separate from JWT Bearer auth for API
- Both must work simultaneously — do not break the REST API
- Add `// Added: <reason>` comment above every new method
- Never delete existing comments in files you modify