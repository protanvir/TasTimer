import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class TimingSystemGUI extends JFrame implements RFIDDataListener {

    // ------------------------------------------------------------------ //
    // Core state
    // ------------------------------------------------------------------ //
    private ReaderInterface           controller;
    private final ParticipantRegistry registry     = ParticipantRegistry.getInstance();
    private final RaceSession         raceSession  = new RaceSession();
    private final DuplicateFilter     dupFilter    = new DuplicateFilter();
    private final FeiBotClient        feiBotClient = new FeiBotClient();
    private final RufusAPIClient      rufusApiClient = new RufusAPIClient();
    private int                       waveCounter  = 1;  // for auto-generating wave IDs
    private final WebBridge           bridge       = new WebBridge();
    private WebServer                 server;

    // ------------------------------------------------------------------ //
    // Timing point manager + per-point tab models
    // ------------------------------------------------------------------ //
    private final TimingPointManager tpm = TimingPointManager.getInstance();
    /** Maps timing-point id → table model for that point's live-reads tab. */
    private final Map<String, DefaultTableModel> pointTableModels = new LinkedHashMap<>();
    /** The center tabbed pane (held as a field so tabs can be rebuilt). */
    private JTabbedPane centerTabs;

    // ------------------------------------------------------------------ //
    // Live reads table  (columns defined by COL_* constants below)
    // ------------------------------------------------------------------ //
    private DefaultTableModel tableModel;
    private JTable            tagTable;
    private final Set<String> uniqueTags = new HashSet<>();

    // Live reads column indices (only those accessed by name are declared)
    private static final int COL_BIB      = 1;
    private static final int COL_NAME     = 2;
    private static final int COL_EPC      = 3;
    private static final int COL_CATEGORY = 6;

    // ------------------------------------------------------------------ //
    // Participants tab
    // ------------------------------------------------------------------ //
    private DefaultTableModel           participantsTableModel;
    private JTable                      participantsTable;
    private JLabel                      participantStatsLabel;
    private TableRowSorter<DefaultTableModel> participantsSorter;

    // ------------------------------------------------------------------ //
    // Results tab
    // ------------------------------------------------------------------ //
    private DefaultTableModel resultsTableModel;
    private JTable            resultsTable;
    private JLabel            resultsStatsLabel;

    // Results column indices (only those accessed by name are declared)
    private static final int RES_PLACE = 0;

    // ------------------------------------------------------------------ //
    // Top bar
    // ------------------------------------------------------------------ //
    private JLabel clockLabel;
    private JLabel raceTimerLabel;
    private JLabel uniqueTagsLabel;

    // ------------------------------------------------------------------ //
    // Reader controls
    // ------------------------------------------------------------------ //
    private JTextField        ipField;
    private JComboBox<String> readerTypeCombo;
    private JButton           connectButton;
    private JButton           startButton;
    private JButton           stopButton;

    // ------------------------------------------------------------------ //
    // Race control widgets
    // ------------------------------------------------------------------ //
    private JLabel            raceStateLabel;
    private JButton           armButton;
    private JComboBox<Wave>   waveCombo;
    private JButton           fireGunButton;
    private JButton           finishRaceButton;
    private JButton           exportResultsButton;

    // ------------------------------------------------------------------ //
    // Connection log
    // ------------------------------------------------------------------ //
    private JTextArea connectionLogArea;
    private JPanel    connectionLogPanel; // built once, reused across tab rebuilds

    // ------------------------------------------------------------------ //
    // Status bar
    // ------------------------------------------------------------------ //
    private JLabel statusLabel;
    private JLabel suppressedLabel;

    // ------------------------------------------------------------------ //
    // Antenna indicators
    // ------------------------------------------------------------------ //
    private JPanel                antennaPanel;
    private Map<Integer, JPanel>  antennaIndicators      = new HashMap<>();
    private Map<Integer, Boolean> antennaConnectionState = new HashMap<>();
    private Map<Integer, Integer> antennaTagCounts       = new HashMap<>();
    private Map<Integer, JLabel>  antennaCountLabels     = new HashMap<>();

    // ------------------------------------------------------------------ //
    // Race timer
    // ------------------------------------------------------------------ //
    private Timer   timer;
    private long    raceStartTime = -1;
    private boolean isRaceRunning = false;

    // ================================================================== //
    // Constructor
    // ================================================================== //
    public TimingSystemGUI() {
        super("RFID Race Timing System");
        controller = new RFIDController(this);
        server = new WebServer(controller, bridge, AppConfig.getInstance(), raceSession, dupFilter);
        Thread.ofVirtual().start(server::start);

        // Load persisted timing points before building UI
        tpm.loadFromConfig();

        // Always start with one default wave
        raceSession.addWave(new Wave("W" + waveCounter, "Wave " + waveCounter));

        initComponents();
        ipField.setText(AppConfig.getInstance().getReaderIp());
        startClock();
        updateRaceControlState();

        setSize(980, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                AppConfig.getInstance().setReaderIp(ipField.getText());
                AppConfig.getInstance().save();
                feiBotClient.close();
            }
        });
    }

    // ================================================================== //
    // UI Construction
    // ================================================================== //
    private void initComponents() {
        setLayout(new BorderLayout());
        add(buildTopPanel(),     BorderLayout.NORTH);
        add(buildControlPanel(), BorderLayout.WEST);
        add(buildCenterTabs(),   BorderLayout.CENTER);
        add(buildStatusBar(),    BorderLayout.SOUTH);
    }

    // ------------------------------------------------------------------ //
    // Top bar — clock | unique tags | race timer
    // ------------------------------------------------------------------ //
    private JPanel buildTopPanel() {
        JPanel p = new JPanel(new GridLayout(1, 3));
        p.setBackground(Color.DARK_GRAY);

        clockLabel = label("00:00:00", Color.WHITE);
        uniqueTagsLabel = label("Unique Tags: 0", Color.CYAN);
        raceTimerLabel  = label("Race Time: 00:00:00", Color.YELLOW);

        p.add(clockLabel);
        p.add(uniqueTagsLabel);
        p.add(raceTimerLabel);
        return p;
    }

    private JLabel label(String text, Color fg) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(new Font("Monospaced", Font.BOLD, 22));
        l.setForeground(fg);
        return l;
    }

    // ------------------------------------------------------------------ //
    // Left control panel
    // ------------------------------------------------------------------ //
    private JPanel buildControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Controls"));
        panel.setPreferredSize(new Dimension(210, 0));

        // --- Reader section ---
        ipField         = new JTextField(AppConfig.DEF_IP);
        readerTypeCombo = new JComboBox<>(new String[]{"Impinj", "Zebra"});
        readerTypeCombo.setMaximumSize(new Dimension(190, 28));

        connectButton            = btn("Connect");
        JButton disconnectButton = btn("Disconnect");
        startButton              = btn("Start Reading");
        stopButton               = btn("Stop Reading");
        JButton clearButton      = btn("Clear Table");
        JButton resetFilterBtn   = btn("Reset Filter");
        JButton importBtn        = btn("Import Participants");
        JButton exportBtn        = btn("Export Status");
        JButton timingPointsBtn  = btn("Timing Points...");
        JButton settingsButton   = btn("Settings");

        startButton.setEnabled(false);
        stopButton.setEnabled(false);
        ipField.setMaximumSize(new Dimension(190, 28));

        connectButton.addActionListener(e -> {
            String type = (String) readerTypeCombo.getSelectedItem();
            if ("Zebra".equals(type)) {
                if (!(controller instanceof ZebraReaderAdapter))
                    controller = new ZebraReaderAdapter(TimingSystemGUI.this);
            } else {
                if (!(controller instanceof RFIDController))
                    controller = new RFIDController(TimingSystemGUI.this);
            }
            new Thread(() -> controller.connect(ipField.getText())).start();
        });
        disconnectButton.addActionListener(e ->
                new Thread(() -> controller.disconnect()).start());
        startButton.addActionListener(e -> {
            new Thread(() -> controller.startReading()).start();
        });
        stopButton.addActionListener(e ->
                new Thread(() -> controller.stopReading()).start());
        clearButton.addActionListener(e -> {
            tableModel.setRowCount(0);
            uniqueTags.clear();
            uniqueTagsLabel.setText("Unique Tags: 0");
        });
        resetFilterBtn.addActionListener(e -> {
            dupFilter.reset();
            updateSuppressedLabel();
        });
        importBtn.addActionListener(e -> onImportParticipants());
        exportBtn.addActionListener(e -> onExportStatus());
        timingPointsBtn.addActionListener(e -> {
            new TimingPointsDialog(TimingSystemGUI.this).setVisible(true);
            rebuildPointTabs();
        });
        settingsButton.addActionListener(e -> {
            new SettingsDialog(TimingSystemGUI.this, rufusApiClient).setVisible(true);
            ipField.setText(AppConfig.getInstance().getReaderIp());
        });

        panel.add(new JLabel("Reader Type:"));
        panel.add(readerTypeCombo);
        panel.add(Box.createVerticalStrut(3));
        panel.add(new JLabel("Reader IP:"));

        JPanel ipRow = new JPanel(new BorderLayout());
        ipRow.setMaximumSize(new Dimension(190, 28));
        ipRow.add(ipField, BorderLayout.CENTER);
        JButton kbBtn = new JButton("KB");
        kbBtn.setMargin(new Insets(0, 2, 0, 2));
        kbBtn.setPreferredSize(new Dimension(36, 28));
        kbBtn.addActionListener(e -> showVirtualKeypad());
        ipRow.add(kbBtn, BorderLayout.EAST);
        panel.add(ipRow);

        panel.add(Box.createVerticalStrut(5));
        panel.add(connectButton);
        panel.add(disconnectButton);
        panel.add(Box.createVerticalStrut(4));
        panel.add(startButton);
        panel.add(stopButton);
        panel.add(Box.createVerticalStrut(4));
        panel.add(clearButton);
        panel.add(resetFilterBtn);
        panel.add(Box.createVerticalStrut(4));
        panel.add(importBtn);
        panel.add(exportBtn);
        panel.add(Box.createVerticalStrut(4));
        panel.add(timingPointsBtn);
        panel.add(settingsButton);
        panel.add(Box.createVerticalStrut(8));

        // --- Race Control section ---
        panel.add(buildRaceControlSection());
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private JPanel buildRaceControlSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBorder(BorderFactory.createTitledBorder("Race Control"));
        section.setMaximumSize(new Dimension(210, 300));

        // State indicator
        raceStateLabel = new JLabel("State: IDLE", SwingConstants.CENTER);
        raceStateLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        raceStateLabel.setForeground(Color.GRAY);
        raceStateLabel.setMaximumSize(new Dimension(190, 20));
        raceStateLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        armButton = btn("Arm Race");
        armButton.setBackground(new Color(255, 200, 50));

        // Wave row: [combo] [Manage]
        waveCombo = new JComboBox<>();
        refreshWaveCombo();
        waveCombo.setMaximumSize(new Dimension(120, 28));

        JButton manageWavesBtn = new JButton("Waves...");
        manageWavesBtn.setMargin(new Insets(1, 4, 1, 4));
        manageWavesBtn.setPreferredSize(new Dimension(62, 28));
        manageWavesBtn.addActionListener(e -> showWaveManagerDialog());

        JPanel waveRow = new JPanel(new BorderLayout(3, 0));
        waveRow.setMaximumSize(new Dimension(190, 28));
        waveRow.add(waveCombo, BorderLayout.CENTER);
        waveRow.add(manageWavesBtn, BorderLayout.EAST);

        fireGunButton = btn("Fire Gun!");
        fireGunButton.setBackground(new Color(220, 50, 50));
        fireGunButton.setForeground(Color.WHITE);
        fireGunButton.setFont(fireGunButton.getFont().deriveFont(Font.BOLD));

        finishRaceButton   = btn("Finish Race");
        exportResultsButton = btn("Export Results");

        armButton.addActionListener(e -> {
            raceSession.arm();
            updateRaceControlState();
        });

        fireGunButton.addActionListener(e -> {
            Wave selected = (Wave) waveCombo.getSelectedItem();
            if (selected == null) return;
            if (selected.hasFired()) {
                JOptionPane.showMessageDialog(this,
                        selected.getName() + " has already been fired.",
                        "Already Fired", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Fire gun for " + selected.getName() + "?",
                    "Confirm Gun Start", JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) return;

            boolean fired = raceSession.fireWave(selected.getId());
            if (fired) {
                // Sync the GUI race timer to the gun time
                raceStartTime = selected.getGunTimeMs();
                isRaceRunning = true;
                refreshWaveCombo();
                updateRaceControlState();
            }
        });

        finishRaceButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Lock the race results? No new finishes will be recorded.",
                    "Finish Race", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;
            raceSession.finish();
            isRaceRunning = false;
            updateRaceControlState();
        });

        exportResultsButton.addActionListener(e -> onExportResults());

        section.add(raceStateLabel);
        section.add(Box.createVerticalStrut(4));
        section.add(armButton);
        section.add(Box.createVerticalStrut(3));
        section.add(new JLabel("Wave:"));
        section.add(waveRow);
        section.add(Box.createVerticalStrut(3));
        section.add(fireGunButton);
        section.add(Box.createVerticalStrut(3));
        section.add(finishRaceButton);
        section.add(Box.createVerticalStrut(3));
        section.add(exportResultsButton);

        return section;
    }

    /** Repopulate the wave combo from raceSession.getWaves(). */
    private void refreshWaveCombo() {
        Wave selected = (Wave) waveCombo.getSelectedItem();
        waveCombo.removeAllItems();
        for (Wave w : raceSession.getWaves()) {
            waveCombo.addItem(w);
        }
        if (selected != null) {
            for (int i = 0; i < waveCombo.getItemCount(); i++) {
                if (waveCombo.getItemAt(i).getId().equals(selected.getId())) {
                    waveCombo.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    /** Enables/disables race control buttons based on the current session state. */
    private void updateRaceControlState() {
        RaceSession.State s = raceSession.getState();

        armButton.setEnabled(s == RaceSession.State.IDLE);

        boolean canFire = s == RaceSession.State.ARMED || s == RaceSession.State.RUNNING;
        fireGunButton.setEnabled(canFire);

        finishRaceButton.setEnabled(s == RaceSession.State.RUNNING);
        exportResultsButton.setEnabled(
                s == RaceSession.State.RUNNING || s == RaceSession.State.FINISHED);

        switch (s) {
            case IDLE:
                raceStateLabel.setText("State: IDLE");
                raceStateLabel.setForeground(Color.GRAY);
                break;
            case ARMED:
                raceStateLabel.setText("State: ARMED");
                raceStateLabel.setForeground(new Color(200, 130, 0));
                break;
            case RUNNING:
                raceStateLabel.setText("State: RUNNING");
                raceStateLabel.setForeground(new Color(0, 150, 0));
                break;
            case FINISHED:
                raceStateLabel.setText("State: FINISHED");
                raceStateLabel.setForeground(new Color(0, 80, 180));
                isRaceRunning = false;
                break;
        }
    }

    // ------------------------------------------------------------------ //
    // Center tabbed pane
    // ------------------------------------------------------------------ //
    private JTabbedPane buildCenterTabs() {
        centerTabs = new JTabbedPane();
        centerTabs.addTab("Live Reads", buildLiveReadsPanel());
        // Insert a tab for each configured timing point
        for (TimingPoint pt : tpm.getPointsSnapshot()) {
            centerTabs.addTab(pt.getName(), buildPointReadsPanel(pt.getId()));
        }
        centerTabs.addTab("Participants", buildParticipantsPanel());
        centerTabs.addTab("Results",      buildResultsPanel());
        centerTabs.addTab("Log",          getOrBuildConnectionLogPanel());
        return centerTabs;
    }

    /**
     * Builds a live-reads panel for a specific timing point and registers
     * its table model in {@link #pointTableModels}.
     */
    private JPanel buildPointReadsPanel(String pointId) {
        String[] cols = {"#", "Bib", "Name", "EPC", "Timestamp", "Antenna", "Category"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        pointTableModels.put(pointId, model);

        JTable tbl = new JTable(model);
        tbl.setFillsViewportHeight(true);
        tbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        tbl.setRowHeight(24);
        int[] widths = {40, 55, 140, 100, 160, 55, 110};
        for (int i = 0; i < widths.length; i++)
            tbl.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        TagTableRenderer renderer = new TagTableRenderer();
        for (int i = 0; i < cols.length; i++)
            tbl.getColumnModel().getColumn(i).setCellRenderer(renderer);

        JPanel p = new JPanel(new BorderLayout());
        p.add(new JScrollPane(tbl));
        return p;
    }

    /**
     * Rebuilds the timing-point tabs in {@link #centerTabs} after the point
     * configuration changes (add / remove / rename).
     * Preserves the Participants and Results tabs at the end.
     */
    private void rebuildPointTabs() {
        if (centerTabs == null) return;

        // Identify fixed tabs to keep: Live Reads at index 0, Participants and Results at the end
        // Remove everything except index 0 (Live Reads)
        while (centerTabs.getTabCount() > 1) {
            centerTabs.removeTabAt(1);
        }

        // Clear old point models
        pointTableModels.clear();

        // Re-add one tab per timing point
        for (TimingPoint pt : tpm.getPointsSnapshot()) {
            centerTabs.addTab(pt.getName(), buildPointReadsPanel(pt.getId()));
        }

        // Re-add Participants, Results and Log
        centerTabs.addTab("Participants", buildParticipantsPanel());
        centerTabs.addTab("Results",      buildResultsPanel());
        centerTabs.addTab("Log",          getOrBuildConnectionLogPanel());
    }

    // --- Connection Log panel (lazy singleton) ---
    private JPanel getOrBuildConnectionLogPanel() {
        if (connectionLogPanel != null) return connectionLogPanel;

        connectionLogArea = new JTextArea();
        connectionLogArea.setEditable(false);
        connectionLogArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        connectionLogArea.setMargin(new Insets(4, 6, 4, 6));

        JScrollPane scroll = new JScrollPane(connectionLogArea);

        JButton clearBtn = new JButton("Clear Log");
        clearBtn.addActionListener(e -> connectionLogArea.setText(""));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        btnRow.add(clearBtn);

        connectionLogPanel = new JPanel(new BorderLayout());
        connectionLogPanel.add(scroll,  BorderLayout.CENTER);
        connectionLogPanel.add(btnRow,  BorderLayout.SOUTH);
        return connectionLogPanel;
    }

    // --- Live Reads ---
    private JPanel buildLiveReadsPanel() {
        String[] cols = {"#", "Bib", "Name", "EPC", "Timestamp", "Antenna", "Category"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tagTable = new JTable(tableModel);
        tagTable.setFillsViewportHeight(true);
        tagTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        tagTable.setRowHeight(24);

        int[] widths = {40, 55, 140, 100, 160, 55, 110};
        for (int i = 0; i < widths.length; i++)
            tagTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        TagTableRenderer renderer = new TagTableRenderer();
        for (int i = 0; i < cols.length; i++)
            tagTable.getColumnModel().getColumn(i).setCellRenderer(renderer);

        tagTable.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e)  { if (e.isPopupTrigger()) showTagContextMenu(e); }
            public void mouseReleased(MouseEvent e) { if (e.isPopupTrigger()) showTagContextMenu(e); }
        });

        JPanel p = new JPanel(new BorderLayout());
        p.add(new JScrollPane(tagTable));
        return p;
    }

    // --- Participants ---
    private JPanel buildParticipantsPanel() {
        participantStatsLabel = new JLabel("No participants loaded.");
        participantStatsLabel.setBorder(BorderFactory.createEmptyBorder(4, 6, 2, 6));
        participantStatsLabel.setFont(new Font("SansSerif", Font.BOLD, 13));

        JTextField searchField = new JTextField();
        searchField.setToolTipText("Search by bib, name, or EPC");

        JPanel searchRow = new JPanel(new BorderLayout(6, 0));
        searchRow.setBorder(BorderFactory.createEmptyBorder(2, 6, 4, 6));
        searchRow.add(new JLabel("Search: "), BorderLayout.WEST);
        searchRow.add(searchField, BorderLayout.CENTER);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.add(participantStatsLabel, BorderLayout.NORTH);
        topBar.add(searchRow, BorderLayout.SOUTH);

        String[] cols = {"Bib", "Name", "Category", "Wave", "EPC", "Status", "Last Read"};
        participantsTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        participantsTable = new JTable(participantsTableModel);
        participantsTable.setFillsViewportHeight(true);
        participantsTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        participantsTable.setRowHeight(24);

        int[] widths = {55, 160, 120, 60, 100, 70, 160};
        for (int i = 0; i < widths.length; i++)
            participantsTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Status column: green = Read, amber = Waiting
        participantsTable.getColumnModel().getColumn(5).setCellRenderer(
                new DefaultTableCellRenderer() {
                    @Override public Component getTableCellRendererComponent(
                            JTable t, Object v, boolean sel, boolean focus, int row, int col) {
                        Component c = super.getTableCellRendererComponent(t, v, sel, focus, row, col);
                        if (!sel) {
                            boolean read = "Read".equals(v);
                            c.setBackground(read ? new Color(198, 239, 206) : new Color(255, 235, 156));
                            c.setForeground(read ? new Color(0, 97, 0) : new Color(156, 87, 0));
                        }
                        return c;
                    }
                });

        participantsSorter = new TableRowSorter<>(participantsTableModel);
        participantsTable.setRowSorter(participantsSorter);
        participantsTable.getRowSorter().toggleSortOrder(0);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { filterParticipants(searchField.getText()); }
            public void removeUpdate(DocumentEvent e)  { filterParticipants(searchField.getText()); }
            public void changedUpdate(DocumentEvent e) { filterParticipants(searchField.getText()); }
        });

        JPanel p = new JPanel(new BorderLayout());
        p.add(topBar, BorderLayout.NORTH);
        p.add(new JScrollPane(participantsTable), BorderLayout.CENTER);
        return p;
    }

    // --- Results ---
    private JPanel buildResultsPanel() {
        resultsStatsLabel = new JLabel("No results yet. Arm the race and fire the gun to begin.");
        resultsStatsLabel.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        resultsStatsLabel.setFont(new Font("SansSerif", Font.BOLD, 13));

        String[] cols = {"Place", "Bib", "Name", "Category", "Wave",
                         "Gun Start", "Finish Time", "Net Time"};
        resultsTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        resultsTable = new JTable(resultsTableModel);
        resultsTable.setFillsViewportHeight(true);
        resultsTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        resultsTable.setRowHeight(24);

        int[] widths = {50, 55, 150, 120, 70, 100, 160, 100};
        for (int i = 0; i < widths.length; i++)
            resultsTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Alternate row shading + bold for top 3
        resultsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, focus, row, col);
                if (!sel) {
                    Object placeVal = t.getModel().getValueAt(row, RES_PLACE);
                    int place = placeVal instanceof Integer ? (Integer) placeVal : -1;
                    if (place == 1) {
                        c.setBackground(new Color(255, 215, 0, 120));  // gold tint
                        c.setFont(c.getFont().deriveFont(Font.BOLD));
                    } else if (place == 2) {
                        c.setBackground(new Color(192, 192, 192, 120)); // silver
                        c.setFont(c.getFont().deriveFont(Font.BOLD));
                    } else if (place == 3) {
                        c.setBackground(new Color(205, 127, 50, 120));  // bronze
                        c.setFont(c.getFont().deriveFont(Font.BOLD));
                    } else {
                        c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 245, 245));
                        c.setFont(c.getFont().deriveFont(Font.PLAIN));
                    }
                }
                return c;
            }
        });

        JPanel p = new JPanel(new BorderLayout());
        p.add(resultsStatsLabel, BorderLayout.NORTH);
        p.add(new JScrollPane(resultsTable), BorderLayout.CENTER);
        return p;
    }

    // ------------------------------------------------------------------ //
    // Status bar
    // ------------------------------------------------------------------ //
    private JPanel buildStatusBar() {
        statusLabel = new JLabel("Ready");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        suppressedLabel = new JLabel("Suppressed: 0");
        suppressedLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        suppressedLabel.setForeground(new Color(180, 100, 0));
        updateSuppressedLabel();

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.add(statusLabel);
        left.add(suppressedLabel);

        antennaPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        for (int i = 1; i <= 4; i++) {
            JPanel ind = createAntennaIndicator(i);
            antennaIndicators.put(i, ind);
            antennaConnectionState.put(i, false);
            antennaTagCounts.put(i, 0);
            antennaPanel.add(ind);
        }

        JPanel bar = new JPanel(new BorderLayout());
        bar.add(left, BorderLayout.WEST);
        bar.add(antennaPanel, BorderLayout.EAST);
        bar.setBorder(BorderFactory.createEtchedBorder());
        return bar;
    }

    // ------------------------------------------------------------------ //
    // Helpers: antenna
    // ------------------------------------------------------------------ //
    private JPanel createAntennaIndicator(int id) {
        JPanel p = new JPanel();
        p.setPreferredSize(new Dimension(60, 25));
        p.setBackground(Color.LIGHT_GRAY);
        p.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        p.setToolTipText("Antenna " + id);
        JLabel l = new JLabel(id + ": 0");
        l.setForeground(Color.BLACK);
        l.setFont(new Font("SansSerif", Font.BOLD, 10));
        antennaCountLabels.put(id, l);
        p.add(l);
        return p;
    }

    private void updateAntennaColor(int port) {
        JPanel ind = antennaIndicators.get(port);
        if (ind == null) return;
        ind.setBackground(antennaConnectionState.getOrDefault(port, false)
                ? new Color(0, 200, 0) : Color.LIGHT_GRAY);
    }

    private void blinkAntenna(int port) {
        JPanel ind = antennaIndicators.get(port);
        if (ind == null) return;
        ind.setBackground(Color.YELLOW);
        javax.swing.Timer t = new javax.swing.Timer(150, e -> updateAntennaColor(port));
        t.setRepeats(false);
        t.start();
    }

    // ------------------------------------------------------------------ //
    // Clock
    // ------------------------------------------------------------------ //
    private void startClock() {
        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override public void run() {
                SwingUtilities.invokeLater(() -> {
                    clockLabel.setText(LocalDateTime.now()
                            .format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                    if (isRaceRunning && raceStartTime > 0) {
                        long el = System.currentTimeMillis() - raceStartTime;
                        raceTimerLabel.setText(String.format("Race Time: %02d:%02d:%02d",
                                el / 3_600_000, (el / 60_000) % 60, (el / 1000) % 60));
                        bridge.broadcastRaceTimer(el);
                    }
                });
            }
        }, 0, 1000);

        // FeiBot heartbeat — every 5 seconds, daemon timer so it doesn't block shutdown
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override public void run() {
                feiBotClient.sendHeartbeat(buildReaderStatusMap());
            }
        }, 0, 5000);
    }

    private String getLocalIpAddress() {
        try { return InetAddress.getLocalHost().getHostAddress(); }
        catch (UnknownHostException e) { return "Unknown"; }
    }

    // ================================================================== //
    // RFIDDataListener
    // ================================================================== //
    @Override
    public void onTagRead(String rawEpc, String timestamp, String readerIp, int antennaPort) {
        long readTimeMs = System.currentTimeMillis();
        String epc = AppConfig.getInstance().formatEpc(rawEpc);

        // Gate 1: duplicate suppression
        if (!dupFilter.shouldAccept(epc, readerIp)) {
            SwingUtilities.invokeLater(this::updateSuppressedLabel);
            return;
        }

        // Participant lookup (try raw then formatted for LAST_N mode)
        Participant p = registry.findByEpc(rawEpc);
        if (p == null && !rawEpc.equalsIgnoreCase(epc)) p = registry.findByEpc(epc);

        String bib      = p != null ? p.getBibNumber() : "";
        String name     = p != null ? p.getFullName()  : "";
        String category = p != null ? p.getCategory()  : "";

        if (p != null) registry.markRead(rawEpc.toUpperCase(), timestamp);

        // Gate 2: route to timing point and record session data
        TimingPoint point = tpm.findByIp(readerIp);
        final boolean isNewFinish;
        final DefaultTableModel targetModel;

        if (point != null) {
            // Route to the point-specific tab model
            DefaultTableModel pm = pointTableModels.get(point.getId());
            targetModel = pm != null ? pm : tableModel;

            if (!bib.isEmpty()) {
                if (point.getType() == TimingPoint.PointType.FINISH) {
                    isNewFinish = raceSession.recordFinish(bib, readTimeMs, timestamp);
                } else {
                    // START or SPLIT — record crossing, refresh results for splits too
                    raceSession.recordCrossing(bib, point.getId(), readTimeMs, timestamp);
                    isNewFinish = false;
                }
            } else {
                isNewFinish = false;
            }
        } else {
            // No configured point for this IP — fall back to Live Reads tab
            targetModel = tableModel;
            isNewFinish = !bib.isEmpty()
                    && raceSession.recordFinish(bib, readTimeMs, timestamp);
        }

        // Data pipeline
        sendToLocalApi(epc, timestamp);
        sendToWiclax(epc);
        sendToFeiBot(epc, antennaPort);
        sendToRufusAPI(epc, timestamp);
        saveToCsv(epc, timestamp);

        SwingUtilities.invokeLater(() -> {
            int count = targetModel.getRowCount() + 1;
            targetModel.insertRow(0, new Object[]{count, bib, name, epc,
                    timestamp, antennaPort, category});

            if (uniqueTags.add(epc))
                uniqueTagsLabel.setText("Unique Tags: " + uniqueTags.size());

            int cur = antennaTagCounts.getOrDefault(antennaPort, 0);
            antennaTagCounts.put(antennaPort, cur + 1);
            JLabel lbl = antennaCountLabels.get(antennaPort);
            if (lbl != null) lbl.setText(antennaPort + ": " + (cur + 1));

            blinkAntenna(antennaPort);

            if (!registry.isEmpty()) refreshParticipantsPanel();
            if (isNewFinish)         refreshResultsPanel();
        });
        bridge.onTagRead(epc, timestamp, readerIp, antennaPort);
    }

    @Override
    public void onAntennaStatus(List<UnifiedAntennaStatus> statuses, String readerIp) {
        SwingUtilities.invokeLater(() -> {
            for (UnifiedAntennaStatus s : statuses) {
                antennaConnectionState.put(s.getPortNumber(), s.isConnected());
                updateAntennaColor(s.getPortNumber());
            }
        });
        bridge.onAntennaStatus(statuses, readerIp);
    }

    @Override
    public void onReaderStatus(String status, boolean isConnected, String readerIp) {
        SwingUtilities.invokeLater(() -> {
            // Find which timing point this reader belongs to (for label prefix)
            TimingPoint point = tpm.findByIp(readerIp);
            String prefix = (point != null) ? "[" + point.getName() + "] " : "";
            statusLabel.setText(prefix + status);

            // Only update connect/start/stop buttons if this is the single-reader in the
            // control panel (i.e. the reader whose IP matches ipField)
            String panelIp = ipField.getText().trim();
            if (readerIp.isBlank() || readerIp.equals(panelIp)) {
                if (isConnected) {
                    statusLabel.setForeground(new Color(0, 150, 0));
                    connectButton.setEnabled(false);
                    startButton.setEnabled(true);
                    stopButton.setEnabled(true);
                    ipField.setEditable(false);
                } else {
                    statusLabel.setForeground(Color.RED);
                    connectButton.setEnabled(true);
                    startButton.setEnabled(false);
                    stopButton.setEnabled(false);
                    ipField.setEditable(true);
                }
            } else {
                // Status from a timing-point reader — just colour the label
                statusLabel.setForeground(isConnected ? new Color(0, 150, 0) : Color.RED);
            }
        });
        bridge.onReaderStatus(status, isConnected, readerIp);
    }

    @Override
    public void onConnectionLog(String message, String readerIp) {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        TimingPoint point = tpm.findByIp(readerIp);
        String prefix = (point != null) ? "[" + point.getName() + "] " : "";
        String line = "[" + ts + "] " + prefix + message + "\n";
        SwingUtilities.invokeLater(() -> {
            if (connectionLogArea != null) {
                connectionLogArea.append(line);
                connectionLogArea.setCaretPosition(connectionLogArea.getDocument().getLength());
            }
        });
    }

    // ================================================================== //
    // Panel refresh helpers
    // ================================================================== //
    private void refreshParticipantsPanel() {
        participantsTableModel.setRowCount(0);
        for (Participant pt : registry.getAll()) {
            participantsTableModel.addRow(new Object[]{
                    pt.getBibNumber(), pt.getFullName(), pt.getCategory(),
                    pt.getWaveId(), pt.getEpc(),
                    pt.hasBeenRead() ? "Read" : "Waiting",
                    pt.getLastReadTime()
            });
        }
        int total = registry.getTotalCount();
        int read  = registry.getReadCount();
        String pct = total > 0 ? String.format("  (%.0f%%)", 100.0 * read / total) : "";
        participantStatsLabel.setText("Athletes seen: " + read + " / " + total + pct);
    }

    private void refreshResultsPanel() {
        // Collect SPLIT-type points in course order
        List<TimingPoint> splitPoints = new ArrayList<>();
        for (TimingPoint pt : tpm.getPointsSnapshot()) {
            if (pt.getType() == TimingPoint.PointType.SPLIT) splitPoints.add(pt);
        }

        // Rebuild column headers dynamically (handles 0 or more splits)
        String[] headers = ResultsExporter.buildColumnHeaders(splitPoints);
        resultsTableModel.setColumnIdentifiers(headers);
        resultsTableModel.setRowCount(0);

        int n = splitPoints.size();
        List<Object[]> rows = ResultsExporter.buildRows(raceSession, registry, splitPoints);
        for (Object[] row : rows) {
            int placeVal = (row[0] instanceof Integer) ? (Integer) row[0] : -1;
            Object[] display = new Object[8 + n];
            display[0] = placeVal > 0 ? placeVal : "";
            for (int i = 1; i < 8 + n; i++) display[i] = row[i];
            resultsTableModel.addRow(display);
        }

        // Reapply column widths after dynamic rebuild
        int[] fixed = {50, 55, 150, 120, 70, 100};
        for (int i = 0; i < fixed.length; i++)
            resultsTable.getColumnModel().getColumn(i).setPreferredWidth(fixed[i]);
        for (int i = 0; i < n; i++)
            resultsTable.getColumnModel().getColumn(6 + i).setPreferredWidth(110);
        resultsTable.getColumnModel().getColumn(6 + n).setPreferredWidth(160);
        resultsTable.getColumnModel().getColumn(7 + n).setPreferredWidth(100);

        int finished = raceSession.getFinishCount();
        int total    = registry.getTotalCount();
        String stats = total > 0
                ? "Finishers: " + finished + " / " + total
                : "Finishers: " + finished;
        resultsStatsLabel.setText(stats + "   |   State: " + raceSession.getState());
    }

    private void filterParticipants(String text) {
        if (text == null || text.trim().isEmpty()) {
            participantsSorter.setRowFilter(null);
        } else {
            try {
                participantsSorter.setRowFilter(
                        RowFilter.regexFilter("(?i)" + text.trim(), 0, 1, 4));
            } catch (java.util.regex.PatternSyntaxException ignored) {}
        }
    }

    private void updateSuppressedLabel() {
        suppressedLabel.setText("Suppressed: " + dupFilter.getSuppressedCount());
        suppressedLabel.setToolTipText("Duplicate reads dropped within the "
                + AppConfig.getInstance().getSuppressionSecs()
                + "s window. Use 'Reset Filter' to clear.");
    }

    // ================================================================== //
    // Wave manager dialog (inline)
    // ================================================================== //
    private void showWaveManagerDialog() {
        JDialog dialog = new JDialog(this, "Manage Waves", true);
        dialog.setSize(400, 300);
        dialog.setLayout(new BorderLayout(6, 6));
        dialog.setLocationRelativeTo(this);

        // Table of waves
        String[] cols = {"ID", "Name", "Status"};
        DefaultTableModel waveModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable waveTable = new JTable(waveModel);
        waveTable.setRowHeight(22);
        populateWaveTable(waveModel);

        // Buttons
        JButton addBtn    = new JButton("Add Wave");
        JButton removeBtn = new JButton("Remove Selected");

        addBtn.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(dialog,
                    "Wave name:", "Add Wave", JOptionPane.PLAIN_MESSAGE);
            if (name == null || name.trim().isEmpty()) return;
            waveCounter++;
            Wave w = new Wave("W" + waveCounter, name.trim());
            raceSession.addWave(w);
            populateWaveTable(waveModel);
            refreshWaveCombo();
        });

        removeBtn.addActionListener(e -> {
            int row = waveTable.getSelectedRow();
            if (row < 0) return;
            String waveId = (String) waveModel.getValueAt(row, 0);
            boolean removed = raceSession.removeWave(waveId);
            if (!removed) {
                JOptionPane.showMessageDialog(dialog,
                        "Cannot remove a wave that has already fired.",
                        "Cannot Remove", JOptionPane.WARNING_MESSAGE);
                return;
            }
            populateWaveTable(waveModel);
            refreshWaveCombo();
        });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRow.add(addBtn);
        btnRow.add(removeBtn);
        btnRow.add(new JButton("Close") {{ addActionListener(e -> dialog.dispose()); }});

        dialog.add(new JScrollPane(waveTable), BorderLayout.CENTER);
        dialog.add(btnRow, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void populateWaveTable(DefaultTableModel model) {
        model.setRowCount(0);
        for (Wave w : raceSession.getWaves()) {
            model.addRow(new Object[]{
                    w.getId(), w.getName(),
                    w.hasFired() ? "Fired @ " + w.getFormattedGunTime() : "Waiting"
            });
        }
    }

    // ================================================================== //
    // Import / Export
    // ================================================================== //
    private void onImportParticipants() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Import Participants CSV");
        fc.setFileFilter(new FileNameExtensionFilter("CSV Files (*.csv)", "csv"));
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            registry.loadFromCsv(fc.getSelectedFile());
            refreshParticipantsPanel();
            tagTable.repaint();
            JOptionPane.showMessageDialog(this,
                    "Loaded " + registry.getTotalCount() + " participants.",
                    "Import Complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error reading file:\n" + ex.getMessage(),
                    "Import Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onExportStatus() {
        if (registry.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No participants loaded.",
                    "Nothing to Export", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Export Participant Status");
        fc.setSelectedFile(new File("participants_status.csv"));
        fc.setFileFilter(new FileNameExtensionFilter("CSV Files (*.csv)", "csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            registry.exportStatusCsv(fc.getSelectedFile());
            JOptionPane.showMessageDialog(this, "Exported successfully.",
                    "Export Complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error writing file:\n" + ex.getMessage(),
                    "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onExportResults() {
        if (raceSession.getFinishCount() == 0) {
            JOptionPane.showMessageDialog(this, "No finishers recorded yet.",
                    "Nothing to Export", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Export Results");
        fc.setSelectedFile(new File("results.csv"));
        fc.setFileFilter(new FileNameExtensionFilter("CSV Files (*.csv)", "csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            List<TimingPoint> splitPoints = new ArrayList<>();
            for (TimingPoint pt : tpm.getPointsSnapshot()) {
                if (pt.getType() == TimingPoint.PointType.SPLIT) splitPoints.add(pt);
            }
            ResultsExporter.exportCsv(fc.getSelectedFile(), raceSession, registry, splitPoints);
            JOptionPane.showMessageDialog(this, "Results exported to "
                    + fc.getSelectedFile().getName(),
                    "Export Complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error writing file:\n" + ex.getMessage(),
                    "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ================================================================== //
    // Right-click on live reads table
    // ================================================================== //
    private void showTagContextMenu(MouseEvent e) {
        int row = tagTable.rowAtPoint(e.getPoint());
        if (row < 0) return;
        tagTable.setRowSelectionInterval(row, row);
        String epc        = (String) tableModel.getValueAt(row, COL_EPC);
        String currentBib = (String) tableModel.getValueAt(row, COL_BIB);

        JPopupMenu menu = new JPopupMenu();
        String label = currentBib.isEmpty()
                ? "Assign Bib to EPC: " + epc
                : "Reassign Bib (currently: " + currentBib + ")";
        JMenuItem item = new JMenuItem(label);
        item.addActionListener(ev -> showAssignBibDialog(epc, row));
        menu.add(item);
        menu.show(tagTable, e.getX(), e.getY());
    }

    private void showAssignBibDialog(String epc, int tableRow) {
        String bib = (String) JOptionPane.showInputDialog(this,
                "Enter bib number for EPC: " + epc, "Assign Bib",
                JOptionPane.PLAIN_MESSAGE, null, null,
                tableModel.getValueAt(tableRow, COL_BIB));
        if (bib == null || bib.trim().isEmpty()) return;
        bib = bib.trim();

        Participant p = registry.findByBib(bib);
        if (p == null) {
            JOptionPane.showMessageDialog(this,
                    "Bib \"" + bib + "\" not found.\nImport a participants CSV first.",
                    "Bib Not Found", JOptionPane.WARNING_MESSAGE);
            return;
        }
        registry.assignEpc(bib, epc);
        final String finalBib = bib;
        SwingUtilities.invokeLater(() -> {
            tableModel.setValueAt(finalBib,        tableRow, COL_BIB);
            tableModel.setValueAt(p.getFullName(), tableRow, COL_NAME);
            tableModel.setValueAt(p.getCategory(), tableRow, COL_CATEGORY);
            refreshParticipantsPanel();
            tagTable.repaint();
        });
    }

    // ================================================================== //
    // Virtual keypad
    // ================================================================== //
    private void showVirtualKeypad() {
        JDialog dialog = new JDialog(this, "IP Keypad", true);
        dialog.setSize(300, 450);
        dialog.setLayout(new BorderLayout(5, 5));

        JTextField display = new JTextField(ipField.getText());
        display.setFont(new Font("Monospaced", Font.BOLD, 24));
        display.setHorizontalAlignment(JTextField.CENTER);
        dialog.add(display, BorderLayout.NORTH);

        JPanel keys = new JPanel(new GridLayout(4, 3, 5, 5));
        for (String lbl : new String[]{"7","8","9","4","5","6","1","2","3",".","0","Del"}) {
            JButton b = new JButton(lbl);
            b.setFont(new Font("SansSerif", Font.BOLD, 20));
            b.setFocusable(false);
            b.addActionListener(ev -> {
                if ("Del".equals(ev.getActionCommand())) {
                    String t = display.getText();
                    if (!t.isEmpty()) display.setText(t.substring(0, t.length() - 1));
                } else display.setText(display.getText() + ev.getActionCommand());
            });
            keys.add(b);
        }
        dialog.add(keys, BorderLayout.CENTER);

        JButton enter = new JButton("ENTER");
        enter.setFont(new Font("SansSerif", Font.BOLD, 20));
        enter.setBackground(new Color(50, 200, 50));
        enter.setForeground(Color.WHITE);
        enter.setPreferredSize(new Dimension(0, 60));
        enter.addActionListener(ev -> { ipField.setText(display.getText()); dialog.dispose(); });
        dialog.add(enter, BorderLayout.SOUTH);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // ================================================================== //
    // Data services
    // ================================================================== //
    private void sendToLocalApi(String epc, String timestamp) {
        AppConfig cfg = AppConfig.getInstance();
        if (!cfg.isLocalApiEnabled()) return;
        new Thread(() -> {
            try {
                String url = "http://localhost:" + cfg.getLocalApiPort() + cfg.getLocalApiEndpoint();
                HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                String json = "{\"epc\":\"" + epc + "\",\"timestamp\":\"" + timestamp + "\"}";
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(json.getBytes(StandardCharsets.UTF_8));
                }
                conn.getResponseCode();
            } catch (Exception e) {
                System.err.println("Local API Error: " + e.getMessage());
            }
        }).start();
    }

    private void sendToWiclax(String epc) {
        AppConfig cfg = AppConfig.getInstance();
        if (!cfg.isWiclaxEnabled()) return;
        new Thread(() -> {
            try {
                String ts = LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
                String query = "user=" + cfg.getWiclaxUser()
                        + "&deviceId=" + cfg.getWiclaxDeviceId()
                        + "&passings=@" + epc + "@" + ts;
                HttpURLConnection conn = (HttpURLConnection) URI.create(
                        cfg.getWiclaxUrl() + ":" + cfg.getWiclaxPort() + "?" + query
                ).toURL().openConnection();
                conn.setRequestMethod("GET");
                conn.getResponseCode();
            } catch (Exception e) {
                System.err.println("Wiclax Error: " + e.getMessage());
            }
        }).start();
    }

    private void sendToFeiBot(String epc, int antennaPort) {
        feiBotClient.sendDetection(epc, antennaPort);
    }

    private void sendToRufusAPI(String epc, String timestamp) {
        rufusApiClient.sendPassing(epc, timestamp);
    }

    /** Builds the reader_status map for FeiBot heartbeat packets. */
    private Map<String, String> buildReaderStatusMap() {
        Map<String, String> map = new LinkedHashMap<>();
        // Timing-point readers managed by TimingPointManager
        for (TimingPoint pt : tpm.getPointsSnapshot()) {
            ReaderInterface r = tpm.getReader(pt.getId());
            String state = (r != null && r.isConnected()) ? "reading" : "stopped";
            map.put(pt.getName(), state);
        }
        // Legacy single-reader from the control panel (if no timing points configured)
        if (map.isEmpty()) {
            String state = (controller != null && controller.isConnected()) ? "reading" : "stopped";
            map.put("reader_1", state);
        }
        return map;
    }

    private synchronized void saveToCsv(String epc, String timestamp) {
        AppConfig cfg = AppConfig.getInstance();
        try {
            File dir = new File(cfg.getCsvDir());
            if (!dir.exists()) dir.mkdirs();
            try (PrintWriter pw = new PrintWriter(
                    new FileWriter(new File(dir, "tag_reads.csv"), true))) {
                pw.println(epc + "," + timestamp);
            }
        } catch (IOException e) {
            System.err.println("CSV Error: " + e.getMessage());
        }
    }

    // ================================================================== //
    // Layout helper
    // ================================================================== //
    private JButton btn(String text) {
        JButton b = new JButton(text);
        b.setMaximumSize(new Dimension(190, 32));
        return b;
    }

    // ================================================================== //
    // Inner class: live reads row renderer
    // ================================================================== //
    private class TagTableRenderer extends DefaultTableCellRenderer {
        private static final Color UNKNOWN = new Color(255, 213, 170);
        private static final Color KNOWN   = Color.WHITE;
        private static final Color SEL     = new Color(184, 207, 229);
        private static final Color FINISHED_BG = new Color(198, 239, 206);

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean selected, boolean focus, int row, int col) {
            Component c = super.getTableCellRendererComponent(
                    table, value, selected, focus, row, col);
            if (!selected) {
                String epc = (String) table.getModel().getValueAt(row, COL_EPC);
                String bib = (String) table.getModel().getValueAt(row, COL_BIB);
                if (!bib.isEmpty() && raceSession.hasFinished(bib)) {
                    c.setBackground(FINISHED_BG); // green tint for confirmed finishers
                } else if (registry.findByEpc(epc) != null) {
                    c.setBackground(KNOWN);
                } else {
                    c.setBackground(UNKNOWN);     // orange for unknown tags
                }
            } else {
                c.setBackground(SEL);
            }
            return c;
        }
    }

    // ================================================================== //
    // Entry point
    // ================================================================== //
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> {
            TimingSystemGUI gui = new TimingSystemGUI(); // server starts inside constructor
            gui.setVisible(true);
        });
    }
}
