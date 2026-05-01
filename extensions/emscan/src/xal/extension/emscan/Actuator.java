/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.extension.emscan;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import xal.ca.Channel;
import xal.ca.ChannelFactory;
import xal.ca.ChannelTimeRecord;
import xal.ca.ConnectionException;
import xal.ca.IEventSinkValTime;
import xal.ca.Monitor;
import xal.ca.MonitorException;
import xal.ca.PutException;
import xal.ca.PutListener;

/**
 *
 * @author sashazhukov
 */
public class Actuator {
	
	private static double EPS = 0.01;
	
	private boolean DEBUG = false;
	public final int STATE_STOPPED;
	public final int STATE_MOVING;
	public final int CMND_STOP;
	public final int CMND_PARK;
	public final int CMND_MOVE;
	public final int CMND_IDL;

	final String velocitySuf;
	final String commandSuf;
	final String destinationSuf;
	final String statusSuf;

	final String positionSuf;
	final String positionEncSuf;

	Channel command = null;
	Channel destination = null;

	Channel velocity = null;
	IEventSinkValTime status = null;
	List<Channel> allChannels = new ArrayList<>();
	List<Monitor> allMonitors = new ArrayList<>();
	private int statusValue = 0;
	private IEventSinkValTime position;
	private double positionValue = 0;
	
	private IEventSinkValTime positionEnc;
	private double positionEncValue = 0;
	
	private final String devName;

	Actuator(String devname, ChannelFactory cf) {
		this(devname, cf, false, null);
	}

	Actuator(String devname, ChannelFactory cf, boolean debug, String actuatorType) {
		if ("BTF".equals(actuatorType)) {
			STATE_STOPPED = 0;
			STATE_MOVING = 1;
			CMND_STOP = 1;
			CMND_PARK = 3;
			CMND_MOVE = 2;
			CMND_IDL = 0;

			velocitySuf = ":VelocitySet";
			commandSuf = ":Command";
			destinationSuf = ":DestinationSet";
			statusSuf = ":Status";

			positionSuf = ":Position";
			positionEncSuf = ":PositionEnc";
		} else {
			STATE_STOPPED = 0;
			STATE_MOVING = 1;
			CMND_STOP = 0;
			CMND_PARK = 1;
			CMND_MOVE = 2;
			CMND_IDL = 3;

			velocitySuf = ":Vmmps";
			commandSuf = ":Cmnd";
			destinationSuf = ":Posmm";
			statusSuf = ":State";

			positionSuf = ":PosmmRb";
			positionEncSuf = positionSuf;

		}

		DEBUG = debug;
		this.devName = devname;
		if (DEBUG) {
			return;
		}
		try {

			command = cf.getChannel(devname + commandSuf);
			velocity = cf.getChannel(devname + velocitySuf);
			destination = cf.getChannel(devname + destinationSuf);
			Channel positionChannel = cf.getChannel(devname + positionSuf);
			Channel positionEncChannel = cf.getChannel(devname + positionEncSuf);

			position = new IEventSinkValTime() {

				@Override
				public void eventValue(ChannelTimeRecord record, Channel chan) {
					positionValue = record.doubleValue();
				}
			};
			allMonitors.add(positionChannel.addMonitorValTime(position, 1));
			
			positionEnc = new IEventSinkValTime() {

				@Override
				public void eventValue(ChannelTimeRecord record, Channel chan) {
					positionEncValue = record.doubleValue();
				}
			};
			allMonitors.add(positionEncChannel.addMonitorValTime(positionEnc, 1));
			

			Channel statusChannel = cf.getChannel(devname + statusSuf);
			status = new IEventSinkValTime() {

				@Override
				public void eventValue(ChannelTimeRecord record, Channel chan) {
					statusValue = record.intValue();
				}
			};
			allMonitors.add(statusChannel.addMonitorValTime(status, 1));
			
			allChannels.add(command);
			allChannels.add(destination);
			allChannels.add(velocity);
			allChannels.add(statusChannel);
			allChannels.add(positionChannel);
			allChannels.add(positionEncChannel);
			
			for(Channel ch : allChannels){
				ch.connectAndWait(1000);
			}
		} catch (ConnectionException ex) {
			Logger.getLogger(Scanner.class.getName()).log(Level.SEVERE, null, ex);
		} catch (MonitorException ex) {
			Logger.getLogger(Scanner.class.getName()).log(Level.SEVERE, null, ex);
		}

	}

