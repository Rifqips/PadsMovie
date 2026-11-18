# PadsMovie

Aplikasi Android untuk menemukan film berdasarkan genre, melihat detail, membaca ulasan, dan menonton trailer YouTube. Metadata dan gambar berasal dari TMDB.

## Menjalankan aplikasi

- Android Studio dengan JDK 17 atau lebih baru.
- Android SDK 36; perangkat atau emulator Android 8.1 (API 27) atau lebih baru.
- Gradle wrapper 8.13, AGP 8.13.2, Kotlin 2.3.10.

Tambahkan API Read Access Token TMDB ke Gradle properties pengguna, di luar repository:

```properties
TMDB_READ_ACCESS_TOKEN=your_read_access_token
```

Di Windows, file tersebut berada di `%USERPROFILE%/.gradle/gradle.properties`. Jangan memakai API key v3 sebagai pengganti Read Access Token. Setelah mengubah token, build ulang aplikasi karena nilainya dimasukkan ke BuildConfig.

Buka proyek, lakukan Gradle sync, pilih konfigurasi app, lalu Run. Jika token kosong atau ditolak, layar menampilkan petunjuk setup.

## Alur aplikasi

1. Pilih genre pada layar awal.
2. Daftar film menampilkan poster dan judul; scroll untuk memuat halaman berikutnya.
3. Pilih film untuk melihat tanggal rilis, durasi, rating, dan sinopsis.
4. Buka Ulasan untuk membaca ulasan dalam bahasa aslinya, atau Tonton trailer untuk membuka pemutar di aplikasi.
5. Jika video tidak dapat diputar dalam aplikasi, gunakan Buka di YouTube.

Tombol kembali mempertahankan daftar sebelumnya. Tema serta bahasa mengikuti perangkat. Teks UI tersedia dalam bahasa Inggris dan Indonesia. Trailer mencoba en-US jika trailer pada bahasa perangkat tidak tersedia.

## Struktur

| Modul | Tanggung jawab |
| --- | --- |
| :app | Navigation Compose, layar, ViewModel, PagingSource, konfigurasi Hilt |
| :core:domain | Model Kotlin dan kontrak MovieRepository |
| :core:data | Retrofit/OkHttp, DTO, mapping dan autentikasi TMDB |
| :core:designsystem | Tema Material 3, string lokal, komponen loading/error/retry |

Hilt menyediakan repository singleton ke activity. Setiap destination membuat ViewModel yang mengikuti lifecycle back stack melalui factory; constructor menerima repository sehingga tes dapat memakai fake. Data film dan ulasan disimpan dengan cachedIn(viewModelScope). Status trailer terpisah dari detail film.

Aplikasi mengambil data langsung dari jaringan, tanpa cache database atau mode offline. Error first load menyediakan retry; error append mempertahankan item lama dan menyediakan retry di footer. Discover dibatasi sampai halaman 500 sesuai batas TMDB.

## Verifikasi

Windows:

```powershell
.\gradlew.bat :app:assembleDevelopmentDebug :app:lintDevelopmentDebug :app:testDevelopmentDebugUnitTest :core:data:test
.\gradlew.bat :app:connectedDevelopmentDebugAndroidTest
```

Perintah kedua membutuhkan emulator atau perangkat terhubung. Unit test dan tes UI memakai data palsu atau MockWebServer, sehingga tidak memerlukan token TMDB. Workflow Android checks menjalankan build, lint dan unit test untuk pull request.

Cakupan tes mencakup parameter request dan mapping, token kosong/ditolak, fallback trailer, pagination awal/akhir/kosong, kegagalan append dan retry, serta navigasi Genre → Film → Detail → Ulasan.

Pemeriksaan manual sebelum submission:

- Coba dua genre berbeda dan halaman kedua daftar film.
- Kembali dari detail dan periksa posisi scroll.
- Coba offline pada first load dan append, lalu sambungkan jaringan dan retry.
- Periksa judul dengan ulasan kosong, trailer kosong, dan poster yang gagal dimuat.
- Putar trailer nyata; periksa fallback YouTube dan penghentian playback saat aplikasi masuk background.
- Coba tema gelap, bahasa Indonesia/Inggris, rotasi, dan font sistem besar.

## Varian build

| Varian | Application ID |
| --- | --- |
| `developmentDebug` | `id.application.padsmovie.dev` |
| `stagingRelease` | `id.application.padsmovie.staging` |
| `productionRelease` | `id.application.padsmovie` |

Pilih `developmentDebug` di panel Build Variants untuk pengembangan harian. Kedua varian release belum dikonfigurasi dengan signing key.

## Pengembangan berikutnya

Roadmap lengkap tersedia di [readme.txt](readme.txt). Notifikasi FCM, autentikasi, billing, backend verifikasi langganan, dan streaming masih menjadi milestone berikutnya.

## Banner Remote Config

Gradle membaca `app/src/<flavor>/google-services.json` atau `app/google-services.json`. Client harus cocok dengan Application ID varian. Varian tanpa client yang cocok menampilkan katalog tanpa banner.

Di Firebase Remote Config, siapkan parameter `banner_enabled` (Boolean, default false), `banner_title` (String), `banner_image_url` (URL HTTPS), dan `banner_movie_id` (Number, ID TMDB positif). Aktifkan banner setelah semua nilai terisi. Ketika layar genre dibuka, aplikasi membaca konfigurasi aktif lalu mencoba mengambil konfigurasi terbaru. Jika fetch gagal, konfigurasi aktif terakhir masih dapat dipakai.

## Atribusi

This product uses the TMDB API but is not endorsed or certified by TMDB.

[TMDB](https://www.themoviedb.org) menyediakan metadata dan gambar; halaman Tentang aplikasi memuat logo dan atribusi. Trailer diputar melalui YouTube.
