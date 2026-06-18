ALTER TABLE employes
ADD COLUMN IF NOT EXISTS created_at TIMESTAMP;

ALTER TABLE employes
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;

UPDATE employes
SET created_at = COALESCE(created_at, NOW()),
    updated_at = COALESCE(updated_at, NOW());

ALTER TABLE employes
ALTER COLUMN created_at SET NOT NULL;
