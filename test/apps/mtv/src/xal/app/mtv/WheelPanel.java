/*
 * MyWindow.java
 *
 * Created on April 14, 2003, 10:25 AM
 */

package xal.app.mtv;

import javax.swing.*;
import java.beans.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import xal.extension.widgets.swing.Wheelswitch;

import xal.ca.*;
import xal.tools.text.ScientificNumberFormat;

/**
 * The window representation / view of an xiodiag document
 *
 * @author  jdg
 */
public class WheelPanel extends JPanel {
    /** serialization identifier */
    private static final long serialVersionUID = 1L;
    
    private MTVDocument theDoc;
    private JLabel pvLabel, upperLabel, lowerLabel, restoreLabel;
    protected Wheelswitch pvWheel;
		private JRadioButton bindBBookButton = new JRadioButton("Bind to B_Book"); 
    private Number upperLimit, lowerLimit;
    private JButton restoreButton = null;
    private JButton memorizeButton = null;
    private double restoreValue;
    private PropertyChangeListener wheelListener;
		private PVTableCell pv_cell = null;
		
    private ScientificNumberFormat scientificFormat = new ScientificNumberFormat( 6, 12, false );
		
    /** Creates a new instance of MainWindow */
    public WheelPanel(MTVDocument aDocument) {
			theDoc = aDocument;
			makeContent();
			updateWheelListener();
    }
    
