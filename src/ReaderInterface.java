public interface ReaderInterface {
    void connect(String hostname);
    void disconnect();
    void startReading();
    void stopReading();
    boolean isConnected();
}
