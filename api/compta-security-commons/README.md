# Compta Security Commons

A shared security library for Compta microservices that provides authentication, authorization, and security utilities.

## Features

- **JWT Token Validation**: Validate and extract user details from JWT tokens
- **Gateway Authentication**: Extract authentication info from API Gateway headers
- **Role-Based Access Control**: Check user roles with annotations
- **Permission-Based Access Control**: Check user permissions with annotations
- **Security Context**: Thread-local storage for authenticated user details
- **Argument Resolvers**: Inject authenticated user into controller methods
- **AOP Support**: Security checks at service layer using aspects

## Installation

Add the dependency to your service's `pom.xml`:

```xml
<dependency>
  <groupId>tn.cyberious.compta</groupId>
  <artifactId>compta-security-commons</artifactId>
  <version>0.0.1-SNAPSHOT</version>
</dependency>
```

## Configuration

Add the following properties to your `application.yml`:

```yaml
compta:
  security:
    enabled: true
    jwt:
      secret: ${JWT_SECRET:your-secret-key-change-in-production}
    public-paths: /actuator/**,/v3/api-docs/**,/swagger-ui/**,/auth/login,/auth/refresh
```

## Usage

### 1. Public Endpoints

Mark endpoints as publicly accessible (no authentication required):

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {

  @PostMapping("/login")
  @Public
  public AuthResponse login(@RequestBody LoginRequest request) {
    return authService.login(request);
  }
}
```

### 2. Role-Based Access Control

Require specific roles to access endpoints:

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

  @GetMapping
  @RequireRole({"ADMIN", "COMPTABLE"})
  public List<User> listUsers() {
    // Only ADMIN or COMPTABLE can access this
    return userService.findAll();
  }

  @DeleteMapping("/{id}")
  @RequireRole(value = "ADMIN", requireAll = true)
  public void deleteUser(@PathVariable Long id) {
    // Only ADMIN can access this
    userService.delete(id);
  }
}
```

### 3. Permission-Based Access Control

Require specific permissions to access endpoints:

```java
@RestController
@RequestMapping("/api/societes")
public class SocieteController {

  @PostMapping
  @RequirePermission("societe:create")
  public Societe createSociete(@RequestBody CreateSocieteRequest request) {
    return societeService.create(request);
  }

  @DeleteMapping("/{id}")
  @RequirePermission({"societe:delete", "societe:manage"})
  public void deleteSociete(@PathVariable Long id) {
    // User needs either societe:delete OR societe:manage permission
    societeService.delete(id);
  }
}
```

### 4. Inject Authenticated User

Use the `@AuthenticatedUser` annotation to inject user details:

```java
@RestController
@RequestMapping("/api/profile")
public class ProfileController {

  @GetMapping
  public UserProfile getProfile(@AuthenticatedUser AuthenticatedUserDetails user) {
    // user is automatically injected from security context
    return profileService.getProfile(user.getUserId());
  }

  @PutMapping
  public UserProfile updateProfile(
      @AuthenticatedUser AuthenticatedUserDetails user,
      @RequestBody UpdateProfileRequest request) {
    return profileService.update(user.getUserId(), request);
  }
}
```

### 5. Access Security Context

Access the security context programmatically:

```java
@Service
public class UserService {

  public void doSomething() {
    // Get user from context
    AuthenticatedUserDetails user = SecurityContextHolder.getUser();

    // Get user ID
    Long userId = SecurityContextHolder.getUserId();

    // Check if authenticated
    boolean isAuth = SecurityContextHolder.isAuthenticated();
  }
}
```

### 6. Service Layer Security

Use annotations on service methods:

```java
@Service
public class AdminService {

  @RequireRole("ADMIN")
  public void performAdminAction() {
    // Only users with ADMIN role can call this method
  }

  @RequirePermission("system:configure")
  public void configureSystem(SystemConfig config) {
    // Only users with system:configure permission can call this
  }
}
```

## Authentication Modes

The library supports two authentication modes:

### Gateway Mode (Recommended for Production)

The API Gateway validates JWT tokens and passes user info via headers:

```
X-User-Id: 1
X-User-Username: john.doe
X-User-Email: john@example.com
X-User-Roles: ADMIN,USER
X-User-Societe-Ids: 1,2,3
X-User-Primary-Societe-Id: 1
X-User-Permissions: user:create,user:read
```

### Direct JWT Mode (Development/Testing)

Services can validate JWT tokens directly:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

## Security Context Structure

The `AuthenticatedUserDetails` object contains:

```java
public class AuthenticatedUserDetails {
  private Long userId;
  private String username;
  private String email;
  private List<String> roles;
  private List<UserPermission> permissions;
  private List<Long> societeIds;
  private Long primarySocieteId;
  private boolean active;
  private boolean locked;

  // Helper methods
  public boolean hasRole(String role);
  public boolean hasAnyRole(String... roles);
  public boolean hasAllRoles(String... roles);
  public boolean hasPermission(String permission);
  public boolean hasAnyPermission(String... permissions);
  public boolean hasAllPermissions(String... permissions);
}
```

## Permission Format

Permissions follow the pattern: `resource:action`

Examples:
- `user:create` - Can create users
- `user:read` - Can read user data
- `user:*` - Can perform any action on users (wildcard)
- `societe:manage` - Can manage companies
- `document:upload` - Can upload documents

Wildcards are supported:
- `user:*` matches `user:create`, `user:read`, `user:update`, `user:delete`

## Exception Handling

The library throws specific exceptions:

- `UnauthorizedException` (401) - User not authenticated
- `ForbiddenException` (403) - User lacks required role/permission
- `InvalidTokenException` (401) - JWT token is invalid or expired

These should be handled by your global exception handler:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(new ErrorResponse("Unauthorized", ex.getMessage()));
  }

  @ExceptionHandler(ForbiddenException.class)
  public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenException ex) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(new ErrorResponse("Forbidden", ex.getMessage()));
  }
}
```

## Testing

The library includes comprehensive unit tests. To run them:

```bash
mvn test
```

## Architecture

```
compta-security-commons/
├── annotation/           # Security annotations
├── aspect/              # AOP security aspects
├── config/              # Auto-configuration
├── context/             # Security context (ThreadLocal)
├── exception/           # Security exceptions
├── filter/              # Servlet filters
├── interceptor/         # MVC interceptors
├── model/               # Data models
├── resolver/            # Argument resolvers
└── util/                # Utilities (JWT validator)
```

## Auto-Configuration

The library uses Spring Boot auto-configuration. It automatically:

1. Registers the `GatewayAuthenticationFilter` for all requests
2. Sets up argument resolvers for `@AuthenticatedUser` injection
3. Configures interceptors for role/permission checks
4. Enables AOP aspects for service-layer security

You can disable auto-configuration:

```yaml
compta:
  security:
    enabled: false
```

## Contributing

This is a shared library used by all Compta microservices. When making changes:

1. Ensure backward compatibility
2. Add comprehensive tests
3. Update this README
4. Coordinate with all service teams before releasing breaking changes

## License

Internal use only - Cyberious Compta Project
