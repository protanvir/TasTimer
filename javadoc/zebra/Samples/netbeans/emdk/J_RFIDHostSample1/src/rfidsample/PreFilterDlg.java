/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * PreFilterDlg.java
 *
 * Created on May 4, 2011, 4:19:34 AM
 */

package rfidsample;

import com.mot.rfid.api3.FILTER_ACTION;
import com.mot.rfid.api3.InvalidUsageException;
import com.mot.rfid.api3.MEMORY_BANK;
import com.mot.rfid.api3.OperationFailureException;
import com.mot.rfid.api3.PreFilters;
import com.mot.rfid.api3.STATE_AWARE_ACTION;
import com.mot.rfid.api3.STATE_UNAWARE_ACTION;
import com.mot.rfid.api3.TARGET;
import java.awt.event.ItemEvent;

/**
 *
 * @author CFRN78
 */
public class PreFilterDlg extends javax.swing.JDialog {
        PreFilters.PreFilter preFilter1;
        PreFilters.PreFilter preFilter2;
        String[] stateAwareAction = new String[] {
                                    "Inv A Not Inv B",
                                    "Asrt SL Not Dsrt SL",
                                    "Inv A",
                                    "Asrt SL",
                                    "Not Inv B",
                                    "Not Dsrt SL",
                                    "Inv A2BB2A Not Inv B",
                                    "Neg SL Not Asrt SL",
                                    "Inv B Not Inv A",
                                    "Dsrt SL Not Asrt SL",
                                    "Inv B",
                                    "Dsrt SL",
                                    "Not Inv A",
                                    "Not Asrt SL",
                                    "Not Inv A2BB2A",
                                    "Not Neg SL"

                                    };

        String[] stateUnawareAction = new String[] {
                                    "Select Not Unselect",
                                    "Select",
                                    "Not Unselect",
                                    "Unselect",
                                    "Unselect Not Select",
                                    "Not Select"

                                    };
        
    /** Creates new form PreFilterDlg */
    public PreFilterDlg(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        preFilter1 = RFIDMainDlg.rfidBase.preFilter1;
        preFilter2= RFIDMainDlg.rfidBase.preFilter2;

        short[] antennaList = RFIDMainDlg.rfidBase.getMyReader().Config.Antennas.getAvailableAntennas();

        // Add antennas
        for (int index = 0; index < antennaList.length; index++)
            jComboBoxAntennaID.addItem(String.valueOf(antennaList[index]));

        for (int index = 0; index < antennaList.length; index++)
            jComboBoxAntennaID1.addItem(String.valueOf(antennaList[index]));

       // set the antenna selection
        int antennaIDsel1 = preFilter1.getAntennaID();
        if (antennaIDsel1 > 0) antennaIDsel1--;
        jComboBoxAntennaID.setSelectedIndex(antennaIDsel1);

        int antennaIDsel2 = preFilter2.getAntennaID();
        if (antennaIDsel2 > 0) antennaIDsel2--;
        jComboBoxAntennaID1.setSelectedIndex(antennaIDsel2);

        // Memory Bank
        int memoryBank1 = RFIDMainDlg.rfidBase.preFilter1.getMemoryBank().getValue();
        if (memoryBank1 > 0) memoryBank1 -= 1;
        jComboBoxMemBank.setSelectedIndex(memoryBank1);    
            
        int memoryBank2 = RFIDMainDlg.rfidBase.preFilter2.getMemoryBank().getValue();
        if (memoryBank2 > 0)memoryBank2 -= 1;
        jComboBoxMemBank1.setSelectedIndex(memoryBank2);

        // Set the Offset
        jTextFieldOffset.setText(String.valueOf(preFilter1.getBitOffset()));
        jTextFieldOffset1.setText(String.valueOf(preFilter2.getBitOffset()));


        // Set the Tag Pattern
        if (RFIDMainDlg.rfidBase.preFilterTagPattern1 == null)
            jTextFieldTagPattern.setText("");
        else
            jTextFieldTagPattern.setText(RFIDMainDlg.rfidBase.preFilterTagPattern1);

        if (RFIDMainDlg.rfidBase.preFilterTagPattern2 == null)
            jTextFieldTagPattern1.setText("");
        else
            jTextFieldTagPattern1.setText(RFIDMainDlg.rfidBase.preFilterTagPattern2);

        // Use filter check box
        jCheckBoxUseFilter.setSelected(RFIDMainDlg.rfidBase.isPreFilterSet1);
        jCheckBoxUseFilter1.setSelected(RFIDMainDlg.rfidBase.isPreFilterSet2);
        
        jcheckBoxUseFilterAction();
        jcheckBoxUseFilterAction1();

        // Filter Action
        jComboBoxFilterAction.setSelectedIndex(preFilter1.getFilterAction().getValue());
        updateFilterAction();

        jComboBoxFilterAction1.setSelectedIndex(preFilter2.getFilterAction().getValue());
        updateFilterAction1();

    }

