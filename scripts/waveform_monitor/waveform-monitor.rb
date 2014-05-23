#!/usr/bin/env jruby
#
#  waveform-monitor.rb
#  Monitor and Display a selected waveform and its spectrum
#
#  Created by Tom Pelaia on 3/3/08.
#  Copyright (c) 2007 SNS. All rights reserved.
#
include Java

import java.awt.Color
import java.io.FileWriter
import java.io.StringWriter
import java.lang.StringBuffer
import java.lang.System
import java.net.URL
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.JOptionPane
import javax.swing.SpinnerNumberModel

java_import 'xal.extension.application.ApplicationAdaptor'
java_import 'xal.extension.application.XalDocument'
java_import 'xal.extension.application.smf.AcceleratorApplication'
java_import 'xal.extension.application.smf.AcceleratorDocument'

java_import 'xal.extension.widgets.apputils.SimpleChartPopupMenu'
java_import 'xal.tools.StringJoiner'
java_import 'xal.extension.widgets.plot.RainbowColorGenerator'
java_import 'xal.extension.widgets.plot.FunctionGraphsJPanel'
java_import 'xal.extension.widgets.plot.BasicGraphData'
java_import 'xal.extension.bricks.WindowReference'
java_import 'xal.tools.statistics.MutableUnivariateStatistics'
java_import 'xal.tools.math.DiscreteFourierTransform'
java_import 'xal.extension.widgets.smf.NodeChannelSelector'
java_import 'xal.smf.impl.BPM'
java_import 'xal.smf.data.XMLDataManager'
java_import 'xal.ca.ConnectionListener'
java_import 'xal.ca.Channel'
java_import 'xal.ca.ChannelFactory'
java_import 'xal.ca.IEventSinkValTime'
java_import 'xal.ca.Monitor'
java_import 'xal.tools.apputils.ImageCaptureManager'
java_import 'xal.tools.xml.XmlDataAdaptor'
java_import 'xal.tools.data.DataAdaptor'
java_import 'xal.tools.data.DataListener'


module Java
	java_import 'java.awt.event.MouseListener'
	java_import 'java.lang.reflect.Array'
	java_import 'java.lang.Class'
	java_import 'java.lang.Double'
	java_import 'java.io.File'
    java_import 'java.util.Date'
	java_import 'java.util.List'
	java_import 'java.lang.Math'
	java_import 'java.lang.String'
	java_import 'java.util.ArrayList'
	java_import 'java.util.Vector'
	java_import 'java.text.DecimalFormat'
	java_import 'javax.swing.ButtonGroup'
	java_import 'javax.swing.Timer'
	java_import 'javax.swing.ListSelectionModel'
	java_import 'javax.swing.DefaultListModel'
end



# model of functions to resolve Java ambiguities
module JavaResolver
	# Construct a SpinnerNumberModel using integer parameters (addresses ambiguous Java constructor)
	def newSpinnerNumberModel( value, minimum, maximum, step )
		constructor = SpinnerNumberModel.java_class.constructor( Java::int, Java::int, Java::int, Java::int )
		return constructor.new_instance( value, minimum, maximum, step )
	end
end

include JavaResolver


class WaveformReader	
	attr_reader :waveformPV
	
	def initialize( pv )
		@waveformPV = pv
		channel = ChannelFactory.defaultFactory().getChannel( pv )
		@monitor = WaveformMonitor.new( channel )
	end
    
    def is_fresh
        return @monitor.is_fresh
    end
    
    def set_is_fresh( fresh_status )
        @monitor.is_fresh = fresh_status
    end
	
	def destroy
		@monitor.destroy
	end
	
	def waveform
		return @monitor.latest_waveform
	end
end



class WaveformMonitor
	include ConnectionListener
	include IEventSinkValTime
	attr_reader :channel
	attr_reader :latest_waveform
    attr_accessor :is_fresh

	def initialize( channel )
		@channel = channel
		@monitor = nil
        @is_fresh = false
        @latest_waveform = nil
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
    
    
    def latest_waveform
        return @latest_waveform
    end
	
    
	def eventValue( record, channel )
		@latest_waveform = Waveform.new( record.doubleArray, record.getTimestamp )	# comment this line for validation
        @is_fresh = true
