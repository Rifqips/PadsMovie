# PadsMovie

PadsMovie is a native Android app for exploring movies from The Movie Database (TMDB). Users can browse by genre, view movie details, read reviews, and watch YouTube trailers. The project was built for **Mandatory Task — Option 1**.

**The project owner defined the idea, scope, architecture, and product design direction. The app was implemented entirely using Codex as an AI agent, under the project owner's direction and review.**

## Mandatory requirements

| Requirement | Implementation |
| --- | --- |
| 1. Official movie genres | The home screen lists official TMDB movie genres from `/genre/movie/list`. |
| 2. Discover movies by genre | Selecting a genre opens a poster grid populated from `/discover/movie` with a genre filter. |
| 3. Primary movie information | Tapping a movie opens its details, including the title, release date, runtime, rating, synopsis, and artwork when available. |
| 4. User reviews | The **Reviews** button opens the review list for the selected movie. |
| 5. YouTube trailers | The detail screen opens an available trailer in the app and provides an option to open it on YouTube. |
| 6. Endless scrolling | Paging 3 loads more movies and reviews as users scroll. If a page fails to load, previously loaded items remain visible and users can retry. |
| 7. Positive and negative cases | The UI handles loading, success, empty results, and recoverable errors. Unit tests cover response mapping, TMDB authentication errors, trailer fallback, and pagination. |

The main review flow is **Genre → Movie list → Movie details → Reviews or Trailer**. The catalog is available without signing in. Firebase Remote Config banners, FCM notifications, and Firebase Authentication are additional features. The subscription screen is a placeholder.

## Architecture

The movie catalog follows Clean Architecture principles by separating domain contracts, data access, and presentation.

| Module | Responsibility |
| --- | --- |
| `:core:domain` | Defines domain models such as `Genre`, `MovieSummary`, and `Page`, along with the `MovieRepository` contract. It has no dependency on Android or Retrofit. |
| `:core:data` | Implements the TMDB repository, maps DTOs to domain models, and configures Retrofit and OkHttp. TMDB code is grouped into `tmdb.model`, `tmdb.network`, and `tmdb.repository`. |
| `:core:designsystem` | Provides the shared Material 3 theme and reusable Compose components. |
| `:app` | Contains screens, ViewModels, paging sources, navigation, and Hilt configuration. Feature code lives under `feature/<name>`, with separate packages for navigation and shared UI. |

```text
:app ──► :core:domain
     ├─► :core:data ──► :core:domain
     └─► :core:designsystem
```

ViewModels use the `MovieRepository` contract. Hilt supplies the TMDB implementation from `:core:data`. The repository calls `TmdbApi` and converts the results to domain models for the UI. Unit tests can inject a fake repository without calling TMDB.

## Tech stack

- **UI** — Kotlin, Jetpack Compose, Material 3, Navigation Compose, and Coil.
- **State and pagination** — Jetpack ViewModel, Kotlin Coroutines, Flow, and Paging 3.
- **Networking and dependency injection** — Retrofit, OkHttp, Gson, Dagger Hilt, and KSP.
- **Additional services** — Firebase Remote Config, Cloud Messaging, and Authentication.
- **Build and testing** — Gradle Kotlin DSL, a Gradle version catalog, JUnit, MockWebServer, Compose UI Test, and GitHub Actions.

## Getting started

1. Clone the repository and open it in Android Studio Rabit 2. Use JDK 17 and Android SDK 36. The app supports Android 8.1 (API 27) and later.
2. Get your own **TMDB API Read Access Token** and set `TMDB_READ_ACCESS_TOKEN` in your user-level Gradle properties file. On Windows, this file is typically at `%USERPROFILE%\.gradle\gradle.properties`. Keep your token outside the repository.

   ```properties
   TMDB_READ_ACCESS_TOKEN=your_read_access_token
   ```
3. Run Gradle Sync, select `developmentDebug` under **Build Variants**, and run the app on a physical device or emulator. The bundled Firebase configuration matches the production application ID. The TMDB catalog works in development builds without Firebase.

Run the build, unit tests, and lint without a device:

```powershell
.\gradlew.bat :app:assembleDevelopmentDebug :app:testDevelopmentDebugUnitTest :core:data:test :app:lintDevelopmentDebug
```

Run Compose UI tests on a connected device or emulator:

```powershell
.\gradlew.bat :app:connectedDevelopmentDebugAndroidTest
```

Builds, unit tests, lint, and Android test APK compilation were verified during development. Device checks are still pending for real trailer playback, second-page loading for movies and reviews, and retry after reconnecting. The optional FCM flow also needs a physical-device check to confirm that tapping a notification opens the correct movie.

## Attribution

This product uses the TMDB API but is not endorsed or certified by TMDB.
