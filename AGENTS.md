# Repository Guidelines

## Project Structure & Module Organization

FamsPay is a native Android project using Java and Gradle Kotlin DSL. The app module is `app/`.

- `app/src/main/java/com/kelompoksepuluh/famspay/`: Activities, fragments, helpers, and app logic.
- `app/src/main/java/com/kelompoksepuluh/famspay/data/model/`: API request/response models.
- `app/src/main/java/com/kelompoksepuluh/famspay/data/remote/`: Retrofit API client and service definitions.
- `app/src/main/java/com/kelompoksepuluh/famspay/utils/`: shared utilities such as formatting, API parsing, and session handling.
- `app/src/main/res/layout/`: XML screens, dialogs, and bottom sheets.
- `app/src/main/res/drawable/`, `mipmap-*`, `raw/`, `font/`: icons, launch assets, Lottie files, and fonts.
- `app/src/test/` and `app/src/androidTest/`: unit and instrumented tests.

Use `PROJECT_CONTEXT.md` as the business-rule source of truth. `CLAUDE.md` is an old draft and should not guide implementation.

## Build, Test, and Development Commands

Run commands from the repository root.

- `.\gradlew.bat assembleDebug`: builds a debug APK and validates compilation.
- `.\gradlew.bat test`: runs local JVM unit tests.
- `.\gradlew.bat connectedAndroidTest`: runs instrumented tests on a connected emulator/device.
- `.\gradlew.bat clean`: removes generated build output when Gradle state is stale.

On Unix-like shells, use `./gradlew` instead of `.\gradlew.bat`.

## Coding Style & Naming Conventions

Use Java for app code and XML for UI. Follow the existing Android style: 4-space indentation, `PascalCase` for classes, `camelCase` for fields/methods, and descriptive Android IDs such as `tvBalance`, `btnSubmit`, or `fragment_add_transaction.xml`.

Keep API contracts in `data/model` and `data/remote`; avoid hardcoding backend payloads directly in activities. Prefer small utilities in `utils` for repeated formatting or response parsing.

## Testing Guidelines

Add local tests under `app/src/test/java/...` for pure Java logic, especially utilities and request-building behavior. Add instrumented tests under `app/src/androidTest/java/...` only for UI or Android framework behavior.

Name tests after the behavior, for example `FormatUtilsTest` with `toApiDate_rejectsInvalidDisplayDate`. Run `.\gradlew.bat assembleDebug` before handing off changes; run `test` for non-trivial logic changes.

## Commit & Pull Request Guidelines

Recent commits use short summaries, often imperative and sometimes bilingual. Keep new commit messages feature-focused, for example:

- `implement transaction preview confirmation`
- `fix future date validation in transaction forms`

Pull requests should include a short summary, affected screens/features, commands run, and screenshots or screen recordings for UI changes. Link related issues or task notes when available, and call out any backend API assumptions.

## Security & Configuration Tips

Do not commit local secrets or machine-specific files such as `local.properties`. Treat `app/google-services.json` and API base URLs carefully; verify they are intended for the target environment before release builds.