    void loadActionCombo(boolean isStateAware)
    {
        if (isStateAware)
            jComboBoxAction.setModel(new javax.swing.DefaultComboBoxModel(stateAwareAction));
        else
            jComboBoxAction.setModel(new javax.swing.DefaultComboBoxModel(stateUnawareAction));
    }

    void loadActionCombo1(boolean isStateAware)
    {
        if (isStateAware)
            jComboBoxAction1.setModel(new javax.swing.DefaultComboBoxModel(stateAwareAction));
        else
            jComboBoxAction1.setModel(new javax.swing.DefaultComboBoxModel(stateUnawareAction));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jTextFieldOffset = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jCheckBoxUseFilter = new javax.swing.JCheckBox();
        jLabel3 = new javax.swing.JLabel();
        jTextFieldTagPattern = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jComboBoxMemBank = new javax.swing.JComboBox();
        jComboBoxAntennaID = new javax.swing.JComboBox();
        jLabel7 = new javax.swing.JLabel();
        jComboBoxAction = new javax.swing.JComboBox();
        jLabel6 = new javax.swing.JLabel();
        jComboBoxFilterAction = new javax.swing.JComboBox();
        jComboBoxTarget = new javax.swing.JComboBox();
        jPanel2 = new javax.swing.JPanel();
        jTextFieldOffset1 = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jCheckBoxUseFilter1 = new javax.swing.JCheckBox();
        jLabel9 = new javax.swing.JLabel();
        jTextFieldTagPattern1 = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jComboBoxMemBank1 = new javax.swing.JComboBox();
        jComboBoxAntennaID1 = new javax.swing.JComboBox();
        jLabel13 = new javax.swing.JLabel();
        jComboBoxAction1 = new javax.swing.JComboBox();
        jLabel14 = new javax.swing.JLabel();
        jComboBoxFilterAction1 = new javax.swing.JComboBox();
        jComboBoxTarget1 = new javax.swing.JComboBox();
        jButtonApply = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Pre Filter");
        setResizable(false);

        jTabbedPane1.setTabPlacement(javax.swing.JTabbedPane.BOTTOM);

        jLabel4.setText("Tag Pattern (Hex)");

        jCheckBoxUseFilter.setText("Use Filter");
        jCheckBoxUseFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxUseFilterActionPerformed(evt);
            }
        });

        jLabel3.setText("Offset (bit)");

        jLabel5.setText("Filter Action");

        jLabel1.setText("Antenna ID");

        jLabel2.setText("Memory Bank");

        jComboBoxMemBank.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "EPC", "TID", "USER" }));

        jLabel7.setText("Target");

        jComboBoxAction.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBoxActionItemStateChanged(evt);
            }
        });

        jLabel6.setText("Action");

        jComboBoxFilterAction.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Default", "State Aware", "State Unaware" }));
        jComboBoxFilterAction.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBoxFilterActionItemStateChanged(evt);
            }
        });

        jComboBoxTarget.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "SL", "S0", "S1", "S2", "S3" }));
        jComboBoxTarget.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxTargetActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(43, 43, 43)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2))
                        .addGap(29, 29, 29)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jComboBoxAntennaID, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jComboBoxMemBank, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jCheckBoxUseFilter))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(12, 12, 12)
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextFieldOffset, javax.swing.GroupLayout.DEFAULT_SIZE, 60, Short.MAX_VALUE))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addGap(63, 63, 63)
                        .addComponent(jComboBoxAction, 0, 135, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBoxTarget, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addGap(36, 36, 36)
                        .addComponent(jComboBoxFilterAction, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addGap(6, 6, 6)
                        .addComponent(jTextFieldTagPattern, javax.swing.GroupLayout.DEFAULT_SIZE, 240, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addComponent(jLabel1))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(2, 2, 2)
                        .addComponent(jComboBoxAntennaID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jCheckBoxUseFilter))
                .addGap(28, 28, 28)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(jLabel2))
                    .addComponent(jComboBoxMemBank, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jTextFieldOffset, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel3)))
                .addGap(28, 28, 28)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(jTextFieldTagPattern, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(32, 32, 32)
                        .addComponent(jLabel5))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(30, 30, 30)
                        .addComponent(jComboBoxFilterAction, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jComboBoxAction, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel6))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jComboBoxTarget, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel7)))
                .addContainerGap(26, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Pre Filter 1", jPanel1);

        jLabel8.setText("Tag Pattern (Hex)");

        jCheckBoxUseFilter1.setText("Use Filter");
        jCheckBoxUseFilter1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxUseFilter1ActionPerformed(evt);
            }
        });

        jLabel9.setText("Offset (bit)");

        jLabel10.setText("Filter Action");

        jLabel11.setText("Antenna ID");

        jLabel12.setText("Memory Bank");

        jComboBoxMemBank1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "EPC", "TID", "USER" }));

        jLabel13.setText("Target");

        jLabel14.setText("Action");

        jComboBoxFilterAction1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Default", "State Aware", "State Unaware" }));
        jComboBoxFilterAction1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBoxFilterAction1ItemStateChanged(evt);
            }
        });

        jComboBoxTarget1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "SL", "S0", "S1", "S2", "S3" }));
        jComboBoxTarget1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxTarget1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(43, 43, 43)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel11)
                            .addComponent(jLabel12))
                        .addGap(29, 29, 29)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jComboBoxAntennaID1, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jComboBoxMemBank1, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jCheckBoxUseFilter1))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(12, 12, 12)
                                .addComponent(jLabel9)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextFieldOffset1, javax.swing.GroupLayout.DEFAULT_SIZE, 60, Short.MAX_VALUE))))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel14)
                        .addGap(63, 63, 63)
                        .addComponent(jComboBoxAction1, 0, 135, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel13)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBoxTarget1, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel10)
                        .addGap(36, 36, 36)
                        .addComponent(jComboBoxFilterAction1, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addGap(6, 6, 6)
                        .addComponent(jTextFieldTagPattern1, javax.swing.GroupLayout.DEFAULT_SIZE, 240, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addComponent(jLabel11))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(2, 2, 2)
                        .addComponent(jComboBoxAntennaID1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jCheckBoxUseFilter1))
                .addGap(28, 28, 28)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(jLabel12))
                    .addComponent(jComboBoxMemBank1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jTextFieldOffset1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel9)))
                .addGap(28, 28, 28)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel8)
                    .addComponent(jTextFieldTagPattern1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(32, 32, 32)
                        .addComponent(jLabel10))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(30, 30, 30)
                        .addComponent(jComboBoxFilterAction1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jComboBoxAction1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel14))
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jComboBoxTarget1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel13)))
                .addContainerGap(26, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Pre Filter 2", jPanel2);

        jButtonApply.setText("Apply");
        jButtonApply.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonApplyActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButtonApply)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 391, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 268, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButtonApply)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBoxTargetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxTargetActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBoxTargetActionPerformed

    private void jComboBoxTarget1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxTarget1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBoxTarget1ActionPerformed

    private void jButtonApplyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonApplyActionPerformed

        // Create New PreFilter
        preFilter1 = RFIDMainDlg.rfidBase.preFilters.new PreFilter();
        preFilter2 = RFIDMainDlg.rfidBase.preFilters.new PreFilter();
        RFIDMainDlg.rfidBase.preFilter1 = preFilter1;
        RFIDMainDlg.rfidBase.preFilter2 = preFilter2;
 
        // Pre-Filter1
        try {
            if (jCheckBoxUseFilter.isSelected())
            {
                short antennaID = (short)jComboBoxAntennaID.getSelectedIndex();
                antennaID++;
                byte[] tagPattern = null;
                preFilter1.setAntennaID(antennaID);
                preFilter1.setMemoryBank(RFIDMainDlg.rfidBase.getMemoryBankEnum(jComboBoxMemBank.getSelectedIndex() + 1));
                tagPattern = RFIDBase.hexStringToByteArray(jTextFieldTagPattern.getText());
                preFilter1.setTagPattern(tagPattern);
                preFilter1.setBitOffset(Integer.parseInt(jTextFieldOffset.getText()));
                preFilter1.setTagPatternBitCount(tagPattern.length * 8);
                RFIDMainDlg.rfidBase.preFilterTagPattern1 = jTextFieldTagPattern.getText();
                RFIDMainDlg.rfidBase.preFilterActionIndex1 = jComboBoxAction.getSelectedIndex();
            }
        } catch (Exception ex) {
            RFIDMainDlg.rfidBase.postStatusNotification(RFIDBase.PARAM_ERROR, ex.getMessage());
        }

        // Pre-Filter2
        try {        
            if (jCheckBoxUseFilter1.isSelected())
            {
                short antennaID1 = (short)jComboBoxAntennaID1.getSelectedIndex();
                antennaID1++;
                byte[] tagPattern = null;
                preFilter2.setAntennaID(antennaID1);
                preFilter2.setMemoryBank(RFIDMainDlg.rfidBase.getMemoryBankEnum(jComboBoxMemBank1.getSelectedIndex() + 1));
                tagPattern = RFIDBase.hexStringToByteArray(jTextFieldTagPattern1.getText());
                preFilter2.setTagPattern(tagPattern);
                preFilter2.setBitOffset(Integer.parseInt(jTextFieldOffset1.getText()));
                preFilter2.setTagPatternBitCount(tagPattern.length * 8);
                RFIDMainDlg.rfidBase.preFilterTagPattern2 = jTextFieldTagPattern1.getText();
                RFIDMainDlg.rfidBase.preFilterActionIndex2 = jComboBoxAction1.getSelectedIndex();
            }
        } catch (Exception ex) {
            RFIDMainDlg.rfidBase.postStatusNotification(RFIDBase.PARAM_ERROR, ex.getMessage());
        }        

        // Set the filter Action
        preFilter1.setFilterAction(getFilterAction(jComboBoxFilterAction.getSelectedIndex()));
        preFilter2.setFilterAction(getFilterAction(jComboBoxFilterAction1.getSelectedIndex()));

        // Set the state Aware Params
        setStateAwareParams(jComboBoxFilterAction.getSelectedIndex());
        setStateAwareParams(jComboBoxFilterAction1.getSelectedIndex());

        RFIDMainDlg.rfidBase.isPreFilterSet1 = jCheckBoxUseFilter.isSelected();
        RFIDMainDlg.rfidBase.isPreFilterSet2 = jCheckBoxUseFilter1.isSelected();

        try {
            // check pre-filter is set
            RFIDMainDlg.rfidBase.getMyReader().Actions.PreFilters.deleteAll();

            // Pre Filter 1
            if (jCheckBoxUseFilter.isSelected())
            {
                RFIDMainDlg.rfidBase.getMyReader().Actions.PreFilters.add(preFilter1);
                RFIDMainDlg.rfidBase.postStatusNotification("Set " + RFIDMainDlg.rfidBase.getMyReader().Actions.PreFilters.length(),
                         " Prefilter Successfully");
            }

            // Pre Filter 2
            if (jCheckBoxUseFilter1.isSelected())
            {
                RFIDMainDlg.rfidBase.getMyReader().Actions.PreFilters.add(preFilter2);
                RFIDMainDlg.rfidBase.postStatusNotification("Set " + RFIDMainDlg.rfidBase.getMyReader().Actions.PreFilters.length(),
                         " Prefilter Successfully");
            }
       } catch (InvalidUsageException ex) {
            RFIDMainDlg.rfidBase.postStatusNotification(RFIDBase.PARAM_ERROR, ex.getVendorMessage());
        } catch (OperationFailureException ex) {
            RFIDMainDlg.rfidBase.postStatusNotification(ex.getStatusDescription(), ex.getVendorMessage());
        }
    }//GEN-LAST:event_jButtonApplyActionPerformed

    void setStateAwareParams(int filterAction)
    {
        switch (filterAction)
        {
            // Default
            case 0:
                break;
            // State Aware
            case 1:
                {
                     preFilter1.StateAwareAction.setStateAwareAction(getStateAwareAction(jComboBoxAction.getSelectedIndex()));
                     preFilter2.StateAwareAction.setStateAwareAction(getStateAwareAction(jComboBoxAction1.getSelectedIndex()));
                     preFilter1.StateAwareAction.setTarget(getStateAwareTarget(jComboBoxTarget.getSelectedIndex()));
                     preFilter2.StateAwareAction.setTarget(getStateAwareTarget(jComboBoxTarget1.getSelectedIndex()));

                }
                break;
            // State UnAware
            case 2:
                {
                     preFilter1.StateUnawareAction.setStateUnawareAction(getStateUnawareAction(jComboBoxAction.getSelectedIndex()));
                     preFilter2.StateUnawareAction.setStateUnawareAction(getStateUnawareAction(jComboBoxAction1.getSelectedIndex()));

                }
                break;
        }
    }
    
    STATE_AWARE_ACTION getStateAwareAction(int actionIndex)
    {
        STATE_AWARE_ACTION stateAwareAction = STATE_AWARE_ACTION.STATE_AWARE_ACTION_ASRT_SL_NOT_DSRT_SL;
        switch (actionIndex)
        {
            case 0:
                stateAwareAction = STATE_AWARE_ACTION.STATE_AWARE_ACTION_INV_A_NOT_INV_B;
                break;
            case 1:
                stateAwareAction = STATE_AWARE_ACTION.STATE_AWARE_ACTION_ASRT_SL_NOT_DSRT_SL;
                break;
            case 2:
                stateAwareAction = STATE_AWARE_ACTION.STATE_AWARE_ACTION_INV_A;
                break;
            case 3:
                stateAwareAction = STATE_AWARE_ACTION.STATE_AWARE_ACTION_ASRT_SL;
                break;
            case 4:
                stateAwareAction = STATE_AWARE_ACTION.STATE_AWARE_ACTION_NOT_INV_B;           
                break;
            case 5:
                stateAwareAction = STATE_AWARE_ACTION.STATE_AWARE_ACTION_NOT_DSRT_SL;            
                break;
            case 6:
                stateAwareAction = STATE_AWARE_ACTION.STATE_AWARE_ACTION_INV_A2BB2A;            
                break;
            case 7:
                stateAwareAction = STATE_AWARE_ACTION.STATE_AWARE_ACTION_NEG_SL;          
                break;
            case 8:
                stateAwareAction = STATE_AWARE_ACTION.STATE_AWARE_ACTION_INV_B_NOT_INV_A;           
                break;
            case 9:
                stateAwareAction = STATE_AWARE_ACTION.STATE_AWARE_ACTION_DSRT_SL_NOT_ASRT_SL;           
                break;
            case 10:
                stateAwareAction = STATE_AWARE_ACTION.STATE_AWARE_ACTION_INV_B;
                break;
            case 11:
                stateAwareAction = STATE_AWARE_ACTION.STATE_AWARE_ACTION_DSRT_SL;           
                break;
            case 12:
                stateAwareAction = STATE_AWARE_ACTION.STATE_AWARE_ACTION_NOT_INV_A;           
                break;
            case 13:
                stateAwareAction = STATE_AWARE_ACTION.STATE_AWARE_ACTION_NOT_ASRT_SL;           
                break;
            case 14:
                stateAwareAction = STATE_AWARE_ACTION.STATE_AWARE_ACTION_NOT_INV_A2BB2A;           
                break;
            case 15:
                stateAwareAction = STATE_AWARE_ACTION.STATE_AWARE_ACTION_NOT_NEG_SL;           
                break;
        }
        return stateAwareAction;
    }

    STATE_UNAWARE_ACTION getStateUnawareAction(int index)
    {
        STATE_UNAWARE_ACTION action = STATE_UNAWARE_ACTION.STATE_UNAWARE_ACTION_NOT_SELECT;
        switch (index)
        {
        case 0:
            action = STATE_UNAWARE_ACTION.STATE_UNAWARE_ACTION_SELECT_NOT_UNSELECT;
            break;
        case 1:
            action = STATE_UNAWARE_ACTION.STATE_UNAWARE_ACTION_SELECT;
            break;
        case 2:
            action = STATE_UNAWARE_ACTION.STATE_UNAWARE_ACTION_NOT_UNSELECT;
            break;
        case 3:
            action = STATE_UNAWARE_ACTION.STATE_UNAWARE_ACTION_UNSELECT;
            break;
        case 4:
            action = STATE_UNAWARE_ACTION.STATE_UNAWARE_ACTION_UNSELECT_NOT_SELECT;
            break;
        case 5:
            action = STATE_UNAWARE_ACTION.STATE_UNAWARE_ACTION_NOT_SELECT;
            break;
        }
        return action;
    }

    TARGET getStateAwareTarget(int index)
    {
        TARGET target = TARGET.TARGET_SL;
        switch (index)
        {
            case 0:
                target = TARGET.TARGET_SL;
                break;
            case 1:
                target = TARGET.TARGET_INVENTORIED_STATE_S0;
                break;
            case 2:
                target = TARGET.TARGET_INVENTORIED_STATE_S1;;
                break;
            case 3:
                target = TARGET.TARGET_INVENTORIED_STATE_S2;
                break;
            case 4:
                target = TARGET.TARGET_INVENTORIED_STATE_S3;
                break;

        }
        return target;
    }

    FILTER_ACTION getFilterAction(int index)
    {
        FILTER_ACTION filterAction = FILTER_ACTION.FILTER_ACTION_DEFAULT;
        switch (index) {
            case 0:
                filterAction = FILTER_ACTION.FILTER_ACTION_DEFAULT;
                break;
            case 1:
                filterAction = FILTER_ACTION.FILTER_ACTION_STATE_AWARE;
                break;
            case 2:
                filterAction = FILTER_ACTION.FILTER_ACTION_STATE_UNAWARE;
                break;
        }
        return filterAction;
    }
    private void jComboBoxActionItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBoxActionItemStateChanged
  
    }//GEN-LAST:event_jComboBoxActionItemStateChanged

    private void jComboBoxFilterActionItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBoxFilterActionItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED)
        {
            updateFilterAction();
        }
    }//GEN-LAST:event_jComboBoxFilterActionItemStateChanged

    private void jComboBoxFilterAction1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBoxFilterAction1ItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED)
        {
            updateFilterAction1();
        }
    }//GEN-LAST:event_jComboBoxFilterAction1ItemStateChanged

    private void jcheckBoxUseFilterAction ()
    {
        if (true == jCheckBoxUseFilter.isSelected()) {
            jComboBoxAntennaID.enable();
            jComboBoxMemBank.enable();
            jTextFieldOffset.enable();
            jTextFieldTagPattern.enable();
            jComboBoxFilterAction.enable();    
        } else {
            jComboBoxAntennaID.disable();
            jComboBoxMemBank.disable();
            jTextFieldOffset.disable();
            jTextFieldTagPattern.disable();
            jComboBoxFilterAction.disable();             
        }       
        jPanel1.repaint();
    }
    
    private void jCheckBoxUseFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxUseFilterActionPerformed
        // TODO add your handling code here:
        jcheckBoxUseFilterAction();
    }//GEN-LAST:event_jCheckBoxUseFilterActionPerformed

    private void jcheckBoxUseFilterAction1 ()
    {
        if (true == jCheckBoxUseFilter1.isSelected()) {
            jComboBoxAntennaID1.enable();
            jComboBoxMemBank1.enable();
            jTextFieldOffset1.enable();
            jTextFieldTagPattern1.enable();
            jComboBoxFilterAction1.enable();    
        } else {
            jComboBoxAntennaID1.disable();
            jComboBoxMemBank1.disable();
            jTextFieldOffset1.disable();
            jTextFieldTagPattern1.disable();
            jComboBoxFilterAction1.disable();             
        }        
        jPanel2.repaint();
    }
    
    private void jCheckBoxUseFilter1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxUseFilter1ActionPerformed
        // TODO add your handling code here:
        jcheckBoxUseFilterAction1();
    }//GEN-LAST:event_jCheckBoxUseFilter1ActionPerformed

    void updateFilterAction()
    {
            // Default Filter Action
            if (jComboBoxFilterAction.getSelectedIndex() == 0)
            {
                jComboBoxAction.setEnabled(false);
                jComboBoxTarget.setEnabled(false);
            }


            // State Aware Filter Action
            if (jComboBoxFilterAction.getSelectedIndex() == 1)
            {
                loadActionCombo(true);
                jComboBoxAction.setEnabled(true);
                jComboBoxTarget.setEnabled(true);
                jComboBoxAction.setSelectedIndex(RFIDMainDlg.rfidBase.preFilterActionIndex1);
                jComboBoxTarget.setSelectedIndex(RFIDMainDlg.rfidBase.preFilter1.StateAwareAction.getTarget().getValue());

            }
            // State UnAware Filter Action
            if (jComboBoxFilterAction.getSelectedIndex() == 2)
            {
                loadActionCombo(false);
                jComboBoxAction.setEnabled(true);
                jComboBoxTarget.setEnabled(false);


                // Set selected index
                jComboBoxAction.setSelectedIndex(RFIDMainDlg.rfidBase.preFilter1.StateUnawareAction.getStateUnawareAction().getValue());

            }
    }

    void updateFilterAction1()
    {
            // Default Filter Action
            if (jComboBoxFilterAction1.getSelectedIndex() == 0)
            {
                jComboBoxAction1.setEnabled(false);
                jComboBoxTarget1.setEnabled(false);
            }

            // State Aware Filter Action
            if(jComboBoxFilterAction1.getSelectedIndex() == 1)
            {
                loadActionCombo1(true);
                jComboBoxAction1.setEnabled(true);
                jComboBoxTarget1.setEnabled(true);
                // Set selected index  - Action
                jComboBoxAction1.setSelectedIndex(RFIDMainDlg.rfidBase.preFilterActionIndex2);

                // Set selected index  - Target
                jComboBoxTarget1.setSelectedIndex(RFIDMainDlg.rfidBase.preFilter2.StateAwareAction.getTarget().getValue());
            }

            // State UnAware Filter Action
            if(jComboBoxFilterAction1.getSelectedIndex() == 2)
            {
                loadActionCombo1(false);
                jComboBoxAction1.setEnabled(true);
                jComboBoxTarget1.setEnabled(false);

                // Set selected index
                jComboBoxAction1.setSelectedIndex(RFIDMainDlg.rfidBase.preFilter2.StateUnawareAction.getStateUnawareAction().getValue());

            }
    }

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                PreFilterDlg dialog = new PreFilterDlg(new javax.swing.JFrame(), true);
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
    private javax.swing.JCheckBox jCheckBoxUseFilter;
    private javax.swing.JCheckBox jCheckBoxUseFilter1;
    private javax.swing.JComboBox jComboBoxAction;
    private javax.swing.JComboBox jComboBoxAction1;
    private javax.swing.JComboBox jComboBoxAntennaID;
    private javax.swing.JComboBox jComboBoxAntennaID1;
    private javax.swing.JComboBox jComboBoxFilterAction;
    private javax.swing.JComboBox jComboBoxFilterAction1;
    private javax.swing.JComboBox jComboBoxMemBank;
    private javax.swing.JComboBox jComboBoxMemBank1;
    private javax.swing.JComboBox jComboBoxTarget;
    private javax.swing.JComboBox jComboBoxTarget1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField jTextFieldOffset;
    private javax.swing.JTextField jTextFieldOffset1;
    private javax.swing.JTextField jTextFieldTagPattern;
    private javax.swing.JTextField jTextFieldTagPattern1;
    // End of variables declaration//GEN-END:variables

}
