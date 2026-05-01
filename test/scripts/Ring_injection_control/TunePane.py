from javax.swing import JTable, DefaultCellEditor, JTextField, ListSelectionModel, JLabel, BorderFactory, JButton, JPanel, BoxLayout, JTabbedPane, GroupLayout, LayoutStyle, JRadioButton, JScrollPane, Box, JCheckBox
from java.lang import String, Short, Object, Boolean, Runnable
from java.awt import Color, Font, Dimension, GridLayout, Component, BorderLayout, GridBagLayout, GridBagConstraints, Insets
from javax.swing.table import AbstractTableModel,TableCellRenderer,DefaultTableCellRenderer


from java.text import DecimalFormat
import sys, os, math, random, time
from javax.swing.border import TitledBorder

from xal.extension.widgets.plot import BasicGraphData, FunctionGraphsJPanel




class QuadsData(AbstractTableModel):
	def __init__(self, ro):
            self.columnNames = ["Quads PS","Initial quads","Calculated quads","Use"]
            self.ro = ro
		
	def getColumnCount(self):
            return len(self.columnNames)
		
	def getRowCount(self):
            return len(self.ro.psIds)
		
	def getColumnName(self,col):
            return self.columnNames[col]
		
	def getValueAt(self,row,col):                    
            return [self.ro.psIds[row], str(self.ro.BQuadSet0[row])[0:10], str(self.ro.BQuadSet[row])[0:10], self.ro.b[row]][col]
        
	def setValueAt(self, value, row, col):
            if col == 3:
                self.ro.b[row] = Boolean(value)

	def getColumnClass(self,col):
            return [String, String, String, Boolean][col]
	
	def isCellEditable(self,row,col):
            return col == 3


class TwissData(AbstractTableModel):
	def __init__(self, ro):
            self.columnNames = ["Axis","Initial Ring Tune","Goal Ring Tune","Progress"]
            self.ro = ro
		
	def getColumnCount(self):
            return len(self.columnNames)
		
	def getRowCount(self):
            return 2
	
	def getColumnName(self,col):
            return self.columnNames[col]
		
	def getValueAt(self, row, col):
            if (col == 0):
                return ["X","Y"][row]
            if (col == 1):
                return [str(self.ro.tuneX0)[0:8],str(self.ro.tuneY0)[0:8]][row]
            if (col == 2):
                return [str(self.ro.tuneX)[0:8],str(self.ro.tuneY)[0:8]][row]
            if (col == 3):
                return [str(self.ro.tuneXprogress) +" %",str(self.ro.tuneYprogress)+" %"][row]

            return

	def setValueAt(self, value, row, col):
            if col == 2 and row == 0:
                self.ro.tuneX = float(value)
            if col == 2 and row == 1:
                self.ro.tuneY = float(value)
            return

			
	def getColumnClass(self,col):
            return [String, String, String, String][col]
	
	def isCellEditable(self,row,col):
            return col == 2
        
        
class ClosedOrbitParameters(AbstractTableModel):
	def __init__(self, ro):
            self.columnNames = ["Closed orbit parameters at the foil","initial parameters","final parameters","final - initial"]
            self.ro = ro
		
	def getColumnCount(self):
            return len(self.columnNames)
		
	def getRowCount(self):
            return 4
		
	def getColumnName(self,col):
            return self.columnNames[col]
		
	def getValueAt(self, row, col):
            if col == 0:
                return ["x (mm)","xp (mrad)","y (mm)","yp (mrad)"][row]
            if col == 1:
                return [str(0)[0:8],str(0)[0:8],str(0)[0:8],str(0)[0:8]][row]
            if col == 2:
                return [str(0)[0:8],str(0)[0:8],str(0)[0:8],str(0)[0:8]][row]
            if col == 3:
                return [str(0)[0:8],str(0)[0:8],str(0)[0:8],str(0)[0:8]][row]

	def setValueAt(self, value, row, col):
            return
        
	def getColumnClass(self,col):
            return [String, String, String, String][col]
	
	def isCellEditable(self,row,col):
            return None




