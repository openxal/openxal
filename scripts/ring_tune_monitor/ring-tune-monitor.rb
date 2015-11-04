#!/usr/bin/env jruby
#
#  ring-tune-monitor.rb
#  Monitor and Display the horizontal and vertical ring tune spectra
#
#  Created by Tom Pelaia on 1/23/08.
#  Copyright (c) 2007 SNS. All rights reserved.
#
include Java

import java.awt.Color
import java.io.FileWriter
import java.lang.StringBuffer
import java.lang.System
import java.net.URL
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.JOptionPane
import javax.swing.SpinnerNumberModel

java_import 'xal.extension.widgets.apputils.SimpleChartPopupMenu'
java_import 'xal.extension.widgets.plot.FunctionGraphsJPanel'
java_import 'xal.extension.widgets.plot.BasicGraphData'
java_import 'xal.extension.bricks.WindowReference'
java_import 'xal.tools.statistics.MutableUnivariateStatistics'
java_import 'xal.tools.math.DiscreteFourierTransform'
java_import 'xal.smf.impl.BPM'
java_import 'xal.smf.data.XMLDataManager'
java_import 'xal.smf.TimingCenter'
java_import 'xal.ca.ConnectionListener'
java_import 'xal.ca.Channel'
java_import 'xal.ca.IEventSinkValTime'
java_import 'xal.ca.Monitor'
java_import 'xal.tools.apputils.ImageCaptureManager'
java_import 'xal.extension.fit.DampedSinusoidFit'


module Java
	java_import 'java.awt.Toolkit'
	java_import 'java.lang.reflect.Array'
	java_import 'java.lang.Class'
	java_import 'java.lang.Double'
	java_import 'java.io.File'
	java_import 'java.util.Date'
	java_import 'java.util.List'
	java_import 'java.lang.Math'
	java_import 'java.util.ArrayList'
	java_import 'java.util.Vector'
	java_import 'java.text.DecimalFormat'
	java_import 'javax.swing.ButtonGroup'
	java_import 'javax.swing.Timer'
end


TUNE_FORMAT = DecimalFormat.new( "0.00000" )

# extend DecimalFormat to address ambiguity in format()
class DecimalFormat
	java_alias :format_double, :format, [Java::double]
end



class BPMReader	
	attr_reader :bpm
	
	def initialize( bpm )
		@bpm = bpm
		x_channel = bpm.getChannel( BPM::X_TBT_HANDLE )
		@x_monitor = WaveformMonitor.new( x_channel )
		y_channel = bpm.getChannel( BPM::Y_TBT_HANDLE )
		@y_monitor = WaveformMonitor.new( y_channel )
	end
	
	def destroy
		@x_monitor.destroy
		@y_monitor.destroy
	end
	
	def x_waveform
		return @x_monitor.latest_waveform
	end
	
	def y_waveform
		return @y_monitor.latest_waveform
	end
end



class WaveformMonitor
	include ConnectionListener
	include IEventSinkValTime
	attr_reader :channel
	attr_reader :latest_waveform

	def initialize( channel )
		@channel = channel
		@monitor = nil
		channel.addConnectionListener( self )
		channel.requestConnection
		Channel::flushIO
		puts "Requested connection for channel: #{channel.channelName}"
	end
	
	def destroy
		@channel.removeConnectionListener( self )
		monitor = @monitor
		if monitor != nil
			monitor.clear
			Channel::flushIO
		end
	end
	
	def connectionMade( channel )
		if @monitor == nil
			@monitor = channel.addMonitorValTime( self, Monitor::VALUE )
			Channel::flushIO
		end
	end
	
	def connectionDropped( channel )
	end
	
	def eventValue( record, channel )
		@latest_waveform = Waveform.new( record.doubleArray, record.getTimestamp )
	end
end



class Waveform
	attr_reader :positions
	attr_reader :timestamp
	
	def initialize( positions, timestamp )
		@positions = positions
		@timestamp = timestamp
	end
	
	def element_count
		return @positions.length
	end
end



