# TimingSoft — Development Phases

A phase-by-phase roadmap for evolving TimingSoft from a basic RFID reader into a production-grade race timing system.

Each phase is self-contained and shippable. Complete one fully before starting the next.

---

## Phase 1 — Settings Panel & Remove All Hardcodes

**Goal:** Every hardcoded value moves to a persistent Settings dialog. No credentials or ports buried in source code.

### Tasks

- [x] Add a `Settings` button to the control panel
- [x] Build a tabbed `SettingsDialog` (JDialog) with the following tabs:
  - **Wiclax** — User, Device ID, Server URL (`http://wiclax.com`), Port (`35014`), Enable/Disable toggle
  - **Local API** — Enable/Disable toggle, Port (default `3000`), Endpoint path (`/api/tags`)
  - **Reader** — Zebra TCP port (default `5084`), Connection timeout (seconds), Auto-reconnect toggle
  - **Data** — EPC format (Full EPC / Last N chars), N value, CSV output directory (browse button)
- [x] Persist all settings to `config.properties` on Save
- [x] Load all settings on startup; apply defaults if key is absent
- [x] Remove all hardcoded constants from `TimingSystemGUI.java`, `TagReader.java`, and `ZebraReaderAdapter.java`
- [x] Fix the `tas245` vs `tas246` Device ID mismatch between `TagReader.java` and `TimingSystemGUI.java`
- [x] EPC formatting (Full / Last N) applied at listener layer via `AppConfig.formatEpc()`

### Files Affected
- `src/TimingSystemGUI.java`
- `src/ZebraReaderAdapter.java`
- `src/TagReader.java`
- `config.properties`
- **New:** `src/SettingsDialog.java`
- **New:** `src/AppConfig.java` (singleton config loader/saver)

### Done When
All values previously hardcoded are editable in the UI, survive an app restart, and no raw credentials or ports remain in Java source.

---

## Phase 2 — Duplicate Tag Suppression

**Goal:** Each athlete is recorded exactly once per timing point crossing, regardless of how many times the antenna reads their tag.

### Tasks

- [x] Add `suppressionSecs` setting to `AppConfig` (default: 300 s / 5 min) and Settings → Data tab
- [x] Implement `DuplicateFilter` class:
  - Key: `epc|readerIp` (one independent slot per timing point)
  - Value: timestamp of last accepted read
  - Accepts a read only if `now - lastRead >= suppressionWindowMs`
  - Thread-safe (`ConcurrentHashMap`)
- [x] Wire `DuplicateFilter` into `TimingSystemGUI.onTagRead()` — first gate before any downstream processing
- [x] "Suppressed: N" counter in the status bar; tooltip shows current window duration
- [x] "Reset Filter" button in control panel — clears timestamps and resets counter (use between waves)

### Files Affected
- `src/TimingSystemGUI.java`
- **New:** `src/DuplicateFilter.java`
- `src/AppConfig.java` (new setting)

### Done When
A single tag crossing the mat 50 times in 5 minutes produces exactly 1 row in the table and 1 CSV entry. The suppression window is adjustable without recompiling.

---

## Phase 3 — Participant Database & Bib Mapping

**Goal:** Raw EPC hex values are replaced with meaningful athlete information (bib number, name) throughout the UI.

### Tasks

- [x] Define `Participant` model: `bibNumber`, `firstName`, `lastName`, `epc`, `category`, `waveId` + runtime state (`hasBeenRead`, `lastReadTime`)
- [x] Build `ParticipantRegistry` singleton: ConcurrentHashMap by EPC & bib, suffix-match lookup for LAST_N mode, `loadFromCsv()`, `exportStatusCsv()`, `assignEpc()`, `markRead()`
- [x] "Import Participants" button → file chooser for CSV (`bib,firstName,lastName,epc,category,wave`), auto-detects/skips header row
- [x] Live reads table columns updated: `#` | `Bib` | `Name` | `EPC` | `Timestamp` | `Antenna` | `Category`
- [x] Unknown tags highlighted in soft orange; known participants shown on white background (custom `TagTableRenderer`)
- [x] "Participants" tab: searchable (bib/name/EPC), sortable table showing all athletes with green/amber status badges and last read time; live stats label "Athletes seen: X / Y (Z%)"
- [x] Right-click on any live reads row → "Assign Bib" dialog; updates row and participants panel immediately
- [x] "Export Status" button → saves `participants_status.csv` with read/unread status and last read time

