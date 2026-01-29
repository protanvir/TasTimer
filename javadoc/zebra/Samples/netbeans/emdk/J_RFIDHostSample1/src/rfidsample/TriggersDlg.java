/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * TriggersDlg.java
 *
 * Created on May 8, 2011, 7:00:59 PM
 */

package rfidsample;

import com.mot.rfid.api3.HANDHELD_TRIGGER_EVENT_TYPE;
import com.mot.rfid.api3.START_TRIGGER_TYPE;
import com.mot.rfid.api3.STOP_TRIGGER_TYPE;
import com.mot.rfid.api3.SYSTEMTIME;
import com.mot.rfid.api3.TAG_EVENT_REPORT_TRIGGER;
import com.mot.rfid.api3.TAG_MOVING_EVENT_REPORT;
import com.mot.rfid.api3.TriggerInfo;
import java.awt.event.ItemEvent;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.StringTokenizer;

/**
 *
 * @author CFRN78
 */
public class TriggersDlg extends javax.swing.JDialog {
    String currentDate;
    String currentTime;
    TriggerInfo triggerInfo;
    int numOfGPIS;
    /** Creates new form TriggersDlg */
    public TriggersDlg(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        triggerInfo = RFIDMainDlg.rfidBase.triggerInfo;

        // Trigger Dialog
        initializeTriggerDlg();

    }

