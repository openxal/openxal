package xal.app.lossviewer.dndcomponents.lossplot;




import java.awt.*;
import java.awt.event.*;
import java.text.*;
import javax.swing.*;
import javax.swing.border.*;
import xal.tools.apputils.ChartPopupAdaptor;
import xal.tools.apputils.SimpleChartDialog;



public class LossDialog extends SimpleChartDialog implements  SwingConstants {
    private static final long serialVersionUID = -6473656321262092545L;
    protected ChartPopupAdaptor chartAdaptor;
    protected Component _parent;
	
	// visual components
    private JCheckBox yAutoScaleCheckbox;
    private JTextField yAxisMinValueField;
    private JTextField yAxisMaxValueField;

    private JTextField yAxisDivisionsField;

	
    
    /**
	 * Creates new form SimpleChartDialog
	 * @param parent the parent view near which to display this dialog
	 * @param aChartAdaptor the chart popup adaptor to use
	 */
    public LossDialog(Component parent, ChartPopupAdaptor aChartAdaptor) {
        
		super(parent, aChartAdaptor);
    }
	
    
    /**
	 * Creates new form SimpleChartDialog
	 * @param owner the window which owns this dialog
	 * @param parent the parent view near which to display this dialog
	 * @param aChartAdaptor the chart popup adaptor to use
	 */
    public LossDialog(Frame owner, Component parent, ChartPopupAdaptor aChartAdaptor) {
        
		super(owner, parent, aChartAdaptor);
    }
	
    
    /**
	 * Creates new form SimpleChartDialog
	 * @param owner the window which owns this dialog
	 * @param parent the parent view near which to display this dialog
	 * @param aChartAdaptor the chart popup adaptor to use
	 */
    public LossDialog(Dialog owner, Component parent, ChartPopupAdaptor aChartAdaptor) {
        
		super(owner,parent, aChartAdaptor);
    }
	
	public void setType(String type) {
		setTitle(type+" settings");
		border.setTitle(type+" axis");
		
	}
	
	
	/**
	 * Setup the dialog
	 * @param parent the parent view near which to display this dialog
	 * @param aChartAdaptor the chart popup adaptor to use
	 */
	protected void setup(Component parent, ChartPopupAdaptor aChartAdaptor) {
        setModal(true);
        initComponents();
		
        _parent = parent;
        chartAdaptor = aChartAdaptor;
	}
    
    
	/**
	 * display this dialog
	 */
    public void showDialog() {
        revertSettings();
        setLocationRelativeTo(_parent);
        setVisible( true );
    }
	
    
    /** convenience method for setting an int field */
    static public void setFieldValue(JTextField field, int value) {
        field.setText( String.valueOf(value) );
    }
    
    
    /** convenience method for setting a double field */
    static public void setFieldValue(JTextField field, double value) {
        DecimalFormat formatter = new DecimalFormat("0.0##E0");
        field.setText( formatter.format(value) );
    }
    
    
    /** Set the values in the panel to reflect the setting in the chart */
    protected void revertSettings() {
       
		
        setFieldValue( yAxisMinValueField, chartAdaptor.getMinYLimit() );
        setFieldValue( yAxisMaxValueField, chartAdaptor.getMaxYLimit() );

        setFieldValue( yAxisDivisionsField, chartAdaptor.getYNumMajorTicks()-1 );

		
        boolean yAutoScales = chartAdaptor.isYAutoScale();
        yAutoScaleCheckbox.setSelected(yAutoScales);
		
        yAxisMinValueField.setEnabled(!yAutoScales);
        yAxisMaxValueField.setEnabled(!yAutoScales);

        yAxisDivisionsField.setEnabled(!yAutoScales);
    }
    
    
    /** Apply the values entered in the panel to the chart */
    protected void applySettings() {
        
        
        if ( !chartAdaptor.isYAutoScale() ) {
            applyYAxisMinValue();
            applyYAxisMaxValue();
            applyYAxisMinValue();   // repeated in case new y-min is greater than old y-max

            applyYAxisMajorTicks();
        }
        
        // update display with actual settings
        revertSettings();
    }
	

    
    /** Apply the Minimum y-axis value */
    protected void applyYAxisMinValue() {
        try {
            String text = yAxisMinValueField.getText();
            double yAxisMinValue = Double.parseDouble(text);
            // don't change the value unless the user does to avoid inadvertantly
            // changing from autoscale
            if ( yAxisMinValue != chartAdaptor.getMinYLimit() ) {
                chartAdaptor.setMinYLimit( yAxisMinValue );
            }
        }
        catch(NumberFormatException excpt) {
            Toolkit.getDefaultToolkit().beep();
        }
    }
    
    
    /** Apply the Maximum y-axis value */
    protected void applyYAxisMaxValue() {
        try {
            String text = yAxisMaxValueField.getText();
            double yAxisMaxValue = Double.parseDouble(text);
            // don't change the value unless the user does to avoid inadvertantly
            // changing from autoscale
            if ( yAxisMaxValue != chartAdaptor.getMaxYLimit() ) {
                chartAdaptor.setMaxYLimit( yAxisMaxValue );
            }
        }
        catch(NumberFormatException excpt) {
            Toolkit.getDefaultToolkit().beep();
        }
    }
    
    
    
