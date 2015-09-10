/*
 * TemplateWindow.java
 *
 * Created on Fri Oct 10 15:12:03 EDT 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */
package xal.app.lossviewer;

import xal.extension.application.*;
import xal.app.lossviewer.views.*;
import xal.extension.application.smf.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.*;
import java.util.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.InternalFrameUI;
import javax.swing.plaf.basic.BasicInternalFrameUI;
import xal.app.lossviewer.preferences.ApplicationPreferences;
import xal.app.lossviewer.signals.NormalizationDevice;

/**
 * TemplateViewerWindow
 *
 * @author somebody
 */
public class LV2Window extends AcceleratorWindow implements SwingConstants {
    private static final long serialVersionUID = -6955984530557635002L;

    private Main application;
    private JDesktopPane desktop;
    private JTextField msgTextField;
    //private String currentLossSignal;
    //   private View view;

    public LV2Window(final XalDocument aDocument) {
        super(aDocument);
        application = (Main) Application.getAdaptor();
        desktop = new JDesktopPane();
        desktop.setBackground(new Color(96, 96, 96));
        desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
        this.add(desktop, BorderLayout.CENTER);
//		this.setLayout(null);
        initMenu();
        setSize(800, 600);

    }
//    private Map<String, SortedSet<LossDetector>> sequences;
//    private Map<String, SortedSet<LossDetector>> combos;

    public void setMsg(String msg) {
        if (msgTextField != null) {
            msgTextField.setText(msg);
        }
    }
    private List<String> detectorTypes = new ArrayList<String>(Arrays.asList(new String[]{"BLM", "ND"}));
    private List<String> filteredTypes = null;

    private void setIFrameTitle(View<LossDetector> view, JInternalFrame iframe) {
        String title = "";
        if ("Slow60".equals(view.getLossSignal())) {
            title = "1 Second";

        }
        if ("PulseLoss".equals(view.getLossSignal())) {
            title = "1 Pulse";

        }
        String refname = view.getReferenceName();
        if (refname == null || refname.equals("")) {
            refname = "";
        } else {
            refname = " #" + refname;
        }
        iframe.setTitle(view.getTitle() + " " + title + refname);
    }

