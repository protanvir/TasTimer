/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * RFIDSample.java
 *
 * Created on Mar 8, 2011, 5:12:20 PM
 */
package rfidsample;

import com.mot.rfid.api3.GPI_PORT_STATE;
import com.mot.rfid.api3.InvalidUsageException;
import com.mot.rfid.api3.OperationFailureException;
import com.mot.rfid.api3.RADIO_POWER_STATE;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JMenuItem;

/**
 *
 * @author CFRN78
 */
public class RFIDMainDlg extends javax.swing.JFrame{
    static RFIDBase rfidBase = null;

    /** Creates new form RFIDSample */
    public RFIDMainDlg() {
        initComponents();
        rfidBase = new RFIDBase(this);

        Component[] myComponent = getComponents();

        // initialize menu item state
        // Generic Menu interface
        updateGenericMenuItems(false);

        // RM Menu interface
        updateRMMenuItems(false);

        // disable the GPI port images
        for (int index = 1; index <= 10; index++) {
            setGPIOnScreen(index, false, false);
        }

        // Create Popup menu (context menu on right click)
        createPopupMenu();

    }
    private void createPopupMenu()
    {
        // Create Popup menu
        JMenuItem menuItemPoupRead = new JMenuItem("Read");
        jPopupMenuAccess.add(menuItemPoupRead);
        menuItemPoupRead.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemReadAccessActionPerformed(evt);
            }
        });

        JMenuItem menuItemPoupWrite = new JMenuItem("Write");
        jPopupMenuAccess.add(menuItemPoupWrite);
        menuItemPoupWrite.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemWriteAccessActionPerformed(evt);
            }
        });

        JMenuItem menuItemPoupLock = new JMenuItem("Lock");
        jPopupMenuAccess.add(menuItemPoupLock);
        menuItemPoupLock.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuLockActionPerformed(evt);
            }
        });


        JMenuItem menuItemPoupKill = new JMenuItem("Kill");
        jPopupMenuAccess.add(menuItemPoupKill);
        menuItemPoupKill.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemKillAccessActionPerformed(evt);
            }
        });

        JMenuItem menuItemPoupBlockWrite = new JMenuItem("Block Write");
        jPopupMenuAccess.add(menuItemPoupBlockWrite);
        menuItemPoupBlockWrite.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemBlockWriteActionPerformed(evt);
            }
        });

        JMenuItem menuItemPoupBlockErase = new JMenuItem("Block Erase");
        jPopupMenuAccess.add(menuItemPoupBlockErase);
        menuItemPoupBlockErase.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemBlockEraseActionPerformed(evt);
            }
        });

    }


    public void initGPIportImages()
    {
        if (rfidBase.isConnected)
        {
            int gpiPorts = rfidBase.getMyReader().ReaderCapabilities.getNumGPIPorts();
            for (int index = 1; index <= gpiPorts; index++)
            {
                try {
                    if (rfidBase.getMyReader().Config.GPI.getPortState(index) == GPI_PORT_STATE.GPI_PORT_STATE_HIGH) {
                        setGPIOnScreen(index, true, true);
                    } else {
                        setGPIOnScreen(index, true, false);
                    }
                } catch (InvalidUsageException ex) {
                    ex.printStackTrace();
                } catch (OperationFailureException ex) {
                    ex.printStackTrace();
                }
            }
        }
        else
        {
            // disable the GPI port images
            for (int index = 1; index <= 10; index++) {
                setGPIOnScreen(index, false, false);
            }
        }
    }

    public void setGPIOnScreen(int portNo, boolean isVisible, boolean isPortStateHigh)
    {
        switch(portNo)
        {
            case 1:
                jLabelGPI1.setVisible(isVisible);
                if (isPortStateHigh)
                    jLabelGPI1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/connected.GIF"))); // NOI18N
                else
                    jLabelGPI1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/disconnected.GIF"))); // NOI18N

                break;
            case 2:
                jLabelGPI2.setVisible(isVisible);
                if (isPortStateHigh)
                    jLabelGPI2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/connected.GIF"))); // NOI18N
                else
                    jLabelGPI2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/disconnected.GIF"))); // NOI18N
                break;
            case 3:
                jLabelGPI3.setVisible(isVisible);
                if (isPortStateHigh)
                    jLabelGPI3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/connected.GIF"))); // NOI18N
                else
                    jLabelGPI3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/disconnected.GIF"))); // NOI18N
                break;
            case 4:
                jLabelGPI4.setVisible(isVisible);
                if (isPortStateHigh)
                    jLabelGPI4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/connected.GIF"))); // NOI18N
                else
                    jLabelGPI4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/disconnected.GIF"))); // NOI18N
                break;
            case 5:
                if (isPortStateHigh)
                    jLabelGPI5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/connected.GIF"))); // NOI18N
                else
                    jLabelGPI5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/disconnected.GIF"))); // NOI18N
                jLabelGPI5.setVisible(isVisible);
                break;
            case 6:
                if (isPortStateHigh)
                    jLabelGPI6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/connected.GIF"))); // NOI18N
                else
                    jLabelGPI6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/disconnected.GIF"))); // NOI18N
                jLabelGPI6.setVisible(isVisible);
                break;
            case 7:
                if (isPortStateHigh)
                    jLabelGPI7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/connected.GIF"))); // NOI18N
                else
                    jLabelGPI7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/disconnected.GIF"))); // NOI18N

                jLabelGPI7.setVisible(isVisible);
                break;
            case 8:
                if (isPortStateHigh)
                    jLabelGPI8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/connected.GIF"))); // NOI18N
                else
                    jLabelGPI8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/disconnected.GIF"))); // NOI18N

                jLabelGPI8.setVisible(isVisible);
                break;
            case 9:
                if (isPortStateHigh)
                    jLabelGPI9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/connected.GIF"))); // NOI18N
                else
                    jLabelGPI9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/disconnected.GIF"))); // NOI18N

                jLabelGPI9.setVisible(isVisible);
                break;
            case 10:
                if (isPortStateHigh)
                    jLabelGPI10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/connected.GIF"))); // NOI18N
                else
                    jLabelGPI10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/disconnected.GIF"))); // NOI18N

                jLabelGPI10.setVisible(isVisible);
                break;
        }

    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
   // @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPopupMenuAccess = new javax.swing.JPopupMenu();
        menuBar1 = new java.awt.MenuBar();
        menu1 = new java.awt.Menu();
        menu2 = new java.awt.Menu();
        menuBar2 = new java.awt.MenuBar();
        menu3 = new java.awt.Menu();
        menu4 = new java.awt.Menu();
        menuBar3 = new java.awt.MenuBar();
        menu5 = new java.awt.Menu();
        menu6 = new java.awt.Menu();
        menuBar4 = new java.awt.MenuBar();
        menu7 = new java.awt.Menu();
        menu8 = new java.awt.Menu();
        jDialog1 = new javax.swing.JDialog();
        startReadButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jInventoryTable = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        jLabelTotalTags = new javax.swing.JLabel();
        jButtonClearReports = new javax.swing.JButton();
        jComboBoxMemBank = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        jLabelStatus = new javax.swing.JLabel();
        jCheckBoxAutonomousMode = new javax.swing.JCheckBox();
        jPanelGPINotification = new javax.swing.JPanel();
        jLabelGPI1 = new javax.swing.JLabel();
        jLabelGPI2 = new javax.swing.JLabel();
        jLabelGPI3 = new javax.swing.JLabel();
        jLabelGPI4 = new javax.swing.JLabel();
        jLabelGPI5 = new javax.swing.JLabel();
        jLabelGPI6 = new javax.swing.JLabel();
        jLabelGPI7 = new javax.swing.JLabel();
        jLabelGPI8 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabelGPI9 = new javax.swing.JLabel();
        jLabelGPI10 = new javax.swing.JLabel();
        jPanelNotification = new javax.swing.JPanel();
        label1 = new java.awt.Label();
        labelEventName = new java.awt.Label();
        labelEventValue = new java.awt.Label();
        jLabelConnectImage = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItemConnection = new javax.swing.JMenuItem();
        jMenuItemCapabilities = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenu3 = new javax.swing.JMenu();
        jMenuItemTagStorageSettings = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        jMenuItemAntenna = new javax.swing.JMenuItem();
        jMenuItemRFModes = new javax.swing.JMenuItem();
        jMenuItemGPIO = new javax.swing.JMenuItem();
        jMenuItemSingulation = new javax.swing.JMenuItem();
        jMenuItemPowerOffRadio = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        jMenuItemResetFactoryDefaults = new javax.swing.JMenuItem();
        jMenu4 = new javax.swing.JMenu();
        jMenu6 = new javax.swing.JMenu();
        jMenuItemPreFilter = new javax.swing.JMenuItem();
        jMenuItemPostFilter = new javax.swing.JMenuItem();
        jMenuItemAccessFilter = new javax.swing.JMenuItem();
        jMenu7 = new javax.swing.JMenu();
        jMenuItemReadAccess = new javax.swing.JMenuItem();
        jMenuItemWriteAccess = new javax.swing.JMenuItem();
        jMenuItemLockAccess = new javax.swing.JMenuItem();
        jMenuItemKillAccess = new javax.swing.JMenuItem();
        jMenuItemBlockWrite = new javax.swing.JMenuItem();
        jMenuItemBlockErase = new javax.swing.JMenuItem();
        jMenuItemTriggers = new javax.swing.JMenuItem();
        jMenuItemAntennaInfo = new javax.swing.JMenuItem();
        jMenu5 = new javax.swing.JMenu();
        jMenuItemLogin = new javax.swing.JMenuItem();
        jMenuItemAntennaMode = new javax.swing.JMenuItem();
        jMenuItemReadPoint = new javax.swing.JMenuItem();
        jMenuItemSoftwareUpdate = new javax.swing.JMenuItem();
        jMenuItemReboot = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItemAbout = new javax.swing.JMenuItem();

        menu1.setLabel("File");
        menuBar1.add(menu1);

        menu2.setLabel("Edit");
        menuBar1.add(menu2);

        menu3.setLabel("File");
        menuBar2.add(menu3);

        menu4.setLabel("Edit");
        menuBar2.add(menu4);

        menu5.setLabel("File");
        menuBar3.add(menu5);

        menu6.setLabel("Edit");
        menuBar3.add(menu6);

        menu7.setLabel("File");
        menuBar4.add(menu7);

        menu8.setLabel("Edit");
        menuBar4.add(menu8);

        javax.swing.GroupLayout jDialog1Layout = new javax.swing.GroupLayout(jDialog1.getContentPane());
        jDialog1.getContentPane().setLayout(jDialog1Layout);
        jDialog1Layout.setHorizontalGroup(
            jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        jDialog1Layout.setVerticalGroup(
            jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("J_RFIDHostSample");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setFocusTraversalPolicyProvider(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });

        startReadButton.setText("Start Read");
        startReadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startReadButtonActionPerformed(evt);
            }
        });

        jInventoryTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "EPC ID", "State", "Antenna ID", "Seen Count", "RSSI", "Phase", "PC Bits", "Memory Bank Data", "MB", "Offset"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jInventoryTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jInventoryTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jInventoryTableMouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jInventoryTableMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jInventoryTableMouseReleased(evt);
            }
        });
        jScrollPane1.setViewportView(jInventoryTable);
        jInventoryTable.getColumnModel().getColumn(0).setMinWidth(90);
        jInventoryTable.getColumnModel().getColumn(0).setPreferredWidth(180);
        jInventoryTable.getColumnModel().getColumn(9).setPreferredWidth(70);

        jLabel1.setText("Total Tags:");

        jButtonClearReports.setText("Clear Reports");
        jButtonClearReports.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonClearReportsActionPerformed(evt);
            }
        });

        jComboBoxMemBank.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "None", "Reserved", "EPC", "TID", "USER" }));

        jLabel3.setText("Memory Bank:");

        jLabelStatus.setText("Ready");
        jLabelStatus.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jCheckBoxAutonomousMode.setText("Autonomous Mode");
        jCheckBoxAutonomousMode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxAutonomousModeActionPerformed(evt);
            }
        });

        jPanelGPINotification.setBorder(javax.swing.BorderFactory.createTitledBorder("GPI Port State"));

        jLabelGPI1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/disconnected.GIF"))); // NOI18N
        jLabelGPI1.setText("1");
        jLabelGPI1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabelGPI2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/disconnected.GIF"))); // NOI18N
        jLabelGPI2.setText("2");
        jLabelGPI2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabelGPI3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/disconnected.GIF"))); // NOI18N
        jLabelGPI3.setText("3");
        jLabelGPI3.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabelGPI4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/disconnected.GIF"))); // NOI18N
        jLabelGPI4.setText("4");
        jLabelGPI4.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabelGPI5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/disconnected.GIF"))); // NOI18N
        jLabelGPI5.setText("5");
        jLabelGPI5.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabelGPI6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/disconnected.GIF"))); // NOI18N
        jLabelGPI6.setText("6");
        jLabelGPI6.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabelGPI7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/disconnected.GIF"))); // NOI18N
        jLabelGPI7.setText("7");
        jLabelGPI7.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabelGPI8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/disconnected.GIF"))); // NOI18N
        jLabelGPI8.setText("8");
        jLabelGPI8.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel2.setText("Red: Low");

        jLabel4.setText("Green: High");

        jLabelGPI9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/disconnected.GIF"))); // NOI18N
        jLabelGPI9.setText("9");
        jLabelGPI9.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabelGPI10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/disconnected.GIF"))); // NOI18N
        jLabelGPI10.setText("10");
        jLabelGPI10.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout jPanelGPINotificationLayout = new javax.swing.GroupLayout(jPanelGPINotification);
        jPanelGPINotification.setLayout(jPanelGPINotificationLayout);
        jPanelGPINotificationLayout.setHorizontalGroup(
            jPanelGPINotificationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelGPINotificationLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelGPINotificationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel4))
                .addGap(18, 18, 18)
                .addComponent(jLabelGPI1, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(jLabelGPI2, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelGPI3, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelGPI4, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabelGPI5, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelGPI6, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabelGPI7, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabelGPI8, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabelGPI9, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabelGPI10, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanelGPINotificationLayout.setVerticalGroup(
            jPanelGPINotificationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelGPINotificationLayout.createSequentialGroup()
                .addGroup(jPanelGPINotificationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelGPINotificationLayout.createSequentialGroup()
                        .addGap(22, 22, 22)
                        .addGroup(jPanelGPINotificationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabelGPI1, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabelGPI3, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabelGPI2, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabelGPI4, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabelGPI5, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabelGPI6, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabelGPI7, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabelGPI8, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabelGPI9, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabelGPI10, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanelGPINotificationLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel4)))
                .addContainerGap(23, Short.MAX_VALUE))
        );

        jPanelNotification.setBorder(javax.swing.BorderFactory.createTitledBorder("Notification"));

        label1.setText("Last Event:");

        javax.swing.GroupLayout jPanelNotificationLayout = new javax.swing.GroupLayout(jPanelNotification);
        jPanelNotification.setLayout(jPanelNotificationLayout);
        jPanelNotificationLayout.setHorizontalGroup(
            jPanelNotificationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelNotificationLayout.createSequentialGroup()
                .addGroup(jPanelNotificationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanelNotificationLayout.createSequentialGroup()
                        .addGap(84, 84, 84)
                        .addComponent(labelEventValue, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanelNotificationLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(label1, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(labelEventName, javax.swing.GroupLayout.PREFERRED_SIZE, 204, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(30, Short.MAX_VALUE))
        );
        jPanelNotificationLayout.setVerticalGroup(
            jPanelNotificationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelNotificationLayout.createSequentialGroup()
                .addGroup(jPanelNotificationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelEventName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(label1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelEventValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(18, Short.MAX_VALUE))
        );

        jLabelConnectImage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/disconnected.GIF"))); // NOI18N
        jLabelConnectImage.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jMenu1.setMnemonic('r');
        jMenu1.setText("Reader");

        jMenuItemConnection.setMnemonic('n');
        jMenuItemConnection.setText("Connection...");
        jMenuItemConnection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemConnectionActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItemConnection);

        jMenuItemCapabilities.setMnemonic('p');
        jMenuItemCapabilities.setText("Capabilities");
        jMenuItemCapabilities.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemCapabilitiesActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItemCapabilities);
        jMenu1.add(jSeparator1);

        jMenuItem3.setMnemonic('x');
        jMenuItem3.setText("Exit");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem3);

        jMenuBar1.add(jMenu1);

        jMenu3.setMnemonic('c');
        jMenu3.setText("Config");
        jMenu3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenu3ActionPerformed(evt);
            }
        });

        jMenuItemTagStorageSettings.setMnemonic('T');
        jMenuItemTagStorageSettings.setText("Tag Storage settings...");
        jMenuItemTagStorageSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemTagStorageSettingsActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItemTagStorageSettings);
        jMenu3.add(jSeparator2);

        jMenuItemAntenna.setMnemonic('A');
        jMenuItemAntenna.setText("Antenna...");
        jMenuItemAntenna.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAntennaActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItemAntenna);

        jMenuItemRFModes.setText("RF Modes...");
        jMenuItemRFModes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemRFModesActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItemRFModes);

        jMenuItemGPIO.setMnemonic('G');
        jMenuItemGPIO.setText("GPIO...");
        jMenuItemGPIO.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemGPIOActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItemGPIO);

        jMenuItemSingulation.setMnemonic('S');
        jMenuItemSingulation.setText("Singulation...");
        jMenuItemSingulation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSingulationActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItemSingulation);

        jMenuItemPowerOffRadio.setMnemonic('P');
        jMenuItemPowerOffRadio.setText("Power Off Radio");
        jMenuItemPowerOffRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemPowerOffRadioActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItemPowerOffRadio);
        jMenu3.add(jSeparator3);

        jMenuItemResetFactoryDefaults.setMnemonic('D');
        jMenuItemResetFactoryDefaults.setText("Reset to Factory Defaults");
        jMenuItemResetFactoryDefaults.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemResetFactoryDefaultsActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItemResetFactoryDefaults);

        jMenuBar1.add(jMenu3);

        jMenu4.setText("Operations");

        jMenu6.setMnemonic('F');
        jMenu6.setText("Filter");

        jMenuItemPreFilter.setText("Pre-Filter...");
        jMenuItemPreFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemPreFilterActionPerformed(evt);
            }
        });
        jMenu6.add(jMenuItemPreFilter);

        jMenuItemPostFilter.setText("Post-Filter...");
        jMenuItemPostFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemPostFilterActionPerformed(evt);
            }
        });
        jMenu6.add(jMenuItemPostFilter);

        jMenuItemAccessFilter.setText("Access-Filter...");
        jMenuItemAccessFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAccessFilterActionPerformed(evt);
            }
        });
        jMenu6.add(jMenuItemAccessFilter);

        jMenu4.add(jMenu6);

        jMenu7.setMnemonic('c');
        jMenu7.setText("Access");

        jMenuItemReadAccess.setText("Read...");
        jMenuItemReadAccess.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemReadAccessActionPerformed(evt);
            }
        });
        jMenu7.add(jMenuItemReadAccess);

        jMenuItemWriteAccess.setText("Write...");
        jMenuItemWriteAccess.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemWriteAccessActionPerformed(evt);
            }
        });
        jMenu7.add(jMenuItemWriteAccess);

                jMenuItemLockAccess.setText("Lock...");





        jMenu7.add(jMenuItemLockAccess);
		 jMenuItemLockAccess.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuLockActionPerformed(evt);
            }
        });

        jMenuItemKillAccess.setText("Kill...");
        jMenuItemKillAccess.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemKillAccessActionPerformed(evt);
            }
        });
        jMenu7.add(jMenuItemKillAccess);

        jMenuItemBlockWrite.setText("BlockWrite...");
        jMenuItemBlockWrite.setActionCommand("BlockWrite");
        jMenuItemBlockWrite.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemBlockWriteActionPerformed(evt);
            }
        });
        jMenu7.add(jMenuItemBlockWrite);

        jMenuItemBlockErase.setText("Block Erase...");
        jMenuItemBlockErase.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemBlockEraseActionPerformed(evt);
            }
        });
        jMenu7.add(jMenuItemBlockErase);

        jMenu4.add(jMenu7);

        jMenuItemTriggers.setMnemonic('T');
        jMenuItemTriggers.setText("Triggers...");
        jMenuItemTriggers.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemTriggersActionPerformed(evt);
            }
        });
        jMenu4.add(jMenuItemTriggers);

        jMenuItemAntennaInfo.setMnemonic('A');
        jMenuItemAntennaInfo.setText("Antenna Info..");
        jMenuItemAntennaInfo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAntennaInfoActionPerformed(evt);
            }
        });
        jMenu4.add(jMenuItemAntennaInfo);

        jMenuBar1.add(jMenu4);

        jMenu5.setMnemonic('M');
        jMenu5.setText("Management");

        jMenuItemLogin.setMnemonic('L');
        jMenuItemLogin.setText("Login/Logout...");
        jMenuItemLogin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemLoginActionPerformed(evt);
            }
        });
        jMenu5.add(jMenuItemLogin);

        jMenuItemAntennaMode.setMnemonic('A');
        jMenuItemAntennaMode.setText("Antenna Mode...");
        jMenuItemAntennaMode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAntennaModeActionPerformed(evt);
            }
        });
        jMenu5.add(jMenuItemAntennaMode);

        jMenuItemReadPoint.setText("Read Point...");
        jMenuItemReadPoint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemReadPointActionPerformed(evt);
            }
        });
        jMenu5.add(jMenuItemReadPoint);

        jMenuItemSoftwareUpdate.setMnemonic('U');
        jMenuItemSoftwareUpdate.setText("Software/Firmware Update...");
        jMenuItemSoftwareUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSoftwareUpdateActionPerformed(evt);
            }
        });
        jMenu5.add(jMenuItemSoftwareUpdate);

        jMenuItemReboot.setMnemonic('b');
        jMenuItemReboot.setText("Reboot");
        jMenuItemReboot.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemRebootActionPerformed(evt);
            }
        });
        jMenu5.add(jMenuItemReboot);

        jMenuBar1.add(jMenu5);

        jMenu2.setMnemonic('H');
        jMenu2.setText("Help");

        jMenuItemAbout.setMnemonic('A');
        jMenuItemAbout.setText("About");
        jMenuItemAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAboutActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItemAbout);

        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(startReadButton)
                .addGap(18, 18, 18)
                .addComponent(jLabel1)
                .addGap(6, 6, 6)
                .addComponent(jLabelTotalTags, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(50, 50, 50)
                .addComponent(jCheckBoxAutonomousMode)
                .addGap(83, 83, 83)
                .addComponent(jButtonClearReports)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 31, Short.MAX_VALUE)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBoxMemBank, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jPanelGPINotification, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelNotification, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabelStatus, javax.swing.GroupLayout.DEFAULT_SIZE, 880, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelConnectImage, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1))
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 907, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(startReadButton)
                    .addComponent(jLabelTotalTags, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCheckBoxAutonomousMode)
                    .addComponent(jButtonClearReports)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jComboBoxMemBank, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel3)))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(11, 11, 11)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanelGPINotification, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanelNotification, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabelStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelConnectImage, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public JComboBox getjComboBoxMemBank() {
        return jComboBoxMemBank;
    }

    public JLabel getjLabelStatus() {
        return jLabelStatus;
    }

    private void startReadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startReadButtonActionPerformed
        //None, EPC, TID, RESERVED, USER
        if (startReadButton.getLabel().equals("Start Read")){
            startReadButton.setLabel("Stop Read");
            rfidBase.startRead();
        }
        else{
           startReadButton.setLabel("Start Read");
           rfidBase.stopRead();
        }
    }//GEN-LAST:event_startReadButtonActionPerformed

    public void StartReadButtonClicked ()
    {
        startReadButton.setLabel("Start Read");
    }
    
    public void updateGenericMenuItems(boolean isConnected)
    {
        //jMenuItemConnection.setEnabled(false);
        jMenuItemCapabilities.setEnabled(isConnected);
        jMenuItemTagStorageSettings.setEnabled(isConnected);
        jMenuItemAntenna.setEnabled(isConnected);
        jMenuItemRFModes.setEnabled(isConnected);
        jMenuItemGPIO.setEnabled(isConnected);
        jMenuItemSingulation.setEnabled(isConnected);
        jMenuItemPowerOffRadio.setEnabled(isConnected);
        jMenuItemResetFactoryDefaults.setEnabled(isConnected);
        jMenuItemPreFilter.setEnabled(isConnected);
        jMenuItemPostFilter.setEnabled(isConnected);
        jMenuItemAccessFilter.setEnabled(isConnected);
        jMenuItemReadAccess.setEnabled(isConnected);
        jMenuItemWriteAccess.setEnabled(isConnected);
        jMenuItemLockAccess.setEnabled(isConnected);
        jMenuItemKillAccess.setEnabled(isConnected);
        jMenuItemBlockWrite.setEnabled(isConnected);
        jMenuItemBlockErase.setEnabled(isConnected);
        jMenuItemTriggers.setEnabled(isConnected);
        jMenuItemAntennaInfo.setEnabled(isConnected);
        
        // update control
        startReadButton.setEnabled(isConnected);
        if (isConnected == false)
            startReadButton.setLabel("Start Read");
        else // connection established
            postNotification("", "");
        jCheckBoxAutonomousMode.setEnabled(isConnected);
        jButtonClearReports.setEnabled(isConnected);
        jComboBoxMemBank.setEnabled(isConnected);
    }

    public void updateRMMenuItems(boolean isConnected)
    {
        jMenuItemAntennaMode.setEnabled(isConnected);
        jMenuItemReadPoint.setEnabled(isConnected);
        jMenuItemSoftwareUpdate.setEnabled(isConnected);
        jMenuItemReboot.setEnabled(isConnected);

    }

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        rfidBase.disconnectReader();
    }//GEN-LAST:event_formWindowClosed

    private void jButtonClearReportsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonClearReportsActionPerformed
        clearReports();
    }
    
    public void clearReports() {                                                    
       DefaultTableModel tableModel = (DefaultTableModel)jInventoryTable.getModel();
       synchronized (this)
       {
           tableModel.setRowCount(0);
           rfidBase.tagStore.clear();
           tableModel.setRowCount(25);

           rfidBase.totalTags = 0;
           rfidBase.uniqueTags = 0;
           rfidBase.rowId = 0;
           jLabelTotalTags.setText("");
       }

    }//GEN-LAST:event_jButtonClearReportsActionPerformed

    private void jMenuItemConnectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemConnectionActionPerformed
        ConnectionDlg connectionDlg = new ConnectionDlg(this, rootPaneCheckingEnabled);
        connectionDlg.setLocationRelativeTo(null);
        connectionDlg.show();

    }//GEN-LAST:event_jMenuItemConnectionActionPerformed

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        // TODO add your handling code here:
        processEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));

    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private void jInventoryTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jInventoryTableMouseClicked
        // TODO add your handling code here:

    }//GEN-LAST:event_jInventoryTableMouseClicked

    private void jMenuItemAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAboutActionPerformed
        AboutDlg aboutDlg = new AboutDlg(this, rootPaneCheckingEnabled);
        aboutDlg.setLocationRelativeTo(null);
        aboutDlg.show();
    }//GEN-LAST:event_jMenuItemAboutActionPerformed

    private void jMenuItemCapabilitiesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemCapabilitiesActionPerformed
        CapababilitiesDlg capabilityDlg = new CapababilitiesDlg(this, rootPaneCheckingEnabled);
        capabilityDlg.setLocationRelativeTo(null);
        capabilityDlg.show();

    }//GEN-LAST:event_jMenuItemCapabilitiesActionPerformed

    private void jMenu3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenu3ActionPerformed

    }//GEN-LAST:event_jMenu3ActionPerformed

    private void jMenuItemTagStorageSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemTagStorageSettingsActionPerformed
        TagStorageSettingsDlg tagStorageSettingDlg = new TagStorageSettingsDlg(this, rootPaneCheckingEnabled);
        tagStorageSettingDlg.setLocationRelativeTo(null);
        tagStorageSettingDlg.show();
    }//GEN-LAST:event_jMenuItemTagStorageSettingsActionPerformed

    private void jMenuItemAntennaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAntennaActionPerformed
        AntennaConfigDlg antennaConfigDlg = new AntennaConfigDlg(this, rootPaneCheckingEnabled);
        antennaConfigDlg.setLocationRelativeTo(null);
        antennaConfigDlg.show();
    }//GEN-LAST:event_jMenuItemAntennaActionPerformed

    private void jMenuItemResetFactoryDefaultsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemResetFactoryDefaultsActionPerformed
        try {
            rfidBase.getMyReader().Config.resetFactoryDefaults();
        } catch (InvalidUsageException ex) {
            rfidBase.postStatusNotification(RFIDBase.PARAM_ERROR, ex.getVendorMessage());
        } catch (OperationFailureException ex) {
            rfidBase.postStatusNotification(ex.getStatusDescription(), ex.getVendorMessage());
        }
    }//GEN-LAST:event_jMenuItemResetFactoryDefaultsActionPerformed

    private void jMenuItemPowerOffRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemPowerOffRadioActionPerformed
        try {

            if (rfidBase.getMyReader().Config.getRadioPowerState() == RADIO_POWER_STATE.OFF)
            {
                rfidBase.getMyReader().Config.setRadioPowerState(RADIO_POWER_STATE.ON);
                jMenuItemPowerOffRadio.setText("Power Off Radio");
            }
            else
            {
                rfidBase.getMyReader().Config.setRadioPowerState(RADIO_POWER_STATE.OFF);
                jMenuItemPowerOffRadio.setText("Power On Radio");
            }

        } catch (InvalidUsageException ex) {
            rfidBase.postStatusNotification(RFIDBase.PARAM_ERROR, ex.getVendorMessage());
        } catch (OperationFailureException ex) {
            rfidBase.postStatusNotification(ex.getStatusDescription(), ex.getVendorMessage());
        }
    }//GEN-LAST:event_jMenuItemPowerOffRadioActionPerformed

    private void jMenuItemRFModesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemRFModesActionPerformed
        RFModeDlg rfModeDlg = new RFModeDlg(this, rootPaneCheckingEnabled);
        rfModeDlg.setLocationRelativeTo(null);
        rfModeDlg.show();

    }//GEN-LAST:event_jMenuItemRFModesActionPerformed

    private void jMenuItemSingulationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSingulationActionPerformed
        SingulationDlg singulationDlg = new SingulationDlg(this, rootPaneCheckingEnabled);
        singulationDlg.setLocationRelativeTo(null);
        singulationDlg.show();
    }//GEN-LAST:event_jMenuItemSingulationActionPerformed

    private void jMenuItemReadAccessActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemReadAccessActionPerformed
        ReadAccessDlg readDlg = new ReadAccessDlg(this, rootPaneCheckingEnabled);
        readDlg.setLocationRelativeTo(null);
        readDlg.show();
    }//GEN-LAST:event_jMenuItemReadAccessActionPerformed

    private void jInventoryTableMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jInventoryTableMousePressed

    }//GEN-LAST:event_jInventoryTableMousePressed

    private void jInventoryTableMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jInventoryTableMouseReleased
        if (evt.isPopupTrigger())
        {
            JTable source = (JTable)evt.getSource();
            int row = source.rowAtPoint(evt.getPoint());
            int column = source.columnAtPoint(evt.getPoint());

            if(source.isRowSelected(row))
            {
                source.changeSelection(row, column, rootPaneCheckingEnabled, rootPaneCheckingEnabled);
                jPopupMenuAccess.show(evt.getComponent(), evt.getX(), evt.getY());
            }


        }
    }//GEN-LAST:event_jInventoryTableMouseReleased

    private void jMenuItemWriteAccessActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemWriteAccessActionPerformed
        WriteAccessDlg writeDlg = new WriteAccessDlg(this, rootPaneCheckingEnabled, false);
        writeDlg.setLocationRelativeTo(null);
        writeDlg.show();

    }//GEN-LAST:event_jMenuItemWriteAccessActionPerformed

    private void jMenuItemBlockWriteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemBlockWriteActionPerformed
        WriteAccessDlg writeDlg = new WriteAccessDlg(this, rootPaneCheckingEnabled, true);
        writeDlg.setLocationRelativeTo(null);
        writeDlg.show();
    }//GEN-LAST:event_jMenuItemBlockWriteActionPerformed

    private void jMenuItemBlockEraseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemBlockEraseActionPerformed
        BlockEraseAccessDlg blockEraseAccessDlg = new BlockEraseAccessDlg(this, rootPaneCheckingEnabled);
        blockEraseAccessDlg.setLocationRelativeTo(null);
        blockEraseAccessDlg.show();
    }//GEN-LAST:event_jMenuItemBlockEraseActionPerformed
	
	private void jMenuLockActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenu7ActionPerformed
        LockAccessDlg lockAccessDlg = new LockAccessDlg(this, rootPaneCheckingEnabled);
        lockAccessDlg.setLocationRelativeTo(null);
        lockAccessDlg.show();
    }//GEN-LAST:event_jMenu7ActionPerformed

    private void jMenuItemKillAccessActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemKillAccessActionPerformed
        KillAccessDlg killAccessDlg = new KillAccessDlg(this, rootPaneCheckingEnabled);
        killAccessDlg.setLocationRelativeTo(null);
        killAccessDlg.show();
    }//GEN-LAST:event_jMenuItemKillAccessActionPerformed

    private void jMenuItemAccessFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAccessFilterActionPerformed
        AccessFilterDlg accessFilterDlg = new AccessFilterDlg(this, rootPaneCheckingEnabled);
        accessFilterDlg.setLocationRelativeTo(null);
        accessFilterDlg.show();

    }//GEN-LAST:event_jMenuItemAccessFilterActionPerformed

    private void jMenuItemPostFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemPostFilterActionPerformed
        PostFilterDialog postFilterDlg = new PostFilterDialog(this, rootPaneCheckingEnabled);
        postFilterDlg.setLocationRelativeTo(null);
        postFilterDlg.show();
    }//GEN-LAST:event_jMenuItemPostFilterActionPerformed

    private void jMenuItemAntennaInfoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAntennaInfoActionPerformed
        AntennaSelection antennaSelDlg = new AntennaSelection(this, rootPaneCheckingEnabled);
        antennaSelDlg.setLocationRelativeTo(null);
        antennaSelDlg.show();
    }//GEN-LAST:event_jMenuItemAntennaInfoActionPerformed

    private void jMenuItemPreFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemPreFilterActionPerformed
        PreFilterDlg preFilterDlg = new PreFilterDlg(this, rootPaneCheckingEnabled);
        preFilterDlg.setLocationRelativeTo(null);
        preFilterDlg.show();
    }//GEN-LAST:event_jMenuItemPreFilterActionPerformed

    private void jMenuItemTriggersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemTriggersActionPerformed
        TriggersDlg triggersDlg = new TriggersDlg(this, rootPaneCheckingEnabled);
        triggersDlg.setLocationRelativeTo(null);
        triggersDlg.show();
    }//GEN-LAST:event_jMenuItemTriggersActionPerformed

    private void jMenuItemRebootActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemRebootActionPerformed
        try {
            rfidBase.rm.restart();
            rfidBase.logOut();
        } catch (InvalidUsageException ex) {
            rfidBase.postStatusNotification(RFIDBase.PARAM_ERROR, ex.getVendorMessage());
        } catch (OperationFailureException ex) {
            rfidBase.postStatusNotification(ex.getStatusDescription(), ex.getVendorMessage());
        }
    }//GEN-LAST:event_jMenuItemRebootActionPerformed

    private void jMenuItemLoginActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemLoginActionPerformed
           LoginDlg loginDlg = new LoginDlg(this, rootPaneCheckingEnabled);
           loginDlg.setLocationRelativeTo(null);
           loginDlg.show();
    }//GEN-LAST:event_jMenuItemLoginActionPerformed

    private void jMenuItemAntennaModeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAntennaModeActionPerformed
           AntennaModeDlg antennaModeDlg = new AntennaModeDlg(this, rootPaneCheckingEnabled);
           antennaModeDlg.setLocationRelativeTo(null);
           antennaModeDlg.show();
           
    }//GEN-LAST:event_jMenuItemAntennaModeActionPerformed

    private void jMenuItemReadPointActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemReadPointActionPerformed
           ReadPointDlg readPointDlg = new ReadPointDlg(this, rootPaneCheckingEnabled);
           readPointDlg.setLocationRelativeTo(null);
           readPointDlg.show();
    }//GEN-LAST:event_jMenuItemReadPointActionPerformed

    private void jMenuItemGPIOActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemGPIOActionPerformed
           GPIOConfigDlg gpioConfigDlg = new GPIOConfigDlg(this, rootPaneCheckingEnabled);
           gpioConfigDlg.setLocationRelativeTo(null);
           gpioConfigDlg.show();
    }//GEN-LAST:event_jMenuItemGPIOActionPerformed

    private void jMenuItemSoftwareUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSoftwareUpdateActionPerformed
           SoftwareUpdateDlg softwareUpdateDlg = new SoftwareUpdateDlg(this, rootPaneCheckingEnabled);
           softwareUpdateDlg.setLocationRelativeTo(null);
           softwareUpdateDlg.show();
    }//GEN-LAST:event_jMenuItemSoftwareUpdateActionPerformed

    private void jCheckBoxAutonomousModeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxAutonomousModeActionPerformed
        // TODO add your handling code here:
        if (jCheckBoxAutonomousMode.isSelected())
            rfidBase.triggerInfo.setEnableTagEventReport(true);
        else
            rfidBase.triggerInfo.setEnableTagEventReport(false);
    }//GEN-LAST:event_jCheckBoxAutonomousModeActionPerformed

    public void postNotification(String eventName, String eventValue)
    {
        labelEventName.setText(eventName);
        labelEventValue.setText(eventValue);
    }

    public void updateUI()
    {
        if (rfidBase.isConnected == true)
        {
            jLabelConnectImage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/connected.GIF")));
        }
        else
        {
            jLabelConnectImage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/disconnected.GIF")));
        }
        initGPIportImages();
    }
    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                Toolkit tk = Toolkit.getDefaultToolkit();
                Dimension d = tk.getScreenSize();
                RFIDMainDlg rfidSample = new RFIDMainDlg();
                rfidSample.setLocation((int)(d.getWidth() - rfidSample.getWidth())/2, (int)(d.getHeight() - rfidSample.getHeight())/2);
                Image appIcon = tk.getImage("Zebra.GIF");
                rfidSample.setIconImage(appIcon);
                rfidSample.setVisible(true);


            }
        });
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonClearReports;
    private javax.swing.JCheckBox jCheckBoxAutonomousMode;
    private javax.swing.JComboBox jComboBoxMemBank;
    private javax.swing.JDialog jDialog1;
    private javax.swing.JTable jInventoryTable;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabelConnectImage;
    private javax.swing.JLabel jLabelGPI1;
    private javax.swing.JLabel jLabelGPI10;
    private javax.swing.JLabel jLabelGPI2;
    private javax.swing.JLabel jLabelGPI3;
    private javax.swing.JLabel jLabelGPI4;
    private javax.swing.JLabel jLabelGPI5;
    private javax.swing.JLabel jLabelGPI6;
    private javax.swing.JLabel jLabelGPI7;
    private javax.swing.JLabel jLabelGPI8;
    private javax.swing.JLabel jLabelGPI9;
    private javax.swing.JLabel jLabelStatus;
    private javax.swing.JLabel jLabelTotalTags;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenu jMenu5;
    private javax.swing.JMenu jMenu6;
    private javax.swing.JMenu jMenu7;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItemAbout;
    private javax.swing.JMenuItem jMenuItemAccessFilter;
    private javax.swing.JMenuItem jMenuItemAntenna;
    private javax.swing.JMenuItem jMenuItemAntennaInfo;
    private javax.swing.JMenuItem jMenuItemAntennaMode;
    private javax.swing.JMenuItem jMenuItemBlockErase;
    private javax.swing.JMenuItem jMenuItemBlockWrite;
    private javax.swing.JMenuItem jMenuItemCapabilities;
    private javax.swing.JMenuItem jMenuItemConnection;
    private javax.swing.JMenuItem jMenuItemGPIO;
    private javax.swing.JMenuItem jMenuItemKillAccess;
    private javax.swing.JMenuItem jMenuItemLockAccess;
    private javax.swing.JMenuItem jMenuItemLogin;
    private javax.swing.JMenuItem jMenuItemPostFilter;
    private javax.swing.JMenuItem jMenuItemPowerOffRadio;
    private javax.swing.JMenuItem jMenuItemPreFilter;
    private javax.swing.JMenuItem jMenuItemRFModes;
    private javax.swing.JMenuItem jMenuItemReadAccess;
    private javax.swing.JMenuItem jMenuItemReadPoint;
    private javax.swing.JMenuItem jMenuItemReboot;
    private javax.swing.JMenuItem jMenuItemResetFactoryDefaults;
    private javax.swing.JMenuItem jMenuItemSingulation;
    private javax.swing.JMenuItem jMenuItemSoftwareUpdate;
    private javax.swing.JMenuItem jMenuItemTagStorageSettings;
    private javax.swing.JMenuItem jMenuItemTriggers;
    private javax.swing.JMenuItem jMenuItemWriteAccess;
    private javax.swing.JPanel jPanelGPINotification;
    private javax.swing.JPanel jPanelNotification;
    private javax.swing.JPopupMenu jPopupMenuAccess;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private java.awt.Label label1;
    private java.awt.Label labelEventName;
    private java.awt.Label labelEventValue;
    private java.awt.Menu menu1;
    private java.awt.Menu menu2;
    private java.awt.Menu menu3;
    private java.awt.Menu menu4;
    private java.awt.Menu menu5;
    private java.awt.Menu menu6;
    private java.awt.Menu menu7;
    private java.awt.Menu menu8;
    private java.awt.MenuBar menuBar1;
    private java.awt.MenuBar menuBar2;
    private java.awt.MenuBar menuBar3;
    private java.awt.MenuBar menuBar4;
    private javax.swing.JButton startReadButton;
    // End of variables declaration//GEN-END:variables

    public void setjLabelStatus(JLabel jLabelStatus) {
        this.jLabelStatus = jLabelStatus;
    }
    // End of variables declaration


    // End of variables declaration

    public JLabel getjLabelTotalTags() {
        return jLabelTotalTags;
    }

    public JTable getjInventoryTable() {
        return jInventoryTable;
    }

}
