/*
 * EAWindow.java
 *
 * Created on July 22nd, 2010
 */

package xal.app.experimentautomator.core;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToggleButton.ToggleButtonModel;
import javax.swing.border.EmptyBorder;

import xal.app.experimentautomator.listeners.InitButtonActionListner;
import xal.app.experimentautomator.listeners.LoadStateButtonActionListener;
import xal.app.experimentautomator.listeners.SaveButtonActionListener;
import xal.app.experimentautomator.listeners.SaveStateButtonActionListener;
import xal.app.experimentautomator.listeners.SkipButtonActionListener;
import xal.app.experimentautomator.listeners.StartButtonActionListener;
import xal.app.experimentautomator.listeners.StepButtonActionListener;
import xal.app.experimentautomator.listeners.StopButtonActionListener;
import xal.extension.application.Commander;
import xal.extension.application.smf.AcceleratorWindow;

public class EAWindow extends AcceleratorWindow {

	private static final long serialVersionUID = 1L;

	private static final int HORIZ_SIZE = 900;
	private static final int VERT_SIZE = 400;

	JRadioButton elemScanButton;
	JRadioButton probeScanButton;
	JCheckBox boundMonitorCheckBox;
	JCheckBox pvAcqTableCheckBox;
	JCheckBox probeAcqTableCheckBox;

	private JTextPane statusLabel;
	private JTextPane messageLabel;
	private JProgressBar progressBar;

	private JTextField scanTableTextField;
	private JTextField pvAcqTableTextField;
	private JTextField probeAcqTableTextField;
	private JTextField boundTableTextField;
	// private JTextField delayTimeTextField;

	private JButton skipButton;
	private JButton stepButton;
	private JButton saveButton;
	private JButton pauseButton;
	private JButton startButton;
	private JButton initButton;
	private JButton saveStateButton;
	private JButton loadStateButton;
	private JButton prefButton;

	protected JTextArea textView;
	protected String scanTableFileName;
	JPanel panel = new JPanel();

	EADocument EADoc;

	/** Creates a new instance of MainWindow */
	public EAWindow(EADocument aDocument) {
		super(aDocument);
		EADoc = aDocument;
		setSize(HORIZ_SIZE, VERT_SIZE);
		makeContent();
	}

	public boolean usesToolbar() {
		return false;
	}

