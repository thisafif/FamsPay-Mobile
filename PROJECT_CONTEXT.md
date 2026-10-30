# PROJECT_CONTEXT.md

## FamsPay – Family Finance Management Application

---

# 1. Project Overview

**FamsPay** adalah aplikasi pencatatan dan pengelolaan keuangan keluarga yang dirancang untuk digunakan bersama oleh anggota dalam satu rumah tangga.

Aplikasi ini membantu keluarga untuk:
- mencatat pemasukan dan pengeluaran secara terpusat,
- memantau kondisi keuangan tiap anggota dan keluarga,
- mengatur batas pengeluaran bulanan per anggota,
- mendorong kebiasaan finansial yang lebih disiplin,
- serta mendukung target tabungan pribadi tiap anggota.

FamsPay bukan aplikasi dompet bersama keluarga. Setiap pengguna memiliki **dompet personal**, sementara keluarga berfungsi sebagai **grup manajemen dan agregasi data keuangan**.

**Platform yang didukung:**
- **Web Application**
- **Mobile Application (Android)**

Kedua platform menggunakan **backend API yang sama** dengan seluruh data tersimpan secara terpusat di **cloud database**.

---

# 2. System Goals

Tujuan utama pengembangan sistem:

1. Menyediakan sistem pencatatan keuangan keluarga yang sederhana dan mudah digunakan.
2. Memberikan visibilitas keuangan keluarga bagi admin tanpa membuka data pribadi secara berlebihan.
3. Membantu mengontrol pengeluaran anggota melalui limit bulanan.
4. Menyediakan dashboard dan analitik keuangan yang relevan.
5. Memotivasi anggota keluarga untuk menabung melalui fitur goals pribadi.

---

# 3. Core Concept

Konsep dasar operasional FamsPay:

- **1 Keluarga = 1 Grup**
- Satu user hanya boleh bergabung dalam **1 family**
- Tidak ada **dompet keluarga bersama**
- Setiap user memiliki **dompet virtual personal**
- **Admin adalah member dengan hak akses tambahan**
- **Akun admin adalah akun pribadi biasa dengan privilege khusus**, bukan akun sistem terpisah
- Admin dapat mengatur limit pengeluaran bulanan tiap anggota
- Semua transaksi tetap tercatat sebagai histori keuangan user
- Goals bersifat **privat per user**
- Family dashboard bersifat **agregat**, bukan detail pribadi semua goals

---

# 4. Final Business Rules

Bagian ini berisi keputusan final yang harus menjadi acuan implementasi.

## 4.1 Wallet

- Hanya ada **dompet personal** untuk setiap user.
- Family **tidak memiliki wallet bersama**.
- Admin juga memiliki wallet karena admin adalah member dengan hak tambahan.
- `wallet_balance` dapat menjadi **negatif**.
- Jika transaksi menyebabkan `wallet_balance` negatif, sistem harus menampilkan **warning** sebelum transaksi dikonfirmasi.
- Setelah user menyetujui warning, transaksi tetap boleh disimpan.

## 4.2 Spending Limit

- Setiap user memiliki `monthly_limit` yang ditetapkan oleh admin.
- Sistem juga menyimpan `remaining_limit` untuk periode bulan berjalan.
- **Expense** mengurangi `remaining_limit`.
- **Income** menambah `remaining_limit`.
- Jika transaksi menyebabkan `remaining_limit` negatif, sistem harus menampilkan **warning** sebelum transaksi dikonfirmasi.
- Setelah user menyetujui warning, transaksi tetap boleh disimpan.
- Reset limit dilakukan setiap awal bulan dengan membuka periode limit baru.

## 4.3 Transaction Visibility

- Member hanya dapat melihat transaksi miliknya sendiri.
- Admin dapat melihat seluruh transaksi dalam family.
- Member tidak dapat melihat transaksi anggota lain.
- Goals tetap privat dan tidak dapat dilihat user lain.

## 4.4 Transaction Date

- User **tidak boleh** membuat transaksi dengan tanggal di masa depan.
- Jika transaksi lama diedit atau dihapus, perubahan hanya memengaruhi **periode bulan transaksi tersebut**, bukan bulan berjalan.

## 4.5 Categories

- Sistem memiliki **kategori bawaan**.
- Jika user memasukkan transaksi di luar kategori yang tersedia, sistem akan memetakan transaksi tersebut ke kategori **`Other`**.
- Kategori `Savings` dan `Goal Withdrawal` adalah kategori sistem.

## 4.6 Goal-Linked Transactions

