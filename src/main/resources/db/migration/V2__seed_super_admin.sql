-- Seed super admin par défaut (mot de passe: Admin@1234 bcrypt)
-- IMPORTANT: changer le mot de passe après premier déploiement
INSERT INTO users (id, restaurant_id, email, phone, password_hash, first_name, last_name, role, is_active)
VALUES (
    gen_random_uuid(),
    NULL,
    'admin@cfg.app',
    NULL,
    '$2a$10$Zru6Zr5/UqNh3y2VMYIXz.N5UqayKRoM42QEwwHJSF8S36gxAMst6',  -- Admin@1234
    'Super',
    'Admin',
    'SUPER_ADMIN',
    TRUE
) ON CONFLICT (email) DO NOTHING;
