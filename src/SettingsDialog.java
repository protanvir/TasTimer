import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Modal settings dialog with four tabs:
 *   Wiclax | Local API | Reader | Data
 *
 * Reads current values from AppConfig on open.
 * Writes back and saves only when the user clicks Save.
 */
public class SettingsDialog extends JDialog {

    private final RufusAPIClient rufusApiClient;

    // --- Wiclax tab ---
    private final JCheckBox  wiclaxEnabled  = new JCheckBox("Enable Wiclax Integration");
    private final JTextField wiclaxUser     = new JTextField(20);
    private final JTextField wiclaxDeviceId = new JTextField(20);
    private final JTextField wiclaxUrl      = new JTextField(20);
    private final JTextField wiclaxPort     = new JTextField(20);

    // --- RufusAPI tab ---
    private final JCheckBox  rufusApiEnabled      = new JCheckBox("Enable RufusAPI Integration");
    private final JTextField rufusApiKey          = new JTextField(20);
    private final JTextField rufusApiSerialNumber = new JTextField(20);
    private final JTextField rufusApiAlias        = new JTextField(20);
    private final JTextField rufusApiDeviceId     = new JTextField(20);
    private final JLabel     rufusApiStatus       = new JLabel("(no session)");

    // --- FeiBot tab ---
    private final JCheckBox  feiBotEnabled     = new JCheckBox("Enable FeiBot Integration");
    private final JTextField feiBotServerIp    = new JTextField(20);
    private final JTextField feiBotServerPort  = new JTextField(20);
    private final JTextField feiBotDeviceId    = new JTextField(20);
    private final JTextField feiBotDeviceModel = new JTextField(20);

    // --- Local API tab ---
    private final JCheckBox  localApiEnabled  = new JCheckBox("Enable Local API Integration");
    private final JTextField localApiPort     = new JTextField(20);
    private final JTextField localApiEndpoint = new JTextField(20);

    // --- Reader tab ---
    private final JTextField zebraPort         = new JTextField(20);
    private final JTextField connectionTimeout = new JTextField(20);
    private final JCheckBox  autoReconnect     = new JCheckBox("Enable Auto-Reconnect on Disconnect");

    // --- Data tab ---
    private final JComboBox<String> epcFormat       = new JComboBox<>(new String[]{"LAST_N", "FULL"});
    private final JSpinner          epcLastN        = new JSpinner(new SpinnerNumberModel(8, 1, 32, 1));
    private final JTextField        csvDir          = new JTextField(20);
    private final JSpinner          suppressionSecs = new JSpinner(new SpinnerNumberModel(300, 1, 86400, 30));

    public SettingsDialog(Frame parent, RufusAPIClient rufusApiClient) {
        super(parent, "Settings", true);
        this.rufusApiClient = rufusApiClient;
        populateFromConfig();
        buildUi();
        pack();
        setMinimumSize(new Dimension(440, 360));
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    // ------------------------------------------------------------------ //
    // Populate fields from AppConfig
    // ------------------------------------------------------------------ //
    private void populateFromConfig() {
        AppConfig c = AppConfig.getInstance();

        wiclaxEnabled.setSelected(c.isWiclaxEnabled());
        wiclaxUser.setText(c.getWiclaxUser());
        wiclaxDeviceId.setText(c.getWiclaxDeviceId());
        wiclaxUrl.setText(c.getWiclaxUrl());
        wiclaxPort.setText(String.valueOf(c.getWiclaxPort()));

        feiBotEnabled.setSelected(c.isFeiBotEnabled());
        feiBotServerIp.setText(c.getFeiBotServerIp());
        feiBotServerPort.setText(String.valueOf(c.getFeiBotServerPort()));
        feiBotDeviceId.setText(c.getFeiBotDeviceId());
        feiBotDeviceModel.setText(c.getFeiBotDeviceModel());

        rufusApiEnabled.setSelected(c.isRufusApiEnabled());
        rufusApiKey.setText(c.getRufusApiKey());
        rufusApiSerialNumber.setText(c.getRufusApiSerialNumber());
        rufusApiAlias.setText(c.getRufusApiAlias());
        rufusApiDeviceId.setText(c.getRufusApiDeviceId());

        localApiEnabled.setSelected(c.isLocalApiEnabled());
        localApiPort.setText(String.valueOf(c.getLocalApiPort()));
        localApiEndpoint.setText(c.getLocalApiEndpoint());

        zebraPort.setText(String.valueOf(c.getZebraPort()));
        connectionTimeout.setText(String.valueOf(c.getConnectionTimeout()));
        autoReconnect.setSelected(c.isAutoReconnect());

        epcFormat.setSelectedItem(c.getEpcFormat());
        epcLastN.setValue(c.getEpcLastN());
        csvDir.setText(c.getCsvDir());
        suppressionSecs.setValue(c.getSuppressionSecs());
    }

    // ------------------------------------------------------------------ //
    // Build UI
    // ------------------------------------------------------------------ //
    private void buildUi() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Wiclax",    buildWiclaxTab());
        tabs.addTab("RufusAPI",  buildRufusApiTab());
        tabs.addTab("FeiBot",    buildFeiBotTab());
        tabs.addTab("Local API", buildLocalApiTab());
        tabs.addTab("Reader",    buildReaderTab());
        tabs.addTab("Data",      buildDataTab());

        // Toggle LAST_N spinner based on combo selection
        epcFormat.addActionListener(e ->
            epcLastN.setEnabled("LAST_N".equals(epcFormat.getSelectedItem()))
        );
        epcLastN.setEnabled("LAST_N".equals(epcFormat.getSelectedItem()));

        // Button row
        JButton saveBtn   = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");
        saveBtn.setPreferredSize(new Dimension(90, 32));
        cancelBtn.setPreferredSize(new Dimension(90, 32));
        saveBtn.addActionListener(this::onSave);
        cancelBtn.addActionListener(e -> dispose());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);

