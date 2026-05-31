ALTER TABLE wallet_transactions
    ADD COLUMN IF NOT EXISTS after_balance BIGINT,
    ADD COLUMN IF NOT EXISTS note VARCHAR(255),
    ADD COLUMN IF NOT EXISTS actor VARCHAR(50),
    ADD COLUMN IF NOT EXISTS related_order_code VARCHAR(100);

INSERT INTO system_settings (setting_key, setting_value, created_at, updated_at)
VALUES
    ('fixed_commission_fee', '10000', NOW(), NOW()),
    ('minimum_commission_balance', '20000', NOW(), NOW()),
    ('auto_lock_enabled', 'true', NOW(), NOW())
ON CONFLICT (setting_key)
DO UPDATE SET
    setting_value = EXCLUDED.setting_value,
    updated_at = NOW();
