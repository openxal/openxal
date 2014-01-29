package xal.app.lossviewer.signals;

import xal.ca.*;
import java.util.*;
import java.util.logging.*;

public class ChargeNormalizer extends AbstractSignal implements ConnectionListener {

    public void connectionMade(Channel channel) {
            connectBCM(channel);
        
    }
    private String currentBCMPVname = "";

    private void connectBCM(final Channel channel) {
        //	System.out.println("Connected BCM: "+channel.channelName());
        Monitor m = bcmMonitors.get(channel);
        if (m == null) {
            try {
                m = channel.addMonitorValTime(new IEventSinkValTime() {

                    public void eventValue(ChannelTimeRecord record, Channel chan) {
                        boolean amIcurrentBCM = false;
                        synchronized (currentBCMPVname) {
                            amIcurrentBCM = currentBCMPVname.equals(channel.channelName());
                        }
                        if (amIcurrentBCM) {
                            long tst = (long) (record.getTimestamp().getSeconds() * 1000);
                            double v = record.doubleValue();
                            dispatcher.processNewValue(new ScalarSignalValue(ChargeNormalizer.this, tst, v));
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
        synchronized(currentBCMPVname){
            currentBCMPVname=newBCM.getChargePV();
            Logger.getLogger(ChargeNormalizer.this.getClass().getCanonicalName()).log(Level.INFO, ("Normalization BCM changed to " + currentBCMPVname));
        }
    }
   

    public void connectionDropped(Channel channel) {
        Logger.getLogger(this.getClass().getCanonicalName()).log(Level.WARNING, ("Connection dropped " + channel.channelName()));
    //	System.out.println("Connection dropped: " + getID() + " " + channel.channelName());
    }
    Channel machineModeChannel;
    Monitor machineModeMonitor;
    Map<String, Channel> bcmChannels = new HashMap<String, Channel>();
    Map<Channel, Monitor> bcmMonitors = new HashMap<Channel, Monitor>();
   List<NormalizationDevice> normBCMs;
    String machineModeName;

    public ChargeNormalizer(String machineModeName, List<NormalizationDevice> bcmNames) {
        this.machineModeName = machineModeName;
        normBCMs = bcmNames;
        setName("Charge");
        currentBCMPVname=normBCMs.get(0).getChargePV();
    }

    public void close() {
        machineModeChannel.disconnect();
        machineModeChannel.flushIO();
        for (String bcmName : bcmChannels.keySet()) {
            bcmChannels.get(bcmName).disconnect();
            bcmChannels.get(bcmName).flushIO();
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
