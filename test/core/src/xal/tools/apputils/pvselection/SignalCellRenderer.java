package xal.tools.apputils.pvselection;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import javax.swing.border.*;

public class SignalCellRenderer implements TreeCellRenderer{

    private EmptyBorder epmtyBorder = new EmptyBorder(0,0,0,0);

    public SignalCellRenderer(){};
 
    public Component getTreeCellRendererComponent(JTree tree,
						  Object value,
						  boolean selected,
						  boolean expanded,
						  boolean leaf,
						  int row,
						  boolean hasFocus)
    {
	JPanel treecell = new JPanel();
	treecell.setLayout(new FlowLayout(FlowLayout.LEFT,0,0));
	treecell.setFont(tree.getFont());

        if(value instanceof HandleNode){

	    if(((HandleNode) value).isSignal()){
		JCheckBox center = new JCheckBox(((HandleNode)value).toString());
		center.setFont(tree.getFont());
                center.setBorder(epmtyBorder); 
		center.setSelected(selected);
		if(selected){
		    center.setBackground(treecell.getBackground().brighter());
		}
		treecell.add(center);
	    }
	    else{
		JLabel label = new JLabel(((HandleNode)value).toString(),JLabel.LEFT);
		label.setFont(tree.getFont());
                treecell.add(label);
		if(row == 0){
		    label.setForeground(Color.red);
		}
	    }
	}
	return treecell;     
    }
}