### Files Affected
- `src/TimingSystemGUI.java`
- **New:** `src/Participant.java`
- **New:** `src/ParticipantRegistry.java`

### Done When
Importing a participant CSV causes all subsequent tag reads to display athlete name and bib number. Unknown tags are visually distinct. No bib assignment requires recompiling.

---

## Phase 4 — Race Control & Net Time Calculation

**Goal:** The app knows when a race starts and can calculate each athlete's official finish time.

### Tasks

- [x] `RaceSession` state machine: `IDLE → ARMED → RUNNING → FINISHED`; thread-safe finish recording keyed by bib
- [x] `Wave` model: id, name, gun time (epoch ms), `fire()`, `getFormattedGunTime()`
- [x] "Arm Race" button (IDLE only) + coloured state indicator in Race Control panel
- [x] Wave management: "Waves..." button opens inline dialog; add/remove waves; default Wave 1 always present
- [x] "Fire Gun!" button (ARMED/RUNNING): confirmation dialog → fires selected wave → starts GUI race timer from gun time
- [x] Net time = `finishTimeMs − wave.gunTimeMs`; athletes assigned to their wave via `Participant.waveId`
- [x] Results tab: live leaderboard sorted by net time; gold/silver/bronze row highlights for top 3; stats label "Finishers: X / Y"
- [x] `ResultsExporter.buildRows()` shared by GUI table and CSV export; sorted ascending by net time
- [x] "Finish Race" button locks session (FINISHED state); no new finishes recorded after
- [x] "Export Results" button (RUNNING/FINISHED): CSV with place, bib, name, category, wave, gun start, finish time, net time
- [x] Live reads row renderer: green tint for confirmed finishers, white for known bibs, orange for unknown tags

### Files Affected
- `src/TimingSystemGUI.java`
- **New:** `src/RaceSession.java`
- **New:** `src/Wave.java`
- **New:** `src/ResultsExporter.java`

### Done When
Clicking "Gun Start" records T=0. Each tag read produces a net time. The live leaderboard re-sorts as athletes cross. Exported CSV matches what a race director would hand to athletes.

---

## Phase 5 — Multiple Timing Points & Split Times

**Goal:** Support a full course layout with start mat, intermediate splits, and finish line — each driven by a separate reader.

### Tasks
- [x] Define a `TimingPoint` model: `id`, `name` (e.g., "Start", "10K Split", "Finish"), `readerIp`, `pointType` (`START / SPLIT / FINISH`)
- [x] Add "Timing Points" configuration in Settings: add/edit/remove points, assign reader IPs to point names
- [x] `onTagRead` routes each read to the correct `TimingPoint` by `readerIp`
- [x] `RaceSession` stores per-athlete reads at each timing point
- [x] Calculate split times between consecutive points for each athlete
- [x] Update the UI: tab per timing point showing live reads at that location
- [x] Results export includes all split columns
- [x] Support connecting to multiple readers simultaneously (one `ReaderInterface` instance per point)

### Files Affected
- `src/TimingSystemGUI.java`
- `src/RaceSession.java`
- **New:** `src/TimingPoint.java`
- **New:** `src/TimingPointManager.java`

### Done When
A 3-reader setup (start, 5K, finish) produces a results table with `Start Time`, `5K Split`, `Finish Time`, and `Net Time` columns for every athlete.

---

## Phase 6 — Auto-Reconnect & Reader Resilience

**Goal:** Temporary network or hardware glitches do not require operator intervention.

### Tasks
- [x] Implement auto-reconnect in both `RFIDController` and `ZebraReaderAdapter`:
  - On disconnect event or exception, enter reconnect loop
  - Back-off schedule: 1s → 2s → 4s → 8s → 16s → 30s (cap at 30s)
  - Show reconnect attempt count and next retry countdown in status bar
