# AssetTrack — Offline-First Asset Management System

Sistem manajemen aset inventaris berbasis Android yang bekerja penuh secara **offline** dan melakukan sinkronisasi otomatis ke server FastAPI + PostgreSQL saat koneksi tersedia.

---

## Arsitektur Sistem

```
┌─────────────────────────────────────────────────────────┐
│                  ANDROID APP (Kotlin)                   │
│                                                         │
│  UI Layer (Jetpack Compose)                             │
│  ├── DashboardScreen   ← observes Room DB reactively    │
│  ├── RegistrationScreen ← OCR / Barcode / CSV import    │
│  ├── TransactionScreen  ← Check-out / Check-in + GPS    │
│  └── HistoryScreen      ← filterable transaction log    │
│                                                         │
│  ViewModel Layer (Hilt + StateFlow)                     │
│                                                         │
│  Domain Layer (Repository Pattern)                      │
│  └── AssetRepository                                    │
│       ├── Read  → Room DB (instant, no network needed)  │
│       └── Write → Room DB (isSynced = false)            │
│                                                         │
│  Data Layer                                             │
│  ├── Room DB (SQLite) ← single source of truth          │
│  │   ├── Table: assets       (AssetEntity)              │
│  │   └── Table: transactions (TransactionEntity)        │
│  ├── Retrofit API Client     (HTTP POST batch sync)     │
│  ├── WorkManager SyncWorker  (background, periodic)     │
│  ├── CameraX + ML Kit        (Barcode + OCR)            │
│  └── FusedLocationProvider   (GPS lock)                 │
└────────────────────┬────────────────────────────────────┘
                     │ HTTPS (JSON batch)
                     │ NetworkType.CONNECTED
                     ▼
┌─────────────────────────────────────────────────────────┐
│              FASTAPI BACKEND (Python)                   │
│                                                         │
│  POST /api/v1/assets/batch       ← UPSERT assets        │
│  POST /api/v1/transactions/batch ← INSERT txs           │
│  GET  /api/v1/health             ← connectivity probe   │
│                                                         │
│  Pydantic v2 validation                                 │
│  SQLAlchemy async ORM                                   │
│  PostgreSQL 16                                          │
└─────────────────────────────────────────────────────────┘
```

---

## Struktur Proyek

```
assettrack/
├── android/
│   └── app/src/main/
│       ├── AndroidManifest.xml
│       └── java/com/assettrack/
│           ├── AssetTrackApp.kt          # Hilt app + WorkManager config
│           ├── MainActivity.kt           # NavHost + Scaffold
│           ├── data/
│           │   ├── local/
│           │   │   ├── AssetTrackDatabase.kt
│           │   │   ├── dao/AssetDao.kt
│           │   │   ├── dao/TransactionDao.kt
│           │   │   ├── entity/AssetEntity.kt
│           │   │   └── entity/TransactionEntity.kt
│           │   ├── remote/
│           │   │   ├── api/AssetTrackApiService.kt
│           │   │   └── dto/Dtos.kt
│           │   ├── CsvParser.kt          # CSV bulk import
│           │   ├── LocationHelper.kt     # FusedLocation wrapper
│           │   └── Mappers.kt            # Entity ↔ Domain ↔ DTO
│           ├── di/
│           │   └── AppModule.kt          # Hilt DI bindings
│           ├── domain/
│           │   ├── model/Models.kt       # Domain data classes + enums
│           │   └── repository/AssetRepository.kt
│           ├── presentation/
│           │   ├── Screen.kt             # Nav routes
│           │   ├── components/
│           │   │   ├── SharedComponents.kt  # TopBar, BottomNav, Badges
│           │   │   └── CameraScanner.kt     # CameraX + ML Kit overlay
│           │   ├── screens/
│           │   │   ├── dashboard/        # ViewModel + Screen
│           │   │   ├── registration/     # ViewModel + Screen
│           │   │   ├── transaction/      # ViewModel + Screen
│           │   │   └── history/          # ViewModel + Screen
│           │   └── theme/Theme.kt        # Material3 colors
│           └── worker/
│               └── SyncWorker.kt         # WorkManager background sync
│
└── backend/
    ├── main.py                   # FastAPI app entry point
    ├── requirements.txt
    ├── Dockerfile
    ├── docker-compose.yml
    └── app/
        ├── database.py           # Async SQLAlchemy engine + session
        ├── models/models.py      # ORM models (Asset, Transaction)
        ├── schemas/schemas.py    # Pydantic v2 request/response schemas
        └── routers/
            ├── health.py         # GET /health
            ├── assets.py         # POST /assets/batch, GET /assets
            └── transactions.py   # POST /transactions/batch, GET /transactions
```

