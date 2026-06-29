const { Pool } = require('pg');

const pool = new Pool({
  host: process.env.DB_HOST || 'localhost',
  port: parseInt(process.env.DB_PORT || '3306', 10),
  user: process.env.DB_USER || 'masterbarang',
  password: process.env.DB_PASS || 'M3rakest',
  database: process.env.DB_NAME || 'masterbarang_db',
  max: 20,
  idleTimeoutMillis: 30000,
  connectionTimeoutMillis: 5000,
});

pool.on('error', (err) => {
  console.error('[DB] Unexpected error on idle client:', err);
});

/**
 * Helper query dengan parameterized statement.
 * Contoh: query('SELECT * FROM assets WHERE id = $1', [id])
 */
async function query(text, params) {
  const start = Date.now();
  const result = await pool.query(text, params);
  const duration = Date.now() - start;
  console.log(`[DB] Query: ${duration}ms | rows: ${result.rowCount} | ${text.substring(0, 80)}`);
  return result;
}

/**
 * Test koneksi database
 */
async function testConnection() {
  try {
    const result = await query('SELECT NOW() AS current_time');
    return { ok: true, time: result.rows[0].current_time };
  } catch (err) {
    console.error('[DB] Connection test failed:', err.message);
    return { ok: false, error: err.message };
  }
}

module.exports = { pool, query, testConnection };
