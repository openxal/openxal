import sys
import time

#from java.io import *

from xal.tools.xml import XmlDataAdaptor
from xal.tools.data import DataAdaptor
from xal.smf.impl import BPM
from xal.smf.impl import BLM

from xal.ca import ChannelFactory, BatchGetValueRequest

class scanEvent:
	# Scan event is a boiler-plate class I use for apps
	# that require event-based data acquisition. e.g. resonance scan
	# Trigger beam, collect batch data from diagnostics, wait for next event.
	def __init__(self,bpms):
		#Bookkeeping
		self.bpms = bpms
		self.eventFile   = None
		self.javaFile    = None
                
		self.batch       = BatchGetValueRequest()
                
		
		self.doc         = XmlDataAdaptor.newEmptyDocumentAdaptor()
		#self.runAdaptor  = self.doc.createChild('run')

		#One off values that will go in the header
		self.accTurns    = { 'pv':'ICS_Tim:Chop_Flavor1:BeamOn' , 'ch':None , 'value':None }
		self.storedTurns = { 'pv':'ICS_Tim:RTDLGen:StoredTurns' , 'ch':None , 'value':None }
		self.bpm_tbt_dict = {}
		self.totalTurns = 0
                
		#Event by event vaules
		# No changes will be made for model investigations.
		# self.tuneSetting = [ 0, 0 ]

		#Diagnostics channels
		self.bpm_ch = []
		# self.blm_ch = []
		# self.bcm_ch = []

		#self.exclusionList = []
		self.initializeBatchGet()
		self.updateMachineState()
                


	def writeHeader(self):
		self.writeMachineState()
		return

	#def writeEventData(self):
        #	self.runAdaptor.writeTo( self.javaFile )
	#	return

	def initializeBatchGet(self):
		""" 
		Initializes all channels needed. 
		"""
		#print 'Initializing Diagnostics.'

		self.accTurns['ch']    = ChannelFactory.defaultFactory().getChannel( self.accTurns['pv'])
		self.storedTurns['ch'] = ChannelFactory.defaultFactory().getChannel( self.storedTurns['pv'])

		#all_bpms = self.accelerator.findSequence( "Ring" ).getNodesOfType(BPM.s_strType)
		#blms = self.accelerator.findSequence( "Ring" ).getNodesOfType(BLM.s_strType)
		bcms = [ 'Ring_Diag:BCM_D09:currentTBT' ]

		#This is a bad hack at the moment. 
		#exclude = [ 'Ring_Diag:BPM_C08' ]
                #exclude = ['Ring_Diag:BPM_D10','Ring_Diag:BPM_C08']
		#bpms = []
		#for i in all_bpms:
			#if str(i.getId()) not in exclude:
                            #bpms.append(i)
                        
                        

		#BPMS
		for i in self.bpms:
			x = i.getChannel( BPM.X_TBT_HANDLE )
			y = i.getChannel( BPM.Y_TBT_HANDLE )

			self.bpm_ch.append( [i,x,y] )
			self.batch.addChannel(x)
			self.batch.addChannel(y)
                
		#BLMS
		# for i in blms:
		# 	b = ChannelFactory.defaultFactory().getChannel( str(i) + ':Slow1PulseBeamOnTotalLoss' )
		# 	self.blm_ch.append(b)
		# 	self.batch.addChannel(b)

		#BCM
		# for i in bcms:
		# 	b = ChannelFactory.defaultFactory().getChannel( i )
		# 	self.bcm_ch.append( b )
		# 	self.batch.addChannel( b )

		return

	def submitBatchRequest(self):
                
		return self.batch.submitAndWait(10.0)
                

	def updateMachineState(self):


		self.accTurns['value']    = self.accTurns['ch'].getValInt()  
		self.storedTurns['value'] = self.storedTurns['ch'].getValInt() 
                
		return

	#def writeMachineState(self):
		#self.runAdaptor.setValue( 'accumulatedTurns', str(self.accTurns['ch'].getValInt()))
		#self.runAdaptor.setValue( 'storedTurns', str(self.storedTurns['ch'].getValInt()))

		#return

	def populateXMLEvent(self):

		#event - run child
		#eventNode = self.runAdaptor.createChild( 'event' )
		#Tune settings - event attribute
		# eventNode.setValue(  'qx' , self.tuneSetting[0] )
		# eventNode.setValue(  'qy' , self.tuneSetting[1] )
		self.totalTurns = self.accTurns['value'] + self.storedTurns['value']
		totalTurns = self.totalTurns

		#BPMS - event child
		for [i,x,y] in self.bpm_ch:
			#First try to get the data...
			fetchHasFailed = False
			try:
				rec = self.batch.getRecord(x)
				data = rec.doubleArray()
				data = data[0:totalTurns]
				key = str(i)+'-x'
				self.bpm_tbt_dict[ key ] = data
				#print 'xtbt=',data
			except:
				print "Exception raised fetching x data from ", str(i)
				fetchHasFailed = True

			#if not fetchHasFailed:
				#bpmNode = eventNode.createChild( 'bpm' )
				#bpmNode.setValue( 'id', str(i) )
				#bpmNode.setValue( 'amp-x', data )

			fetchHasFailed = False
			try:
				rec = self.batch.getRecord(y)
				data = rec.doubleArray()
				data = data[0:totalTurns]
				key = str(i)+'-y'
				self.bpm_tbt_dict[ key ] = data
				# print 'ytbt=',data
			except:
				print "Exception raised fetching y data from ", str(i)
				fetchHasFailed = True

			#if not fetchHasFailed:
				#bpmNode = eventNode.createChild( 'bpm' )
				#bpmNode.setValue( 'id', str(i) )
				#bpmNode.setValue( 'amp-y', data )
		
		# #BLMS - event child
		# for i in self.blm_ch:
		# 	blmNode = eventNode.createChild( 'blm' )

		# 	blmNode.setValue( 'id', str(i.getId()) )
		# 	rec = self.batch.getRecord( i )
		# 	blmNode.setValue( 'loss', rec.doubleValue() )

		# #BCM - event child
		# for i in self.bcm_ch:
		# 	bcmNode = eventNode.createChild( 'bcm' )

		# 	bcmNode.setValue( 'id', str(i.getId()) )
		# 	rec  = self.batch.getRecord( i )
		# 	data = rec.doubleArray()
		# 	data = data[0:totalTurns]
		# 	bcmNode.setValue( 'currentTBT', data )

		# 	# Writing after each event slows the script down alot.
		# 	# eventNode.writeTo( self.javaFile ) 
		return

	def getTotalTurns(self):
		return self.totalTurns

	def getTBT(self,bpmid,XorY):
		name = bpmid+'-'+XorY
		return self.bpm_tbt_dict[name]
	# def writeLastEvent(self):

	# 	#Tune settings
	# 	if(self.eventFile is not None):
	# 		self.eventFile.write( ' tune x  = ' + str(self.tuneSetting[0]) + '\t' )
	# 		self.eventFile.write( ' tune y  = ' + str(self.tuneSetting[1]) + '\t' )
	# 		self.eventFile.write( '\n' )

	# 		#BPMS
	# 		for [i,x,y] in self.bpm_ch:
	# 			self.eventFile.write( str(x.getId()) + '\t' )
	# 			self.writeBPMTBT( x )

	# 			self.eventFile.write( str(y.getId()) + '\t' )
	# 			self.writeBPMTBT( y )

	# 		#BLMS
	# 		for i in self.blm_ch:
	# 			self.eventFile.write( str(i.getId()) + '\t' )
	# 			self.writeBLMTotalLoss( i )

	# 		for i in self.bcm_ch:
	# 			self.eventFile.write( str(i.getId()) + '\t' )
	# 			self.writeBCMTBT( i )

	# 	return

	# def writeArrayChannel( self, channel ):
	# 	rec = self.batch.getRecord( channel )

	# 	if( rec is not None ):
	# 		data = rec.doubleArray()
	# 	else:
	# 		data = [ 0.0 ]
	# 		print 'Record empty'

	# 	if(self.eventFile is not None):
	# 		for i in data:
	# 			self.eventFile.write( str(i) + '\t' )
	# 		self.eventFile.write('\n')

	# def writeDoubleChannel(self, channel):
	# 	rec = self.batch.getRecord( channel )
	# 	if( rec is not None ):
	# 		data = rec.doubleValue()
	# 	else:
	# 		data = 0.0
	# 		print 'Record empty'

	# 	if( self.eventFile is not None):
	# 		self.eventFile.write( str(data) )
	# 		self.eventFile.write('\n')


	# 	return 

	# def writeBPMTBT(self, channel):
	# 	self.writeArrayChannel(channel)
	# 	return 

	# def writeBPMTBT(self, channel, da):
	# 	self.writeArrayChannel(channel)
	# 	return 

	# def writeBLMTotalLoss(self, channel):
	# 	self.writeDoubleChannel(channel)
	# 	return 

	# def writeBCMTBT(self, channel):
	# 	self.writeArrayChannel(channel)
	# 	return 

