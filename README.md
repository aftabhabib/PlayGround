# PlayGround

<!-- omgithub:readme:start -->
## 🚀 Build, play, and remix with OMGithub

**Created using [OMGithub.com](https://omgithub.com).**

[![OMGithub](https://img.shields.io/badge/OMGithub-Open%20project-orange?style=for-the-badge)](https://omgithub.com/aftabhabib/PlayGround)
[![GitHub](https://img.shields.io/badge/GitHub-Source-181717?logo=github&style=for-the-badge)](https://github.com/aftabhabib/PlayGround)

- 🎮 [Open the project](https://omgithub.com/aftabhabib/PlayGround).
- ✨ [Remix this project](https://omgithub.com/?remix=aftabhabib%2FPlayGround).
- 💻 [Explore the source](https://github.com/aftabhabib/PlayGround).
- 🛠️ [Check build runs](https://github.com/aftabhabib/PlayGround/actions).
- 🐛 [Report an issue](https://github.com/aftabhabib/PlayGround/issues).
- 👤 [Explore the creator's projects](https://omgithub.com/aftabhabib).
- 🌍 [Create with OMGithub](https://omgithub.com).
<!-- omgithub:readme:end -->

---

# Desi Meals — Mess Management, Accounting & Expense Tracking (Android)

Native Android app (Kotlin + Jetpack Compose + Material 3 + Room) for mess / tiffin / hostel food businesses:
customers, debit/credit ledgers, meal billing, expenses, suppliers, cashbook & bank, date-filtered
reports with PDF/Excel/CSV export, currency selection, light/dark themes, backup & restore.

## Build (verified)

- SDK: `/usr/local/lib/android/sdk` (`sdk.dir` in `local.properties`), compile/target 34, min 26, Java 17
- Gradle wrapper: `8.7`, AGP `8.5.2`, Kotlin `2.0.21`, Compose BOM `2024.10.00`, Room `2.6.1`

```bash
./gradlew :app:assembleDebug      # APK -> app/build/outputs/apk/debug/app-debug.apk (19 MB, verified)
./gradlew :app:testDebugUnitTest  # 9 unit tests, all pass (AccountingTest)
```

Status: `BUILD SUCCESSFUL`, 9/9 tests pass, debug APK builds and installs.

## What's implemented (all requested modules)

- **Dashboard**: received / paid-out / outstanding / expenses / advance, today + month figures, customer
  counts, Today/Week/Month/Year filter, pending dues, recent activity, quick actions.
- **Customers**: add/edit/deactivate, search by name/ID/mobile, status filter, profile page, monthly bill
  generation, statement share (PDF).
- **Ledger (debit/credit)**: per-customer chronological ledger with running balance; debit increases amount
  owed, credit reduces it; advance = negative balance; labels Amount Due / Advance Balance / Settled;
  duplicate-payment guard (2-min window); corrections via new reversing entries (no silent deletes).
- **Meals & billing**: meal plans, daily present/absent entries, extra charges, monthly bill generation with
  duplicate-period protection, statuses Draft/Unpaid/Partially Paid/Paid/Overdue/Cancelled.
- **Expenses**: 16 default categories + custom, paid/unpaid tracking, search + category filter, top-category
  summary, cashbook mirroring for paid expenses, PDF/Excel/CSV via Reports.
- **Cashbook & accounts**: cash/bank accounts with opening balances, IN/OUT entries, transfers (excluded
  from income/expense), daily totals, running balances.
- **Suppliers**: bills vs. payments kept separate (no double-count), outstanding = opening + billed − paid.
- **Reports**: profit & loss (income = recognized meal charges + cash sales; payments reduce receivables,
  never double-counted), income, expense (category/vendor/paid-unpaid), receivables, payables, cash flow,
  meal revenue, overdue, customer list — all with Today/Yesterday/This-Last week/month/year presets,
  business name + range + timestamp on every report, PDF (platform PdfDocument) / XLSX (dependency-free
  writer) / CSV export + Android share sheet.
- **Settings**: business profile, searchable currency list (PKR/INR/USD/EUR/GBP/AED/SAR/CAD/AUD + more;
  display-only, never silently converts stored amounts), light/dark/system theme, date format, decimals,
  default payment method, PIN (optional), per-category notification toggles, backup/restore with SQLite
  header validation + pre-restore warning + last-backup stamp.
- **About/Privacy**: description, version, configurable owner info, no fake links.
- **Accounting tests**: `AccountingTest` covers balance formula, advance, labels, running balances, profit,
  supplier outstanding, invoice totals, no-double-count income.

## Project layout

```
settings.gradle.kts / build.gradle.kts / gradle.properties / gradlew (8.7)
app/build.gradle.kts (AGP 8.5.2, KSP/Room, Compose, DataStore, WorkManager)
app/src/main/AndroidManifest.xml
app/src/main/java/com/desimeals/app/
  MainActivity.kt, DesiMealsApp.kt
  data/db/ (Entities.kt, Daos.kt, DesiDatabase.kt)  data/Repository.kt
  util/ (Accounting.kt, Currency.kt, Dates.kt, SettingsStore.kt, Exporters.kt, BackupHelper.kt)
  worker/ReminderWorker.kt
  ui/theme/Theme.kt  ui/navigation/ (Routes.kt, NavGraph.kt)
  ui/components/Common.kt  ui/viewmodel/AppViewModel.kt
  ui/screens/ (Splash, Dashboard, Customers, Meals, Transactions, Expenses, Reports, Settings)
app/src/test/.../AccountingTest.kt (9 tests)
```
