const express = require('express');
const path = require('path');
const { query } = require('../db/pool');
const { paginated } = require('../utils/responseFormatter');

const router = express.Router();

const EVIDENCE_DIR = process.env.EVIDENCE_DIR || path.resolve(__dirname, '..', '..', '..', '..', 'backend', 'evidence');

/**
 * GET /api/v1/transactions
 * Daftar semua transaksi dengan filter:
 *   ?tx_type=CHECK_OUT        — filter keluar/masuk
 *   ?from_ms=xxx&to_ms=xxx    — filter rentang waktu
 *   ?page=1&limit=20
 */
router.get('/', async (req, res, next) => {
  try {
    const { tx_type, from_ms, to_ms, page = '1', limit = '20' } = req.query;
    const pageNum = Math.max(1, parseInt(page, 10) || 1);
    const limitNum = Math.min(100, Math.max(1, parseInt(limit, 10) || 20));
    const offset = (pageNum - 1) * limitNum;

    const conditions = [];
    const params = [];
    let paramIdx = 1;

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

    const whereClause = conditions.length > 0 ? `WHERE ${conditions.join(' AND ')}` : '';

    const countResult = await query(
      `SELECT COUNT(*)::int AS total FROM transactions t ${whereClause}`,
      params
    );

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

/**
 * GET /api/v1/transactions/:id
 * Detail satu transaksi
 */
router.get('/:id', async (req, res, next) => {
  try {
    const { id } = req.params;
    const result = await query(
      `SELECT t.*, a.name AS asset_name, a.serial_number AS asset_serial_number, a.category AS asset_category
       FROM transactions t
       LEFT JOIN assets a ON a.id = t.asset_id
       WHERE t.id = $1`,
      [id]
    );

    if (result.rows.length === 0) {
      return res.status(404).json({ success: false, error: 'Transaction not found' });
    }

    res.json({ success: true, data: result.rows[0] });
  } catch (err) {
    next(err);
  }
});

/**
 * GET /api/v1/transactions/:id/evidence
 * Tampilkan/download file bukti (foto/PDF)
 */
router.get('/:id/evidence', async (req, res, next) => {
  try {
    const { id } = req.params;
    const result = await query(
      'SELECT evidence_filename, evidence_type FROM transactions WHERE id = $1',
      [id]
    );

    if (result.rows.length === 0) {
      return res.status(404).json({ success: false, error: 'Transaction not found' });
    }

    const { evidence_filename, evidence_type } = result.rows[0];

    if (!evidence_filename) {
      return res.status(404).json({ success: false, error: 'No evidence for this transaction' });
    }

    const filePath = path.join(EVIDENCE_DIR, evidence_filename);
    res.sendFile(filePath, (err) => {
      if (err) {
        if (err.code === 'ENOENT') {
          return res.status(404).json({ success: false, error: 'Evidence file not found on server' });
        }
        next(err);
      }
    });
  } catch (err) {
    next(err);
  }
});

module.exports = router;
