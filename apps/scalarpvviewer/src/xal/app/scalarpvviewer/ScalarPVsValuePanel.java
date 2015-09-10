/*
 *  ScalarPVsValuePanel.java
 *
 *  Created on May 24, 2005
 */
package xal.app.scalarpvviewer;

import xal.extension.widgets.apputils.SimpleChartPopupMenu;
import xal.tools.apputils.VerticalLayout;
import xal.extension.widgets.plot.FunctionGraphsJPanel;
import xal.extension.scan.UpdatingEventController;
import xal.tools.text.ScientificNumberFormat;
import xal.extension.widgets.plot.CurveData;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *  The panel keeps the values graph and the PVs table.
 *
 *@author    shishlo
 */
public class ScalarPVsValuePanel {

	private final JPanel mainPanel = new JPanel(new BorderLayout());
	private final JLabel titleLabel = new JLabel("EMPTY VIEWER. GO TO PREDEFINED CONFIG. PANEL", JLabel.CENTER);

	private JSplitPane mainSplitPanel = null;

	private final JPanel upperPanel = new JPanel(new BorderLayout());
	private final JPanel graphPanel = new JPanel(new BorderLayout());
	private final JPanel tablePanel = new JPanel(new BorderLayout());

	private TitledBorder borderGraph = null;
	private final FunctionGraphsJPanel GP = new FunctionGraphsJPanel();

	private ScalarPVs spvs = null;

	private ScalarPVsValuesTable table = null;

	private UpdatingEventController uc = null;

	//cntrl panel
	private final JLabel cntrlPanelTitle_Label = new JLabel("=UPDATE CONTROL=", JLabel.CENTER);
	private final JRadioButton autoUpdate_Button = new JRadioButton("Listen to EPICS", false);
	private final JSpinner freq_cntrlPanel_Spinner = new JSpinner(new SpinnerNumberModel( 1, 1, 300, 1 ));
	private final JLabel cntrlPanelTime_Label = new JLabel("Update Time [sec]", JLabel.LEFT);
	private final JButton setRefButton = new JButton("Memorize as Ref.");
	private final JButton wrapButton = new JButton("Wrap Phases");

	private final JSpinner averagingSpinner = new JSpinner( new SpinnerNumberModel( ScalarPV.DEFAULT_AVERAGING_PULSE_COUNT, 1, 100, 1 ) );
	private final JLabel averagingLabel = new JLabel( "Averging [pulses]", JLabel.LEFT );

	private final JLabel lastTimeLabel_Label = new JLabel("Last Time Memorized at:", JLabel.CENTER);
	private final JLabel lastTimeText_Label = new JLabel("NEVER", JLabel.CENTER);


