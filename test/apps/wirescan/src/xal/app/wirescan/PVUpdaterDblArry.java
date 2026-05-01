/*
 * PVUpdaterDblArry.java
 */

package xal.app.wirescan;

import xal.ca.*;

/**
 * This class monitors a channel that returns a double array for updates.
 * A PVUpdaterDblArry connects to a channel and stores a new
 * record as a double array in the WireData class.
 *
 * @author	S. Bunch
 * @version	1.0
 * @see PVUpdater
 * @see WireData
 */
public class PVUpdaterDblArry extends PVUpdater implements Runnable 
{
	private String theID;
	private WireDoc theDoc;
	private WirePanel thePanel;
	private WireData wd;
        
        int chan_flag = 0;
        Channel posChannel;
        
        private double position = 0.0;
        private double yData = 0.;
        
        static int V_Data = 0;
        static int D_Data = 1;
        static int H_Data = 2;
        
        private int vCounter = 0;
	private int dCounter = 0;
	private int hCounter = 0;
	
	/**
	 * The PVUpdaterDblArry constructor takes a Channel, String, and WireDoc type.
	 *
	 * @param channel		The channel to be used
	 * @param wsID		The ID of the wirescanner
	 * @param wiredocument	The WireDoc currently used
         * @param ch_flag inicating which channel it represents (0:vert, 1:diag, 2:hori)
         * @param pos_channel channel for live position data
	 */
	public PVUpdaterDblArry(Channel channel, String wsID, WireDoc wiredocument, int ch_flag,
                                Channel pos_channel, WirePanel panel){
		theDoc = wiredocument;
                thePanel = panel;
		theID = wsID;                
                chan_flag = ch_flag;
                posChannel = pos_channel;
                
		/* Get the current WireData */
		wd = theDoc.wireDataMap.get((Object) theID);
                
		theChannel = channel;
		theChannel.addConnectionListener(this);
                
		if(!theChannel.isConnected()) {
			theChannel.connectAndWait();
		}
		else makeMonitor(theChannel);
                
                if (!posChannel.isConnected()) {
                    posChannel.connectAndWait();
                }
	}

	/**
	 * The eventValue function is overloaded to store a specified double array
	 * to a given WireData value.
	 * The raw data values are negated to allow for better plotting with post
	 * scan data plot comparison.
	 *
	 * @param newRecord		The value that is checked for changes.
	 * @param chan		The channel that is being monitored.
	 * @see ChannelRecord
	 * @see Channel
	 * @see WireData
	 */
	public void eventValue(ChannelRecord newRecord, Channel chan) {
            yData = newRecord.doubleValue();
            refreshGraph();
	}
        
        public void refreshGraph() {
            Thread update = new Thread(this);
            update.start();
        }
        
        public void run() {
            
            try {
                position = posChannel.getValDbl();
//		System.out.println("position = " + position);
            } catch (ConnectionException e) {
                System.out.println(e);
            } catch (GetException e) {
                System.out.println(e);
            }
            
            addPoint(chan_flag);
            
        }
        
        private void addPoint(int flag) {
//	    System.out.println("chan_flag = " + chan_flag);
//	    boolean status = true;
            switch(flag) {
                // for vertical live data
                case 0:
                    vCounter = wd.addPoint(vCounter, position, yData, PVUpdaterDblArry.V_Data);
//            	    vCounter = vCounter + 1; 
		    break;
                // for diagonal live data
                case 1:
                    dCounter = wd.addPoint(dCounter, position, yData, PVUpdaterDblArry.D_Data);
//            	    dCounter = dCounter + 1;
		    break;
                // for horizontal live data
                case 2:
                    hCounter = wd.addPoint(hCounter, position, yData, PVUpdaterDblArry.H_Data);
//            	    hCounter = hCounter + 1;
		    break;
            }

        }
        
        public void reset() {
		wd.resetData();
		vCounter = 0;
		dCounter = 0;
		hCounter = 0;
        }
}
