package xal.extension.scan;

import javax.swing.*;
import java.awt.*;
import java.text.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.util.*;
import javax.swing.border.*;

import xal.extension.widgets.swing.*;
import xal.ca.*;
import xal.tools.apputils.*;

/**
 *  Description of the Class
 *
 *@author     shishlo
 *created    September 30, 2005
 */
public class ScanController1D {

  private String title = "Scan Controller";

  private JPanel controllerPanel = new JPanel();

  private Thread measurementThread = null;

  private ScanVariable scanVariable = null;

  private Vector<MeasuredValue> measuredValuesV = new Vector<MeasuredValue>();
  private Vector<MeasuredValue> validationValuesV = new Vector<MeasuredValue>();

  //time of sleeping between settings variables and measurements
  private double sleepTime = 0.2;

  private double scanValue = 0.0;
  private double scanValueRB = 0.0;

  private double scanValueMem = 0.0;

  private double lowLim = -180;
  private double uppLim = 180;
  private double step = 5.0;

  //averaging , time in second
  private AvgController avgController = null;
  private ChangeListener avgParamChangeListener = null;
  private volatile double avrgTime = 0.0;
  private volatile int nAveraging = 1;

  //validation information and limits
  private ValidationController validationController = null;
  private ChangeListener validationParamChangeListener = null;
  private volatile boolean validateMeasurement = false;
  private volatile double lowValidationLim = 0.0;
  private volatile double uppValidationLim = 100.0;
  private int maxNumberBadMeasurements = 10;

  //external button
  private JCheckBox restoreValueAfterScanButton = new JCheckBox("Restore Scan PV value after scan", true);

  //beam trigger
  BeamTrigger beamTrigger = new BeamTrigger();

  //public GUI components
  private JPanel phaseScanAndRB_Panel = new JPanel();
  private JRadioButton phaseScan_Button = new JRadioButton("Phase. ");
  private JLabel valueRB_Label = new JLabel(" This is a Read Back Value:  ");
  private JLabel scanStep_Label = new JLabel("SCAN with step:  ");

  private DoubleInputTextField lowLimText = new DoubleInputTextField(10);
  private DoubleInputTextField uppLimText = new DoubleInputTextField(10);
  private DoubleInputTextField stepText = new DoubleInputTextField(10);

  private JTextField valueText = new JTextField(10);
  private JTextField valueTextRB = new JTextField(10);

  private JLabel unitsLabel = new JLabel("dim");

  private DoubleInputTextField sleepTimeText = new DoubleInputTextField(6);
  private JLabel sleepTimeLabel = new JLabel("Time delay after settings [sec]: ", JLabel.CENTER);

  private JButton startButton = new JButton("START ");
  private JButton resumeButton = new JButton("RESUME");
  private JButton stopButton = new JButton(" STOP ");

  private JScrollBar scrollBar = new JScrollBar(Scrollbar.HORIZONTAL, 0, 0, 0, 1000);
  private boolean scrollBarLocked = false;

  //public FORMATs for GUI component
  private DecimalFormat valueFormat = new DecimalFormat("####.###");
  private DecimalFormat sleepTimeFormat = new DecimalFormat("##.##");

  //message text. It is not shown on the panel.
  //The purpose is to connect the document of
  //this text field with the message string of the main window.
  private JTextField messageText = new JTextField(40);

  //-----------------------------------------------------------------
  //actions and listeners for "new set of data" and "new data point"
  //-----------------------------------------------------------------
  private ActionEvent newSetOfDataAction = null;
  private ActionEvent newPointOfDataAction = null;
  private Vector<ActionListener> newSetOfDataListenersV = new Vector<ActionListener>();
  private Vector<ActionListener> newPointOfDataListenersV = new Vector<ActionListener>();

  //-----------------------------------------------------------------
  //actions and listeners for the START button
  //-----------------------------------------------------------------
  private ActionEvent startButtonAction = null;
  private ActionEvent stopButtonAction = null;
  private ActionEvent resumeButtonAction = null;
  private Vector<ActionListener> startListenersV = new Vector<ActionListener>();
  private Vector<ActionListener> stopListenersV = new Vector<ActionListener>();
  private Vector<ActionListener> resumeListenersV = new Vector<ActionListener>();

  //This is stop scan listener. It should be used to stop scan from anywhere.
  private ActionListener stopScanListener = null;

  //The "stop", "start" and "restart" buttons listeners
  private ActionListener startButtonListener = null;
  private ActionListener resumeButtonListener = null;
  private ActionListener stopButtonListener = null;

  //Internal set of values during the scan
  private double[] variableSet = new double[100];
  private int nPoints = 0;
  private int positionInd = 0;

  //continue mode yes or no
  private boolean continueMode = false;

  //scan state. This state can be changed from everywhere
  private volatile boolean scanOn = false;

  //state of buttons there are three possible combinations
  private static int START_BUTTONS_STATE = 0;
  private static int RESUME_BUTTONS_STATE = 1;
  private static int SCAN_BUTTONS_STATE = 2;

  private int CURRENT_BUTTONS_STATE = 0;

  //key defining if the memory value should be restored
  private boolean scanVarShouldBeRestored = true;
  private boolean scanVarShouldBeMemorized = true;

  //synchronizing lock
  private Object lockObj = new Object();


