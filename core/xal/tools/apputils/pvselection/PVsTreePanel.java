package xal.tools.apputils.pvselection;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

public class PVsTreePanel extends JPanel{
	private static final long serialVersionUID = 0L;

    private JTree tree;

    /** the tree cell render */
    private TreeCellRenderer render = null;
    private TreeCellRenderer editRender = null;
    private TreeCellRenderer controlRender = null;

    /** the tree cell  TreeSelectionListener */
    private TreeSelectionListener treeSelectionListener = null;
    private TreeSelectionListener editTreeSelectionListener = null;
    private TreeSelectionListener controlTreeSelectionListener = null;

    private ActionListener extTreeSelectionListener = null;

    private int renderMode;
    static public int RENDER_MODE_EDIT    = 0;
    static public int RENDER_MODE_CONTROL = 1;

    /** The constructor.
     *  @param pvNode - the root PV node, specifiing all structure of PVs
     */
    public PVsTreePanel(PVTreeNode pvNode){

	if(pvNode == null){
	    pvNode = new PVTreeNode();
	}

	editRender = new EditModeTreeCellRenderer();
	controlRender = new ControlModeTreeCellRenderer();

        tree = new JTree((TreeNode) pvNode);
	tree.setRootVisible(false); 
        tree.setBackground(getBackground());

	tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION); 

	tree.setShowsRootHandles(true);

        render = controlRender;
	renderMode = RENDER_MODE_CONTROL;
	tree.setCellRenderer(render);

	//Action listener
	MouseListener ml = new MouseAdapter() {
		public void mousePressed(MouseEvent e) {
		    int selRow = tree.getRowForLocation(e.getX(), e.getY());
		    TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
		    if(selRow != -1) {
			Object value = selPath.getLastPathComponent();
			if(value instanceof PVTreeNode){
			    PVTreeNode tn = (PVTreeNode) value;
			    if(extTreeSelectionListener != null){
				ActionEvent evnt = new ActionEvent(tn,1,"selected");
				extTreeSelectionListener.actionPerformed(evnt);
			    } 
			}

		    }
		}
	    };

	tree.addMouseListener(ml);

	//listeners
        editTreeSelectionListener =  new TreeSelectionListener(){
		public void valueChanged(TreeSelectionEvent e){
		    TreePath path_new = e.getNewLeadSelectionPath();
		    if(path_new != null){
			PVTreeNode tn_new = (PVTreeNode) path_new.getLastPathComponent();
			if(tn_new.isPVName() || tn_new.isPVNamesAllowed()){
			    tn_new.setSelected(true);
			}
		    }
		    TreePath path_old = e.getOldLeadSelectionPath();
		    if(path_old != null){
			PVTreeNode tn_old = (PVTreeNode) path_old.getLastPathComponent(); 
			if(tn_old.isPVName() || tn_old.isPVNamesAllowed()){
			    tn_old.setSelected(false);
			}
		    }
		}
	    };

	controlTreeSelectionListener  =  new TreeSelectionListener(){
		public void valueChanged(TreeSelectionEvent e){
		    TreePath path = e.getNewLeadSelectionPath();
		    if(path != null){
			PVTreeNode tn = (PVTreeNode) path.getLastPathComponent();
			if(tn.isPVName()){
			    if(tn.isSwitchedOn()){
				tn.setSwitchedOn(false);
			    }
			    else{
				tn.setSwitchedOn(true);
			    }
			}
		    }
		    tree.clearSelection();
		}
	    };

	treeSelectionListener = controlTreeSelectionListener;
	tree.addTreeSelectionListener(treeSelectionListener);

	JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(tree);

        setLayout(new BorderLayout());
        add(scrollPane,BorderLayout.CENTER);
	setAllFonts(getFont());
    }

    public JTree getJTree(){
	return tree;
    }

    public void setEditMode(){
	render = editRender;
        tree.removeTreeSelectionListener(treeSelectionListener); 
	treeSelectionListener = editTreeSelectionListener;
        tree.setCellRenderer(render);
	tree.addTreeSelectionListener(treeSelectionListener);
        renderMode = RENDER_MODE_EDIT;
    }

    public void setControlMode(){
	render = controlRender;
        tree.removeTreeSelectionListener(treeSelectionListener); 
	treeSelectionListener = controlTreeSelectionListener;
        tree.setCellRenderer(render);
 	tree.addTreeSelectionListener(treeSelectionListener);
        renderMode = RENDER_MODE_CONTROL;
    }

    public void setExtTreeSelectionListener(ActionListener extTreeSelectionListener){
	this.extTreeSelectionListener = extTreeSelectionListener;
    }

    public int getRenderMode(){
        return renderMode;
    }

    public void setAllFonts(Font fnt){
	tree.setFont(fnt);
    }
}