#		@latest_waveform = Waveform.new( self.validation_waveform, record.getTimestamp )	# uncomment this line for validation
	end
	
    
    # this method is used for testing purposes to validate the spectrum
	def validation_waveform
		frequency = 0.1
		points = []
		1000.times do |index|
			value = Math.sin( 2 * Math::PI * frequency * index )
			points.push value 
		end
		return points
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
	attr_accessor :custom_lower_index
	attr_accessor :custom_upper_index
	attr_accessor :use_custom_range
    attr_reader :lower_index
    attr_reader :upper_index
	
	def initialize
		@custom_lower_index = 0
		@custom_upper_index = 256
        @lower_index = @custom_lower_index
        @upper_index = @custom_upper_index
		@use_custom_range = true;
	end
		
	def positions( waveform )
		if @use_custom_range
			@lower_index = @custom_lower_index
			@upper_index = Java::Math.min( @custom_upper_index, waveform.element_count - 1 )
		else
			@lower_index = 0
			@upper_index = waveform.element_count - 1
		end
        
		if @lower_index >= 0 and @upper_index >= @lower_index
			return waveform.positions[ @lower_index..@upper_index ]
		else
			return Array.new
		end
	end
end



# handle taking a snapshot of the window
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



# handle exporting selected waveforms to a file
class ExportHandler
    include java.awt.event.ActionListener
    
    # class constants
    TIME_FORMATTER = Java::java.text.SimpleDateFormat.new( "EEE, MMM d, yyyy HH:mm:ss" )
    FILENAME_TIME_FORMATTER = Java::java.text.SimpleDateFormat.new( "yyyyMMdd_HHmmss" )
    
    # constructor
    def initialize( controller )
        @controller = controller
        self.make_file_chooser
        export_button = controller.window_reference.getView( "ExportButton" )
		export_button.addActionListener( self )
    end
	
	def actionPerformed( event )
        readers = @controller.waveform_readers
        if readers != nil
            now = Java::java.util.Date.new
            
            output = StringWriter.new
            timestamp = TIME_FORMATTER.format now
            output.write( "# #{timestamp}" )
            output.write( "\n\n" )
            
            readers.each do |reader|
                channelName = reader.waveformPV
                waveform = reader.waveform
                
                if waveform != nil
                    output.write channelName
                    output.write "\t"
                    data_joiner = StringJoiner.new
                    data_joiner.append waveform.positions
                    waveform_string = data_joiner.toString
                    output.write waveform_string
                    output.write "\n\n"
                end
            end
            output.flush
            
            defaultName = "Waveforms_" + FILENAME_TIME_FORMATTER.format( now ) + ".txt"
            file = self.request_output_file defaultName
            writer = FileWriter.new file
            writer.write output.toString
            writer.flush
            writer.close
        else
            JOptionPane.showMessageDialog( @controller.mainWindow, "Warning!\n No Waveform data was captured.\n There is nothing to write.", "No Data to Write", JOptionPane::WARNING_MESSAGE )
        end
	end
    
    
	
	def make_file_chooser
		@file_chooser = JFileChooser.new
		@file_chooser.setFileSelectionMode( JFileChooser::FILES_ONLY )
		@file_chooser.setMultiSelectionEnabled false
	end
    
	
	def request_output_file defaultName
		window = @controller.mainWindow
		file_chooser = @file_chooser
		defaultFile = Java::java.io.File.new( file_chooser.getCurrentDirectory(), defaultName )
		file_chooser.setSelectedFile( defaultFile )
        
		status = file_chooser.showSaveDialog( window )
		if status == JFileChooser::APPROVE_OPTION
			file = file_chooser.getSelectedFile
			if file.exists
				proceed_status = JOptionPane.showOptionDialog( @controller.mainWindow, "Warning, #{file.toString} Exists! \nDo you want to overwrite this file?", "Existing File", JOptionPane::YES_NO_CANCEL_OPTION, JOptionPane::WARNING_MESSAGE, nil, nil, nil )
				if proceed_status == JOptionPane::CLOSED_OPTION or proceed_status == JOptionPane::CANCEL_OPTION
					return nil
                elsif proceed_status == JOptionPane::NO_OPTION
					return self.request_output_file( defaultName )
				end
            else
				return file
			end
        else
			return nil
		end
	end

