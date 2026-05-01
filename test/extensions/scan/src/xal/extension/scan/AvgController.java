package xal.extension.scan;

import javax.swing.*;
import java.text.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.util.*;

import xal.extension.widgets.swing.*;

public class AvgController{

    private IntegerInputTextField avgNumberText = new IntegerInputTextField(5);       
    private DoubleInputTextField timeDelayText  = new DoubleInputTextField(5);

    private JRadioButton  avgCntrButton   = new JRadioButton("Average for N read out with T delay");

    public JLabel avgNumberLabel         = new JLabel(" N= ",JLabel.CENTER);
    public JLabel timeDelayLabel         = new JLabel(" T delay [sec]= ",JLabel.CENTER);

    //controller panel
    private JPanel avgCntrPanel = new JPanel();

    //number of possible panels that look different
    private int nPanelLooks = 2;

    public DecimalFormat avgNumberFormat = new DecimalFormat("###");
    public DecimalFormat timeDelayFormat = new DecimalFormat("#0.0#");

    private volatile int avgNumber    = 1;
    private volatile double timeDelay = 0.2;

    private volatile boolean isOn = false;

    private Vector<ChangeListener> ChangeListenerV = new Vector<ChangeListener>();
    private ChangeEvent changeEvent = null;

    public AvgController(){
	this(5,0.2);
    }

