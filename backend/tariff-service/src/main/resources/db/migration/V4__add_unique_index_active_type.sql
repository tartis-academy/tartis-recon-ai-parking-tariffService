-- Evitar fallo de migración si ya hay múltiples tarifas activas para el mismo tipo.
-- Se deja activa únicamente la que tenga el ID más bajo (o cualquier otra forma determinista).
UPDATE tariffs
SET active = false
WHERE active = true
  AND name NOT IN (
    SELECT MIN(name)
    FROM tariffs
    WHERE active = true
    GROUP BY type
);

CREATE UNIQUE INDEX ux_tariffs_one_active_per_type ON tariffs (type) WHERE active;
