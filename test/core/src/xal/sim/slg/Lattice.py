# $Id $
############################################################################
import sys
from math import fabs
############################################################################
LatticeError="LATTICE_ERROR:"
true =(1>0)  #get true
false=(1<0)  #get false
eps=1.e-5    #precision limit for position calculations
debug=false
#.........................................................
class Lattice:
	"""The lattice is a list of elements. Each element is associated with one and only one acclerator node."""

	def __init__(self,name,base=0):
		"""Create an empty lattice with a 'name' and a 'base' position."""
		self.name=name
		self.base=base    #the base position of the latice
		self.elements=[]  #the element list
		#an empty lattice
		self.elements.append(Marker(0,0,"BEGIN "+name))
		self.elements.append(Marker(0))
		self.elements.append(Marker(0,0,"END "+name))
		#all elements get same offset
		self.updateBases()

	def __len__(self):
		"""Return the lattice length in number of elements."""
		return len(self.elements)

	def __getitem__(self,index):
		"""Get element with 'index' from lattice."""
		return self.elements[index]

	def getLength(self):
		"""Return the lattice length in distance units."""
		return self.elements[-1].getEndPosition()

	def getName(self):
		"""Return the lattice name."""
		return self.name

	def append(self,element):
		"""Append an element at the end of the lattice."""
		lattice_end=self.elements.pop()
		last=self.elements[-1]
		start_pos=element.getStartPosition()
		#space to fill up with drift space
		drift_len=start_pos-last.getPosition()
		if drift_len < -eps:
			#ooops negative length: severe error
			raise LatticeError,"length is negative when appending: "+element.getName()
		elif fabs(drift_len) < eps:
			#too short drift: ignore
			pass
		else:
			#add an upstream drift space
			drift_pos=last.getPosition()+drift_len*0.5
			self.elements.append(Drift(drift_pos,drift_len))
			self.elements.append(Marker(start_pos))
		#add the element
		self.elements.append(element)
		end_pos=element.getEndPosition()
		self.elements.append(Marker(end_pos))
		#place 'lattice END' marker
		self.elements.append(Marker(end_pos,0,lattice_end.getName()))
		#all elements get same offset
		self.updateBases()
	
	def appendTuple(self,tuple):
		"""Append a tuple to the end of the lattice"""
		for el in tuple:
			self.append(el)

	def insert(self,element):
		"""Insert a zero length element into the lattice. Lumped elements in the lattice will be sliced accordingly."""
		if element.getLength() != 0.0:
			raise LatticeError,"length must be zero when inserting: "+element.getName()
		#assemble a list of indexes of marker elements
		markers=[]
		for i in range(len(self)):
			if self.elements[i].type == "marker":
				markers.append(i)
		#search the markers that embrace the element 
		before=markers[0]
		after=markers[-1]
		for m in markers[1:-1]:
			if self.elements[m].getPosition() <= element.getPosition():
				before=m
				continue
			after=m
			break
		#slice the element between the two markers
		between=after-1
		self.elements[between:after]=self.elements[between].split(element)
		#all elements get same offset
		self.updateBases()

	def clearMarkers(self):   
		"""Remove all marker elements from the lattice."""
		lattice=[]
		begin=self.elements[0]
		end  =self.elements[-1]
		lattice.append(begin)
		for el in self.elements:
			if el.getType() == 'marker': continue
			lattice.append(el)
		lattice.append(end)
		self.elements=lattice

	def updateBases(self):
		"""NOT FOR PUBLIC USE"""
		#all elements get same offset
		for el in self.elements:
			el.setBase(self.base)

	def joinDrifts(self):
		"""Join neighboring drift spaces into a single one."""
		lattice=[]
		#pass 1
		end=self.elements[-1]
		ix=0
		while ix < len(self)-1:
			el=self.elements[ix]
			ix=ix+1
			if el.getType() == 'drift': 
				elnext=self.elements[ix]
				if elnext.getType() == 'drift':
					ix=ix+1
					len1=el.getLength()
					len2=elnext.getLength()
					spos1=el.getStartPosition()
					spos2=elnext.getStartPosition()
					jlen=len1+len2
					jpos=spos1+jlen*0.5
					jspos=jpos-jlen*0.5
					jdrift=Drift(jpos,jlen)
					lattice.append(jdrift)
					if debug: print "join drifts:",(spos1,len1),"+",(spos2,len2),"=",(jspos,jlen)
				else:
					lattice.append(el)
			else: 
				lattice.append(el)
		lattice.append(end)
		self.elements=lattice

		#pass 2
		#check if there are still touching drifts in the lattice
		#if so call this function recursively until all done
		ix=0
		while ix < len(self)-1:
			el=self.elements[ix]
			ix=ix+1
			if el.getType() == 'drift':
				elnext=self.elements[ix]
				if elnext.getType() == 'drift':
					#repeat the joining process
					self.joinDrifts()
					return
				else:
					pass
			else:
				pass
		return

	def cout(self):
		"""Make a printed output of the lattice."""
		for count in range(len(self)):
			el=self.elements[count]
			abs_start_pos=el.getAbsPosition()-el.getLength()*0.5
			print "s=",abs_start_pos,"m\t",el.getName(),"\t",el.getType(),el.getPosition(),el.getLength()
		print "Totals: length of",self.getName(),"=",self.getLength(),"m with",len(self),"elements."

	def isConsistent(self):
		"""Consistency check"""
		for ix in range(len(self)-1):
			el=self[ix]
			next=self[ix+1]
			el_pos=el.getPosition()
			el_len=el.getLength()
			next_pos=next.getPosition()
			next_len=next.getLength()
			if fabs(el_pos+(el_len+next_len)*0.5-next_pos) > eps:
				raise LatticeError,"inconsistent distances between "+el.getName()+" and "+next.getName()
