import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Client for RUFUS Cloud Public API (v0).
 *
 * Typical flow:
 *   1. bindDeviceSync()   → POST /devices  (get deviceid)
 *   2. createSessionSync() → POST /sessions (get token_session)
 *   3. sendPassing()      → POST /passings (fire-and-forget, background thread)
 */
public class RufusAPIClient {

    private static final String BASE_URL   = "https://api.runonrufus.com/v0";
    private static final int    TIMEOUT_MS = 8000;

    private final AtomicLong              passingsCounter    = new AtomicLong(0);
    private final AtomicReference<String> cachedSessionToken = new AtomicReference<>(null);

    public RufusAPIClient() {}

    // ------------------------------------------------------------------ //
    // Public static API — called from Settings dialog background threads
    // ------------------------------------------------------------------ //

    /**
     * Calls POST /devices. Returns the deviceid on success, null on failure.
     * Requires WRITE or READ_WRITE API key.
     */
    public static String bindDeviceSync(String apiKey, String serialNumber, String alias) {
        try {
            String body = "{"
                    + "\"type\":\"CUSTOM_CHRONO\","
                    + "\"model\":\"TimingSoft\","
                    + "\"serial_number\":\"" + escapeJson(serialNumber) + "\","
                    + "\"alias\":\"" + escapeJson(alias) + "\""
                    + "}";

            HttpURLConnection conn = openConn("POST", "/devices", apiKey);
            writeBody(conn, body);

            int code = conn.getResponseCode();
            String response = readFull(conn, code);
            if (code == 201 || code == 200 || code == 409) {
                // 409 = already bound — deviceid is still returned
                String deviceId = extractJsonField(response, "deviceid");
                if (deviceId != null) {
                    System.out.println("RufusAPI: device bound — " + deviceId
                            + (code == 409 ? " (already existed)" : ""));
                    return deviceId;
                }
                System.err.println("RufusAPI: bind response missing deviceid. Body: " + response);
            } else {
                System.err.println("RufusAPI: bind failed HTTP " + code + " — " + response);
            }
        } catch (Exception e) {
            System.err.println("RufusAPI: bind error — " + e.getMessage());
        }
        return null;
    }

    /**
     * Calls POST /sessions. Returns token_session on success, null on failure.
     * Requires WRITE or READ_WRITE API key.
     */
    public static String createSessionSync(String apiKey, String deviceId) {
        try {
            String body = "{"
                    + "\"deviceid\":\"" + escapeJson(deviceId) + "\","
                    + "\"alias\":\"TimingSoft\""
                    + "}";

            HttpURLConnection conn = openConn("POST", "/sessions", apiKey);
            writeBody(conn, body);

            int code = conn.getResponseCode();
            String response = readFull(conn, code);
            if (code == 201 || code == 200) {
                String token = extractJsonField(response, "token_session");
                if (token != null) {
                    System.out.println("RufusAPI: session created — " + token);
                    return token;
                }
                System.err.println("RufusAPI: session response missing token_session. Body: " + response);
            } else {
                System.err.println("RufusAPI: session creation failed HTTP " + code + " — " + response);
            }
        } catch (Exception e) {
            System.err.println("RufusAPI: session creation error — " + e.getMessage());
        }
        return null;
    }

    // ------------------------------------------------------------------ //
    // Instance API — called from the tag pipeline
    // ------------------------------------------------------------------ //

    public void cacheSessionToken(String token) {
        cachedSessionToken.set(token);
    }

    public String getCachedSessionToken() {
        return cachedSessionToken.get();
    }

    /**
     * Sends a tag read as a passing to RUFUS (background thread, fire-and-forget).
     * Auto-creates a session if none is cached.
     */
    public void sendPassing(String epc, String timestamp) {
        AppConfig cfg = AppConfig.getInstance();
        if (!cfg.isRufusApiEnabled()) return;

        String apiKey  = cfg.getRufusApiKey();
        String deviceId = cfg.getRufusApiDeviceId();

        if (apiKey.isBlank() || deviceId.isBlank()) {
            System.err.println("RufusAPIClient: missing api_key or device_id in config");
            return;
        }

        String sessionToken = cachedSessionToken.get();
        if (sessionToken == null) {
            sessionToken = createSessionSync(apiKey, deviceId);
            if (sessionToken == null) return;
            cachedSessionToken.set(sessionToken);
        }

        long numPassing = passingsCounter.incrementAndGet();

        String passingJson = "{"
                + "\"timestamp\":\"" + timestamp + "\","
                + "\"num_passing\":" + numPassing + ","
                + "\"bib\":\"" + escapeJson(epc) + "\""
                + "}";

        String reqBody = "{"
                + "\"token_session\":\"" + escapeJson(sessionToken) + "\","
                + "\"passings_list\":[" + passingJson + "]"
                + "}";

        final String finalToken = sessionToken;
        new Thread(() -> {
            try {
                HttpURLConnection conn = openConn("POST", "/passings", apiKey);
                writeBody(conn, reqBody);
                int code = conn.getResponseCode();
                String resp = readFull(conn, code);
                if (code == 201 || code == 200) {
                    System.out.println("RufusAPI: passing sent (" + code + ")");
                } else {
                    System.err.println("RufusAPI: passing error HTTP " + code + " — " + resp);
                }
            } catch (Exception e) {
                System.err.println("RufusAPI: passing send error — " + e.getMessage());
            }
        }, "RufusAPI-Sender").start();
    }

    // ------------------------------------------------------------------ //
    // Internal helpers
    // ------------------------------------------------------------------ //

    private static HttpURLConnection openConn(String method, String path, String apiKey)
            throws IOException {
        HttpURLConnection conn = (HttpURLConnection) URI.create(BASE_URL + path)
                .toURL().openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("x-api-key", apiKey);
        conn.setConnectTimeout(TIMEOUT_MS);
        conn.setReadTimeout(TIMEOUT_MS);
        conn.setDoOutput(true);
        return conn;
    }

    private static void writeBody(HttpURLConnection conn, String body) throws IOException {
        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
            os.flush();
        }
    }

    /** Reads success or error response body. Never throws. */
    private static String readFull(HttpURLConnection conn, int code) {
        try {
            InputStream is = (code >= 200 && code < 300)
                    ? conn.getInputStream()
                    : conn.getErrorStream();
            if (is == null) return "(empty)";
            try (Scanner s = new Scanner(is, StandardCharsets.UTF_8)) {
                s.useDelimiter("\\A");
                return s.hasNext() ? s.next() : "(empty)";
            }
        } catch (IOException e) {
            return "(unreadable: " + e.getMessage() + ")";
        }
    }

    private static String extractJsonField(String json, String fieldName) {
        String key = "\"" + fieldName + "\":\"";
        int idx = json.indexOf(key);
        if (idx < 0) return null;
        int start = idx + key.length();
        int end = json.indexOf("\"", start);
        if (end < 0) return null;
        return json.substring(start, end);
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