	/**
	 *  Constructor for the ScalarPVsValuePanel object
	 *
	 *@param  spvsIn  ScalarPVs object with ScalarPVs
	 *@param  ucIn    The update controller
	 */
	public ScalarPVsValuePanel(ScalarPVs spvsIn, UpdatingEventController ucIn) {
		uc = ucIn;
		spvs = spvsIn;
		table = new ScalarPVsValuesTable(spvs);

		//Graph panel definition
		GP.setOffScreenImageDrawing(true);
		GP.setGraphBackGroundColor(Color.white);
		GP.setAxisNames("PV index", "PV Value");
		GP.setNumberFormatX(new DecimalFormat("#0"));
		ScientificNumberFormat frmt = new ScientificNumberFormat( 5, 9, false );
		frmt.setFixedLength(true);
		GP.setNumberFormatY(frmt);
		GP.setLimitsAndTicksX(0., 1.0, 1, 0);
		GP.addHorizontalLine(0.0, Color.black);
		SimpleChartPopupMenu.addPopupMenuTo(GP);

		Border etchedBorder = BorderFactory.createEtchedBorder();
		borderGraph = BorderFactory.createTitledBorder(etchedBorder, "Current,Reference, and Difference PVs Values");
		graphPanel.setBorder(borderGraph);

    FunctionGraphsJPanel.ClickedPoint cpObj = GP.getClickedPointObject();
    cpObj.xValueFormat = new MarkerFormat(spvs);
    cpObj.xValueText = new JTextField(20);
    cpObj.xValueText.setHorizontalAlignment(JTextField.CENTER);
    cpObj.xValueText.setForeground(Color.blue);		
		
		graphPanel.add(GP, BorderLayout.CENTER);
		graphPanel.add(cpObj.xValueText,BorderLayout.SOUTH);

		//make control panel
		JPanel tmp_panel_1 = new JPanel();
		tmp_panel_1.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 1));
		tmp_panel_1.add(autoUpdate_Button);

		JPanel tmp_panel_2 = new JPanel();
		tmp_panel_2.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 1));
		tmp_panel_2.add(freq_cntrlPanel_Spinner);
		tmp_panel_2.add(cntrlPanelTime_Label);

		JPanel averagingPanel = new JPanel();
		averagingPanel.setLayout( new FlowLayout( FlowLayout.LEFT, 1, 1 ) );
		averagingPanel.add( averagingSpinner );
		averagingPanel.add( averagingLabel );

		JPanel tmp_panel_3 = new JPanel();
		tmp_panel_3.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 1));
		tmp_panel_3.add(setRefButton);

		JPanel tmp_panel_3_1 = new JPanel();
		tmp_panel_3_1.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 1));
		tmp_panel_3_1.add(wrapButton);
		
		JPanel tmp_panel_4 = new JPanel();
		tmp_panel_4.setLayout(new GridLayout(5, 1, 1, 1));
		tmp_panel_4.add(tmp_panel_1);
		tmp_panel_4.add(tmp_panel_2);
		tmp_panel_4.add( averagingPanel );
		tmp_panel_4.add(tmp_panel_3);
		tmp_panel_4.add(tmp_panel_3_1);

		JPanel tmp_panel_5 = new JPanel(new GridLayout(2, 1, 1, 1));
		tmp_panel_5.add(lastTimeLabel_Label);
		tmp_panel_5.add(lastTimeText_Label);
		tmp_panel_5.setBorder(etchedBorder);

		JPanel tmp_panel_6 = new JPanel();
		tmp_panel_6.setLayout(new VerticalLayout());
		tmp_panel_6.add(cntrlPanelTitle_Label);
		tmp_panel_6.add(tmp_panel_4);
		tmp_panel_6.add(tmp_panel_5);
		tmp_panel_6.setBorder(etchedBorder);

		//make upper panel
		upperPanel.add(graphPanel, BorderLayout.CENTER);
		upperPanel.add(tmp_panel_6, BorderLayout.WEST);

		//make table panel
		tablePanel.add(table.getPanel(), BorderLayout.CENTER);

		mainSplitPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				upperPanel, tablePanel);

		titleLabel.setForeground(Color.red);

		mainPanel.add(mainSplitPanel, BorderLayout.CENTER);
		mainPanel.add(titleLabel, BorderLayout.NORTH);

		//make listeners
		uc.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					if (autoUpdate_Button.isSelected()) {
						spvs.measure();
						GP.refreshGraphJPanel();
					}
				}
			});

		table.addChangeListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					updateGraph();
				}
			});

		autoUpdate_Button.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (autoUpdate_Button.isSelected()) {
						spvs.measure();
						GP.refreshGraphJPanel();
					}
				}
			});

		freq_cntrlPanel_Spinner.addChangeListener(
			new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					double time_new = ((Integer) freq_cntrlPanel_Spinner.getValue()).doubleValue();
					double time_old = uc.getUpdateTime();
					uc.setUpdateTime(time_new);
					if (time_old > time_new) {
						uc.update();
					}
				}
			});

		averagingSpinner.addChangeListener( new ChangeListener() {
			public void stateChanged( final ChangeEvent event ) {
				final int pulseCount = (Integer)averagingSpinner.getValue();
				spvs.setAveragingPulseCount( pulseCount );
			}
		});

		setRefButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					spvs.memorizeRef();
					SimpleDateFormat dF = new SimpleDateFormat("MM.dd.yy HH:mm ");
					lastTimeText_Label.setText(dF.format(new Date()));
					GP.refreshGraphJPanel();
					table.doLayout();
				}
			});

		wrapButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int nPV = spvs.getSize();
					for (int i = 0; i < nPV; i++) {
						spvs.getScalarPV(i).setWrappingSwitch(true);
					}
					updateGraph();
					GP.refreshGraphJPanel();
					table.doLayout();
				}
			});
	}


	/**
	 *  Sets the title
	 *
	 *@param  title  The new title
	 */
	public void setTitle(String title) {
		titleLabel.setText(title);
	}


	/**
	 *  Returns the title
	 *
	 *@return    The title
	 */
	public String getTitle() {
		return titleLabel.getText();
	}


	/**
	 *  Returns the boolean value for the "listen to EPICS" mode
	 *
	 *@return    the boolean value for the "listen to EPICS" mode
	 */
	public boolean listenModeOn() {
		return autoUpdate_Button.isSelected();
	}


	/**
	 *  Sets the boolean value for the "listen to EPICS" mode
	 *
	 *@param  isOn  the new boolean value for the "listen to EPICS" mode
	 */
	public void listenModeOn(boolean isOn) {
		autoUpdate_Button.setSelected(isOn);
	}


	/**
	 *  Returns the time step of updating
	 *
	 *@return    The time step value
	 */
	public double getTimeStep() {
		return ((Integer) freq_cntrlPanel_Spinner.getValue()).doubleValue();
	}


	/**
	 *  Sets the time step of updating
	 *
	 *@param  timeStep  The new time step value
	 */
	public void setTimeStep(double timeStep) {
		freq_cntrlPanel_Spinner.setValue(new Integer((int) timeStep));
		uc.setUpdateTime(timeStep);
	}


	/**
	 *  Sets the string that defines the last time when the values were memorized
	 *
	 *@param  lstTime  The string that defines the last time when the values were memorized
	 */
	public void setLastMemorizingTime(String lstTime) {
		lastTimeText_Label.setText(lstTime);
	}


	/**
	 *  Returns the string that defines the last time when the values were memorized
	 *
	 *@return    The string that defines the last time when the values were memorized
	 */
	public String getLastMemorizingTime() {
		return lastTimeText_Label.getText();
	}


	/**
	 *  Gets the panel attribute of the ScalarPVsValuePanel object
	 *
	 *@return    The panel value
	 */
	public JPanel getPanel() {
		return mainPanel;
	}


	/**
	 *  Update the graphs according the table
	 */
	public void updateGraph() {
		Vector<CurveData> gdV = new Vector<CurveData>();
		int nPV = spvs.getSize();
		for (int i = 0; i < nPV; i++) {
			ScalarPV spv = spvs.getScalarPV(i);
			//to do wrapping -);
			spv.setIndex(spv.getIndex());
			if (spv.showValue()) {
				gdV.add(spv.getValueGraphData());
			}
			if (spv.showRef()) {
				gdV.add(spv.getRefGraphData());
			}
			if (spv.showDif()) {
				gdV.add(spv.getDifGraphData());
			}
		}
		spvs.findMinMax();
		GP.removeAllCurveData();
		GP.setLimitsAndTicksX(0., 1.0, nPV + 1, 0);
		GP.addCurveData(gdV);
	}



	/**
	 *  Sets the font
	 *
	 *@param  fnt  The new font
	 */
	public void setFont(Font fnt) {
		table.setFont(fnt);
		borderGraph.setTitleFont(fnt);
		titleLabel.setFont(fnt);

		cntrlPanelTitle_Label.setFont(fnt);
		autoUpdate_Button.setFont(fnt);
		lastTimeLabel_Label.setFont(fnt);
		lastTimeText_Label.setFont(fnt);
		averagingLabel.setFont( fnt );

		freq_cntrlPanel_Spinner.setFont(fnt);
		((JSpinner.DefaultEditor) freq_cntrlPanel_Spinner.getEditor()).getTextField().setFont(fnt);
		cntrlPanelTime_Label.setFont(fnt);

		averagingSpinner.setFont( fnt );
		((JSpinner.DefaultEditor) averagingSpinner.getEditor()).getTextField().setFont(fnt);

		setRefButton.setFont(fnt);
		wrapButton.setFont(fnt);
	}

	
	//===========================================================
  // Auxiliary classes
  //===========================================================

  /**
   *  The format for marked values
   *
   *@author     shishlo
   */
  class MarkerFormat extends DecimalFormat {
      /** serialization ID */
      private static final long serialVersionUID = 1L;

    private ScalarPVs spvs= null;


    /**
     *  Constructor for the MarkerFormat object
     *
     *@param  spvsIn  Description of the Parameter
     */
    public MarkerFormat(ScalarPVs spvsIn) {
      spvs = spvsIn;
    }


    /**
     *  The overridden format method of the DecimalFormat class.
     *
     *@param  val         The value to format
     *@param  toAppendTo  The string buffer to add to
     *@param  pos         The position where to add
     *@return             The formated string
     */
    @Override
    public StringBuffer format(double val, StringBuffer toAppendTo, FieldPosition pos) {
      StringBuffer strb = new StringBuffer(" ");
      if (spvs != null && spvs.getSize() > 0) {
        int ind = (int) Math.round(val - 1.0);
        if (ind >= 0 && ind < spvs.getSize()) {
					String pv_name = spvs.getScalarPV(ind).getMonitoredPV().getChannelName();
          strb.append(pv_name);
        } else {
          strb.append(" ");
        }
      } else {
        strb.append(" ");
      }
      strb.append(" ");
      return strb;
    }


    /**
     *  The overridden method of the DecimalFormat that delegates formatting to
     *  the specific inner formatter
     *
     *@param  val         The integer value to be formatted
     *@param  toAppendTo  Where the text is to be appended
     *@param  pos         On input: an alignment field, if desired. On output:
     *      the offsets of the alignment field
     *@return             The text that will be displayed
     */
    @Override
    public StringBuffer format(long val, StringBuffer toAppendTo, FieldPosition pos) {
      return format((double) val, toAppendTo, pos);
    }
  }


	
	
	
}