#.........................................................
class Element:
	"""The super class of all lattice elements."""

	def __init__(self,name,position=0,len=0):
		"""NOT FOR PUBLIC USE"""
		self.name=name
		self.position=position
		self.base=0
		self.len=len

	def getStartPosition(self):
		"""Return the upstream start position of this element."""
		return self.position-self.len*0.5

	def getEndPosition(self):
		"""Return the downstream end position of this element."""
		return self.position+self.len*0.5

	def getPosition(self):
		"""Return the center position of this element."""
		return self.position

	def getAbsPosition(self):
		"""Return the absolute center position of this element."""
		return self.position+self.base

	def getLength(self):
		"""Return the length of the element in distance units."""
		return self.len

	def getName(self):
		"""Return the name of the element."""
		return self.name

	def getBase(self):
		"""Return the base for relative positions."""
		return self.base

	def getSlicePositions(self,cut_pos):
		"""NOT FOR PUBLIC USE"""
		#calculate length and position of sliced parts
		up_len=cut_pos-self.getStartPosition()
		if fabs(up_len) < eps: up_len = 0
		dn_len=self.getLength()-up_len
		if fabs(dn_len) < eps: dn_len = 0
		up_pos=self.getStartPosition()+up_len*0.5
		dn_pos=self.getEndPosition()-dn_len*0.5
		return (up_pos,up_len,dn_pos,dn_len)

	def setBase(self,base):
		"""NOT FOR PUBLIC USE"""
		#Set the base for relative positions.
		self.base=base

	def setPosition(self,position):
		"""Set the element position."""
		self.position=position

	def setLength(self,length):
		"""Set the element length."""
		self.len=length

	def split(self,insert,CLASS):
		"""NOT FOR PUBLIC USE"""
		#The slice (and replace) operation. The thick element 'self' of type 'CLASS'
		#is cut into an upstream and a downstream part and then element 'insert'
		#is inserted (with limiting markers) into the lattice.
		cut_pos=insert.getPosition()
		positions=self.getSlicePositions(cut_pos)
		upstream=CLASS(positions[0],positions[1],self.getName())
		dnstream=CLASS(positions[2],positions[3],self.getName()+"+")
		marker=Marker(cut_pos)
		if fabs(upstream.getLength()) < eps:
			return [insert,marker,self]
		elif fabs(dnstream.getLength()) < eps:
			return [self,marker,insert]
		else:
			return [upstream,marker,insert,marker,dnstream]
#.........................................................
class Drift(Element):
	"""The drift space element."""
	type="drift"

	def __init__(self,position,len,name="---"):
		"""Create the element: center position and len(ght) are needed"""
		Element.__init__(self,name,position,len)

	def getType(self):
		"""Return the element type."""
		return self.type;

	def split(self,insert):
		"""NOT FOR PUBLIC USE"""
		#Split this element to place element 'insert' in between.
		return Element.split(self,insert,Drift)