    public void addView(final View<LossDetector> newView) {
        final JInternalFrame iframe = new JInternalFrame("", true, true, true, true);
        ;

        InternalFrameUI ui = iframe.getUI();
        if (!(ui instanceof BasicInternalFrameUI)) {
            return;
        }

        final JComponent titlePane = (((BasicInternalFrameUI) ui).getNorthPane());
        final JPopupMenu popup = new JPopupMenu();

        desktop.add(iframe);

        iframe.setBounds(newView.getParentBounds());
        iframe.add(newView.getView());

        filteredTypes = newView.getFilter();
//        if(filteredTypes==null){
//            filteredTypes=detectorTypes;
//        }

        newView.setFilter(filteredTypes);

        for (String type : detectorTypes) {
            final JCheckBoxMenuItem mi = new JCheckBoxMenuItem(type);
            mi.setAction(new AbstractAction(type) {
                private static final long serialVersionUID = 4377386270269629176L;

                public void actionPerformed(ActionEvent e) {
                    JCheckBoxMenuItem item = (JCheckBoxMenuItem) e.getSource();
                    if (item.isSelected()) {
                        filteredTypes.remove(item.getText());
                    } else {
                        if (!filteredTypes.contains(item.getText())) {
                            filteredTypes.add(item.getText());
                        }

                    }
                    newView.setFilter(filteredTypes);
                }
            });
            mi.setSelected(!filteredTypes.contains(type));
            popup.add(mi);
        }

        newView.setParentContainer(iframe);
        iframe.setVisible(true);

        iframe.addInternalFrameListener(new InternalFrameAdapter() {

            public void internalFrameClosed(InternalFrameEvent e) {
                ((LV2Document) document).removeView(newView);
            }
        });

        Action renameAction = new AbstractAction("Rename") {
            private static final long serialVersionUID = 4377386270269629176L;

            public void actionPerformed(ActionEvent e) {
                String oldTitle = newView.getTitle();
                String newTitle = (String) JOptionPane.showInputDialog(
                        iframe,
                        "New Title", "Rename",
                        JOptionPane.PLAIN_MESSAGE, null, null,
                        newView.getTitle());
                if (newTitle != null && !newTitle.equals(oldTitle)) {
                    newView.setTitle(newTitle);
                    //iframe.setTitle(newTitle);
                    setIFrameTitle(newView, iframe);
                }
            }
        };

        popup.add(new JMenuItem(renameAction));
        //      titlePane.add(popup);

        //     currentLossSignal = newView.getLossSignal();
        JRadioButtonMenuItem pulseLoss = new JRadioButtonMenuItem(new AbstractAction("1 Pulse") {
            private static final long serialVersionUID = 4377386270269629176L;

            public void actionPerformed(ActionEvent e) {
                newView.switchLossSignal("PulseLoss");
                //    switchSignal("Slow60");
                setIFrameTitle(newView, iframe);
            }
        });
        pulseLoss.setSelected(newView.getLossSignal().equals("PulseLoss"));

        JRadioButtonMenuItem slow60 = new JRadioButtonMenuItem(new AbstractAction("1 Second") {
            private static final long serialVersionUID = 4377386270269629176L;

            public void actionPerformed(ActionEvent e) {
                //   switchSignal("Slow60");
                newView.switchLossSignal("Slow60");
                setIFrameTitle(newView, iframe);
            }
        });
        slow60.setSelected(newView.getLossSignal().equals("Slow60"));

        ButtonGroup grp = new ButtonGroup();
        grp.add(pulseLoss);
        grp.add(slow60);

        popup.add(pulseLoss);
        popup.add(slow60);

        if (newView.getClass().getName().equals("xal.app.lossviewer.views.DefaultLossView")) {

            final JMenu referenceMenu = new JMenu("References");

            final AbstractAction a = new AbstractAction() {
                private static final long serialVersionUID = -5644390861803492172L;

                public void actionPerformed(ActionEvent ae) {
                    String s = ((JMenuItem) ae.getSource()).getText();
                    s.toString();
                    newView.switchToReference(s);
                    setIFrameTitle(newView, iframe);
                }
            };
            JMenuItem none = new JMenuItem(a);

            referenceMenu.addMenuListener(new MenuListener() {

                public void menuSelected(MenuEvent me) {

                    for (String s : ((LV2Document) document).getReferences().keySet()) {
                        JMenuItem mi = new JMenuItem(a);
                        mi.setText(s);
                        referenceMenu.add(mi);
                    }
                }

                public void menuDeselected(MenuEvent me) {
                    referenceMenu.removeAll();
                }

                public void menuCanceled(MenuEvent me) {
                    referenceMenu.removeAll();
                }
            });

            popup.add(referenceMenu);
        }

        setIFrameTitle(newView, iframe);

        if (titlePane != null) {
            titlePane.addMouseListener(new MouseAdapter() {

                public void mousePressed(MouseEvent me) {
                    if (me.isPopupTrigger()) {
                        popup.show(titlePane, me.getX(), me.getY());
                    }

                }

                public void mouseReleased(MouseEvent me) {
                    if (me.isPopupTrigger()) {
                        popup.show(titlePane, me.getX(), me.getY());
                    }
                }
            });
        }

    }
//    private void switchSignal(String lossig){
//        currentLossSignal = lossig;
//    }

    public void documentWillClose(XalDocument doc) {
        LV2Document document = (LV2Document) doc;
        document.removeViewAllViews();
        super.documentWillClose(doc);
    }

