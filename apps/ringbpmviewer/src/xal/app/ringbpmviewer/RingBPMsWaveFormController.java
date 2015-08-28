package xal.app.ringbpmviewer;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import java.util.*;
import java.io.*;
import java.util.prefs.*;

import xal.extension.scan.UpdatingEventController;
import xal.tools.xml.*;
import xal.extension.widgets.swing.*;
import xal.tools.text.ScientificNumberFormat;

import xal.extension.fit.DampedSinusoidFit;

import xal.service.pvlogger.*;
import xal.tools.database.*;

/**
 *  The GUI panel for ring BPM TBT waveform representation.
 *
 *@author     shishlo
 */
public class RingBPMsWaveFormController {
	/** Center for Remote PV Logger services */
	final private RemoteLoggingCenter REMOTE_LOGGING_CENTER;

	private JFrame ownerFrame = null;

	//message text field. It is actually message text field from
	private JTextField messageTextLocal = new JTextField();

	private JPanel mainWFsPanel = new JPanel(new BorderLayout());

	private JPanel graphAndKnobPanel = new JPanel(new BorderLayout());
	private JPanel northKnobPanel = new JPanel(new BorderLayout());
	private JPanel graphsPanel = new JPanel(new GridLayout(0, 1));
	private JPanel southKnobPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));

	private JPanel bpmsPanel = new JPanel(new BorderLayout());
	private JList<String> bpmsList = new JList<String>();

	//BPMs List label
	private JLabel listBPMs_Label = new JLabel("= Ring BPMs =", JLabel.CENTER);

	//The set of ring BPMs
	private RingBPMset ring_bpm_set = null;

	// knobs for the graphs (TBT array) showing and policy about empty spaces if
	// user does not want to show specific BPM
	private JRadioButton showTBTarrXButton = new JRadioButton("x", true);
	private JRadioButton showTBTarrYButton = new JRadioButton("y", true);
	private JRadioButton showTBTarrAmpButton = new JRadioButton("amp", true);

	private JLabel selectedBPM_Label = new JLabel("No selected BPM", JLabel.CENTER);
	private JButton analyzeAll_Button = new JButton(" Perform analysis for ALL BPMs ");
	private volatile boolean stopAllAnalysis = false;

	//charts for X,Y,Amplitude
	private AnalysisWFPanel analysisWFPanelX = new AnalysisWFPanel();
	private AnalysisWFPanel analysisWFPanelY = new AnalysisWFPanel();
	private AnalysisWFPanel analysisWFPanelAmp = new AnalysisWFPanel();

	//analyzer from fitting package
	final private Object FITTER_LOCK = new Object();
	private DampedSinusoidFit fitter;
	private ScientificNumberFormat fmt = new ScientificNumberFormat( 5, 10, false );
	//-------------------------------------------------------------
	//south knobs panel
	//-------------------------------------------------------------

	//the controll knobs
	private JRadioButton listenToEPICS_Button = new JRadioButton("Listen to EPICS", false);
	private JButton exportASCII_Button = new JButton(" EXPORT DATA to ASCII file");

	//PV Logger part
	private JButton makeSnapshotButton = new JButton("Make PV Logger Snapshot");
	private JButton clearSnapshotButton = new JButton("Clear Snapshot");
	private String noSnapshotIdString = "No Snapshot";
	private String snapshotIdString = "Last Snapshot Id: ";
	private JLabel snapshotIdLabel = new JLabel("No Snapshot", JLabel.LEFT);
	private long snapshotId = -1;
	private boolean pvLogged = false;

	//update controller for the redrawing the TBT array
	private UpdatingEventController uc = null;

	//update controller for the changing the set of bar columns in charts
	private UpdatingEventController ucContent = null;

	// format with a fixed length
	private ScientificNumberFormat frmt = new ScientificNumberFormat( 6, 10 );

	private volatile boolean isShowing = false;

	private volatile int indSelected = -1;

	//local ascii data file
	private File asciiDataFile = null;
	private String asciiDataFileKey = "RING_BPMs_DATA_ASCII_FILE";

	//Preferences for this package
	private Preferences preferences = null;


	/**
	 *  Constructor for the RingBPMsWaveFormControllerobject
	 *
	 *@param  ucIn         The update controller for the redrawing the TBT array
	 *@param  ucContentIn  The update controller for the changing the set of BPMs
	 *      columns in charts
	 */
	public RingBPMsWaveFormController(UpdatingEventController ucIn, UpdatingEventController ucContentIn) {
		/** Construct the center up front so it can discover services in the background before it is needed */
		REMOTE_LOGGING_CENTER = new RemoteLoggingCenter();

		uc = ucIn;
		ucContent = ucContentIn;

		analysisWFPanelX.getPVWFPanel().setTitle("x [mm] - bpm signal");
		analysisWFPanelY.getPVWFPanel().setTitle("y [mm] - bpm signal");
		analysisWFPanelAmp.getPVWFPanel().setTitle("amplitude - bpm signal");

		analysisWFPanelX.getPVWFPanel().setAxisNameY("x [mm]");
		analysisWFPanelY.getPVWFPanel().setAxisNameY("y [mm]");
		analysisWFPanelAmp.getPVWFPanel().setAxisNameY("amplitude");

		//button decoration
		showTBTarrXButton.setForeground(Color.blue);
		showTBTarrYButton.setForeground(Color.blue);
		showTBTarrAmpButton.setForeground(Color.blue);

		listBPMs_Label.setForeground(Color.blue);
		selectedBPM_Label.setForeground(Color.blue);

		listenToEPICS_Button.setForeground(Color.blue);
		exportASCII_Button.setForeground(Color.blue);
		analyzeAll_Button.setForeground(Color.blue);

		//set all tooltips
		showTBTarrXButton.setToolTipText("Shows x-positions TBT array");
		showTBTarrYButton.setToolTipText("Shows y-positions TBT array");
		showTBTarrAmpButton.setToolTipText("Shows amplitudes TBT array");

		listenToEPICS_Button.setToolTipText("Listen to EPICS channels");
		exportASCII_Button.setToolTipText("EXPORT waveforms and analysis data to ASCII file");

		//update controllers listeners
		uc.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					clearAnalysisResults();
					ring_bpm_set.clearAnalysis();
					updateWFsetOnGraphs();
				}
			});

		ucContent.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//nothing to update
				}
			});

		//buttons action
		showTBTarrXButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					composeWFsPanel();
				}
			});

		showTBTarrYButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					composeWFsPanel();
				}
			});

		showTBTarrAmpButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					composeWFsPanel();
				}
			});

		AbstractListModel<String> bpmsListModel =
			new AbstractListModel<String>() {
                /** serialization ID */
                private static final long serialVersionUID = 1L;
				public String getElementAt(int index) {
					if(ring_bpm_set != null && ring_bpm_set.size() > index) {
						return ring_bpm_set.getRingBPM(index).getBPMName();
					} else {
						return "";
					}
				}

				public int getSize() {
					if(ring_bpm_set != null) {
						return ring_bpm_set.size();
					} else {
						return 0;
					}
				}
			};

		bpmsList.setModel(bpmsListModel);
		bpmsList.addListSelectionListener(
			new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
					indSelected = bpmsList.getSelectedIndex();
					if(ring_bpm_set != null && indSelected >= 0 && indSelected < ring_bpm_set.size()) {
						selectedBPM_Label.setText("Selected BPM: " + ring_bpm_set.getRingBPM(indSelected).getBPMName());
					} else {
						selectedBPM_Label.setText("No Selected BPM");
						indSelected = -1;
					}
					clearAnalysisResults();
					messageTextLocal.setText(null);
					updateWFsetOnGraphs();
				}
			});

		bpmsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		//analysis buttons listeners
		analysisWFPanelX.addAnalysisButtonListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					synchronized( FITTER_LOCK ) {

						messageTextLocal.setText(null);

						RingBPM rbpm = null;

						if(ring_bpm_set != null && indSelected >= 0 && indSelected < ring_bpm_set.size()) {
							rbpm = ring_bpm_set.getRingBPM(indSelected);
						}

						if(rbpm == null) {
							analysisWFPanelX.getPVWFPanel().clearAnalysis();
							messageTextLocal.setText("There is no BPM selected. Please select BPM first.");
							return;
						}

						if(!performTuneAnalysis(rbpm.getBPM_X(), analysisWFPanelX, true)) {
							messageTextLocal.setText("Cannot perform analysis. Bad data.");
						}
						updateWFsetOnGraphs();
					}
				}
			});

		analysisWFPanelY.addAnalysisButtonListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					synchronized( FITTER_LOCK ) {

						messageTextLocal.setText(null);

						RingBPM rbpm = null;

						if(ring_bpm_set != null && indSelected >= 0 && indSelected < ring_bpm_set.size()) {
							rbpm = ring_bpm_set.getRingBPM(indSelected);
						}

						if(rbpm == null) {
							analysisWFPanelY.getPVWFPanel().clearAnalysis();
							messageTextLocal.setText("There is no BPM selected. Please select BPM first.");
							return;
						}

						if(!performTuneAnalysis(rbpm.getBPM_Y(), analysisWFPanelY, true)) {
							messageTextLocal.setText("Cannot perform analysis. Bad data.");
						}
						updateWFsetOnGraphs();
					}
				}
			});

		analysisWFPanelAmp.addAnalysisButtonListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					synchronized( FITTER_LOCK ) {
						messageTextLocal.setText(null);
						messageTextLocal.setText("This analysis is not implemented yet.");
					}
				}
			});

		Border etchedBorder = BorderFactory.createEtchedBorder();

		analyzeAll_Button.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					analyzeAllBPMs();
				}
			});

		exportASCII_Button.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					exportDataToASCII();
				}
			});

		//pv logger buttons - make pv logger snapshot
		makeSnapshotButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Date startTime = new Date();
					String comments = startTime.toString();
					comments = comments + " = Ring BPM Viewer =";
					snapshotId = REMOTE_LOGGING_CENTER.takeAndPublishSnapshot( "Ring BPM Test", comments);
					if(snapshotId > 0){
						pvLogged = true;
						snapshotIdLabel.setText(snapshotIdString + snapshotId + "  ");
					} else {
						pvLogged = false;
						snapshotIdLabel.setText("Unsuccessful PV Logging");
					}
				}
			});

		clearSnapshotButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					snapshotId = -1;
					pvLogged = false;
					snapshotIdLabel.setText(noSnapshotIdString);
				}
			});

		//------------------------------------------------
		//compose panels
		//------------------------------------------------

		//north button panel
		JPanel tmp_1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		tmp_1.add(showTBTarrXButton);
		tmp_1.add(showTBTarrYButton);
		tmp_1.add(showTBTarrAmpButton);
		tmp_1.setBorder(etchedBorder);

		JPanel tmp_2 = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 1));
		tmp_2.add(selectedBPM_Label);
		tmp_2.add(analyzeAll_Button);

		northKnobPanel.add(tmp_1, BorderLayout.WEST);
		northKnobPanel.add(tmp_2, BorderLayout.CENTER);

		southKnobPanel.add(listenToEPICS_Button);
		southKnobPanel.add(exportASCII_Button);

		JPanel tmp_3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 3));
		tmp_3.add(makeSnapshotButton);
		tmp_3.add(snapshotIdLabel);
		tmp_3.add(clearSnapshotButton);
		tmp_3.setBorder(etchedBorder);
		southKnobPanel.add(tmp_3);

		//set panels
		graphAndKnobPanel.add(graphsPanel, BorderLayout.CENTER);
		graphAndKnobPanel.add(northKnobPanel, BorderLayout.NORTH);
		graphAndKnobPanel.add(southKnobPanel, BorderLayout.SOUTH);

		JPanel center_tmp = new JPanel(new BorderLayout());
		center_tmp.add(graphAndKnobPanel, BorderLayout.CENTER);

		JScrollPane scrollPane = new JScrollPane(bpmsList,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		bpmsPanel.add(listBPMs_Label, BorderLayout.NORTH);
		bpmsPanel.add(scrollPane, BorderLayout.CENTER);

		mainWFsPanel.add(center_tmp, BorderLayout.CENTER);
		mainWFsPanel.add(bpmsPanel, BorderLayout.WEST);

		composeWFsPanel();

		//define preferences
		preferences = Preferences.userNodeForPackage(this.getClass());
		findOldASCIIFile();
	}


	/**
	 *  Gets the panel attribute of the RingBPMsWaveFormControllerobject
	 *
	 *@return    The panel value
	 */
	public JPanel getPanel() {
		return mainWFsPanel;
	}


	/**
	 *  Creates the left vertical panel with BPM buttons
	 */
	private void updateBPMsList() {
		bpmsList.validate();
		mainWFsPanel.validate();
		mainWFsPanel.repaint();
		updateWFsetOnGraphs();
	}


	/**
	 *  Description of the Method
	 */
	private void composeWFsPanel() {
		messageTextLocal.setText(null);
		graphsPanel.removeAll();
		if(showTBTarrXButton.isSelected()) {
			graphsPanel.add(analysisWFPanelX.getPanel());
		}
		if(showTBTarrYButton.isSelected()) {
			graphsPanel.add(analysisWFPanelY.getPanel());
		}
		if(showTBTarrAmpButton.isSelected()) {
			graphsPanel.add(analysisWFPanelAmp.getPanel());
		}
		graphsPanel.validate();
		graphsPanel.repaint();
		updateWFsetOnGraphs();
	}


	/**
	 *  Updates the waveform data on graph panels
	 */
	public void updateWFsetOnGraphs() {
		if(isShowing) {

			RingBPM rbpm = null;

			if(ring_bpm_set != null && indSelected >= 0 && indSelected < ring_bpm_set.size()) {
				rbpm = ring_bpm_set.getRingBPM(indSelected);
			}

			if(rbpm == null) {
				analysisWFPanelX.getPVWFPanel().clear();
				analysisWFPanelY.getPVWFPanel().clear();
				analysisWFPanelAmp.getPVWFPanel().clear();
				return;
			}

			if(showTBTarrXButton.isSelected()) {
				analysisWFPanelX.setAnalysisText(rbpm.getBPM_X().getAnalysisString());
				analysisWFPanelX.getPVWFPanel().setData(rbpm.getBPM_X().getArrY());
			}
			if(showTBTarrYButton.isSelected()) {
				analysisWFPanelY.setAnalysisText(rbpm.getBPM_Y().getAnalysisString());
				analysisWFPanelY.getPVWFPanel().setData(rbpm.getBPM_Y().getArrY());
			}
			if(showTBTarrAmpButton.isSelected()) {
				analysisWFPanelAmp.setAnalysisText(rbpm.getBPM_AMP().getAnalysisString());
				analysisWFPanelAmp.getPVWFPanel().setData(rbpm.getBPM_AMP().getArrY());
			}
		}
	}

	/**
	 *  Description of the Method
	 */
	public void clearAnalysisResults() {
		analysisWFPanelX.clearAnalysis();
		analysisWFPanelY.clearAnalysis();
		analysisWFPanelAmp.clearAnalysis();
	}


	/**
	 *  Returns the "listen to EPICS" JRadioButton
	 *
	 *@return    The listen to EPICS button
	 */
	public JRadioButton getListenToEPICS_Button() {
		return listenToEPICS_Button;
	}


	/**
	 *  Sets the ring BPM set and initialize GUI BPMs list
	 *
	 *@param  ring_bpm_set  The Parameter
	 */
	public void init(RingBPMset ring_bpm_set) {
		this.ring_bpm_set = ring_bpm_set;
		indSelected = -1;
		selectedBPM_Label.setText("No BPM Selected");
		updateBPMsList();
	}


	/**
	 *  Sets the "is showing" attribute tu "true or false" for the
	 *  RingBPMsWaveFormController panel
	 *
	 *@param  isShowing  The new "is showing" boolean value
	 */
	public void setShowing(boolean isShowing) {
		this.isShowing = isShowing;
		if(isShowing) {
			updateWFsetOnGraphs();
		}
	}

	/**
	 *  Sets the messageTextLocal attribute of the RingBPMsWaveFormController
	 *  object
	 *
	 *@param  messageTextLocal  The new messageTextLocal value
	 */
	public void setMessageTextLocal(JTextField messageTextLocal) {
		this.messageTextLocal = messageTextLocal;
	}


	/**
	 *  Sets the font for all GUI elements
	 *
	 *@param  fnt  The new font
	 */
	public void setFont(Font fnt) {

		showTBTarrXButton.setFont(fnt);
		showTBTarrYButton.setFont(fnt);
		showTBTarrAmpButton.setFont(fnt);

		listBPMs_Label.setFont(fnt);
		listenToEPICS_Button.setFont(fnt);
		exportASCII_Button.setFont(fnt);
		selectedBPM_Label.setFont(fnt);
		analyzeAll_Button.setFont(fnt);

		analysisWFPanelX.setFont(fnt);
		analysisWFPanelY.setFont(fnt);
		analysisWFPanelAmp.setFont(fnt);

		bpmsList.setFont(fnt);

		makeSnapshotButton.setFont(fnt);
		clearSnapshotButton.setFont(fnt);
		snapshotIdLabel.setFont(fnt);

	}

	/**
	 *  Sets the onwnerFrame attribute of the RingBPMsWaveFormController object
	 *
	 *@param  ownerFrame  The new onwnerFrame value
	 */
	public void setOnwnerFrame(JFrame ownerFrame) {
		this.ownerFrame = ownerFrame;
	}


	/**
	 *  Performs all analysis at once
	 */
	private void analyzeAllBPMs() {

		final JDialog progressDialog = new JDialog(ownerFrame, "Analysis Progress", true);

		progressDialog.addWindowListener(
			new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent evt) {
					stopAllAnalysis = true;
				}
			});

		final int nBPMs = ring_bpm_set.size();
		final int nPVs = 2;

		final JProgressBar progressBar = new JProgressBar(0, nBPMs * nPVs);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);

		JButton stopAnalysis_Button = new JButton("Stop Analysis");
		stopAnalysis_Button.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					stopAllAnalysis = true;
				}
			});

		stopAllAnalysis = false;

		JPanel tmp_0 = new JPanel(new FlowLayout());
		tmp_0.add(progressBar);

		JPanel tmp_1 = new JPanel(new FlowLayout());
		tmp_1.add(stopAnalysis_Button);

		progressDialog.getContentPane().setLayout(new BorderLayout());
		progressDialog.getContentPane().add(tmp_0, BorderLayout.CENTER);
		progressDialog.getContentPane().add(tmp_1, BorderLayout.SOUTH);

		Rectangle bounds = ownerFrame.getBounds();

		Runnable analysisRun =
			new Runnable() {
				public void run() {

					messageTextLocal.setText(null);

					int nCount = 0;
					for(int i = 0; i < nBPMs; i++) {
						RingBPMtbtAvg rbpmPV = ring_bpm_set.getRingBPM(i).getBPM_X();
						if(rbpmPV.getSwitchedOn()) {
							if(!performTuneAnalysis(rbpmPV, analysisWFPanelX, false)) {
								messageTextLocal.setText(null);
								messageTextLocal.setText("Analysis is not finished. Stop at BPM: " + rbpmPV.getBPMName());
								progressDialog.setVisible(false);
								progressDialog.dispose();
								updateWFsetOnGraphs();
								return;
							}
						}
						nCount++;
						progressBar.setValue(nCount);
						if(stopAllAnalysis) {
							messageTextLocal.setText(null);
							messageTextLocal.setText("Analysis is not finished. Stop at BPM: " + rbpmPV.getBPMName());
							progressDialog.setVisible(false);
							progressDialog.dispose();
							updateWFsetOnGraphs();
							return;
						}
					}

					for(int i = 0; i < nBPMs; i++) {
						RingBPMtbtAvg rbpmPV = ring_bpm_set.getRingBPM(i).getBPM_Y();
						if(rbpmPV.getSwitchedOn()) {
							if(!performTuneAnalysis(rbpmPV, analysisWFPanelY, false)) {
								messageTextLocal.setText(null);
								messageTextLocal.setText("Analysis is not finished. Stop at BPM: " + rbpmPV.getBPMName());
								progressDialog.setVisible(false);
								progressDialog.dispose();
								updateWFsetOnGraphs();
								return;
							}
						}
						nCount++;
						progressBar.setValue(nCount);
						if(stopAllAnalysis) {
							messageTextLocal.setText(null);
							messageTextLocal.setText("Analysis is not finished. Stop at BPM: " + rbpmPV.getBPMName());
							progressDialog.setVisible(false);
							progressDialog.dispose();
							updateWFsetOnGraphs();
							return;
						}
					}

					progressDialog.setVisible(false);
					progressDialog.dispose();
					updateWFsetOnGraphs();
				}
			};

		Thread analysisThread = new Thread(analysisRun);
		analysisThread.start();

		progressDialog.pack();
		progressDialog.setLocation((int) bounds.getCenterX(), (int) bounds.getCenterY());
		progressDialog.setVisible(true);
	}

	/**
	 *  Method performs tune analysis of the turn by turn data
	 *
	 *@param  rbpmPV       The ring BPM PV data container
	 *@param  awfp         The analysis panel with analysis parameters (limits)
	 *@param  showResults  The Parameter
	 *@return              The success of falue of analysis
	 */
	private boolean performTuneAnalysis(RingBPMtbtAvg rbpmPV, AnalysisWFPanel awfp, boolean showResults) {

		rbpmPV.clearAnalysis();
		PVWaveFormPanel pvWF = awfp.getPVWFPanel();

		double[] x_arr = rbpmPV.getArrX();
		double[] y_arr = rbpmPV.getArrY();

		int i_min = 0;
		int i_max = y_arr.length - 1;

		if(pvWF.getUseLimits()) {
			i_min = pvWF.getLowLimit();
			i_max = pvWF.getUppLimit();

			i_max = Math.min(i_max, y_arr.length - 1);

			if(i_min >= i_max || i_min >= x_arr.length) {
				return false;
			}
		}

		int i_size = i_max - i_min + 1;

		if(i_size < 6) {
			return false;
		}

		//fitter.clear();

//		for(int i = 0; i < i_size; i++) {
//			int ii = i + i_min;
//			if(Math.abs(y_arr[ii]) > 1.0e-10) {
////				fitter.addData(x_arr[ii], y_arr[ii]);
//			}
//		}
//
//		int n_loops = 5;
//
//		if(!fitter.guessAndFit(n_loops)) {
//
//			return false;
//		}
        
        double[] waveform = new double[i_size];
        
        
        
        System.arraycopy( y_arr, i_min, waveform, 0, i_max );
        fitter = new DampedSinusoidFit( waveform );


		double[] resArrX = new double[i_size];
		double[] resArrY = new double[i_size];

		for(int i = 0; i < i_size; i++) {
			int ii = i + i_min;
			resArrX[i] = x_arr[ii];
		}

		fitter.calculateFittedWaveform( resArrX, resArrY );

		if(showResults) {
			pvWF.setAnalysisData(resArrX, resArrY);
		}

		StringBuffer resStrB = new StringBuffer("======Analysis Results=====");
		resStrB.append(System.getProperty("line.separator"));

		resStrB.append("Ampl. = ");
		resStrB.append(fmt.format(fitter.getInitialAmplitude()));
		//resStrB.append(" +- ");
		//resStrB.append(fmt.format(fitter.getParameterError(fitter.AMP)));
        //TODO: Add amplitude error
		resStrB.append(System.getProperty("line.separator"));

		resStrB.append("Tune  = ");
		resStrB.append(fmt.format(fitter.getInitialFrequency()));
		resStrB.append(" +- ");
		resStrB.append(fmt.format(Math.sqrt(fitter.getInitialFrequencyVariance())));
		resStrB.append(System.getProperty("line.separator"));

		resStrB.append("Phase = ");
		resStrB.append(fmt.format(fitter.getInitialCosineLikePhase()));
		//resStrB.append(" +- ");
		//resStrB.append(fmt.format(fitter.getParameterError(fitter.PHASE)));
        //TODO: Add Phase error
		resStrB.append(System.getProperty("line.separator"));

		resStrB.append("Damping Rate = ");
		resStrB.append(fmt.format(-fitter.getInitialGrowthRate()));
		resStrB.append(" +- ");
		resStrB.append(fmt.format(Math.sqrt(fitter.getInitialGrowthRateVariance())));
		resStrB.append(System.getProperty("line.separator"));

		resStrB.append("Offset = ");
		resStrB.append(fmt.format(fitter.getInitialOffset()));
		resStrB.append(" +- ");
		resStrB.append(fmt.format(Math.sqrt(fitter.getInitialOffsetVariance())));
		resStrB.append(System.getProperty("line.separator"));

		resStrB.append("===========================");

		rbpmPV.setAnalysisString(resStrB.toString());

		HashMap<String, Double> resultsMap = new HashMap<String, Double>();
		resultsMap.put("AMP", new Double(fitter.getInitialAmplitude()));
		resultsMap.put("TUNE", new Double(fitter.getInitialFrequency()));
		resultsMap.put("PHASE", new Double(fitter.getInitialCosineLikePhase()));
		resultsMap.put("DAMPINGRATE", new Double(-fitter.getInitialGrowthRate()));
		resultsMap.put("OFFSET", new Double(fitter.getInitialOffset()));

        //TODO: Add amplitude and phase error
		//resultsMap.put("AMP_ERR", new Double(fitter.getParameterError(fitter.AMP)));
		resultsMap.put("TUNE_ERR", new Double(Math.sqrt(fitter.getInitialFrequencyVariance())));
		//resultsMap.put("PHASE_ERR", new Double(fitter.getParameterError(fitter.PHASE)));
		resultsMap.put("DAMPINGRATE_ERR", new Double(Math.sqrt(fitter.getInitialGrowthRateVariance())));
		resultsMap.put("OFFSET_ERR", new Double(Math.sqrt(fitter.getInitialOffsetVariance())));

		rbpmPV.setAnalysisResults(resArrX, resArrY, resultsMap);

		return true;
	}

	/**
	 *  Exports the waveforms and analysis data to the ASCII file.
	 */
	private void exportDataToASCII() {

		JFileChooser ch = new JFileChooser();
		ch.setDialogTitle("Export to ASCII");
		if(asciiDataFile != null) {
			ch.setSelectedFile(asciiDataFile);
		}
		int returnVal = ch.showSaveDialog(this.getPanel());
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			try {
				asciiDataFile = ch.getSelectedFile();
				BufferedWriter out = new BufferedWriter(new FileWriter(asciiDataFile));

				//print analysis parameters
				out.write("%  ================== RESULTS OF ANALYSIS  =======================");
				out.newLine();
				out.write("%  ===HORIZONTAL PLANE    ========================================");
				out.newLine();

				out.write("%  BPM Name ");
				out.write("       X-amp");
				out.write("   X-amp_err");
				out.write("      X-tune");
				out.write("  X-tune_err");
				out.write("     X-phase");
				out.write(" X-phase_err");
				out.write("     X-slope");
				out.write(" X-slope_err");
				out.write("    X-offset");
				out.write(" X-offset_err");
				out.newLine();

				int nBPMs = ring_bpm_set.size();

				for(int i = 0; i < nBPMs; i++) {
					RingBPMtbtAvg rbpmPV = ring_bpm_set.getRingBPM(i).getBPM_X();
					String res = rbpmPV.getBPMName();
					while(res.length() < 12) {
						res = res + " ";
					}
//TODO: Implement phase and amplitude error
					if(rbpmPV.isAnalysisDone()) {
						res = res + " " + frmt.format(((Double) rbpmPV.getAnalysisResult("AMP")).doubleValue()) + " ";
						//res = res + " " + frmt.format(((Double) rbpmPV.getAnalysisResult("AMP_ERR")).doubleValue()) + " ";
						res = res + " " + frmt.format(((Double) rbpmPV.getAnalysisResult("TUNE")).doubleValue()) + " ";
						res = res + " " + frmt.format(((Double) rbpmPV.getAnalysisResult("TUNE_ERR")).doubleValue()) + " ";
						res = res + " " + frmt.format(((Double) rbpmPV.getAnalysisResult("PHASE")).doubleValue()) + " ";
						//res = res + " " + frmt.format(((Double) rbpmPV.getAnalysisResult("PHASE_ERR")).doubleValue()) + " ";
						res = res + " " + frmt.format(((Double) rbpmPV.getAnalysisResult("DAMPINGRATE")).doubleValue()) + " ";
						res = res + " " + frmt.format(((Double) rbpmPV.getAnalysisResult("DAMPINGRATE_ERR")).doubleValue()) + " ";
						res = res + " " + frmt.format(((Double) rbpmPV.getAnalysisResult("OFFSET")).doubleValue()) + " ";
						res = res + " " + frmt.format(((Double) rbpmPV.getAnalysisResult("OFFSET_ERR")).doubleValue()) + " ";
						out.write(res);
						out.newLine();
					}
				}

				out.write("%  ================== RESULTS OF ANALYSIS  =======================");
				out.newLine();
				out.write("%  ===VERTICAL PLANE    ==========================================");
				out.newLine();

				out.write("%  BPM Name ");
				out.write("       Y-amp");
				out.write("   Y-amp_err");
				out.write("      Y-tune");
				out.write("  Y-tune_err");
				out.write("     Y-phase");
				out.write(" Y-phase_err");
				out.write("     Y-slope");
				out.write(" Y-slope_err");
				out.write("    Y-offset");
				out.write(" Y-offset_err");
				out.newLine();

				for(int i = 0; i < nBPMs; i++) {
					RingBPMtbtAvg rbpmPV = ring_bpm_set.getRingBPM(i).getBPM_Y();
					String res = rbpmPV.getBPMName();
					while(res.length() < 12) {
						res = res + " ";
					}
//TODO: Add phase and amplitude error 
					if(rbpmPV.isAnalysisDone()) {
						res = res + " " + frmt.format(((Double) rbpmPV.getAnalysisResult("AMP")).doubleValue()) + " ";
						//res = res + " " + frmt.format(((Double) rbpmPV.getAnalysisResult("AMP_ERR")).doubleValue()) + " ";
						res = res + " " + frmt.format(((Double) rbpmPV.getAnalysisResult("TUNE")).doubleValue()) + " ";
						res = res + " " + frmt.format(((Double) rbpmPV.getAnalysisResult("TUNE_ERR")).doubleValue()) + " ";
						res = res + " " + frmt.format(((Double) rbpmPV.getAnalysisResult("PHASE")).doubleValue()) + " ";
						//res = res + " " + frmt.format(((Double) rbpmPV.getAnalysisResult("PHASE_ERR")).doubleValue()) + " ";
						res = res + " " + frmt.format(((Double) rbpmPV.getAnalysisResult("DAMPINGRATE")).doubleValue()) + " ";
						res = res + " " + frmt.format(((Double) rbpmPV.getAnalysisResult("DAMPINGRATE_ERR")).doubleValue()) + " ";
						res = res + " " + frmt.format(((Double) rbpmPV.getAnalysisResult("OFFSET")).doubleValue()) + " ";
						res = res + " " + frmt.format(((Double) rbpmPV.getAnalysisResult("OFFSET_ERR")).doubleValue()) + " ";
						out.write(res);
						out.newLine();
					}
				}

				//print waveforms
				out.write("%WAVEFORMS  =====================================================");
				out.newLine();

				for(int i = 0; i < nBPMs; i++) {
					RingBPMtbtAvg rbpmPV = ring_bpm_set.getRingBPM(i).getBPM_X();

					double[] arrY = rbpmPV.getArrY();

					out.write("% BPM Name " + rbpmPV.getBPMName() + "  HORIZONTAL PLANE ");
					out.newLine();
					out.write("% WAVEFORM - EPICS DATA  nPoints=" + arrY.length);
					out.newLine();

					for(int j = 0; j < arrY.length; j++) {
						out.write(" " + j + "  " + frmt.format(arrY[j]));
						out.newLine();
					}

					if(rbpmPV.isAnalysisDone()) {
						double[] arrX = rbpmPV.getAnalysisArrX();
						arrY = rbpmPV.getAnalysisArrY();

						out.write("% BPM Name " + rbpmPV.getBPMName() + "  HORIZONTAL PLANE ");
						out.newLine();
						out.write("% WAVEFORM - FITTING DATA  nPoints=" + arrY.length);
						out.newLine();

						for(int j = 0; j < arrY.length; j++) {
							out.write(" " + arrX[j] + "  " + frmt.format(arrY[j]));
							out.newLine();
						}
					}
				}

				for(int i = 0; i < nBPMs; i++) {
					RingBPMtbtAvg rbpmPV = ring_bpm_set.getRingBPM(i).getBPM_Y();

					double[] arrY = rbpmPV.getArrY();

					out.write("% BPM Name " + rbpmPV.getBPMName() + "  VERTICAL PLANE ");
					out.newLine();
					out.write("% WAVEFORM - EPICS DATA  nPoints=" + arrY.length);
					out.newLine();

					for(int j = 0; j < arrY.length; j++) {
						out.write(" " + j + "  " + frmt.format(arrY[j]));
						out.newLine();
					}

					if(rbpmPV.isAnalysisDone()) {
						double[] arrX = rbpmPV.getAnalysisArrX();

						out.write("% BPM Name " + rbpmPV.getBPMName() + "  VERTICAL PLANE ");
						out.newLine();
						out.write("% WAVEFORM - FITTING DATA  nPoints=" + arrY.length);
						out.newLine();

						arrY = rbpmPV.getAnalysisArrY();
						for(int j = 0; j < arrY.length; j++) {
							out.write(" " + arrX[j] + "  " + frmt.format(arrY[j]));
							out.newLine();
						}
					}
				}

				for(int i = 0; i < nBPMs; i++) {
					RingBPMtbtAvg rbpmPV = ring_bpm_set.getRingBPM(i).getBPM_AMP();

					double[] arrY = rbpmPV.getArrY();

					out.write("% BPM Name " + rbpmPV.getBPMName() + "  AMPLITUDE SIGNAL ");
					out.newLine();
					out.write("% WAVEFORM - EPICS DATA  nPoints=" + arrY.length);
					out.newLine();

					for(int j = 0; j < arrY.length; j++) {
						out.write(" " + j + "  " + frmt.format(arrY[j]));
						out.newLine();
					}
				}

				out.flush();
				out.close();
				preferences.put(asciiDataFileKey, asciiDataFile.getAbsolutePath());
				try {
					preferences.flush();

				} catch(BackingStoreException exept) {
				}
			} catch(IOException exp) {
				Toolkit.getDefaultToolkit().beep();
				System.out.println(exp.toString());
			}
		}
	}


	/**
	 *  Reads the ASCII file location from the java preferences
	 */
	private void findOldASCIIFile() {
		if(asciiDataFile == null) {
			String abs_path = preferences.get(asciiDataFileKey, null);
			if(abs_path != null) {
				asciiDataFile = new File(abs_path);
				if(!asciiDataFile.exists()) {
					asciiDataFile = null;
				}
			}
		}
	}

}

