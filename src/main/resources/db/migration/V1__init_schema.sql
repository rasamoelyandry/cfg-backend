-- =============================================================
-- CFG - Schema initial
-- =============================================================

-- Extensions
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- =============================================================
-- RESTAURANTS (tenant racine)
-- =============================================================
CREATE TABLE restaurants (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name          VARCHAR(255) NOT NULL,
    slug          VARCHAR(100) NOT NULL UNIQUE,
    address       TEXT,
    phone         VARCHAR(30),
    email         VARCHAR(255),
    currency      VARCHAR(10) NOT NULL DEFAULT 'MGA',
    timezone      VARCHAR(50) NOT NULL DEFAULT 'Indian/Antananarivo',
    is_active     BOOLEAN NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- =============================================================
-- UTILISATEURS
-- =============================================================
CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    restaurant_id   UUID REFERENCES restaurants(id) ON DELETE SET NULL,  -- NULL = SUPER_ADMIN
    email           VARCHAR(255) UNIQUE,
    phone           VARCHAR(30) UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    role            VARCHAR(30) NOT NULL,  -- SUPER_ADMIN, OWNER, MANAGER, WAITER, KITCHEN
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    last_login_at   TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT users_email_or_phone CHECK (email IS NOT NULL OR phone IS NOT NULL),
    CONSTRAINT users_role_check CHECK (role IN ('SUPER_ADMIN','OWNER','MANAGER','WAITER','KITCHEN'))
);

-- =============================================================
-- TABLES RESTAURANT
-- =============================================================
CREATE TABLE restaurant_tables (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    restaurant_id   UUID NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
    number          INT NOT NULL,
    label           VARCHAR(50),  -- ex: "Terrasse 3", optionnel
    capacity        INT NOT NULL DEFAULT 4,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT restaurant_tables_unique_number UNIQUE (restaurant_id, number)
);