- Alokasi dana ke goal akan membuat transaksi sistem yang terhubung ke goal.
- Transaksi yang terhubung ke goal tidak boleh diedit atau dihapus langsung dari modul transaksi umum.
- Perubahan dana goal hanya boleh dilakukan melalui aksi khusus pada modul goal.
- Withdraw dana dari goal boleh dilakukan **sebagian**.
- Withdraw dari goal dicatat sebagai transaksi **income**.
- Jika `current_amount >= target_amount`, status goal otomatis menjadi **`completed`**.

## 4.7 Family Management

- `join_code` dapat digunakan **berkali-kali** oleh calon anggota family.
- Admin dapat mengubah `family_name`.
- User dapat mengubah profil pribadi: nama, email, password, dan foto profil.
- Pengelolaan member dilakukan secara wajar oleh admin, termasuk pengaturan role dan manajemen anggota.
- Admin dapat melihat profil keuangan anggota dalam family, termasuk saldo anggota, total limit, sisa limit, pemakaian limit, statistik transaksi, dan riwayat transaksi anggota tersebut.
- Akses profil keuangan anggota tidak membuka goals pribadi anggota lain.
- Admin tetap memiliki wallet personal, transaksi pribadi, limit pribadi, dan goals pribadi seperti member biasa.

---

# 5. User Roles & Permissions

## Admin

Admin adalah akun pribadi/user biasa yang juga memiliki privilege manajemen family.

Hak admin:
- membuat family baru,
- membagikan `join_code`,
- mengelola anggota family,
- mengatur `monthly_limit` tiap anggota,
- melihat profil keuangan anggota, termasuk saldo, limit, pemakaian limit, statistik, dan riwayat transaksi anggota,
- melihat seluruh histori transaksi family,
- mengakses family dashboard dan analitik agregat,
- membuat dan mengelola transaksi pribadi,
- membuat dan mengelola goals pribadi.

## Member

Member adalah user yang hanya mengelola datanya sendiri.

Hak member:
- bergabung ke family melalui `join_code`,
- membuat, mengedit, dan menghapus (soft delete) transaksi miliknya sendiri,
- melihat dashboard dan analitik pribadi,
- membuat dan mengelola goals pribadi,
- tidak dapat melihat transaksi anggota lain,
- tidak dapat mengatur limit anggota lain.

---

# 6. Permission Matrix

| Aksi | Admin | Member |
|---|---|---|
| Lihat transaksi sendiri | Ya | Ya |
| Lihat transaksi semua anggota | Ya | Tidak |
| Tambah transaksi sendiri | Ya | Ya |
| Edit/hapus transaksi sendiri | Ya | Ya |
| Edit/hapus transaksi goal-linked dari modul transaksi umum | Tidak | Tidak |
| Atur monthly limit anggota | Ya | Tidak |
| Lihat profil keuangan anggota | Ya | Tidak |
| Ubah family name | Ya | Tidak |
| Buat goal pribadi | Ya | Ya |
| Lihat goal user lain | Tidak | Tidak |
| Lihat family dashboard | Ya | Tidak |
| Lihat personal dashboard | Ya | Ya |

---

# 7. Feature List (Functional Requirements / MVP)

## F-01: Authentication

- Register
- Login
- Logout
- Social login/Google login disembunyikan dari UI aplikasi mobile untuk MVP
- Forgot password/reset password disembunyikan dari UI aplikasi mobile untuk MVP
- Update profil
  - nama
  - email
  - password
  - foto profil

## F-02: Family Management

- Create family (Admin)
- Join family via `join_code`
- `join_code` dapat digunakan berulang kali
- Ubah `family_name` (Admin)
- Manajemen anggota dan role (Admin)
- Profil keuangan anggota (Admin)
  - melihat saldo anggota
  - melihat total limit, sisa limit, dan pemakaian limit anggota
  - melihat statistik dan riwayat transaksi anggota tertentu
  - tidak menampilkan goals pribadi anggota lain

## F-03: Personal Wallet & Monthly Limit

### Wallet
- Setiap user memiliki wallet personal
- `wallet_balance` bertambah saat income
- `wallet_balance` berkurang saat expense
- Jika saldo menjadi negatif, sistem menampilkan warning sebelum konfirmasi
- Setelah dikonfirmasi user, transaksi tetap boleh disimpan

### Monthly Limit
- Admin menetapkan `monthly_limit`
- `remaining_limit` disimpan per user per bulan
- Expense mengurangi `remaining_limit`
- Income menambah `remaining_limit`
- Jika `remaining_limit` negatif, sistem menampilkan warning sebelum konfirmasi
- Setelah dikonfirmasi user, transaksi tetap boleh disimpan
- Periode limit baru dibuka pada awal bulan

