package gov.sns.apps.lossviewer2.detectors;

import gov.sns.apps.lossviewer2.*;
import gov.sns.apps.lossviewer2.signals.*;
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
