import com.impinj.octane.AntennaStatus;
import java.util.List;

public interface RFIDDataListener {
    void onTagRead(String epc, String timestamp, String readerIp, int antennaPort);

    void onReaderStatus(String status, boolean isConnected);

    void onAntennaStatus(List<AntennaStatus> antennaStatuses);
}
