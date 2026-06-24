# Panduan Deploy AssetTrack — Backend + Android

---

## BAGIAN 1 — Deploy Backend ke Docker

### Prasyarat
- Docker & Docker Compose sudah terinstall.
- Port-port berikut harus bebas dan tidak terpakai oleh aplikasi lain:
  - **`1626`** (API FastAPI)
  - **`3306`** (Database PostgreSQL host port)
  - **`5050`** (pgAdmin GUI)
  - **`9000`** (Portainer GUI)

---

### Langkah 1 — Masuk ke folder backend

```bash
cd ~/Downloads/assettrack/backend
```

---

### Langkah 2 — Build dan jalankan semua container

```bash
sudo docker compose up -d --build
```

Perintah ini akan membuat dan menjalankan:
- **`masterbarang_db`** (PostgreSQL 16) dengan password `M3rakest`
- **`masterbarang_api`** (FastAPI) berjalan di port `1626`
- **`masterbarang_pgadmin`** (pgAdmin 4) di port `5050`
- **`masterbarang_portainer`** (Portainer CE) di port `9000`

---

### Langkah 3 — Verifikasi semua container berjalan

```bash
sudo docker compose ps
```

Output yang diharapkan:
```
NAME                     STATUS          PORTS
masterbarang_api         Up              0.0.0.0:1626->8000/tcp
masterbarang_db          Up (healthy)    0.0.0.0:3306->5432/tcp
masterbarang_pgadmin     Up              0.0.0.0:5050->80/tcp
masterbarang_portainer   Up              0.0.0.0:9000->9000/tcp
```

Semua container harus berstatus **Up**.

---

### Langkah 4 — Uji Coba Layanan

1. **API Health Check**:
   ```bash
   curl http://localhost:1626/api/v1/health
   ```
   Respons yang benar:
   ```json
   {"status":"ok","version":"1.0.0","db":"ok"}
   ```

2. **pgAdmin**:
   Akses `http://172.16.170.128:5050` melalui browser.
   - **Email**: `admin@masterbarang.local`
   - **Password**: `M3rakest`
   - **Koneksi ke DB**: Tambahkan server baru dengan host: `db` dan port: `5432`

3. **Portainer**:
   Akses `http://172.16.170.128:9000` melalui browser untuk mengelola container.

---

## BAGIAN 2 — Hubungkan Android ke Backend

### Skenario A — Emulator Android (AVD)

Jika Anda menggunakan emulator lokal, konfigurasikan `app/build.gradle.kts` dengan IP `10.0.2.2`:
```kotlin
debug {
    buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:1626/\"")
}
```

---

### Skenario B — HP Fisik (Menggunakan IP Server `172.16.170.128`)

HP fisik dan server backend harus berada dalam **jaringan lokal yang sama**.

#### Langkah 1 — Pastikan Konfigurasi Gradle
Buka file `android/app/build.gradle.kts` dan pastikan konfigurasi url debug & release sudah mengarah ke IP Server:
```kotlin
debug {
    buildConfigField("String", "BASE_URL", "\"http://172.16.170.128:1626/\"")
}
release {
    buildConfigField("String", "BASE_URL", "\"http://172.16.170.128:1626/\"")
}
```

#### Langkah 2 — Pastikan Whitelist Network Security
Pastikan file `android/app/src/main/res/xml/network_security_config.xml` sudah mengizinkan IP server untuk lalu lintas HTTP (Cleartext):
```xml
<domain-config cleartextTrafficPermitted="true">
    <domain includeSubdomains="false">172.16.170.128</domain>
</domain-config>
```

#### Langkah 3 — Izinkan Port pada Firewall Server
Jalankan perintah ini di server backend Anda agar port API bisa diakses dari HP:
```bash
sudo ufw allow 1626/tcp
sudo ufw allow 5050/tcp
sudo ufw allow 9000/tcp
```

#### Langkah 4 — Uji Koneksi dari HP
Buka browser di HP Anda dan ketikkan alamat:
`http://172.16.170.128:1626/api/v1/health`
Jika muncul response JSON `{"status":"ok", ...}`, koneksi siap digunakan.

#### Langkah 5 — Bersihkan dan Bangun Ulang Aplikasi
Di Android Studio:
1. Klik **Build → Clean Project**
2. Klik **Build → Rebuild Project**
3. Jalankan aplikasi (**Run 'app'**) di HP Anda.

---

## BAGIAN 3 — Perintah Docker Penting

### Melihat Log Real-time
```bash
# Log API saja
sudo docker compose logs api -f

# Log Database saja
sudo docker compose logs db -f

# Log seluruh container proyek
sudo docker compose logs -f
```

### Menghentikan Container
```bash
sudo docker compose down
```

### Menghentikan dan Menghapus Seluruh Data Database (Reset Total)
```bash
sudo docker compose down -v
```

### Restart Layanan API (Jika ada perubahan kode)
```bash
sudo docker compose up -d --build api
```
