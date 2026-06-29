# 🚀 Panduan Deploy AssetTrack Web

## Persiapan

Semua konfigurasi ada di file **`.env`** (copy dari template yang tersedia).
Docker Compose sudah 100% parameterized — tinggal pilih skenario.

---

## 📌 Skenario 1: Server Sama dengan Backend FastAPI (Kondisi Kamu)

Backend FastAPI + PostgreSQL sudah berjalan di server yang sama.

```bash
# 1. Copy konfigurasi
cp env.same-server.txt .env

# 2. Jalankan (tanpa db — pakai yang sudah ada)
docker compose up -d --build

# 3. Buka http://localhost
```

**Yang terjadi:**
- ✅ Web API join ke network `backend_masterbarang_net`
- ✅ Konek ke `masterbarang_db` yang sudah jalan
- ✅ Folder `backend/evidence/` di-share

---

## 📌 Skenario 2: Standalone (Server Baru + DB Baru)

Server baru, belum ada apa-apa.

```bash
# 1. Copy konfigurasi
cp env.standalone.txt .env

# 2. Edit .env — ganti password database!
nano .env
# → DB_PASS=GantiPassword123!

# 3. Jalankan DENGAN database
docker compose --profile db up -d --build

# 4. Buka http://localhost
```

**Yang terjadi:**
- ✅ Database PostgreSQL baru dibuat
- ✅ Tabel otomatis terisi dari `init_database.sql`
- ✅ Web + API + DB dalam 1 network

---

## 📌 Skenario 3: Web di Server Baru, DB di Server Lama

Frontend + API di server baru, database tetap di server backend lama.

```bash
# 1. Copy konfigurasi
cp env.remote-db.txt .env

# 2. Edit .env — ganti IP server backend
nano .env
# → DB_HOST=192.168.1.100   (IP server backend)
# → DB_PORT=3306            (port external DB)

# 3. Copy file evidence dari server lama
rsync -av user@server-lama:/path/to/backend/evidence/ ./data/evidence/

# 4. Jalankan
docker compose up -d --build

# 5. Buka http://localhost
```

---

## ⚙️ Variable Reference

| Variable | Default | Skenario 1 | Skenario 2 | Skenario 3 |
|----------|---------|-----------|-----------|-----------|
| `DB_HOST` | `masterbarang_db` | `masterbarang_db` | `db` | `192.168.1.x` |
| `DB_PORT` | `5432` | `5432` | `5432` | `3306` |
| `DB_USER` | `masterbarang` | ✅ | ✅ | ✅ |
| `DB_PASS` | `M3rakest` | ✅ | **GANTI** | ✅ |
| `DB_NAME` | `masterbarang_db` | ✅ | ✅ | ✅ |
| `NETWORK_NAME` | `assettrack_web_net` | `backend_masterbarang_net` | `assettrack_web_net` | `assettrack_web_net` |
| `NETWORK_EXTERNAL` | `false` | `true` | `false` | `false` |
| `EVIDENCE_HOST_PATH` | `../backend/evidence` | ✅ | `./data/evidence` | `/mnt/data/evidence` |
| `API_PORT` | `3001` | ✅ | ✅ | ✅ |
| `CLIENT_PORT` | `80` | ✅ | ✅ | ✅ |

---

## 🔧 Perintah Berguna

```bash
# Lihat status container
docker compose ps

# Lihat log real-time
docker compose logs -f

# Stop semua
docker compose down

# Hapus semua + volume database (HATI-HATI! data hilang)
docker compose down -v

# Rebuild tanpa cache
docker compose build --no-cache
```

---

## ✅ Checklist Deploy

- [ ] Copy `.env` dari template yang sesuai
- [ ] Ganti `DB_PASS` (khusus standalone)
- [ ] Ganti `DB_HOST` + `DB_PORT` (khusus remote-db)
- [ ] Pastikan folder evidence bisa diakses
- [ ] Pastikan port `80` dan `3001` tidak dipakai aplikasi lain
- [ ] Jika pakai firewall: buka port `80` (HTTP)
- [ ] Jalankan `docker compose up -d --build`
- [ ] Test: `curl http://localhost/api/v1/health`
