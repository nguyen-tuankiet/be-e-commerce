ALTER TABLE wallet_transactions ADD COLUMN order_id BIGINT;
ALTER TABLE wallet_transactions ADD CONSTRAINT fk_wallet_transaction_order FOREIGN KEY (order_id) REFERENCES orders(id);

