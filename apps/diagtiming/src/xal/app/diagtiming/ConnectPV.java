package xal.app.diagtiming;

import xal.ca.*;

public class ConnectPV implements Runnable {

	Channel theCh;
	
	BPMPane thePane;
	
	public ConnectPV(Channel ch, BPMPane bpmPane) {
		theCh = ch;
		thePane = bpmPane;
	}
	public ConnectPV(Channel ch) {
		theCh = ch;
	}
	public void run() {
		if (thePane != null)
			theCh.addConnectionListener(thePane);
		
		theCh.requestConnection();
	}

}