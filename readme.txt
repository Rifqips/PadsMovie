PadsMovie
Rencana pengembangan Android dan alur kerja GitHub

PadsMovie adalah konsep aplikasi Android untuk menemukan film berdasarkan
genre, melihat detail, membaca ulasan, dan menonton trailer YouTube. Versi
pertama memenuhi tujuh poin take-home dengan data dari TMDB. Dokumen ini
menjadi roadmap pengembangan. Setup dan arsitektur aktual tersedia di README.md.
Kode alur Phase 1 sudah ditambahkan. Checklist di bawah tetap menjadi panduan
verifikasi; pengujian perangkat dan persiapan submission harus diselesaikan
sebelum menandai seluruh milestone selesai.

ALUR DAN DESAIN

Genre → Daftar Film → Detail Film → Ulasan atau Trailer

Genre jadi layar pertama. Daftar film memakai grid poster. Detail menampilkan
judul, tahun rilis, rating, durasi, sinopsis, dan tombol trailer serta ulasan.
Layar ulasan punya daftar sendiri supaya pengguna bisa scroll lebih panjang.

Light mode memakai latar putih, permukaan biru muda untuk chip atau state
aktif, serta biru yang lebih pekat untuk tombol utama. Dark mode memakai
latar navy dengan aksen biru yang lebih terang. Atur warna lewat Material 3.
Contoh palet awal yang bisa disesuaikan setelah dilihat di emulator:

  Light   background #FFFFFF   surface #F4F8FF   primary #2563EB
  Dark    background #0B1220   surface #111C2E   primary #60A5FA

Gunakan string resources English dan Bahasa Indonesia. Versi pertama mengikuti
bahasa dan tema perangkat. Kirim locale yang sesuai untuk metadata TMDB dan
tampilkan isi ulasan dalam bahasa aslinya dari API. Format tanggal, angka,
dan pesan aksesibilitas ikut locale aplikasi.

TUJUH POIN MANDATORY

1. Genre resmi             GET /3/genre/movie/list
2. Film berdasarkan genre  GET /3/discover/movie?with_genres={genreId}&page={page}
3. Detail film             GET /3/movie/{movieId}
4. Ulasan pengguna         GET /3/movie/{movieId}/reviews?page={page}
5. Trailer YouTube         GET /3/movie/{movieId}/videos
6. Endless scrolling       Paging 3 pada daftar film dan daftar ulasan
7. Positive/negative cases Verifikasi sukses, kosong, gagal, dan retry

CLEAN ARCHITECTURE, MVVM, DAN HILT

  :app                 Navigation, Compose screen, ViewModel, PagingSource,
                       dan konfigurasi Dagger Hilt.
  :core:domain         Model Kotlin, kontrak MovieRepository, dan Page<T>.
  :core:data           Retrofit/OkHttp, DTO, mapping, dan implementasi
                       MovieRepository untuk TMDB.
  :core:designsystem   Tema Material 3 dan komponen UI yang dipakai bersama.

Dependency project
  :app → :core:domain, :core:data, :core:designsystem
  :core:data → :core:domain

Domain berupa modul Kotlin/JVM. Kontrak repository menerima genreId, movieId,
locale, atau nomor halaman sesuai kebutuhan. Page<T> membawa item dan
informasi halaman yang diperlukan oleh PagingSource. Tambahkan use case
ketika ada aturan yang dipakai ulang atau perlu diuji sendiri.

Alur data dimulai dari action di Compose. ViewModel memanggil kontrak
repository. Hilt memasang implementasi TMDB dari :core:data. Repository
memetakan DTO ke model domain sebelum hasilnya diterima ViewModel. Compose
menggambar ulang dari UiState atau PagingData yang diterbitkan ViewModel.

Di :app, pisahkan package genres, movies, detail, dan reviews. Composable
tingkat route mengambil state dari ViewModel. Screen menerima state dan
callback. GenreCard, MovieCard, dan ReviewCard tinggal di fitur masing-masing.
Loading, empty, dan error state yang dipakai lintas fitur masuk design system.

