/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.extension.emscan;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import xal.ca.Channel;
import xal.ca.ChannelTimeRecord;
import xal.ca.ConnectionException;
import xal.ca.IEventSinkValTime;
import xal.ca.Monitor;
import xal.ca.MonitorException;

/**
 *
 * @author sashazhukov
 */
class Recorder implements Runnable {

	private Map<String, Channel> allChannels = new HashMap<>();
	private List<String> allNames = new ArrayList<>();
	private final IEventSinkValTime monitor;
	private Map<Double, Map<String, ChannelTimeRecord>> records = Collections.synchronizedMap(new HashMap<Double, Map<String, ChannelTimeRecord>>());
	private BufferedWriter saveWriter = null;
	private BufferedWriter timeWriter = null;
	private boolean recording = false;
	private int numberOfUniqueChannels = 0;
	private final List<Corrector> correctors;
	private final String slitName;
	private final String harpName;
	private boolean isRunning;
	private int recordedUpdates;
	private int samples;
	private int recordCounter;
	private double slitExpected;
	private double harpExpected;
	private final double EPS = 0.08;
	private final String fileName;
    private ScanDataListener dataListener;
    
    
        
        Recorder(List<Channel> channels, List<Corrector> correctors, String slit, String harp) 
                throws FileNotFoundException, IOException, InterruptedException, ConnectionException {
            this("",channels,correctors,slit,harp);
        }
        
	Recorder(String path, List<Channel> channels, List<Corrector> correctors, String slit, String harp) 
                throws FileNotFoundException, IOException, InterruptedException, ConnectionException {
		storageQ = new LinkedBlockingQueue<Pair>();
		this.correctors = correctors;
		slitName = slit;
		harpName = harp;

               fileName = path;
                
		saveWriter = new BufferedWriter(new FileWriter(fileName));
		timeWriter = new BufferedWriter(new FileWriter(fileName+".time"));

		for (Channel ch : channels) {
			if (ch == null) {
				continue;
			}
			String name = ch.channelName();
			allNames.add(name);
			if (!allChannels.containsKey(name)) {
				allChannels.put(name, ch);

			}
		}

		numberOfUniqueChannels = allChannels.size();

		StringBuilder sb = new StringBuilder();
		for (String s : allNames) {
			sb.append(s + " ");
		}
		sb.append("\n");
		saveWriter.write(sb.toString());
		saveWriter.flush();

		monitor = new IEventSinkValTime() {

			@Override
			public void eventValue(ChannelTimeRecord record, Channel chan) {
				processEvents(record, chan);

			}
		};

		for (Channel ch : allChannels.values()) {
			try {
                            Monitor m = ch.addMonitorValTime(monitor, 1);
                            allMonitors.add(m);
			} catch (ConnectionException ex) {
				Logger.getLogger(Scanner.class.getName()).log(Level.SEVERE, null, ex);
			} catch (MonitorException ex) {
				Logger.getLogger(Scanner.class.getName()).log(Level.SEVERE, null, ex);
			}
		}

	}
        
        
        private List<Monitor> allMonitors = new ArrayList<>();
	public void start() {
		isRunning = true;
		new Thread(this).start();
	}

	public void waitForSamples(int samples, double slit, double harp) {
		this.slitExpected = slit;
		this.harpExpected = harp;
		this.samples = samples;
		recordCounter = 0;
		setRecording(true);
		if (samples != 0) {
			while (true) {
				if (!isRecording()) {
					break;
				} else {
					try {
						Thread.sleep(25);
					} catch (InterruptedException ex) {
						Logger.getLogger(Recorder.class.getName()).log(Level.SEVERE, null, ex);
					}

				}
			}

		}
	}

	public void setRecording(boolean start) {
		recording = start;

		if (start) {
			recordedUpdates = 0;
		} else {
			System.out.println(recordedUpdates + " records received.");
		}

	}

	public boolean isRecording() {
		return recording;
	}

