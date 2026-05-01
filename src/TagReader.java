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
import java.net.URI;
import java.net.URL;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.File;
import java.io.IOException;

/**
 * Standalone CLI tag reader (Impinj only).
 * All configuration is read from AppConfig / config.properties.
 */
public class TagReader implements TagReportListener {

    public static void main(String[] args) {
        AppConfig config = AppConfig.getInstance();
        String hostname = args.length > 0 ? args[0] : config.getReaderIp();

        try {
            new TagReader().run(hostname);
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

            Settings settings = reader.queryDefaultSettings();
            ReportConfig report = settings.getReport();
            report.setIncludeAntennaPortNumber(true);
            report.setMode(ReportMode.Individual);
            reader.applySettings(settings);
            reader.setTagReportListener(this);

            System.out.println("Starting reader...");
            reader.start();

            System.out.println("Press Enter to exit.");
            new Scanner(System.in).nextLine();

            reader.stop();
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
        AppConfig config = AppConfig.getInstance();
        List<Tag> tags = report.getTags();

        for (Tag t : tags) {
            String rawEpc = t.getEpc().toHexString();
            String epc    = config.formatEpc(rawEpc);
            System.out.println("EPC: " + epc);

            sendTagData(epc, reader.getAddress());
            sendToWiclax(epc);
            saveToCsv(epc);
        }
    }

    private void saveToCsv(String epc) {
        AppConfig config = AppConfig.getInstance();
        try {
            File dataDir = new File(config.getCsvDir());
            if (!dataDir.exists()) dataDir.mkdirs();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
            String timestamp = LocalDateTime.now().format(formatter);

            try (FileWriter fw = new FileWriter(new File(dataDir, "tag_reads.csv"), true);
                 PrintWriter pw = new PrintWriter(fw)) {
                pw.println(epc + "," + timestamp);
            }
        } catch (IOException e) {
            System.err.println("Failed to write to CSV: " + e.getMessage());
        }
    }

    private void sendToWiclax(String epc) {
        AppConfig config = AppConfig.getInstance();
        if (!config.isWiclaxEnabled()) return;

        try {
            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
            String passingData = String.format("@%s@%s", epc, timestamp);
            String query = String.format("user=%s&deviceId=%s&passings=%s",
                    config.getWiclaxUser(), config.getWiclaxDeviceId(), passingData);

            URL url = URI.create(config.getWiclaxUrl() + ":" + config.getWiclaxPort() + "?" + query).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            int responseCode = conn.getResponseCode();
            System.out.println("Wiclax Response: " + responseCode);
        } catch (Exception e) {
            System.err.println("Failed to send to Wiclax: " + e.getMessage());
        }
    }

    private void sendTagData(String epc, String readerAddress) {
        AppConfig config = AppConfig.getInstance();
        if (!config.isLocalApiEnabled()) return;

        try {
            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
            String json = String.format("{\"epc\":\"%s\",\"timestamp\":\"%s\"}", epc, timestamp);

            URL url = URI.create("http://localhost:" + config.getLocalApiPort() + config.getLocalApiEndpoint()).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes(StandardCharsets.UTF_8));
            }
            conn.getResponseCode();
        } catch (Exception e) {
            System.err.println("Failed to send tag data: " + e.getMessage());
        }
    }
}