    private void initializeTriggerDlg()
    {

        //numOfGPIS = RFIDMainDlg.rfidBase.getMyReader().ReaderCapabilities.getNumGPIPorts();
        numOfGPIS = 4;

        // Start Trigger Settings
        jComboBoxStartTriggerType.setSelectedIndex(triggerInfo.StartTrigger.getTriggerType().getValue());
        updateStartTriggerPanel();

        //  GPI Port
        for (int index = 1; index <= numOfGPIS; index ++)
            jComboBoxStartTriggerGPIPort.addItem(String.valueOf(index));

        // Periodic
        jTextFieldStartTriggerPeriod.setText(String.valueOf(triggerInfo.StartTrigger.Periodic.getPeriod()));
        //triggerInfo.StartTrigger.Periodic.
        DateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
        DateFormat timeFormat = new SimpleDateFormat("hh:mm:ss");
        //jFormattedTextFieldStartTriggerStartDate.setfor
        Date now=new Date();
        currentDate=dateFormat.format(now);
        jFormattedTextFieldStartTriggerStartDate.setValue(currentDate);

        currentTime=timeFormat.format(now);
        jFormattedTextFieldStartTriggerStartTime.setValue(currentTime);


        // GPI
        if (triggerInfo.StartTrigger.GPI.isGPIEvent())
            jCheckBoxStartTriggerLowToHigh.setSelected(true);
        else
            jCheckBoxStartTriggerHighToLow.setSelected(true);

        int startPortIndex = triggerInfo.StartTrigger.GPI.getPortNumber();
        if (startPortIndex > 0) startPortIndex -= 1;
        jComboBoxStartTriggerGPIPort.setSelectedIndex(startPortIndex);


        // Handheld
        if (triggerInfo.StartTrigger.Handheld.getHandheldTriggerEvent() == HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_PRESSED)
            jCheckBoxStartTriggerGunPressed.setSelected(true);
        else
            jCheckBoxStartTriggerGunReleased.setSelected(true);


        // Stop Trigger Settings

        //  GPI Port
        for (int index = 1; index <= numOfGPIS; index ++)
            jComboBoxStopTriggerGPIPort.addItem(String.valueOf(index));


        jComboBoxStopTriggerType.setSelectedIndex(triggerInfo.StopTrigger.getTriggerType().getValue());
        updateStopTriggerPanel();

        // Duration Stop Trigger
        jTextFieldStopTriggerDuration.setText(String.valueOf(triggerInfo.StopTrigger.getDurationMilliSeconds()));

        // N Attempts Stop Trigger
        jTextFieldStopTriggerNAttempts.setText(String.valueOf(triggerInfo.StopTrigger.NumAttempts.getN()));
        jTextFieldStopTriggerNAttemptTimeout.setText(String.valueOf(triggerInfo.StopTrigger.NumAttempts.getTimeout()));

        // Tag Observation Stop Trigger
        jTextFieldStopTriggerNObservation.setText(String.valueOf(triggerInfo.StopTrigger.TagObservation.getN()));
        jTextFieldStopTriggerObservationTimeout.setText(String.valueOf(triggerInfo.StopTrigger.TagObservation.getTimeout()));

        // GPI Stop Trigger
        int stopPortIndex = triggerInfo.StopTrigger.GPI.getPortNumber();
        if (stopPortIndex > 0) stopPortIndex -= 1;
        jComboBoxStopTriggerGPIPort.setSelectedIndex(stopPortIndex);
        jTextFieldStopTriggerGPITimeout.setText(String.valueOf(triggerInfo.StopTrigger.GPI.getTimeout()));
        if (triggerInfo.StopTrigger.GPI.isGPIEvent())
            jCheckBoxStopTriggerLowToHigh.setSelected(true);  
        else
            jCheckBoxStopTriggerHighToLow.setSelected(true);

        // Handheld Stop Trigger
        jTextFieldStopTriggerHandheldTimeout.setText(String.valueOf(triggerInfo.StopTrigger.Handheld.getHandheldTriggerTimeout()));
        if (triggerInfo.StopTrigger.Handheld.getHandheldTriggerEvent() == HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_PRESSED )
            jCheckBoxStopTriggerGunPressed.setSelected(true);
        else
            jCheckBoxStopTriggerGunReleased.setSelected(true);

        // Tag Event Reporting Settings
        jComboBoxNewTag.setSelectedIndex(triggerInfo.TagEventReportInfo.getReportNewTagEvent().getValue());
        jTextFieldReportTriggerNewTag.setText(String.valueOf(triggerInfo.TagEventReportInfo.getNewTagEventModeratedTimeoutMilliseconds()));

        jComboBoxNewTagInvisible.setSelectedIndex(triggerInfo.TagEventReportInfo.getReportTagInvisibleEvent().getValue());
        jTextFieldReportTriggerTagInvisible.setText(String.valueOf(triggerInfo.TagEventReportInfo.getTagInvisibleEventModeratedTimeoutMilliseconds()));

        jComboBoxBackToVisible.setSelectedIndex(triggerInfo.TagEventReportInfo.getReportTagBackToVisibilityEvent().getValue());
        jTextFieldReportTriggerTagBackVisible.setText(String.valueOf(triggerInfo.TagEventReportInfo.getTagBackToVisibilityModeratedTimeoutMilliseconds()));

        jComboBoxTagMoving.setSelectedIndex(triggerInfo.TagEventReportInfo.getReportTagMovingEvent().getValue());
        jTextFieldReportTriggerTagMoving.setText(String.valueOf(triggerInfo.TagEventReportInfo.getTagStationaryModeratedTimeoutMilliseconds()));

        jTextFieldReportTrigger.setText(String.valueOf(triggerInfo.getTagReportTrigger()));
        jTextFieldPeriodicReportTrigger.setText(String.valueOf(triggerInfo.ReportTriggers.getPeriodicReportTrigger()));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanelStartTrigger = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jComboBoxStartTriggerType = new javax.swing.JComboBox();
        jPanelStartTriggerPeriodic = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jFormattedTextFieldStartTriggerStartDate = new javax.swing.JFormattedTextField();
        jLabel11 = new javax.swing.JLabel();
        jFormattedTextFieldStartTriggerStartTime = new javax.swing.JFormattedTextField();
        jLabel12 = new javax.swing.JLabel();
        jTextFieldStartTriggerPeriod = new javax.swing.JTextField();
        jPanelStartTriggerGPI = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jComboBoxStartTriggerGPIPort = new javax.swing.JComboBox();
        jCheckBoxStartTriggerHighToLow = new javax.swing.JCheckBox();
        jCheckBoxStartTriggerLowToHigh = new javax.swing.JCheckBox();
        jPanelStartTriggerHandheld = new javax.swing.JPanel();
        jLabel16 = new javax.swing.JLabel();
        jCheckBoxStartTriggerGunReleased = new javax.swing.JCheckBox();
        jCheckBoxStartTriggerGunPressed = new javax.swing.JCheckBox();
        jPanelStopTrigger = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jComboBoxStopTriggerType = new javax.swing.JComboBox();
        jPanelStopTriggerObservation = new javax.swing.JPanel();
        jLabel14 = new javax.swing.JLabel();
        jTextFieldStopTriggerNObservation = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        jTextFieldStopTriggerObservationTimeout = new javax.swing.JTextField();
        jPanelStopTriggerNAttempts = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();
        jTextFieldStopTriggerNAttempts = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        jTextFieldStopTriggerNAttemptTimeout = new javax.swing.JTextField();
        jPanelStopTriggerDuration = new javax.swing.JPanel();
        jLabel20 = new javax.swing.JLabel();
        jTextFieldStopTriggerDuration = new javax.swing.JTextField();
        jPanelStopTriggerGPI = new javax.swing.JPanel();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jComboBoxStopTriggerGPIPort = new javax.swing.JComboBox();
        jCheckBoxStopTriggerHighToLow = new javax.swing.JCheckBox();
        jCheckBoxStopTriggerLowToHigh = new javax.swing.JCheckBox();
        jLabel23 = new javax.swing.JLabel();
        jTextFieldStopTriggerGPITimeout = new javax.swing.JTextField();
        jPanelStopTriggerHandheld = new javax.swing.JPanel();
        jLabel24 = new javax.swing.JLabel();
        jCheckBoxStopTriggerGunReleased = new javax.swing.JCheckBox();
        jCheckBoxStopTriggerGunPressed = new javax.swing.JCheckBox();
        jLabel25 = new javax.swing.JLabel();
        jTextFieldStopTriggerHandheldTimeout = new javax.swing.JTextField();
        jPanelReportTrigger = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jComboBoxNewTag = new javax.swing.JComboBox();
        jTextFieldReportTriggerNewTag = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jComboBoxNewTagInvisible = new javax.swing.JComboBox();
        jTextFieldReportTriggerTagInvisible = new javax.swing.JTextField();
        jTextFieldReportTriggerTagBackVisible = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jComboBoxBackToVisible = new javax.swing.JComboBox();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        jComboBoxTagMoving = new javax.swing.JComboBox();
        jTextFieldReportTriggerTagMoving = new javax.swing.JTextField();
        jLabel28 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jTextFieldReportTrigger = new javax.swing.JTextField();
        jButtonApply = new javax.swing.JButton();
        jLabel26 = new javax.swing.JLabel();
        jTextFieldPeriodicReportTrigger = new javax.swing.JTextField();

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Triggers");
        setResizable(false);

        jTabbedPane1.setTabPlacement(javax.swing.JTabbedPane.BOTTOM);

        jLabel1.setText("Trigger Type");

        jComboBoxStartTriggerType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Immediate", "Periodic", "GPI", "Handheld Trigger" }));
        jComboBoxStartTriggerType.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBoxStartTriggerTypeItemStateChanged(evt);
            }
        });

        jLabel10.setText("Start Date");

        jLabel11.setText("Start Time");

        jLabel12.setText("Period (ms)");

        javax.swing.GroupLayout jPanelStartTriggerPeriodicLayout = new javax.swing.GroupLayout(jPanelStartTriggerPeriodic);
        jPanelStartTriggerPeriodic.setLayout(jPanelStartTriggerPeriodicLayout);
        jPanelStartTriggerPeriodicLayout.setHorizontalGroup(
            jPanelStartTriggerPeriodicLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelStartTriggerPeriodicLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(jPanelStartTriggerPeriodicLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel10)
                    .addComponent(jLabel11)
                    .addComponent(jLabel12))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 57, Short.MAX_VALUE)
                .addGroup(jPanelStartTriggerPeriodicLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jFormattedTextFieldStartTriggerStartDate, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jFormattedTextFieldStartTriggerStartTime, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldStartTriggerPeriod, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(47, 47, 47))
        );
        jPanelStartTriggerPeriodicLayout.setVerticalGroup(
            jPanelStartTriggerPeriodicLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelStartTriggerPeriodicLayout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addGroup(jPanelStartTriggerPeriodicLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(jFormattedTextFieldStartTriggerStartDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanelStartTriggerPeriodicLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel11)
                    .addComponent(jFormattedTextFieldStartTriggerStartTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20)
                .addGroup(jPanelStartTriggerPeriodicLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel12)
                    .addComponent(jTextFieldStartTriggerPeriod, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(97, Short.MAX_VALUE))
        );

        jLabel13.setText("GPI Port");

        jLabel15.setText("Event");

        jCheckBoxStartTriggerHighToLow.setText("High to Low");
        jCheckBoxStartTriggerHighToLow.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBoxStartTriggerHighToLowItemStateChanged(evt);
            }
        });
        jCheckBoxStartTriggerHighToLow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxStartTriggerHighToLowActionPerformed(evt);
            }
        });

        jCheckBoxStartTriggerLowToHigh.setText("Low to High");
        jCheckBoxStartTriggerLowToHigh.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBoxStartTriggerLowToHighItemStateChanged(evt);
            }
        });
        jCheckBoxStartTriggerLowToHigh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxStartTriggerLowToHighActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelStartTriggerGPILayout = new javax.swing.GroupLayout(jPanelStartTriggerGPI);
        jPanelStartTriggerGPI.setLayout(jPanelStartTriggerGPILayout);
        jPanelStartTriggerGPILayout.setHorizontalGroup(
            jPanelStartTriggerGPILayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelStartTriggerGPILayout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(jPanelStartTriggerGPILayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(34, 34, 34)
                .addGroup(jPanelStartTriggerGPILayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBoxStartTriggerLowToHigh)
                    .addGroup(jPanelStartTriggerGPILayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(jPanelStartTriggerGPILayout.createSequentialGroup()
                            .addComponent(jCheckBoxStartTriggerHighToLow)
                            .addGap(72, 72, 72))
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanelStartTriggerGPILayout.createSequentialGroup()
                            .addComponent(jComboBoxStartTriggerGPIPort, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(45, 45, 45))))
                .addGap(36, 36, 36))
        );
        jPanelStartTriggerGPILayout.setVerticalGroup(
            jPanelStartTriggerGPILayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelStartTriggerGPILayout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addGroup(jPanelStartTriggerGPILayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE, false)
                    .addComponent(jComboBoxStartTriggerGPIPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13))
                .addGap(64, 64, 64)
                .addGroup(jPanelStartTriggerGPILayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE, false)
                    .addComponent(jCheckBoxStartTriggerHighToLow)
                    .addComponent(jLabel15))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jCheckBoxStartTriggerLowToHigh)
                .addGap(64, 64, 64))
        );

        jLabel16.setText("Event");

        jCheckBoxStartTriggerGunReleased.setText("Trigger Released");
        jCheckBoxStartTriggerGunReleased.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBoxStartTriggerGunReleasedItemStateChanged(evt);
            }
        });
        jCheckBoxStartTriggerGunReleased.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxStartTriggerGunReleasedActionPerformed(evt);
            }
        });

        jCheckBoxStartTriggerGunPressed.setText("Trigger Pressed");
        jCheckBoxStartTriggerGunPressed.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBoxStartTriggerGunPressedItemStateChanged(evt);
            }
        });
        jCheckBoxStartTriggerGunPressed.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxStartTriggerGunPressedActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelStartTriggerHandheldLayout = new javax.swing.GroupLayout(jPanelStartTriggerHandheld);
        jPanelStartTriggerHandheld.setLayout(jPanelStartTriggerHandheldLayout);
        jPanelStartTriggerHandheldLayout.setHorizontalGroup(
            jPanelStartTriggerHandheldLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelStartTriggerHandheldLayout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(34, 34, 34)
                .addGroup(jPanelStartTriggerHandheldLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBoxStartTriggerGunPressed)
                    .addComponent(jCheckBoxStartTriggerGunReleased))
                .addGap(108, 108, 108))
        );
        jPanelStartTriggerHandheldLayout.setVerticalGroup(
            jPanelStartTriggerHandheldLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelStartTriggerHandheldLayout.createSequentialGroup()
                .addGap(117, 117, 117)
                .addGroup(jPanelStartTriggerHandheldLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE, false)
                    .addComponent(jCheckBoxStartTriggerGunReleased)
                    .addComponent(jLabel16))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jCheckBoxStartTriggerGunPressed)
                .addGap(64, 64, 64))
        );

        javax.swing.GroupLayout jPanelStartTriggerLayout = new javax.swing.GroupLayout(jPanelStartTrigger);
        jPanelStartTrigger.setLayout(jPanelStartTriggerLayout);
        jPanelStartTriggerLayout.setHorizontalGroup(
            jPanelStartTriggerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelStartTriggerLayout.createSequentialGroup()
                .addGroup(jPanelStartTriggerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanelStartTriggerLayout.createSequentialGroup()
                        .addGap(34, 34, 34)
                        .addComponent(jLabel1)
                        .addGap(49, 49, 49)
                        .addComponent(jComboBoxStartTriggerType, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanelStartTriggerLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanelStartTriggerPeriodic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
            .addGroup(jPanelStartTriggerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanelStartTriggerLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jPanelStartTriggerGPI, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(34, Short.MAX_VALUE)))
            .addGroup(jPanelStartTriggerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelStartTriggerLayout.createSequentialGroup()
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanelStartTriggerHandheld, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(8, 8, 8)))
        );
        jPanelStartTriggerLayout.setVerticalGroup(
            jPanelStartTriggerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelStartTriggerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelStartTriggerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jComboBoxStartTriggerType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanelStartTriggerPeriodic, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(jPanelStartTriggerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanelStartTriggerLayout.createSequentialGroup()
                    .addGap(40, 40, 40)
                    .addComponent(jPanelStartTriggerGPI, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
            .addGroup(jPanelStartTriggerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelStartTriggerLayout.createSequentialGroup()
                    .addContainerGap(48, Short.MAX_VALUE)
                    .addComponent(jPanelStartTriggerHandheld, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(1, 1, 1)))
        );

        jTabbedPane1.addTab("Start Trigger", jPanelStartTrigger);

        jLabel2.setText("Trigger Type");

        jComboBoxStopTriggerType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Immediate", "Duration", "GPI with timeout", "Tag Observation", "N Attempts", "Handheld Trigger" }));
        jComboBoxStopTriggerType.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBoxStopTriggerTypeItemStateChanged(evt);
            }
        });

        jLabel14.setText("Tag Observation");

        jLabel17.setText("Timeout (ms)");

        javax.swing.GroupLayout jPanelStopTriggerObservationLayout = new javax.swing.GroupLayout(jPanelStopTriggerObservation);
        jPanelStopTriggerObservation.setLayout(jPanelStopTriggerObservationLayout);
        jPanelStopTriggerObservationLayout.setHorizontalGroup(
            jPanelStopTriggerObservationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelStopTriggerObservationLayout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addGroup(jPanelStopTriggerObservationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 91, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jPanelStopTriggerObservationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jTextFieldStopTriggerObservationTimeout)
                    .addComponent(jTextFieldStopTriggerNObservation, javax.swing.GroupLayout.DEFAULT_SIZE, 106, Short.MAX_VALUE))
                .addContainerGap(93, Short.MAX_VALUE))
        );
        jPanelStopTriggerObservationLayout.setVerticalGroup(
            jPanelStopTriggerObservationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelStopTriggerObservationLayout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addGroup(jPanelStopTriggerObservationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jTextFieldStopTriggerNObservation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel14))
                .addGap(36, 36, 36)
                .addGroup(jPanelStopTriggerObservationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel17)
                    .addComponent(jTextFieldStopTriggerObservationTimeout, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(143, Short.MAX_VALUE))
        );

        jLabel18.setText("N Attempts");

        jLabel19.setText("Timeout (ms)");

        javax.swing.GroupLayout jPanelStopTriggerNAttemptsLayout = new javax.swing.GroupLayout(jPanelStopTriggerNAttempts);
        jPanelStopTriggerNAttempts.setLayout(jPanelStopTriggerNAttemptsLayout);
        jPanelStopTriggerNAttemptsLayout.setHorizontalGroup(
            jPanelStopTriggerNAttemptsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelStopTriggerNAttemptsLayout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addGroup(jPanelStopTriggerNAttemptsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel19, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanelStopTriggerNAttemptsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jTextFieldStopTriggerNAttemptTimeout)
                    .addComponent(jTextFieldStopTriggerNAttempts, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(93, Short.MAX_VALUE))
        );
        jPanelStopTriggerNAttemptsLayout.setVerticalGroup(
            jPanelStopTriggerNAttemptsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelStopTriggerNAttemptsLayout.createSequentialGroup()
                .addGap(57, 57, 57)
                .addGroup(jPanelStopTriggerNAttemptsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jTextFieldStopTriggerNAttempts, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel18))
                .addGap(36, 36, 36)
                .addGroup(jPanelStopTriggerNAttemptsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel19)
                    .addComponent(jTextFieldStopTriggerNAttemptTimeout, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(115, Short.MAX_VALUE))
        );

        jLabel20.setText("Duration (ms)");

        javax.swing.GroupLayout jPanelStopTriggerDurationLayout = new javax.swing.GroupLayout(jPanelStopTriggerDuration);
        jPanelStopTriggerDuration.setLayout(jPanelStopTriggerDurationLayout);
        jPanelStopTriggerDurationLayout.setHorizontalGroup(
            jPanelStopTriggerDurationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelStopTriggerDurationLayout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jTextFieldStopTriggerDuration, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(92, Short.MAX_VALUE))
        );
        jPanelStopTriggerDurationLayout.setVerticalGroup(
            jPanelStopTriggerDurationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelStopTriggerDurationLayout.createSequentialGroup()
                .addGap(52, 52, 52)
                .addGroup(jPanelStopTriggerDurationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jTextFieldStopTriggerDuration, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel20))
                .addContainerGap(176, Short.MAX_VALUE))
        );

        jLabel21.setText("GPI Port");

        jLabel22.setText("Event");

        jCheckBoxStopTriggerHighToLow.setText("High to Low");
        jCheckBoxStopTriggerHighToLow.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBoxStopTriggerHighToLowItemStateChanged(evt);
            }
        });
        jCheckBoxStopTriggerHighToLow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxStopTriggerHighToLowActionPerformed(evt);
            }
        });

        jCheckBoxStopTriggerLowToHigh.setText("Low to High");
        jCheckBoxStopTriggerLowToHigh.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBoxStopTriggerLowToHighItemStateChanged(evt);
            }
        });
        jCheckBoxStopTriggerLowToHigh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxStopTriggerLowToHighActionPerformed(evt);
            }
        });

        jLabel23.setText("Timeout (ms)");

        javax.swing.GroupLayout jPanelStopTriggerGPILayout = new javax.swing.GroupLayout(jPanelStopTriggerGPI);
        jPanelStopTriggerGPI.setLayout(jPanelStopTriggerGPILayout);
        jPanelStopTriggerGPILayout.setHorizontalGroup(
            jPanelStopTriggerGPILayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelStopTriggerGPILayout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(jPanelStopTriggerGPILayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel23))
                .addGap(34, 34, 34)
                .addGroup(jPanelStopTriggerGPILayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBoxStopTriggerLowToHigh)
                    .addGroup(jPanelStopTriggerGPILayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(jPanelStopTriggerGPILayout.createSequentialGroup()
                            .addComponent(jCheckBoxStopTriggerHighToLow)
                            .addGap(72, 72, 72))
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanelStopTriggerGPILayout.createSequentialGroup()
                            .addGroup(jPanelStopTriggerGPILayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(jTextFieldStopTriggerGPITimeout, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jComboBoxStopTriggerGPIPort, javax.swing.GroupLayout.Alignment.LEADING, 0, 108, Short.MAX_VALUE))
                            .addGap(45, 45, 45))))
                .addGap(81, 81, 81))
        );
        jPanelStopTriggerGPILayout.setVerticalGroup(
            jPanelStopTriggerGPILayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelStopTriggerGPILayout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addGroup(jPanelStopTriggerGPILayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE, false)
                    .addComponent(jComboBoxStopTriggerGPIPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel21))
                .addGap(18, 18, 18)
                .addGroup(jPanelStopTriggerGPILayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel23)
                    .addComponent(jTextFieldStopTriggerGPITimeout, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(32, 32, 32)
                .addGroup(jPanelStopTriggerGPILayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE, false)
                    .addComponent(jCheckBoxStopTriggerHighToLow)
                    .addComponent(jLabel22))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jCheckBoxStopTriggerLowToHigh)
                .addGap(64, 64, 64))
        );

        jLabel24.setText("Event");

        jCheckBoxStopTriggerGunReleased.setText("Trigger Released");
        jCheckBoxStopTriggerGunReleased.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBoxStopTriggerGunReleasedItemStateChanged(evt);
            }
        });
        jCheckBoxStopTriggerGunReleased.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxStopTriggerGunReleasedActionPerformed(evt);
            }
        });

        jCheckBoxStopTriggerGunPressed.setText("Trigger Pressed");
        jCheckBoxStopTriggerGunPressed.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBoxStopTriggerGunPressedItemStateChanged(evt);
            }
        });
        jCheckBoxStopTriggerGunPressed.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxStopTriggerGunPressedActionPerformed(evt);
            }
        });

        jLabel25.setText("Timeout (ms)");

        javax.swing.GroupLayout jPanelStopTriggerHandheldLayout = new javax.swing.GroupLayout(jPanelStopTriggerHandheld);
        jPanelStopTriggerHandheld.setLayout(jPanelStopTriggerHandheldLayout);
        jPanelStopTriggerHandheldLayout.setHorizontalGroup(
            jPanelStopTriggerHandheldLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelStopTriggerHandheldLayout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(jPanelStopTriggerHandheldLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel24, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel25))
                .addGap(34, 34, 34)
                .addGroup(jPanelStopTriggerHandheldLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jCheckBoxStopTriggerGunPressed)
                    .addComponent(jCheckBoxStopTriggerGunReleased, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTextFieldStopTriggerHandheldTimeout))
                .addGap(180, 180, 180))
        );
        jPanelStopTriggerHandheldLayout.setVerticalGroup(
            jPanelStopTriggerHandheldLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelStopTriggerHandheldLayout.createSequentialGroup()
                .addGap(74, 74, 74)
                .addGroup(jPanelStopTriggerHandheldLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel25)
                    .addComponent(jTextFieldStopTriggerHandheldTimeout, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(29, 29, 29)
                .addGroup(jPanelStopTriggerHandheldLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE, false)
                    .addComponent(jCheckBoxStopTriggerGunReleased)
                    .addComponent(jLabel24))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jCheckBoxStopTriggerGunPressed)
                .addGap(64, 64, 64))
        );

        javax.swing.GroupLayout jPanelStopTriggerLayout = new javax.swing.GroupLayout(jPanelStopTrigger);
        jPanelStopTrigger.setLayout(jPanelStopTriggerLayout);
        jPanelStopTriggerLayout.setHorizontalGroup(
            jPanelStopTriggerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelStopTriggerLayout.createSequentialGroup()
                .addGroup(jPanelStopTriggerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanelStopTriggerLayout.createSequentialGroup()
                        .addGap(34, 34, 34)
                        .addComponent(jLabel2)
                        .addGap(49, 49, 49)
                        .addComponent(jComboBoxStopTriggerType, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanelStopTriggerLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanelStopTriggerObservation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
            .addGroup(jPanelStopTriggerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanelStopTriggerLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jPanelStopTriggerNAttempts, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(104, Short.MAX_VALUE)))
            .addGroup(jPanelStopTriggerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelStopTriggerLayout.createSequentialGroup()
                    .addContainerGap(104, Short.MAX_VALUE)
                    .addComponent(jPanelStopTriggerDuration, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap()))
            .addGroup(jPanelStopTriggerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanelStopTriggerLayout.createSequentialGroup()
                    .addGap(14, 14, 14)
                    .addComponent(jPanelStopTriggerGPI, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(63, Short.MAX_VALUE)))
            .addGroup(jPanelStopTriggerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanelStopTriggerLayout.createSequentialGroup()
                    .addGap(14, 14, 14)
                    .addComponent(jPanelStopTriggerHandheld, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        jPanelStopTriggerLayout.setVerticalGroup(
            jPanelStopTriggerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelStopTriggerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelStopTriggerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jComboBoxStopTriggerType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanelStopTriggerObservation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addGroup(jPanelStopTriggerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelStopTriggerLayout.createSequentialGroup()
                    .addContainerGap(32, Short.MAX_VALUE)
                    .addComponent(jPanelStopTriggerNAttempts, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(21, 21, 21)))
            .addGroup(jPanelStopTriggerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelStopTriggerLayout.createSequentialGroup()
                    .addContainerGap(42, Short.MAX_VALUE)
                    .addComponent(jPanelStopTriggerDuration, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(11, 11, 11)))
            .addGroup(jPanelStopTriggerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanelStopTriggerLayout.createSequentialGroup()
                    .addGap(36, 36, 36)
                    .addComponent(jPanelStopTriggerGPI, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(31, Short.MAX_VALUE)))
            .addGroup(jPanelStopTriggerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanelStopTriggerLayout.createSequentialGroup()
                    .addGap(36, 36, 36)
                    .addComponent(jPanelStopTriggerHandheld, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(29, Short.MAX_VALUE)))
        );

        jTabbedPane1.addTab("Stop Trigger", jPanelStopTrigger);

        jLabel4.setText("New Tag");

        jComboBoxNewTag.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Never", "Immediate", "Moderated" }));
        jComboBoxNewTag.setSelectedIndex(2);
        jComboBoxNewTag.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxNewTagActionPerformed(evt);
            }
        });

        jTextFieldReportTriggerNewTag.setText("500");

        jLabel5.setText("(ms)");

        jLabel6.setText("Tag Invisible");

        jComboBoxNewTagInvisible.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Never", "Immediate", "Moderated" }));
        jComboBoxNewTagInvisible.setSelectedIndex(2);

        jTextFieldReportTriggerTagInvisible.setText("500");
        jTextFieldReportTriggerTagInvisible.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldReportTriggerTagInvisibleActionPerformed(evt);
            }
        });

        jTextFieldReportTriggerTagBackVisible.setText("500");

        jLabel7.setText("Tag Back to visibility");

        jComboBoxBackToVisible.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Never", "Immediate", "Moderated" }));
        jComboBoxBackToVisible.setSelectedIndex(2);

        jLabel8.setText("(ms)");

        jLabel9.setText("(ms)");

        jLabel27.setText("Tag Moving");

        jComboBoxTagMoving.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Disable", "Enable" }));

        jTextFieldReportTriggerTagMoving.setText("500");

        jLabel28.setText("(ms)");

        javax.swing.GroupLayout jPanelReportTriggerLayout = new javax.swing.GroupLayout(jPanelReportTrigger);
        jPanelReportTrigger.setLayout(jPanelReportTriggerLayout);
        jPanelReportTriggerLayout.setHorizontalGroup(
            jPanelReportTriggerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelReportTriggerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelReportTriggerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(jLabel6)
                    .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel27, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(11, 11, 11)
                .addGroup(jPanelReportTriggerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jComboBoxNewTag, 0, 1, Short.MAX_VALUE)
                    .addComponent(jComboBoxNewTagInvisible, 0, 1, Short.MAX_VALUE)
                    .addComponent(jComboBoxBackToVisible, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBoxTagMoving, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelReportTriggerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextFieldReportTriggerNewTag, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldReportTriggerTagInvisible, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldReportTriggerTagBackVisible, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldReportTriggerTagMoving, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelReportTriggerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel9, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel28, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        jPanelReportTriggerLayout.setVerticalGroup(
            jPanelReportTriggerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelReportTriggerLayout.createSequentialGroup()
                .addGap(46, 46, 46)
                .addGroup(jPanelReportTriggerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jComboBoxNewTag, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldReportTriggerNewTag, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addGap(31, 31, 31)
                .addGroup(jPanelReportTriggerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jComboBoxNewTagInvisible, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldReportTriggerTagInvisible, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addGap(31, 31, 31)
                .addGroup(jPanelReportTriggerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(jComboBoxBackToVisible, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldReportTriggerTagBackVisible, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9))
                .addGap(31, 31, 31)
                .addGroup(jPanelReportTriggerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel27)
                    .addComponent(jComboBoxTagMoving, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldReportTriggerTagMoving, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel28))
                .addContainerGap(42, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Report Trigger", jPanelReportTrigger);

        jLabel3.setText("Tag Report Trigger");

        jTextFieldReportTrigger.setText("1");
        jTextFieldReportTrigger.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldReportTriggerActionPerformed(evt);
            }
        });

        jButtonApply.setText("Apply");
        jButtonApply.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonApplyActionPerformed(evt);
            }
        });

        jLabel26.setText("Periodic Report Duration (secs)");

        jTextFieldPeriodicReportTrigger.setText("0");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(jLabel26))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jTextFieldPeriodicReportTrigger)
                    .addComponent(jTextFieldReportTrigger, javax.swing.GroupLayout.DEFAULT_SIZE, 70, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonApply)
                .addGap(18, 18, 18))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 315, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(12, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 299, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(jTextFieldReportTrigger, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel26)
                            .addComponent(jTextFieldPeriodicReportTrigger, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 13, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButtonApply)
                        .addGap(27, 27, 27))))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jTextFieldReportTriggerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldReportTriggerActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldReportTriggerActionPerformed

    private void jComboBoxNewTagActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxNewTagActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBoxNewTagActionPerformed

    private void jComboBoxStopTriggerTypeItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBoxStopTriggerTypeItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED)
        {
            updateStopTriggerPanel();
        }
    }//GEN-LAST:event_jComboBoxStopTriggerTypeItemStateChanged

    private void jComboBoxStartTriggerTypeItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBoxStartTriggerTypeItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED)
        {
            updateStartTriggerPanel();
        }
    }//GEN-LAST:event_jComboBoxStartTriggerTypeItemStateChanged

    private void updateStartTriggerPanel()
    {
            switch(jComboBoxStartTriggerType.getSelectedIndex())
            {
                // Immediate
                case 0:
                   jPanelStartTriggerPeriodic.setVisible(false);
                   jPanelStartTriggerGPI.setVisible(false);
                   jPanelStartTriggerHandheld.setVisible(false);
                   break;
                // Periodic
                case 1:
                    jPanelStartTriggerPeriodic.setVisible(true);
                    jPanelStartTriggerGPI.setVisible(false);
                    jPanelStartTriggerHandheld.setVisible(false);
                    break;
                // GPI
                case 2:
                   jPanelStartTriggerPeriodic.setVisible(false);
                   jPanelStartTriggerGPI.setVisible(true);
                   jPanelStartTriggerHandheld.setVisible(false);
                    break;
                // Handheld Trigger
                case 3:
                    jPanelStartTriggerHandheld.setVisible(true);
                    jPanelStartTriggerPeriodic.setVisible(false);
                    jPanelStartTriggerGPI.setVisible(false);
                    break;
            }

    }

    private void updateStopTriggerPanel()
    {
            switch(jComboBoxStopTriggerType.getSelectedIndex())
            {
                // Immediate
                case 0:
                   jPanelStopTriggerDuration.setVisible(false);
                   jPanelStopTriggerNAttempts.setVisible(false);
                   jPanelStopTriggerObservation.setVisible(false);
                   jPanelStopTriggerGPI.setVisible(false);
                   jPanelStopTriggerHandheld.setVisible(false);

                   break;
                // Duration
                case 1:
                   jPanelStopTriggerDuration.setVisible(true);
                   jPanelStopTriggerNAttempts.setVisible(false);
                   jPanelStopTriggerObservation.setVisible(false);
                   jPanelStopTriggerGPI.setVisible(false);
                   jPanelStopTriggerHandheld.setVisible(false);
                    break;
                // GPI with time out
                case 2:
                   jPanelStopTriggerDuration.setVisible(false);
                   jPanelStopTriggerNAttempts.setVisible(false);
                   jPanelStopTriggerObservation.setVisible(false);
                   jPanelStopTriggerGPI.setVisible(true);
                   jPanelStopTriggerHandheld.setVisible(false);
                    break;
                // Tag Observation
                case 3:
                   jPanelStopTriggerDuration.setVisible(false);
                   jPanelStopTriggerNAttempts.setVisible(false);
                   jPanelStopTriggerObservation.setVisible(true);
                   jPanelStopTriggerGPI.setVisible(false);
                   jPanelStopTriggerHandheld.setVisible(false);

                   break;
               // N Attempts
                case 4:
                   jPanelStopTriggerDuration.setVisible(false);
                   jPanelStopTriggerNAttempts.setVisible(true);
                   jPanelStopTriggerObservation.setVisible(false);
                   jPanelStopTriggerGPI.setVisible(false);
                   jPanelStopTriggerHandheld.setVisible(false);
                    break;
                // Handheld Trigger
                case 5:
                   jPanelStopTriggerDuration.setVisible(false);
                   jPanelStopTriggerNAttempts.setVisible(false);
                   jPanelStopTriggerObservation.setVisible(false);
                   jPanelStopTriggerGPI.setVisible(false);
                   jPanelStopTriggerHandheld.setVisible(true);

                    break;

            }

    }

    private void jButtonApplyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonApplyActionPerformed

        // Start Trigger settings
        triggerInfo.StartTrigger.setTriggerType(getStartTriggerType(jComboBoxStartTriggerType.getSelectedIndex()));

        // GPI start trigger
        if (jCheckBoxStartTriggerLowToHigh.isSelected())
            triggerInfo.StartTrigger.GPI.setGPIEvent(true);
        else
            triggerInfo.StartTrigger.GPI.setGPIEvent(false);
        triggerInfo.StartTrigger.GPI.setPortNumber(jComboBoxStartTriggerGPIPort.getSelectedIndex() + 1);

        // Handheld trigger
        if (jCheckBoxStartTriggerGunPressed.isSelected())
            triggerInfo.StartTrigger.Handheld.setHandheldTriggerEvent(HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_PRESSED);
        else
            triggerInfo.StartTrigger.Handheld.setHandheldTriggerEvent(HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_RELEASED);

        // Periodic
        triggerInfo.StartTrigger.Periodic.setPeriod(Integer.parseInt(jTextFieldStartTriggerPeriod.getText()));
        SYSTEMTIME sysTime=new SYSTEMTIME();

        currentDate=jFormattedTextFieldStartTriggerStartDate.getText();
        currentTime=jFormattedTextFieldStartTriggerStartTime.getText();

        StringTokenizer st1 = new StringTokenizer(currentDate,"-");

        sysTime.Month=Short.parseShort(st1.nextToken());
        sysTime.Day=Short.parseShort(st1.nextToken());
        sysTime.Year=Short.parseShort(st1.nextToken());

        st1 = new StringTokenizer(currentTime,":");

        sysTime.Hour=Short.parseShort(st1.nextToken());
        sysTime.Minute=Short.parseShort(st1.nextToken());
        sysTime.Second=Short.parseShort(st1.nextToken());
        sysTime.Milliseconds=0;
        triggerInfo.StartTrigger.Periodic.StartTime=sysTime;

        
        // Stop Trigger settings
        triggerInfo.StopTrigger.setTriggerType(getStopTriggerType(jComboBoxStopTriggerType.getSelectedIndex()));

        // Duration
        triggerInfo.StopTrigger.setDurationMilliSeconds(Integer.parseInt(jTextFieldStopTriggerDuration.getText()));

        // N-Attempts
        triggerInfo.StopTrigger.NumAttempts.setN(Short.parseShort(jTextFieldStopTriggerNAttempts.getText()));
        triggerInfo.StopTrigger.NumAttempts.setTimeout(Integer.parseInt(jTextFieldStopTriggerNAttemptTimeout.getText()));

        // Tag Observation
        triggerInfo.StopTrigger.TagObservation.setN(Short.parseShort(jTextFieldStopTriggerNObservation.getText()));
        triggerInfo.StopTrigger.TagObservation.setTimeout(Integer.parseInt(jTextFieldStopTriggerObservationTimeout.getText()));

        // GPI with Timeout
        if (jCheckBoxStopTriggerLowToHigh.isSelected())
            triggerInfo.StopTrigger.GPI.setGPIEvent(true);
        else
            triggerInfo.StopTrigger.GPI.setGPIEvent(false);
        triggerInfo.StopTrigger.GPI.setPortNumber(jComboBoxStopTriggerGPIPort.getSelectedIndex() + 1);
        triggerInfo.StopTrigger.GPI.setTimeout(Integer.parseInt(jTextFieldStopTriggerGPITimeout.getText()));

        // Handheld Trigger
        triggerInfo.StopTrigger.Handheld.setHandheldTriggerTimeout(Integer.parseInt(jTextFieldStopTriggerHandheldTimeout.getText()));

        if (jCheckBoxStopTriggerGunPressed.isSelected())
            triggerInfo.StopTrigger.Handheld.setHandheldTriggerEvent(HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_PRESSED);
        else
            triggerInfo.StopTrigger.Handheld.setHandheldTriggerEvent(HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_RELEASED);
        
        // Report Trigger settings
        triggerInfo.TagEventReportInfo.setReportNewTagEvent(getReportTriggerEvent(jComboBoxNewTag.getSelectedIndex()));
        triggerInfo.TagEventReportInfo.setNewTagEventModeratedTimeoutMilliseconds(Short.parseShort(jTextFieldReportTriggerNewTag.getText()));

        triggerInfo.TagEventReportInfo.setReportTagInvisibleEvent(getReportTriggerEvent(jComboBoxNewTagInvisible.getSelectedIndex()));
        triggerInfo.TagEventReportInfo.setTagInvisibleEventModeratedTimeoutMilliseconds(Short.parseShort(jTextFieldReportTriggerTagInvisible.getText()));

        triggerInfo.TagEventReportInfo.setReportTagBackToVisibilityEvent(getReportTriggerEvent(jComboBoxBackToVisible.getSelectedIndex()));
        triggerInfo.TagEventReportInfo.setTagBackToVisibilityModeratedTimeoutMilliseconds(Short.parseShort(jTextFieldReportTriggerTagBackVisible.getText()));

        triggerInfo.TagEventReportInfo.setReportTagMovingEvent(getReportMovingEvent(jComboBoxTagMoving.getSelectedIndex()));
        triggerInfo.TagEventReportInfo.setTagStationaryModeratedTimeoutMilliseconds(Short.parseShort(jTextFieldReportTriggerTagMoving.getText()));

        // set report trigger
        triggerInfo.setTagReportTrigger(Integer.parseInt(jTextFieldReportTrigger.getText()));
        // periodic report trigger
        triggerInfo.ReportTriggers.setPeriodicReportTrigger(Integer.parseInt(jTextFieldPeriodicReportTrigger.getText()));

    }//GEN-LAST:event_jButtonApplyActionPerformed

    START_TRIGGER_TYPE getStartTriggerType(int index)
    {
        START_TRIGGER_TYPE startTriggerType = START_TRIGGER_TYPE.START_TRIGGER_TYPE_IMMEDIATE;

            switch(jComboBoxStartTriggerType.getSelectedIndex())
            {
                // Immediate
                case 0:
                    startTriggerType = START_TRIGGER_TYPE.START_TRIGGER_TYPE_IMMEDIATE;
                   break;
                // Periodic
                case 1:
                    startTriggerType = START_TRIGGER_TYPE.START_TRIGGER_TYPE_PERIODIC;
                    break;
                // GPI
                case 2:
                    startTriggerType = START_TRIGGER_TYPE.START_TRIGGER_TYPE_GPI;
                    break;
                // Handheld Trigger
                case 3:
                    startTriggerType = START_TRIGGER_TYPE.START_TRIGGER_TYPE_HANDHELD;
                    break;
            }
            return startTriggerType;
    }

    STOP_TRIGGER_TYPE getStopTriggerType(int index)
    {
        STOP_TRIGGER_TYPE stopTriggerType = STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_IMMEDIATE;

        switch(jComboBoxStopTriggerType.getSelectedIndex())
        {
            // Immediate
            case 0:
               stopTriggerType = STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_IMMEDIATE;
               break;
            // Duration
            case 1:
                stopTriggerType = STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_DURATION;
                break;
            // GPI with time out
            case 2:
                stopTriggerType = STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_GPI_WITH_TIMEOUT;
                break;
            // Tag Observation
            case 3:
                stopTriggerType = STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_TAG_OBSERVATION_WITH_TIMEOUT;
               break;
           // N Attempts
            case 4:
                stopTriggerType = STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_N_ATTEMPTS_WITH_TIMEOUT;
                break;
            // Handheld Trigger
            case 5:
                stopTriggerType = STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_HANDHELD_WITH_TIMEOUT;
                break;

        }
        return stopTriggerType;
    }

    TAG_EVENT_REPORT_TRIGGER getReportTriggerEvent(int index)
    { 
        TAG_EVENT_REPORT_TRIGGER tagReportTrigger = TAG_EVENT_REPORT_TRIGGER.IMMEDIATE;
        switch(index)
        {
            // Never
            case 0:
                tagReportTrigger = TAG_EVENT_REPORT_TRIGGER.NEVER;
                break;
            case 1:
                tagReportTrigger = TAG_EVENT_REPORT_TRIGGER.IMMEDIATE;
                break;
            case 2:
                tagReportTrigger = TAG_EVENT_REPORT_TRIGGER.MODERATED;
                break;
        }
        return tagReportTrigger;
    }

    TAG_MOVING_EVENT_REPORT getReportMovingEvent(int index)
    {
        TAG_MOVING_EVENT_REPORT movingEvent = TAG_MOVING_EVENT_REPORT.DISABLE;
        
        switch(index)
        {
            case 0:
                movingEvent = TAG_MOVING_EVENT_REPORT.DISABLE;
                break;
            case 1:
                movingEvent = TAG_MOVING_EVENT_REPORT.ENABLE;
                break;
                
        }
        
        return movingEvent;
    }
    private void jCheckBoxStartTriggerHighToLowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxStartTriggerHighToLowActionPerformed
        // TODO add your handling code here:
        jCheckBoxStartTriggerLowToHigh.setSelected(false);
    }//GEN-LAST:event_jCheckBoxStartTriggerHighToLowActionPerformed

    private void jCheckBoxStartTriggerGunReleasedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxStartTriggerGunReleasedActionPerformed
        // TODO add your handling code here:
        jCheckBoxStartTriggerGunPressed.setSelected(false);

    }//GEN-LAST:event_jCheckBoxStartTriggerGunReleasedActionPerformed

    private void jCheckBoxStopTriggerHighToLowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxStopTriggerHighToLowActionPerformed
        // TODO add your handling code here:
        jCheckBoxStopTriggerLowToHigh.setSelected(false);
    }//GEN-LAST:event_jCheckBoxStopTriggerHighToLowActionPerformed

    private void jCheckBoxStopTriggerGunReleasedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxStopTriggerGunReleasedActionPerformed
        // TODO add your handling code here:
        jCheckBoxStopTriggerGunPressed.setSelected(false);
    }//GEN-LAST:event_jCheckBoxStopTriggerGunReleasedActionPerformed

    private void jTextFieldReportTriggerTagInvisibleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldReportTriggerTagInvisibleActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldReportTriggerTagInvisibleActionPerformed

    private void jCheckBoxStopTriggerGunReleasedItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBoxStopTriggerGunReleasedItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_jCheckBoxStopTriggerGunReleasedItemStateChanged

    private void jCheckBoxStopTriggerGunPressedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxStopTriggerGunPressedActionPerformed
        // TODO add your handling code here:
        jCheckBoxStopTriggerGunReleased.setSelected(false);
    }//GEN-LAST:event_jCheckBoxStopTriggerGunPressedActionPerformed

    private void jCheckBoxStopTriggerGunPressedItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBoxStopTriggerGunPressedItemStateChanged
        // TODO add your handling code here:

    }//GEN-LAST:event_jCheckBoxStopTriggerGunPressedItemStateChanged

    private void jCheckBoxStopTriggerHighToLowItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBoxStopTriggerHighToLowItemStateChanged
        // TODO add your handling code here:

    }//GEN-LAST:event_jCheckBoxStopTriggerHighToLowItemStateChanged

    private void jCheckBoxStopTriggerLowToHighItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBoxStopTriggerLowToHighItemStateChanged
        // TODO add your handling code here:

    }//GEN-LAST:event_jCheckBoxStopTriggerLowToHighItemStateChanged

    private void jCheckBoxStartTriggerHighToLowItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBoxStartTriggerHighToLowItemStateChanged
        // TODO add your handling code here:

    }//GEN-LAST:event_jCheckBoxStartTriggerHighToLowItemStateChanged

    private void jCheckBoxStartTriggerLowToHighItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBoxStartTriggerLowToHighItemStateChanged
        // TODO add your handling code here:

    }//GEN-LAST:event_jCheckBoxStartTriggerLowToHighItemStateChanged

    private void jCheckBoxStartTriggerGunReleasedItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBoxStartTriggerGunReleasedItemStateChanged
        // TODO add your handling code here:
        
    }//GEN-LAST:event_jCheckBoxStartTriggerGunReleasedItemStateChanged

    private void jCheckBoxStartTriggerGunPressedItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBoxStartTriggerGunPressedItemStateChanged
        // TODO add your handling code here:

    }//GEN-LAST:event_jCheckBoxStartTriggerGunPressedItemStateChanged

    private void jCheckBoxStopTriggerLowToHighActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxStopTriggerLowToHighActionPerformed
        // TODO add your handling code here:
        jCheckBoxStopTriggerHighToLow.setSelected(false);
    }//GEN-LAST:event_jCheckBoxStopTriggerLowToHighActionPerformed

    private void jCheckBoxStartTriggerLowToHighActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxStartTriggerLowToHighActionPerformed
        // TODO add your handling code here:
        jCheckBoxStartTriggerHighToLow.setSelected(false);
        
    }//GEN-LAST:event_jCheckBoxStartTriggerLowToHighActionPerformed

    private void jCheckBoxStartTriggerGunPressedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxStartTriggerGunPressedActionPerformed
        // TODO add your handling code here:
        jCheckBoxStartTriggerGunReleased.setSelected(false);
    }//GEN-LAST:event_jCheckBoxStartTriggerGunPressedActionPerformed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                TriggersDlg dialog = new TriggersDlg(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonApply;
    private javax.swing.JCheckBox jCheckBoxStartTriggerGunPressed;
    private javax.swing.JCheckBox jCheckBoxStartTriggerGunReleased;
    private javax.swing.JCheckBox jCheckBoxStartTriggerHighToLow;
    private javax.swing.JCheckBox jCheckBoxStartTriggerLowToHigh;
    private javax.swing.JCheckBox jCheckBoxStopTriggerGunPressed;
    private javax.swing.JCheckBox jCheckBoxStopTriggerGunReleased;
    private javax.swing.JCheckBox jCheckBoxStopTriggerHighToLow;
    private javax.swing.JCheckBox jCheckBoxStopTriggerLowToHigh;
    private javax.swing.JComboBox jComboBoxBackToVisible;
    private javax.swing.JComboBox jComboBoxNewTag;
    private javax.swing.JComboBox jComboBoxNewTagInvisible;
    private javax.swing.JComboBox jComboBoxStartTriggerGPIPort;
    private javax.swing.JComboBox jComboBoxStartTriggerType;
    private javax.swing.JComboBox jComboBoxStopTriggerGPIPort;
    private javax.swing.JComboBox jComboBoxStopTriggerType;
    private javax.swing.JComboBox jComboBoxTagMoving;
    private javax.swing.JFormattedTextField jFormattedTextFieldStartTriggerStartDate;
    private javax.swing.JFormattedTextField jFormattedTextFieldStartTriggerStartTime;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanelReportTrigger;
    private javax.swing.JPanel jPanelStartTrigger;
    private javax.swing.JPanel jPanelStartTriggerGPI;
    private javax.swing.JPanel jPanelStartTriggerHandheld;
    private javax.swing.JPanel jPanelStartTriggerPeriodic;
    private javax.swing.JPanel jPanelStopTrigger;
    private javax.swing.JPanel jPanelStopTriggerDuration;
    private javax.swing.JPanel jPanelStopTriggerGPI;
    private javax.swing.JPanel jPanelStopTriggerHandheld;
    private javax.swing.JPanel jPanelStopTriggerNAttempts;
    private javax.swing.JPanel jPanelStopTriggerObservation;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField jTextFieldPeriodicReportTrigger;
    private javax.swing.JTextField jTextFieldReportTrigger;
    private javax.swing.JTextField jTextFieldReportTriggerNewTag;
    private javax.swing.JTextField jTextFieldReportTriggerTagBackVisible;
    private javax.swing.JTextField jTextFieldReportTriggerTagInvisible;
    private javax.swing.JTextField jTextFieldReportTriggerTagMoving;
    private javax.swing.JTextField jTextFieldStartTriggerPeriod;
    private javax.swing.JTextField jTextFieldStopTriggerDuration;
    private javax.swing.JTextField jTextFieldStopTriggerGPITimeout;
    private javax.swing.JTextField jTextFieldStopTriggerHandheldTimeout;
    private javax.swing.JTextField jTextFieldStopTriggerNAttemptTimeout;
    private javax.swing.JTextField jTextFieldStopTriggerNAttempts;
    private javax.swing.JTextField jTextFieldStopTriggerNObservation;
    private javax.swing.JTextField jTextFieldStopTriggerObservationTimeout;
    // End of variables declaration//GEN-END:variables

}
