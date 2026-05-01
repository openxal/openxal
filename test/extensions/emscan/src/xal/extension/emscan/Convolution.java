/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.extension.emscan;

/**
 *
 * @author sashazhukov
 */
public interface Convolution<E extends DataRecord> {
    
    public void reset();
    public void processRecord(E record);
    public TimeData<? extends DataRecord> convolve(final TimeData<E> data);
    public TimeData<? extends DataRecord> getResult();
    
}
