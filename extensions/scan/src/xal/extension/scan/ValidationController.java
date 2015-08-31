package xal.extension.scan;

import xal.ca.*;
import xal.extension.widgets.swing.*;

import java.text.*;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.util.*;

public class ValidationController{

    private DoubleInputTextField  lowLimText = new DoubleInputTextField (6);       
    private DoubleInputTextField  uppLimText = new DoubleInputTextField (6); 

    private DecimalFormat limFormat = new DecimalFormat("###.###");

    private JRadioButton validationButton  = new JRadioButton("Validation:");
    private JLabel lowLimLabel = new JLabel(" low=",JLabel.CENTER);
    private JLabel uppLimLabel = new JLabel(" upp=",JLabel.CENTER);

    private JPanel validatorLimitsPanel = new JPanel();

    private double lowLim = 0.;
    private double uppLim = 100.;

    private boolean isOn = false;

    private Vector<ChangeListener> changeListenerV = new Vector<ChangeListener>();
    private ChangeEvent changeEvent = null;

    public ValidationController(){
	init(lowLim,uppLim);
    }

    public ValidationController(double lowLimIn, double uppLimIn){
	init(lowLimIn,uppLimIn);
    }

    public void init(double lowLimIn, double uppLimIn){
        lowLim = lowLimIn;
        uppLim = uppLimIn;
	lowLimText.setHorizontalAlignment(JTextField.CENTER);
	uppLimText.setHorizontalAlignment(JTextField.CENTER);
        lowLimText.setNormalBackground(Color.white);
        uppLimText.setNormalBackground(Color.white);

        lowLimText.setNumberFormat(limFormat);
        uppLimText.setNumberFormat(limFormat);
 
	lowLimText.setEditable(false);
	uppLimText.setEditable(false);
	setLowLim(lowLim);
	setUppLim(uppLim);

	validationButton.setSelected(false);

	changeEvent = new ChangeEvent(this);

	lowLimText.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e){
		    lowLim = lowLimText.getValue(); 
		    notifyChanges();   		    
		}
	    });

        uppLimText.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e){
		    uppLim = uppLimText.getValue(); 
		    notifyChanges();    
		}
	    });

	validationButton.addItemListener(new ItemListener(){
		public void itemStateChanged(ItemEvent e) {
		    if (e.getStateChange() == ItemEvent.SELECTED) {
			setOnOff(true);
			lowLimText.setEditable(true);
			uppLimText.setEditable(true);
		    }
		    else{
			setOnOff(false);
			lowLimText.setEditable(false);
			uppLimText.setEditable(false);
		    }
		}
	    });

	setFontForAll(new Font("Monospaced",Font.PLAIN,10));
    }

    public void setFontForAll(Font fnt){
	lowLimText.setFont(fnt);
	uppLimText.setFont(fnt);
	validationButton.setFont(fnt);
	lowLimLabel.setFont(fnt);
	uppLimLabel.setFont(fnt);
    }

    public JPanel getJPanel(){
	return getJPanel(0);
    }

    public JPanel getJPanel(int index){
	validatorLimitsPanel.removeAll();

	if(index == 0) {
	    JPanel tmp = new JPanel();
	    tmp.setLayout(new FlowLayout(FlowLayout.LEFT,0,0));
	    tmp.add(validationButton);
	    tmp.add(lowLimLabel);
	    tmp.add(lowLimText);
	    tmp.add(uppLimLabel);
	    tmp.add(uppLimText);

	    JPanel tmp_1 = new JPanel();
            tmp_1.setLayout(new BorderLayout());
            tmp_1.add(tmp,BorderLayout.NORTH);

	    validatorLimitsPanel.setLayout(new BorderLayout());
	    validatorLimitsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),null));
	    validatorLimitsPanel.setBackground(validatorLimitsPanel.getBackground().darker());
	    validatorLimitsPanel.add(tmp_1,BorderLayout.NORTH);

	    notifyChanges();
	}

	if(index == 1) {
	    JPanel tmp = new JPanel();
	    tmp.setLayout(new FlowLayout(FlowLayout.LEFT,0,0));
	    validationButton.setText("Threshold for re-measuring = ");
	    tmp.add(validationButton);
	    tmp.add(lowLimText);

	    JPanel tmp_1 = new JPanel();
            tmp_1.setLayout(new BorderLayout());
            tmp_1.add(tmp,BorderLayout.NORTH);

	    validatorLimitsPanel.setLayout(new BorderLayout());
	    validatorLimitsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),null)); 
	    validatorLimitsPanel.setBackground(validatorLimitsPanel.getBackground().darker());
	    validatorLimitsPanel.add(tmp_1,BorderLayout.NORTH);

	    uppLim = Double.MAX_VALUE;
	    notifyChanges();
	}

	return validatorLimitsPanel;
    }

    public void setLowLim(double lowLimIn){
	lowLimText.setValue(lowLimIn);
	notifyChanges();
    }

    public void setUppLim(double uppLimIn){
	uppLimText.setValue(uppLimIn);
	notifyChanges();        
    }

    public double getLowLim(){
	if(isOn){
	  return lowLim;
	}
	return (- Double.MAX_VALUE);
    }

    public double getUppLim(){ 
	if(isOn){
	    return uppLim;
	}
	return Double.MAX_VALUE;
    }

    public double getInnerLowLim(){
       return lowLim;
    }

    public double getInnerUppLim(){
       return uppLim;
    }

    public boolean isOn(){
	return isOn;
    }

    public void setOnOff(boolean isOnIn){
	isOn = isOnIn;
	validationButton.setSelected(isOn);
	notifyChanges();
    }

    public void addChangeListener(ChangeListener chgL){
	changeListenerV.add(chgL);
    }

    public void removeChangeListener(ChangeListener chgL){
	changeListenerV.remove(chgL);
    }

    public void removeAllChangeListeners(){
	changeListenerV.clear();
    }

    private void notifyChanges(){
	for(int i = 0, n = changeListenerV.size(); i < n; i++){
	    changeListenerV.get(i).stateChanged(changeEvent);
	}
    }

    //------------------------------------
    //MAIN for debugging
    //------------------------------------
    public static void main(String args[]) {
	JFrame mainFrame = new JFrame("Valuator Limits Manager Class");
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
	mainFrame.getContentPane().add(tmp_p,BorderLayout.WEST);

	ValidationController vm =  new ValidationController(0.0,20.0);
	JPanel vmPanel = vm.getJPanel();
        vmPanel.setBackground(Color.getHSBColor(0.9f, 0.9f,0.9f));
	tmp_p.add(vmPanel,BorderLayout.NORTH);

	mainFrame.pack();
	mainFrame.setSize(new Dimension(300,430));
	mainFrame.setVisible(true);

    }


}
