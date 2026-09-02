# JARVIS – Persönlicher KI-Assistent für Android

Eigenständige Android-App (Kotlin + Jetpack Compose), inspiriert vom Look
des hochgeladenen "JARVIS OS"-Konzepts. Kein kopierter Code, keine
kopierten Logos/Texte – eigene Umsetzung.

## Enthaltene Funktionen (in dieser Version fertig implementiert)

- **HUD-Startbildschirm**: animierte "Core"-Visualisierung (Compose Canvas),
  Statuszeile (Uhrzeit, Akku, KI-Status), Vitals-Panel (Memory-Anzahl,
  letzter Befehl).
- **Sprachsteuerung**: Mikrofon-Button → Android `SpeechRecognizer` (on-device/
  System-Spracherkennung, Deutsch) → Routing an Skills → Antwort wird per
  `TextToSpeech` vorgelesen. Live-Zustände: BEREIT / HÖRT ZU / DENKT NACH / SPRICHT.
- **Chat-Bildschirm**: gleicher Assistent als Text-Chat, für lautlose Nutzung.
- **Lokales Memory**: Room/SQLite-Datenbank auf dem Gerät. "Merke dir, dass
  mein 3D-Drucker ein Anycubic Kobra 2 Neo ist" → später "Welchen 3D-Drucker
  habe ich?" liest es wieder aus. Keine Cloud, keine Server-Anbindung.
- **Modulares Skill-System** (`skills/`): Uhrzeit, Datum, Taschenrechner,
  Memory speichern/abrufen, Notizen, einfache Erinnerungen, Websuche
  (öffnet System-Suche), App öffnen (installierte Apps per Namen),
  Einstellungen öffnen, Systeminfo (Akku, freier RAM), Smalltalk-Fallback.
  Neue Skills = eine neue Klasse, die `Skill` implementiert und in
  `SkillRegistry.kt` eingetragen wird.
- **Berechtigungen**: Mikrofon wird zur Laufzeit angefragt, mit eigenem
  Erklärungs-Screen, bevor die App nutzbar ist.
- **Datenschutz**: keine API-Keys im Code, keine Cloud-Anbindung, Memory-
  Datenbank ist explizit vom Android-Backup ausgeschlossen
  (`backup_rules.xml`, `data_extraction_rules.xml`).

## Bewusste Vereinfachungen (nächste Ausbaustufen)

- Erinnerungen werden aktuell in der lokalen DB gespeichert, aber noch
  nicht als System-Notification via `AlarmManager`/`WorkManager` ausgelöst
  (Kommentar an der Stelle im Code: `ReminderSkill.kt`).
- Wetter-Skill ist noch nicht enthalten (würde einen Wetterdienst/API-Key
  benötigen – bewusst nicht "hart codiert", da keine Secrets in den Code
  gehören). Lässt sich als neue Skill-Klasse ergänzen, sobald du dich für
  einen Anbieter entschieden hast.
- "Öffne App" sucht per Name unter den installierten Apps – exaktere
  Treffer verbessern sich mit einer festen Zuordnungstabelle.

## Projektstruktur

```
JarvisApp/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/jarvis/assistant/
│       │   ├── MainActivity.kt
│       │   ├── data/            (Room: Memory, Chat, Notes, Reminders)
│       │   ├── skills/          (Skill-Interface + alle Skills + Registry)
│       │   ├── voice/           (SpeechRecognizer- und TTS-Wrapper)
│       │   ├── viewmodel/       (JarvisViewModel – zentraler State)
│       │   └── ui/              (Theme, HUD-Komponenten, Screens)
│       └── res/
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

## App bauen – Schritt für Schritt

### Voraussetzung
- [Android Studio](https://developer.android.com/studio) (aktuelle Version,
  z. B. "Koala" oder neuer)
- Ein Android-Gerät (oder Emulator) mit Android 8.0 (API 26) oder neuer

### 1. Projekt öffnen
1. Android Studio starten → **Open** → den Ordner `JarvisApp/` auswählen.
2. Android Studio erkennt das Gradle-Projekt automatisch und lädt beim
   ersten Öffnen den Gradle-Wrapper nach (Internetverbindung nötig).
   Falls gefragt wird "Gradle wrapper missing, use local Gradle
   distribution?" → **OK/Trust Project** bestätigen; Android Studio
   ergänzt die fehlende `gradle-wrapper.jar` automatisch.
3. Warten, bis der Gradle-Sync unten rechts fertig ist (kann beim ersten
   Mal einige Minuten dauern, da Abhängigkeiten heruntergeladen werden).

### 2. App direkt auf dein Handy übertragen (empfohlen)
1. Auf deinem Android-Handy: **Einstellungen → Über das Telefon** →
   mehrfach auf "Build-Nummer" tippen, bis "Entwickleroptionen" aktiviert
   sind.
2. **Einstellungen → Entwickleroptionen** → "USB-Debugging" aktivieren.
3. Handy per USB-Kabel mit dem PC verbinden, auf dem Handy die Nachfrage
   "USB-Debugging erlauben?" bestätigen.
4. In Android Studio oben in der Geräteliste dein Handy auswählen.
5. Auf den grünen **Run ▶**-Button klicken (oder `Shift + F10`).
   Android Studio baut die App, installiert sie automatisch auf deinem
   Handy und startet sie.

### 3. Alternativ: APK bauen und manuell installieren
1. In Android Studio: **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
2. Nach dem Bau erscheint unten rechts ein Link **"locate"** – die Datei
   liegt unter `app/build/outputs/apk/debug/app-debug.apk`.
3. Die APK auf dein Handy übertragen (USB, Cloud-Speicher, o. Ä.).
4. Auf dem Handy die APK-Datei antippen. Falls Android "Installation aus
   unbekannten Quellen" blockiert: dem Dialog folgen und die
   entsprechende App-Quelle einmalig erlauben.
5. Antippen zum Installieren, dann **JARVIS** öffnen.

### 4. Über die Kommandozeile bauen (ohne Android Studio)
Voraussetzung: Android SDK (`ANDROID_HOME` gesetzt) und Java 17 installiert.
```bash
cd JarvisApp
gradle wrapper           # erzeugt einmalig gradlew + gradle-wrapper.jar
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Erste Nutzung
1. App öffnen → Mikrofon-Berechtigung erlauben.
2. Mikrofon-Button antippen und z. B. sagen: *"JARVIS, wie spät ist es?"*
3. Für Text statt Sprache: oben links auf das Chat-Symbol tippen.
4. Etwas merken: *"Merke dir, dass mein 3D-Drucker ein Anycubic Kobra 2
   Neo ist."* – Abfragen mit: *"Welchen 3D-Drucker habe ich?"*

## Neue Skills hinzufügen
1. Neue Datei in `skills/`, Klasse implementiert `Skill`
   (`matches()` + `execute()`).
2. Instanz in `SkillRegistry.kt` in die `skills`-Liste eintragen
   (Reihenfolge = Priorität; der Smalltalk-Fallback bleibt immer am Ende).

Fertig – kein Rebuild der übrigen App-Teile nötig.
