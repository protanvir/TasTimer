import com.impinj.octane.ImpinjReader;
import com.impinj.octane.OctaneSdkException;
import com.impinj.octane.Settings;
import com.impinj.octane.ReportConfig;
import com.impinj.octane.ReportMode;
import com.impinj.octane.TagReport;
import com.impinj.octane.TagReportListener;
import com.impinj.octane.Tag;
import com.impinj.octane.AntennaStatus;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class RFIDController implements TagReportListener, ReaderInterface {

    private ImpinjReader reader;
    private RFIDDataListener listener;
    private boolean isRunning = false;

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
                    List<UnifiedAntennaStatus> unifiedStatuses = new ArrayList<>();
                    for (AntennaStatus as : antennaStatuses) {
                        // Assuming port number is 1-based index or using explicit port number from
                        // object
                        // AntennaStatus in Octane usually has 'portNumber' or similar.
                        // Checking docs or previous usages: t.getAntennaPortNumber() exists on Tag.
                        // Validating AntennaStatus methods:
                        // It has getPortNumber() and isConnected().
                        unifiedStatuses.add(new UnifiedAntennaStatus(as.getPortNumber(), as.isConnected(),
                                as.isConnected() ? "Connected" : "Disconnected"));
                    }
                    listener.onAntennaStatus(unifiedStatuses);
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

    @Override
    public boolean isConnected() {
        return reader != null && reader.isConnected();
    }

    // --- Tag Processing ---

    @Override
    public void onTagReported(ImpinjReader reader, TagReport report) {
        List<Tag> tags = report.getTags();
        for (Tag t : tags) {
            String epc = t.getEpc().toHexString();
            int antennaPort = t.getAntennaPortNumber();

            // Format Data
            String shortEpc = epc.length() > 8 ? epc.substring(epc.length() - 8) : epc;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
            String timestamp = LocalDateTime.now().format(formatter);

            // Notify GUI
            if (listener != null) {
                listener.onTagRead(shortEpc, timestamp, reader.getAddress(), antennaPort);
            }
        }
    }

    private void notifyStatus(String msg, boolean isConnected) {
        if (listener != null) {
            listener.onReaderStatus(msg, isConnected);
        }
    }
}