class EditModeTreeCellRenderer implements TreeCellRenderer{

    public EditModeTreeCellRenderer(){}
 
    public Component getTreeCellRendererComponent(JTree tree,
						  Object value,
						  boolean selected,
						  boolean expanded,
						  boolean leaf,
						  int row,
						  boolean hasFocus)
    {
	Font fnt = tree.getFont();
	JPanel treecell = new JPanel();
	treecell.setLayout(new FlowLayout(FlowLayout.LEFT,0,0));
	treecell.setFont(fnt);

        if(value instanceof PVTreeNode){
	    if(((PVTreeNode) value).isPVName()){
                JLabel pv_label   = new JLabel("PV : ",JLabel.LEFT);
		pv_label.setForeground(Color.blue);
                JLabel name_label = new JLabel(((PVTreeNode) value).getName(),JLabel.LEFT);
                treecell.add(pv_label);
                treecell.add(name_label);
		if(selected){
		    pv_label.setBackground(treecell.getBackground().brighter());
		    treecell.setBackground(treecell.getBackground().brighter());
		    name_label.setBackground(treecell.getBackground().brighter());
		}
		if(((PVTreeNode) value).getColor() != null){
		    name_label.setForeground(((PVTreeNode) value).getColor());
		}
                pv_label.setFont(fnt);
		name_label.setFont(fnt);
	    }
	    else{
		JLabel name_label = new JLabel(((PVTreeNode) value).getName(),JLabel.LEFT);
		name_label.setFont(fnt);
		treecell.add(name_label);
		if(((PVTreeNode) value).isPVNamesAllowed()){
		    if(selected){
                        name_label.setForeground(Color.blue);
			name_label.setBackground(treecell.getBackground().brighter());
			treecell.setBackground(treecell.getBackground().brighter());            
		    }
		}
	    }
	}
	return treecell;     
    }
} 


class ControlModeTreeCellRenderer implements TreeCellRenderer{

    static private EmptyBorder epmtyBorder = new EmptyBorder(0,0,0,0);

    public ControlModeTreeCellRenderer(){}
 
    public Component getTreeCellRendererComponent(JTree tree,
						  Object value,
						  boolean selected,
						  boolean expanded,
						  boolean leaf,
						  int row,
						  boolean hasFocus)
    {
	Font fnt = tree.getFont();
	Color bkgColor = tree.getBackground();
	JPanel treecell = new JPanel();
	treecell.setLayout(new FlowLayout(FlowLayout.LEFT,0,0));
	treecell.setFont(fnt);
	treecell.setBackground(bkgColor);

        if(value instanceof PVTreeNode){
	    String name = ((PVTreeNode)value).getName();
	    if(((PVTreeNode) value).isPVName()){
		if(((PVTreeNode) value).isCheckBoxVisible()){
		    JCheckBox chckB = null;
		    if(name != null){
			chckB = new JCheckBox(((PVTreeNode)value).getName());
		    }
		    else{
			chckB = new JCheckBox("");
		    }
		    chckB.setBackground(bkgColor);
		    chckB.setBorder(epmtyBorder);
		    chckB.setFont(fnt);
		    if(((PVTreeNode) value).isSwitchedOn()){
			chckB.setSelected(true);
		    }
		    else{
			chckB.setSelected(false);
		    }
		    treecell.add(chckB);
		    if(((PVTreeNode) value).getColor() != null){
			chckB.setForeground(((PVTreeNode) value).getColor());
		    }
		}
		else{
		    JLabel name_label = null;
		    if(name != null){
			name_label = new JLabel(name,JLabel.LEFT);
		    }
		    else{
                       name_label = new JLabel("",JLabel.LEFT);
		    }
                    name_label.setForeground(((PVTreeNode) value).getColor());
		    name_label.setFont(fnt);
                    treecell.add(name_label);
		}
	    }
	    else{
		JLabel name_label = new JLabel(((PVTreeNode) value).getName(),JLabel.LEFT);
		name_label.setFont(fnt);
		treecell.add(name_label);
	    }
	}
	return treecell;     
    }
} 
