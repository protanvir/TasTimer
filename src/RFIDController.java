import com.impinj.octane.ImpinjReader;
import com.impinj.octane.OctaneSdkException;
import com.impinj.octane.Settings;
import com.impinj.octane.ReportConfig;
import com.impinj.octane.ReportMode;
import com.impinj.octane.TagReport;
import com.impinj.octane.TagReportListener;
import com.impinj.octane.ConnectionLostListener;
import com.impinj.octane.Tag;
import com.impinj.octane.AntennaStatus;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class RFIDController implements TagReportListener, ConnectionLostListener, ReaderInterface {

    private ImpinjReader reader;
    private RFIDDataListener listener;
    private volatile boolean isRunning = false;

    // Auto-reconnect state
    private volatile boolean intentionalDisconnect = false;
    private volatile boolean wasReading            = false;
    private volatile String  hostname              = "";

    private static final int[] RECONNECT_DELAYS = {1, 2, 4, 8, 16, 30};

    public RFIDController(RFIDDataListener listener) {
        this.listener = listener;
        this.reader = new ImpinjReader();
    }

    public void connect(String hostname) {
        this.hostname = hostname;
        this.intentionalDisconnect = false;

        try {
            if (reader.isConnected()) {
                reader.disconnect(); // quiet internal disconnect — do not set intentionalDisconnect
            }

            notifyStatus("Connecting to " + hostname + "...", false);
            notifyLog("Connecting to " + hostname);
            reader.connect(hostname);

            Settings settings = reader.queryDefaultSettings();
            ReportConfig report = settings.getReport();
            report.setIncludeAntennaPortNumber(true);
            report.setMode(ReportMode.Individual);
            reader.applySettings(settings);
            reader.setTagReportListener(this);
            reader.setConnectionLostListener(this);

            notifyStatus("Connected to " + hostname, true);
            notifyLog("Connected to " + hostname);

            // Query and notify antenna status
            try {
                List<AntennaStatus> antennaStatuses = reader.queryStatus()
                        .getAntennaStatusGroup()
                        .getAntennaList();
                if (listener != null) {
                    List<UnifiedAntennaStatus> unifiedStatuses = new ArrayList<>();
                    for (AntennaStatus as : antennaStatuses) {
                        unifiedStatuses.add(new UnifiedAntennaStatus(as.getPortNumber(), as.isConnected(),
                                as.isConnected() ? "Connected" : "Disconnected"));
                    }
                    listener.onAntennaStatus(unifiedStatuses, reader.getAddress());
                }
            } catch (Exception e) {
                System.err.println("Failed to query antenna status: " + e.getMessage());
            }

        } catch (OctaneSdkException e) {
            notifyStatus("Connection Failed: " + e.getMessage(), false);
            notifyLog("Connection failed to " + hostname + ": " + e.getMessage());
            if (!intentionalDisconnect && AppConfig.getInstance().isAutoReconnect()) {
                startReconnectCountdown(reconnectDelaySecs(1), 1);
            }
        } catch (Exception e) {
            notifyStatus("Error: " + e.getMessage(), false);
            notifyLog("Connection error on " + hostname + ": " + e.getMessage());
        }
    }

    public void disconnect() {
        intentionalDisconnect = true;
        isRunning = false;
        wasReading = false;
        try {
            if (reader.isConnected()) {
                try { reader.stop(); } catch (Exception ignored) {} // stop inventory before disconnecting
                reader.disconnect();
            }
            notifyStatus("Disconnected", false);
            notifyLog("Disconnected from " + hostname + " (user request)");
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
            wasReading = true;
            notifyStatus("Reading Tags...", true);
        } catch (Exception e) {
            notifyStatus("Start Failed: " + e.getMessage(), true);
        }
    }

    public void stopReading() {
        isRunning = false;
        wasReading = false;
        try {
            if (reader.isConnected()) {
                reader.stop();
            }
            notifyStatus("Stopped Reading", true);
        } catch (Exception e) {
            notifyStatus("Stop Failed: " + e.getMessage(), true);
        }
    }

    @Override
    public boolean isConnected() {
        return reader != null && reader.isConnected();
    }

    // ------------------------------------------------------------------ //
    // ConnectionLostListener
    // ------------------------------------------------------------------ //

    @Override
    public void onConnectionLost(ImpinjReader r) {
        if (!intentionalDisconnect) {
            notifyStatus("Connection lost", false);
            notifyLog("Unexpected connection lost from " + hostname);
            if (AppConfig.getInstance().isAutoReconnect()) {
                startReconnectCountdown(reconnectDelaySecs(1), 1);
            }
        }
    }

    // ------------------------------------------------------------------ //
    // Tag Processing
    // ------------------------------------------------------------------ //

    @Override
    public void onTagReported(ImpinjReader reader, TagReport report) {
        List<Tag> tags = report.getTags();
        for (Tag t : tags) {
            String epc = t.getEpc().toHexString();
            int antennaPort = t.getAntennaPortNumber();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
            String timestamp = LocalDateTime.now().format(formatter);

            if (listener != null) {
                listener.onTagRead(epc, timestamp, reader.getAddress(), antennaPort);
            }
        }
    }

    // ------------------------------------------------------------------ //
    // Auto-reconnect internals
    // ------------------------------------------------------------------ //

    private void startReconnectCountdown(int delaySecs, int attemptNum) {
        notifyLog("Reconnect attempt #" + attemptNum + " scheduled in " + delaySecs + "s");
        Thread t = new Thread(() -> {
            for (int remaining = delaySecs; remaining > 0; remaining--) {
                if (intentionalDisconnect) return;
                notifyStatus("Reconnecting in " + remaining + "s (attempt #" + attemptNum + ")", false);
                try { Thread.sleep(1000); } catch (InterruptedException e) { return; }
            }
            if (intentionalDisconnect || !AppConfig.getInstance().isAutoReconnect()) return;

            notifyStatus("Reconnecting... (attempt #" + attemptNum + ")", false);
            notifyLog("Attempting reconnect #" + attemptNum + " to " + hostname);
            connect(hostname);

            if (isConnected()) {
                notifyLog("Reconnected successfully on attempt #" + attemptNum);
                if (wasReading) startReading();
            } else if (!intentionalDisconnect) {
                int nextDelay = reconnectDelaySecs(attemptNum + 1);
                notifyLog("Reconnect #" + attemptNum + " failed — next attempt in " + nextDelay + "s");
                startReconnectCountdown(nextDelay, attemptNum + 1);
            }
        }, "Reconnect-Impinj-" + hostname);
        t.setDaemon(true);
        t.start();
    }

    private int reconnectDelaySecs(int attempt) {
        // 1-based: attempt 1→1s, 2→2s, 3→4s, 4→8s, 5→16s, 6+→30s
        return RECONNECT_DELAYS[Math.min(attempt - 1, RECONNECT_DELAYS.length - 1)];
    }

    // ------------------------------------------------------------------ //
    // Notification helpers
    // ------------------------------------------------------------------ //

    private void notifyStatus(String msg, boolean isConnected) {
        if (listener != null) {
            String ip = hostname.isEmpty() ? "" : hostname;
            try { ip = reader.getAddress(); } catch (Exception ignored) {}
            listener.onReaderStatus(msg, isConnected, ip);
        }
    }

    private void notifyLog(String msg) {
        if (listener != null) {
            listener.onConnectionLog(msg, hostname);
        }
    }
}
