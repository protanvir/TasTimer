/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rfidsample;

import com.mot.rfid.api3.*;
import com.mot.rfid.api3.PreFilters.PreFilter;
import java.awt.Cursor;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.util.Hashtable;
import javax.swing.JLabel;
import java.io.File;


/**
 *
 * @author CFRN78
 */
public class RFIDBase {
       // Generic Interface
       RFIDReader myReader = null;

       RFIDMainDlg mainApp = null;
       public Hashtable tagStore = null;
       
       public static final String API_SUCCESS = "Function Succeeded";
       public static final String PARAM_ERROR = "Parameter Error";
       final String APP_NAME = "J_RFIDHostSample";
       public boolean isConnected;
       public String hostName = null;
       public int port = 5084;

       public boolean isAccessSequenceRunning = false;
       String[] memoryBank = new String[] {"Reserved", "EPC", "TID", "USER"};

       String[] tagState = new String[] {"New", "Gone", "Back", "Moving", "Stationary", "None"};

       // To display tag read count
       public long uniqueTags = 0;
       public long totalTags = 0;

       // Access Filter
       public AccessFilter accessFilter = null;
       public boolean isAccessFilterSet = false;

       // Post Filter
       public PostFilter postFilter = null;
       public boolean isPostFilterSet = false;

       // Antenna Info
       public AntennaInfo antennaInfo = null;

       // Pre Filter
       public PreFilters preFilters = null;

       public PreFilters.PreFilter preFilter1 = null;
       public PreFilters.PreFilter preFilter2 = null;

       public String preFilterTagPattern1 = null;
       public String preFilterTagPattern2 = null;

       public boolean isPreFilterSet1 = false;
       public boolean isPreFilterSet2 = false;
       public int preFilterActionIndex1 = 0;
       public int preFilterActionIndex2 = 0;
       
       public TriggerInfo triggerInfo = null;

       // Reader Management Interface
       ReaderManagement rm = null;
       public boolean isRmConnected = false;
       public LoginInfo loginInfo;
       public int readerTypeIndex = 1;

       public int rowId = 0;
       
       public	String m_strClientCertFilePath;
       public 	String m_strClientKeyFilePath;
       public 	String m_strRootCertFilePath;
       public 	String m_strKeyPassword;
       boolean  m_bSecureConnection;
       boolean  m_bValidatePeer;

       private boolean tagStorageSettingsPhasInfoEnabled = false;

    public RFIDReader getMyReader() {
        return myReader;
    }

    public RFIDBase(RFIDMainDlg rfidSample)
    {
        /* Generic Reader Inteface */

        // Create Reader Object
        myReader = new RFIDReader();

        mainApp = rfidSample;

        // Hash table to hold the tag data
        tagStore = new Hashtable();
        isAccessSequenceRunning = false;

        // Create the Access Filter
        accessFilter = new AccessFilter();

        // create the post filter
        postFilter = new PostFilter();

        // Create Antenna Info
        antennaInfo = new AntennaInfo();

        // Create Pre Filter
        preFilters = new PreFilters();

        preFilter1 = preFilters.new PreFilter();
        preFilter2 = preFilters.new PreFilter();

        triggerInfo = new TriggerInfo();

        triggerInfo.StartTrigger.setTriggerType(START_TRIGGER_TYPE.START_TRIGGER_TYPE_IMMEDIATE);
        triggerInfo.StopTrigger.setTriggerType(STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_IMMEDIATE);

        triggerInfo.TagEventReportInfo.setReportNewTagEvent(TAG_EVENT_REPORT_TRIGGER.MODERATED);
        triggerInfo.TagEventReportInfo.setNewTagEventModeratedTimeoutMilliseconds((short)500);

        triggerInfo.TagEventReportInfo.setReportTagInvisibleEvent(TAG_EVENT_REPORT_TRIGGER.MODERATED);
        triggerInfo.TagEventReportInfo.setTagInvisibleEventModeratedTimeoutMilliseconds((short)500);

        triggerInfo.TagEventReportInfo.setReportTagBackToVisibilityEvent(TAG_EVENT_REPORT_TRIGGER.MODERATED);
        triggerInfo.TagEventReportInfo.setTagBackToVisibilityModeratedTimeoutMilliseconds((short)500);

        triggerInfo.setTagReportTrigger(1);
        
        /* Reader Management Interface */
        rm = new ReaderManagement();
        loginInfo = new LoginInfo();
        loginInfo.setUserName("admin");

        m_strRootCertFilePath = m_strClientKeyFilePath = m_strClientCertFilePath = "";
        m_bSecureConnection = m_bValidatePeer = false;
    }