	/**
	 * Register actions specific to this window instance. This code demonstrates
	 * how to define custom actions for menus and the toolbar for a particular
	 * window instance. This method is optional. You may similarly define
	 * actions in the document class if those actions are document specific and
	 * also for the entire application if the actions are application wide.
	 * 
	 * @param commander
	 *            The commander with which to register the custom commands.
	 */
	public void customizeCommands(final Commander commander) {
		// define a toggle "edit" action
		final ToggleButtonModel editModel = new ToggleButtonModel();
		editModel.setSelected(true);
		editModel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				textView.setEditable(editModel.isSelected());
				Logger.getLogger("global").log(Level.INFO,
						"Toggle whether text is editable.");
				System.out.println("toggled editable...");
			}
		});
		commander.registerModel("toggle-editable", editModel);
	}

	/**
	 * Getter of the text view that displays the document content.
	 * 
	 * @return The text area that displays the document text.
	 */
	JTextArea getTextView() {
		return textView;
	}

	/**
	 * Create the main window subviews.
	 */
	protected void makeContent() {
		getContentPane().setLayout(new BorderLayout());

		JPanel northPanel = new JPanel();
		JPanel eastPanel = new JPanel();
		JPanel westPanel = new JPanel();
		JPanel centerPanel = new JPanel();
		JPanel southPanel = new JPanel();
		JPanel controlPanelA = new JPanel();
		JPanel controlPanelB = new JPanel();

		northPanel.setLayout(new GridLayout(2, 4));
		eastPanel.setLayout(new GridLayout(4, 1));
		westPanel.setLayout(new GridLayout(4, 1));
		centerPanel.setLayout(new GridLayout(4, 1));
		southPanel.setLayout(new GridLayout(0, 1));
		controlPanelA.setLayout(new FlowLayout());
		controlPanelB.setLayout(new FlowLayout());

		northPanel.setBorder(new EmptyBorder(2, 110, 2, 80));
		westPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

		ButtonGroup scanGroup = new ButtonGroup();

		elemScanButton = new JRadioButton("Elem. Scan", true);
		probeScanButton = new JRadioButton("Probe Scan");
		scanGroup.add(elemScanButton);
		scanGroup.add(probeScanButton);

		pvAcqTableCheckBox = new JCheckBox("Record Machine PVs", false);
		probeAcqTableCheckBox = new JCheckBox("Record Model Probe", false);
		boundMonitorCheckBox = new JCheckBox("PV Bound Monitoring", false);

		initButton = new JButton("Initialize");
		startButton = new JButton("Start");
		pauseButton = new JButton("Pause");
		stepButton = new JButton("Step Forward");
		skipButton = new JButton("Skip Step");
		saveButton = new JButton("Save Data");
		saveStateButton = new JButton("Save State");
		loadStateButton = new JButton("Load State");
		prefButton = new JButton("More Prefs");

		final JLabel scanTableLabel = new JLabel("Element Scan Table");
		JLabel pvAcqListLabel = new JLabel("PV Acquisition List");
		JLabel probeAcqListLabel = new JLabel("Probe Acquisition List");
		JLabel boundTableLabel = new JLabel("PV Bound Table");
		// JLabel delayTimeLabel = new JLabel("Delay Time (ms)");

		statusLabel = new JTextPane();
		statusLabel.setContentType("text/html");
		statusLabel.setText("Status");
		statusLabel.setEditable(false);
		statusLabel.setBackground(null);
		statusLabel.setBorder(null);
		messageLabel = new JTextPane();
		messageLabel.setContentType("text/html");
		messageLabel.setText("Message");
		messageLabel.setEditable(false);
		messageLabel.setBackground(null);
		messageLabel.setBorder(null);
		progressBar = new JProgressBar();

		scanTableTextField = new JTextField(12);
		scanTableTextField.setDisabledTextColor(panel.getBackground());

		pvAcqTableTextField = new JTextField(12);
		pvAcqTableTextField.setDisabledTextColor(panel.getBackground());
		pvAcqTableTextField.setEnabled(true);

		probeAcqTableTextField = new JTextField(12);
		probeAcqTableTextField.setDisabledTextColor(panel.getBackground());
		probeAcqTableTextField.setEnabled(false);

		boundTableTextField = new JTextField(12);
		boundTableTextField.setDisabledTextColor(panel.getBackground());
		boundTableTextField.setEnabled(false);

		// delayTimeTextField = new JTextField(5);
		// delayTimeTextField.setText(EADocument.DEFAULT_DELAY.toString());

		JButton scanTableBrowseButton = new JButton("Browse");
		scanTableBrowseButton.addActionListener(new BrowseActionListener(
				scanTableTextField));

		JButton pvAcqTableBrowseButton = new JButton("Browse");
		pvAcqTableBrowseButton.addActionListener(new BrowseActionListener(
				pvAcqTableTextField));

		JButton probeAcqTableBrowseButton = new JButton("Browse");
		probeAcqTableBrowseButton.addActionListener(new BrowseActionListener(
				probeAcqTableTextField));

		JButton boundTableBrowseButton = new JButton("Browse");
		boundTableBrowseButton.addActionListener(new BrowseActionListener(
				boundTableTextField));

		elemScanButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				scanTableLabel.setText("Element Scan Table");
				pvAcqTableCheckBox.setSelected(true);
				pvAcqTableCheckBox.setEnabled(true);
				pvAcqTableTextField.setEnabled(true);
				probeAcqTableCheckBox.setSelected(false);
				probeAcqTableTextField.setEnabled(false);
			}
		});

		probeScanButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				scanTableLabel.setText("Probe Scan Table");
				pvAcqTableCheckBox.setSelected(false);
				pvAcqTableCheckBox.setEnabled(false);
				pvAcqTableTextField.setEnabled(false);
				probeAcqTableCheckBox.setSelected(true);
				probeAcqTableTextField.setEnabled(true);
			}
		});

		elemScanButton.setSelected(true);

		{
			pvAcqTableCheckBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					pvAcqTableTextField.setEnabled(pvAcqTableCheckBox
							.isSelected());
				}
			});

			probeAcqTableCheckBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					probeAcqTableTextField.setEnabled(probeAcqTableCheckBox
							.isSelected());
				}
			});
			pvAcqTableCheckBox.setSelected(true);
		}

		boundMonitorCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boundTableTextField.setEnabled(boundMonitorCheckBox
						.isSelected());
			}
		});

		prefButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				EADoc.editPreferences();
			}
		});

		// row 1
		northPanel.add(elemScanButton);
		northPanel.add(pvAcqTableCheckBox);
		northPanel.add(boundMonitorCheckBox);
		northPanel.add(Box.createHorizontalGlue());
		// row 2
		northPanel.add(probeScanButton);
		northPanel.add(probeAcqTableCheckBox);
		northPanel.add(prefButton);
		northPanel.add(Box.createHorizontalGlue());

		controlPanelA.add(initButton);
		controlPanelA.add(startButton);
		controlPanelA.add(pauseButton);
		controlPanelA.add(stepButton);
		controlPanelA.add(skipButton);
		controlPanelB.add(saveButton);
		controlPanelB.add(saveStateButton);
		controlPanelB.add(loadStateButton);

		westPanel.add(scanTableLabel);
		westPanel.add(pvAcqListLabel);
		westPanel.add(probeAcqListLabel);
		westPanel.add(boundTableLabel);

		eastPanel.add(scanTableBrowseButton);
		eastPanel.add(pvAcqTableBrowseButton);
		eastPanel.add(probeAcqTableBrowseButton);
		eastPanel.add(boundTableBrowseButton);

		centerPanel.add(scanTableTextField);
		centerPanel.add(pvAcqTableTextField);
		centerPanel.add(probeAcqTableTextField);
		centerPanel.add(boundTableTextField);

		southPanel.add(controlPanelA);
		southPanel.add(controlPanelB);
		southPanel.add(statusLabel);
		southPanel.add(messageLabel);
		southPanel.add(progressBar);

		getContentPane().add(northPanel, BorderLayout.NORTH);
		getContentPane().add(southPanel, BorderLayout.SOUTH);
		getContentPane().add(eastPanel, BorderLayout.EAST);
		getContentPane().add(westPanel, BorderLayout.WEST);
		getContentPane().add(centerPanel, BorderLayout.CENTER);

		initButton.addActionListener(new InitButtonActionListner(EADoc));
		startButton.addActionListener(new StartButtonActionListener(EADoc));
		pauseButton.addActionListener(new StopButtonActionListener(EADoc));
		stepButton.addActionListener(new StepButtonActionListener(EADoc));
		skipButton.addActionListener(new SkipButtonActionListener(EADoc));
		saveButton.addActionListener(new SaveButtonActionListener(EADoc));
		saveStateButton.addActionListener(new SaveStateButtonActionListener(
				EADoc));
		loadStateButton.addActionListener(new LoadStateButtonActionListener(
				EADoc));

		initButton
				.setToolTipText("<html>Initializes a new experiment. Will read initial values of PV's from scan table. <br> Must have an accelerator sequence selected and a path to the scan table and the acquisition list defined</html>");
		startButton
				.setToolTipText("Begin the experiment. Runs all steps of the scan consecutively.</html>");
		pauseButton
				.setToolTipText("Sends a pause request to the experiment. Will suspend on next step");
		stepButton
				.setToolTipText("Executes one step of the experiment scan table");
		skipButton
				.setToolTipText("Skips one step of the experiment scan table");
		saveButton
				.setToolTipText("Saves the current data in a timestamped CSV file");
		saveStateButton
				.setToolTipText("<html>Saves the current values of the PVs to be modified by the element scan table <br> A path to a valid element scan table must be entered in the text field.</html>");
		loadStateButton
				.setToolTipText("Sets the PVs according to previously saved state");

		messageLabel.setToolTipText("Experiment Message");
		statusLabel.setToolTipText("Experiment Status");
		progressBar.setToolTipText("Experiment Progress");

	}

	public String getElementScanTableTextField() {
		return scanTableTextField.getText().trim();
	}

	public String getProbeScanTableTextField() {
		return scanTableTextField.getText().trim();
	}

	public String getPvAcqTableTextField() {
		return pvAcqTableTextField.getText().trim();
	}

	public String getProbeAcqTableTextField() {
		return probeAcqTableTextField.getText().trim();
	}

	public String getBoundTableTextField() {
		return boundTableTextField.getText().trim();
	}

	public void setStatusText(String status) {
		statusLabel.setText(status);
	}

	public void setMessageText(String message) {
		messageLabel.setText(message);
	}

	public void diableButtons() {
		skipButton.setEnabled(false);
		stepButton.setEnabled(false);
		startButton.setEnabled(false);
		initButton.setEnabled(false);
		saveStateButton.setEnabled(false);
		loadStateButton.setEnabled(false);

		pauseButton.setEnabled(true);
	}

	public void enableButtons() {
		skipButton.setEnabled(true);
		stepButton.setEnabled(true);
		startButton.setEnabled(true);
		initButton.setEnabled(true);
		pauseButton.setEnabled(true);
		saveStateButton.setEnabled(true);
		loadStateButton.setEnabled(true);
	}

	public void startButtonLabel(String label) {
		startButton.setText(label);
	}

	public void updateProgress(Integer complete, Integer total) {
		progressBar.setMaximum(total);
		progressBar.setValue(complete);
		Integer percentComplete = (complete * 100 / total);
		progressBar.setString(complete.toString() + "/" + total.toString()
				+ "    (" + percentComplete.toString() + "%)");
		progressBar.setStringPainted(true);
	}
}
