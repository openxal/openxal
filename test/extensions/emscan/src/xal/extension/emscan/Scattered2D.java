/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.extension.emscan;

/**
 *
 * @author sashazhukov
 */
public class Scattered2D <E extends DataRecord2D> extends Integral<E> {
    final DefaultTimeData<Scalar2D> result;
    
    public Scattered2D(int baseLine, int start, int end) {
        super(baseLine,start,end);
        result = new DefaultTimeData<Scalar2D>(Factories.getScalar2DFactory());
    }
    
    @Override
    public synchronized void processRecord(E record){
        double value = calculateIntegral(record);
        result.addRecord(new Scalar2D(record.getMillis(),record.getX(),record.getY(),value));
        upadateMaxMin(value,size()-1);
        
    }
    

    
    
    public synchronized int size(){
        return result.size();
    }

    @Override
    public TimeData<Scalar2D> getResult() {
        return result;
    }
    
}