    public boolean connectToReader(String readerHostName, int readerPort)
    {
        boolean retVal = false;
        hostName = readerHostName;
        port = readerPort;
        myReader.setHostName(hostName);
        loginInfo.setHostName(hostName);
        myReader.setPort(port);
        mainApp.setTitle("J_RFIDHostSample");
        mainApp.setCursor(Cursor.WAIT_CURSOR);

        try {
            myReader.connect();
            
            myReader.Events.setInventoryStartEvent(true);
            myReader.Events.setInventoryStopEvent(true);
            myReader.Events.setAccessStartEvent(true);
            myReader.Events.setAccessStopEvent(true);
            myReader.Events.setAntennaEvent(true);
            myReader.Events.setGPIEvent(true);
            myReader.Events.setBufferFullEvent(true);
            myReader.Events.setBufferFullWarningEvent(true);
            myReader.Events.setReaderDisconnectEvent(true);
            myReader.Events.setReaderExceptionEvent(true);
            myReader.Events.setTagReadEvent(true);
            myReader.Events.setAttachTagDataWithReadEvent(false);
            myReader.Events.setTemperatureAlarmEvent(true);

            myReader.Events.addEventsListener(new EventsHandler(mainApp));
            retVal = true;
            isConnected = true;
            mainApp.setTitle("Connected to " + hostName);
            postStatusNotification(API_SUCCESS, null);

            //update Menu Items
            mainApp.updateGenericMenuItems(true);

        }        
        catch (InvalidUsageException ex) {
            postStatusNotification(PARAM_ERROR, ex.getVendorMessage());
        } catch (OperationFailureException ex) {
            postStatusNotification(ex.getStatusDescription(), ex.getVendorMessage());
        }
        
        mainApp.setCursor(Cursor.DEFAULT_CURSOR);
        mainApp.updateUI();
        return retVal;
    }

    public void disconnectReader()
    {
        try {
                myReader.disconnect();
                isConnected = false;

            //update Menu Items
            mainApp.updateGenericMenuItems(false);

            postStatusNotification(API_SUCCESS, null);
            mainApp.updateUI();

            mainApp.setTitle("J_RFIDHostSample");
            
        } catch (InvalidUsageException ex) {
            postStatusNotification(PARAM_ERROR, ex.getVendorMessage());
            
        } catch (OperationFailureException ex) {
            postStatusNotification(ex.getStatusDescription(), ex.getVendorMessage());

        }
    }

    public void startRead()
    {
        PostFilter myPostFilter = null;
        AntennaInfo myAntennInfo = null;
        AccessFilter myAccessFilter = null;

        // Set the Antenna Info
        if (antennaInfo.getAntennaID() != null)
            myAntennInfo = antennaInfo;

        // set the post filter
        if (isPostFilterSet)
            myPostFilter = postFilter;

        // set the access filter
        if (isAccessFilterSet)
            myAccessFilter = accessFilter;
        
        try {

            // Access Sequence Operation - Read the specified Memory Bank
            int memoryBankSelected = mainApp.getjComboBoxMemBank().getSelectedIndex();
            if ( memoryBankSelected > 0)
            {
                TagAccess tagaccess = new TagAccess();
                MEMORY_BANK memoryBank = MEMORY_BANK.MEMORY_BANK_EPC;
                TagAccess.Sequence opSequence = tagaccess.new Sequence(tagaccess);
                TagAccess.Sequence.Operation op1 = opSequence.new Operation();
                op1.setAccessOperationCode(ACCESS_OPERATION_CODE.ACCESS_OPERATION_READ);

                switch (--memoryBankSelected)
                {
                    case 0:
                        memoryBank = MEMORY_BANK.MEMORY_BANK_RESERVED;
                        break;
                    case 1:
                        memoryBank = MEMORY_BANK.MEMORY_BANK_EPC;
                        break;
                    case 2:
                        memoryBank = MEMORY_BANK.MEMORY_BANK_TID;
                        break;
                    case 3:
                        memoryBank = MEMORY_BANK.MEMORY_BANK_USER;
                        break;
                }
                op1.ReadAccessParams.setMemoryBank(memoryBank);
                op1.ReadAccessParams.setByteCount(0);
                op1.ReadAccessParams.setByteOffset(0);
                op1.ReadAccessParams.setAccessPassword(0);
                myReader.Actions.TagAccess.OperationSequence.deleteAll();
                myReader.Actions.TagAccess.OperationSequence.add(op1);
                myReader.Actions.purgeTags();
                myReader.Actions.TagAccess.OperationSequence.performSequence(myAccessFilter, triggerInfo, myAntennInfo);

                isAccessSequenceRunning = true;

            }
            else // Simple Inventory
            {
                myReader.Actions.purgeTags();
                myReader.Actions.Inventory.perform(myPostFilter, triggerInfo, myAntennInfo);
            }
            postStatusNotification(API_SUCCESS, null);

        } catch (InvalidUsageException ex) {
            postStatusNotification(PARAM_ERROR, ex.getVendorMessage());
        } catch (OperationFailureException ex) {
                postStatusNotification(ex.getStatusDescription(), ex.getVendorMessage());
        }
        
    }

