# group-6-android-app-dev

A lightweight Android app built by Group 6 with Android Studio.

## Tech stack

| Item | Version |
| --- | --- |
| Language | Java 11 |
| Build | Gradle 9.6.0 (wrapper), Android Gradle Plugin 9.4.1, Kotlin DSL |
| SDK | minSdk 24 / targetSdk 37 / compileSdk 37 |
| UI | AppCompat, Material Components, ConstraintLayout |

Dependency versions are managed centrally in [`gradle/libs.versions.toml`](gradle/libs.versions.toml).

## Getting started

1. Clone the repo:
   ```bash
   git clone https://github.com/Neilwwz3/group-6-android-app-dev.git
   ```
2. Open the folder in Android Studio (**File > Open**) and let Gradle sync.
   Android Studio creates `local.properties` with your SDK path automatically. Do not commit it.
3. Pick an emulator or device and press **Run**.

Command-line build:

```bash
./gradlew assembleDebug        # build debug APK -> app/build/outputs/apk/debug/
./gradlew test                 # unit tests
./gradlew connectedAndroidTest # instrumented tests (needs a device/emulator)
```

## Project structure

```
.
├── app/
│   ├── build.gradle.kts              # app module config (SDK levels, dependencies)
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/hk/hkucs/group_6_android_app_dev/
│       │   │   └── MainActivity.java # entry screen
│       │   ├── res/
│       │   │   ├── layout/           # screen layouts (XML)
│       │   │   ├── values/           # strings, colours, themes
│       │   │   ├── values-night/     # dark theme overrides
│       │   │   ├── drawable/         # vector images / shapes
│       │   │   └── mipmap-*/         # launcher icons
│       │   └── keepRules/            # R8 keep rules for release builds
│       ├── test/                     # local JVM unit tests
│       └── androidTest/              # instrumented tests (run on device)
├── gradle/
│   ├── libs.versions.toml            # version catalog (all dependency versions)
│   └── wrapper/                      # Gradle wrapper, do not edit by hand
├── build.gradle.kts                  # root build file
├── settings.gradle.kts               # module list and repositories
└── gradle.properties                 # Gradle JVM / build flags
```

As the app grows, group code into packages under `hk.hkucs.group_6_android_app_dev`, for example:

```
ui/       # Activities, Fragments, Adapters
model/    # data classes
data/     # storage, network, repositories
util/     # small helpers
```

## Workflow

- `main` should always build. Work on a feature branch (`feature/<name>`) and open a pull request to merge.
- Pull before you start: `git pull --rebase`.
- Add new libraries through `gradle/libs.versions.toml`, not hard-coded versions in `build.gradle.kts`.
- Put user-facing text in `res/values/strings.xml` instead of hard-coding it in layouts or Java.
- Never commit `local.properties`, keystores (`*.jks`, `*.keystore`) or build outputs; `.gitignore` already covers these.

## License

[MIT](LICENSE)