class WaveformAnalyzer
	attr_accessor :lower_index
	attr_accessor :upper_index
	attr_accessor :use_range
	
	def initialize
		@accelerator = XMLDataManager.loadDefaultAccelerator()

		@lower_index = 0
		@upper_index = 256
		@use_range = true;
	end
	
	
	# fetch the stored turns and return 1 if it cannot be fetched
	def storedTurns
		storedTurnsChannel = @accelerator.getTimingCenter.findChannel( TimingCenter::RING_STORED_TURNS_HANDLE )
		if storedTurnsChannel != nil
			storedTurnsChannel.connectAndWait( 2.0 )
			return storedTurnsChannel.isConnected ? storedTurnsChannel.getValDbl : 1.0
		else
			return 1.0
		end
	end
	
	
	def bpms
		ring = @accelerator.findSequence( "Ring" )
		return ring.getNodesOfType( BPM.s_strType, true )
	end
	
	
	def positions( waveform )
		if @use_range
			lower_index = @lower_index
			upper_index = [ @upper_index, waveform.element_count - 1 ].min
		else
			lower_index = 0
			upper_index = waveform.element_count - 1
		end
		if lower_index >= 0 and upper_index >= lower_index
			return waveform.positions[ lower_index..upper_index ]
		else
			return Array.new
		end
	end
end



class WaveformMerger
	def merge( analyzer, waveforms )
		if waveforms == nil; return nil; end

		# if just one waveform, the waveform consists of the its positions tagged by timestamp
		if waveforms.length == 1
			raw_waveform = waveforms[0]
			if raw_waveform != nil
				raw_positions = analyzer.positions( raw_waveform )
				positions = raw_positions.collect { |position| position	}	# convert Java array to Ruby array
				waveform = Waveform.new( positions, raw_waveform.timestamp )
				return waveform
			else
				return nil
			end
		elsif waveforms.length > 1
			# for more than one waveform, merge them into one waveform constructively
			merged_positions = []
			waveforms.each do |waveform|
				if waveform != nil
					# get the waveform's positions and remove the offset
					raw_positions = analyzer.positions( waveform )
					centered_positions = self.center_positions( raw_positions )
					
					if merged_positions.length == 0
						# this is oour first waveform so its positions are just that of the waveform's
						base_norm = norm centered_positions
						if base_norm > 0.0
							merged_positions = centered_positions.collect { |position| position }
						else
							merged_positions = []
						end
					else
						# merge the new centered positions into the existing merged positions
						merged_positions = merge_arrays( merged_positions, centered_positions )
					end
				end
			end
			if merged_positions.length > 0
				merged_waveform = Waveform.new( merged_positions, Date.new )
				return merged_waveform
			else
				return nil
			end
		else
			return nil
		end
	end
	

	# Merge the new array into the base array by adding the new array and the base array aligning them to the same quadrant to avoid cancellation.
	# All BPM waveforms should share a common frequency and damping rate.
	# The set of all waveforms with the same frequency and damping rate and centered at zero form a 2D (amplitude and phase) plane
	# passing through zero in the space of all possible waveforms (dimension of the waveform length).
	# We want to sum the waveforms on the surface of constant frequency avoiding which maximizes the signal to noise ratio.
	# We only care about frequency here and not phase, amplitude or damping (offset has mostly been removed by centering).
	def merge_arrays( base_array, new_array )
		merged_array = []
		if base_array != nil
			base_norm = norm base_array
			new_norm = norm new_array
			if new_norm > 0.0
				b_dot_n = scalar_product( base_array, new_array )
				if b_dot_n != 0
					n2_minus_b2 = new_norm * new_norm - base_norm * base_norm
					coef = b_dot_n > 0 ? 1 : -1
					ws = ( n2_minus_b2 + coef * Math.sqrt( n2_minus_b2 * n2_minus_b2 + 4 * b_dot_n * b_dot_n ) ) / ( 2 * b_dot_n )
					w1 = Math.sqrt( 1 / ( 1 + ws * ws ) )
					w2 = ws * w1
					base_array.each_with_index { |base_item, index| merged_array.push( base_item + coef * new_array[index] ) }
				else
					return base_array
				end
			else
				return base_array
			end
		end
		return merged_array
	end
	

	# compute the norm of the positions vector: sqrt( sum(p_i * p_i) )
	def norm positions
		product = scalar_product( positions, positions )
		return product > 0.0 ? Math.sqrt( product ) : 0.0
	end
	

	# compute the scalar product between the two arrays as if they were vectors
	def scalar_product( array1, array2 )
		if array1 == nil or array2 == nil; return 0.0; end
		
		if array1.length == 0 or array2.length == 0; return 0.0; end
				
		array_less = array1
		array_more = array2
		if array1.length > array2.length
			array_less = array2
			array_more = array1
		end
		sum = 0.0
		array_less.each_with_index do |value1, index|
			if value1 == nil; return 0.0; end
			value2 = array_more[index]
			sum += value1 * value2
		end
		return sum
	end
	

	# shift the waveform signals to remove the offset so the signal is centered about zero
	def center_positions raw_positions
		sum = 0.0
		raw_positions.each { |position| sum += position }
		count = raw_positions.length
		if count > 0
			offset = sum / count
			centered_positions = raw_positions.collect { |position| position - offset }
			return centered_positions
		else
			return raw_positions
		end
	end
