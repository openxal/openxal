package xal.app.lossviewer.signals;

import xal.ca.*;
import java.util.*;
import java.util.logging.*;

public class ChargeNormalizer extends AbstractSignal implements ConnectionListener {

    public void connectionMade(Channel channel) {
            connectBCM(channel);
        
    }
    private NormalizationDevice currentBCM = null;

    private void connectBCM(final Channel channel) {
        //	System.out.println("Connected BCM: "+channel.channelName());
        Monitor m = bcmMonitors.get(channel);
        if (m == null) {
            try {
                m = channel.addMonitorValTime(new IEventSinkValTime() {

                    public void eventValue(ChannelTimeRecord record, Channel chan) {
                        boolean amIcurrentBCM = false;
                        synchronized (currentBCM) {
                            amIcurrentBCM = currentBCM.getChargePV().equals(channel.channelName());
                        }
                        if (amIcurrentBCM) {
                            long tst = (long) (record.getTimestamp().getSeconds() * 1000);
                            double v = record.doubleValue();
                            dispatcher.processNewValue(new ScalarSignalValue(ChargeNormalizer.this, tst, v*currentBCM.getScale()));
                        //		System.out.println(chan.channelName()+" is used, charge is "+v);
                        }
                    }
                }, 1);
                bcmMonitors.put(channel, m);
            } catch (MonitorException e) {
            } catch (ConnectionException e) {
            }
        }
    }
    
    public  void setBCM(NormalizationDevice newBCM){
        synchronized(currentBCM){
            currentBCM=newBCM;
            Logger.getLogger(ChargeNormalizer.this.getClass().getCanonicalName()).log(Level.INFO, 
                    ("Normalization BCM changed to " + currentBCM.getName()));
        }
    }
   

    public void connectionDropped(Channel channel) {
        Logger.getLogger(this.getClass().getCanonicalName()).log(Level.WARNING, ("Connection dropped " + channel.channelName()));
    //	System.out.println("Connection dropped: " + getID() + " " + channel.channelName());
    }

    Monitor machineModeMonitor;
    Map<String, Channel> bcmChannels = new HashMap<String, Channel>();
    Map<Channel, Monitor> bcmMonitors = new HashMap<Channel, Monitor>();
   List<NormalizationDevice> normBCMs;
    String machineModeName;

    public ChargeNormalizer(List<NormalizationDevice> bcmNames) {
       
        normBCMs = bcmNames;
        setName("Charge");
        currentBCM=normBCMs.get(0);
    }

    public void close() {

        for (String bcmName : bcmChannels.keySet()) {
            bcmChannels.get(bcmName).disconnect();
            Channel.flushIO();
        }
    }

    /**
     * Method start
     *
     */
    public void start() {

        ChannelFactory cf = ChannelFactory.defaultFactory();
        for (NormalizationDevice bcm : normBCMs) {
            Channel ch = cf.getChannel(bcm.getChargePV());
            ch.addConnectionListener(this);
            ch.requestConnection();
            bcmChannels.put(bcm.getChargePV(), ch);
            bcmMonitors.put(ch, null);

        }

    }

    public double doubleValue() {
        return 1.0;
    }

    public boolean setValue(double v) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
