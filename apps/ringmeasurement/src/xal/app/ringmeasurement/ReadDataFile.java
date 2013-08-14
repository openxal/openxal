package xal.app.ringmeasurement;

import java.io.*;

public class ReadDataFile {
	long bpmPVLogId = 0;
	long defPVLogId = 0;
	
	public ReadDataFile(File file) {
        try {
            BufferedReader in = new BufferedReader(
            new InputStreamReader(new FileInputStream(file)));
		
            String line;
            
            while ((line=in.readLine()) != null) {
                String[] tokens = line.split("\\s+");
                
                String firstName = tokens[0];
            	
                if(firstName.startsWith("PVLoggerID")) {
        		    defPVLogId = Integer.parseInt(tokens[2]);
        		    bpmPVLogId = Integer.parseInt(tokens[5]);
                }
            }
            
            in.close();
        } catch (FileNotFoundException e) {
            System.out.println("Cannot find file: " + file.getPath());
        } catch (IOException e) {
            System.out.println("File reading error: " + file.getPath());
        }
		
	}
	
	protected long getBPMPVLogId() {
		return bpmPVLogId;
	}
	
	protected long getDefPVLogId() {
		return defPVLogId;
	}
}