## F-04: Transactions

Field transaksi minimum:
- `type` (`income` / `expense`)
- `amount`
- `txn_date`
- `category_name`
- `note`

Aturan transaksi:
- transaksi tidak boleh bertanggal masa depan,
- transaksi biasa boleh diedit dan di-soft delete,
- perubahan transaksi hanya memengaruhi bulan dari `txn_date` transaksi tersebut,
- transaksi menjadi basis utama histori keuangan.

## F-05: Dashboard & Analytics

### Personal Dashboard
Menampilkan data milik user sendiri:
- wallet balance
- pemasukan bulan ini
- pengeluaran bulan ini
- total tabungan pribadi
- flow analysis
- budget analysis
- recent transactions

### Family Dashboard (Admin Only)
Menampilkan data agregat family:
- total saldo keluarga
- total pemasukan bulan ini
- total pengeluaran bulan ini
- total tabungan keluarga (agregat)
- flow analysis keluarga
- budget analysis keluarga
- recent transactions family

## F-06: Individual Goals

- Membuat goal pribadi
- Menentukan `target_amount`
- Mengalokasikan dana dari wallet ke goal
- Menarik dana dari goal secara sebagian atau penuh
- Menampilkan progress tabungan
- Status goal otomatis menjadi `completed` saat target tercapai

Aturan goal:
- goals bersifat privat,
- allocate to goal dicatat sebagai transaksi **expense** kategori `Savings`,
- withdraw from goal dicatat sebagai transaksi **income** kategori `Goal Withdrawal`,
- transaksi goal-linked hanya bisa diubah melalui modul goal.

---

# 8. Business Definitions

## `wallet_balance`
Jumlah saldo uang personal yang dimiliki user pada saat ini.

## `monthly_limit`
Batas dasar pengeluaran bulanan user yang ditetapkan admin.

## `remaining_limit`
Sisa limit user pada satu periode bulan tertentu, setelah dikurangi expense dan ditambah income.

## `family dashboard`
Dashboard agregat yang hanya dapat diakses admin dan menampilkan data keseluruhan family.

## `personal dashboard`
Dashboard yang menampilkan data keuangan user sendiri.

## `goal-linked transaction`
Transaksi sistem yang dibuat oleh aksi goal dan memiliki relasi ke `goal_id`.

---

# 9. Database Architecture

Database menggunakan pendekatan **Document-Oriented Database (MongoDB)**.

Untuk MVP, sistem menggunakan **5 collection utama**:

1. `users`
2. `families`
3. `transactions`
4. `goals`
5. `user_monthly_limits`

---

# 10. Database Design Decisions

## 10.1 General Principles

- Gunakan relasi referensial sederhana antar collection.
- Jangan embed members di `families`; membership dibaca dari `users.family_id`.
- Semua nominal uang disimpan sebagai **integer** rupiah, bukan float.
- Gunakan soft delete untuk transaksi.
- Gunakan `created_at` dan `updated_at` pada semua collection utama.

## 10.2 Users

Collection `users` menyimpan data identitas user dan state wallet aktif.

### Minimal fields
- `_id`
- `full_name`
- `email`
- `password_hash`
- `avatar_url`
- `family_id`
- `role` (`admin` / `member`)
- `wallet_balance`
- `created_at`
- `updated_at`

## 10.3 Families

Collection `families` menyimpan data inti family.

### Minimal fields
- `_id`
- `family_name`
- `join_code`
- `created_by`
- `created_at`
- `updated_at`

## 10.4 Transactions

Collection `transactions` adalah ledger utama histori keuangan.

### Minimal fields
- `_id`
- `user_id`
- `family_id`
- `type`
- `amount`
- `txn_date`
- `category_name`
- `note`
- `goal_id` nullable
- `is_system`
- `is_deleted`
- `deleted_at` nullable
- `deleted_by` nullable
- `created_at`
- `updated_at`

## 10.5 Goals

Collection `goals` menyimpan target tabungan personal.

### Minimal fields
- `_id`
- `user_id`
- `family_id`
- `title`
- `target_amount`
- `current_amount`
- `status` (`active`, `completed`, `archived`)
- `created_at`
- `updated_at`

## 10.6 User Monthly Limits

Collection `user_monthly_limits` menyimpan limit per user per bulan.

### Minimal fields
- `_id`
- `user_id`
- `family_id`
- `period_month` (format: `YYYY-MM`)
- `monthly_limit_base`
- `remaining_limit`
- `total_income_in_period`
- `total_expense_in_period`
- `created_at`
- `updated_at`

