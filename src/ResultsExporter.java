import java.io.*;
import java.util.*;

/**
 * Exports race results to CSV.
 *
 * The output is sorted by net time ascending (fastest first).
 * Athletes whose wave gun has not yet fired appear at the bottom without a net time.
 */
public class ResultsExporter {

    /**
     * Builds an ordered list of result rows from the session and registry,
     * ready for either display (GUI table) or CSV export.
     *
     * Each row array has the columns:
     *   [0] place       (Integer)
     *   [1] bib         (String)
     *   [2] name        (String)
     *   [3] category    (String)
     *   [4] wave        (String)
     *   [5] gunStart    (String) — formatted
     *   [6] finishTime  (String) — formatted timestamp
     *   [7] netTime     (String) — formatted H:MM:SS.mmm
     *   [8] netTimeMs   (Long)   — raw ms for sorting; -1 if unavailable
     */
    public static List<Object[]> buildRows(RaceSession session,
                                           ParticipantRegistry registry) {
        List<Object[]> rows = new ArrayList<>();

        for (Map.Entry<String, Long> entry : session.getFinishTimesSnapshot().entrySet()) {
            String bib         = entry.getKey();
            String finishTs    = session.getFinishTimestamp(bib);

            Participant p      = registry.findByBib(bib);
            String name        = p != null ? p.getFullName()  : "";
            String category    = p != null ? p.getCategory()  : "";
            String waveId      = p != null ? p.getWaveId()    : "";

            Wave   wave        = session.getWaveForId(waveId);
            String waveName    = wave != null ? wave.getName()              : "";
            String gunStart    = wave != null ? wave.getFormattedGunTime()  : "";
            long   netMs       = session.getNetTimeMs(bib, waveId);
            String netFormatted = RaceSession.formatNetTime(netMs);

            rows.add(new Object[]{
                0,            // place — assigned after sort
                bib,
                name,
                category,
                waveName,
                gunStart,
                finishTs,
                netFormatted,
                netMs         // hidden sort key
            });
        }

        // Sort: net time ascending; -1 (no gun time) goes to the bottom
        rows.sort((a, b) -> {
            long aMs = (long) a[8];
            long bMs = (long) b[8];
            if (aMs < 0 && bMs < 0) return 0;
            if (aMs < 0) return 1;
            if (bMs < 0) return -1;
            return Long.compare(aMs, bMs);
        });

        // Assign places (only to rows with a valid net time)
        int place = 1;
        for (Object[] row : rows) {
            row[0] = (long) row[8] >= 0 ? place++ : -1;
        }

        return rows;
    }

