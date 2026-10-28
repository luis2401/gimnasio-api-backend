-- ============================================================
-- V3__optimizacion_indices_y_escalabilidad.sql
-- Índices compuestos para acelerar reportes, consultas por sede
-- y filtros por vencer con alta concurrencia.
-- ============================================================

-- 1. Optimización para validación de suscripciones por vencer y acumulación
CREATE INDEX IF NOT EXISTS idx_suscripcion_socio_estado_fin
    ON suscripcion(socio_id, estado, fecha_fin);

-- 2. Optimización para asignaciones y rotaciones de sede
CREATE INDEX IF NOT EXISTS idx_asignacion_emp_vigente
    ON asignacion_sede(empleado_id, anulada, fecha_hasta, fecha_desde);

CREATE INDEX IF NOT EXISTS idx_asignacion_sede_vigente
    ON asignacion_sede(sede_id, anulada, fecha_hasta);

-- 3. Optimización para reportes de pagos y arqueos por rango de fechas
CREATE INDEX IF NOT EXISTS idx_pago_reporte_filtro
    ON pago(estado, fecha_pago, sede_id);

-- 4. Optimización para asistencias a clases
CREATE INDEX IF NOT EXISTS idx_asistencia_fecha_estado
    ON asistencia_clase(fecha, estado);
