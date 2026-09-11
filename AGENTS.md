# AGENTS.md - What The Scribble ("Co Za Bazgroł")

Context and quick-start guide for AI agents and developers working on the What The Scribble Android repository.

---

## 1. Project Overview

- **Name:** What The Scribble (Polish UI title: *Co Za Bazgroł*)
- **Type:** Pass-and-play drawing & guessing party game for Android (digital "telephone pictionary" / "głuchy telefon rysunkowy").
- **Target Audience / Locale:** Polish UI strings (`app/src/main/res/values/strings.xml`), English codebase and identifiers.
- **Core Gameplay Loop:**
  - Designed for **3+ players** using a **single shared phone**.
  - **Player Setup:** Register players (minimum 3); configure drawing timer (30–90s) and peek timer (5–20s).
  - **Word Selection:** First player chooses a random Polish word or enters a custom word (sanitized to prevent emojis/symbols, max 30 chars).
  - **First Drawing:** First player draws the word on the canvas within the time limit.
  - **Pass Phone (Handoff):** Phone is passed to the next player with a cover screen to prevent peeking.
  - **Redraw / Memory Phase:** Intermediate players get a brief preview (e.g., 10 seconds) of the previous player's sketch, then must redraw it from memory.
  - **Final Guess:** The last player inspects the final drawing and types their guess for the original word.
  - **Summary & Story:** Recap screen reveals the initial word, the step-by-step evolution of sketches, and the final guess.

---

## 2. Tech Stack & Environment

- **Language:** Kotlin 2.x
- **Compatibility:** Java 11 bytecode (`JavaVersion.VERSION_11`)
- **Android SDK:**
  - `minSdk`: 26
  - `compileSdk`: 37
  - `targetSdk`: 37
- **UI Toolkit:** Jetpack Compose with Material Design 3 (`androidx.compose.material3`).
- **Navigation:** Modern Jetpack Navigation 3 (`androidx.navigation3:navigation3-runtime`, `navigation3-ui`).
- **State Management:** MVVM with `GameViewModel` exposing Compose State objects (`mutableStateOf`, `mutableIntStateOf`, `mutableStateListOf`).
- **Build System:** Gradle Kotlin DSL (`build.gradle.kts`) with version catalog (`gradle/libs.versions.toml`).
- **Testing:** JUnit 4 + `kotlinx-coroutines-test` for ViewModel unit testing.

---

## 3. Directory Structure

```
S:\WhatTheScribble\android\
├── AGENTS.md                          # This context file
├── README.md                          # Project readme
├── build.gradle.kts                   # Root build configuration
├── settings.gradle.kts                # Project settings & plugin repositories
├── gradle/libs.versions.toml          # Dependency version catalog
└── app/
    ├── build.gradle.kts               # App module build configuration
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml
        │   ├── java/com/mmosiur/whatthescribble/
        │   │   ├── MainActivity.kt    # Entry Activity, edge-to-edge, NavDisplay host
        │   │   ├── data/
        │   │   │   └── TimingPreferencesRepository.kt # Persistent timing preferences (SharedPreferences)
        │   │   ├── navigation/
        │   │   │   └── NavKeys.kt     # AppNavKey sealed interface (Navigation 3 routes)
        │   │   └── ui/
        │   │       ├── GameViewModel.kt      # Central game state, turn machine, timer & sanitization
        │   │       ├── DrawingCanvas.kt      # Canvas with pointer gestures & pressure support
        │   │       ├── MainMenuScreen.kt     # Landing screen & rules dialog trigger
        │   │       ├── PlayerSetupScreen.kt  # Player roster configuration & IME handling
        │   │       ├── PassPhoneScreen.kt    # Turn handoff screen & custom word input
        │   │       ├── GameScreen.kt         # Drawing screen, palette, stroke width & timer
        │   │       ├── GuessScreen.kt        # Final player's guessing screen
        │   │       ├── SummaryScreen.kt      # Turn-by-turn evolution & gallery recap
        │   │       ├── TimingConfigDialog.kt # Modal to set draw/preview durations
        │   │       └── theme/                # Theme, Color, Type definitions
        │   └── res/
        │       └── values/
        │           ├── strings.xml           # User-facing Polish strings
        │           ├── colors.xml
        │           └── themes.xml
        └── test/
            └── java/com/mmosiur/whatthescribble/ui/
                └── GameViewModelTest.kt      # Unit tests for game logic & sanitization
```

---

## 4. Key Developer Workflows & Commands

All commands can be run from `S:\WhatTheScribble\android` via PowerShell:

- **Build Debug APK:**
  ```powershell
  .\gradlew.bat assembleDebug
  ```
- **Run Unit Tests:**
  ```powershell
  .\gradlew.bat testDebugUnitTest
  ```
- **Install on Connected Device / Emulator:**
  ```powershell
  .\gradlew.bat installDebug
  ```
- **Clean Project:**
  ```powershell
  .\gradlew.bat clean
  ```

---

## 5. Architectural & Implementation Details

