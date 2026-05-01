import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * UDP client for the Feibot External Protocol V1.0.
 *
 * Sends two packet types to a configurable UDP endpoint:
 *   - Heartbeat  (every 5 s while the timer is running)
 *   - Detection  (once per accepted tag read)
 *
 * This class is purely a transport layer.  All business decisions
 * (which readers are reading, what reader-status map to pass) live
 * in the caller.
 */
public class FeiBotClient {

    private static final DateTimeFormatter RFC3339_NANO =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSSXXX");

    private static final int DEFAULT_SIGNAL_POWER = -65; // dBm placeholder until Phase 7 RSSI
    private static final float DEFAULT_BATTERY     = 5.0f;
    private static final int   DEFAULT_POWER_PCT   = 100;
    private static final float DEFAULT_VOLTAGE     = 5.0f;

    private DatagramSocket socket;

    public FeiBotClient() {
        try {
            socket = new DatagramSocket();
        } catch (Exception e) {
            System.err.println("FeiBotClient: failed to open socket — " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------ //
    // Public API
    // ------------------------------------------------------------------ //

    /**
     * Sends a heartbeat packet if FeiBot is enabled in AppConfig.
     *
     * @param readerStatuses map of {readerName → "reading" | "stopped"}
     */
    public void sendHeartbeat(Map<String, String> readerStatuses) {
        AppConfig cfg = AppConfig.getInstance();
        if (!cfg.isFeiBotEnabled()) return;

        String readerStatusJson = buildReaderStatusJson(readerStatuses);
        String json = "{"
                + q("protocol_version") + ":\"external_v1.0\","
                + q("device_identity")  + ":" + q(cfg.getFeiBotDeviceId())   + ","
                + q("device_model")     + ":" + q(cfg.getFeiBotDeviceModel()) + ","
                + q("ip_address")       + ":" + q(localIp())                 + ","
                + q("power_status")     + ":" + DEFAULT_POWER_PCT            + ","
                + q("voltage_level")    + ":" + DEFAULT_VOLTAGE              + ","
                + q("reader_status")    + ":" + readerStatusJson             + ","
                + q("timestamp")        + ":" + q(now())
                + "}";

        send(json, cfg);
    }

    /**
     * Sends a tag detection packet if FeiBot is enabled in AppConfig.
     *
     * @param epc         formatted EPC string
     * @param antennaPort antenna port number from the reader
     */
    public void sendDetection(String epc, int antennaPort) {
        AppConfig cfg = AppConfig.getInstance();
        if (!cfg.isFeiBotEnabled()) return;

        String json = "{"
                + q("protocol_version") + ":\"external_v1.0\","
                + q("device_identity")  + ":" + q(cfg.getFeiBotDeviceId()) + ","
                + q("detection_data")   + ":{"
                +     q("tag_identifier")  + ":" + q(epc)                   + ","
                +     q("detection_time")  + ":" + q(now())                 + ","
                +     q("signal_power")    + ":" + DEFAULT_SIGNAL_POWER     + ","
                +     q("antenna_port")    + ":" + antennaPort              + ","
                +     q("battery_info")    + ":" + DEFAULT_BATTERY
                + "}"
                + "}";

        send(json, cfg);
    }

    public void close() {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }

    // ------------------------------------------------------------------ //
    // Internal helpers
    // ------------------------------------------------------------------ //

    private void send(String json, AppConfig cfg) {
        if (socket == null || socket.isClosed()) return;
        try {
            byte[] data = json.getBytes(StandardCharsets.UTF_8);
            InetAddress addr = InetAddress.getByName(cfg.getFeiBotServerIp());
            DatagramPacket pkt = new DatagramPacket(data, data.length, addr, cfg.getFeiBotServerPort());
            socket.send(pkt);
        } catch (Exception e) {
            System.err.println("FeiBotClient send error: " + e.getMessage());
        }
    }

    private String buildReaderStatusJson(Map<String, String> statuses) {
        if (statuses == null || statuses.isEmpty()) return "{}";
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, String> entry : statuses.entrySet()) {
            if (!first) sb.append(",");
            sb.append(q(entry.getKey())).append(":\"").append(entry.getValue()).append("\"");
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    private static String now() {
        return OffsetDateTime.now().format(RFC3339_NANO);
    }

    private static String localIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "127.0.0.1";
        }
    }

    /** Wraps a string value in JSON double-quotes. */
    private static String q(String s) {
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
