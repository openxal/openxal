#!/usr/bin/env jruby
#
#  ring-tune-monitor.rb
#  Monitor and display the ring orbit using the online model to propagate the orbit beyond the BPMs
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

java_import 'xal.extension.orbit.CoordinateMap'
java_import 'xal.tools.FreshProcessor'
java_import 'xal.extension.widgets.apputils.SimpleChartPopupMenu'
java_import 'xal.extension.widgets.plot.FunctionGraphsJPanel'
java_import 'xal.extension.widgets.plot.BasicGraphData'
java_import 'xal.extension.bricks.WindowReference'
java_import 'xal.smf.impl.BPM'
java_import 'xal.smf.data.XMLDataManager'
java_import 'xal.ca.ConnectionListener'
java_import 'xal.ca.Channel'
java_import 'xal.ca.Monitor'
java_import 'xal.tools.apputils.ImageCaptureManager'
java_import 'xal.sim.scenario.Scenario'


module Java
	java_import 'java.lang.reflect.Array'
	java_import 'java.lang.Class'
	java_import 'java.lang.Double'
	java_import 'java.io.File'
	java_import 'java.util.List'
	java_import 'java.lang.Math'
	java_import 'java.util.ArrayList'
	java_import 'java.util.Vector'
	java_import 'java.text.DecimalFormat'
	java_import 'javax.swing.ButtonGroup'
	java_import 'javax.swing.SwingUtilities'
end



class BeamPositionMonitor
	include ConnectionListener
	include Java::xal.ca.IEventSinkValTime
	attr_reader :channel
	attr_reader :latest_position

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
		@latest_position = record.doubleValue
	end
end



class BPMReader
	attr_reader :bpm
	attr_reader :location
	
	def initialize( bpm, location )
		@bpm = bpm
		@location = location
		
		x_channel = bpm.getChannel( BPM::X_AVG_HANDLE )
		@x_monitor = BeamPositionMonitor.new( x_channel )
		y_channel = bpm.getChannel( BPM::Y_AVG_HANDLE )
		@y_monitor = BeamPositionMonitor.new( y_channel )
	end
	
	def destroy
		@x_monitor.destroy
		@y_monitor.destroy
	end
	
	def x
		return @x_monitor.latest_position
	end
	
	def y
		return @y_monitor.latest_position
	end
end



class Target
	attr_reader :node
	attr_reader :location
	
	def initialize( node, location, readerA, readerB, sequence )
		@node = node
		@location = location
		@readerA = readerA
		@readerB = readerB
		@coordinate_map = CoordinateMap.new( @node, @readerA.bpm, @readerB.bpm, nil, sequence )
	end
	
	def x
		return @coordinate_map.getX( @readerA.x, @readerB.x )
	end
	
	def y
		return @coordinate_map.getY( @readerA.y, @readerB.y )
	end

	def setScenario( scenario )
		@coordinate_map.setScenario( scenario )
	end
	
	def setTrajectory( trajectory )
		@coordinate_map.setTrajectory( trajectory )
	end
end



class BeamPosition
	attr_reader :location
	attr_reader :x
	attr_reader :y
	
	def initialize( location, x, y )
		@location = location
		@x = x
		@y = y
	end
end



class OrbitModel
	attr_accessor :bpm_readers
	attr_reader :measured_positions
	attr_reader :fit_positions
	attr_reader :sequence
	attr_reader :hasData
	
	def initialize		
		@sequence = XMLDataManager.loadDefaultAccelerator().findSequence( "Ring" )
		
		@hasData = false
		
		populate_bpms
		populate_targets
		zero_chicane_reference
		
		setup_model
	end
	
	
	def zero_chicane_reference
		chicanes = []
		chicanes.push( @sequence.getNodeWithId( "Ring_Mag:DH_A10" ) )
		chicanes.push( @sequence.getNodeWithId( "Ring_Mag:DH_A11" ) )
		chicanes.push( @sequence.getNodeWithId( "Ring_Mag:DH_A12" ) )
		chicanes.push( @sequence.getNodeWithId( "Ring_Mag:DH_A13" ) )
		for chicane in chicanes
			chicane.getMagBucket.setBendAngle( 0.0 )
		end
	end
	
		
	def populate_bpms
		@bpms = @sequence.getNodesOfType( BPM.s_strType, true )
		@bpm_readers = @bpms.collect do |bpm|
			location = @sequence.getPosition bpm
			BPMReader.new( bpm, location )
		end		
	end
	
	
	def populate_targets
		types = ["magnet", "marker"].to_java( :String )
		qualifier = Java::xal.smf.impl.qualify.QualifierFactory.qualifierWithStatusAndTypes( true, types )
		target_nodes = @sequence.getNodesWithQualifier qualifier
		puts "targets: #{target_nodes}"
		
		bpm_reader_map = Hash.new
		@bpm_readers.each do |reader|
			bpm_reader_map[reader.bpm] = reader
		end
		
		bpms = ArrayList.new( @bpms )
		sequence_length = @sequence.length
		@targets = target_nodes.collect do |target_node|
			location = @sequence.getPosition( target_node )
			@sequence.sortNodesByProximity( bpms, target_node )
			selections = Array.new
			for bpm in bpms
				selections.push bpm
				if selections.length >= 2; break; end
			end
			readers = selections.collect { |selection| bpm_reader_map[selection] }
			Target.new( target_node, location, readers[0], readers[1], @sequence )
		end
	end
	
	
	def setup_model
		@sequence.getNodesOfType( "emag", true ).each { |magnet| magnet.setUseFieldReadback( false ) }
		tracker = Java::xal.model.alg.TransferMapTracker.new
		@probe = Java::xal.sim.scenario.ProbeFactory.getTransferMapProbe( @sequence, tracker )
		@probe.reset
		@scenario = Scenario.newScenarioFor( @sequence )
		@scenario.setSynchronizationMode( Scenario::SYNC_MODE_RF_DESIGN )
		@scenario.setProbe( @probe )
	end
	
	
	def refresh_orbit
		@probe.reset
		@scenario.resync
		@scenario.run
		trajectory = @scenario.getTrajectory
		
		@measured_positions = @bpm_readers.collect do |bpm_reader|
			location = bpm_reader.location
			BeamPosition.new( location, bpm_reader.x, bpm_reader.y )
		end
		
		@fit_positions = @targets.collect do |target|
			location = target.location
			#target.setScenario( @scenario )
			target.setTrajectory( trajectory )
			BeamPosition.new( location, target.x, target.y )
		end
		
		@hasData = true
	end