#.........................................................
class Dipole(Element):
	"""The dipole element."""
	type="dipole"

	def __init__(self,position,len,name="DIP"):
		"""Create the element: center position and len(ght) are needed"""
		Element.__init__(self,name,position,len)

	def getType(self):
		"""Return the element type."""
		return self.type;

	def split(self,insert):
		"""NOT FOR PUBLIC USE"""
		#Split this element to place element 'insert' in between.
		return Element.split(self,insert,Dipole)
#.........................................................
class Quadrupole(Element):
	"""The quadrupole element."""
	type="quadrupole"

	def __init__(self,position,len,name="NQP"):
		"""Create the element: center position and len(ght) are needed"""
		Element.__init__(self,name,position,len)

	def getType(self):
		"""Return the element type."""
		return self.type;

	def split(self,insert):
		"""NOT FOR PUBLIC USE"""
		#Split this element to place element 'insert' in between.
		return Element.split(self,insert,Quadrupole)
#.........................................................
class Sextupole(Element):
	"""The sextupole element."""
	type="sextupole"

	def __init__(self,position,len,name="NSX"):
		"""Create the element: center position and len(ght) are needed"""
		Element.__init__(self,name,position,len)

	def getType(self):
		"""Return the element type."""
		return self.type;

	def split(self,insert):
		"""NOT FOR PUBLIC USE"""
		#Split this element to place element 'insert' in between.
		return Element.split(self,insert,Sextupole)
#.........................................................
class SkewQuad(Element):
	"""The skew quadrupole element."""
	type="skew_quadrupole"

	def __init__(self,position,len,name="SQP"):
		"""Create the element: center position and len(ght) are needed"""
		Element.__init__(self,name,position,len)

	def split(self,insert):
		"""NOT FOR PUBLIC USE"""
		#Split this element to place element 'insert' in between.
		return Element.split(self,insert,SkewQuad)
#.........................................................
class SkewSext(Element):
	"""The skew sextupole element."""
	type="skew_sextupole"

	def __init__(self,position,len,name="SSX"):
		"""Create the element: center position and len(ght) are needed"""
		Element.__init__(self,name,position,len)

	def getType(self):
		"""Return the element type."""
		return self.type;

	def split(self,insert):
		"""NOT FOR PUBLIC USE"""
		#Split this element to place element 'insert' in between.
		return Element.split(self,insert,SkewSext)
#.........................................................
class Octupole(Element):
	"""The octupole element."""
	type="octupole"

	def __init__(self,position,len,name="OCT"):
		"""Create the element: center position and len(ght) are needed"""
		Element.__init__(self,name,position,len)

	def getType(self):
		"""Return the element type."""
		return self.type;

	def split(self,insert):
		"""NOT FOR PUBLIC USE"""
		#Split this element to place element 'insert' in between.
		return Element.split(self,insert,Octupole)
#.........................................................
class ThinElement(Element):
	"""The superclass of all thin elements"""

	def __init__(self,name,position=0,len=0):
		"""NOT FOR PUBLIC USE"""
		Element.__init__(self,name,position,len)

	def getStartPosition(self):
		"""Return the upstream start position of this element."""
		return Element.getPosition(self)

	def getEndPosition(self):
		"""Return the downstream end position of this element."""
		return Element.getPosition(self)

	def getLength(self):
		"""Return the length of the element in distance units."""
		return 0

	def getEffLength(self):
		"""Return the effective length of the element in distance units."""
		return Element.getLength(self)

	def getUpstreamDrift(self):
		"""Return the upstream drift space."""
		len=self.getEffLength()*0.5
		position=Element.getPosition(self)
		if fabs(len) < eps:
			return Marker(position)
		else:
			position=position-len*0.5
			return Drift(position,len)

	def getDownstreamDrift(self):
		"""Return the downstream drift space."""
		len=self.getEffLength()*0.5
		position=Element.getPosition(self)
		if fabs(len) < eps:
			return Marker(position)
		else:
			position=position+len*0.5
			return Drift(position,len)

	def asTuple(self):
		"""Return the thin element as a tuple (drift,element,drift)"""
		return (self.getUpstreamDrift(),self,self.getDownstreamDrift())

	def split(self,insert):
		"""NOT FOR PUBLIC USE"""
		#Split this element to place element 'insert' in between.
		return [self,insert]
#.........................................................
class Marker(ThinElement):
	"""The marker element."""
	type="marker"

	def __init__(self,position,len=0,name="***"):
		"""Create the element: center position and len(ght) are needed"""
		ThinElement.__init__(self,name,position)

	def getType(self):
		"""Return the element type."""
		return self.type;
