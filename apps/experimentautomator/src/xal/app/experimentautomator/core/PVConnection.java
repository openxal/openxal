package xal.app.experimentautomator.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import xal.app.experimentautomator.exception.ChannelAccessException;
import xal.ca.Channel;
import xal.ca.ChannelFactory;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.ca.PutException;

public abstract class PVConnection {

	private static final int ATTEMPT_LIMIT = 3;
	private static final long SLEEP_LENGTH = 500; // milliseconds

	/**
	 * Reads a HashMap of PV names and values to set them to. Attempts to set
	 * these values
	 * 
	 * @param pvMapCopy
	 * @throws ChannelAccessException
	 */
	public static void setPvValues(Map<String, Double> pvMap)
			throws ChannelAccessException {
		HashMap<String, Double> pvMapCopy = new HashMap<>(pvMap);
		ChannelFactory cf = ChannelFactory.defaultFactory();

		Iterator<Entry<String, Double>> it = pvMapCopy.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Double> pvPair = it.next();
			it.remove(); // avoids a ConcurrentModificationException
			String pvName = pvPair.getKey();
			Double pvValue = pvPair.getValue();
			Channel ch = cf.getChannel(pvPair.getKey());

			for (int i = 0; i <= ATTEMPT_LIMIT; ) {

				i++;
				if (i >= ATTEMPT_LIMIT)
					throw new ChannelAccessException(
							"Cannot Connect to and/or set " + pvName);

				if (ch.connectAndWait())
					System.out.println("Connection Made: " + pvName);
				else {
					System.out.println("Connection Error: Unable to set "
							+ pvName);
					sleep();
				}

				try {
					ch.putVal(pvValue);
				} catch (ConnectionException e) {
					System.out.println("ConnectionException setting " + pvName);
					e.printStackTrace();
					sleep();
				} catch (PutException e) {
					System.out.println("GetException setting " + pvName);
					e.printStackTrace();
					sleep();
				}

				System.out.println(pvName + " succesfully set.");
				break; // from inner loop

			}

		}
	}

	/**
	 * Reads given set of PVs and stores them in a HashMap. If there was an
	 * error in connecting or reading, stores a Not a Number object in place of
	 * a value.
	 * 
	 * @throws ChannelAccessException
	 */
	public static HashMap<String, Double> readPvValues(List<String> pvNames)
			throws ChannelAccessException {

		HashMap<String, Double> pvValues = new HashMap<String, Double>();

		for (String PV : pvNames) {
			pvValues.put(PV, readPvValue(PV));
		}
		return pvValues;
	}

	/**
	 * Uses Java Channel Access to connect to and read the value associated with
	 * the given EPICS PV (Process Variable)
	 * 
	 * @param pvName
	 * @return pvValue
	 * @throws ChannelAccessException
	 */
	public static Double readPvValue(String pvName)
			throws ChannelAccessException {

		Double pvValue = Double.NaN;
		ChannelFactory cf = ChannelFactory.defaultFactory();

		for (int i = 0; i <= ATTEMPT_LIMIT; i++) {

			if (i >= ATTEMPT_LIMIT)
				throw new ChannelAccessException(
						"Cannot Connect to and/or read " + pvName);

			Channel ch = cf.getChannel(pvName);
			if (ch.connectAndWait()) {

				try {
					pvValue = ch.getValDbl();
				} catch (ConnectionException e) {
					System.out.println("ConnectionException reading " + pvName);
					e.printStackTrace();
					sleep();
				} catch (GetException e) {
					System.out.println("GetException reading " + pvName);
					e.printStackTrace();
					sleep();
				}

				System.out.println("Connection Made: " + pvName
						+ " succesfully read.");
				break; // from inner loop
			} else {

				System.out
						.println("Connection Error: Unable to read " + pvName);
				sleep();
			}

		}
		return pvValue;
	}

	private static void sleep() {
		try {
			Thread.sleep(SLEEP_LENGTH);
		} catch (InterruptedException e) {
			System.out.println("Thread Interrupted. Continuing.");
			e.printStackTrace();
		}
	}
}
