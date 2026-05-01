from javax.swing import JTable, DefaultCellEditor, JTextField, ListSelectionModel, JLabel, BorderFactory, JButton, JPanel, BoxLayout, JTabbedPane, GroupLayout, LayoutStyle, JRadioButton, JScrollPane, Box, JCheckBox
from java.lang import String, Short, Object, Boolean, Runnable
from java.awt import Color, Font, Dimension, GridLayout, Component, BorderLayout
from javax.swing.table import AbstractTableModel,TableCellRenderer,DefaultTableCellRenderer
#from java.io import *
#from java.util import *
from java.text import DecimalFormat
import sys, os, math, random, time
from javax.swing.border import TitledBorder
from xal.extension.widgets.plot import GridLimits

#from java.awt.event import *
from javax.swing.event import ListSelectionListener
from java.awt.font import TextAttribute
from java.lang import Thread
from xal.extension.fit import GaussianSinusoidFit
import random
from random import randrange





from xal.extension.widgets.plot import BasicGraphData, FunctionGraphsJPanel




def action0(event):
    print "action0"
    return

class PhaseSpaceOrbit(AbstractTableModel):
	def __init__(self, ro):
            self.columnNames = ["","closed orbit params"]
            self.ro = ro
		
	def getColumnCount(self):
            return len(self.columnNames)
		
	def getRowCount(self):
            return 4
		
	def getColumnName(self,col):
            return self.columnNames[col]
		
	def getValueAt(self, row, col):
            if col == 0:
                return ["<html>&Delta;x (mm)</html>","<html>&Delta;xp (mrad)</html>","<html>&Delta;y (mm)</html>","<html>&Delta;yp (mrad)</html>"][row]
            if col == 1:
                return [str(self.ro.dxf)[0:8],str(self.ro.dxpf)[0:8],str(self.ro.dyf)[0:8],str(self.ro.dypf)[0:8]][row]

	def setValueAt(self, value, row, col):
            return

	def getColumnClass(self,col):
            return [String, String][col]
	
	def isCellEditable(self,row,col):
            return False





class CalculationOptions(AbstractTableModel):
	def __init__(self, ro, pane):
            self.ro = ro
            self.columnNames = ["parameter","value"]
            self.paramNames = ["beam energy T (MeV)","waveform points","number of pulses"]
            self.pane = pane
		
	def getColumnCount(self):
            return len(self.columnNames)
		
	def getRowCount(self):
            return len(self.paramNames)
	
	def getColumnName(self,col):
            return self.columnNames[col]
		
	def getValueAt(self,row,col):
            if col == 0:
                return self.paramNames[row]
            if col == 1:
                return [str(self.ro.d['TkinNominalMeV'])[0:8], str(self.ro.d['WFLength']), str(self.ro.d['Npulses'])][row]
        
	def setValueAt(self, value, row, col):
            if col == 1:
                if row == 0:
                    #T0 = self.ro.d['TkinNominalMeV']
                    T1 = float(value)
                    self.ro.d['TkinNominalMeV'] = float(value)
                    #self.ro.rescaleMagnetsToEnergy(T0, T1)
                    
                    #self.pane.table1.getModel().fireTableDataChanged()
                    #self.pane.table6.getModel().fireTableDataChanged()
                    #self.pane.table7.getModel().fireTableDataChanged()
                    
                if row == 1:
                    self.ro.d['WFLength'] = int(value)
                if row == 2:
                    self.ro.d['Npulses'] = int(value)
            return

	def getColumnClass(self,col):
            return [String, String][col]
	
	def isCellEditable(self,row,col):
            return col == 1




class TuneParameters(AbstractTableModel):
	def __init__(self, ro):
            self.ro = ro
            self.paramNames = ["X Tune","Y Tune"]
            self.columnNames = [None,"model","frac. meas."]
		
	def getColumnCount(self):
            return len(self.columnNames)
		
	def getRowCount(self):
            return len(self.paramNames)
	
	def getColumnName(self,col):
            return self.columnNames[col]
		
	def getValueAt(self,row,col):
            if col == 0:
                return self.paramNames[row]
            if col == 1:
                return [str(self.ro.tuneX)[0:8], str(self.ro.tuneY)[0:8]][row]
            if col == 2:
                
                arrtuneX = [self.ro.dav[kickId]['voltage-1'][bpmId]['tuneX'] for kickId in self.ro.dav.keys() for bpmId in self.ro.dav[kickId]['voltage-1'].keys() if bpmId in self.ro.d['useBPMs']]
                arrtuneY = [self.ro.dav[kickId]['voltage-1'][bpmId]['tuneY'] for kickId in self.ro.dav.keys() for bpmId in self.ro.dav[kickId]['voltage-1'].keys() if bpmId in self.ro.d['useBPMs']]
                    
                return [str(sum(arrtuneX) / len(arrtuneX) )[0:8], str(sum(arrtuneY) / len(arrtuneY))[0:8]][row]
        
	def setValueAt(self, value, row, col):
            return

	def getColumnClass(self,col):
            return [String, String, String][col]
	
	def isCellEditable(self,row,col):
            return False



class IKickerData(AbstractTableModel):
	def __init__(self, ro):
            self.ro = ro
            self.columnNames = ["Kicker ID","Control voltage (V)","<html>Voltage variance &Delta;V</html>", "date / time"]
		
	def getColumnCount(self):
            return len(self.columnNames)
		
	def getRowCount(self):
            return len(self.ro.IkickIds)
		
	def getColumnName(self, col):
            return self.columnNames[col]
		
	def getValueAt(self,row,col):
            id = self.ro.IkickIds[row]
            V2 = self.ro.d['IKickParams'][id]['voltage-2']
            V1 = self.ro.d['IKickParams'][id]['voltage-1']
            return[id, str(V1)[0:8],str(V2 - V1)[0:8], self.ro.d['IKickParams'][id]['date/time']][col]

	def setValueAt(self, value, row, col):
            id = self.ro.IkickIds[row]
            #if col == 0:
                #self.ro.ikb[row] = Boolean(value)
            if col == 2:
                self.ro.d['IKickParams'][id]['voltage-2'] = self.ro.d['IKickParams'][id]['voltage-1'] + float(value)
            return

	def getColumnClass(self,col):
            return [String, String, String, String,String][col]
	
	def isCellEditable(self,row,col):
            return col in [2,3]
        
class PhaseSpaceFoilData(AbstractTableModel):
	def __init__(self, ro, ikp):
            self.ro = ro
            self.ikp = ikp
            self.columnNames = ["Kicker ID","x (mm)","xp (mrad)","y (mm)", "yp (mrad)","plot",""]
		
	def getColumnCount(self):
            return len(self.columnNames)
		
	def getRowCount(self):
            return len(self.ro.IkickIds)
		
	def getColumnName(self, col):
            return self.columnNames[col]
		
	def getValueAt(self,row,col):
            
            id = self.ro.IkickIds[row]
            
            xarr = [self.ro.dav[id]['voltage-1'][bpmId]['foil-X'] for bpmId in self.ro.dav[id]['voltage-1'].keys() if bpmId in self.ro.d['useBPMs']]
            xparr = [self.ro.dav[id]['voltage-1'][bpmId]['foil-XP'] for bpmId in self.ro.dav[id]['voltage-1'].keys() if bpmId in self.ro.d['useBPMs']]
            yarr = [self.ro.dav[id]['voltage-1'][bpmId]['foil-Y'] for bpmId in self.ro.dav[id]['voltage-1'].keys() if bpmId in self.ro.d['useBPMs']]
            yparr = [self.ro.dav[id]['voltage-1'][bpmId]['foil-YP'] for bpmId in self.ro.dav[id]['voltage-1'].keys() if bpmId in self.ro.d['useBPMs']]

            return[id, str(sum(xarr) / len(xarr))[0:8],str(sum(xparr) / len(xparr) )[0:8], str(sum(yarr) / len(yarr) )[0:8],str(sum(yparr) / len(yparr))[0:8], Boolean(self.ro.plotFoil[row]),""][col]

	def setValueAt(self, value, row, col):
 
            self.ro.plotFoil[row] = value
            self.ikp.plotIKickPhaseSpaceFoil()

            return

	def getColumnClass(self,col):            
            return [String, String, String, String, String, Boolean, String][col]
	
	def isCellEditable(self,row,col):
            return col == 5 or col == 6
        
    
class ColorCellRenderer(TableCellRenderer):
    def __init__(self, colors):
        self.colors = colors
        return
    
    def getTableCellRendererComponent(self, table, value, isSelected, hasFocus, row, col):
        
        DTCR = DefaultTableCellRenderer()
        comp = DTCR.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col)
        if col == 6:
            comp.setBackground( self.colors[row] )
        return comp

     

        
        
        
