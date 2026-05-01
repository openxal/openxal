/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.extension.emscan;

/**
 *
 * @author sashazhukov
 */
public class Factories {
    public static DataRecordFactory<DefaultDataRecord> getDefaultRecordFactory(){
        return new DataRecordFactory<DefaultDataRecord>() {

            @Override
            public DefaultDataRecord create(long millis, double[] data) {
                return new DefaultDataRecord(millis, data);
            }
        };
    }
    
    public static DataRecordFactory<DataRecord2D> getDataRecord2DFactory(){
        
        return new DataRecordFactory<DataRecord2D>() {

            @Override
            public DataRecord2D create(long millis, double[] data) {
                return new DataRecord2D(millis, data);
            }
        };
    }
    
    public static DataRecordFactory<Scalar2D> getScalar2DFactory(){
        
        return new DataRecordFactory<Scalar2D>() {

            @Override
            public Scalar2D create(long millis, double[] data) {
                return new Scalar2D(millis, data);
            }
        };
    }
    
    
    
    
}
