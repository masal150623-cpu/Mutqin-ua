# MUTQIN ULUL ALBAB

**Platform Monitoring dan Evaluasi Perkembangan Santri Al-Qur'an untuk TPA dan Tahfidz Khusus Ulul Albab**

Aplikasi Android native yang dirancang khusus untuk memonitor, mengevaluasi, dan mengelola perkembangan santri Al-Qur'an secara terpadu, terstruktur, dan aman. Mendukung dua peran pengguna utama dalam satu APK: **Kepala Program / Admin** dan **Ustaz / Ustazah Pengajar**.

---

## 1. Arsitektur Aplikasi & Struktur Package

Aplikasi dibangun menggunakan prinsip **Clean Architecture** dan **MVVM (Model-View-ViewModel)** dengan **Repository Pattern**:

```
com.example/
├── MainActivity.kt               # Entrypoint & host tema
├── ui/
│   └── theme/                    # Color (Emerald Islamic Palette), Theme, Type
└── mutqin/
    ├── core/
    │   ├── model/                # UserRole (ADMIN, TEACHER), UserSession, Resource<T>, ProgramConstants
    │   └── security/             # PasswordSecurity (SHA-256 + cryptographic Salt generator & validator)
    ├── data/
    │   ├── local/
    │   │   ├── entity/           # Room Database Entities (16 tabel data)
    │   │   ├── dao/              # MutqinDao (Query reaktif Flow & transaksi)
    │   │   └── AppDatabase.kt    # Database Room & seeding otomatis awal
    │   └── repository/
    │       ├── AuthRepository.kt       # Otentikasi, sesi login, audit log masuk/keluar
    │       ├── TeacherRepository.kt    # CRUD pengajar, penugasan kelompok, aktivasi akun
    │       ├── GroupRepository.kt      # Kelompok D/C/B/A, tahun ajaran aktif
    │       ├── SantriRepository.kt     # Profil santri, filter status, mutasi kelompok & histori
    │       ├── LearningRepository.kt   # 6 modul: Iqro, Tahsin, Ziyadah, Murajaah, Presensi, Jurnal
    │       ├── EvaluationRepository.kt # Evaluasi bulanan, usulan & persetujuan kenaikan kelompok
    │       └── BackupRepository.kt     # Backup JSON dengan checksum SHA-256, inspeksi & restore
    ├── feature/
    │   ├── auth/                 # LoginScreen, AuthViewModel (dengan Quick Demo buttons)
    │   ├── admin/                # AdminDashboardScreen (Beranda, Santri, Pengajar, Kenaikan, Sistem/Backup)
    │   └── teacher/              # TeacherDashboardScreen (Setoran, Murajaah, Iqro, Tahsin, Presensi, Jurnal)
    └── navigation/
        └── MutqinNavGraph.kt     # Pengatur alur layar & otorisasi role
```

---

## 2. Empat Kelompok Konfigurasi Pembelajaran

Aplikasi mengimplementasikan 4 kelompok standar Al-Qur'an Ulul Albab:
1. **Kelompok D — Pembelajaran Iqro'**:
   - Fokus: Pengenalan huruf hijaiyah, makharijul huruf dasar, kelancaran Iqro jilid 1–6.
   - Indikator: Kelancaran membaca, ketepatan makhraj, pencatatan jilid dan halaman.
2. **Kelompok C — Perbaikan Bacaan Al-Qur'an (Tahsin)**:
   - Fokus: Tartil tilawah, panjang-pendek (mad), ghunnah, hukum nun sukun/tanwin, waqaf & ibtida.
   - Indikator: Skor kelancaran, skor makhraj, skor tajwid, rekomendasi latihan.
3. **Kelompok B — Persiapan & Setoran Hafalan (Ziyadah)**:
   - Fokus: Penambahan hafalan baru dengan target standar minimal 5 ayat lancar per sesi.
   - Indikator: Validasi rentang ayat, penandaan ayat mutqin/dikuasai, skor kelancaran & tajwid.
4. **Kelompok A — Murajaah & Pemeliharaan Hafalan**:
   - Fokus: Menjaga dan memelihara hafalan yang sudah mutqin agar tidak mudah lupa.
   - Indikator: Pengulangan berkala juz/surah, ketahanan sima'an, pencatatan kesalahan.

*Catatan: Admin dapat menambah, mengedit, atau menonaktifkan kelompok secara dinamis.*

---

## 3. Matriks Hak Akses (Role Access Matrix)