---

## Alur Data Lengkap

### 1. Pendaftaran Aset (Offline)
```
Petugas → [Scan Barcode/OCR] → SN terisi otomatis
        → Validasi duplikasi SN di Room DB lokal
        → Jika unik: INSERT ke assets (status=AVAILABLE, isSynced=false)
        → UI terupdate instan (reactive Flow)
```

### 2. Transaksi Check-out (Offline)
```
Petugas → Scan SN aset → Lookup di Room DB
        → GPS diambil otomatis (FusedLocationProvider)
        → Isi nama penerima + tujuan
        → INSERT ke transactions (isSynced=false)
        → UPDATE asset status → BORROWED
        → UI Dashboard terupdate otomatis
```

### 3. Sinkronisasi Otomatis (WorkManager)
```
WorkManager monitors NetworkType.CONNECTED
        → Jika ada koneksi: SyncWorker.doWork() aktif
        → Query Room: SELECT * FROM assets WHERE isSynced=0
        → POST /api/v1/assets/batch (JSON array)
        → Server validates + UPSERT ke PostgreSQL
        → Server returns HTTP 200 + {synced_count, failed_ids}
        → Room: UPDATE assets SET isSynced=1 WHERE id IN (successIds)
        → UI: sync indicator hilang otomatis (StateFlow update)
```

---

## Setup & Menjalankan

### Backend (FastAPI)

**Prasyarat:** Docker + Docker Compose

```bash
cd assettrack/backend

# Jalankan PostgreSQL + API server
docker-compose up -d

# API akan berjalan di: http://localhost:8000
# Docs interaktif: http://localhost:8000/docs
```

**Variabel environment (.env):**
```env
DATABASE_URL=postgresql+asyncpg://assettrack:assettrack123@localhost:5432/assettrack_db
```

### Android App

**Prasyarat:** Android Studio Hedgehog+, JDK 17, Android SDK 35

```bash
cd assettrack/android

# Buka di Android Studio
# Pastikan emulator/device terhubung
# Run 'app' configuration
```

**Konfigurasi BASE_URL** di `app/build.gradle.kts`:
```kotlin
debug {
    // Untuk emulator (localhost device → 10.0.2.2)
    buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8000/\"")
}
release {
    buildConfigField("String", "BASE_URL", "\"https://your-server.com/\"")
}
```

---

## API Endpoints

| Method | Path | Deskripsi |
|--------|------|-----------|
| GET | `/api/v1/health` | Health check + DB status |
| POST | `/api/v1/assets/batch` | Batch sync assets dari device |
| GET | `/api/v1/assets` | List semua aset di server |
| GET | `/api/v1/assets/{id}` | Detail satu aset |
| POST | `/api/v1/transactions/batch` | Batch sync transaksi dari device |
| GET | `/api/v1/transactions` | List semua transaksi di server |

### Contoh Request — Batch Asset Sync
```json
POST /api/v1/assets/batch
{
  "assets": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "name": "Mikrotik RB750Gr3",
      "category": "Networking Gear",
      "serial_number": "MTK-8829-1A",
      "description": "Router kantor utama",
      "location": "Rak A-12",
      "status": "AVAILABLE",
      "created_at": 1718000000000,
      "updated_at": 1718000000000
    }
  ]
}
```

### Contoh Response
```json
{
  "synced_count": 1,
  "failed_ids": [],
  "message": "Synced 1 assets. 0 failed."
}
```

---

## Format CSV Bulk Import

```csv
name,category,serial_number,description,location
"Laptop Dell XPS","Laptops","SN-ABC-001","Core i7 16GB","Rak A-01"
"iPhone 14 Pro","Mobile Devices","IMEI-123456789012345","Telepon lapangan","Laci B-03"
```

---

## Teknologi yang Digunakan

| Komponen | Teknologi |
|----------|-----------|
| UI | Jetpack Compose + Material3 |
| State Management | ViewModel + StateFlow + collectAsStateWithLifecycle |
| Local DB | Room (SQLite) dengan TypeConverters |
| Background Sync | WorkManager (PeriodicWork + OneTimeWork) |
| DI | Hilt (Dagger) |
| Network | Retrofit2 + OkHttp3 + Gson |
| Camera / OCR | CameraX + ML Kit (Barcode + TextRecognition) |
| GPS | FusedLocationProviderClient |
| Backend | FastAPI + Pydantic v2 |
| ORM | SQLAlchemy 2.x (async) |
| Database | PostgreSQL 16 |
| Container | Docker + Docker Compose |

---

## Perizinan Android

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
```

Izin runtime (CAMERA dan LOCATION) diminta menggunakan **Accompanist Permissions** saat fitur pertama kali digunakan.
