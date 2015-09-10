package xal.app.magnetcycling;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import xal.extension.widgets.swing.*;

/**
 *  The Runner class does time steps and calls methods of PowerSupplyCycler
 *  class instances combined in PowerSupplyGroup's.
 *
 *@author     shishlo
 */
public class Runner {

	//All time parameters are in seconds

	// This time step defines the minimal value of the time step.
	// The real time step could be bigger becasue of time delays
	// caused by magnets and EPICS

	private DoubleInputTextField time_step_TextField = new DoubleInputTextField(8);

	private volatile double run_time = 0.;

	private volatile double max_run_time = 0.;

	private volatile double wall_clock_run_time = 0.;

	private volatile boolean shouldStop = true;

	private volatile boolean isRunning = false;

	private volatile boolean itWasError = false;

	private volatile boolean itWasNormalEnd = false;

	private volatile boolean isStartReady = false;

	private int nMaxTry = 500;

	private Vector<PowerSupplyGroup> powerSupplyGroupV = new Vector<PowerSupplyGroup>();

	private Vector<ActionListener> initListenersV = new Vector<ActionListener>();

	private Vector<ActionListener> startListenersV = new Vector<ActionListener>();
	private Vector<ActionListener> stepListenersV = new Vector<ActionListener>();
	private Vector<ActionListener> stopListenersV = new Vector<ActionListener>();

	private Vector<ActionListener> errorListenersV = new Vector<ActionListener>();

	private ActionEvent runnerEvent = null;

	//======================================================
	//GUI Objects
	//======================================================
	private JButton initButton = new JButton("INIT");
	private JButton startButton = new JButton("START");
	private JButton resumeButton = new JButton("RESUME");
	private JButton stopButton = new JButton("STOP");

	private JLabel timeStepLabel = new JLabel("Time step (sec) :  ");

	private JPanel buttonPanel = new JPanel();


	/**
	 *  Constructor for the Runner object
	 */
	public Runner() {
		runnerEvent = new ActionEvent(this, 0, "event");
		time_step_TextField.setValue(1.0);
		initGUI();
		init();
		setButtonsState(true, false, false, false);
		isStartReady = false;
	}

	/**
	 *  Returns the panel of the Runner object
	 *
	 *@return    The panel
	 */
	public JPanel getPanel() {
		return buttonPanel;
	}


	/**
	 *  Initialization of the Runner variables and PowerSupplyCyclers.
	 *
	 *@return    The true if init() was successful
	 */
	public boolean init() {

		//we have to be shure that we stop running
		if(isRunning) {
			//sleep for 5*(time step)
			shouldStop = true;
			try {
				Thread.sleep((long) (5 * time_step_TextField.getValue() * 1000.0));
			} catch(InterruptedException e) {
			}
		}

		if(isRunning) {
			setButtonsState(true, false, false, false);
			isStartReady = false;
			return false;
		}

		run_time = 0.;
		wall_clock_run_time = 0.;

		shouldStop = true;
		itWasError = false;
		isRunning = false;
		itWasNormalEnd = false;

		double max_run_time_local = 0.;
		for(int i = 0, n = powerSupplyGroupV.size(); i < n; i++) {
			PowerSupplyGroup psg = powerSupplyGroupV.get(i);
			psg.init();
			double max_run_time_tmp = psg.getMaxTime();
			if(max_run_time_local < max_run_time_tmp) {
				max_run_time_local = max_run_time_tmp;
			}
		}
		max_run_time = max_run_time_local;

		setButtonsState(true, true, false, false);
		isStartReady = true;

		for(int i = 0, n = initListenersV.size(); i < n; i++) {
			( initListenersV.get(i)).actionPerformed(runnerEvent);
		}

		return true;
	}

	/**
	 *  Stops the run.
	 */
	public void stop() {
		shouldStop = true;
	}

	/**
	 *  Returns the <<is running>> boolean attribute of the Runner object
	 *
	 *@return    The <<is running>> value
	 */
	public boolean isRunning() {
		return isRunning;
	}

	/**
	 *  Returns the <<it was error>> boolean attribute of the Runner object
	 *
	 *@return    The <<it was error>> Value
	 */
	public boolean itWasError() {
		return itWasError;
	}

