public class UnifiedAntennaStatus {
    private int portNumber;
    private boolean isConnected;
    private String statusStr; // "Connected", "Not Connected", etc.

    public UnifiedAntennaStatus(int portNumber, boolean isConnected, String statusStr) {
        this.portNumber = portNumber;
        this.isConnected = isConnected;
        this.statusStr = statusStr;
    }

    public int getPortNumber() {
        return portNumber;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public String getStatusStr() {
        return statusStr;
    }

    @Override
    public String toString() {
        return "Antenna " + portNumber + ": " + statusStr;
    }
}
