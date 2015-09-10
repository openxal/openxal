package xal.app.sclmonitor;

/**
 * Make connections to all BPM PVs in the display tables.
 * 
 * @author chu
 * 
 */
public class MakeConnections implements Runnable {
	LLRFPanel thePane;

	public MakeConnections(LLRFPanel bpmPane) {
		thePane = bpmPane;
	}

	public void run() {
		connectRBChannels();
	}

	private void connectRBChannels() {
			for (int i = 0; i < thePane.cellData.length; i++) {
				ConnectPV connectPV1 = new ConnectPV(thePane.repChs[i],
						thePane);
				Thread thread1 = new Thread(connectPV1);
				thread1.start();

				ConnectPV connectPV2 = new ConnectPV(thePane.cavVChs[i],
						thePane);
				Thread thread2 = new Thread(connectPV2);
				thread2.start();

			}
	}


}
