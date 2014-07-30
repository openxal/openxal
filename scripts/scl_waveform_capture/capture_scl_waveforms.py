#!/usr/bin/env jython
# This script allows the user to capture correlated waveforms for a selected SCL cavity.

import sys
import string

from jarray import *
from java.io import *
from java.lang import *
from java.util import *
from java.text import *
from javax.swing import *
from javax.swing.event import *
from javax.swing.table import *
from java.awt import *
from java.awt.event import *
from java.awt.datatransfer import *

from xal.extension.bricks import *
from xal.tools.apputils import *
from xal.extension.widgets.plot import *
from xal.extension.widgets.apputils import SimpleChartPopupMenu
from xal.tools.text import *
from xal.ca import *
from xal.ca.correlator import *
from xal.tools.correlator import *
from xal.smf import *
from xal.smf.data import *
from xal.smf.impl  import *


# java constants
true = (1==1)
false = not true
null = None


	
# application controller
class ControlApp:
	def __init__( self, window_reference ):
		self.window_reference = window_reference
		accelerator = XMLDataManager.loadDefaultAccelerator()
		sequence = accelerator.findSequence( "SCL" )
		cavities = sequence.getNodesOfType( SCLCavity.s_strType, true )
		cavity_keys = Vector()
		for cavity in cavities:
			cavity_keys.add( String( cavity.getId() ).substring( 10 ) )
		self.cavity_list = window_reference.getView( "CavityList" )
		self.cavity_list.setListData( cavity_keys )
		self.waveformIDs = [ "Field_WfA", "Fwd_WfA", "Rfl_WfA", "Field_WfP", "Fwd_WfP", "Rfl_WfP" ]
		self.pvs = []
		self.waveform_plot = window_reference.getView( "WaveformPlot" )
		self.last_correlation = null
		self.cavityID = null
		self.file_chooser = null
		self.waveforms = null
	
	def capture_waveforms( self ):
		self.last_correlation = null
		self.waveforms = null
		cavity_key = self.cavity_list.getSelectedValue()
		self.cavityID = cavity_key
		window = self.window_reference.getWindow()
		if cavity_key == null:
			JOptionPane.showMessageDialog( window, "You must first select a cavity.", "Error", JOptionPane.ERROR_MESSAGE )
			return
		print "capturing the current waveforms for:  ", cavity_key
		correlator = self.make_correlator( cavity_key )
		correlation = correlator.fetchCorrelationWithTimeout( 5.0 )
		correlator.dispose()
		self.last_correlation = correlation
		self.waveforms = self.get_waveforms( correlation )
		if correlation != null:
			self.save_waveforms()
		else:
			JOptionPane.showMessageDialog( window, "No correlation found for the cavity waveforms.", "Warning", JOptionPane.WARNING_MESSAGE )
			return
	
	def get_waveforms( self, correlation ):
		waveforms = []
		for pv in self.pvs:
			if correlation != null:
				record = correlation.getRecord( pv )
				waveforms.append( record.doubleArray() )
		return waveforms
	
	def plot_waveforms( self ):
		correlation = self.last_correlation
		if correlation == null:
			JOptionPane.showMessageDialog( self.window_reference.getWindow(), "No waveforms to plot.", "Warning", JOptionPane.WARNING_MESSAGE )
			return
		self.waveform_plot.removeAllGraphData()
		self.waveform_plot.setName( "SCL Cavity " + self.cavityID + " Waveforms" )
		self.waveform_plot.setAxisNameX( "Sample" )
		self.waveform_plot.setAxisNameY( "Amplitude" )
		series = Vector();
		waveforms = self.waveforms
		windex = 0
		for waveform in waveforms:
			data_name = self.waveformIDs[windex]
			graphData = BasicGraphData();
			graphData.setGraphColor( IncrementalColors.getColor( windex ) );
			graphData.setGraphProperty( self.waveform_plot.getLegendKeyString(), data_name );
			x = 0
			for value in waveform:
				graphData.addPoint( x, value )
				x += 1
			series.add( graphData );
			windex += 1
		self.waveform_plot.addGraphData( series );
	
	def save_waveforms( self ):
		if self.last_correlation == null:
			JOptionPane.showMessageDialog( self.window_reference.getWindow(), "No waveforms to save.", "Warning", JOptionPane.WARNING_MESSAGE )
			return
		file = self.request_file()
		if file == null:  return
		writer = FileWriter( file )
		waveforms = self.waveforms
		writer.write( "# SCL Cavity " + self.cavityID + " Waveforms\n" )
		date_format = SimpleDateFormat( "EEE, MMM d, yyyy HH:mm:ss z" )
		writer.write( "# Saved:  " +  date_format.format( Date() ) + "\n" )
		writer.write( "# Event Time:  " +  date_format.format( self.last_correlation.meanDate() ) + "\n" )
		writer.write( "#\n" )
		writer.write( "Sample" )
		for waveformID in self.waveformIDs:
			writer.write( "\t" + waveformID )
		writer.write( "\n" )
		num_samples = self.get_max_waveform_samples( waveforms )
		for sample in range( num_samples ):
			writer.write( String.valueOf( sample ) )
			for waveform in waveforms:
				writer.write( "\t" )
				if sample < len( waveform ):
					writer.write( String.valueOf( waveform[sample] ) )
			writer.write( "\n" )
		writer.flush()
		writer.close()

	def request_file( self ):
		window = self.window_reference.getWindow()
		if self.last_correlation == null:
			JOptionPane.showMessageDialog( window, "Error: There is no data to save.", "Error", JOptionPane.ERROR_MESSAGE )
			return
		if self.file_chooser == null:
			self.file_chooser = JFileChooser()
		file_chooser = self.file_chooser
		current_directory = file_chooser.getCurrentDirectory()
		file_chooser.setSelectedFile( File( current_directory, "SCL_Cavity_" + self.cavityID + ".waveforms" ) )
		status = file_chooser.showSaveDialog( window )
		if status == JFileChooser.APPROVE_OPTION:
			file = file_chooser.getSelectedFile()
			if file.exists():
				confirmation = JOptionPane.showConfirmDialog( window, "The selected file exists.\nOverwrite the file?" )
				if confirmation == JOptionPane.YES_OPTION:  return file
				elif confirmation == JOptionPane.NO_OPTION:  return self.request_file()
				else: return null
			return file
		return null
		
	def make_correlator( self, cavity_key ):
		self.pvs = []
		correlator = ChannelCorrelator( 0.001 )
		
		# add the channels to monitor
		for waveformID in self.waveformIDs:
			self.append_channel( correlator, cavity_key, waveformID )
		
		return correlator
	
	def append_channel( self, correlator, cavity_key, waveformID ):
		pv = self.getPV( cavity_key, waveformID )
		print "Appending channel: " + pv
		self.pvs.append( pv )
		correlator.addChannel( pv )
	
	def getPV( self, cavity_key, waveformID ):
		return "SCL_LLRF:FCM" + cavity_key + ":" + waveformID
	
	def get_max_waveform_samples( self, waveforms ):
		max_samples = 0
		for waveform in waveforms:
			count = len( waveform )
			if count > max_samples:  max_samples = count
		return max_samples
	

