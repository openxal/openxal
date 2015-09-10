# This program interpolates/returns the magnet values required to change the 
#beam size by a certain percent, using a certain energy beam.  The GUI
#returns the information in a table-like format and includes a chart   
#of possible percent change.
#  Also, this program has a beta table and plot.
#Author Catherine Schiber
#Created summer of 2012
import sys #basic imports
import math
import types

from java import awt  #GUI imports
from pawt import swing
from jarray import *
from java.lang import *
from java.util import *
from java.io import *
from javax.swing import *
from java.awt import *

from org.xml.sax import *  #Imports that deal with the accelerator
from xal.smf import *
from xal.model.alg import EnvTrackerAdapt
from xal.smf.data import *
from xal.model import *
from xal.model.probe import *
from xal.model.xml import *
from xal.sim.scenario import *
from xal.service.pvlogger.sim import *
from xal.tools.beam import *
#from gov.sns.xal.slg import *
from xal.tools.xml import XmlDataAdaptor
from xal.extension.widgets.plot import *
from xal.extension.widgets.apputils import SimpleChartPopupMenu
#from gov.sns.xal.smf.proxy import *

false= Boolean("false").booleanValue()
true= Boolean("true").booleanValue()


# get the path to a file residing in this script's local directory
def getPathToLocalFile( fileName ):
	scriptFolder = File( sys.argv[0] ).getParentFile()
	parsingFile = File( scriptFolder, fileName )
	return parsingFile.getAbsolutePath()


#global data for interpolation:----------------------------------------------
grid_data = {}
xdisplay = swing.JTextField("x% change (integer)")
ydisplay = swing.JTextField("y% change (integer)")
qh26display = swing.JTextField()  #A lot of this stuff is repeated in the 
qv27display = swing.JTextField()  #GUI section.  It needs to be here to fit
qh28display = swing.JTextField()  #in the functions.
qv29display = swing.JTextField()
qh30display = swing.JTextField()
error = swing.JTextField("Messages appear here.")
edisplay = swing.JTextField(" Enter energy value")
#----------------------------------------------------------------------------
#Main interpolation program functions
#----------------------------------------------------------------------------
def readFile():                   #Gets all of the data.
    iFile = open( getPathToLocalFile( 'forparsing.txt' ) )
    for line in iFile.readlines():   
        s = String(line[0:-1])        
        tokens = s.split("\\s+") 
    for i in range(0, len(tokens), 7):
        quadset = []
        perx = int(tokens[i])
        pery = int(tokens[i+1])
        quadset.append(float(tokens[i+2]))#floats get rid of the u''
        quadset.append(float(tokens[i+3]))
        quadset.append(float(tokens[i+4]))
        quadset.append(float(tokens[i+5]))
        quadset.append(float(tokens[i+6]))
        grid_data[perx, pery] = quadset
        pquadset = grid_data[perx, pery]

readFile()#Keep this here so everything is in the dictionary.

newx = xdisplay.text  #Need these for the functions.
newy = ydisplay.text
newkey = newx, newy
closest = {}
corners = {}
Q = {}
newe = edisplay.text
ad_list = ['','','','',''] 
#new list of values made by the adjust_mag function, has blank spaces so I can directly enter values into it.

def results(newkey):     #Main function, goes into GUI
    newx = int(xdisplay.text)
    newy = int(ydisplay.text)
    newe = float(edisplay.text)
    newkey = newx, newy
    if newkey in grid_data:
        adjust_mag(grid_data[newkey], ad_list)
        qh26display.text = '%.3f' % ad_list[0]
        qv27display.text = '%.3f' % ad_list[1]
        qh28display.text = '%.3f' % ad_list[2]
        qv29display.text = '%.3f' % ad_list[3]
        qh30display.text = '%.3f' % ad_list[4] 
        error.text = "Known point %s with energy %.3fGeV." % (str(newkey), newe)
    else:
        newx = int(xdisplay.text)
        newy = int(ydisplay.text)
        newkey = newx, newy
        newe = float(edisplay.text)
        hunt(newkey)
        make_corners()
        trim_corners()
        error.text = "Interpolated point %s with energy %.3fGeV." % (str(newkey), newe)
        return interpolate()



