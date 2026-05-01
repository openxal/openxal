package xal.app.diagtiming;

import xal.ca.Channel;
import xal.ca.ChannelFactory;

/**
 * Make connections to all BPM PVs in the display tables.
 * 
 * @author chu
 * 
 */
public class MakeConnections implements Runnable {
	BPMPane thePane;
	BCMPane theBCMPane;

	int typeInd = 0;

	public MakeConnections(BPMPane bpmPane, int typeInd) {
		thePane = bpmPane;
		this.typeInd = typeInd;
	}

	public MakeConnections(BCMPane bcmPane, int typeInd) {
		theBCMPane = bcmPane;
		this.typeInd = typeInd;
	}

	public void run() {
		connectRBChannels();
		connectSetChannels();		
	}

	private void connectRBChannels() {
		// for linac BPMs
		if (this.typeInd == 0) {
			for (int i = 0; i < thePane.bpmDelays.length; i++) {
				ConnectPV connectPV1 = new ConnectPV(thePane.bpmDelays[i],
						thePane);
				Thread thread1 = new Thread(connectPV1);
				thread1.start();

				ConnectPV connectPV2 = new ConnectPV(thePane.bpmAvgStarts[i],
						thePane);
				Thread thread2 = new Thread(connectPV2);
				thread2.start();

				ConnectPV connectPV3 = new ConnectPV(thePane.bpmAvgStops[i],
						thePane);
				Thread thread3 = new Thread(connectPV3);
				thread3.start();

				ConnectPV connectPV4 = new ConnectPV(thePane.bpmChopFreqs[i],
						thePane);
				Thread thread4 = new Thread(connectPV4);
				thread4.start();

				ConnectPV connectPV5 = new ConnectPV(thePane.xWFChs[i], thePane);
				Thread thread5 = new Thread(connectPV5);
				thread5.start();

				ConnectPV connectPV6 = new ConnectPV(thePane.yWFChs[i], thePane);
				Thread thread6 = new Thread(connectPV6);
				thread6.start();

				ConnectPV connectPV7 = new ConnectPV(thePane.bpmTDelays[i],
						thePane);
				Thread thread7 = new Thread(connectPV7);
				thread7.start();

			}
		}
		else if (this.typeInd == 4){
			for (int i = 0; i < theBCMPane.bcmNames.length; i++) {
				ConnectPV connectPV1 = new ConnectPV(theBCMPane.bcmWFChs[i]);
				Thread thread1 = new Thread(connectPV1);
				thread1.start();
			}
		}
		// for ring BPMs
		else {
			for (int i = 0; i < thePane.bpmDelays.length; i++) {
				ConnectPV connectPV0 = new ConnectPV(thePane.bpmDelays[i],
						thePane);
				Thread thread0 = new Thread(connectPV0);
				thread0.start();

				ConnectPV connectPV0a = new ConnectPV(thePane.bpmDelayRBs[i],
						thePane);
				Thread thread0a = new Thread(connectPV0a);
				thread0a.start();

				ConnectPV connectPV00 = new ConnectPV(thePane.trigEvtChs[i],
						thePane);
				Thread thread00 = new Thread(connectPV00);
				thread00.start();

				ConnectPV connectPV00a = new ConnectPV(thePane.trigEvtRbChs[i],
						thePane);
				Thread thread00a = new Thread(connectPV00a);
				thread00a.start();

				ConnectPV connectPV1 = new ConnectPV(thePane.st1LenChs[i],
						thePane);
				Thread thread1 = new Thread(connectPV1);
				thread1.start();

				ConnectPV connectPV2 = new ConnectPV(thePane.st1GainChs[i],
						thePane);
				Thread thread2 = new Thread(connectPV2);
				thread2.start();

				ConnectPV connectPV3 = new ConnectPV(thePane.st1MthdChs[i],
						thePane);
				Thread thread3 = new Thread(connectPV3);
				thread3.start();

				ConnectPV connectPV4 = new ConnectPV(thePane.st2LenChs[i],
						thePane);
				Thread thread4 = new Thread(connectPV4);
				thread4.start();

				ConnectPV connectPV5 = new ConnectPV(thePane.st2GainChs[i],
						thePane);
				Thread thread5 = new Thread(connectPV5);
				thread5.start();

				ConnectPV connectPV6 = new ConnectPV(thePane.st2MthdChs[i],
						thePane);
				Thread thread6 = new Thread(connectPV6);
				thread6.start();

				ConnectPV connectPV7 = new ConnectPV(thePane.st3LenChs[i],
						thePane);
				Thread thread7 = new Thread(connectPV7);
				thread7.start();

				ConnectPV connectPV8 = new ConnectPV(thePane.st3GainChs[i],
						thePane);
				Thread thread8 = new Thread(connectPV8);
				thread8.start();

				ConnectPV connectPV9 = new ConnectPV(thePane.st3MthdChs[i],
						thePane);
				Thread thread9 = new Thread(connectPV9);
				thread9.start();

				ConnectPV connectPV10 = new ConnectPV(thePane.st4LenChs[i],
						thePane);
				Thread thread10 = new Thread(connectPV10);
				thread10.start();

				ConnectPV connectPV11 = new ConnectPV(thePane.st4GainChs[i],
						thePane);
				Thread thread11 = new Thread(connectPV11);
				thread11.start();

				ConnectPV connectPV12 = new ConnectPV(thePane.st4MthdChs[i],
						thePane);
				Thread thread12 = new Thread(connectPV12);
				thread12.start();

				ConnectPV connectPV13 = new ConnectPV(thePane.opModeRbChs[i],
						thePane);
				Thread thread13 = new Thread(connectPV13);
				thread13.start();

				ConnectPV connectPV14 = new ConnectPV(thePane.freqRbChs[i],
						thePane);
				Thread thread14 = new Thread(connectPV14);
				thread14.start();

				ConnectPV connectPV15 = new ConnectPV(thePane.betaRbChs[i],
						thePane);
				Thread thread15 = new Thread(connectPV15);
				thread15.start();

				ConnectPV connectPV16 = new ConnectPV(thePane.trnDlyRbChs[i],
						thePane);
				Thread thread16 = new Thread(connectPV16);
				thread16.start();

				ConnectPV connectPV17 = new ConnectPV(thePane.xWFChs[i],
						thePane);
				Thread thread17 = new Thread(connectPV17);
				thread17.start();

				ConnectPV connectPV18 = new ConnectPV(thePane.yWFChs[i],
						thePane);
				Thread thread18 = new Thread(connectPV18);
				thread18.start();

				ConnectPV connectPV19 = new ConnectPV(thePane.gainSatChs[i],
						thePane);
				Thread thread19 = new Thread(connectPV19);
				thread19.start();
			}
		}
	}