end



class ControlApp
	include javax.swing.event.ChangeListener
	include java.awt.event.ActionListener
	
	attr_reader :main_window
	attr_reader :file_chooser
	attr_accessor :bpm_readers
	
	def initialize window_reference
		@waveform_analyzer = WaveformAnalyzer.new	# main model
		@tune_stats = [MutableUnivariateStatistics.new, MutableUnivariateStatistics.new]	# horizontal and vertical tune statistics
		
		@window_reference = window_reference	# main view container
		
		@main_window = window_reference.getWindow
		
		@bpm_select_list = @window_reference.getView( "BPM Selection List" )
		@bpm_select_list.setSelectionMode( Java::javax.swing.ListSelectionModel::MULTIPLE_INTERVAL_SELECTION )
		
		@spectrum_plot = loadSpectrumPlot( window_reference, "SpectrumPlot", "Spectrum" )
		
		BPMSelectListener.new( @bpm_select_list, self )
		@bpm_select_list.setListData( Vector.new( @waveform_analyzer.bpms ) )

		@horizontal_tune_field = window_reference.getView( "Horizontal Tune Field" )
		@vertical_tune_field = window_reference.getView( "Vertical Tune Field" )
		
		@clearTuneAverageButton = window_reference.getView( "ClearAverage" )
		@clearTuneAverageButton.addActionListener self
		@tuneAveragingCheckbox = window_reference.getView( "TuneAveragingCheckbox" )
		@tuneAveragingCheckbox.addActionListener self
		@averaging_on = @tuneAveragingCheckbox.isSelected
		
		@fastFittingCheckbox = window_reference.getView( "FastFittingCheckbox" )
		@fastFittingCheckbox.addActionListener self
		@fastFitting = @fastFittingCheckbox.isSelected
		
		@updateTunesButton = window_reference.getView( "UpdateTunesButton" )
		@updateTunesButton.addActionListener self
		@liveTunesCheckbox = window_reference.getView( "LiveTunesCheckbox" )
		@liveTunesCheckbox.addActionListener self
		
		@copytunesbutton = window_reference.getView( "CopyTunesButton" )
		@copytunesbutton.addActionListener self
		
		@range_checkbox = @window_reference.getView( "RangeCheckBox" )
		@range_checkbox.addActionListener self
		
		@lower_range_spinner = @window_reference.getView( "LowerRangeSpinner" )
		@upper_range_spinner = @window_reference.getView( "UpperRangeSpinner" )
		self.configureTurnsRange
		@lower_range_spinner.addChangeListener self
		@upper_range_spinner.addChangeListener self
		
		PlotRefresher.new( self )
		
		self.apply_range
		
		SnapshotHandler.new( window_reference )
	end
	
	
	def clearTuneStats
		@tune_stats.each { |stats| stats.clear }
	end
	
	
	def loadSpectrumPlot( window_reference, viewID, title )
		plot = window_reference.getView( viewID )
		plot.setGridLineColor( Color::LIGHT_GRAY )
		plot.setAxisNameX( "Fractional Tune" )
		plot.setAxisNameY( "Spectrum Amplitude" )
		plot.setName( title )
		plot.setLegendVisible( true )
	        
        plot.setDraggingHorLinesGraphMode( true )
        plot.setDraggingVerLinesGraphMode( true )
		
		plot.addHorizontalLine( 0.0 )
		plot.addVerticalLine( 0.0 )
		plot.addVerticalLine( 0.0 )
		plot.addVerticalLine( 1.0 )
		plot.addVerticalLine( 1.0 )
		
		SimpleChartPopupMenu.addPopupMenuTo plot
		return plot
	end
	
	
	def configureTurnsRange
		# need to disambiguate the constructors to pick the one that takes four integers
		spinner_model_constructor = SpinnerNumberModel.java_class.constructor( Java::int, Java::int, Java::int, Java::int )
		
		@lower_range_spinner.setModel( spinner_model_constructor.new_instance( 0, 0, 1500, 1 ) )
		@upper_range_spinner.setModel( spinner_model_constructor.new_instance( 256, 0, 1500, 1 ) )
		@upper_range_spinner.setValue( @waveform_analyzer.storedTurns - 1.0 )	# subtract 1 since the waveform is zero based
	end
	
	
	def actionPerformed( event )
		event_source = event.source
		if event_source == @range_checkbox
			use_range = @range_checkbox.isSelected
			@lower_range_spinner.setEnabled( use_range )
			@upper_range_spinner.setEnabled( use_range )
			@waveform_analyzer.use_range = use_range
			apply_range
			refreshDisplay
		elsif event_source == @tuneAveragingCheckbox
			@averaging_on = @tuneAveragingCheckbox.isSelected
			self.clearTuneStats
			@clearTuneAverageButton.setEnabled @averaging_on
		elsif event_source == @clearTuneAverageButton
			self.clearTuneStats
		elsif event_source == @updateTunesButton
			self.post_tunes
		elsif event_source == @fastFittingCheckbox
			@fastFitting = @fastFittingCheckbox.isSelected
		elsif event_source == @liveTunesCheckbox
			@updateTunesButton.setEnabled !@liveTunesCheckbox.isSelected
		elsif event_source == @copytunesbutton
			tune_data = Java::java.awt.datatransfer.StringSelection.new "#{@horizontal_tune_field.getText}, #{@vertical_tune_field.getText}"
			Toolkit.getDefaultToolkit.getSystemClipboard.setContents( tune_data, tune_data )
		end
	end
	
	
	def stateChanged( event )
		if ( @range_checkbox.isSelected )
			apply_range
			self.refreshDisplay
		end
	end
	
	
	def apply_range
		lower_index = @lower_range_spinner.getModel.getNumber.to_i
		upper_index = @upper_range_spinner.getModel.getNumber.to_i
		if ( upper_index < lower_index )
			upper_index = lower_index
			@upper_range_spinner.getModel.setValue( upper_index.to_i )
		end
		@waveform_analyzer.lower_index = lower_index
		@waveform_analyzer.upper_index = upper_index
		
		use_range = @range_checkbox.isSelected
		
		canSolve = !use_range || upper_index > 5
		@updateTunesButton.setEnabled( canSolve )
		@liveTunesCheckbox.setEnabled( canSolve );
		if !canSolve; @liveTunesCheckbox.setSelected( false ); end
	end
	
	
	def displayWindow
		@main_window.setDefaultCloseOperation( JFrame::EXIT_ON_CLOSE )
		@main_window.setVisible( true )
	end
	
	
	def refreshDisplay
		waveforms = get_waveforms
		
		plotSpectra waveforms
		if @liveTunesCheckbox.isSelected; postTunes( waveforms ); end
	end
	
	
	def get_waveforms
		if @bpm_readers != nil
			x_waveforms = []
			y_waveforms = []
			@bpm_readers.each do |reader|
				x_waveforms.push( reader.x_waveform )
				y_waveforms.push( reader.y_waveform )
			end
			merger = WaveformMerger.new
			x_waveform = merger.merge( @waveform_analyzer, x_waveforms )
			y_waveform = merger.merge( @waveform_analyzer, y_waveforms )
			waveforms = { :x => x_waveform, :y => y_waveform }
			return waveforms
		else
			return nil
		end
	end
	
	
	def post_tunes
		postTunes self.get_waveforms
	end
	
	
	def postTunes waveforms
		if waveforms != nil
			postTune( waveforms[:x], @tune_stats[0], @horizontal_tune_field )
			postTune( waveforms[:y], @tune_stats[1], @vertical_tune_field )
		end
	end
	
	
	def postTune( waveform, stats, field )
		if waveform != nil
			positions = @waveform_analyzer.positions( waveform )
			damped_sinusoid = DampedSinusoidFit.new( positions.to_java :double )
			tune = 0.0
			if ( @fastFitting )
				tune = damped_sinusoid.getInitialFrequency
			else
				damped_sinusoid.solveWithNoise 0.5
				tune = damped_sinusoid.getFrequency
			end
			if ( @averaging_on )
				stats.addSample tune
				tune = stats.mean
			end
			field.setText( TUNE_FORMAT.format_double( tune ) )
		end
	end
	
	
	def plotSpectra waveforms
		if waveforms != nil
			has_update = false
				
			x_waveform = waveforms[:x]
			if x_waveform != nil
				x_timestamp = x_waveform.timestamp
				if x_timestamp != @last_x_waveform_timestamp
					@last_x_waveform_timestamp = x_timestamp
					has_update = true
				end
			end
				
			y_waveform = waveforms[:y]
			if y_waveform != nil
				y_timestamp = y_waveform.timestamp
				if y_timestamp != @last_y_waveform_timestamp
					@last_y_waveform_timestamp = y_timestamp
					has_update = true
				end
			end
			
			if has_update
				@spectrum_plot.setName self.bpm_summary
				
				@spectrum_plot.removeAllGraphData
				plotSpectrum( @spectrum_plot, "Horizontal Spectrum", Color::RED, x_waveform )
				plotSpectrum( @spectrum_plot, "Vertical Spectrum", Color::GREEN.darker(), y_waveform )
			end
		end
	end
	
	
	def bpm_summary
		readers = @bpm_readers
		bpm_count = readers != nil ? readers.length : 0
		case bpm_count
		when 0
			return "Spectrum"
		when 1
			return readers[0].bpm.getId
		else
			return readers[0].bpm.getId + " ... " + readers[bpm_count-1].bpm.getId
		end
	end
	
	
	def plotSpectrum( plot, title, color, waveform )
		if waveform != nil
			graph_data = BasicGraphData.new
			graph_data.setDrawLinesOn( true )
			graph_data.setGraphColor( color )
			graph_data.setGraphProperty( plot.getLegendKeyString(), title )
			
			all_values = @waveform_analyzer.positions( waveform )
			total_count = all_values.size
			if total_count >= 2
				num_points = 2 * ( total_count / 2 )
				points = Java::Array.newInstance( Java::Double::TYPE, num_points )
				0.upto( num_points - 1 ) do |index|
					Java::Array.setDouble( points, index, all_values[index] )
				end
				period = num_points
				transform = DiscreteFourierTransform.new( points, period )
				spectrum = transform.getSpectrum
				spectrum.each_with_index { |fourier_coefficient, index|
					frequency = transform.getFrequency( index )
					graph_data.addPoint( frequency, fourier_coefficient.modulus )
				}
			end
			plot.addGraphData( graph_data )
		end
	end
