-- ============================================================
-- V2 - Agregar usuario_id a la tabla socio
-- ============================================================

ALTER TABLE socio
    ADD COLUMN IF NOT EXISTS usuario_id integer NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_socio_usuario'
    ) THEN
        ALTER TABLE socio
            ADD CONSTRAINT fk_socio_usuario
            FOREIGN KEY (usuario_id)
            REFERENCES usuario_sistema (id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_socio_usuario
    ON socio (usuario_id);
