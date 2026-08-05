# tartis-recon-ai-parking — tariff-service

## Responsabilidad del microservicio

`tariff-service` es el microservicio responsable de la configuración del catálogo de tarifas por tipo de vehículo (**HU-04**) y del cálculo del importe a cobrar por las estancias en el parking. Sus responsabilidades principales incluyen:
- **Gestión de Tarifas (HU-04):** Alta, modificación, deshabilitación y consulta de tarifas por categoría de vehículo (`CAR`, `CAR_PMR`, `MOTORBIKE`).
- **Garantía del Invariante IN-08:** Garantiza que en todo momento exista como máximo **una única tarifa activa** por tipo de vehículo. Al crear o activar una tarifa nueva para una categoría, la tarifa anterior queda desactivada de forma atómica en la misma transacción.
- **Cálculo del Importe de Estancia (`/tariffs/calculate`):** Aplica la lógica de tarificación y reglas de negocio del dominio en el cálculo del precio consumido síncronamente por `stay-service` durante el check-out:
  - **RN-09 (Cuota de acceso y cortesía):** Aplica una cuota fija de acceso (0,10 €) e incluye los primeros 10 minutos de cortesía (0,00 €) antes de iniciar la tarificación por minutos.
  - **RN-07 (Tramos por tiempo):** 0,03 €/min para la primera hora, 0,02 €/min una vez completada la primera hora (61 a 1440 min), y 0,01 €/min tras completar el primer día (>1440 min).
  - **RN-08 (Acumulación escalonada no retroactiva):** El importe acumulado en tramos inferiores se conserva al cruzar de tramo; la tarifa reducida del nuevo tramo aplica solo a los minutos adicionales.
  - **RN-06 (Redondeo):** Redondeo de minutos hacia arriba a favor del sistema.

## Endpoints expuestos

Todos los endpoints requieren autenticación mediante Bearer Token (Access Token emitido por Keycloak), exceptuando el probe público de salud.

| Método | Endpoint | Descripción | Roles Autorizados |
|---|---|---|---|
| `GET` | `/v1/tariffs` | Listado paginado de tarifas (filtros por `vehicleType` y `active`) | `ADMIN`, `OPERARIO` |
| `POST` | `/v1/tariffs` | Crea y activa una tarifa (desactiva la anterior según IN-08) | `ADMIN` |
| `GET` | `/v1/tariffs/{tariffId}` | Obtiene el detalle de una tarifa por su UUID | `ADMIN`, `OPERARIO` |
| `PUT` | `/v1/tariffs/{tariffId}` | Modifica los datos de una tarifa existente | `ADMIN` |
| `PATCH` | `/v1/tariffs/{tariffId}/deactivate` | Desactiva una tarifa existente | `ADMIN` |
| `GET` | `/v1/tariffs/current` | Consulta la tarifa actualmente activa para un tipo de vehículo (IN-08) | `ADMIN`, `OPERARIO` |
| `POST` | `/v1/tariffs/calculate` | Calcula el importe de una estancia aplicando RN-06..RN-09 | `ADMIN`, `OPERARIO` |
| `GET` | `/actuator/health` | Probes de salud del servicio (Liveness / Readiness) | Público |

## Eventos publicados y consumidos

Este microservicio opera bajo un modelo de comunicación REST síncrono.
- **Eventos publicados en RabbitMQ:** Ninguno.
- **Eventos consumidos de RabbitMQ:** Ninguno.

## Variables de entorno

| Variable | Descripción | Valor por defecto (Dev) | Perfil / Uso |
|---|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Perfil activo de Spring Boot | `dev` | `dev` / `prod` |
| `DB_HOST` | Host de la BD compartida de desarrollo | `localhost` | Dev |
| `DB_PORT` | Puerto de la BD compartida | `5432` | Dev |
| `DB_NAME` | Nombre de la BD de desarrollo | `parking_dev` | Dev |
| `DB_USER` | Usuario de la BD de desarrollo | `parking_dev` | Dev |
| `DB_PASSWORD` | Contraseña de la BD de desarrollo | `change.me` | Dev |
| `TARIFF_DB_HOST` | Host de la BD dedicada de tarifas | — | Prod / Aislado |
| `TARIFF_DB_PORT` | Puerto de la BD dedicada de tarifas | `5432` | Prod / Aislado |
| `TARIFF_DB_NAME` | Nombre de la BD dedicada | `tariff_db` | Prod / Aislado |
| `TARIFF_DB_USER` | Usuario de la BD dedicada | — | Prod / Aislado |
| `TARIFF_DB_PASSWORD` | Contraseña de la BD dedicada | — | Prod / Aislado |
| `KEYCLOAK_ISSUER_URI` | URI del emisor de Keycloak (Issuer URI) | `http://localhost:8180/realms/parking` | Dev / Prod |
| `KEYCLOAK_JWK_SET_URI` | URI del conjunto de claves JWK de Keycloak | `http://localhost:8180/realms/parking/protocol/openid-connect/certs` | Dev / Prod |

## Ejecución de forma aislada

Para ejecutar y probar `tariff-service` de forma independiente sin depender del resto de microservicios:

1. **Opción 1: Entorno de Desarrollo (Perfil `dev`)**
   Navegar a la carpeta del microservicio y arrancar con Maven:
   ```bash
   cd backend/tariff-service
   mvn spring-boot:run
   ```
   *El servicio se conectará al esquema `tariff` del Postgres compartido.*

2. **Opción 2: Base de Datos Dedicada (Perfil `prod` / Contenedores Aislados)**
   Para ejecutar contra una base de datos PostgreSQL exclusiva en puerto `5435`:
   ```bash
   cd backend/tariff-service
   cp .env.example .env
   docker compose up -d
   mvn spring-boot:run -Dspring-boot.run.profiles=prod
   ```

## Migraciones de base de datos (Flyway)

El esquema ya no se crea a mano ni con un `schema.sql` montado como init
script: `V1__init.sql` (en `backend/tariff-service/src/main/resources/db/migration`)
es la baseline, y Flyway la aplica solo al arrancar la app contra la BD
dedicada (perfil `prod`). En dev, Flyway está desactivado
(`spring.flyway.enabled=false` en `application-dev.properties`): el Postgres
compartido con 5 schemas sigue gestionado por `ddl-auto=update`, fuera del
alcance de esta migración.

Para añadir un cambio de esquema: crea `V2__descripcion.sql` (nunca edites
`V1__init.sql` una vez desplegado) en la misma carpeta, con el DDL nuevo.
Flyway lo detecta y lo aplica en el siguiente arranque.

## Escaneo de imagen (Trivy)

El job `docker-scan` de la CI construye la imagen final del Dockerfile y la
escanea con [Trivy](https://trivy.dev/). El informe completo (`CRITICAL` +
`HIGH`) se publica siempre en la pestaña **Security** del repo; solo una
vulnerabilidad `CRITICAL` hace fallar el job.

Si una `CRITICAL` no tiene fix disponible todavía y hay que aceptar el riesgo
de forma consciente, se ignora explícitamente añadiendo su CVE a un
`.trivyignore` en la raíz del repo (no existe ninguno hoy).