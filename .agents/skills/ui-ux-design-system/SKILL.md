---
name: ui-ux-design-system
description: Guía integral de diseño UI/UX, Design System, paleta de colores, tipografía, componentes visuales para plataformas de integración (Mapeador visual de campos, monitor de ejecuciones, badges de estado, inspector forense de payloads y microinteracciones).
---

# Skill: UI/UX Design System — Integration Hub

Este skill define el **Design System** y los estándares de experiencia de usuario (UX) e interfaz visual (UI) para convertir **Integration Hub** en una plataforma de nivel enterprise, con estética moderna, limpia y de alto impacto visual.

---

## 1. Filosofía de Diseño
- **Estilo:** *Deep Slate Dark Mode* con acentos de neón/gradiente sutil y superficies *Glassmorphism*.
- **Claridad sobre Ruido:** Interfaces densas en datos (logs, esquemas JSON, mapeos) pero organizadas con jerarquía tipográfica estricta y espaciado consistente.
- **Feedback Inmediato:** Toda acción asíncrona (ping a un servidor, escaneo de OpenAPI, ejecución de lote) debe reflejar estados de carga visuales, barras de progreso y micro-animaciones.

---

## 2. Paleta de Colores y Tokens Semánticos

### Fondos y Superficies (Dark Theme)
```css
--bg-app: #090d16;          /* Fondo base de la aplicación */
--bg-surface: #111827;      /* Tarjetas y contenedores primarios */
--bg-surface-elevated: #1e293b; /* Modales, popovers y dropdowns */
--border-subtle: rgba(255, 255, 255, 0.07);
--border-highlight: rgba(99, 102, 241, 0.4);
```

### Acentos de Marca (Brand & Gradients)
```css
--primary-indigo: #6366f1;   /* Color primario de acción */
--primary-hover: #4f46e5;
--accent-cyan: #06b6d4;      /* Acento secundario para datos/métricas */
--gradient-primary: linear-gradient(135deg, #6366f1 0%, #06b6d4 100%);
```

### Estados Semánticos
```css
--status-success: #10b981;   /* 🟢 200 OK / SUCCESS */
--status-success-bg: rgba(16, 185, 129, 0.12);

--status-warning: #f59e0b;   /* 🟡 PARTIAL_SUCCESS / Latencia alta */
--status-warning-bg: rgba(245, 158, 11, 0.12);

--status-error: #f43f5e;     /* 🔴 FAILED / 4xx / 5xx */
--status-error-bg: rgba(244, 63, 94, 0.12);

--status-info: #38bdf8;      /* 🔵 RUNNING / PENDING */
--status-info-bg: rgba(56, 189, 248, 0.12);
```

---

## 3. Tipografía
- **Fuente Principal:** `'Plus Jakarta Sans'`, `'Inter'`, `-apple-system`, `sans-serif`.
- **Fuente Monoespaciada (para Payloads, JSONPath, Endpoints y Logs):** `'JetBrains Mono'`, `'Fira Code'`, `monospace`.
- **Jerarquía:**
  - `Display / H1`: 24px - 30px, Bold (Titulares de vistas principales).
  - `H2 / Section Title`: 18px - 20px, SemiBold.
  - `Body`: 14px, Regular (Lectura cómoda de datos).
  - `Caption / Meta`: 12px, Medium (Timestamps, badges, tamaños de payloads).
  - `Code Snippet`: 13px, Regular con altura de línea `1.5`.

---

## 4. Componentes Clave de Integration Hub

### A. Tarjetas KPI de Métricas (Dashboard)
- **Efecto Glassmorphism:**
  ```css
  background: rgba(17, 24, 39, 0.75);
  backdrop-filter: blur(16px);
  border: 1px solid var(--border-subtle);
  border-radius: 14px;
  padding: 20px;
  transition: transform 0.2s ease, border-color 0.2s ease;
  ```
- **Interacción:** `hover: transform: translateY(-3px); border-color: var(--primary-indigo);`
- **Contenido:** Icono con fondo translúcido, valor numérico grande, etiqueta y badge de porcentaje de variación.

### B. Badge de Estado de Conexión (Ping Indicator)
- Badge con punto pulsante:
  - 🟢 **Activo:** Punto verde con animación `@keyframes pulse` + texto `Activo (124 ms)`.
  - 🔴 **Inalcanzable:** Punto rojo fijo + texto `Inactivo (Timeout)`.

### C. Mapeador Visual de Datos (Visual Data Mapper)
El componente estrella de la plataforma:
- **Layout de 3 Columnas:**
  1. **Columna Izquierda (Origen):** Árbol de campos descubiertos del endpoint origen con tipo de dato (`string`, `number`, `boolean`).
  2. **Columna Central (Transformación):** Conectores visuales y badges con la estrategia aplicada (`DIRECT`, `TRIM`, `UPPER`, `NUMBER`).
  3. **Columna Derecha (Destino):** Campos esperados por el endpoint destino, indicando cuáles son obligatorios (`* required`).
- **Líneas de Unión SVG:** Curvas Bezier dinámicas que unen el campo origen con el destino.

### D. Inspector Forense de Ejecuciones (Payload Inspector)
- **Panel dividido en pestañas:**
  - `Payload Origen`: JSON formateado con resaltado de sintaxis.
  - `Payload Transformado`: Resultado de la transformación.
  - `Respuesta Destino`: Status HTTP (`200`, `201`, `400`) y JSON devuelto.
- **Acción Rápida:** Botón `[Reintentar este registro]` junto al código de error `HTTP 400`.

### E. Barra de Progreso Bicolor (Batch Execution Bar)
- Visualización de lotes: Barra única dividida porcentualmente:
  - Franja verde para registros exitosos (ej. 97%).
  - Franja roja para registros fallidos (ej. 3%).

---

## 5. Micro-interacciones y Estados Vacíos (Empty States)
- **Skeletons Shimmer:** En lugar de spinners genéricos, usar cajas con gradiente animado en shimmer durante la carga de datos.
- **Empty States:** Si no hay sistemas registrados, mostrar una ilustración limpia con mensaje orientador: *"Aún no tienes sistemas registrados. Conecta tu primera API para comenzar."* y un botón de llamada a la acción primario `[+ Registrar Sistema]`.
