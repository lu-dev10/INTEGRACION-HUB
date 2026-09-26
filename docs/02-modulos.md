# Especificación de Módulos del Sistema — Integration Hub

La arquitectura se estructura en **módulos independientes de alto desacoplamiento y alta cohesión** dentro de un monolito modular. Cada módulo tiene su propia capa de dominio, aplicación e infraestructura.

```
com.integrationhub/
├── auth/           # Seguridad, Usuarios, Roles, JWT
├── system/         # Sistemas Externos (ExternalSystem), Health Checks
├── discovery/      # Inspección OpenAPI, Parseo de Endpoints y Schemas
├── mapping/        # Reglas de mapeo campo a campo, JsonPath
├── transformation/ # Motor de transformaciones (Strategy Pattern)
├── execution/      # Motor de orquestación, Splitter EIP, Virtual Threads
├── audit/          # Bitácora, Logs, Métricas de rendimiento
└── shared/         # DTOs comunes, Excepciones, Utilitarios, Handlers globales
```

---

## 1. Módulo `auth` (Seguridad y Autenticación)

### Responsabilidad
Controlar la identidad de los usuarios, la emisión y validación de tokens JWT y la autorización basada en roles (RBAC).

### Componentes Clave
- **Dominio:** `User`, `Role` (`ROLE_ADMIN`, `ROLE_OPERATOR`).
- **Aplicación:** `AuthService` (login, validación de credenciales, refresh token).
- **Infraestructura:** `JwtTokenProvider`, `JwtAuthenticationFilter`, `SecurityConfig`, `UserRepository`.
- **Endpoints:**
  - `POST /api/v1/auth/login`: Autenticación y obtención de Bearer Token.
  - `GET /api/v1/auth/me`: Datos del usuario autenticado actual.

---

## 2. Módulo `system` (Gestión de Sistemas Externos)

### Responsabilidad
Gestionar el ciclo de vida de los sistemas integrados (orígenes y destinos), tipos de autenticación y verificación de conectividad.

### Componentes Clave
- **Dominio:** 
  - `ExternalSystem`: Entidad con `name`, `description`, `type` (`REST`), `baseUrl`, `authType` (`NONE`, `BEARER_TOKEN`, `BASIC_AUTH`, `API_KEY`), `encryptedCredentials`, `status` (`ACTIVE`, `INACTIVE`).
  - Enum `AuthType`, Enum `SystemStatus`.
- **Aplicación:**
  - `ExternalSystemService`: CRUD de sistemas, encriptación de credenciales.
  - `ConnectionTesterService`: Ejecuta un ping HTTP `HEAD` o `GET` con timeout corto (3s) y retorna latencia y código de estado.
- **Endpoints:**
  - `POST /api/v1/systems`: Registrar nuevo sistema.
  - `GET /api/v1/systems`: Listar sistemas con filtros.
  - `GET /api/v1/systems/{id}`: Detalle de un sistema.
  - `PUT /api/v1/systems/{id}`: Actualizar sistema.
  - `POST /api/v1/systems/{id}/ping`: Probar conectividad.

---

## 3. Módulo `discovery` (Descubrimiento OpenAPI)

### Responsabilidad
Inspeccionar automáticamente especificaciones OpenAPI 3.0 y Swagger 2.0 desde URLs remotas o archivos subidos, resolviendo referencias `$ref` y extrayendo paths, verbos HTTP y esquemas JSON.

### Patrones de Diseño
- **Adapter Pattern:** Adapta la salida de `io.swagger.parser.v3.OpenAPIV3Parser` a los modelos de dominio internos del Hub.

### Componentes Clave
- **Dominio:**
  - `ApiDefinition`: Metadata general de la API (título, versión, especificación en formato JSON).
  - `Endpoint`: Ruta, método HTTP (`GET`, `POST`, etc.), resumen, requestSchema (JSON Schema) y responseSchema (JSON Schema).
- **Aplicación:**
  - `OpenApiDiscoveryService`: Descarga el documento desde `baseUrl + /v3/api-docs` (o `/swagger.json`), parsea modelos y sincroniza los endpoints en la base de datos.
- **Endpoints:**
  - `POST /api/v1/systems/{id}/discover`: Disparar proceso de descubrimiento.
  - `GET /api/v1/systems/{id}/endpoints`: Listar endpoints descubiertos para un sistema.
  - `GET /api/v1/endpoints/{id}`: Detalle de un endpoint (parámetros y esquemas).

---

## 4. Módulo `mapping` (Definición de Integraciones y Mapeos)

### Responsabilidad
Permitir a los usuarios vincular un endpoint origen con un endpoint destino y definir las reglas campo por campo, admitiendo JsonPath para campos anidados.

### Componentes Clave
- **Dominio:**
  - `Integration`: Nombre, `sourceSystemId`, `sourceEndpointId`, `targetSystemId`, `targetEndpointId`, estado (`DRAFT`, `ACTIVE`, `INACTIVE`).
  - `FieldMapping`: `sourceField` (ej. `$.id` o `customerName`), `targetField` (ej. `$.reference` o `customer`), `transformationType` (Enum), `required` (boolean), `defaultValue` (string opcional).
