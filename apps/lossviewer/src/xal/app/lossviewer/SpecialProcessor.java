package xal.app.lossviewer;

import xal.app.lossviewer.signals.*;

public  interface SpecialProcessor {
		
		SignalValue getValue(Dispatcher d, LossDetector ld);
	}
