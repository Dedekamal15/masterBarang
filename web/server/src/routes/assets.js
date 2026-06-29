const express = require('express');
const { query } = require('../db/pool');
const { paginated } = require('../utils/responseFormatter');

const router = express.Router();

/**
 * GET /api/v1/assets
 * Daftar semua barang dengan filter:
 *   ?status=BORROWED          — filter by status
 *   ?category=Perangkat...    — filter by kategori
 *   ?search=HK                — cari by nama / serial_number (ILIKE)
 *   ?page=1&limit=20          — pagination
 */
router.get('/', async (req, res, next) => {
  try {
    const { status, category, search, page = '1', limit = '20' } = req.query;
    const pageNum = Math.max(1, parseInt(page, 10) || 1);
    const limitNum = Math.min(100, Math.max(1, parseInt(limit, 10) || 20));
    const offset = (pageNum - 1) * limitNum;

    const conditions = [];
    const params = [];
    let paramIdx = 1;

    if (status) {
      conditions.push(`a.status = $${paramIdx++}`);
      params.push(status.toUpperCase());
    }
    if (category) {
      conditions.push(`a.category = $${paramIdx++}`);
      params.push(category);
    }
    if (search) {
      conditions.push(`(a.name ILIKE $${paramIdx} OR a.serial_number ILIKE $${paramIdx})`);
      params.push(`%${search}%`);
      paramIdx++;
    }

    const whereClause = conditions.length > 0 ? `WHERE ${conditions.join(' AND ')}` : '';

    const countResult = await query(`SELECT COUNT(*)::int AS total FROM assets a ${whereClause}`, params);

    const dataResult = await query(
      `SELECT a.* FROM assets a ${whereClause} ORDER BY a.updated_at DESC LIMIT $${paramIdx} OFFSET $${paramIdx + 1}`,
      [...params, limitNum, offset]
    );

    return paginated(res, {
      data: dataResult.rows,
      total: countResult.rows[0].total,
      page: pageNum,
      limit: limitNum,
    });
  } catch (err) {
    next(err);
  }
});

/**
 * GET /api/v1/assets/:id
 * Detail satu barang
 */
router.get('/:id', async (req, res, next) => {
  try {
    const { id } = req.params;
    const result = await query('SELECT * FROM assets WHERE id = $1', [id]);

    if (result.rows.length === 0) {
      return res.status(404).json({ success: false, error: 'Asset not found' });
    }

    res.json({ success: true, data: result.rows[0] });
  } catch (err) {
    next(err);
  }
});

/**
 * GET /api/v1/assets/:id/transactions
 * Riwayat transaksi per barang
 *   ?tx_type=CHECK_OUT        — filter keluar/masuk
 *   ?from_ms=xxx&to_ms=xxx    — filter rentang waktu
 *   ?page=1&limit=20
 */
router.get('/:id/transactions', async (req, res, next) => {
  try {
    const { id } = req.params;
    const { tx_type, from_ms, to_ms, page = '1', limit = '20' } = req.query;
    const pageNum = Math.max(1, parseInt(page, 10) || 1);
    const limitNum = Math.min(100, Math.max(1, parseInt(limit, 10) || 20));
    const offset = (pageNum - 1) * limitNum;

    // Validasi asset exists
    const assetCheck = await query('SELECT id, name FROM assets WHERE id = $1', [id]);
    if (assetCheck.rows.length === 0) {
      return res.status(404).json({ success: false, error: 'Asset not found' });
    }

    const conditions = ['t.asset_id = $1'];
    const params = [id];
    let paramIdx = 2;

    if (tx_type) {
      conditions.push(`t.type = $${paramIdx++}`);
      params.push(tx_type.toUpperCase());
    }
    if (from_ms) {
      conditions.push(`t.timestamp_ms >= $${paramIdx++}`);
      params.push(parseInt(from_ms, 10));
    }
    if (to_ms) {
      conditions.push(`t.timestamp_ms <= $${paramIdx++}`);
      params.push(parseInt(to_ms, 10));
    }

    const whereClause = `WHERE ${conditions.join(' AND ')}`;

    const countResult = await query(`SELECT COUNT(*)::int AS total FROM transactions t ${whereClause}`, params);

    const dataResult = await query(
      `SELECT t.* FROM transactions t ${whereClause} ORDER BY t.timestamp_ms DESC LIMIT $${paramIdx} OFFSET $${paramIdx + 1}`,
      [...params, limitNum, offset]
    );

    return paginated(res, {
      data: dataResult.rows,
      total: countResult.rows[0].total,
      page: pageNum,
      limit: limitNum,
    });
  } catch (err) {
    next(err);
  }
});

module.exports = router;
