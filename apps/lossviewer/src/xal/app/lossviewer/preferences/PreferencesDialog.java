package xal.app.lossviewer.preferences;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

public class PreferencesDialog extends JDialog {

    private static final long serialVersionUID = -2230551343747813873L;

    private ApplicationPreferences appPrefs;
    private JTable propertyTable;
    PropertyTableModel tableModel;

    private AcceleratorDocumentWithPreferences currentDocument;

    public PreferencesDialog(ApplicationPreferences appPrefs) {
        super((JFrame) null, "Preferences", true);
        this.appPrefs = appPrefs;

        tableModel = new PropertyTableModel(appPrefs);

        initGUI();

    }

    private void initGUI() {
        setLayout(new BorderLayout());
        JPanel tablePanel = new JPanel();
        tablePanel.setBorder(BorderFactory.createLineBorder(Color.red));
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createLineBorder(Color.green));

        propertyTable = new JTable();
        propertyTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JScrollPane scr = new JScrollPane(propertyTable);

        tablePanel.setLayout(new BorderLayout());
        tablePanel.add(scr, BorderLayout.CENTER);

        JButton updateButton = new JButton("Update Preferences");
        updateButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                updatePreferences();
            }

        });
        buttonPanel.add(updateButton);
        add(tablePanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(400, 400));
        pack();
    }

    private void updatePreferences() {
        appPrefs.updateUserPreferences(currentDocument.getDocumentPreferences());
    }

    public void showPreferenceDialog(AcceleratorDocumentWithPreferences doc) {
        this.currentDocument = doc;
        propertyTable.setModel(tableModel);
        invalidate();
        this.setVisible(true);
    }

    private class PropertyTableModel extends AbstractTableModel {
        private static final long serialVersionUID = 7700859055079961587L;

        private ApplicationPreferences appPrefs;

        private String[] keys;

        private final String[] colNames = new String[]{"Property", "Value"};

        private static final int VALUEINDEX = 1;
        private static final int KEYINDEX = 0;

        public PropertyTableModel(ApplicationPreferences ap) {
            this.appPrefs = ap;
            keys = appPrefs.getKeys(); //keySet().toArray(new String[]{});
        }
//		public void initializeModel(Map<String,Object> docPrefs) {
//
//			this.documentPrefs = docPrefs;
//
//		}

        public int getRowCount() {
            return keys.length;
        }

        public String getColumnName(int index) {
            return colNames[index];
        }

        public int getColumnCount() {

            return 2;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            String key = keys[rowIndex];
            if (columnIndex == 0) {
                return key;
            } else {
                Object value = currentDocument.get(key);
                if (!value.getClass().getName().equals("java.lang.String")) {
                    return ObjectConverter.convertObjectToString(value);
                }

                return value;
            }

        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == VALUEINDEX ? true : false;
        }

        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            String key = keys[rowIndex];
            Object newValue = ObjectConverter.createObjectFromString((String) aValue);
            Object currentValue = currentDocument.get(key);
            if (newValue != null && !newValue.equals(currentValue)) {
                currentDocument.put(key, newValue);
            }

        }

    }
}
