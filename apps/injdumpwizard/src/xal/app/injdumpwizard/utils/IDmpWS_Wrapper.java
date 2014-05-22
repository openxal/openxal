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
import xal.smf.impl.ProfileMonitor;

/**
 * This class creates a JPanel with control elements to start and to stop
 * the wire scanner WS01 in the Injection Dump.
 *
 *@author     shishlo
 */
public class  IDmpWS_Wrapper {

	//Profile Monitor - WireScanner - WS
	private ProfileMonitor ws = null;

	private double scanLength = 0.;

	private volatile boolean scanSuccess = false;

  private volatile boolean hasToStop = false;
  private volatile boolean isScanRunning = false;

	//JPanel with all GUI elements
	private JPanel wrapperPanel = new JPanel(new GridLayout(3, 1, 1, 1));
	private TitledBorder wrapperBorder = null;

	//WS Label
	private JLabel wsLabel = new JLabel("  None  ");

	//progress of the scan - in percents
	private JProgressBar wsProgressBar = new JProgressBar(JProgressBar.HORIZONTAL,1,100);

	//Buttons to start and abort
	private JButton startButton = new JButton(" START SCAN ");
	private JButton abortButton = new JButton(" ABORT SCAN ");
	private JButton getHandVButton = new JButton(" GET X&Y FROM WS ");

  //positions in X and Y and labels
	private DoubleInputTextField xPositionTextField = new DoubleInputTextField(10);
	private DoubleInputTextField yPositionTextField = new DoubleInputTextField(10);
	private JLabel xPosLabel = new JLabel(" x [mm] = ");
	private JLabel yPosLabel = new JLabel(" y [mm] = ");

	//message text field. It is actually message text field from Window
	private JTextField messageTextLocal = new JTextField();

	/**
	 *  Constructor for the IDmpWS_Wrapper object
	 */
	public IDmpWS_Wrapper() {

		Border border = BorderFactory.createEtchedBorder();
		wrapperBorder = BorderFactory.createTitledBorder(border, "WS Control");
		wrapperPanel.setBorder(wrapperBorder);

		//progress bar and label
		wsProgressBar.setBorderPainted(true);
		wsProgressBar.setForeground(Color.blue);
		wsProgressBar.setValue(0);
		JPanel wsProgressPanel = new JPanel(new GridLayout(1, 2, 5, 1));
		wsProgressPanel.add(wsLabel);
		wsProgressPanel.add(wsProgressBar);

		//buttons
		startButton.setEnabled(true);
		abortButton.setEnabled(false);
		JPanel wsButtonPanel = new JPanel(new GridLayout(1, 3, 5, 1));
		wsButtonPanel.add(startButton);
		wsButtonPanel.add(abortButton);
		wsButtonPanel.add(getHandVButton);

   //positions values and labels
	 xPositionTextField.setDecimalFormat(new DecimalFormat("###.#"));
	 yPositionTextField.setDecimalFormat(new DecimalFormat("###.#"));
	 xPositionTextField.setValue(0.);
	 yPositionTextField.setValue(0.);
	 JPanel xPosPanel = new JPanel(new BorderLayout());
	 xPosPanel.add(xPosLabel,BorderLayout.WEST);
	 xPosPanel.add(xPositionTextField,BorderLayout.CENTER);
	 JPanel yPosPanel = new JPanel(new BorderLayout());
	 yPosPanel.add(yPosLabel,BorderLayout.WEST);
	 yPosPanel.add(yPositionTextField,BorderLayout.CENTER);
	 JPanel posPanel = new JPanel(new GridLayout(1, 2, 5, 1));
	 posPanel.add(xPosPanel);
	 posPanel.add(yPosPanel);

	 //add all panels to wrapperPanel
	 wrapperPanel.add(wsProgressPanel);
	 wrapperPanel.add(wsButtonPanel);
	 wrapperPanel.add(posPanel);

	 //set buttons' actions
	 abortButton.addActionListener(
		 new ActionListener() {
			 public void actionPerformed(ActionEvent e) {
				 hasToStop = true;
				 try{
					 ws.stopScan();
				 }
				 catch(ConnectionException ce){
					 wsProgressBar.setValue(0);
					 hasToStop = true;
					 messageTextLocal.setText("Cannot scan! Cannot start WS!");
				 }
				 catch(PutException ge){
					 wsProgressBar.setValue(0);
					 hasToStop = true;						
					 messageTextLocal.setText("Cannot scan! Cannot start WS!");
				 }				 
			 }
		 });

	 startButton.addActionListener(
		 new ActionListener() {
			 public void actionPerformed(ActionEvent e) {
				 makeScan();
			 }
		 });

	 getHandVButton.addActionListener(
		 new ActionListener() {
			 public void actionPerformed(ActionEvent e) {

				 try{
					 xPositionTextField.setValue(ws.getHMeanF());
					 yPositionTextField.setValue(ws.getVMeanF());
					 scanSuccess = true;
				 }
				 catch(ConnectionException ce){
					 scanSuccess = false;
					 messageTextLocal.setText("Cannot get the position from WS!");
				 }
				 catch(GetException ge){
					 scanSuccess = false;
					 messageTextLocal.setText("Cannot get the position from WS!");
				 }

			 }
		 });
	}

