-- =====================================================================
-- GlowUp Concierge — demo seed data
-- =====================================================================
-- Idempotent: an toàn để chạy nhiều lần. ON CONFLICT (code) DO NOTHING.
--
-- Yêu cầu trong application.yml:
--   spring.jpa.defer-datasource-initialization: true
--   spring.sql.init.mode: always
--
-- Tất cả user trong file này dùng password DEMO: "password"
-- BCrypt hash dưới đây = $2a$10$v7D/LWQ3B1QEhFUKA9tCP.9U.xWWiDv92EL7pSQnkPXUIB51i0Mwm
-- (đã verify bcrypt.checkpw("password") == True)
--
-- Login mẫu sau khi seed:
--   admin@glowup.vn  / password   (role: admin)
--   lan@email.com    / password   (role: customer)
--   hoang@email.com  / password   (role: customer)
--   tuan@glowup.pro  / password   (role: technician)
--   minh@glowup.pro  / password   (role: technician)
-- =====================================================================


-- ---------------------------------------------------------------------
-- 1. USERS — 1 admin + 2 customers + 2 technicians
-- ---------------------------------------------------------------------

INSERT INTO users (code, full_name, email, phone, password, role, status, district, address, bio, avatar, deleted, created_at, updated_at)
VALUES
('USR-001', 'Quản trị viên', 'admin@glowup.vn', '0900000000',
 '$2a$10$v7D/LWQ3B1QEhFUKA9tCP.9U.xWWiDv92EL7pSQnkPXUIB51i0Mwm',
 'ADMIN', 'ACTIVE', 'Quận 1', '123 Lê Lợi, Quận 1', NULL,
 'https://i.pravatar.cc/150?img=68', FALSE, NOW(), NOW()),

('USR-002', 'Trần Thị Lan', 'lan@email.com', '0901234567',
 '$2a$10$v7D/LWQ3B1QEhFUKA9tCP.9U.xWWiDv92EL7pSQnkPXUIB51i0Mwm',
 'CUSTOMER', 'ACTIVE', 'Quận 7', '123 Nguyễn Văn Linh, Quận 7', NULL,
 'https://i.pravatar.cc/150?img=5', FALSE, NOW(), NOW()),

('USR-003', 'Phạm Hoàng', 'hoang@email.com', '0901234568',
 '$2a$10$v7D/LWQ3B1QEhFUKA9tCP.9U.xWWiDv92EL7pSQnkPXUIB51i0Mwm',
 'CUSTOMER', 'ACTIVE', 'Quận 3', '45 Võ Văn Tần, Quận 3', NULL,
 'https://i.pravatar.cc/150?img=12', FALSE, NOW(), NOW()),

('USR-004', 'Trần Anh Tuấn', 'tuan@glowup.pro', '0987654321',
 '$2a$10$v7D/LWQ3B1QEhFUKA9tCP.9U.xWWiDv92EL7pSQnkPXUIB51i0Mwm',
 'TECHNICIAN', 'ACTIVE', 'Quận 1', '25 Bis Nguyễn Thị Minh Khai, Quận 1',
 'Hơn 10 năm kinh nghiệm sửa điện lạnh, bảo trì máy lạnh, hệ thống thông gió.',
 'https://i.pravatar.cc/150?img=33', FALSE, NOW(), NOW()),

('USR-005', 'Nguyễn Văn Minh', 'minh@glowup.pro', '0987654322',
 '$2a$10$v7D/LWQ3B1QEhFUKA9tCP.9U.xWWiDv92EL7pSQnkPXUIB51i0Mwm',
 'TECHNICIAN', 'ACTIVE', 'Quận Bình Thạnh', '78 Đinh Bộ Lĩnh, Bình Thạnh',
 'Chuyên sửa máy giặt cửa ngang, tủ lạnh inverter các hãng.',
 'https://i.pravatar.cc/150?img=13', FALSE, NOW(), NOW())

ON CONFLICT (code) DO NOTHING;


-- ---------------------------------------------------------------------
-- 2. CATEGORIES — 5 dịch vụ chính
-- ---------------------------------------------------------------------

INSERT INTO categories (code, title, description, icon_url, priority, status, deleted, created_at, updated_at)
VALUES
('CAT-001', 'Máy lạnh',     'Vệ sinh, bơm gas, sửa chữa board mạch máy lạnh inverter.',
  NULL, 'HIGH',   'ACTIVE', FALSE, NOW(), NOW()),
('CAT-002', 'Máy giặt',     'Sửa chữa máy giặt cửa ngang, cửa trên, vắt không sạch, không xả nước.',
  NULL, 'HIGH',   'ACTIVE', FALSE, NOW(), NOW()),
('CAT-003', 'Tủ lạnh',      'Sửa tủ lạnh không lạnh, chảy nước, kêu to, board điện tử.',
  NULL, 'NORMAL', 'ACTIVE', FALSE, NOW(), NOW()),
('CAT-004', 'Máy nước nóng','Lắp đặt, sửa chữa, bảo trì máy nước nóng năng lượng mặt trời + điện.',
  NULL, 'NORMAL', 'ACTIVE', FALSE, NOW(), NOW()),
