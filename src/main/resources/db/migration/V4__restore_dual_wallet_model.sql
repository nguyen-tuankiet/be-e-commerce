ALTER TABLE wallets
    ADD COLUMN IF NOT EXISTS credit_balance NUMERIC(19, 0) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS personal_balance NUMERIC(19, 0) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS pending_withdraw_balance NUMERIC(19, 0) NOT NULL DEFAULT 0;

UPDATE wallets
SET personal_balance = GREATEST(
        COALESCE(total_earned, 0) - COALESCE(total_withdrawn, 0) - COALESCE(pending_balance, 0),
        0
    )
WHERE COALESCE(personal_balance, 0) = 0;

UPDATE wallets
SET credit_balance = GREATEST(
        COALESCE(balance, 0) - COALESCE(personal_balance, 0),
        0
    )
WHERE COALESCE(credit_balance, 0) = 0;

UPDATE wallets
SET pending_withdraw_balance = COALESCE(pending_balance, 0)
WHERE COALESCE(pending_withdraw_balance, 0) = 0;

ALTER TABLE wallet_transactions
    ADD COLUMN IF NOT EXISTS wallet_type VARCHAR(30);

UPDATE wallet_transactions
SET wallet_type = CASE
    WHEN type = 'TOPUP' THEN 'CREDIT'
    WHEN type = 'WITHDRAW' THEN 'PERSONAL'
    WHEN type = 'PAYMENT' THEN 'CREDIT'
    WHEN type = 'REFUND' THEN 'PERSONAL'
    WHEN type = 'COMMISSION' AND category = 'REVENUE' THEN 'PERSONAL'
    WHEN type = 'COMMISSION' AND category = 'COMMISSION_DEDUCTION' THEN 'CREDIT'
    ELSE 'CREDIT'
END
WHERE wallet_type IS NULL;

ALTER TABLE wallet_transactions
    ALTER COLUMN wallet_type SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_wallet_transactions_wallet_type
    ON wallet_transactions(wallet_type);
