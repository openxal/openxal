package xal.app.diagtiming;

import xal.ca.*;

public class SetPV implements Runnable {
	Channel theCh;

	BPMPane thePane;

	int valInd = 0;

	int valInt;

	double valDbl;
	
	int row = 0;
	int col = 0;

	public SetPV(Channel ch, BPMPane bpmPane, int val, int row, int col) {
		theCh = ch;
		thePane = bpmPane;
		valInd = 0;
		valInt = val;
		this.row = row;
		this.col = col;
	}

	public SetPV(Channel ch, BPMPane bpmPane, double val, int row, int col) {
		theCh = ch;
		thePane = bpmPane;
		valInd = 1;
		valDbl = val;
		this.row = row;
		this.col = col;
	}

	public void run() {
//		theCh.addConnectionListener(thePane);
//		theCh.connectAndWait();

		try {
			switch (valInd) {
			case 0:
				theCh.putVal(valInt);
				break;
			case 1:
				theCh.putVal(valDbl);
				break;
			default:
				break;

			}
			if (row >= 0 && col >= 0)
				((InputPVTableCell)thePane.bpmTableModel.getValueAt(row,
					col)).setUpdating(true);
		} catch (PutException pe) {
			// handle analysis turns here?
			System.out.println(pe);
		} catch (ConnectionException ce) {
//			 handle analysis turns here?
			System.out.println(ce);
		}
	}
}
