import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import com.impinj.octane.AntennaStatus;

public class TimingSystemGUI extends JFrame implements RFIDDataListener {

    private RFIDController controller;
    private JTextField ipField;
    private JLabel statusLabel;

    private JLabel clockLabel;
    private JLabel raceTimerLabel;
    private JLabel uniqueTagsLabel;
    private JTable tagTable;
    private Set<String> uniqueTags = new HashSet<>();
    private DefaultTableModel tableModel;
    private JButton connectButton;
    private JButton startButton;
    private JButton stopButton;

    // Antenna Indicators
    private JPanel antennaPanel;
    private Map<Integer, JPanel> antennaIndicators = new HashMap<>();
    private Map<Integer, Boolean> antennaConnectionState = new HashMap<>();

    // Timer
    private Timer timer;
    private long raceStartTime = -1;
    private boolean isRaceRunning = false;

    // Config
    private static final String CONFIG_FILE = "config.properties";

    public TimingSystemGUI() {
        super("RFID Race Timing System");
        controller = new RFIDController(this);
        initComponents();
        loadConfig();
        startClock();

        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Save config on close
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                saveConfig();
            }
        });
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // --- TOP PANEL: Clock & Timer ---
        JPanel topPanel = new JPanel(new GridLayout(1, 3));
        topPanel.setBackground(Color.DARK_GRAY);

        clockLabel = new JLabel("00:00:00", SwingConstants.CENTER);
        clockLabel.setFont(new Font("Monospaced", Font.BOLD, 24));
        clockLabel.setForeground(Color.WHITE);

        raceTimerLabel = new JLabel("Race Time: 00:00:00", SwingConstants.CENTER);
        raceTimerLabel.setFont(new Font("Monospaced", Font.BOLD, 24));
        raceTimerLabel.setForeground(Color.YELLOW);

        topPanel.add(clockLabel);

        uniqueTagsLabel = new JLabel("Unique Tags: 0", SwingConstants.CENTER);
        uniqueTagsLabel.setFont(new Font("Monospaced", Font.BOLD, 24));
        uniqueTagsLabel.setForeground(Color.CYAN);
        topPanel.add(uniqueTagsLabel);

        topPanel.add(raceTimerLabel);
        add(topPanel, BorderLayout.NORTH);

        // --- LEFT PANEL: Controls ---
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(BorderFactory.createTitledBorder("Controls"));
        controlPanel.setPreferredSize(new Dimension(200, 0));

        ipField = new JTextField("172.16.1.114");
        connectButton = new JButton("Connect");
        JButton disconnectButton = new JButton("Disconnect");
        startButton = new JButton("Start Reading");
        stopButton = new JButton("Stop Reading");
        JButton clearButton = new JButton("Clear Table");

        // Styling
        Dimension btnSize = new Dimension(180, 40);
        connectButton.setMaximumSize(btnSize);
        disconnectButton.setMaximumSize(btnSize);
        startButton.setMaximumSize(btnSize);
        stopButton.setMaximumSize(btnSize);
        clearButton.setMaximumSize(btnSize);
        ipField.setMaximumSize(new Dimension(180, 30));

        startButton.setEnabled(false);
        stopButton.setEnabled(false);

        // Actions
        connectButton.addActionListener(e -> new Thread(() -> controller.connect(ipField.getText())).start());
        disconnectButton.addActionListener(e -> new Thread(() -> controller.disconnect()).start());

        startButton.addActionListener(e -> {
            new Thread(() -> controller.startReading()).start();
            raceStartTime = System.currentTimeMillis();
            isRaceRunning = true;
        });

        stopButton.addActionListener(e -> {
            new Thread(() -> controller.stopReading()).start();
            isRaceRunning = false;
        });

        clearButton.addActionListener(e -> {
            tableModel.setRowCount(0);
            uniqueTags.clear();
            uniqueTagsLabel.setText("Unique Tags: 0");
        });

        controlPanel.add(new JLabel("Reader IP:"));
        controlPanel.add(ipField);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(connectButton);
        controlPanel.add(disconnectButton);
        controlPanel.add(Box.createVerticalStrut(20));
        controlPanel.add(startButton);
        controlPanel.add(stopButton);
        controlPanel.add(Box.createVerticalStrut(20));
        controlPanel.add(clearButton);
        controlPanel.add(Box.createVerticalGlue()); // Push to top

        add(controlPanel, BorderLayout.WEST);

        // --- CENTER: Tag Table ---
        String[] columns = { "Count", "EPC", "Timestamp", "Reader IP" };
        tableModel = new DefaultTableModel(columns, 0);
        tagTable = new JTable(tableModel);
        tagTable.setFillsViewportHeight(true);
        tagTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
        tagTable.setRowHeight(25);
        add(new JScrollPane(tagTable), BorderLayout.CENTER);

        // --- BOTTOM: Status & Antenna ---
        JPanel bottomPanel = new JPanel(new BorderLayout());

        statusLabel = new JLabel("Ready");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Antenna Indicators Panel
        antennaPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        for (int i = 1; i <= 4; i++) {
            JPanel indicator = createIndicator(i);
            antennaIndicators.put(i, indicator);
            antennaConnectionState.put(i, false); // Default disconnected
            antennaPanel.add(indicator);
        }

        bottomPanel.add(statusLabel, BorderLayout.WEST);
        bottomPanel.add(antennaPanel, BorderLayout.EAST);
        bottomPanel.setBorder(BorderFactory.createEtchedBorder());

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createIndicator(int id) {
        JPanel p = new JPanel();
        p.setPreferredSize(new Dimension(25, 25));
        p.setBackground(Color.LIGHT_GRAY);
        p.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        p.setToolTipText("Antenna " + id);
        JLabel l = new JLabel(String.valueOf(id));
        l.setForeground(Color.BLACK);
        p.add(l);
        return p;
    }

    private void startClock() {
        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    // Update Wall Clock
                    LocalDateTime now = LocalDateTime.now();
                    clockLabel.setText(now.format(DateTimeFormatter.ofPattern("HH:mm:ss")));

                    // Update Race Timer
                    if (isRaceRunning && raceStartTime != -1) {
                        long elapsed = System.currentTimeMillis() - raceStartTime;
                        long s = (elapsed / 1000) % 60;
                        long m = (elapsed / (1000 * 60)) % 60;
                        long h = (elapsed / (1000 * 60 * 60));
                        raceTimerLabel.setText(String.format("Race Time: %02d:%02d:%02d", h, m, s));
                    }
                });
            }
        }, 0, 1000);
    }

    private void loadConfig() {
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream(CONFIG_FILE)) {
            prop.load(input);
            if (prop.getProperty("ip") != null) {
                ipField.setText(prop.getProperty("ip"));
            }
        } catch (IOException ex) {
            // Ignore if file doesn't exist
        }
    }

    private void saveConfig() {
        Properties prop = new Properties();
        prop.setProperty("ip", ipField.getText());
        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            prop.store(output, null);
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    // --- RFIDDataListener Implementation ---

    @Override
    public void onTagRead(String epc, String timestamp, String readerIp, int antennaPort) {
        SwingUtilities.invokeLater(() -> {
            int count = tableModel.getRowCount() + 1;
            tableModel.insertRow(0, new Object[] { count, epc, timestamp, readerIp });

            // Update Unique Count
            if (uniqueTags.add(epc)) {
                uniqueTagsLabel.setText("Unique Tags: " + uniqueTags.size());
            }

            // Blink Antenna
            blinkAntenna(antennaPort);
        });
    }

    @Override
    public void onAntennaStatus(List<AntennaStatus> antennaStatuses) {
        SwingUtilities.invokeLater(() -> {
            for (AntennaStatus status : antennaStatuses) {
                int port = status.getPortNumber();
                // Assuming isConnected or state check.
                // Based on "antenna Status for the set of reader antennas", presence in list
                // usually implies connectivity or we check a property.
                // Researching AntennaStatus showed isConnected().
                boolean isConnected = status.isConnected();

                antennaConnectionState.put(port, isConnected);
                updateAntennaColor(port);
            }
        });
    }

    private void updateAntennaColor(int port) {
        JPanel indicator = antennaIndicators.get(port);
        if (indicator != null) {
            boolean connected = antennaConnectionState.getOrDefault(port, false);
            indicator.setBackground(connected ? new Color(0, 200, 0) : Color.LIGHT_GRAY); // Green or Grey
        }
    }

    private void blinkAntenna(int port) {
        JPanel indicator = antennaIndicators.get(port);
        if (indicator != null) {
            indicator.setBackground(Color.YELLOW);
            // Revert after 150ms
            javax.swing.Timer t = new javax.swing.Timer(150, e -> updateAntennaColor(port));
            t.setRepeats(false);
            t.start();
        }
    }

    @Override
    public void onReaderStatus(String status, boolean isConnected) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(status);
            if (isConnected) {
                statusLabel.setForeground(new Color(0, 150, 0)); // Dark Green
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
                isRaceRunning = false;
            }
        });
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> new TimingSystemGUI().setVisible(true));
    }
}