# capture waveform handler
class CaptureButtonHandler( ActionListener ):
	def __init__( self, window_reference, button, main_controller ):
		self.window_reference = window_reference
		self.button = button
		self.main_controller = main_controller
		
	def actionPerformed( self, event ):
		self.main_controller.capture_waveforms()

	

# save waveform data handler
class SaveButtonHandler( ActionListener ):
	def __init__( self, window_reference, button, main_controller ):
		self.window_reference = window_reference
		self.button = button
		self.main_controller = main_controller
		
	def actionPerformed( self, event ):
		self.main_controller.save_waveforms()

	

# save waveform data handler
class PlotButtonHandler( ActionListener ):
	def __init__( self, window_reference, button, main_controller ):
		self.window_reference = window_reference
		self.button = button
		self.main_controller = main_controller
		
	def actionPerformed( self, event ):
		self.main_controller.plot_waveforms()



# load the user interface
def load_user_interface():
	# locate the enclosing folder and get the bricks file within it
	folder = File( sys.argv[0] ).getParentFile()
	url = File( folder, "gui.bricks" ).toURI().toURL()
	
	# generate a window reference resource and pass the desired constructor arguments
	window_reference = WindowReference( url, "MainWindow", [] )
		
	main_controller = ControlApp( window_reference )
	
	capture_button = window_reference.getView( "CaptureButton" )
	capture_button.addActionListener( CaptureButtonHandler( window_reference, capture_button, main_controller ) )
	
	plot_button = window_reference.getView( "PlotButton" )
	plot_button.addActionListener( PlotButtonHandler( window_reference, plot_button, main_controller ) )
	
	save_button = window_reference.getView( "SaveButton" )
	save_button.addActionListener( SaveButtonHandler( window_reference, save_button, main_controller ) )
	
	window = window_reference.getWindow()
	window.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE )
	window.setVisible( true )
	
	return window_reference


# ---------------- main procedure -----------------------
window_ref = load_user_interface()


