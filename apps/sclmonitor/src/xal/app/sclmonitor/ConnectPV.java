package xal.app.sclmonitor;

import xal.ca.*;

public class ConnectPV implements Runnable {

	Channel theCh;
	
	LLRFPanel thePane;
	
	public ConnectPV(Channel ch, LLRFPanel pane) {
		theCh = ch;
		thePane = pane;
	}
	public void run() {
		theCh.addConnectionListener(thePane);
		theCh.requestConnection();
	}

}