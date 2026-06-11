# Gold-MS (Gold Management System) ⚜️

Gold-MS adalah aplikasi manajemen toko emas berbasis Android yang dirancang untuk memudahkan operasional toko, mulai dari manajemen inventaris, transaksi penjualan, buyback, hingga laporan keuangan dan grafik performa.

## ✨ Fitur Utama
- **Dashboard Interaktif**: Statistik harian (Omzet, Buyback, Pengeluaran, Stok) dan Grafik Mingguan (khusus Admin).
- **Manajemen Inventaris**: Kelola stok barang (Kalung, Gelang, Cincin, dll) lengkap dengan data karat dan berat.
- **Transaksi Real-time**: Pencatatan Penjualan dan Buyback dengan kalkulasi otomatis berdasarkan harga emas dunia.
- **Integrasi GoldAPI.io**: Mengambil harga emas dunia secara real-time untuk akurasi perhitungan.
- **Manajemen Pengeluaran**: Catat biaya operasional toko.
- **Laporan Transaksi**: Filter laporan berdasarkan rentang tanggal dan tipe transaksi.
- **Manajemen Pengguna**: Sistem Role (Admin & Kasir) dengan fitur aktivasi/nonaktifkan akun.

## 🚀 Cara Menjalankan Project

Aplikasi ini bersifat **Open Source**. Karena alasan keamanan, kredensial Firebase dan API Key telah dihapus. Ikuti langkah berikut untuk menjalankan di lingkungan lokal Anda:

### 1. Prasyarat
- Android Studio Ladybug atau versi lebih baru.
- Akun Firebase (Gratis).
- API Key dari [GoldAPI.io](https://www.goldapi.io/).

### 2. Setup Firebase
- Buat project baru di [Firebase Console](https://console.firebase.google.com/).
- Tambahkan aplikasi Android dengan package name: `com.kelompok.goldms`.
- Download file `google-services.json` dan letakkan di dalam folder `app/`.
- Aktifkan **Authentication** (Email/Password).
- Aktifkan **Cloud Firestore** dan buat koleksi bernama `users`.
- Buat satu akun Admin secara manual di tab Authentication, lalu buat dokumen di koleksi `users` dengan ID sesuai UID-nya (cek detail di panduan setup Admin).

### 3. Setup API Key
Buka file `local.properties` di root project Anda, lalu tambahkan baris berikut:
```properties
GOLD_API_KEY="goldapi-ISI_DENGAN_KEY_ANDA-io"
GOLD_API_BASE_URL="https://www.goldapi.io/"
```

### 4. Build & Run
- Klik **Sync Project with Gradle Files**.
- Pastikan koneksi internet aktif.
- Jalankan di Emulator atau Perangkat Fisik.

## 🛠️ Tech Stack
- **Language**: Kotlin
- **Architecture**: MVVM (Pattern)
- **Database & Auth**: Firebase Firestore & Firebase Auth
- **Networking**: Retrofit & OkHttp
- **Concurrency**: Kotlin Coroutines
- **UI Components**: Material Design 2/3, MPAndroidChart, ViewBinding

---
*Dibuat untuk keperluan manajemen toko emas yang efisien dan transparan.*