    public void stopRead()
    {
        try {
            if (isAccessSequenceRunning)
            {
                myReader.Actions.TagAccess.OperationSequence.stopSequence();
                myReader.Actions.TagAccess.OperationSequence.deleteAll();
                isAccessSequenceRunning = false;
            }
            else
            {
                myReader.Actions.Inventory.stop();
            }
            postStatusNotification(API_SUCCESS, null);

        } catch (InvalidUsageException ex) {
            postStatusNotification(PARAM_ERROR, ex.getVendorMessage());
        } catch (OperationFailureException ex) {
            postStatusNotification(ex.getStatusDescription(), ex.getVendorMessage());
        }

    }

    public boolean logIn(READER_TYPE readerType)
    {
        boolean retVal = false;
        mainApp.setCursor(Cursor.WAIT_CURSOR);

        try {
            rm.login(loginInfo, readerType);
            retVal = true;
            isRmConnected = true;
            postStatusNotification(API_SUCCESS, null);

            //update Menu Items
            mainApp.updateRMMenuItems(true);


        } catch (InvalidUsageException ex) {
            postStatusNotification(PARAM_ERROR, ex.getVendorMessage());
        } catch (OperationFailureException ex) {
            postStatusNotification(ex.getStatusDescription(), ex.getVendorMessage());
        }
        mainApp.setCursor(Cursor.DEFAULT_CURSOR);
        return retVal;
    }

    public void logOut() {
        try {
            rm.logout();
            postStatusNotification(API_SUCCESS, null);
        } catch (InvalidUsageException ex) {
            postStatusNotification(PARAM_ERROR, ex.getVendorMessage());

        } catch (OperationFailureException ex) {
            postStatusNotification(ex.getStatusDescription(), ex.getVendorMessage());
        }
        isRmConnected = false;
        //update Menu Items
        mainApp.updateRMMenuItems(false);
        
    }



    public void postStatusNotification(String statusMsg, String vendorMsg)
    {
        String status;
        if (vendorMsg != null &&
            !vendorMsg.isEmpty())
            status = statusMsg + "[" + vendorMsg + "]";
        else
            status = statusMsg;

        mainApp.getjLabelStatus().setText(status);
    }

    public String getSelectedTagID()
    {
        Object tagID = null;
        TableModel tableModel = mainApp.getjInventoryTable().getModel();
        int selectedRow = mainApp.getjInventoryTable().getSelectedRow();
        if (selectedRow >= 0)
        {
            tagID = ((DefaultTableModel)tableModel).getValueAt(selectedRow, 0);
        }
        return String.valueOf(tagID);
        
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
    public static String byteArrayToHexString(byte[] b) {
        StringBuffer sb = new StringBuffer(b.length * 2);
        for (int i = 0; i < b.length; i++) {
            int v = b[i] & 0xff;
            if (v < 16) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(v));
        }
        return sb.toString().toUpperCase();
    }

    public MEMORY_BANK getMemoryBankEnum(int index)
    {
        MEMORY_BANK memBank = MEMORY_BANK.MEMORY_BANK_EPC;
        switch(index)
        {
            case 0:
                memBank = MEMORY_BANK.MEMORY_BANK_RESERVED;
                break;
            case 1:
                memBank = MEMORY_BANK.MEMORY_BANK_EPC;
                break;
            case 2:
                memBank = MEMORY_BANK.MEMORY_BANK_TID;
                break;
            case 3:
                memBank = MEMORY_BANK.MEMORY_BANK_USER;
                break;
        }
        return memBank;
    }

    public void setTagStorageSettingsPhasInfoEnabled(boolean value)
    {
       tagStorageSettingsPhasInfoEnabled = value;
    }

    public boolean getTagStorageSettingsPhasInfoEnabled()
    {
       return tagStorageSettingsPhasInfoEnabled;
    }

    public class EventsHandler implements RfidEventsListener
    {
        RFIDMainDlg inventoryUI = null;


