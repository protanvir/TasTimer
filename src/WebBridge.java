import io.javalin.websocket.WsContext;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class WebBridge implements RFIDDataListener {

    private final Set<WsContext> clients = new CopyOnWriteArraySet<>();

    public void addClient(WsContext ctx) {
        clients.add(ctx);
    }

    public void removeClient(WsContext ctx) {
        clients.remove(ctx);
    }

    // -------------------------------------------------------------------------
    // RFIDDataListener callbacks
    // -------------------------------------------------------------------------

    @Override
    public void onTagRead(String epc, String timestamp, String readerIp, int antennaPort) {
        broadcast(String.format(
            "{\"event\":\"TAG_READ\",\"payload\":{\"epc\":\"%s\",\"timestamp\":\"%s\",\"readerIp\":\"%s\",\"antenna\":%d}}",
            esc(epc), esc(timestamp), esc(readerIp), antennaPort));
    }

    @Override
    public void onAntennaStatus(List<UnifiedAntennaStatus> antennaStatuses, String readerIp) {
        for (UnifiedAntennaStatus s : antennaStatuses) {
            broadcast(String.format(
                "{\"event\":\"ANTENNA_STATUS\",\"payload\":{\"antenna\":%d,\"connected\":%b,\"readerIp\":\"%s\"}}",
                s.getPortNumber(), s.isConnected(), esc(readerIp)));
        }
    }

    @Override
    public void onReaderStatus(String status, boolean isConnected, String readerIp) {
        broadcast(String.format(
            "{\"event\":\"READER_STATUS\",\"payload\":{\"message\":\"%s\",\"connected\":%b,\"readerIp\":\"%s\"}}",
            esc(status), isConnected, esc(readerIp)));
    }

    @Override
    public void onConnectionLog(String message, String readerIp) {
        broadcast(String.format(
            "{\"event\":\"CONNECTION_LOG\",\"payload\":{\"message\":\"%s\",\"readerIp\":\"%s\"}}",
            esc(message), esc(readerIp)));
    }

    // -------------------------------------------------------------------------
    // Race engine push helpers
    // -------------------------------------------------------------------------

    public void broadcastRaceTimer(long elapsedMs) {
        broadcast(String.format(
            "{\"event\":\"RACE_TIMER\",\"payload\":{\"elapsed\":%d}}", elapsedMs));
    }

    public void broadcastRaceState(String state) {
        broadcast(String.format(
            "{\"event\":\"RACE_STATE\",\"payload\":{\"state\":\"%s\"}}", esc(state)));
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private void broadcast(String json) {
        for (WsContext ctx : clients) {
            try {
                if (ctx.session.isOpen()) {
                    ctx.send(json);
                }
            } catch (Exception ignored) {
                // Session closed between isOpen() check and send(); harmless.
            }
        }
    }

    /** Minimal JSON string escaping — only what String.format-built payloads need. */
    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
