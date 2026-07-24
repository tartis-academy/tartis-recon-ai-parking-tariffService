-- DDL para el perfil prod (ddl-auto=validate): sin Flyway/Liquibase todavia,
-- el esquema se crea fuera de banda. Debe reflejar exactamente TariffEntity.
-- Se monta como init script en la Postgres dedicada de tariff-service.

CREATE TABLE IF NOT EXISTS tariffs (
    unique_id        UUID PRIMARY KEY,
    name             VARCHAR(255) NOT NULL UNIQUE,
    type             VARCHAR(20) NOT NULL,
    price_per_minute NUMERIC NOT NULL,
    base_price       NUMERIC NOT NULL,
    active           BOOLEAN NOT NULL
);