end



#implement empty handlers for MouseListener events
module MouseHandler	
	include java.awt.event.MouseListener
    
	def mouseClicked event
	end
	
	def mouseEntered event
	end
	
	def mouseExited event
	end
	
	def mousePressed event
	end
		
	def mouseReleased event
	end
end



# handle the selection of waveforms to display
class WaveformSelectionHandler
	include javax.swing.event.ListSelectionListener
	include java.awt.event.ActionListener
	include MouseHandler
	
	def initialize( list, controller )
		@list = list
		@controller = controller
		
		@list_model = DefaultListModel.new
		@list.setModel @list_model
		@list.addListSelectionListener self
		@list.addMouseListener self
		
		window_reference = controller.window_reference
		@pick_channel_button = window_reference.getView( "Add Channel Button" )
		@add_waveform_button = window_reference.getView( "AddWaveformButton" )
		@delete_waveform_button = window_reference.getView( "DeleteWaveformButton" )
		
		@pick_channel_button.addActionListener self
		@add_waveform_button.addActionListener self
		@delete_waveform_button.addActionListener self
	end

	def clearChannelSelector
		@channel_selector = nil;
	end
	
	def loadChannelSelector
		puts "loading accelerator..."
		source = @controller.getSelectedSequence
		if source == nil
			source = @controller.getAccelerator
		end
		nodes = nil
		if source != nil
			nodes = source.getAllNodes true
		else
			nodes = ArrayList.new
		end
		@channel_selector = NodeChannelSelector.getInstanceFromNodes( nodes, @controller.mainWindow, "Pick Waveform Channels" )
	end
	
	def valueChanged event
		if not event.getValueIsAdjusting
			pvs = @list.getSelectedValues
			monitorPVs pvs
		end
	end

	def getChannelPVs
		return @list_model.toArray
	end

	def addChannelPV( channelPV )
		@list_model.addElement channelPV
		@controller.setHasChanges true
	end
	
	def monitorPVs pvs
		old_readers = @controller.waveform_readers
		if old_readers != nil
            old_readers.each { |reader| reader.destroy }
		end
        @controller.waveform_readers = []
		if pvs != nil and pvs.length > 0
            pvs.each { |pv| @controller.waveform_readers.push( WaveformReader.new( pv ) ) }
		end
		@controller.refreshDisplay
	end
	
	def actionPerformed( event )		
		source = event.getSource
		if source == @add_waveform_button
			pv = JOptionPane.showInputDialog( @controller.window_reference.getWindow, "Enter the Waveform PV to add:", "" )
			if pv != nil and pv.length > 0
				@list_model.addElement( pv )
			end
		elsif source == @delete_waveform_button
			selected_index = @list.getSelectedIndex
			if selected_index > -1
				@list_model.removeElementAt( selected_index )
			end
		elsif source == @pick_channel_button
			if @channel_selector == nil then loadChannelSelector end
			channelRefs = @channel_selector.showDialog
			if channelRefs != nil
				channelRefs.each { |channelRef| self.addChannelPV channelRef.channel.channelName }
			end
		end
	end
	
	def mouseClicked event
		if event.getClickCount == 2
			selected_index = @list.getSelectedIndex
			if selected_index > -1
				selected_PV = @list.getSelectedValue
				pv = JOptionPane.showInputDialog( @controller.window_reference.getWindow, "Enter the new PV:", selected_PV )
				@list_model.setElementAt( pv, selected_index )
				monitorPVs [pv]
			end
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
		@controller.refreshDisplayIfNeeded
	end
end