def hunt(newkey): #Puts the four closest points in a dictionary
    newx = xdisplay.text
    newy = ydisplay.text
    newkey = newx, newy
    error.text = 'Error:'# I want to be able to display multiple problems
    if newkey in grid_data:
        pass
    elif newkey not in grid_data:
        loxtstx = int(newx) 
        tsty = 0  #Using y = 0 catches most of the points (only at
        loxtstkey = loxtstx, tsty#y = -5 does x have more range)
        while loxtstkey not in grid_data: #found lower x...
            loxtstx -= 1
            loxtstkey = loxtstx,tsty #For some reason it stopped displaying
            if loxtstkey in grid_data:#error messages if I used y = -5
                pass                  #to get all of the points.
            elif loxtstx < -100:
                error.text = error.text + "The x was too low, enter a new x value. "    #This lets the error display display more
                break           #than one error message.
        hixtstx = int(newx)
        hixtstkey = hixtstx, tsty
        while hixtstkey not in grid_data: #found upper x...
            hixtstx += 1
            hixtstkey = hixtstx,tsty
            if hixtstkey in grid_data:
                pass
            elif hixtstx > 100:
                error.text = error.text + "The x was too high, enter a new x value. "
                break
        hiytsty = int(newy)
        tstx = 0
        hiytstkey = tstx, hiytsty                #upper y...
        while hiytstkey not in grid_data:
            hiytsty += 1
            hiytstkey = tstx, hiytsty
            if hiytstkey in grid_data:
                pass
            elif hiytsty > 100:
                error.text = error.text + "The y was too high, enter a new y value."
                break
        loytsty = int(newy) 
        loytstkey = tstx, loytsty
        while loytstkey not in grid_data:   #lower y...
            loytsty -= 1
            loytstkey = tstx, loytsty
            if loytstkey in grid_data:
                pass
            elif loytsty < - 100:
                error.text = error.text + "The y was too low, enter a new y value."
                break
        closest[newx] = loxtstx, hixtstx
        closest[newy] = loytsty, hiytsty   #now for corners
        return error.text

def make_corners():  #this finds the four corners around the point
    newx = xdisplay.text
    newy = ydisplay.text
    newkey = newx, newy 
    if newkey not in grid_data:  #GUIs are picky, need try/except.
        try:
            corners["hix,loy"] = closest[newx][1], closest[newy][0]
            corners["lox,loy"] = closest[newx][0], closest[newy][0]
            corners["hix,hiy"] = closest[newx][1], closest[newy][1]
            corners["lox,hiy"] = closest[newx][0], closest[newy][1]
        except KeyError:
            pass

def trim_corners(): #gets rid of too high/low corners
    try:
        if corners["hix,loy"] not in grid_data: #GUI still picky.
            del corners["hix,loy"]
        if corners["lox,loy"] not in grid_data:
            del corners["lox,loy"]
        if corners["hix,hiy"] not in grid_data:
            del corners["hix,hiy"]
        if corners["lox,hiy"] not in grid_data:
            del corners["lox,hiy"]
    except KeyError:
        pass

