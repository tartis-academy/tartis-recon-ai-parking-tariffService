# tariff-service — Alcance y Especificación de Fase 1 (v0.5.0)

Este documento especifica el alcance funcional, el modelo de datos y los endpoints del microservicio `tariff-service` correspondientes a la **Fase 1 (MVP - v0.5.0)** del sistema de gestión de parking **TARTIS Recon-AI**.

---

## 1. Responsabilidad del Microservicio en Fase 1

En la Fase 1 (`v0.5.0`), `tariff-service` gestiona el **catálogo inicial de tarifas** asociadas a cada tipo de vehículo:

- **Gestión de Tarifas:** Creación, edición, consulta y desactivación manual de tarifas por categoría (`CAR`, `CAR_PMR`, `MOTORBIKE`).
- **Consulta de Tarifa Vigente:** Endpoint `GET /v1/tariffs/current` para obtener el precio base y precio por minuto configurado.

---

## 2. Modelo de Dominio (`Tariff`) en Fase 1

| Atributo | Tipo Java | Descripción | Obligatorio |
|---|---|---|---|
| `id` | `UUID` | Identificador único de la tarifa | Sí |
| `vehicleType` | `VehicleType` | Categoría (`CAR`, `CAR_PMR`, `MOTORBIKE`) | Sí |
| `basePrice` | `BigDecimal` | Cuota fija de acceso inicial | Sí |
| `minutePrice` | `BigDecimal` | Precio por minuto ordinario | Sí |
| `active` | `boolean` | Estado de vigencia de la tarifa | Sí |

---

## 3. Endpoints REST Expuestos en Fase 1 (v0.5.0)

| Método HTTP | Endpoint | Descripción | Respuesta Exitosa |
|---|---|---|---|
| `GET` | `/v1/tariffs` | Consulta del catálogo de tarifas | `200 OK` (`List<TariffResponse>`) |
| `POST` | `/v1/tariffs` | Creación de tarifa por categoría | `201 Created` (`TariffResponse`) |
| `GET` | `/v1/tariffs/{tariffId}` | Consulta por UUID | `200 OK` (`TariffResponse`) |
| `PUT` | `/v1/tariffs/{tariffId}` | Edición de tarifa | `200 OK` (`TariffResponse`) |
| `PATCH` | `/v1/tariffs/{tariffId}/deactivate` | Desactivación manual de tarifa | `200 OK` (`TariffResponse`) |
| `GET` | `/v1/tariffs/current` | Consulta de tarifa activa por tipo | `200 OK` (`TariffResponse`) |

---

## 4. Persistencia PostgreSQL — Baseline (`V1__init.sql`)

```sql
CREATE SCHEMA IF NOT EXISTS tariff;

CREATE TABLE tariff.tariffs (
    id UUID PRIMARY KEY,
    vehicle_type VARCHAR(20) NOT NULL,
    base_price NUMERIC(10,2) NOT NULL,
    minute_price NUMERIC(10,4) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

---

## 5. Exclusiones de la Fase 1 (Diferencias con Fase 2 / v1.0.0)

- ❌ **Sin cálculo síncrono avanzado (`/tariffs/calculate`):** No incluía el algoritmo de cortesía 10 min, cuota fija 0,10 € ni tramos escalonados (**RN-06** a **RN-09**).
- ❌ **Sin garantía del invariante IN-08:** La desactivación automática de tarifas obsoletas se gestionaba manualmente.
- ❌ **Sin autenticación Keycloak ni RBAC (SEC-03):** Peticiones sin JWT.
- ❌ **Sin enrutamiento Kong API Gateway:** Peticiones directas al puerto `8083`.