- [x] Add "Auto-Reconnect" toggle in Settings → Reader tab
- [x] On successful reconnect, automatically resume reading if it was active before disconnect
- [x] Add connection state history log (timestamped connect/disconnect events) accessible from a "Log" panel
- [x] Distinguish "intentional disconnect" (user clicked button) from "unexpected disconnect" — only auto-reconnect on unexpected

### Files Affected
- `src/RFIDController.java`
- `src/ZebraReaderAdapter.java`
- `src/TimingSystemGUI.java`
- `src/AppConfig.java`

### Done When
Unplugging and replugging a reader's network cable causes automatic reconnection within 30 seconds with no operator action, and reading resumes from where it stopped.

---

## Phase 7 — Live Statistics & Reader Health Panel

**Goal:** Give operators visibility into reader performance so they can diagnose antenna placement and coverage issues on-site.

### Tasks
- [ ] Add a "Stats" panel (collapsible bottom drawer or separate tab):
  - Reads/sec (rolling 10-second average), per antenna
  - Total raw reads vs. unique athletes seen
  - % of reads from known bibs vs. unknown tags
  - Suppressed duplicate count
  - Reader uptime / connection duration
- [ ] Add RSSI (signal strength) column to the tag table (where SDK provides it — Impinj Octane supports this)
- [ ] Per-antenna read count trend (simple bar chart using Java2D or JFreeChart)
- [ ] Alert thresholds: warn if an antenna drops to 0 reads/min while others are active (possible cable fault)
- [ ] Export stats summary to a log file alongside the race results

### Files Affected
- `src/TimingSystemGUI.java`
- `src/RFIDController.java`
- **New:** `src/StatsPanel.java`
- **New:** `src/ReadStatistics.java`

### Done When
An operator can see at a glance which antennas are working, what the read rate is, and get an alert if an antenna goes silent mid-race.

---

## Phase 8 — Wiclax TCP Mode & Protocol Hardening

**Goal:** Replace the fragile HTTP GET Wiclax integration with the more reliable direct TCP protocol, and make the integration robust under load.

### Tasks
- [ ] Read and implement the `Generic-passing-cloud-protocol-for-Wiclax.pdf` TCP protocol (`javadoc/` folder)
- [ ] Add `WiclaxTcpClient` class:
  - Persistent TCP socket connection to Wiclax
  - Send passings in the binary/text frame format specified in the protocol doc
  - Heartbeat / keep-alive mechanism
  - Automatic reconnect on socket drop
- [ ] Add mode selector in Settings → Wiclax tab: `HTTP (Cloud)` vs. `TCP (Direct)`
- [ ] Queue passings locally if Wiclax is unreachable; flush queue on reconnect (no lost data)
- [ ] Add Wiclax connection status indicator to the status bar (green/red dot)
- [ ] Support sending the race start time to Wiclax via the protocol's start command

### Files Affected
- `src/TimingSystemGUI.java`
- `src/AppConfig.java`
- **New:** `src/WiclaxClient.java` (interface)
- **New:** `src/WiclaxHttpClient.java` (existing logic, refactored)
- **New:** `src/WiclaxTcpClient.java`

### Done When
Under a simulated burst of 200 tag reads, all passings reach Wiclax with no dropped records. If Wiclax is temporarily unreachable, reads queue locally and flush automatically on reconnection.

---

## Summary Table

| Phase | Feature | Key Benefit |
|---|---|---|
| 1 | Settings Panel | No more hardcoded credentials; fully configurable |
| 2 | Duplicate Suppression | One record per athlete crossing — usable race data |
| 3 | Participant / Bib Mapping | Human-readable results; unknown tag alerts |
| 4 | Race Control & Net Time | Official timing; live leaderboard |
| 5 | Multiple Timing Points | Full course support; split times |
| 6 | Auto-Reconnect | Operator doesn't babysit hardware |
| 7 | Stats & Health Panel | Antenna diagnostics; read-rate visibility |
| 8 | Wiclax TCP Mode | Reliable high-volume integration; no dropped passings |