('CAT-005', 'Khác',         'Các thiết bị gia dụng khác: lò vi sóng, máy lọc nước, quạt công nghiệp.',
  NULL, 'LOW',    'ACTIVE', FALSE, NOW(), NOW())

ON CONFLICT (code) DO NOTHING;


-- ---------------------------------------------------------------------
-- 3. TECHNICIAN PROFILES — cho 2 thợ
-- ---------------------------------------------------------------------

INSERT INTO technician_profiles (user_id, cover_image, service_category, price_per_hour, is_available,
                                  type, title_badge, years_experience, verification_status,
                                  last_active_at, joined_at, updated_at)
SELECT u.id,
       'https://images.unsplash.com/photo-1556909114-f6e7ad7d3136',
       'Điện lạnh', 250000, TRUE,
       'PREMIUM', 'CHUYÊN GIA KIM CƯƠNG', 10, 'APPROVED',
       NOW(), NOW() - INTERVAL '2 years', NOW()
FROM users u WHERE u.code = 'USR-004'
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO technician_profiles (user_id, cover_image, service_category, price_per_hour, is_available,
                                  type, title_badge, years_experience, verification_status,
                                  last_active_at, joined_at, updated_at)
SELECT u.id,
       'https://images.unsplash.com/photo-1581092160562-40aa08e78837',
       'Máy giặt', 180000, TRUE,
       'NORMAL', NULL, 5, 'APPROVED',
       NOW(), NOW() - INTERVAL '1 year', NOW()
FROM users u WHERE u.code = 'USR-005'
ON CONFLICT (user_id) DO NOTHING;


-- ---------------------------------------------------------------------
-- 4. TECHNICIAN SKILLS — collection table
-- ---------------------------------------------------------------------

INSERT INTO technician_skills (profile_id, skill)
SELECT tp.id, s.skill
FROM technician_profiles tp
JOIN users u ON tp.user_id = u.id
CROSS JOIN LATERAL (
    VALUES ('Sửa điện lạnh'), ('Bảo trì máy lạnh'), ('Hệ thống thông gió')
) AS s(skill)
WHERE u.code = 'USR-004'
  AND NOT EXISTS (
      SELECT 1 FROM technician_skills ts WHERE ts.profile_id = tp.id AND ts.skill = s.skill
  );

INSERT INTO technician_skills (profile_id, skill)
SELECT tp.id, s.skill
FROM technician_profiles tp
JOIN users u ON tp.user_id = u.id
CROSS JOIN LATERAL (
    VALUES ('Sửa máy giặt cửa ngang'), ('Sửa tủ lạnh inverter'), ('Thay board điện tử')
) AS s(skill)
WHERE u.code = 'USR-005'
  AND NOT EXISTS (
      SELECT 1 FROM technician_skills ts WHERE ts.profile_id = tp.id AND ts.skill = s.skill
  );


-- ---------------------------------------------------------------------
-- 5. TECHNICIAN SERVICE AREAS
-- ---------------------------------------------------------------------

INSERT INTO technician_service_areas (profile_id, area)
SELECT tp.id, a.area
FROM technician_profiles tp
JOIN users u ON tp.user_id = u.id
CROSS JOIN LATERAL (VALUES ('Quận 1'), ('Quận 3'), ('Quận 7')) AS a(area)
WHERE u.code = 'USR-004'
  AND NOT EXISTS (
      SELECT 1 FROM technician_service_areas ta WHERE ta.profile_id = tp.id AND ta.area = a.area
  );

INSERT INTO technician_service_areas (profile_id, area)
SELECT tp.id, a.area
FROM technician_profiles tp
JOIN users u ON tp.user_id = u.id
CROSS JOIN LATERAL (VALUES ('Quận Bình Thạnh'), ('Quận Phú Nhuận'), ('Quận Gò Vấp')) AS a(area)
WHERE u.code = 'USR-005'
  AND NOT EXISTS (
      SELECT 1 FROM technician_service_areas ta WHERE ta.profile_id = tp.id AND ta.area = a.area
  );


-- ---------------------------------------------------------------------
-- 6. SAMPLE COMPLETED ORDER + REVIEW — phục vụ test detail/review
--    Customer: lan (USR-002), Technician: tuan (USR-004)
-- ---------------------------------------------------------------------

INSERT INTO orders (code, service_name, sub_service, service_category, device_name, description,
                    address, scheduled_at, started_at, completed_at,
                    estimated_price, final_price, payment_method, warranty_months,
                    status, customer_id, technician_id, deleted, created_at, updated_at)
SELECT 'GU-99210',
       'Sửa máy lạnh', 'Vệ sinh + nạp gas R32', 'Máy lạnh',
       'Máy lạnh Daikin Inverter 1.5HP',
       'Máy lạnh bị rỉ nước, không mát đều, cần vệ sinh và nạp gas.',
       '25 Bis Nguyễn Thị Minh Khai, Quận 1',
       NOW() - INTERVAL '3 days',
       NOW() - INTERVAL '3 days' + INTERVAL '5 minutes',
       NOW() - INTERVAL '3 days' + INTERVAL '2 hours',
       450000, 450000, 'MOMO', 3,
       'COMPLETED',
       (SELECT id FROM users WHERE code = 'USR-002'),
       (SELECT id FROM users WHERE code = 'USR-004'),
       FALSE, NOW() - INTERVAL '4 days', NOW() - INTERVAL '3 days'
