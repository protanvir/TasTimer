import com.mot.rfid.api3.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ZebraReaderAdapter implements ReaderInterface {

    private RFIDReader reader;
    private RFIDDataListener listener;
    private volatile boolean isRunning = false;  // volatile: read by polling thread, written by main thread
    private volatile String  hostname             = "";

    // Auto-reconnect state
    private volatile boolean intentionalDisconnect = false;
    private volatile boolean wasReading            = false;

    private static final int[] RECONNECT_DELAYS = {1, 2, 4, 8, 16, 30};

    public ZebraReaderAdapter(RFIDDataListener listener) {
        this.listener = listener;
        this.reader = new RFIDReader();
    }

    @Override
    public void connect(String hostname) {
        this.hostname = hostname;
        this.intentionalDisconnect = false;

        try {
            if (isConnected()) {
                reader.disconnect(); // quiet internal disconnect
            }

            notifyStatus("Connecting to " + hostname + "...", false);
            notifyLog("Connecting to " + hostname);

            reader.setHostName(hostname);
            reader.setPort(AppConfig.getInstance().getZebraPort());
            reader.connect();

            // Configure Events
            reader.Events.setInventoryStartEvent(true);
            reader.Events.setInventoryStopEvent(true);
            reader.Events.setTagReadEvent(true);
            reader.Events.setAntennaEvent(true);
            reader.Events.setReaderDisconnectEvent(true);
            reader.Events.setReaderExceptionEvent(true);
            reader.Events.addEventsListener(new ZebraEventsHandler());

            notifyStatus("Connected to " + hostname, true);
            notifyLog("Connected to " + hostname);

        } catch (InvalidUsageException | OperationFailureException e) {
            notifyStatus("Connection Failed: " + e.getMessage(), false);
            notifyLog("Connection failed to " + hostname + ": " + e.getMessage());
            if (!intentionalDisconnect && AppConfig.getInstance().isAutoReconnect()) {
                startReconnectCountdown(reconnectDelaySecs(1), 1);
            }
        }
    }

    @Override
    public void disconnect() {
        intentionalDisconnect = true;
        isRunning = false;   // stop polling thread before touching the reader
        wasReading = false;
        try {
            if (isConnected()) {
                try { reader.Actions.Inventory.stop(); } catch (Exception ignored) {}
                reader.disconnect();
            }
            notifyStatus("Disconnected", false);
            notifyLog("Disconnected from " + hostname + " (user request)");
        } catch (InvalidUsageException | OperationFailureException e) {
            notifyStatus("Disconnect Error: " + e.getMessage(), false);
        }
    }

    @Override
    public void startReading() {
        try {
            if (!isConnected()) {
                notifyStatus("Not connected!", false);
                return;
            }
            reader.Actions.purgeTags();
            reader.Actions.Inventory.perform(null, null, null);
            isRunning = true;
            wasReading = true;
            notifyStatus("Reading Tags...", true);

            // Polling Thread
            new Thread(() -> {
                while (isRunning) {
                    try {
                        Thread.sleep(500);
                        if (reader != null && reader.Actions != null) {
                            TagData[] tags = reader.Actions.getReadTags(100);
                            if (tags != null && tags.length > 0) {
                                for (TagData tag : tags) {
                                    String epc = tag.getTagID();
                                    int antennaPort = tag.getAntennaID();
                                    String timestamp = LocalDateTime.now()
                                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
                                    if (listener != null) {
                                        listener.onTagRead(epc, timestamp, hostname, antennaPort);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        // polling errors are suppressed to keep loop alive
                    }
                }
            }).start();
        } catch (InvalidUsageException | OperationFailureException e) {
            notifyStatus("Start Failed: " + e.getMessage(), true);
        }
    }

    @Override
    public void stopReading() {
        // Signal the polling thread to exit first, before any SDK call that might throw
        isRunning = false;
        wasReading = false;
        try {
            if (isConnected()) {
                reader.Actions.Inventory.stop();
            }
            notifyStatus("Stopped Reading", true);
        } catch (InvalidUsageException | OperationFailureException e) {
            notifyStatus("Stop Failed: " + e.getMessage(), true);
        }
    }

    @Override
    public boolean isConnected() {
        return reader != null && reader.isConnected();
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
        }, "Reconnect-Zebra-" + hostname);
        t.setDaemon(true);
        t.start();
    }

    private int reconnectDelaySecs(int attempt) {
        return RECONNECT_DELAYS[Math.min(attempt - 1, RECONNECT_DELAYS.length - 1)];
    }

    // ------------------------------------------------------------------ //
    // Notification helpers
    // ------------------------------------------------------------------ //

    private void notifyStatus(String msg, boolean isConnected) {
        if (listener != null) {
            listener.onReaderStatus(msg, isConnected, hostname != null ? hostname : "");
        }
    }

    private void notifyLog(String msg) {
        if (listener != null) {
            listener.onConnectionLog(msg, hostname != null ? hostname : "");
        }
    }

    // ------------------------------------------------------------------ //
    // Events Handler
    // ------------------------------------------------------------------ //

    class ZebraEventsHandler implements RfidEventsListener {
        @Override
        public void eventReadNotify(RfidReadEvents rre) {
            // Processing handled by polling loop due to event unreliability
        }

        @Override
        public void eventStatusNotify(RfidStatusEvents rse) {
            STATUS_EVENT_TYPE type = rse.StatusEventData.getStatusEventType();

            if (type == STATUS_EVENT_TYPE.DISCONNECTION_EVENT) {
                notifyStatus("Disconnected", false);
                if (!intentionalDisconnect) {
                    notifyLog("Unexpected connection lost from " + hostname);
                    if (AppConfig.getInstance().isAutoReconnect()) {
                        startReconnectCountdown(reconnectDelaySecs(1), 1);
                    }
                }
            } else if (type == STATUS_EVENT_TYPE.ANTENNA_EVENT) {
                int port = rse.StatusEventData.AntennaEventData.getAntennaID();
                boolean connected = rse.StatusEventData.AntennaEventData
                        .getAntennaEvent() == ANTENNA_EVENT_TYPE.ANTENNA_CONNECTED;

                List<UnifiedAntennaStatus> list = new ArrayList<>();
                list.add(new UnifiedAntennaStatus(port, connected, connected ? "Connected" : "Disconnected"));

                if (listener != null) {
                    listener.onAntennaStatus(list, hostname != null ? hostname : "");
                }
            }
        }
    }
}
