# MasterBarang — Dokumentasi Lengkap

Sistem manajemen barang offline-first berbasis Android dengan backend FastAPI + PostgreSQL.

---

## DAFTAR ISI

1. [Arsitektur Sistem](#arsitektur)
2. [Struktur Folder](#struktur-folder)
3. [Setup Backend (Docker)](#setup-backend)
4. [Setup Database PostgreSQL](#setup-database)
5. [Setup Android](#setup-android)
6. [Konfigurasi Koneksi Android ↔ Backend](#koneksi)
7. [Yang Harus Diubah untuk Deployment Production](#deployment-production)
8. [Cara Menjalankan Sync](#sync)
9. [Upload Bukti Transaksi](#bukti)
10. [Perintah Berguna](#perintah-berguna)
11. [Troubleshooting](#troubleshooting)

---

## 1. Arsitektur Sistem <a name="arsitektur"></a>

```
┌──────────────────────────────────────────────────┐
│                ANDROID (HP)                       │
│                                                   │
│  UI: Jetpack Compose (MasterBarang)               │
│  DB Lokal: Room (SQLite)                          │
│  Sync: WorkManager (15 menit / manual)            │
│  File: Downloads/MasterBarang/bukti/              │
└────────────────────┬─────────────────────────────┘
                     │ HTTP/JSON
                     │ WiFi / Internet
                     ▼
┌──────────────────────────────────────────────────┐
│              DOCKER (Server/Laptop)               │
│                                                   │
│  FastAPI → port 8000                              │
│  PostgreSQL → port 5432                           │
│  Evidence files → ./backend/evidence/             │
└──────────────────────────────────────────────────┘
```

**Alur Sync:**
1. **Push UP** — data lokal (`isSynced=false`) dikirim ke server
2. **Upload** — file bukti foto/PDF diunggah ke server
3. **Pull DOWN** — semua data server ditarik ke lokal
4. UI terupdate otomatis (reaktif via Room Flow)

---

## 2. Struktur Folder <a name="struktur-folder"></a>

```
assettrack/
├── android/                    ← Project Android Studio
│   └── app/src/main/
│       ├── java/com/assettrack/
│       │   ├── data/           ← Room, Retrofit, File manager
│       │   ├── domain/         ← Repository, Model
│       │   ├── presentation/   ← Compose screens
│       │   └── worker/         ← SyncWorker
│       ├── res/
│       │   ├── values/strings.xml   ← Nama app: MasterBarang
│       │   ├── values/themes.xml
│       │   └── xml/file_paths.xml   ← FileProvider config
│       └── AndroidManifest.xml
│
├── backend/                    ← Project FastAPI
│   ├── app/
│   │   ├── models/models.py    ← SQLAlchemy ORM
│   │   ├── schemas/schemas.py  ← Pydantic validation
│   │   ├── routers/
│   │   │   ├── assets.py       ← /assets, /master-sync
│   │   │   ├── transactions.py ← /transactions, /evidence
│   │   │   └── health.py       ← /health
│   │   └── database.py         ← Koneksi PostgreSQL async
│   ├── evidence/               ← Folder bukti transaksi (auto-created)
│   ├── main.py                 ← Entry point FastAPI
│   ├── requirements.txt
│   ├── Dockerfile
│   └── docker-compose.yml
│
├── init_database.sql           ← Script SQL manual PostgreSQL
└── DOKUMENTASI.md              ← File ini
```

---

## 3. Setup Backend (Docker) <a name="setup-backend"></a>

### Prasyarat
- Docker & Docker Compose terinstall
- Port 8000 dan 5432 tidak dipakai aplikasi lain

### Langkah-langkah

```bash
# 1. Masuk ke folder backend
cd assettrack/backend

# 2. Build dan jalankan semua container
sudo docker compose up -d --build

# 3. Cek status container (tunggu ~30 detik pertama kali)
sudo docker compose ps

# Output yang diharapkan:
# NAME               STATUS          PORTS
# masterbarang_api   Up (healthy)    0.0.0.0:8000->8000/tcp
# masterbarang_db    Up (healthy)    0.0.0.0:5432->5432/tcp

# 4. Test API berjalan
curl http://localhost:8000/api/v1/health
# → {"status":"ok","version":"1.0.0","db":"ok"}

# 5. Lihat dokumentasi API interaktif
# Buka browser: http://localhost:8000/docs
```

> **Catatan:** Folder `evidence/` untuk menyimpan bukti transaksi
> dibuat **otomatis** saat build. Tidak perlu buat manual.

---

## 4. Setup Database PostgreSQL <a name="setup-database"></a>

### Opsi A — Otomatis via Docker (Disarankan)

Database dibuat otomatis saat `docker compose up --build` pertama kali.
FastAPI akan membuat semua tabel saat startup.

### Opsi B — Manual via SQL Script

Jika ingin inisialisasi manual atau migrasi ke server lain:

```bash
# Masuk ke container PostgreSQL
sudo docker exec -it masterbarang_db psql -U masterbarng -d masterbarang_db

# Di dalam psql, jalankan script:
\i /path/to/init_database.sql

# Atau dari luar container:
sudo docker exec -i masterbarang_db psql -U masterbarng -d masterbarang_db \
    < init_database.sql
```

### Struktur Database

```sql
-- Tabel utama
assets (
    id VARCHAR(36) PRIMARY KEY,    -- UUID dari device
    name VARCHAR(255),
    category VARCHAR(100),         -- Perangkat Komputer | Perangkat Jaringan | Tools | Other
    serial_number VARCHAR(100) UNIQUE,
    description TEXT,
    location VARCHAR(200),
    status ENUM(AVAILABLE, BORROWED, MAINTENANCE),
    created_at BIGINT,             -- epoch milliseconds
    updated_at BIGINT
)

transactions (
    id VARCHAR(36) PRIMARY KEY,    -- UUID dari device
    asset_id VARCHAR(36) FK → assets.id,
    type ENUM(CHECK_OUT, CHECK_IN),
    recipient_name VARCHAR(255),
    destination VARCHAR(200),      -- free text
    notes TEXT,
    timestamp_ms BIGINT,           -- epoch milliseconds
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    gps_accuracy_meters REAL,
    evidence_filename VARCHAR(255), -- nama file di folder /evidence
    evidence_type VARCHAR(10)       -- PHOTO | PDF
)
```

### Query berguna di PostgreSQL

```sql
-- Lihat semua barang
SELECT id, name, serial_number, category, status FROM assets;

-- Barang yang sedang dipinjam
SELECT * FROM v_borrowed_assets;

-- Riwayat transaksi per hari
SELECT * FROM v_daily_transactions;

-- Barang kategori tertentu
SELECT * FROM assets WHERE category = 'Perangkat Komputer';

-- Transaksi dengan bukti foto
SELECT t.*, a.name FROM transactions t
JOIN assets a ON a.id = t.asset_id
WHERE t.evidence_filename IS NOT NULL;
```

---

## 5. Setup Android <a name="setup-android"></a>

### Prasyarat
- Android Studio Hedgehog (2023.1) atau lebih baru
- JDK 17 (sudah bundled dengan Android Studio)
- Android SDK API 26+ (minSdk = 26)
- HP dengan Android 8.0+ atau Emulator AVD

### Langkah-langkah

```
1. Buka Android Studio
2. File → Open → pilih folder "assettrack/android"
3. Tunggu Gradle sync selesai (perlu internet, 3-10 menit pertama kali)
4. Sambungkan HP via USB atau buka Emulator
5. Klik tombol ▶ Run
```

### Permission yang Diminta Pertama Kali
- **Kamera** — untuk scan barcode dan foto bukti
- **Lokasi** — untuk GPS lock saat transaksi
- **Storage** — untuk simpan foto/PDF di MasterBarang/

---

## 6. Konfigurasi Koneksi Android ↔ Backend <a name="koneksi"></a>

### Jika pakai Emulator Android

Tidak perlu ubah apa-apa. `10.0.2.2` sudah dikonfigurasi di `build.gradle.kts`:

```kotlin
debug {
    buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8000/\"")
}
```

### Konfigurasi Jaringan Lokal (Local Network)

Server: **192.168.70.86**, port API: **1626**, port DB: **3306**

Konfigurasi ini sudah diterapkan di `build.gradle.kts` dan `docker-compose.yml`.
Tidak perlu mengubah apa pun — langsung build dan deploy.

```kotlin
// android/app/build.gradle.kts — sudah terkonfigurasi
debug {
    buildConfigField("String", "BASE_URL", "\"http://192.168.70.86:1626/\"")
}
release {
    buildConfigField("String", "BASE_URL", "\"http://192.168.70.86:1626/\"")
}
```

**Langkah 3** — Izinkan port di firewall:
```bash
sudo ufw allow 8000/tcp
```

**Langkah 4** — Test dari browser HP:
```
http://192.168.1.25:8000/api/v1/health
```
Harus muncul: `{"status":"ok","db":"ok"}`

**Langkah 5** — Rebuild app:
Di Android Studio: **Build → Clean Project** → **▶ Run**

---

## 7. Yang Harus Diubah untuk Deployment Production <a name="deployment-production"></a>

### A. Backend — `docker-compose.yml`

```yaml
# UBAH password database (jangan pakai default!)
environment:
  POSTGRES_PASSWORD: GantiDenganPasswordKuat123!

# UBAH di service api:
environment:
  DATABASE_URL: postgresql+asyncpg://masterbarng:GantiDenganPasswordKuat123!@db:5432/masterbarang_db
```

### B. Backend — Tambah HTTPS (Production)

Pasang Nginx sebagai reverse proxy:
```nginx
# /etc/nginx/sites-available/masterbarang
server {
    listen 443 ssl;
    server_name api.masterbarang.com;

    ssl_certificate     /etc/ssl/certs/masterbarang.crt;
    ssl_certificate_key /etc/ssl/private/masterbarang.key;

    location / {
        proxy_pass http://localhost:8000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # Untuk upload file besar (foto/PDF)
    client_max_body_size 20M;
}
```

### C. Android — `build.gradle.kts` untuk Release

```kotlin
release {
    isMinifyEnabled = true
    buildConfigField(
        "String", "BASE_URL",
        "\"https://api.masterbarang.com/\""  // ← Ganti URL production
    )
}
```

### D. Android — Izin Jaringan Cleartext (Development Only)

File `AndroidManifest.xml` sudah allow cleartext untuk development.
Untuk production (HTTPS), hapus baris ini:
```xml
<!-- HAPUS untuk production karena HTTPS tidak butuh ini -->
android:usesCleartextTraffic="true"
```

### E. Checklist Deployment Production

```
□ Ganti password PostgreSQL di docker-compose.yml
□ Set BASE_URL ke HTTPS di build.gradle.kts release config
□ Pasang SSL certificate (Let's Encrypt gratis)
□ Setup Nginx reverse proxy
□ Backup otomatis database PostgreSQL (cron job)
□ Monitor disk space folder evidence/ (foto/PDF bisa besar)
□ Generate signed APK / AAB untuk distribusi:
  Android Studio → Build → Generate Signed Bundle/APK
□ Hapus android:usesCleartextTraffic dari Manifest untuk production
```

---

## 8. Cara Menjalankan Sync <a name="sync"></a>

### Otomatis (setiap 15 menit)
WorkManager berjalan di background secara otomatis saat ada koneksi internet.

### Manual via UI
- **TopBar** → tap ikon `⟳` (berputar saat syncing, merah jika ada pending)
- **Dashboard Banner** → tap tombol **"Sync"** di banner kuning

### Urutan proses Sync:
1. PUSH assets baru → `POST /api/v1/assets/batch`
2. PUSH transaksi baru → `POST /api/v1/transactions/batch`
3. UPLOAD file bukti → `POST /api/v1/transactions/{id}/evidence`
4. PULL semua data dari server → `GET /api/v1/master-sync`

---

## 9. Upload Bukti Transaksi <a name="bukti"></a>

Saat **Barang Keluar (Check-Out)**, petugas bisa melampirkan bukti:

| Tipe | Sumber | Disimpan di HP | Diupload ke Server |
|------|--------|----------------|-------------------|
| Foto | Kamera langsung | `Downloads/MasterBarang/bukti/` | `/app/evidence/` |
| Foto | Galeri HP | `Downloads/MasterBarang/bukti/` | `/app/evidence/` |
| PDF  | Storage HP | `Downloads/MasterBarang/bukti/` | `/app/evidence/` |

**Folder di HP:**
```
/storage/emulated/0/Download/MasterBarang/
└── bukti/
    ├── {transaction-id}.jpg
    └── {transaction-id}.pdf
```

**Folder di Server:**
```
assettrack/backend/evidence/
├── {transaction-id}.jpg
└── {transaction-id}.pdf
```

---

## 10. Perintah Berguna <a name="perintah-berguna"></a>

### Docker

```bash
# Jalankan semua container
sudo docker compose up -d --build

# Stop semua container
sudo docker compose down

# Reset total (hapus semua data)
sudo docker compose down -v

# Lihat log real-time
sudo docker compose logs -f

# Log API saja
sudo docker logs masterbarang_api -f

# Restart hanya API setelah update kode
sudo docker compose up -d --build api

# Masuk ke database
sudo docker exec -it masterbarang_db psql -U masterbarng -d masterbarang_db

# Backup database
sudo docker exec masterbarang_db pg_dump -U masterbarng masterbarang_db \
    > backup_$(date +%Y%m%d).sql

# Restore database
sudo docker exec -i masterbarang_db psql -U masterbarng masterbarang_db \
    < backup_20240101.sql
```

### Android Studio

```
Gradle Sync:     File → Sync Project with Gradle Files
Clean Build:     Build → Clean Project
Run:             Shift + F10
Logcat:          View → Tool Windows → Logcat
Filter log:      Tag: SyncWorker ATAU Tag: AssetRepository
```

---

## 11. Troubleshooting <a name="troubleshooting"></a>

| Masalah | Penyebab | Solusi |
|---------|----------|--------|
| App tidak bisa konek ke backend | IP salah atau firewall | Cek `BASE_URL` di `build.gradle.kts`, jalankan `sudo ufw allow 8000` |
| Kamera hitam saat scan | Background thread issue | Update sudah fix ini — rebuild app |
| Sync tidak jalan otomatis | WorkManager throttle | Tunggu 15 menit, atau tap tombol Sync manual |
| Upload bukti gagal | File sudah dihapus | Cek folder `MasterBarang/bukti/` di HP |
| Database error saat startup | Volume corrupt | `sudo docker compose down -v && sudo docker compose up -d --build` |
| Port 8000 sudah dipakai | Konflik port | Ganti `"8000:8000"` ke `"8001:8000"` di `docker-compose.yml` |
| Gradle sync gagal | Cache corrupt | Android Studio: `File → Invalidate Caches → Restart` |
| Room database error | Schema berubah | `fallbackToDestructiveMigration()` sudah aktif — uninstall app di HP lalu install ulang |
| Evidence folder tidak ada | Docker build belum jalan | `sudo docker compose up -d --build` ulang |
| `python-multipart` error | Dependency hilang | Pastikan `requirements.txt` punya baris `python-multipart==0.0.9` |

---

## API Endpoints Referensi

| Method | Endpoint | Deskripsi |
|--------|----------|-----------|
| `GET`  | `/api/v1/health` | Cek status server dan database |
| `GET`  | `/api/v1/master-sync?since_ms=0` | Pull semua data dari server |
| `POST` | `/api/v1/assets/batch` | Push batch asset ke server |
| `GET`  | `/api/v1/assets` | List semua asset |
| `GET`  | `/api/v1/assets/{id}` | Detail satu asset |
| `POST` | `/api/v1/transactions/batch` | Push batch transaksi ke server |
| `GET`  | `/api/v1/transactions` | List semua transaksi |
| `POST` | `/api/v1/transactions/{id}/evidence` | Upload bukti foto/PDF |
| `GET`  | `/api/v1/transactions/{id}/evidence` | Download bukti |

Dokumentasi interaktif: `http://localhost:8000/docs`
