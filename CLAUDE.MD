# TasTimer — Web UI Migration

## What this project is
TasTimer is a Java desktop application for RFID race timing. It supports Impinj
and Zebra FX readers, runs a full race engine (duplicate suppression, participant
registry, wave-based net time, split times, auto-reconnect), and streams passings
to RUFUS Cloud, Wiclax, and FeiBot.

## What we are building NOW
We are replacing the Java Swing operator interface with a browser-based web UI.
The Java process stays running on the timing laptop — we are adding an embedded
Javalin HTTP + WebSocket server to it so any browser on the same machine can
serve as the operator screen.

## What we are NOT changing
- RFIDController.java, ZebraReaderAdapter.java — touch nothing
- DuplicateFilter.java, ParticipantRegistry.java, RaceSession.java — touch nothing
- TimingPointManager.java, RufusAPIClient.java, FeiBotClient.java — touch nothing
- ResultsExporter.java, AppConfig.java — touch nothing
- All outbound integrations (RUFUS Cloud, Wiclax, FeiBot) — touch nothing
- CSV export to local disk — keep as-is

## What we ARE removing
- The Local API outbound HTTP POST to localhost:3000 — remove entirely
- The Node.js mock server as a runtime dependency (keep in repo for dev/test only)
- The "Node Server: IP:3000" label from the Swing status bar
- All javax.swing.* imports and UI code from TimingSystemGUI once the web UI
  is confirmed working

## Architecture after migration

Timing laptop (Java process)
├── Reader engine (unchanged)
│   ├── RFIDController  →  Impinj reader over TCP
│   └── ZebraReaderAdapter  →  Zebra FX over TCP
├── Race engine (unchanged)
│   ├── DuplicateFilter, ParticipantRegistry, RaceSession
│   └── TimingPointManager, ResultsExporter
├── Outbound integrations (unchanged)
│   ├── RufusAPIClient, FeiBotClient, WiclaxClient
├── NEW: WebServer.java  (Javalin on port 8080)
│   ├── Serves web/dist/ as static files
│   ├── REST endpoints for all operator actions
│   └── WebSocket /ws  for live push to browser
└── NEW: WebBridge.java
├── Implements RFIDDataListener
├── Receives onTagRead / onAntennaStatus / onReaderStatus callbacks
└── Broadcasts JSON events to all connected WebSocket clients
Browser (any device on same machine or network)
└── React SPA (web/)
    ├── WebSocket client — live tag feed, antenna status, race timer
    └── REST calls — connect reader, start/stop, settings, CSV export

## Build system
Using Maven (pom.xml at root). Run: mvn package
Output JAR: target/TimingSoft.jar
Main class: TimingSystemGUI

## Web frontend
Location: web/  (Vite + React + TypeScript + Tailwind + shadcn/ui)
Build output: web/dist/  (served by Javalin as static files)
Dev command: cd web && npm run dev  (proxies /api and /ws to :8080)

## REST API contract

| Method | Path | Description |
|--------|------|-------------|
| GET | /api/status | Reader connection state, race state |
| POST | /api/reader/connect | { type, ip } |
| POST | /api/reader/disconnect | — |
| POST | /api/reader/start | Start reading |
| POST | /api/reader/stop | Stop reading |
| GET | /api/participants | List all participants |
| POST | /api/participants/import | Multipart CSV upload |
| GET | /api/race | Current race state |
| POST | /api/race/arm | Arm race |
| POST | /api/race/start | Fire gun (starts wave) |
| POST | /api/race/finish | Lock race as finished |
| POST | /api/filter/reset | Reset duplicate filter |
| GET | /api/export/csv | Download results CSV |
| GET | /api/settings | Get all config values |
| POST | /api/settings | Save config values |

## WebSocket events (server → browser, JSON)

| event | payload |
|-------|---------|
| TAG_READ | { epc, bib, name, timestamp, readerIp, antenna, netTime } |
| ANTENNA_STATUS | { antenna, connected, count } |
| READER_STATUS | { message, connected } |
| RACE_TIMER | { elapsed } |
| RACE_STATE | { state } — IDLE/ARMED/RUNNING/FINISHED |

## Coding standards
- Java: existing code style, no Lombok, Java 21
- React: functional components, hooks only, no class components
- All API responses: { success: boolean, data?: any, error?: string }
- WebSocket messages: { event: string, payload: object }
- No hardcoded localhost — use relative URLs from browser (same origin)

## Key source files (current)
- src/TimingSystemGUI.java — main(), Swing UI, RFIDDataListener implementation
- src/AppConfig.java — singleton settings
- src/RFIDController.java — Impinj adapter
- src/ZebraReaderAdapter.java — Zebra adapter  
- src/RFIDDataListener.java — callback interface
- src/ParticipantRegistry.java — bib/EPC mapping
- src/RaceSession.java — race state machine
- src/TimingPointManager.java — split point routing
- src/ResultsExporter.java — CSV builder
- src/RufusAPIClient.java — RUFUS Cloud REST
- src/FeiBotClient.java — FeiBot UDP

