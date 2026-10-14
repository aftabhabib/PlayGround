# Desi Meals — Mess Accounting & Bookkeeping (Native Android)

Offline-first mess management app built with Kotlin, Jetpack Compose (Material 3), MVVM, Room, Navigation Compose, Coroutines + Flow.

## Features
- **Dashboard**: total receivable, month net, billed vs received, recent activity
- **Members**: add/search/activate/deactivate/delete mess members with monthly rate
- **Member detail**: per-customer ledger, balance (billed − paid), one-tap monthly bill, meal history
- **Daily meals**: mark breakfast/lunch/dinner per active member per day (offline)
- **Ledger**: unified bookkeeping (CHARGE / PAYMENT / EXPENSE / INCOME) with type filters
- **Add entry**: member picker, validation, error + empty states everywhere
- **100% offline**: Room database `desi_meals.db`, no network permission needed

## Project structure
```
DesiMeals/
├── settings.gradle.kts
├── build.gradle.kts
├── gradle.properties
├── gradle/libs.versions.toml
├── gradle/wrapper/gradle-wrapper.properties
└── app/
    ├── build.gradle.kts
    ├── src/main/AndroidManifest.xml
    └── src/main/java/com/desimeals/app/
        ├── MainActivity.kt
        ├── DesiMealsApp.kt
        ├── di/AppContainer.kt
        ├── data/local/ (CustomerEntity, TransactionEntity, MealEntryEntity, DAOs, AppDatabase, Converters)
        ├── data/repository/ (CustomerRepository, LedgerRepository, MealRepository)
        ├── navigation/NavGraph.kt
        ├── ui/theme/ (Color, Type, Theme)
        ├── ui/components/ (Common, Cards)
        ├── ui/screens/ (Dashboard, Customers, CustomerDetail, Meals, Ledger, AddTransaction)
        ├── ui/viewmodel/ViewModels.kt
        └── util/Utils.kt
```

## Build & install APK
### Option A — Android Studio (recommended)
1. Open Android Studio → **Open** → select the `DesiMeals/` folder.
2. Let Gradle sync (AGP 8.5.2, Kotlin 2.0.20, compileSdk 34).
3. **Build → Build APK(s)** or run `app` on an emulator/device.
4. APK output: `app/build/outputs/apk/debug/app-debug.apk`.

### Option B — Command line
```bash
cd DesiMeals
gradle assembleDebug
# APK at: app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Requirements: JDK 17, Android SDK with platform 34 + build-tools 34.0.0.

## Release APK
```bash
gradle assembleRelease
# app/build/outputs/apk/release/app-release-unsigned.apk
# Sign it via Android Studio → Generate Signed Bundle/APK
```

## Data model
- `customers(id, name, phone, address, monthlyRate, isActive, createdAt)`
- `transactions(id, customerId?, type, amount, description, mealType, date)`
- `meal_entries(id, customerId, date[start-of-day], breakfast, lunch, dinner)`
