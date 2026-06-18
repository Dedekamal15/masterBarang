# Panduan Deploy AssetTrack — Backend + Android

---

## BAGIAN 1 — Deploy Backend ke Docker

### Prasyarat
- Docker & Docker Compose sudah terinstall
- Jalankan `docker ps` untuk verifikasi Docker aktif

---

### Langkah 1 — Masuk ke folder backend

```bash
cd ~/Desktop/assettrack/backend
```

---

### Langkah 2 — Build dan jalankan semua container

```bash
sudo docker compose up -d --build
```

Perintah ini akan:
- Build image FastAPI dari `Dockerfile`
- Pull image `postgres:16-alpine` dari Docker Hub
- Jalankan keduanya sebagai container di background (`-d`)
- Otomatis buat tabel database saat API pertama kali start

> **Pertama kali** butuh waktu 2–5 menit karena harus download image.
> Selanjutnya cukup beberapa detik.

---

### Langkah 3 — Verifikasi semua container jalan

```bash
sudo docker compose ps
```

Output yang diharapkan:
```
NAME               STATUS          PORTS
assettrack_api     Up (healthy)    0.0.0.0:8000->8000/tcp
assettrack_db      Up (healthy)    0.0.0.0:5432->5432/tcp
```

Kedua container harus **Up**.

---

### Langkah 4 — Test API berjalan

```bash
curl http://localhost:8000/api/v1/health
```

Respons yang benar:
```json
{"status":"ok","version":"1.0.0","db":"ok"}
```

Jika sudah muncul → **Backend siap**.

---

### Langkah 5 — Buka dokumentasi API interaktif (opsional)

Buka browser → `http://localhost:8000/docs`

Di sini kamu bisa test semua endpoint secara visual.

---

## BAGIAN 2 — Hubungkan Android ke Backend

Ada **2 skenario** tergantung cara kamu menjalankan app:

---

### Skenario A — Emulator Android (AVD)

Emulator Android tidak bisa akses `localhost` komputer langsung.
Gunakan IP khusus **`10.0.2.2`** yang mengarah ke localhost host.

File `app/build.gradle.kts` sudah dikonfigurasi otomatis:
```kotlin
debug {
    buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8000/\"")
}
```

**Tidak perlu ubah apa-apa** — langsung Run dari Android Studio.

---

### Skenario B — HP Fisik via USB / WiFi (yang kamu pakai sekarang)

HP fisik dan komputer harus **terhubung ke jaringan WiFi yang sama**.

#### Langkah 1 — Cari IP komputer kamu

```bash
ip addr show | grep "inet " | grep -v 127.0.0.1
```

Atau:
```bash
hostname -I | awk '{print $1}'
```

Contoh output: `192.168.1.10`
→ Catat IP ini, misalnya **`192.168.1.10`**

---

#### Langkah 2 — Update BASE_URL di Android

Buka file:
```
android/app/build.gradle.kts
```

Ubah bagian debug:
```kotlin
debug {
    // Ganti dengan IP komputer kamu
    buildConfigField("String", "BASE_URL", "\"http://192.168.1.10:8000/\"")
}
```

---

#### Langkah 3 — Pastikan port 8000 tidak diblokir firewall

```bash
# Izinkan port 8000 di firewall Linux
sudo ufw allow 8000/tcp

# Verifikasi API bisa diakses dari luar
curl http://192.168.1.10:8000/api/v1/health
```

Jika berhasil, coba akses dari browser HP:
`http://192.168.1.10:8000/api/v1/health`

Harus muncul `{"status":"ok",...}` di browser HP.

---

#### Langkah 4 — Rebuild dan install ulang app ke HP

Di Android Studio:
1. **Build → Clean Project**
2. **Run 'app'** — pilih HP kamu sebagai target device

---

## BAGIAN 3 — Perintah Docker yang Berguna

### Melihat log real-time

```bash
# Log API saja
sudo docker logs assettrack_api -f

# Log database saja
sudo docker logs assettrack_db -f

# Log semua container
sudo docker compose logs -f
```

### Stop semua container

```bash
sudo docker compose down
```

### Stop dan hapus data database (reset total)

```bash
sudo docker compose down -v
```

> ⚠️ Perintah `-v` menghapus volume database. Data hilang permanen.

### Restart hanya API (setelah update kode)

```bash
sudo docker compose up -d --build api
```

### Masuk ke dalam container database

```bash
sudo docker exec -it assettrack_db psql -U assettrack -d assettrack_db
```

Contoh query di dalam psql:
```sql
-- Lihat semua aset
SELECT id, name, serial_number, status FROM assets;

-- Lihat semua transaksi
SELECT id, asset_name, type, recipient_name, timestamp_ms FROM transactions;

-- Keluar
\q
```

---

## BAGIAN 4 — Alur Sinkronisasi

```
HP (offline mode)
  │
  │  Room DB lokal menyimpan data dengan isSynced = false
  │
  ▼
WorkManager (background, setiap 15 menit)
  │
  │  Deteksi koneksi internet/WiFi tersedia
  │
  ▼
POST http://192.168.1.10:8000/api/v1/assets/batch
POST http://192.168.1.10:8000/api/v1/transactions/batch
  │
  ▼
FastAPI → validasi Pydantic → UPSERT ke PostgreSQL
  │
  ▼
HTTP 200 → WorkManager update isSynced = true di Room
  │
  ▼
UI otomatis update: badge "Pending" → "Synced ✅"
```

---

## BAGIAN 5 — Troubleshooting

| Masalah | Solusi |
|---------|--------|
| `Connection refused` dari HP | Pastikan IP benar, firewall allow port 8000, HP dan PC 1 jaringan |
| `db` container tidak healthy | `sudo docker compose down -v` lalu `up -d --build` ulang |
| API crash saat start | `sudo docker logs assettrack_api` untuk lihat error |
| Data tidak sync ke server | Pastikan BASE_URL benar di build.gradle, rebuild app |
| Port 8000 sudah dipakai | Ganti `"8000:8000"` ke `"8001:8000"` di docker-compose.yml |
| Sync tidak terjadi otomatis | WorkManager berjalan 15 menit sekali — bisa trigger manual lewat tombol sync di topbar |

---

## BAGIAN 6 — Cek Koneksi dari Android ke Backend

Sebelum run app, test dulu dari HP:

1. Buka browser di HP
2. Ketik: `http://[IP_KOMPUTER]:8000/api/v1/health`
3. Harus muncul: `{"status":"ok","version":"1.0.0","db":"ok"}`

Kalau muncul → koneksi HP ke backend OK → app pasti bisa sync.
