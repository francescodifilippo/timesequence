# Sequenza Timer - Compilazione e Pubblicazione

## Prerequisiti

- **Android Studio** Ladybug (2024.2) o successivo
- **JDK 17** (incluso in Android Studio)
- **Android SDK 35** (installabile da Android Studio > Settings > SDK Manager)

---

## 1. Aprire il Progetto

1. Apri Android Studio
2. File > Open > seleziona la cartella `timesequence/`
3. Android Studio rileverà automaticamente il progetto Gradle
4. Attendi il completamento del sync Gradle (la prima volta scarica le dipendenze)

> **Nota:** Se manca il Gradle Wrapper JAR, Android Studio lo genererà automaticamente. In alternativa, se hai Gradle installato globalmente: `gradle wrapper` nella directory del progetto.

---

## 2. Compilazione Debug (Test)

### Da Android Studio:
- Collega un dispositivo Android via USB (o avvia un emulatore)
- Clicca il pulsante **Run** (triangolo verde) o `Shift+F10`

### Da terminale:
```bash
# APK debug
./gradlew assembleDebug

# L'APK si trova in:
# app/build/outputs/apk/debug/app-debug.apk
```

---

## 3. Compilazione Release (Pubblicazione)

### 3a. Creare il Keystore (solo la prima volta)

```bash
keytool -genkey -v -keystore sequenza-timer-release.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias sequenza-timer
```

Ti verrà chiesto:
- Password del keystore (scegli una password sicura, SALVALA)
- Nome, organizzazione, località
- Password della chiave (può essere uguale al keystore)

**IMPORTANTE:** Conserva il file `.jks` e le password in un luogo sicuro. Se le perdi, non potrai più aggiornare l'app sul Play Store.

### 3b. Configurare la signing nel progetto

Crea il file `local.properties` (NON commitare su git):
```properties
sdk.dir=/path/to/Android/Sdk
RELEASE_STORE_FILE=../sequenza-timer-release.jks
RELEASE_STORE_PASSWORD=la_tua_password
RELEASE_KEY_ALIAS=sequenza-timer
RELEASE_KEY_PASSWORD=la_tua_password
```

Oppure aggiungi in `app/build.gradle.kts` dentro il blocco `android {}`:
```kotlin
signingConfigs {
    create("release") {
        storeFile = file("../sequenza-timer-release.jks")
        storePassword = "la_tua_password"
        keyAlias = "sequenza-timer"
        keyPassword = "la_tua_password"
    }
}

buildTypes {
    release {
        isMinifyEnabled = true
        signingConfig = signingConfigs.getByName("release")
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
    }
}
```

### 3c. Generare l'AAB (Android App Bundle)

```bash
./gradlew bundleRelease
```

Il file AAB si trova in:
```
app/build/outputs/bundle/release/app-release.aab
```

> Google Play richiede il formato AAB, non APK.

---

## 4. Pubblicazione su Google Play Store

### 4a. Account sviluppatore

1. Vai su https://play.google.com/console
2. Registra un account sviluppatore (costo una tantum: $25 USD)
3. Completa la verifica dell'identità

### 4b. Creare l'app

1. Google Play Console > **Crea app**
2. Compila i campi:
   - **Nome app:** Sequenza Timer
   - **Lingua predefinita:** Italiano
   - **App o gioco:** App
   - **Gratuita o a pagamento:** Gratuita

### 4c. Scheda dello Store

#### Titolo
```
Sequenza Timer
```

#### Descrizione breve (max 80 caratteri)
```
Create and run custom timer sequences for breathing, exercise and more.
```

#### Descrizione completa (inglese)
```
Sequenza Timer lets you create, save, and play custom timer sequences — perfect for breathing exercises, workout intervals, meditation routines, and any activity that follows a timed pattern.

KEY FEATURES:

• Create Custom Sequences — Build your own timer sequences with as many phases as you need. Name each phase (e.g., "Inhale", "Hold", "Exhale") and set its duration in seconds.

• Color-Coded Phases — Assign a distinct color to each phase for instant visual recognition. The entire screen transitions smoothly between colors as phases change.

• Flexible Repetition — Set your sequence to repeat a specific number of times, run for a total duration (e.g., 5 minutes of breathing cycles), or loop infinitely until you stop it.

• Beautiful Countdown Display — A full-screen playback view features a large animated countdown ring that depletes as time passes, with the current phase name displayed prominently.

• Pause, Resume & Skip — Full playback control: pause and resume at any time, or skip ahead to the next phase.

• Haptic Feedback — Feel a gentle vibration at each phase transition so you can close your eyes and still follow along.

• Save & Organize — Save your favorite sequences with custom names and find them instantly on the home screen.

• Pre-loaded Examples — Get started immediately with built-in sequences for guided breathing and core exercises.

• Screen Always On — The display stays on during playback so your timer is always visible.

• Material Design 3 — Clean, modern interface with dark mode support that follows your system theme.

PERFECT FOR:
- Breathing exercises (Box breathing, 4-7-8, Wim Hof)
- Workout intervals and HIIT
- Meditation and mindfulness
- Yoga pose timing
- Physical therapy exercises
- Cooking and baking steps
- Study/break intervals (Pomodoro variations)
- Any timed routine you can imagine

Sequenza Timer is free, with no ads and no in-app purchases. Your data stays on your device.
```