    public AvgController(int avgNumberIn, double timeDelayIn){

	avgNumberText.setNormalBackground(Color.white);
	timeDelayText.setNormalBackground(Color.white);

	avgNumberText.setHorizontalAlignment(JTextField.CENTER);
	timeDelayText.setHorizontalAlignment(JTextField.CENTER);

	avgCntrButton.setSelected(false);
	avgNumberText.setEditable(false);
	timeDelayText.setEditable(false);

	avgNumberText.setDecimalFormat(avgNumberFormat);
	timeDelayText.setNumberFormat(timeDelayFormat);

	changeEvent = new ChangeEvent(this);

        timeDelayText.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e){
		    timeDelay = timeDelayText.getValue();
		    notifyChanges();
		}
	    });

        avgNumberText.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e){
		    avgNumber = avgNumberText.getValue();
		    notifyChanges();
		}		    
	    });

	avgCntrButton.addItemListener(new ItemListener(){
		public void itemStateChanged(ItemEvent e) {
		    if (e.getStateChange() == ItemEvent.SELECTED) {
			setOnOff(true);
			avgNumberText.setEditable(true);
			timeDelayText.setEditable(true);
		    }
		    else{
			setOnOff(false);
			avgNumberText.setEditable(false);
			timeDelayText.setEditable(false);
		    }
		}
	    });

        setAvgNumber(avgNumberIn);
        setTimeDelay(timeDelayIn);

	setFontForAll(new Font("Monospaced",Font.PLAIN,10));
    }


    public void setFontForAll(Font fnt){
	avgNumberText.setFont(fnt);
	timeDelayText.setFont(fnt);
	avgCntrButton.setFont(fnt);
	avgNumberLabel.setFont(fnt);
	timeDelayLabel.setFont(fnt);
	avgCntrPanel.setFont(fnt);
    }

    public void setAvgNumber(int avgNumberIn){
	    avgNumberText.setValue(avgNumberIn);
	    notifyChanges(); 
    }

    public void setTimeDelay(double timeDelayIn){
	    timeDelayText.setValue(timeDelayIn);
	    notifyChanges();
    }

    public int getAvgNumber(){
	if(isOn){
	    return avgNumber;
	}
	return 1;
    }

    public double getTimeDelay(){
	    if(isOn){
		return timeDelay;
	    }
	    return 0.0;	
    }

    public IntegerInputTextField getAvgNumberText(){
	return avgNumberText;
    }

    public DoubleInputTextField getTimeDelayText(){
	return timeDelayText;
    }

    public JLabel getAvgNumberLabel(){
	return avgNumberLabel;
    }

    public JLabel getTimeDelayLabel(){
	return timeDelayLabel;
    }

    public JRadioButton getAvgCntrButton(){
	return avgCntrButton;
    }

    public void setAvgNumberFormat(DecimalFormat avgNumberFormat){
	avgNumberText.setDecimalFormat(avgNumberFormat);
    }

    public void setTimeDelayFormat(DecimalFormat timeDelayFormat){
	timeDelayText.setNumberFormat(avgNumberFormat);
    }

    public void setOnOff(boolean isOnIn){
	isOn = isOnIn;
	avgCntrButton.setSelected(isOn);
	notifyChanges();
    }

    public boolean isOn(){
	return isOn;
    }

    public void addChangeListener(ChangeListener chgL){
	ChangeListenerV.add(chgL);
    }

    public void removeChangeListener(ChangeListener chgL){
	ChangeListenerV.remove(chgL);
    }

    public void removeAllChangeListeners(){
	ChangeListenerV.clear();
    }

    private void notifyChanges(){
	for(int i = 0, n = ChangeListenerV.size(); i < n; i++){
	    ChangeListenerV.get(i).stateChanged(changeEvent); 
	}
    }

    public JPanel getJPanel(){
	return getJPanel(0);
    }

    public JPanel getJPanel(int index){
	avgCntrPanel.removeAll();

	if(index == 0){
	    JPanel tmp_ButtonPanel = new JPanel();
	    tmp_ButtonPanel.setLayout(new FlowLayout(FlowLayout.LEFT,0,0));
	    tmp_ButtonPanel.add(avgCntrButton);
	    JPanel tmp_ParamPanel = new JPanel();
	    tmp_ParamPanel.setLayout(new FlowLayout(FlowLayout.CENTER,0,0));
	    tmp_ParamPanel.add(avgNumberLabel);
	    tmp_ParamPanel.add(avgNumberText);
	    tmp_ParamPanel.add(timeDelayLabel);
	    tmp_ParamPanel.add(timeDelayText);

	    JPanel tmp_Panel = new JPanel();
	    tmp_Panel.setLayout(new BorderLayout());
	    tmp_Panel.add(tmp_ButtonPanel,BorderLayout.NORTH);
	    tmp_Panel.add(tmp_ParamPanel,BorderLayout.SOUTH);

	    avgCntrPanel.setLayout(new BorderLayout());
	    avgCntrPanel.add(tmp_Panel,BorderLayout.NORTH);
	    avgCntrPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),null));
	    avgCntrPanel.setBackground(avgCntrPanel.getBackground().darker());
	}

	if(index == 1){
            JPanel tmp = new JPanel();
            tmp.setLayout(new FlowLayout(FlowLayout.LEFT,0,0));
            avgCntrButton.setText("Average for N read out, N= ");
            tmp.add(avgCntrButton);
            tmp.add(avgNumberText);

	    avgCntrPanel.setLayout(new BorderLayout());
	    avgCntrPanel.add(tmp,BorderLayout.NORTH);
	    avgCntrPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),null));
	    avgCntrPanel.setBackground(avgCntrPanel.getBackground().darker());
	}

        return avgCntrPanel;
    }

    //------------------------------------
    //MAIN for debugging
    //------------------------------------
    public static void main(String args[]) {
	JFrame mainFrame = new JFrame("Averaging Controller Class");

	mainFrame.addWindowListener(
	    new java.awt.event.WindowAdapter() {
		public void windowClosing(java.awt.event.WindowEvent evt) {
		    System.exit(0);
		}
	    }
	);

	mainFrame.getContentPane().setLayout(new BorderLayout());

	AvgController avgCnt =  new AvgController(5,0.5);
        JPanel avgPanel = avgCnt.getJPanel();
        avgPanel.setBackground(Color.getHSBColor(0.8f, 0.8f,0.8f));

	JPanel tmp_p = new JPanel();
        tmp_p.setLayout(new BorderLayout());
        tmp_p.add(avgPanel,BorderLayout.NORTH);

	mainFrame.getContentPane().add(tmp_p,BorderLayout.WEST);

	mainFrame.pack();
	mainFrame.setSize(new Dimension(300,430));
	mainFrame.setVisible(true);
    }
}