	private void processEvents(ChannelTimeRecord record, Channel chan) {

		if (isRecording()) {
			recordedUpdates++;
			storageQ.add(new Pair(chan.channelName(), record));
		}

	}

	private void save(Map<String, ChannelTimeRecord> entry) throws IOException {

		double x = entry.get(slitName).doubleValue();
		double xp = entry.get(harpName).doubleValue();
                double[] signal = new double[]{};

		for (Corrector c : correctors) {
			c.adjust(x, xp);
		}

		StringBuilder sb = new StringBuilder();
		StringBuilder timeb = new StringBuilder();
		int i = 0;
		for (String name : allNames) {
			ChannelTimeRecord record = entry.get(name);
			if (i == 0) {
				sb.append(record.getTimestamp());
				System.out.print(record.getTimestamp());
				timeb.append(record.getTimestamp().getTime());
                                
			}
                        else if(i==2){
                            signal=record.doubleArray();
                        }
			

			if (record.getCount() == 1) {
				sb.append("," + record.doubleValue());
				System.out.print(" " + record.doubleValue());
			} else {
				double sum = 0;
				double[] array = record.doubleArray();
                                
				for (double d : array) {
					sb.append("," + d);
					sum += d;
				}
				System.out.print(" " + sum);
			}
                        i++;
		}
                
                if(dataListener!=null){
                    dataListener.dataReceived(x, xp, signal);
                }
		System.out.println();

		sb.append("\n");
		saveWriter.write(sb.toString());
		saveWriter.flush();
		timeb.append("\n");
		timeWriter.write(timeb.toString());
		timeWriter.flush();

	}

	public void close() throws IOException {
            for(Monitor m: allMonitors){
                m.clear();
            }
		for (Channel ch : allChannels.values()) {
//			ch.disconnect();

		}
		saveWriter.close();
		isRunning = false;

	}
	private final LinkedBlockingQueue<Pair> storageQ;

	@Override
	public void run() {
		while (isRunning) {
			try {
				Pair element = storageQ.poll(1, TimeUnit.SECONDS);
				if (element != null && isRecording()) {
					ChannelTimeRecord record = element.getRecord();
					String name = element.getChannelName();
					double ts = Math.round((record.timeStampInSeconds() * 1000.0)) / 1000.0;
					Map<String, ChannelTimeRecord> entry = records.get(ts);
					if (entry == null) {
						entry = new HashMap<>();
						entry.put(name, record);
						records.put(ts, entry);
					} else {
						entry.put(name, record);
					}
					if (entry.size() == numberOfUniqueChannels) {
						records.remove(ts);

						double slitRb = entry.get(slitName).doubleValue();
						double harpRb = entry.get(harpName).doubleValue();

						boolean valid = true; //Math.abs(slitRb - slitExpected) < EPS && Math.abs(harpRb - harpExpected) < EPS;
						if (valid) {
							recordCounter++;
							if (samples != 0 && recordCounter >= samples) {
								setRecording(false);
							}
							try {
								save(entry);
							} catch (IOException ex) {
								Logger.getLogger(Scanner.class.getName()).log(Level.SEVERE, null, ex);
							}
						}
					}

				}
			} catch (Exception ex) {
				Logger.getLogger(Recorder.class.getName()).log(Level.SEVERE, null, ex);
			}

		}
	}

	void convert() {
		try {
			FileTimeData.convertCSVData( fileName,0);
		} catch (IOException ex) {
			Logger.getLogger(Recorder.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

    void setDataListener(ScanDataListener dataListener) {
        this.dataListener = dataListener;
    }

}

final class Pair {

	private final String channelName;
	private final ChannelTimeRecord record;

	Pair(String channelName, ChannelTimeRecord record) {
		this.channelName = channelName;
		this.record = record;
	}

	String getChannelName() {
		return channelName;
	}

	ChannelTimeRecord getRecord() {
		return record;
	}
}