WHERE NOT EXISTS (SELECT 1 FROM orders WHERE code = 'GU-99210');

-- Image trên đơn (record loại "request")
INSERT INTO order_images (order_id, url, role, created_at)
SELECT o.id, 'https://images.unsplash.com/photo-1581093458791-9d42f6e8a3d3', 'request', NOW()
FROM orders o
WHERE o.code = 'GU-99210'
  AND NOT EXISTS (SELECT 1 FROM order_images oi WHERE oi.order_id = o.id);

-- Review sau khi hoàn thành
INSERT INTO reviews (code, order_id, author_id, technician_id, rating, content, created_at)
SELECT 'REV-001',
       o.id,
       (SELECT id FROM users WHERE code = 'USR-002'),
       (SELECT id FROM users WHERE code = 'USR-004'),
       5,
       'Anh Tuấn làm việc rất kỹ, giải thích rõ ràng nguyên nhân máy lạnh bị rỉ nước. Sẽ ủng hộ lần sau!',
       NOW() - INTERVAL '2 days'
FROM orders o
WHERE o.code = 'GU-99210'
  AND NOT EXISTS (SELECT 1 FROM reviews WHERE code = 'REV-001');


-- ---------------------------------------------------------------------
-- 7. SAMPLE NEW ORDER — đang chờ thợ nhận (test flow accept)
--    Customer: hoang (USR-003)
-- ---------------------------------------------------------------------

INSERT INTO orders (code, service_name, service_category, device_name, description,
                    address, expected_time, estimated_price,
                    status, customer_id, deleted, created_at, updated_at)
SELECT 'GU-99300',
       NULL, 'Máy giặt',
       'Máy giặt cửa ngang LG 9kg',
       'Máy giặt nhà tôi dạo này chạy không vắt được, có tiếng kêu lạ.',
       '45 Võ Văn Tần, Quận 3',
       NOW() + INTERVAL '1 day',
       450000,
       'NEW',
       (SELECT id FROM users WHERE code = 'USR-003'),
       FALSE, NOW() - INTERVAL '2 hours', NOW() - INTERVAL '2 hours'
WHERE NOT EXISTS (SELECT 1 FROM orders WHERE code = 'GU-99300');


-- ---------------------------------------------------------------------
-- 8. ADMIN SYSTEM SETTINGS — default commission & VAT
-- ---------------------------------------------------------------------
INSERT INTO system_settings (setting_key, setting_value, created_at, updated_at)
VALUES
  ('fixed_commission_fee', '10000', NOW(), NOW()),
  ('minimum_commission_balance', '20000', NOW(), NOW()),
  ('auto_lock_enabled', 'true', NOW(), NOW())
ON CONFLICT (setting_key) DO UPDATE SET
  setting_value = EXCLUDED.setting_value,
  updated_at = NOW();


-- =====================================================================
-- DONE. Verify với:
--   SELECT code, role, status FROM users ORDER BY code;
--   SELECT code, title FROM categories ORDER BY code;
--   SELECT code, status FROM orders ORDER BY code;
-- =====================================================================

-- ---------------------------------------------------------------------
-- ADDITIONAL SEED: bulk users, categories and 30 orders for dashboard demo
-- Idempotent inserts (ON CONFLICT DO NOTHING)
-- ---------------------------------------------------------------------

-- 1) Bulk technicians (100)
INSERT INTO users (code, full_name, email, phone, password, role, status, district, address, bio, avatar, deleted, created_at, updated_at)
SELECT 'USR-T-' || LPAD(s::text,3,'0') AS code,
       'Kỹ thuật viên ' || s AS full_name,
       'tech' || s || '@example.com' AS email,
       ('0900' || LPAD(s::text,4,'0')) AS phone,
       '$2a$10$v7D/LWQ3B1QEhFUKA9tCP.9U.xWWiDv92EL7pSQnkPXUIB51i0Mwm' AS password,
       'TECHNICIAN' AS role,
       'ACTIVE' AS status,
       'Quận 1' AS district,
       ('Địa chỉ kỹ thuật viên ' || s) AS address,
       NULL AS bio,
       ('https://i.pravatar.cc/150?img=' || ((s % 70) + 1)) AS avatar,
       FALSE, NOW(), NOW()
FROM generate_series(1,100) s
ON CONFLICT (code) DO NOTHING;

