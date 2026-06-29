# AssetTrack Web — Website Manajemen Barang

Website untuk melihat data barang dan transaksi dari database **AssetTrack**.
Frontend **Vite + React**, backend **Express.js + PostgreSQL**.

---

## 📁 Struktur Folder

```
web/
├── server/              ← Express.js REST API
│   ├── src/
│   │   ├── db/pool.js            ← Koneksi PostgreSQL
│   │   ├── routes/health.js      ← GET /api/v1/health
│   │   ├── routes/stats.js       ← GET /api/v1/stats
│   │   ├── routes/assets.js      ← GET /api/v1/assets
│   │   ├── routes/transactions.js ← GET /api/v1/transactions
│   │   ├── routes/export.js      ← GET /api/v1/export/*/csv
│   │   ├── routes/masterSync.js  ← GET /api/v1/master-sync
│   │   ├── middleware/errorHandler.js
│   │   ├── utils/csvGenerator.js
│   │   ├── utils/responseFormatter.js
│   │   └── index.js              ← Entry point
│   ├── Dockerfile
│   └── package.json
│
├── client/              ← Vite + React
│   ├── src/
│   │   ├── api/                  ← Axios services
│   │   ├── components/
│   │   │   ├── layout/           ← Sidebar, Navbar, Layout
│   │   │   ├── ui/               ← StatCard, DataTable, Pagination, dll
│   │   │   └── maps/             ← LocationMap (Leaflet)
│   │   ├── pages/                ← Dashboard, Assets, Transactions, Export
│   │   ├── hooks/                ← useFetch, useDebounce
│   │   ├── utils/                ← formatDate, formatStatus
│   │   └── App.jsx               ← Routing
│   ├── Dockerfile
│   └── package.json
│
└── docker-compose.yml
```

---

## 🚀 Cara Menjalankan (Development)

### 1. Jalankan Backend Express.js

Backend Express.js membutuhkan **PostgreSQL** yang sudah berisi database `masterbarang_db`.
Gunakan backend Docker yang sudah ada (`assettrack/backend/docker-compose.yml`).

```bash
# Dari folder assettrack/backend (pastikan database jalan)
sudo docker compose up -d db

# Dari folder web/server
cd web/server

# Buat .env (copy dari .env.example)
cp .env.example .env
# Edit .env: sesuaikan DB_HOST, DB_PORT, DB_USER, DB_PASS, DB_NAME

# Install dependencies
npm install

# Jalankan server
npm run dev
```

Server berjalan di **http://localhost:3001**

### 2. Jalankan Frontend Vite

```bash
# Dari folder web/client
cd web/client

# Install dependencies
npm install

# Jalankan dev server
npm run dev
```

Frontend berjalan di **http://localhost:5173** (Vite otomatis proxy `/api` ke Express)

---

## 🐳 Cara Menjalankan (Production dengan Docker)

```bash
cd web

# Build & jalankan semua service
sudo docker compose up -d --build

# Akses website di http://localhost
```

---

## 📡 API Endpoints

| Method | Endpoint | Query Params | Kegunaan |
|--------|----------|-------------|----------|
| GET | `/api/v1/health` | - | Cek server & database |
| GET | `/api/v1/stats` | - | Statistik dashboard |
| GET | `/api/v1/master-sync` | `?since_ms=0` | Semua data sekaligus |
| GET | `/api/v1/assets` | `?status=&category=&search=&page=&limit=` | Daftar barang |
| GET | `/api/v1/assets/:id` | - | Detail barang |
| GET | `/api/v1/assets/:id/transactions` | `?tx_type=&from_ms=&to_ms=` | Riwayat transaksi barang |
| GET | `/api/v1/transactions` | `?tx_type=&from_ms=&to_ms=&page=&limit=` | Daftar transaksi |
| GET | `/api/v1/transactions/:id` | - | Detail transaksi |
| GET | `/api/v1/transactions/:id/evidence` | - | File bukti (foto/PDF) |
| GET | `/api/v1/export/assets/csv` | `?status=&category=&search=` | Download CSV barang |
| GET | `/api/v1/export/transactions/csv` | `?tx_type=&from_ms=&to_ms=` | Download CSV transaksi |

---

## 🔧 Environment Variables

### Server (`web/server/.env`)

```
PORT=3001
DB_HOST=localhost          # Host PostgreSQL
DB_PORT=3306               # Port PostgreSQL (mapped)
DB_USER=masterbarang        # User PostgreSQL
DB_PASS=M3rakest           # Password PostgreSQL
DB_NAME=masterbarang_db    # Nama database
EVIDENCE_DIR=../../backend/evidence  # Folder bukti transaksi
CORS_ORIGIN=http://localhost:5173     # Origin frontend
```

### Client (`web/client/.env`)

```
VITE_API_URL=http://localhost:3001/api/v1
```

> **Catatan:** Di development, Vite proxy `/api` ke Express, jadi tidak perlu set `VITE_API_URL`.
> Set hanya jika frontend di-deploy terpisah dari backend.
