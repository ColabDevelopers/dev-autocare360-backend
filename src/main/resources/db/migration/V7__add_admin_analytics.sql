-- V7 changed to a safe no-op migration because objects already exist.
-- The schema objects for admin_analytics, workload_metrics and task_assignments
-- and the appointments columns already exist in your database. To avoid
-- re-running DDL that may cause errors, this migration is intentionally a no-op.
-- This preserves existing data from V1..V6 and allows Flyway to mark V7 as applied.

-- No-op statement
SELECT 1;
