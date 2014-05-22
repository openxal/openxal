package xal.app.injdumpwizard.utils;


import java.text.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.util.*;


import xal.ca.*;
import xal.tools.xml.*;
import xal.extension.widgets.swing.*;

import xal.smf.impl.Electromagnet;
import xal.smf.NoSuchChannelException;

/**
*  This is a display for QV01, DCH01, DCV01 fileds 
*
*@author     shishlo
*/
public class  IDmpMagnets_Wrapper {
	
	//JPanel with all GUI elements
	private JPanel wrapperPanel = new JPanel(new BorderLayout());
	private TitledBorder wrapperBorder = null;
	
	//BPM instance
	private Electromagnet quad = null;
	private Electromagnet dch = null;
	private Electromagnet dcv = null;
	
	//positions in X and Y and labels
	private DoubleInputTextField quadFieldTextField = new DoubleInputTextField(10);
	private DoubleInputTextField dchFieldTextField = new DoubleInputTextField(10);
	private DoubleInputTextField dcvFieldTextField = new DoubleInputTextField(10);
	private JLabel quadLabel = new JLabel("QV01 dB/dr [T/m] ");
	private JLabel dchLabel = new JLabel("DCH01   B [T] ");
	private JLabel dcvLabel = new JLabel("DCV01   B [T} ");
	
	private JRadioButton quadMonButton = new JRadioButton("QV01 monitor on/off", true); 
	private JRadioButton dchMonButton = new JRadioButton("DCH01 monitor on/off", true); 
	private JRadioButton dcvMonButton = new JRadioButton("DCV01 monitor on/off", true); 
	
	//coefficients for magnets
	private DoubleInputTextField quadCoeffTextField = new DoubleInputTextField(10);
	private DoubleInputTextField dchCoeffTextField = new DoubleInputTextField(10);
	private DoubleInputTextField dcvCoeffTextField = new DoubleInputTextField(10);
	private JLabel quadCoeffLabel = new JLabel("QV01 coeff.");
	private JLabel dchCoeffLabel = new JLabel("DCH01 coeff.");
	private JLabel dcvCoeffLabel = new JLabel("DCV01 coeff.");	
	private JPanel coeffPanel = new JPanel(new GridLayout(3, 2, 1, 1));
	
	
	//message text field. It is actually message text field from Window
	private JTextField messageTextLocal = new JTextField();
	