-- =============================================================
-- MENU
-- =============================================================
CREATE TABLE menu_categories (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    restaurant_id   UUID NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
    name            VARCHAR(100) NOT NULL,
    description     TEXT,
    sort_order      INT NOT NULL DEFAULT 0,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE menu_items (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    restaurant_id   UUID NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
    category_id     UUID NOT NULL REFERENCES menu_categories(id) ON DELETE CASCADE,
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    price           NUMERIC(10,2) NOT NULL CHECK (price >= 0),
    image_url       TEXT,
    sort_order      INT NOT NULL DEFAULT 0,
    is_available    BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE menu_item_modifiers (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    menu_item_id    UUID NOT NULL REFERENCES menu_items(id) ON DELETE CASCADE,
    name            VARCHAR(100) NOT NULL,
    price_delta     NUMERIC(10,2) NOT NULL DEFAULT 0,
    is_default      BOOLEAN NOT NULL DEFAULT FALSE
);

-- =============================================================
-- COMMANDES
-- =============================================================
CREATE TABLE orders (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    restaurant_id   UUID NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
    table_id        UUID REFERENCES restaurant_tables(id) ON DELETE SET NULL,
    waiter_id       UUID REFERENCES users(id) ON DELETE SET NULL,
    customer_name   VARCHAR(255),
    status          VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    notes           TEXT,
    total_amount    NUMERIC(10,2) NOT NULL DEFAULT 0,
    client_uuid     UUID UNIQUE,   -- pour idempotence offline sync
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    sent_to_kitchen_at TIMESTAMPTZ,
    completed_at    TIMESTAMPTZ,
    CONSTRAINT orders_status_check CHECK (
        status IN ('DRAFT','PENDING','PREPARING','READY','SERVED','PAID','CANCELLED')
    )
);

CREATE TABLE order_items (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id        UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    menu_item_id    UUID NOT NULL REFERENCES menu_items(id),
    menu_item_name  VARCHAR(255) NOT NULL,  -- snapshot nom au moment commande
    unit_price      NUMERIC(10,2) NOT NULL,  -- snapshot prix au moment commande
    quantity        INT NOT NULL DEFAULT 1 CHECK (quantity > 0),
    notes           TEXT,
    status          VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    CONSTRAINT order_items_status_check CHECK (
        status IN ('PENDING','PREPARING','READY','CANCELLED')
    )
);

CREATE TABLE order_item_modifiers (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_item_id   UUID NOT NULL REFERENCES order_items(id) ON DELETE CASCADE,
    modifier_name   VARCHAR(100) NOT NULL,
    price_delta     NUMERIC(10,2) NOT NULL DEFAULT 0
);

-- =============================================================
-- PAIEMENTS
-- =============================================================
CREATE TABLE payments (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    restaurant_id   UUID NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
    order_id        UUID NOT NULL REFERENCES orders(id),
    method          VARCHAR(30) NOT NULL,
    amount          NUMERIC(10,2) NOT NULL CHECK (amount > 0),
    reference       VARCHAR(255),  -- référence transaction mobile money
    notes           TEXT,
    paid_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by      UUID REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT payments_method_check CHECK (
        method IN ('CASH','ORANGE_MONEY','MVOLA','AIRTEL_MONEY')
    )
);

-- =============================================================
-- SYNC OFFLINE
-- =============================================================
CREATE TABLE sync_events (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_uuid     UUID NOT NULL UNIQUE,
    restaurant_id   UUID REFERENCES restaurants(id),
    user_id         UUID REFERENCES users(id),
    event_type      VARCHAR(50) NOT NULL,
    payload         JSONB NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    error_message   TEXT,
    local_timestamp TIMESTAMPTZ NOT NULL,
    processed_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT sync_events_status_check CHECK (
        status IN ('PENDING','PROCESSED','CONFLICT','ERROR')
    )
);

-- =============================================================
-- AUDIT LOGS
-- =============================================================
CREATE TABLE audit_logs (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    restaurant_id   UUID REFERENCES restaurants(id),
    user_id         UUID REFERENCES users(id),
    action          VARCHAR(100) NOT NULL,
    entity_type     VARCHAR(50),
    entity_id       UUID,
    details         JSONB,
    ip_address      VARCHAR(45),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- =============================================================
-- INDEX
-- =============================================================
CREATE INDEX idx_users_restaurant       ON users(restaurant_id);
CREATE INDEX idx_users_email            ON users(email) WHERE email IS NOT NULL;
CREATE INDEX idx_users_phone            ON users(phone) WHERE phone IS NOT NULL;

CREATE INDEX idx_tables_restaurant      ON restaurant_tables(restaurant_id);

CREATE INDEX idx_categories_restaurant  ON menu_categories(restaurant_id, sort_order);
CREATE INDEX idx_items_category         ON menu_items(category_id, sort_order);
CREATE INDEX idx_items_restaurant_avail ON menu_items(restaurant_id, is_available);

CREATE INDEX idx_orders_restaurant_status ON orders(restaurant_id, status);
CREATE INDEX idx_orders_table           ON orders(table_id) WHERE status NOT IN ('PAID','CANCELLED');
CREATE INDEX idx_orders_waiter          ON orders(waiter_id);
CREATE INDEX idx_orders_client_uuid     ON orders(client_uuid) WHERE client_uuid IS NOT NULL;

CREATE INDEX idx_order_items_order      ON order_items(order_id);

CREATE INDEX idx_payments_restaurant    ON payments(restaurant_id, paid_at);
CREATE INDEX idx_payments_order         ON payments(order_id);

CREATE INDEX idx_sync_events_status     ON sync_events(status, created_at);
CREATE INDEX idx_sync_events_user       ON sync_events(user_id, created_at);

CREATE INDEX idx_audit_restaurant       ON audit_logs(restaurant_id, created_at);

-- =============================================================
-- TRIGGER updated_at automatique
-- =============================================================
CREATE OR REPLACE FUNCTION update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_restaurants_updated_at
    BEFORE UPDATE ON restaurants
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();

CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();

CREATE TRIGGER trg_tables_updated_at
    BEFORE UPDATE ON restaurant_tables
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();

CREATE TRIGGER trg_categories_updated_at
    BEFORE UPDATE ON menu_categories
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();

CREATE TRIGGER trg_items_updated_at
    BEFORE UPDATE ON menu_items
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();

CREATE TRIGGER trg_orders_updated_at
    BEFORE UPDATE ON orders
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();