        public EventsHandler(RFIDMainDlg rfidSample){
            inventoryUI = rfidSample;
        }
        public void eventReadNotify(RfidReadEvents rre) {
           // System.out.println("Read Event Signalled");
            TableModel tableModel = inventoryUI.getjInventoryTable().getModel();
            JLabel totalTagsLabel = inventoryUI.getjLabelTotalTags();



            // Read tags
            TagData[] myTags = myReader.Actions.getReadTags(100);
            if (myTags != null)
            {
                for (int index = 0; index < myTags.length; index++){

                    TagData tag = myTags[index];
                    String key = tag.getTagID();
                    String memBank = new String ();
                    
                    if (tag.getOpCode() != ACCESS_OPERATION_CODE.ACCESS_OPERATION_NONE)
                    {
                        key += tag.getMemoryBank().toString();
                        if (tag.getOpStatus() != ACCESS_OPERATION_STATUS.ACCESS_SUCCESS)
                            continue;
                    }
                   
                    if (tagStore.containsKey(key))
                    {
                        Integer rowIndex = (Integer)tagStore.get(key);
                        Object seenCount = ((DefaultTableModel)tableModel).getValueAt(rowIndex.intValue(), 3);

                        // Update Seen Count
                        Long seenCountTemp = Long.valueOf(seenCount.toString());
                     
                        long seenCountTmp = (long)tag.getTagSeenCount() + seenCountTemp.longValue();
                        Long seenCountNew = Long.valueOf(seenCountTmp);

                       // Update Tag State
                       ((DefaultTableModel)tableModel).setValueAt(tagState[tag.getTagEvent().getValue() - 1], rowIndex.intValue(), 1);

                       // Update Antenna ID
                       ((DefaultTableModel)tableModel).setValueAt(Short.valueOf(tag.getAntennaID()), rowIndex.intValue(), 2);

                       // Update Seen Count
                       ((DefaultTableModel)tableModel).setValueAt(seenCountNew, rowIndex.intValue(), 3);

                       // Update Peak RSSI
                       ((DefaultTableModel)tableModel).setValueAt(Short.valueOf(tag.getPeakRSSI()), rowIndex.intValue(), 4);

                       // Update Phase
                       double dPhase = 0;
                       if(tagStorageSettingsPhasInfoEnabled) 
                       {
                            dPhase = (180.0/32767)*tag.getPhase();
                       }
                       DecimalFormat fPhase =  new DecimalFormat("##.##");
                       ((DefaultTableModel)tableModel).setValueAt(Double.valueOf(fPhase.format(dPhase)), rowIndex.intValue(), 5);
                       
                       // Update PC bits
                       ((DefaultTableModel)tableModel).setValueAt(Integer.toHexString(tag.getPC()), rowIndex.intValue(), 6);

                        if (tag.getOpCode() != ACCESS_OPERATION_CODE.ACCESS_OPERATION_NONE)
                        {
                           // Update Memory Bank Data
                           ((DefaultTableModel)tableModel).setValueAt(tag.getMemoryBankData(), rowIndex.intValue(), 7);

                           // Update Memory Bank Type
                           ((DefaultTableModel)tableModel).setValueAt(memoryBank[tag.getMemoryBank().getValue()], rowIndex.intValue(), 8);

                           // Update offset
                           ((DefaultTableModel)tableModel).setValueAt(Integer.valueOf(tag.getMemoryBankDataOffset()), rowIndex.intValue(), 9);
                        }
                    }
                    else
                    {
                        if (tag.getOpCode() != ACCESS_OPERATION_CODE.ACCESS_OPERATION_NONE)
                        {
                            memBank = memoryBank[tag.getMemoryBank().getValue()];
                        }
                        double dPhase = 0;
                        if(tagStorageSettingsPhasInfoEnabled) 
                        {
                            dPhase = (180.0/32767)*tag.getPhase();
                        }
                        DecimalFormat fPhase =  new DecimalFormat("##.##");
                        ((DefaultTableModel)tableModel).insertRow(rowId, new Object[] {tag.getTagID(), tagState[tag.getTagEvent().getValue() - 1], Short.valueOf(tag.getAntennaID()), Short.valueOf(tag.getTagSeenCount()), Short.valueOf(tag.getPeakRSSI()), Double.valueOf(fPhase.format(dPhase)),Integer.toHexString(tag.getPC()), tag.getMemoryBankData(), memBank, Integer.valueOf(tag.getMemoryBankDataOffset())});

                        tagStore.put(key, Integer.valueOf(rowId));
                        rowId++;
                        uniqueTags++;
                    }
                    totalTags+=tag.getTagSeenCount();

                    totalTagsLabel.setText(String.valueOf(uniqueTags) + "(" + String.valueOf(totalTags) + ")");
                }
                
            }
        }