class IKickerData3(AbstractTableModel):
	def __init__(self, ro):
            self.ro = ro
            self.columnNames = ["IKick id1","<html>&Delta;V1</html>","IKick id2","<html>&Delta;V2</html>","IKick id3","<html>&Delta;V3</html>","status","Kick id","rel. coeff.", "actions (use from top to bottom)"]
		
	def getColumnCount(self):
            return len(self.columnNames)
		
	def getRowCount(self):
            return len(self.ro.d['TripleScan'].keys())
		
	def getColumnName(self, col):
            return self.columnNames[col]
		
	def getValueAt(self, row, col):
            
            trkeys = self.ro.d['TripleScan'].keys()
            
            if col == 0 or col == 2 or col == 4:
                #kick = self.ro.d['TripleScan'][trkeys[row]]
                #kick = self.ro.trs.childAdaptors().get(row).childAdaptors().get(col/2)
                return self.ro.d['TripleScan']['scan-'+str(row)]['ids'][col/2][9:]
            
            if col == 1 or col == 3 or col == 5:
                #kick = self.ro.trs.childAdaptors().get(row).childAdaptors().get(col/2)
                return str(self.ro.d['TripleScan']['scan-'+str(row)]['dV'][col/2])[0:8]
            
            if col == 6:
                return str(self.ro.d['TripleScan']['scan-'+str(row)]['status'])
            
            if col == 7:
                return self.ro.IkickIds[row][9:]
            
            if col == 8:
                return str(self.ro.relativeCoeff[self.ro.IkickIds[row]])[0:8]
            
            return
        

	def setValueAt(self, value, row, col):
            
            
            if col == 1:
                #kick = self.ro.trs.childAdaptors().get(row).childAdaptors().get(0)
                #kick.setValue("dV",float(value))
                self.ro.d['TripleScan']['scan-'+str(row)]['dV'][0] = float(value)

            return

	def getColumnClass(self,col):
            return [String, String, String, String, String, String, String, String, String, String][col]
	
	def isCellEditable(self,row,col):
            return col == 1
        
        
class QuadsCorrections(AbstractTableModel):
	def __init__(self, ro):
            self.columnNames = ["Quads PS","BL correction"]
            self.ro = ro
		
	def getColumnCount(self):
            return len(self.columnNames)
		
	def getRowCount(self):
            return len(self.ro.psIds)
		
	def getColumnName(self,col):
            return self.columnNames[col]
		
	def getValueAt(self,row,col):
            
            return [self.ro.psIds[row], str(abs(self.ro.d['BLQuadCorrections'][self.ro.psIds[row]]))[0:8]][col]
    
	def setValueAt(self, value, row, col):
            return

	def getColumnClass(self,col):
            return [String, String][col]
	
	def isCellEditable(self,row,col):
            return False
        

class IKickCalibration(AbstractTableModel):
	def __init__(self, ro):
            self.columnNames = ["IKick Id","coeff. (mrad/V)"]
            self.ro = ro
		
	def getColumnCount(self):
            return len(self.columnNames)
		
	def getRowCount(self):
            return len(self.ro.IkickIds)
		
	def getColumnName(self,col):
            return self.columnNames[col]
		
	def getValueAt(self,row,col):
            
            valout = ""
            val = self.ro.d['IKickParams'][self.ro.IkickIds[row]]['mrad/V']
            if val >= 0:
                valout = "+%s"%(str(abs(val))[0:8])
            if val < 0:
                valout = "<html>&minus;%s</html>"%(str(abs(val))[0:8])
            return [self.ro.IkickIds[row], valout][col]

	def setValueAt(self, value, row, col):
            return

	def getColumnClass(self,col):
            return [String, String][col]
	
	def isCellEditable(self,row,col):
            return False



class BPMsData(AbstractTableModel):
	def __init__(self, ro):
            self.ro = ro
            self.columnNames = ["Use BPMs"]
		
	def getColumnCount(self):
            return len(self.columnNames)
		
	def getRowCount(self):
            return len(self.ro.allBPMsIds)
		
	def getColumnName(self,col):
            return self.columnNames[col]
		
	def getValueAt(self,row, col):
            return self.ro.allBPMsIds[row]


	def setValueAt(self, value, row, col):
            return

	def getColumnClass(self,col):
            return [String][col]
	
	def isCellEditable(self,row,col):
            return False
        
        
class PulsesDataV1(AbstractTableModel):
	def __init__(self, pane):
            
            self.pane = pane
            self.ro = pane.ro
            self.columnNames = ["<html>V</html>"]

	def getColumnCount(self):
            return 1
		
	def getRowCount(self):  
            
            kickInd = self.pane.table1.getSelectedRow()
            kickId = self.ro.IkickIds[kickInd]
            
            if kickId in self.ro.d['BPMWaveForms'].keys():
                if 'voltage-1' in self.ro.d['BPMWaveForms'][kickId].keys():
                    return len(self.ro.d['BPMWaveForms'][kickId]['voltage-1'].keys())
    
            return 0
                    
		
	def getColumnName(self,col):
            return self.columnNames[col]
		
	def getValueAt(self, row, col):
            if self.getRowCount() > 0:
                return 'pulse-' + str(row)

	def setValueAt(self, value, row, col):
            return

	def getColumnClass(self,col):
            return [String][col]
	
	def isCellEditable(self,row,col):
            return False
        
class PulsesDataV2(AbstractTableModel):
	def __init__(self, pane):
            
            self.pane = pane
            self.ro = pane.ro

            self.columnNames = ["<html>V+&Delta;V</html>"]
            	
	def getColumnCount(self):
            return 1
		
	def getRowCount(self):  
            
            kickInd = self.pane.table1.getSelectedRow()
            kickId = self.ro.IkickIds[kickInd]
            
            if kickId in self.ro.d['BPMWaveForms'].keys():
                if 'voltage-2' in self.ro.d['BPMWaveForms'][kickId].keys():
                    return len(self.ro.d['BPMWaveForms'][kickId]['voltage-2'].keys())
    
            return 0
                    
		
	def getColumnName(self,col):
            return self.columnNames[col]
		
	def getValueAt(self, row, col):
            if self.getRowCount() > 0:
                return 'pulse-' + str(row)

	def setValueAt(self, value, row, col):
            return

	def getColumnClass(self,col):
            return [String][col]
	
	def isCellEditable(self,row,col):
            return False

        

class MonitorWF(AbstractTableModel):
	def __init__(self, ro):
            self.ro = ro
            self.columnNames = ["waveform"]
		
	def getColumnCount(self):
            return len(self.columnNames)
		
	def getRowCount(self):
            return len(self.ro.allBPMsIds)
		
	def getColumnName(self,col):
            return self.columnNames[col]
		
	def getValueAt(self,row,col):
            return

	def setValueAt(self, value, row, col):
            return

	def getColumnClass(self,col):
            return [String][col]
	
	def isCellEditable(self,row,col):
            return False
        

class MonitorPulseWF(AbstractTableModel):
	def __init__(self, pane):
            self.ro = pane.ro
            self.pane = pane
            self.columnNames = ["waveform"]
            self.kickId = self.ro.IkickIds[0]
		
	def getColumnCount(self):
            return len(self.columnNames)
		
	def getRowCount(self):
            
            kickInd = self.pane.table1.getSelectedRow()
            kickId = self.ro.IkickIds[kickInd]
            
            if kickId in self.ro.d['BPMWaveForms'].keys():
                if len(self.ro.d['BPMWaveForms'][kickId].keys()) == 2:
                    num1 = len(self.ro.d['BPMWaveForms'][kickId]['voltage-1'].keys())
                    num2 = len(self.ro.d['BPMWaveForms'][kickId]['voltage-2'].keys())
                    if num1 == num2:
                        return num1
            return 0

		
	def getColumnName(self,col):
            return self.columnNames[col]
		
	def getValueAt(self,row,col):
            return

	def setValueAt(self, value, row, col):
            return

	def getColumnClass(self,col):
            return [String][col]
	
	def isCellEditable(self,row,col):
            return False
        



