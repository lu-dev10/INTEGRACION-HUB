# Especificación de Casos de Uso — Integration Hub (MVP v1.0)

## Matriz de Casos de Uso

| ID | Nombre | Actor Principal | Nivel de Prioridad |
| :--- | :--- | :--- | :--- |
| **CU-001** | Autenticación de Usuario (Login) | Administrador / Operador | Alta (Crítica) |
| **CU-002** | Registrar Sistema Externo | Administrador | Alta (Crítica) |
| **CU-003** | Probar Conexión con Sistema Externo (Ping) | Administrador | Alta |
| **CU-004** | Descubrir API vía OpenAPI / Swagger | Administrador | Alta (Crítica) |
| **CU-005** | Consultar Endpoints y Esquemas Descubiertos | Administrador / Operador | Media |
| **CU-006** | Crear Integración | Administrador | Alta (Crítica) |
| **CU-007** | Configurar Reglas de Mapeo y Transformación | Administrador | Alta (Crítica) |
| **CU-008** | Probar Integración (Simulación / Dry Run) | Administrador | Media |
| **CU-009** | Ejecutar Integración Manualmente | Administrador / Operador | Alta (Crítica) |
| **CU-010** | Consultar Estado y Progreso de Ejecución | Administrador / Operador | Alta |
| **CU-011** | Consultar Errores y Detalle por Registro | Administrador / Operador | Alta |
| **CU-012** | Reintentar Registros Fallidos | Administrador / Operador | Alta |
| **CU-013** | Activar / Desactivar Integración | Administrador | Media |
| **CU-014** | Visualizar Dashboard de Métricas | Administrador / Operador | Media |

---

### CU-001: Autenticación de Usuario
- **Actor:** Administrador / Operador
- **Precondición:** El usuario debe estar previamente registrado en el sistema.
- **Flujo Principal:**
  1. El usuario ingresa credenciales (`username` y `password`) en el login de Angular.
  2. El frontend envía `POST /api/v1/auth/login`.
  3. El backend valida el hash BCrypt y genera un token JWT firmado.
  4. El backend responde con el token JWT, tiempo de expiración y rol del usuario.
  5. El frontend almacena el token en memoria/sessionStorage y redirige al Dashboard.
- **Flujo Alternativo:**
  - 3a. Credenciales inválidas: El backend responde `401 Unauthorized`. El frontend muestra mensaje de error.

---

### CU-002: Registrar Sistema Externo
- **Actor:** Administrador
- **Precondición:** Usuario autenticado con rol `ADMIN`.
- **Flujo Principal:**
  1. El administrador ingresa nombre, descripción, URL base (ej. `http://localhost:8081`), tipo (`REST`) y configuración de autenticación (ej. Bearer Token).
  2. El frontend envía `POST /api/v1/systems`.
  3. El backend cifra las credenciales sensibles y almacena el sistema en estado `ACTIVE`.
  4. El sistema queda disponible en la lista de sistemas registrados.
- **Flujo Alternativo:**
  - 3a. URL malformada o nombre duplicado: El backend responde `400 Bad Request` con el campo en error.

---

### CU-003: Probar Conexión con Sistema Externo (Ping)
- **Actor:** Administrador
- **Precondición:** Sistema registrado o en proceso de registro.
- **Flujo Principal:**
  1. El usuario presiona el botón "Probar Conexión".
  2. El backend emite una petición HTTP `HEAD`/`GET` a la URL base con timeout de 3 segundos.
  3. Si responde con código HTTP 2xx/3xx/401/403, se mide la latencia en milisegundos.
  4. Se muestra al usuario: "Conexión Exitosa (HTTP 200 - 124 ms)".
- **Flujo Alternativo:**
  - 2a. Timeout o error de DNS/red: El backend responde con estado `DOWN` y el mensaje de error de conexión.

---

### CU-004: Descubrir API vía OpenAPI / Swagger
- **Actor:** Administrador
- **Precondición:** El sistema externo expone un endpoint `/v3/api-docs` o `/swagger.json`.
- **Flujo Principal:**
  1. El usuario selecciona un sistema registrado y hace clic en "Descubrir API".
  2. El backend invoca el parser `OpenAPIV3Parser` sobre la URL de especificación.
  3. El parser resuelve referencias `$ref` de modelos y genera la estructura de paths, métodos HTTP, query parameters, y JSON Schemas de Request y Response.
  4. El backend persiste la metadata en `ApiDefinition` y cada endpoint en la tabla `Endpoint`.
  5. El frontend muestra la lista de endpoints descubiertos agrupados por tag/controlador.
- **Flujo Alternativo:**
  - 2a. No se encuentra documento OpenAPI: El backend responde con mensaje descriptivo ("No se pudo localizar especificación OpenAPI en la URL indicada").

---

### CU-005: Consultar Endpoints y Esquemas Descubiertos
- **Actor:** Administrador / Operador
- **Precondición:** El sistema debe haber completado el proceso de descubrimiento (CU-004).
- **Flujo Principal:**
  1. El usuario consulta los detalles de un sistema.
  2. Se despliega el árbol de endpoints (ej. `GET /api/sales`, `POST /api/invoices`).
  3. Al seleccionar un endpoint, se visualiza el esquema JSON de entrada y salida con tipos de datos.

---