	/**
	 *  Returns the <<it was normal end>> boolean attribute of the Runner object.
	 *  If it is not normal it could be error or just temporary stop with
	 *  possibility of proceeding.
	 *
	 *@return    The <<it was normal end>> Value
	 */
	public boolean itWasNormalEnd() {
		return itWasNormalEnd;
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
	 *  Returns the run time in seconds
	 *
	 *@return    The run time value in seconds
	 */
	public double getRunTime() {
		return run_time;
	}


	/**
	 *  Returns the max Run Time in seconds
	 *
	 *@return    The max Run Time value in seconds
	 */
	public double getMaxRunTime() {
		return max_run_time;
	}

	/**
	 *  Returns the wall Clock Run Time in seconds
	 *
	 *@return    The wall Clock Run Time value in seconds
	 */
	public double getWallClockRunTime() {
		return wall_clock_run_time;
	}

	/**
	 *  Returns the powerSupplyGroups Vector.
	 *
	 *@return    The powerSupplyGroups value
	 */
	public Vector<PowerSupplyGroup> getPowerSupplyGroups() {
		return powerSupplyGroupV;
	}

	/**
	 *  Restores initial currents in power supplies that were memorized in init().
	 */
	public void restoreInitialCurrents() {
		for(int i = 0, n = powerSupplyGroupV.size(); i < n; i++) {
			PowerSupplyGroup psg = powerSupplyGroupV.get(i);
			psg.restoreInitialCurrents();
		}
	}


	/**
	 *  Adds a PowerSupplyGroup to the Runner object.
	 *
	 *@param  psg  The PowerSupplyGroup
	 */
	public void addPowerSupplyGroup(PowerSupplyGroup psg) {
		if(!isRunning()) {
			powerSupplyGroupV.add(psg);
			setButtonsState(true, false, false, false);
			isStartReady = false;
		}
	}

	/**
	 *  Removes the PowerSupplyGroup from Runner object.
	 *
	 *@param  psg  The PowerSupplyGroup
	 */
	public void removePowerSupplyGroup(PowerSupplyGroup psg) {
		powerSupplyGroupV.remove(psg);
	}

	/**
	 *  Removes all PowerSupplyGroups from Runner object.
	 */
	public void removePowerSupplyGroups() {
		powerSupplyGroupV.clear();
	}

	/**
	 *  Adds a start listener.
	 *
	 *@param  actionListener  a start listener
	 */
	public void addStartListener(ActionListener actionListener) {
		if(actionListener != null) {
			startListenersV.add(actionListener);
		}
	}

	/**
	 *  Removes one of the start listeners.
	 *
	 *@param  actionListener  The start listener to remove
	 */
	public void removeStartListener(ActionListener actionListener) {
		startListenersV.remove(actionListener);
	}

	/**
	 *  Removes all start listeners.
	 */
	public void removeStartListeners() {
		startListenersV.clear();
	}


	/**
	 *  Adds a step listener.
	 *
	 *@param  actionListener  a step listener
	 */
	public void addStepListener(ActionListener actionListener) {
		if(actionListener != null) {
			stepListenersV.add(actionListener);
		}
	}

	/**
	 *  Removes one of the step listeners.
	 *
	 *@param  actionListener  The step listener to remove
	 */
	public void removeStepListener(ActionListener actionListener) {
		stepListenersV.remove(actionListener);
	}

	/**
	 *  Removes all step listeners.
	 */
	public void removeStepListeners() {
		stepListenersV.clear();
	}

	/**
	 *  Adds a stop listener.
	 *
	 *@param  actionListener  a stop listener
	 */
	public void addStopListener(ActionListener actionListener) {
		if(actionListener != null) {
			stopListenersV.add(actionListener);
		}
	}

	/**
	 *  Removes one of the stop listeners.
	 *
	 *@param  actionListener  The stop listener to remove
	 */
	public void removeStopListener(ActionListener actionListener) {
		stopListenersV.remove(actionListener);
	}

	/**
	 *  Removes all stop listeners.
	 */
	public void removeStopListeners() {
		stopListenersV.clear();
	}


	/**
	 *  Adds a error listener.
	 *
	 *@param  actionListener  a error listener
	 */
	public void addErrorListener(ActionListener actionListener) {
		if(actionListener != null) {
			errorListenersV.add(actionListener);
		}
	}

	/**
	 *  Removes one of the error listeners.
	 *
	 *@param  actionListener  The error listener to remove
	 */
	public void removeErrorListener(ActionListener actionListener) {
		errorListenersV.remove(actionListener);
	}

	/**
	 *  Removes all error listeners.
	 */
	public void removeErrorListeners() {
		errorListenersV.clear();
	}

	/**
	 *  Adds a init listener.
	 *
	 *@param  actionListener  a init listener
	 */
	public void addInitListener(ActionListener actionListener) {
		if(actionListener != null) {
			initListenersV.add(actionListener);
		}
	}

	/**
	 *  Removes one of the init listeners.
	 *
	 *@param  actionListener  The init listener to remove
	 */
	public void removeInitListener(ActionListener actionListener) {
		initListenersV.remove(actionListener);
	}

	/**
	 *  Removes all init listeners.
	 */
	public void removeInitListeners() {
		initListenersV.clear();
	}


	/**
	 *  Main processing method for the Runner object
	 */
	public void run() {

		Runnable tryConnectRun =
			new Runnable() {
				public void run() {
					shouldStop = false;
					itWasError = false;
					itWasNormalEnd = false;

					//call for start listeners
					for(int i = 0, n = startListenersV.size(); i < n; i++) {
						( startListenersV.get(i)).actionPerformed(runnerEvent);
					}

					long start_time_mills = Calendar.getInstance().getTimeInMillis();

					//calculate max time - and we added 1 sec just for case
					double max_run_time_local = 0.;
					for(int i = 0, n = powerSupplyGroupV.size(); i < n; i++) {
						double max_run_time_tmp = (powerSupplyGroupV.get(i)).getMaxTime();
						if(max_run_time_local < max_run_time_tmp) {
							max_run_time_local = max_run_time_tmp;
						}
					}
					max_run_time = max_run_time_local + 1.0;

					//the time loop
					while(!shouldStop) {
						//should we stop because of "time is up"?
						if(max_run_time < run_time) {
							isRunning = false;
							itWasNormalEnd = true;
							break;
						}

						isRunning = true;
						setButtonsState(false, false, false, true);
						isStartReady = false;
						for(int i = 0, n = powerSupplyGroupV.size(); i < n; i++) {
							(powerSupplyGroupV.get(i)).makeTimeStep(time_step_TextField.getValue());
						}
						if(shouldStop) {
							isRunning = false;
							break;
						}

						//sleep for time step
						try {
							Thread.sleep((long) (time_step_TextField.getValue() * 1000.0));
							run_time += time_step_TextField.getValue();
						} catch(InterruptedException e) {
							//call to stop listener
							isRunning = false;
							break;
						}

						if(shouldStop || (isRunning == false)) {
							isRunning = false;
							break;
						}

						//call for step listeners
						for(int i = 0, n = powerSupplyGroupV.size(); i < n; i++) {
							PowerSupplyGroup grp = powerSupplyGroupV.get(i);
							grp.accountGraphPoint();
						}

						for(int i = 0, n = stepListenersV.size(); i < n; i++) {
							(stepListenersV.get(i)).actionPerformed(runnerEvent);
						}

						if(shouldStop) {
							isRunning = false;
							break;
						}

						long stop_time_mills = Calendar.getInstance().getTimeInMillis();
						wall_clock_run_time += 0.001 * (stop_time_mills - start_time_mills);

						//should we stop because of "time is up"?
						if(max_run_time < run_time) {
							isRunning = false;
							itWasNormalEnd = true;
							break;
						}
					}

					//call end of run listeners
					if(itWasError == true) {
						restoreInitialCurrents();

						for(int i = 0, n = errorListenersV.size(); i < n; i++) {
							(errorListenersV.get(i)).actionPerformed(runnerEvent);
						}

						setButtonsState(true, true, false, false);
						isStartReady = true;
					} else {
						if(itWasNormalEnd == false) {
							setButtonsState(false, false, true, true);
							isStartReady = false;
						} else {
							setButtonsState(true, true, false, false);
							isStartReady = true;
						}
					}

					for(int i = 0, n = stopListenersV.size(); i < n; i++) {
						(stopListenersV.get(i)).actionPerformed(runnerEvent);
					}

				}
			};

		Thread monitorThread = new Thread(tryConnectRun);
		monitorThread.start();

	}

	//===============================================
	//the GIUI related method definition
	//===============================================

	/**
	 *  Sets the buttons state of the Runner object
	 *
	 *@param  initButtonState    The new buttonsState value
	 *@param  startButtonState   The new buttonsState value
	 *@param  resumeButtonState  The new buttonsState value
	 *@param  stopButtonState    The new buttonsState value
	 */
	private void setButtonsState(boolean initButtonState, boolean startButtonState,
			boolean resumeButtonState, boolean stopButtonState) {
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
		JPanel tmp_time_step = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 1));