def interpolate():  #Uses bi-linear interpolation.  Is split up to avoid
    try:
        newx = xdisplay.text #/0 errors.
        newy = ydisplay.text
        newkey = int(newx), int(newy)# the int() gets rid of the u''. 
        Q[11] = grid_data[corners["lox,loy"]] #def. the variables
        Q[21] = grid_data[corners["hix,loy"]]
        Q[12] = grid_data[corners["lox,hiy"]]
        Q[22] = grid_data[corners["hix,hiy"]]
        x = float(newx)
        y = float(newy)
        x1 = float(closest[newx][0])
        x2 = float(closest[newx][1])
        y1 = float(closest[newy][0])
        y2 = float(closest[newy][1])   #making the equations shorter
        xs = x2 - x1
        ys = y2 - y1
        xsys = xs * ys
        x2x = x2 - x 
        xx1 = x - x1
        y2y = y2 - y 
        yy1 = y - y1
        if closest[newy][0] == closest[newy][1]: #For known y point.
            listx = []
            for i in xrange(5):
                R = (x2x/xs) * Q[11][i] + (xx1/xs) * Q[21][i]
                listx.append(R) 
            adjust_mag(listx, ad_list)
            qh26display.text = '%.3f' % ad_list[0]
            qv27display.text = '%.3f' % ad_list[1]
            qh28display.text = '%.3f' % ad_list[2]
            qv29display.text = '%.3f' % ad_list[3]
            qh30display.text = '%.3f' % ad_list[4]
        elif closest[newx][0] == closest[newx][1]:  #For known x point.
            listy = []
            for i in xrange(5):
                P = (y2y/ys) * Q[11][i] + (yy1/ys) * Q[12][i]
                listy.append(P) 
            adjust_mag(listy, ad_list)
            qh26display.text = '%.3f' % ad_list[0]
            qv27display.text = '%.3f' % ad_list[1]
            qh28display.text = '%.3f' % ad_list[2]
            qv29display.text = '%.3f' % ad_list[3]
            qh30display.text = '%.3f' % ad_list[4]
        else:                               #Both unknown.
            list = []
            for i in xrange(5):
                S = (Q[11][i]/xsys) * x2x * y2y + (Q[21][i]/xsys) * xx1 * y2y + (Q[12][i]/xsys) * x2x * yy1 + (Q[22][i]/xsys) * xx1 * yy1
                list.append(S)
            adjust_mag(list, ad_list)
            qh26display.text = '%.3f' % ad_list[0]
            qv27display.text = '%.3f' % ad_list[1]
            qh28display.text = '%.3f' % ad_list[2]
            qv29display.text = '%.3f' % ad_list[3]
            qh30display.text = '%.3f' % ad_list[4]
    except KeyError:
        error.setForeground(dred) #I want red error messages.
        error.text = hunt(newkey)
        clrr(event)
#The try/except was required because repeatedely using the program eventually
#causes it to treat failed points as interpolated points.  Including this try
#/except catches them again.

def add_spike():   #the points (36,-5) to (39,-5) do not get caught by the 
    vector = []    #hunt function and my attempts to fix it caused the 
    newm = []      #program to stop working properly and as I am running
    newx = xdisplay.text#low on time I will keep this here.
    newy = int(ydisplay.text)
    newkey = int(newx), int(newy)
    newe = float(edisplay.text)
    x = float(newx)
    for i in xrange(5):
        vector.append(grid_data[40, -5][i] - grid_data[35, -5][i])
    percent = (x - 35.0)/5.0
    for i in xrange(5):
        newm.append(grid_data[35, -5][i] + percent * vector[i])
    if newy == -5:
        adjust_mag(newm, ad_list)
        qh26display.text = '%.3f' % ad_list[0]
        qv27display.text = '%.3f' % ad_list[1]
        qh28display.text = '%.3f' % ad_list[2]
        qv29display.text = '%.3f' % ad_list[3]
        qh30display.text = '%.3f' % ad_list[4]#if it is not reset to black it 
        error.setForeground(awt.Color.black) #is red from the earlier change.
        error.text = "Interpolated point %s with energy %.3fGeV." % (str(newkey), newe)
    else:
        error.text = hunt(newkey) #This is the error message, works because x 
                               #only gets to be over 35 at y=-5.

def adjust_mag(old_value, new_value): #To adjust the values for different 
    newe = float(edisplay.text)       #energies
    T_old = 0.910         #GeV, initial energy
    T_new = newe
    mp = 0.93829   #Mass of proton in GeV
    pc_old = Math.sqrt(T_old*(T_old+(2.0*mp)))
    pc_new = Math.sqrt(T_new*(T_new+(2.0*mp)))
    for i in xrange(5):
        new_value[i] = old_value[i] * (pc_new/pc_old)


#---------------------------------------------------------------------------
#Beta plotting and table program
#Adapted from S. Cousineau's example.
#It used an online model to calculate the required data.
#----------------------------------------------------------------------------

# read the accelerator & make the sequence
accl = XMLDataManager.loadDefaultAccelerator()
seq = accl.getSequence("RTBT2")  #Start element required for accuracy
init_position = seq.getPosition(seq.getNodeWithId("RTBT_Diag:WS20"))

