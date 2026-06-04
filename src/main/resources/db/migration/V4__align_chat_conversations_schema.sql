-- Align conversations table with JPA entity (customer_id / technician_id on conversation row).
-- Safe to run multiple times (IF NOT EXISTS).

ALTER TABLE conversations
    ADD COLUMN IF NOT EXISTS customer_id BIGINT REFERENCES users(id) ON DELETE RESTRICT;

ALTER TABLE conversations
    ADD COLUMN IF NOT EXISTS technician_id BIGINT REFERENCES users(id) ON DELETE RESTRICT;

ALTER TABLE conversations
    ADD COLUMN IF NOT EXISTS customer_last_read_at TIMESTAMPTZ;

ALTER TABLE conversations
    ADD COLUMN IF NOT EXISTS technician_last_read_at TIMESTAMPTZ;

ALTER TABLE messages
    ADD COLUMN IF NOT EXISTS code VARCHAR(30);

-- Backfill participants from legacy conversation_participants table when present.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = 'public' AND table_name = 'conversation_participants'
    ) THEN
        UPDATE conversations c
        SET customer_id = cp.user_id
        FROM conversation_participants cp
        WHERE cp.conversation_id = c.id
          AND cp.role = 'CUSTOMER'
          AND c.customer_id IS NULL;

        UPDATE conversations c
        SET technician_id = cp.user_id
        FROM conversation_participants cp
        WHERE cp.conversation_id = c.id
          AND cp.role = 'TECHNICIAN'
          AND c.technician_id IS NULL;
    END IF;
END $$;

-- Normalize enum values stored as uppercase by older Hibernate builds.
UPDATE conversations SET status = LOWER(status) WHERE status IS NOT NULL AND status <> LOWER(status);
UPDATE messages SET type = LOWER(type) WHERE type IS NOT NULL AND type <> LOWER(type);
UPDATE notifications SET type = LOWER(type) WHERE type IS NOT NULL AND type <> LOWER(type);
UPDATE quotations SET status = LOWER(status) WHERE status IS NOT NULL AND status <> LOWER(status);