	private void connectSetChannels() {
		// for linac BPMs
		if (this.typeInd == 0) {
			ChannelFactory caF = ChannelFactory.defaultFactory();			
			for (int i = 0; i < thePane.bpmDelays.length; i++) {
				Channel bpmWidthSetCh = caF.getChannel(thePane.bpmNames[i] + ":IntgrlLength");
				Channel bpmTDelaySetCh = caF.getChannel(thePane.bpmNames[i] + ":Delay00");
				Channel bpmAvgWidthSetCh = caF.getChannel(thePane.bpmNames[i] + ":pulNum");
				Channel bpmSamplingSetCh = caF.getChannel(thePane.bpmNames[i] + ":pulAv");
				
				ConnectPV connectPV1 = new ConnectPV(bpmWidthSetCh,
						thePane);
				Thread thread1 = new Thread(connectPV1);
				thread1.start();

				ConnectPV connectPV2 = new ConnectPV(bpmTDelaySetCh,
						thePane);
				Thread thread2 = new Thread(connectPV2);
				thread2.start();

				ConnectPV connectPV3 = new ConnectPV(bpmAvgWidthSetCh,
						thePane);
				Thread thread3 = new Thread(connectPV3);
				thread3.start();

				ConnectPV connectPV4 = new ConnectPV(bpmSamplingSetCh,
						thePane);
				Thread thread4 = new Thread(connectPV4);
				thread4.start();
				
			}
		}
		else if (this.typeInd == 4) {
			// no need to do anything for BCMs
		}
		// for Ring BPMs
		else {
			for (int i = 0; i < thePane.bpmDelays.length; i++) {
				ConnectPV connectPV0 = new ConnectPV(thePane.bpmDelays[i],
						thePane);
				Thread thread0 = new Thread(connectPV0);
				thread0.start();

				// ConnectPV connectPV00 = new
				// ConnectPV(thePane.trigEvtSetChs[i],
				// thePane);
				// Thread thread00 = new Thread(connectPV00);
				// thread00.start();

				ConnectPV connectPV13 = new ConnectPV(thePane.st1MthdLenChs[i],
						thePane);
				Thread thread13 = new Thread(connectPV13);
				thread13.start();

				ConnectPV connectPV14 = new ConnectPV(thePane.st2MthdLenChs[i],
						thePane);
				Thread thread14 = new Thread(connectPV14);
				thread14.start();

				ConnectPV connectPV15 = new ConnectPV(thePane.st3MthdLenChs[i],
						thePane);
				Thread thread15 = new Thread(connectPV15);
				thread15.start();

				ConnectPV connectPV16 = new ConnectPV(thePane.st4MthdLenChs[i],
						thePane);
				Thread thread16 = new Thread(connectPV16);
				thread16.start();

				ConnectPV connectPV1 = new ConnectPV(thePane.st1LenSetChs[i],
						thePane);
				Thread thread1 = new Thread(connectPV1);
				thread1.start();

				ConnectPV connectPV2 = new ConnectPV(thePane.st1GainSetChs[i],
						thePane);
				Thread thread2 = new Thread(connectPV2);
				thread2.start();

				ConnectPV connectPV3 = new ConnectPV(thePane.st1MthdSetChs[i],
						thePane);
				Thread thread3 = new Thread(connectPV3);
				thread3.start();

				ConnectPV connectPV4 = new ConnectPV(thePane.st2LenSetChs[i],
						thePane);
				Thread thread4 = new Thread(connectPV4);
				thread4.start();

				ConnectPV connectPV5 = new ConnectPV(thePane.st2GainSetChs[i],
						thePane);
				Thread thread5 = new Thread(connectPV5);
				thread5.start();

				ConnectPV connectPV6 = new ConnectPV(thePane.st2MthdSetChs[i],
						thePane);
				Thread thread6 = new Thread(connectPV6);
				thread6.start();

				ConnectPV connectPV7 = new ConnectPV(thePane.st3LenSetChs[i],
						thePane);
				Thread thread7 = new Thread(connectPV7);
				thread7.start();

				ConnectPV connectPV8 = new ConnectPV(thePane.st3GainSetChs[i],
						thePane);
				Thread thread8 = new Thread(connectPV8);
				thread8.start();

				ConnectPV connectPV9 = new ConnectPV(thePane.st3MthdSetChs[i],
						thePane);
				Thread thread9 = new Thread(connectPV9);
				thread9.start();

				ConnectPV connectPV10 = new ConnectPV(thePane.st4LenSetChs[i],
						thePane);
				Thread thread10 = new Thread(connectPV10);
				thread10.start();

				ConnectPV connectPV11 = new ConnectPV(thePane.st4GainSetChs[i],
						thePane);
				Thread thread11 = new Thread(connectPV11);
				thread11.start();

				ConnectPV connectPV12 = new ConnectPV(thePane.st4MthdSetChs[i],
						thePane);
				Thread thread12 = new Thread(connectPV12);
				thread12.start();

				ConnectPV connectPV17 = new ConnectPV(thePane.opModeChs[i],
						thePane);
				Thread thread17 = new Thread(connectPV17);
				thread17.start();

				ConnectPV connectPV18 = new ConnectPV(thePane.freqChs[i],
						thePane);
				Thread thread18 = new Thread(connectPV18);
				thread18.start();

				ConnectPV connectPV19 = new ConnectPV(thePane.betaChs[i],
						thePane);
				Thread thread19 = new Thread(connectPV19);
				thread19.start();

				ConnectPV connectPV20 = new ConnectPV(thePane.trnDlyChs[i],
						thePane);
				Thread thread20 = new Thread(connectPV20);
				thread20.start();
				
			}
		}

	}

}
