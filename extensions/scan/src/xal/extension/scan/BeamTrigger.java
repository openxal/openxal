package xal.extension.scan;

import javax.swing.*;
import java.util.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.awt.Font;
import java.awt.Color;
import java.awt.BorderLayout;

import xal.ca.*;
import xal.extension.widgets.swing.*;
import xal.smf.TimingCenter;


public class BeamTrigger{

    //name of the beam trigger PV
    private String triggerNamePV = "ICS_Tim:Gate_BeamOn:SSTrigger";
    private Channel ch   = null;
   
    private JRadioButton useTriggerButton  = new JRadioButton(" Use Beam Trigger, Delay [sec]:");

    private DoubleInputTextField tDelayText = new DoubleInputTextField(5);

    private DecimalFormat tDelayFormat = new DecimalFormat("0.0#");

    public BeamTrigger(){
         
	tDelayText.setHorizontalAlignment(JTextField.CENTER);
        tDelayText.setNormalBackground(Color.white);
        tDelayText.setNumberFormat(tDelayFormat);
	tDelayText.setValue(0.2);
        
	//get channel from timing center
	final TimingCenter tmCenter = TimingCenter.getDefaultTimingCenter();
		// some sites may not have a default accelerator defined or the trigger channel
		if ( tmCenter != null ) {
			ch = tmCenter.findChannel( TimingCenter.TRIGGER_HANDLE );
			if ( ch != null ) {
				useTriggerButton.setSelected(true);

				useTriggerButton.addItemListener(new ItemListener(){
					public void itemStateChanged(ItemEvent e) {
						if (e.getStateChange() == ItemEvent.SELECTED) {
							setOnOff(true);
						}
						else{
							setOnOff(false);
						}
					}
				});
			}
			else {
				useTriggerButton.setSelected( false );
				useTriggerButton.setEnabled( false );
			}
		}
		else {
			useTriggerButton.setSelected( false );
			useTriggerButton.setEnabled( false );
		}
  
    }

    public void setOnOff(boolean onOff){
	tDelayText.setEditable(onOff);
        useTriggerButton.setSelected(onOff);
    }

    public void setFontForAll(Font fnt){
	tDelayText.setFont(fnt);
	useTriggerButton.setFont(fnt);
    }

    public JPanel getJPanel(){
	JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(useTriggerButton,BorderLayout.CENTER);
        panel.add(tDelayText,BorderLayout.EAST);
	return panel;
    }

    public boolean isOn(){
	return useTriggerButton.isSelected();
    }

    public void makePulse(){
	if(useTriggerButton.isSelected() && ch != null){
	    try {
		ch.putVal(1.0);
	    }
	    catch (ConnectionException e){
		setOnOff(false);
		return;
	    }
	    catch (PutException e){ 
		setOnOff(false);
		return;
	    }
	    try{Thread.sleep((long) (tDelayText.getValue()*1000.0));}
	    catch(InterruptedException e){}
	}
    }

    public String getChannelName(){
	return triggerNamePV ;
    }

    public Channel getChannel(){
	return ch;
    }

    public void setChannelName(String chanName){
	triggerNamePV = chanName;
	ch = ChannelFactory.defaultFactory().getChannel(triggerNamePV);
    }

    public void setChannel(Channel ch_In){
	ch = ch_In;
        triggerNamePV = ch.channelName();
    }


    public void setDelay(double timeDelay){
	tDelayText.setValue(timeDelay);
    }

    public double getDelay(){
	return tDelayText.getValue();
    }
}