-- 2) Bulk customers (300)
INSERT INTO users (code, full_name, email, phone, password, role, status, district, address, bio, avatar, deleted, created_at, updated_at)
SELECT 'USR-C-' || LPAD(s::text,3,'0') AS code,
       'Khách hàng ' || s AS full_name,
       'cust' || s || '@example.com' AS email,
       ('0910' || LPAD(s::text,4,'0')) AS phone,
       '$2a$10$v7D/LWQ3B1QEhFUKA9tCP.9U.xWWiDv92EL7pSQnkPXUIB51i0Mwm' AS password,
       'CUSTOMER' AS role,
       'ACTIVE' AS status,
       'Quận 7' AS district,
       ('Địa chỉ khách hàng ' || s) AS address,
       NULL AS bio,
       ('https://i.pravatar.cc/150?img=' || ((s % 70) + 10)) AS avatar,
       FALSE, NOW(), NOW()
FROM generate_series(1,300) s
ON CONFLICT (code) DO NOTHING;

-- 3.5) Commission wallet demo data (status is computed dynamically)
INSERT INTO wallets (
    user_id,
    total_earned,
    total_withdrawn,
    currency,
    created_at,
    updated_at
)
SELECT
    u.id,
    70000,
    0,
    70000,  -- credit_balance
    0,      -- personal_balance
    0,      -- pending_withdraw_balance
    'VND',
    NOW(),
    NOW()
FROM users u
WHERE u.code = 'USR-004'
    ON CONFLICT (user_id) DO NOTHING;

INSERT INTO wallets (
    user_id,
    total_earned,
    total_withdrawn,
    currency,
    created_at,
    updated_at
)
SELECT
    u.id,
    35000,
    20000,
    15000,  -- credit_balance
    0,      -- personal_balance
    0,      -- pending_withdraw_balance
    'VND',
    NOW(),
    NOW()
FROM users u
WHERE u.code = 'USR-005'
    ON CONFLICT (user_id) DO NOTHING;

INSERT INTO wallets (
    user_id,
    total_earned,
    total_withdrawn,
    currency,
    created_at,
    updated_at
)
SELECT
    u.id,
    15000,
    15000,
    0,      -- credit_balance
    0,      -- personal_balance
    0,      -- pending_withdraw_balance
    'VND',
    NOW(),
    NOW()
FROM users u
WHERE u.code = 'USR-T-001'
    ON CONFLICT (user_id) DO NOTHING;

INSERT INTO wallet_transactions (
  transaction_code, wallet_id, type, wallet_type, category, title, amount, fee, net_amount,
  after_balance, note, actor, related_order_code, status, created_at, processed_at
)
SELECT 'TX-COMM-001', w.id, 'COMMISSION', 'CREDIT', 'COMMISSION_TOPUP', 'Nạp hoa hồng cho thợ',
       70000, 0, 70000, 70000,
       'Nạp quỹ hoa hồng khởi tạo', 'ADMIN', NULL,
       'SUCCESS', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days'
FROM wallets w
JOIN users u ON u.id = w.user_id
WHERE u.code = 'USR-004'
  AND NOT EXISTS (SELECT 1 FROM wallet_transactions wt WHERE wt.transaction_code = 'TX-COMM-001');

INSERT INTO wallet_transactions (
  transaction_code, wallet_id, type, wallet_type, category, title, amount, fee, net_amount,
  after_balance, note, actor, related_order_code, status, created_at, processed_at
)
SELECT 'TX-COMM-002', w.id, 'COMMISSION', 'CREDIT', 'COMMISSION_TOPUP', 'Nạp thêm hoa hồng cho thợ Minh',
       35000, 0, 35000, 35000,
       'Nạp quỹ để demo trạng thái LOW_BALANCE sau khi khấu trừ', 'ADMIN', 'GU-SEED-002',
       'SUCCESS', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'
FROM wallets w
JOIN users u ON u.id = w.user_id
WHERE u.code = 'USR-005'
  AND NOT EXISTS (SELECT 1 FROM wallet_transactions wt WHERE wt.transaction_code = 'TX-COMM-002');

INSERT INTO wallet_transactions (
  transaction_code, wallet_id, type, wallet_type, category, title, amount, fee, net_amount,
  after_balance, note, actor, related_order_code, status, created_at, processed_at
)
SELECT 'TX-COMM-003', w.id, 'COMMISSION', 'CREDIT', 'COMMISSION_DEDUCTION', 'Khấu trừ hoa hồng đơn GU-99210',
       20000, 0, -20000, 15000,
       'Khấu trừ phí cố định theo đơn hoàn thành', 'SYSTEM', 'GU-99210',
       'SUCCESS', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'
FROM wallets w
JOIN users u ON u.id = w.user_id
WHERE u.code = 'USR-005'
  AND NOT EXISTS (SELECT 1 FROM wallet_transactions wt WHERE wt.transaction_code = 'TX-COMM-003');

INSERT INTO wallet_transactions (
  transaction_code, wallet_id, type, wallet_type, category, title, amount, fee, net_amount,
  after_balance, note, actor, related_order_code, status, created_at, processed_at
)
SELECT 'TX-COMM-004', w.id, 'COMMISSION', 'CREDIT', 'COMMISSION_TOPUP', 'Nạp hoa hồng khởi tạo cho thợ LOCKED demo',
       15000, 0, 15000, 15000,
       'Tạo lịch sử để kiểm thử lastOrderAt và status LOCKED', 'ADMIN', 'GU-SEED-LOCK-001',
       'SUCCESS', NOW() - INTERVAL '12 hours', NOW() - INTERVAL '12 hours'
