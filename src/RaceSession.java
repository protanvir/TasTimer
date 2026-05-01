import java.util.*;

/**
 * Manages the lifecycle of a single race.
 *
 * State machine:
 *   IDLE ──arm()──► ARMED ──fireWave()──► RUNNING ──finish()──► FINISHED
 *                                         (any additional waves can fire here too)
 *
 * Finish times are recorded only while RUNNING.
 * Net time = finishTimeMs − wave.gunTimeMs  (athlete's assigned wave).
 *
 * Thread-safe: all mutating methods are synchronized.
 */
public class RaceSession {

    // ------------------------------------------------------------------ //
    // State
    // ------------------------------------------------------------------ //
    public enum State { IDLE, ARMED, RUNNING, FINISHED }

    private volatile State state = State.IDLE;

    // ------------------------------------------------------------------ //
    // Waves
    // ------------------------------------------------------------------ //
    private final List<Wave> waves = new ArrayList<>();

    // ------------------------------------------------------------------ //
    // Finish records  (keyed by bib number)
    // ------------------------------------------------------------------ //
    private static class FinishRecord {
        final long   finishTimeMs;
        final String finishTimestamp;
        FinishRecord(long ms, String ts) { finishTimeMs = ms; finishTimestamp = ts; }
    }

    // LinkedHashMap preserves insertion (arrival) order
    private final Map<String, FinishRecord> finishRecords = new LinkedHashMap<>();

    // ------------------------------------------------------------------ //
    // Crossing records  (keyed by "bib|pointId")
    // ------------------------------------------------------------------ //
    private static class CrossingRecord {
        final long   crossingTimeMs;
        final String crossingTimestamp;
        CrossingRecord(long ms, String ts) { crossingTimeMs = ms; crossingTimestamp = ts; }
    }

    /** Stores the first crossing of each bib at each timing point. */
    private final Map<String, CrossingRecord> crossingRecords = new LinkedHashMap<>();

    private static String crossingKey(String bib, String pointId) {
        return bib + "|" + pointId;
    }

    // ------------------------------------------------------------------ //
    // State transitions
    // ------------------------------------------------------------------ //

    public synchronized void arm() {
        if (state == State.IDLE) state = State.ARMED;
    }

    /**
     * Fires the gun for the given wave.
     * Transitions ARMED → RUNNING on the first wave fire.
     * Additional waves can be fired while already RUNNING (wave starts).
     *
     * @return true if the wave was found and fired; false if not found or already fired.
     */
    public synchronized boolean fireWave(String waveId) {
        Wave w = findWave(waveId);
        if (w == null || w.hasFired()) return false;
        w.fire();
        if (state == State.ARMED) state = State.RUNNING;
        return true;
    }

    public synchronized void finish() {
        if (state == State.RUNNING) state = State.FINISHED;
    }

    /** Resets everything back to IDLE (clears waves, finish records, and crossing records). */
    public synchronized void reset() {
        state = State.IDLE;
        waves.clear();
        finishRecords.clear();
        crossingRecords.clear();
    }

    // ------------------------------------------------------------------ //
    // State queries
    // ------------------------------------------------------------------ //
    public State   getState()     { return state; }
    public boolean isIdle()       { return state == State.IDLE; }
    public boolean isArmed()      { return state == State.ARMED; }
    public boolean isRunning()    { return state == State.RUNNING; }
    public boolean isFinished()   { return state == State.FINISHED; }

    // ------------------------------------------------------------------ //
    // Finish recording
    // ------------------------------------------------------------------ //

    /**
     * Records a finish time for the athlete identified by bib.
     * Ignored if: state != RUNNING, bib is blank, or the bib already has a record.
     *
     * @return true if this is a new (accepted) finish; false if ignored.
     */
    public synchronized boolean recordFinish(String bib, long finishTimeMs, String timestamp) {
        if (state != State.RUNNING)           return false;
        if (bib == null || bib.isEmpty())     return false;
        if (finishRecords.containsKey(bib))   return false;
        finishRecords.put(bib, new FinishRecord(finishTimeMs, timestamp));
        return true;
    }

    public boolean hasFinished(String bib) {
        return finishRecords.containsKey(bib);
    }

    public String getFinishTimestamp(String bib) {
        FinishRecord r = finishRecords.get(bib);
        return r != null ? r.finishTimestamp : "";
    }

    /**
     * Returns an unmodifiable snapshot of bib → finishTimeMs.
     * Used by the GUI to build the results table.
     */
    public synchronized Map<String, Long> getFinishTimesSnapshot() {
        Map<String, Long> snap = new LinkedHashMap<>();
        for (Map.Entry<String, FinishRecord> e : finishRecords.entrySet()) {
            snap.put(e.getKey(), e.getValue().finishTimeMs);
        }
        return Collections.unmodifiableMap(snap);
    }

