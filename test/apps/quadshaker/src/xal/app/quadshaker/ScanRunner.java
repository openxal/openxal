package xal.app.quadshaker;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import xal.extension.widgets.swing.*;
import xal.tools.xml.*;
import xal.tools.data.DataAdaptor;

/**
 *  The Runner class does the scan steps and calls methods of the ScanObject
 *  class instance. To implement custom scanner a user should override methods
 *  of the ScanObject class. This class does not have a programmatic control
 *  over the scan process. Users should use only GUI elements to control the
 *  scan.
 *
 *@author     shishlo
 */
public class ScanRunner {

	//All time parameters are in seconds

	// The time step defines the minimal value of the time step.
	// The real time step could be bigger becasue of time delays
	// caused by devices and EPICS

	private TitledBorder panelBorder = null;

	private volatile boolean shouldStop = true;

	private volatile boolean isRunning = false;

	private volatile boolean itWasError = false;

	private volatile boolean isStartReady = false;

	private ActionEvent runnerEvent = null;

	//the ScanObject class instance
	private ScanObject scanObject = new ScanObject();

	//======================================================
	//GUI Objects
	//======================================================
	private JButton initButton = new JButton("INIT");
	private JButton startButton = new JButton("START");
	private JButton resumeButton = new JButton("RESUME");
	private JButton stopButton = new JButton("STOP");

	private JLabel timeStepLabel = new JLabel("Time step (sec) :  ");
	private DoubleInputTextField time_step_TextField = new DoubleInputTextField(8);

	private JLabel nMaxTryLabel = new JLabel("  Max Tries: ");
	private DoubleInputTextField nMaxTry_TextField = new DoubleInputTextField(5);

	private JRadioButton isValudationOnButton = new JRadioButton("Use Validation", false);

	private JLabel msgTextLabel = new JLabel("Msg: ");
	private JTextField msgTextField = new JTextField();

	private JProgressBar progressBar = new JProgressBar();

	private JPanel buttonPanel = new JPanel();

	//the scan thread
	private Thread scanThread = null;

	//xml data root name
	private String rootName = "SCAN_RUNNER_DATA";

	/**
	 *  Constructor for the Runner object. It registers the empty ScanObject as the
	 *  object of the scan.
	 */
	public ScanRunner() {
		runnerEvent = new ActionEvent(this, 0, "event");
		time_step_TextField.setValue(1.0);
		nMaxTry_TextField.setValue(5.0);
		setScanObject(scanObject);
		initGUI();
		init();
		setButtonsState(true, false, false, false);
		isStartReady = false;
		msgTextField.setForeground(Color.red);
	}

	/**
	 *  Returns the GUI panel of the Runner object.
	 *
	 *@return    The panel
	 */
	public JPanel getPanel() {
		return buttonPanel;
	}


	/**
	 *  Sets the message on the message text field of the ScanRunner GUI panel.
	 *
	 *@param  str  The new message value
	 */
	public void setMessage(String str) {
		msgTextField.setText(str);
	}

	/**
	 *  Sets the scanObject of the ScanRunner object.
	 *
	 *@param  scanObject  The new scanObject
	 */
	public void setScanObject(ScanObject scanObject) {
		this.scanObject = scanObject;
		scanObject.setScanRunner(this);
	}


	/**
	 *  Returns the scanObject of the ScanRunner object
	 *
	 *@return    The scanObject
	 */
	public ScanObject getScanObject() {
		return scanObject;
	}

	/**
	 *  Initialization of the Runner variables and scanObject.
	 *
	 *@return    The true if init() was successful
	 */
	private boolean init() {

		//we have to be shure that we stop running
		if(isRunning) {
			//sleep for 1*(time step)
			shouldStop = true;
			try {
				Thread.sleep((long) (time_step_TextField.getValue() * 1000.0));
			} catch(InterruptedException e) {
			}
		}

		if(isRunning) {
			setButtonsState(true, false, false, false);
			isStartReady = false;
			return false;
		}

		shouldStop = true;
		itWasError = false;
		isRunning = false;

		setButtonsState(true, true, false, false);
		isStartReady = true;

		scanObject.initScan();

		return true;
	}

	/**
	 *  Returns the "is running" boolean attribute of the Runner object
	 *
	 *@return    The "is running" value
	 */
	public boolean isRunning() {
		return isRunning;
	}

	/**
	 *  Returns the "it was an error" boolean attribute of the Runner object
	 *
	 *@return    The "it was an error" Value
	 */
	public boolean itWasError() {
		return itWasError;
	}


	/**
	 *  Sets the time step in seconds for the Runner object
	 *
	 *@param  time_step  The new time step value in seconds
	 */
	public void setTimeStep(double time_step) {
		time_step_TextField.setValue(time_step);
	}

	/**
	 *  Returns the time step attribute of the Runner object
	 *
	 *@return    The time step value in seconds
	 */
	public double getTimeStep() {
		return time_step_TextField.getValue();
	}