class IKickPane:
    
    def __init__(self, ro):
        
        self.ro = ro
        
        self.colors = [Color.GRAY, Color.BLUE, Color.CYAN, Color.GREEN.darker(), Color.MAGENTA, Color.ORANGE, Color.PINK, Color.RED.darker()]
        
        plotTunes = self.plotTunes
        plotClosedOrbits = self.plotClosedOrbits
        plotWaveform = self.plotWaveform

        updatePulsetable = self.updatePulsetable
        plotPulseWaveform = self.plotPulseWaveform
        updatePulseselection = self.updatePulseselection
        plotIKickPhaseSpaceFoil = self.plotIKickPhaseSpaceFoil
        
        class monitorIKickTable(JTable):
            def changeSelection(self, rowIndex, columnIndex, toggle, extend):
                JTable.changeSelection(self, rowIndex, columnIndex, toggle, extend)

                kickInd = table1.getSelectedRow()
                kickId = ro.IkickIds[kickInd]

                bpmInd = table8.getSelectedRow()
                bpmId = ro.allBPMsIds[bpmInd]
                
                #plotTunes()
                plotClosedOrbits(kickId)
                plotWaveform(kickId, bpmId)
                updatePulsetable(kickId)                
                updatePulseselection(kickId)
                
                if table12.getModel().getRowCount() > 0:                                
                    pulseInd = table12.getSelectedRow()
                    plotPulseWaveform(kickId, bpmId, pulseInd)                
                
                return
            

            
            
        class monitorBPMTable(JTable):
            def changeSelection(self, rowIndex, columnIndex, toggle, extend):
                JTable.changeSelection(self, rowIndex, columnIndex, toggle, extend)

                kickInd = table1.getSelectedRow()
                kickId = ro.IkickIds[kickInd]

                bpmInd = table8.getSelectedRow()
                bpmId = ro.allBPMsIds[bpmInd]
                
                plotWaveform(kickId, bpmId)

                
                if table12.getModel().getRowCount() > 0:
                    pulseInd = table12.getSelectedRow()
                    plotPulseWaveform(kickId, bpmId, pulseInd)
                
                return
            
        class monitorPulseTable(JTable):
            def changeSelection(self, rowIndex, columnIndex, toggle, extend):
                JTable.changeSelection(self, rowIndex, columnIndex, toggle, extend)

                kickInd = table1.getSelectedRow()
                kickId = ro.IkickIds[kickInd]

                bpmInd = table8.getSelectedRow()
                bpmId = ro.allBPMsIds[bpmInd]
                
                if table12.getModel().getRowCount() > 0:
                    pulseInd = table12.getSelectedRow()
                    plotPulseWaveform(kickId, bpmId, pulseInd)

                
                return
        
        ikd = IKickerData(self.ro)
        self.table1 = monitorIKickTable(ikd)
        table1 = self.table1
        self.table1.setRowSelectionInterval(0, 0)
        #self.table1.setRowHeight(20)
        
        singleclick = DefaultCellEditor(JTextField())
        singleclick.setClickCountToStart(1)
        self.table1.setDefaultEditor(self.table1.getColumnClass(2), singleclick)
        self.table1.setDefaultEditor(self.table1.getColumnClass(3), singleclick) 
        
        cellSelectionModel1 = self.table1.getSelectionModel()
        cellSelectionModel1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)


        

        
        
        
        self.ikd3 = IKickerData3(self.ro)
        table3 = JTable(self.ikd3)
        table3.setRowSelectionInterval(0, 0)
        
        singleclick = DefaultCellEditor(JTextField())
        singleclick.setClickCountToStart(1)
        table3.setDefaultEditor(table3.getColumnClass(1), singleclick)
        
        
        col7 = table3.getColumnModel().getColumn(7)
        colrenderer7 = DefaultTableCellRenderer()
        colrenderer7.setHorizontalAlignment( JLabel.RIGHT )
        colrenderer7.setFont(Font("Times", Font.BOLD, 24))
        #table3.setDefaultRenderer(String, colrenderer7)
        col7.setCellRenderer(colrenderer7)
        
        #table3.setDefaultEditor(table3.getColumnClass(3), singleclick)

        
        #col = self.table1.getColumnModel().getColumn(1)
        #col.setHeaderRenderer(EditableHeaderRenderer(JButton("Button")))
        
        #self.table1.getTableHeader().setPreferredSize(Dimension(0,40))
           
        #self.table1.setAutoResizeMode(JTable.AUTO_RESIZE_OFF)
        #self.table1.setFocusable(False)
        
        self.table5 = JTable(CalculationOptions(self.ro, self))
        
        self.table5.setFocusable(False)
        self.table5.setRowSelectionAllowed(False)
        
        singleclick = DefaultCellEditor(JTextField())
        singleclick.setClickCountToStart(1)
        self.table5.setDefaultEditor(self.table5.getColumnClass(1), singleclick)
        

        
        blackline = BorderFactory.createLineBorder(Color.black)
        title5 = BorderFactory.createTitledBorder(blackline, "title")
        title5.setTitleJustification(TitledBorder.CENTER)
        
        
        self.table7 = JTable(TuneParameters(self.ro))
        self.table7.setFocusable(False)
        self.table7.setRowSelectionAllowed(False)
        

        table5 = self.table5
        table7 = self.table7
        
        class selectBPMTable(JTable):
            def changeSelection(self, rowIndex, columnIndex, toggle, extend):
                JTable.changeSelection(self, rowIndex, columnIndex, True, extend)
                                
                ro.d['useBPMs'] = [ro.allBPMsIds[i] for i in range(len(ro.allBPMsIds)) if self.isRowSelected(i)]
                
                kickInd = table1.getSelectedRow()
                kickId = ro.IkickIds[kickInd]
                
                plotTunes()
                table7.getModel().fireTableDataChanged()
                
                plotClosedOrbits(kickId)
                plotIKickPhaseSpaceFoil()
                
                table14.getModel().fireTableDataChanged()

                return
            
            

        centerRenderer = DefaultTableCellRenderer()
        centerRenderer.setHorizontalAlignment( JLabel.CENTER )

        

        
        #buttonRenderer = JTableButtonRenderer()
        #self.table1.getColumn(7).setCellRenderer(buttonRenderer)
        #table2 = JTable(bpmd)
        bpmd = BPMsData(self.ro)
        self.table2 = selectBPMTable(bpmd)
        self.table2.setFocusable(False)
        self.table2.getColumnModel().getColumn(0).setMinWidth(140)
        self.table2.getColumnModel().getColumn(0).setMaxWidth(140)
        
        wfd = MonitorWF(self.ro)
        self.table8 = monitorBPMTable(wfd)        
        table8 = self.table8
        self.table8.setRowSelectionInterval(0, 0)
        #self.table8.setFocusable(False)
        self.table8.getColumnModel().getColumn(0).setMinWidth(70)
        self.table8.getColumnModel().getColumn(0).setMaxWidth(70)
       

        self.updateBPMselection()
        
        
        
        cellSelectionModel8 = self.table8.getSelectionModel()
        cellSelectionModel8.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
        
        
        
        class selectPulsesTableV1(JTable):
            def changeSelection(self, rowIndex, columnIndex, toggle, extend):
                JTable.changeSelection(self, rowIndex, columnIndex, True, extend)
                
                kickInd = table1.getSelectedRow()
                kickId = ro.IkickIds[kickInd]

                bpmInd = table8.getSelectedRow()
                bpmId = ro.allBPMsIds[bpmInd]
                
                
                ro.d['IKickParams'][kickId]['usepulses']['voltage-1'] = ['pulse-' + str(i) for i in range(len(ro.d['BPMWaveForms'][kickId]['voltage-1'].keys())) if self.isRowSelected(i)]
                ro.dav[kickId]['voltage-1'] = ro.getFoilParameters(ro.d['BPMWaveForms'][kickId]['voltage-1'], ro.d['IKickParams'][kickId]['usepulses']['voltage-1'])
                
                plotWaveform(kickId, bpmId)
                                

                plotTunes()
                table7.getModel().fireTableDataChanged()
                
                plotClosedOrbits(kickId)
                
                plotIKickPhaseSpaceFoil()
                table14.getModel().fireTableDataChanged()

                return
            
        class selectPulsesTableV2(JTable):
            def changeSelection(self, rowIndex, columnIndex, toggle, extend):
                JTable.changeSelection(self, rowIndex, columnIndex, True, extend)
                
                kickInd = table1.getSelectedRow()
                kickId = ro.IkickIds[kickInd]

                bpmInd = table8.getSelectedRow()
                bpmId = ro.allBPMsIds[bpmInd]
                
                ro.d['IKickParams'][kickId]['usepulses']['voltage-2'] = ['pulse-' + str(i) for i in range(len(ro.d['BPMWaveForms'][kickId]['voltage-2'].keys())) if self.isRowSelected(i)]
                ro.dav[kickId]['voltage-2'] = ro.getFoilParameters(ro.d['BPMWaveForms'][kickId]['voltage-2'], ro.d['IKickParams'][kickId]['usepulses']['voltage-2'])
                
                
                plotWaveform(kickId, bpmId)
                                

                plotTunes()
                table7.getModel().fireTableDataChanged()
                
                plotClosedOrbits(kickId)
                
                plotIKickPhaseSpaceFoil()
                table14.getModel().fireTableDataChanged()

                return
        
        
        pulsesv1 = PulsesDataV1(self)
        self.table9 = selectPulsesTableV1(pulsesv1)
        self.table9.setFocusable(False)
        self.table9.setDefaultRenderer(String, centerRenderer)
        self.table9.getColumnModel().getColumn(0).setMinWidth(70)
        self.table9.getColumnModel().getColumn(0).setMaxWidth(70)
        

        pulsesv2 = PulsesDataV2(self)
        self.table11 = selectPulsesTableV2(pulsesv2)
        self.table11.setFocusable(False)
        self.table11.setDefaultRenderer(String, centerRenderer)
        self.table11.getColumnModel().getColumn(0).setMinWidth(70)
        self.table11.getColumnModel().getColumn(0).setMaxWidth(70)
                
        wfsd = MonitorPulseWF(self)
        self.table12 = monitorPulseTable(wfsd)
        table12 = self.table12
        #self.table12.setRowSelectionInterval(0, 0)
        #self.table10.setFocusable(False)'
        self.table12.getColumnModel().getColumn(0).setMinWidth(70)
        self.table12.getColumnModel().getColumn(0).setMaxWidth(70)
        
        cellSelectionModel12 = self.table12.getSelectionModel()
        cellSelectionModel12.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
        
        
        self.updatePulsetable(ro.IkickIds[0])
        self.updatePulseselection( ro.IkickIds[0])

        
        """
        wfd = MonitorWF(self.ro)
        self.table8 = monitorIKickTable(wfd)        
        table8 = self.table8
        self.table8.setRowSelectionInterval(0, 0)
        self.table8.setFocusable(False)
        self.table8.getColumnModel().getColumn(0).setMinWidth(40)
        self.table8.getColumnModel().getColumn(0).setMaxWidth(40)
        
        cellSelectionModel8 = self.table8.getSelectionModel()
        cellSelectionModel8.setSelectionmonitorIKickTableMode(ListSelectionModel.SINGLE_SELECTION)
        """
        
        #cellSelectionModel = self.table2.getSelectionModel()
        #cellSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
        
        #col1 = self.table2.getColumnModel().getColumn(1).getSelectionModel()
        
        #self.table2 = JTable(bpmd)
        #table2.getColumnModel().getColumn(0).setPreferredWidth(140)

        #table2.setAutoResizeMode(JTable.AUTO_RESIZE_ON)
        
        #self.table2.setFocusable(False)
        
        

        self.table2.setDefaultRenderer(String, centerRenderer)
        #table2.getColumnModel().getColumn(0).getCellRenderer().setHorizontalAlignment( JLabel.CENTER )
        


        
        

        

        table4 = JTable(QuadsCorrections(self.ro))
        table4.getColumnModel().getColumn(1).setMinWidth(110)
        table4.getColumnModel().getColumn(1).setMaxWidth(110)
        #buttonRenderer = JTableButtonRenderer()
        #table4.getColumn(1).setCellRenderer(buttonRenderer)
        #table4.getColumnModel().getColumn(0).setPreferredWidth(250)
        table4.setFocusable(False)
        table4.setRowSelectionAllowed(False)
        

        
        
        self.table6 = JTable(IKickCalibration(self.ro))
        self.table6.getColumnModel().getColumn(1).setMinWidth(110)
        self.table6.getColumnModel().getColumn(1).setMaxWidth(110)
        #buttonRenderer = JTableButtonRenderer()
        #table4.getColumn(1).setCellRenderer(buttonRenderer)
        #table4.getColumnModel().getColumn(0).setPreferredWidth(250)
        self.table6.setFocusable(False)
        self.table6.setRowSelectionAllowed(False)



        #self.table5.setBorder(title5)

        
        #self.table1.setRowSelectionAllowed(False)
        #self.table1.setFocusable(False)
        #table2.getColumnModel().getColumn(0).setPreferredWidth(50)
        #table2.setRowHeight(0, 30)
        #table2.setRowHeight(1, 30)
        #table2.setFont(Font("Arial", 0, 20))



        th1 = self.table1.getTableHeader()
        
        #th1.setBackground(Color(153, 204, 255))
        #th1.setForeground(Color.white)
        #th1.setFont(Font("Arial", Font.BOLD, 14))
        
        th2 = self.table2.getTableHeader()
        #th2.setBackground(Color(153, 204, 255))
        #th1.setForeground(Color.white)
        #th2.setFont(Font("Arial", Font.BOLD, 14))
        
        th3 = table3.getTableHeader()
        #th3.setBackground(Color(153, 204, 255))
        #th1.setForeground(Color.white)
        #th3.setFont(Font("Arial", Font.BOLD, 14))
        
        th4 = table4.getTableHeader()
        #th4.setBackground(Color(153, 204, 255))
        #th1.setForeground(Color.white)
        #th4.setFont(Font("Arial", Font.BOLD, 14))
        
        th6 = self.table6.getTableHeader()
        #th4.setBackground(Color(153, 204, 255))
        #th1.setForeground(Color.white)
        #th6.setFont(Font("Arial", Font.BOLD, 14))

        #table.getColumnModel().getColumn(1).setCellRenderer(DecimalFormatRenderer() )

        #nr = NumberRenderer()
        #mod.getColumn(1).setCellRenderer(nr)








        def action1(event):
            return
        def action2(event):
            return
        def action3(event):
            return
        
        def actionIKick1(event):
            class Thr(Runnable):
                def run(self):
                    _actionIKick1(event)
            Thread(Thr()).start()  
        def _actionIKick1(event):

            kickInd = self.table1.getSelectedRow()
            self.ro.measureOrbit(self.ro.IkickIds[kickInd], self)
            self.table1.getModel().fireTableCellUpdated(kickInd, 4)

            self.plotTunes()
            self.table7.getModel().fireTableDataChanged()
            self.plotClosedOrbits(self.ro.IkickIds[kickInd])
            
            
            
            
        def actionClosedOrbit(event):
            class Thr(Runnable):
                def run(self):
                    _actionClosedOrbit(event)
            Thread(Thr()).start()  
        def _actionClosedOrbit(event):

            ind = table3.getSelectedRow()
            self.JP.removeAllGraphData()
            Npulses = int(self.NclosedField.getText())
            self.ro.measureClosedOrbit(Npulses, self)
            self.table1.getModel().fireTableCellUpdated(ind, 0)
            
            
            
            
        def optimizekickers(event):
            class Thr(Runnable):
                def run(self):
                    _optimizekickers(event)
            Thread(Thr()).start()
        def _optimizekickers(event):
            
            trKickInd = table3.getSelectedRow()
            self.JP.removeAllGraphData()
            ro.optimizeOrbit(trKickInd, self)


            
            
            
            
        def calculatekickers(event):

                
            trKickInd = table3.getSelectedRow()

            ro.tripleValueEstimate(trKickInd)
            self.ikd3.fireTableCellUpdated(trKickInd, 3)
            self.ikd3.fireTableCellUpdated(trKickInd, 5)
            self.ikd3.fireTableCellUpdated(trKickInd, 6)

            
            

            
            

        def actionCalculate(event):
            class Thr(Runnable):
                def run(self):
                    _actionCalculate(event)
            Thread(Thr()).start()  
        def _actionCalculate(event):
            
            ro.analyse(useMeasuredTunes.isSelected(),useCoupledIKicks.isSelected(),progressLabel) 
            table4.getModel().fireTableDataChanged()
            self.table7.getModel().fireTableDataChanged()
            self.table6.getModel().fireTableDataChanged()
            
            self.plotTunes()
            
            self.plotIKickPhaseSpaceFoil()
            self.table14.getModel().fireTableDataChanged()
            
            #kickInd = table1.getSelectedRow()
            #kickId = ro.IkickIds[kickInd]
            #plotClosedOrbits(kickId)

            
            
        def actionGenerate(event):
            class Thr(Runnable):
                def run(self):
                    _actionGenerate(event)
            Thread(Thr()).start()  
        def _actionGenerate(event):
    
            ro.generateRingxdxf()

            
            
            
            
            
        def stopOptimization(event):
            class Thr(Runnable):
                def run(self):
                    _stopOptimization(event)
            Thread(Thr()).start()  
        def _stopOptimization(event):
            ro.stopOptimization()


        def analyseIKickers(event):
            class Thr(Runnable):
                def run(self):
                    _analyseIKickers(event)
            Thread(Thr()).start()  
        def _analyseIKickers(event):
            ro.analyseIKickers()
            for ind in range(8):
                table3.getModel().fireTableCellUpdated(ind, 8)
            

       


        b1 = JButton("name 1",actionPerformed = action1)
        b2 = JButton("name 2",actionPerformed = action2)
        b3 = JButton("name 3",actionPerformed = action3)
        #b3.setPreferredSize(Dimension(100, 100))
        
        
        #self.panel.setLayout(BoxLayout(self.panel, BoxLayout.X_AXIS))
        



        #self.panel1 = JPanel()
        #self.panel1.setLayout(BoxLayout(self.panel1, BoxLayout.Y_AXIS))
        
        
        

        
        


        blackline = BorderFactory.createLineBorder(Color.black)
        title1 = BorderFactory.createTitledBorder(blackline, "Measurement of the closed orbit")
        title1.setTitleJustification(TitledBorder.CENTER)
        title2 = BorderFactory.createTitledBorder(blackline, "Use BPMs, Use pulses, monitor waveforms")
        title2.setTitleJustification(TitledBorder.CENTER)
        title3 = BorderFactory.createTitledBorder(blackline, "Measurement of 3 Kicker change with conservation of Ring orbit")
        title3.setTitleJustification(TitledBorder.CENTER)
        title4 = BorderFactory.createTitledBorder(blackline, "Ring model calibration")
        title4.setTitleJustification(TitledBorder.CENTER)
        
        
        panelbpm = JPanel()
        panelbpm.setLayout(BoxLayout(panelbpm, BoxLayout.X_AXIS))
        #panelbpm.setPreferredSize(Dimension(140 + 40 + 10 + 60 + 40 + 60 + 40 + 30, 1000000))
        panelbpm.setBorder(title2)
        
        

        #panelbpm.setMaximumSize(Dimension(500,1000))
        #panelbpm.setPreferredSize(Dimension(140 + 40 + 10 + 140 + 10,2000))



        #paneltg = JPanel()
        #paneltg.setLayout(BoxLayout(paneltg, BoxLayout.X_AXIS))
        #paneltg.setBorder(title1)
        #paneltg.setPreferredSize(Dimension(1000000, self.table1.getRowHeight()*(self.table1.getRowCount()+2)+5))
        
        
        paneTables = JTabbedPane()

        panelt = JPanel()
        panelt.setLayout(BoxLayout(panelt, BoxLayout.Y_AXIS))
        #panelt.setBorder(EmptyBorder(20, 0, 0, 0))
        #panelt.setBorder(title1)

        
        paneTables.add("Kickers scan",panelt)
        
        
        
        panelopt = JPanel()
        panelopt.setLayout(BoxLayout(panelopt, BoxLayout.Y_AXIS))
        panelopt.setPreferredSize(Dimension(310, 1000000))
        panelopt.setBorder(title4)
        
        
        
        closedOrbitBut = JButton("<html>Measure Closed Orbit</html>",actionPerformed = actionClosedOrbit)
        CalculateBut = JButton("<html>Estimate initial &Delta;V2,&Delta;V3</html>",actionPerformed = calculatekickers)
        OptimizeBut = JButton("<html>Optimize &Delta;V2,&Delta;V3</html>",actionPerformed = optimizekickers)
        StopOptimizationBut = JButton("<html>Stop optimization</html>",actionPerformed = stopOptimization)
        AnalyseIKickBut = JButton("<html>Analyse rel. coeff.</html>",actionPerformed = analyseIKickers)
        self.NclosedField = JTextField("10")
        
        lab = JLabel("Cl.orb. N aver:")
        lab.setHorizontalAlignment(JLabel.RIGHT)
        
        plabel = JPanel(GridLayout(1,2))
        plabel.add(lab)
        plabel.add(self.NclosedField)
        
        #self.NclosedField.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Closed orbit pulses:"))
        
        



        bpanel = JPanel(GridLayout(6,1,0,1))
        #bpanel.add(lab)
        bpanel.add(plabel)
        bpanel.add(closedOrbitBut)
        bpanel.add(CalculateBut)
        bpanel.add(OptimizeBut)
        bpanel.add(StopOptimizationBut)
        bpanel.add(AnalyseIKickBut)
        
        bpanel.setMaximumSize(Dimension(230, table3.getRowCount()*table3.getRowHeight()))
        bpanel.setMinimumSize(Dimension(230, table3.getRowCount()*table3.getRowHeight()))
        
        table3.getColumnModel().getColumn(9).setMinWidth(230)
        table3.getColumnModel().getColumn(9).setMaxWidth(230)
        
        layout3 = GroupLayout(table3)
        table3.setLayout(layout3)
        
        layout3.setHorizontalGroup(layout3.createSequentialGroup().addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(bpanel))
        layout3.setVerticalGroup(layout3.createSequentialGroup().addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(bpanel))



        panelt1 = JPanel()
        panelt1.setLayout(BoxLayout(panelt1, BoxLayout.Y_AXIS))
        #panelt1.setBorder(title3)
        #panelt1.add(bpanel)
        #panelt1.add(lab)
        panelt1.add(table3.getTableHeader())
        panelt1.add(table3)
        
        ###########paneTables.add("3 Kicker optimization",panelt1)

        #panelgb = JPanel()
        #panelgb.setLayout(GridBagLayout())
        #c = GridBagConstraints()
        #c.gridx = 0
        
        #textMeasure = JLabel("Measure",JTextField.CENTER)
        #textMeasure.setFont(Font("Arial", Font.BOLD, 12))
        #textMeasure.setPreferredSize(Dimension(200-2, self.table1.getRowHeight()-2))

        #c.gridy = 0
        #c.insets = Insets(1,1,1,1)
        #panelgb.add(textMeasure, c)
        #for id in self.ro.IkickIds:
        #    button = JButton(id,actionPerformed = actionIKick)
        #    button.setPreferredSize(Dimension(200-2, self.table1.getRowHeight()-2))
        #    c.gridy += 1
        #    panelgb.add(button, c)
            

        #panelgb.setMaximumSize(Dimension(200, self.table1.getRowHeight()*(self.table1.getRowCount()+1)))

        #panelgb.setAlignmentY(Component.BOTTOM_ALIGNMENT)
        #panelt.setAlignmentY(Component.BOTTOM_ALIGNMENT)
        
        #paneltg.add(panelt)
        #paneltg.add(panelgb)
        
        
        
        
        layout = GroupLayout(self.table1)
        self.table1.setLayout(layout)
        butonpanel = JPanel(GridLayout(len(ro.IkickIds),1,0,1))
        butonpanel.setBorder(BorderFactory.createEmptyBorder(1, 1, 0, 0))
        for id in ro.IkickIds:
            butonpanel.add(JButton(id,actionPerformed = actionIKick1))
        butonpanel.setMaximumSize(Dimension(180, len(ro.IkickIds)*self.table1.getRowHeight()))
        butonpanel.setMinimumSize(Dimension(180, len(ro.IkickIds)*self.table1.getRowHeight()))
        #butonpanel.setOpaque(False)
        
        #self.table1.getColumnModel().getColumn(0).setMinWidth(180)
        #self.table1.getColumnModel().getColumn(0).setMaxWidth(180)
        
        #layout.setHorizontalGroup(layout.createSequentialGroup().addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(butonpanel))
        #layout.setVerticalGroup(layout.createSequentialGroup().addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(butonpanel))
                
        
        #layouth1 = GroupLayout(th1)
        #th1.setLayout(layouth1)
        StartButton = JButton("Start measurement",actionPerformed = actionIKick1)
        #StartButton.setMinimumSize(Dimension(180, th1.getPreferredSize().height))
        #StartButton.setMaximumSize(Dimension(180, th1.getPreferredSize().height))
        #layouth1.setHorizontalGroup(layouth1.createSequentialGroup().addComponent(StartButton))
        #layouth1.setVerticalGroup(layouth1.createSequentialGroup().addComponent(StartButton))
        StartButton.setAlignmentX(Component.CENTER_ALIGNMENT)
        panelt.add(StartButton)
        panelt.add(self.table1.getTableHeader())
        panelt.add(self.table1)
        
        
        #table3.getColumnModel().getColumn(0).setMinWidth(180)
        #table3.getColumnModel().getColumn(0).setMaxWidth(180)
        
        #layouth3 = GroupLayout(th3)
        #th3.setLayout(layouth3)
        #OptimizeButton = JButton("Optimize IKickers",actionPerformed = actionIKick3)
        #OptimizeButton.setMinimumSize(Dimension(180, th3.getPreferredSize().height))
        #OptimizeButton.setMaximumSize(Dimension(180, th3.getPreferredSize().height))
        #layouth3.setHorizontalGroup(layouth3.createSequentialGroup().addComponent(OptimizeButton))
        #layouth3.setVerticalGroup(layouth3.createSequentialGroup().addComponent(OptimizeButton))
        

        pt3 = JPanel(GridLayout(1,7, 2,2))
        pt3.setMaximumSize(Dimension(1000000, 70))
        pt3.setBorder(BorderFactory.createEmptyBorder(20, 2, 20, 2))



        
       
        
        pt3.add(b1)
        pt3.add(b2)
        pt3.add(JButton())
        pt3.add(JButton())
        pt3.add(JButton())
        pt3.add(JButton())
        pt3.add(b3)

        
        self.JP = FunctionGraphsJPanel()
        self.JP.setAxisNames("N (BPM)","X , Y Closed orbit position, [mm]")
        #self.JP.setLegendVisible(True)
        #self.JP.setChooseModeButtonVisible(True)
        #self.JP.setLegendButtonVisible(True)
        #f = new JFrame("frame")
        
        
        
        layout = GroupLayout(self.JP)
        self.JP.setLayout(layout)
        self.JP.setBorder(BorderFactory.createEmptyBorder(5,85,45,5))
        
        
        self.TP1 = FunctionGraphsJPanel()
        self.TP1.setAxisNames("Fractional Tune X","Fractional Tune Y")
        
        self.WFPV1X = FunctionGraphsJPanel()
        self.WFPV1X.setAxisNames("N","V Waveform X")
        self.WFPV2X = FunctionGraphsJPanel()
        self.WFPV2X.setAxisNames("N","V+dV Waveform X")
        self.WFPV1Y = FunctionGraphsJPanel()
        self.WFPV1Y.setAxisNames("N","V Waveform Y")
        self.WFPV2Y = FunctionGraphsJPanel()
        self.WFPV2Y.setAxisNames("N","V+dV Waveform Y")
        
        
        graphWF = JPanel(GridLayout(2,2))
        
        graphWF.add(self.WFPV1X)
        graphWF.add(self.WFPV2X)
        graphWF.add(self.WFPV1Y)
        graphWF.add(self.WFPV2Y)
        
        
        self.PWFPV1X = FunctionGraphsJPanel()
        self.PWFPV1X.setAxisNames("N","V Waveform X")
        self.PWFPV2X = FunctionGraphsJPanel()
        self.PWFPV2X.setAxisNames("N","V+dV Waveform X")
        self.PWFPV1Y = FunctionGraphsJPanel()
        self.PWFPV1Y.setAxisNames("N","V Waveform Y")
        self.PWFPV2Y = FunctionGraphsJPanel()
        self.PWFPV2Y.setAxisNames("N","V+dV Waveform Y")
        
        
        graphPWF = JPanel(GridLayout(2,2))
        
        graphPWF.add(self.PWFPV1X)
        graphPWF.add(self.PWFPV2X)
        graphPWF.add(self.PWFPV1Y)
        graphPWF.add(self.PWFPV2Y)
        
        
        self.FPSX = FunctionGraphsJPanel()
        self.FPSX.setAxisNames("x (mm)","xp (mrad)")
        self.FPSY = FunctionGraphsJPanel()
        self.FPSY.setAxisNames("y (mm)","yp (mrad)")
        
        graphFPS12 = JPanel(GridLayout(1,2))
        graphFPS12.add(self.FPSX)
        graphFPS12.add(self.FPSY)
        
        psd = PhaseSpaceFoilData(ro, self)
        self.table14 = JTable(psd)
        
        table14 = self.table14
        #table14.setRowSelectionInterval(0, 0)
        self.table14.setFocusable(False)
        self.table14.setRowSelectionAllowed(False)
        graphFPSpanel = JPanel()
        graphFPSpanel.setLayout(BoxLayout(graphFPSpanel, BoxLayout.Y_AXIS))
        graphFPSpanel.add(self.table14.getTableHeader())
        graphFPSpanel.add(self.table14)
        self.table14.getColumnModel().getColumn(5).setMinWidth(60)
        self.table14.getColumnModel().getColumn(5).setMaxWidth(60)
        self.table14.getColumnModel().getColumn(6).setMinWidth(60)
        self.table14.getColumnModel().getColumn(6).setMaxWidth(60)
        
        #self.table14.getDefaultRenderer(Boolean).setOpaque(True)
        self.table14.setDefaultRenderer(Object, ColorCellRenderer(self.colors))




        
        
        graphFPS = JPanel(BorderLayout())
        graphFPS.add(graphFPSpanel, BorderLayout.NORTH)
        graphFPS.add(graphFPS12,BorderLayout.CENTER)
        
        

    

        graphpane = JTabbedPane()
        
        graphpane.addTab("Closed orbits", self.JP)
        graphpane.addTab("Tunes", self.TP1)
        graphpane.addTab("Average Waveforms", graphWF)
        graphpane.addTab("Single pulse Waveforms", graphPWF)
        graphpane.addTab("Phase space params at foil", graphFPS)

        
        
        rbuttonPanel = JPanel(GridLayout(8,1))
        rbuttonPanel.setMaximumSize(Dimension(110, 150))
        rbuttonPanel.setMinimumSize(Dimension(110, 150))

        
        def OnCheck(event):
            kickInd = table1.getSelectedRow()
            kickId = ro.IkickIds[kickInd]
            
            plotClosedOrbits(kickId)
            return
        
        b1 = JButton("but1")
        b2 = JButton("but2")
        
        self.rb1 = JRadioButton("X measured", True,actionPerformed = OnCheck)
        self.rb2 = JRadioButton("Y measured", True,actionPerformed = OnCheck)
        self.rb3 = JRadioButton("X average", True,actionPerformed = OnCheck)
        self.rb4 = JRadioButton("Y average", True,actionPerformed = OnCheck)
        self.rb5 = JRadioButton("<html>&Delta;X average</html>", True,actionPerformed = OnCheck)
        self.rb6 = JRadioButton("<html>&Delta;Y average</html>", True,actionPerformed = OnCheck)
        self.rb7 = JRadioButton("<html>&Delta;X model fit</html>", True,actionPerformed = OnCheck)
        self.rb8 = JRadioButton("<html>&Delta;Y model fit</html>", True,actionPerformed = OnCheck)

        font = Font("Arial", Font.BOLD, 12)
        self.rb1.setForeground(Color.RED)
        self.rb1.setFont(font)
        self.rb2.setForeground(Color.BLUE)
        self.rb2.setFont(font)
        self.rb3.setForeground(Color.BLACK)
        self.rb3.setFont(font)
        self.rb4.setForeground(Color.GREEN.darker())
        self.rb4.setFont(font)
        self.rb5.setForeground(Color.RED)
        self.rb5.setFont(font)
        self.rb6.setForeground(Color.BLUE)
        self.rb6.setFont(font)
        self.rb7.setForeground(Color.BLACK)
        self.rb7.setFont(font)
        self.rb8.setForeground(Color.GREEN.darker())
        self.rb8.setFont(font)

        
        rbuttonPanel.add(self.rb1)
        rbuttonPanel.add(self.rb2)
        rbuttonPanel.add(self.rb3)
        rbuttonPanel.add(self.rb4)
        rbuttonPanel.add(self.rb5)
        rbuttonPanel.add(self.rb6)
        rbuttonPanel.add(self.rb7)
        rbuttonPanel.add(self.rb8)
        
        foilpspanel = JPanel()
        foilpspanel.setLayout(BoxLayout(foilpspanel, BoxLayout.Y_AXIS))
        
        pso = PhaseSpaceOrbit(ro)
        self.table13 = JTable(pso)
        self.table13.getColumnModel().getColumn(0).setMinWidth(100)
        self.table13.getColumnModel().getColumn(0).setMaxWidth(100)
        self.table13.getColumnModel().getColumn(1).setMinWidth(80)
        self.table13.getColumnModel().getColumn(1).setMaxWidth(80)
        self.table13.setAlignmentX(Component.LEFT_ALIGNMENT)

        
        def ShowOnCheck(event):
            if self.rbshow.isSelected():
                foilpspanel.add(self.table13)
            else:
                foilpspanel.remove(self.table13)
            
            foilpspanel.revalidate()
            foilpspanel.repaint()
                
            return
        
        self.rbshow = JRadioButton("Parameters at the foil", False,actionPerformed = ShowOnCheck)
        self.rbshow.setAlignmentX(Component.LEFT_ALIGNMENT)

        foilpspanel.add(self.rbshow)
        

        

        

        
        
        #layout.setHorizontalGroup(layout.createSequentialGroup().addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(labelpanel))
        #layout.setVerticalGroup(layout.createSequentialGroup().addComponent(labelpanel))
        
        #layout.setHorizontalGroup(layout.createSequentialGroup().addComponent(rbuttonPanel).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(foilpspanel))
        #layout.setVerticalGroup(layout.createSequentialGroup().addComponent(rbuttonPanel).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(foilpspanel))        

        layout.setHorizontalGroup(layout.createSequentialGroup().addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(foilpspanel).addComponent(rbuttonPanel))
        layout.setVerticalGroup(layout.createParallelGroup().addComponent(foilpspanel).addComponent(rbuttonPanel))

        


        self.table2.setAlignmentY(Component.TOP_ALIGNMENT)
        self.table8.setAlignmentY(Component.TOP_ALIGNMENT)
        
        #self.table9.setAlignmentY(Component.TOP_ALIGNMENT)
        
        panel2 = JPanel()
        panel2.setLayout(BoxLayout(panel2, BoxLayout.Y_AXIS))
        panel2.add(self.table2.getTableHeader())
        panel2.add(self.table2)
        panel2.setAlignmentY(Component.TOP_ALIGNMENT)
        
        panel8 = JPanel()
        panel8.setLayout(BoxLayout(panel8, BoxLayout.Y_AXIS))
        panel8.add(self.table8.getTableHeader())
        panel8.add(self.table8)
        panel8.setAlignmentY(Component.TOP_ALIGNMENT)
        
        panel9 = JPanel()
        panel9.setLayout(BoxLayout(panel9, BoxLayout.Y_AXIS))
        panel9.add(self.table9.getTableHeader())
        panel9.add(self.table9)
        panel9.setAlignmentY(Component.TOP_ALIGNMENT)
        #self.table9.setAlignmentY(Component.TOP_ALIGNMENT)
        panel11 = JPanel()
        panel11.setLayout(BoxLayout(panel11, BoxLayout.Y_AXIS))
        panel11.add(self.table11.getTableHeader())
        panel11.add(self.table11)
        panel11.setAlignmentY(Component.TOP_ALIGNMENT)
        #self.table11.setAlignmentY(Component.TOP_ALIGNMENT)
        panel12 = JPanel()
        panel12.setLayout(BoxLayout(panel12, BoxLayout.Y_AXIS))
        panel12.add(self.table12.getTableHeader())
        panel12.add(self.table12)
        panel12.setAlignmentY(Component.TOP_ALIGNMENT)
        #self.table12.setAlignmentY(Component.TOP_ALIGNMENT)
        

        
        panelpulses = JPanel()
        panelpulses.setLayout(BoxLayout(panelpulses, BoxLayout.X_AXIS))
        #panelpulses.setPreferredSize(Dimension(100, 2000))
        #panelpulses.setPreferredSize(self.table9.getPreferredSize())
        
        
        scrollpulses = JScrollPane(panelpulses,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER)
        scrollpulses.setAlignmentY(Component.TOP_ALIGNMENT)
        
        
        panelpulses.add(panel9)
        #panelpulses.add(self.table10)
        panelpulses.add(panel11)
        #panelpulses.add(self.table12)
        panelpulses.add(panel12)
        
        
        
        panelbpm.add(panel2)
        panelbpm.add(panel8)
        panelbpm.add(scrollpulses)
        

        
        l1 = JLabel("------ Input model parameters -----")
        l1.setHorizontalAlignment(JLabel.CENTER)
        l1.setAlignmentX(Component.CENTER_ALIGNMENT)
        #panelopt.add(l1)
        
        panelopt.add(self.table5)
        panelopt.add(Box.createRigidArea(Dimension(10, 10)))
        panelopt.add(self.table7.getTableHeader())
        panelopt.add(self.table7)
        

        
        #bp = JPanel(GridLayout(1,1))
        #bp.setMaximumSize(Dimension(1000000, 30))

        titleopt = BorderFactory.createTitledBorder(blackline, "Optimization options")
        titleopt.setTitleJustification(TitledBorder.CENTER)
        calcoptionsPanel = JPanel(GridLayout(2,1))
        calcoptionsPanel.setMaximumSize(Dimension(1000, 40))
        calcoptionsPanel.setMinimumSize(Dimension(1000, 40))
        #calcoptionsPanel.setBorder(titleopt)
        
        useMeasuredTunes = JCheckBox("Equate measured and model tunes",False)
        #useMeasuredTunes.setAlignmentX(Component.CENTER_ALIGNMENT)
        
        useCoupledIKicks = JCheckBox("Equate some I-Kicker coefficients",False)
        #useCoupledIKicks.setAlignmentX(Component.CENTER_ALIGNMENT)
        
        but = JButton("Calculate magnet parameters below",actionPerformed = actionCalculate)
        but.setAlignmentX(Component.CENTER_ALIGNMENT)
        
        calcoptionsPanel.add(useMeasuredTunes)
        calcoptionsPanel.add(useCoupledIKicks)
        #calcoptionsPanel.add(but)

        #useThreeMeas = JCheckBox("Use 3 IKicker meas",False)
        #useThreeMeas.setAlignmentX(Component.CENTER_ALIGNMENT)
        
        progressLabel = JLabel("Nsteps = 0")
        progressLabel.setAlignmentX(Component.CENTER_ALIGNMENT)
        
        generatebut = JButton("Generate Ring.xdxf optics file",actionPerformed = actionGenerate)
        generatebut.setAlignmentX(Component.CENTER_ALIGNMENT)
        
        #panelopt.add(useThreeMeas)
        panelopt.add(calcoptionsPanel)
        panelopt.add(but)
        panelopt.add(progressLabel)
        
        panelopt.add(Box.createRigidArea(Dimension(10, 10)))
        panelopt.add(table4.getTableHeader())
        panelopt.add(table4)
        panelopt.add(self.table6.getTableHeader())
        panelopt.add(self.table6)
        
        panelopt.add(generatebut)
        
        #paneltg.setAlignmentY(Component.BOTTOM_ALIGNMENT)
        #panelopt.setAlignmentY(Component.BOTTOM_ALIGNMENT)
        
        
        #paneltop.add(paneltg)
        #paneltop.add(panelopt)
        
        #self.panel2.setPreferredSize(Dimension(400, 1000000))
        
        
        self.panel = JPanel(BorderLayout())
        
        panel1 = JPanel(BorderLayout())
        panel1.add(paneTables,BorderLayout.NORTH)
        panel1.add(graphpane,BorderLayout.CENTER)
        #panel1.add(panelt1,BorderLayout.SOUTH)
        
        self.panel.add(panelopt,BorderLayout.WEST)
        self.panel.add(panel1,BorderLayout.CENTER)
        self.panel.add(panelbpm,BorderLayout.EAST)
        #self.panel.add(datatable,BorderLayout.SOUTH)
        
    def updateBPMselection(self):
        self.table2.getSelectionModel().clearSelection()

        for i in range(len(self.ro.allBPMsIds)):
            if self.ro.allBPMsIds[i] in self.ro.d['useBPMs']:
                self.table2.addRowSelectionInterval(i, i)
                
    
    def updatePulseselection(self, kickId):
        self.table9.getSelectionModel().clearSelection()
        self.table11.getSelectionModel().clearSelection()
        
        
        if kickId in self.ro.d['BPMWaveForms'].keys():
            if 'voltage-1' in self.ro.d['BPMWaveForms'][kickId].keys():
                for i in range(len(self.ro.d['BPMWaveForms'][kickId]['voltage-1'].keys())):
                    if 'pulse-' + str(i) in self.ro.d['IKickParams'][kickId]['usepulses']['voltage-1']:
                        self.table9.addRowSelectionInterval(i, i)
            if 'voltage-2' in self.ro.d['BPMWaveForms'][kickId].keys():
                for i in range(len(self.ro.d['BPMWaveForms'][kickId]['voltage-2'].keys())):
                    if 'pulse-' + str(i) in self.ro.d['IKickParams'][kickId]['usepulses']['voltage-2']:
                        self.table11.addRowSelectionInterval(i, i)
                        
                        
                        

        
    def plotTunes(self):
        
        self.TP1.removeAllGraphData()
        txty = BasicGraphData()
        txty.setDrawLinesOn(False)

        arrtuneX = [self.ro.dav[kickId][vid][bpmId]['tuneX'] for kickId in self.ro.dav.keys() for vid in self.ro.dav[kickId].keys() for bpmId in self.ro.dav[kickId][vid].keys() if bpmId in self.ro.d['useBPMs']]
        arrtuneY = [self.ro.dav[kickId][vid][bpmId]['tuneY'] for kickId in self.ro.dav.keys() for vid in self.ro.dav[kickId].keys() for bpmId in self.ro.dav[kickId][vid].keys() if bpmId in self.ro.d['useBPMs']] 
        
        txty.addPoint(arrtuneX, arrtuneY)
        self.TP1.addGraphData(txty)
        
        
        #self.TP1.setExternalGL(GridLimits())

        #Xxmin,Xxmax,Xymin,Xymax = self.TP1.getCurrentMinX(), self.TP1.getCurrentMaxX(), self.TP1.getCurrentMinY(), self.TP1.getCurrentMaxY()
        #XYmax = max([Xxmax,Xymax])

        #self.TP1.setLimitsAndTicksX(0, XYmax, XYmax/10)
        #self.TP1.setLimitsAndTicksY(0, XYmax, XYmax/10)

       
        
        
        

        
    def plotClosedOrbits(self, kickId):
        
        #phase space parameters at the foil
        self.table13.getModel().fireTableDataChanged()
        
        self.JP.removeAllGraphData()

        if kickId in self.ro.d['BPMWaveForms'].keys():
            for vid in self.ro.d['BPMWaveForms'][kickId].keys():
                for pulseId in self.ro.d['BPMWaveForms'][kickId][vid].keys():
                    if pulseId in self.ro.d['IKickParams'][kickId]['usepulses'][vid]:
                        self.addPulseToPlot(self.ro.d['BPMWaveForms'][kickId][vid][pulseId])

            zind, showBPMsIDs, bpmXaver1, bpmYaver1, bpmXaver2, bpmYaver2, dxaver, dyaver = self.ro.idsdataCalc( kickId)


            if self.rb3.isSelected():
                for xav in [bpmXaver1, bpmXaver2]:
                    gxaver = BasicGraphData()
                    gxaver.setGraphColor(Color.BLACK)
                    gxaver.setLineThick(2)
                    gxaver.addPoint(zind, xav)
                    self.JP.addGraphData(gxaver)

            if self.rb4.isSelected():
                for yav in [bpmYaver1, bpmYaver2]:
                    gyaver = BasicGraphData()
                    gyaver.setGraphColor(Color.GREEN.darker())
                    gyaver.setLineThick(2)
                    gyaver.addPoint(zind, yav)
                    self.JP.addGraphData(gyaver)

            if self.rb5.isSelected() and "IKickH" in kickId:
                dx = BasicGraphData()
                dx.setGraphColor(Color.RED)
                dx.addPoint(zind, dxaver)
                self.JP.addGraphData(dx)

            if self.rb6.isSelected() and "IKickV" in kickId:
                dy = BasicGraphData()
                dy.setGraphColor(Color.BLUE)
                dy.addPoint(zind, dyaver)
                self.JP.addGraphData(dy)

            if self.rb7.isSelected() and "IKickH" in kickId:
                dxt = self.ro.ModelOrbits(kickId, showBPMsIDs)
                dx = BasicGraphData()
                dx.setGraphColor(Color.BLACK)
                dx.setDrawPointsOn(False)
                dx.addPoint(zind, dxt)
                self.JP.addGraphData(dx)

            if self.rb8.isSelected() and "IKickV" in kickId:
                dyt = self.ro.ModelOrbits(kickId, showBPMsIDs)
                dy = BasicGraphData()
                dy.setGraphColor(Color.GREEN.darker())
                dy.setDrawPointsOn(False)
                dy.addPoint(zind, dyt)
                self.JP.addGraphData(dy)
                    
 
                
                
                
    def addPulseToPlot(self,pulse):
        
        bpmX = []
        bpmY = []
        zind = []

        indBPM = 0
        for bpmId in self.ro.allBPMsIds:
            if bpmId in pulse.keys() and bpmId in self.ro.d['useBPMs']:
                arx = pulse[bpmId]['waveform-x']
                ary = pulse[bpmId]['waveform-y']
                averx = sum(arx)/self.ro.d['WFLength']
                avery = sum(ary)/self.ro.d['WFLength']

                bpmX.append(averx)
                bpmY.append(avery)
                zind.append(indBPM)

            indBPM += 1

        if self.rb1.isSelected():
            gdx = BasicGraphData()
            gdx.addPoint(zind, bpmX)
            gdx.setGraphColor(Color.RED)
            self.JP.addGraphData(gdx)

        if self.rb2.isSelected():
            gdy = BasicGraphData()
            gdy.setGraphColor(Color.BLUE)
            gdy.addPoint(zind, bpmY)
            self.JP.addGraphData(gdy)
            

    def plotWaveform(self, kickId, bpmId):
        
        self.WFPV1X.removeAllGraphData()
        self.WFPV2X.removeAllGraphData()
        self.WFPV1Y.removeAllGraphData()
        self.WFPV2Y.removeAllGraphData()
        
        gl = GridLimits()
        gl.setXmin(0)
        gl.setXmax(100)
        
        self.WFPV1X.setExternalGL(gl)
        self.WFPV2X.setExternalGL(gl)
        self.WFPV1Y.setExternalGL(gl)
        self.WFPV2Y.setExternalGL(gl)


        for vid in self.ro.dav[kickId].keys():
            if bpmId in self.ro.dav[kickId][vid].keys():

                wfx = self.ro.dav[kickId][vid][bpmId]['waveform-x']
                wfy = self.ro.dav[kickId][vid][bpmId]['waveform-y']
                indexes = range(len(wfx))
                wdx = BasicGraphData()
                wdx.setDrawLinesOn(False)
                wdx.addPoint(indexes, wfx)
                wdy = BasicGraphData()
                wdy.setDrawLinesOn(False)
                wdy.addPoint(indexes, wfy)

                fitgx = GaussianSinusoidFit(wfx, self.ro.d['WFLength'])
                fitgy = GaussianSinusoidFit(wfy, self.ro.d['WFLength'])
                fitgx.solveWithNoiseMaxEvaluations(0.0, 200)
                fitgy.solveWithNoiseMaxEvaluations(0.0, 200)

                indexesfit = [i/10.0 for i in range(1000)]

                wfxf = fitgx.getFittedWaveform(indexesfit)
                wfyf = fitgy.getFittedWaveform(indexesfit)

                wdxf = BasicGraphData()
                wdxf.setGraphColor(Color.RED)
                wdxf.setDrawPointsOn(False)
                wdxf.addPoint(indexesfit, wfxf)
                wdyf = BasicGraphData()
                wdyf.setGraphColor(Color.BLUE)
                wdyf.setDrawPointsOn(False)
                wdyf.addPoint(indexesfit, wfyf)

                if vid == "voltage-1":
                    self.WFPV1X.addGraphData(wdx)
                    self.WFPV1Y.addGraphData(wdy)
                    self.WFPV1X.addGraphData(wdxf)
                    self.WFPV1Y.addGraphData(wdyf)
                if vid == "voltage-2":
                    self.WFPV2X.addGraphData(wdx) 
                    self.WFPV2Y.addGraphData(wdy)
                    self.WFPV2X.addGraphData(wdxf) 
                    self.WFPV2Y.addGraphData(wdyf)
                    
    def plotPulseWaveform(self, kickId, bpmId, pulseInd):
        
        self.PWFPV1X.removeAllGraphData()
        self.PWFPV2X.removeAllGraphData()
        self.PWFPV1Y.removeAllGraphData()
        self.PWFPV2Y.removeAllGraphData()
        
        gl = GridLimits()
        gl.setXmin(0)
        gl.setXmax(100)
        
        self.PWFPV1X.setExternalGL(gl)
        self.PWFPV2X.setExternalGL(gl)
        self.PWFPV1Y.setExternalGL(gl)
        self.PWFPV2Y.setExternalGL(gl)
        
        
        for vid in self.ro.d['BPMWaveForms'][kickId].keys():
            if bpmId in self.ro.d['BPMWaveForms'][kickId][vid]['pulse-' + str(pulseInd)].keys():

                wfx = self.ro.d['BPMWaveForms'][kickId][vid]['pulse-' + str(pulseInd)][bpmId]['waveform-x']
                wfy = self.ro.d['BPMWaveForms'][kickId][vid]['pulse-' + str(pulseInd)][bpmId]['waveform-y']
                indexes = range(len(wfx))
                wdx = BasicGraphData()
                wdx.setGraphColor(Color.RED)
                #wdx.setDrawLinesOn(False)
                wdx.addPoint(indexes, wfx.tolist())
                wdy = BasicGraphData()
                wdy.setGraphColor(Color.BLUE)
                #wdy.setDrawLinesOn(False)
                wdy.addPoint(indexes, wfy.tolist())
                
                #fitgx = GaussianSinusoidFit(wfx, self.ro.Nturn)
                #fitgy = GaussianSinusoidFit(wfy, self.ro.Nturn)
                #fitgx.solveWithNoiseMaxEvaluations(0.0, 200)
                #fitgy.solveWithNoiseMaxEvaluations(0.0, 200)
                
                #indexesfit = [i/10.0 for i in range(1000)]

                #wfxf = fitgx.getFittedWaveform(indexesfit)
                #wfyf = fitgy.getFittedWaveform(indexesfit)
                
                #wdxf = BasicGraphData()
                #wdxf.setGraphColor(Color.RED)
                #wdxf.setDrawPointsOn(False)
                #wdxf.addPoint(indexesfit, wfxf)
                #wdyf = BasicGraphData()
                #wdyf.setGraphColor(Color.BLUE)
                #wdyf.setDrawPointsOn(False)
                #wdyf.addPoint(indexesfit, wfyf)
                        
                if vid == "voltage-1":
                    self.PWFPV1X.addGraphData(wdx)
                    self.PWFPV1Y.addGraphData(wdy)
                    #self.PWFPV1X.addGraphData(wdxf)
                    #self.PWFPV1Y.addGraphData(wdyf)
                if vid == "voltage-2":
                    self.PWFPV2X.addGraphData(wdx) 
                    self.PWFPV2Y.addGraphData(wdy)
                    #self.PWFPV2X.addGraphData(wdxf)
                    #self.PWFPV2Y.addGraphData(wdyf)


        return
    
    
    
    def plotIKickPhaseSpaceFoil(self):
            
        
        self.FPSX.removeAllGraphData()
        self.FPSY.removeAllGraphData()
        
        for i in range(len(self.ro.IkickIds)):
            if self.ro.plotFoil[i] == True:
                id = self.ro.IkickIds[i]
                xpx = BasicGraphData()
                ypy = BasicGraphData()
                xpx.setGraphColor(self.colors[i])
                ypy.setGraphColor(self.colors[i])
                xpx.setDrawLinesOn(False)
                ypy.setDrawLinesOn(False)

    
                xarr = [self.ro.dav[id]['voltage-1'][bpmId]['foil-X'] for bpmId in self.ro.dav[id]['voltage-1'].keys() if bpmId in self.ro.d['useBPMs']]
                xparr = [self.ro.dav[id]['voltage-1'][bpmId]['foil-XP'] for bpmId in self.ro.dav[id]['voltage-1'].keys() if bpmId in self.ro.d['useBPMs']]
                yarr = [self.ro.dav[id]['voltage-1'][bpmId]['foil-Y'] for bpmId in self.ro.dav[id]['voltage-1'].keys() if bpmId in self.ro.d['useBPMs']]
                yparr = [self.ro.dav[id]['voltage-1'][bpmId]['foil-YP'] for bpmId in self.ro.dav[id]['voltage-1'].keys() if bpmId in self.ro.d['useBPMs']]


                xpx.addPoint(xarr, xparr)
                ypy.addPoint(yarr, yparr)

                self.FPSX.addGraphData(xpx)
                self.FPSY.addGraphData(ypy)

                
                self.FPSX.setExternalGL(GridLimits())
                self.FPSY.setExternalGL(GridLimits())
                
                
                Xxmin,Xxmax,Xymin,Xymax = self.FPSX.getCurrentMinX(), self.FPSX.getCurrentMaxX(), self.FPSX.getCurrentMinY(), self.FPSX.getCurrentMaxY()
                Yxmin,Yxmax,Yymin,Yymax = self.FPSY.getCurrentMinX(), self.FPSY.getCurrentMaxX(), self.FPSY.getCurrentMinY(), self.FPSY.getCurrentMaxY()
                
                """
                Xdx = Xxmax - Xxmin
                Xdy = Xymax - Xymin
                Ydx = Yxmax - Yxmin
                Ydy = Yymax - Yymin
                
                if Xdx > Ydx:
                    Yxmax = Yxmin + Xdx
                else:
                    Xxmax = Xxmin + Ydx
                    
                if Xdy > Ydy:
                    Yymax = Yymin + Xdy
                else:
                    Xymax = Xymin + Ydy

                self.FPSX.setLimitsAndTicksX(Xxmin, Xxmax, (Xxmax - Xxmin)/5)
                self.FPSX.setLimitsAndTicksY(Xymin, Xymax, (Xymax - Xymin)/10)
                self.FPSY.setLimitsAndTicksX(Yxmin, Yxmax, (Yxmax - Yxmin)/5)
                self.FPSY.setLimitsAndTicksY(Yymin, Yymax, (Yymax - Yymin)/10)
                """
                
                self.FPSX.setLimitsAndTicksX(-20.0, 20.0, (20.0 + 20.0)/5)
                self.FPSX.setLimitsAndTicksY(-1.0, 1.0, (1.0 + 1.0)/10)
                self.FPSY.setLimitsAndTicksX(-20.0, 20.0, (20.0 + 20.0)/5)
                self.FPSY.setLimitsAndTicksY(-1.0, 1.0, (1.0 + 1.0)/10)
                
                
        return
    
    
   
                    
    def updatePulsetable(self, kickId):
        
        
        self.table9.getModel().kickId = kickId
        self.table11.getModel().kickId = kickId
        self.table12.getModel().kickId = kickId

        self.table9.getModel().fireTableDataChanged()
        self.table11.getModel().fireTableDataChanged()
        self.table12.getModel().fireTableDataChanged()
        
        if self.table12.getModel().getRowCount() > 0:
            self.table12.setRowSelectionInterval(0, 0)
        
        
                    
                
                    
                  
                

        
                
            
            
