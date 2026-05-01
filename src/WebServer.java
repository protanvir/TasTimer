import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Embedded Javalin HTTP + WebSocket server.
 *
 * Constructor args are intentionally separate from their singleton counterparts
 * so the same instances can be shared with the rest of the app when wired in.
 */
public class WebServer {

    private final WebBridge bridge;
    private final AppConfig config;
    private final RaceSession session;
    private final DuplicateFilter filter;

    // Mutable: replaced when the operator connects a different reader
    private volatile ReaderInterface controller;
    private volatile String readerType = "Impinj";

    private Javalin app;

    public WebServer(ReaderInterface controller, WebBridge bridge, AppConfig config,
                     RaceSession session, DuplicateFilter filter) {
        this.controller = controller;
        this.bridge     = bridge;
        this.config     = config;
        this.session    = session;
        this.filter     = filter;
    }

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    public void start() {
        app = Javalin.create(cfg -> {
            File webDist = new File("web/dist");
            if (webDist.isDirectory()) {
                cfg.staticFiles.add("web/dist", Location.EXTERNAL);
                cfg.spaRoot.addFile("/", "web/dist/index.html", Location.EXTERNAL);
            }
            cfg.bundledPlugins.enableCors(cors ->
                cors.addRule(rule -> rule.anyHost())
            );
        });

        app.exception(Exception.class, (e, ctx) ->
            ctx.status(500).json(Map.of(
                "success", false,
                "error", e.getMessage() != null ? e.getMessage() : "Internal error"))
        );

        app.ws("/ws", ws -> {
            ws.onConnect(ctx -> bridge.addClient(ctx));
            ws.onClose(ctx  -> bridge.removeClient(ctx));
            ws.onError(ctx  -> bridge.removeClient(ctx));
        });

        registerRoutes();
        app.start(8080);
    }

    public void stop() {
        if (app != null) app.stop();
    }

    // -------------------------------------------------------------------------
    // Routes
    // -------------------------------------------------------------------------

    private void registerRoutes() {

        // GET /api/status
        app.get("/api/status", ctx -> ctx.json(Map.of(
            "success", true,
            "data", Map.of(
                "connected",        controller != null && controller.isConnected(),
                "readerType",       readerType,
                "raceState",        session.getState().name(),
                "finishCount",      session.getFinishCount(),
                "participantCount", ParticipantRegistry.getInstance().getTotalCount()
            )
        )));

        // POST /api/reader/connect  { type, ip }
        app.post("/api/reader/connect", ctx -> {
            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) ctx.bodyAsClass(Map.class);
            String type = str(body.getOrDefault("type", "Impinj"));
            String ip   = str(body.getOrDefault("ip",   config.getReaderIp()));

            if (controller != null && controller.isConnected()) controller.disconnect();

            boolean isZebra  = "Zebra".equalsIgnoreCase(type);
            ReaderInterface next = isZebra
                ? new ZebraReaderAdapter(bridge)
                : new RFIDController(bridge);
            readerType = isZebra ? "Zebra" : "Impinj";
            controller = next;
            config.setReaderIp(ip);

            // connect() blocks until timeout — run on a virtual thread
            Thread.ofVirtual().start(() -> next.connect(ip));
            ctx.json(Map.of("success", true));
        });

        // POST /api/reader/disconnect
        app.post("/api/reader/disconnect", ctx -> {
            if (controller != null) controller.disconnect();
            ctx.json(Map.of("success", true));
        });

        // POST /api/reader/start
        app.post("/api/reader/start", ctx -> {
            if (controller != null) controller.startReading();
            ctx.json(Map.of("success", true));
        });

        // POST /api/reader/stop
        app.post("/api/reader/stop", ctx -> {
            if (controller != null) controller.stopReading();
            ctx.json(Map.of("success", true));
        });

        // POST /api/filter/reset
        app.post("/api/filter/reset", ctx -> {
            filter.reset();
            ctx.json(Map.of("success", true));
        });