#This function changes the magnet values.
def changeMag(name, value):  #This is too long to keep typing out.
    return model.setModelInput(seq.getNodeWithId(name),ElectromagnetPropertyAccessor.PROPERTY_FIELD, float(value))

#This function keeps all of the magnet changes in one place.
def setMags():  
    changeMag("RTBT_Mag:QH18", magc.text) #The constant magnets
    changeMag("RTBT_Mag:QV19", '-'+magc.text)
    changeMag("RTBT_Mag:QH20", magc.text)
    changeMag("RTBT_Mag:QV21", '-'+magc.text)#If the QV values are not -, it 
    changeMag("RTBT_Mag:QH22", magc.text) #does not work.
    changeMag("RTBT_Mag:QV23", '-'+magc.text)
    changeMag("RTBT_Mag:QH24", magc.text)
    changeMag("RTBT_Mag:QV25", '-'+magc.text)
    changeMag("RTBT_Mag:QH26", qh26display.text)  #Variable magnets
    changeMag("RTBT_Mag:QV27", '-'+qv27display.text)
    changeMag("RTBT_Mag:QH28", qh28display.text)
    changeMag("RTBT_Mag:QV29", '-'+qv29display.text)
    changeMag("RTBT_Mag:QH30", qh30display.text)

etracker = EnvTrackerAdapt(); #This sets up the model, must be global data.
probe = ProbeFactory.getEnvelopeProbe(seq, etracker);
model = Scenario.newScenarioFor(seq);
model.setProbe(probe);
model.setStartElementId("RTBT_Diag:WS20") #Required for acuracy.
model.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);
plds = PVLoggerDataSource(20124281)#This is the id of the file we used.
plds.closeConnection()
model = plds.setModelSource(seq, model);

def setUp():
    #initialize probe with previously solved for a twiss params
    alphax0 = 0.665  #These are the constants I used in the model to solve
    betax0 = 7.126   #for the magnet settings.
    alphay0 = -1.673
    betay0 = 17.605
    alphaz0 = 0.;
    betaz0 = 9600.;
    emitx0 = 0.35544e-4
    emity0 = 0.37038e-4
    
    xTwiss = Twiss(alphax0, betax0, emitx0)
    yTwiss = Twiss(alphay0, betay0, emity0)
    zTwiss = Twiss(alphaz0, betaz0, 11.4e-3)
    tw=[]
    tw.append(xTwiss)
    tw.append(yTwiss)
    tw.append(zTwiss)
    probe.reset()#The reset stops it from continously adding to the plot.
    probe.initFromTwiss(tw)
    
    setMags()  #Here is where the magnets are changed.
    model.resync()#Resyncing is important.
    model.run()

xBeta = [] #I will want to display these in a table later.
yBeta = []
elements = []

def plotAns():    # plot the answer
    cdx = BasicGraphData()
    cdx.removeAllPoints#Just in case some points are left from the last plot,
    cdy = BasicGraphData()#might not be needed.
    cdy.removeAllPoints
    pos = []  #Graph data
    xSize = []
    ySize = []
    probe = model.getProbe()
    traj= probe.getTrajectory()
    iterState= traj.stateIterator()
    while iterState.hasNext(): #This collects all the data I need.
        state= iterState.next()
        s= state.getPosition()
        twiss= state.twissParameters()
        x = 1000. * twiss[0].getBeta()
        y = 1000. * twiss[1].getBeta()
        pos.append(s)
        xSize.append(x)
        ySize.append(y)
        xb = twiss[0].getBeta()
        yb = twiss[1].getBeta()
        xBeta.append(xb)#betas
        yBeta.append(yb)
        place = state.getElementId()#names of elements
        elements.append(place)
    
    cdx.addPoint(pos, xSize) #This plots the x betas.
    cdx.setDrawLinesOn(true)
    cdx.setDrawPointsOn(false)
    cdx.setGraphProperty("Legend","X Model")
    cdx.setGraphColor(Color.RED)	
    
    cdy.addPoint(pos, ySize) #This plots the y betas.
    cdy.setDrawLinesOn(true)
    cdy.setDrawPointsOn(false)
    cdy.setGraphColor(Color.BLUE)
    cdy.setGraphProperty("Legend","Y Model")
	
    plot.addGraphData(cdx) #Adds the graph data to an already defined plot.
    plot.addGraphData(cdy)
    popupMenu = SimpleChartPopupMenu.addPopupMenuTo(plot)
    plot.setLegendButtonVisible(true) #adds a legend

