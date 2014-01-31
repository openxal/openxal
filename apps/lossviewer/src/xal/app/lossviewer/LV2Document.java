/*
 * TemplateDocument.java
 *
 * Created on Fri Oct 10 14:08:21 EDT 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */
package xal.app.lossviewer;

import xal.extension.application.*;
import xal.app.lossviewer.preferences.*;
import xal.app.lossviewer.signals.*;
import xal.app.lossviewer.views.*;

import java.awt.*;
import java.io.*;
import java.text.*;
import java.util.*;
import javax.swing.*;

import java.util.List;
import xal.tools.data.DataAdaptor;
import xal.tools.xml.XmlDataAdaptor;

/**
 * TemplateDocument
 *
 * @author  somebody
 */
public class LV2Document extends AcceleratorDocumentWithPreferences {

    private Dispatcher dispatcher;

    public void removeViewAllViews() {
        for (View<?> v : allViews) {
            dispatcher.removeSignalListener(v);

        }
        allViews.clear();
    }

    public void saveSnapshot() {
        saveSnapshot(null);
    }
    private Map<String, List<LossDetector>> references = new TreeMap<String, List<LossDetector>>();
    //should be not in EDT thread - IO operations

    public void saveSnapshot(String whatToSave) {
        GregorianCalendar gc = new GregorianCalendar();

        createNewReference(new SimpleDateFormat("MM/dd HH:mm:ss").format(gc.getTime()), ((Main) Application.getAdaptor()).getAllBLMs(null));


        String scoreDir = getDefaultFolder().getAbsolutePath();
        List<LossDetector> detList = ((Main) Application.getAdaptor()).getAllBLMs(whatToSave);
        StringBuffer sb = getSnapshotBuffer(detList);

        DateFormat dfull = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");

        String fname = dfull.format(gc.getTime());
        final File scFile = new File(scoreDir + File.separator + fname + ".txt");
        try {

            BufferedWriter bw = new BufferedWriter(new FileWriter(scFile));
            bw.write(sb.toString());
            bw.close();
            SwingUtilities.invokeLater(
                    new Runnable() {

                        public void run() {
                            ((LV2Window) mainWindow).setMsg("Last save: " + scFile.getAbsolutePath());
                        }
                    });
        } catch (Exception e) {
        }
    }

