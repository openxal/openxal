package xal.app.ringbpmviewer;

import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import javax.swing.border.*;
import java.awt.event.*;
import java.text.*;

/**
 *  The panel keeps the PVWaveFormPanel and GUI controll elements for analysis.
 *
 *@author     shishlo
 */
public class AnalysisWFPanel {

	private PVWaveFormPanel wfPanel = new PVWaveFormPanel();

	private JPanel aWFPanel = new JPanel(new BorderLayout());

	//analysis control panels
	private JPanel analysisPanel = new JPanel(new BorderLayout());

	private JPanel analysisCntrlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 2));
	private JPanel analysisTxtPanel = new JPanel(new BorderLayout());

	private JRadioButton showAnalysis_Button = new JRadioButton("A", false);
	private JButton makeAnalysis_Button = new JButton("====   Perform Analysis   ====");

	private JTextArea analysisTXT = new JTextArea();

	//analysis button listeners
	private Vector<ActionListener> analysisListenersV = new Vector<ActionListener>();


	/**
	 *  Constructor for the AnalysisWFPanel object
	 */
	public AnalysisWFPanel() {

		Border etchedBorder = BorderFactory.createEtchedBorder();

		aWFPanel.setBorder(etchedBorder);
		aWFPanel.add(wfPanel.getPanel(), BorderLayout.CENTER);

		aWFPanel.add(analysisPanel, BorderLayout.EAST);

		analysisPanel.setBorder(etchedBorder);
		analysisPanel.add(analysisCntrlPanel, BorderLayout.NORTH);
		analysisPanel.add(analysisTxtPanel, BorderLayout.CENTER);

		JScrollPane scrollPane = new JScrollPane(analysisTXT,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		analysisTxtPanel.add(scrollPane, BorderLayout.CENTER);

		analysisCntrlPanel.add(showAnalysis_Button);
		analysisCntrlPanel.add(makeAnalysis_Button);

		makeAnalysis_Button.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					ActionEvent ae = new ActionEvent(wfPanel, 0, "make_analysis");
					for(int i = 0, n = analysisListenersV.size(); i < n; i++) {
                        ActionListener al = analysisListenersV.get(i);
 						al.actionPerformed(ae);
					}
				}
			});

		showAnalysis_Button.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					makeAnalysisCntrlPanel();
				}
			});

		showAnalysis_Button.setToolTipText("Show analysis panel");

		makeAnalysisCntrlPanel();
	}

	/**
	 *  Forms the analysis panel
	 */
	private void makeAnalysisCntrlPanel() {
		if(showAnalysis_Button.isSelected()) {
			analysisCntrlPanel.removeAll();
			analysisCntrlPanel.add(showAnalysis_Button);
			analysisCntrlPanel.add(makeAnalysis_Button);
			analysisPanel.removeAll();
			analysisPanel.add(analysisCntrlPanel, BorderLayout.NORTH);
			analysisPanel.add(analysisTxtPanel, BorderLayout.CENTER);
		} else {
			analysisCntrlPanel.removeAll();
			analysisCntrlPanel.add(showAnalysis_Button);
			analysisPanel.removeAll();
			analysisPanel.add(analysisCntrlPanel, BorderLayout.NORTH);
		}
		aWFPanel.validate();
		aWFPanel.repaint();
	}


	/**
	 *  Adds an Analysis Button Listener of the analysis button
	 *
	 *@param  al  The Analysis Button Listener
	 */
	public void addAnalysisButtonListener(ActionListener al) {
		analysisListenersV.add(al);
	}

	/**
	 *  Returns the JPanel instance with a wave form graph and analysis panel
	 *
	 *@return    The panel instance
	 */
	public JPanel getPanel() {
		return aWFPanel;
	}

	/**
	 *  Returns the PVWaveFormPanel object
	 *
	 *@return    The PVWaveFormPanel instance
	 */
	public PVWaveFormPanel getPVWFPanel() {
		return wfPanel;
	}

	/**
	 *  Sets the analysis text
	 *
	 *@param  txt  The new analysis text
	 */
	public void setAnalysisText(String txt) {
		analysisTXT.setText(null);
		analysisTXT.setText(txt);
	}

	/**
	 *  Sets the font of all GUI elements
	 *
	 *@param  fnt  The new font
	 */
	public void setFont(Font fnt) {
		makeAnalysis_Button.setFont(fnt);
		showAnalysis_Button.setFont(fnt);
		analysisTXT.setFont(fnt);
		wfPanel.setFont(fnt);
	}


	/**
	 *  Clear analysis data
	 */
	public void clearAnalysis() {
		wfPanel.clearAnalysis();
	}



	/**
	 *  Test method
	 *
	 *@param  args  No arguments needed
	 */
	public static void main(String args[]) {
		JFrame mainFrame = new JFrame("Test of the PVWaveFormPanel class");
		mainFrame.addWindowListener(
			new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent evt) {
					System.exit(0);
				}
			});

		mainFrame.getContentPane().setLayout(new BorderLayout());

		AnalysisWFPanel awfp = new AnalysisWFPanel();

		mainFrame.getContentPane().add(awfp.getPanel(), BorderLayout.CENTER);

		awfp.getPVWFPanel().setAxisNameY("Value, [mm]");
		awfp.getPVWFPanel().setAxisNameX("turn");

		mainFrame.pack();
		mainFrame.setSize(new Dimension(300, 430));
		mainFrame.setVisible(true);

	}

}


