import java.io.*;
import java.util.Properties;

/**
 * Singleton configuration store.
 * Reads from and writes to config.properties.
 * All previously hardcoded constants live here.
 */
public class AppConfig {

    private static final String CONFIG_FILE = "config.properties";
    private static AppConfig instance;
    private final Properties props = new Properties();

    // --- Defaults ---
    public static final String  DEF_IP                  = "172.16.1.114";
    public static final boolean DEF_WICLAX_ENABLED      = true;
    public static final String  DEF_WICLAX_USER         = "totalactivesports";
    public static final String  DEF_WICLAX_DEVICE_ID    = "tas246";
    public static final String  DEF_WICLAX_URL          = "http://wiclax.com";
    public static final int     DEF_WICLAX_PORT         = 35014;
    public static final boolean DEF_LOCAL_API_ENABLED   = true;
    public static final int     DEF_LOCAL_API_PORT      = 3000;
    public static final String  DEF_LOCAL_API_ENDPOINT  = "/api/tags";
    public static final int     DEF_ZEBRA_PORT          = 5084;
    public static final int     DEF_CONN_TIMEOUT        = 30;
    public static final boolean DEF_AUTO_RECONNECT      = false;
    public static final String  DEF_EPC_FORMAT          = "LAST_N";  // "FULL" or "LAST_N"
    public static final int     DEF_EPC_LAST_N          = 8;
    public static final String  DEF_CSV_DIR             = "data";
    public static final int     DEF_SUPPRESSION_SECS    = 300;   // 5 minutes

    // FeiBot
    public static final boolean DEF_FEIBOT_ENABLED      = false;
    public static final String  DEF_FEIBOT_SERVER_IP    = "127.0.0.1";
    public static final int     DEF_FEIBOT_SERVER_PORT  = 42100;
    public static final String  DEF_FEIBOT_DEVICE_ID    = "DEV001";
    public static final String  DEF_FEIBOT_DEVICE_MODEL = "RFID-Reader-V2";

    // RufusAPI
    public static final boolean DEF_RUFUSAPI_ENABLED      = false;
    public static final String  DEF_RUFUSAPI_API_KEY      = "";
    public static final String  DEF_RUFUSAPI_DEVICE_ID    = "";
    public static final String  DEF_RUFUSAPI_SERIAL_NUM   = "";
    public static final String  DEF_RUFUSAPI_ALIAS        = "TimingSoft Device";

    private AppConfig() {
        load();
    }

    public static synchronized AppConfig getInstance() {
        if (instance == null) {
            instance = new AppConfig();
        }
        return instance;
    }

    public void load() {
        try (InputStream in = new FileInputStream(CONFIG_FILE)) {
            props.load(in);
        } catch (IOException ignored) {
            // File absent on first run — defaults will be used
        }
    }

