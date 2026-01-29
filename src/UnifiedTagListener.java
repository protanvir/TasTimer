import java.util.List;

public interface UnifiedTagListener {
    void onTagRead(String epc, String timestamp, String readerAddress, int antennaPort);
    void onReaderStatus(String status, boolean isConnected);
    void onAntennaStatus(List<String> antennaStatuses);
}
