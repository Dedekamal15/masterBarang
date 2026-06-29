const express = require('express');
const router = express.Router();
const { testConnection } = require('../db/pool');

/**
 * GET /api/v1/health
 * Cek server aktif dan koneksi database
 */
router.get('/', async (req, res, next) => {
  try {
    const db = await testConnection();
    res.json({
      success: true,
      status: 'ok',
      version: '1.0.0',
      db: db.ok ? 'connected' : 'disconnected',
      timestamp: Date.now(),
    });
  } catch (err) {
    next(err);
  }
});

module.exports = router;
