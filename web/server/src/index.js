require('dotenv').config();

const express = require('express');
const cors = require('cors');
const morgan = require('morgan');
const helmet = require('helmet');
const path = require('path');

const healthRoutes = require('./routes/health');
const statsRoutes = require('./routes/stats');
const assetsRoutes = require('./routes/assets');
const transactionsRoutes = require('./routes/transactions');
const exportRoutes = require('./routes/export');
const masterSyncRoutes = require('./routes/masterSync');

const { errorHandler, notFoundHandler } = require('./middleware/errorHandler');

const app = express();
const PORT = parseInt(process.env.PORT || '3001', 10);

// ── Middleware ──────────────────────────────────────────────
app.use(helmet({ crossOriginResourcePolicy: { policy: 'cross-origin' } }));
app.use(cors({
  origin: process.env.CORS_ORIGIN || 'http://localhost:5173',
  credentials: true,
}));
app.use(morgan('dev'));
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true }));

// ── Routes ─────────────────────────────────────────────────
app.use('/api/v1/health', healthRoutes);
app.use('/api/v1/stats', statsRoutes);
app.use('/api/v1/assets', assetsRoutes);
app.use('/api/v1/transactions', transactionsRoutes);
app.use('/api/v1/export', exportRoutes);
app.use('/api/v1/master-sync', masterSyncRoutes);

// ── Serve evidence files sebagai static ────────────────────
const evidenceDir = process.env.EVIDENCE_DIR || path.resolve(__dirname, '..', '..', '..', 'backend', 'evidence');
app.use('/evidence', express.static(evidenceDir));

// ── Root check ─────────────────────────────────────────────
app.get('/', (req, res) => {
  res.json({
    name: 'AssetTrack Web API',
    version: '1.0.0',
    endpoints: {
      health: '/api/v1/health',
      stats: '/api/v1/stats',
      assets: '/api/v1/assets',
      transactions: '/api/v1/transactions',
      export: '/api/v1/export',
      masterSync: '/api/v1/master-sync',
    },
  });
});

// ── Error handling ─────────────────────────────────────────
app.use(notFoundHandler);
app.use(errorHandler);

// ── Start Server ───────────────────────────────────────────
app.listen(PORT, () => {
  console.log(`[Server] AssetTrack Web API running on http://localhost:${PORT}`);
  console.log(`[Server] Health check: http://localhost:${PORT}/api/v1/health`);
});