FROM wallets w
JOIN users u ON u.id = w.user_id
WHERE u.code = 'USR-T-001'
  AND NOT EXISTS (SELECT 1 FROM wallet_transactions wt WHERE wt.transaction_code = 'TX-COMM-004');

INSERT INTO wallet_transactions (
  transaction_code, wallet_id, type, wallet_type, category, title, amount, fee, net_amount,
  after_balance, note, actor, related_order_code, status, created_at, processed_at
)
SELECT 'TX-COMM-005', w.id, 'COMMISSION', 'CREDIT', 'COMMISSION_DEDUCTION', 'Khấu trừ hoa hồng về 0',
       15000, 0, -15000, 0,
       'Đưa ví về trạng thái LOCKED theo balance = 0', 'SYSTEM', 'GU-SEED-LOCK-001',
       'SUCCESS', NOW() - INTERVAL '11 hours', NOW() - INTERVAL '11 hours'
FROM wallets w
JOIN users u ON u.id = w.user_id
WHERE u.code = 'USR-T-001'
  AND NOT EXISTS (SELECT 1 FROM wallet_transactions wt WHERE wt.transaction_code = 'TX-COMM-005');

-- 3) Ensure required categories exist (Sửa điện, Sửa nước, Vệ sinh máy lạnh)
INSERT INTO categories (code, title, description, icon_url, priority, status, deleted, created_at, updated_at)
VALUES
  ('CAT-SD', 'Sửa điện', 'Sửa chữa, đấu nối điện dân dụng, thay aptomat, sửa ổ cắm, bóng đèn', NULL, 'HIGH', 'ACTIVE', FALSE, NOW(), NOW()),
  ('CAT-SW', 'Sửa nước', 'Sửa ống nước, thay vòi, xử lý rò rỉ, thông tắc', NULL, 'HIGH', 'ACTIVE', FALSE, NOW(), NOW()),
  ('CAT-VML', 'Vệ sinh máy lạnh', 'Vệ sinh, bảo trì, nạp gas và kiểm tra hiệu suất máy lạnh', NULL, 'HIGH', 'ACTIVE', FALSE, NOW(), NOW())
ON CONFLICT (code) DO NOTHING;

-- 4) 30 Orders with varied completed_at across 2024-2026
-- A) Explicit important timestamps to cover the required cases
INSERT INTO orders (code, service_name, service_category, device_name, description, address, scheduled_at, started_at, completed_at, estimated_price, final_price, payment_method, warranty_months, status, customer_id, technician_id, deleted, created_at, updated_at)
SELECT 'GU-SEED-001', 'Sửa điện tại nhà', 'Sửa điện', 'Hệ thống điện gia đình', 'Sửa aptomat và ổ cắm', 'Hẻm 123, Quận 1',
       TIMESTAMP '2026-01-09 09:00:00', TIMESTAMP '2026-01-09 09:10:00', TIMESTAMP '2026-01-10 11:00:00',
       300000, 450000, 'MOMO', 0, 'COMPLETED',
       (SELECT id FROM users WHERE role = 'CUSTOMER' ORDER BY random() LIMIT 1),
       (SELECT id FROM users WHERE role = 'TECHNICIAN' ORDER BY random() LIMIT 1), FALSE, TIMESTAMP '2026-01-08', TIMESTAMP '2026-01-10'
WHERE NOT EXISTS (SELECT 1 FROM orders WHERE code = 'GU-SEED-001');

INSERT INTO orders (code, service_name, service_category, device_name, description, address, scheduled_at, started_at, completed_at, estimated_price, final_price, payment_method, warranty_months, status, customer_id, technician_id, deleted, created_at, updated_at)
SELECT 'GU-SEED-002', 'Sửa nước tại nhà', 'Sửa nước', 'Đường ống gia đình', 'Sửa rò rỉ ống nước', 'Số 5, Quận 3',
       TIMESTAMP '2026-02-19 10:00:00', TIMESTAMP '2026-02-19 10:10:00', TIMESTAMP '2026-02-20 12:30:00',
        250000, 350000, 'BANK_TRANSFER', 0, 'COMPLETED',
       (SELECT id FROM users WHERE role = 'CUSTOMER' ORDER BY random() LIMIT 1),
       (SELECT id FROM users WHERE role = 'TECHNICIAN' ORDER BY random() LIMIT 1), FALSE, TIMESTAMP '2026-02-18', TIMESTAMP '2026-02-20'
WHERE NOT EXISTS (SELECT 1 FROM orders WHERE code = 'GU-SEED-002');

