/*
 * Created on Oct 1, 2003
 *
 */
package xal.sim.mpx;

import java.util.Date;

/**A stop watch utility to measure elapsed total time.
 * @author klotz
 */
public class MPXStopWatch extends Date {
    /** ID for serializable version */
    private static final long serialVersionUID = 1L;
    
	private static MPXStopWatch instance = null;
	private static boolean beQuiet = false;

	private static MPXStopWatch getInstance() {
		if (instance == null) {
			instance = new MPXStopWatch();
		}
		return instance;
	}
	/**
	 * Time in msec since last call.
	 * @return time
	 */
	public static long getDeltaMsec() {
		return new Date().getTime() - MPXStopWatch.getInstance().getTime();
	}

	/**
	 * Time in seconds since last call.
	 * @return time
	 */
	public static double getDeltaSec() {
		long msec = getDeltaMsec();
		Double sec = new Double(msec);
		return sec.doubleValue() / 1000.;
	}

	/**
	 * Elapsed time since last call as string.
	 * @return time
	 */
	public static String timeElapsed() {
		return "seconds elapsed: " + getDeltaSec();
	}

	public static void timeElapsed(String message) {
		if (!beQuiet)
			System.out.println(message + timeElapsed());
	}

	/**
	 * Reset the stop watch.
	 *
	 */
	public static void reset() {
		reset(false);
	}

	public static void reset(boolean quiet) {
		instance = null;
		beQuiet = quiet;
	}

} ///////////////////  MPXStopWatch