end



class ControlApp	
	attr_reader :window_reference
	attr_reader :main_window
	attr_reader :model
	
	def initialize window_reference
		@window_reference = window_reference
		@main_window = window_reference.getWindow
		
		@model = OrbitModel.new
		
		@orbit_plot = load_orbit_plot( window_reference, "OrbitPlot", "Orbit" )
		
		@synopticView = Java::xal.extension.widgets.smf.FunctionGraphsXALSynopticAdaptor.assignXALSynopticViewTo( @orbit_plot, @model.sequence )
		synopticBox = window_reference.getView( "SynopticContainer" )
		synopticBox.add @synopticView
		
		SnapshotHandler.new( window_reference )
		RefreshHandler.new( self )
		
		RingOriginHandler.new( self )
		@origin_shift = 0.0
	end
	
	def load_orbit_plot( window_reference, viewID, title )
		plot = window_reference.getView( viewID )
		plot.setGridLineColor( Color::LIGHT_GRAY )
		plot.setAxisNameX( "Location (m)" )
		plot.setAxisNameY( "Beam Position (mm)" )
		plot.setName( title )
		plot.setLegendVisible( true )
		
		SimpleChartPopupMenu.addPopupMenuTo plot
				
		return plot
	end
	
	
	def setOriginPercentShift( percentShift )
		@origin_shift = percentShift * model.sequence.length / 100.0
		@synopticView.setWrapShift @origin_shift
		
		if @model.hasData
			self.plotOrbit
		end
	end
	
	
	def displayWindow
		@main_window.setDefaultCloseOperation( JFrame::EXIT_ON_CLOSE )
		@main_window.setVisible( true )
	end
	
	
	def refreshOrbit
		@model.refresh_orbit
		self.plotOrbit
	end
	
	
	def plotOrbit
		xMeasuredGraphData = new_point_data( "X BPM Orbit", Color::BLUE.darker() );
		yMeasuredGraphData = new_point_data( "Y BPM Orbit", Color::GREEN.darker() );
		xFitGraphData = new_line_data( "X Fit Orbit", Color::BLUE.darker() );
		yFitGraphData = new_line_data( "Y Fit Orbit", Color::GREEN.darker() );
		
		measured_positions = @model.measured_positions
		fit_positions = @model.fit_positions
		
		sequence_length = @model.sequence.getLength
		max_location = @model.sequence.getLength - @origin_shift
		
		measured_positions.each do |beam_position|
			location = beam_position.location
			if location > max_location
				location -= sequence_length
			end
			xMeasuredGraphData.addPoint( location, beam_position.x )
			yMeasuredGraphData.addPoint( location, beam_position.y )
		end
		
		fit_positions.each do |beam_position|
			location = beam_position.location
			if location > max_location
				location -= sequence_length
			end
			xFitGraphData.addPoint( location, beam_position.x )
			yFitGraphData.addPoint( location, beam_position.y )
		end
		
		graphData = Vector.new
		graphData.add xMeasuredGraphData
		graphData.add yMeasuredGraphData
		graphData.add xFitGraphData
		graphData.add yFitGraphData
		@orbit_plot.setGraphData graphData
	end
	
	
	def new_point_data( label, pointColor )
		graphData = BasicGraphData.new
		graphData.setDrawLinesOn( false )
		graphData.setDrawPointsOn( true )
		graphData.setGraphColor( pointColor )
		legendKey = @orbit_plot.getLegendKeyString
		graphData.setGraphProperty( legendKey, label )
		
		return graphData
	end
	
	
	def new_line_data( label, lineColor )
		graphData = BasicGraphData.new
		graphData.setDrawLinesOn( true )
		graphData.setDrawPointsOn( false )
		graphData.setGraphColor( lineColor )
		legendKey = @orbit_plot.getLegendKeyString
		graphData.setGraphProperty( legendKey, label )
		
		return graphData
	end
end



class RefreshHandler
	include java.awt.event.ActionListener
	
	def initialize( controller )
		@controller = controller
		snapshot_button = controller.window_reference.getView( "RefreshButton" )
		snapshot_button.addActionListener( self )
	end
	
	def actionPerformed( event )
		@controller.refreshOrbit;
	end
end



class RingOriginHandler
	include javax.swing.event.ChangeListener
	include java.lang.Runnable
	
	def initialize( controller )
		@processor = FreshProcessor.new
		@controller = controller
		@slider = controller.window_reference.getView( "RingOriginSlider" )
		@slider.addChangeListener( self )
	end
	
	def stateChanged( event )
		@processor.post self
	end
	
	def run
		if SwingUtilities.isEventDispatchThread
			@controller.setOriginPercentShift( @slider.getValue )
		else
			SwingUtilities.invokeAndWait self
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
		now = Time.new
		filename = "orbit-viewer_" + now.strftime( "%Y%m%dT%H%M%S" ) 
		ImageCaptureManager.defaultManager().saveSnapshot( @window.getContentPane, filename );
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
