/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.extension.emscan;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author sashazhukov
 */
public class DataReader {
    
    public static void main(String[] args) throws FileNotFoundException, IOException {

        int baseLine = 18000;
        int integrationStart = 1900;
        int integrationEnd = 6500;
	int skip = 0;

        if (args.length < 1) {
            System.out.println("Usage WaveformReader fileName");
            System.exit(1);
        }
	if(args.length>1){
		try {
			skip = Integer.parseInt(args[1]);
		}
		catch (NumberFormatException nfe){
			
		}
	}
        
        String fileName = args[0];
        File file;
        if(fileName.endsWith(".txt")||fileName.endsWith(".csv")){
            file = FileTimeData.convertCSVData( fileName,skip);
        }
        else {
            file = new File(fileName);
        }
        
        TimeData<DataRecord2D> data = new FileTimeData<DataRecord2D>(Factories.getDataRecord2DFactory(),file);
        
        System.out.println(data.getDataRecord(0).size());
        
        Scattered2D<DataRecord2D> integral = new Scattered2D<DataRecord2D>(baseLine,integrationStart,integrationEnd);
        
        long startTime = System.currentTimeMillis();
	TimeData<? extends DataRecord> dataPoints = integral.convolve(data);        
        
        
        Logger.getLogger(FileTimeData.class.getName()).log(Level.INFO,
                "Calculating integrals {0} ms",System.currentTimeMillis()-startTime);
        System.out.println(dataPoints);
        System.out.println(dataPoints.size());
        
        data.close();


    }
    
}
