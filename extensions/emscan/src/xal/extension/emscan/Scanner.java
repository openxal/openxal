/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.extension.emscan;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import xal.ca.Channel;
import xal.ca.ChannelFactory;
import xal.ca.ConnectionException;
import xal.ca.MonitorException;

/**
 *
 * @author sashazhukov
 */
public class Scanner {

    private boolean paused;
    private boolean aborted;
    private final ScanStatusListener<String> statusListener;
    private final ScanDataListener dataListener;

    public void setPaused(boolean b) {
        paused = b;
    }
    public Scanner(){
        this(null,null);
    }
    public Scanner(ScanStatusListener<String> sl, ScanDataListener dl) {
        this.statusListener=sl;
        this.dataListener = dl;
        paused = true;
        aborted = false;
    }

    public void runAsync(Map<String, String> params) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Scanner.this.run(params);

            }
        }).start();
    }

    public void run(Map<String, String> params) {

        String slitName = params.get("Slit");
        String harpName = params.get("Harp");

        String slitRb = params.get("SlitRb");
        String harpRb = params.get("HarpRb");
        String signal = params.get("Signal");

        String systemType = params.get("SystemType");

        double slitInitial = Double.parseDouble(params.get("SlitInitial"));
        double harpInitial = Double.parseDouble(params.get("HarpInitial"));
        double slitStepSize = Double.parseDouble(params.get("SlitStepSize"));
        double harpStepSlitSize = Double.parseDouble(params.get("HarpStepSlitSize"));
        double harpSpan = Double.parseDouble(params.get("HarpSpan"));
        double harpStepSize = Double.parseDouble(params.get("HarpStepSize"));
        int stopTimeout = Integer.parseInt(params.get("StopTimeout"));
        int slitSteps = Integer.parseInt(params.get("SlitSteps"));
        double repRate = Double.parseDouble(params.get("RepRate"));
        double speed = Double.parseDouble(params.get("Speed"));
        boolean DEBUG = Boolean.parseBoolean(params.get("DEBUG"));
        boolean ADJUST = Boolean.parseBoolean(params.get("ADJUST"));
        
        String dataPath = params.get("DataPath");

        String sampleString = params.get("Samples");
        int samples = (sampleString == null) ? 0 : Integer.parseInt(sampleString);

        System.out.println("Starting scan, DEBUG is " + DEBUG + ", samples = " + samples);
      

        double swipeSpeed = Math.min(harpStepSize * repRate, speed);
        System.out.println("Speed/swiping speed " + speed + " " + swipeSpeed);
        Actuator slit = new Actuator(slitName, cf, DEBUG, systemType);
        Actuator harp = new Actuator(harpName, cf, DEBUG, systemType);

        List<Channel> recordedChannels = new ArrayList<>(3);
        List<Corrector> correctors = new ArrayList<>(3);
        recordedChannels.add(cf.getChannel(slitRb));
        recordedChannels.add(cf.getChannel(harpRb));
//        recordedChannels.add(cf.getChannel(signal));

        for (Object s : params.keySet()) {
            String key = (String) s;
            if (key.startsWith("Signal")) {
                System.out.println("Found signal " + key);
                recordedChannels.add(cf.getChannel(params.get(key)));
            } else if (key.startsWith("Corrector") && ADJUST) {
                System.out.println("Found corrector " + key);
                correctors.add(new Corrector(params.get(key), cf, DEBUG));
            }
        }

        try {
            String fileName = generateFileName(dataPath);
            Recorder recorder = new Recorder(fileName,recordedChannels, correctors, slitRb, harpRb);
            updateStatus(new ScanEvent<String>(ScanStatus.START,fileName));
            recorder.setDataListener(dataListener);
            recorder.start();

            slit.setVelocity(speed);

            double slitPosition = slitInitial;
            double harpPosition = harpInitial;
            double swipeDestination = harpPosition - harpSpan;
            double actualHarpPosition = 0;
            for (int i = 0; i < slitSteps; i++) {

                while (paused) {
                    if (aborted) {
                        break;
                    }
                    updateStatus(new ScanEvent<String>(ScanStatus.PAUSED));
                    Thread.sleep(50);
                }
                if (aborted) {
                    updateStatus(new ScanEvent<String>(ScanStatus.ABORT));
                    break;
                    
                }
                updateStatus(new ScanEvent<String>(ScanStatus.PROGRESS,(i+1)+"/"+slitSteps));

                harp.setVelocity(speed);
                //simultaneous movement before swipe
                slit.moveTo(slitPosition);

                double start = 0.0;
                double end = 0.0;
                if (Math.abs(actualHarpPosition - harpPosition) < Math.abs(actualHarpPosition - swipeDestination)) {
                    start = harpPosition;
                    end = swipeDestination;
                } else {
                    end = harpPosition;
                    start = swipeDestination;
                }

                harp.moveTo(start);
                for (Corrector c : correctors) {
                    c.set(slitPosition, start);
                }

                slit.waitForMovementStart(200);
                harp.waitForMovementStart(200);
                System.out.println("Moving slit/harp to " + slitPosition + " " + start);
                slit.waitForStatusAndDest(stopTimeout, slitPosition);
                harp.waitForStatusAndDest(stopTimeout, start);

                harp.setVelocity(swipeSpeed);
                System.out.println("Swiping harp to " + end);

                if (samples == 0) {
                    recorder.setRecording(true);

                    harp.moveAndWait(stopTimeout, end);
                    recorder.setRecording(false);
                } else {
                    double step = (end > start) ? harpStepSize : -harpStepSize;
                    int numberOfSteps = 1 + (int) Math.round((end - start) / step);
                    for (int j = 0; j < numberOfSteps; j++) {
                        double position = start + j * step;
                        System.out.println("Stepping harp to " + position);
                        harp.moveAndWait(stopTimeout, position);
                        System.out.println("Acquiring " + samples + " samples.");
                        recorder.waitForSamples(samples, slitPosition, position);
                    }
                }

                System.out.println("Swipe complete ");

                actualHarpPosition = end;

                slitPosition += slitStepSize;
                harpPosition += harpStepSlitSize;
                swipeDestination = harpPosition - harpSpan;

            }

            for (Corrector c : correctors) {
                c.restoreInitialValue();
            }

            harp.close();
            slit.close();
            recorder.convert();
            recorder.close();
            updateStatus(new ScanEvent<String>(ScanStatus.IDLE));

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
    public void abort(){
        aborted = true;
    }
    static ChannelFactory cf = ChannelFactory.defaultFactory();

    private void updateStatus(ScanEvent<String> scanEvent) {
        if(statusListener!=null){
            statusListener.statusChanged(scanEvent);
        }
    }
    private static final DateFormat DFMT = new SimpleDateFormat("yyyy-MM-dd");
    private static String generateFileName(String path){
         String fullPath= (path==null||path.equals(""))?"":path;
                long millis = System.currentTimeMillis();
                fullPath = fullPath+File.separator+DFMT.format(new Date(millis));
                
                File dir = new File(fullPath);
                if(!dir.isDirectory()){
                    dir.mkdir();
                }
                
                String fileName = fullPath+File.separator+millis + ".txt";
                System.out.println("Data file: "+fileName);
                return fileName;
    }

}