	public IDmpMagnets_Wrapper(){
		
		Border border = BorderFactory.createEtchedBorder();
		wrapperBorder = BorderFactory.createTitledBorder(border, "IDmp+ Magnets");
		wrapperPanel.setBorder(wrapperBorder);
		
		//subpanel for fields
		JPanel monPanel = new JPanel(new GridLayout(3, 1, 1, 1));
		monPanel.add(quadMonButton);
		monPanel.add(dchMonButton);
		monPanel.add(dcvMonButton);
		
		JPanel fieldPanel = new JPanel(new GridLayout(3, 1, 1, 1));
		fieldPanel.add(quadFieldTextField);
		fieldPanel.add(dchFieldTextField);
		fieldPanel.add(dcvFieldTextField);	
		
		JPanel labelPanel = new JPanel(new GridLayout(3, 1, 1, 1));
		labelPanel.add(quadLabel);
		labelPanel.add(dchLabel);
		labelPanel.add(dcvLabel);	
		
		JPanel tmp0Panel = new JPanel(new BorderLayout());
		tmp0Panel.add(monPanel,BorderLayout.WEST);
		
		JPanel tmp1Panel = new JPanel(new BorderLayout());		
		tmp1Panel.add(fieldPanel,BorderLayout.WEST);
		tmp1Panel.add(labelPanel,BorderLayout.CENTER);
		
		tmp0Panel.add(tmp1Panel,BorderLayout.CENTER);
		
		JPanel tmp2Panel = new JPanel(new BorderLayout());
		tmp2Panel.add(tmp0Panel,BorderLayout.WEST);
		
		wrapperPanel.add(tmp2Panel,BorderLayout.NORTH);
		
		//external coeff. panel
		coeffPanel.add(quadCoeffTextField);
		coeffPanel.add(quadCoeffLabel);
		coeffPanel.add(dchCoeffTextField);
		coeffPanel.add(dchCoeffLabel);
		coeffPanel.add(dcvCoeffTextField);
		coeffPanel.add(dcvCoeffLabel);
		
		
		quadCoeffTextField.setValue(0.997);
		dchCoeffTextField.setValue(0.554);
		dcvCoeffTextField.setValue(0.487);		
		
		//set up action of the monitor button
		quadMonButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(quadMonButton.isSelected()){
						try{
							quadFieldTextField.setValue(quad.getField());
						}
						catch(ConnectionException ce){
							messageTextLocal.setText("Cannot connect to Magnet:"+quad.getId());
						}
						catch(GetException ge){
							messageTextLocal.setText("Cannot connect to Magnet:"+quad.getId());
						}	
					}
				}
			});
		
		dchMonButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(dchMonButton.isSelected()){
						try{
							dchFieldTextField.setValue(dch.getField());
						}
						catch(ConnectionException ce){
							messageTextLocal.setText("Cannot connect to Magnet:"+dch.getId());
						}
						catch(GetException ge){
							messageTextLocal.setText("Cannot connect to Magnet:"+dch.getId());
						}	
					}
				}
			});		
		
		dcvMonButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(dcvMonButton.isSelected()){
						try{
							dcvFieldTextField.setValue(dcv.getField());
						}
						catch(ConnectionException ce){
							messageTextLocal.setText("Cannot connect to Magnet:"+dcv.getId());
						}
						catch(GetException ge){
							messageTextLocal.setText("Cannot connect to Magnet:"+dcv.getId());
						}	
					}
				}
			});			
	}
	
	/**
	* Returns the panel with all GUI elements
	*/
	public JPanel getJPanel(){
		return wrapperPanel;
	}
	
	/**
	* Sets the Magnets instances
	*/
	public void setMagnets(Electromagnet quad_in,Electromagnet dch_in,Electromagnet dcv_in){
		quad = quad_in;
		dch = dch_in;
		dcv = dcv_in;
		try{
			Channel chQ = quad.findChannel("fieldSet");
			Channel chDCH = dch.findChannel("fieldSet");
			Channel chDCV = dcv.findChannel("fieldSet");
			chQ.connectAndWait();
			chDCH.connectAndWait();
			chDCV.connectAndWait();
			
			chQ.addMonitorValue(new IEventSinkValue(){
					public void eventValue(ChannelRecord record, Channel chan) {
						if(quadMonButton.isSelected()){
							quadFieldTextField.setValue(record.doubleValue());
						}
					}
			}, Monitor.VALUE);
			
			chDCH.addMonitorValue(new IEventSinkValue(){
					public void eventValue(ChannelRecord record, Channel chan) {
						if(dchMonButton.isSelected()){
							dchFieldTextField.setValue(record.doubleValue());
						}
					}
			}, Monitor.VALUE);
			
			chDCV.addMonitorValue(new IEventSinkValue(){
					public void eventValue(ChannelRecord record, Channel chan) {
						if(dcvMonButton.isSelected()){
							dcvFieldTextField.setValue(record.doubleValue());
						}
					}
			}, Monitor.VALUE);
			
			
		}
		catch(NoSuchChannelException nce){
			messageTextLocal.setText("Cannot connect to Magnets");
		}
		catch(ConnectionException ce){
			messageTextLocal.setText("Cannot connect to Magnets");
		}
		catch(MonitorException me){
			messageTextLocal.setText("Cannot monitor Magnets");
		}
	}
	
	/**
	* Returns the quad corrected field
	*/
	public double getQuadField(){
		return quadFieldTextField.getValue()*quadCoeffTextField.getValue();
	}
	
	/**
	* Returns the DCH01 corrected field
	*/
	public double getDCHField(){
		return dchFieldTextField.getValue()*dchCoeffTextField.getValue();
	}	
	
	/**
	* Returns the DCV01 corrected field
	*/
	public double getDCVField(){
		return dcvFieldTextField.getValue()*dcvCoeffTextField.getValue();
	}	
	
	/**
	* Returns the panel with magnet coefficients.
	*/	
	public JPanel getMagnetCoeffPanel(){
		return coeffPanel;
	}
	
	/**
	* Connects the local text message field with the outside field
	*/
	public void setMessageText( JTextField messageTextLocal){
		this.messageTextLocal.setDocument(messageTextLocal.getDocument());
	}
	
	/**
	*  Sets the font for all GUI elements.
	*
	*@param  fnt  The new font
	*/
	public void setFontForAll(Font fnt) {
		wrapperBorder.setTitleFont(fnt);
		quadFieldTextField.setFont(fnt);
		dchFieldTextField.setFont(fnt);
		dcvFieldTextField.setFont(fnt);
		quadLabel.setFont(fnt);
		dchLabel.setFont(fnt);
		dcvLabel.setFont(fnt);
		quadMonButton.setFont(fnt);
		dchMonButton.setFont(fnt);
		dcvMonButton.setFont(fnt);
		
		//coeff panel
		quadCoeffTextField.setFont(fnt);
		quadCoeffLabel.setFont(fnt);    
		dchCoeffTextField.setFont(fnt); 
		dchCoeffLabel.setFont(fnt);     
		dcvCoeffTextField.setFont(fnt);
		dcvCoeffLabel.setFont(fnt);     	
	}
}