    private void initMenu() {
        JMenuBar mb = getJMenuBar();

        msgTextField = new JTextField();
        msgTextField.setEditable(false);
        msgTextField.setBackground(mb.getBackground());
        msgTextField.setBorder(null);
        msgTextField.setHorizontalAlignment(JTextField.RIGHT);

        mb.add(msgTextField);

        JMenu mainMenu, normMenu;
        int num = mb.getMenuCount();
        String menuLabel = (String) ((LV2Document) document).get("Mainmenu.label");
        String barLabel = (String) ((LV2Document) document).get("Barmenu.label");
        String mpsLabel = (String) ((LV2Document) document).get("MPSmenu.label");
        String sumLabel = (String) ((LV2Document) document).get("Summenu.label");
        String waterLabel = (String) ((LV2Document) document).get("Watermenu.label");
        String normalizationLabel = (String) ((LV2Document) document).get("Normalizationmenu.label");

        Action seqActionBar = new AbstractAction() {
            private static final long serialVersionUID = -5644390861803492172L;

            public void actionPerformed(ActionEvent e) {

                ((LV2Document) document).addDefaultView(((JMenuItem) e.getSource()).getText());
            }
        };

        Action seqActionMPS = new AbstractAction() {
            private static final long serialVersionUID = -5644390861803492172L;

            public void actionPerformed(ActionEvent e) {

                ((LV2Document) document).addView("MPSview", ((JMenuItem) e.getSource()).getText());
            }
        };

        Action seqActionSum = new AbstractAction() {
            private static final long serialVersionUID = -5644390861803492172L;

            public void actionPerformed(ActionEvent e) {

                ((LV2Document) document).addView("Sumview", ((JMenuItem) e.getSource()).getText());
            }
        };

        Action seqActionWater = new AbstractAction() {
            private static final long serialVersionUID = -5644390861803492172L;

            public void actionPerformed(ActionEvent e) {

                ((LV2Document) document).addView("Waterview", ((JMenuItem) e.getSource()).getText());
            }
        };

        for (int i = 0; i < num; i++) {
            JMenu mnu = mb.getMenu(i);
            if(mnu==null){
                break;              
            }
            String text = mnu.getText();
            
            
            if(text.equals(normalizationLabel)){
                normMenu=mb.getMenu(i);
                List<NormalizationDevice> bcms = ((Main)Application.getAdaptor()).getBCMs();
                ButtonGroup bg = new ButtonGroup();
                int index=0;
                for(final NormalizationDevice bcm : bcms){
                    JRadioButtonMenuItem rb = new JRadioButtonMenuItem(new AbstractAction(bcm.getName()){
                        private static final long serialVersionUID = 4377386270269629176L;
                        
                      ///  NormalizationDevice myBCM=bcm;
                        public void actionPerformed(ActionEvent e) {
                            changeNormalizationBCM(bcm);
                        }
                        
                    });
                    if(index==0){
                        rb.setSelected(true);
                        changeNormalizationBCM(bcm);
                    }
                    normMenu.add(rb);
                    bg.add(rb);
                    index++;
                    
                    
                }
                normMenu.setEnabled(true);
                
            }
            else if (text.equals(menuLabel)) {
                mainMenu = mb.getMenu(i);

                Map<String, SortedSet<LossDetector>> list;

                menuLabel = (String) (((LV2Document) document).get("Sequence.label"));
                list = application.getSequences();
                JMenu seqMenu = new JMenu(menuLabel);
                JMenu seqBarMenu = new JMenu(menuLabel);
                JMenu seqMenuMPS = new JMenu(menuLabel);
                JMenu seqMenuSum = new JMenu(menuLabel);
                JMenu seqMenuWater = new JMenu(menuLabel);

                menuLabel = (String) (((LV2Document) document).get("Combo.label"));
                JMenu comboMenu = new JMenu(menuLabel);

                JMenu comboMenuMPS = new JMenu(menuLabel);
                JMenu mpsMenu = new JMenu(mpsLabel);

                JMenu comboBarMenu = new JMenu(menuLabel);
                JMenu barMenu = new JMenu(barLabel);

                JMenu comboMenuSum = new JMenu(menuLabel);
                JMenu sumMenu = new JMenu(sumLabel);

                JMenu comboMenuWater = new JMenu(menuLabel);
                JMenu waterMenu = new JMenu(waterLabel);

                mainMenu.add(barMenu);

                mainMenu.add(mpsMenu);
                mainMenu.add(sumMenu);
                mainMenu.add(waterMenu);

                mpsMenu.add(seqMenuMPS);
                mpsMenu.add(comboMenuMPS);

                barMenu.add(seqBarMenu);
                barMenu.add(comboBarMenu);

                sumMenu.add(seqMenuSum);
                sumMenu.add(comboMenuSum);

                waterMenu.add(seqMenuWater);
                waterMenu.add(comboMenuWater);

                for (String sn : list.keySet()) {
                    JMenuItem item = new JMenuItem(seqActionBar);
                    item.setText(sn);
                    seqMenu.add(item);

                    item = new JMenuItem(seqActionMPS);
                    item.setText(sn);
                    seqMenuMPS.add(item);

                    item = new JMenuItem(seqActionBar);
                    item.setText(sn);
                    seqBarMenu.add(item);

                    item = new JMenuItem(seqActionSum);
                    item.setText(sn);
                    seqMenuSum.add(item);

                    item = new JMenuItem(seqActionWater);
                    item.setText(sn);
                    seqMenuWater.add(item);

                }

                Map<String, SortedSet<LossDetector>> clist = application.getCombos();

                for (String sn : clist.keySet()) {
                    JMenuItem item = new JMenuItem(seqActionBar);
                    item.setText(sn);
                    comboMenu.add(item);

                    item = new JMenuItem(seqActionMPS);
                    item.setText(sn);
                    comboMenuMPS.add(item);

                    item = new JMenuItem(seqActionBar);
                    item.setText(sn);
                    comboBarMenu.add(item);

                    item = new JMenuItem(seqActionSum);
                    item.setText(sn);
                    comboMenuSum.add(item);

                    item = new JMenuItem(seqActionWater);
                    item.setText(sn);
                    comboMenuWater.add(item);
                }

                Action emptyAction = new AbstractAction("Empty") {
                    private static final long serialVersionUID = 4377386270269629176L;

                    public void actionPerformed(ActionEvent e) {
                        ((LV2Document) document).addDefaultView(null);
                    }
                };

                Action emptyMPS = new AbstractAction("Empty") {
                    private static final long serialVersionUID = 4377386270269629176L;

                    public void actionPerformed(ActionEvent e) {

                        ((LV2Document) document).addView("MPSview", null);
                    }
                };

                Action emptySum = new AbstractAction("Empty") {
                    private static final long serialVersionUID = 4377386270269629176L;

                    public void actionPerformed(ActionEvent e) {

                        ((LV2Document) document).addView("Sumview", null);
                    }
                };

                Action emptyWater = new AbstractAction("Empty") {
                    private static final long serialVersionUID = 4377386270269629176L;

                    public void actionPerformed(ActionEvent e) {

                        ((LV2Document) document).addView("Waterview", null);
                    }
                };

                barMenu.add(new JMenuItem(emptyAction));
                mpsMenu.add(new JMenuItem(emptyMPS));
                sumMenu.add(new JMenuItem(emptySum));
                waterMenu.add(new JMenuItem(emptyWater));

                mainMenu.setEnabled(true);
                
            }
        }

    }