        public void eventStatusNotify(RfidStatusEvents rse) {

            STATUS_EVENT_TYPE statusType = rse.StatusEventData.getStatusEventType();

            if (statusType == STATUS_EVENT_TYPE.INVENTORY_START_EVENT)
            {
                mainApp.postNotification(statusType.toString(), "");
            }
            else if(statusType == STATUS_EVENT_TYPE.INVENTORY_STOP_EVENT)
            {
                mainApp.StartReadButtonClicked();
                mainApp.postNotification(statusType.toString(), "");
            }
            else if (statusType == STATUS_EVENT_TYPE.ACCESS_START_EVENT)
            {
                mainApp.postNotification(statusType.toString(), "");
            }
            else if (statusType == STATUS_EVENT_TYPE.ACCESS_STOP_EVENT)
            {
                mainApp.postNotification(statusType.toString(), "");
            }
            else if (statusType == STATUS_EVENT_TYPE.ANTENNA_EVENT)
            {
                int antennaID = rse.StatusEventData.AntennaEventData.getAntennaID();

                String antennaEventValue = "";
                if (rse.StatusEventData.AntennaEventData.getAntennaEvent() == ANTENNA_EVENT_TYPE.ANTENNA_CONNECTED)
                    antennaEventValue = String.valueOf(antennaID)+ " Connected";
                else if(rse.StatusEventData.AntennaEventData.getAntennaEvent() == ANTENNA_EVENT_TYPE.ANTENNA_DISCONNECTED)
                    antennaEventValue = String.valueOf(antennaID)+" Disconnected";

                mainApp.postNotification(statusType.toString(), antennaEventValue);

            }
            else if (statusType == STATUS_EVENT_TYPE.READER_EXCEPTION_EVENT)
            {
                mainApp.postNotification(statusType.toString(), rse.StatusEventData.ReaderExceptionEventData.getReaderExceptionEventInfo());
            }
            else if (statusType == STATUS_EVENT_TYPE.BUFFER_FULL_WARNING_EVENT)
            {
                mainApp.postNotification(statusType.toString(), "");
            }
            else if (statusType == STATUS_EVENT_TYPE.BUFFER_FULL_EVENT)
            {
                mainApp.postNotification(statusType.toString(), "");
            }
            else if (statusType == STATUS_EVENT_TYPE.DISCONNECTION_EVENT)
            {
                String disconnectionEventData = "";

                if (rse.StatusEventData.DisconnectionEventData.getDisconnectionEvent() == DISCONNECTION_EVENT_TYPE.READER_INITIATED_DISCONNECTION)
                    disconnectionEventData = "Reader Initiated Disconnection";
                else if ((rse.StatusEventData.DisconnectionEventData.getDisconnectionEvent() == DISCONNECTION_EVENT_TYPE.CONNECTION_LOST))
                    disconnectionEventData = "Connection Lost";
                else if ((rse.StatusEventData.DisconnectionEventData.getDisconnectionEvent() == DISCONNECTION_EVENT_TYPE.READER_EXCEPTION))
                    disconnectionEventData = "Reader Exception";

                mainApp.postNotification(statusType.toString(), disconnectionEventData);
                BackgroundDisconnectThread disconnetThread = new BackgroundDisconnectThread(mainApp.rfidBase);
                disconnetThread.start();
            }
            else if (statusType == STATUS_EVENT_TYPE.GPI_EVENT)
            {
                mainApp.postNotification(statusType.toString(), "");
                mainApp.setGPIOnScreen(rse.StatusEventData.GPIEventData.getGPIPort(), true, rse.StatusEventData.GPIEventData.getGPIEventState() ? true : false);
            }
            else if (statusType == STATUS_EVENT_TYPE.TEMPERATURE_ALARM_EVENT)
            {
                mainApp.postNotification(statusType.toString(), "Temperature " + rse.StatusEventData.TemperatureAlarmData.getTemperatureSource() +", " 
                        +rse.StatusEventData.TemperatureAlarmData.getCurrentTemperature() + ", Level" + 
                        rse.StatusEventData.TemperatureAlarmData.getAlarmLevel().toString());
            }

        }
    }
}
class BackgroundDisconnectThread extends Thread
{
    RFIDBase rfidBase;
    public BackgroundDisconnectThread(RFIDBase _rfidBase) {
        rfidBase = _rfidBase;
    }


    public void run()
    {
        // invoke Disconnect reader
        rfidBase.disconnectReader();
    }

}
