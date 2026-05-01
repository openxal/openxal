/*
 * EmpiricalSimulatorView.java
 *
 * Created on Tue Oct 12 13:27:21 EDT 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.orbitcorrect;

import java.util.logging.*;
import java.io.*;
import java.net.*;
import java.text.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.Dimension;

import xal.tools.apputils.files.*;

/**
 * EmpiricalSimulatorView
 *
 * @author  tap
 * @since Oct 12, 2004
 */
public class EmpiricalSimulatorView extends MappedSimulatorView {
	
    private static final long serialVersionUID = 1L;
    
    protected JFormattedTextField _settleTimeField;
	protected JFormattedTextField _fieldExcursionField;
	protected JFormattedTextField _calibrationTrialsField;
	
	
	/**
	 * Constructor
	 */
	public EmpiricalSimulatorView( final EmpiricalSimulator simulator ) {
		super( simulator );
	}
	
	
	/**
	 * Convenience method for getting the empirical simulator.
	 */
	protected EmpiricalSimulator getEmpiricalSimulator() {
		return (EmpiricalSimulator)_simulator;
	}
	
	
	/** Make the view's content */
	protected void makeContent() {
		super.makeContent();
		
		final int VERTICAL_PADDING = 5;
				
		final Box settleRow = new Box( BoxLayout.X_AXIS );
		add( settleRow );
		settleRow.add( Box.createHorizontalGlue() );
 		settleRow.add( new JLabel( "Beam Settle Time(msec):" ) );
		_settleTimeField = new JFormattedTextField( new DecimalFormat( "0" ) );
		settleRow.add( _settleTimeField );
		_settleTimeField.setHorizontalAlignment( JTextField.RIGHT );
		_settleTimeField.setColumns( 8 );
		_settleTimeField.setMaximumSize( _settleTimeField.getPreferredSize() );
		
		//_settleTimeField.setValue( getEmpiricalSimulator().getBeamSettleTime() );
		_settleTimeField.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				try {
					applySettings();
				}
				catch(Exception exception) {
					exception.printStackTrace();
				}
			}
		});
		add( Box.createVerticalStrut( VERTICAL_PADDING ) );

		final Box fieldRow = new Box( BoxLayout.X_AXIS );
		add( fieldRow );
		fieldRow.add( Box.createHorizontalGlue() );
		fieldRow.add( new JLabel( "Field excursion(%):" ) );
		_fieldExcursionField = new JFormattedTextField( new DecimalFormat( "0.0 %" ) );
		_fieldExcursionField.setColumns( 8 );
		_fieldExcursionField.setHorizontalAlignment( JTextField.RIGHT );
		_fieldExcursionField.setMaximumSize( _fieldExcursionField.getPreferredSize() );
		fieldRow.add( _fieldExcursionField );
		
		_fieldExcursionField.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				try {
					applySettings();
				}
				catch(Exception exception) {
					exception.printStackTrace();
				}
			}
		});
		add( Box.createVerticalStrut( VERTICAL_PADDING ) );
		
		final Box sampleRow = new Box( BoxLayout.X_AXIS );
		add( sampleRow );
		sampleRow.add( Box.createHorizontalGlue() );
		sampleRow.add( new JLabel( "Samples per corrector:" ) );
		_calibrationTrialsField = new JFormattedTextField( new DecimalFormat( "0" ) );
		_calibrationTrialsField.setColumns( 8 );
		_calibrationTrialsField.setHorizontalAlignment( JTextField.RIGHT );
		_calibrationTrialsField.setMaximumSize( _calibrationTrialsField.getPreferredSize() );
		sampleRow.add( _calibrationTrialsField );
		
		//_calibrationTrialsField.setValue( getEmpiricalSimulator().getCorrectorCalibrationTrials() );
		_calibrationTrialsField.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				try {
					applySettings();
				}
				catch(Exception exception) {
					exception.printStackTrace();
				}
			}
		});
		add( Box.createVerticalStrut( VERTICAL_PADDING ) );
		add( Box.createVerticalGlue() );
		
		refreshSettings();
	}
	
	
	/** apply this view's settings to the simulator */
	public void applySettings() {
		getEmpiricalSimulator().setCorrectorCalibrationTrials( ((Number)_calibrationTrialsField.getValue()).intValue() );
		getEmpiricalSimulator().setCorrectorSampleExcursion( ((Number)_fieldExcursionField.getValue()).doubleValue() );
		getEmpiricalSimulator().setBeamSettleTime( ((Number)_settleTimeField.getValue()).longValue() );
		
		refreshSettings();
	}
	
	
	/** refresh this view's settings to reflect its simulator's settings */
	public void refreshSettings() {
		_calibrationTrialsField.setValue( getEmpiricalSimulator().getCorrectorCalibrationTrials() );
		_fieldExcursionField.setValue( getEmpiricalSimulator().getCorrectorSampleExcursion() );
		_settleTimeField.setValue( getEmpiricalSimulator().getBeamSettleTime() );
	}	
}