        setLayout(new BorderLayout(0, 0));
        add(tabs, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
    }

    // ------------------------------------------------------------------ //
    // Tab builders
    // ------------------------------------------------------------------ //
    private JPanel buildWiclaxTab() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(12, 14, 8, 14));

        GridBagConstraints gc = baseGc();

        gc.gridwidth = 2;
        p.add(wiclaxEnabled, gc); gc.gridy++;
        gc.gridwidth = 1;

        addRow(p, gc, "User:",        wiclaxUser);
        addRow(p, gc, "Device ID:",   wiclaxDeviceId);
        addRow(p, gc, "Server URL:",  wiclaxUrl);
        addRow(p, gc, "Port:",        wiclaxPort);

        addNote(p, gc, "Credentials are sent with every tag passing to Wiclax.");
        return p;
    }

    private JPanel buildRufusApiTab() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(12, 14, 8, 14));

        GridBagConstraints gc = baseGc();

        gc.gridwidth = 2;
        p.add(rufusApiEnabled, gc); gc.gridy++;
        gc.gridwidth = 1;

        addRow(p, gc, "API Key:",       rufusApiKey);
        addRow(p, gc, "Serial Number:", rufusApiSerialNumber);
        addRow(p, gc, "Device Alias:",  rufusApiAlias);

        // Bind Device row
        gc.gridx = 0; gc.weightx = 0; gc.anchor = GridBagConstraints.EAST;
        p.add(new JLabel("Device ID:"), gc);
        gc.gridx = 1; gc.weightx = 1; gc.anchor = GridBagConstraints.WEST;
        JPanel bindRow = new JPanel(new BorderLayout(6, 0));
        bindRow.add(rufusApiDeviceId, BorderLayout.CENTER);
        JButton bindBtn = new JButton("Bind Device");
        bindBtn.setToolTipText("Register this device with RUFUS and retrieve its Device ID");
        bindBtn.addActionListener(e -> onBindRufusDevice());
        bindRow.add(bindBtn, BorderLayout.EAST);
        p.add(bindRow, gc);
        gc.gridy++;

        // Session row
        gc.gridx = 0; gc.weightx = 0; gc.anchor = GridBagConstraints.EAST;
        p.add(new JLabel("Session:"), gc);
        gc.gridx = 1; gc.weightx = 1; gc.anchor = GridBagConstraints.WEST;
        JPanel sessionRow = new JPanel(new BorderLayout(6, 0));
        sessionRow.add(rufusApiStatus, BorderLayout.CENTER);
        JButton createSessionBtn = new JButton("Create Session");
        createSessionBtn.addActionListener(e -> onCreateRufusSession());
        sessionRow.add(createSessionBtn, BorderLayout.EAST);
        p.add(sessionRow, gc);
        gc.gridy++;

        addNote(p, gc, "Step 1: Enter API Key + Serial Number, click \"Bind Device\" → fills Device ID.\nStep 2: Click \"Create Session\" to start receiving passings.\nStep 3: Save settings.");
        return p;
    }

    private void onBindRufusDevice() {
        String apiKey = rufusApiKey.getText().trim();
        String serial = rufusApiSerialNumber.getText().trim();
        String alias  = rufusApiAlias.getText().trim();
        if (apiKey.isEmpty() || serial.isEmpty()) {
            JOptionPane.showMessageDialog(this, "API Key and Serial Number are required to bind a device.",
                    "Missing Input", JOptionPane.WARNING_MESSAGE);
            return;
        }
        new Thread(() -> {
            String deviceId = RufusAPIClient.bindDeviceSync(apiKey, serial,
                    alias.isEmpty() ? "TimingSoft Device" : alias);
            SwingUtilities.invokeLater(() -> {
                if (deviceId != null) {
                    rufusApiDeviceId.setText(deviceId);
                    JOptionPane.showMessageDialog(this,
                            "Device bound successfully!\nDevice ID: " + deviceId
                            + "\n\nNow click \"Create Session\" to continue.",
                            "Device Bound", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Failed to bind device.\nCheck the API Key and Serial Number, and see console for details.",
                            "Bind Failed", JOptionPane.ERROR_MESSAGE);
                }
            });
        }).start();
    }

    private void onCreateRufusSession() {
        String apiKey   = rufusApiKey.getText().trim();
        String deviceId = rufusApiDeviceId.getText().trim();
        if (apiKey.isEmpty() || deviceId.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "API Key and Device ID are required.\nBind a device first if you don't have a Device ID.",
                    "Missing Input", JOptionPane.WARNING_MESSAGE);
            return;
        }
        new Thread(() -> {
            String token = RufusAPIClient.createSessionSync(apiKey, deviceId);
            SwingUtilities.invokeLater(() -> {
                if (token != null) {
                    rufusApiClient.cacheSessionToken(token);
                    rufusApiStatus.setText("✓ Active: " + token.substring(0, Math.min(8, token.length())) + "...");
                    rufusApiStatus.setForeground(new Color(0, 150, 0));
                    JOptionPane.showMessageDialog(this,
                            "Session created successfully!\nToken cached in memory.",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    rufusApiStatus.setText("✗ Failed");
                    rufusApiStatus.setForeground(Color.RED);
                    JOptionPane.showMessageDialog(this,
                            "Failed to create session.\nCheck API Key and Device ID. See console for server error details.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        }).start();
    }

    private JPanel buildFeiBotTab() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(12, 14, 8, 14));

        GridBagConstraints gc = baseGc();

        gc.gridwidth = 2;
        p.add(feiBotEnabled, gc); gc.gridy++;
        gc.gridwidth = 1;

        addRow(p, gc, "Server IP:",    feiBotServerIp);
        addRow(p, gc, "Server Port:",  feiBotServerPort);
        addRow(p, gc, "Device ID:",    feiBotDeviceId);
        addRow(p, gc, "Device Model:", feiBotDeviceModel);

        addNote(p, gc, "Sends UDP packets to the FeiBot server (External Protocol V1.0).\nHeartbeat every 5s. Detection packet on every accepted tag read.");
        return p;
    }

    private JPanel buildLocalApiTab() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(12, 14, 8, 14));

        GridBagConstraints gc = baseGc();

        gc.gridwidth = 2;
        p.add(localApiEnabled, gc); gc.gridy++;
        gc.gridwidth = 1;

        addRow(p, gc, "Port:",     localApiPort);
        addRow(p, gc, "Endpoint:", localApiEndpoint);

        addNote(p, gc, "The Node.js mock server receives tag data at\nlocalhost:<Port><Endpoint>.");
        return p;
    }

    private JPanel buildReaderTab() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(12, 14, 8, 14));

        GridBagConstraints gc = baseGc();

        addRow(p, gc, "Zebra TCP Port:",       zebraPort);
        addRow(p, gc, "Connection Timeout (s):", connectionTimeout);

        gc.gridwidth = 2;
        p.add(autoReconnect, gc); gc.gridy++;

        addNote(p, gc, "Auto-reconnect retries with back-off: 1s → 2s → 4s → 8s → 16s → 30s.\nOnly unexpected disconnects trigger reconnect. Connection events appear in the Log tab.");
        return p;
    }

    private JPanel buildDataTab() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(12, 14, 8, 14));

        GridBagConstraints gc = baseGc();

        addRow(p, gc, "EPC Format:",    epcFormat);
        addRow(p, gc, "Last N chars:",  epcLastN);

        // CSV dir row with browse button
        gc.gridx = 0; gc.anchor = GridBagConstraints.EAST;
        p.add(new JLabel("CSV Directory:"), gc);
        gc.gridx = 1; gc.anchor = GridBagConstraints.WEST;

        JPanel dirRow = new JPanel(new BorderLayout(4, 0));
        dirRow.add(csvDir, BorderLayout.CENTER);
        JButton browse = new JButton("...");
        browse.setPreferredSize(new Dimension(36, csvDir.getPreferredSize().height));
        browse.addActionListener(e -> {
            JFileChooser fc = new JFileChooser(csvDir.getText());
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                csvDir.setText(fc.getSelectedFile().getAbsolutePath());
            }
        });
        dirRow.add(browse, BorderLayout.EAST);
        p.add(dirRow, gc); gc.gridy++;

        addRow(p, gc, "Suppression Window (s):", suppressionSecs);

        addNote(p, gc, "LAST_N keeps only the last N hex characters of the EPC.\nFULL stores the complete EPC string.\nSuppression window: ignore re-reads of the same tag within N seconds.");
        return p;
    }

    // ------------------------------------------------------------------ //
    // Save handler
    // ------------------------------------------------------------------ //
    private void onSave(ActionEvent e) {
        if (!validateInputs()) return;

        AppConfig c = AppConfig.getInstance();

        c.setWiclaxEnabled(wiclaxEnabled.isSelected());
        c.setWiclaxUser(wiclaxUser.getText().trim());
        c.setWiclaxDeviceId(wiclaxDeviceId.getText().trim());
        c.setWiclaxUrl(wiclaxUrl.getText().trim());
        c.setWiclaxPort(Integer.parseInt(wiclaxPort.getText().trim()));

        c.setFeiBotEnabled(feiBotEnabled.isSelected());
        c.setFeiBotServerIp(feiBotServerIp.getText().trim());
        c.setFeiBotServerPort(Integer.parseInt(feiBotServerPort.getText().trim()));
        c.setFeiBotDeviceId(feiBotDeviceId.getText().trim());
        c.setFeiBotDeviceModel(feiBotDeviceModel.getText().trim());

        c.setRufusApiEnabled(rufusApiEnabled.isSelected());
        c.setRufusApiKey(rufusApiKey.getText().trim());
        c.setRufusApiSerialNumber(rufusApiSerialNumber.getText().trim());
        c.setRufusApiAlias(rufusApiAlias.getText().trim());
        c.setRufusApiDeviceId(rufusApiDeviceId.getText().trim());

        c.setLocalApiEnabled(localApiEnabled.isSelected());
        c.setLocalApiPort(Integer.parseInt(localApiPort.getText().trim()));
        c.setLocalApiEndpoint(localApiEndpoint.getText().trim());

        c.setZebraPort(Integer.parseInt(zebraPort.getText().trim()));
        c.setConnectionTimeout(Integer.parseInt(connectionTimeout.getText().trim()));
        c.setAutoReconnect(autoReconnect.isSelected());

        c.setEpcFormat((String) epcFormat.getSelectedItem());
        c.setEpcLastN((Integer) epcLastN.getValue());
        c.setCsvDir(csvDir.getText().trim());
        c.setSuppressionSecs((Integer) suppressionSecs.getValue());

        c.save();
        dispose();
    }

    // ------------------------------------------------------------------ //
    // Validation
    // ------------------------------------------------------------------ //
    private boolean validateInputs() {
        // Validate integer fields
        String[][] intFields = {
            { wiclaxPort.getText(),        "Wiclax Port"            },
            { feiBotServerPort.getText(),  "FeiBot Server Port"     },
            { localApiPort.getText(),      "Local API Port"         },
            { zebraPort.getText(),         "Zebra TCP Port"         },
            { connectionTimeout.getText(), "Connection Timeout"     }
        };
        for (String[] pair : intFields) {
            try {
                int v = Integer.parseInt(pair[0].trim());
                if (v < 1 || v > 65535) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                    pair[1] + " must be a valid port number (1–65535).",
                    "Invalid Input", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        if (wiclaxUser.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Wiclax User cannot be empty.",
                "Invalid Input", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (wiclaxDeviceId.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Wiclax Device ID cannot be empty.",
                "Invalid Input", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    // ------------------------------------------------------------------ //
    // Layout helpers
    // ------------------------------------------------------------------ //
    private GridBagConstraints baseGc() {
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets  = new Insets(4, 4, 4, 4);
        gc.fill    = GridBagConstraints.HORIZONTAL;
        gc.gridx   = 0;
        gc.gridy   = 0;
        gc.weightx = 0;
        return gc;
    }

    private void addRow(JPanel p, GridBagConstraints gc, String label, JComponent field) {
        gc.gridx = 0; gc.weightx = 0; gc.anchor = GridBagConstraints.EAST;
        p.add(new JLabel(label), gc);
        gc.gridx = 1; gc.weightx = 1; gc.anchor = GridBagConstraints.WEST;
        p.add(field, gc);
        gc.gridy++;
    }

    private void addNote(JPanel p, GridBagConstraints gc, String text) {
        JLabel note = new JLabel("<html><i>" + text.replace("\n", "<br>") + "</i></html>");
        note.setForeground(Color.GRAY);
        note.setFont(note.getFont().deriveFont(11f));
        gc.gridx = 0; gc.gridwidth = 2; gc.anchor = GridBagConstraints.WEST;
        gc.insets = new Insets(10, 4, 4, 4);
        p.add(note, gc);
        gc.gridy++;
        gc.gridwidth = 1;
        gc.insets = new Insets(4, 4, 4, 4);
    }
}