### Navigation 3 (`NavDisplay`)
- Routes are strongly typed using `@Serializable` data objects implementing `AppNavKey`:
  - `MainMenu`, `PlayerSetup`, `PassPhone`, `Drawing`, `GuessPhase`, `Summary`, `Rules`.
- `MainActivity` manages backstack via `rememberNavBackStack(AppNavKey.MainMenu)` and provides composable destinations in `NavDisplay`.

### State & Game Progression (`GameViewModel`)
- Models:
  - `DrawnPoint(val offset: Offset, val pressure: Float)`: Stores coordinates and pressure.
  - `Line(val points: List<DrawnPoint>, val color: Color, val strokeWidth: Float, val isEraser: Boolean)`: A continuous stroke or eraser path.
  - `GameTurn(val playerName: String, val word: String?, val drawing: List<Line>, val canvasWidth: Float, val canvasHeight: Float)`: Turn snapshot for summary reproduction.
- Word Sanitization:
  - Input is sanitized via `GameViewModel.sanitizeWord(input)`:
    - Retains only letters (including Polish diacritics `ąćęłńóśźż`), digits, spaces, hyphens `-`, and apostrophes `'`.
    - Automatically strips emojis, surrogate pairs, and special punctuation.
    - Caps word length at `GameViewModel.MAX_WORD_LENGTH` (30 characters).
- Persistent Timing Settings (`TimingPreferencesRepository`):
  - Timing settings (`drawDuration` in range 15–300s, default 60s; `peekDuration` in range 3–60s, default 10s) are persisted across app sessions and games via `SharedPreferencesTimingPreferencesRepository`.
  - Accessible via a compact bar and top-bar button on `PlayerSetupScreen` as well as the timing card on `MainMenuScreen`, both opening `TimingConfigDialog`.

### Canvas Engine & Eraser (`DrawingCanvas.kt`)
- Implemented with `Modifier.pointerInput(interactive)` and `awaitEachGesture`.
- **Hardware Layer Blending (Option B Eraser):** Canvas utilizes `graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)` so eraser strokes drawn with `BlendMode.Clear` cleanly wipe drawing strokes while preserving the underlying Surface and parent backgrounds.
- **S-Pen Hardware Integration:** Directly checks `MotionEvent.BUTTON_STYLUS_PRIMARY`, `BUTTON_SECONDARY`, `TOOL_TYPE_ERASER`, and Compose `PointerButtons`. Pressing the S-Pen button mid-drawing immediately splits the stroke and activates the eraser, returning to the active brush upon release. Hover gestures are also tracked to highlight the active tool.
- **Closure Safety:** Uses `rememberUpdatedState` for callback parameters (`onDrawStart`, `onDrawMove`, `onCanvasSizeMeasured`, `onSpenEraserActiveChanged`, `isEraserMode`, `eraserStrokeWidth`) to avoid stale closure references when selecting colors, toggling tools, or updating lambda instances.
- **Visual Cursor & Sizing:** Live circular boundary guide (`DEFAULT_ERASER_STROKE_WIDTH = 80f`) rendered on touch and hover points with high-contrast dual outline (dark outer, white inner, translucent fill) for clear visual feedback on the area being erased.
- **Aspect Scaling:** Records canvas dimensions (`canvasWidth`, `canvasHeight`) with drawings so that previews in `GuessScreen` and `SummaryScreen` scale strokes accurately across different screen sizes.

### Keyboard & IME Inset Handling
- All screens with text entry (`PlayerSetupScreen`, `PassPhoneScreen`, `GuessScreen`) must handle the software keyboard properly:
  - Use `Modifier.imePadding()` on the root layout or `Scaffold`.
  - Use `Modifier.imeNestedScroll()` on scrollable containers (`LazyColumn` or `verticalScroll`).
  - Use `WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)` for bottom action bars so they float cleanly above the keyboard or navigation bar.
  - Use `KeyboardOptions(imeAction = ImeAction.Done)` and `KeyboardActions(onDone = ...)` so pressing Enter/Done immediately triggers the action (e.g. adding a player or submitting a guess).

### Palette & Color Selection
- Color chips in `GameScreen` highlight the currently active color with an inner checkmark icon (inverting color for dark vs. bright chips) and a solid border.
- The Rubber (eraser) button sits alongside the palette, with visual selection states reflecting both manual UI toggles and hardware S-Pen button presses.

---

## 6. Coding Standards & Conventions

- **Kotlin DSL & Jetpack Compose:** All new UI must be in Compose; prefer Material 3 components (`Scaffold`, `TopAppBar`, `Button`, `OutlinedTextField`, `Card`).
- **No Hardcoded Strings:** Place user-facing UI text in `strings.xml`.
- **Unidirectional Data Flow:** Screen composables receive state values and trigger events/methods on `GameViewModel`.
- **Testing:** New game mechanics or sanitization rules must include automated unit test coverage in `GameViewModelTest.kt`.
- **Testing naming pattern:** `` `given [condition] when [action] then [expected result]` `` or clear descriptive names.
