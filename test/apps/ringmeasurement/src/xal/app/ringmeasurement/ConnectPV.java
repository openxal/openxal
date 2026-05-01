package xal.app.ringmeasurement;

import xal.ca.*;

public class ConnectPV implements Runnable {

	Channel theCh;
	
	TunePanel thePane;
	
	public ConnectPV(Channel ch, TunePanel bpmPane) {
		theCh = ch;
		thePane = bpmPane;
	}
	public void run() {
		theCh.addConnectionListener(thePane);
		theCh.requestConnection();
	}

}