end



class SnapshotHandler
	include java.awt.event.ActionListener
	
	def initialize( window_reference )
		@window = window_reference.getWindow()
		snapshot_button = window_reference.getView( "SnapshotButton" )
		snapshot_button.addActionListener( self )
	end
	
	def actionPerformed( event )
		ImageCaptureManager.defaultManager().saveSnapshot( @window.getContentPane );
	end
end



class BPMSelectListener
	include javax.swing.event.ListSelectionListener
	
	def initialize( list, controller )
		@list = list
		@controller = controller
		
		@list.addListSelectionListener self
	end
	
	def valueChanged event
		if not event.getValueIsAdjusting
			bpms = @list.getSelectedValues
			old_bpm_readers = @controller.bpm_readers
			if old_bpm_readers != nil
				old_bpm_readers.each { |old_reader| old_reader.destroy }
			end
			readers = bpms.collect { |bpm| BPMReader.new( bpm ) }
			@controller.bpm_readers = readers
			@controller.refreshDisplay
		end
	end
end



class PlotRefresher
	include java.awt.event.ActionListener
	
	def initialize( controller )
		@controller = controller
		Java::Timer.new( 100, self ).start
	end
	
	def actionPerformed( event )
		@controller.refreshDisplay
	end
end


# load the user interface
def load_user_interface()
	# locate the enclosing folder and get the bricks file within it
	folder = File.dirname( $0 )
	gui_path = File.join( folder, "gui.bricks" )
	url = Java::File.new( gui_path ).toURI.toURL
	
	# generate a window reference resource and pass the desired constructor arguments
	window_reference = WindowReference::getDefaultInstance( url, "MainWindow" )
	
	main_controller = ControlApp.new window_reference
	main_controller.displayWindow
end


load_user_interface
