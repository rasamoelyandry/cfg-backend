-- Seed super admin par défaut (mot de passe: Admin@1234 bcrypt)
-- IMPORTANT: changer le mot de passe après premier déploiement
INSERT INTO users (id, restaurant_id, email, phone, password_hash, first_name, last_name, role, is_active)
VALUES (
    gen_random_uuid(),
    NULL,
    'admin@cfg.app',
    NULL,
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQyCuxDCUE9.YjXV.G5qlCBIG',  -- Admin@1234
    'Super',
    'Admin',
    'SUPER_ADMIN',
    TRUE
) ON CONFLICT (email) DO NOTHING;