    private StringBuffer getSnapshotBuffer(final List<LossDetector> detList) {
        final StringBuffer sb = new StringBuffer();
        try {
            SwingUtilities.invokeAndWait(
                    new Runnable() {

                        public void run() {
                            Set<String> norm = new HashSet<String>(4);
                            norm.add("CHRG");
                            for (LossDetector e : detList) {
                                String s = e.getName();
                                String val = "null";
                                ScalarSignalValue sv = ((ScalarSignalValue) e.getValue("PulseLoss"));
                                if (sv != null) {
                                    val = String.valueOf(sv.getValue());
                                }
                                s = s + " " + val;
                                sv = ((ScalarSignalValue) e.getValue("PulseLossNQ", norm));

                                val = "null";
                                if (sv != null) {
                                    val = String.valueOf(sv.getValue());
                                }
                                s = s + " " + val + "\n";
                                sb.append(s);
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb;
    }

    public String dataLabel() {
        return "LossViewer2Document";
    }

    /** Create a new empty document */
    public LV2Document(Dispatcher d) {
        this(d, null);
        this.loadDefaultAccelerator();
    }

    /**
     * Create a new document loaded from the URL file
     * @param url The URL of the file to load into the new document.
     */
    public LV2Document(Dispatcher d, java.net.URL url) {
        super();
        this.dispatcher = d;
        loadFrom(url);
        addPreferenceListener(new PreferenceListener() {

            public void preferenceChanged(PreferenceEvent event) {
                //	System.out.println(event);
            }
        });
    
        List<LossDetector> allBLMs = ((Main) Application.getAdaptor()).getAllBLMs(null);



        references.put("NONE",allBLMs);

        

    }
    private List<View<LossDetector>> allViews = new ArrayList<View<LossDetector>>();

    public void addDefaultView(String text) {
        addView("Defaultview", text);
    }

    public void addView(String viewname, String text) {
        String className = (String) get(viewname + ".classname");
        View<LossDetector> newView;
        try {
            newView = (LossView) (Class.forName(className).getConstructor().newInstance());
            newView.setDocument(this);
            Rectangle r = (Rectangle) get(viewname + ".bounds");
            newView.setParentBounds(r);

            Set<LossDetector> detSet;

            if (text == null) {
                text = "Custom";
            }


            detSet = ((Main) Application.getAdaptor()).getSequences().get(text);

            newView.setTitle(text);
            newView.setListName(text);

            if (detSet == null) {
                detSet = ((Main) Application.getAdaptor()).getCombos().get(text);
            }

            newView.setDetectors(detSet);
            addView(newView);
            //dispatcher.addSignalListener(newView);


        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

    }

    public void removeView(View<LossDetector> v) {
        dispatcher.removeSignalListener(v);
        allViews.remove(v);
    }

    public void addView(View<LossDetector> newView) {
        allViews.add(newView);
        if (mainWindow != null) {
            ((LV2Window) mainWindow).addView(newView);
        }
        dispatcher.addSignalListener(newView);
    }

    /**
     * Make a main window by instantiating the my custom window.
     */
    public void makeMainWindow() {
        mainWindow = new LV2Window(this);
        Rectangle bounds = getMainWindowBounds();
        if (bounds != null) {
            mainWindow.setSize(bounds.width, bounds.height);
        }

        for (View<LossDetector> view : allViews) {
            ((LV2Window) mainWindow).addView(view);
        }

    }
    @SuppressWarnings("unchecked")
    public void update(DataAdaptor da) {
        super.update(da);
        List<DataAdaptor> viewDAs = da.childAdaptors("LossView");
        for (DataAdaptor vda : viewDAs) {
            try {
                String cn = vda.stringValue("classname");
                if(cn.startsWith("gov.sns.apps.lossviewer2")){
                    cn=cn.replace("gov.sns.apps.lossviewer2", this.getClass().getPackage().getName());
                }
                View<LossDetector> view = (View<LossDetector>) (Class.forName(cn).getConstructor().newInstance());
                view.setDocument(this);
                view.update(vda);
                addView(view);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void write(DataAdaptor da) {
        super.write(da);
        for (View<LossDetector> view : allViews) {
            DataAdaptor vda = da.createChild("LossView");
            vda.setValue("classname", view.getClass().getCanonicalName());
            view.write(vda);

        }
    }

    protected String dataLabelForPreviousVersion() {
        return "LOSS_VIEWER";
    }

    protected void updateFromPreviousVersion(DataAdaptor da) {
        String text = "<?xml version = '1.0' encoding = 'UTF-8' ?>";
        text = text + "<" + dataLabel() + "/>";
        XmlDataAdaptor xda = XmlDataAdaptor.adaptorForString(text, false);
        DataAdaptor newVersionDa = xda.childAdaptor(dataLabel());

        DataAdaptor bda = da.childAdaptor("Bounds");
        DataAdaptor nbda = newVersionDa.createChild("Bounds");
        for (String attr : bda.attributes()) {
            nbda.setValue(attr, bda.stringValue(attr));
        }

        newVersionDa.createChild("DocumentPreferences");


        List<DataAdaptor> vdas = da.childAdaptor("AllViews").childAdaptors("View");
        for (DataAdaptor vda : vdas) {
            DataAdaptor nvda = newVersionDa.createChild("LossView");
            nvda.setValue("classname", "xal.app.lossviewer.views.DefaultLossView");
            nvda.setValue("name", vda.stringValue("name"));
            nvda.setValue("listname", vda.stringValue("name"));


            bda = vda.childAdaptor("Bounds");
            nbda = nvda.createChild("Bounds");
            for (String attr : bda.attributes()) {
                nbda.setValue(attr, bda.stringValue(attr));
            }
            DataAdaptor divda, ndivda = null;
            try {
                divda = vda.childAdaptor("DividerLocation");
                ndivda = nvda.createChild("Split");
                ndivda.setValue("LastPosition", divda.stringValue("lastlocation"));
                ndivda.setValue("Position", divda.stringValue("location"));
            } catch (NullPointerException npe) {
                ndivda.setValue("LastPosition", 100);
                ndivda.setValue("Position", 0);
            }


            nvda.createChild("Added");
            nvda.createChild("Removed");

            DataAdaptor ntda = nvda.createChild("LossTable");
            DataAdaptor colda = ntda.createChild("Column");
            colda.setValue("name", "name");
            colda = ntda.createChild("Column");
            colda.setValue("name", "PulseLoss");

        }

        update(newVersionDa);

    }
    

    public Map<String,List<LossDetector>> getReferences(){
        return references;
    }

    private void createNewReference(final String timestamp, List<LossDetector> allBLMs) {
        List<LossDetector> l = new ArrayList<LossDetector>();
        for (LossDetector ld : allBLMs) {
            l.add(new ReferencedDetector(ld));
        }
        references.put(timestamp, l);
        
    }
}
