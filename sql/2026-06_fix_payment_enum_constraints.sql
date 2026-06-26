-- ============================================================================
-- One-time migration: widen stale enum CHECK constraints for the payment feature
-- ============================================================================
-- Hibernate 6.4 auto-generates CHECK constraints for @Enumerated(STRING) columns
-- listing the enum values that existed when the table was first created. With
-- spring.jpa.hibernate.ddl-auto=update, Hibernate NEVER alters these existing
-- constraints, so newly added enum values are rejected at INSERT/UPDATE time
-- (PSQLException -> 500 + transaction rollback).
--
-- This feature adds: OrderStatus.AWAITING_PAYMENT, PaymentMethod.CASH and
-- NotificationType.PAYMENT_REQUESTED. Run this ONCE against each existing
-- database (it is idempotent). Fresh databases created after this change are
-- generated with the correct constraints automatically.
--
-- Note: Postgres auto-names column check constraints "<table>_<column>_check".
-- If your DB uses different names, adjust the DROP statements accordingly
-- (SELECT conname, pg_get_constraintdef(oid) FROM pg_constraint
--    WHERE conrelid = 'orders'::regclass AND contype = 'c';).
-- ============================================================================

ALTER TABLE orders DROP CONSTRAINT IF EXISTS orders_status_check;
ALTER TABLE orders ADD CONSTRAINT orders_status_check
    CHECK (status IN ('NEW','ASSIGNED','SCHEDULED','IN_PROGRESS','AWAITING_PAYMENT','COMPLETED','CANCELLED'));

ALTER TABLE orders DROP CONSTRAINT IF EXISTS orders_payment_method_check;
ALTER TABLE orders ADD CONSTRAINT orders_payment_method_check
    CHECK (payment_method IS NULL OR payment_method IN ('VIETQR','VNPAY','MOMO','BANK_TRANSFER','CASH'));

ALTER TABLE notifications DROP CONSTRAINT IF EXISTS notifications_type_check;
ALTER TABLE notifications ADD CONSTRAINT notifications_type_check
    CHECK (type IN ('ORDER_ACCEPTED','PRICE_ADJUSTMENT','PAYMENT_REQUESTED','ORDER_COMPLETED',
                    'PAYMENT_SUCCESS','PAYMENT_FAILED','WITHDRAW_SUCCESS','WITHDRAW_FAILED',
                    'SYSTEM','PROMOTION','CHAT_MESSAGE'));

-- order_status_history records the OrderStatus transitions, so its from/to status
-- columns carry the same enum CHECK and must include AWAITING_PAYMENT too.
ALTER TABLE order_status_history DROP CONSTRAINT IF EXISTS order_status_history_from_status_check;
ALTER TABLE order_status_history ADD CONSTRAINT order_status_history_from_status_check
    CHECK (from_status IS NULL OR from_status IN ('NEW','ASSIGNED','SCHEDULED','IN_PROGRESS','AWAITING_PAYMENT','COMPLETED','CANCELLED'));

ALTER TABLE order_status_history DROP CONSTRAINT IF EXISTS order_status_history_to_status_check;
ALTER TABLE order_status_history ADD CONSTRAINT order_status_history_to_status_check
    CHECK (to_status IN ('NEW','ASSIGNED','SCHEDULED','IN_PROGRESS','AWAITING_PAYMENT','COMPLETED','CANCELLED'));

-- wallet_transactions.payment_method may also carry a stale CHECK; the payment
-- feature does not write CASH there, but widen it too for safety/consistency.
ALTER TABLE wallet_transactions DROP CONSTRAINT IF EXISTS wallet_transactions_payment_method_check;
ALTER TABLE wallet_transactions ADD CONSTRAINT wallet_transactions_payment_method_check
    CHECK (payment_method IS NULL OR payment_method IN ('VIETQR','VNPAY','MOMO','BANK_TRANSFER','CASH'));