### Constraint
- unique: `(user_id, period_month)`

---

# 11. Source of Truth

Untuk menjaga performa dan konsistensi data, sistem menggunakan pendekatan berikut:

- `transactions` adalah **source of truth utama** untuk histori pergerakan uang
- `users.wallet_balance` adalah **cached field** untuk saldo user saat ini
- `goals.current_amount` adalah **cached field** untuk progress goal
- `user_monthly_limits.remaining_limit` adalah **cached field** untuk limit aktif per periode

Semua cached field harus diperbarui secara konsisten setiap kali:
- transaksi dibuat,
- transaksi diubah,
- transaksi dihapus,
- goal dialokasikan,
- dana goal ditarik.

---

# 12. Goal Allocation Logic

## Allocate to Goal

Saat user mengalokasikan dana ke goal:

- buat transaksi `expense`
- `category_name = Savings`
- simpan `goal_id`
- `is_system = true`
- `goal.current_amount += amount`
- `wallet_balance -= amount`
- `remaining_limit -= amount`

## Withdraw from Goal

Saat user menarik dana dari goal:

- buat transaksi `income`
- `category_name = Goal Withdrawal`
- simpan `goal_id`
- `is_system = true`
- `goal.current_amount -= amount`
- `wallet_balance += amount`
- `remaining_limit += amount`

---

# 13. Transaction & Limit Rules

## Transaction Create/Update Validation

Saat transaksi dibuat atau diperbarui, sistem harus:

1. memvalidasi `txn_date` tidak melebihi hari ini,
2. memvalidasi `amount > 0`,
3. memetakan kategori yang tidak dikenal ke `Other`,
4. menghitung proyeksi `wallet_balance`,
5. menghitung proyeksi `remaining_limit` untuk bulan dari `txn_date`,
6. menampilkan warning bila salah satu menjadi negatif,
7. tetap menyimpan transaksi jika user menyetujui warning.

## Period Rule

- Transaksi memengaruhi bulan berdasarkan `txn_date`
- Edit atau delete transaksi hanya mengubah histori bulan yang sama
- Tidak mengubah bucket limit bulan lain

---

# 14. Indexing Recommendation

## `users`
- unique index pada `email`
- index pada `family_id`
- index pada `(family_id, role)`

## `families`
- unique index pada `join_code`

## `transactions`
- index pada `(user_id, txn_date)`
- index pada `(family_id, txn_date)`
- index pada `goal_id`
- index pada `(is_deleted, txn_date)`

## `goals`
- index pada `(user_id, status)`
- index pada `family_id`

## `user_monthly_limits`
- unique index pada `(user_id, period_month)`
- index pada `(family_id, period_month)`

---

# 15. API Architecture

Seluruh pertukaran data antara client dan database diakses melalui REST API.

```text
 [ Mobile App ]        [ Web App ]
       │                    │
       └─────────┬──────────┘
                 ▼
           [ REST API ]
                 │
                 ▼
            [ MongoDB ]
```

---

# 16. Technology Stack (Planned)

Frontend Web: Laravel Blade, TailwindCSS  
Mobile App: Android Native (Java)  
Backend: Laravel REST API  
Database: MongoDB  
Hosting: Cloud Infrastructure

---

# 17. Out of Scope (MVP v1)

Fitur berikut tidak termasuk dalam rilis awal:
- dompet keluarga bersama,
- recurring transaction otomatis,
- upload foto/scan struk,
- export PDF/Excel,
- kategori bertingkat,
- notifikasi push/email,
- approval transaksi over-limit,
- multi-rekening / transfer antar bank.

---

# 18. Key Design Principles

## Sederhana
Mudah dipahami dan digunakan seluruh anggota keluarga.

## Terstruktur
Semua aturan bisnis penting harus eksplisit dan tidak multitafsir.

## Privat namun tetap terkontrol
Goals bersifat privat, transaksi bersifat role-based, dan dashboard keluarga berbasis agregat.

## Konsisten
Web dan mobile harus mengikuti aturan bisnis yang sama.

## Scalable
Struktur database cukup sederhana untuk MVP, tetapi siap dikembangkan pada versi berikutnya.

---

# 19. Open Notes for Implementation

Beberapa detail operasional dapat dirinci lebih lanjut pada dokumen lanjutan seperti API Spec, ERD, dan Technical Design, tetapi project context ini sudah menjadi acuan utama untuk:
- business rules,
- functional scope,
- permission,
- struktur data utama,
- serta arah implementasi backend dan frontend.
