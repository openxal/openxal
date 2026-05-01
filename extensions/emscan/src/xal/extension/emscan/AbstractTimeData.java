/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.extension.emscan;

import java.io.IOException;

/**
 *
 * @author sashazhukov
 */
public abstract class AbstractTimeData<E extends DataRecord> implements TimeData<E> {
    protected final DataRecordFactory<E> factory;
    
    
    
    protected DataRecordFactory<E> getFactory(){
        return factory;
    }
    protected AbstractTimeData(DataRecordFactory<E> factory){
        this.factory=factory;
    }
    
    public Convolution<E> convolve(final Convolution<E> conv) throws IndexOutOfBoundsException, IOException {
        int numOfRecords = size();
        for (int i = 0; i < numOfRecords; i++) {
            conv.processRecord(getDataRecord(i));
        }
        return conv;
    }
    public void close() throws IOException{
        
    }
    
    public String toString(){
        StringBuilder sb = new StringBuilder();
        int size = size();
        for(int i=0;i<size;i++){
            sb.append(i).append(": ").append(getDataRecord(i)).append("\n");
        }
        return sb.toString();
                
    }
    
}
