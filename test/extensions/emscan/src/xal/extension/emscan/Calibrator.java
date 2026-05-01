/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.extension.emscan;


import java.io.*;
import java.util.Properties;
import xal.ca.Channel;
import xal.ca.ChannelFactory;
import xal.ca.ConnectionException;
import xal.ca.MonitorException;

/**
 *
 * @author sashazhukov
 */
public class Calibrator {

    static ChannelFactory cf = ChannelFactory.defaultFactory();
    private static BufferedWriter saveWriter;

    public static void main(String[] args) throws ConnectionException, MonitorException, InterruptedException, FileNotFoundException, IOException {
        
        Properties p = new Properties();
        p.load(new FileInputStream(args[0]));

        String actName = p.getProperty("Actuator");
        double start = Double.parseDouble(p.getProperty("Start"));
        double end = Double.parseDouble(p.getProperty("End"));
        double step = Double.parseDouble(p.getProperty("Step"));
        
        

        int numberOfSamples = Integer.parseInt(p.getProperty("Samples"));
        int measureTime = Integer.parseInt(p.getProperty("MeasureTime"));

        Actuator actuator = new Actuator(actName, cf);

        Channel channel = cf.getChannel(actName + ":PosmmMon");

        ScalarSampler sampler = new ScalarSampler(channel);

        saveWriter = new BufferedWriter(new FileWriter(System.currentTimeMillis() + ".txt"));
        saveWriter.write("% "+actName+"\n");
        
        double position = start;
        for (int i = 0; position < end; i++) {
            StringBuilder sb = new StringBuilder();
            System.out.println("Moving to "+position);
            sb.append(position).append(",");
            actuator.moveAndWait(60000, position);

            sampler.measure(numberOfSamples);
            boolean finished = sampler.waitMeasurementDone(measureTime, true);
            sb.append(actuator.getPosition()).append(",").append(sampler.getMean())
                    .append(",").append(sampler.getSigma()).append("\n");

            System.out.println(sampler);
            System.out.println(actuator);
            saveWriter.write(sb.toString());
            position+=step;
        }


        channel.disconnect();
        saveWriter.close();
        System.exit(0);

    }
}
