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
                     │ WiFi / Internet (Port 1626)
                     ▼
┌──────────────────────────────────────────────────┐
│              DOCKER (Server/Laptop)               │
│                                                   │
│  Portainer (Manajemen)    → port 9000            │
│  pgAdmin (GUI DB)         → port 5050            │
│  FastAPI (API Gateway)    → port 1626 (ext)      │
│  PostgreSQL (Database)    → port 3306 (ext)      │
│  Evidence files           → ./backend/evidence/  │
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
│   └── docker-compose.yml      ← Konfigurasi Docker (API, DB, pgAdmin, Portainer)
│
├── init_database.sql           ← Script SQL manual PostgreSQL
└── DOKUMENTASI.md              ← File ini
```

---

## 3. Setup Backend (Docker) <a name="setup-backend"></a>

### Prasyarat
- Docker & Docker Compose terinstall
- Port `1626` (API), `3306` (PostgreSQL), `5050` (pgAdmin), dan `9000` (Portainer) tidak digunakan oleh aplikasi lain.

### Langkah-langkah

```bash
# 1. Masuk ke folder backend
cd assettrack/backend

# 2. Build dan jalankan semua container (API, DB, pgAdmin, Portainer)
sudo docker compose up -d --build

# 3. Cek status container (tunggu ~30 detik pertama kali)
sudo docker compose ps

# Output yang diharapkan:
# NAME                     STATUS          PORTS
# masterbarang_api         Up              0.0.0.0:1626->8000/tcp
# masterbarang_db          Up (healthy)    0.0.0.0:3306->5432/tcp
# masterbarang_pgadmin     Up              0.0.0.0:5050->80/tcp
# masterbarang_portainer   Up              0.0.0.0:9000->9000/tcp

# 4. Test API berjalan
curl http://localhost:1626/api/v1/health
# → {"status":"ok","version":"1.0.0","db":"ok"}

# 5. Lihat dokumentasi API interaktif
# Buka browser: http://localhost:1626/docs
```

---

## 4. Setup Database PostgreSQL <a name="setup-database"></a>

### Opsi A — Otomatis via Docker (Disarankan)

Database dibuat otomatis saat `docker compose up --build` pertama kali.
FastAPI akan membuat semua tabel secara otomatis saat startup.

### Opsi B — UI via pgAdmin (Mudah)

1. Buka browser: `http://172.16.170.128:5050`
2. Login menggunakan:
   - Email: `admin@masterbarang.local`
   - Password: `M3rakest`
3. Tambahkan server baru dengan konfigurasi:
   - Host name/address: `db`
   - Port: `5432`
   - Maintenance database: `masterbarang_db`
   - Username: `masterbarang`
   - Password: `M3rakest`

### Opsi C — Manual via SQL Script

Jika ingin inisialisasi manual:

```bash
# Masuk ke container PostgreSQL dan jalankan query
sudo docker exec -it masterbarang_db psql -U masterbarng -d masterbarang_db

# Jalankan script SQL dari luar container:
sudo docker exec -i masterbarang_db psql -U masterbarng -d masterbarang_db < init_database.sql
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
3. Tunggu Gradle sync selesai
4. Sambungkan HP via USB atau buka Emulator
5. Klik tombol ▶ Run
```

---

## 6. Konfigurasi Koneksi Android ↔ Backend <a name="koneksi"></a>

### Konfigurasi Jaringan Lokal (Local Network)

Server: **172.16.170.128**, port API: **1626**, port DB: **3306**

Konfigurasi ini sudah diterapkan di `build.gradle.kts` dan `docker-compose.yml`.

```kotlin
// android/app/build.gradle.kts
debug {
    buildConfigField("String", "BASE_URL", "\"http://172.16.170.128:1626/\"")
}
release {
    buildConfigField("String", "BASE_URL", "\"http://172.16.170.128:1626/\"")
}
```

**Penting:** Whitelist HTTP di android diatur pada file `network_security_config.xml`:
```xml
<!-- android/app/src/main/res/xml/network_security_config.xml -->
<domain-config cleartextTrafficPermitted="true">
    <domain includeSubdomains="false">172.16.170.128</domain>
</domain-config>
```

---

## 7. Yang Harus Diubah untuk Deployment Production <a name="deployment-production"></a>

### A. Backend — `docker-compose.yml`

```yaml
# UBAH password database (jangan pakai default!)
environment:
  POSTGRES_PASSWORD: GantiDenganPasswordKuatBaru!

# UBAH di service api:
environment:
  DATABASE_URL: postgresql+asyncpg://masterbarng:GantiDenganPasswordKuatBaru!@db:5432/masterbarang_db
```

### B. Backend — Tambah HTTPS (Production)

Pasang Nginx sebagai reverse proxy ke port `1626`:
```nginx
server {
    listen 443 ssl;
    server_name api.masterbarang.com;

    ssl_certificate     /etc/ssl/certs/masterbarang.crt;
    ssl_certificate_key /etc/ssl/private/masterbarang.key;

    location / {
        proxy_pass http://localhost:1626;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    client_max_body_size 20M;
}
```

---

## 8. Cara Menjalankan Sync <a name="sync"></a>

### Otomatis (setiap 15 menit)
WorkManager berjalan di background secara otomatis saat ada koneksi internet.

### Manual via UI
- **TopBar** → tap ikon `⟳`
- **Dashboard Banner** → tap tombol **"Sync"**

---

## 9. Upload Bukti Transaksi <a name="bukti"></a>

| Tipe | Sumber | Disimpan di HP | Diupload ke Server |
|------|--------|----------------|-------------------|
| Foto | Kamera langsung | `Downloads/MasterBarang/bukti/` | `/app/evidence/` |
| PDF  | Storage HP | `Downloads/MasterBarang/bukti/` | `/app/evidence/` |

---

## 10. Perintah Berguna <a name="perintah-berguna"></a>

```bash
# Jalankan semua container
sudo docker compose up -d --build

# Stop semua container
sudo docker compose down

# Reset total (HAPUS DATA DATABASE)
sudo docker compose down -v

# Lihat log real-time
sudo docker compose logs -f
```
