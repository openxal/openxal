from javax.swing import JTable, DefaultCellEditor, JTextField, ListSelectionModel, JLabel, BorderFactory, JButton, JPanel, BoxLayout, JTabbedPane, GroupLayout, LayoutStyle, JRadioButton, JScrollPane, Box, JCheckBox
from java.lang import String, Short, Object, Boolean, Runnable
from java.awt import Color, Font, Dimension, GridLayout, Component, BorderLayout, GridBagLayout, GridBagConstraints, Insets
from javax.swing.table import AbstractTableModel,TableCellRenderer,DefaultTableCellRenderer

from java.text import DecimalFormat
import sys, os, math, random, time
from javax.swing.border import TitledBorder

from xal.extension.widgets.plot import BasicGraphData, FunctionGraphsJPanel, GridLimits
from xal.extension.fit import GaussianSinusoidFit




class IKickControl(AbstractTableModel):
	def __init__(self, ro):
            self.columnNames = ["IKick id","coeff. (mrad/V)","kick change (mrad)","Control voltage (V)","Calculated voltage (V)"]
            self.ro = ro
		
	def getColumnCount(self):
            return len(self.columnNames)
		
	def getRowCount(self):
            return len(self.ro.IkickIds)
		
	def getColumnName(self,col):
            return self.columnNames[col]
		
	def getValueAt(self,row,col):    
            id = self.ro.IkickIds[row]
            #val = self.ro.kch.childAdaptor(id).doubleValue("mrad-per-v")
            val = self.ro.d['IKickParams'][id]['mrad/V']
            
            v0 = self.ro.d['IKickParams'][id]['voltage-1']
            vgoal = v0 + self.ro.dIKick[id]/val
            return[id, str(val)[0:8], str(self.ro.dIKick[id])[0:8], str(v0)[0:8],str(vgoal)[0:8]][col]
        
	def setValueAt(self, value, row, col):
            return

	def getColumnClass(self,col):
            return [String, String, String, String, String][col]
	
	def isCellEditable(self, row, col):
            return False
        
        

class PhaseParameters(AbstractTableModel):
	def __init__(self, ro):
            self.columnNames = ["","closed orbit","foil phase spase parameters"]
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
                return [str(self.ro.dx)[0:8],str(self.ro.dxp)[0:8],str(self.ro.dy)[0:8],str(self.ro.dyp)[0:8]][row]
            if col == 2:
                return [str(-self.ro.dx)[0:8],str(-self.ro.dxp)[0:8],str(-self.ro.dy)[0:8],str(-self.ro.dyp)[0:8]][row]

	def setValueAt(self, value, row, col):
            if col == 1:
                if row == 0:
                    self.ro.dx = float(value)
                if row == 1:
                    self.ro.dxp = float(value)
                if row == 2:
                    self.ro.dy = float(value)
                if row == 3:
                    self.ro.dyp = float(value)
                    
            if col == 2:
                if row == 0:
                    self.ro.dx = -float(value)
                if row == 1:
                    self.ro.dxp = -float(value)
                if row == 2:
                    self.ro.dy = -float(value)
                if row == 3:
                    self.ro.dyp = -float(value)
                    
            self.fireTableDataChanged()
                    
            return

	def getColumnClass(self,col):
            return [String, String, String][col]
	
	def isCellEditable(self,row,col):
            return col > 0
        
        

