#!/usr/bin/env jruby
#
#  save-restore.rb
#  Simple Save and Restore as an alternative to SCORE if the database is not available
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
	def initialize( node, setpoint_channel )
		super()
		put( "node", node )
		put( "setpoint_channel", setpoint_channel )
		put( "live_setpoint", Double::NaN )
		put( "saved_setpoint", Double::NaN )
	end


	def node
		return self["node"]
	end

	def setpoint_channel
		return self["setpoint_channel"]
	end

	def live_setpoint
		return self["live_setpoint"]
	end

	def set_live_setpoint value
		self["live_setpoint"] = value
	end

	def saved_setpoint
		return self["saved_setpoint"]
	end

	def set_saved_setpoint value
		self["saved_setpoint"] = value
	end

	def to_s
		return "node: #{self.node.getId}, setpoint_channel: #{self.setpoint_channel.channelName}"
	end
end



class MachineState
	include XAL::PutListener

	attr_reader :records
	attr_reader :accelerator
	attr_accessor :comment

	def initialize
		@records = ArrayList.new
		@comment = ""
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
				setpoint_channel = node.findChannel( handle )
				if setpoint_channel != nil
					record = MachineStateRecord.new( node, setpoint_channel )
					@records.add record
				end
			end
		end
	end

	def refresh
		setpoint_channels = ArrayList.new
		@records.each { |record| setpoint_channels.add( record.setpoint_channel ) }
		request = XAL::BatchGetValueRequest.new( setpoint_channels )
		request.submitAndWait( 5.0 )
		@records.each do |record|
			setpoint_channel_record = request.getRecord( record.setpoint_channel )
			value = Double::NaN
			if setpoint_channel_record != nil
				value = setpoint_channel_record.doubleValue
			end
			record.set_live_setpoint value
			puts "#{value}"
		end
	end

	def restore( records )
		records.each do |record|
			setpoint_channel_record = record.setpoint_channel
			saved_setpoint = record.saved_setpoint
			if !Double.isNaN( saved_setpoint )
				puts "restoring record: #{record}"
				record.setpoint_channel.putValCallback( saved_setpoint, self )
			end
		end
		XAL::Channel.flushIO
	end

	def putCompleted(setpoint_channel)
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

		record_filter_field = window_reference.getView( "RecordFilterField" )

		@restore_button.addActionListener( self )
		@refresh_button.addActionListener( self )

		@machine_state = MachineState.new

		@channel_records_table_model = XAL::KeyValueFilteredTableModel.new()
		@channel_records_table_model.setInputFilterComponent record_filter_field
		@channel_records_table_model.setKeyPaths( "node.id", "setpoint_channel.channelName", "live_setpoint", "saved_setpoint" )
		@channel_records_table_model.setColumnClassForKeyPaths( Double.class, "live_setpoint", "saved_setpoint" )
		@channel_records_table_model.setColumnName( "node.id", "Node" )
		@channel_records_table_model.setColumnName( "setpoint_channel.channelName", "Setpoint Channel" )
		@channel_records_table_model.setColumnName( "live_setpoint", "Live Setpoint" )
		@channel_records_table_model.setColumnName( "saved_setpoint", "Saved Setpoint" )
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
		# get the version
		version = adaptor.stringValue("version")

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
		model_adaptor = adaptor.childAdaptor( "MachineState" )
		record_adaptors = model_adaptor.childAdaptors( "record" )
		values_by_pv = Hash.new
		record_adaptors.each do |record_adaptor|
			setpoint_pv = nil
			setpoint = nil
			if record_adaptor.hasAttribute("setpoint")
				# this is the new style
				setpoint_pv = record_adaptor.stringValue( "setpoint_pv" )
				setpoint = record_adaptor.doubleValue( "setpoint" )
			else
				# this is the old style
				setpoint_pv = record_adaptor.stringValue( "channel" )
				setpoint = record_adaptor.doubleValue( "value" )
			end
			if setpoint_pv != nil
				values_by_pv[ setpoint_pv ] = setpoint
			end
		end

		@machine_state.records.each do |record|
			setpoint = values_by_pv[ record.setpoint_channel.channelName ]
			if setpoint != nil
				record.set_saved_setpoint( setpoint )
			end
		end
	end


	def write( adaptor )
		adaptor.setValue( "version", "2.0.0" )
		adaptor.setValue( "date", Java::Date.new.toString )

		# write the model state
		model_adaptor = adaptor.createChild( "MachineState" )
		model_adaptor.setValue( "comment", @machine_state.comment )

		# write the model records
		@machine_state.records.each do |record|
			if !Double.isNaN( record.live_setpoint )
				record_adaptor = model_adaptor.createChild( "record" )
				record_adaptor.setValue( "setpoint_pv", record.setpoint_channel.channelName )
				record_adaptor.setValue( "setpoint", record.live_setpoint )
			end
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
			@machine_state.refresh
			@channel_records_table_model.fireTableDataChanged
		elsif event.source == @restore_button
			puts "Restore the data"
			selected_rows = @channel_records_table.getSelectedRows
			selected_records = []
			selected_rows.each do |row|
				model_row = @channel_records_table.convertRowIndexToModel(row)
				record = @channel_records_table_model.getRecordAtRow( model_row )
				selected_records.push record
			end
			@machine_state.restore( selected_records )
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
