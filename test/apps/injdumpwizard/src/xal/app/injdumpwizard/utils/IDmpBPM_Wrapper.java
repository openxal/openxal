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

import xal.smf.impl.BPM;
import xal.smf.NoSuchChannelException;

/**
 *  This is a display of the last BMP in IDump.
 *
 *@author     shishlo
 */
public class  IDmpBPM_Wrapper {

	//JPanel with all GUI elements
	private JPanel wrapperPanel = new JPanel(new BorderLayout());
	private TitledBorder wrapperBorder = null;

	//BPM instance
	private BPM bpm = null;

  //positions in X and Y and labels
	private DoubleInputTextField xPositionTextField = new DoubleInputTextField(10);
	private DoubleInputTextField yPositionTextField = new DoubleInputTextField(10);
	private JLabel xPosLabel = new JLabel(" x [mm] = ");
	private JLabel yPosLabel = new JLabel(" y [mm] = ");
	
	private JRadioButton monitorButton = new JRadioButton("BPM monitor on/off", true); 

	//message text field. It is actually message text field from Window
	private JTextField messageTextLocal = new JTextField();

	public IDmpBPM_Wrapper(){

		Border border = BorderFactory.createEtchedBorder();
		wrapperBorder = BorderFactory.createTitledBorder(border, "IDump BPM");
		wrapperPanel.setBorder(wrapperBorder);

		//subpanel for fields
		JPanel xPosPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 1));
		xPosPanel.add(xPosLabel);
		xPosPanel.add(xPositionTextField);

		JPanel yPosPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 1));
		yPosPanel.add(yPosLabel);
		yPosPanel.add(yPositionTextField);

		JPanel posPanel = new JPanel(new GridLayout(2, 1, 5, 1));
		posPanel.add(xPosPanel);
		posPanel.add(yPosPanel);
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 1));
		buttonPanel.add(monitorButton);
		
		JPanel pos_and_buttonPanel = new JPanel(new BorderLayout());
		pos_and_buttonPanel.add(posPanel,BorderLayout.NORTH);
		pos_and_buttonPanel.add(buttonPanel,BorderLayout.CENTER);
		
		wrapperPanel.add(pos_and_buttonPanel,BorderLayout.NORTH);
		
		//set up action of the monitor button
		monitorButton.addActionListener(
		 new ActionListener() {
			 public void actionPerformed(ActionEvent e) {
				 if(monitorButton.isSelected()){
						try{
							xPositionTextField.setValue(bpm.getXAvg());
							yPositionTextField.setValue(bpm.getYAvg());
						}
						catch(ConnectionException ce){
							messageTextLocal.setText("Cannot connect to BPM:"+bpm.getId());
						}
						catch(GetException ge){
							messageTextLocal.setText("Cannot connect to BPM:"+bpm.getId());
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
	* Sets the BPM instance
	*/
  public void setBPM(BPM bpm_in){
		bpm = bpm_in;
		wrapperBorder.setTitle(bpm.getId());
		try{
			Channel chX = bpm.getAndConnectChannel(BPM.X_AVG_HANDLE);
			Channel chY = bpm.getAndConnectChannel(BPM.Y_AVG_HANDLE);

			chX.addMonitorValue(new IEventSinkValue(){
					public void eventValue(ChannelRecord record, Channel chan) {
							if(monitorButton.isSelected()){
								xPositionTextField.setValue(record.doubleValue());
							}
					}
			}, Monitor.VALUE);

			chY.addMonitorValue(new IEventSinkValue(){
					public void eventValue(ChannelRecord record, Channel chan) {
							if(monitorButton.isSelected()){
								yPositionTextField.setValue(record.doubleValue());
							}
					}
			}, Monitor.VALUE);

		}
		catch(NoSuchChannelException nce){
			messageTextLocal.setText("Cannot connect to BPM:"+bpm.getId());
		}
		catch(ConnectionException ce){
			messageTextLocal.setText("Cannot connect to BPM:"+bpm.getId());
		}
		catch(MonitorException me){
			messageTextLocal.setText("Cannot monitor BPM:"+bpm.getId());
		}
	}

	/**
	* Returns the x BPM signal
	*/
	public double getX(){
		return xPositionTextField.getValue();
	}

	/**
	* Returns the y BPM signal
	*/
	public double getY(){
		return yPositionTextField.getValue();
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
		xPositionTextField.setFont(fnt);
		yPositionTextField.setFont(fnt);
		xPosLabel.setFont(fnt);
		yPosLabel.setFont(fnt);
		monitorButton.setFont(fnt);
	}
}
