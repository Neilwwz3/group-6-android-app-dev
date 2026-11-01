# group-6-android-app-dev

COMP7506 Smart Phone Apps Development, Group 6 project (HKU, 2026).

**Working title:** MovieTracker, a local-first app for rating and tracking the movies and TV shows you watch.

**Status:** we are researching the core user need (Nov 2–4). Features and the remaining task assignments will be added here after that.

## Key dates

| Date | Milestone |
| --- | --- |
| Thu, Nov 19, 2026 | In-class demo |
| Mon, Nov 30, 2026, 23:59 | Final submission: document, source code with this README, 1–2 minute video |

## Team

| Member (GitHub) | Role |
| --- | --- |
| Neilwwz3 | Research and coordination: user research, design decisions, task assignment, code review |
| TBD | TBD |
| TBD | TBD |
| TBD | TBD |

## Tech stack

| Item | Version |
| --- | --- |
| Language | Java 11, XML layouts |
| Build | Gradle 9.6.0 (wrapper), Android Gradle Plugin 9.4.1, Kotlin DSL |
| SDK | minSdk 24 / targetSdk 37 / compileSdk 37 |
| UI | AppCompat, Material Components, ConstraintLayout |

Dependency versions live in [`gradle/libs.versions.toml`](gradle/libs.versions.toml).

## Build and run

1. Clone the repo:
   ```bash
   git clone https://github.com/Neilwwz3/group-6-android-app-dev.git
   ```
2. Open the folder in Android Studio (**File > Open**) and wait for Gradle sync. Android Studio creates `local.properties` with your SDK path; do not commit it.
3. Pick an emulator or device and press **Run**.

From the command line (use `gradlew.bat` on Windows):

```bash
./gradlew assembleDebug   # debug APK -> app/build/outputs/apk/debug/
```

## Workflow

- `main` is protected. Nobody pushes to it directly; every change goes through a pull request approved by the coordinator.
- One task, one branch (`feature/<task-id>-<short-name>`), one pull request.
- Pull the latest `main` before you start work each day.
- Only change the files your task card allows. Ask before touching shared code: data models, database, navigation, themes.
- Ask before adding a library; add approved ones through `gradle/libs.versions.toml`.
- A pull request is ready when the app builds and you have attached a screenshot for each acceptance criterion.
- Never commit `local.properties`, keystores or build outputs; `.gitignore` already covers these.

## License

[MIT](LICENSE)
