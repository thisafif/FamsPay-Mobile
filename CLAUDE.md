# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test Commands

- Build debug APK: `./gradlew assembleDebug`
- Install debug APK: `./gradlew installDebug`
- Run unit tests: `./gradlew test`
- Run instrumented tests: `./gradlew connectedAndroidTest`
- Run a single test class: `./gradlew test --tests "com.kelompoksepuluh.famspay.ExampleUnitTest"`
- Clean build: `./gradlew clean assembleDebug`

## Project Architecture

**FamsPay** — An Android app for family financial management in Indonesian language.

### Navigation Flow
```
MainActivity (Onboarding, 4 pages via ViewPager2)
  → AuthActivity (auth landing: Login / Register buttons)
      → LoginActivity
      → RegisterActivity
```

### Key Screens & Their State
- **MainActivity** — Onboarding carousel (Lottie animations + dot indicators). Navigates to AuthActivity on "Mulai".
- **AuthActivity** — Brand splash screen with Masuk/Daftar buttons. Currently routes to Login/Register activities.
- **LoginActivity** — Empty shell (layout placeholder, no form logic yet).
- **RegisterActivity** — Registration form (name, email, password fields + Google sign-in button). Skeleton Java class (no backend logic yet).

### Tech Stack
- **Language:** Java
- **UI:** XML layouts with ConstraintLayout + RecyclerView (ViewPager2 adapter)
- **Animations:** Airbnb Lottie 6.7.1 (`.lottie` raw resources)
- **UI Library:** Material 3 (NoActionBar theme)
- **Min SDK:** 25, Target SDK: 36
- **Gradle:** Version catalog (`gradle/libs.versions.toml`), AGP 9.0.1
- **Dependencies:** AppCompat, Activity, ConstraintLayout, Material, Lottie

### Design System
- **Colors:** `green` (#10B981), `white`, `black`, `gray` (#919191), `light_gray` (#F3F4F6) — defined in `res/values/colors.xml`
- **Typography:** Inter font family
- **Components:** Custom drawables for buttons (`bg_button_green`, `bg_button_white_outline`), input fields (`bg_input_field`), icons (`ic_person`, `ic_email`, `ic_lock`, `arrow_back`), onboarding indicators (`indicator_active`, `indicator_inactive`)
- **Button radius:** 32dp consistently across the app

### Project Structure
- `app/src/main/java/com/example/famspay/` — Java activities + adapters + models
- `app/src/main/res/layout/` — XML layout files (activity_*, item_*)
- `app/src/main/res/drawable/` — Custom drawable resources (button backgrounds, icons, indicators)
- `app/src/main/res/raw/` — Lottie animation JSON files (onboarding1-4, login.lottie, register.lottie)
- `app/src/main/res/values/` — colors.xml, strings.xml, themes.xml
