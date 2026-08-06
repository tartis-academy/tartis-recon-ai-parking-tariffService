# tariff-service — Alcance y Especificación de Fase 2 (v1.0.0)

Este documento especifica el alcance funcional completo, el algoritmo de cálculo y la seguridad del microservicio `tariff-service` correspondientes a la **Fase 2 (v1.0.0)** del sistema **TARTIS Recon-AI**.

---

## 1. Responsabilidad del Microservicio en Fase 2

En la Fase 2 (`v1.0.0`), `tariff-service` incorpora el **algoritmo de cálculo de importe exacto de estancia**:

- **Endpoint de Cálculo (`GET /v1/tariffs/calculate`):** Invocado síncronamente por `stay-service` durante el check-out de vehículos.
- **Invariante IN-08:** Garantía en dominio de que solo existe **1 única tarifa activa por tipo de vehículo**, desactivando atómicamente la previa al crear/activar una nueva.
- **Seguridad & RBAC (SEC-03):** Integración con Keycloak IdP (`KEYCLOAK_JWK_SET_URI` fix **SEC-07**) y Kong API Gateway (`ADMIN` para modificación; `ADMIN`/`OPERARIO` para cálculo y consulta).

---

## 2. Reglas del Algoritmo de Cálculo Tarifario por Tipo de Vehículo (v1.0.0)

El cálculo del importe de estancia en `tariff-service` se realiza de forma **dinámica según la categoría/tipo de vehículo** (`CAR`, `CAR_PMR`, `MOTORBIKE`):

1. **Obtención de Tarifa Activa por Tipo (IN-08):** El sistema recupera la única tarifa marcada como activa (`active = true`) correspondiente al tipo de vehículo del coche/moto estacionado (`tariffPersistence.findActiveByType(type)`).
2. **Componentes de la Tarifa:** Cada tarifa configurada por el administrador especifica dos valores clave:
   - **`basePrice` (Precio Base):** Cuota fija de acceso aplicada al iniciar la estancia.
   - **`pricePerMinute` (Precio por Minuto):** Tarifa aplicada por cada minuto consumido.
3. **Fórmula de Cálculo:**
   $$\text{Importe Total} = \text{basePrice} + (\text{pricePerMinute} \times \text{minutos})$$
4. **Redondeo:** Los minutos se redondean hacia arriba a favor del sistema en caso de fracciones.

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
