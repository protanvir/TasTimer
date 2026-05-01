import java.util.List;

public interface RFIDDataListener {
    void onTagRead(String epc, String timestamp, String readerIp, int antennaPort);

    /**
     * Called when a reader's connection status changes.
     *
     * @param status      human-readable status message
     * @param isConnected true if the reader is now connected
     * @param readerIp    IP address of the reader that generated this status
     */
    void onReaderStatus(String status, boolean isConnected, String readerIp);

    /**
     * Called when antenna status is updated for a reader.
     *
     * @param antennaStatuses list of antenna statuses
     * @param readerIp        IP address of the reader that generated this update
     */
    void onAntennaStatus(List<UnifiedAntennaStatus> antennaStatuses, String readerIp);

    /**
     * Called to log a timestamped connection event (connect, disconnect, reconnect attempt, etc.).
     *
     * @param message  human-readable description of the event
     * @param readerIp IP address of the reader that generated this event
     */
    void onConnectionLog(String message, String readerIp);
}