GITHUB BRANCHING DAN VERSI

  main                 Snapshot yang siap diberikan ke reviewer atau dirilis.
  develop              Cabang integrasi untuk milestone yang sedang dikerjakan.
  feature/<nama>       Perubahan fitur, dibuat dari develop. PR ke develop.
  fix/<nama>           Perbaikan pada versi yang sedang dikerjakan. PR ke develop.
  test/<nama>          Tambahan cakupan tes, dibuat dari develop. PR ke develop.
  release/<versi>      Dibuat dari develop saat scope versi selesai. Untuk
                       verifikasi, changelog, dan perubahan versi. PR ke main.
  hotfix/<nama>        Jika perlu memperbaiki versi yang sudah dirilis. Mulai
                       dari main, rilis patch, lalu sinkronkan ke develop.

Mulai dari initial commit di main lalu buat develop. Untuk tiap pekerjaan,
buat issue singkat berisi hasil yang diharapkan. Buat branch dari develop
terbaru, kerjakan satu perubahan yang utuh, lalu buka PR ke develop. Isi PR
dengan perubahan, cara mencoba, dan screenshot jika tampilan berubah. Squash
merge PR fitur agar riwayat develop mudah dibaca dan tutup issue terkait.

Contoh satu pekerjaan dari awal sampai PR:

  git switch develop
  git pull --ff-only origin develop
  git switch -c feature/genre-discovery
  git add app core
  git commit -m "feat: browse movies by genre"
  git push -u origin feature/genre-discovery

Set base PR ke develop dan tulis satu skenario yang bisa langsung dicoba,
misalnya pilih Action lalu pilih Animation dan cek hasilnya berbeda. Pakai
judul commit bahasa Inggris yang menjelaskan perubahan sebenarnya. Komentar
kode cukup untuk menjelaskan keputusan yang perlu konteks, seperti alasan
fallback trailer ke en-US. Nama fungsi dan tes menjelaskan alur normalnya.

Untuk submission, buat release/1.0.0 lalu verifikasi build. Setelah PR release
masuk ke main, tag commit rilis sebagai v1.0.0 dan buat GitHub Release jika
dibutuhkan. Bawa perbaikan dari release kembali ke develop sebelum lanjut.
Target berikutnya v1.1.0 untuk peningkatan berbasis feedback dan Firebase.
Naikkan versionCode saat mengunggah build baru untuk application ID yang
sama ke Google Play.

PHASE 1 — TAKE-HOME SAMPAI SUBMISSION

01. Project foundation — feature/project-foundation
[ ] Buat project Kotlin di Android Studio dengan Jetpack Compose, Material 3,
    Navigation Compose, dan empat modul di atas. Simpan versi dependency di
    version catalog serta catat JDK dan SDK yang benar-benar dipakai.
[ ] Tambahkan Dagger Hilt, Retrofit/OkHttp, Coil, dan Paging 3. Pastikan
    Gradle sync dan layar awal bisa dibuka di emulator.
[ ] Baca TMDB_READ_ACCESS_TOKEN dari Gradle properties lokal, teruskan dari
    konfigurasi app ke HTTP client sebagai Authorization: Bearer <token>.
    Sediakan pesan setup saat token kosong atau ditolak. Simpan token di
    luar repo dan gunakan fake repository untuk automated tests.
Selesai ketika debug build bisa dijalankan dari Android Studio.

02. Genre dan katalog — feature/genre-discovery
[ ] Ambil genre resmi dan tampilkan sebagai grid. Pilihan genre meneruskan
    genreId ke layar film dan mengambil halaman pertama dengan with_genres.
[ ] Tampilkan poster dan judul. Gunakan placeholder jika poster kosong atau
    gagal dimuat. Sediakan loading, empty state, dan retry.
Selesai ketika dua genre yang berbeda membuka hasil masing-masing.

