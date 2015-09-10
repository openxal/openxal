package xal.app.lossviewer;

import xal.app.lossviewer.signals.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.*;
import java.util.*;
import java.util.Vector;
import javax.swing.Timer;

public class Dispatcher {

    private ArrayList<Signal> allSignals = new ArrayList<Signal>();
    private Map<String, Constructor<Signal>> types = new HashMap<String, Constructor<Signal>>();
    private Map<String, Integer> signalNames = new HashMap<String, Integer>();
    Timer swingTimer;
    private static final int INITIAL_SIZE=10000;
    private Object valueLock = new Object();
    private Object historyLock = new Object();

    public Dispatcher() {
        
        resetValueVector();
        resetHistoryVector();
        swingTimer = new Timer(200, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                performUpdate();
            }
        });
        swingTimer.setRepeats(false);


        initializeDefaultTypes();
        fireUpdate();
    }

    public int indexOfSignal(String sName) {

        return signalNames.get(sName);
    }

    private SignalHistory getH0(int id) {
        SignalHistory history = historicValues.get(id);

        return history;
    }

    private SignalHistory makeH0(int id, int histsize, SignalHistory history) {

        if (history == null) {
            history = new SignalHistory(histsize);
            // historicValues.put(id, history);
            historicValues.set(id, history);
        }
        return history;
    }
    
    
    private void resetValueVector(){
         synchronized(valueLock){
            currentValues.clear();
            for(int i=0;i<INITIAL_SIZE;i++){
                currentValues.add(null);
            }
        }
    }
    
    private void resetHistoryVector(){
      //   synchronized(historyLock){
             historicValues.clear();
             for (int i = 0; i < INITIAL_SIZE; i++) {
                 historicValues.add(null);
             }
      //   }
    }
    
    protected void performUpdate() {
        long start = System.currentTimeMillis();
//azuk

        synchronized (valueLock) {
            
//            for (Integer id : currentValues.keySet()) {
//                allValues.set(id, currentValues.get(id));
//            }
            for (int id=0; id< currentValues.size();id++) {
                SignalValue sv = currentValues.get(id);
                if(sv!=null)
                    allValues.set(id, sv);
            }
         //   currentValues.clear();
            resetValueVector();
        }
        
     //   synchronized (historyLock) {
//            for (Integer id : historicValues.keySet()) {
//                allHistories.set(id, historicValues.get(id).copy());
//            }
            for (int id=0; id< historicValues.size();id++) {
                SignalHistory sh = historicValues.get(id);
                if(sh!=null)
                    allHistories.set(id, sh.copy());
            }
 //           resetHistoryVector();

      //  }



        for (SignalListener l : allSignalListeners) {
            l.signalUpdated(new SignalEvent(null, this));
        }
        long end = System.currentTimeMillis();
    //	System.out.println(" swing update "+end+" "+start);
    }

    protected void fireUpdate() {

        swingTimer.start();

    }
//    private Map<Integer, SignalValue> currentValues = Collections.synchronizedMap(new HashMap<Integer, SignalValue>(5000));
//    private Map<Integer, SignalHistory> historicValues = Collections.synchronizedMap(new HashMap<Integer, SignalHistory>(5000));        
    private List<SignalValue> currentValues = new ArrayList<SignalValue>(INITIAL_SIZE);   
    private List<SignalHistory> historicValues = new Vector<SignalHistory>(INITIAL_SIZE);

    public void processNewValue(SignalValue sv) {
        Signal signal = sv.getSignal();
        int histsize = signal.getHistorySize();
        int id = signal.getID();
   //     currentValues.put(id, sv);
         
        synchronized(valueLock){
            currentValues.set(id, sv);
         }
        
        addNewValueToHistory(sv, histsize,id);

    }
    private void addNewValueToHistory(SignalValue sv,int histsize,int id){
     //   synchronized(historyLock){
            if (histsize == 0) {
                return;
            }        
            SignalHistory history = getH0(id);
            history = makeH0(id, histsize, history);        
            history.addSync(sv);
    //    }
        

    //	System.out.println(sv);
    }

    public boolean setValue(int index, double value) {
        boolean result = false;

        result = allSignals.get(index).setValue(value);

        //System.out.println("Pushing " +result);
        return result;
    }
    @SuppressWarnings("unchecked")
    private void initializeDefaultTypes() {
        try {
            Class<Signal> cl = (Class<Signal>)Class.forName("xal.app.lossviewer.signals.TimerSignal");
            if (cl != null) {
                types.put("TimerSignal",  cl.getConstructor());
            }
            cl = (Class<Signal>)Class.forName("xal.app.lossviewer.signals.CASignal");
            if (cl != null) {
                types.put("CASignal", cl.getConstructor());
            }

        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    @SuppressWarnings("unchecked")
    public Signal createSignal(String signalType, String signalName, int history, String className) {
        try {
            Class<Signal> cl = (Class<Signal>)Class.forName(className);
            if (cl != null) {
                types.put("TimerSignal", cl.getConstructor());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
        return createSignal(signalType, signalName, history);
    }

    public Signal addSignal(AbstractSignal signal) {
        String signalName = signal.getName();
        Integer curid = signalNames.get(signalName);
        if (curid != null) {
            //	System.out.println(signalName + " already exists");
            return allSignals.get(curid);
        }
        int id = allSignals.size();
        signal.setID(id);
        allSignals.add(signal);
        allValues.add(null);
        allHistories.add(null);
        signalNames.put(signalName, id);
        signal.setDispatcher(this);
        signal.start();
        return signal;

    }

    public Signal createSignal(String signalType, String signalName, int history) {

        AbstractSignal signal;
        Integer curid = signalNames.get(signalName);
        if (curid != null) {
            //	System.out.println(signalName + " already exists");
            Signal s = allSignals.get(curid);
            int hs = s.getHistorySize();
            if (hs < history) {
                s.setHistorySize(history);
            }
            return s;
        }

        Constructor<Signal> c = types.get(signalType);
        if (c == null) {
            return null;
        }

        try {
            signal = (AbstractSignal) (c.newInstance());
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }



        signal.setName(signalName);
        int id = allSignals.size();
        signal.setID(id);
        signal.setHistorySize(history);

        allSignals.add(signal);
        allValues.add(null);
        allHistories.add(null);
        signalNames.put(signalName, id);
        signal.setDispatcher(this);
        signal.start();

        return signal;

    }
    private ArrayList<SignalValue> allValues = new ArrayList<SignalValue>();
    private ArrayList<SignalHistory> allHistories = new ArrayList<SignalHistory>();

    public SignalValue getValue(String signalName) {
        SignalValue sv = getValue(indexOfSignal(signalName));
        return sv;
    }

    public SignalValue getValue(int signalID) {
        return allValues.get(signalID);
    }

    public SignalHistory getHistory(String signalName) {
        SignalHistory sv = getHistory(indexOfSignal(signalName));
        return sv;
    }

    public SignalHistory getHistory(int signalID) {
        return allHistories.get(signalID);
    }

    public void close() {
        for (Signal signal : allSignals) {
            signal.close();
        }
    }
    List<SignalListener> allSignalListeners = new ArrayList<SignalListener>();

    public void addSignalListener(SignalListener l) {
        allSignalListeners.add(l);
    }

    public void removeSignalListener(SignalListener l) {
        allSignalListeners.remove(l);
    }

    public void removeAllSignalListeners() {
        allSignalListeners.clear();
    }
}
