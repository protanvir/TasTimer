import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.UUID;

/**
 * Modal dialog for managing timing points.
 *
 * Operators can add / edit / remove / reorder points, and connect or disconnect
 * the RFID reader assigned to each point.  Tag reads from connected readers
 * are routed back to the main GUI via the {@link RFIDDataListener} interface.
 *
 * Usage:
 *   new TimingPointsDialog(mainFrame).setVisible(true);
 *   // caller should rebuildPointTabs() after close
 */
public class TimingPointsDialog extends JDialog {

    // ------------------------------------------------------------------ //
    // State
    // ------------------------------------------------------------------ //
    private final TimingPointManager tpm;
    private final RFIDDataListener   guiListener;

    private DefaultTableModel tableModel;
    private JTable            table;
    private javax.swing.Timer refreshTimer;

    // Status column index (used for targeted cell updates)
    private static final int COL_STATUS = 4;

    // ================================================================== //
    // Constructor
    // ================================================================== //
    public TimingPointsDialog(JFrame owner) {
        super(owner, "Timing Points Configuration", true);
        this.tpm         = TimingPointManager.getInstance();
        this.guiListener = (owner instanceof RFIDDataListener)
                ? (RFIDDataListener) owner : null;

        buildUi();
        refreshTable();

        // Keep Status column live while dialog is open
        refreshTimer = new javax.swing.Timer(2000, e -> refreshStatusColumn());
        refreshTimer.start();

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { shutdown(); }
        });

        setSize(660, 430);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    }

    // ================================================================== //
    // UI construction
    // ================================================================== //
    private void buildUi() {
        setLayout(new BorderLayout(6, 6));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // ---- Points table ----
        String[] cols = {"#", "Name", "Reader IP", "Type", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setRowHeight(24);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        int[] widths = {30, 160, 140, 75, 95};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Colour-code the Status column
        table.getColumnModel().getColumn(COL_STATUS).setCellRenderer(
                new javax.swing.table.DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(
                            JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                        Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                        if (!sel) {
                            String s = v != null ? v.toString() : "";
                            if ("Connected".equals(s))    c.setForeground(new Color(0, 140, 0));
                            else if ("No IP".equals(s))  c.setForeground(Color.GRAY);
                            else                          c.setForeground(Color.RED);
                        }
                        return c;
                    }
                });

        add(new JScrollPane(table), BorderLayout.CENTER);

        // ---- Button column (right side) ----
        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.Y_AXIS));
        btnPanel.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 0));

        JButton addBtn        = wideBtn("Add");
        JButton editBtn       = wideBtn("Edit");
        JButton removeBtn     = wideBtn("Remove");
        JButton upBtn         = wideBtn("Move Up");
        JButton downBtn       = wideBtn("Move Down");
        JButton connectBtn    = wideBtn("Connect");
        JButton disconnectBtn = wideBtn("Disconnect");
        JButton connAllBtn    = wideBtn("Connect All");
        JButton discAllBtn    = wideBtn("Disconnect All");
        JButton closeBtn      = wideBtn("Close");

        addBtn.addActionListener(e        -> onAdd());
        editBtn.addActionListener(e       -> onEdit());
        removeBtn.addActionListener(e     -> onRemove());
        upBtn.addActionListener(e         -> onMove(true));
        downBtn.addActionListener(e       -> onMove(false));
        connectBtn.addActionListener(e    -> onConnect());
        disconnectBtn.addActionListener(e -> onDisconnect());
        connAllBtn.addActionListener(e    -> onConnectAll());
        discAllBtn.addActionListener(e    -> onDisconnectAll());
        closeBtn.addActionListener(e      -> shutdown());

        btnPanel.add(addBtn);
        btnPanel.add(Box.createVerticalStrut(3));
        btnPanel.add(editBtn);
        btnPanel.add(Box.createVerticalStrut(3));
        btnPanel.add(removeBtn);
        btnPanel.add(Box.createVerticalStrut(10));
        btnPanel.add(upBtn);
        btnPanel.add(Box.createVerticalStrut(3));
        btnPanel.add(downBtn);
        btnPanel.add(Box.createVerticalStrut(10));
        btnPanel.add(connectBtn);
        btnPanel.add(Box.createVerticalStrut(3));
        btnPanel.add(disconnectBtn);
        btnPanel.add(Box.createVerticalStrut(10));
        btnPanel.add(connAllBtn);
        btnPanel.add(Box.createVerticalStrut(3));
        btnPanel.add(discAllBtn);
        btnPanel.add(Box.createVerticalGlue());
        btnPanel.add(closeBtn);

        add(btnPanel, BorderLayout.EAST);
    }

    private JButton wideBtn(String text) {
        JButton b = new JButton(text);
        b.setMaximumSize(new Dimension(120, 28));
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        return b;
    }

    // ================================================================== //
    // Table population
    // ================================================================== //

    /** Full rebuild — preserves the previously selected row by re-matching order. */
    private void refreshTable() {
        int selRow = table.getSelectedRow();

        tableModel.setRowCount(0);
        for (TimingPoint pt : tpm.getPointsSnapshot()) {
            tableModel.addRow(new Object[]{
                    pt.getOrder() + 1,
                    pt.getName(),
                    pt.getReaderIp(),
                    pt.getType().name(),
                    statusOf(pt)
            });
        }

        if (selRow >= 0 && selRow < tableModel.getRowCount())
            table.setRowSelectionInterval(selRow, selRow);
    }

    /** Lightweight update: only refresh the Status column values. */
    private void refreshStatusColumn() {
        List<TimingPoint> pts = tpm.getPointsSnapshot();
        for (int r = 0; r < tableModel.getRowCount() && r < pts.size(); r++) {
            tableModel.setValueAt(statusOf(pts.get(r)), r, COL_STATUS);
        }
    }

    private String statusOf(TimingPoint pt) {
        if (pt.getReaderIp().isBlank()) return "No IP";
        return tpm.isPointConnected(pt.getId()) ? "Connected" : "Disconnected";
    }

    // ================================================================== //
    // Button actions
    // ================================================================== //

    private void onAdd() {
        PointEditorResult res = showPointEditor("Add Timing Point",
                "", "", TimingPoint.PointType.FINISH);
        if (res == null) return;
        String id = UUID.randomUUID().toString().substring(0, 8);
        tpm.addPoint(new TimingPoint(id, res.name, res.readerIp, res.type, 0));
        tpm.saveToConfig();
        refreshTable();
    }

    private void onEdit() {
        TimingPoint pt = selectedPoint();
        if (pt == null) return;
        PointEditorResult res = showPointEditor("Edit Timing Point",
                pt.getName(), pt.getReaderIp(), pt.getType());
        if (res == null) return;
        tpm.updatePoint(pt.getId(), res.name, res.readerIp, res.type);
        tpm.saveToConfig();
        refreshTable();
    }

    private void onRemove() {
        TimingPoint pt = selectedPoint();
        if (pt == null) return;
        int ok = JOptionPane.showConfirmDialog(this,
                "Remove timing point \"" + pt.getName() + "\"?",
                "Confirm Remove", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        tpm.removePoint(pt.getId());
        tpm.saveToConfig();
        refreshTable();
    }

    private void onMove(boolean up) {
        TimingPoint pt = selectedPoint();
        if (pt == null) return;
        boolean moved = up ? tpm.moveUp(pt.getId()) : tpm.moveDown(pt.getId());
        if (moved) {
            tpm.saveToConfig();
            refreshTable();
        }
    }

    private void onConnect() {
        TimingPoint pt = selectedPoint();
        if (pt == null) return;
        if (pt.getReaderIp().isBlank()) {
            JOptionPane.showMessageDialog(this,
                    "No reader IP configured for \"" + pt.getName() + "\".",
                    "Cannot Connect", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (guiListener == null) return;

        int choice = JOptionPane.showOptionDialog(this,
                "Select reader type for \"" + pt.getName() + "\":",
                "Connect Reader",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                new String[]{"Impinj", "Zebra", "Cancel"}, "Impinj");
        if (choice < 0 || choice == 2) return;

        final ReaderInterface reader = (choice == 1)
                ? new ZebraReaderAdapter(guiListener)
                : new RFIDController(guiListener);
        final String pointId = pt.getId();

        new Thread(() -> {
            tpm.connectPoint(pointId, reader);
            // Brief pause to let the async connect complete before starting reads
            try { Thread.sleep(1200); } catch (InterruptedException ignored) {}
            if (reader.isConnected()) reader.startReading();
            SwingUtilities.invokeLater(this::refreshTable);
        }, "connect-" + pt.getName()).start();
    }

    private void onDisconnect() {
        TimingPoint pt = selectedPoint();
        if (pt == null) return;
        final String pointId = pt.getId();
        new Thread(() -> {
            tpm.disconnectPoint(pointId);
            SwingUtilities.invokeLater(this::refreshTable);
        }, "disconnect-" + pt.getName()).start();
    }

    private void onConnectAll() {
        if (guiListener == null) return;
        List<TimingPoint> points = tpm.getPointsSnapshot();
        if (points.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No timing points configured.",
                    "Nothing to Connect", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int choice = JOptionPane.showOptionDialog(this,
                "Select reader type for all timing points:",
                "Connect All",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                new String[]{"Impinj", "Zebra", "Cancel"}, "Impinj");
        if (choice < 0 || choice == 2) return;
        final boolean useZebra = (choice == 1);

        new Thread(() -> {
            for (TimingPoint pt : points) {
                if (pt.getReaderIp().isBlank()) continue;
                ReaderInterface r = useZebra
                        ? new ZebraReaderAdapter(guiListener)
                        : new RFIDController(guiListener);
                tpm.connectPoint(pt.getId(), r);
            }
            try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
            for (TimingPoint pt : points) {
                ReaderInterface r = tpm.getReader(pt.getId());
                if (r != null && r.isConnected()) r.startReading();
            }
            SwingUtilities.invokeLater(this::refreshTable);
        }, "connect-all").start();
    }

    private void onDisconnectAll() {
        new Thread(() -> {
            tpm.disconnectAll();
            SwingUtilities.invokeLater(this::refreshTable);
        }, "disconnect-all").start();
    }

    // ================================================================== //
    // Point editor sub-dialog
    // ================================================================== //

    private static class PointEditorResult {
        final String                name;
        final String                readerIp;
        final TimingPoint.PointType type;
        PointEditorResult(String n, String ip, TimingPoint.PointType t) {
            name = n; readerIp = ip; type = t;
        }
    }

    private PointEditorResult showPointEditor(String title,
                                               String initName,
                                               String initIp,
                                               TimingPoint.PointType initType) {
        JTextField nameField = new JTextField(initName, 22);
        JTextField ipField   = new JTextField(initIp,   22);
        JComboBox<TimingPoint.PointType> typeCombo =
                new JComboBox<>(TimingPoint.PointType.values());
        typeCombo.setSelectedItem(initType);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets  = new Insets(4, 4, 4, 4);
        gbc.anchor  = GridBagConstraints.WEST;
        gbc.fill    = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; form.add(new JLabel("Name:"),      gbc);
        gbc.gridx = 1;                form.add(nameField,                 gbc);
        gbc.gridx = 0; gbc.gridy = 1; form.add(new JLabel("Reader IP:"), gbc);
        gbc.gridx = 1;                form.add(ipField,                   gbc);
        gbc.gridx = 0; gbc.gridy = 2; form.add(new JLabel("Type:"),      gbc);
        gbc.gridx = 1;                form.add(typeCombo,                 gbc);

        int result = JOptionPane.showConfirmDialog(this, form, title,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return null;

        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name cannot be empty.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return new PointEditorResult(name, ipField.getText().trim(),
                (TimingPoint.PointType) typeCombo.getSelectedItem());
    }

    // ================================================================== //
    // Helpers
    // ================================================================== //

    /** Returns the TimingPoint for the currently selected table row, or null. */
    private TimingPoint selectedPoint() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a timing point first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        List<TimingPoint> pts = tpm.getPointsSnapshot();
        return (row < pts.size()) ? pts.get(row) : null;
    }

    private void shutdown() {
        if (refreshTimer != null) refreshTimer.stop();
        dispose();
    }
}
