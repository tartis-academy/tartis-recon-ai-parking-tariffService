# tariff-service — Alcance y Especificación de Fase 2 (v1.0.0)

Este documento especifica el alcance funcional completo, el algoritmo de cálculo y la seguridad del microservicio `tariff-service` correspondientes a la **Fase 2 (v1.0.0)** del sistema **TARTIS Recon-AI**.

---

## 1. Responsabilidad del Microservicio en Fase 2

En la Fase 2 (`v1.0.0`), `tariff-service` incorpora el **algoritmo de cálculo de importe exacto de estancia**:

- **Endpoint de Cálculo (`GET /v1/tariffs/calculate`):** Invocado síncronamente por `stay-service` durante el check-out de vehículos.
- **Invariante IN-08:** Garantía en dominio de que solo existe **1 única tarifa activa por tipo de vehículo**, desactivando atómicamente la previa al crear/activar una nueva.
- **Seguridad & RBAC (SEC-03):** Integración con Keycloak IdP (`KEYCLOAK_JWK_SET_URI` fix **SEC-07**) y Kong API Gateway (`ADMIN` para modificación; `ADMIN`/`OPERARIO` para cálculo y consulta).

---

## 2. Reglas del Algoritmo de Cálculo Tarifario (v1.0.0)

1. **Cuota Fija & Cortesía (RN-09):** Se aplica una cuota fija de acceso de `0,10 €`. Si el tiempo consumido es $\le 10$ minutos (minutos de cortesía), el importe final es `0,00 €`.
2. **Tramos Escalonados (RN-07):**
   - Tramo 1 (Minutos 11 a 60): `0,03 €/minuto`.
   - Tramo 2 (Minutos 61 a 1440 / 24 horas): `0,02 €/minuto`.
   - Tramo 3 (Minutos > 1440 / días posteriores): `0,01 €/minuto`.
3. **Acumulación Escalonada No Retroactiva (RN-08):** Al cruzar de tramo, los minutos del tramo anterior conservan su precio específico sin recargar el tramo completo.
4. **Redondeo (RN-06):** Los minutos se redondean hacia arriba a favor del sistema (ej: 10 min y 1 seg = 11 minutos).

---

## 3. Matriz de Endpoints REST & Seguridad RBAC (v1.0.0)

| Método HTTP | Endpoint | Descripción | Roles Permitidos | Respuesta Exitosa |
|---|---|---|---|---|
| `GET` | `/v1/tariffs` | Consulta de catálogo | `ADMIN`, `OPERARIO` | `200 OK` |
| `POST` | `/v1/tariffs` | Creación de tarifa (Invariante IN-08) | `ADMIN` | `201 Created` |
| `GET` | `/v1/tariffs/{tariffId}` | Consulta por UUID | `ADMIN`, `OPERARIO` | `200 OK` |
| `PUT` | `/v1/tariffs/{tariffId}` | Edición de tarifa | `ADMIN` | `200 OK` |
| `PATCH` | `/v1/tariffs/{tariffId}/deactivate` | Desactivación de tarifa | `ADMIN` | `200 OK` |
| `GET` | `/v1/tariffs/current` | Consulta de tarifa activa | `ADMIN`, `OPERARIO` | `200 OK` |
| `GET` | `/v1/tariffs/calculate` | **Cálculo de importe (RN-06..09)** | `ADMIN`, `OPERARIO` | `200 OK` (`CalculationResponse`) |

---

## 4. Persistencia PostgreSQL (Database Per Service)

En perfil `prod`, se ejecuta sobre base de datos dedicada `tariff_db` en puerto `5435`.
