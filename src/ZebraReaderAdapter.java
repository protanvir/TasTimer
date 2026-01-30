import com.mot.rfid.api3.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ZebraReaderAdapter implements ReaderInterface {

    private RFIDReader reader;
    private RFIDDataListener listener;
    private boolean isRunning = false;
    private String hostname;

    public ZebraReaderAdapter(RFIDDataListener listener) {
        this.listener = listener;
        this.reader = new RFIDReader();
    }

    @Override
    public void connect(String hostname) {
        this.hostname = hostname;
        try {
            if (isConnected()) {
                disconnect();
            }

            notifyStatus("Connecting to " + hostname + "...", false);

            reader.setHostName(hostname);
            reader.setPort(5084); // Default Zebra port

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

        } catch (InvalidUsageException | OperationFailureException e) {
            notifyStatus("Connection Failed: " + e.getMessage(), false);
        }
    }

    @Override
    public void disconnect() {
        try {
            if (isConnected()) {
                reader.disconnect();
            }
            notifyStatus("Disconnected", false);
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
            // Simple Inventory settings
            reader.Actions.purgeTags();
            reader.Actions.Inventory.perform(null, null, null);
            isRunning = true;
            notifyStatus("Reading Tags...", true);

            // Polling Thread
            new Thread(() -> {
                while (isRunning) {
                    try {
                        Thread.sleep(500); // Check every 500ms
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
                        // calculated risk: ignore polling errors to keep loop alive or exit gracefully
                    }
                }
            }).start();
        } catch (InvalidUsageException | OperationFailureException e) {
            notifyStatus("Start Failed: " + e.getMessage(), true);
        }
    }

    @Override
    public void stopReading() {
        try {
            if (isConnected()) {
                reader.Actions.Inventory.stop();
            }
            isRunning = false;
            notifyStatus("Stopped Reading", true);
        } catch (InvalidUsageException | OperationFailureException e) {
            notifyStatus("Stop Failed: " + e.getMessage(), true);
        }
    }

    @Override
    public boolean isConnected() {
        return reader != null && reader.isConnected();
    }

    private void notifyStatus(String msg, boolean isConnected) {
        if (listener != null) {
            listener.onReaderStatus(msg, isConnected);
        }
    }

    // --- Events Handler ---

    class ZebraEventsHandler implements RfidEventsListener {
        @Override
        public void eventReadNotify(RfidReadEvents rre) {
            // Processing handled by polling loop due to event unreliability
        }

        @Override
        public void eventStatusNotify(RfidStatusEvents rse) {
            STATUS_EVENT_TYPE type = rse.StatusEventData.getStatusEventType();

            if (type == STATUS_EVENT_TYPE.DISCONNECTION_EVENT) {
                notifyStatus("Disconnected (Event)", false);
            } else if (type == STATUS_EVENT_TYPE.ANTENNA_EVENT) {
                int port = rse.StatusEventData.AntennaEventData.getAntennaID();
                boolean connected = rse.StatusEventData.AntennaEventData
                        .getAntennaEvent() == ANTENNA_EVENT_TYPE.ANTENNA_CONNECTED;

                // Zebra reports single antenna events. We should ideally aggregate or just
                // report this one.
                // Our listener expects a list.
                List<UnifiedAntennaStatus> list = new ArrayList<>();
                list.add(new UnifiedAntennaStatus(port, connected, connected ? "Connected" : "Disconnected"));

                if (listener != null) {
                    listener.onAntennaStatus(list);
                }
            }
        }
    }
}