-- March 2026 specific days
INSERT INTO orders (code, service_name, service_category, device_name, description, address, scheduled_at, started_at, completed_at, estimated_price, final_price, payment_method, warranty_months, status, customer_id, technician_id, deleted, created_at, updated_at)
SELECT 'GU-SEED-003', 'Vệ sinh máy lạnh', 'Vệ sinh máy lạnh', 'Điều hòa Panasonic', 'Vệ sinh + kiểm tra gas', 'Số 21, Quận 7',
       TIMESTAMP '2026-03-05 08:30:00', TIMESTAMP '2026-03-05 08:45:00', TIMESTAMP '2026-03-05 11:00:00',
       400000, 600000, 'MOMO', 0, 'COMPLETED',
       (SELECT id FROM users WHERE role = 'CUSTOMER' ORDER BY random() LIMIT 1),
       (SELECT id FROM users WHERE role = 'TECHNICIAN' ORDER BY random() LIMIT 1), FALSE, TIMESTAMP '2026-03-04', TIMESTAMP '2026-03-05'
WHERE NOT EXISTS (SELECT 1 FROM orders WHERE code = 'GU-SEED-003');

INSERT INTO orders (code, service_name, service_category, device_name, description, address, scheduled_at, started_at, completed_at, estimated_price, final_price, payment_method, warranty_months, status, customer_id, technician_id, deleted, created_at, updated_at)
SELECT 'GU-SEED-004', 'Vệ sinh máy lạnh - lần 2', 'Vệ sinh máy lạnh', 'Điều hòa LG', 'Vệ sinh + thay keo', 'Số 99, Quận Phú Nhuận',
       TIMESTAMP '2026-03-14 14:00:00', TIMESTAMP '2026-03-14 14:15:00', TIMESTAMP '2026-03-15 16:30:00',
        350000, 550000, 'BANK_TRANSFER', 0, 'COMPLETED',
       (SELECT id FROM users WHERE role = 'CUSTOMER' ORDER BY random() LIMIT 1),
       (SELECT id FROM users WHERE role = 'TECHNICIAN' ORDER BY random() LIMIT 1), FALSE, TIMESTAMP '2026-03-13', TIMESTAMP '2026-03-15'
WHERE NOT EXISTS (SELECT 1 FROM orders WHERE code = 'GU-SEED-004');

-- Q2/2025 examples
INSERT INTO orders (code, service_name, service_category, device_name, description, address, scheduled_at, started_at, completed_at, estimated_price, final_price, payment_method, warranty_months, status, customer_id, technician_id, deleted, created_at, updated_at)
SELECT 'GU-SEED-005', 'Sửa điện lớn', 'Sửa điện', 'Bảng điện gia đình', 'Thay aptomat, đấu lại bóng đèn', 'Khu A, Quận 2',
       TIMESTAMP '2025-04-14 09:00:00', TIMESTAMP '2025-04-14 09:30:00', TIMESTAMP '2025-04-15 12:00:00',
       500000, 1200000, 'MOMO', 0, 'COMPLETED',
       (SELECT id FROM users WHERE role = 'CUSTOMER' ORDER BY random() LIMIT 1),
       (SELECT id FROM users WHERE role = 'TECHNICIAN' ORDER BY random() LIMIT 1), FALSE, TIMESTAMP '2025-04-10', TIMESTAMP '2025-04-15'
WHERE NOT EXISTS (SELECT 1 FROM orders WHERE code = 'GU-SEED-005');

INSERT INTO orders (code, service_name, service_category, device_name, description, address, scheduled_at, started_at, completed_at, estimated_price, final_price, payment_method, warranty_months, status, customer_id, technician_id, deleted, created_at, updated_at)
SELECT 'GU-SEED-006', 'Sửa rò rỉ nước', 'Sửa nước', 'Hệ thống ống', 'Xử lý rò rỉ, thay gioăng', 'Khu B, Quận 5',
       TIMESTAMP '2025-05-04 10:00:00', TIMESTAMP '2025-05-04 10:20:00', TIMESTAMP '2025-05-05 13:00:00',
        220000, 280000, 'BANK_TRANSFER', 0, 'COMPLETED',
       (SELECT id FROM users WHERE role = 'CUSTOMER' ORDER BY random() LIMIT 1),
       (SELECT id FROM users WHERE role = 'TECHNICIAN' ORDER BY random() LIMIT 1), FALSE, TIMESTAMP '2025-05-01', TIMESTAMP '2025-05-05'
WHERE NOT EXISTS (SELECT 1 FROM orders WHERE code = 'GU-SEED-006');

-- Additional historical orders (2025, 2024)
INSERT INTO orders (code, service_name, service_category, device_name, description, address, scheduled_at, started_at, completed_at, estimated_price, final_price, payment_method, warranty_months, status, customer_id, technician_id, deleted, created_at, updated_at)
SELECT 'GU-SEED-007', 'Sửa nhỏ', 'Sửa điện', 'Thiết bị điện nhỏ', 'Sửa ổ cắm', 'Quận Tân Bình',
       TIMESTAMP '2025-03-09 08:00:00', TIMESTAMP '2025-03-09 08:15:00', TIMESTAMP '2025-03-10 09:30:00',
        200000, 300000, 'BANK_TRANSFER', 0, 'COMPLETED',
       (SELECT id FROM users WHERE role = 'CUSTOMER' ORDER BY random() LIMIT 1),
       (SELECT id FROM users WHERE role = 'TECHNICIAN' ORDER BY random() LIMIT 1), FALSE, TIMESTAMP '2025-03-01', TIMESTAMP '2025-03-10'
