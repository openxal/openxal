This directory contains a first (draft) version of a LATTICE GENERATOR. 
The following files are present:

README.txt              This file
BCMonitor.java
BPMonitor.java
Bscript.py
Cscript.py
Dipole.java
Drift.java
Element.java            The superclass of all lattice elements.
HSteerer.java
LatticeError.java
LatticeFactory.java     The lattice generator implementation.
LatticeIterator.java
Lattice.java            The lattice class implementation.
Lattice.mod.xal.dtd
Lattice.py
LatticeTest.java        A console application to test the lattice implementation.
Marker.java
ModelTypeLookUp.java
NativeXMLLattice.java
Node2ElementMapper.java
Octupole.java
PermMarker.java
Quadrupole.java
RFGap.java
Sextupole.java
SkewQuad.java
SkewSext.java
SNSLattice.form
SNSLattice.java         A GUI application to test the lattice generator for the SNS XAL object tree.
TestComboLengthMain.java
TestVisitor.java
TestVisitorMain.java
ThinElement.java        The superclass of all thin lattice elements.
Visitor.java            The visitor interface (Visitor Design Pattern)
VisitorListener.java    The interface for visitor listeners.
VSteerer.java
WScanner.java

------
Thick elements:
Dipole.java             dipole
Drift.java              drift
Octupole.java           octupole
Quadrupole.java         quadrupole
Sextupole.java          sextupole
SkewQuad.java           skew quadrupole
SkewSext.java           skew sextupole

------
Slim elements:
BCMonitor.java          beam current monitor
RFGap.java              radio frequency gap

------
Thin elements:
BPMonitor.java          beam position monitor
HSteerer.java           horizontal steerer
VSteerer.java           vertical steerer
WScanner.java           wire scanning profile monitor
Marker.java             marker
PermMarker.java         permanent marker

------
Python code:
Lattice.py		The python equivalent to Lattice.java.
Bscript.py		A jython GUI app that does the same thing as SNSLattice using Lattice.py.
Cscript.py		A much shorter jython GUI app that does the same as SNSLattice and makes use of the java implementation of LatticeFactory and Lattice.
------
Launcher:
To start the java app SNSLattice use the following launch script:

//BASE points to your xadev base directory
BASE=$XAL_HOME     
CLASSPATH=$BASE/build/jar/xal.jar
CLASSPATH=$CLASSPATH:$BASE/ext_jars/Jama-1.0.1.jar
CLASSPATH=$CLASSPATH:$BASE/ext_jars/jca.jar
java -classpath $CLASSPATH gov.sns.xal.slg.SNSLattice

------
Slim elements are elements that have a physical length, but are presented as
an upstream drift and a downstream drift with the element as a thin element
between the two drifts.

The algorithm works roughly as follows: In a first pass the XAL object tree is scanned
for all nodes of a given kind. All nodes of this first pass schould be thick or slim. 
A lattice is created in filling up the space between these nodes with drift space. 
In a second pass the XAL object tree is scanned again for all nodes of another given kind.
All nodes of the second pass should be thin elements. These elements are then inserted
in the lattice of thick nodes created in the first pass. At insertion the corresponding
thick elements are cut into upstream and downstream pieces around the inserted thin
elements. The kind of thick(slim) and thin elements can be specified at instantiaton
of the LatticeFactory. By means of this you can generate any lattice with selected
thick(slim) and thin elements present.

------
xml stuff:
2LANL.xsl					An XSL stylesheet that transforms all_sns_lattice.xml into
								all_LANL-lattice.xml
all_sns_lattice.xml		The lattice in xml as it is produced by app.
								SNSLattice.java
all_LANL_lattice.xml    The lattice in xml as the on-line models needs it

To make the xslt transformation I used Apache's xalan with the following script:

XML_JARS=/home/klotz/jwsdp-1_0_01/common/endorsed
if [ "$1" = "--help" ]
then
echo $0" -xsl <stylesheet> -in <xmlSource> [-out <outputfile>]"
exit
fi
unset CLASSPATH
export CLASSPATH=$XML_JARS/bin/xercesImpl.jar:$CLASSPATH
export CLASSPATH=$XML_JARS/bin/xml-apis.jar:$CLASSPATH
export CLASSPATH=$XML_JARS/bin/xalan.jar:$CLASSPATH
export CLASSPATH=$XML_JARS/bin/xsltc.jar:$CLASSPATH
export CLASSPATH=$XML_JARS/bin/xalansamples.jar:$CLASSPATH

java org.apache.xalan.xslt.Process "$@"


------
ToDo:
Bring this file up-to-date.
Establish live connection to the XAL object tree.
Integrate the lattice with the on-line model.

------
wdklotz    March 2003  (klz@ornl.gov)
