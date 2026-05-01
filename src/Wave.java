import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * A race wave (start group).
 *
 * Each wave has its own gun time recorded when the start signal is given.
 * Net times for athletes in this wave are calculated as:
 *   netTimeMs = athleteFinishMs - gunTimeMs
 */
public class Wave {

    private static final DateTimeFormatter DISPLAY_FMT =
            DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private final String id;
    private final String name;
    private long gunTimeMs = -1; // -1 = not yet fired

    public Wave(String id, String name) {
        this.id   = id;
        this.name = name;
    }

    // ------------------------------------------------------------------ //
    // Gun control
    // ------------------------------------------------------------------ //

    /** Records the current system time as the gun time. */
    public void fire() {
        gunTimeMs = System.currentTimeMillis();
    }

    /** Records a specific epoch-millisecond timestamp as the gun time (for manual entry). */
    public void fireAt(long epochMs) {
        gunTimeMs = epochMs;
    }

    // ------------------------------------------------------------------ //
    // Getters
    // ------------------------------------------------------------------ //

    public String  getId()          { return id; }
    public String  getName()        { return name; }
    public long    getGunTimeMs()   { return gunTimeMs; }
    public boolean hasFired()       { return gunTimeMs >= 0; }

    /** Human-readable gun time, e.g. "09:30:00.000", or "—" if not yet fired. */
    public String getFormattedGunTime() {
        if (!hasFired()) return "\u2014"; // em-dash
        LocalDateTime ldt = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(gunTimeMs), ZoneId.systemDefault());
        return ldt.format(DISPLAY_FMT);
    }

    /** Displayed in JComboBox drop-downs. */
    @Override
    public String toString() {
        return hasFired() ? name + " [fired " + getFormattedGunTime() + "]" : name;
    }
}
