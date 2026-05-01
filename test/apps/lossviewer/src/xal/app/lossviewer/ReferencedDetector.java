/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package xal.app.lossviewer;

import xal.app.lossviewer.signals.*;

import java.util.Set;

/**
 *
 * @author sasha
 */
public class  ReferencedDetector extends LossDetector {


    private LossDetector referred=null;
    private double pulseLoss=0.0;
    private double pulseLoss60=0.0;

    public ReferencedDetector(LossDetector ld){
        referred = ld;
        if(ld!=null){
            try {
            pulseLoss = ((ScalarSignalValue)(ld.getValue("PulseLoss"))).getValue();
            pulseLoss60 = ((ScalarSignalValue)(ld.getValue("Slow60"))).getValue();
            } catch(Exception ex) {

            }
        }
    }

    @Override
    public String getType() {
        return referred.getType();
    }

        public double getDefaultWeight() {
        return 1.0;
    }

    public int getStatus() {


        return referred.getStatus();
    }

    public double getNoiseThreshold() {

        return referred.getNoiseThreshold();
    }

    public void setDistanceToBeamline(double distance) {

    }

    public double getPosition() {

        return referred.getPosition();
    }

    /**
     * Sets Name
     *
     * @param    Name                a  String
     */
    private void setName(String name) {

    }

    /**
     * Returns Name
     *
     * @return    a  String
     */
    public String getName() {
        return referred.getName();
    }

    public String getShortName() {
        return "R:"+referred.getShortName();
    }

    /**
     * Sets Seq
     *
     * @param    Seq                 a  String
     */
    private void setSeq(String seq) {

    }

    /**
     * Returns Seq
     *
     * @return    a  String
     */
    public String getSeq() {
        return referred.getSeq();
    }


    /**
     * Sets Index
     *
     * @param    index               an int
     */
    public void setIndex(int index) {

    }

    /**
     * Returns Index
     *
     * @return    an int
     */
    public int getIndex() {
        return referred.getIndex();
    }







    @Override
    public String toString() {
        return referred.toString();
    }


    public String getRawSignalName(String name) {
        return referred.getRawSignalName(name);
    }

    public void startSignals(Dispatcher d) {


    }

//	private Map<String,SpecialProcessor> specialSignals = new HashMap<String,SpecialProcessor>();
    public SpecialProcessor getSpecialSignal(String name) {
        return referred.getSpecialSignal(name);
    }



    public SignalValue getValue(String suffix) {
        if("Slow60".equals(suffix)){
            ScalarSignalValue refval = (ScalarSignalValue)(referred.getValue(suffix));
            return new ScalarSignalValue(null, refval.getTimestamp(),refval.getValue()-pulseLoss60);
        }
        else if("PulseLoss".equals(suffix)){
            ScalarSignalValue refval = (ScalarSignalValue)(referred.getValue(suffix));
            return new ScalarSignalValue(null, refval.getTimestamp(),refval.getValue()-pulseLoss);
        }
        return referred.getValue(suffix);
    }


    public boolean setValue(String suffix, double value){
            return false;
    }
    public SignalHistory getHistory(String suffix) {
        


        return referred.getHistory(suffix);
    }


    public SignalValue getValue(String suffix, Set<String> normalization) {

        if("Slow60".equals(suffix)){
            ScalarSignalValue refval = (ScalarSignalValue)(referred.getValue(suffix,normalization));
            ScalarSignalValue noNorm = (ScalarSignalValue)(referred.getValue(suffix));
            return new ScalarSignalValue(null, refval.getTimestamp(),refval.getValue()-pulseLoss60*refval.getValue()/noNorm.getValue());
        }
        else if("PulseLoss".equals(suffix)){
            ScalarSignalValue refval = (ScalarSignalValue)(referred.getValue(suffix,normalization));
            ScalarSignalValue noNorm = (ScalarSignalValue)(referred.getValue(suffix));
            return new ScalarSignalValue(null, refval.getTimestamp(),refval.getValue()-pulseLoss*refval.getValue()/noNorm.getValue());
        }
        return referred.getValue(suffix,normalization);




    }

    public double getDistanceToBeamline() {

        return referred.getDistanceToBeamline();
    }





}