  /**
   *  Constructor for the ScanController1D object
   *
   *@param  title  Description of the Parameter
   */
  public ScanController1D(String title) {
    this.title = title;

    //Definition of GUI components
    lowLimText.setNormalBackground(Color.white);
    uppLimText.setNormalBackground(Color.white);
    stepText.setNormalBackground(Color.white);
    sleepTimeText.setNormalBackground(Color.white);
    valueText.setBackground(Color.getHSBColor(0.5f, 0.5f, 1.0f));
    valueTextRB.setBackground(Color.getHSBColor(0.0f, 0.0f, 0.9f));
    valueTextRB.setEditable(false);

    lowLimText.setNumberFormat(valueFormat);
    uppLimText.setNumberFormat(valueFormat);
    stepText.setNumberFormat(valueFormat);
    sleepTimeText.setNumberFormat(sleepTimeFormat);

    lowLimText.setHorizontalAlignment(JTextField.CENTER);
    uppLimText.setHorizontalAlignment(JTextField.CENTER);
    valueText.setHorizontalAlignment(JTextField.CENTER);
    valueTextRB.setHorizontalAlignment(JTextField.CENTER);
    stepText.setHorizontalAlignment(JTextField.CENTER);
    sleepTimeText.setHorizontalAlignment(JTextField.CENTER);

    valueText.addActionListener(
      new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (scanOn == false) {
            try {
              scanValue = Double.parseDouble(valueText.getText());
            } catch (NumberFormatException exc) {}
            setCurrentValue(scanValue);

            boolean containersCreated = false;

            for (int i = 0, n = measuredValuesV.size(); i < n; i++) {
              MeasuredValue mv_tmp = measuredValuesV.get(i);
              if (mv_tmp != null && mv_tmp.getNumberOfDataContainers() == 0) {
                mv_tmp.createNewDataContainer();
                containersCreated = true;
                if (scanVariable.getChannelRB() != null &&
                    mv_tmp.getNumberOfDataContainersRB() == 0) {
                  mv_tmp.createNewDataContainerRB();
                }
              }
            }

            if (containersCreated) {
              for (int i = 0, n = newSetOfDataListenersV.size(); i < n; i++) {
                newSetOfDataListenersV.get(i).actionPerformed(newSetOfDataAction);
              }
            }

            measure(scanValue);
          } else {
            Toolkit.getDefaultToolkit().beep();
            setCurrentValue(scanValue);
          }
        }
      });

    valueText.addMouseListener(
      new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
          if (e.getClickCount() == 2) {
            if (scanOn == false) {
              try {
                scanValue = Double.parseDouble(valueText.getText());
              } catch (NumberFormatException exc) {}
              setCurrentValue(scanValue);

              boolean containersCreated = false;

              for (int i = 0, n = measuredValuesV.size(); i < n; i++) {
                MeasuredValue mv_tmp = measuredValuesV.get(i);
                if (mv_tmp != null && mv_tmp.getNumberOfDataContainers() == 0) {
                  mv_tmp.createNewDataContainer();
                  containersCreated = true;
                  if (scanVariable.getChannelRB() != null &&
                      mv_tmp.getNumberOfDataContainersRB() == 0) {
                    mv_tmp.createNewDataContainerRB();
                  }
                }
              }

              if (containersCreated) {
                for (int i = 0, n = newSetOfDataListenersV.size(); i < n; i++) {
                  newSetOfDataListenersV.get(i).actionPerformed(newSetOfDataAction);
                }
              }

              measure(scanValue);
            } else {
              Toolkit.getDefaultToolkit().beep();
              setCurrentValue(scanValue);
            }
          }
        }
      });

    valueTextRB.addMouseListener(
      new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
          valueTextRB.setText(null);
          if (scanVariable != null && scanOn == false) {
            scanValueRB = scanVariable.getValueRB();
            if (scanVariable.getChannelRB() != null) {
              scanValueRB = scanVariable.getValueRB();
              valueTextRB.setText(valueFormat.format(scanValueRB));
            }
          }
        }
      });

    lowLimText.addActionListener(
      new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          lowLim = lowLimText.getValue();
          setSliderValue(scanValue);
          continueMode = false;
          setButtonsState(START_BUTTONS_STATE);
        }
      });

    uppLimText.addActionListener(
      new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          uppLim = uppLimText.getValue();
          setSliderValue(scanValue);
          continueMode = false;
          setButtonsState(START_BUTTONS_STATE);
        }
      });

    stepText.addActionListener(
      new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          step = stepText.getValue();
          continueMode = false;
          setButtonsState(START_BUTTONS_STATE);
        }
      });

    sleepTimeText.addActionListener(
      new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          sleepTime = sleepTimeText.getValue();
        }
      });

    scrollBar.setBlockIncrement((scrollBar.getMaximum() - scrollBar.getMinimum()) / 50);
    scrollBar.getModel().addChangeListener(
      new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
          if (!scrollBarLocked) {
            int i_val = scrollBar.getValue();
            double val = lowLim + i_val * (uppLim - lowLim) / (scrollBar.getMaximum() - scrollBar.getMinimum());
            valueText.setText(null);
            valueText.setText(valueFormat.format(val));
          }
        }
      });

    //set buttons actions
    setButtonsState(START_BUTTONS_STATE);

    startButton.setHorizontalTextPosition(SwingConstants.CENTER);
    stopButton.setHorizontalTextPosition(SwingConstants.CENTER);
    resumeButton.setHorizontalTextPosition(SwingConstants.CENTER);

    startButtonListener =
      new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (scanOn == true) {
            Toolkit.getDefaultToolkit().beep();
            return;
          }
          messageText.setText(null);
          continueMode = false;
          scanVarShouldBeRestored = true;

          if (CURRENT_BUTTONS_STATE == START_BUTTONS_STATE) {
            scanVarShouldBeMemorized = true;
          } else {
            scanVarShouldBeMemorized = false;
          }

          setButtonsState(SCAN_BUTTONS_STATE);
          for (int i = 0, n = startListenersV.size(); i < n; i++) {
            startListenersV.get(i).actionPerformed(startButtonAction);
          }
          measure();
        }
      };

    startButton.addActionListener(startButtonListener);

    resumeButtonListener =
      new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          scanVarShouldBeMemorized = false;
          if (CURRENT_BUTTONS_STATE == RESUME_BUTTONS_STATE) {
            if (scanOn == true) {
              Toolkit.getDefaultToolkit().beep();
              return;
            }
            messageText.setText(null);
            continueMode = true;
            scanVarShouldBeRestored = true;
            setButtonsState(SCAN_BUTTONS_STATE);
            for (int i = 0, n = resumeListenersV.size(); i < n; i++) {
              resumeListenersV.get(i).actionPerformed(resumeButtonAction);
            }
            measure();
          } else {
            if (scanOn == false) {
              Toolkit.getDefaultToolkit().beep();
              return;
            }
            scanOn = false;
            scanVarShouldBeRestored = false;
            if (measurementThread != null && measurementThread.isAlive()) {
              measurementThread.interrupt();
            }
          }
        }
      };

    resumeButton.addActionListener(resumeButtonListener);

    stopButtonListener =
      new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          scanVarShouldBeMemorized = false;
          if (scanOn == false) {
            Toolkit.getDefaultToolkit().beep();
            return;
          }
          scanOn = false;
          scanVarShouldBeRestored = true;
          if (measurementThread != null && measurementThread.isAlive()) {
            measurementThread.interrupt();
          }
        }
      };

    stopButton.addActionListener(stopButtonListener);

    //stop scan listener definition
    stopScanListener =
      new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          scanOn = false;
          if (measurementThread != null && measurementThread.isAlive()) {
            measurementThread.interrupt();
          }
        }
      };

    //-----------------------------------------------------------------
    //actions for "new set of data" and "new data point"
    //-----------------------------------------------------------------
    newSetOfDataAction = new ActionEvent(this, 0, "newSet");
    newPointOfDataAction = new ActionEvent(this, 0, "newPoint");
    startButtonAction = new ActionEvent(this, 0, "startButton");
    stopButtonAction = new ActionEvent(this, 0, "stopButton");
    resumeButtonAction = new ActionEvent(this, 0, "resumeButton");

    //definition of the initial values
    setCurrentValueRB(scanValue);
    setCurrentValue(scanValueRB);
    setLowLimit(lowLim);
    setUppLimit(uppLim);
    setStep(step);
    setSleepTime(sleepTime);

    //Tool tip setting
    phaseScan_Button.setToolTipText("Use this button to indicate a phase scan");

    //Layout and Border definition
    controllerPanel.setLayout(new BorderLayout());
    controllerPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), title));

    FlowLayout flwC = new FlowLayout(FlowLayout.LEFT, 1, 1);

    JPanel panel_1 = phaseScanAndRB_Panel;
    panel_1.setBorder(BorderFactory.createEmptyBorder());
    panel_1.setLayout(flwC);
    panel_1.add(valueRB_Label);
    panel_1.add(valueTextRB);

    JPanel panel_2 = new JPanel();
    panel_2.setBorder(BorderFactory.createEmptyBorder());
    panel_2.setLayout(new GridLayout(1, 3, 1, 1));
    panel_2.add(lowLimText);
    panel_2.add(valueText);
    panel_2.add(uppLimText);

    JPanel panel_3 = new JPanel();
    panel_3.setBorder(BorderFactory.createEmptyBorder());
    panel_3.setLayout(new BorderLayout());
    panel_3.add(scrollBar, BorderLayout.NORTH);

    JPanel panel_4 = new JPanel();
    panel_4.setBorder(BorderFactory.createEmptyBorder());
    panel_4.setLayout(flwC);
    panel_4.add(scanStep_Label);
    panel_4.add(stepText);
    panel_4.add(unitsLabel);

    JPanel panel_5 = new JPanel();
    panel_5.setBorder(BorderFactory.createEmptyBorder());
    panel_5.setLayout(flwC);
    panel_5.add(sleepTimeLabel);
    panel_5.add(sleepTimeText);

    JPanel panel_6 = new JPanel();
    panel_6.setBorder(BorderFactory.createEmptyBorder());
    panel_6.setLayout(new GridLayout(1, 3, 1, 1));
    panel_6.add(startButton);
    panel_6.add(resumeButton);
    panel_6.add(stopButton);

    JPanel panel_7 = new JPanel();
    panel_7.setBorder(BorderFactory.createEmptyBorder());
    panel_7.setLayout(flwC);
    panel_7.add(beamTrigger.getJPanel());

    JPanel inner_panel = new JPanel();
    inner_panel.setLayout(new VerticalLayout());

    inner_panel.add(panel_1);
    inner_panel.add(panel_2);
    inner_panel.add(panel_3);
    inner_panel.add(panel_4);
    inner_panel.add(panel_5);
    inner_panel.add(panel_6);
    inner_panel.add(panel_7);

    controllerPanel.add(inner_panel, BorderLayout.WEST);

    setFontForAll(new Font("Monospaced", Font.PLAIN, 10));
    controllerPanel.setBackground(controllerPanel.getBackground().darker());
  }


  /**
   *  Sets the restoreButton button of the ScanController1D object
   *
   *@param  restoreValueAfterScanButton  The JCheckBox button
   */
  public void setRestoreButton(JCheckBox restoreValueAfterScanButton) {
    this.restoreValueAfterScanButton = restoreValueAfterScanButton;
  }


  /**
   *  Sets the fontForAll attribute of the ScanController1D object
   *
   *@param  fnt  The new fontForAll value
   */
  public void setFontForAll(Font fnt) {
    phaseScan_Button.setFont(fnt);
    valueRB_Label.setFont(fnt);
    scanStep_Label.setFont(fnt);
    lowLimText.setFont(fnt);
    uppLimText.setFont(fnt);
    stepText.setFont(fnt);
    valueText.setFont(fnt);
    valueTextRB.setFont(fnt);
    sleepTimeText.setFont(fnt);
    sleepTimeLabel.setFont(fnt);
    unitsLabel.setFont(fnt);
    startButton.setFont(fnt);
    stopButton.setFont(fnt);
    resumeButton.setFont(fnt);
    sleepTimeLabel.setFont(fnt);
    TitledBorder border = (TitledBorder) controllerPanel.getBorder();
    border.setTitleFont(fnt);
    if (avgController != null) {
      avgController.setFontForAll(fnt);
    }
    if (validationController != null) {
      validationController.setFontForAll(fnt);
    }
    beamTrigger.setFontForAll(fnt);
  }


  /**
   *  Returns the title attribute of the ScanController1D object
   *
   *@return    The title value
   */
  public String getTitle() {
    return title;
  }


  /**
   *  Sets the title attribute of the ScanController1D object
   *
   *@param  title  The new title value
   */
  public void setTitle(String title) {
    this.title = title;
    controllerPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), title));
    controllerPanel.validate();
    controllerPanel.repaint();
  }


  /**
   *  Returns the jPanel attribute of the ScanController1D object
   *
   *@return    The jPanel value
   */
  public JPanel getJPanel() {
    return controllerPanel;
  }


  /**
   *  Returns the scanON attribute of the ScanController1D object
   *
   *@return    The scanON value
   */
  public boolean isScanON() {
    return scanOn;
  }


  /**
   *  Returns the continueON attribute of the ScanController1D object
   *
   *@return    The continueON value
   */
  public boolean isContinueON() {
    return continueMode;
  }


  /**
   *  Sets the scanVariable attribute of the ScanController1D object
   *
   *@param  scanVariable  The new scanVariable value
   */
  public void setScanVariable(ScanVariable scanVariable) {
    synchronized (lockObj) {
      if (this.scanVariable != null) {
        this.scanVariable.setMessageTextField(null);
        this.scanVariable.setStopScanListener(null);
        this.scanVariable.setLockObject(new Object());
      }
      this.scanVariable = scanVariable;
      if (scanVariable == null) {
        return;
      }
      scanVariable.setStopScanListener(stopScanListener);
      scanVariable.setMessageTextField(messageText);
      scanVariable.setLockObject(lockObj);
      setButtonsState(START_BUTTONS_STATE);
    }
  }


  /**
   *  Sets the avgController attribute of the ScanController1D object
   *
   *@param  avgController  The new avgController value
   */
  public void setAvgController(AvgController avgController) {
    if (this.avgController != null) {
      this.avgController.removeChangeListener(avgParamChangeListener);
      avrgTime = 0.0;
      nAveraging = 1;
    }
    this.avgController = avgController;
    if (avgController != null) {
      if (avgParamChangeListener == null) {
        avgParamChangeListener =
          new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
              AvgController avgCntr = (AvgController) changeEvent.getSource();
              avrgTime = avgCntr.getTimeDelay();
              nAveraging = avgCntr.getAvgNumber();
            }
          };
      }
      avgController.addChangeListener(avgParamChangeListener);
      avrgTime = avgController.getTimeDelay();
      nAveraging = avgController.getAvgNumber();
    }
  }


  /**
   *  Sets the validationController attribute of the ScanController1D object
   *
   *@param  validationController  The new validationController value
   */
  public void setValidationController(ValidationController validationController) {
    if (this.validationController != null) {
      this.validationController.removeChangeListener(validationParamChangeListener);
      validateMeasurement = false;
    }
    this.validationController = validationController;
    if (validationController != null) {
      if (validationParamChangeListener == null) {
        validationParamChangeListener =
          new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
              ValidationController validCntr = (ValidationController) changeEvent.getSource();
              validateMeasurement = validCntr.isOn();
              lowValidationLim = validCntr.getLowLim();
              uppValidationLim = validCntr.getUppLim();
            }
          };
      }
      validationController.addChangeListener(validationParamChangeListener);
      validateMeasurement = validationController.isOn();
      lowValidationLim = validationController.getLowLim();
      uppValidationLim = validationController.getUppLim();
    }
  }


  /**
   *  Sets the currentValue attribute of the ScanController1D object
   *
   *@param  scanValue  The new currentValue value
   */
  public void setCurrentValue(double scanValue) {
    valueText.setText(null);
    valueText.setText(valueFormat.format(scanValue));
    setSliderValue(scanValue);
    this.scanValue = scanValue;
  }


  /**
   *  Sets the currentValueRB attribute of the ScanController1D object
   *
   *@param  scanValueRB  The new currentValueRB value
   */
  public void setCurrentValueRB(double scanValueRB) {
    this.scanValueRB = scanValueRB;
    valueTextRB.setText(null);
    valueTextRB.setText(valueFormat.format(scanValueRB));
  }


  /**
   *  Sets the lowLimit attribute of the ScanController1D object
   *
   *@param  lowLim  The new lowLimit value
   */
  public void setLowLimit(double lowLim) {
    lowLimText.setValue(lowLim);
  }


  /**
   *  Sets the uppLimit attribute of the ScanController1D object
   *
   *@param  uppLim  The new uppLimit value
   */
  public void setUppLimit(double uppLim) {
    uppLimText.setValue(uppLim);
  }


  /**
   *  Sets the step attribute of the ScanController1D object
   *
   *@param  step  The new step value
   */
  public void setStep(double step) {
    stepText.setValue(step);
  }


  /**
   *  Returns the lowLimit attribute of the ScanController1D object
   *
   *@return    The lowLimit value
   */
  public double getLowLimit() {
    return lowLim;
  }


  /**
   *  Returns the uppLimit attribute of the ScanController1D object
   *
   *@return    The uppLimit value
   */
  public double getUppLimit() {
    return uppLim;
  }


  /**
   *  Returns the step attribute of the ScanController1D object
   *
   *@return    The step value
   */
  public double getStep() {
    return step;
  }


  /**
   *  Sets the sliderValue attribute of the ScanController1D object
   *
   *@param  val  The new sliderValue value
   */
  private void setSliderValue(double val) {
    int i_val = (scrollBar.getMaximum() + scrollBar.getMinimum()) / 2;
    if (lowLim < uppLim) {
      i_val = (int) (((val - lowLim) / (uppLim - lowLim)) * (scrollBar.getMaximum() - scrollBar.getMinimum()));
      if (i_val < scrollBar.getMinimum()) {
        i_val = scrollBar.getMinimum();
      }
      if (i_val > scrollBar.getMaximum()) {
        i_val = scrollBar.getMaximum();
      }
    }
    scrollBarLocked = true;
    scrollBar.setValue(i_val);
    scrollBarLocked = false;
  }


  /**
   *  Sets the sleepTime attribute of the ScanController1D object
   *
   *@param  sleepTimeIn  The new sleepTime value
   */
  public void setSleepTime(double sleepTimeIn) {
    sleepTimeText.setValue(sleepTimeIn);
  }


  /**
   *  Returns the sleepTime attribute of the ScanController1D object
   *
   *@return    The sleepTime value
   */
  public double getSleepTime() {
    return sleepTime;
  }


  /**
   *  Returns the scanVariable attribute of the ScanController1D object
   *
   *@return    The scanVariable value
   */
  public ScanVariable getScanVariable() {
    return scanVariable;
  }
	
  /**
   *  Returns the scanValue attribute of the ScanController1D object
   *
   *@return    The scanValue value
   */
	public double getScanValue(){
		return scanValue;
	}

  /**
   *  Adds a feature to the MeasuredValue attribute of the ScanController1D
   *  object
   *
   *@param  mv  The feature to be added to the MeasuredValue attribute
   */
  public void addMeasuredValue(MeasuredValue mv) {
    synchronized (lockObj) {
      if (mv != null) {
        measuredValuesV.add(mv);
      }
    }
  }


  /**
   *  Returns the measuredValuesV attribute of the ScanController1D object
   *
   *@return    The measuredValuesV value
   */
  public Vector<MeasuredValue> getMeasuredValuesV() {
    return measuredValuesV;
  }


  /**
   *  Description of the Method
   *
   *@param  mv  Description of the Parameter
   */
  public void removeMeasuredValue(MeasuredValue mv) {
    synchronized (lockObj) {
      measuredValuesV.remove(mv);
    }
  }


  /**
   *  Description of the Method
   */
  public void removeAllMeasuredValues() {
    synchronized (lockObj) {
      measuredValuesV.clear();
    }
  }


  /**
   *  Adds a feature to the ValidationValue attribute of the ScanController1D
   *  object
   *
   *@param  mv  The feature to be added to the ValidationValue attribute
   */
  public void addValidationValue(MeasuredValue mv) {
    synchronized (lockObj) {
      if (mv != null) {
        validationValuesV.add(mv);
      }
    }
  }


  /**
   *  Returns the validationValuesV attribute of the ScanController1D object
   *
   *@return    The validationValuesV value
   */
  public Vector<MeasuredValue> getValidationValuesV() {
    return validationValuesV;
  }


  /**
   *  Description of the Method
   *
   *@param  mv  Description of the Parameter
   */
  public void removeValidationValue(MeasuredValue mv) {
    synchronized (lockObj) {
      validationValuesV.remove(mv);
    }
  }


  /**
   *  Description of the Method
   */
  public void removeAllValidationValues() {
    synchronized (lockObj) {
      validationValuesV.clear();
    }
  }


  /**
   *  Adds a feature to the NewSetOfDataListener attribute of the
   *  ScanController1D object
   *
   *@param  newSetListener  The feature to be added to the NewSetOfDataListener
   *      attribute
   */
  public void addNewSetOfDataListener(ActionListener newSetListener) {
    if (newSetListener == null) {
      return;
    }
    newSetOfDataListenersV.add(newSetListener);
  }


  /**
   *  Adds a feature to the NewPointOfDataListener attribute of the
   *  ScanController1D object
   *
   *@param  newPointListener  The feature to be added to the
   *      NewPointOfDataListener attribute
   */
  public void addNewPointOfDataListener(ActionListener newPointListener) {
    if (newPointListener == null) {
      return;
    }
    newPointOfDataListenersV.add(newPointListener);
  }


  /**
   *  Description of the Method
   */
  public void removeAllNewSetOfDataListeners() {
    newSetOfDataListenersV.clear();
  }


  /**
   *  Description of the Method
   */
  public void removeAllNewPointOfDataListeners() {
    newPointOfDataListenersV.clear();
  }


  /**
   *  Adds a new listener to the start listeners.
   *
   *@param  newStartListener  New listener
   */
  public void addStartListener(ActionListener newStartListener) {
    startListenersV.add(newStartListener);
  }


  /**
   *  Removes the listener from the start listeners.
   *
   *@param  startListener  The listener
   */
  public void removeStartListener(ActionListener startListener) {
    startListenersV.remove(startListener);
  }


  /**
   *  Removes all start listeners.
   */
  public void removeAllStartListeners() {
    startListenersV.clear();
  }


  /**
   *  Adds a new listener to the stop listeners.
   *
   *@param  newStopListener  New listener
   */
  public void addStopListener(ActionListener newStopListener) {
    stopListenersV.add(newStopListener);
  }


  /**
   *  Removes the listener from the stop listeners.
   *
   *@param  stopListener  The listener
   */
  public void removeStopListener(ActionListener stopListener) {
    stopListenersV.remove(stopListener);
  }


  /**
   *  Removes all stop listeners.
   */
  public void removeAllStopListeners() {
    stopListenersV.clear();
  }


  /**
   *  Adds a new listener to the resume listeners.
   *
   *@param  newResumeListener  New listener
   */
  public void addResumeListener(ActionListener newResumeListener) {
    resumeListenersV.add(newResumeListener);
  }


  /**
   *  Removes the listener from the resume listeners.
   *
   *@param  resumeListener  The listener
   */
  public void removeResumeListener(ActionListener resumeListener) {
    resumeListenersV.remove(resumeListener);
  }


  /**
   *  Removes all resume listeners.
   */
  public void removeAllResumeListeners() {
    resumeListenersV.clear();
  }


  /**
   *  Starts scan
   *
   *@return    true if it is possible
   */
  public boolean startScan() {
    if (CURRENT_BUTTONS_STATE != START_BUTTONS_STATE) {
      return false;
    }
    startButtonListener.actionPerformed(startButtonAction);
    return true;
  }


  /**
   *  Resumes scan
   *
   *@return    true if it is possible
   */
  public boolean resumeScan() {
    if (CURRENT_BUTTONS_STATE != RESUME_BUTTONS_STATE) {
      return false;
    }
    resumeButtonListener.actionPerformed(resumeButtonAction);
    return true;
  }


  /**
   *  Stops scan
   *
   *@return    true if it is possible
   */
  public boolean stopScan() {
    if (CURRENT_BUTTONS_STATE != SCAN_BUTTONS_STATE) {
      return false;
    }
    stopButtonListener.actionPerformed(stopButtonAction);
    return true;
  }


  /**
   *  Sets the beamTriggerState attribute of the ScanController1D object
   *
   *@param  triggerOn  The new beamTriggerState value
   */
  public void setBeamTriggerState(boolean triggerOn) {
    beamTrigger.setOnOff(triggerOn);
  }


  /**
   *  Sets the beamTriggerDelay attribute of the ScanController1D object
   *
   *@param  triggerDelay  The new beamTriggerDelay value
   */
  public void setBeamTriggerDelay(double triggerDelay) {
    beamTrigger.setDelay(triggerDelay);
  }


  /**
   *  Returns the beamTriggerState attribute of the ScanController1D object
   *
   *@return    The beamTriggerState value
   */
  public boolean getBeamTriggerState() {
    return beamTrigger.isOn();
  }


  /**
   *  Returns the beamTriggerDelay attribute of the ScanController1D object
   *
   *@return    The beamTriggerDelay value
   */
  public double getBeamTriggerDelay() {
    return beamTrigger.getDelay();
  }


  /**
   *  Sets the beamTriggerChannel attribute of the ScanController1D object
   *
   *@param  triggerCh  The new beamTriggerChannel value
   */
  public void setBeamTriggerChannel(Channel triggerCh) {
    beamTrigger.setChannel(triggerCh);
  }


  /**
   *  Sets the beamTriggerChannelName attribute of the ScanController1D object
   *
   *@param  triggerChName  The new beamTriggerChannelName value
   */
  public void setBeamTriggerChannelName(String triggerChName) {
    beamTrigger.setChannelName(triggerChName);
  }


  /**
   *  Returns the beamTriggerChannel attribute of the ScanController1D object
   *
   *@return    The beamTriggerChannel value
   */
  public Channel getBeamTriggerChannel() {
    return beamTrigger.getChannel();
  }


  /**
   *  Returns the beamTriggerChannelName attribute of the ScanController1D
   *  object
   *
   *@return    The beamTriggerChannelName value
   */
  public String getBeamTriggerChannelName() {
    return beamTrigger.getChannelName();
  }


  /**
   *  Sets the variableSet attribute of the ScanController1D object
   */
  private void setVariableSet() {
    positionInd = 0;
    nPoints = 1;
    if (step != 0.) {
      nPoints = (int) ((uppLim - lowLim) / step);
      if (lowLim + nPoints * step < uppLim) {
        nPoints++;
      }
      nPoints++;
    }

    if (variableSet.length < nPoints) {
      variableSet = new double[nPoints];
    }

    for (int i = 0; i < nPoints; i++) {
      variableSet[i] = lowLim + i * step;
    }
    if (variableSet[nPoints - 1] > uppLim) {
      variableSet[nPoints - 1] = uppLim;
    }
  }


  /**
   *  Sets the buttonsState attribute of the ScanController1D object
   *
   *@param  BUTTONS_STATE  The new buttonsState value
   */
  private void setButtonsState(int BUTTONS_STATE) {

    int OLD_BUTTONS_STATE = CURRENT_BUTTONS_STATE;
    CURRENT_BUTTONS_STATE = BUTTONS_STATE;

    if (CURRENT_BUTTONS_STATE == START_BUTTONS_STATE) {
      startButton.setEnabled(true);
      resumeButton.setEnabled(false);
      stopButton.setEnabled(false);

      resumeButton.setText("PAUSE");
    } else {
      if (CURRENT_BUTTONS_STATE == RESUME_BUTTONS_STATE) {
        startButton.setEnabled(true);
        resumeButton.setEnabled(true);
        stopButton.setEnabled(false);
        resumeButton.setText("RESUME");
      } else {
        //SCAN_BUTTONS_STATE
        startButton.setEnabled(false);
        resumeButton.setEnabled(true);
        stopButton.setEnabled(true);
        resumeButton.setText("PAUSE");
      }
    }

    boolean startB = startButton.isEnabled();
    boolean resumeB = resumeButton.isEnabled();
    boolean stopB = stopButton.isEnabled();

    if (startB) {
      startButton.setBackground(Color.red);
    } else {
      startButton.setBackground(Color.lightGray);
    }

    if (resumeB) {
      resumeButton.setBackground(Color.red);
    } else {
      resumeButton.setBackground(Color.lightGray);
    }

    if (stopB) {
      stopButton.setBackground(Color.red);
      lowLimText.setEditable(false);
      uppLimText.setEditable(false);
      stepText.setEditable(false);
      sleepTimeText.setEditable(false);
    } else {
      stopButton.setBackground(Color.lightGray);
      lowLimText.setEditable(true);
      uppLimText.setEditable(true);
      stepText.setEditable(true);
      sleepTimeText.setEditable(true);
    }
  }


  /**
   *  Description of the Method
   */
  public void measure() {
    Runnable runMeasure =
      new Runnable() {
        public void run() {
          synchronized (lockObj) {
            scanOn = true;
            measurementThread = Thread.currentThread();

            if (scanVarShouldBeMemorized == true) {
              if (scanVariable != null &&
                  scanVariable.getMonitoredPV().isGood()) {
                scanVariable.memorizeValue();
              }
              scanValueMem = scanValue;
            }

            trueMeasure();

            if (scanVarShouldBeRestored == true &&
                scanVariable != null &&
                scanVariable.getMonitoredPV().isGood()) {
              if (restoreValueAfterScanButton.isSelected() == true) {
                scanVariable.restoreFromMemory();
              }
            }

            if (scanVariable != null && scanVariable.getMonitoredPV().isGood()) {
              if (scanOn != false && sleepTime > 0.) {
                //try{Thread.sleep((long)(1000.0*sleepTime));}catch(InterruptedException e){}
                try {
                  lockObj.wait((long) (1000.0 * sleepTime));
                } catch (InterruptedException e) {}
              }
              if (scanVariable.getChannel() != null) {
                Thread localUpDateThread = new Thread(
                  new Runnable() {
                    public void run() {
                      try {
                        Thread.sleep(1000);
                      } catch (InterruptedException e) {}
                      setCurrentValue(scanVariable.getValue());
                      if (scanVariable.getChannelRB() != null) {
                        setCurrentValueRB(scanVariable.getValueRB());
                      } else {
                        valueTextRB.setText(null);
                      }

                    }
                  });
                localUpDateThread.start();
              } else {
                valueTextRB.setText(null);
                if (scanVarShouldBeRestored == true) {
                  setCurrentValue(scanValueMem);
                } else {
                  setCurrentValue(scanValue);
                }
              }
            } else {
              valueTextRB.setText(null);
              if (scanVarShouldBeRestored == true) {
                setCurrentValue(scanValueMem);
              } else {
                setCurrentValue(scanValue);
              }
            }

            if (continueMode) {
              setButtonsState(RESUME_BUTTONS_STATE);
            } else {
              setButtonsState(START_BUTTONS_STATE);
            }
            scanOn = false;
            for (int i = 0, n = stopListenersV.size(); i < n; i++) {
              stopListenersV.get(i).actionPerformed(stopButtonAction);
            }
          }
        }
      };
    Thread mThread = new Thread(runMeasure);
    mThread.start();
  }


  /**
   *  Description of the Method
   *
   *@param  val  Description of the Parameter
   */
  private void measure(final double val) {
    Runnable runMeasure =
      new Runnable() {
        public void run() {
          synchronized (lockObj) {

            scanOn = true;
            measurementThread = Thread.currentThread();

            scanValueMem = scanValue;

            if (scanVariable != null && scanVariable.getMonitoredPV().isGood()) {
              scanVariable.memorizeValue();
            }

            trueMeasure(val);

            if (scanVariable != null && scanVariable.getMonitoredPV().isGood()) {
              if (restoreValueAfterScanButton.isSelected() == true) {
                scanVariable.restoreFromMemory();
              }
            }

            if (scanVariable != null && scanVariable.getMonitoredPV().isGood()) {
              //try{Thread.sleep((long)(1000.0*sleepTime));}catch(InterruptedException e){}
              try {
                lockObj.wait((long) (1000.0 * sleepTime));
              } catch (InterruptedException e) {}
              if (scanVariable.getChannel() != null) {
                setCurrentValue(scanVariable.getValue());
                if (scanVariable.getChannelRB() != null) {
                  setCurrentValueRB(scanVariable.getValueRB());
                } else {
                  valueTextRB.setText(null);
                  scanValueRB = scanValue;
                }
              } else {
                valueTextRB.setText(null);
                setCurrentValue(scanValueMem);
                scanValueRB = scanValue;
              }
            } else {
              valueTextRB.setText(null);
              setCurrentValue(scanValueMem);
              scanValueRB = scanValue;
            }
            scanOn = false;
          }
        }
      };
    Thread mThread = new Thread(runMeasure);
    mThread.start();
  }


  /**
   *  Description of the Method
   *
   *@param  val  Description of the Parameter
   *@return      Description of the Return Value
   */
	private boolean trueMeasure(double val) {
		scanValue = val;
		if (scanVariable != null) {
			scanVariable.setValue(phaseWrappingFunction(scanValue));
		}
		if (!scanOn) {
			return false;
		}
		if (sleepTime > 0.) {
			//try{Thread.sleep((long)(1000.0*sleepTime));}catch(InterruptedException e){}
			try {
				lockObj.wait((long) (1000.0 * sleepTime));
			} catch (InterruptedException e) {}
		}
		for (int k = 0, n = measuredValuesV.size(); k < n; k++) {
			if (!scanOn) {
				return false;
			}
			measuredValuesV.get(k).restoreIniState();
		}
		if (!scanOn) {
			return false;
		}
		int j = 0;
		int badCount = 0;
		while (j < nAveraging) {
			badCount = 0;
			while (true) {
				//start beam trigger
				beamTrigger.makePulse();
				if (validateMeasurements()) {
					for (int k = 0, n = measuredValuesV.size(); k < n; k++) {
						measuredValuesV.get(k).measure();
						if (!scanOn) {
							return false;
						}
					}
					j++;
					break;
				}
				if (!scanOn) {
					return false;
				}
				if (nAveraging > 1 && badCount > 0 && avrgTime > 0.) {
					//try{Thread.sleep((long)(1000.0*avrgTime));}catch(InterruptedException e){}
					try {
						lockObj.wait((long) (1000.0 * avrgTime));
					} catch (InterruptedException e) {}
				}
				if (nAveraging == 1 && badCount > 0) {
					try {
						lockObj.wait((long) (1000.0 * Math.max(sleepTime, avrgTime)));
					} catch (InterruptedException e) {}
				}
				badCount++;
				if (badCount > maxNumberBadMeasurements) {
					scanOn = false;
					messageText.setText("Cannot validate measurements.");
				}
				if (!scanOn) {
					return false;
				}
			}

			if (!scanOn) {
				return false;
			}
			if (nAveraging > 1 && j != nAveraging && avrgTime > 0.) {
				//try{Thread.sleep((long)(1000.0*avrgTime));}catch(InterruptedException e){}
				try {
					lockObj.wait((long) (1000.0 * Math.max(sleepTime, avrgTime)));
				} catch (InterruptedException e) {}
			}
			if (!scanOn) {
				return false;
			}
		}

		setCurrentValue(scanValue);
		if (scanVariable != null && scanVariable.getChannelRB() != null
			&& scanVariable.getMonitoredPV_RB().isGood()) {
			setCurrentValueRB(scanVariable.getValueRB());
		} else {
			scanValueRB = scanValue;
			valueTextRB.setText(null);
		}

		accountNewDataPoint();
		return true;
	}


  /**
   *  Description of the Method
   */
  private void trueMeasure() {
      if (continueMode == false) {
          setVariableSet();
          startNewSetOfData();
      }

      // add metadata to the graph
      updateGraphMetadata();

      int newPositionInd = positionInd;
      for (int i = positionInd; i < nPoints; i++) {
          if (!trueMeasure(variableSet[i])) {
              break;
          }
          newPositionInd = i + 1;
      }
      positionInd = newPositionInd;
      if (positionInd == 0) {
          continueMode = false;
          return;
      }
      if (positionInd == nPoints) {
          positionInd = 0;
          continueMode = false;
          return;
      }
      continueMode = true;
  }
    
    /** update the graph meta data */
    private void updateGraphMetadata() {
        if ( scanVariable != null ) {
            final String xScanLabel = scanVariable != null ? scanVariable.getChannelName() : "";
            final String xScanReadbackLabel = scanVariable != null ? scanVariable.getChannelNameRB() : "";
            
            for (int k = 0, n = measuredValuesV.size(); k < n; k++) {
                measuredValuesV.get(k).setLabels( xScanLabel, xScanReadbackLabel );
            }
        }
    }


  /**
   *  Description of the Method
   *
   *@return    Description of the Return Value
   */
  private boolean validateMeasurements() {
    if (validateMeasurement && validationController != null) {
      double validVal = 0.;
      for (int i = 0, n = validationValuesV.size(); i < n; i++) {
        MeasuredValue mv = validationValuesV.get(i);
        validVal = mv.getValue();
        if (validVal < lowValidationLim || validVal > uppValidationLim) {
          return false;
        }
      }
    }
    return true;
  }


  /**
   *  Description of the Method
   */
  private void startNewSetOfData() {
    for (int i = 0, n = measuredValuesV.size(); i < n; i++) {
      measuredValuesV.get(i).createNewDataContainer();
    }
    if (scanVariable != null && scanVariable.getChannelRB() != null) {
      for (int i = 0, n = measuredValuesV.size(); i < n; i++) {
        measuredValuesV.get(i).createNewDataContainerRB();
      }
    }
    for (int i = 0, n = newSetOfDataListenersV.size(); i < n; i++) {
      newSetOfDataListenersV.get(i).actionPerformed(newSetOfDataAction);
    }
  }


  /**
   *  Description of the Method
   */
  private void accountNewDataPoint() {
    for (int i = 0, n = measuredValuesV.size(); i < n; i++) {
      measuredValuesV.get(i).consumeData(phaseWrappingFunction(scanValue));
    }
    if (scanVariable != null && scanVariable.getChannelRB() != null) {
      for (int i = 0, n = measuredValuesV.size(); i < n; i++) {
        measuredValuesV.get(i).consumeDataRB(scanValueRB);
      }
    }
    for (int i = 0, n = newPointOfDataListenersV.size(); i < n; i++) {
      newPointOfDataListenersV.get(i).actionPerformed(newPointOfDataAction);
    }
  }


  //-------------------------------------------
  //Access to GUI elements methods
  //-------------------------------------------
  /**
   *  Shows or removes the phase scan button on the pannel
   *
   *@param  vis  The new phaseScanButtonVisible value
   */
  public void setPhaseScanButtonVisible(boolean vis) {
    Component[] cmpArr = phaseScanAndRB_Panel.getComponents();
    phaseScanAndRB_Panel.removeAll();
    if (vis) {
      phaseScanAndRB_Panel.add(phaseScan_Button);
    }
    if (cmpArr != null) {
      for (int i = 0; i < cmpArr.length; i++) {
        if (((Component) phaseScan_Button) != cmpArr[i]) {
          phaseScanAndRB_Panel.add(cmpArr[i]);
        }
      }
    }

    Container contRoot = null;
    Container contIni = controllerPanel.getParent();
    while (contIni != null) {
      contRoot = contIni;
      contIni = contIni.getParent();
    }

    if (contRoot != null) {
      contRoot.validate();
      contRoot.repaint();
    }
  }


  /**
   *  Returns true if the phase scan button is visible and false otherwise
   *
   *@return    The boolean True if the phase scan button is visible
   */
  public boolean getPhaseScanButtonVizible() {
    if (phaseScanAndRB_Panel.getComponentCount() == 2) {
      return false;
    }
    return true;
  }


  /**
   *  Returns the phase scan button state on or off
   *
   *@return    The phase scan button state on(true) off(false)
   */
  public boolean getPhaseScanButtonOn() {
    return phaseScan_Button.isSelected();
  }


  /**
   *  Sets the state of the phase scan button
   *
   *@param  onOff  The new phase scan button value
   */
  public void setPhaseScanButtonOn(boolean onOff) {
    phaseScan_Button.setSelected(onOff);
  }


  /**
   *  Wrapps the phase if the phase button is on
   *
   *@param  inValue  Input scan value
   *@return          Wrapped value ( if prescribed )
   */
  private double phaseWrappingFunction(double inValue) {
    double outValue = inValue;
    if (phaseScan_Button.isSelected()) {
      if (Math.abs(inValue) > 180.) {
        outValue += 180.;
        while (outValue < 0.) {
          outValue += 360.;
        }
        outValue = outValue % 360.;
        outValue -= 180.;
      }
    }
    return outValue;
  }


  /**
   *  Returns the valueRB_Label attribute of the ScanController1D object
   *
   *@return    The valueRB_Label value
   */
  public JLabel getValueRB_Label() {
    return valueRB_Label;
  }


  /**
   *  Returns the scanStep_Label attribute of the ScanController1D object
   *
   *@return    The scanStep_Label value
   */
  public JLabel getScanStep_Label() {
    return scanStep_Label;
  }


  /**
   *  Returns the unitsLabel attribute of the ScanController1D object
   *
   *@return    The unitsLabel value
   */
  public JLabel getUnitsLabel() {
    return unitsLabel;
  }


  /**
   *  Returns the valueText attribute of the ScanController1D object
   *
   *@return    The valueText value
   */
  public JTextField getValueText() {
    return valueText;
  }


  /**
   *  Returns the valueTextRB attribute of the ScanController1D object
   *
   *@return    The valueTextRB value
   */
  public JTextField getValueTextRB() {
    return valueTextRB;
  }


  /**
   *  Returns the messageText attribute of the ScanController1D object
   *
   *@return    The messageText value
   */
  public JTextField getMessageText() {
    return messageText;
  }


  //------------------------------------
  //MAIN for debugging
  //------------------------------------
  /**
   *  Description of the Method
   *
   *@param  args  Description of the Parameter
   */
  public static void main(String args[]) {
    JFrame mainFrame = new JFrame("Test of the IndependentValueRange class");
    mainFrame.addWindowListener(
      new java.awt.event.WindowAdapter() {
        public void windowClosing(java.awt.event.WindowEvent evt) {
          System.exit(0);
        }
      }
        );

    mainFrame.getContentPane().setLayout(new BorderLayout());

    JPanel tmp_p = new JPanel();
    tmp_p.setLayout(new BorderLayout());
    mainFrame.getContentPane().add(tmp_p, BorderLayout.WEST);

    ScanController1D iRange = new ScanController1D("SCAN CONTROL PANEL");
    iRange.getUnitsLabel().setText(" kV ");
    iRange.setLowLimit(-10.0);
    iRange.setUppLimit(10.0);
    iRange.setStep(1.0);
    iRange.setSleepTime(2.0);
    iRange.setBeamTriggerState(false);

    tmp_p.add(iRange.getJPanel(), BorderLayout.NORTH);

    mainFrame.pack();
    mainFrame.setSize(new Dimension(300, 430));
    mainFrame.setVisible(true);

    //add listeners
    iRange.addStartListener(
      new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          ScanController1D scc = (ScanController1D) e.getSource();
          System.out.println("Start Listener is scanOn = " +
              scc.isScanON() +
              "    Is it continue=" +
              scc.isContinueON());
        }
      });

    iRange.addStopListener(
      new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          ScanController1D scc = (ScanController1D) e.getSource();
          System.out.println("Stop Listener is scanOn = " +
              scc.isScanON() +
              "    Is it continue=" +
              scc.isContinueON());
        }
      });

    iRange.addResumeListener(
      new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          ScanController1D scc = (ScanController1D) e.getSource();
          System.out.println("Resume Listener is scanOn = " +
              scc.isScanON() +
              "    Is it continue=" +
              scc.isContinueON());
        }
      });

    //start scan
    try {
      Thread.sleep(2000);
    } catch (InterruptedException exc) {}

    iRange.startScan();

    //stop scan temporary
    try {
      Thread.sleep(10000);
    } catch (InterruptedException exc) {}

    iRange.stopScan();

    //resume scan
    try {
      Thread.sleep(5000);
    } catch (InterruptedException exc) {}

    iRange.resumeScan();

  }
}

