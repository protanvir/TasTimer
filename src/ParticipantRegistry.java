import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory participant store backed by a CSV file.
 *
 * Expected CSV columns (with or without a header row):
 *   bib, firstName, lastName, epc, category, wave
 *
 * EPC column is optional — participants can be imported without tags and
 * assigned later via assignEpc().
 *
 * Lookup by EPC uses a suffix match so that LAST_N formatted EPCs (e.g.
 * "1234ABCD") correctly resolve against full EPCs stored in the CSV
 * (e.g. "E2800115200000001234ABCD").  Exact matches are tried first.
 *
 * Thread-safe: all mutable state is in ConcurrentHashMaps; markRead() uses
 * volatile fields on Participant.
 */
public class ParticipantRegistry {

    private static ParticipantRegistry instance;

    // Primary index: full uppercase EPC → Participant
    private final Map<String, Participant> byEpc = new ConcurrentHashMap<>();
    // Secondary index: bib number → Participant
    private final Map<String, Participant> byBib = new ConcurrentHashMap<>();

    private ParticipantRegistry() {}

    public static synchronized ParticipantRegistry getInstance() {
        if (instance == null) instance = new ParticipantRegistry();
        return instance;
    }

    // ------------------------------------------------------------------ //
    // Import
    // ------------------------------------------------------------------ //

    /**
     * Loads participants from a CSV file, replacing any previously loaded data.
     * Skips rows where the bib column is blank.
     * Auto-detects and skips a header row if the first column looks like text
     * rather than a bib number.
     */
    public synchronized void loadFromCsv(File file) throws IOException {
        Map<String, Participant> newByEpc = new LinkedHashMap<>();
        Map<String, Participant> newByBib = new LinkedHashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] cols = line.split(",", -1);

                // Skip header row: if the first column is "bib" (case-insensitive)
                // or is completely non-numeric on the very first line
                if (firstLine) {
                    firstLine = false;
                    String first = cols[0].trim().toLowerCase();
                    if (first.equals("bib") || first.equals("bibno")
                            || first.equals("bib number") || first.equals("bibnumber")) {
                        continue;
                    }
                }

                String bib      = get(cols, 0);
                String firstName = get(cols, 1);
                String lastName  = get(cols, 2);
                String epc       = get(cols, 3).toUpperCase();
                String category  = get(cols, 4);
                String wave      = get(cols, 5);

                if (bib.isEmpty()) continue;

                Participant p = new Participant(bib, firstName, lastName,
                                               epc, category, wave);
                newByBib.put(bib, p);
                if (!epc.isEmpty()) {
                    newByEpc.put(epc, p);
                }
            }
        }

        byEpc.clear();
        byBib.clear();
        byEpc.putAll(newByEpc);
        byBib.putAll(newByBib);
    }

    // ------------------------------------------------------------------ //
    // Lookup
    // ------------------------------------------------------------------ //

    /**
     * Finds a participant by EPC.
     * Tries exact match first, then suffix match to handle LAST_N display mode.
     */
    public Participant findByEpc(String epc) {
        if (epc == null || epc.isEmpty()) return null;
        String upper = epc.toUpperCase();

        // 1. Exact match
        Participant p = byEpc.get(upper);
        if (p != null) return p;

        // 2. Suffix match (formatted EPC is a tail of the stored full EPC)
        for (Map.Entry<String, Participant> entry : byEpc.entrySet()) {
            if (entry.getKey().endsWith(upper)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public Participant findByBib(String bib) {
        if (bib == null || bib.isEmpty()) return null;
        return byBib.get(bib.trim());
    }

    // ------------------------------------------------------------------ //
    // Mutations
    // ------------------------------------------------------------------ //

    /**
     * Assigns an EPC to a participant identified by bib number.
     * Removes any previous EPC mapping for that participant.
     */
    public synchronized void assignEpc(String bib, String epc) {
        Participant p = byBib.get(bib.trim());
        if (p == null) return;

        // Remove old EPC entry
        if (!p.getEpc().isEmpty()) {
            byEpc.remove(p.getEpc());
        }

        String upperEpc = epc.toUpperCase();
        p.setEpc(upperEpc);
        if (!upperEpc.isEmpty()) {
            byEpc.put(upperEpc, p);
        }
    }

    /** Marks the participant with the given EPC as read at the given timestamp. */
    public void markRead(String epc, String timestamp) {
        Participant p = findByEpc(epc);
        if (p != null) p.markRead(timestamp);
    }

    // ------------------------------------------------------------------ //
    // Queries
    // ------------------------------------------------------------------ //

    /** Returns all participants in import order. */
    public Collection<Participant> getAll() {
        return Collections.unmodifiableCollection(byBib.values());
    }

    public int getTotalCount() { return byBib.size(); }

    public int getReadCount() {
        return (int) byBib.values().stream().filter(Participant::hasBeenRead).count();
    }

    public boolean isEmpty() { return byBib.isEmpty(); }

    /** Clears all participant data (useful for reloading). */
    public synchronized void clear() {
        byEpc.clear();
        byBib.clear();
    }

    // ------------------------------------------------------------------ //
    // Export
    // ------------------------------------------------------------------ //

    /**
     * Exports all participants with their read status to a CSV file.
     * Columns: bib, firstName, lastName, epc, category, wave, status, lastReadTime
     */
    public void exportStatusCsv(File file) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.println("bib,firstName,lastName,epc,category,wave,status,lastReadTime");
            for (Participant p : byBib.values()) {
                pw.printf("%s,%s,%s,%s,%s,%s,%s,%s%n",
                        csv(p.getBibNumber()),
                        csv(p.getFirstName()),
                        csv(p.getLastName()),
                        csv(p.getEpc()),
                        csv(p.getCategory()),
                        csv(p.getWaveId()),
                        p.hasBeenRead() ? "Read" : "Waiting",
                        csv(p.getLastReadTime()));
            }
        }
    }

    // ------------------------------------------------------------------ //
    // Helpers
    // ------------------------------------------------------------------ //

    /** Safe column extractor — returns empty string if column is missing. */
    private static String get(String[] cols, int index) {
        if (index >= cols.length) return "";
        return cols[index].trim();
    }

    /** Wraps a value in quotes if it contains a comma. */
    private static String csv(String v) {
        if (v == null) return "";
        return v.contains(",") ? "\"" + v + "\"" : v;
    }
}