| Modul / Fitur | Kepala Program / Admin | Ustaz / Ustazah Pengajar |
| :--- | :---: | :---: |
| **Login & Ganti Sandi Sendiri** | Ya | Ya |
| **Melihat Seluruh Santri** | Ya (Semua Kelompok) | Terbatas (Hanya kelompok binaan) |
| **Pendaftaran & Edit Profil Santri** | Ya (Penuh) | Tidak (Hanya baca) |
| **Pindah Kelompok Santri (Mutasi)** | Ya (Eksekusi langsung) | Tidak (Hanya usulan) |
| **Kelola Akun Pengajar (NIP, Nama, Status)**| Ya | Tidak |
| **Penugasan Pengajar ke Kelompok** | Ya (Multi-assign) | Tidak |
| **Kelola Kelompok & Tahun Ajaran** | Ya | Tidak |
| **Input Setoran Ziyadah & Murajaah** | Ya | Ya (Santri binaan) |
| **Input Catatan Iqro' & Tahsin** | Ya | Ya (Santri binaan) |
| **Input Presensi Kehadiran Santri** | Ya | Ya (Santri binaan) |
| **Pengisian Jurnal Pengajar Harian** | Ya | Ya |
| **Usulan Kenaikan Kelompok** | Memutuskan (Setuju/Tolak/Tunda)| Mengajukan usulan |
| **Ekspor Data & Cadangan (Backup JSON)** | Ya (Full export) | Tidak |
| **Pemulihan Data (Restore)** | Ya (Dengan verifikasi & pratinjau)| Tidak |
| **Audit Log Keamanan** | Ya | Tidak |

---

## 4. Keamanan & Mekanisme Akun Pengajar

1. **Pencegahan Kredensial Teks Biasa**:
   - Kata sandi tidak pernah disimpan dalam format teks biasa.
   - Menggunakan hashing `SHA-256` dengan salt acak kriptografis 16-byte unik per pengguna (`PasswordSecurity.kt`).
2. **Pembuatan Akun Tanpa Logout Admin**:
   - Admin mendaftarkan pengajar langsung di dalam aplikasi melalui formulir terenkripsi tanpa merusak sesi login admin.
3. **Nonaktifkan Akun Tanpa Merusak Histori**:
   - Penonaktifan akun pengajar dilakukan via toggle `isActive = false`. ID stabil pengajar tetap ada dan seluruh riwayat catatan pembelajaran, setoran santri, dan jurnal masa lalu tetap utuh.
4. **Validasi Hak Akses Berlapis**:
   - Pengajar hanya dapat memuat dan menginput santri yang terdaftar dalam `assignedGroupIds` miliknya.

---

## 5. Mekanisme Backup & Restore Aman

- **Ekspor JSON Lengkap**:
  - Menyimpan seluruh 16 tabel data ke dalam berkas JSON terstruktur.
  - Dilengkapi metadata: waktu ekspor, nama pembuat, jumlah santri, jumlah pengajar, jumlah catatan.
  - Memiliki **SHA-256 Checksum** digital untuk menjamin data tidak mengalami korupsi atau modifikasi tidak sah.
- **Restore dengan Pratinjau & Konfirmasi**:
  - Memeriksa keabsahan format header aplikasi (`MUTQIN_ULUL_ALBAB`).
  - Menampilkan ringkasan data yang akan dipulihkan sebelum dieksekusi.
  - Menjalankan transaksi restore tanpa menghancurkan relasi foreign key stabil.
  - Mencatat log audit pemulihan data.

---

## 6. Kredensial Uji Coba Cepat (Demo Mode)

Aplikasi dilengkapi tombol pintasan instan pada halaman Login:
- **Admin / Kepala Program**:
  - Email: `admin@ululalbab.id`
  - Kata Sandi: `admin123`
- **Ustaz Pengajar**:
  - Email: `ustaz.ahmad@ululalbab.id`
  - Kata Sandi: `ustaz123`
  - Kelompok diampu default: Kelompok D (Iqro') & Kelompok C (Tahsin).

---

## 7. Cara Membuka & Menjalankan Proyek di Android Studio

1. Buka **Android Studio** (Koala / Ladybug atau versi terbaru).
2. Pilih **Open**, arahkan ke direktori proyek ini.
3. Biarkan Gradle melakukan sync otomatis (Gradle 9.x, Android Gradle Plugin 9.x, Kotlin 2.2.x, KSP).
4. Jalankan pada Emulator atau Perangkat Android fisik via tombol **Run 'app'** (`Shift + F10`).
5. Perintah Build APK Debug via terminal:
   ```bash
   gradle assembleDebug
   ```
   File APK hasil build tersimpan di:
   `app/build/outputs/apk/debug/app-debug.apk`
