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
import java.util.HashMap

java_import 'xal.extension.application.ApplicationAdaptor'
java_import 'xal.extension.application.XalDocument'
java_import 'xal.extension.application.smf.AcceleratorApplication'
java_import 'xal.extension.application.smf.AcceleratorDocument'

java_import 'xal.tools.StringJoiner'
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

module XAL
	include_package "xal.smf.data"
	include_package "xal.sim.scenario"
	include_package "xal.smf.impl"
	include_package "xal.ca"
	include_package "xal.extension.widgets.swing"
end



class MachineStateRecord < HashMap
	def initialize( node, channel )
		super()
		put( "node", node )
		put( "channel", channel )
		put( "live_value", Double::NaN )
		put( "saved_value", Double::NaN )
	end


	def node
		return self["node"]
	end

	def channel
		return self["channel"]
	end

	def to_s
		return "node: #{self.node.getId}, channel: #{self.channel.channelName}"
	end
end



class MachineState
	attr_reader :records
	attr_reader :accelerator

	def initialize
		@records = ArrayList.new
	end

	def set_accelerator(accelerator)
		@records = ArrayList.new
		@accelerator = accelerator
		puts "setting the machine state accelerator..."

		magnets = accelerator.getAllNodesOfType( XAL::Electromagnet.s_strType )
		append_records( magnets, [XAL::MagnetMainSupply::FIELD_SET_HANDLE] )

		cavities = accelerator.getAllNodesOfType( XAL::RfCavity.s_strType )
		append_records( cavities, [XAL::RfCavity::CAV_AMP_SET_HANDLE, XAL::RfCavity::CAV_PHASE_SET_HANDLE] )

		@records.each { |record| puts "#{record}" }
	end

	def append_records( nodes, handles )
		nodes.each do |node|
			handles.each do |handle|
				channel = node.findChannel( handle )
				if channel != nil
					record = MachineStateRecord.new( node, channel )
					@records.add record
				end
			end
		end
	end
end



class SaveRestoreDocument < AcceleratorDocument
	include java.awt.event.ActionListener
	include DataListener

	field_accessor :mainWindow
	attr_reader :window_reference

	def initialize
		super	# allows us to access inherited self

		@window_reference = XalDocument.getDefaultWindowReference( "MainWindow", [ self ].to_java )

		@channel_records_table = @window_reference.getView( "ChannelRecordsTable" )
		@restore_button = window_reference.getView( "RestoreButton" )
		@refresh_button = window_reference.getView( "RefreshButton" )

		@restore_button.addActionListener( self )
		@refresh_button.addActionListener( self )

		@machine_state = MachineState.new

		@channel_records_table_model = XAL::KeyValueFilteredTableModel.new()
		@channel_records_table_model.setKeyPaths( "node.id", "channel.channelName" )
		@channel_records_table.setModel( @channel_records_table_model )

		self.hasChanges = false
	end


	# static initializer since constructor arguments must match inherited Java constructor arguments
	def self.createFrom( location )
		document = SaveRestoreDocument.new

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
		return "SaveRestoreDocument"
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
		@machine_state.set_accelerator self.accelerator
		@channel_records_table_model.setRecords( @machine_state.records )
		puts "setting the document accelerator..."
		self.hasChanges = true
	end


	def selectedSequenceChanged
		self.hasChanges = true
	end

		
	def actionPerformed( event )
		if event.source == @refresh_button
			puts "Refresh the data"
		elsif event.source == @restore_button
			puts "Restore the data"
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
		return [ "mstate" ].to_java(Java::String)
	end


	def writableDocumentTypes()
		return self.readableDocumentTypes
	end


	def newEmptyDocument()
		return self.newDocument( nil )
	end


	def newDocument( location )
		return SaveRestoreDocument.createFrom( location )
	end


	def applicationName
		return "Simple Save and Restore";
	end
end



# main entry point
AcceleratorApplication.launch( Main.new )
