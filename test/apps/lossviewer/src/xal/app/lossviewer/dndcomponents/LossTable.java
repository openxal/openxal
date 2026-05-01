package xal.app.lossviewer.dndcomponents;

import xal.extension.application.*;
import xal.app.lossviewer.*;
import xal.app.lossviewer.preferences.*;
import xal.app.lossviewer.views.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import xal.tools.data.DataAdaptor;
import xal.tools.data.DataListener;

public class LossTable extends JTable implements SelectionHandler<LossDetector>, DataListener {
    private static final long serialVersionUID = 3583273594890296074L;

    private String dataLabel = "LossTable";
    private int EDIT_MENU_INDEX = 1;

    public String dataLabel() {

        return dataLabel;
    }

    public void update(DataAdaptor adaptor) {
        List<DataAdaptor> das = adaptor.childAdaptors("Column");
        ArrayList<String> neCols = new ArrayList<String>();
        ArrayList<Integer> width = new ArrayList<Integer>();
        for (DataAdaptor da : das) {
            String col = da.stringValue("name");
            try {
                Integer w = da.intValue("width");
                if (allLabelNames.keySet().contains(col)) {
                    width.add(w);
                }
                neCols.add(col);
            } catch (NullPointerException npe) {
                //no width attributu=e
                neCols.add(col);
                width.add(-1);
            }

        }
        setSignalNames(neCols);

        int index = 0;
        for (Integer w : width) {
            if (w >= 0) {
                getColumnModel().getColumn(index).setPreferredWidth(w);
            }
            index++;
        }

    }

    public void write(DataAdaptor adaptor) {
        int index = 0;
        for (String col : visibleSignalNames) {
            DataAdaptor da = adaptor.createChild("Column");
            da.setValue("name", col);
            da.setValue("width", getColumnModel().getColumn(index).getWidth());
            index++;
        }

    }
    private LossView view;
    private ListSelectionModel selectionModel;
    protected DetectorTableModel model;
    private boolean eventPropagation = true;
    protected Map<String, String> allLabelNames = new HashMap<String, String>();
    protected Map<String, Set<String>> allLabelNormalizations = new HashMap<String, Set<String>>();
    protected Map<String, NumberFormat> allFormats = new HashMap<String, NumberFormat>();

    public LossTable(LossView v) {
        this(v, null);
    }

    public LossTable(LossView v, String label) {
        super();
        this.view = v;

        if (label != null) {
            this.dataLabel = label;
        }

        initilizeColumnNames();
        initializeModleSelectionAndMenu();

    }

    protected void initializeModleSelectionAndMenu() {
        //ugly!!!
        model = new DetectorTableModel(visibleSignalNames, allLabelNormalizations, allFormats, allLabelNames);

        setModel(model);
        initilizeSelectionModel();
        initializeMenu();
    }

    protected void initilizeColumnNames() {
        ApplicationPreferences apr = ((ApplicationWithPreferences) Application.getAdaptor()).getPreferences();
        Map<String, Object> labels = apr.getPreferencesFor(dataLabel + ".labels.");

        for (String e : labels.keySet()) {
            String labelName = e.substring(e.lastIndexOf('.') + 1, e.length());
            //		System.out.println(labelName + " " + labels.get(e));
            allLabelNames.put(labelName, (String) labels.get(e));
            String normPref = (String) apr.get(dataLabel + ".normalizations." + labelName);
            allLabelNormalizations.put(labelName, convertStringToNormalization(normPref));

            String formatPref = (String) apr.get(dataLabel + ".formats." + labelName);
            if (formatPref != null) {
                allFormats.put(labelName, new DecimalFormat(formatPref));
            }

        }
        allLabelNames.put("name", "Detector name");

    }

    private Set<String> convertStringToNormalization(String normPref) {

        if (normPref == null) {
            return null;
        }
        Set<String> result = new HashSet<String>();
        String s = normPref.trim();
        String[] tokens = s.split(";");
        for (String token : tokens) {
            result.add(token);
        }
        return result;
    }

