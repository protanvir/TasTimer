import com.impinj.octane.ImpinjReader;
import com.impinj.octane.OctaneSdkException;
import com.impinj.octane.Settings;
import com.impinj.octane.ReportConfig;
import com.impinj.octane.ReportMode;
import com.impinj.octane.TagReport;
import com.impinj.octane.TagReportListener;
import com.impinj.octane.Tag;
import com.impinj.octane.AntennaStatus;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class RFIDController implements TagReportListener {

    private ImpinjReader reader;
    private RFIDDataListener listener;
    private boolean isRunning = false;

    // Wiclax Config
    private static final String WICLAX_USER = "totalactivesports";
    private static final String WICLAX_DEVICE_ID = "tas245";

    public RFIDController(RFIDDataListener listener) {
        this.listener = listener;
        this.reader = new ImpinjReader();
    }

    public void connect(String hostname) {
        try {
            if (reader.isConnected()) {
                disconnect();
            }

            notifyStatus("Connecting to " + hostname + "...", false);
            reader.connect(hostname);

            Settings settings = reader.queryDefaultSettings();
            ReportConfig report = settings.getReport();
            report.setIncludeAntennaPortNumber(true);
            report.setMode(ReportMode.Individual);
            reader.applySettings(settings);
            reader.setTagReportListener(this);

            notifyStatus("Connected to " + hostname, true);

            // Query and notify antenna status
            try {
                // Correct API chain found via research
                List<AntennaStatus> antennaStatuses = reader.queryStatus()
                        .getAntennaStatusGroup()
                        .getAntennaList();
                if (listener != null) {
                    listener.onAntennaStatus(antennaStatuses);
                }
            } catch (Exception e) {
                System.err.println("Failed to query antenna status: " + e.getMessage());
            }

        } catch (OctaneSdkException e) {
            notifyStatus("Connection Failed: " + e.getMessage(), false);
        } catch (Exception e) {
            notifyStatus("Error: " + e.getMessage(), false);
        }
    }

    public void disconnect() {
        try {
            if (reader.isConnected()) {
                reader.disconnect();
            }
            notifyStatus("Disconnected", false);
        } catch (Exception e) {
            notifyStatus("Disconnect Error: " + e.getMessage(), false);
        }
    }

    public void startReading() {
        try {
            if (!reader.isConnected()) {
                notifyStatus("Not connected!", false);
                return;
            }
            reader.start();
            isRunning = true;
            notifyStatus("Reading Tags...", true);
        } catch (Exception e) {
            notifyStatus("Start Failed: " + e.getMessage(), true);
        }
    }

    public void stopReading() {
        try {
            if (reader.isConnected()) {
                reader.stop();
            }
            isRunning = false;
            notifyStatus("Stopped Reading", true);
        } catch (Exception e) {
            notifyStatus("Stop Failed: " + e.getMessage(), true);
        }
    }

    // --- Tag Processing ---

    @Override
    public void onTagReported(ImpinjReader reader, TagReport report) {
        List<Tag> tags = report.getTags();
        for (Tag t : tags) {
            String epc = t.getEpc().toHexString();
            int antennaPort = t.getAntennaPortNumber();

            // Process Data pipeline
            processTag(epc, reader.getAddress(), antennaPort);
        }
    }

    private void processTag(String epc, String readerAddress, int antennaPort) {
        // 1. Format Data
        String shortEpc = epc.length() > 8 ? epc.substring(epc.length() - 8) : epc;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        String timestamp = LocalDateTime.now().format(formatter);

        // 2. Notify GUI
        if (listener != null) {
            listener.onTagRead(shortEpc, timestamp, readerAddress, antennaPort);
        }

        // 3. Send to Local REST API
        sendToLocalApi(shortEpc, timestamp);

        // 4. Send to Wiclax
        sendToWiclax(shortEpc);

        // 5. Save to CSV
        saveToCsv(shortEpc, timestamp);
    }

    // --- Data Services ---

    private void sendToLocalApi(String epc, String timestamp) {
        // Run in thread to avoid blocking reader callback
        new Thread(() -> {
            try {
                URL url = new URL("http://localhost:3000/api/tags");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                String jsonInputString = String.format(
                        "{\"epc\": \"%s\", \"timestamp\": \"%s\"}",
                        epc, timestamp);

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
                conn.getResponseCode(); // Trigger request
            } catch (Exception e) {
                System.err.println("Local API Error: " + e.getMessage());
            }
        }).start();
    }

    private void sendToWiclax(String epc) {
        new Thread(() -> {
            try {
                DateTimeFormatter wiclaxFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
                String timestamp = LocalDateTime.now().format(wiclaxFormatter);
                String passingData = String.format("@%s@%s", epc, timestamp);

                String query = String.format("user=%s&deviceId=%s&passings=%s",
                        WICLAX_USER, WICLAX_DEVICE_ID, passingData);

                URL url = new URL("http://wiclax.com:35014?" + query);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.getResponseCode(); // Trigger request
            } catch (Exception e) {
                System.err.println("Wiclax Error: " + e.getMessage());
            }
        }).start();
    }

    private synchronized void saveToCsv(String epc, String timestamp) {
        try {
            File dataDir = new File("data");
            if (!dataDir.exists())
                dataDir.mkdirs();
            File csvFile = new File(dataDir, "tag_reads.csv");

            try (FileWriter fw = new FileWriter(csvFile, true);
                    PrintWriter pw = new PrintWriter(fw)) {
                pw.println(epc + "," + timestamp);
            }
        } catch (IOException e) {
            System.err.println("CSV Error: " + e.getMessage());
        }
    }

    private void notifyStatus(String msg, boolean isConnected) {
        if (listener != null) {
            listener.onReaderStatus(msg, isConnected);
        }
    }
}
