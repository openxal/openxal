/*
 * Main.java
 *
 * Created on Fri Oct 10 14:03:52 EDT 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */
package xal.app.lossviewer;

import xal.extension.application.*;
import xal.app.lossviewer.preferences.*;
import xal.app.lossviewer.signals.*;

import xal.extension.application.smf.*;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.*;
import javax.swing.*;
import xal.smf.Accelerator;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;
import xal.smf.AcceleratorSeqCombo;
import xal.smf.data.OpticsSwitcher;
import xal.smf.data.XMLDataManager;
import xal.smf.impl.qualify.AndTypeQualifier;

/**
 * Main is the ApplicationAdaptor for the Template application.
 *
 * @author  somebody
 */
public class Main extends ApplicationWithPreferences {

    private static Accelerator localAccelerator = null;
    private Dispatcher dispatcher;

    public LossDetector getDetector(String detName) {
        for (LossDetector detector : allBLMs) {
            if (detector.getName().equals(detName)) {
                return detector;
            }
        }
        return null;
    }

    public Map<String, SortedSet<LossDetector>> getSequences() {

        return allSequencesWithBLMs;
    }

    public Map<String, SortedSet<LossDetector>> getCombos() {

        return allCombosWithBLMs;
    }

    public static Accelerator getAccelerator() {
        return localAccelerator;

    }

    // --------- Document management -------------------------------------------
    /**
     * Returns the text file suffixes of files this application can open.
     * @return Suffixes of readable files
     */
    public String[] readableDocumentTypes() {
        return new String[]{"blm"};
    }

    /**
     * Returns the text file suffixes of files this application can write.
     * @return Suffixes of writable files
     */
    public String[] writableDocumentTypes() {
        return new String[]{"blm"};
    }

    /**
     * Implement this method to return an instance of my custom document.
     * @return An instance of my custom document.
     */
    public XalDocument newEmptyDocument() {
        return new LV2Document(dispatcher);
    }

    /**
     * Implement this method to return an instance of my custom document
     * corresponding to the specified URL.
     * @param url The URL of the file to open.
     * @return An instance of my custom document.
     */
    public XalDocument newDocument(java.net.URL url) {
        return new LV2Document(dispatcher, url);
    }

    // --------- Global application management ---------------------------------
    /**
     * Specifies the name of my application.
     * @return Name of my application.
     */
    public String applicationName() {
        return "LossViewer2";
    }

    // --------- Application events --------------------------------------------
    /**
     * Capture the application launched event and print it.  This is an optional
     * hook that can be used to do something useful at the end of the application launch.
     */
    public void applicationFinishedLaunching() {
        //  System.out.println("Application has finished launching!");
    }

    /**
     * Constructor
     */
    public Main() {
        super();

        dispatcher = new EpicsDispatcher((String) getPreferences().get("TriggerName"));
        initializeBLMs();
        initializeBCMs();

        ((EpicsDispatcher) dispatcher).startTrigger();

    }

    public void changeNormalizationBCM(NormalizationDevice newValue){
        chargeNormalizer.setBCM(newValue);
    }
    private ChargeNormalizer chargeNormalizer;
    private List<NormalizationDevice> bcms;
    public List<NormalizationDevice> getBCMs(){
        return bcms;
    }
    
    private void initializeBCMs() {
        
        
        bcms = new ArrayList<NormalizationDevice>();
        
        Map<String, String> bcmNames = new HashMap<String, String>();
        Map<String, Object> bcmLabels = getPreferences().getPreferencesFor("BCM.signals.mode.");
        bcms = new ArrayList<NormalizationDevice>(bcmLabels.size());
        for (String e : bcmLabels.keySet()) {
            String labelName = e.substring(e.lastIndexOf('.') + 1, e.length());
            final int id = Integer.parseInt(labelName);
            final String name = (String)getPreferences().get("BCM.signals.name."+id);
            final String pv = (String)bcmLabels.get(e);
            final double scale = Double.parseDouble( (String)getPreferences().get("BCM.signals.scale."+id));
            
            bcms.add(new NormalizationDevice() {

                public String getName() {
                    return name;
                }

                public String getChargePV() {
                    return pv;
                }

                public int getID() {
                    return id;
                }

                public int compareTo(NormalizationDevice o) {
                    return getID()-o.getID();
                }

                public double getScale() {
                    return scale;
                }
            });
           
            
        }
        
        Collections.sort(bcms);
        chargeNormalizer = new ChargeNormalizer(bcms);
        dispatcher.addSignal(chargeNormalizer);
    }
    private List<LossDetector> allBLMs;

    public List<LossDetector> getAllBLMs() {
        return allBLMs;
    }

    public List<LossDetector> getAllBLMs(String filter) {
        if (filter == null) {
            return getAllBLMs();
        }
        List<LossDetector> list = new ArrayList<LossDetector>();
        for (LossDetector ld : allBLMs) {
            if (ld.getType().equals(filter)) {
                list.add(ld);
            }
        }
        return list;
    }
    private Map<String, SortedSet<LossDetector>> allSequencesWithBLMs;
    private Map<String, SortedSet<LossDetector>> allCombosWithBLMs;

    public void initializeBLMs() {

        String defaultFileName = XMLDataManager.defaultPath();

        if (defaultFileName == null) {
            initilaizeFromDefaultAccelerator();
            return;
        }
        File f = new File(defaultFileName);
        String fileSize = String.valueOf(f.length());
        String timeStamp = String.valueOf(f.lastModified());

        Preferences userPrefs = Preferences.userNodeForPackage(this.getClass());
        String fName = userPrefs.get("opticsfile", null);
        String fSize = userPrefs.get("opticssize", null);
        String fTime = userPrefs.get("opticstimestamp", null);


        try {

            userPrefs.put("opticsfile", defaultFileName);
            userPrefs.put("opticssize", fileSize);
            userPrefs.put("opticstimestamp", timeStamp);
            userPrefs.flush();


        } catch (Exception ex) {
            ex.printStackTrace();
        }


        initilaizeFromDefaultAccelerator();

        loadDistances();

    }

