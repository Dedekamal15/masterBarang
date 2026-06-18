-- ============================================================
-- MasterBarang — PostgreSQL Database Initialization Script
-- Jalankan sekali saat setup pertama kali
-- ============================================================

-- Buat database (jalankan sebagai superuser postgres)
-- CREATE DATABASE masterbarang_db;
-- \c masterbarang_db

-- ── ENUM Types ────────────────────────────────────────────────────────────────

CREATE TYPE assetstatus AS ENUM (
    'AVAILABLE',
    'BORROWED',
    'MAINTENANCE'
);

CREATE TYPE transactiontype AS ENUM (
    'CHECK_OUT',
    'CHECK_IN'
);

-- ── Tabel assets ──────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS assets (
    id              VARCHAR(36)     PRIMARY KEY,
    name            VARCHAR(255)    NOT NULL,
    category        VARCHAR(100)    NOT NULL    DEFAULT '',
    serial_number   VARCHAR(100)    NOT NULL    UNIQUE,
    description     TEXT            NOT NULL    DEFAULT '',
    location        VARCHAR(200)    NOT NULL    DEFAULT '',
    status          assetstatus     NOT NULL    DEFAULT 'AVAILABLE',
    created_at      BIGINT          NOT NULL,   -- epoch milliseconds (dari device)
    updated_at      BIGINT          NOT NULL    -- epoch milliseconds
);

-- Index untuk performa query
CREATE INDEX IF NOT EXISTS ix_assets_name          ON assets (name);
CREATE INDEX IF NOT EXISTS ix_assets_status        ON assets (status);
CREATE INDEX IF NOT EXISTS ix_assets_serial_number ON assets (serial_number);
CREATE INDEX IF NOT EXISTS ix_assets_updated_at    ON assets (updated_at DESC);

-- ── Tabel transactions ────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS transactions (
    id                  VARCHAR(36)     PRIMARY KEY,
    asset_id            VARCHAR(36)     NOT NULL
                        REFERENCES assets(id) ON DELETE CASCADE,
    asset_name          VARCHAR(255)    NOT NULL,
    asset_serial_number VARCHAR(100)    NOT NULL,
    type                transactiontype NOT NULL,
    recipient_name      VARCHAR(255)    NOT NULL    DEFAULT '',
    destination         VARCHAR(200)    NOT NULL    DEFAULT '',
    notes               TEXT            NOT NULL    DEFAULT '',
    timestamp_ms        BIGINT          NOT NULL,   -- epoch milliseconds (dari device)
    latitude            DOUBLE PRECISION,
    longitude           DOUBLE PRECISION,
    gps_accuracy_meters REAL,
    evidence_filename   VARCHAR(255),               -- nama file di folder /evidence
    evidence_type       VARCHAR(10)                 -- 'PHOTO' atau 'PDF'
);

-- Index untuk performa query
CREATE INDEX IF NOT EXISTS ix_transactions_asset_id     ON transactions (asset_id);
CREATE INDEX IF NOT EXISTS ix_transactions_timestamp_ms ON transactions (timestamp_ms DESC);
CREATE INDEX IF NOT EXISTS ix_transactions_type         ON transactions (type);

-- ── Views berguna untuk laporan ───────────────────────────────────────────────

-- View: aset yang sedang dipinjam beserta info penerima terakhir
CREATE OR REPLACE VIEW v_borrowed_assets AS
SELECT
    a.id,
    a.name,
    a.serial_number,
    a.category,
    a.location,
    t.recipient_name,
    t.destination,
    t.timestamp_ms  AS borrowed_at_ms,
    to_timestamp(t.timestamp_ms / 1000.0) AS borrowed_at,
    t.latitude,
    t.longitude
FROM assets a
JOIN transactions t ON t.id = (
    SELECT id FROM transactions
    WHERE asset_id = a.id
      AND type = 'CHECK_OUT'
    ORDER BY timestamp_ms DESC
    LIMIT 1
)
WHERE a.status = 'BORROWED';

-- View: ringkasan transaksi per hari
CREATE OR REPLACE VIEW v_daily_transactions AS
SELECT
    DATE(to_timestamp(timestamp_ms / 1000.0)) AS tanggal,
    type,
    COUNT(*) AS jumlah,
    COUNT(evidence_filename) AS dengan_bukti
FROM transactions
GROUP BY tanggal, type
ORDER BY tanggal DESC;

-- ── Sample data (opsional, hapus di production) ───────────────────────────────

-- INSERT INTO assets (id, name, category, serial_number, description, location, status, created_at, updated_at)
-- VALUES
--   ('aaa-001', 'Laptop Dell XPS 15', 'Perangkat Komputer', 'SN-DELL-001', 'Core i7, 16GB RAM', 'Rak A-01', 'AVAILABLE', 1700000000000, 1700000000000),
--   ('aaa-002', 'Switch Cisco SG350', 'Perangkat Jaringan', 'SN-CISC-002', '24-port managed switch', 'Rak B-03', 'AVAILABLE', 1700000000000, 1700000000000),
--   ('aaa-003', 'Crimping Tool RJ45', 'Tools',              'SN-TOOL-003', 'Set lengkap dengan kabel tester', 'Laci C-01', 'AVAILABLE', 1700000000000, 1700000000000);

-- ── Verifikasi struktur ───────────────────────────────────────────────────────

-- \dt         → list semua tabel
-- \dv         → list semua view
-- SELECT * FROM assets LIMIT 5;
-- SELECT * FROM transactions LIMIT 5;
-- SELECT * FROM v_borrowed_assets;