    protected void makeContent () {
			GridBagLayout gridBag = new GridBagLayout(); 
			this.setLayout(gridBag);
			
			// from top -> down:
			int sumy = 0;
			
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.fill = GridBagConstraints.NONE;
			gbc.weightx = 0.; gbc.weighty = 0.;
			gbc.gridx = 0; gbc.gridy = sumy++;
			gbc.gridwidth = 1; gbc.gridheight = 1;
									
			gbc.gridx = 0; gbc.gridy = sumy;
			gbc.gridheight = 2;
			pvLabel = new JLabel("Null");
			gridBag.setConstraints(pvLabel , gbc);
			add(pvLabel);
			
			pvWheel = new Wheelswitch();
			pvWheel.setFormat("+###.#####");
			
			gbc.gridx = 1; gbc.gridy = sumy;
			gbc.insets = new Insets(10, 5, 10,5);
			gbc.gridwidth = 1;  
			gridBag.setConstraints(pvWheel, gbc);
			add(pvWheel);
			
			gbc.gridx = 2; gbc.gridy = sumy;			
			gridBag.setConstraints(bindBBookButton, gbc);	
			add(bindBBookButton);
			bindBBookButton.setEnabled(false);
			sumy += 2;
			
			upperLabel = new JLabel("upper  lim = ");
			lowerLabel = new JLabel("lower  lim = ");
			
			gbc.gridx = 0; gbc.gridy = sumy; gbc.gridheight = 1;
			gridBag.setConstraints(upperLabel, gbc);
			add(upperLabel);
			
			gbc.gridx = 1; gbc.gridy = sumy++;
			gridBag.setConstraints(lowerLabel, gbc);
			add(lowerLabel);
			
			bindBBookButton.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed (java.awt.event.ActionEvent evt) {
						if(pv_cell != null && pv_cell.getBBookCell() != null){
							pv_cell.getBBookCell().setIsBundToBBook(bindBBookButton.isSelected());
						}
					}
			});			
			
			restoreButton = new JButton("Restore Original Value");
			restoreButton.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed (java.awt.event.ActionEvent evt) {
						if(pv_cell != null && pv_cell.getChannel() != null && pv_cell.isDummy() == false){
							pvWheel.removePropertyChangeListener("value", wheelListener);
							pv_cell.restoreValueFromMemory();
							try{
								Thread.sleep(1800);
							}
							catch(InterruptedException excp){
							}
							setLimits();
							pvWheel.addPropertyChangeListener("value", wheelListener); 							
						}
					}
			});	
			
			memorizeButton  = new JButton("Memorize Value as Original");
			memorizeButton.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed (java.awt.event.ActionEvent evt) {
						if(pv_cell != null && pv_cell.getChannel() != null && pv_cell.isDummy() == false){
							pvWheel.removePropertyChangeListener("value", wheelListener);
							pv_cell.memorizeValue();
							setLimits();
							pvWheel.addPropertyChangeListener("value", wheelListener);
						}
					}
			});	
			
			gbc.gridx = 0; gbc.gridy = sumy;
			gridBag.setConstraints(restoreButton, gbc);
			add(restoreButton);
			
			restoreLabel = new JLabel("null");
			gbc.gridx = 1; gbc.gridy = sumy;
			gridBag.setConstraints(restoreLabel, gbc);
			add(restoreLabel);
			
			gbc.gridx = 2; gbc.gridy = sumy;
			gridBag.setConstraints(memorizeButton, gbc);
			add(memorizeButton);			
			
    }
    
    /** a listener to send updated values to a channel */
    private void updateWheelListener() {
			wheelListener = new PropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent event) {
					double val = pvWheel.getValue();
					try{
						if(pv_cell != null && pv_cell.getChannel() != null && pv_cell.isDummy() == false){
							pv_cell.getChannel().putVal(val);
							pv_cell.valueChanged();
							if(pv_cell.getBBookCell() != null && bindBBookButton.isSelected()){
								pv_cell.getBBookCell().getChannel().putVal(val);
								pv_cell.getBBookCell().valueChanged();
							}
						}		
					}
					catch(Exception excp){
						pv_cell = null;
						pvWheel.removePropertyChangeListener("value", wheelListener);
						setLimits();
						pvWheel.addPropertyChangeListener("value", wheelListener); 		
					}
				}
			};
    }
	    
    protected void setPVTableCell(PVTableCell pv_cell) {
			this.pv_cell = pv_cell;
			pvWheel.removePropertyChangeListener("value", wheelListener);
			setLimits();
			pvWheel.addPropertyChangeListener("value", wheelListener);  
			if(pv_cell != null && pv_cell.getBBookCell() != null){
				bindBBookButton.setSelected(pv_cell.getBBookCell().getIsBundToBBook());
			}			
    }
    
    private void setLimits(){
			if(pv_cell == null || pv_cell.isDummy() == true || pv_cell.getChannel() == null ){
				pv_cell = null;
				pvWheel.setValue(0.);
				upperLabel.setText("upper Lim = ");
				lowerLabel.setText("lower Lim = ");
				pvLabel.setText("null");
				restoreLabel.setText("");
				bindBBookButton.setEnabled(false);
				bindBBookButton.setSelected(false);
				return;
			}
			try{
				double val = pv_cell.getValDbl();
				pvWheel.setValue(val);
				pvLabel.setText(pv_cell.getChannel().getId());
				if(pv_cell.wasClicked() == false){
					pv_cell.memorizeValue();
				}
				bindBBookButton.setEnabled(false);
				if(pv_cell.getBBookCell() != null){
					if(pv_cell.getBBookCell().wasClicked() == false){
						pv_cell.getBBookCell().memorizeValue();
					}
					bindBBookButton.setEnabled(true);
					bindBBookButton.setSelected(false); 
				}
				restoreValue = pv_cell.getValueFromMemory();
				restoreLabel.setText(scientificFormat.format(restoreValue));
				upperLimit = pv_cell.getChannel().upperControlLimit();
				upperLabel.setText("Upper Lim = " + upperLimit);
				lowerLimit = pv_cell.getChannel().lowerControlLimit();
				lowerLabel.setText("Lower Lim = " + lowerLimit.toString());
			}
			catch(Exception excp){
				pv_cell = null;
				pvWheel.setValue(0.);
				upperLabel.setText("upper Lim = ");
				lowerLabel.setText("lower Lim = ");
				pvLabel.setText("null");
				restoreLabel.setText("");
				bindBBookButton.setEnabled(false);
				bindBBookButton.setSelected(false);				
			}
    }
}
