package gov.sns.apps.lossviewer2;

import gov.sns.apps.lossviewer2.signals.*;

public  interface SpecialProcessor {
		
		SignalValue getValue(Dispatcher d, LossDetector ld);
	}