### CU-006: Crear Integración
- **Actor:** Administrador
- **Precondición:** Existen al menos dos sistemas con endpoints descubiertos (uno para origen y otro para destino).
- **Flujo Principal:**
  1. El usuario hace clic en "Nueva Integración".
  2. Ingresa un nombre representativo (ej. "Ventas a Facturación").
  3. Selecciona el Sistema Origen y su endpoint (ej. `Sistema Ventas` ➔ `GET /sales`).
  4. Selecciona el Sistema Destino y su endpoint (ej. `Sistema Contabilidad` ➔ `POST /invoices`).
  5. Se crea la integración en estado `DRAFT`.
  6. Se pasa a la pantalla de configuración de Mappings.

---

### CU-007: Configurar Reglas de Mapeo y Transformación
- **Actor:** Administrador
- **Precondición:** Integración creada en estado `DRAFT` o `ACTIVE`.
- **Flujo Principal:**
  1. La interfaz muestra dos columnas: campos detectados en la respuesta origen y campos esperados en el request destino.
  2. El usuario une o asigna campos:
     - Origen `id` ➔ Destino `reference` (Transformación: `DIRECT`)
     - Origen `customerName` ➔ Destino `customer` (Transformación: `TRIM`)
     - Origen `total` ➔ Destino `amount` (Transformación: `NUMBER`)
  3. El usuario marca campos obligatorios y guarda las reglas.
  4. El backend valida coherencia y persiste los registros en `FieldMapping`.
  5. La integración pasa a estado `ACTIVE`.

---

### CU-008: Probar Integración (Simulación / Dry Run)
- **Actor:** Administrador
- **Precondición:** Mappings configurados.
- **Flujo Principal:**
  1. El usuario hace clic en "Probar Mapeo con muestra".
  2. El backend consulta 1 único registro del sistema origen.
  3. Aplica las transformaciones configuradas en memoria.
  4. Muestra un visor comparativo (JSON Origen vs JSON Transformado Resultante) sin enviar al destino.
  5. El usuario valida que la estructura generada coincide con lo requerido por el destino.

---

### CU-009: Ejecutar Integración Manualmente
- **Actor:** Administrador / Operador
- **Precondición:** Integración en estado `ACTIVE`.
- **Flujo Principal:**
  1. El usuario presiona el botón "Ejecutar Ahora".
  2. El backend genera un `Correlation ID` (UUID) y crea un registro `Execution` en estado `RUNNING`.
  3. El backend invoca al sistema origen (`GET /sales`).
  4. Si la respuesta es un array, el Splitter extrae cada elemento de forma individual.
  5. Para cada elemento:
     - Aplica el mapeo y las estrategias de transformación.
     - Valida el payload resultante.
     - Envía mediante HTTP `POST` al endpoint destino vía **Virtual Threads**.
     - Almacena un `ExecutionRecord` con el payload de entrada, salida, respuesta HTTP y estado (`SUCCESS` o `FAILED`).
  6. Finalizado el lote, actualiza la cabecera `Execution` con:
     - Estado (`SUCCESS`, `PARTIAL_SUCCESS`, o `FAILED`).
     - `totalRecords`, `successfulRecords`, `failedRecords`.
     - Timestamp de fin y duración en segundos.
  7. Retorna el resultado al frontend.

---

### CU-010: Consultar Estado y Progreso de Ejecución
- **Actor:** Administrador / Operador
- **Flujo Principal:**
  1. El usuario ingresa a la vista de "Historial de Ejecuciones".
  2. Visualiza una tabla con: ID, Integración, Fecha de inicio, Duración, Registros (Totales / Exitosos / Fallidos) y Estado con badges de color.

---

### CU-011: Consultar Errores y Detalle por Registro
- **Actor:** Administrador / Operador
- **Precondición:** Existe una ejecución con registros en estado `FAILED`.
- **Flujo Principal:**
  1. El usuario selecciona la ejecución y filtra por "Solo Fallidos".
  2. Selecciona un registro específico (ej. Registro #48).
  3. El sistema muestra:
     - Payload origen recibido.
     - Payload transformado enviado.
     - Código HTTP retornado por el destino (ej. `HTTP 400 Bad Request`).
     - Mensaje de error retornado por el destino (`{"error": "Invalid amount"}`).

---

### CU-012: Reintentar Registros Fallidos
- **Actor:** Administrador / Operador
- **Precondición:** Una ejecución tiene estado `PARTIAL_SUCCESS` o `FAILED`.
- **Flujo Principal:**
  1. En el detalle de la ejecución, el usuario presiona "Reintentar Fallidos (3)".
  2. El backend recupera únicamente los `ExecutionRecord` con estado `FAILED`.
  3. Re-aplica transformaciones y reenvía al sistema destino.
  4. Si el destino responde exitosamente (ej. 201 Created), actualiza el `ExecutionRecord` a `SUCCESS`.
  5. Recalcula los contadores de la cabecera `Execution`. Si todos los fallidos fueron resueltos, el estado pasa a `SUCCESS`.

---

### CU-013: Activar / Desactivar Integración
- **Actor:** Administrador
- **Flujo Principal:**
  1. El usuario activa/desactiva un interruptor en la lista de integraciones.
  2. Si está desactivada (`INACTIVE`), el backend rechaza cualquier intento de ejecución con un error `409 Conflict`.

---

### CU-014: Visualizar Dashboard de Métricas
- **Actor:** Administrador / Operador
- **Flujo Principal:**
  1. Al acceder a la aplicación, se despliega el Dashboard con tarjetas KPI:
     - Total integraciones activas.
     - Total ejecuciones del día.
     - Tasa de éxito porcentual (ej. 98.2%).
     - Tiempo promedio de respuesta.
  2. Gráfico de ejecuciones recientes con desglose de éxitos y fallos.
