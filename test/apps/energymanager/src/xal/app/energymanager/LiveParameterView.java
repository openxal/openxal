//
//  LiveParameterView.java
//  xal
//
//  Created by Thomas Pelaia on 4/19/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//


package xal.app.energymanager;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.Container;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.text.*;
import javax.swing.text.*;
import java.beans.*;


/** View of a live parameter's properties and state. */
public class LiveParameterView extends Box implements LiveParameterListener, SwingConstants {
	
    private final static long serialVersionUID = 1L;

    static private final Color DEFAULT_VALUE_COLOR = Color.BLUE;
	static private final Color WARNING_COLOR = Color.RED;
	
	protected DecimalFormat _valueFormatter;
	protected LiveParameter _parameter;
	
	protected final JLabel _titleView;
	
	protected final JFormattedTextField _designView;
	protected final JFormattedTextField _readbackView;
	protected final JFormattedTextField _controlView;
	
	protected final JFormattedTextField _controlLowerLimitView;
	protected final JFormattedTextField _controlUpperLimitView;
	
	protected final JRadioButton _designButton;
	protected final JRadioButton _customButton;
	protected final JRadioButton _controlButton;
	protected final JFormattedTextField _variableInitialValueView;
	protected final JFormattedTextField _variableLowerLimitView;
	protected final JFormattedTextField _variableUpperLimitView;
	
