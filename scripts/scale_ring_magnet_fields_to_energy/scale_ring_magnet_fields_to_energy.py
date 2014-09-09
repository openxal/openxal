#!/usr/bin/env jython
# This script scales the Ring magnet fields from the initial energy to the target energy specified

import sys

from jarray import *
from java.lang import *
from java.util import *
from java.text import *
from javax.swing import *
from javax.swing.table import *
from java.awt import *
from java.awt.event import *

from xal.ca import *
from xal.smf import *
from xal.smf.impl  import *
from xal.smf.data import *
from xal.smf.impl.qualify import *
from xal.model.probe import *
from xal.model.alg import *
from xal.sim.scenario import ProbeFactory


# java constants
true = (1==1)
false = not true
null = None


# okay button handler
class EnergyScaleHandler(ActionListener):
	def __init__( self, dialog ):
		self.dialog = dialog
		
	def actionPerformed( self, event ):
		self.dialog.set_okay_status( true )
		self.dialog.setVisible( false )
		return


# dialog for getting the initial and target energies
class EnergyScaleDialog(JDialog):
	def __init__( self, initial_energy ):
		self.setTitle( "Energy Inputs" )
		self.setModal( true )
		self.okay_status = false
		
		format = DecimalFormat( "0.0" )
		num_columns = 10
		self.initial_energy_field = self.get_energy_field( initial_energy )
		self.target_energy_field = self.get_energy_field( initial_energy )
		
		self.make_content_view()
		self.pack()
	
	def set_okay_status( self, status ):
		self.okay_status = status
		
	def get_okay_status( self ):
		return self.okay_status
		
	def get_energy_field( self, energy ):
		field = JFormattedTextField( DecimalFormat( "0.0" ) )
		field.setColumns( 10 )
		field.setHorizontalAlignment( JTextField.RIGHT )
		field.setMaximumSize( field.getPreferredSize() )
		field.setValue( energy )
		return field
	
	def get_initial_energy( self ):
		return self.get_energy( self.initial_energy_field )
		
	def get_target_energy( self ):
		return self.get_energy( self.target_energy_field )
	
	def get_energy( self, field ):
		return 1.0e6 * field.getValue()
		
	def make_content_view( self ):
		view = Box( BoxLayout.Y_AXIS )
		self.getContentPane().add( view )
		view.add( self.make_field_row( "Initial Kinetic Energy(MeV):  ", self.initial_energy_field ) )
		view.add( self.make_field_row( "Target Kinetic Energy(MeV):  ", self.target_energy_field ) )
		view.add( self.make_button_row() )
		return
	
	def make_field_row( self, label, field ):
		row = Box( BoxLayout.X_AXIS )
		row.add( Box.createHorizontalStrut( 10 ) )
		row.add( JLabel( label ) )
		row.add( Box.createHorizontalStrut( 10 ) )
		row.add( field )
		return row
	
	def make_button_row( self ):
		row = Box( BoxLayout.X_AXIS )
		row.setBorder( BorderFactory.createEtchedBorder() )
		row.add( Box.createHorizontalGlue() )
		okayButton = JButton( "Okay" )
		row.add( okayButton )
		okayButton.addActionListener( EnergyScaleHandler( self ) )
		return row


# Magnet Record
class MagnetRecord(ConnectionListener, IEventSinkValTime):
	def __init__( self, magnet ):
		self.magnet = magnet
		self.monitor_field()
		self.field = None
	
	def get_magnet( self ):
		return self.magnet
	
	def get_field( self ):
		return self.field
	
	def monitor_field( self ):
		channel = self.magnet.getChannel( MagnetMainSupply.FIELD_SET_HANDLE )
		channel.addConnectionListener( self )
		channel.requestConnection()
	
	def connectionMade( self, channel ):
		channel.addMonitorValTime( self, Monitor.VALUE )
		
	def connectionDropped( self, channel ):
		return
	
	def eventValue( self, record, channel ):
		self.field = record.doubleValue()
		

# calculate beta-gamma for the specified energy and mass
def get_beta_gamma( mass, kinetic_energy ):
	return Math.sqrt( Math.pow( ( mass + kinetic_energy ) / mass, 2.0 ) - 1.0 )


# handler of the main window events
class WindowHandler(WindowAdapter):
	def windowClosed(self, event):
		sys.exit(0)


# Magnet table model
class MagnetTableModel(AbstractTableModel):
	def __init__( self, magnet_records, field_scale ):
		self.magnet_records = magnet_records
	
	def getColumnName( self, column ):
		if column == 0:  return "Magnet"
		elif column == 1:  return "Current Field"
		elif column == 2:  return "Scaled Field"
		else:  return ""
	
	def getColumnCount( self ):  return 3
	
	def getValueAt( self, row, column ):
		magnet_record = self.magnet_records[row]
		if column == 0:  return magnet_record.get_magnet().getId()
		elif column == 1:
			field = magnet_record.get_field()
			if field != None:  return field
			else:  return "?"
		elif column == 2:  
			field = magnet_record.get_field()
			if field != None:  return field_scale * field
			else:  return "?"
		else:  return "?"
	
	def getRowCount( self ):
		return len( self.magnet_records )


# display the results
def display_results( magnet_records, field_scale ):
	frame = JFrame( "Magnet Energy Scaling" )
	frame.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE )
	frame.addWindowListener( WindowHandler() )
	frame.setBounds( 100, 100, 900, 600 )

	mainView = Box( BoxLayout.Y_AXIS )
	frame.getContentPane().add( mainView )
	magnet_table = JTable( MagnetTableModel( magnet_records, field_scale ) )
	mainView.add( JScrollPane( magnet_table ) )
	frame.show()


# begin the main process
accelerator = XMLDataManager.loadDefaultAccelerator()
sequence = accelerator.getComboSequence( "Ring" )
probe = ProbeFactory.getTransferMapProbe( sequence, TransferMapTracker() )
mass = probe.getSpeciesRestEnergy()
kinetic_energy = probe.getKineticEnergy()
beta_gamma = get_beta_gamma( mass, kinetic_energy )

magnets = sequence.getNodesOfType( Electromagnet.s_strType ).toArray()
magnet_records = []
for magnet in magnets:  
	magnet_records.append( MagnetRecord( magnet ) )

energy_scale_dialog = EnergyScaleDialog( kinetic_energy * 1.0e-6 )
energy_scale_dialog.setVisible( true )
if energy_scale_dialog.get_okay_status():
	initial_energy = energy_scale_dialog.get_initial_energy()
	target_energy = energy_scale_dialog.get_target_energy()
	field_scale = get_beta_gamma( mass, target_energy ) / get_beta_gamma( mass, initial_energy )
	display_results( magnet_records, field_scale )
else:
	sys.exit( 0 )


