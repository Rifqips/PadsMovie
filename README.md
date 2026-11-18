# PadsMovie

PadsMovie adalah aplikasi Android native untuk menjelajahi film dari The Movie Database (TMDB). Pengguna bisa memilih genre, membuka detail film, membaca ulasan, dan menonton trailer YouTube. Proyek ini dibuat untuk memenuhi **Mandatory Task — Option 1**.

**Ide, ruang lingkup, rancangan arsitektur, dan arah desain produk berasal dari pemilik proyek. Implementasi aplikasi dikerjakan sepenuhnya menggunakan Codex sebagai agen AI, di bawah arahan dan review pemilik proyek.**

## Cakupan mandatory

| Poin | Implementasi |
| --- | --- |
| 1. Daftar genre resmi | Layar awal menampilkan genre film dari endpoint TMDB `/genre/movie/list`. |
| 2. Daftar film berdasarkan genre | Pilihan genre membuka grid poster dari endpoint `/discover/movie` dengan filter genre. |
| 3. Informasi utama film | Kartu film membuka detail berisi judul, tanggal rilis, durasi, rating, sinopsis, dan gambar jika tersedia. |
| 4. Ulasan pengguna | Tombol **Ulasan** membuka daftar ulasan untuk film yang dipilih. |
| 5. Trailer YouTube | Detail film membuka trailer dalam aplikasi jika tersedia. Pengguna juga dapat membuka video di YouTube. |
| 6. Endless scrolling | Paging 3 memuat halaman selanjutnya pada daftar film dan ulasan, mempertahankan item yang sudah dimuat saat terjadi error, dan menyediakan retry. |
| 7. Kasus positif dan negatif | UI menangani loading, sukses, data kosong, serta kegagalan yang bisa dicoba lagi. Unit test mencakup pemetaan respons, autentikasi TMDB, fallback trailer, dan pagination. |

Alur utama untuk reviewer adalah **Genre → Daftar film → Detail → Ulasan atau Trailer**. Katalog dapat dibuka tanpa login. Banner Remote Config, notifikasi FCM, serta masuk dan keluar dengan Firebase Authentication adalah fitur tambahan. Layar langganan masih berupa tampilan persiapan.

## Arsitektur

PadsMovie memakai pendekatan Clean Architecture untuk katalog film, dengan pemisahan domain, sumber data, dan presentasi.

| Modul | Tanggung jawab |
| --- | --- |
| `:core:domain` | Model domain seperti `Genre`, `MovieSummary`, dan `Page`, serta kontrak `MovieRepository`. Modul ini tidak bergantung pada Android atau Retrofit. |
| `:core:data` | Implementasi repository TMDB, pemetaan DTO ke model domain, serta konfigurasi Retrofit dan OkHttp. Kode dipisah ke `tmdb.model`, `tmdb.network`, dan `tmdb.repository`. |
| `:core:designsystem` | Tema Material 3 dan komponen Compose yang digunakan bersama. |
| `:app` | Layar, ViewModel, PagingSource, navigasi, dan konfigurasi Hilt. Kode fitur dikelompokkan di `feature/<nama>`, dengan navigasi dan komponen UI bersama di package terpisah. |

```text
:app ──► :core:domain
     ├─► :core:data ──► :core:domain
     └─► :core:designsystem
```

ViewModel memanggil kontrak `MovieRepository`. Hilt menyediakan implementasi TMDB dari `:core:data`. Implementasi itu memakai `TmdbApi` dan mengubah respons API menjadi model domain sebelum data ditampilkan oleh Compose. Pemisahan ini juga memungkinkan unit test menyuntikkan repository palsu tanpa menghubungi TMDB.

## Tech stack

- **UI** — Kotlin, Jetpack Compose, Material 3, Navigation Compose, dan Coil.
- **State dan pagination** — Jetpack ViewModel, Kotlin Coroutines, Flow, dan Paging 3.
- **Networking dan DI** — Retrofit, OkHttp, Gson, Dagger Hilt, dan KSP.
- **Layanan tambahan** — Firebase Remote Config, Cloud Messaging, dan Authentication.
- **Build dan tes** — Gradle Kotlin DSL, version catalog, JUnit, MockWebServer, Compose UI Test, dan GitHub Actions.

## Menjalankan proyek

1. Clone repo dan buka folder proyek di Android Studio. Gunakan JDK 17 dan Android SDK 36. Aplikasi mendukung Android 8.1 (API 27) ke atas.
2. Siapkan **TMDB API Read Access Token** dan set `TMDB_READ_ACCESS_TOKEN` di Gradle properties pengguna. Di Windows, file ini biasanya berada di `%USERPROFILE%\.gradle\gradle.properties`. Gunakan token milik sendiri dan simpan di luar repo.

   ```properties
   TMDB_READ_ACCESS_TOKEN=your_read_access_token
   ```
3. Jalankan Gradle Sync. Pilih `developmentDebug` di **Build Variants**, lalu gunakan **Run** dengan HP fisik atau emulator. Konfigurasi Firebase yang disertakan cocok untuk application ID production. Katalog TMDB tetap dapat dipakai di development tanpa Firebase.

Build, unit test, dan lint dapat dijalankan tanpa perangkat:

```powershell
.\gradlew.bat :app:assembleDevelopmentDebug :app:testDevelopmentDebugUnitTest :core:data:test :app:lintDevelopmentDebug
```

Tes Compose memerlukan perangkat terhubung:

```powershell
.\gradlew.bat :app:connectedDevelopmentDebugAndroidTest
```

Build, unit test, lint, dan kompilasi APK tes Android sudah diverifikasi selama pengembangan. Sebelum penyerahan, coba pemuatan halaman kedua untuk film dan ulasan, putar trailer nyata, serta cek retry setelah koneksi dipulihkan. Untuk fitur tambahan, verifikasi juga bahwa ketukan notifikasi FCM membuka detail film yang tepat di HP fisik.

## Atribusi

This product uses the TMDB API but is not endorsed or certified by TMDB.