class PhaseParametersAtFoil(AbstractTableModel):
	def __init__(self, ro):
            self.columnNames = ["","average","sigma"]
            self.ro = ro
		
	def getColumnCount(self):
            return len(self.columnNames)
		
	def getRowCount(self):
            return 4
		
	def getColumnName(self,col):
            return self.columnNames[col]
		
	def getValueAt(self, row, col):
            
            xarr = [self.ro.davfoil[bpmId]['foil-X'] for bpmId in self.ro.davfoil.keys() if bpmId in self.ro.d['useBPMs']]
            xparr = [self.ro.davfoil[bpmId]['foil-XP'] for bpmId in self.ro.davfoil.keys() if bpmId in self.ro.d['useBPMs']]
            yarr = [self.ro.davfoil[bpmId]['foil-Y'] for bpmId in self.ro.davfoil.keys() if bpmId in self.ro.d['useBPMs']]
            yparr = [self.ro.davfoil[bpmId]['foil-YP'] for bpmId in self.ro.davfoil.keys() if bpmId in self.ro.d['useBPMs']]
            
            avx = sum(xarr) / len(xarr) 
            avxp = sum(xparr) / len(xparr) 
            avy = sum(yarr) / len(yarr)
            avyp = sum(yparr) / len(yparr)
            
            sx = math.sqrt(sum([(e - avx)*(e - avx) for e in xarr]) / len(xarr)) 
            sxp = math.sqrt(sum([(e - avxp)*(e - avxp) for e in xparr]) / len(xparr))
            sy = math.sqrt(sum([(e - avy)*(e - avy) for e in yarr]) / len(yarr))
            syp = math.sqrt(sum([(e - avyp)*(e - avyp) for e in yparr]) / len(yparr))
            
            
            if col == 0:
                return ["x (mm)","xp (mrad)","y (mm)","yp (mrad)"][row]
            if col == 1:
                return [str(avx)[0:8],str(avxp)[0:8],str(avy)[0:8],str(avyp)[0:8]][row]
            if col == 2:
                return [str(sx)[0:8],str(sxp)[0:8],str(sy)[0:8],str(syp)[0:8]][row]

	def setValueAt(self, value, row, col):
            return

	def getColumnClass(self,col):
            return [String, String, String][col]
	
	def isCellEditable(self,row,col):
            return False
        
        
class TuneParameters(AbstractTableModel):
	def __init__(self, ro):
            self.ro = ro
            self.paramNames = ["X Tune","Y Tune"]
            self.columnNames = ["Tunes","model","frac. meas."]
		
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
                
                arrtuneX = [self.ro.davfoil[bpmId]['tuneX'] for bpmId in self.ro.davfoil.keys() if bpmId in self.ro.d['useBPMs']]
                arrtuneY = [self.ro.davfoil[bpmId]['tuneY'] for bpmId in self.ro.davfoil.keys() if bpmId in self.ro.d['useBPMs']]
                    
                return [str(sum(arrtuneX) / len(arrtuneX) )[0:8], str(sum(arrtuneY) / len(arrtuneY))[0:8]][row]
        
	def setValueAt(self, value, row, col):
            return

	def getColumnClass(self,col):
            return [String, String, String][col]
	
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
		
	def getValueAt(self,row,col):
            return self.ro.allBPMsIds[row]


	def setValueAt(self, value, row, col):
            return

	def getColumnClass(self,col):
            return [String][col]
	
	def isCellEditable(self,row,col):
            return False
        
        
class PulsesData(AbstractTableModel):
	def __init__(self, ro):
            
            self.ro = ro
            self.columnNames = ["pulse"]

            	
	def getColumnCount(self):
            return 1
		
	def getRowCount(self):  
            return len(self.ro.dfoil['BPMWaveForms'].keys())
		
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
            return len(self.ro.bpms)
		
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
	def __init__(self, ro):
            self.ro = ro
            self.columnNames = ["waveform"]
		
	def getColumnCount(self):
            return len(self.columnNames)
		
	def getRowCount(self):
            return len(self.ro.dfoil['BPMWaveForms'].keys())
		
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




