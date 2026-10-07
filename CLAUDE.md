# Sequenza Timer

App Android per creare, salvare e riprodurre sequenze di timer personalizzate (respirazione, esercizi, meditazione).

## Tech Stack

- **Kotlin + Jetpack Compose** (no XML layouts)
- **Room** con fasi serializzate in JSON (`kotlinx.serialization`)
- **MVVM**: ViewModel + StateFlow
- **Material 3** con supporto colori dinamici (Android 12+)
- **Navigation Compose** con route type-safe (`@Serializable`)
- **Coroutines** per il TimerEngine (tick 50ms)
- **Nessun DI framework** (dipendenze via `TimerSequenceApp` Application class)

## Struttura

```
app/src/main/java/com/francescodf/sequenzatimer/
├── MainActivity.kt                    # Single activity, setta tema e navigazione
├── TimerSequenceApp.kt                # Application: Room DB, repository, seed data
├── data/
│   ├── local/                         # AppDatabase, SequenceDao, Converters (JSON)
│   ├── model/                         # TimerSequence (Room entity), TimerPhase
│   └── repository/                    # SequenceRepository interface + impl
├── domain/timer/                      # TimerEngine (coroutine loop), TimerState
├── ui/
│   ├── navigation/                    # Routes (@Serializable), AppNavigation
│   ├── theme/                         # Color (PhaseColorPalette), Theme, Type
│   ├── components/                    # CountdownRing, PhaseColorPicker, SequenceCard
│   └── screens/
│       ├── home/                      # Lista sequenze, empty state, delete confirm
│       ├── editor/                    # Crea/modifica: fasi, colori, repeat mode
│       └── playback/                  # Countdown animato, sfondo colore, haptic
└── util/                              # HapticFeedbackHelper
```

## Convenzioni

- Stringhe UI tutte in `res/values/strings.xml` (EN default) e `res/values-it/strings.xml`
- I colori fase sono definiti in `PhaseColorPalette` (`ui/theme/Color.kt`), 12 colori
- Le fasi sono salvate come JSON nella colonna `phases` di Room (`Converters.kt`)
- Il timer usa `@Volatile var skipRequested` per segnalare skip cross-coroutine
- Navigazione con guardia anti-double-tap (`Lifecycle.State.RESUMED`)

## Build

Il progetto richiede **Android Studio** (il Gradle Wrapper JAR non e' nel repo). Vedi `PUBLISH.md` per istruzioni complete di compilazione e pubblicazione.

```bash
# Da Android Studio o dopo aver rigenerato il wrapper:
./gradlew assembleDebug    # APK debug
./gradlew bundleRelease    # AAB per Play Store
```

## Test

Non ci sono test automatici. Testare manualmente:
1. Crea sequenza -> salva -> verifica in home
2. Modifica -> cambia nome/fasi/colori -> salva
3. Play -> verifica anello, countdown, cambio colore sfondo, vibrazione
4. Pausa/riprendi, skip fase, stop con conferma
5. Modalita infinite -> stop manuale
6. Back con modifiche non salvate -> dialog discard
