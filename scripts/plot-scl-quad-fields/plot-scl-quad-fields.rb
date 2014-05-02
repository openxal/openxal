#!/usr/bin/env jruby
#
#  scl-quad-fields.rb
#  Monitor and Display the SCL quadrupole fields
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
import java.lang.Runnable
import java.net.URL
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.JOptionPane

java_import 'xal.extension.widgets.apputils.SimpleChartPopupMenu'
java_import 'xal.extension.widgets.plot.RainbowColorGenerator'
java_import 'xal.extension.widgets.plot.FunctionGraphsJPanel'
java_import 'xal.extension.widgets.plot.BasicGraphData'
java_import 'xal.extension.bricks.WindowReference'
java_import 'xal.smf.data.XMLDataManager'
java_import 'xal.ca.ConnectionListener'
java_import 'xal.ca.Channel'
java_import 'xal.ca.ChannelFactory'
java_import 'xal.ca.IEventSinkValTime'
java_import 'xal.ca.Monitor'
java_import 'xal.tools.apputils.ImageCaptureManager'

module Java
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
	java_import 'javax.swing.ListSelectionModel'
	java_import 'javax.swing.DefaultListModel'
end


module XAL
	include_package "xal.model.probe"
	include_package "xal.model.alg"
	include_package "xal.sim.scenario"
	include_package "xal.smf"
	include_package "xal.smf.impl"
	include_package "xal.ca"
	include_package "xal.tools.dispatch"
end



class FieldMonitor
	include XAL::IEventSinkValTime
	include XAL::ConnectionListener

	attr_reader :magnet
	attr_reader :last_record

	def initialize( magnet, delegate )
		@magnet = magnet
		@delegate = delegate

		@last_record = nil

		channel = magnet.findChannel( XAL::Electromagnet::FIELD_RB_HANDLE )
		@channel = channel

		channel.addConnectionListener( self )

		#puts "#{channel.channelName}"
	end


	def start
		@channel.requestConnection()
	end


	def setDelegate( delegate )
		@delegate = delegate
	end


	def connectionMade( channel )
		puts "#{channel.channelName} connection made..."
		@monitor = channel.addMonitorValTime( self, XAL::Monitor::VALUE )
	end


	def connectionDropped( channel )
	end


	def eventValue( record, channel )
		@last_record = record;
		#puts "Monitor event: #{record}"

		delegate = @delegate
		if delegate != nil
			delegate.fieldChanged( self, record )
		end
	end
end



class ControlApp
	include javax.swing.event.ChangeListener
	include java.awt.event.ActionListener
	include java.lang.Runnable
	
	attr_reader :window_reference
	attr_reader :main_window

	def initialize window_reference
		@window_reference = window_reference

		@main_window = window_reference.getWindow
		@field_plot = window_reference.getView( "FieldPlot" )

		SnapshotHandler.new( window_reference )

		self.fetchMagnets
	end


	def fetchMagnets
		@willRefresh = false

		# load the optics for the Ring sequence
		accelerator = XMLDataManager.loadDefaultAccelerator
		if accelerator == nil
			puts "Default accelerator could not be loaded. Exiting..."
			exit 0
		end

		sequenceID = "SCL"
		sequence = accelerator.findSequence( sequenceID )
		if sequence == nil
			puts "No sequence matching #{sequenceID} could be found."
			exit 0
		end
		@sequence = sequence

		quads = sequence.getNodesOfType( XAL::Quadrupole.s_strType, true )
		@monitors = []
		quads.each do |magnet|
			monitor = FieldMonitor.new( magnet, self )
			@monitors.push( monitor )
		end
		@monitors.each { |monitor| monitor.start }
		Channel.flushIO

		#puts "Quadrupoles:"
		#quads.each { |magnet| puts "\t#{magnet.getId}, #{magnet.type}" }
	end


	def fieldChanged( monitor, record )
		if not @willRefresh
			@willRefresh = true
			nextRunTime = Java::Date.new( Java::Date.new.getTime + 250 )
			XAL::DispatchQueue.getMainQueue.dispatchAfter( nextRunTime, self )	# dispatched after 250 msec
		end
	end


	def run()
		@willRefresh = false
		self.plotFields()
	end

	
	def loadFieldPlot( window_reference, viewID )
		plot = window_reference.getView( viewID )
		plot.setGridLineColor( Color::LIGHT_GRAY )
		plot.setAxisNameX( "Beamline Position (meters)" )
		plot.setAxisNameY( "Field Gradient (T/m)" )
		plot.setLegendVisible( false )

		SimpleChartPopupMenu.addPopupMenuTo plot
		return plot
	end

	
	def displayWindow
		@main_window.setDefaultCloseOperation( JFrame::EXIT_ON_CLOSE )
		@main_window.setVisible( true )
	end

	
	def plotFields
		plot = @field_plot
		plot.removeAllGraphData
		plot.setLegendVisible( false );

		graph_data = BasicGraphData.new
		graph_data.setDrawLinesOn( true )
		graph_data.setGraphColor( Color::BLUE )
		@monitors.each do |monitor|
			record = monitor.last_record
			if record != nil
				position = @sequence.getPosition( monitor.magnet )
				field = record.doubleValue
				graph_data.addPoint( position, field )
			end
		end
		plot.addGraphData( graph_data )
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
