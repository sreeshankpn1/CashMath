# CashMath — Engineering & Architecture Development Guide

## 1. Project Overview & Identity
- **App Name**: CashMath (formerly RupeeMath)
- **Application ID**: `com.cashmath.app` (or configured namespace)
- **Core Mission**: 100% Private, Offline-First Smart Digital Cashbook, Customer Khata, and Loan EMI Tracker designed for Indian small business owners, contractors, and freelancers.
- **Compliance**: Fully compliant with India's Digital Personal Data Protection (DPDP) Act, 2023. Core transactions are processed and stored on-device.

---

## 2. Mandatory Rule for AI Agents
**CRITICAL INSTRUCTION**: Whenever you add, refactor, or modify any feature, database entity, screen, or architecture pattern in this repository, you MUST update the corresponding section in this `DEV_GUIDE.md` file before finishing your task. Keep this document as the living, accurate source of truth for the project.

---

## 3. Tech Stack & Architecture Standards
- **Platform & Language**: Native Android with Kotlin 2.0+.
- **UI Framework**: 100% Jetpack Compose with Material Design 3.
- **Architecture Pattern**: MVVM (Model-View-ViewModel) with Clean Architecture principles (UI, Domain, Data layers).
- **Local Persistence**: Room Database (SQLite) with reactive Kotlin `Flow`s, TypeConverters (for JSON lists), and offline-first single source of truth.
- **Cloud Layer**: Firebase Firestore & Firebase Auth (Google Sign-In / Phone OTP) as an optional sync layer.
- **Background Automation**: Android `WorkManager` (Daily Business Pulse at 9:00 PM) + `NotificationListenerService` (on-device UPI notification parsing for GPay, PhonePe, Paytm, BHIM, and bank SMS).
- **Hardware Security**: `BiometricPrompt` (Fingerprint / Face / Device PIN) for app launch lock.

---

## 4. Design System & UI Principles (CRED & Apple Style)
- **Color Palette**:
  - Background (Deep Obsidian): `Color(0xFF0A0E17)`
  - Cards & Surfaces: `Color(0xFF141A28)`
  - Inset Borders: `1.dp` solid `Color(0xFF222C42)`
  - Primary Accent (Cash In / Success): Neon Emerald `Color(0xFF00E676)`
  - Secondary Accent (Cash Out / Danger): Neon Coral `Color(0xFFFF5252)`
  - Administrative / Operators: Royal Blue `Color(0xFF5E9CFF)`
  - Viewer / Warnings: Amber Gold `Color(0xFFFFB300)`
  - Labels & Subtitles: Slate Gray `Color(0xFF8E9BB0)`
  - Primary Text: Crisp White `Color.White` (Dark) / Deep Charcoal `Color(0xFF111827)` (Light)
- **Geometry**: Continuous rounded corners (`14.dp` to `18.dp`).
- **Motion & Physics**:
  - Spring physics (`Spring.DampingRatioLowBouncy`, `Spring.StiffnessMediumLow`) on all bottom sheets, modals, and dialogs.
  - Scale press micro-interactions (`0.92f–0.94f`) with haptic feedback on buttons and keypad keys.
- **Input & Keyboard Harmony**:
  - Mutually exclusive inputs: The in-app number pad must smoothly collapse when text fields ("Remarks", "Party Name") gain focus.
  - The software QWERTY keyboard must never overlap or collide with custom number pads.
  - Unified calculator: Arithmetic operators, GST chips (`+5%`, `+12%`, `+18%`), and dedicated `%` key live inside the keypad area (no nested sheets).

---

## 5. Feature Implementation Rules
1. **Attachments (Up to 3)**:
   - When picking images/receipts via `PickMultipleVisualMedia(maxItems = 3)`, immediately copy files into app-private storage (`context.filesDir/receipts/`) via `FileStorageHelper`.
   - Never store raw ephemeral `content://` URIs in Room to prevent permission expiration.
   - Display horizontal thumbnail gallery with an in-app fullscreen viewer.
2. **Contact Book Picker (Zero-Permission)**:
   - Use `Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)` to extract both Name and Phone number simultaneously without requiring `READ_CONTACTS`.
3. **WhatsApp Reminders**:
   - Strict separation between "To Receive" (Collection) and "To Pay" (Settlement) templates.
   - No raw UPI links or mention of attached statements in standalone text reminders.
4. **Team & Granular Book Access**:
   - Unified `BusinessMemberEntity` with roles: `OWNER`, `ADMIN`, `STAFF`, `VIEWER`.
   - Support scoping members to "All Books" or "Specific Books Only".
   - Support 6-digit invite code connecting outside members via Firebase.
5. **PDF & Excel Statement Exports**:
   - Clean, standardized filenames: `<CleanName>_Statement_<Date>.pdf`.
   - Header must display Business Name, Phone, GSTIN, and Address if configured.

---

## 6. Code Quality & Lint Guidelines
- Do not import `android.R` globally; use fully qualified names (`android.R.drawable.ic_...`) or project resources (`R.drawable.ic_...`).
- Use Kotlin KTX extension `sharedPreferences.edit { ... }`.
- Use modern `java.time.Duration` for WorkManager delays.
- Wrap all ContentResolver and cursor queries in strict `try-catch` blocks with null safety.
- Maintain `minSdk = 24` with Java Core Library Desugaring enabled for universal compatibility across older phones, flagships, and tablets.
