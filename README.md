# What The Scribble (Co Za Bazgroł)

[![Android SDK](https://img.shields.io/badge/SDK-26%2B-brightgreen.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.x-blue.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-purple.svg)](https://developer.android.com/jetpack/compose)

**What The Scribble** (*Co Za Bazgroł*) is a modern, fast-paced pass-and-play drawing and guessing party game for Android, inspired by the classic paper-and-pen "telephone pictionary" (*głuchy telefon rysunkowy*). 

Designed for **3 or more players on a single shared device**, the game challenges players to draw words, pass the phone, redraw sketches from memory, and try to guess the original word at the very end—resulting in hilarious misunderstandings and fun recaps!

---

## 🎮 Core Gameplay Loop

1. **Player Registration:** Setup 3+ players and configure game timers (drawing limits and preview/peek limits).
2. **Word Choice:** The first player chooses a random Polish word or inputs a sanitized custom phrase.
3. **The First Sketch:** The player draws the word on the canvas before the timer expires.
4. **Pass & Cover:** The phone is handed over to the next player with a cover screen protecting the drawing from peeking eyes.
5. **Memory Phase (Redraw):** Subsequent players get a brief look at the *previous* player's drawing, and must reproduce it entirely from memory.
6. **The Final Guess:** The last player inspects the final drawing and attempts to guess the original word.
7. **Game Summary:** A comprehensive gallery recap reveals the entire evolution of the word, the sequence of sketches, and the final guess.

---

## ✨ Features

- **Single-Device Pass & Play:** Seamless handoff screens tailored for group party environments.
- **Advanced Canvas Engine:**
  - Smooth gesture tracking with continuous pointer paths.
  - **Hardware Layer Blending Eraser:** Uses an offscreen compositing strategy (`BlendMode.Clear`) to ensure eraser strokes cleanly wipe drawings without affecting the underlying backgrounds.
  - **Aspect-Ratio Scaling:** Smart stroke and canvas resolution recording ensuring drawings scale perfectly and look consistent on any device screen or recap summary.
- **Premium Stylus / S-Pen Hardware Integration:**
  - Instant tool switching via primary/secondary hardware buttons.
  - Automatic eraser activation using physical stylus erasers (`TOOL_TYPE_ERASER`).
  - Active hover path detection and live visual cursor guides with a high-contrast dual outline (dark outer, white inner) for perfect precision.
- **Persistent Preferences:** Game configurations (such as drawing and peek timers) are automatically persisted across sessions using a robust repository layer.
- **Modern Input Handling:** Comprehensive IME / software keyboard inset tracking that prevents layout clipping and keeps buttons floating cleanly above the keyboard.
- **Localized Experience:** UI fully localized into Polish for an immersive party game atmosphere, while keeping an idiomatic English codebase under the hood.

---

## 🛠️ Tech Stack & Architecture

- **Language:** Kotlin 2.x (compiled with Java 17 compatibility).
- **UI Architecture:** 100% Jetpack Compose with Material Design 3.
- **Navigation:** Modern Jetpack Navigation 3 (`navigation3-runtime` & `navigation3-ui`) utilizing strongly-typed, serializable destination routes.
- **State Management:** Unidirectional Data Flow (UDF) powered by MVVM (`GameViewModel`) exposing state observables directly optimized for Compose.
- **Build System:** Gradle Kotlin DSL (`.gradle.kts`) with a centralized Version Catalog (`libs.versions.toml`).
- **Testing Infrastructure:** Robust automated unit test coverage using JUnit 4 and `kotlinx-coroutines-test` for game state machines and word sanitization.

---

## 🚀 Getting Started

### Prerequisites
- Android Studio (Ladybug or newer recommended)
- Android SDK 37 (Compile/Target SDK)
- JDK 17

### Building and Running
You can easily build and test the application directly from your terminal using the Gradle wrapper:

*   **Build the Debug APK:**
    ```shell
    .\gradlew.bat assembleDebug
    ```
*   **Run Unit Tests:**
    ```shell
    .\gradlew.bat testDebugUnitTest
    ```
*   **Install on Connected Device/Emulator:**
    ```shell
    .\gradlew.bat installDebug
    ```
*   **Clean Build Artifacts:**
    ```shell
    .\gradlew.bat clean
    ```

---

## 📂 Project Structure

```
app/src/main/java/pl/mmorus/whatthescribble/
├── MainActivity.kt        # Application Entry point, Edge-to-Edge setup & Navigation 3 Host
├── data/
│   └── TimingPreferencesRepository.kt  # Persistent settings management
├── navigation/
│   └── NavKeys.kt         # Type-safe Serializable Navigation 3 routes
└── ui/
    ├── GameViewModel.kt   # Centralized state machine & game loop business logic
    ├── DrawingCanvas.kt   # Interactive drawing canvas with S-Pen/Stylus interop
    ├── theme/             # Material 3 Color Schemes, Typography, and Themes
    └── *Screen.kt         # Feature-specific Screen Composables (Menu, Setup, Game, Guess, Summary)
```