03. Detail film — feature/movie-detail
[ ] Teruskan movieId dari kartu poster dan ambil informasi film. Tampilkan
    backdrop atau poster, judul, tahun, durasi, rating, dan sinopsis yang ada.
[ ] Cek navigasi kembali dari detail agar pengguna bisa melanjutkan scroll
    pada daftar film yang sama.
Selesai ketika informasi di detail sesuai film yang dipilih.

04. Ulasan dan trailer — feature/movie-reviews, feature/youtube-trailers
[ ] Buat layar ulasan dengan nama penulis, isi ulasan, dan rating jika ada.
[ ] Pilih video dengan site=YouTube dan type=Trailer. Utamakan trailer resmi.
    Coba en-US jika trailer dari locale aktif kosong. Jika tetap kosong,
    tampilkan keterangan bahwa trailer belum tersedia.
[ ] Putar video di aplikasi saat tombol trailer ditekan. Sediakan Buka di
    YouTube jika embed gagal dan URL tersedia. Pisahkan status trailer dari
    detail agar ulasan tetap bisa dibuka ketika video gagal.
Selesai ketika satu judul bisa menampilkan ulasan dan memutar trailer.

05. Pagination — feature/movies-and-reviews-paging
[ ] Pasang PagingSource terpisah untuk film dan ulasan. Mulai dari page 1,
    teruskan genreId atau movieId di setiap request, dan gunakan metadata
    API untuk menentukan kapan daftar selesai.
[ ] Simpan stream dengan cachedIn(viewModelScope). Tampilkan error first load
    di layar dan error append di footer. Retry halaman yang gagal sambil
    menjaga item lama tetap terlihat. Saat genre berubah, gunakan stream baru.
Selesai ketika kedua daftar bisa memuat halaman kedua dan berhenti di akhir.

06. UI, localization, dan testing — feature/theme-and-localization,
    test/submission-coverage
[ ] Selesaikan light/dark dan Indonesia/English. Coba layar dengan font
    sistem besar. Buat Compose Preview untuk kartu dan state utama.
[ ] Tambahkan TMDB attribution di About atau Credits dengan logo resmi
    dan notice yang diminta dalam panduan atribusi.
[ ] Uji PagingSource film dan ulasan dengan fake repository. Cek parameter,
    halaman berikutnya, halaman akhir, dan kegagalan append.
[ ] Buat tes navigasi Compose untuk Genre → Film → Detail → Ulasan.
[ ] Uji manual ketika offline saat first load dan append. Sambungkan jaringan
    lalu retry. Cek daftar kosong, poster gagal, ulasan kosong, dan trailer
    yang tidak tersedia atau gagal diputar di player yang tertanam.
[ ] Tambahkan check build, lint, dan unit test pada pull request lewat GitHub
    Actions. Jalankan tes UI di emulator untuk verifikasi submission.
Selesai ketika hasil sukses maupun gagal bisa diulang dari data uji.

07. Finishing — release/1.0.0
[ ] Siapkan satu genre dengan beberapa halaman film dan satu judul dengan
    trailer serta beberapa halaman ulasan. Rekam demo singkat bila diminta.
[ ] Verifikasi dari checkout bersih dengan instruksi setup yang bisa diikuti.
    Catat perintah yang benar-benar berhasil. Target sebelum ada flavor:

      ./gradlew :app:assembleDebug :app:lintDebug :app:testDebugUnitTest
      ./gradlew :app:connectedDebugAndroidTest

    Gunakan gradlew.bat di Windows. Untuk perintah kedua, hubungkan emulator
    atau perangkat yang akan dipakai untuk tes UI.
[ ] Periksa diff dan riwayat dari kredensial, file signing, serta log debug.
[ ] Ringkas bagian awal readme.txt untuk reviewer setelah implementasi jadi.
    Isi setup token, cara run, arsitektur aktual, dan skenario demo. Bagian
    Phase 2 bisa dihapus dari salinan submission untuk merampingkan dokumen.
