import java.util.Objects;

/**
 * Represents a single timing point on a race course.
 * Each timing point is associated with one RFID reader (by IP address)
 * and has a defined role in the race (START, SPLIT, or FINISH).
 */
public class TimingPoint {

    public enum PointType {
        START, SPLIT, FINISH
    }

    private final String id;        // unique identifier (UUID or user-assigned)
    private String name;            // display name, e.g. "Start Line", "5 km Split"
    private String readerIp;        // IP address of the RFID reader at this point
    private PointType type;         // START / SPLIT / FINISH
    private int order;              // display / processing order (0-based)

    public TimingPoint(String id, String name, String readerIp, PointType type, int order) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("id required");
        this.id       = id;
        this.name     = name  != null ? name     : "";
        this.readerIp = readerIp != null ? readerIp : "";
        this.type     = type  != null ? type     : PointType.FINISH;
        this.order    = order;
    }

    // --- Getters ---

    public String    getId()       { return id; }
    public String    getName()     { return name; }
    public String    getReaderIp() { return readerIp; }
    public PointType getType()     { return type; }
    public int       getOrder()    { return order; }

    // --- Setters (id is immutable) ---

    public void setName(String name)         { this.name     = name     != null ? name     : ""; }
    public void setReaderIp(String readerIp) { this.readerIp = readerIp != null ? readerIp : ""; }
    public void setType(PointType type)      { this.type     = type     != null ? type     : PointType.FINISH; }
    public void setOrder(int order)          { this.order    = order; }

    /** Serialise to a compact string for storage in config.properties. */
    public String toConfigString() {
        // format: id|name|readerIp|type|order
        return id + "|" + esc(name) + "|" + esc(readerIp) + "|" + type.name() + "|" + order;
    }

    /**
     * Deserialise from a config string produced by {@link #toConfigString()}.
     * Returns {@code null} if the string is malformed.
     */
    public static TimingPoint fromConfigString(String s) {
        if (s == null || s.isBlank()) return null;
        String[] parts = s.split("\\|", -1);
        if (parts.length < 5) return null;
        try {
            String   id       = parts[0];
            String   name     = unesc(parts[1]);
            String   readerIp = unesc(parts[2]);
            PointType type    = PointType.valueOf(parts[3]);
            int      order    = Integer.parseInt(parts[4]);
            return new TimingPoint(id, name, readerIp, type, order);
        } catch (Exception e) {
            return null;
        }
    }

    /** Escape pipe characters inside field values. */
    private static String esc(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("|", "\\|");
    }

    private static String unesc(String s) {
        return s == null ? "" : s.replace("\\|", "|").replace("\\\\", "\\");
    }

    @Override
    public String toString() {
        return name.isBlank() ? id : name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimingPoint)) return false;
        return id.equals(((TimingPoint) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