		JPanel tmp_0 = new JPanel(new GridLayout(2, 1, 1, 1));
		tmp_0.add(tmp_buttons);
		tmp_0.add(tmp_time_step);

		buttonPanel.setLayout(new BorderLayout());
		buttonPanel.setBorder(BorderFactory.createEtchedBorder());
		buttonPanel.add(tmp_0, BorderLayout.NORTH);

		tmp_buttons.add(initButton);
		tmp_buttons.add(startButton);
		tmp_buttons.add(resumeButton);
		tmp_buttons.add(stopButton);

		tmp_time_step.add(timeStepLabel);
		tmp_time_step.add(time_step_TextField);

		//set up buttons actions

		//stop scan listener definition
		ActionListener initButtonListener =
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					init();
				}
			};

		initButton.addActionListener(initButtonListener);

		ActionListener startButtonListener =
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(!isRunning()) {
						run();
					}
				}
			};

		startButton.addActionListener(startButtonListener);

		ActionListener resumeButtonListener =
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
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
						stop();
					} else {
						restoreInitialCurrents();
						init();
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
	 *  Sets the necessity of the initialization of the Runner object.
	 */
	public void setNeedInit() {
		setButtonsState(true, false, false, false);
	}

	/**
	 *  Sets the all component fonts
	 *
	 *@param  fnt  The new font
	 */
	public void setFontForAll(Font fnt) {

		time_step_TextField.setFont(fnt);

		initButton.setFont(fnt);
		startButton.setFont(fnt);
		resumeButton.setFont(fnt);
		stopButton.setFont(fnt);
		timeStepLabel.setFont(fnt);
	}

}