[ ] Merge release ke main, tag v1.0.0, dan siapkan APK atau link repo sesuai
    format dari recruiter. Ulangi alur demo dari build yang akan dikirim.
Selesai ketika ketujuh poin bisa dicoba dari versi yang dikirim.

PHASE 2 — PENGEMBANGAN PORTOFOLIO SETELAH SUBMISSION

08. Build flavors — feature/environment-flavors
[ ] Tambah development, staging, dan production di flavor dimension
    environment. Gunakan suffix application ID untuk development dan staging
    agar keduanya bisa diinstal berdampingan dengan versi production.
[ ] Pisahkan konfigurasi environment dan daftarkan application ID yang sesuai
    ke Firebase. Gunakan developmentDebug untuk kerja harian, stagingRelease
    untuk QA, dan productionRelease untuk rilis.
[ ] Gunakan flavor Gradle saat menentukan target build di CI. Selaraskan
    kembali nama task build dan test di dokumentasi setelah flavor masuk.
    Buat build QA dan rilis dari commit yang sudah diuji. Branch mengatur
    alur perubahan kode, sedangkan flavor menentukan konfigurasi build.
Selesai ketika ketiga variant target bisa dibangun dan diverifikasi.

09. Banner promosi — feature/remote-config-banner
[ ] Pasang Firebase Remote Config. Mulai dari banner_enabled, banner_title,
    banner_image_url, dan banner_movie_id. Default banner dalam keadaan nonaktif.
[ ] Tampilkan banner di atas daftar genre saat konfigurasi valid. Tap banner
    membuka detail film yang dikonfigurasi. Uji keadaan aktif, nonaktif,
    data tidak lengkap, dan jaringan gagal.
Selesai ketika banner staging bisa diubah melalui Remote Config tanpa build baru.

10. Notifikasi film — feature/movie-notifications
[ ] Tambah Firebase Cloud Messaging. Kirim movieId di payload dan buka detail
    saat notifikasi ditekan. Tangani izin, payload rusak, dan film yang hilang.
[ ] Uji saat aplikasi terbuka, di background, dan saat aplikasi pertama kali
    dibuka dari notifikasi. Siapkan staging untuk demo dan rilis v1.1.0 jika siap.
Selesai ketika satu notifikasi uji membuka detail film yang tepat.

11. Prototipe streaming berlangganan — feature/subscription-access,
    feature/streaming-playback
[ ] Siapkan sumber video yang punya izin distribusi. Jika metadata atau gambar
    TMDB akan dipakai secara komersial, cek opsi lisensi dengan TMDB.
[ ] Buat login dan satu paket bulanan. Alurnya login → paywall untuk akun tanpa
    langganan aktif → katalog streaming untuk pelanggan aktif.
[ ] Integrasikan Google Play Billing dengan transaksi uji. Backend memverifikasi
    pembelian dan menentukan akses, termasuk saat film dibuka dari notifikasi.
[ ] Putar video dengan Media3 dan simpan progres lokal untuk Lanjutkan Menonton.
    Uji transaksi pending, batal, akses aktif, langganan berakhir, dan pemulihan
    akses setelah instal ulang.
Selesai ketika pembelian uji membuka akses, video bisa diputar, dan progres
bisa dilanjutkan setelah aplikasi dibuka lagi.

REFERENSI

TMDB API dan atribusi
https://developer.themoviedb.org/reference/intro/getting-started
https://developer.themoviedb.org/docs/authentication-application
https://developer.themoviedb.org/docs/faq

Android Paging dan build variants
https://developer.android.com/topic/libraries/architecture/paging/v3-overview
https://developer.android.com/build/build-variants

YouTube embedded player dan verifikasi Play Billing
https://developers.google.com/youtube/iframe_api_reference
https://developer.android.com/google/play/billing/security

GitHub branches dan pull request
https://docs.github.com/en/pull-requests/reference/branches
