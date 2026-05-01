/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.extension.emscan;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author sashazhukov
 */
public class DefaultTimeData<E extends DataRecord> extends AbstractTimeData<E> {

    private List<E> data;

    public DefaultTimeData(DataRecordFactory<E> factory) {
        super(factory);
        data = new ArrayList<>();
    }
    
    public void addRecord(E record){
        data.add(record);
    }

    @Override
    public E getDataRecord(int index) throws IndexOutOfBoundsException {
        return data.get(index);
    }

    @Override
    public int size() {
        return data.size();
    }
}
