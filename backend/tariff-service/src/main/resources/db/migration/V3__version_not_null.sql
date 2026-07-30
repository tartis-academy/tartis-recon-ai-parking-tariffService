-- V2__add_version.sql creo la columna nullable. Eso es un bug latente, y este
-- mismo PR es el que lo arma: al anadir spring-boot-flyway, Flyway pasa a
-- ejecutarse por primera vez y la V2 se aplica de verdad.
--
-- El problema de dejarla nullable: las filas que ya existan quedan con version
-- NULL, y Spring Data decide en JpaMetamodelEntityInformation.isNew() que la
-- entidad es NUEVA cuando la version es null (con @Version Long, no
-- primitivo). El save() siguiente hace persist() en vez de merge: un INSERT
-- contra una PK que ya existe.
--
-- Reproducido sobre el stack real en vehicle-service, que tenia exactamente el
-- mismo defecto: poniendo version=NULL a mano en una fila y actualizandola por
-- la API, sale un 400 "There's already a vehicle with the specified plate",
-- porque el DataIntegrityViolationException del INSERT se reporta como
-- registro duplicado. Diagnostico completamente falso.
--
-- Va en una V3 y no editando la V2 porque la V2 ya esta en release123: cambiar
-- una migracion ya publicada rompe la validacion de checksum de Flyway para
-- todo el que la haya aplicado.
--
-- El UPDATE previo es imprescindible: sin el, el SET NOT NULL falla si hay
-- alguna fila con version NULL.
UPDATE tariffs SET version = 0 WHERE version IS NULL;

ALTER TABLE tariffs
    ALTER COLUMN version SET DEFAULT 0,
    ALTER COLUMN version SET NOT NULL;