        // GET /api/race
        app.get("/api/race", ctx -> {
            List<Map<String, Object>> waves = session.getWaves().stream()
                .map(w -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id",             w.getId());
                    m.put("name",           w.getName());
                    m.put("hasFired",       w.hasFired());
                    m.put("gunTimeMs",      w.getGunTimeMs());
                    m.put("gunTimeFormatted", w.getFormattedGunTime());
                    return m;
                })
                .collect(Collectors.toList());

            ctx.json(Map.of(
                "success", true,
                "data", Map.of(
                    "state",       session.getState().name(),
                    "finishCount", session.getFinishCount(),
                    "waves",       waves
                )
            ));
        });

        // POST /api/race/arm
        app.post("/api/race/arm", ctx -> {
            session.arm();
            bridge.broadcastRaceState(session.getState().name());
            ctx.json(Map.of("success", true));
        });

        // POST /api/race/start  { waveName, gunTime? }
        app.post("/api/race/start", ctx -> {
            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) ctx.bodyAsClass(Map.class);
            String waveName   = str(body.getOrDefault("waveName", "Wave 1"));
            Object gunTimeRaw = body.get("gunTime");

            String waveId = UUID.randomUUID().toString();
            Wave wave = new Wave(waveId, waveName);
            session.addWave(wave);
            session.fireWave(waveId); // transitions ARMED → RUNNING, stamps gun time as now

            // Caller may supply an explicit epoch-ms (e.g. manual start entry)
            if (gunTimeRaw instanceof Number n) {
                wave.fireAt(n.longValue());
            }

            bridge.broadcastRaceState(session.getState().name());
            ctx.json(Map.of("success", true, "waveId", waveId));
        });

        // POST /api/race/finish
        app.post("/api/race/finish", ctx -> {
            session.finish();
            bridge.broadcastRaceState(session.getState().name());
            ctx.json(Map.of("success", true));
        });

        // GET /api/export/csv
        app.get("/api/export/csv", ctx -> {
            File tmp = File.createTempFile("results", ".csv");
            tmp.deleteOnExit();
            ResultsExporter.exportCsv(tmp, session, ParticipantRegistry.getInstance());
            ctx.contentType("text/csv");
            ctx.header("Content-Disposition", "attachment; filename=\"results.csv\"");
            ctx.result(new FileInputStream(tmp));
        });

        // GET /api/settings
        app.get("/api/settings", ctx ->
            ctx.json(Map.of("success", true, "data", buildSettingsMap()))
        );

        // POST /api/settings
        app.post("/api/settings", ctx -> {
            applySettings(ctx.bodyAsClass(Map.class));
            config.save();
            ctx.json(Map.of("success", true));
        });

        // GET /api/participants
        app.get("/api/participants", ctx -> {
            List<Map<String, Object>> list = ParticipantRegistry.getInstance().getAll().stream()
                .map(p -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("bib",          p.getBibNumber());
                    m.put("firstName",    p.getFirstName());
                    m.put("lastName",     p.getLastName());
                    m.put("epc",          p.getEpc());
                    m.put("category",     p.getCategory());
                    m.put("wave",         p.getWaveId());
                    m.put("hasBeenRead",  p.hasBeenRead());
                    m.put("lastReadTime", p.getLastReadTime());
                    return m;
                })
                .collect(Collectors.toList());
            ctx.json(Map.of("success", true, "data", list));
        });

        // POST /api/participants/import  (multipart, field name "file")
        app.post("/api/participants/import", ctx -> {
            var uploaded = ctx.uploadedFile("file");
            if (uploaded == null) {
                ctx.status(400).json(Map.of("success", false, "error", "No file provided"));
                return;
            }
            File tmp = File.createTempFile("participants", ".csv");
            tmp.deleteOnExit();
            try (InputStream in   = uploaded.content();
                 FileOutputStream out = new FileOutputStream(tmp)) {
                in.transferTo(out);
            }
            ParticipantRegistry registry = ParticipantRegistry.getInstance();
            registry.loadFromCsv(tmp);
            ctx.json(Map.of(
                "success", true,
                "data", Map.of("count", registry.getTotalCount())
            ));
        });
    }

    // -------------------------------------------------------------------------
    // Settings helpers
    // -------------------------------------------------------------------------

    private Map<String, Object> buildSettingsMap() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("readerIp",             config.getReaderIp());
        m.put("zebraPort",            config.getZebraPort());
        m.put("connectionTimeout",    config.getConnectionTimeout());
        m.put("autoReconnect",        config.isAutoReconnect());
        m.put("epcFormat",            config.getEpcFormat());
        m.put("epcLastN",             config.getEpcLastN());
        m.put("csvDir",               config.getCsvDir());
        m.put("suppressionSecs",      config.getSuppressionSecs());
        m.put("wiclaxEnabled",        config.isWiclaxEnabled());
        m.put("wiclaxUser",           config.getWiclaxUser());
        m.put("wiclaxDeviceId",       config.getWiclaxDeviceId());
        m.put("wiclaxUrl",            config.getWiclaxUrl());
        m.put("wiclaxPort",           config.getWiclaxPort());
        m.put("feibotEnabled",        config.isFeiBotEnabled());
        m.put("feibotServerIp",       config.getFeiBotServerIp());
        m.put("feibotServerPort",     config.getFeiBotServerPort());
        m.put("feibotDeviceId",       config.getFeiBotDeviceId());
        m.put("feibotDeviceModel",    config.getFeiBotDeviceModel());
        m.put("rufusApiEnabled",      config.isRufusApiEnabled());
        m.put("rufusApiKey",          config.getRufusApiKey());
        m.put("rufusApiDeviceId",     config.getRufusApiDeviceId());
        m.put("rufusApiSerialNumber", config.getRufusApiSerialNumber());
        m.put("rufusApiAlias",        config.getRufusApiAlias());
        return m;
    }

    private void applySettings(Map<?, ?> body) {
        if (body.containsKey("readerIp"))            config.setReaderIp(str(body.get("readerIp")));
        if (body.containsKey("zebraPort"))           config.setZebraPort(toInt(body.get("zebraPort"), config.getZebraPort()));
        if (body.containsKey("connectionTimeout"))   config.setConnectionTimeout(toInt(body.get("connectionTimeout"), config.getConnectionTimeout()));
        if (body.containsKey("autoReconnect"))       config.setAutoReconnect(toBool(body.get("autoReconnect")));
        if (body.containsKey("epcFormat"))           config.setEpcFormat(str(body.get("epcFormat")));
        if (body.containsKey("epcLastN"))            config.setEpcLastN(toInt(body.get("epcLastN"), config.getEpcLastN()));
        if (body.containsKey("csvDir"))              config.setCsvDir(str(body.get("csvDir")));
        if (body.containsKey("suppressionSecs"))     config.setSuppressionSecs(toInt(body.get("suppressionSecs"), config.getSuppressionSecs()));
        if (body.containsKey("wiclaxEnabled"))       config.setWiclaxEnabled(toBool(body.get("wiclaxEnabled")));
        if (body.containsKey("wiclaxUser"))          config.setWiclaxUser(str(body.get("wiclaxUser")));
        if (body.containsKey("wiclaxDeviceId"))      config.setWiclaxDeviceId(str(body.get("wiclaxDeviceId")));
        if (body.containsKey("wiclaxUrl"))           config.setWiclaxUrl(str(body.get("wiclaxUrl")));
        if (body.containsKey("wiclaxPort"))          config.setWiclaxPort(toInt(body.get("wiclaxPort"), config.getWiclaxPort()));
        if (body.containsKey("feibotEnabled"))       config.setFeiBotEnabled(toBool(body.get("feibotEnabled")));
        if (body.containsKey("feibotServerIp"))      config.setFeiBotServerIp(str(body.get("feibotServerIp")));
        if (body.containsKey("feibotServerPort"))    config.setFeiBotServerPort(toInt(body.get("feibotServerPort"), config.getFeiBotServerPort()));
        if (body.containsKey("feibotDeviceId"))      config.setFeiBotDeviceId(str(body.get("feibotDeviceId")));
        if (body.containsKey("feibotDeviceModel"))   config.setFeiBotDeviceModel(str(body.get("feibotDeviceModel")));
        if (body.containsKey("rufusApiEnabled"))     config.setRufusApiEnabled(toBool(body.get("rufusApiEnabled")));
        if (body.containsKey("rufusApiKey"))         config.setRufusApiKey(str(body.get("rufusApiKey")));
        if (body.containsKey("rufusApiDeviceId"))    config.setRufusApiDeviceId(str(body.get("rufusApiDeviceId")));
        if (body.containsKey("rufusApiSerialNumber")) config.setRufusApiSerialNumber(str(body.get("rufusApiSerialNumber")));
        if (body.containsKey("rufusApiAlias"))       config.setRufusApiAlias(str(body.get("rufusApiAlias")));
    }

    // -------------------------------------------------------------------------
    // Type-coercion helpers (Jackson deserialises numbers as Integer/Long/Double)
    // -------------------------------------------------------------------------

    private static String str(Object v) {
        return v != null ? v.toString() : "";
    }

    private static int toInt(Object v, int def) {
        if (v instanceof Number n) return n.intValue();
        try { return Integer.parseInt(String.valueOf(v)); }
        catch (NumberFormatException e) { return def; }
    }

    private static boolean toBool(Object v) {
        if (v instanceof Boolean b) return b;
        return Boolean.parseBoolean(String.valueOf(v));
    }
}
