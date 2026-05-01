package xal.app.injdumpwizard.utils;


import java.text.*;
import java.io.*;
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
import xal.extension.widgets.plot.*;

/**
 *  This class will perform the beam position calculation at the Injection Dump.
 *
 *@author     shishlo
*/
public class  IDmpPositionCalculator {

	//JPanel with all GUI elements
	private JPanel wrapperPanel = new JPanel(new BorderLayout());
	private TitledBorder wrapperBorder = null;

	//WS, BPM, and Magnets wrappers
	private IDmpWS_Wrapper wsWrapper = null;
	private IDmpBPM_Wrapper bpm00Wrapper = null;
	private IDmpBPM_Wrapper bpm01Wrapper = null;
	private IDmpBPM_Wrapper bpm02Wrapper = null;
	private IDmpBPM_Wrapper bpm03Wrapper = null;
	private IDmpMagnets_Wrapper magnetsWrapper = null;
	private JPanel magnetsWrapperPanel = new JPanel(new BorderLayout());
	private TwoGraph_Wrapper twoGraphWrapperPanel = new TwoGraph_Wrapper();

	//Use in calculation Buttons
	private JRadioButton bpm00Button = new JRadioButton("Use BPM00 ", false);
	private JRadioButton bpm01Button = new JRadioButton("BPM01a ", true);
	private JRadioButton bpm02Button = new JRadioButton("BPM01b ", true);
	private JRadioButton bpm03Button = new JRadioButton("BPM01c ", true);
	private JRadioButton wsButton = new JRadioButton("WS01 ", false);
		
	//our accelerator sequence
	private IDmpAccSeq  accModel = new IDmpAccSeq();
	
	//calculation button
	private JButton calcButton = new JButton(" CALCULATE POSITION ");

	//positions in X and Y and labels
	private DoubleInputTextField xPositionTextField = new DoubleInputTextField(10);
	private DoubleInputTextField yPositionTextField = new DoubleInputTextField(10);
	private JLabel xPosLabel = new JLabel("Beam at Injection Dump x [mm] = ");
	private JLabel yPosLabel = new JLabel("Beam at Injection Dump y [mm] = ");

	//the switcher for bpm transformation
	public JRadioButton bpmTableSwitcherButton = new JRadioButton("Use BPM Transform. table ", false);
	
	//the bpm data transformation table
	private LinearData3D bpmTransformTable = null;
	
