/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.extension.emscan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import xal.ca.Channel;
import xal.ca.ChannelFactory;
import xal.ca.ChannelTimeRecord;
import xal.ca.ConnectionException;
import xal.ca.IEventSinkValTime;
import xal.ca.MonitorException;

/**
 *
 * @author az9
 */
public class TestActuator {

	static ChannelFactory cf = ChannelFactory.defaultFactory();
	private static String usageMessage = "Usage: actuatorName destination step [signal1] [signal2] ...";

	public static void main(String[] args) throws InterruptedException, ConnectionException, MonitorException {

		if (args.length < 3) {
			System.out.println(usageMessage);
			System.exit(0);
		}

		String actName = args[0];
		double destination = Double.parseDouble(args[1]);
		double stepSize = Double.parseDouble(args[2]);

		int timeout = 10000;

		List<String> signals = new ArrayList<>();
		Map<String, Double> signalMap = new HashMap<>();

		IEventSinkValTime mon = new IEventSinkValTime() {
			@Override
			public void eventValue(ChannelTimeRecord ctr, Channel chnl) {
				String name = chnl.channelName();
				double value = ctr.doubleValue();
				signalMap.put(name, value);
			}

		};

		if (args.length > 3) {
			for (int i = 3; i < args.length; i++) {
				Channel signal = cf.getChannel(args[i]);
				signal.connectAndWait(2.0);
				signal.addMonitorValTime(mon,1);
				signals.add(args[i]);

			}
		}

		Actuator actuator = new Actuator(actName, cf, false, "BTF");

		double initialPosition = actuator.getPosition();

		System.out.println("# Name = " + actName);
		System.out.println("# Found at " + initialPosition + " will move to " + destination + " with step size " + stepSize);
		StringBuilder sBuf = new StringBuilder("# Destination (mm), Position (mm), Encoder (mm)");
		
		for (String signal : signals) {
			sBuf.append(", " + signal);
		}
		System.out.println(sBuf);
		
		

		sBuf = new StringBuilder(initialPosition + " " + initialPosition + " " + actuator.getPositionEnc());
		for (String signal : signals) {
			sBuf.append(" " + signalMap.get(signal));
		}
		System.out.println(sBuf);

		double nextPosition = initialPosition;
		stepSize = (destination > initialPosition) ? stepSize : -stepSize;
		while (nextPosition < destination) {
			nextPosition += stepSize;
			if (!actuator.moveAndWait(timeout, nextPosition)) {
				System.out.println("Didn't reach " + nextPosition + " after " + timeout + "(ms).\n Exiting...");
				break;
			}
			Thread.sleep(500);
			
			sBuf = new StringBuilder(nextPosition + " " + actuator.getPosition() + " " + actuator.getPositionEnc());
			for (String signal : signals) {
				sBuf.append(" " + signalMap.get(signal));
			}
			
			System.out.println(sBuf);

		}

		actuator.close();

	}

}
