class EditableHeaderRenderer(TableCellRenderer):



    def __init__(self, editor):
        self.table = None
        self.reporter = None
        self.editor = editor
        self.editor.setBorder(UIManager.getBorder("TableHeader.cellBorder"))


    def getTableCellRendererComponent(self,table, value, isSelected, hasFocus, row, col):
        
        if (table != None and self.table != table):
            self.table = table
            header = table.getTableHeader()
            if (header != None):
                self.editor.setForeground(header.getForeground()) 
                self.editor.setBackground(header.getBackground()) 
                self.editor.setFont(header.getFont())
                self.reporter = MouseEventReposter(header, col, self.editor)
                header.addMouseListener(self.reporter)
            
    

        if (self.reporter != None):
            self.reporter.setColumn(col)

        return self.editor


class MouseEventReposter(MouseAdapter):

    def __init__(self, header, column, editor):
        
        self.dispatchComponent = None
        self.header = header
        self.column = column
        self.editor = editor
            
            
    def setColumn(self, column):
        self.column = column
            
    
    def setDispatchComponent(self, e):
        col = header.getTable().columnAtPoint(e.getPoint())
        if (col != self.column or col == -1):
            return

        p = e.getPoint()
        p2 = SwingUtilities.convertPoint(self.header, p, self.editor)
        self.dispatchComponent = SwingUtilities.getDeepestComponentAt(self.editor, p2.x, p2.y)


    def repostEvent(self,e):
        if (dispatchComponent == null):
            return False
        e2 = SwingUtilities.convertMouseEvent(self.header, e, self.dispatchComponent)
        self.dispatchComponent.dispatchEvent(e2)
        return True
    


    def mousePressed(self, e):  
        if (self.header.getResizingColumn() == None):
            p = e.getPoint()

            col = self.header.getTable().columnAtPoint(p)
            if (col != column or col == -1):
                return

                int index = header.getColumnModel().getColumnIndexAtX(p.x);
                if (index == -1) return;

                editor.setBounds(header.getHeaderRect(index));
                header.add(editor);
                editor.validate();
                setDispatchComponent(e);
                repostEvent(e);
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            repostEvent(e);
            dispatchComponent = null;
            header.remove(editor);
        }
    }
}