	/**
	 *  Main processing method for the Runner object
	 */
	private void run() {

		Runnable tryConnectRun =
			new Runnable() {
				public void run() {
					shouldStop = false;
					itWasError = false;

					//start of the scan
					scanObject.startScan();
					progressBar.setValue(scanObject.getProgress());

					//the time loop
					while(!shouldStop) {

						//should we stop because of scanObject asked us?
						if(!scanObject.nextStepExists()) {
							isRunning = false;
							break;
						}

						isRunning = true;
						setButtonsState(false, false, false, true);
						isStartReady = false;

						boolean shouldRepeat = true;
						int count = 0;

						while(shouldRepeat) {
							shouldRepeat = false;
							//make step
							scanObject.makeStep();
							count = count + 1;

							if(shouldStop) {
								isRunning = false;
								break;
							}

							//sleep for time step
							try {
								Thread.sleep((long) (time_step_TextField.getValue() * 1000.0));
							} catch(InterruptedException e) {
								//call to stop listener
								isRunning = false;
								break;
							}

							if(shouldStop || (isRunning == false)) {
								isRunning = false;
								break;
							}

							if(isValudationOnButton.isSelected() && !scanObject.validateStep()) {
								shouldRepeat = true;
							}

							if(count >= ((int) nMaxTry_TextField.getValue())) {
								shouldRepeat = false;
								isRunning = false;
								itWasError = true;
								msgTextField.setText("Can not validate!");
							}
						}

						if(isRunning == false) {
							break;
						}

						//account step
						scanObject.accountStep();
						progressBar.setValue(scanObject.getProgress());

						if(shouldStop) {
							isRunning = false;
							break;
						}
					}

					//call end of run listeners
					if(itWasError == true) {
						scanObject.errorHappened();
					}

					boolean nextStepExists = scanObject.nextStepExists();
					//scan accomplished
					scanObject.scanFinished();
					progressBar.setValue(scanObject.getProgress());

					if(nextStepExists) {
						setButtonsState(false, false, true, true);
						isStartReady = false;
					} else {
						scanObject.restoreInitialState();
						progressBar.setValue(scanObject.getProgress());
						setButtonsState(true, true, false, false);
						isStartReady = true;
					}
				}
			};

		scanThread = new Thread(tryConnectRun);
		scanThread.start();

	}

	//===============================================
	//the GIUI related method definition
	//===============================================


	/**
	 *  Sets the states (enabled or disabled) of all buttons at the GUI panel
	 *
	 *@param  initButtonState    The state of init button
	 *@param  startButtonState   The state of start button
	 *@param  resumeButtonState  The state of resume button
	 *@param  stopButtonState    The state of stop button
	 */
	private void setButtonsState(
			boolean initButtonState,
			boolean startButtonState,
			boolean resumeButtonState,
			boolean stopButtonState) {
		initButton.setEnabled(initButtonState);
		startButton.setEnabled(startButtonState);
		resumeButton.setEnabled(resumeButtonState);
		stopButton.setEnabled(stopButtonState);
	}


