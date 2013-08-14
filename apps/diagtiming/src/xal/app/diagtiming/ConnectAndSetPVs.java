package xal.app.diagtiming;

import java.text.NumberFormat;
import java.text.ParseException;

public class ConnectAndSetPVs implements Runnable {
	BPMPane thePane;
	int theBPM;
	int typeInd = 0;
	
	NumberFormat nf = NumberFormat.getNumberInstance();

/*	public ConnectAndSetPVs(BPMPane bpmPane, int typeInd) {
		thePane = bpmPane;
		this.typeInd = typeInd;
		runInd = 0;
	}
	
*/	
	public ConnectAndSetPVs(BPMPane bpmPane, int bpmInd, int typeInd) {
		thePane = bpmPane;
		theBPM = bpmInd;
		this.typeInd = typeInd;		
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
				connectChannels();
	}
	
	private void connectChannels() {
		// for linac BPMs
		if (this.typeInd == 0) {
/*			for (int i = 0; i < thePane.bpmDelays.length; i++) {
				ConnectPV connectPV1 = new ConnectPV(thePane.bpmDelays[i],
						thePane);
				Thread thread1 = new Thread(connectPV1);
				thread1.start();

				ConnectPV connectPV2 = new ConnectPV(thePane.bpmAvgWidths[i],
						thePane);
				Thread thread2 = new Thread(connectPV2);
				thread2.start();

				ConnectPV connectPV3 = new ConnectPV(thePane.bpmWidths[i],
						thePane);
				Thread thread3 = new Thread(connectPV3);
				thread3.start();

				ConnectPV connectPV4 = new ConnectPV(thePane.bpmSamplings[i],
						thePane);
				Thread thread4 = new Thread(connectPV4);
				thread4.start();

			}
*/		}
		// for ring BPMs
		else {
//			for (int i = 0; i < thePane.bpmDelays.length; i++) {
				try {	
				SetPV connectPV0 = new SetPV(thePane.bpmDelays[theBPM],
						thePane, nf.parse(
								((InputPVTableCell) thePane.bpmTableModel.getValueAt(theBPM, 2))
								.toString()).doubleValue(), theBPM, 2);
				Thread thread0 = new Thread(connectPV0);
				thread0.start();				
				
//				ConnectPV connectPV00 = new ConnectPV(thePane.trigEvtSetChs[theBPM],
//						thePane);
//				Thread thread00 = new Thread(connectPV00);
//				thread00.start();	
				
				// the analysis methods don't have corresponding readback PVs!
				// We set the anaysis turns to the same as acquire turns
				SetPV connectPV13 = new SetPV(thePane.st1MthdLenChs[theBPM],
						thePane, nf.parse(
								((InputPVTableCell) thePane.bpmTableModel.getValueAt(theBPM, 4))
								.toString()).intValue(), -1, -1);
				Thread thread13 = new Thread(connectPV13);
				thread13.start();
				
				SetPV connectPV14 = new SetPV(thePane.st2MthdLenChs[theBPM],
						thePane, nf.parse(
								((InputPVTableCell) thePane.bpmTableModel.getValueAt(theBPM, 7))
								.toString()).intValue(), -1, -1);
				Thread thread14 = new Thread(connectPV14);
				thread14.start();
				
				SetPV connectPV15 = new SetPV(thePane.st3MthdLenChs[theBPM],
						thePane, nf.parse(
								((InputPVTableCell) thePane.bpmTableModel.getValueAt(theBPM, 10))
								.toString()).intValue(), -1, -1);
				Thread thread15 = new Thread(connectPV15);
				thread15.start();
				
				SetPV connectPV16 = new SetPV(thePane.st4MthdLenChs[theBPM],
						thePane, nf.parse(
								((InputPVTableCell) thePane.bpmTableModel.getValueAt(theBPM, 13))
								.toString()).intValue(), -1, -1);
				Thread thread16 = new Thread(connectPV16);
				thread16.start();
				
				
				SetPV connectPV1 = new SetPV(thePane.st1LenSetChs[theBPM],
						thePane, nf.parse(
								((InputPVTableCell) thePane.bpmTableModel.getValueAt(theBPM, 4))
								.toString()).intValue(), theBPM, 4);
				Thread thread1 = new Thread(connectPV1);
				thread1.start();

				SetPV connectPV2 = new SetPV(thePane.st1GainSetChs[theBPM],
						thePane, nf.parse(
								((InputPVTableCell) thePane.bpmTableModel.getValueAt(theBPM, 5))
								.toString()).intValue(), theBPM, 5);
				Thread thread2 = new Thread(connectPV2);
				thread2.start();

				SetPV connectPV3 = new SetPV(thePane.st1MthdSetChs[theBPM],
						thePane, nf.parse(
								((InputPVTableCell) thePane.bpmTableModel.getValueAt(theBPM, 6))
								.toString()).intValue(), theBPM, 6);
				Thread thread3 = new Thread(connectPV3);
				thread3.start();

				SetPV connectPV4 = new SetPV(thePane.st2LenSetChs[theBPM],
						thePane, nf.parse(
								((InputPVTableCell) thePane.bpmTableModel.getValueAt(theBPM, 7))
								.toString()).intValue(), theBPM, 7);
				Thread thread4 = new Thread(connectPV4);
				thread4.start();

				SetPV connectPV5 = new SetPV(thePane.st2GainSetChs[theBPM],
						thePane, nf.parse(
								((InputPVTableCell) thePane.bpmTableModel.getValueAt(theBPM, 8))
								.toString()).intValue(), theBPM, 8);
				Thread thread5 = new Thread(connectPV5);
				thread5.start();

				SetPV connectPV6 = new SetPV(thePane.st2MthdSetChs[theBPM],
						thePane, nf.parse(
								((InputPVTableCell) thePane.bpmTableModel.getValueAt(theBPM, 9))
								.toString()).intValue(), theBPM, 9);
				Thread thread6 = new Thread(connectPV6);
				thread6.start();

				SetPV connectPV7 = new SetPV(thePane.st3LenSetChs[theBPM],
						thePane, nf.parse(
								((InputPVTableCell) thePane.bpmTableModel.getValueAt(theBPM, 10))
								.toString()).intValue(), theBPM, 10);
				Thread thread7 = new Thread(connectPV7);
				thread7.start();

				SetPV connectPV8 = new SetPV(thePane.st3GainSetChs[theBPM],
						thePane, nf.parse(
								((InputPVTableCell) thePane.bpmTableModel.getValueAt(theBPM, 11))
								.toString()).intValue(), theBPM, 11);
				Thread thread8 = new Thread(connectPV8);
				thread8.start();

				SetPV connectPV9 = new SetPV(thePane.st3MthdSetChs[theBPM],
						thePane, nf.parse(
								((InputPVTableCell) thePane.bpmTableModel.getValueAt(theBPM, 12))
								.toString()).intValue(), theBPM, 12);
				Thread thread9 = new Thread(connectPV9);
				thread9.start();

				SetPV connectPV10 = new SetPV(thePane.st4LenSetChs[theBPM],
						thePane, nf.parse(
								((InputPVTableCell) thePane.bpmTableModel.getValueAt(theBPM, 13))
								.toString()).intValue(), theBPM, 13);
				Thread thread10 = new Thread(connectPV10);
				thread10.start();

				SetPV connectPV11 = new SetPV(thePane.st4GainSetChs[theBPM],
						thePane, nf.parse(
								((InputPVTableCell) thePane.bpmTableModel.getValueAt(theBPM, 14))
								.toString()).intValue(), theBPM, 14);
				Thread thread11 = new Thread(connectPV11);
				thread11.start();

				SetPV connectPV12 = new SetPV(thePane.st4MthdSetChs[theBPM],
						thePane, nf.parse(
								((InputPVTableCell) thePane.bpmTableModel.getValueAt(theBPM, 15))
								.toString()).intValue(), theBPM, 15);
				Thread thread12 = new Thread(connectPV12);
				thread12.start();
				
				SetPV connectPV17 = new SetPV(thePane.opModeChs[theBPM],
						thePane, nf.parse(
								((InputPVTableCell) thePane.bpmTableModel.getValueAt(theBPM, 16))
								.toString()).intValue(), theBPM, 16);
				Thread thread17 = new Thread(connectPV17);
				thread17.start();
				
				SetPV connectPV18 = new SetPV(thePane.freqChs[theBPM],
						thePane, nf.parse(
								((InputPVTableCell) thePane.bpmTableModel.getValueAt(theBPM, 17))
								.toString()).doubleValue(), theBPM, 17);
				Thread thread18 = new Thread(connectPV18);
				thread18.start();
				
				SetPV connectPV19 = new SetPV(thePane.betaChs[theBPM],
						thePane, nf.parse(
								((InputPVTableCell) thePane.bpmTableModel.getValueAt(theBPM, 18))
								.toString()).doubleValue(), theBPM, 18);
				Thread thread19 = new Thread(connectPV19);
				thread19.start();
				
				SetPV connectPV20 = new SetPV(thePane.trnDlyChs[theBPM],
						thePane, nf.parse(
								((InputPVTableCell) thePane.bpmTableModel.getValueAt(theBPM, 19))
								.toString()).doubleValue(), theBPM, 19);
				Thread thread20 = new Thread(connectPV20);
				thread20.start();
				
				} catch (ParseException pe) {
					
				}

//			}
		}
		
	}

}
