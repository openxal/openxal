package xal.app.experimentautomator.core;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton.ToggleButtonModel;
import javax.swing.border.EmptyBorder;

import xal.extension.application.Commander;

public class PrefWindow extends JFrame {

	private static final long serialVersionUID = 1L;

	private static final int HORIZ_SIZE = 650;
	private static final int VERT_SIZE = 200;

	private static final Integer DEFAULT_DELAY_1 = 1000;
	private static final Integer DEFAULT_DELAY_2 = 1000;
	private static final Integer DEFAULT_DELAY_3 = 1000;
	private static final Integer DEFAULT_THRESHOLD_CHECKS = 15;

	private JTextField delayTimeTextField1;
	private JTextField delayTimeTextField2;
	private JTextField delayTimeTextField3;
	private JTextField thresholdChecks;

	private JButton restoreDefaultsButton;
	private JButton updatePrefsButton;

	protected JTextArea textView;

	EADocument EADoc;

	/** Creates a new instance of PrefWindow */
	public PrefWindow(EADocument aDocument) {
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
	protected void customizeCommands(final Commander commander) {
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
		// JPanel eastPanel = new JPanel();
		// JPanel westPanel = new JPanel();
		JPanel centerPanel = new JPanel();
		JPanel southPanel = new JPanel();
		// JPanel controlPanelA = new JPanel();
		// JPanel controlPanelB = new JPanel();

		northPanel.setLayout(new GridLayout(6, 2));
		southPanel.setLayout(new GridLayout(2, 1));

		northPanel.setBorder(new EmptyBorder(2, 110, 2, 80));

		JLabel instructionLabel = new JLabel(
				"New settings will only be used on next experiment initialization");

		delayTimeTextField1 = new JTextField();
		delayTimeTextField1.setText(DEFAULT_DELAY_1.toString());
		delayTimeTextField2 = new JTextField();
		delayTimeTextField2.setText(DEFAULT_DELAY_2.toString());
		delayTimeTextField3 = new JTextField();
		delayTimeTextField3.setText(DEFAULT_DELAY_3.toString());
		thresholdChecks = new JTextField();
		thresholdChecks.setText(DEFAULT_THRESHOLD_CHECKS.toString());

		restoreDefaultsButton = new JButton("Restore Defaults");
		restoreDefaultsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				resoreDefaults();
			}
		});

		updatePrefsButton = new JButton("Update Running Experiment");
		updatePrefsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				EADoc.updatePrefs();
			}
		});

		northPanel.add(new JLabel("Post-set delay time (ms)"));
		northPanel.add(delayTimeTextField1);
		northPanel.add(new JLabel("Threshold re-check delay time (ms)"));
		northPanel.add(delayTimeTextField2);
		northPanel.add(new JLabel("Pre-acquisition delay time (ms)"));
		northPanel.add(delayTimeTextField3);
		northPanel.add(new JLabel("Number of threshold re-checks"));
		northPanel.add(thresholdChecks);
		northPanel.add(Box.createHorizontalGlue());
		northPanel.add(Box.createHorizontalGlue());

		centerPanel.add(instructionLabel);

		southPanel.add(updatePrefsButton);
		southPanel.add(restoreDefaultsButton);

		getContentPane().add(northPanel, BorderLayout.NORTH);
		getContentPane().add(centerPanel, BorderLayout.CENTER);
		getContentPane().add(southPanel, BorderLayout.SOUTH);

	}

	public Integer getDelayTime1() {
		return Integer.parseInt(delayTimeTextField1.getText());
	}

	public Integer getDelayTime2() {
		return Integer.parseInt(delayTimeTextField2.getText());
	}

	public Integer getDelayTime3() {
		return Integer.parseInt(delayTimeTextField3.getText());
	}

	public Integer getThresholdChecks() {
		return Integer.parseInt(thresholdChecks.getText());
	}

	protected void resoreDefaults() {
		delayTimeTextField1.setText(DEFAULT_DELAY_1.toString());
		delayTimeTextField2.setText(DEFAULT_DELAY_2.toString());
		delayTimeTextField3.setText(DEFAULT_DELAY_3.toString());
		thresholdChecks.setText(DEFAULT_THRESHOLD_CHECKS.toString());
	}

}