    /** Apply the Y-Axis tick spacing */
    protected void applyYAxisMajorTicks() {
        try {
            String text = yAxisDivisionsField.getText();
            int numTicks = Integer.parseInt(text) + 1;
            // don't change the value unless the user does to avoid inadvertantly
            // changing from autoscale
			if ( numTicks < 2 ) {
				Toolkit.getDefaultToolkit().beep();
			}
            else if ( numTicks != chartAdaptor.getYNumMajorTicks() ) {
                chartAdaptor.setYNumMajorTicks(numTicks);
            }
        }
        catch(NumberFormatException excpt) {
            Toolkit.getDefaultToolkit().beep();
        }
    }
    

	
	
	
	private TitledBorder border = new TitledBorder("Y Axis");
	/**
	 * Create and layout the visual components.
	 */
    private void initComponents() {
        setTitle("Chart Settings");
		setSize( new Dimension(450, 375) );
        setResizable(false);
		
        addWindowListener(new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent evt) {
					closeDialog(evt);
				}
			});
		
		Box mainView = new Box(VERTICAL);
        getContentPane().add(mainView);
		
		
		Box row;
		
		Box yAxisView = new Box(VERTICAL);
        yAxisView.setBorder(border);
		mainView.add(yAxisView);
		
        yAutoScaleCheckbox = new JCheckBox("Auto Scale");
        yAutoScaleCheckbox.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					yAutoScaleCheckboxActionPerformed(evt);
				}
			});
		row = new Box(HORIZONTAL);
		row.add( Box.createHorizontalGlue() );
		row.add(yAutoScaleCheckbox);
		yAxisView.add(row);
		
        yAxisMinValueField = new JTextField(10);
		yAxisMinValueField.setMaximumSize( yAxisMinValueField.getPreferredSize() );
        yAxisMinValueField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
		yAxisMinValueField.addFocusListener( new FocusAdapter() {
				public void focusGained(FocusEvent event) {
					yAxisMinValueField.selectAll();
				}
				public void focusLost(FocusEvent event) {
					yAxisMinValueField.setCaretPosition(0);
					yAxisMinValueField.moveCaretPosition(0);
				}
			});
		row = new Box(HORIZONTAL);
		row.add( Box.createHorizontalGlue() );
		row.add( new JLabel("Min:") );
		row.add(yAxisMinValueField);
		yAxisView.add(row);
		
        yAxisMaxValueField = new JTextField(10);
		yAxisMaxValueField.setMaximumSize( yAxisMaxValueField.getPreferredSize() );
        yAxisMaxValueField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
		yAxisMaxValueField.addFocusListener( new FocusAdapter() {
				public void focusGained(FocusEvent event) {
					yAxisMaxValueField.selectAll();
				}
				public void focusLost(FocusEvent event) {
					yAxisMaxValueField.setCaretPosition(0);
					yAxisMaxValueField.moveCaretPosition(0);
				}
			});
		row = new Box(HORIZONTAL);
		row.add( Box.createHorizontalGlue() );
		row.add( new JLabel("Max:") );
		row.add(yAxisMaxValueField);
		yAxisView.add(row);
		
        yAxisDivisionsField = new JTextField(10);
		yAxisDivisionsField.setMaximumSize( yAxisDivisionsField.getPreferredSize() );
        yAxisDivisionsField.setHorizontalAlignment(JTextField.RIGHT);
		yAxisDivisionsField.addFocusListener( new FocusAdapter() {
				public void focusGained(FocusEvent event) {
					yAxisDivisionsField.selectAll();
				}
				public void focusLost(FocusEvent event) {
					yAxisDivisionsField.setCaretPosition(0);
					yAxisDivisionsField.moveCaretPosition(0);
				}
			});
		row = new Box(HORIZONTAL);
		row.add( Box.createHorizontalGlue() );
		row.add( new JLabel("Major Divisions:") );
		row.add(yAxisDivisionsField);
		yAxisView.add(row);
		

		
		
		Box buttonView = new Box(HORIZONTAL);
		mainView.add(buttonView);
		buttonView.add( Box.createHorizontalGlue() );
		
		JButton revertButton = new JButton("Revert");
        revertButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					revertButtonActionPerformed(event);
				}
			});
		buttonView.add(revertButton);
		
		JButton applyButton = new JButton("Apply");
        applyButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					applyButtonActionPerformed(event);
				}
			});
		buttonView.add(applyButton);
		
        pack();
    }
	
	
 
	
	
    private void yAutoScaleCheckboxActionPerformed(java.awt.event.ActionEvent evt) {
        boolean autoScales = yAutoScaleCheckbox.isSelected();
        chartAdaptor.setYAutoScale(autoScales);
        revertSettings();
    }
	
	

	
	
    private void revertButtonActionPerformed(java.awt.event.ActionEvent evt) {
        revertSettings();
    }
	
	
    private void applyButtonActionPerformed(java.awt.event.ActionEvent evt) {
        applySettings();
    }
	
    
    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {
        setVisible(false);
    }
}
