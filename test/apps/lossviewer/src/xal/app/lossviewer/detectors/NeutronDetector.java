package xal.app.lossviewer.detectors;

import xal.app.lossviewer.*;
import xal.app.lossviewer.signals.*;
import java.util.*;

public class NeutronDetector extends IonizationChamber {
	
	private static final double NOISE_THRESHOLD = 0;
	
	public String getType() {

		return "ND";
	}
	public double getNoiseThreshold(){
		return NOISE_THRESHOLD;
	}
        public double getDefaultWeight(){
            return 0.0;
        }

	
}