    private Map<String,String> getNormBCMs(){
        Map<String,String> bcms = new TreeMap<String,String>();
        ApplicationPreferences appPrefs = ((Main)Application.getAdaptor()).getPreferences();
        Map<String, Object> preferences = appPrefs.getPreferencesFor("BCM.signals.name");
        for(String prefname: preferences.keySet()){
            String pvName="BCM.signals.mode"+prefname.substring(prefname.lastIndexOf("."),prefname.length());
            bcms.put((String)preferences.get(prefname), (String)appPrefs.get(pvName));
        }
        
        return bcms;
        
    }
    
    private void changeNormalizationBCM(NormalizationDevice newValue){
        ((Main)Application.getAdaptor()).changeNormalizationBCM(newValue);
    }
    
    public void customizeCommands(Commander c) {

        c.registerAction(new AbstractAction("export-action") {
            private static final long serialVersionUID = 4377386270269629176L;

            public void actionPerformed(ActionEvent e) {

                saveSnapshot(null);
            }
        });
        c.registerAction(new AbstractAction("exportblm-action") {
            private static final long serialVersionUID = 4377386270269629176L;

            public void actionPerformed(ActionEvent e) {

                saveSnapshot("BLM");
            }
        });
        c.registerAction(new AbstractAction("exportnd-action") {
            private static final long serialVersionUID = 4377386270269629176L;

            public void actionPerformed(ActionEvent e) {

                saveSnapshot("ND");
            }
        });
    }

    private void saveSnapshot(final String whatToSave) {
        new Thread() {

            public void run() {
                ((LV2Document) document).saveSnapshot(whatToSave);
            }
        }.start();
    }
}
