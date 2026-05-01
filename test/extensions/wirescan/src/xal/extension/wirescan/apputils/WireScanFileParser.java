/*
 * The WireScanFileParser class parses a wire scanner data file and creates a Vector
 * of WireScanData instances with raw data and results of analysis. 
 */
 
package xal.extension.wirescan.apputils;

import java.util.Vector;
import xal.extension.widgets.plot.BasicGraphData;
import java.io.*;


/**
 *
 * @author T. Gorlov
 */
public class WireScanFileParser {

	
	/** Returns the Vector&lt;WireScanData&gt; for all WS in the WS data file */
	static public Vector<WireScanData> parseFile(File file) {
		
		Vector<WireScanData> resV = new Vector<WireScanData>();
		int count_data = 0;
		Boolean raw = false;
		int pvLogId = -1;
		WireScanData wsD = new WireScanData();	
		String str;		
		
		try {
			BufferedReader in = new BufferedReader(new FileReader(file));
			while ((str = in.readLine()) != null) {
				if (str.contains("WS")) {
					wsD = new WireScanData();
					resV.add(wsD);
					wsD.setId(str.trim());
					wsD.setWSFileName(file.getName()); 
				}
				
				if(str.contains("Raw")) raw = true;
				if(str.contains("Fit")) raw = false;
				
				if (str.contains("PVLoggerID")) {
					String [] tokens = str.split("\\s+");
					pvLogId = Integer.parseInt(tokens[2]);
				}
				
				if (raw && str.split("\\s+").length == 7 && !str.contains("Position")) {
					String [] tokens = str.trim().split("\\s+");
					double x = Double.parseDouble(tokens[4]);
					double h = Double.parseDouble(tokens[3]);
					double v = Double.parseDouble(tokens[1]);
					wsD.getRawWFX().addPoint(x, h);
					wsD.getRawWFY().addPoint(x, v);
				}
				
			}
			in.close();
			for(int i = 0; i < resV.size(); i++){
				wsD = resV.get(i);
				wsD.setPVLogId(pvLogId);
				for(int ix = 0; ix < wsD.getRawWFX().getNumbOfPoints() ; ix++){
					double x = wsD.getRawWFX().getX(ix);
					double y = wsD.getRawWFX().getY(ix);
					if(y > 0.){
						y = Math.log10(y);
						wsD.getLogRawWFX().addPoint(x,y);
					}
				}
				for(int ix = 0; ix < wsD.getRawWFY().getNumbOfPoints() ; ix++){
					double x = wsD.getRawWFY().getX(ix);
					double y = wsD.getRawWFY().getY(ix);
					if(y > 0.){
						y = Math.log10(y);
						wsD.getLogRawWFY().addPoint(x,y);
					}
				}				
			}
			return resV;
    } catch (IOException e) {
		}
		return new Vector<WireScanData>();							
	}
}

