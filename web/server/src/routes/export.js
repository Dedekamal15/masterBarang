const express = require('express');
const { query } = require('../db/pool');
const { generateCSV } = require('../utils/csvGenerator');

const router = express.Router();

/**
 * GET /api/v1/export/assets/csv
 * Download CSV semua barang (dengan filter opsional)
 *   ?status=BORROWED
 *   ?category=Perangkat Jaringan
 *   ?search=HK
 */
router.get('/assets/csv', async (req, res, next) => {
  try {
    const { status, category, search } = req.query;

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

    const result = await query(
      `SELECT a.id, a.name, a.category, a.serial_number, a.description,
              a.location, a.status, a.created_at, a.updated_at
       FROM assets a ${whereClause}
       ORDER BY a.updated_at DESC`,
      params
    );

    const fields = [
      { label: 'ID', value: 'id' },
      { label: 'Nama Barang', value: 'name' },
      { label: 'Kategori', value: 'category' },
      { label: 'Serial Number', value: 'serial_number' },
      { label: 'Deskripsi', value: 'description' },
      { label: 'Lokasi', value: 'location' },
      { label: 'Status', value: 'status' },
      { label: 'Dibuat (Epoch)', value: 'created_at' },
      { label: 'Diupdate (Epoch)', value: 'updated_at' },
    ];

    const csv = generateCSV(result.rows, fields);

    res.setHeader('Content-Type', 'text/csv; charset=utf-8');
    res.setHeader('Content-Disposition', `attachment; filename="assets_${Date.now()}.csv"`);
    res.send(csv);
  } catch (err) {
    next(err);
  }
});

/**
 * GET /api/v1/export/transactions/csv
 * Download CSV semua transaksi (dengan filter opsional)
 *   ?tx_type=CHECK_OUT
 *   ?from_ms=xxx&to_ms=xxx
 */
router.get('/transactions/csv', async (req, res, next) => {
  try {
    const { tx_type, from_ms, to_ms } = req.query;

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

    const result = await query(
      `SELECT t.id, t.asset_id, t.asset_name, t.asset_serial_number, t.type,
              t.recipient_name, t.destination, t.notes, t.timestamp_ms,
              t.latitude, t.longitude, t.gps_accuracy_meters,
              t.evidence_filename, t.evidence_type
       FROM transactions t ${whereClause}
       ORDER BY t.timestamp_ms DESC`,
      params
    );

    const fields = [
      { label: 'ID', value: 'id' },
      { label: 'Asset ID', value: 'asset_id' },
      { label: 'Nama Barang', value: 'asset_name' },
      { label: 'Serial Number', value: 'asset_serial_number' },
      { label: 'Tipe', value: 'type' },
      { label: 'Penerima', value: 'recipient_name' },
      { label: 'Tujuan', value: 'destination' },
      { label: 'Catatan', value: 'notes' },
      { label: 'Waktu (Epoch)', value: 'timestamp_ms' },
      { label: 'Latitude', value: 'latitude' },
      { label: 'Longitude', value: 'longitude' },
      { label: 'Akurasi GPS (m)', value: 'gps_accuracy_meters' },
      { label: 'File Bukti', value: 'evidence_filename' },
      { label: 'Tipe Bukti', value: 'evidence_type' },
    ];

    const csv = generateCSV(result.rows, fields);

    res.setHeader('Content-Type', 'text/csv; charset=utf-8');
    res.setHeader('Content-Disposition', `attachment; filename="transactions_${Date.now()}.csv"`);
    res.send(csv);
  } catch (err) {
    next(err);
  }
});

/**
 * GET /api/v1/export/transactions/csv/:assetId
 * Export CSV transaksi untuk satu barang tertentu
 */
router.get('/transactions/csv/:assetId', async (req, res, next) => {
  try {
    const { assetId } = req.params;
    const { tx_type } = req.query;

    const conditions = ['t.asset_id = $1'];
    const params = [assetId];
    let paramIdx = 2;

    if (tx_type) {
      conditions.push(`t.type = $${paramIdx++}`);
      params.push(tx_type.toUpperCase());
    }

    const whereClause = `WHERE ${conditions.join(' AND ')}`;

    const result = await query(
      `SELECT t.* FROM transactions t ${whereClause} ORDER BY t.timestamp_ms DESC`,
      params
    );

    const fields = [
      { label: 'ID', value: 'id' },
      { label: 'Asset ID', value: 'asset_id' },
      { label: 'Nama Barang', value: 'asset_name' },
      { label: 'Serial Number', value: 'asset_serial_number' },
      { label: 'Tipe', value: 'type' },
      { label: 'Penerima', value: 'recipient_name' },
      { label: 'Tujuan', value: 'destination' },
      { label: 'Catatan', value: 'notes' },
      { label: 'Waktu (Epoch)', value: 'timestamp_ms' },
      { label: 'Latitude', value: 'latitude' },
      { label: 'Longitude', value: 'longitude' },
    ];

    const csv = generateCSV(result.rows, fields);

    res.setHeader('Content-Type', 'text/csv; charset=utf-8');
    res.setHeader('Content-Disposition', `attachment; filename="transactions_${assetId}_${Date.now()}.csv"`);
    res.send(csv);
  } catch (err) {
    next(err);
  }
});

module.exports = router;
