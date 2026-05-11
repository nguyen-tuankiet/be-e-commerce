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
-- BCrypt hash dưới đây = $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
-- (đây là hash chính thức trong Spring Security docs encode "password")
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
 '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
 'ADMIN', 'ACTIVE', 'Quận 1', '123 Lê Lợi, Quận 1', NULL,
 'https://i.pravatar.cc/150?img=68', FALSE, NOW(), NOW()),

('USR-002', 'Trần Thị Lan', 'lan@email.com', '0901234567',
 '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
 'CUSTOMER', 'ACTIVE', 'Quận 7', '123 Nguyễn Văn Linh, Quận 7', NULL,
 'https://i.pravatar.cc/150?img=5', FALSE, NOW(), NOW()),

('USR-003', 'Phạm Hoàng', 'hoang@email.com', '0901234568',
 '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
 'CUSTOMER', 'ACTIVE', 'Quận 3', '45 Võ Văn Tần, Quận 3', NULL,
 'https://i.pravatar.cc/150?img=12', FALSE, NOW(), NOW()),

('USR-004', 'Trần Anh Tuấn', 'tuan@glowup.pro', '0987654321',
 '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
 'TECHNICIAN', 'ACTIVE', 'Quận 1', '25 Bis Nguyễn Thị Minh Khai, Quận 1',
 'Hơn 10 năm kinh nghiệm sửa điện lạnh, bảo trì máy lạnh, hệ thống thông gió.',
 'https://i.pravatar.cc/150?img=33', FALSE, NOW(), NOW()),

('USR-005', 'Nguyễn Văn Minh', 'minh@glowup.pro', '0987654322',
 '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
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
       450000, 450000, 'WALLET', 3,
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
-- system_settings được Hibernate auto-create từ entity SystemSetting.
-- Skip — service layer tự tạo default khi gọi GET /admin/settings lần đầu.


-- =====================================================================
-- DONE. Verify với:
--   SELECT code, role, status FROM users ORDER BY code;
--   SELECT code, title FROM categories ORDER BY code;
--   SELECT code, status FROM orders ORDER BY code;
-- =====================================================================
