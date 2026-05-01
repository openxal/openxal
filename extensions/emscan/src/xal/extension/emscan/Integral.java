/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.extension.emscan;

/**
 *
 * @author sashazhukov
 */
public abstract class Integral<E extends DataRecord> extends AbstractConvolution<E>{ 
    private final int baseLine;
    private final int end;
    private final int start;
    
    private double minimum=Double.MAX_VALUE;
    private double maximum = Double.MIN_VALUE;
    
    int minIndex =-1;
    int maxIndex =-1;
    
    

    public Integral(int baseLine, int start, int end) {
        this.baseLine = baseLine;
        this.start = start;
        this.end = end;
               
    }

    @Override
    public synchronized void reset() {
       
        minimum=Double.MAX_VALUE;
        maximum = Double.MIN_VALUE;
    }


    
    protected double calculateIntegral(E record){
        double base = getSum(record, 0,baseLine);
        double integral = getSum(record, start,end);
        double signal = integral / (double) (end-start) - base / (double) baseLine;
        
        return signal;
    }
    
    private double getSum(E record,int start, int end){
        double sum = 0;
        for(int i=start;i<end;i++){
            sum+=record.getValue(i);
        }
        return sum;
    }

    protected synchronized void upadateMaxMin(double value, int index) {
        if(value>maximum){
            maximum=value;
            maxIndex = index;
        }
        if(value<minimum){
            minimum=value;
            minIndex = index;
        }
    }
    
 
    
    
}
