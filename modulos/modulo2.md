# Módulo 2: Autenticación JWT y Gestión de Sistemas Externos (Sprint 1 — US-102 & US-201)

Este documento detalla a nivel técnico exhaustivo el **Módulo 2** de **Integration Hub**, cubriendo la seguridad basada en Spring Security 6 y JWT, así como el registro y verificación de conectividad de sistemas externos (`ExternalSystem`).

---

## 1. Alcance Técnico del Módulo
- **Historias de Usuario:**
  - `US-102: Autenticación con JWT y Roles (5 SP)`
  - `US-201: Registro y CRUD de Sistemas Externos (5 SP)`
  - `US-202: Prueba de Conexión (Ping) (3 SP)`
- **Resultado Esperado (DoD):**
  - Entidad `User` con cifrado BCrypt y roles (`ROLE_ADMIN`, `ROLE_OPERATOR`).
  - Filtro `JwtAuthenticationFilter` validando tokens en cada request protegido.
  - Endpoint `POST /api/v1/auth/login` emitiendo JWT con claims de rol y expiración de 24 horas.
  - CRUD completo para `ExternalSystem` con encriptación simétrica AES-256 para credenciales sensibles.
  - Endpoint `POST /api/v1/systems/{id}/ping` que mide latencia real en milisegundos.

---

## 2. Modelo de Datos y Entidades JPA

### A. Entidad `User` (`auth/domain/User.java`)
```java
@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
```

### B. Entidad `ExternalSystem` (`system/domain/ExternalSystem.java`)
```java
@Entity
@Table(name = "external_systems")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ExternalSystem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SystemType type; // REST

    @Column(nullable = false, length = 255)
    private String baseUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AuthType authType; // NONE, BEARER_TOKEN, BASIC_AUTH, API_KEY

    @Column(columnDefinition = "TEXT")
    private String encryptedCredentials;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SystemStatus status; // ACTIVE, INACTIVE

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
```

---

## 3. Arquitectura de Seguridad (Spring Security 6 + JJWT)

```
com.integrationhub.auth/
├── domain/
│   ├── User.java
│   ├── Role.java
│   └── UserRepository.java
├── application/
│   ├── AuthService.java
│   ├── dto/
│   │   ├── LoginRequest.java
│   │   ├── AuthResponse.java
│   │   └── UserDto.java
└── infrastructure/
    ├── AuthController.java
    ├── security/
    │   ├── SecurityConfig.java
    │   ├── JwtTokenProvider.java
    │   ├── JwtAuthenticationFilter.java
    │   └── CustomUserDetailsService.java
```

### Reglas de Acceso en `SecurityConfig.java`:
- Rutas públicas:
  - `POST /api/v1/auth/login`
  - `GET /api/v1/health`
  - `/swagger-ui/**`, `/api-docs/**`
- Rutas protegidas:
  - Todo `/api/v1/**` requiere cabecera `Authorization: Bearer <TOKEN>` y rol autorizado.

---

## 4. Servicio de Conectividad (Ping / Connection Tester)
En `system/application/ConnectionTesterService.java`:
- Utiliza el cliente HTTP moderno de Java 21 con Virtual Threads:
```java
HttpClient client = HttpClient.newBuilder()
    .connectTimeout(Duration.ofSeconds(3))
    .build();

HttpRequest request = HttpRequest.newBuilder()
    .uri(URI.create(system.getBaseUrl()))
    .method("HEAD", HttpRequest.BodyPublishers.noBody())
    .timeout(Duration.ofSeconds(3))
    .build();

long start = System.currentTimeMillis();
HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
long latency = System.currentTimeMillis() - start;
```
- Devuelve `PingResult`: estado (`UP` / `DOWN`), latencia en ms y código HTTP recibido.

---

## 5. Criterios de Aceptación (Gherkin)

```gherkin
Feature: Autenticación y Conectividad de Sistemas

  Scenario: Emisión de Token JWT con credenciales válidas
    Given un usuario administrador con username "admin" y password "admin123"
    When envía POST a "/api/v1/auth/login" con credenciales correctas
    Then el código de respuesta es 200 OK
    And el payload contiene un token JWT válido con rol "ROLE_ADMIN"

  Scenario: Registro y Ping de Sistema Externo
    Given un usuario autenticado con rol "ROLE_ADMIN"
    When registra el sistema "Sistema Ventas" con URL "http://localhost:8081"
    And solicita un ping a "/api/v1/systems/1/ping"
    Then el sistema responde con estado "UP" y el tiempo de latencia en milisegundos
```
