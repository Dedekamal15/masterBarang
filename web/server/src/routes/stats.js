const express = require('express');
const router = express.Router();
const { query } = require('../db/pool');

/**
 * GET /api/v1/stats
 * Statistik ringkasan dashboard:
 * - totalAssets, availableCount, borrowedCount, maintenanceCount
 * - totalTransactions, todayTransactions
 * - categories (distribusi per kategori)
 */
router.get('/', async (req, res, next) => {
  try {
    // Ambil semua stat dalam satu query
    const assetStats = await query(`
      SELECT
        COUNT(*)::int                                          AS "totalAssets",
        COUNT(*) FILTER (WHERE status = 'AVAILABLE')::int      AS "availableCount",
        COUNT(*) FILTER (WHERE status = 'BORROWED')::int       AS "borrowedCount",
        COUNT(*) FILTER (WHERE status = 'MAINTENANCE')::int    AS "maintenanceCount"
      FROM assets
    `);

    const txStats = await query(`
      SELECT
        COUNT(*)::int                                                   AS "totalTransactions",
        COUNT(*) FILTER (WHERE to_timestamp(timestamp_ms / 1000.0)::date = CURRENT_DATE)::int AS "todayTransactions"
      FROM transactions
    `);

    const categories = await query(`
      SELECT category, COUNT(*)::int AS count
      FROM assets
      GROUP BY category
      ORDER BY count DESC
    `);

    res.json({
      success: true,
      ...assetStats.rows[0],
      ...txStats.rows[0],
      categories: categories.rows,
    });
  } catch (err) {
    next(err);
  }
});

module.exports = router;
