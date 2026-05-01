# TasTimer

A Java desktop application for RFID race timing, supporting **Impinj Octane** and **Zebra FX Series** readers. Designed for live race events with real-time bib tracking, net time calculation, split times, and multi-platform cloud integration.

---

## Features

| Category | Capability |
| --- | --- |
| **Reader Support** | Impinj (Octane SDK) and Zebra FX (API3 SDK) |
| **Duplicate Suppression** | Configurable window (default 5 min) — one record per crossing |
| **Participant Database** | CSV import, bib/EPC mapping, live status tracking |
| **Race Control** | Armed → Running → Finished state machine, gun times per wave |
| **Net Time** | Per-wave gun start; net time calculated on every crossing |
| **Timing Points** | Multiple readers (Start / Split / Finish); split times per athlete |
| **Auto-Reconnect** | Exponential back-off on unexpected disconnect; resumes reading |
| **RUFUS Cloud API** | Register device, create session, stream passings to RUFUS Cloud |
| **Wiclax Integration** | HTTP passing forwarding to Wiclax cloud |
| **FeiBot Integration** | UDP External Protocol V1.0 with 5 s heartbeat |
| **Local API** | HTTP POST to a local endpoint (e.g. custom scoring software) |
| **CSV Export** | Results with place, bib, name, wave, gun start, finish, net time, splits |

---

## RUFUS Cloud API Integration

TasTimer connects to the [RUFUS Public REST API](https://api.runonrufus.com/v0) to stream passings into your RUFUS account in real time.

### Setup (Settings → RufusAPI tab)

1. **Enable** the RufusAPI integration checkbox.
2. Enter your **API Key** (requires `WRITE` or `READ_WRITE` access type).
3. Enter a **Serial Number** for this timing device and an optional **Device Alias**.
4. Click **Bind Device** — TasTimer calls `POST /devices` and auto-fills the Device ID field.
5. Click **Create Session** — TasTimer calls `POST /sessions` and caches the session token.
6. Click **Save**.

From that point every accepted tag crossing is forwarded to `POST /passings` on a background thread.

### API Flow

```text
POST /devices   →  deviceid          (Bind Device button)
POST /sessions  →  token_session     (Create Session button)
POST /passings  →  fire-and-forget   (on every tag read)
```

If a session token is not yet cached when the first passing arrives, TasTimer will attempt to create a session automatically using the stored device ID.

---

## Building

**Requirements:** Java 21+, `javac`/`jar` in PATH.

```bash
javac -cp "lib/*" -d bin src/*.java
jar cvfm TimingSoft.jar MANIFEST.MF -C bin .
```

Or on Windows:

```cmd
build_release.bat
```

---

## Running

```bash
java --enable-native-access=ALL-UNNAMED -jar TimingSoft.jar
```

The working directory must contain `lib/` and (on Windows) `RFIDAPI32PC.dll`.

For macOS / Linux with Zebra readers, ensure the native `.so` libraries are on the library path.

### Mock Server (development / testing)

```bash
node mock-server/server.js
```

Simulates tag reads on `http://localhost:3000` so you can test the full pipeline without hardware.

---

## Project Structure

```text
src/                   Java source files
lib/                   octane-sdk.jar, Symbol.RFID.API3.jar
mock-server/           Node.js simulation server
MANIFEST.MF            JAR entry point
DEVELOPMENT_PHASES.md  Phase-by-phase feature roadmap
build_release.bat      Windows build + dist packager
run.sh                 macOS / Linux launcher
```

Key source files:

| File | Responsibility |
| --- | --- |
| `TimingSystemGUI.java` | Main window, tag pipeline, race control UI |
| `AppConfig.java` | Singleton settings store (`config.properties`) |
| `RufusAPIClient.java` | RUFUS Cloud REST client (bind, session, passings) |
| `SettingsDialog.java` | All configuration tabs including RufusAPI |
| `RFIDController.java` | Impinj Octane reader adapter with auto-reconnect |
| `ZebraReaderAdapter.java` | Zebra FX reader adapter with auto-reconnect |
| `DuplicateFilter.java` | Suppression window per EPC + reader IP |
| `ParticipantRegistry.java` | EPC → bib/name lookup, CSV import/export |
| `RaceSession.java` | Race state machine, per-athlete crossing records |
| `TimingPointManager.java` | Maps reader IPs to named timing points |
| `FeiBotClient.java` | UDP FeiBot External Protocol V1.0 |
| `ResultsExporter.java` | CSV results builder (place, splits, net time) |

---

## Configuration

Settings are persisted to `config.properties` in the working directory. The file is excluded from version control — do not commit it as it may contain API keys.

Notable settings:

| Key | Default | Description |
| --- | --- | --- |
| `rufusapi.enabled` | `false` | Enable RUFUS Cloud integration |
| `rufusapi.apiKey` | _(empty)_ | Your RUFUS API key |
| `rufusapi.deviceId` | _(empty)_ | Filled automatically after Bind Device |
| `rufusapi.serialNumber` | _(empty)_ | Serial number used when binding the device |
| `rufusapi.alias` | `TimingSoft Device` | Human-readable device label in RUFUS |
| `suppression.secs` | `300` | Duplicate suppression window in seconds |
| `epc.format` | `LAST_N` | `FULL` or `LAST_N` |
| `epc.lastN` | `8` | Characters to use when format is `LAST_N` |

---

## Dependencies

| Library | Purpose |
| --- | --- |
| `lib/octane-sdk.jar` | Impinj Octane RFID SDK |
| `lib/Symbol.RFID.API3.jar` | Zebra RFID API3 SDK |
| `RFIDAPI32PC.dll` | Zebra native bridge (Windows only) |

---

## Development Phases

See [DEVELOPMENT_PHASES.md](DEVELOPMENT_PHASES.md) for the full phase-by-phase roadmap.

| Phase | Feature | Status |
| --- | --- | --- |
| 1 | Settings Panel & config persistence | Done |
| 2 | Duplicate Tag Suppression | Done |
| 3 | Participant Database & Bib Mapping | Done |
| 4 | Race Control & Net Time | Done |
| 5 | Multiple Timing Points & Split Times | Done |
| 6 | Auto-Reconnect + RUFUS / FeiBot / Wiclax | Done |
| 7 | Live Statistics & Reader Health Panel | Planned |
| 8 | Wiclax TCP Mode & Protocol Hardening | Planned |
