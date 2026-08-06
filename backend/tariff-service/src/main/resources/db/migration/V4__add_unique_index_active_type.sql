CREATE UNIQUE INDEX ux_tariffs_one_active_per_type ON tariffs (type) WHERE active;