    // ------------------------------------------------------------------ //
    // Net time helpers
    // ------------------------------------------------------------------ //

    /**
     * Returns the net time in milliseconds for the given bib/wave pair,
     * or -1 if either the finish or gun time is unavailable.
     */
    public long getNetTimeMs(String bib, String waveId) {
        FinishRecord r = finishRecords.get(bib);
        if (r == null) return -1;
        Wave w = getWaveForId(waveId);
        if (w == null || !w.hasFired()) return -1;
        long net = r.finishTimeMs - w.getGunTimeMs();
        return net >= 0 ? net : -1;
    }

    /** Formats a net-time duration into H:MM:SS.mmm or MM:SS.mmm. */
    public static String formatNetTime(long netMs) {
        if (netMs < 0) return "";
        long ms = netMs % 1000;
        long s  = (netMs / 1000) % 60;
        long m  = (netMs / 60_000) % 60;
        long h  = netMs / 3_600_000;
        if (h > 0) return String.format("%d:%02d:%02d.%03d", h, m, s, ms);
        return String.format("%d:%02d.%03d", m, s, ms);
    }

    // ------------------------------------------------------------------ //
    // Wave management
    // ------------------------------------------------------------------ //

    public synchronized void addWave(Wave wave) {
        waves.add(wave);
    }

    public synchronized boolean removeWave(String waveId) {
        Wave w = findWave(waveId);
        if (w == null || w.hasFired()) return false; // can't remove a fired wave
        waves.remove(w);
        return true;
    }

    public List<Wave> getWaves() {
        return Collections.unmodifiableList(waves);
    }

    public Wave findWave(String waveId) {
        return waves.stream()
                .filter(w -> w.getId().equals(waveId))
                .findFirst().orElse(null);
    }

    /**
     * Resolves the wave for an athlete.
     * Priority: exact waveId match → first fired wave → first wave → null.
     */
    public Wave getWaveForId(String waveId) {
        if (waveId != null && !waveId.isEmpty()) {
            Wave w = findWave(waveId);
            if (w != null) return w;
        }
        // Fall back: first wave that has fired, or just the first wave
        return waves.stream().filter(Wave::hasFired).findFirst()
                .orElseGet(() -> waves.isEmpty() ? null : waves.get(0));
    }

    /** Returns the first fired wave's gun time (for updating the GUI race timer). */
    public long getFirstGunTimeMs() {
        return waves.stream()
                .filter(Wave::hasFired)
                .mapToLong(Wave::getGunTimeMs)
                .min()
                .orElse(-1);
    }

    public int getFinishCount() { return finishRecords.size(); }

    // ------------------------------------------------------------------ //
    // Crossing recording (split / intermediate timing points)
    // ------------------------------------------------------------------ //

    /**
     * Records the first crossing of the given bib at the given timing point.
     * Subsequent crossings at the same point are ignored (first-read wins).
     *
     * @return true if this is a new (accepted) crossing; false if ignored.
     */
    public synchronized boolean recordCrossing(String bib, String pointId,
                                                long crossingTimeMs, String timestamp) {
        if (state != State.RUNNING)       return false;
        if (bib == null || bib.isEmpty()) return false;
        if (pointId == null || pointId.isEmpty()) return false;
        String key = crossingKey(bib, pointId);
        if (crossingRecords.containsKey(key)) return false;
        crossingRecords.put(key, new CrossingRecord(crossingTimeMs, timestamp));
        return true;
    }

    /**
     * Returns the crossing time in ms for the given bib / point pair,
     * or -1 if not yet crossed.
     */
    public long getCrossingTimeMs(String bib, String pointId) {
        CrossingRecord r = crossingRecords.get(crossingKey(bib, pointId));
        return r != null ? r.crossingTimeMs : -1;
    }

    /** Returns the crossing timestamp string, or empty string if not yet crossed. */
    public String getCrossingTimestamp(String bib, String pointId) {
        CrossingRecord r = crossingRecords.get(crossingKey(bib, pointId));
        return r != null ? r.crossingTimestamp : "";
    }

    /** Returns true if the bib has crossed the given timing point. */
    public boolean hasCrossed(String bib, String pointId) {
        return crossingRecords.containsKey(crossingKey(bib, pointId));
    }

    /**
     * Returns an unmodifiable snapshot of all crossing keys ("bib|pointId") → crossingTimeMs.
     * Used by the results exporter to build split columns.
     */
    public synchronized Map<String, Long> getCrossingsSnapshot() {
        Map<String, Long> snap = new LinkedHashMap<>();
        for (Map.Entry<String, CrossingRecord> e : crossingRecords.entrySet()) {
            snap.put(e.getKey(), e.getValue().crossingTimeMs);
        }
        return Collections.unmodifiableMap(snap);
    }
}
