package xal.app.lossviewer.signals;

import xal.app.lossviewer.*;
import java.util.*;

public class ProcessorFactory {

    private static Map<String, SpecialProcessor> specialSignals = new HashMap<String, SpecialProcessor>();

    static {
        specialSignals.put("Distance", new SpecialProcessor() {

            public SignalValue getValue(Dispatcher d, LossDetector ld) {

                if (ld == null) {
                    return null;
                } else {
                    double value = ld.getDistanceToBeamline();
                    return new ScalarSignalValue(null, 0, value);

                }

            }
        });

        specialSignals.put("LimitPrev", new SpecialProcessor() {
            
            private static final String SN="SwTrip";
            
            public SignalValue getValue(Dispatcher d, LossDetector ld) {

                if (ld == null) {
                    return null;
                } else {
                    SignalValue value = ld.getValue(SN);
                    SignalHistory sh  = ld.getHistory(SN);
                    if(sh!=null&&sh.size()>1){
                        value = sh.getBackWard(1);
                    }
                        
                    
                    
                    return value;

                }

            }
        });
        
        specialSignals.put("LimitPrev2", new SpecialProcessor() {
            
            private static final String SN="SwTrip2";
            
            public SignalValue getValue(Dispatcher d, LossDetector ld) {

                if (ld == null) {
                    return null;
                } else {
                    SignalValue value = ld.getValue(SN);
                    SignalHistory sh  = ld.getHistory(SN);
                    if(sh!=null&&sh.size()>1){
                        value = sh.getBackWard(1);
                    }
                        
                    
                    
                    return value;

                }

            }
        });

         specialSignals.put("LimitPrev2", new SpecialProcessor() {

            private static final String SN="SwTrip2";

            public SignalValue getValue(Dispatcher d, LossDetector ld) {

                if (ld == null) {
                    return null;
                } else {
                    SignalValue value = ld.getValue(SN);
                    SignalHistory sh  = ld.getHistory(SN);
                    if(sh!=null&&sh.size()>1){
                        value = sh.getBackWard(1);
                    }



                    return value;

                }

            }
        });

        specialSignals.put("Sigma1", new SpecialProcessor() {
            
            private static final String SN="PulseLoss";
            
            public SignalValue getValue(Dispatcher d, LossDetector ld) {

                if (ld == null) {
                    return null;
                } else {
                    SignalValue value = ld.getValue(SN);
                    SignalHistory sh  = ld.getHistory(SN);
                    if(sh!=null){
                        int n = sh.getNonZeroCount();
                        double average = sh.getSum()/n;
                        double disp = sh.getSumSquared()/n -average*average;
                        double sigma = Math.sqrt(disp)/average;
                        value = new ScalarSignalValue(value.getSignal(), value.getTimestamp(), sigma);
                    }
                    
                    
                    return value;

                }

            }
        });

        specialSignals.put("Sigma60", new SpecialProcessor() {

            private static final String SN="Slow60";

            public SignalValue getValue(Dispatcher d, LossDetector ld) {

                if (ld == null) {
                    return null;
                } else {
                    SignalValue value = ld.getValue(SN);
                    SignalHistory sh  = ld.getHistory(SN);
                    if(sh!=null){
                        int n = sh.getNonZeroCount();
                        double average = sh.getSum()/n;
                        double disp = sh.getSumSquared()/n -average*average;
                        double sigma = Math.sqrt(disp)/average;
                        value = new ScalarSignalValue(value.getSignal(), value.getTimestamp(), sigma);
                    }


                    return value;

                }

            }
        });




        specialSignals.put("PulseLossAVG", new SpecialProcessor() {

            private static final String SN="PulseLoss";

            public SignalValue getValue(Dispatcher d, LossDetector ld) {

                if (ld == null) {
                    return null;
                } else {
                    SignalValue value = ld.getValue(SN);
                    SignalHistory sh  = ld.getHistory(SN);
                    if(sh!=null){
                        int n = sh.getNonZeroCount();
                        double average = sh.getSum()/n;
                        value = new ScalarSignalValue(value.getSignal(), value.getTimestamp(), average);
                    }

                    return value;

                }

            }
        });

         specialSignals.put("Slow60AVG", new SpecialProcessor() {

            private static final String SN="Slow60";

            public SignalValue getValue(Dispatcher d, LossDetector ld) {

                if (ld == null) {
                    return null;
                } else {
                    SignalValue value = ld.getValue(SN);
                    SignalHistory sh  = ld.getHistory(SN);
                    if(sh!=null){
                        int n = sh.getNonZeroCount();
                        double average = sh.getSum()/n;
                        value = new ScalarSignalValue(value.getSignal(), value.getTimestamp(), average);
                    }

                    return value;

                }

            }
        });


    }

    public static SpecialProcessor getProcessor(String name) {
        return specialSignals.get(name);
    }

    public static NormalizationProcessor getNormalizationProcessor(final String suffix) {

        return new NormalizationProcessor() {

            public SignalValue getValue(Set<String> normalization,
                    Dispatcher dispatcher,
                    LossDetector ld,
                    SignalValue sv) {
                double loss = ((ScalarSignalValue) sv).getValue();
                if (normalization.contains("LMT")) {

                    //	System.out.println(sv.getSignal().getName()+" "+suffix);
                    if (suffix.contains("PulseLoss") || suffix.equals("HwTrip")) {
                        ScalarSignalValue limit = (ScalarSignalValue) ld.getValue("HwTrip");
                        loss = loss / limit.getValue() * 100.0;
                    }
                    if (suffix.contains("Slow60") || suffix.equals("SwTrip")) {
                        ScalarSignalValue limit = (ScalarSignalValue) ld.getValue("SwTrip");
                        loss = loss / limit.getValue() * 100.0;
                    }
                } else {
                    if (normalization.contains("CHRG")) {
                        ScalarSignalValue charge = (ScalarSignalValue) dispatcher.getValue("Charge");
                        if (charge == null) {
                            return null;
                        }
                        loss = loss / charge.getValue();
                    }
                    if (normalization.contains("DST1")) {
                        loss = loss * ld.getDistanceToBeamline();
                    } else if (normalization.contains("DST2")) {
                        loss = loss * ld.getDistanceToBeamline() * ld.getDistanceToBeamline();
                    }
                }
                return new ScalarSignalValue(null, sv.getTimestamp(), loss);
            }
        };
    }
}
