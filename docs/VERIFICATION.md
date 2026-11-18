# Verifikasi lokal

Pemeriksaan dilakukan pada Windows melalui Android Studio.

## Berhasil

- Gradle sync untuk modul app, domain, data, dan design system.
- Build debug.
- Lint debug.
- 24 unit test: 9 repository TMDB, 12 PagingSource, 2 ViewModel, dan 1 tes bawaan proyek.
- Kompilasi APK tes UI, termasuk tiga tes navigasi dan pemulihan error.

Perintah pemeriksaan yang berhasil:

```powershell
.\gradlew.bat :app:assembleDebug :app:lintDebug :app:testDebugUnitTest :core:data:test :app:assembleDebugAndroidTest --console=plain
```

## Pemeriksaan terakhir

Setelah perapian import, pengelolaan ImageLoader, dan identitas WebView YouTube, APK debug berhasil dibangun kembali. Pengulangan lint dan tes terhalang Windows yang mengunci:

```text
app/build/intermediates/compile_app_classes_jar/debug/bundleDebugClassesToCompileJar/classes.jar
The process cannot access the file because it is being used by another process
```

Percobaan dengan satu worker dan compiler in-process juga masih mengalami kunci file yang sama. Hasil lint dan 24 unit test di atas berasal dari pemeriksaan sukses sebelum perapian terakhir, bukan pengulangan suite terakhir.

## Belum diverifikasi

- Eksekusi tes UI: Android Studio melaporkan tidak ada perangkat aktif.
- Pemutaran trailer YouTube nyata dan fallback ke aplikasi YouTube.
- First load dan append saat offline pada perangkat.
- Posisi scroll setelah navigasi, rotasi, font besar, dan tampilan light/dark pada perangkat.
- Eksekusi workflow GitHub Actions dan pemeriksaan dari checkout bersih.

Daftar ini perlu dituntaskan sebelum submission atau membuat tag rilis.