#Table stuff


def data(name):  #Makes adding stuff to the table easier.
    elem = elements.index(name)
    return [name, "%.3f" % xBeta[elem], "%.3f" % yBeta[elem]]
#It returns the name and the matching betas for the element.

#The index numbers of the elements change when the magnets are changed, so I 
#entered every name it to make sure I am displaying the right elements.
def makeTable():
    ColumnNames = ['Element', 'x Beta', 'y Beta']
    table_data = [] 
    table_data = [data("RTBT_Diag:WS20"), data("RTBT_Diag:BLM21a"),
                  data("RTBT_Mag:QV21xx"), data("RTBT_Diag:BLM_Mov03"),
                  data("RTBT_Diag:BPM21"), data("RTBT_Mag:DCV21"),
                  data("RTBT_Diag:BLM21b"), data("RTBT_Diag:WS21"),
                  data("RTBT_Diag:BPM22"), data("RTBT_Diag:BLM22a"),
                  data("RTBT_Mag:QH22xx"), data("RTBT_Diag:BLM22b"),
                  data("RTBT_Diag:BLM23"), data("RTBT_Mag:QV23xx"),
                  data("RTBT_Diag:BPM23"), data("RTBT_Mag:DCV23"),
                  data("RTBT_Diag:WS23"), data("RTBT_Diag:BLM24"),
                  data("RTBT_Mag:QH24xx"), data("RTBT_Diag:BPM24"),
                  data("RTBT_Diag:WS24"), data("RTBT_Diag:BLM25"),
                  data("RTBT_Mag:QV25xx"), data("RTBT_Diag:BLM_Mov04"),
                  data("RTBT_Diag:BPM25"), data("RTBT_Diag:BCM25"),
                  data("RTBT_Diag:BLM26"), data("RTBT_Mag:QH26xx"),
                  data("RTBT_Diag:BLM27"), data("RTBT_Mag:QV27xx"),
                  data("RTBT_Diag:BPM27"), data("RTBT_Diag:BLM28"),
                  data("RTBT_Mag:QH28xx"), data("RTBT_Mag:DCH28"),
                  data("RTBT_Mag:DCV28"), data("RTBT_Diag:BLM29"),
                  data("RTBT_Mag:QV29xx"), data("RTBT_Diag:BPM29"),
                  data("RTBT_Diag:BLM30"), data("RTBT_Mag:QH30xx"),
                  data("RTBT_Mag:DCH30"), data("RTBT_Mag:DCV30"),
                  data("RTBT_Diag:Harp30"), data("RTBT_Diag:BPM30"),
                  data("RTBT_Vac:VIW"), data("RTBT:Tgt")]
    table = JTable(table_data, ColumnNames)
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF)
    for i in xrange(2):
        column = table.getColumnModel().getColumn(i)
        if i == 0:
            column.setPreferredWidth(150)#The table is too wide if this is 
        if i == 1:                       #not included.
            column.setPreferredWidth(60)
        if i == 2:
            column.setPreferredWidth(60)   
    scroll = JScrollPane(table)
    scroll.setPreferredSize(Dimension(280, 340))
    filler.add(scroll)#Adding the scroll to a seperate panel allows me to
    filler.validate()#get it to repaint the component with the new table

#----------------------------------------------------------------------------
#Pure GUI portion for interpolation/buttons and displays
#----------------------------------------------------------------------------

dred = awt.Color.darker(awt.Color.red) #The red is too bright.
#Def. labels, displays ------------------------------------------------------

xlabel = swing.JLabel(" Enter desired % change in x:")#x,y stuff
ylabel = swing.JLabel(" Enter desired % change in y:")
xdisplay = swing.JTextField("x% change (integer)")
ydisplay = swing.JTextField("y% change (integer)")
elabel = swing.JLabel(" Enter beam energy (GeV):")
edisplay = swing.JTextField(" Enter energy value")