    /**
     * Writes the results to a CSV file.
     * Only the first 8 columns are written (the hidden sort key is excluded).
     *
     * @throws IOException if the file cannot be written.
     */
    public static void exportCsv(File file, RaceSession session,
                                  ParticipantRegistry registry) throws IOException {

        List<Object[]> rows = buildRows(session, registry);

        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.println("place,bib,name,category,wave,gunStart,finishTime,netTime");
            for (Object[] row : rows) {
                String place = ((int) (Integer) row[0]) > 0 ? String.valueOf(row[0]) : "";
                pw.printf("%s,%s,%s,%s,%s,%s,%s,%s%n",
                        place,
                        csv(row[1]),
                        csv(row[2]),
                        csv(row[3]),
                        csv(row[4]),
                        csv(row[5]),
                        csv(row[6]),
                        csv(row[7]));
            }
        }
    }

    // ------------------------------------------------------------------ //
    // Split-aware overloads (Phase 5)
    // ------------------------------------------------------------------ //

    /**
     * Returns the column-header array for a result table that may include
     * split columns between "Gun Start" and "Finish Time".
     *
     * @param splitPoints ordered list of SPLIT-type timing points; null/empty = no splits
     */
    public static String[] buildColumnHeaders(List<TimingPoint> splitPoints) {
        int n = (splitPoints != null) ? splitPoints.size() : 0;
        String[] h = new String[8 + n];
        h[0] = "Place";
        h[1] = "Bib";
        h[2] = "Name";
        h[3] = "Category";
        h[4] = "Wave";
        h[5] = "Gun Start";
        for (int i = 0; i < n; i++) h[6 + i] = splitPoints.get(i).getName();
        h[6 + n] = "Finish Time";
        h[7 + n] = "Net Time";
        return h;
    }

    /**
     * Builds result rows with an optional split-time column for each entry in
     * {@code splitPoints}. Split time is cumulative time from the wave gun to
     * the athlete's first crossing of that timing point.
     *
     * Row layout (length = 9 + n, where n = splitPoints.size()):
     *   [0]       place      (Integer; -1 if no net time)
     *   [1]       bib        (String)
     *   [2]       name       (String)
     *   [3]       category   (String)
     *   [4]       wave       (String)
     *   [5]       gunStart   (String)
     *   [6..5+n]  splits     (String, formatted; "" if not yet crossed)
     *   [6+n]     finishTime (String)
     *   [7+n]     netTime    (String)
     *   [8+n]     netTimeMs  (Long)  — hidden sort key
     *
     * @param splitPoints ordered SPLIT-type timing points; null/empty = no splits
     */
    public static List<Object[]> buildRows(RaceSession session,
                                           ParticipantRegistry registry,
                                           List<TimingPoint> splitPoints) {
        int n = (splitPoints != null) ? splitPoints.size() : 0;
        List<Object[]> rows = new ArrayList<>();

        for (Map.Entry<String, Long> entry : session.getFinishTimesSnapshot().entrySet()) {
            String bib      = entry.getKey();
            String finishTs = session.getFinishTimestamp(bib);

            Participant p   = registry.findByBib(bib);
            String name     = p != null ? p.getFullName()  : "";
            String category = p != null ? p.getCategory()  : "";
            String waveId   = p != null ? p.getWaveId()    : "";

            Wave   wave     = session.getWaveForId(waveId);
            String waveName = wave != null ? wave.getName()             : "";
            String gunStart = wave != null ? wave.getFormattedGunTime() : "";
            long   netMs    = session.getNetTimeMs(bib, waveId);
            String netFmt   = RaceSession.formatNetTime(netMs);

            Object[] row = new Object[9 + n];
            row[0] = 0;       // place — assigned after sort
            row[1] = bib;
            row[2] = name;
            row[3] = category;
            row[4] = waveName;
            row[5] = gunStart;

            // One split column per point
            for (int i = 0; i < n; i++) {
                TimingPoint sp = splitPoints.get(i);
                long crossMs   = session.getCrossingTimeMs(bib, sp.getId());
                if (crossMs >= 0 && wave != null && wave.hasFired()) {
                    long splitNet = crossMs - wave.getGunTimeMs();
                    row[6 + i] = splitNet >= 0 ? RaceSession.formatNetTime(splitNet) : "";
                } else {
                    row[6 + i] = "";
                }
            }

            row[6 + n] = finishTs;
            row[7 + n] = netFmt;
            row[8 + n] = netMs;   // hidden sort key
            rows.add(row);
        }

        final int sortIdx = 8 + n;
        rows.sort((a, b) -> {
            long aMs = (long) a[sortIdx];
            long bMs = (long) b[sortIdx];
            if (aMs < 0 && bMs < 0) return 0;
            if (aMs < 0) return 1;
            if (bMs < 0) return -1;
            return Long.compare(aMs, bMs);
        });

        int place = 1;
        for (Object[] row : rows) {
            row[0] = (long) row[sortIdx] >= 0 ? place++ : -1;
        }

        return rows;
    }

    /**
     * Exports results to CSV with dynamic split columns.
     *
     * @param splitPoints ordered SPLIT-type timing points; null/empty = no splits
     * @throws IOException if the file cannot be written
     */
    public static void exportCsv(File file, RaceSession session,
                                  ParticipantRegistry registry,
                                  List<TimingPoint> splitPoints) throws IOException {
        int n = (splitPoints != null) ? splitPoints.size() : 0;
        List<Object[]> rows    = buildRows(session, registry, splitPoints);
        String[]       headers = buildColumnHeaders(splitPoints);

        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.println(String.join(",", headers));
            for (Object[] row : rows) {
                int place = (row[0] instanceof Integer) ? (Integer) row[0] : -1;
                StringBuilder sb = new StringBuilder(place > 0 ? String.valueOf(place) : "");
                for (int i = 1; i < 8 + n; i++) {
                    sb.append(',').append(csv(row[i]));
                }
                pw.println(sb);
            }
        }
    }

    private static String csv(Object v) {
        if (v == null) return "";
        String s = v.toString();
        return s.contains(",") ? "\"" + s + "\"" : s;
    }
}
