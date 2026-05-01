import java.util.concurrent.ConcurrentHashMap;

/**
 * Suppresses duplicate tag reads within a configurable time window.
 *
 * At a finish line, one athlete crossing the mat may generate dozens of reads
 * per second.  This filter accepts the first read and silently drops any
 * further reads of the same EPC on the same timing point until the suppression
 * window expires — at which point the tag is eligible again (useful for lap
 * races or multi-lap events).
 *
 * Key  : "<epc>|<readerIp>"  — one independent slot per timing point
 * Value: System.currentTimeMillis() of the last accepted read
 *
 * Thread-safe: backed by ConcurrentHashMap, safe to call from any thread.
 */
public class DuplicateFilter {

    private final ConcurrentHashMap<String, Long> lastAccepted = new ConcurrentHashMap<>();
    private volatile int suppressedCount = 0;

    /**
     * Returns true if this read should be accepted (processed), false if it
     * should be suppressed as a duplicate.
     *
     * When accepted, the internal timestamp for this epc+readerIp pair is
     * updated so the window resets from now.
     */
    public boolean shouldAccept(String epc, String readerIp) {
        String key = epc + "|" + readerIp;
        long now = System.currentTimeMillis();
        long windowMs = AppConfig.getInstance().getSuppressionWindowMs();

        Long last = lastAccepted.get(key);
        if (last != null && (now - last) < windowMs) {
            suppressedCount++;
            return false;
        }

        lastAccepted.put(key, now);
        return true;
    }

    /**
     * Clears all stored timestamps and resets the suppressed counter.
     * Call between race waves so previously-seen tags are accepted again.
     */
    public void reset() {
        lastAccepted.clear();
        suppressedCount = 0;
    }

    /** Total number of reads suppressed since the last reset(). */
    public int getSuppressedCount() {
        return suppressedCount;
    }
}
