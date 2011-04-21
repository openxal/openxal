#!/usr/bin/env jython
import sys
from jarray import *
from java.lang import *
from java.util import *
from org.xml.sax import *
from gov.sns.xal.smf import *
from gov.sns.xal.smf.impl import *
from gov.sns.xal.smf.impl.qualify import *
#from gov.sns.xal.smf.xdxf import *
from gov.sns.xal.smf.parser import *
from gov.sns.xal.smf.data import *
from pawt import swing
from Lattice import *
############################################################################
Node = swing.tree.DefaultMutableTreeNode
TreeModel = swing.tree.DefaultTreeModel
debug=false
printout=true

def getAccelerator():
	"""Get the XAL datagraph from the default path"""
	defaultPath = XMLDataManager.defaultPath()
	acc = XMLDataManager.loadDefaultAccelerator()
	print "reading accelerator from",defaultPath
	return acc

def addLeave(tree, node):
	"""Add the AcceleratorNode 'node' to the TreeModel 'tree'"""
	s = node.getPosition()
	l = node.getLength()
	leave = Node((node, "s= "+repr(s)+"m, l= "+repr(l)+"m"))
	tree.add(leave)
	return leave

def walk(tree, sequences):
	"""Walk recursively through the XAL datagraph 'sequences' and build a TreeModel 'tree'"""
	for seq in sequences:
		leave = addLeave(tree, seq)
		try:
			nodes = seq.getNodes()
		except AttributeError:
			#print "AttributeError: getNodes()"
			pass
		else:
			walk(leave, nodes)

def makeXALTree(name, acc):
	"""Make a TreeModel with root 'name' from the XAL datagraph 'acc'"""
	sequences = acc.getSequences()
	treeRoot = Node(name)
	walk(treeRoot,sequences)
	return treeRoot
#------------------------------------------------------------
def nodeToElement(node,result):
	"""Convert acc node to lattice element and add it to the 'result'
	Vector."""
	name=node.getId()
	type=node.getType()
	position=node.getPosition()
	length=node.getLength()
	#print "==n2E==",name,type,position,length

	#thick elements
	if node.isKindOf("dh"):
		result.add(Dipole(position,length,name))
	elif node.isKindOf("qh"):
		result.add(Quadrupole(position,length,name))
	elif node.isKindOf("qv"):
		result.add(Quadrupole(position,length,name))
	elif node.isKindOf("pq"):
		result.add(Quadrupole(position,length,name))

	#thin elements within nonzero drift space (quasi thick elements)
	elif node.isKindOf("rfgap"):
		for i in RFGap(position,length,name).asTuple(): result.add(i)
	elif node.isKindOf("bcm"):
		for i in BCMonitor(position,length,name).asTuple(): result.add(i)

	#thin elements
	elif node.isKindOf("dch"):
		result.add(HSteerer(position,length,name))
	elif node.isKindOf("dcv"):
		result.add(VSteerer(position,length,name))
	elif node.isKindOf("bpm"):
		result.add(BPMonitor(position,length,name))
	elif node.isKindOf("ws"):
		result.add(WScanner(position,length,name))
	else:
		print node.getId(),"is unknown node type."
		sys.exit(-1)

def nodesOfKind(sequence,kind,result):
	"""Recursive walk through all acc sequences and subsequences,
	filtering nodes of 'kind' and returning a Vector of lattice 
	elements in 'result'."""
	position_base=sequence.getPosition()
	for k in kind:
		nodes=sequence.getNodesOfType(k)
		if debug: print sequence.getId(),":",k,nodes
		for n in nodes:
			n.setPosition(n.getPosition()+position_base)
			nodeToElement(n,result)
			n.setPosition(n.getPosition()-position_base)
	try:
		subsequences=sequence.getSequences()
	except:
		return 
	else:
		for s in subsequences:
			nodesOfKind(s,kind,result)

def sortByPos(seq):
	"""Sort accelerator elements by position"""
	def cmp_by_pos(n1,n2):
		return cmp(n1.getPosition(),n2.getPosition())
	seq.sort(cmp_by_pos)

def processThickElements(sequence,lattice):
	"""Process all thick nodes. Filter them from 'sequence' and
	append them into 'lattice'."""

	#walk the XAL tree to get all nodes of a given kind
	kinds=("DH","QH","QV","PQ","RG","BCM")
	allElements=Vector()
	nodesOfKind(sequence,kinds,allElements)
	#conv Vector to list
	allElements=list(allElements)
	#sort all elements by their position
	sortByPos(allElements)
	#append all elements to the lattice
	for el in allElements:
		lattice.append(el)

def processThinElements(sequence,lattice):
	"""Process all thin nodes. Filter them from 'sequence' and
	Insert them into 'lattice'."""

	#walk the XAL tree to get all nodes of a given kind
	kinds=("DCH","DCV","BPM","WS")
	allElements=Vector()
	nodesOfKind(sequence,kinds,allElements)
	#conv Vector to list
	allElements=list(allElements)
	#sort all elements by their position
	sortByPos(allElements)
	#insert all elements to the lattice
	for el in allElements:
		lattice.insert(el)

def makeLattice(sequence):
	"""Make a lattice from a sequence."""
	lattice=Lattice(sequence.getId())
	seq_pos=sequence.getPosition()
	seq_len=sequence.getLength()

	#process all thick (len!=0) elements first
	processThickElements(sequence,lattice)

	#fill lattice up to end with drift space
	if seq_len > lattice.getLength():
		len=seq_len-lattice.getLength()
		lattice.append(Drift(seq_len-len*0.5,len))

	#special handling for DPLT elements
	if sequence.getId() == "DTL1":
		last=sequence.getNodeWithId("DTL_Diag:DPLT:BPM02")
		len=last.getPosition()-lattice.getLength()
		lattice.append(Drift(lattice.getLength()+len*0.5,len))

	#process all thin (len=0) ones
	processThinElements(sequence,lattice)

	return lattice

def makeLatticeTree(title,acc):
	treeRoot=Node(title)
	#sequences=[acc.getSequence("DTL1")]
	sequences=acc.getSequences()
	for seq in sequences:
		seqRoot=Node(seq.getId())
		treeRoot.add(seqRoot)
		lattice=makeLattice(seq)
		lattice.clearMarkers()
		lattice.joinDrifts()

		try:
			#consistency check
			lattice.isConsistent()
		except LatticeError,message:
			print LatticeError,message
			sys.exit(-1)

		if printout: lattice.cout()

		for ix in range(len(lattice)):
			el=lattice[ix]
			length=el.getLength()
			strt_pos=el.getAbsPosition()-length*0.5
			label1="s="+repr(strt_pos)+" m"
			label2="l="+repr(length)+" m"
			leave1=Node(label1)
			leave2=Node(label2)
			branch=Node(el.getName())
			branch.add(leave1)
			branch.add(leave2)
			seqRoot.add(branch)
	return treeRoot
#...................................................................
if __name__ == '__main__':
	#print "processing: jython",sys.argv[0]
	acc=getAccelerator()
	tree1=makeXALTree('Accelerator Sequences & Nodes', acc)	
	tree2=makeLatticeTree('Accelerator Sequences & Lattices',acc) 
	
	pane1=swing.JScrollPane(swing.JTree(tree1)) 
	pane2=swing.JScrollPane(swing.JTree(tree2))
	main=swing.Box.createHorizontalBox()
	main.add(pane1)
	main.add(pane2)
	swing.test(main,(900,500),sys.argv[0])