    private void initilaizeFromDefaultAccelerator() {

        long start = System.currentTimeMillis();
        String blmStringQual = (String) getPreferences().get("LossQualifier");
        //    System.out.println("Getting BLMs");
        if (localAccelerator == null) {
            try {
                localAccelerator = XMLDataManager.loadDefaultAccelerator();
            } catch (Exception e) {
                localAccelerator = null;
            }

            //still null, optics not set
            if (localAccelerator == null) {
                OpticsSwitcher switcher = OpticsSwitcher.getInstance();
                switcher.showDialogNearOwner((JFrame)null);
                if (switcher.getDefaultOpticsPath() != null) {
                    localAccelerator = XMLDataManager.loadDefaultAccelerator();
                }
            }
        }


        AndTypeQualifier blmQual = new AndTypeQualifier();
        blmQual.and(blmStringQual);

        final List<String> seqNames = new ArrayList<String>();
        for (AcceleratorSeq seq : localAccelerator.getAllSeqs()) {
            seqNames.add(seq.getId());
        }
        allSequencesWithBLMs = new TreeMap<String, SortedSet<LossDetector>>(
                new Comparator<String>() {

                    public int compare(String o1, String o2) {
                        int index1 = seqNames.indexOf(o1);
                        int index2 = seqNames.indexOf(o2);
                        return index1 - index2;
                    }
                });



        final List<String> comboNames = new ArrayList<String>();
        for (Object seq : localAccelerator.getComboSequences()) {
            comboNames.add(((AcceleratorSeqCombo) seq).getId());
        }
        allCombosWithBLMs = new TreeMap<String, SortedSet<LossDetector>>(
                new Comparator<String>() {

                    public int compare(String o1, String o2) {
                        int index1 = comboNames.indexOf(o1);
                        int index2 = comboNames.indexOf(o2);
                        return index1 - index2;
                    }
                });


        allBLMs = new ArrayList<LossDetector>();


        List<AcceleratorNode> blms = localAccelerator.getAllNodesWithQualifier(blmQual);
        int index = 0;
        for (AcceleratorNode blm : blms) {
            String seqName = blm.getParent().getId();

            String className = (String) getPreferences().get("Detector." + blm.getType() + ".classname");
            //		System.out.println(className+" "+blm.getId()+" "+blm.getType());
            LossDetector det = LossDetector.createDetector(this, className, blm.getId(), seqName, blm.getPosition(), index);
            SortedSet<LossDetector> seqSet = allSequencesWithBLMs.get(seqName);
            if (seqSet == null) {
                seqSet = new TreeSet<LossDetector>(LossDetector.getComparator());
                allSequencesWithBLMs.put(seqName, seqSet);
            }
            seqSet.add(det);
            allBLMs.add(det);
            det.startSignals(dispatcher);

            index++;
        }

        List<AcceleratorSeqCombo> combos = localAccelerator.getComboSequences();
        for (AcceleratorSeqCombo combo : combos) {
            //	System.out.println(combo);
            List<String> sequences = combo.getConstituentNames();
            SortedSet<LossDetector> seqSet = new TreeSet<LossDetector>(LossDetector.getComparator());
            for (String e : sequences) {
                Set<LossDetector> ld = allSequencesWithBLMs.get(e);

                if (ld != null) {
                    //			System.out.println(e + " " + ld.size());
                    seqSet.addAll(ld);
                }
            }
            if (seqSet.size() > 0) {
                allCombosWithBLMs.put(combo.getId(), seqSet);
            }

        }

        // System.out.println("Init from default took: " + (System.currentTimeMillis() - start));
        Logger.getLogger("global").log(Level.INFO, "Init from default took: " + (System.currentTimeMillis() - start));

    }

//cccccccc	public void editPreferences(final XalDocument doc) {
//		super.editPreferences(doc);
//
//	}
    public void applicationWillQuit() {
        try {
            dispatcher.close();
        } catch (Exception ex) {
            //suppress disconnecting JNI channels cursing
        }
    }

    /** The main method of the application. */
    static public void main(String[] args) {
        try {
            setOptions(args);
            Main app = new Main();
            AcceleratorApplication.launch(app);
            Logger.getLogger(app.getClass().getCanonicalName()).log(Level.INFO, "JAVA version = " + (System.getProperty("java.runtime.version")));


        } catch (Exception exception) {
            System.err.println(exception.getMessage());
            exception.printStackTrace();
            Application.displayApplicationError("Launch Exception", "Launch Exception", exception);
            System.exit(-1);
        }
    }

    private void loadDistances() {

        //Map<String,String> distanceMap = Util.loadResourceBundle(getClass().getPackage().getName() + ".resources.distance");
        Map<String,String> distanceMap = Util.loadResourceBundle( this.getResourceURL( "distance.properties" ) );
        for (LossDetector b : allBLMs) {
            String name = b.getName();
            String dist = distanceMap.get(name);
            if (dist != null) {
                try {
                    double distance = Double.parseDouble(dist.toString());
                    b.setDistanceToBeamline(distance);
                } catch (NumberFormatException e) {
                }

            }
        //           System.out.println(name + " " + b.getDistanceToBeamline());


        }
    }
}

