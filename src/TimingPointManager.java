import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton that owns the ordered list of timing points and manages
 * one RFID reader connection per timing point.
 *
 * Timing points are persisted to/from AppConfig under the key
 * "timingpoints" as a semicolon-separated list of serialised
 * {@link TimingPoint#toConfigString()} values.
 *
 * Thread-safe: all mutating operations are synchronized on this instance.
 */
public class TimingPointManager {

    private static TimingPointManager instance;

    /** Ordered list of all configured timing points. */
    private final List<TimingPoint> points = new ArrayList<>();

    /**
     * Active readers keyed by timing-point id.
     * Value is the reader currently assigned to that point (may be connecting,
     * connected, or disconnected — check {@link ReaderInterface#isConnected()}).
     */
    private final Map<String, ReaderInterface> activeReaders = new ConcurrentHashMap<>();

    /**
     * Quick lookup: readerIp → TimingPoint.
     * Kept in sync whenever points are added/removed or IPs change.
     */
    private final Map<String, TimingPoint> ipIndex = new ConcurrentHashMap<>();

    // ------------------------------------------------------------------ //
    // Singleton
    // ------------------------------------------------------------------ //

    private TimingPointManager() {}

    public static synchronized TimingPointManager getInstance() {
        if (instance == null) instance = new TimingPointManager();
        return instance;
    }

    // ------------------------------------------------------------------ //
    // Point management
    // ------------------------------------------------------------------ //

    /** Adds a new timing point (appended to end). Returns false if id already exists. */
    public synchronized boolean addPoint(TimingPoint point) {
        if (point == null) return false;
        if (findById(point.getId()) != null) return false;
        point.setOrder(points.size());
        points.add(point);
        indexIp(point);
        return true;
    }

    /** Replaces an existing point's mutable fields in-place. */
    public synchronized boolean updatePoint(String id, String name, String readerIp,
                                             TimingPoint.PointType type) {
        TimingPoint p = findById(id);
        if (p == null) return false;
        // Re-index IP if it changed
        if (!p.getReaderIp().equals(readerIp)) {
            ipIndex.remove(p.getReaderIp());
        }
        p.setName(name);
        p.setReaderIp(readerIp);
        p.setType(type);
        indexIp(p);
        return true;
    }

    /**
     * Removes a timing point by id, disconnecting its reader first if active.
     * Returns false if the id was not found.
     */
    public synchronized boolean removePoint(String id) {
        TimingPoint p = findById(id);
        if (p == null) return false;
        disconnectPoint(id);          // no-op if not connected
        ipIndex.remove(p.getReaderIp());
        points.remove(p);
        reorderPoints();
        return true;
    }

    /** Moves a point up (lower order) by one position. */
    public synchronized boolean moveUp(String id) {
        int idx = indexOfId(id);
        if (idx <= 0) return false;
        Collections.swap(points, idx, idx - 1);
        reorderPoints();
        return true;
    }

    /** Moves a point down (higher order) by one position. */
    public synchronized boolean moveDown(String id) {
        int idx = indexOfId(id);
        if (idx < 0 || idx >= points.size() - 1) return false;
        Collections.swap(points, idx, idx + 1);
        reorderPoints();
        return true;
    }

    /** Returns an unmodifiable snapshot of the point list in order. */
    public synchronized List<TimingPoint> getPointsSnapshot() {
        return Collections.unmodifiableList(new ArrayList<>(points));
    }

    /** Returns the number of configured timing points. */
    public synchronized int getPointCount() { return points.size(); }

    // ------------------------------------------------------------------ //
    // IP-based lookup  (hot path — called for every tag read)
    // ------------------------------------------------------------------ //

    /**
     * Finds the timing point whose configured readerIp matches the given IP.
     * Returns {@code null} if none is found.
     */
    public TimingPoint findByIp(String readerIp) {
        if (readerIp == null) return null;
        return ipIndex.get(readerIp.trim());
    }

    /** Finds a point by its unique id. */
    public synchronized TimingPoint findById(String id) {
        if (id == null) return null;
        return points.stream().filter(p -> p.getId().equals(id)).findFirst().orElse(null);
    }

    // ------------------------------------------------------------------ //
    // Reader connection management
    // ------------------------------------------------------------------ //

    /**
     * Connects the reader assigned to the given timing point.
     * The caller provides the {@link ReaderInterface} implementation to use;
     * this allows the GUI to supply the appropriate adapter (Impinj or Zebra).
     *
     * @param pointId  timing point id
     * @param reader   pre-constructed reader adapter (not yet connected)
     */
    public synchronized void connectPoint(String pointId, ReaderInterface reader) {
        TimingPoint p = findById(pointId);
        if (p == null || p.getReaderIp().isBlank()) return;
        activeReaders.put(pointId, reader);
        // Connection is async — caller's listener will receive onReaderStatus callbacks
        reader.connect(p.getReaderIp());
    }

    /**
     * Disconnects and removes the active reader for the given timing point.
     */
    public synchronized void disconnectPoint(String pointId) {
        ReaderInterface r = activeReaders.remove(pointId);
        if (r != null) {
            try { r.disconnect(); } catch (Exception ignored) {}
        }
    }

    /** Disconnects all active readers. */
    public synchronized void disconnectAll() {
        for (String id : new ArrayList<>(activeReaders.keySet())) {
            disconnectPoint(id);
        }
    }

    /** Returns true if the reader for the given point is currently connected. */
    public boolean isPointConnected(String pointId) {
        ReaderInterface r = activeReaders.get(pointId);
        return r != null && r.isConnected();
    }

    /** Returns the active ReaderInterface for a point, or {@code null} if none. */
    public ReaderInterface getReader(String pointId) {
        return activeReaders.get(pointId);
    }

    // ------------------------------------------------------------------ //
    // Persistence
    // ------------------------------------------------------------------ //

    /** Serialises the current list of timing points to AppConfig and saves. */
    public synchronized void saveToConfig() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < points.size(); i++) {
            if (i > 0) sb.append(';');
            sb.append(points.get(i).toConfigString());
        }
        AppConfig.getInstance().setTimingPoints(sb.toString());
        AppConfig.getInstance().save();
    }

    /** Loads timing points from AppConfig, replacing the current list. */
    public synchronized void loadFromConfig() {
        String raw = AppConfig.getInstance().getTimingPoints();
        points.clear();
        ipIndex.clear();
        if (raw == null || raw.isBlank()) return;
        String[] entries = raw.split(";", -1);
        for (String entry : entries) {
            TimingPoint p = TimingPoint.fromConfigString(entry.trim());
            if (p != null) {
                points.add(p);
                indexIp(p);
            }
        }
        reorderPoints();
    }

    // ------------------------------------------------------------------ //
    // Internal helpers
    // ------------------------------------------------------------------ //

    private void indexIp(TimingPoint p) {
        if (p.getReaderIp() != null && !p.getReaderIp().isBlank()) {
            ipIndex.put(p.getReaderIp().trim(), p);
        }
    }

    private void reorderPoints() {
        for (int i = 0; i < points.size(); i++) {
            points.get(i).setOrder(i);
        }
    }

    private int indexOfId(String id) {
        for (int i = 0; i < points.size(); i++) {
            if (points.get(i).getId().equals(id)) return i;
        }
        return -1;
    }
}