WHERE NOT EXISTS (SELECT 1 FROM orders WHERE code = 'GU-SEED-007');

INSERT INTO orders (code, service_name, service_category, device_name, description, address, scheduled_at, started_at, completed_at, estimated_price, final_price, payment_method, warranty_months, status, customer_id, technician_id, deleted, created_at, updated_at)
SELECT 'GU-SEED-008', 'Vệ sinh máy lạnh cũ', 'Vệ sinh máy lạnh', 'Điều hòa cũ', 'Vệ sinh sâu, kiểm tra gas', 'Quận 4',
       TIMESTAMP '2024-07-20 09:00:00', TIMESTAMP '2024-07-20 09:20:00', TIMESTAMP '2024-07-22 11:00:00',
       300000, 450000, 'MOMO', 0, 'COMPLETED',
       (SELECT id FROM users WHERE role = 'CUSTOMER' ORDER BY random() LIMIT 1),
       (SELECT id FROM users WHERE role = 'TECHNICIAN' ORDER BY random() LIMIT 1), FALSE, TIMESTAMP '2024-07-19', TIMESTAMP '2024-07-22'
WHERE NOT EXISTS (SELECT 1 FROM orders WHERE code = 'GU-SEED-008');

-- B) Generate remaining orders to reach 30 total (we already inserted 8 explicit above)
WITH seq AS (
  SELECT generate_series(1,22) AS s
), gen AS (
  SELECT
    'GU-SEED-G' || LPAD(s::text,3,'0') AS code,
    (ARRAY['Sửa điện','Sửa nước','Vệ sinh máy lạnh'])[1 + (floor(random()*3))] AS service_category,
    now() - ((random() * extract(epoch FROM (now() - timestamp '2024-01-01')) ) * interval '1 second') AS completed_at,
    s
  FROM seq
)
INSERT INTO orders (code, service_name, service_category, device_name, description, address, scheduled_at, started_at, completed_at, estimated_price, final_price, payment_method, warranty_months, status, customer_id, technician_id, deleted, created_at, updated_at)
SELECT g.code,
       g.service_category || ' (dịch vụ thử nghiệm) ',
       g.service_category,
       'Thiết bị mẫu',
       'Mô tả demo cho đơn',
       'Địa chỉ demo ' || g.s,
       CASE WHEN g.s <= 10 THEN g.completed_at - interval '1 day' ELSE g.completed_at - interval '2 hours' END AS scheduled_at,
       CASE WHEN g.s <= 10 THEN g.completed_at - interval '23 hours' ELSE g.completed_at - interval '1 hour' END AS started_at,
       g.completed_at,
       (floor(random() * (2000000 - 200000) + 200000))::bigint AS estimated_price,
       (floor(random() * (2000000 - 200000) + 200000))::bigint AS final_price,
        CASE WHEN random() < 0.5 THEN 'MOMO' ELSE 'BANK_TRANSFER' END,
       0,
       'COMPLETED',
       (SELECT id FROM users WHERE role = 'CUSTOMER' ORDER BY random() LIMIT 1),
       (SELECT id FROM users WHERE role = 'TECHNICIAN' ORDER BY random() LIMIT 1),
       FALSE,
       g.completed_at - interval '1 day',
       g.completed_at
FROM gen g
WHERE NOT EXISTS (SELECT 1 FROM orders o WHERE o.code = g.code);

-- Verify: at least 30 orders from seed
-- SELECT count(*) FROM orders WHERE code LIKE 'GU-SEED-%';

-- ============================================================
-- Seed 10 order_reports (đúng CHECK constraints)
-- Sử dụng các order đã có: GU-SEED-001 ... GU-SEED-008
-- ============================================================

-- 1. BAD_ATTITUDE, RESOLVED (order 001)
INSERT INTO order_reports (code, order_id, customer_id, technician_id, reason, description, status, created_at, updated_at)
SELECT 'REP-001', o.id, o.customer_id, o.technician_id, 'BAD_ATTITUDE',
       'KTV đến trễ 30p, nói chuyện cộc lốc, không giải thích chi phí phát sinh.',
       'RESOLVED', NOW() - INTERVAL '5 days', NOW() - INTERVAL '2 days'
FROM orders o WHERE o.code = 'GU-SEED-001'
                AND NOT EXISTS (SELECT 1 FROM order_reports WHERE code = 'REP-001');

-- 2. OTHER, INVESTIGATING (order 002)
INSERT INTO order_reports (code, order_id, customer_id, technician_id, reason, description, status, created_at, updated_at)
SELECT 'REP-002', o.id, o.customer_id, o.technician_id, 'OTHER',
       'KTV tự ý sửa thêm hạng mục không được yêu cầu, làm tăng thời gian và chi phí.',
       'INVESTIGATING', NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'
FROM orders o WHERE o.code = 'GU-SEED-002'
                AND NOT EXISTS (SELECT 1 FROM order_reports WHERE code = 'REP-002');