#### Descrizione completa (italiano)
```
Sequenza Timer ti permette di creare, salvare e riprodurre sequenze di timer personalizzate — perfetto per esercizi di respirazione, intervalli di allenamento, routine di meditazione e qualsiasi attività che segue uno schema temporale.

CARATTERISTICHE PRINCIPALI:

• Crea Sequenze Personalizzate — Costruisci le tue sequenze con tutte le fasi che desideri. Dai un nome a ogni fase (es. "Inspira", "Trattieni", "Espira") e imposta la durata in secondi.

• Fasi con Colori — Assegna un colore diverso a ogni fase per un riconoscimento visivo immediato. L'intero schermo cambia colore con una transizione fluida.

• Ripetizione Flessibile — Imposta la sequenza per un numero specifico di ripetizioni, per una durata totale (es. 5 minuti di cicli respiratori), oppure in loop infinito fino all'interruzione manuale.

• Display Countdown — Schermo intero con un anello animato che si svuota, il nome della fase corrente ben visibile e il conto alla rovescia.

• Pausa, Riprendi e Salta — Controllo completo: metti in pausa, riprendi o salta alla fase successiva.

• Feedback Aptico — Una vibrazione ad ogni cambio fase, così puoi chiudere gli occhi e seguire il ritmo.

• Salva e Organizza — Salva le tue sequenze preferite con nomi personalizzati.

• Esempi Precaricati — Inizia subito con sequenze integrate per respirazione guidata ed esercizi.

• Schermo Sempre Acceso — Il display resta attivo durante la riproduzione.

• Material Design 3 — Interfaccia moderna con supporto tema scuro che segue le impostazioni di sistema.

PERFETTO PER:
- Esercizi di respirazione (Box breathing, 4-7-8, Wim Hof)
- Intervalli di allenamento e HIIT
- Meditazione e mindfulness
- Tempistica posizioni yoga
- Esercizi di fisioterapia
- Passi di cucina
- Intervalli studio/pausa (variazioni Pomodoro)
- Qualsiasi routine temporizzata

Sequenza Timer è gratuita, senza pubblicità e senza acquisti in-app. I tuoi dati restano sul tuo dispositivo.
```

### 4d. Categorizzazione e Tag

| Campo | Valore |
|-------|--------|
| **Categoria** | Salute e fitness |
| **Tag** | Timer, Breathing, Exercise, Meditation, Interval Timer |
| **Target audience** | Tutti (Everyone) |
| **Content rating** | IARC - Tutti (completa il questionario) |

### 4e. Classificazione dei contenuti

1. Vai a Policy > App Content > Content rating
2. Avvia il questionario IARC
3. Tipo: Utilità
4. Nessun contenuto sensibile (violenza, linguaggio, ecc.)
5. Risultato atteso: **PEGI 3 / Everyone**

### 4f. Privacy Policy

Poiché l'app non raccoglie dati utente, crea una privacy policy minimale e ospitala (es. su GitHub Pages):

```
Privacy Policy - Sequenza Timer

Last updated: [DATA]

Sequenza Timer does not collect, store, or transmit any personal data. All timer sequences are stored locally on your device and are never shared with third parties.

The app does not use analytics, tracking, advertising SDKs, or any network connections.

Contact: [la tua email]
```

### 4g. Risorse grafiche richieste

| Risorsa | Dimensioni | Note |
|---------|-----------|------|
| Icona app | 512x512 px | PNG, 32-bit, senza trasparenza |
| Feature graphic | 1024x500 px | Banner orizzontale mostrato in alto |
| Screenshot telefono | min. 2, formato 16:9 | Almeno 320px lato corto |
| Screenshot tablet (opz.) | min. 1, 7" e 10" | Consigliato per maggiore visibilità |

**Suggerimenti per screenshot:**
1. Home screen con le sequenze prefatte
2. Editor con una sequenza in modifica
3. Playback con l'anello countdown (fase "Inspira" verde)
4. Playback con fase "Trattieni" (colore diverso)

### 4h. Rilascio

1. Vai a Release > Production
2. Clicca **Create new release**
3. Carica il file `app-release.aab`
4. Inserisci le note di rilascio:
   ```
   Versione 1.0.0
   - Prima release
   - Crea e salva sequenze di timer personalizzate
   - Playback con countdown animato e colori per fase
   - Feedback aptico ai cambi fase
   - Sequenze di esempio precaricate
   ```
5. Clicca **Review release** > **Start rollout to Production**

---

## 5. Aggiornamenti Futuri

Per rilasciare un aggiornamento:

1. Incrementa `versionCode` e `versionName` in `app/build.gradle.kts`
2. `./gradlew bundleRelease`
3. Carica il nuovo AAB su Play Console > Release > Production

---

## 6. Checklist Pre-Pubblicazione

- [ ] App testata su dispositivo fisico
- [ ] Tutti i flussi funzionano (crea, modifica, elimina, play, pausa, skip, stop)
- [ ] Modalita ripetizione: per conteggio, per durata totale, infinita
- [ ] Anello countdown si anima correttamente
- [ ] Vibrazione funziona ai cambi fase
- [ ] Schermo resta acceso durante playback
- [ ] Dark mode funziona correttamente
- [ ] Lingua inglese e italiana verificate
- [ ] Icona app creata (512x512)
- [ ] Feature graphic creata (1024x500)
- [ ] Almeno 2 screenshot catturati
- [ ] Privacy policy pubblicata online
- [ ] Keystore salvato in luogo sicuro
- [ ] Password keystore salvate in luogo sicuro