	public boolean setVelocity(double v) {
		if (DEBUG) {
			return true;
		}
		boolean result = false;
		try {
			velocity.putVal(v);
			result = true;
		} catch (ConnectionException ex) {
			Logger.getLogger(Scanner.class.getName()).log(Level.SEVERE, null, ex);
			
		} catch (PutException ex) {
			Logger.getLogger(Scanner.class.getName()).log(Level.SEVERE, null, ex);
			
		} 
		return result;
	}

	public boolean moveTo(double d) {
		if (DEBUG) {
			return true;
		}
		boolean result = false;
		try {
			destination.putValCallback(d, new PutListener() {
				@Override
				public void putCompleted(Channel chnl) {
					try {
						command.putVal(CMND_MOVE);
						Channel.flushIO();
					} catch (ConnectionException ex) {
						Logger.getLogger(Actuator.class.getName()).log(Level.SEVERE, null, ex);
					} catch (PutException ex) {
						Logger.getLogger(Actuator.class.getName()).log(Level.SEVERE, null, ex);
					}
				}
			});
			Channel.flushIO();
			
		
			
			result = true;
		} catch (ConnectionException ex) {
			Logger.getLogger(Scanner.class.getName()).log(Level.SEVERE, null, ex);
		} catch (PutException ex) {
			Logger.getLogger(Scanner.class.getName()).log(Level.SEVERE, null, ex);
		} catch (Exception ex) {
			Logger.getLogger(Scanner.class.getName()).log(Level.SEVERE, null, ex);
		} 
		return result;
	}

	public boolean waitForMovementStart(int millis) throws InterruptedException {
		return waitForStatus(millis, STATE_MOVING);
	}

	public boolean waitForMovementComplete(int millis) throws InterruptedException {
		return waitForStatus(millis, STATE_STOPPED);
	}

	private  boolean waitForStatus(int millis, int sv) throws InterruptedException {
		if (DEBUG) {
			Thread.sleep(500);
			return true;
		}
		int iterations = millis / 2 + 1;
		for (int i = 0; i < iterations; i++) {
			if (statusValue == sv) {
				return true;
			}
			Thread.sleep(2);
		}
		return false;

	}
	
	public  boolean waitForStatusAndDest(int millis, double dest) throws InterruptedException {
		return waitForStatusAndDest(millis, STATE_STOPPED, dest);
	}
	private  boolean waitForStatusAndDest(int millis, int sv, double dest) throws InterruptedException {
		if (DEBUG) {
			Thread.sleep(500);
			return true;
		}
		int iterations = millis / 2 + 1;
		for (int i = 0; i < iterations; i++) {
			if (statusValue == sv&&Math.abs(positionValue-dest)<EPS) {
				return true;
			}
			Thread.sleep(2);
		}
		return false;

	}
	
	

	public boolean moveAndWait(int millis, double dest) throws InterruptedException {
		moveTo(dest);
		return waitForStatusAndDest(millis, STATE_STOPPED,dest);

	}

	public double getPosition() {
		return positionValue;
	}
	public double getPositionEnc() {
		return positionEncValue;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Actuator ").append(devName).append(" Status: ").append(statusValue).append(" at ").append(getPosition());
		return sb.toString();
	}

	public void close() {
		if (DEBUG) {
			return;
		}
		
		for(Monitor m:allMonitors){
			m.clear();
			
		}
		for (Channel ch : allChannels) {
		
//			ch.disconnect();
			

		}
		Channel.flushIO();
	}
}