class WaveformDocument < AcceleratorDocument
	include javax.swing.event.ChangeListener
	include java.awt.event.ActionListener
	include DataListener

	field_accessor :mainWindow
	attr_reader :window_reference
	attr_reader :profile_analyzer
	attr_reader :file_chooser
	attr_accessor :waveform_readers

	def initialize
		super	# allows us to access inherited self

		@window_reference = XalDocument.getDefaultWindowReference( "MainWindow", [ self ].to_java )

		@sample_period = 1.0

		@waveform_pv_list = window_reference.getView( "Waveform List" )
		@waveform_pv_list.setSelectionMode( ListSelectionModel::MULTIPLE_INTERVAL_SELECTION )

		@waveform_plot = loadWaveformPlot( window_reference, "WaveformPlot", "Waveform" )
		@waveform_plot.setLegendPosition( FunctionGraphsJPanel::LEGEND_POSITION_ARBITRARY );

		@spectrum_plot = loadSpectrumPlot( window_reference, "SpectrumPlot", "Spectrum" )
		@spectrum_plot.setLegendPosition( FunctionGraphsJPanel::LEGEND_POSITION_ARBITRARY );

		@range_checkbox = @window_reference.getView( "RangeCheckBox" )
		@lower_range_spinner = @window_reference.getView( "LowerRangeSpinner" )
		@upper_range_spinner = @window_reference.getView( "UpperRangeSpinner" )

		@range_checkbox.addActionListener self

		@lower_range_spinner.setModel( newSpinnerNumberModel( 0, 0, 1500, 1 ) )
		@lower_range_spinner.addChangeListener self
		@upper_range_spinner.setModel( newSpinnerNumberModel( 256, 0, 1500, 1 ) )
		@upper_range_spinner.addChangeListener self

		@sample_period_field = @window_reference.getView( "Sample Period Field" )
		@sample_period_field.addActionListener self
		@sample_period_field.setText "#{@sample_period}"

		@waveform_selection_handler = WaveformSelectionHandler.new( @waveform_pv_list, self )

		@waveform_analyzer = WaveformAnalyzer.new

		PlotRefresher.new( self )

		SnapshotHandler.new( window_reference )

		ExportHandler.new( self )

		self.hasChanges = false
	end


	# static initializer since constructor arguments must match inherited Java constructor arguments
	def self.createFrom( location )
		document = WaveformDocument.new

		document.source = location

		if location != nil
			documentAdaptor = XmlDataAdaptor.adaptorForUrl( location, false )
			document.update( documentAdaptor.childAdaptor( document.dataLabel ) )
		end

		document.hasChanges = false

		return document
	end


	def makeMainWindow
		self.mainWindow = @window_reference.getWindow
		self.hasChanges = false
	end


	def saveDocumentAs( location )
		writeDataTo( self, location )
	end


	def dataLabel()
		return "WaveformDocument"
	end


	def update( adaptor )
		# restore the accelerator/sequence if any
		if adaptor.hasAttribute( "acceleratorPath" )
			acceleratorPath = adaptor.stringValue( "acceleratorPath" )
			accelerator = applySelectedAcceleratorWithDefaultPath( acceleratorPath )

			if ( accelerator != nil && adaptor.hasAttribute( "sequence" ) )
				sequenceID = adaptor.stringValue( "sequence" )
				setSelectedSequence( accelerator.findSequence( sequenceID ) )
			end
		end

		# read the model data
		modelAdaptor = adaptor.childAdaptor( "model" )
		channelAdaptors = modelAdaptor.childAdaptors( "waveform-channel" )
		channelAdaptors.each do |channelAdaptor|
			channelPV = channelAdaptor.stringValue( "PV" )
			@waveform_selection_handler.addChannelPV channelPV
		end
	end


	def write( adaptor )
		adaptor.setValue( "version", "1.0.0" )
		adaptor.setValue( "date", Java::Date.new.toString )

		# write the model data
		modelAdaptor = adaptor.createChild( "model" )
		channelPVs = @waveform_selection_handler.getChannelPVs
		channelPVs.each do |channelPV|
			channelAdaptor = modelAdaptor.createChild( "waveform-channel" )
			channelAdaptor.setValue( "PV", channelPV )
		end

		# write the accelerator/sequence if any
		if self.getAccelerator != nil
			adaptor.setValue( "acceleratorPath", self.getAcceleratorFilePath )

			sequence = self.getSelectedSequence
			if sequence != nil
				adaptor.setValue( "sequence", sequence.getId )
			end
		end
	end


	def acceleratorChanged
		@waveform_selection_handler.clearChannelSelector
		self.hasChanges = true
	end


	def selectedSequenceChanged
		@waveform_selection_handler.clearChannelSelector
		self.hasChanges = true
	end
	
	
	def loadWaveformPlot( window_reference, viewID, title )
		plot = window_reference.getView( viewID )
		plot.setGridLineColor( Color::LIGHT_GRAY )
		plot.setAxisNameX( "time (s)" )
		plot.setAxisNameY( "Waveform" )
		plot.setName( title )
		plot.setLegendVisible( false )
	        
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
	
	
	def loadSpectrumPlot( window_reference, viewID, title )
		plot = window_reference.getView( viewID )
		plot.setGridLineColor( Color::LIGHT_GRAY )
		plot.setAxisNameX( "fequency (Hz)" )
		plot.setAxisNameY( "Spectrum Amplitude" )
		plot.setName( title )
		plot.setLegendVisible( false )
	        
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

		
	def actionPerformed( event )
		if event.source == @range_checkbox
			use_custom_range = @range_checkbox.isSelected
			@lower_range_spinner.setEnabled( use_custom_range )
			@upper_range_spinner.setEnabled( use_custom_range )
			@waveform_analyzer.use_custom_range = use_custom_range
			apply_range
			refreshDisplay
		elsif event.source == @sample_period_field
			@sample_period = Java::java.lang.Double.parseDouble @sample_period_field.getText
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
		@waveform_analyzer.custom_lower_index = lower_index
		@waveform_analyzer.custom_upper_index = upper_index
	end
	
	
    # refresh the display only if there is fresh data to display
	def refreshDisplayIfNeeded
        readers = @waveform_readers
		if readers != nil
            fresh_reader = readers.find { |reader| reader.is_fresh }    # get the first reader that was found to be fresh or nil if none
            if ( fresh_reader != nil )     # check that at least one reader is fresh otherwise there is nothing to do
                readers.each do |reader|
                    waveform = reader.waveform
                    if waveform != nil
                        refreshDisplay
                        return
                    end
                end
            end
		end
	end
	
	
    # force a refresh of the display
	def refreshDisplay
        readers = @waveform_readers
        waveforms = []
		if readers != nil
            readers.each do |reader|
                reader.set_is_fresh false   #mark that we have already processed this reader
                pv = reader.waveformPV
                waveform = reader.waveform
                if waveform != nil
                    waveforms.push( { :waveform => waveform, :pv => pv } )
                end
            end
            if ( waveforms.length == 1 )
                waveform = waveforms[0][:waveform]
                pv = waveforms[0][:pv]
                plotWaveform( waveform, pv )
                plotSpectrum( waveform, pv )
            else
                plotWaveforms( waveforms )
                plotSpectra( waveforms )
            end
        end
	end
	
	
	def plotWaveform( waveform, pv )
		plot = @waveform_plot
		plot.removeAllGraphData
		plot.setLegendVisible( false );
		
		title = "#{pv} Waveform at #{waveform.timestamp.toString}"
		plot.setName( title )
		
		if waveform != nil
            graph_data = graphDataForWaveform( waveform, pv, Color::BLUE )
			plot.addGraphData( graph_data )
		end
	end
	
	
	def plotSpectrum( waveform, pv )
		plot = @spectrum_plot
		plot.removeAllGraphData
		plot.setLegendVisible( false );
		
		title = "#{pv} Spectrum at #{waveform.timestamp.toString}"
		plot.setName( title )
		
		if waveform != nil
            graph_data = spectraGraphDataForWaveform( waveform, pv, Color::BLUE )
			plot.addGraphData( graph_data )
		end
	end
    
    
    def spectraGraphDataForWaveform( waveform, pv, color )
        graph_data = BasicGraphData.new
        graph_data.setDrawLinesOn( true )
        graph_data.setGraphColor( color )
        
        all_values = @waveform_analyzer.positions( waveform )
        total_count = all_values.size
        if total_count >= 2
            num_points = 2 * ( total_count / 2 )
            points = []
            num_points.times do |index|
                points.push all_values[index]
            end
            period = num_points * @sample_period
            transform = DiscreteFourierTransform.new( points.to_java(:double), period )
            spectrum = transform.getSpectrum
            spectrum.each_with_index { |fourier_coefficient, index|
                frequency = transform.getFrequency( index )
                graph_data.addPoint( frequency, fourier_coefficient.modulus )
            }
        end
        
        return graph_data
    end
	
	
	def plotWaveforms( waveforms )
		plot = @waveform_plot
		plot.removeAllGraphData
		
		plot.setLegendKeyString( "Legend" );
		plot.setLegendBackground( Color::LIGHT_GRAY );
		plot.setLegendColor( Color::BLACK );
		plot.setLegendVisible( true );

		title = "Multiple Waveforms at #{Java::java.util.Date.new.toString}"
		plot.setName( title )
		
		if waveforms != nil and waveforms.length > 1
            color_index = 0.0
            waveforms.each do |waveform_item|
                waveform = waveform_item[:waveform]
                pv = waveform_item[:pv]
                
                color = RainbowColorGenerator.getColorGenerator.getColor( color_index )
                graph_data = graphDataForWaveform( waveform, pv, color )
                graph_data.setGraphProperty( plot.getLegendKeyString, pv );
                
                plot.addGraphData( graph_data )
                
                color_index += 1.0 / ( waveforms.length - 1 )
            end
		end
	end
    
    
    def graphDataForWaveform( waveform, pv, color )
        graph_data = BasicGraphData.new
        graph_data.setDrawLinesOn( true )
        graph_data.setGraphColor( color )

        all_values = @waveform_analyzer.positions( waveform )
        total_count = all_values.size
        if total_count >= 2
            num_points = 2 * ( total_count / 2 )
            points = []
            num_points.times do |index|
                points.push all_values[index]
            end
            sample_time = @sample_period * @waveform_analyzer.lower_index
            points.each { |value|
                sample_time += @sample_period
                graph_data.addPoint( sample_time, value )
            }
        end
        
        return graph_data
    end
	
	
	def plotSpectra( waveforms )
		plot = @spectrum_plot
		plot.removeAllGraphData
        
		plot.setLegendKeyString( "Legend" );
		plot.setLegendBackground( Color::LIGHT_GRAY );
		plot.setLegendColor( Color::BLACK );
		plot.setLegendVisible( true );
		
		title = "Multiple Spectra at #{Java::java.util.Date.new.toString}"
		plot.setName( title )
        
		if waveforms != nil and waveforms.length > 1
            color_index = 0.0
            waveforms.each do |waveform_item|
                waveform = waveform_item[:waveform]
                pv = waveform_item[:pv]
                
                color = RainbowColorGenerator.getColorGenerator.getColor( color_index )
                graph_data = spectraGraphDataForWaveform( waveform, pv, color )
                graph_data.setGraphProperty( plot.getLegendKeyString, pv );
                
                plot.addGraphData( graph_data )
                
                color_index += 1.0 / ( waveforms.length - 1 )
            end
		end
	end
end



class Main < ApplicationAdaptor
	def initialize
		super	# allows us to access inherited self

		# locate the enclosing folder and get the bricks file within it
		folder = File.expand_path File.dirname( __FILE__ )
		puts "script folder: #{folder}"

		self.setResourcesParentDirectoryWithPath folder
	end


	def readableDocumentTypes()
		return [ "wfmon" ].to_java(Java::String)
	end


	def writableDocumentTypes()
		return self.readableDocumentTypes
	end


	def newEmptyDocument()
		return self.newDocument( nil )
	end


	def newDocument( location )
		return WaveformDocument.createFrom( location )
	end


	def applicationName
		return "Waveform Monitor";
	end
end



# main entry point
AcceleratorApplication.launch( Main.new )
