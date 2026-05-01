/**
 * Represents a registered race participant.
 *
 * All fields are set at import time from the CSV.
 * Runtime state (hasBeenRead, lastReadTime) is updated live as tags are read.
 */
public class Participant {

    private final String bibNumber;
    private final String firstName;
    private final String lastName;
    private String epc;       // raw full EPC from the reader/CSV; may be empty if unassigned
    private final String category;
    private final String waveId;

    // Runtime state — updated by ParticipantRegistry.markRead()
    private volatile boolean hasBeenRead = false;
    private volatile String  lastReadTime = "";

    public Participant(String bibNumber, String firstName, String lastName,
                       String epc, String category, String waveId) {
        this.bibNumber = bibNumber != null ? bibNumber.trim() : "";
        this.firstName = firstName != null ? firstName.trim() : "";
        this.lastName  = lastName  != null ? lastName.trim()  : "";
        this.epc       = epc       != null ? epc.trim().toUpperCase() : "";
        this.category  = category  != null ? category.trim() : "";
        this.waveId    = waveId    != null ? waveId.trim()   : "";
    }

    // ------------------------------------------------------------------ //
    // Getters
    // ------------------------------------------------------------------ //
    public String  getBibNumber()   { return bibNumber; }
    public String  getFirstName()   { return firstName; }
    public String  getLastName()    { return lastName; }
    public String  getEpc()         { return epc; }
    public String  getCategory()    { return category; }
    public String  getWaveId()      { return waveId; }
    public boolean hasBeenRead()    { return hasBeenRead; }
    public String  getLastReadTime(){ return lastReadTime; }

    /** Returns "First Last", or just first/last name if one is blank. */
    public String getFullName() {
        if (firstName.isEmpty()) return lastName;
        if (lastName.isEmpty())  return firstName;
        return firstName + " " + lastName;
    }

    // ------------------------------------------------------------------ //
    // Setters (package-private — only ParticipantRegistry should call these)
    // ------------------------------------------------------------------ //
    void setEpc(String epc) {
        this.epc = epc != null ? epc.trim().toUpperCase() : "";
    }

    void markRead(String timestamp) {
        this.hasBeenRead  = true;
        this.lastReadTime = timestamp;
    }
}