#.........................................................
class RFGap(ThinElement):
	"""The radio frequency gap element."""
	type="rfgap"

	def __init__(self,position,len=0,name="RFG"):
		"""Create the element: center position and len(ght) are needed"""
		ThinElement.__init__(self,name,position,len)

	def getType(self):
		"""Return the element type."""
		return self.type;
#.........................................................
class BCMonitor(ThinElement):
	"""The beam current monitor element."""
	type="beamcurrentmonitor"

	def __init__(self,position,len=0,name="BCM"):
		"""Create the element: center position and len(ght) are needed"""
		ThinElement.__init__(self,name,position,len)

	def getType(self):
		"""Return the element type."""
		return self.type;
#.........................................................
class HSteerer(ThinElement):
	"""The horizontal steerer element."""
	type="hsteerer"

	def __init__(self,position,len=0,name="DCH"):
		"""Create the element: center position and len(ght) are needed"""
		ThinElement.__init__(self,name,position)

	def getType(self):
		"""Return the element type."""
		return self.type;
#.........................................................
class VSteerer(ThinElement):
	"""The vertical steerer element."""
	type="vsteerer"

	def __init__(self,position,len=0,name="DCV"):
		"""Create the element: center position and len(ght) are needed"""
		ThinElement.__init__(self,name,position)

	def getType(self):
		"""Return the element type."""
		return self.type;
#.........................................................
class BPMonitor(ThinElement):
	"""The beam position monitor element."""
	type="beampositionmonitor"

	def __init__(self,position,len=0,name="BPM"):
		"""Create the element: center position and len(ght) are needed"""
		ThinElement.__init__(self,name,position)

	def getType(self):
		"""Return the element type."""
		return self.type;
#.........................................................
class WScanner(ThinElement):
	"""The wire scanner monitor element."""
	type="wirescanner"

	def __init__(self,position,len=0,name="WSM"):
		"""Create the element: center position and len(ght) are needed"""
		ThinElement.__init__(self,name,position)

	def getType(self):
		"""Return the element type."""
		return self.type;
#.........................................................
if __name__ == '__main__':
	test="Thick Element Test"
	print test
	lattice=Lattice(test,40.)
	#..............................
	try:
		lattice.append(Dipole(0.5,0.3,"DIP/0"))
		lattice.append(Quadrupole(0.85,0.15,"QH/0"))
		#lattice.append(Quadrupole(1.1,0.15,"QV/3"))
		lattice.append(Quadrupole(1.1,0.15))
		lattice.append(Dipole(1.37,0.3,"DIP/1"))
		#lattice.append(BPMonitor(1.57))
		lattice.append(Drift(1.545,0.05))
		#..............................
		#lattice.insert(BPMonitor(0.0,0,">>>"))
		#lattice.insert(BPMonitor(0.5,0,">>>"))
		#lattice.insert(BPMonitor(1.1,0,">>>"))
		#lattice.insert(BPMonitor(1.1375,0,">>>"))
		#lattice.insert(BPMonitor(1.175,0,">>>"))
		#lattice.insert(BPMonitor(1.57,0,">>>"))
		#..............................
		#lattice.insert(VSteerer(0.0))
		#lattice.insert(HSteerer(0.0))
		#lattice.insert(VSteerer(lattice.getLength()))
		#lattice.insert(HSteerer(lattice.getLength()))
		#..............................
		#lattice.clearMarkers()
		lattice.cout()
		#..............................
		test1="Thin Element Test"
		print test1
		lattice1=Lattice(test1,66)
		rfgap=RFGap(10,20)
		for part in rfgap.asTuple():
			start_pos=part.getStartPosition()
			name=part.getName()
			type=part.getType()
			position=part.getPosition()
			length=part.getLength()
			print start_pos,name,type,position,length	
			lattice1.append(part)
		lattice1.clearMarkers()
		lattice1.cout()
		#..............................
		lattice2=Lattice(test1,66)
		rfgap=RFGap(45.3,0)
		for part in rfgap.asTuple():
			start_pos=part.getStartPosition()
			name=part.getName()
			type=part.getType()
			position=part.getPosition()
			length=part.getLength()
			print start_pos,name,type,position,length	
			lattice2.append(part)
		lattice2.clearMarkers()
		lattice2.cout()
		#..............................
	except LatticeError,message:
		print LatticeError,message
		sys.exit(-1)