    public void save() {
        try (OutputStream out = new FileOutputStream(CONFIG_FILE)) {
            props.store(out, "TimingSoft Configuration");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ------------------------------------------------------------------ //
    // Reader IP
    // ------------------------------------------------------------------ //
    public String getReaderIp()          { return get("ip", DEF_IP); }
    public void   setReaderIp(String v)  { set("ip", v); }

    // ------------------------------------------------------------------ //
    // Wiclax
    // ------------------------------------------------------------------ //
    public boolean isWiclaxEnabled()              { return getBool("wiclax.enabled",   DEF_WICLAX_ENABLED); }
    public void    setWiclaxEnabled(boolean v)    { set("wiclax.enabled",   str(v)); }

    public String  getWiclaxUser()                { return get("wiclax.user",      DEF_WICLAX_USER); }
    public void    setWiclaxUser(String v)        { set("wiclax.user",      v); }

    public String  getWiclaxDeviceId()            { return get("wiclax.deviceId",  DEF_WICLAX_DEVICE_ID); }
    public void    setWiclaxDeviceId(String v)    { set("wiclax.deviceId",  v); }

    public String  getWiclaxUrl()                 { return get("wiclax.url",       DEF_WICLAX_URL); }
    public void    setWiclaxUrl(String v)         { set("wiclax.url",       v); }

    public int     getWiclaxPort()                { return getInt("wiclax.port",    DEF_WICLAX_PORT); }
    public void    setWiclaxPort(int v)           { set("wiclax.port",      str(v)); }

    // ------------------------------------------------------------------ //
    // Local API
    // ------------------------------------------------------------------ //
    public boolean isLocalApiEnabled()            { return getBool("localapi.enabled",  DEF_LOCAL_API_ENABLED); }
    public void    setLocalApiEnabled(boolean v)  { set("localapi.enabled",  str(v)); }

    public int     getLocalApiPort()              { return getInt("localapi.port",     DEF_LOCAL_API_PORT); }
    public void    setLocalApiPort(int v)         { set("localapi.port",     str(v)); }

    public String  getLocalApiEndpoint()          { return get("localapi.endpoint", DEF_LOCAL_API_ENDPOINT); }
    public void    setLocalApiEndpoint(String v)  { set("localapi.endpoint", v); }

    // ------------------------------------------------------------------ //
    // Reader
    // ------------------------------------------------------------------ //
    public int     getZebraPort()                 { return getInt("reader.zebraPort",       DEF_ZEBRA_PORT); }
    public void    setZebraPort(int v)            { set("reader.zebraPort",       str(v)); }

    public int     getConnectionTimeout()         { return getInt("reader.connectionTimeout", DEF_CONN_TIMEOUT); }
    public void    setConnectionTimeout(int v)    { set("reader.connectionTimeout", str(v)); }

    public boolean isAutoReconnect()              { return getBool("reader.autoReconnect",  DEF_AUTO_RECONNECT); }
    public void    setAutoReconnect(boolean v)    { set("reader.autoReconnect",  str(v)); }

    // ------------------------------------------------------------------ //
    // Data / EPC
    // ------------------------------------------------------------------ //
    public String  getEpcFormat()                 { return get("data.epcFormat", DEF_EPC_FORMAT); }
    public void    setEpcFormat(String v)         { set("data.epcFormat", v); }

    public int     getEpcLastN()                  { return getInt("data.epcLastN", DEF_EPC_LAST_N); }
    public void    setEpcLastN(int v)             { set("data.epcLastN", str(v)); }

    public String  getCsvDir()                    { return get("data.csvDir", DEF_CSV_DIR); }
    public void    setCsvDir(String v)            { set("data.csvDir", v); }

    public int     getSuppressionSecs()           { return getInt("data.suppressionSecs", DEF_SUPPRESSION_SECS); }
    public void    setSuppressionSecs(int v)      { set("data.suppressionSecs", str(v)); }
    public long    getSuppressionWindowMs()       { return (long) getSuppressionSecs() * 1000L; }

    // ------------------------------------------------------------------ //
    // FeiBot
    // ------------------------------------------------------------------ //
    public boolean isFeiBotEnabled()               { return getBool("feibot.enabled",      DEF_FEIBOT_ENABLED); }
    public void    setFeiBotEnabled(boolean v)     { set("feibot.enabled",      str(v)); }

    public String  getFeiBotServerIp()             { return get("feibot.serverIp",         DEF_FEIBOT_SERVER_IP); }
    public void    setFeiBotServerIp(String v)     { set("feibot.serverIp",     v); }

    public int     getFeiBotServerPort()           { return getInt("feibot.serverPort",     DEF_FEIBOT_SERVER_PORT); }
    public void    setFeiBotServerPort(int v)      { set("feibot.serverPort",   str(v)); }

    public String  getFeiBotDeviceId()             { return get("feibot.deviceId",          DEF_FEIBOT_DEVICE_ID); }
    public void    setFeiBotDeviceId(String v)     { set("feibot.deviceId",     v); }

    public String  getFeiBotDeviceModel()          { return get("feibot.deviceModel",       DEF_FEIBOT_DEVICE_MODEL); }
    public void    setFeiBotDeviceModel(String v)  { set("feibot.deviceModel",  v); }

    // ------------------------------------------------------------------ //
    // RufusAPI
    // ------------------------------------------------------------------ //
    public boolean isRufusApiEnabled()               { return getBool("rufusapi.enabled",       DEF_RUFUSAPI_ENABLED); }
    public void    setRufusApiEnabled(boolean v)     { set("rufusapi.enabled",       str(v)); }

    public String  getRufusApiKey()                  { return get("rufusapi.apiKey",            DEF_RUFUSAPI_API_KEY); }
    public void    setRufusApiKey(String v)          { set("rufusapi.apiKey",        v); }

    public String  getRufusApiDeviceId()             { return get("rufusapi.deviceId",          DEF_RUFUSAPI_DEVICE_ID); }
    public void    setRufusApiDeviceId(String v)     { set("rufusapi.deviceId",      v); }

    public String  getRufusApiSerialNumber()         { return get("rufusapi.serialNumber",       DEF_RUFUSAPI_SERIAL_NUM); }
    public void    setRufusApiSerialNumber(String v) { set("rufusapi.serialNumber",  v); }

    public String  getRufusApiAlias()                { return get("rufusapi.alias",              DEF_RUFUSAPI_ALIAS); }
    public void    setRufusApiAlias(String v)        { set("rufusapi.alias",         v); }

    // ------------------------------------------------------------------ //
    // Timing points
    // ------------------------------------------------------------------ //
    public String  getTimingPoints()              { return get("timingpoints", ""); }
    public void    setTimingPoints(String v)      { set("timingpoints", v != null ? v : ""); }

    /**
     * Formats a raw EPC string according to the configured EPC format.
     * FULL  → return as-is
     * LAST_N → return the last N hex characters
     */
    public String formatEpc(String rawEpc) {
        if ("FULL".equals(getEpcFormat())) return rawEpc;
        int n = getEpcLastN();
        return rawEpc.length() > n ? rawEpc.substring(rawEpc.length() - n) : rawEpc;
    }

    // ------------------------------------------------------------------ //
    // Internal helpers
    // ------------------------------------------------------------------ //
    private String  get(String key, String def)    { return props.getProperty(key, def); }
    private void    set(String key, String value)  { props.setProperty(key, value); }
    private boolean getBool(String key, boolean d) { return Boolean.parseBoolean(props.getProperty(key, str(d))); }
    private int     getInt(String key, int d)      {
        try { return Integer.parseInt(props.getProperty(key, str(d))); }
        catch (NumberFormatException e) { return d; }
    }
    private static String str(boolean v) { return String.valueOf(v); }
    private static String str(int v)     { return String.valueOf(v); }
}