	//message text field. It is actually message text field from Window
	private JTextField messageTextLocal = new JTextField();
	public IDmpPositionCalculator(){
		Border border = BorderFactory.createEtchedBorder();
		wrapperBorder = BorderFactory.createTitledBorder(border, "IDmp Position Calculation");
		wrapperPanel.setBorder(wrapperBorder);
		//subpanel for fields and the button
		JPanel calcButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 1));
		calcButtonPanel.add(bpm00Button);
		calcButtonPanel.add(bpm01Button);
		calcButtonPanel.add(bpm02Button);
		calcButtonPanel.add(wsButton);		
		calcButtonPanel.add(bpm03Button);
		calcButtonPanel.add(calcButton);		

		JPanel xPosPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 1));
		xPosPanel.add(xPosLabel);
		xPosPanel.add(xPositionTextField);

		JPanel yPosPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 1));
		yPosPanel.add(yPosLabel);
		yPosPanel.add(yPositionTextField);

		JPanel posPanel = new JPanel(new GridLayout(3, 1, 5, 1));
		posPanel.add(calcButtonPanel);
		posPanel.add(xPosPanel);
		posPanel.add(yPosPanel);
		
		JPanel upperPanel = new JPanel(new BorderLayout());
		upperPanel.add(magnetsWrapperPanel, BorderLayout.WEST);
		upperPanel.add(posPanel,BorderLayout.CENTER);

		wrapperPanel.add(upperPanel,BorderLayout.NORTH);
		wrapperPanel.add(twoGraphWrapperPanel.getJPanel(),BorderLayout.CENTER);

		//button action
		calcButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					messageTextLocal.setText(null);
					int diag_count = 0;
					if(bpm00Button.isSelected() == true){diag_count++;}
					if(bpm01Button.isSelected() == true){diag_count++;}
					if(bpm02Button.isSelected() == true){diag_count++;}
					if(bpm03Button.isSelected() == true){diag_count++;}
					if(wsButton.isSelected() == true){diag_count++;}
					if(diag_count < 2){
					  xPositionTextField.setText(null);
						yPositionTextField.setText(null);
						messageTextLocal.setText("Need more then two diagnostics nodes! Stop.");
						return;
					}
					//calculation
					double bpm00X_tmp = bpm00Wrapper.getX();
					double bpm00Y_tmp = bpm00Wrapper.getY();
					double bpm01X_tmp = bpm01Wrapper.getX();
					double bpm01Y_tmp = bpm01Wrapper.getY();					
					double bpm02X_tmp = bpm02Wrapper.getX();
					double bpm02Y_tmp = bpm02Wrapper.getY();					
					double bpm03X_tmp = bpm03Wrapper.getX();
					double bpm03Y_tmp = bpm03Wrapper.getY();					
					double wsX = wsWrapper.getPosH();
					double wsY = wsWrapper.getPosV();	
					double bpm00X = bpm00X_tmp;
					double bpm00Y = bpm00Y_tmp; 
					double bpm01X = bpm01X_tmp;
					double bpm01Y = bpm01Y_tmp;
					double bpm02X = bpm02X_tmp;
					double bpm02Y = bpm02Y_tmp; 
					double bpm03X = bpm03X_tmp;
					double bpm03Y = bpm03Y_tmp;
					//System.out.println("debug "+" bpm X="+bpm00X +" "+bpm01X +" "+bpm02X +" "+bpm03X +" ");
					//System.out.println("debug "+" bpm X="+bpm00Y +" "+bpm01Y +" "+bpm02Y +" "+bpm03Y +" ");
					if(bpmTableSwitcherButton.isSelected()){
						//BPM Transformation correction
						bpm00X = getTransfX(bpm00X_tmp,bpm00Y_tmp);
						bpm00Y = getTransfX(bpm00Y_tmp,bpm00X_tmp);
						bpm01X = getTransfX(bpm01X_tmp,bpm01Y_tmp);
						bpm01Y = getTransfX(bpm01Y_tmp,bpm01X_tmp);
						bpm02X = getTransfX(bpm02X_tmp,bpm02Y_tmp);
						bpm02Y = getTransfX(bpm02Y_tmp,bpm02X_tmp);
						bpm03X = getTransfX(bpm03X_tmp,bpm03Y_tmp);
						bpm03Y = getTransfX(bpm03Y_tmp,bpm03X_tmp);
						wsX = wsX + 12.0;
						wsY = wsY + 5.6;
					}
					//System.out.println("debug bpm00X="+bpm00X+" bpm00Y="+bpm00Y+" bpm01X="+bpm01X+" bpm01Y="+bpm01Y);
					//System.out.println("debug bpm02X="+bpm02X+" bpm02Y="+bpm02Y+" bpm03X="+bpm03X+" bpm03Y="+bpm03Y);
					//set up model
					double quad_f = magnetsWrapper.getQuadField();
					double dch_f = magnetsWrapper.getDCHField();
					double dcv_f = magnetsWrapper.getDCVField();
					//we do not need to set up eff. coeff. because magnetsWrapper
					// has them inside and returns fields with these corrections
					accModel.setMagnetFields(quad_f,dch_f,dcv_f);
					//offsets in mm
					accModel.setMagnetOffsetsX(0.,0.,0.);
					accModel.setMagnetOffsetsY(0.,0.,0.);
					//set bpm00 , bpm01, ws01 usage
					accModel.setDiagUsage(bpm00Button.isSelected(),bpm01Button.isSelected(),bpm02Button.isSelected(),bpm03Button.isSelected(),wsButton.isSelected());
					//set live data
					accModel.setLiveOrbitX(bpm00X,bpm01X,bpm02X,bpm03X,wsX);
					accModel.setLiveOrbitY(bpm00Y,bpm01Y,bpm02Y,bpm03Y,wsY);
					//start fitting initial cond. and orbit
					accModel.findOrbit();
					double x = accModel.getDumpX();
					double y = accModel.getDumpY();
					//double x = wsX + 12.565*(wsX - bpm01X)/7.272;
					//double y = wsY + 12.565*(wsY - bpm01Y)/7.272;
					xPositionTextField.setValue(x);
					yPositionTextField.setValue(y);
					//plot results
					accModel.makeGraphsX(twoGraphWrapperPanel.getExpGraphX(),twoGraphWrapperPanel.getModelGraphX());
					accModel.makeGraphsY(twoGraphWrapperPanel.getExpGraphY(),twoGraphWrapperPanel.getModelGraphY());
					twoGraphWrapperPanel.updateGraphs();
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
	* Sets the external wrappers
	*/
	public void setWrappers(
		IDmpWS_Wrapper wsWrapper, 
		IDmpBPM_Wrapper bpm00Wrapper,
		IDmpBPM_Wrapper bpm01Wrapper,
		IDmpBPM_Wrapper bpm02Wrapper,
		IDmpBPM_Wrapper bpm03Wrapper,
		IDmpMagnets_Wrapper magnetsWrapper)
	{
		this.wsWrapper = wsWrapper;
		this.bpm00Wrapper = bpm00Wrapper;
		this.bpm01Wrapper = bpm01Wrapper;
		this.bpm02Wrapper = bpm02Wrapper;
		this.bpm03Wrapper = bpm03Wrapper;
		this.magnetsWrapper = magnetsWrapper;
		magnetsWrapperPanel.add(magnetsWrapper.getJPanel(),BorderLayout.CENTER);
	}

	/**
	* Sets the momentum of the protons in eV/c.
	*/	
	public void setMomentum(double momentum){
		accModel.setMomentum(momentum);
	}
	
	/**
	* Calculates (x,y) -> (x_real,y_real) transformation.
	* The return is a x_real value. The y-value can be 
	* calculated by using the symmetry.
	*/		
	private double getTransfX(double x, double y){
		double x0 = Math.abs(x);
		double y0 = Math.abs(y);
		double x_real = bpmTransformTable.getValue(x0,y0);
		x_real = Math.signum(x)*x_real;
		return x_real;
	}
	
	/**
	* Reads the table for (x,y) -> (x_real,y_real) transformation
	* The table includes only 1/4 of the whole table, for x_real
	* only, and for positive x and y. The rest of the transformation
	* can be calculated with account of symmetry.
	*/		
	public void readBPMTransformationTable(InputStream inps){

		try{	
			BufferedReader in = new BufferedReader(new InputStreamReader(inps));
			String lineIn = in.readLine();
			String[] dataS = lineIn.split("\\s+");
			int nSize = dataS.length-1;
			double x_min = Double.parseDouble(dataS[1]);
			double x_max = Double.parseDouble(dataS[nSize]);
			bpmTransformTable = new LinearData3D(nSize,nSize);
			bpmTransformTable.setZero();
			bpmTransformTable.setMinMaxX(x_min,x_max);
			bpmTransformTable.setMinMaxY(x_min,x_max);
			for(int i = 0; i < nSize; i++){
				lineIn = in.readLine();
				dataS = lineIn.split("\\s+");
				for(int j = 0; j < nSize; j++){
					bpmTransformTable.setValue(i,j,Double.parseDouble(dataS[j+1]));
				}
			}
			bpmTransformTable.calcMaxMinZ();
			in.close();
		} catch (IOException exception) {
			messageTextLocal.setText(null);
			messageTextLocal.setText("Fatal error. BPM data file is bad. Stop execution. Call the developer.");
		}
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
		calcButton.setFont(fnt);
		bpmTableSwitcherButton.setFont(fnt);
	}
}