labels = ['clear x', 'clear all',      #Naming buttons
          'clear y', 'clear results',
          'clear energy', 'calculate']

error = swing.JTextField("Messages appear here.") #For messages
error.setToolTipText("Gives error messages or info about the point.")              

#magnet stuff
hmlabel = swing.JLabel("  Magnet Name")
hvlabel = swing.JLabel("Value to input (B(T/m)) ")#The spaces make it look 
qh26label = swing.JLabel("  QH26")      #better
qh26display = swing.JTextField()
qh26display.setForeground(awt.Color.blue)#Displays for all variable magnets
qv27label = swing.JLabel("  QV27")
qv27display = swing.JTextField()
qv27display.setForeground(awt.Color.blue)
qh28label = swing.JLabel("  QH28")
qh28display = swing.JTextField()
qh28display.setForeground(awt.Color.blue)
qv29label = swing.JLabel("  QV29")
qv29display = swing.JTextField()
qv29display.setForeground(awt.Color.blue)
qh30label = swing.JLabel("  QH30")
qh30display = swing.JTextField()
qh30display.setForeground(awt.Color.blue)

magnets = [hmlabel, hvlabel,        #Makes it easier to put in a grid
           qh26label, qh26display,
           qv27label, qv27display,
           qh28label, qh28display,
           qv29label, qv29display,
           qh30label, qh30display]

icon = swing.ImageIcon( getPathToLocalFile( "yxoffchart.jpg" ) ) #Pass/fail chart
image = swing.JLabel(icon)
image.border = swing.BorderFactory.createLineBorder(awt.Color.lightGray)
image.setToolTipText("All data taken with an initial kinetic energy of 0.910GeV. The pass/fail conditions may not apply for all cases.")


constants = swing.JLabel(" Constant Magnet Values in T/m: QH 18t24e & QV 19t25o =")
magc = swing.JTextField("constant value here") #Constant magnet stuff
magc.setEditable(false)

qh26display.setEditable(false) #I want the results to be uneditable
qv27display.setEditable(false)
qh28display.setEditable(false)
qv29display.setEditable(false)
qh30display.setEditable(false)
error.setEditable(false)

bbutton = swing.JButton('Plot') #This button makes the plot and table

#def what buttons do---------------------------------------------------------
rest = [2.114] #our initial value
def magconstant(rest): #The constant magnet values also need to change with
    newe = float(edisplay.text)       #the energy
    T_old = 0.910         
    T_new = newe
    mp = 0.93829   
    pc_old = Math.sqrt(T_old*(T_old+(2.0*mp)))
    pc_new = Math.sqrt(T_new*(T_new+(2.0*mp)))
    rest[0] = 2.114 * (pc_new/pc_old)
    magc.text = "%.3f" % rest[0]

def clrx(event):
    xdisplay.text = ''

def clry(event):
    ydisplay.text = ''

def clrr(event):
    qh26display.text = ''
    qv27display.text = ''
    qh28display.text = ''
    qv29display.text = ''
    qh30display.text = ''

def clra(event):
    xdisplay.text = ''
    ydisplay.text = ''
    qh26display.text = ''
    qv27display.text = ''
    qh28display.text = ''
    qv29display.text = ''
    qh30display.text = ''
    error.text = ''
    edisplay.text = ''

def clre(event):
    edisplay.text = ''

def calc(event):
    error.setForeground(awt.Color.black)#if this is not here the red stays
    try:
        magconstant(rest)  
        newx = int(xdisplay.text)
        newy = int(ydisplay.text)
        results(newkey)
        if 35 < newx < 40:
            if newy == -5:
                add_spike()
            else:
                error.text = hunt(newkey)
    except ValueError:#For if they enter nothing or info of the incorrect
        error.setForeground(dred)#format
        error.text = "You must enter integers for x and y and a number for the energy value for this program to work."