    protected void initilizeSelectionModel() {
        view.addViewListener(model);
        view.addSelectionListener(this);
        setDragEnabled(true);
        setTransferHandler(new CollectionTransferHandler<LossDetector>());

        selectionModel = getSelectionModel();
        selectionModel.addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent event) {
                if (!((DefaultListSelectionModel) (event.getSource())).getValueIsAdjusting()) {
                    //	System.out.println(event);
                    if (eventPropagation) {
                        Set<LossDetector> selection = model.getSelectedRows(getSelectedRows());
                        if (selection != null) {
                            fireSelectionUpdate(new SelectionEvent<LossDetector>(selection, LossTable.this));
                        }
                    }

                }

            }
        });
    }
    private JPopupMenu tableMenu;
    private List<String> menuSignalNames = new ArrayList<String>();

    protected void initializeMenu() {

        visibleSignalNames.add("name");
        tableMenu = new JPopupMenu();


        final List<JMenuItem> menuItems = new ArrayList<JMenuItem>();

        for (String sName : allLabelNames.keySet()) {
            JCheckBoxMenuItem mi = new JCheckBoxMenuItem();
            mi.setText(allLabelNames.get(sName));
            if (visibleSignalNames.contains(sName)) {
                mi.setSelected(true);
            }

            mi.addItemListener(new ItemListener() {

                public void itemStateChanged(ItemEvent e) {
                    synchronized (isUpdatingFromColumn) {
                        if (!isUpdatingFromColumn) {
                            JCheckBoxMenuItem src = (JCheckBoxMenuItem) e.getSource();
                            int index = 0;
                            for (MenuElement me : tableMenu.getSubElements()) {
                                if (me == src) {
                                    updateColumnsFromMenu(index - standardEditingSize, src.isSelected());
                                    return;
                                }
                                index++;
                            }
                        }

                    }
                }
            });
            //  tableMenu.add(mi);
            menuItems.add(mi);
            menuSignalNames.add(sName);

        }
        MouseAdapter mouseAdapter = new MouseAdapter() {

            public void mousePressed(MouseEvent ev) {
                if (ev.isPopupTrigger()) {
                    if (!menuInitialized) {
                        addStandardEditing(menuItems);
                    }
                    tableMenu.show(ev.getComponent(), ev.getX(), ev.getY());

                }
            }

            public void mouseReleased(MouseEvent ev) {
                if (ev.isPopupTrigger()) {
                    if (!menuInitialized) {
                        addStandardEditing(menuItems);
                    }
                    tableMenu.show(ev.getComponent(), ev.getX(), ev.getY());

                }
            }
        };
        this.addMouseListener(mouseAdapter);
        this.getTableHeader().addMouseListener(mouseAdapter);
    }
    private int standardEditingSize = 3;

    private void addStandardEditing(List<JMenuItem> menuItems) {

        JMenuBar mb =  (view.getDocument().getMainWindow().getJMenuBar());
        JMenu menu = mb.getMenu(EDIT_MENU_INDEX);

        for (int i = 0; i < menu.getItemCount(); i++) {
            JMenuItem mi = menu.getItem(i);
            if (mi == null) {
                continue;
            }
            String text = mi.getText();
            if (text == null) {
                continue;
            }
            if (mi.getText().toLowerCase().equals("copy")) {
                JMenuItem newMI = new JMenuItem(mi.getAction());
                newMI.setText(mi.getText());
                tableMenu.add(newMI);

            }
            if (mi.getText().toLowerCase().equals("paste")) {
                JMenuItem newMI = new JMenuItem(mi.getAction());
                newMI.setText(mi.getText());
                tableMenu.add(newMI);

            }

        }
        Action deleteAction = new AbstractAction("Delete"){
            private static final long serialVersionUID = 4377386270269629176L;
            public void actionPerformed(ActionEvent e) {
                view.removeDetectors(getSelection());
            }

        };
        JMenuItem deleteMI = new JMenuItem(deleteAction);
        tableMenu.add(deleteMI);
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),"Delete");
        getActionMap().put("Delete", deleteAction);

        for (JMenuItem mi : menuItems) {
            tableMenu.add(mi);
        }
        tableMenu.addSeparator();
        for (JMenuItem mi : menuItems) {
            tableMenu.add(mi);
        }

        

        menuInitialized = true;
    }
    private boolean menuInitialized = false;

    private void updateColumnsFromMenu(int ind, boolean isAdding) {
        if (isAdding) {
            visibleSignalNames.add(menuSignalNames.get(ind));
        } else {
            visibleSignalNames.remove(menuSignalNames.get(ind));
        }
        setSignalNames(visibleSignalNames);
    }
    private Boolean isUpdatingFromColumn = false;

    private void updateMenuFromColumns() {
        synchronized (isUpdatingFromColumn) {
            isUpdatingFromColumn = true;
            if (tableMenu == null) {
                return;
            }
            int index = -standardEditingSize;
            for (MenuElement mi : tableMenu.getSubElements()) {
                if (index >= 0) {
                    if (visibleSignalNames.contains(menuSignalNames.get(index))) {

                        ((JCheckBoxMenuItem) mi).setSelected(true);
                    } else {
                        ((JCheckBoxMenuItem) mi).setSelected(false);
                    }
                    index++;
                }
            }
            isUpdatingFromColumn = false;
        }
    }

    public void setSignalNames(List<String> sNames) {
        visibleSignalNames = sNames;
        model.setVisibleSignalNames(visibleSignalNames);
        if (model != null) {
            model.fireTableStructureChanged();
            updateMenuFromColumns();
        }
    }

//SelectionHandler implementation
    public Collection<LossDetector> getSelection() {
        return view.getSelection();
    }

    public void setSelection(Collection<LossDetector> se) {
        eventPropagation = false;
        selectionModel.clearSelection();
        for (int i : model.getSelectedIndices(se)) {
            selectionModel.addSelectionInterval(i, i);
        }
        eventPropagation = true;

    }
    List<SelectionHandler<LossDetector>> selectionListeners = new ArrayList<SelectionHandler<LossDetector>>();

    public void addSelectionListener(SelectionHandler<LossDetector> s) {
        selectionListeners.add(s);
    }

    public void removeSelectionListener(SelectionHandler<LossDetector> s) {
        selectionListeners.remove(s);
    }

    public void removeAllSelectionListeners() {
        selectionListeners.clear();
    }

    public void fireSelectionUpdate(SelectionEvent<LossDetector> event) {
        for (SelectionHandler<LossDetector> e : selectionListeners) {
            e.processSelectionEvent(event);
        }
    }

    public void processSelectionEvent(SelectionEvent<LossDetector> event) {

        if (event.contains(this)) {
            return;
        }
        //	System.out.println(this+ " got seleEv");
        setSelection(event.getSelection());
        event.addProcessedHandler(this);
        fireSelectionUpdate(event);

    }

    public View<LossDetector> getRoot() {
        return view;

    }
    ////////////////////////////
    protected List<String> visibleSignalNames = new ArrayList<String>();
}