-- 3. NO_SHOW, OPEN (order 003)
INSERT INTO order_reports (code, order_id, customer_id, technician_id, reason, description, status, created_at, updated_at)
SELECT 'REP-003', o.id, o.customer_id, o.technician_id, 'NO_SHOW',
       'KTV không đến đúng giờ hẹn, không báo trước, tôi chờ 1 tiếng.',
       'OPEN', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days'
FROM orders o WHERE o.code = 'GU-SEED-003'
                AND NOT EXISTS (SELECT 1 FROM order_reports WHERE code = 'REP-003');

-- 4. FRAUD, DISMISSED (order 004)
INSERT INTO order_reports (code, order_id, customer_id, technician_id, reason, description, status, created_at, updated_at)
SELECT 'REP-004', o.id, o.customer_id, o.technician_id, 'FRAUD',
       'KTV báo chi phí thay thế linh kiện cao gấp 3 lần giá thị trường, có dấu hiệu gian lận.',
       'DISMISSED', NOW() - INTERVAL '10 days', NOW() - INTERVAL '8 days'
FROM orders o WHERE o.code = 'GU-SEED-004'
                AND NOT EXISTS (SELECT 1 FROM order_reports WHERE code = 'REP-004');

-- 5. EXTRA_FEE, RESOLVED (order 005)
INSERT INTO order_reports (code, order_id, customer_id, technician_id, reason, description, status, created_at, updated_at)
SELECT 'REP-005', o.id, o.customer_id, o.technician_id, 'EXTRA_FEE',
       'Báo giá 500k, sau đó KTV đòi thêm 700k vì "dây cũ", tôi không đồng ý.',
       'RESOLVED', NOW() - INTERVAL '7 days', NOW() - INTERVAL '3 days'
FROM orders o WHERE o.code = 'GU-SEED-005'
                AND NOT EXISTS (SELECT 1 FROM order_reports WHERE code = 'REP-005');

-- 6. POOR_QUALITY, INVESTIGATING (order 006)
INSERT INTO order_reports (code, order_id, customer_id, technician_id, reason, description, status, created_at, updated_at)
SELECT 'REP-006', o.id, o.customer_id, o.technician_id, 'POOR_QUALITY',
       'Sửa xong 2 ngày lại hỏng, KTV không bảo hành, đòi thu thêm tiền.',
       'INVESTIGATING', NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days'
FROM orders o WHERE o.code = 'GU-SEED-006'
                AND NOT EXISTS (SELECT 1 FROM order_reports WHERE code = 'REP-006');

-- 7. EXTRA_FEE, OPEN (order 007)
INSERT INTO order_reports (code, order_id, customer_id, technician_id, reason, description, status, created_at, updated_at)
SELECT 'REP-007', o.id, o.customer_id, o.technician_id, 'EXTRA_FEE',
       'KTV báo thêm phí vệ sinh nhưng đã bao gồm trong gói dịch vụ.',
       'OPEN', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'
FROM orders o WHERE o.code = 'GU-SEED-007'
                AND NOT EXISTS (SELECT 1 FROM order_reports WHERE code = 'REP-007');

-- 8. NO_SHOW, DISMISSED (order 008)
INSERT INTO order_reports (code, order_id, customer_id, technician_id, reason, description, status, created_at, updated_at)
SELECT 'REP-008', o.id, o.customer_id, o.technician_id, 'NO_SHOW',
       'KTV hẹn 9h sáng nhưng 11h mới đến, không báo trước.',
       'DISMISSED', NOW() - INTERVAL '12 days', NOW() - INTERVAL '10 days'
FROM orders o WHERE o.code = 'GU-SEED-008'
                AND NOT EXISTS (SELECT 1 FROM order_reports WHERE code = 'REP-008');

-- 9. FRAUD, INVESTIGATING (order 001)
INSERT INTO order_reports (code, order_id, customer_id, technician_id, reason, description, status, created_at, updated_at)
SELECT 'REP-009', o.id, o.customer_id, o.technician_id, 'FRAUD',
       'KTV thay aptomat mới nhưng thực tế chỉ sửa lại cái cũ, có dấu hiệu lừa đảo.',
       'INVESTIGATING', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'
FROM orders o WHERE o.code = 'GU-SEED-001'
                AND NOT EXISTS (SELECT 1 FROM order_reports WHERE code = 'REP-009');

-- 10. POOR_QUALITY, RESOLVED (order 003)
INSERT INTO order_reports (code, order_id, customer_id, technician_id, reason, description, status, created_at, updated_at)
SELECT 'REP-010', o.id, o.customer_id, o.technician_id, 'POOR_QUALITY',
       'Vệ sinh máy lạnh xong vẫn bốc mùi, KTV không xử lý triệt để.',
       'RESOLVED', NOW() - INTERVAL '5 days', NOW() - INTERVAL '2 days'
FROM orders o WHERE o.code = 'GU-SEED-003'
                AND NOT EXISTS (SELECT 1 FROM order_reports WHERE code = 'REP-010');