def plt(event): #Makes the plot and the table
    try:
        xBeta[:] = [] #Makes those empty lists so they are ready to hold new
        yBeta[:] = []#data
        elements[:] = []
        setUp()
        plot.removeAllGraphData()#To get rid of previous plots
        plotAns()
        filler.removeAll()#Removes previous table
        makeTable()#Adds the new table
    except ValueError:
        error.text = "Make sure all of the magnet displays are displaying numbers before plotting."

#Putting stuff on panels-----------------------------------------------------
                      
buttons = swing.JPanel(awt.GridLayout(3,2,1,10))
                      
for label in labels:  #Adding most of the buttons
    key = swing.JButton(label)
    if label == 'clear x':
        key.actionPerformed = clrx
    if label == 'clear y':
        key.actionPerformed = clry
    if label == 'clear results':
        key.actionPerformed = clrr
    if label == 'clear all':
        key.actionPerformed = clra
    if label == 'clear energy':
        key.actionPerformed = clre
    if label == 'calculate':
        key.actionPerformed = calc
    buttons.add(key)

bbutton.actionPerformed = plt #Defining what the beta button does

#This is all of the input labels and displays.
xypanel = swing.JPanel(awt.GridLayout(3,2,1,10))
xypanel.add(xlabel) 
xypanel.add(xdisplay)
xypanel.add(ylabel)
xypanel.add(ydisplay)
xypanel.add(elabel)
xypanel.add(edisplay)

#This adds all of the variable magnet info to maggrid.                          
maggrid = swing.JPanel(awt.GridLayout(6,2,1,0))

maggrid.border = swing.BorderFactory.createBevelBorder(swing.border.BevelBorder.RAISED, awt.Color.white, awt.Color.lightGray, awt.Color.lightGray, awt.Color.white)
for magnet in magnets:
    maggrid.add(magnet)

#Adding the image
pimage = swing.JPanel()
pimage.add(image)

#Adding the constant magnets and plotting beta button.
bottom = swing.JPanel()
bottom.add(constants)
bottom.add(magc)
bottom.add(bbutton)
bottom.setToolTipText("This contains the value for the magnets QH18 to QV25.")

#Putting panels on panels-------------------------------------------------
top = swing.JPanel()
top.add(xypanel)
top.add(buttons)

tlpanel = swing.JPanel(awt.BorderLayout())
tlpanel.add("North", top)
tlpanel.add("Center", error)

lpanel = swing.JPanel(awt.BorderLayout())
lpanel.add("North", tlpanel)
lpanel.add("West", maggrid)
lpanel.add("Center", pimage)
lpanel.add("South", bottom)

#---------------------------------------------------------------------------
#Plotting and Table GUI
#---------------------------------------------------------------------------

#I want everthing defined globaly so I can display them in the GUI before
#they have information to display.

plot = FunctionGraphsJPanel()
plot.setAxisNames("s (m)", "X Position (mm)")
plot.setPreferredSize(Dimension(620, 290))

filler = JPanel() #The empty table is placed here so I can later just order
table_data = [] #filler to validate itself and it will add the new table.
ColumnNames = ['Element', 'x Beta', 'y Beta']
table = JTable(table_data, ColumnNames)
table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF)
for i in xrange(2):
    column = table.getColumnModel().getColumn(i)
    if i == 0:
        column.setPreferredWidth(150)#The table is too wide if this is 
    if i == 1:                       #not included.
        column.setPreferredWidth(60)
    if i == 2:
        column.setPreferredWidth(60)   
scroll = JScrollPane(table)
scroll.setPreferredSize(Dimension(280, 340))
filler.setPreferredSize(Dimension(280, 340))
filler.add(scroll)
#The table data was repeated so it displays the ColumnNames when the GUI
#first pops up.

#Frame stuff----------------------------------------------------------------
frame = swing.JFrame("Magnet Settings Interpolation")
frame.getContentPane().add(lpanel,BorderLayout.WEST)
frame.getContentPane().add(filler,BorderLayout.CENTER)
frame.getContentPane().add(plot,BorderLayout.SOUTH)
frame.pack()#Makes the frame fit the components.
frame.setDefaultCloseOperation(swing.JFrame.EXIT_ON_CLOSE)
frame.show()  
#If the default close operation is not set, it just hides the program when
#they try to close it.