class OrbitControlPane:
    
    def __init__(self, ro):
        
        self.ro = ro
        
        plotPhaseSpaceFoil = self.plotPhaseSpaceFoil
        plotWaveform = self.plotWaveform
        plotPulseWaveform = self.plotPulseWaveform
        plotPhaseSpaceBPM = self.plotPhaseSpaceBPM
        plotTunes = self.plotTunes
        
        
        centerRenderer = DefaultTableCellRenderer()
        centerRenderer.setHorizontalAlignment( JLabel.CENTER )
        
        kc = IKickControl(ro)
        pp = PhaseParameters(ro)
        table1 = JTable(kc)
        
        table1.setRowSelectionAllowed(False)
        table1.setFocusable(False)
        
        table2 = JTable(pp)
        
        table2.getColumnModel().getColumn(0).setPreferredWidth(100)
        table2.getColumnModel().getColumn(1).setPreferredWidth(150)
        table2.getColumnModel().getColumn(2).setPreferredWidth(150)
        
        table2.setFocusable(False)
        table2.setRowSelectionAllowed(False)
        
        singleclick = DefaultCellEditor(JTextField())
        singleclick.setClickCountToStart(1)
        table2.setDefaultEditor(table2.getColumnClass(1), singleclick)
        table2.setDefaultEditor(table2.getColumnClass(2), singleclick)



        #th1 = table1.getTableHeader()
        #th1.setFont(Font("Arial", Font.BOLD, 14))
        #th2 = table2.getTableHeader()
        #th2.setFont(Font("Arial", Font.BOLD, 14))
        
        
        blackline = BorderFactory.createLineBorder(Color.black)
        title1 = BorderFactory.createTitledBorder(blackline, "Kickers parameters calculation")
        title1.setTitleJustification(TitledBorder.CENTER)
        
        title2 = BorderFactory.createTitledBorder(blackline, "Phase space")
        title2.setTitleJustification(TitledBorder.CENTER)
        
        title3 = BorderFactory.createTitledBorder(blackline, "Measured phase space parameters at the foil")
        title3.setTitleJustification(TitledBorder.CENTER)
        
        
        pb = JPanel()
        
        #pb.setMaximumSize(Dimension(280, 1000))
        #pb.setMinimumSize(Dimension(280, 1000))
        
        pb.setLayout(BoxLayout(pb, BoxLayout.Y_AXIS))
        pb.setBorder(title2)
        pb.add(table2.getTableHeader())
        pb.add(table2)
        
        
        def action1(event):
            
            ro.getRealVoltage()
            ro.calculateRealPhaseParamIKick()
            table1.getModel().fireTableDataChanged()
            
        def action2(event):
            ro.setupIKickers()
            table1.getModel().fireTableDataChanged()
            
            
        class selectBPMTable(JTable):
            def changeSelection(self, rowIndex, columnIndex, toggle, extend):
                JTable.changeSelection(self, rowIndex, columnIndex, True, extend)
                
                ro.d['useBPMs'] = [ro.bpms[i].getId() for i in range(len(ro.bpms)) if self.isRowSelected(i)]
                
                plotTunes()
                table7.getModel().fireTableDataChanged()


                plotPhaseSpaceFoil()
                table13.getModel().fireTableDataChanged()
                

                return
            
        
        class monitorBPMTable(JTable):
            def changeSelection(self, rowIndex, columnIndex, toggle, extend):
                JTable.changeSelection(self, rowIndex, columnIndex, toggle, extend)


                bpmInd = table8.getSelectedRow()
                bpmId = ro.allBPMsIds[bpmInd]
                
                plotWaveform(bpmId)
                plotPhaseSpaceBPM(bpmId)
                
                if table12.getModel().getRowCount() > 0:
                    pulseInd = table12.getSelectedRow()
                    plotPulseWaveform(bpmId, pulseInd)
                
                return
            
            
        class selectPulsesTable(JTable):
            def changeSelection(self, rowIndex, columnIndex, toggle, extend):
                JTable.changeSelection(self, rowIndex, columnIndex, True, extend)
                

                bpmInd = table8.getSelectedRow()
                bpmId = ro.allBPMsIds[bpmInd]
                
                ro.dfoil['usepulses'] = ['pulse-' + str(i) for i in range(len(ro.dfoil['BPMWaveForms'].keys())) if self.isRowSelected(i)]
                
                ro.davfoil = ro.getFoilParameters(ro.dfoil['BPMWaveForms'], ro.dfoil['usepulses'])
                
                
                plotTunes()
                table7.getModel().fireTableDataChanged()
                
                
                plotWaveform(bpmId)
                plotPhaseSpaceBPM(bpmId)

                plotPhaseSpaceFoil()
                table13.getModel().fireTableDataChanged()

                return
            
        class monitorPulseTable(JTable):
            def changeSelection(self, rowIndex, columnIndex, toggle, extend):
                JTable.changeSelection(self, rowIndex, columnIndex, toggle, extend)

                bpmInd = table8.getSelectedRow()
                bpmId = ro.allBPMsIds[bpmInd]
                
                if table12.getModel().getRowCount() > 0:
                    pulseInd = table12.getSelectedRow()
                    plotPulseWaveform(bpmId, pulseInd)
                    

            

        
        b1 = JButton("Calculate IKickers",actionPerformed = action1)
        b2 = JButton("Setup kickers",actionPerformed = action2)
        pb.add(b1)
        pb.add(b2)
        

        pt1 = JPanel()
        pt1.setLayout(BoxLayout(pt1, BoxLayout.Y_AXIS))
        pt1.setBorder(title1)
        pt1.add(table1.getTableHeader())
        pt1.add(table1)
        
        
        self.PSX = FunctionGraphsJPanel()
        self.PSX.setAxisNames("x (mm)","xp (mrad)")
        self.PSY = FunctionGraphsJPanel()
        self.PSY.setAxisNames("y (mm)","yp (mrad)")
        
        
        PS = JPanel(GridLayout(1,2))
        PS.add(self.PSX)
        PS.add(self.PSY)



        self.WFX = FunctionGraphsJPanel()
        self.WFX.setAxisNames("N","Waveform X")
        self.WFY = FunctionGraphsJPanel()
        self.WFY.setAxisNames("N","Waveform Y")


        self.PWFX = FunctionGraphsJPanel()
        self.PWFX.setAxisNames("N","Pulse Waveform X")
        self.PWFY = FunctionGraphsJPanel()
        self.PWFY.setAxisNames("N","Pulse Waveform Y")

        
        
        graphWF = JPanel(GridLayout(2,2))
        graphWF.add(self.WFX)
        graphWF.add(self.PWFX)
        graphWF.add(self.WFY)
        graphWF.add(self.PWFY)
        
        
        self.BPSX = FunctionGraphsJPanel()
        self.BPSX.setAxisNames("x (mm)","xp (mrad)")
        self.BPSY = FunctionGraphsJPanel()
        self.BPSY.setAxisNames("y (mm)","yp (mrad)")
        self.BPSXY = FunctionGraphsJPanel()
        self.BPSXY.setAxisNames("X (mm)","Y (mm)")
        
        
        graphBPS = JPanel(GridBagLayout())

        graphBPS.add(self.BPSX, GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, Insets(0, 0, 0, 0), 0, 0))
        graphBPS.add(self.BPSY, GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, Insets(0, 0, 0, 0), 0, 0))
        graphBPS.add(self.BPSXY, GridBagConstraints(1, 0, 1, 2, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, Insets(0, 0, 0, 0), 0, 0))
        
        self.TP1 = FunctionGraphsJPanel()
        self.TP1.setAxisNames("Fractional Tune X","Fractional Tune Y")
        

        graphpane = JTabbedPane()
        
        
        
        graphpane.addTab("Phase space parameters at foil", PS)
        graphpane.addTab("Average Waveforms / Single pulse Waveforms", graphWF)
        graphpane.addTab("Phase space params at BPM", graphBPS)
        graphpane.addTab("Tunes",self.TP1)
        
        #graphpane.addTab("Single pulse Waveforms", graphPWF)
        
        
        
        bpmd = BPMsData(self.ro)
        self.table2 = selectBPMTable(bpmd)
        self.table2.setFocusable(False)
        self.table2.getColumnModel().getColumn(0).setMinWidth(140)
        self.table2.getColumnModel().getColumn(0).setMaxWidth(140)
        
        def updateBPMselection(self):
            self.table2.getSelectionModel().clearSelection()

            for i in range(len(self.ro.allBPMsIds)):
                if self.ro.allBPMsIds[i] in self.ro.d['useBPMs']:
                    self.table2.addRowSelectionInterval(i, i)
                    
                    
        def startaction(event):
            class Thr(Runnable):
                def run(self):
                    _startaction(event)
            Thread(Thr()).start()
        def _startaction(event):
            
            ro.startPhaseSpaceMeas(self)
            self.plotPhaseSpaceFoil()
            self.table13.getModel().fireTableDataChanged()
            self.plotTunes()
            self.table7.getModel().fireTableDataChanged()

            
        def stopaction(event):
            class Thr(Runnable):
                def run(self):
                    _stopaction(event)
            Thread(Thr()).start()  
        def _stopaction(event):
            ro.stopPhaseSpaceMeas()
                    
                    
                    
        startbutton = JButton("Start measurement",actionPerformed = startaction)
        stopbutton = JButton("Stop measurement",actionPerformed = stopaction)
        
        wfd = MonitorWF(self.ro)
        self.table8 = monitorBPMTable(wfd)        
        table8 = self.table8
        self.table8.setRowSelectionInterval(0, 0)
        #self.table8.setFocusable(False)
        self.table8.getColumnModel().getColumn(0).setMinWidth(70)
        self.table8.getColumnModel().getColumn(0).setMaxWidth(70)
        
        cellSelectionModel8 = self.table8.getSelectionModel()
        cellSelectionModel8.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
        
        
        
        pulses = PulsesData(self.ro)
        self.table11 = selectPulsesTable(pulses)
        self.table11.setFocusable(False)
        self.table11.setDefaultRenderer(String, centerRenderer)
        self.table11.getColumnModel().getColumn(0).setMinWidth(70)
        self.table11.getColumnModel().getColumn(0).setMaxWidth(70)
        
        wfsd = MonitorPulseWF(self.ro)
        self.table12 = monitorPulseTable(wfsd)
        table12 = self.table12
        self.table12.getColumnModel().getColumn(0).setMinWidth(70)
        self.table12.getColumnModel().getColumn(0).setMaxWidth(70)
        
        cellSelectionModel12 = self.table12.getSelectionModel()
        cellSelectionModel12.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
        
        
        ppf = PhaseParametersAtFoil(self.ro)
        self.table13 = JTable(ppf)
        table13 = self.table13
        #self.table13.getColumnModel().getColumn(0).setMinWidth(70)
        #self.table13.getColumnModel().getColumn(0).setMaxWidth(70)
        table13.setFocusable(False)
        table13.setRowSelectionAllowed(False)
        
        
        self.table7 = JTable(TuneParameters(self.ro))
        self.table7.setFocusable(False)
        self.table7.setRowSelectionAllowed(False)
        
        table7 = self.table7
       

        self.updateBPMselection()
        
        
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
        
        panel11 = JPanel()
        panel11.setLayout(BoxLayout(panel11, BoxLayout.Y_AXIS))
        panel11.add(self.table11.getTableHeader())
        panel11.add(self.table11)
        panel11.setAlignmentY(Component.TOP_ALIGNMENT)

        panel12 = JPanel()
        panel12.setLayout(BoxLayout(panel12, BoxLayout.Y_AXIS))
        panel12.add(self.table12.getTableHeader())
        panel12.add(self.table12)
        panel12.setAlignmentY(Component.TOP_ALIGNMENT)
        
        
        
        
        titlebpm = BorderFactory.createTitledBorder(blackline, "Use BPMs, Use pulses, monitor waveforms")
        titlebpm.setTitleJustification(TitledBorder.CENTER)
        
        
        
        panelpulses = JPanel()
        panelpulses.setLayout(BoxLayout(panelpulses, BoxLayout.X_AXIS))
        #panelpulses.setPreferredSize(Dimension(100, 2000))
        #panelpulses.setPreferredSize(self.table9.getPreferredSize())
        
        
        scrollpulses = JScrollPane(panelpulses,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER)
        scrollpulses.setAlignmentY(Component.TOP_ALIGNMENT)
        
        

        panelpulses.add(panel11)
        panelpulses.add(panel12)
        
        
        

        
        panelbpm = JPanel()
        panelbpm.setLayout(BoxLayout(panelbpm, BoxLayout.X_AXIS))
        panelbpm.setBorder(titlebpm)
        
        panelbpm.add(panel2)
        panelbpm.add(panel8)
        panelbpm.add(scrollpulses)
        
        panelmeas = JPanel()
        panelmeas.setLayout(BoxLayout(panelmeas, BoxLayout.Y_AXIS))

        
        panelfoilmeas = JPanel()
        panelfoilmeas.setLayout(BoxLayout(panelfoilmeas, BoxLayout.Y_AXIS))
        panelfoilmeas.add(self.table13.getTableHeader())
        panelfoilmeas.add(self.table13)
        #panelfoilmeas.add(self.table7.getTableHeader())
        #panelfoilmeas.add(self.table7)
        panelfoilmeas.setBorder(title3)
        
        panelbutt = JPanel()
        panelbutt.setLayout(BoxLayout(panelbutt, BoxLayout.X_AXIS))
        panelbutt.add(startbutton)
        panelbutt.add(stopbutton)
        #panelmeas.add(JButton("Stop measurement"))
        
        panelmeas.add(panelfoilmeas)
        panelmeas.add(self.table7.getTableHeader())
        panelmeas.add(self.table7)
        panelmeas.add(panelbutt)
        panelmeas.add(panelbpm)
        
        
        
        
        
        pt2 = JPanel(BorderLayout())
        
        
        pt2.add(pt1,BorderLayout.CENTER)
        pt2.add(pb,BorderLayout.WEST)


        self.panel = JPanel(BorderLayout())
        
        panel1 = JPanel(BorderLayout())
        panel1.add(pt2,BorderLayout.NORTH)
        panel1.add(graphpane,BorderLayout.CENTER)
        

        #self.panel.add(panelopt,BorderLayout.WEST)
        self.panel.add(panel1,BorderLayout.CENTER)
        self.panel.add(panelmeas,BorderLayout.EAST)
        
        
    def updateBPMselection(self):
        self.table2.getSelectionModel().clearSelection()

        for i in range(len(self.ro.allBPMsIds)):
            if self.ro.allBPMsIds[i] in self.ro.d['useBPMs']:
                self.table2.addRowSelectionInterval(i, i)
                
                
    def updatePulsetable(self):
        
        self.table11.getModel().fireTableDataChanged()
        self.table12.getModel().fireTableDataChanged()
        
        if self.table12.getModel().getRowCount() > 0:
            self.table12.setRowSelectionInterval(0, 0)
            
            
    def updatePulseselection(self):

        self.table11.getSelectionModel().clearSelection()
        
        for i in range(len(self.ro.dfoil['BPMWaveForms'].keys())):
            if 'pulse-' + str(i) in self.ro.dfoil['usepulses']:
                self.table11.addRowSelectionInterval(i, i)
                
    def plotPhaseSpaceFoil(self):
        
        self.PSX.removeAllGraphData()
        self.PSY.removeAllGraphData()

        datax = BasicGraphData()
        datax.setDrawLinesOn(False)
        
         
        datay = BasicGraphData()
        datay.setDrawLinesOn(False)

        xarr = [self.ro.davfoil[bpmId]['foil-X'] for bpmId in self.ro.davfoil.keys() if bpmId in self.ro.d['useBPMs']]
        xparr = [self.ro.davfoil[bpmId]['foil-XP'] for bpmId in self.ro.davfoil.keys() if bpmId in self.ro.d['useBPMs']]
        yarr = [self.ro.davfoil[bpmId]['foil-Y'] for bpmId in self.ro.davfoil.keys() if bpmId in self.ro.d['useBPMs']]
        yparr = [self.ro.davfoil[bpmId]['foil-YP'] for bpmId in self.ro.davfoil.keys() if bpmId in self.ro.d['useBPMs']]
        
        
        datax.addPoint(xarr, xparr)
        datay.addPoint(yarr, yparr)
        
        self.PSX.addGraphData(datax)
        self.PSY.addGraphData(datay) 
            
        #----------------------------------------------make scales of two plot equal-----
        self.PSX.setExternalGL(GridLimits())
        self.PSY.setExternalGL(GridLimits())
        """
        Xxmin,Xxmax,Xymin,Xymax = self.PSX.getCurrentMinX(), self.PSX.getCurrentMaxX(), self.PSX.getCurrentMinY(), self.PSX.getCurrentMaxY()
        Yxmin,Yxmax,Yymin,Yymax = self.PSY.getCurrentMinX(), self.PSY.getCurrentMaxX(), self.PSY.getCurrentMinY(), self.PSY.getCurrentMaxY()

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
        
        self.PSX.setLimitsAndTicksX(Xxmin, Xxmax, (Xxmax - Xxmin)/5)
        self.PSX.setLimitsAndTicksY(Xymin, Xymax, (Xymax - Xymin)/10)
        self.PSY.setLimitsAndTicksX(Yxmin, Yxmax, (Yxmax - Yxmin)/5)
        self.PSY.setLimitsAndTicksY(Yymin, Yymax, (Yymax - Yymin)/10)
        """
        
        self.PSX.setLimitsAndTicksX(-20.0, 20.0, (20.0 + 20.0)/5)
        self.PSX.setLimitsAndTicksY(-1.0, 1.0, (1.0 + 1.0)/10)
        self.PSY.setLimitsAndTicksX(-20.0, 20.0, (20.0 + 20.0)/5)
        self.PSY.setLimitsAndTicksY(-1.0, 1.0, (1.0 + 1.0)/10)
        
        #----------------------------------------------make scales of two plot equal-----

        return
    
        
    def plotWaveform(self, bpmId):
        
        self.WFX.removeAllGraphData()
        self.WFY.removeAllGraphData()
        
        gl = GridLimits()
        gl.setXmin(0)
        gl.setXmax(100)
        
        self.WFX.setExternalGL(gl)
        self.WFY.setExternalGL(gl)

        if bpmId in self.ro.davfoil.keys():

            wfx = self.ro.davfoil[bpmId]['waveform-x']
            wfy = self.ro.davfoil[bpmId]['waveform-y']
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

            self.WFX.addGraphData(wdx)
            self.WFY.addGraphData(wdy)
            self.WFX.addGraphData(wdxf)
            self.WFY.addGraphData(wdyf)
                    
                    
    
    def plotPulseWaveform(self, bpmId, pulseInd):
        
        self.PWFX.removeAllGraphData()
        self.PWFY.removeAllGraphData()

        
        gl = GridLimits()
        gl.setXmin(0)
        gl.setXmax(100)
        
        self.PWFX.setExternalGL(gl)
        self.PWFY.setExternalGL(gl)

        
        

        if bpmId in self.ro.dfoil['BPMWaveForms']['pulse-' + str(pulseInd)].keys():

            wfx = self.ro.dfoil['BPMWaveForms']['pulse-' + str(pulseInd)][bpmId]['waveform-x']
            wfy = self.ro.dfoil['BPMWaveForms']['pulse-' + str(pulseInd)][bpmId]['waveform-y']
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

            self.PWFX.addGraphData(wdx)
            self.PWFY.addGraphData(wdy)


        return
    
    
    
    def plotPhaseSpaceBPM(self, bpmId):
        
        self.BPSX.removeAllGraphData()
        self.BPSY.removeAllGraphData()
        self.BPSXY.removeAllGraphData()
        

        if bpmId in self.ro.davfoil.keys():

            AmpX = self.ro.davfoil[bpmId]['ampX']
            AmpY = self.ro.davfoil[bpmId]['ampY']

            phaseX = self.ro.davfoil[bpmId]['phaseX']
            phaseY = self.ro.davfoil[bpmId]['phaseY']

            wX = self.ro.davfoil[bpmId]['omegaX']
            wY = self.ro.davfoil[bpmId]['omegaY']

            kX = self.ro.davfoil[bpmId]['kX']
            kY = self.ro.davfoil[bpmId]['kY']

            alphaX = self.ro.alphasX[bpmId]
            alphaY = self.ro.alphasY[bpmId]
            betaX = self.ro.betasX[bpmId]
            betaY = self.ro.betasY[bpmId]


            #self.ro.dav[kickId][vid][bpmId]['waveform-x']

            arrX = []
            arrY = []
            arrXP = []
            arrYP = []

            xpx = BasicGraphData()
            ypy = BasicGraphData()
            xy = BasicGraphData()
            xpx.setDrawLinesOn(False)
            ypy.setDrawLinesOn(False)
            xy.setDrawLinesOn(False)

            for i in range(self.ro.d['WFLength']):

                #Xbpm  = AmpX*math.exp(kX*i*i)*Math.cos(phaseX + i*wX)
                Xbpm = self.ro.davfoil[bpmId]['waveform-x'][i] - self.ro.davfoil[bpmId]['offsetX']
                XPbpm = -AmpX*math.exp(kX*i*i)*(math.sin(phaseX + i*wX) + alphaX*math.cos(phaseX + i*wX))/betaX
                #Ybpm  = AmpY*math.exp(kY*i*i)*Math.cos(phaseY + i*wY)		
                Ybpm = self.ro.davfoil[bpmId]['waveform-y'][i] - self.ro.davfoil[bpmId]['offsetY']
                YPbpm = -AmpY*math.exp(kY*i*i)*(math.sin(phaseY + i*wY) + alphaY*math.cos(phaseY + i*wY))/betaY

                arrX.append(Xbpm)
                arrXP.append(XPbpm)
                arrY.append(Ybpm)
                arrYP.append(YPbpm)

            xpx.addPoint(arrX, arrXP)
            ypy.addPoint(arrY, arrYP)
            xy.addPoint(arrX, arrY)


            arrXT = []
            arrYT = []
            arrXPT = []
            arrYPT = []

            xpxt = BasicGraphData()
            ypyt = BasicGraphData()
            xyt = BasicGraphData()
            xpxt.setGraphColor(Color.RED)
            ypyt.setGraphColor(Color.RED)
            xyt.setGraphColor(Color.RED)
            xpxt.setDrawLinesOn(False)
            ypyt.setDrawLinesOn(False)
            xyt.setDrawLinesOn(False)
            xpxt.setGraphPointSize(2)
            ypyt.setGraphPointSize(2)
            xyt.setGraphPointSize(2)

            frac = 100
            for j in range(self.ro.d['WFLength']*frac):
                i = j*(1.0/frac)

                Xbpm  = AmpX*math.exp(kX*i*i)*Math.cos(phaseX + i*wX)					    
                XPbpm = -AmpX*math.exp(kX*i*i)*(Math.sin(phaseX + i*wX) + alphaX*Math.cos(phaseX + i*wX))/betaX

                Ybpm  = AmpY*math.exp(kY*i*i)*Math.cos(phaseY + i*wY)				
                YPbpm = -AmpY*math.exp(kY*i*i)*(Math.sin(phaseY + i*wY) + alphaY*Math.cos(phaseY + i*wY))/betaY

                arrXT.append(Xbpm)
                arrXPT.append(XPbpm)
                arrYT.append(Ybpm)
                arrYPT.append(YPbpm)

            xpxt.addPoint(arrXT, arrXPT)
            ypyt.addPoint(arrYT, arrYPT)
            xyt.addPoint(arrXT, arrYT)

            self.BPSX.addGraphData(xpxt)
            self.BPSY.addGraphData(ypyt)
            self.BPSX.addGraphData(xpx)
            self.BPSY.addGraphData(ypy)
            self.BPSXY.addGraphData(xyt)
            self.BPSXY.addGraphData(xy)


        return
    

    
    def plotTunes(self):
        
        self.TP1.removeAllGraphData()
        txty = BasicGraphData()
        txty.setDrawLinesOn(False)
        

        arrtuneX = [self.ro.davfoil[bpmId]['tuneX'] for bpmId in self.ro.davfoil.keys() if bpmId in self.ro.d['useBPMs']]
        arrtuneY = [self.ro.davfoil[bpmId]['tuneY'] for bpmId in self.ro.davfoil.keys() if bpmId in self.ro.d['useBPMs']]
        
        
        txty.addPoint(arrtuneX, arrtuneY)
        self.TP1.addGraphData(txty)
        
        
        #self.TP1.setExternalGL(GridLimits())

        #Xxmin,Xxmax,Xymin,Xymax = self.TP1.getCurrentMinX(), self.TP1.getCurrentMaxX(), self.TP1.getCurrentMinY(), self.TP1.getCurrentMaxY()
        #XYmax = max([Xxmax,Xymax])

        #self.TP1.setLimitsAndTicksX(0, XYmax, XYmax/10)
        #self.TP1.setLimitsAndTicksY(0, XYmax, XYmax/10)



        


        

    