	protected final JCheckBox _variableCheckBox;
	
	
	/**
	 * Primary Constructor 
	 * @param parameter the live parameter to display with this view.
	 * @param formatPattern the format pattern used to format the value fields.
	 */
	public LiveParameterView( final LiveParameter parameter, final String formatPattern ) {
		super( BoxLayout.Y_AXIS );
		
		_valueFormatter = new DecimalFormat( formatPattern );
		
		_titleView = new JLabel( "No Selection" );
		_titleView.setFont( new Font( "Dialog", Font.BOLD, 16 ) );
		
		_designView = makeNumericField( false, false );
		_readbackView = makeNumericField( false, false );
		_controlView = makeNumericField( false, false );
		
		_controlLowerLimitView = makeNumericField( false, false );
		_controlUpperLimitView = makeNumericField( false, false );
		
		final ButtonGroup variableSourceGroup = new ButtonGroup();
		_designButton = new JRadioButton( LiveParameter.getSourceName( LiveParameter.DESIGN_SOURCE ) );
		_customButton = new JRadioButton( LiveParameter.getSourceName( LiveParameter.CUSTOM_SOURCE ) );
		_controlButton = new JRadioButton( LiveParameter.getSourceName( LiveParameter.CONTROL_SOURCE ) );
		variableSourceGroup.add( _designButton );
		variableSourceGroup.add( _customButton );
		variableSourceGroup.add( _controlButton );
		
		_variableInitialValueView = makeNumericField( true );
		_variableLowerLimitView = makeNumericField( true );
		_variableUpperLimitView = makeNumericField( true );
		_variableCheckBox = new JCheckBox( "Variable" );
		
		monitorVariableCheckbox();
		handleTextFieldEvents();
		
		setEnabled( false );
		
		// set the parameter so we can populate the data views
		setParameter( parameter );
		
		addTitleView();
		addValueView();
		addControlsLimitView();
		addVariableView();
	}
	
	
	/**
	 * Constructor 
	 * @param parameter the live parameter to display with this view.
	 */
	public LiveParameterView( final LiveParameter parameter ) {
		this( parameter, "0.00000" );
	}
	
	
	/** Constructor */
	public LiveParameterView() {
		this( null );
	}
	
	
	/** add the title view */
	private void addTitleView() {
		final Box box = new Box( BoxLayout.X_AXIS );
		add( box );
		box.add( Box.createHorizontalGlue() );
		box.add( _titleView );
		box.add( Box.createHorizontalGlue() );
	}
	
	
	/** add the value view */
	private void addValueView() {
		final Box box = new Box( BoxLayout.Y_AXIS );
		add( box );
		final JPanel column = new JPanel();
		box.add( column );
		box.setBorder( BorderFactory.createTitledBorder( "Value" ) );
		column.setLayout( new GridLayout( 0, 2 ) );
		addLabeledItem( column, "Design: ", _designView, DEFAULT_VALUE_COLOR );
		addLabeledItem( column, "Readback: ", _readbackView, DEFAULT_VALUE_COLOR );
		addLabeledItem( column, "Control: ", _controlView, DEFAULT_VALUE_COLOR );		
	}
	
	
	/** add the controls limit view */
	private void addControlsLimitView() {
		final Box box = new Box( BoxLayout.Y_AXIS );
		add( box );
		final JPanel column = new JPanel();
		box.add( column );
		box.setBorder( BorderFactory.createTitledBorder( "Control Limits" ) );
		column.setLayout( new GridLayout( 0, 2 ) );
		addLabeledItem( column, "Lower: " , _controlLowerLimitView, DEFAULT_VALUE_COLOR );
		addLabeledItem( column, "Upper: ", _controlUpperLimitView, DEFAULT_VALUE_COLOR );
	}
	
	
	/** add the controls limit view */
	private void addVariableView() {
		final Box box = new Box( BoxLayout.Y_AXIS );
		add( box );
		
		final Box buttonBox = new Box( BoxLayout.X_AXIS );
		box.add( buttonBox );
		buttonBox.add( _designButton );
		buttonBox.add( _controlButton );
		buttonBox.add( _customButton );
		_designButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				if ( _parameter != null ) {
					if ( _designButton.isSelected() ) {
						_parameter.setActiveSource( LiveParameter.DESIGN_SOURCE );
					}
				}
			}
		});
		_controlButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				if ( _parameter != null ) {
					if ( _controlButton.isSelected() ) {
						_parameter.setActiveSource( LiveParameter.CONTROL_SOURCE );						
					}
				}
			}
		});
		_customButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				if ( _parameter != null ) {
					if ( _customButton.isSelected() ) {
						_parameter.setActiveSource( LiveParameter.CUSTOM_SOURCE );						
					}
				}
			}
		});
		
		final JPanel column = new JPanel();
		box.add( column );
		box.setBorder( BorderFactory.createTitledBorder( "Active Source" ) );
		column.setLayout( new GridLayout( 0, 2 ) );
		addLabeledItem( column, "Initial: ", _variableInitialValueView, DEFAULT_VALUE_COLOR );
		addLabeledItem( column, "Lower: " , _variableLowerLimitView, DEFAULT_VALUE_COLOR );
		addLabeledItem( column, "Upper: ", _variableUpperLimitView, DEFAULT_VALUE_COLOR );
		addLabeledItem( column, "", _variableCheckBox, null );
	}
	
	
	/** 
	 * Get the live parameter. 
	 * @return the parameter which is displayed by this view.
	 */
	public LiveParameter getParameter() {
		return _parameter;
	}
	
	
	/**
	 * Make a numeric field.
	 */
	private JFormattedTextField makeNumericField( final boolean isEditable ) {		
		return makeNumericField( true, false );
	}
	
	
	/**
	 * Make a numeric label.
	 */
	private JFormattedTextField makeNumericField( final boolean isEditable, final boolean isLabel ) {
		final JFormattedTextField field = new JFormattedTextField( _valueFormatter );
		field.setColumns( 10 );
		
		field.setHorizontalAlignment( JTextField.RIGHT );
		field.setEditable( isEditable );
		field.setOpaque( !isLabel );
		field.setMaximumSize( field.getPreferredSize() );
		
		return field;
	}
	
	
	/**
	 * Add the specified componenent to the specified container along with the specified label.
	 */
	private void addLabeledItem( final Container container, final String label, final Component item, final Color itemColor ) {
		container.add( new JLabel( label, RIGHT ) );
		
		final Box itemBox = new Box( BoxLayout.X_AXIS );
		itemBox.add( Box.createHorizontalGlue() );
		if ( itemColor != null ) {
			item.setForeground( itemColor );			
		}
		itemBox.add( item );
		container.add( itemBox );
	}
	
	
	/** Monitor the variable checkbox */
	private void monitorVariableCheckbox() {
		_variableCheckBox.addChangeListener( new ChangeListener() {
			public void stateChanged( final ChangeEvent event ) {
				if ( _parameter != null ) {
					_parameter.setIsVariable( _variableCheckBox.isSelected() );					
				}
			}
		});
	}
	
	
	/** Handle text field events */
	private void handleTextFieldEvents() {
		_variableInitialValueView.addPropertyChangeListener( new PropertyChangeListener() {			
			public void propertyChange( final PropertyChangeEvent event ) {
				if ( event.getPropertyName().equals( "value" ) ) {
					_parameter.setInitialValue( ((Number)_variableInitialValueView.getValue() ).doubleValue() );
				}
			}
		});
		
		_variableLowerLimitView.addPropertyChangeListener( new PropertyChangeListener() {			
			public void propertyChange( final PropertyChangeEvent event ) {
				if ( event.getPropertyName().equals( "value" ) ) {
					_parameter.setLowerLimit( ((Number)_variableLowerLimitView.getValue() ).doubleValue() );
				}
			}
		});
		
		_variableUpperLimitView.addPropertyChangeListener( new PropertyChangeListener() {			
			public void propertyChange( final PropertyChangeEvent event ) {
				if ( event.getPropertyName().equals( "value" ) ) {
					_parameter.setUpperLimit( ((Number)_variableUpperLimitView.getValue() ).doubleValue() );
				}
			}
		});
	}
	
	
	/** Clear the parameter view of all values and disable controls. */
	protected void clear() {
		setEnabled( false );
		_titleView.setText( "No Selection" );
		
		_variableCheckBox.setSelected( false );
		_variableInitialValueView.setText( "" );
		_variableLowerLimitView.setText( "" );
		_variableUpperLimitView.setText( "" );
		_designView.setText( "" );
		_readbackView.setText( "" );
		_controlView.setText( "" );
		_controlLowerLimitView.setText( "" );
		_controlUpperLimitView.setText( "" );
	}
	
	
	/**
	 * Enable this view.
	 * @param shouldEnable indicates whether this view should be enabled
	 */
	public void setEnabled( final boolean shouldEnable ) {
		super.setEnabled( shouldEnable );
		updateViewsEnableStatus();
	}
	
	
	/** Update the enable state of views. */
	protected void updateViewsEnableStatus() {
		final boolean isEnabled = isEnabled();
		final boolean isVariable = _parameter != null && _parameter.isVariable();
		final boolean isCustom = _parameter != null && ( _parameter.getActiveSource() == LiveParameter.CUSTOM_SOURCE );
		
		_designButton.setEnabled( isEnabled );
		_customButton.setEnabled( isEnabled );
		_controlButton.setEnabled( isEnabled );
		
		_variableInitialValueView.setEnabled( isEnabled && isCustom );
		_variableInitialValueView.setOpaque( isEnabled && isCustom );
		_variableLowerLimitView.setEnabled( isEnabled && isVariable && isCustom );
		_variableLowerLimitView.setOpaque( isEnabled && isVariable && isCustom );
		_variableUpperLimitView.setEnabled( isEnabled && isVariable && isCustom );
		_variableUpperLimitView.setOpaque( isEnabled && isVariable && isCustom );
		
		_variableCheckBox.setEnabled( isEnabled );
	}
	
	
	/**
	 * Set the live parameter to display.
	 * @param parameter the parameter to display with this view.
	 */
	public void setParameter( final LiveParameter parameter ) {
		if ( parameter == _parameter )  return;		// nothing to do
		
		if ( _parameter != null ) {
			_parameter.removeLiveParameterListener( this );
		}
		
		_parameter = null;
		
		clear();
		
		_parameter = parameter;
		
		if ( parameter != null ) {
			_titleView.setText( parameter.getName() );
			_designView.setValue( new Double( parameter.getDesignValue() ) );
			parameter.addLiveParameterListener( this );
			setEnabled( true );
		}
	}
	
	
	/**
	 * Handle the event indicating that parameter's control channel connection has changed.
	 * @param parameter the live parameter whose connection has changed.
	 * @param isConnected true if the channel is now connected and false if it is disconnected
	 */
	public void controlConnectionChanged( final LiveParameter parameter, final boolean isConnected ) {
		SwingUtilities.invokeLater( new Runnable() {
			public void run() {
				if ( isConnected ) {
					_controlView.setForeground( DEFAULT_VALUE_COLOR );
					_controlLowerLimitView.setValue( new Double( parameter.getControlLimits()[0] ) );
					_controlUpperLimitView.setValue( new Double( parameter.getControlLimits()[1] ) );
				}
				else {
					_controlView.setForeground( WARNING_COLOR );
				}				
				_controlView.validate();
			}
		});
	}
	
	
	/** 
	 * Handle the event indicating that parameter's readback channel connection has changed.
	 * @param parameter the live parameter whose connection has changed.
	 * @param isConnected true if the channel is now connected and false if it is disconnected
	 */
	public void readbackConnectionChanged( final LiveParameter parameter, final boolean isConnected ) {
		SwingUtilities.invokeLater( new Runnable() {
			public void run() {
				if ( isConnected ) {
					_readbackView.setForeground( DEFAULT_VALUE_COLOR );
				}
				else {
					_readbackView.setForeground( WARNING_COLOR );
				}
				_readbackView.validate();
			}
		});		
	}
	
	
	/** 
	 * Handle the event indicating that parameter's control value has changed.
	 * @param parameter the live parameter whose value has changed.
	 * @param value the new control value of the parameter
	 */
	public void controlValueChanged( final LiveParameter parameter, final double value ) {
		SwingUtilities.invokeLater( new Runnable() {
			public void run() {
				_controlView.setValue( new Double( value ) );
				_controlView.validate();
			}
		});
	}
	
	
	/**
	 * Handle the event indicating that parameter's readback value has changed.
	 * @param parameter the live parameter whose value has changed.
	 * @param value the new readback value of the parameter
	 */
	public void readbackValueChanged( final LiveParameter parameter, final double value ) {
		SwingUtilities.invokeLater( new Runnable() {
			public void run() {
				_readbackView.setValue( new Double( value ) );
				_readbackView.validate();
			}
		});
	}
	
	
	/**
	 * Handle the event in which the parameter's variable status has changed.
	 * @param parameter the live parameter whose value has changed.
	 * @param isVariable indicates whether the parameter is now variable or not
	 */
	public void variableStatusChanged( final LiveParameter parameter, final boolean isVariable ) {
		_variableCheckBox.setSelected( isVariable );
		updateViewsEnableStatus();
	}
	
	
	/**
	 * Handle the event in which the parameter's custom value has changed.
	 * @param parameter the core parameter whose value has changed.
	 * @param value the new custom value
	 */
	public void customValueChanged( final LiveParameter parameter, final double value ) {}
	
	
	/**
	 * Handle the event in which the parameter's custom limits have changed.
	 * @param parameter the core parameter whose limits have changed.
	 * @param limits the new custom limits
	 */
	public void customLimitsChanged( final LiveParameter parameter, final double[] limits ) {}
	
	
	/**
	 * Handle the event in which the parameter's initial variable value has changed.
	 * @param parameter the live parameter whose value has changed.
	 * @param value the new initial variable value
	 */
	public void initialValueChanged( final LiveParameter parameter, final double value ) {
		try {
			_variableInitialValueView.setValue( new Double( value ) );			
		}
		catch ( IllegalStateException exception ) {
		}
		finally {
			validate();			
		}
	}
	
	
	/**
	 * Handle the event in which the parameter's lower variable limit has changed.
	 * @param parameter the live parameter whose value has changed.
	 * @param limit the new lower variable limit
	 */
	public void lowerLimitChanged( final LiveParameter parameter, final double limit ) {
		try {
			_variableLowerLimitView.setValue( new Double( limit ) );			
		}
		catch ( IllegalStateException exception ) {
		}
		finally {
			validate();			
		}
	}
	
	
	/**
	 * Handle the event in which the parameter's upper variable limit has changed.
	 * @param parameter the live parameter whose value has changed.
	 * @param limit the new upper variable limit
	 */
	public void upperLimitChanged( final LiveParameter parameter, final double limit ) {
		try {
			_variableUpperLimitView.setValue( new Double( limit ) );
		}
		catch ( IllegalStateException exception ) {
		}
		finally {
			validate();			
		}
	}
	
	
	/**
	 * Handle the event in which the parameter's variable source has changed.
	 * @param parameter the live parameter whose variable source has changed.
	 * @param source the indicator of the parameter's variable source
	 */
	public void variableSourceChanged( LiveParameter parameter, int source ) {
		switch( source ) {
			case LiveParameter.DESIGN_SOURCE:
				_designButton.setSelected( true );
				break;
			case LiveParameter.CUSTOM_SOURCE:
				_customButton.setSelected( true );
				break;
			case LiveParameter.CONTROL_SOURCE:
				_controlButton.setSelected( true );
				break;
			default:
				break;
		}
		
		updateViewsEnableStatus();
	}
}