	/**
	 *  Initialize the GUI components
	 */
	private void initGUI() {

		JPanel tmp_buttons = new JPanel(new GridLayout(1, 4, 1, 1));
		tmp_buttons.add(initButton);
		tmp_buttons.add(startButton);
		tmp_buttons.add(resumeButton);
		tmp_buttons.add(stopButton);

		JPanel tmp_prg = new JPanel(new BorderLayout());
		tmp_prg.add(progressBar, BorderLayout.CENTER);

		JPanel tmp_time_step = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 1));
		tmp_time_step.add(timeStepLabel);
		tmp_time_step.add(time_step_TextField);

		JPanel tmp_validation = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 1));
		tmp_validation.add(isValudationOnButton);
		tmp_validation.add(nMaxTryLabel);
		tmp_validation.add(nMaxTry_TextField);

		JPanel tmp_msg = new JPanel(new BorderLayout());
		tmp_msg.add(msgTextLabel, BorderLayout.WEST);
		tmp_msg.add(msgTextField, BorderLayout.CENTER);

		JPanel tmp_0 = new JPanel(new GridLayout(5, 1, 1, 1));
		tmp_0.add(tmp_buttons);
		tmp_0.add(tmp_prg);
		tmp_0.add(tmp_time_step);
		tmp_0.add(tmp_validation);
		tmp_0.add(tmp_msg);

		buttonPanel.setLayout(new BorderLayout());
		buttonPanel.setBorder(BorderFactory.createEtchedBorder());
		buttonPanel.add(tmp_0, BorderLayout.NORTH);

		Border border = BorderFactory.createEtchedBorder();
		panelBorder = BorderFactory.createTitledBorder(border, "scanner control");
		buttonPanel.setBorder(panelBorder);

		//set up buttons actions

		//stop scan listener definition
		ActionListener initButtonListener =
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					msgTextField.setText(null);
					init();
				}
			};

		initButton.addActionListener(initButtonListener);

		ActionListener startButtonListener =
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					msgTextField.setText(null);
					if(!isRunning()) {
						run();
					}
				}
			};

		startButton.addActionListener(startButtonListener);

		ActionListener resumeButtonListener =
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					msgTextField.setText(null);
					if(!isRunning()) {
						run();
					}
				}
			};

		resumeButton.addActionListener(resumeButtonListener);

		ActionListener stopButtonListener =
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(isRunning()) {
						shouldStop = true;
						Thread inner_thread = scanThread;
						if(inner_thread != null && inner_thread.isAlive()) {
							inner_thread.interrupt();
						}
					} else {
						msgTextField.setText(null);
						scanObject.restoreInitialState();
						init();
						progressBar.setValue(scanObject.getProgress());
						setButtonsState(true, true, false, false);
						isStartReady = true;
					}
				}
			};

		stopButton.addActionListener(stopButtonListener);
	}

	/**
	 *  Returns the isStartReady attribute of the Runner object. If it is true then
	 *  user can start from the beginning again.
	 *
	 *@return    The isStartReady value
	 */
	public boolean isStartReady() {
		return isStartReady;
	}

	/**
	 *  Sets the all component fonts
	 *
	 *@param  fnt  The new font
	 */
	public void setFontForAll(Font fnt) {

		time_step_TextField.setFont(fnt);
		nMaxTry_TextField.setFont(fnt);
		msgTextField.setFont(fnt);

		initButton.setFont(fnt);
		startButton.setFont(fnt);
		resumeButton.setFont(fnt);
		stopButton.setFont(fnt);
		isValudationOnButton.setFont(fnt);

		timeStepLabel.setFont(fnt);
		nMaxTryLabel.setFont(fnt);
		msgTextLabel.setFont(fnt);

		panelBorder.setTitleFont(fnt);
	}


	/**
	 *  Description of the Method
	 *
	 *@param  da  The Parameter
	 */
	public void dumpData(DataAdaptor da) {
		DataAdaptor scanRunnerDA =  da.createChild(rootName);
		DataAdaptor scanRunnerPramsDA =  scanRunnerDA.createChild("PARAMETERS");
		scanRunnerPramsDA.setValue("time_step", time_step_TextField.getValue());
		scanRunnerPramsDA.setValue("max_tries", (int) nMaxTry_TextField.getValue());
		scanRunnerPramsDA.setValue("use_validation", isValudationOnButton.isSelected());
	}

	/**
	 *  Description of the Method
	 *
	 *@param  da  The Parameter
	 */
	public void readData(DataAdaptor da) {
		DataAdaptor scanRunnerDA = da.childAdaptor(rootName);
		DataAdaptor scanRunnerPramsDA = scanRunnerDA.childAdaptor("PARAMETERS");
		time_step_TextField.setValue(scanRunnerPramsDA.doubleValue("time_step"));
		nMaxTry_TextField.setValue((double) scanRunnerPramsDA.intValue("max_tries"));
		isValudationOnButton.setSelected(scanRunnerPramsDA.booleanValue("use_validation"));
	}


	/**
	 *  The main program for the ScanRunner class to test it.
	 *
	 *@param  args  Should be nothing
	 */
	public static void main(String[] args) {

		System.out.println("===Start ScanRunner Test===");

		//make main frame
		JFrame mainFrame = new JFrame("Test of the ScanRunner");
		mainFrame.addWindowListener(
			new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent evt) {
					System.exit(0);
				}
			});

		//the test part begins
		ScanRunner scanRunner = new ScanRunner();

		scanRunner.setFontForAll(new Font("Monospaced", Font.BOLD, 10));

		//now we define our ScanObject
		ScanObject scanObject =
			new ScanObject() {

				private int nstep = 5;
				private volatile int current_step = -1;

				public void initScan() {
					current_step = 0;
					System.out.println("debug === initScan() === curr. step=" + current_step);
				}

				public boolean nextStepExists() {
					if(current_step > nstep) {
						return false;
					}
					return true;
				}

				public void startScan() {
					System.out.println("debug === startScan() === curr. step=" + current_step);
				}


				public void makeStep() {
					System.out.println("debug === makeStep() === curr. step=" + current_step);
				}

				public boolean validateStep() {
					System.out.println("debug === validateStep() === curr. step=" + current_step);
					return false;
				}


				public void accountStep() {
					System.out.println("debug === accountStep() === curr. step=" + current_step);
					current_step = current_step + 1;
				}

				public void restoreInitialState() {
					current_step = -1;
					System.out.println("debug === restoreInitialState() === curr. step=" + current_step);
				}

				public void errorHappened() {
					System.out.println("debug === errorHappened() === curr. step=" + current_step);
				}

				public void scanFinished() {
					System.out.println("debug === scanFinished() ===");
				}

				public int getProgress() {
					int res = (100 * current_step) / nstep;
					return res;
				}

			};

		scanRunner.setScanObject(scanObject);

		//make panels
		JPanel scanRunnerPanel = scanRunner.getPanel();

		JPanel tmp_0 = new JPanel(new BorderLayout());
		JPanel tmp_1 = new JPanel(new BorderLayout());
		tmp_0.add(scanRunnerPanel, BorderLayout.NORTH);
		tmp_1.add(tmp_0, BorderLayout.WEST);

		mainFrame.getContentPane().add(tmp_1);
		mainFrame.pack();
		mainFrame.setSize(new Dimension(300, 430));
		mainFrame.setVisible(true);

	}

}

