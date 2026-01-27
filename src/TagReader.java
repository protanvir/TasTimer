import com.impinj.octane.ImpinjReader;
import com.impinj.octane.OctaneSdkException;
import com.impinj.octane.Settings;
import com.impinj.octane.ReportConfig;
import com.impinj.octane.ReportMode;
import com.impinj.octane.TagReport;
import com.impinj.octane.TagReportListener;
import com.impinj.octane.Tag;
import java.util.Scanner;
import java.util.List;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.File;
import java.io.IOException;

public class TagReader implements TagReportListener {

    private static final String WICLAX_USER = "totalactivesports";
    private static final String WICLAX_DEVICE_ID = "tas245";

    public static void main(String[] args) {
        // Default IP, can be overridden by arg
        String hostname = "172.16.1.114";
        if (args.length > 0) {
            hostname = args[0];
        }

        try {
            TagReader tagReader = new TagReader();
            tagReader.run(hostname);
        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void run(String hostname) {
        System.out.println("Connecting to " + hostname);
        ImpinjReader reader = new ImpinjReader();

        try {
            reader.connect(hostname);
            System.out.println("Connected.");

            System.out.println("Querying default settings...");
            Settings settings = reader.queryDefaultSettings();

            System.out.println("Configuring report settings...");
            ReportConfig report = settings.getReport();
            report.setIncludeAntennaPortNumber(true);
            report.setMode(ReportMode.Individual);

            System.out.println("Applying settings...");
            reader.applySettings(settings);

            System.out.println("Setting tag report listener...");
            reader.setTagReportListener(this);

            System.out.println("Starting reader...");
            reader.start();

            System.out.println("Press Enter to exit.");
            Scanner s = new Scanner(System.in);
            s.nextLine();

            System.out.println("Stopping reader...");
            reader.stop();
            System.out.println("Disconnecting...");
            reader.disconnect();
            System.out.println("Done.");

        } catch (OctaneSdkException e) {
            System.err.println("SDK Error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onTagReported(ImpinjReader reader, TagReport report) {
        List<Tag> tags = report.getTags();
        for (Tag t : tags) {
            String epc = t.getEpc().toHexString();
            System.out.println("EPC: " + epc);

            // Send to REST API
            sendTagData(epc, reader.getAddress());

            // Send to Wiclax
            sendToWiclax(epc);

            // Save to CSV Backup
            saveToCsv(epc);
        }
    }

    private void saveToCsv(String epc) {
        try {
            // Ensure data directory exists
            File dataDir = new File("data");
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }

            File csvFile = new File(dataDir, "tag_reads.csv");

            // Format timestamp matching requirements
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
            String timestamp = LocalDateTime.now().format(formatter);

            // Extract last 8 digits for consistency with payload (optional, or keep full
            // EPC)
            // Sticking to full EPC or requested format?
            // User asked for "same data format", so likely last 8 digits + custom
            // timestamp.
            String shortEpc = epc;
            if (epc.length() > 8) {
                shortEpc = epc.substring(epc.length() - 8);
            }

            try (FileWriter fw = new FileWriter(csvFile, true);
                    PrintWriter pw = new PrintWriter(fw)) {
                pw.println(shortEpc + "," + timestamp);
            }
        } catch (IOException e) {
            System.err.println("Failed to write to CSV: " + e.getMessage());
        }
    }

    private void sendToWiclax(String epc) {
        try {
            // Wiclax time format: YYYYMMDDHHmmssSSS
            DateTimeFormatter wiclaxFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
            String timestamp = LocalDateTime.now().format(wiclaxFormatter);

            // Format: <num>@<chip_id>@<time>
            // We leave num (bib) empty
            String passingData = String.format("@%s@%s", epc, timestamp);

            String query = String.format("user=%s&deviceId=%s&passings=%s",
                    WICLAX_USER, WICLAX_DEVICE_ID, passingData);

            URL url = new URL("http://wiclax.com:35014?" + query);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            System.out.println("Wiclax Response: " + responseCode);
            if (responseCode == 200) {
                // Optional: read response body to check for 'ok'
                // try (java.io.InputStream is = conn.getInputStream()) { ... }
            }

        } catch (Exception e) {
            System.err.println("Failed to send to Wiclax: " + e.getMessage());
        }
    }

    private void sendTagData(String epc, String readerAddress) {
        try {
            // Extract last 8 digits of EPC
            String shortEpc = epc;
            if (epc.length() > 8) {
                shortEpc = epc.substring(epc.length() - 8);
            }

            // Format timestamp: YYYY-MM-DD HH:MM:SS.mili
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
            String timestamp = LocalDateTime.now().format(formatter);

            URL url = new URL("http://localhost:3000/api/tags");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // Construct JSON manually to match requirements
            String jsonInputString = String.format(
                    "{\"epc\": \"%s\", \"timestamp\": \"%s\"}",
                    shortEpc, timestamp);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            // System.out.println("API Response Code: " + responseCode);

        } catch (Exception e) {
            System.err.println("Failed to send tag data: " + e.getMessage());
        }
    }
}
