#!/usr/bin/env jython
import sys
from java.lang import *
from java.util import *
from org.xml.sax import *
from gov.sns.xal.smf import *
from gov.sns.xal.smf.impl import *
from gov.sns.xal.smf.impl.qualify import *
from gov.sns.xal.smf.xdxf import *
from gov.sns.xal.smf.parser import *
from gov.sns.xal.smf.data import *
from gov.sns.xal.slg import *
from pawt import swing
############################################################################
false=(1<0)
true= (1>0)
Node = swing.tree.DefaultMutableTreeNode
TreeModel = swing.tree.DefaultTreeModel
debug=false
verbose=false
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
def makeLatticeTree(title,acc):
	treeRoot=Node(title)
	#sequences=[acc.getSequence("DTL1")]
	 
	#the lattice factory
	factory=LatticeFactory()
	factory.setDebug(debug)
	factory.setVerbose(verbose)

	sequences=acc.getSequences()
	for seq in sequences:

		#the lattice
		lattice=factory.getLattice(seq)
		lattice.clearMarkers()
		lattice.joinDrifts()

		try:
			#consistency check
			lattice.forConsistency()
		except LatticeError,message:
			print LatticeError,message
			sys.exit(-1)

		if printout: lattice.toConsole()

		#the lattice tree
		seqRoot=Node(seq.getId())
		treeRoot.add(seqRoot)
		for ix in range(lattice.len()):
			el=lattice.getItem(ix)
			pos=el.getPosition()
			length=el.getLength()
			strt_pos=el.getAbsPosition()-length*0.5
			label="s="+repr(strt_pos)+" m,"
			label+=" l="+repr(length)+" m,"
			label+=" p="+repr(pos)+" m"
			leave=Node(label)
			branch=Node(el.getName())
			branch.add(leave)
			seqRoot.add(branch)
	return treeRoot
#...................................................................
if __name__ == '__main__':
	#print "processing: jython",sys.argv[0]
	acc=getAccelerator()
	tree1=makeXALTree('XAL Sequences & Nodes', acc)	
	tree2=makeLatticeTree('Lattice Trees',acc) 
	
	pane1=swing.JScrollPane(swing.JTree(tree1)) 
	pane2=swing.JScrollPane(swing.JTree(tree2))
	main=swing.Box.createHorizontalBox()
	main.add(pane1)
	main.add(pane2)
	swing.test(main,(900,500),sys.argv[0])

