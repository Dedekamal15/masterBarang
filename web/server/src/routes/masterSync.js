const express = require('express');
const { query } = require('../db/pool');

const router = express.Router();

/**
 * GET /api/v1/master-sync?since_ms=0
 * Semua data sekaligus (untuk sync)
 * since_ms=0 → ambil semua
 * since_ms > 0 → hanya data yang berubah sejak timestamp itu
 */
router.get('/', async (req, res, next) => {
  try {
    const sinceMs = parseInt(req.query.since_ms || '0', 10);
    const nowMs = Date.now();

    let assetCondition = '';
    let txCondition = '';
    const assetParams = [];
    const txParams = [];

    if (sinceMs > 0) {
      assetCondition = 'WHERE updated_at >= $1';
      assetParams.push(sinceMs);
      txCondition = 'WHERE timestamp_ms >= $1';
      txParams.push(sinceMs);
    }

    const assets = await query(
      `SELECT * FROM assets ${assetCondition} ORDER BY updated_at DESC`,
      assetParams
    );

    const transactions = await query(
      `SELECT * FROM transactions ${txCondition} ORDER BY timestamp_ms DESC`,
      txParams
    );

    // Count total (unfiltered)
    const totalAssets = (await query('SELECT COUNT(*)::int AS count FROM assets')).rows[0].count;
    const totalTransactions = (await query('SELECT COUNT(*)::int AS count FROM transactions')).rows[0].count;

    res.json({
      success: true,
      total_assets: parseInt(totalAssets, 10),
      total_transactions: parseInt(totalTransactions, 10),
      assets: assets.rows,
      transactions: transactions.rows,
      server_timestamp_ms: nowMs,
    });
  } catch (err) {
    next(err);
  }
});

module.exports = router;
