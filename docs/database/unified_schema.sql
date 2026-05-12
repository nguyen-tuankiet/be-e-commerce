-- GlowUp unified PostgreSQL schema
-- Source: /home/tanluc/Downloads/glowup_schema.dbml + current Spring/JPA entities.
-- Decision: keep BIGSERIAL primary keys and uppercase VARCHAR enums so the existing
-- Java code (GenerationType.IDENTITY + EnumType.STRING) remains compatible.

CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS trigger AS $$
BEGIN
  NEW.updated_at = now();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TABLE IF NOT EXISTS users (
  id              BIGSERIAL PRIMARY KEY,
  code            VARCHAR(32) NOT NULL,
  full_name       VARCHAR(150) NOT NULL,
  email           VARCHAR(255) NOT NULL,
  phone           VARCHAR(20) NOT NULL,
  password        VARCHAR(255) NOT NULL,
  avatar          TEXT,
  role            VARCHAR(30) NOT NULL,
  status          VARCHAR(30) NOT NULL DEFAULT 'PENDING',
  district        VARCHAR(100),
  address         TEXT,
  bio             TEXT,
  email_verified_at TIMESTAMPTZ,
  phone_verified_at TIMESTAMPTZ,
  last_active_at  TIMESTAMPTZ,
  last_login_at   TIMESTAMPTZ,
  metadata        JSONB NOT NULL DEFAULT '{}'::jsonb,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  deleted         BOOLEAN NOT NULL DEFAULT false,
  deleted_at      TIMESTAMPTZ,
  CONSTRAINT ck_users_role CHECK (role IN ('CUSTOMER', 'TECHNICIAN', 'ADMIN')),
  CONSTRAINT ck_users_status CHECK (status IN ('PENDING', 'ACTIVE', 'LOCKED', 'INACTIVE', 'DELETED')),
  CONSTRAINT ck_users_email_format CHECK (email ~* '^[A-Z0-9._%+\-]+@[A-Z0-9.\-]+\.[A-Z]{2,}$'),
  CONSTRAINT ck_users_deleted_at CHECK ((deleted = false AND deleted_at IS NULL) OR deleted = true)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_users_code ON users (code);
CREATE UNIQUE INDEX IF NOT EXISTS uq_users_email_active ON users (lower(email)) WHERE deleted = false;
CREATE UNIQUE INDEX IF NOT EXISTS uq_users_phone_active ON users (phone) WHERE deleted = false;
CREATE INDEX IF NOT EXISTS idx_users_role ON users (role);
CREATE INDEX IF NOT EXISTS idx_users_status ON users (status);
CREATE INDEX IF NOT EXISTS idx_users_role_status ON users (role, status);
CREATE INDEX IF NOT EXISTS idx_users_full_name_trgm ON users USING gin (full_name gin_trgm_ops);

CREATE TABLE IF NOT EXISTS user_addresses (
  id            BIGSERIAL PRIMARY KEY,
  user_id       BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  label         VARCHAR(50),
  address_line  TEXT NOT NULL,
  ward          VARCHAR(100),
  district      VARCHAR(100),
  city          VARCHAR(100),
  latitude      NUMERIC(9,6),
  longitude     NUMERIC(9,6),
  is_default    BOOLEAN NOT NULL DEFAULT false,
  created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT ck_user_addresses_lat CHECK (latitude IS NULL OR latitude BETWEEN -90 AND 90),
  CONSTRAINT ck_user_addresses_lng CHECK (longitude IS NULL OR longitude BETWEEN -180 AND 180)
);

CREATE INDEX IF NOT EXISTS idx_user_addresses_user ON user_addresses (user_id);
CREATE INDEX IF NOT EXISTS idx_user_addresses_area ON user_addresses (district, city);
CREATE UNIQUE INDEX IF NOT EXISTS uq_user_addresses_default ON user_addresses (user_id) WHERE is_default = true;

CREATE TABLE IF NOT EXISTS user_devices (
  id            BIGSERIAL PRIMARY KEY,
  user_id       BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  platform      VARCHAR(20) NOT NULL,
  push_token    TEXT NOT NULL UNIQUE,
  app_version   VARCHAR(20),
  last_seen_at  TIMESTAMPTZ,
  created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT ck_user_devices_platform CHECK (platform IN ('ios', 'android', 'web'))
);

CREATE INDEX IF NOT EXISTS idx_user_devices_user ON user_devices (user_id);

CREATE TABLE IF NOT EXISTS refresh_tokens (
  id          BIGSERIAL PRIMARY KEY,
  token       TEXT NOT NULL UNIQUE,
  token_hash  VARCHAR(255),
  expired_at  TIMESTAMPTZ NOT NULL,
  expires_at  TIMESTAMPTZ GENERATED ALWAYS AS (expired_at) STORED,
  revoked     BOOLEAN NOT NULL DEFAULT false,
  revoked_at  TIMESTAMPTZ,
  user_agent  TEXT,
  ip_address  VARCHAR(45),
  user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user ON refresh_tokens (user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_expired_at ON refresh_tokens (expired_at);

CREATE TABLE IF NOT EXISTS email_confirmation_tokens (
  id          BIGSERIAL PRIMARY KEY,
  token       TEXT NOT NULL UNIQUE,
  expired_at  TIMESTAMPTZ NOT NULL,
  used        BOOLEAN NOT NULL DEFAULT false,
  user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_email_confirmation_tokens_user ON email_confirmation_tokens (user_id);

CREATE TABLE IF NOT EXISTS password_reset_tokens (
  id          BIGSERIAL PRIMARY KEY,
  token       TEXT NOT NULL UNIQUE,
  expired_at  TIMESTAMPTZ NOT NULL,
  used        BOOLEAN NOT NULL DEFAULT false,
  channel     VARCHAR(10),
  user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT ck_password_reset_tokens_channel CHECK (channel IS NULL OR channel IN ('sms', 'email'))
);

CREATE INDEX IF NOT EXISTS idx_password_reset_tokens_user ON password_reset_tokens (user_id);

CREATE TABLE IF NOT EXISTS service_categories (
  id           BIGSERIAL PRIMARY KEY,
  code         VARCHAR(32) NOT NULL UNIQUE,
  parent_id    BIGINT REFERENCES service_categories(id) ON DELETE SET NULL,
  title        VARCHAR(150) NOT NULL,
  description  TEXT,
  icon_url     TEXT,
  priority     VARCHAR(20) NOT NULL DEFAULT 'normal',
  status       VARCHAR(20) NOT NULL DEFAULT 'active',
  sort_order   INTEGER NOT NULL DEFAULT 0,
  created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT ck_service_categories_priority CHECK (priority IN ('high', 'normal', 'low')),
  CONSTRAINT ck_service_categories_status CHECK (status IN ('active', 'inactive')),
  CONSTRAINT ck_service_categories_not_self CHECK (parent_id IS NULL OR parent_id <> id)
);

CREATE INDEX IF NOT EXISTS idx_service_categories_status_sort ON service_categories (status, sort_order);
CREATE INDEX IF NOT EXISTS idx_service_categories_parent ON service_categories (parent_id);

CREATE TABLE IF NOT EXISTS skills (
  id           BIGSERIAL PRIMARY KEY,
  category_id  BIGINT REFERENCES service_categories(id) ON DELETE SET NULL,
  name         VARCHAR(150) NOT NULL UNIQUE,
  created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_skills_category ON skills (category_id);

CREATE TABLE IF NOT EXISTS technician_profiles (
  user_id              BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
  bio                  TEXT,
  cover_image_url      TEXT,
  primary_category_id  BIGINT REFERENCES service_categories(id) ON DELETE SET NULL,
  years_experience     INTEGER,
  price_per_hour       NUMERIC(19,0),
  rating_avg           NUMERIC(3,2) NOT NULL DEFAULT 0,
  rating_count         INTEGER NOT NULL DEFAULT 0,
  completed_jobs       INTEGER NOT NULL DEFAULT 0,
  cancelled_jobs       INTEGER NOT NULL DEFAULT 0,
  is_available         BOOLEAN NOT NULL DEFAULT true,
  available_from       TIMESTAMPTZ,
  tier                 VARCHAR(20) NOT NULL DEFAULT 'normal',
  title_badge          VARCHAR(100),
  verification_status  VARCHAR(20) NOT NULL DEFAULT 'none',
  joined_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT ck_technician_profiles_tier CHECK (tier IN ('normal', 'premium')),
  CONSTRAINT ck_technician_profiles_verification CHECK (verification_status IN ('none', 'pending', 'approved', 'rejected')),
  CONSTRAINT ck_technician_profiles_years CHECK (years_experience IS NULL OR years_experience >= 0),
  CONSTRAINT ck_technician_profiles_price CHECK (price_per_hour IS NULL OR price_per_hour >= 0),
  CONSTRAINT ck_technician_profiles_rating CHECK (rating_avg BETWEEN 0 AND 5 AND rating_count >= 0)
);

CREATE INDEX IF NOT EXISTS idx_technician_profiles_available_rating ON technician_profiles (is_available, rating_avg);
CREATE INDEX IF NOT EXISTS idx_technician_profiles_category ON technician_profiles (primary_category_id);

CREATE TABLE IF NOT EXISTS technician_skills (
  technician_id BIGINT NOT NULL REFERENCES technician_profiles(user_id) ON DELETE CASCADE,
  skill_id      BIGINT NOT NULL REFERENCES skills(id) ON DELETE CASCADE,
  created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (technician_id, skill_id)
);

CREATE INDEX IF NOT EXISTS idx_technician_skills_skill ON technician_skills (skill_id);

CREATE TABLE IF NOT EXISTS technician_service_areas (
  technician_id BIGINT NOT NULL REFERENCES technician_profiles(user_id) ON DELETE CASCADE,
  district      VARCHAR(100) NOT NULL,
  city          VARCHAR(100) NOT NULL,
  created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (technician_id, district, city)
);

CREATE INDEX IF NOT EXISTS idx_technician_service_areas_area ON technician_service_areas (district, city);

CREATE TABLE IF NOT EXISTS technician_schedules (
  id            BIGSERIAL PRIMARY KEY,
  technician_id BIGINT NOT NULL REFERENCES technician_profiles(user_id) ON DELETE CASCADE,
  day_of_week   INTEGER NOT NULL,
  start_time    TIME,
  end_time      TIME,
  is_off        BOOLEAN NOT NULL DEFAULT false,
  CONSTRAINT ck_technician_schedules_day CHECK (day_of_week BETWEEN 0 AND 6),
  CONSTRAINT ck_technician_schedules_time CHECK (is_off = true OR (start_time IS NOT NULL AND end_time IS NOT NULL AND start_time < end_time)),
  UNIQUE (technician_id, day_of_week)
);

CREATE TABLE IF NOT EXISTS orders (
  id               BIGSERIAL PRIMARY KEY,
  code             VARCHAR(32) NOT NULL UNIQUE,
  customer_id      BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  technician_id    BIGINT REFERENCES users(id) ON DELETE SET NULL,
  category_id      BIGINT REFERENCES service_categories(id) ON DELETE SET NULL,
  service_name     VARCHAR(200),
  sub_service      VARCHAR(200),
  device_name      VARCHAR(200),
  description      TEXT,
  address_id       BIGINT REFERENCES user_addresses(id) ON DELETE SET NULL,
  address_text     TEXT NOT NULL,
  district         VARCHAR(100),
  city             VARCHAR(100),
  latitude         NUMERIC(9,6),
  longitude        NUMERIC(9,6),
  estimated_price  NUMERIC(19,0) NOT NULL DEFAULT 0,
  final_price      NUMERIC(19,0),
  payment_method   VARCHAR(30),
  warranty_months  INTEGER NOT NULL DEFAULT 0,
  status           VARCHAR(30) NOT NULL DEFAULT 'new',
  expected_at      TIMESTAMPTZ,
  scheduled_at     TIMESTAMPTZ,
  accepted_at      TIMESTAMPTZ,
  started_at       TIMESTAMPTZ,
  completed_at     TIMESTAMPTZ,
  cancelled_at     TIMESTAMPTZ,
  cancelled_by     VARCHAR(20),
  cancel_reason    TEXT,
  created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
  deleted_at       TIMESTAMPTZ,
  CONSTRAINT ck_orders_status CHECK (status IN ('new', 'assigned', 'scheduled', 'in_progress', 'completed', 'cancelled')),
  CONSTRAINT ck_orders_actor CHECK (cancelled_by IS NULL OR cancelled_by IN ('customer', 'technician', 'admin', 'system')),
  CONSTRAINT ck_orders_payment_method CHECK (payment_method IS NULL OR payment_method IN ('VIETQR', 'VNPAY', 'MOMO', 'BANK_TRANSFER', 'WALLET', 'cash', 'vietqr', 'momo', 'bank_transfer')),
  CONSTRAINT ck_orders_money CHECK (estimated_price >= 0 AND (final_price IS NULL OR final_price >= 0)),
  CONSTRAINT ck_orders_warranty CHECK (warranty_months >= 0),
  CONSTRAINT ck_orders_lat CHECK (latitude IS NULL OR latitude BETWEEN -90 AND 90),
  CONSTRAINT ck_orders_lng CHECK (longitude IS NULL OR longitude BETWEEN -180 AND 180)
);

CREATE INDEX IF NOT EXISTS idx_orders_customer_status ON orders (customer_id, status);
CREATE INDEX IF NOT EXISTS idx_orders_technician_status ON orders (technician_id, status);
CREATE INDEX IF NOT EXISTS idx_orders_status_created ON orders (status, created_at);
CREATE INDEX IF NOT EXISTS idx_orders_area_status ON orders (district, city, status);
CREATE INDEX IF NOT EXISTS idx_orders_scheduled_at ON orders (scheduled_at);

CREATE TABLE IF NOT EXISTS order_status_history (
  id          BIGSERIAL PRIMARY KEY,
  order_id    BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
  from_status VARCHAR(30),
  to_status   VARCHAR(30) NOT NULL,
  actor_id    BIGINT REFERENCES users(id) ON DELETE SET NULL,
  actor_role  VARCHAR(20),
  reason      TEXT,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT ck_order_status_history_to CHECK (to_status IN ('new', 'assigned', 'scheduled', 'in_progress', 'completed', 'cancelled')),
  CONSTRAINT ck_order_status_history_from CHECK (from_status IS NULL OR from_status IN ('new', 'assigned', 'scheduled', 'in_progress', 'completed', 'cancelled')),
  CONSTRAINT ck_order_status_history_actor CHECK (actor_role IS NULL OR actor_role IN ('customer', 'technician', 'admin', 'system'))
);

CREATE INDEX IF NOT EXISTS idx_order_status_history_order_created ON order_status_history (order_id, created_at);

CREATE TABLE IF NOT EXISTS order_images (
  id          BIGSERIAL PRIMARY KEY,
  order_id    BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
  url         TEXT NOT NULL,
  kind        VARCHAR(20) NOT NULL,
  uploaded_by BIGINT REFERENCES users(id) ON DELETE SET NULL,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT ck_order_images_kind CHECK (kind IN ('before', 'completion', 'evidence'))
);

CREATE INDEX IF NOT EXISTS idx_order_images_order_kind ON order_images (order_id, kind);

CREATE TABLE IF NOT EXISTS order_price_adjustments (
  id              BIGSERIAL PRIMARY KEY,
  order_id        BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
  original_price  NUMERIC(19,0) NOT NULL,
  new_price       NUMERIC(19,0) NOT NULL,
  reason          TEXT NOT NULL,
  status          VARCHAR(20) NOT NULL DEFAULT 'pending',
  requested_by    BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  requested_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
  reviewed_by     BIGINT REFERENCES users(id) ON DELETE SET NULL,
  reviewed_at     TIMESTAMPTZ,
  review_note     TEXT,
  CONSTRAINT ck_order_price_adjustments_status CHECK (status IN ('pending', 'approved', 'rejected')),
  CONSTRAINT ck_order_price_adjustments_money CHECK (original_price >= 0 AND new_price >= 0),
  CONSTRAINT ck_order_price_adjustments_review CHECK ((status = 'pending' AND reviewed_at IS NULL) OR status <> 'pending')
);

CREATE INDEX IF NOT EXISTS idx_order_price_adjustments_order_status ON order_price_adjustments (order_id, status);

CREATE TABLE IF NOT EXISTS order_price_adjustment_parts (
  id            BIGSERIAL PRIMARY KEY,
  adjustment_id BIGINT NOT NULL REFERENCES order_price_adjustments(id) ON DELETE CASCADE,
  name          VARCHAR(200) NOT NULL,
  price         NUMERIC(19,0) NOT NULL,
  part_code     VARCHAR(50),
  qty           INTEGER NOT NULL DEFAULT 1,
  CONSTRAINT ck_order_price_adjustment_parts_qty CHECK (qty > 0),
  CONSTRAINT ck_order_price_adjustment_parts_price CHECK (price >= 0)
);

CREATE INDEX IF NOT EXISTS idx_order_price_adjustment_parts_adjustment ON order_price_adjustment_parts (adjustment_id);

CREATE TABLE IF NOT EXISTS order_price_adjustment_evidence (
  id            BIGSERIAL PRIMARY KEY,
  adjustment_id BIGINT NOT NULL REFERENCES order_price_adjustments(id) ON DELETE CASCADE,
  url           TEXT NOT NULL,
  created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_order_price_adjustment_evidence_adjustment ON order_price_adjustment_evidence (adjustment_id);

CREATE TABLE IF NOT EXISTS reviews (
  id            BIGSERIAL PRIMARY KEY,
  order_id      BIGINT NOT NULL UNIQUE REFERENCES orders(id) ON DELETE CASCADE,
  customer_id   BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  technician_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  rating        INTEGER NOT NULL,
  content       TEXT,
  created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT ck_reviews_rating CHECK (rating BETWEEN 1 AND 5),
  CONSTRAINT ck_reviews_distinct_users CHECK (customer_id <> technician_id)
);

CREATE INDEX IF NOT EXISTS idx_reviews_technician_created ON reviews (technician_id, created_at);

CREATE TABLE IF NOT EXISTS review_images (
  id         BIGSERIAL PRIMARY KEY,
  review_id  BIGINT NOT NULL REFERENCES reviews(id) ON DELETE CASCADE,
  url        TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_review_images_review ON review_images (review_id);

CREATE TABLE IF NOT EXISTS warranty_claims (
  id                  BIGSERIAL PRIMARY KEY,
  code                VARCHAR(32) NOT NULL UNIQUE,
  order_id            BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
  customer_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  technician_id       BIGINT REFERENCES users(id) ON DELETE SET NULL,
  status              VARCHAR(20) NOT NULL DEFAULT 'pending',
  description         TEXT NOT NULL,
  scheduled_at        TIMESTAMPTZ,
  warranty_expires_at TIMESTAMPTZ NOT NULL,
  resolved_at         TIMESTAMPTZ,
  created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT ck_warranty_claims_status CHECK (status IN ('pending', 'in_progress', 'completed', 'rejected', 'expired'))
);

CREATE INDEX IF NOT EXISTS idx_warranty_claims_customer ON warranty_claims (customer_id);
CREATE INDEX IF NOT EXISTS idx_warranty_claims_status_created ON warranty_claims (status, created_at);
CREATE INDEX IF NOT EXISTS idx_warranty_claims_expires ON warranty_claims (warranty_expires_at);

CREATE TABLE IF NOT EXISTS warranty_claim_images (
  id          BIGSERIAL PRIMARY KEY,
  warranty_id BIGINT NOT NULL REFERENCES warranty_claims(id) ON DELETE CASCADE,
  url         TEXT NOT NULL,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_warranty_claim_images_warranty ON warranty_claim_images (warranty_id);

CREATE TABLE IF NOT EXISTS order_reports (
  id              BIGSERIAL PRIMARY KEY,
  code            VARCHAR(32) NOT NULL UNIQUE,
  order_id        BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
  reporter_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  against_id      BIGINT REFERENCES users(id) ON DELETE SET NULL,
  reason          VARCHAR(30) NOT NULL,
  description     TEXT,
  status          VARCHAR(30) NOT NULL DEFAULT 'open',
  resolved_by     BIGINT REFERENCES users(id) ON DELETE SET NULL,
  resolved_at     TIMESTAMPTZ,
  resolution_note TEXT,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT ck_order_reports_reason CHECK (reason IN ('extra_fee', 'bad_attitude', 'no_show', 'poor_quality', 'fraud', 'other')),
  CONSTRAINT ck_order_reports_status CHECK (status IN ('open', 'investigating', 'resolved', 'dismissed'))
);

CREATE INDEX IF NOT EXISTS idx_order_reports_status_created ON order_reports (status, created_at);
CREATE INDEX IF NOT EXISTS idx_order_reports_reporter ON order_reports (reporter_id);

CREATE TABLE IF NOT EXISTS order_report_evidence (
  id         BIGSERIAL PRIMARY KEY,
  report_id  BIGINT NOT NULL REFERENCES order_reports(id) ON DELETE CASCADE,
  url        TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_order_report_evidence_report ON order_report_evidence (report_id);

CREATE TABLE IF NOT EXISTS conversations (
  id              BIGSERIAL PRIMARY KEY,
  code            VARCHAR(32) NOT NULL UNIQUE,
  order_id        BIGINT REFERENCES orders(id) ON DELETE SET NULL,
  status          VARCHAR(20) NOT NULL DEFAULT 'active',
  last_message_at TIMESTAMPTZ,
  last_message_id BIGINT,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT ck_conversations_status CHECK (status IN ('active', 'archived', 'closed'))
);

CREATE INDEX IF NOT EXISTS idx_conversations_order ON conversations (order_id);
CREATE INDEX IF NOT EXISTS idx_conversations_status ON conversations (status);
CREATE INDEX IF NOT EXISTS idx_conversations_last_message_at ON conversations (last_message_at);

CREATE TABLE IF NOT EXISTS conversation_participants (
  conversation_id BIGINT NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
  user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  role            VARCHAR(30) NOT NULL,
  unread_count    INTEGER NOT NULL DEFAULT 0,
  last_read_at    TIMESTAMPTZ,
  joined_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
  left_at         TIMESTAMPTZ,
  PRIMARY KEY (conversation_id, user_id),
  CONSTRAINT ck_conversation_participants_role CHECK (role IN ('CUSTOMER', 'TECHNICIAN', 'ADMIN')),
  CONSTRAINT ck_conversation_participants_unread CHECK (unread_count >= 0)
);

CREATE INDEX IF NOT EXISTS idx_conversation_participants_user ON conversation_participants (user_id);

CREATE TABLE IF NOT EXISTS quotations (
  id              BIGSERIAL PRIMARY KEY,
  code            VARCHAR(32) NOT NULL UNIQUE,
  conversation_id BIGINT NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
  technician_id   BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  customer_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  service_name    VARCHAR(200) NOT NULL,
  description     TEXT,
  price           NUMERIC(19,0) NOT NULL,
  scheduled_at    TIMESTAMPTZ,
  notes           TEXT,
  status          VARCHAR(20) NOT NULL DEFAULT 'pending',
  order_id        BIGINT REFERENCES orders(id) ON DELETE SET NULL,
  expires_at      TIMESTAMPTZ,
  accepted_at     TIMESTAMPTZ,
  rejected_at     TIMESTAMPTZ,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT ck_quotations_status CHECK (status IN ('pending', 'accepted', 'rejected', 'expired')),
  CONSTRAINT ck_quotations_price CHECK (price >= 0),
  CONSTRAINT ck_quotations_distinct_users CHECK (technician_id <> customer_id)
);

CREATE INDEX IF NOT EXISTS idx_quotations_technician_status ON quotations (technician_id, status);
CREATE INDEX IF NOT EXISTS idx_quotations_conversation ON quotations (conversation_id);

CREATE TABLE IF NOT EXISTS messages (
  id              BIGSERIAL PRIMARY KEY,
  conversation_id BIGINT NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
  sender_id       BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  type            VARCHAR(20) NOT NULL DEFAULT 'text',
  content         TEXT,
  attachment_url  TEXT,
  quotation_id    BIGINT REFERENCES quotations(id) ON DELETE SET NULL,
  client_msg_id   VARCHAR(64),
  sent_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
  edited_at       TIMESTAMPTZ,
  deleted_at      TIMESTAMPTZ,
  CONSTRAINT ck_messages_type CHECK (type IN ('text', 'image', 'quotation', 'system')),
  CONSTRAINT ck_messages_body CHECK (content IS NOT NULL OR attachment_url IS NOT NULL OR quotation_id IS NOT NULL OR type = 'system')
);

CREATE INDEX IF NOT EXISTS idx_messages_conversation_sent ON messages (conversation_id, sent_at);
CREATE INDEX IF NOT EXISTS idx_messages_sender ON messages (sender_id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_messages_idempotency ON messages (sender_id, client_msg_id) WHERE client_msg_id IS NOT NULL;

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM pg_constraint
    WHERE conname = 'fk_conversations_last_message'
      AND conrelid = 'conversations'::regclass
  ) THEN
    ALTER TABLE conversations
      ADD CONSTRAINT fk_conversations_last_message
      FOREIGN KEY (last_message_id) REFERENCES messages(id)
      DEFERRABLE INITIALLY DEFERRED;
  END IF;
END;
$$;

CREATE TABLE IF NOT EXISTS message_read_receipts (
  message_id BIGINT NOT NULL REFERENCES messages(id) ON DELETE CASCADE,
  user_id    BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  read_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (message_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_message_read_receipts_user ON message_read_receipts (user_id);

CREATE TABLE IF NOT EXISTS wallets (
  id              BIGSERIAL PRIMARY KEY,
  user_id         BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
  balance         NUMERIC(19,0) NOT NULL DEFAULT 0,
  pending_balance NUMERIC(19,0) NOT NULL DEFAULT 0,
  total_earned    NUMERIC(19,0) NOT NULL DEFAULT 0,
  total_withdrawn NUMERIC(19,0) NOT NULL DEFAULT 0,
  currency        VARCHAR(10) NOT NULL DEFAULT 'VND',
  version         BIGINT NOT NULL DEFAULT 0,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT ck_wallets_amounts CHECK (balance >= 0 AND pending_balance >= 0 AND total_earned >= 0 AND total_withdrawn >= 0),
  CONSTRAINT ck_wallets_currency CHECK (currency = upper(currency))
);

CREATE INDEX IF NOT EXISTS idx_wallets_user_id ON wallets (user_id);

CREATE TABLE IF NOT EXISTS bank_accounts (
  id                        BIGSERIAL PRIMARY KEY,
  code                      VARCHAR(40) UNIQUE,
  user_id                   BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  bank_name                 VARCHAR(120) NOT NULL,
  bank_code                 VARCHAR(20),
  account_number            VARCHAR(32),
  account_number_encrypted  TEXT,
  account_number_masked     VARCHAR(50),
  account_owner             VARCHAR(150) NOT NULL,
  is_default                BOOLEAN NOT NULL DEFAULT false,
  is_verified               BOOLEAN NOT NULL DEFAULT false,
  created_at                TIMESTAMPTZ NOT NULL DEFAULT now(),
  deleted_at                TIMESTAMPTZ,
  CONSTRAINT ck_bank_accounts_number_present CHECK (account_number IS NOT NULL OR account_number_encrypted IS NOT NULL),
  CONSTRAINT ck_bank_accounts_masked_present CHECK (account_number_masked IS NOT NULL OR account_number IS NOT NULL)
);

CREATE INDEX IF NOT EXISTS idx_bank_accounts_user_id ON bank_accounts (user_id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_bank_accounts_default ON bank_accounts (user_id) WHERE is_default = true AND deleted_at IS NULL;

CREATE TABLE IF NOT EXISTS wallet_transactions (
  id                     BIGSERIAL PRIMARY KEY,
  transaction_code       VARCHAR(40) NOT NULL UNIQUE,
  code                   VARCHAR(40) GENERATED ALWAYS AS (transaction_code) STORED,
  wallet_id              BIGINT NOT NULL REFERENCES wallets(id) ON DELETE RESTRICT,
  wallet_user_id         BIGINT REFERENCES users(id) ON DELETE RESTRICT,
  type                   VARCHAR(30) NOT NULL,
  category               VARCHAR(120) NOT NULL,
  title                  VARCHAR(255) NOT NULL,
  description            TEXT,
  amount                 NUMERIC(19,0) NOT NULL,
  fee                    NUMERIC(19,0) NOT NULL DEFAULT 0,
  net_amount             NUMERIC(19,0) NOT NULL DEFAULT 0,
  balance_after          NUMERIC(19,0),
  status                 VARCHAR(30) NOT NULL,
  payment_method         VARCHAR(30),
  bank_account_id        BIGINT REFERENCES bank_accounts(id) ON DELETE SET NULL,
  transfer_content       VARCHAR(255),
  gateway_request_id     VARCHAR(120),
  gateway_transaction_id VARCHAR(120),
  gateway_payload        TEXT,
  qr_code                TEXT,
  ref_type               VARCHAR(30),
  ref_id                 BIGINT,
  metadata               JSONB NOT NULL DEFAULT '{}'::jsonb,
  expired_at             TIMESTAMPTZ,
  processed_at           TIMESTAMPTZ,
  posted_at              TIMESTAMPTZ,
  created_at             TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT ck_wallet_transactions_type CHECK (type IN ('TOPUP', 'WITHDRAW', 'COMMISSION', 'PAYMENT', 'REFUND', 'EARNING', 'ADJUSTMENT', 'WARRANTY_HOLD')),
  CONSTRAINT ck_wallet_transactions_status CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED', 'CANCELLED', 'AWAITING_PAYMENT', 'PENDING_VERIFICATION')),
  CONSTRAINT ck_wallet_transactions_payment_method CHECK (payment_method IS NULL OR payment_method IN ('VIETQR', 'VNPAY', 'MOMO', 'BANK_TRANSFER')),
  CONSTRAINT ck_wallet_transactions_fee CHECK (fee >= 0)
);

CREATE INDEX IF NOT EXISTS idx_wallet_transactions_wallet_id ON wallet_transactions (wallet_id);
CREATE INDEX IF NOT EXISTS idx_wallet_transactions_wallet_user_created ON wallet_transactions (wallet_user_id, created_at);
CREATE INDEX IF NOT EXISTS idx_wallet_transactions_type ON wallet_transactions (type);
CREATE INDEX IF NOT EXISTS idx_wallet_transactions_status ON wallet_transactions (status);
CREATE INDEX IF NOT EXISTS idx_wallet_transactions_ref ON wallet_transactions (ref_type, ref_id);

CREATE TABLE IF NOT EXISTS topup_requests (
  id               BIGSERIAL PRIMARY KEY,
  transaction_id   BIGINT REFERENCES wallet_transactions(id) ON DELETE SET NULL,
  user_id          BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  method           VARCHAR(30) NOT NULL,
  amount           NUMERIC(19,0) NOT NULL,
  bank_name        VARCHAR(100),
  account_name     VARCHAR(150),
  account_number   VARCHAR(50),
  transfer_content VARCHAR(100) NOT NULL UNIQUE,
  qr_payload       TEXT,
  expires_at       TIMESTAMPTZ,
  status           VARCHAR(30) NOT NULL DEFAULT 'AWAITING_PAYMENT',
  paid_at          TIMESTAMPTZ,
  verified_at      TIMESTAMPTZ,
  created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT ck_topup_requests_method CHECK (method IN ('vietqr', 'momo', 'bank', 'card', 'vnpay')),
  CONSTRAINT ck_topup_requests_amount CHECK (amount > 0),
  CONSTRAINT ck_topup_requests_status CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED', 'CANCELLED', 'AWAITING_PAYMENT', 'PENDING_VERIFICATION'))
);

CREATE INDEX IF NOT EXISTS idx_topup_requests_user_status ON topup_requests (user_id, status);

CREATE TABLE IF NOT EXISTS withdraw_requests (
  id              BIGSERIAL PRIMARY KEY,
  code            VARCHAR(32) NOT NULL UNIQUE,
  transaction_id  BIGINT REFERENCES wallet_transactions(id) ON DELETE SET NULL,
  user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  bank_account_id BIGINT NOT NULL REFERENCES bank_accounts(id) ON DELETE RESTRICT,
  amount          NUMERIC(19,0) NOT NULL,
  fee             NUMERIC(19,0) NOT NULL DEFAULT 0,
  net_amount      NUMERIC(19,0) NOT NULL,
  status          VARCHAR(30) NOT NULL DEFAULT 'pending',
  processed_by    BIGINT REFERENCES users(id) ON DELETE SET NULL,
  processed_at    TIMESTAMPTZ,
  estimated_at    TIMESTAMPTZ,
  reject_reason   TEXT,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT ck_withdraw_requests_status CHECK (status IN ('pending', 'approved', 'processing', 'paid', 'rejected')),
  CONSTRAINT ck_withdraw_requests_amount CHECK (amount > 0 AND fee >= 0 AND net_amount = amount - fee)
);

CREATE INDEX IF NOT EXISTS idx_withdraw_requests_user_status ON withdraw_requests (user_id, status);
CREATE INDEX IF NOT EXISTS idx_withdraw_requests_status_created ON withdraw_requests (status, created_at);

CREATE TABLE IF NOT EXISTS commission_settings (
  id                   BIGSERIAL PRIMARY KEY,
  platform_fee_percent NUMERIC(5,2) NOT NULL,
  vat_percent          NUMERIC(5,2) NOT NULL,
  effective_from       TIMESTAMPTZ NOT NULL DEFAULT now(),
  effective_to         TIMESTAMPTZ,
  updated_by           BIGINT REFERENCES users(id) ON DELETE SET NULL,
  created_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT ck_commission_settings_percent CHECK (platform_fee_percent BETWEEN 0 AND 100 AND vat_percent BETWEEN 0 AND 100),
  CONSTRAINT ck_commission_settings_range CHECK (effective_to IS NULL OR effective_to > effective_from)
);

CREATE INDEX IF NOT EXISTS idx_commission_settings_effective ON commission_settings (effective_from, effective_to);

CREATE TABLE IF NOT EXISTS verifications (
  id                    BIGSERIAL PRIMARY KEY,
  code                  VARCHAR(32) NOT NULL UNIQUE,
  technician_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  full_name_snapshot    VARCHAR(150) NOT NULL,
  phone_snapshot        VARCHAR(20),
  email_snapshot        VARCHAR(255),
  district              VARCHAR(100),
  city                  VARCHAR(100),
  service_category_id   BIGINT REFERENCES service_categories(id) ON DELETE SET NULL,
  service_category_text VARCHAR(150),
  years_experience      INTEGER,
  status                VARCHAR(20) NOT NULL DEFAULT 'pending',
  note                  TEXT,
  reviewed_by           BIGINT REFERENCES users(id) ON DELETE SET NULL,
  reviewed_at           TIMESTAMPTZ,
  submitted_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT ck_verifications_status CHECK (status IN ('pending', 'approved', 'rejected')),
  CONSTRAINT ck_verifications_years CHECK (years_experience IS NULL OR years_experience >= 0)
);

CREATE INDEX IF NOT EXISTS idx_verifications_technician ON verifications (technician_id);
CREATE INDEX IF NOT EXISTS idx_verifications_status_submitted ON verifications (status, submitted_at);

CREATE TABLE IF NOT EXISTS verification_documents (
  id              BIGSERIAL PRIMARY KEY,
  verification_id BIGINT NOT NULL REFERENCES verifications(id) ON DELETE CASCADE,
  doc_type        VARCHAR(30) NOT NULL,
  url             TEXT NOT NULL,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT ck_verification_documents_doc_type CHECK (doc_type IN ('id_front', 'id_back', 'portrait', 'certificate', 'selfie', 'other')),
  UNIQUE (verification_id, doc_type)
);

CREATE TABLE IF NOT EXISTS notifications (
  id                BIGSERIAL PRIMARY KEY,
  notification_code VARCHAR(30) NOT NULL UNIQUE,
  user_id           BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  type              VARCHAR(40) NOT NULL,
  title             VARCHAR(255) NOT NULL,
  body              VARCHAR(1000) NOT NULL,
  data_json         TEXT,
  data              JSONB,
  is_read           BOOLEAN NOT NULL DEFAULT false,
  read_at           TIMESTAMPTZ,
  created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT ck_notifications_type CHECK (type IN (
    'ORDER_ACCEPTED', 'PRICE_ADJUSTMENT', 'ORDER_COMPLETED', 'PAYMENT_SUCCESS',
    'PAYMENT_FAILED', 'WITHDRAW_SUCCESS', 'WITHDRAW_FAILED', 'SYSTEM',
    'PROMOTION', 'CHAT_MESSAGE', 'ORDER_NEW', 'ORDER_REJECTED', 'ORDER_IN_PROGRESS',
    'ORDER_CANCELLED', 'PRICE_APPROVED', 'PRICE_REJECTED', 'MESSAGE_NEW',
    'REVIEW_NEW', 'WARRANTY_NEW', 'WITHDRAW_APPROVED', 'WITHDRAW_REJECTED',
    'VERIFICATION_APPROVED', 'VERIFICATION_REJECTED'
  )),
  CONSTRAINT ck_notifications_read_at CHECK ((is_read = false AND read_at IS NULL) OR is_read = true)
);

CREATE INDEX IF NOT EXISTS idx_notifications_user_id ON notifications (user_id);
CREATE INDEX IF NOT EXISTS idx_notifications_user_read ON notifications (user_id, is_read);
CREATE INDEX IF NOT EXISTS idx_notifications_user_created ON notifications (user_id, created_at);
CREATE INDEX IF NOT EXISTS idx_notifications_type ON notifications (type);

CREATE TABLE IF NOT EXISTS notification_preferences (
  user_id        BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
  new_order      BOOLEAN NOT NULL DEFAULT true,
  customer_email BOOLEAN NOT NULL DEFAULT true,
  weekly_revenue BOOLEAN NOT NULL DEFAULT false,
  security_alert BOOLEAN NOT NULL DEFAULT true,
  push_enabled   BOOLEAN NOT NULL DEFAULT true,
  email_enabled  BOOLEAN NOT NULL DEFAULT true,
  sms_enabled    BOOLEAN NOT NULL DEFAULT false,
  updated_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS app_settings (
  id         BIGSERIAL PRIMARY KEY,
  scope      VARCHAR(30) NOT NULL UNIQUE,
  data       JSONB NOT NULL,
  version    INTEGER NOT NULL DEFAULT 1,
  updated_by BIGINT REFERENCES users(id) ON DELETE SET NULL,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT ck_app_settings_scope CHECK (scope IN ('general', 'billing', 'notifications', 'operations')),
  CONSTRAINT ck_app_settings_version CHECK (version > 0)
);

CREATE TABLE IF NOT EXISTS media_files (
  id          BIGSERIAL PRIMARY KEY,
  uploader_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
  folder      VARCHAR(30) NOT NULL,
  url         TEXT NOT NULL,
  filename    VARCHAR(255),
  mime_type   VARCHAR(50),
  size_bytes  BIGINT,
  ref_type    VARCHAR(30),
  ref_id      BIGINT,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
  deleted_at  TIMESTAMPTZ,
  CONSTRAINT ck_media_files_folder CHECK (folder IN ('avatars', 'orders', 'verifications', 'categories', 'reviews', 'warranty', 'reports', 'evidence', 'misc')),
  CONSTRAINT ck_media_files_size CHECK (size_bytes IS NULL OR size_bytes >= 0)
);

CREATE INDEX IF NOT EXISTS idx_media_files_uploader ON media_files (uploader_id);
CREATE INDEX IF NOT EXISTS idx_media_files_ref ON media_files (ref_type, ref_id);
CREATE INDEX IF NOT EXISTS idx_media_files_folder ON media_files (folder);

CREATE TABLE IF NOT EXISTS audit_logs (
  id          BIGSERIAL PRIMARY KEY,
  actor_id    BIGINT REFERENCES users(id) ON DELETE SET NULL,
  action      VARCHAR(80) NOT NULL,
  target_type VARCHAR(40),
  target_id   BIGINT,
  before      JSONB,
  after       JSONB,
  ip_address  VARCHAR(45),
  user_agent  TEXT,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_audit_logs_actor ON audit_logs (actor_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_target ON audit_logs (target_type, target_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_action ON audit_logs (action);
CREATE INDEX IF NOT EXISTS idx_audit_logs_created_at ON audit_logs (created_at);

DROP TRIGGER IF EXISTS trg_users_updated_at ON users;
CREATE TRIGGER trg_users_updated_at BEFORE UPDATE ON users
FOR EACH ROW EXECUTE FUNCTION set_updated_at();
DROP TRIGGER IF EXISTS trg_user_addresses_updated_at ON user_addresses;
CREATE TRIGGER trg_user_addresses_updated_at BEFORE UPDATE ON user_addresses
FOR EACH ROW EXECUTE FUNCTION set_updated_at();
DROP TRIGGER IF EXISTS trg_service_categories_updated_at ON service_categories;
CREATE TRIGGER trg_service_categories_updated_at BEFORE UPDATE ON service_categories
FOR EACH ROW EXECUTE FUNCTION set_updated_at();
DROP TRIGGER IF EXISTS trg_orders_updated_at ON orders;
CREATE TRIGGER trg_orders_updated_at BEFORE UPDATE ON orders
FOR EACH ROW EXECUTE FUNCTION set_updated_at();
DROP TRIGGER IF EXISTS trg_reviews_updated_at ON reviews;
CREATE TRIGGER trg_reviews_updated_at BEFORE UPDATE ON reviews
FOR EACH ROW EXECUTE FUNCTION set_updated_at();
DROP TRIGGER IF EXISTS trg_warranty_claims_updated_at ON warranty_claims;
CREATE TRIGGER trg_warranty_claims_updated_at BEFORE UPDATE ON warranty_claims
FOR EACH ROW EXECUTE FUNCTION set_updated_at();
DROP TRIGGER IF EXISTS trg_order_reports_updated_at ON order_reports;
CREATE TRIGGER trg_order_reports_updated_at BEFORE UPDATE ON order_reports
FOR EACH ROW EXECUTE FUNCTION set_updated_at();
DROP TRIGGER IF EXISTS trg_conversations_updated_at ON conversations;
CREATE TRIGGER trg_conversations_updated_at BEFORE UPDATE ON conversations
FOR EACH ROW EXECUTE FUNCTION set_updated_at();
DROP TRIGGER IF EXISTS trg_wallets_updated_at ON wallets;
CREATE TRIGGER trg_wallets_updated_at BEFORE UPDATE ON wallets
FOR EACH ROW EXECUTE FUNCTION set_updated_at();
DROP TRIGGER IF EXISTS trg_notification_preferences_updated_at ON notification_preferences;
CREATE TRIGGER trg_notification_preferences_updated_at BEFORE UPDATE ON notification_preferences
FOR EACH ROW EXECUTE FUNCTION set_updated_at();
DROP TRIGGER IF EXISTS trg_app_settings_updated_at ON app_settings;
CREATE TRIGGER trg_app_settings_updated_at BEFORE UPDATE ON app_settings
FOR EACH ROW EXECUTE FUNCTION set_updated_at();