class TunePane:
    
    def __init__(self, ro):
        
        td = TwissData(ro)
        qd = QuadsData(ro)
        #od = ClosedOrbitParameters(ro)
        table1 = JTable(qd)
        table2 = JTable(td)
        #table3 = JTable(od)

        table1.setRowSelectionAllowed(False)
        table1.setFocusable(False)
        #table2.getColumnModel().getColumn(0).setPreferredWidth(50)
        #table2.setRowHeight(0, 30)
        #table2.setRowHeight(1, 30)
        #table2.setFont(Font("Arial", 0, 20))
        centerRenderer = DefaultTableCellRenderer()
        centerRenderer.setHorizontalAlignment( JLabel.CENTER )

        table2.setRowSelectionAllowed(False)
        table2.setFocusable(False)
        singleclick = DefaultCellEditor(JTextField())
        singleclick.setClickCountToStart(1)
        table2.setDefaultEditor(table2.getColumnClass(2), singleclick)
        table2.getColumnModel().getColumn(0).setCellRenderer(centerRenderer)
        table2.getColumnModel().getColumn(3).setCellRenderer(centerRenderer)


        th1 = table1.getTableHeader()
        #th1.setBackground(Color(153, 204, 255))
        #th1.setForeground(Color.white)
        #th1.setFont(Font("Arial", Font.BOLD, 14))
        

        th2 = table2.getTableHeader()
        #th2.setBackground(Color(153, 204, 255))
        #th2.setForeground(Color.white)
        #th2.setFont(Font("Arial", Font.BOLD, 14))

        #table.getColumnModel().getColumn(1).setCellRenderer(DecimalFormatRenderer() )

        #nr = NumberRenderer()
        #mod.getColumn(1).setCellRenderer(nr)





        def action1(event):
            class Thr(Runnable):
                def run(self):
                    _action1(event)
            Thread(Thr()).start()  
        def _action1(event):
            ro.multisteps(td)
            td.fireTableDataChanged()
            qd.fireTableDataChanged()


        def action2(event):
            class Thr(Runnable):
                def run(self):
                    _action2(event)
            Thread(Thr()).start()  
        def _action2(event):
            ro.setLiveQuads()
            td.fireTableDataChanged()
            qd.fireTableDataChanged()

            return

        b1 = JButton("2. Calculate quads",actionPerformed = action1)
        b2 = JButton("3. Set calculated quads",actionPerformed = action2)
        
        b1.setAlignmentX(Component.CENTER_ALIGNMENT)
        b2.setAlignmentX(Component.CENTER_ALIGNMENT)


        self.panel = JPanel(BorderLayout())

        blackline = BorderFactory.createLineBorder(Color.black)
        
        title1 = BorderFactory.createTitledBorder(blackline, "Initial and final settings in the Ring")
        title1.setTitleJustification(TitledBorder.CENTER)

        title2 = BorderFactory.createTitledBorder(blackline, "Tunes control in the Ring")
        title2.setTitleJustification(TitledBorder.CENTER)
        
        title3 = BorderFactory.createTitledBorder(blackline, "Tune control")
        title3.setTitleJustification(TitledBorder.CENTER)

        pt1 = JPanel()

        pt1.setLayout(BoxLayout(pt1, BoxLayout.Y_AXIS))
        pt1.setBorder(title1)
        
        #th2 = table2.getTableHeader()
        pt1.add(table2.getTableHeader())
        pt1.add(table2)
        
        
        #cwidth = table2.getColumnModel().getColumn(0).getWidth()
        

        
        #pt1.add(Box.createRigidArea(Dimension(0, table2.getRowHeight())))
        
        

        #buttonPanel1 = JPanel(GridLayout(1,4))
        #buttonPanel1.setBorder(BorderFactory.createEmptyBorder(-5, -5, -5, -5))
        #buttonPanel1.setOpaque(False)
        #buttonPanel1.add(JLabel(""))
        #buttonPanel1.add(JLabel(""))
        #buttonPanel1.add(b1)
        #buttonPanel1.add(JLabel(""))
        #pt1.add(buttonPanel1)

        #pt1.add(Box.createRigidArea(Dimension(10, 10)))
        #th1 = table1.getTableHeader()
        
        #pt1.add(buttonPanel1)
        
        pt1.add(table1.getTableHeader())
        pt1.add(table1)
        
        
        #pt1.add(table3.getTableHeader())
        #pt1.add(table3)
        
        pt2 = JPanel(BorderLayout())
        

        
        pb = JPanel(GridLayout(5,1,5,5))
        pb.setBorder(title3)
        
        l1 = JLabel("1. Setup X or Y Goal Tune")
        l1.setHorizontalAlignment(JLabel.CENTER)
        l1.setBorder(BorderFactory.createLineBorder(Color.black))
        pb.add(l1)
        pb.add(b1)
        pb.add(b2)
        pb.add(JLabel(""))
        pb.add(JLabel(""))
        
        pt2.add(pt1,BorderLayout.CENTER)
        pt2.add(pb,BorderLayout.WEST)

        #buttonPanel2 = JPanel(GridLayout(1,4))
        #buttonPanel2.add(JLabel(""))
        #buttonPanel2.add(JLabel(""))
        #buttonPanel2.add(b2)
        #buttonPanel2.add(JLabel(""))
        
        #pt1.add(buttonPanel2)
        #pt1.add(buttonPanel2)

        #layout = GroupLayout(th1)
        #th1.setLayout(layout)
        #layout.setHorizontalGroup(layout.createSequentialGroup().addComponent(buttonPanel1))
        #layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(buttonPanel1))
             
        



        #pt2 = JPanel()
        #pt2.setLayout(BoxLayout(pt2, BoxLayout.Y_AXIS))
        #pt2.setBorder(title2)

        #pt2.add(table2.getTableHeader())
        #pt2.add(table2)




        #pt3 = JPanel(GridLayout(1,5, 2,2))
        #pt3.setMaximumSize(Dimension(1000000, 70))
        #pt3.setBorder(BorderFactory.createLineBorder(Color.black))
        #pt3.setBorder(BorderFactory.createEtchedBorder())
        #pt3.setBorder(BorderFactory.createEmptyBorder(20, 2, 20, 2))

        #pt3.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "title"))



        self.JP = FunctionGraphsJPanel()


        #self.panel.add(pt3)
        #self.panel.add(pt1)
        #self.panel.add(pt2)
        
        self.panel.add(pt2,BorderLayout.NORTH)
        #self.panel.add(pt2,BorderLayout.WEST)
        self.panel.add(self.JP,BorderLayout.CENTER)

        

    

