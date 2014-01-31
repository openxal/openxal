package xal.app.lossviewer;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import xal.app.lossviewer.signals.ProcessorFactory;
import xal.app.lossviewer.signals.ScalarSignalValue;
import xal.app.lossviewer.signals.SignalHistory;
import xal.app.lossviewer.signals.SignalValue;

public abstract class LossDetector {

    public static int STATUS_OK = 0;
    public static int STATUS_NEGATIVE = 1;
    public static int STATUS_NOISE = 2;
    public static int STATUS_INVALID = 3;
    private String name;
    private String seq;
    private int index;
    private double position;
    protected Dispatcher dispatcher;
    private static final double DEFAULT_DISTANCE = 70;
    private static final double NOISE_THRESHOLD = 1e-5;
    private double distanceToBeamline = DEFAULT_DISTANCE;
    private String shortName;

    public double getDefaultWeight() {
        return 1.0;
    }

    public int getStatus() {
        ScalarSignalValue sv = ((ScalarSignalValue) getValue("PulseLoss"));
        if (sv == null) {
            return STATUS_INVALID;
        }
        double loss = sv.getValue();
        if (Math.abs(loss) < getNoiseThreshold()) {
            return STATUS_NOISE;
        }
        if (loss < 0.0) {
            return STATUS_NEGATIVE;
        }

        return STATUS_OK;
    }

    public double getNoiseThreshold() {

        return NOISE_THRESHOLD;
    }

    public void setDistanceToBeamline(double distance) {
        distanceToBeamline = distance;
    }

    public double getPosition() {

        return position;
    }

    /**
     * Sets Name
     *
     * @param    Name                a  String
     */
    private void setName(String name) {
        this.name = name;
        int ind = name.indexOf("_Diag");
        shortName = name.substring(0, ind) + name.substring(ind + 5, name.length());
    }

    /**
     * Returns Name
     *
     * @return    a  String
     */
    public String getName() {
        return name;
    }

    public String getShortName() {
        return shortName;
    }

    /**
     * Sets Seq
     *
     * @param    Seq                 a  String
     */
    private void setSeq(String seq) {
        this.seq = seq;
    }

    /**
     * Returns Seq
     *
     * @return    a  String
     */
    public String getSeq() {
        return seq;
    }

    /**
     * Returns Type
     *
     * @return    a  String
     */
    public abstract String getType();

    /**
     * Sets Index
     *
     * @param    index               an int
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * Returns Index
     *
     * @return    an int
     */
    public int getIndex() {
        return index;
    }
    @SuppressWarnings("unchecked")
    public static LossDetector createDetector(Main app, String className,
            String id, String seqName,
            double position, int index) {

        LossDetector result = null;

        try {
            Class<LossDetector> c = (Class<LossDetector>)Class.forName(className);
            result = c.getConstructor().newInstance();
            result.setIndex(index);
            result.setName(id);
            result.setPosition(position);
            result.setSeq(seqName);
            Map<String, Object> names = app.getPreferences().getPreferencesFor(
                    result.getType() + ".signals.names.");

            Map<String, Object> histories = app.getPreferences().getPreferencesFor(
                    result.getType() + ".signals.histories.");
 /*           for (String n : names.keySet()) {
                String bb = result.getType() + ".signals.histories." + n;
                String histStr = (String) app.getPreferences().get(result.getType() + ".signals.histories." + n);
                int history = 0;
                if (histStr != null) {
                    history = Integer.parseInt(histStr);
                }
                histories.put(n, history);
            }
 */           
            result.setSignalNames(names,histories);



        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private void setSignalNames(Map<String, Object> names, Map<String, Object> histories) {
        for (String e : names.keySet()) {
            String signalName = e.substring(e.lastIndexOf('.') + 1, e.length());

            signalNames.put(signalName, (String) names.get(e));
        }
        for (String e : histories.keySet()) {
            String signalName = e.substring(e.lastIndexOf('.') + 1, e.length());
            signalHistories.put(signalName, Integer.parseInt((String)histories.get(e)));
        }
    }

    private void setPosition(double position) {
        this.position = position;
    }

    public static Comparator<LossDetector> getComparator() {
        return new Comparator<LossDetector>() {

            public int compare(LossDetector ld1, LossDetector ld2) {
                return ld1.getIndex() - ld2.getIndex();
            }

            public boolean equals(LossDetector ld) {
                return false;
            }
        };
    }

    @Override
    public String toString() {
        return "[" + name + "," + getType() + "," + this.getClass().getCanonicalName() + "," + seq + "]";
    }
    private final Map<String, String> signalNames = new HashMap<String, String>();
    private final Map<String, Integer> signalHistories = new HashMap<String, Integer>();

    public String getRawSignalName(String name) {
        return signalNames.get(name);
    }

    public void startSignals(Dispatcher d) {
        this.dispatcher = d;

        for (String key : signalNames.keySet()) {

            Integer history = signalHistories.get(key);
            if (history == null) {
                history = 0;
            }
            d.createSignal("CASignal", name + ":" + signalNames.get(key), history);
        }

    }

//	private Map<String,SpecialProcessor> specialSignals = new HashMap<String,SpecialProcessor>();
    public SpecialProcessor getSpecialSignal(String name) {
        return ProcessorFactory.getProcessor(name);
    }

    private NormalizationProcessor getNormalizationProcessor(String suffix) {
        return ProcessorFactory.getNormalizationProcessor(suffix);
    }

    public SignalValue getValue(String suffix) {
        return getValue(suffix, null);
    }

    
    public boolean setValue(String suffix, double value){
        Integer index = signalCoding.get(suffix);
            if (index == null) {
                index = dispatcher.indexOfSignal(name + ":" + signalNames.get(suffix));
                signalCoding.put(suffix, index);
            }


            return dispatcher.setValue(index,value);
    }
    public SignalHistory getHistory(String suffix) {
        Integer index = signalCoding.get(suffix);
        if (index == null) {
            index = dispatcher.indexOfSignal(name + ":" + signalNames.get(suffix));
            signalCoding.put(suffix, index);
        }
        return dispatcher.getHistory(index);
    }
    private final Map<String, Integer> signalCoding = new HashMap<String, Integer>(8);

    public SignalValue getValue(String suffix, Set<String> normalization) {

//		double r = Math.random()*10.0;
//		return new ScalarSignalValue(null,0,r);

        SpecialProcessor s = getSpecialSignal(suffix);
        SignalValue sv;
        if (s == null) {
            Integer index = signalCoding.get(suffix);
            if (index == null) {
                index = dispatcher.indexOfSignal(name + ":" + signalNames.get(suffix));
                signalCoding.put(suffix, index);
            }


            sv = dispatcher.getValue(index);
//            sv =  dispatcher.getValue(name+":"+signalNames.get(suffix));
        } else {
            sv = getSpecialSignal(suffix).getValue(dispatcher, this);
        }

        if (normalization == null) {
            return sv;
        }

        if (sv == null) {
            return null;
        }

        NormalizationProcessor np = getNormalizationProcessor(suffix);
        return np.getValue(normalization, dispatcher, this, sv);




    }

    public double getDistanceToBeamline() {

        return distanceToBeamline;
    }
}