	/**
	* Sets the profile monitor (WS)
	*/
	public void setWS(ProfileMonitor ws){
		this.ws = ws;
		wsLabel.setText("  " + ws.getId() + "    Progress: ");

		try{
			scanLength = ws.getScanLength();
			if(scanLength <= 0.){
				messageTextLocal.setText("The scan length should be > 0, length= " + scanLength);
				System.out.println("The scan length should be > 0, length= " + scanLength);
			}
		}
		catch(ConnectionException ce){
			messageTextLocal.setText("Could not connect to " + ws.getId());
			System.out.println("Could not connect to " + ws.getId());
			System.err.println( ce.getMessage() );
			ce.printStackTrace();
		}
		catch(GetException ge){
			messageTextLocal.setText("Could not get data from " + ws.getId());
			System.out.println("Could not get data from " + ws.getId());
			System.err.println( ge.getMessage() );
			ge.printStackTrace();
		}

	}

	/**
	* Returns the panel with all GUI elements
	*/
	public JPanel getJPanel(){
		return wrapperPanel;
	}

	/**
	* Starts the separate thread with a WS scan.
	* The scanSuccess should be defined there.
	* The hasToStop and isScanRunning are defined in startActions()
	* and stopActions() methods.
	*/
	private void makeScan(){

		if(isScanRunning == true){
			messageTextLocal.setText("Cannot start the scan! Abort the old scan first!");
			return;
		}

		Runnable run = new Runnable(){
			public void run(){
				startActions();
				
				try{
					ws.doScan();
				} 
				catch(ConnectionException ce){
					wsProgressBar.setValue(0);
					hasToStop = true;
					messageTextLocal.setText("Cannot scan! Cannot start WS!");
				}
				catch(PutException ge){
					wsProgressBar.setValue(0);
					hasToStop = true;						
					messageTextLocal.setText("Cannot scan! Cannot start WS!");
				}
			
		    scanSuccess = false;
				double x_parkPos = 1.0;
				//step in time in seconds
				double time_step = 1.0;

				while(hasToStop == false){
					try{
						if(ws.getPos() < x_parkPos){
							//let's start the scan
							int try_limit = 10;
							int i_try = 0;
							while(ws.getPos() <= x_parkPos && hasToStop == false){
					      //sleep for a while
								try{
									Thread.sleep((long) (1000*time_step));
								}
								catch(InterruptedException ie){
									hasToStop = true;
								}
								i_try++;
								if(i_try == try_limit){
									hasToStop = true;
									messageTextLocal.setText("Cannot start a scan! Wires did not move!");
									break;
								}
							}
							if(hasToStop == false){
								//scan has been started and we will waight until it is done
                while(ws.getPos() > x_parkPos && hasToStop == false){
									updateProgressBar();
									//sleep for a while
									try{
										Thread.sleep((long) (1000*time_step));
									}
									catch(InterruptedException ie){
										hasToStop = true;
									}
								}
								//wire is parked and scan is successfully done
								if(hasToStop == false){
									scanSuccess = true;
								}
							}
						}
						else{
							hasToStop = true;
						  messageTextLocal.setText("Cannot start a scan! Please park the wires!");
						}
					}
					catch(ConnectionException ce){
						wsProgressBar.setValue(0);
						hasToStop = true;
						messageTextLocal.setText("Cannot scan! Connection to WS is lost!");
					}
					catch(GetException ge){
						wsProgressBar.setValue(0);
						hasToStop = true;
						messageTextLocal.setText("Cannot scan! Cannot read the wire position!");
					}
				}
				//wait a litle bit for WS analysis done
				try{
					if(scanSuccess == true){
						Thread.sleep((long) (5*1000*time_step));
					}
				}
				catch(InterruptedException ie){
				}
				stopActions();
			}
		};

		Thread runThread = new	Thread(run);
		runThread.start();
	}


	/**
	* Performs actions before the scan starts
	*/
	private void startActions(){
		messageTextLocal.setText(null);
		hasToStop = false;
		isScanRunning = true;
		startButton.setEnabled(false);
		abortButton.setEnabled(true);
		updateProgressBar();
	}

	/**
	* Performs actions after the scan stops
	*/
	private void stopActions(){
		startButton.setEnabled(true);
		abortButton.setEnabled(false);
		isScanRunning = false;
		hasToStop = false;
		if(scanSuccess == true){
			try{
				xPositionTextField.setValue(ws.getHMeanF());
				yPositionTextField.setValue(ws.getVMeanF());
			}
			catch(ConnectionException ce){
				scanSuccess = false;
				messageTextLocal.setText("Cannot get the position from WS!");
			}
			catch(GetException ge){
				scanSuccess = false;
				messageTextLocal.setText("Cannot get the position from WS!");
			}
		} else {
			xPositionTextField.setValue(0.);
			yPositionTextField.setValue(0.);
		}
	}

	/**
	* Updates the progress bar
	*/
	private void updateProgressBar(){
		try{
			int progress = (int) (100.0*ws.getPos()/scanLength);
			wsProgressBar.setValue(progress);
		}
		catch(ConnectionException ce){
			wsProgressBar.setValue(0);
			hasToStop = true;
			messageTextLocal.setText("Cannot scan! Connection to WS is lost!");
		}
		catch(GetException ge){
			wsProgressBar.setValue(0);
			hasToStop = true;
			messageTextLocal.setText("Cannot scan! Cannot read the wire position!");
		}
	}

	/**
	* Returns true if the scan is a success.
	*/
	public boolean isScanSuccessful(){
		return scanSuccess;
	}

	/**
	* Returns the horizontal position of the beam.
	*/
	public double getPosH(){
		return xPositionTextField.getValue();
	}

	/**
	* Returns the vertical position of the beam.
	*/
	public double getPosV(){
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
		wsLabel.setFont(fnt);
		startButton.setFont(fnt);
		abortButton.setFont(fnt);
		getHandVButton.setFont(fnt);
		xPositionTextField.setFont(fnt);
		yPositionTextField.setFont(fnt);
		xPosLabel.setFont(fnt);
		yPosLabel.setFont(fnt);
	}
}

