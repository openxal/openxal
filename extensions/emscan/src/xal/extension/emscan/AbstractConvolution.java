/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.extension.emscan;

/**
 *
 * @author sashazhukov
 */
public abstract class AbstractConvolution<E extends DataRecord> implements Convolution<E> {
    
    public TimeData<? extends DataRecord> convolve(final TimeData<E> data){
        int numOfRecords = data.size();
        for (int i = 0; i < numOfRecords; i++) {
            processRecord(data.getDataRecord(i));
        }
        return getResult();
    }
    
    public void reset(){
        
    }
    
}