- **Aplicación:**
  - `IntegrationService`: Creación y gestión de integraciones y sus mappings.
  - `MappingValidator`: Valida que los campos requeridos por el esquema destino estén mapeados.
- **Endpoints:**
  - `POST /api/v1/integrations`: Crear integración.
  - `GET /api/v1/integrations`: Listar integraciones.
  - `GET /api/v1/integrations/{id}`: Obtener configuración completa con mappings.
  - `PUT /api/v1/integrations/{id}/mappings`: Actualizar lista de reglas de mapeo.
  - `PATCH /api/v1/integrations/{id}/status`: Cambiar estado (ACTIVE/INACTIVE).

---

## 5. Módulo `transformation` (Motor de Transformación de Datos)

### Responsabilidad
Transformar los valores individuales de los campos de entrada según las reglas configuradas.

### Patrones de Diseño
- **Strategy Pattern:**
  - Interfaz: `TransformationStrategy` con método `Object transform(Object input, Map<String, Object> params)`.
  - Implementaciones concretas:
    - `DirectTransformation`: Pasa el valor tal cual.
    - `TrimTransformation`: Elimina espacios en blanco iniciales/finales.
    - `UppercaseTransformation`: Convierte a mayúsculas.
    - `LowercaseTransformation`: Convierte a minúsculas.
    - `NumberTransformation`: Convierte string a número (Integer/BigDecimal con redondeo).
    - `StringTransformation`: Convierte cualquier tipo a String.
    - `DateFormatTransformation`: Transforma formato de fecha (ej. `yyyy-MM-dd` a `dd/MM/yyyy`).
- **TransformationFactory / Registry:** Inyección dinámica de las estrategias mediante Spring (`Map<TransformationType, TransformationStrategy>`).

---

## 6. Módulo `execution` (Motor de Ejecución y Orquestación)

### Responsabilidad
Coordinar la llamada al sistema origen, particionar la respuesta si es una lista (Splitter EIP), aplicar transformaciones a cada registro, invocar al sistema destino y consolidar el resultado.

### Patrones de Diseño
- **Pipeline / Chain of Responsibility:**
  `FetchSourceStep` ➔ `SplitPayloadStep` ➔ `TransformRecordStep` ➔ `ValidatePayloadStep` ➔ `DispatchTargetStep` ➔ `RecordAuditStep`.
- **EIP Splitter:** Si el payload origen es un array `[{...}, {...}]`, se particiona en N registros independientes.
- **Java 21 Virtual Threads:** Los envíos a destino se ejecutan de forma paralela y liviana mediante `Executors.newVirtualThreadPerTaskExecutor()`.

### Componentes Clave
- **Dominio:**
  - `Execution`: Entidad cabecera (`id`, `integrationId`, `correlationId`, `status`, `startedAt`, `finishedAt`, `totalRecords`, `successfulRecords`, `failedRecords`).
  - `ExecutionRecord`: Detalle por elemento (`id`, `executionId`, `sourcePayload` [JSON], `transformedPayload` [JSON], `targetResponse` [JSON], `status` [`SUCCESS`, `FAILED`], `httpStatus`, `errorMessage`).
- **Aplicación:**
  - `ExecutionEngineService`: Orquesta la ejecución completa de una integración.
  - `RetryService`: Reejecuta únicamente los `ExecutionRecord` que se encuentren en estado `FAILED` para una ejecución dada.
- **Endpoints:**
  - `POST /api/v1/integrations/{id}/execute`: Disparar ejecución manual inmediata.
  - `POST /api/v1/executions/{executionId}/retry-failed`: Reintentar registros fallidos.
  - `GET /api/v1/executions/{id}`: Estado general de una ejecución.
  - `GET /api/v1/executions/{id}/records`: Listado paginado de registros procesados con sus errores.

---

## 7. Módulo `audit` (Auditoría y Trazabilidad)

### Responsabilidad
Almacenar métricas globales, tiempos de respuesta de los sistemas integrados y proveer información para el Dashboard.

### Componentes Clave
- **Dominio:** `ExecutionMetric`, `SystemHealthLog`.
- **Aplicación:** `DashboardMetricsService` (cálculo de tasa de éxito, tiempos promedio, ejecuciones por día).
- **Endpoints:**
  - `GET /api/v1/dashboard/summary`: Resumen global para el dashboard (totales de hoy, tasa de éxito, sistemas activos).
  - `GET /api/v1/audit/logs`: Historial filtrable de ejecuciones.

---

## 8. Módulo `mock` (Simuladores para Pruebas E2E)

### Responsabilidad
Proveer dos servicios REST desacoplados que simulen el comportamiento de sistemas reales para pruebas y demostraciones en vivo.

### Servicios Simulados:
1. **Mock Ventas (Sistema A):**
   - `GET /api/mock/sales`: Retorna un listado de ventas (`id`, `customerName`, `total`, `saleDate`).
   - Soporte de parámetro `?count=100&failRatio=0.03` (para simular intencionalmente 3 registros erróneos).
2. **Mock Contabilidad (Sistema B):**
   - `POST /api/mock/invoices`: Recibe factura (`reference`, `customer`, `amount`, `invoiceDate`).
   - Valida que `amount` sea mayor a 0 y que `reference` no esté vacío; si falla la validación, responde HTTP 400.
