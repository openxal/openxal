/*
 * VerticalLayout.java
 *
 * Created on June 10, 2004, 2:44 PM
 */

package xal.tools.apputils;

import java.awt.*;
import java.util.Vector;
import javax.swing.*;

/**
 * The custom layout manager. This layout manager store components vertically 
 * and provides width that is the maximal one among components. 
 *
 * @version 1.0
 * @author  A. Shishlo
 */

public class VerticalLayout implements LayoutManager {
    private int vgap;
    private int minWidth = 0, minHeight = 0;
    private int preferredWidth = 0, preferredHeight = 0;
    private boolean sizeUnknown = true;

    /* Constructor with vgap = 2. */
    public VerticalLayout() {
        this(2);
    }

    /* Constructor with the vertical gap value parameter. */
    public VerticalLayout(int v) {
        vgap = v;
    }

    /* Required by LayoutManager. */
    public void addLayoutComponent(String name, Component comp) {
    }

    /* Required by LayoutManager. */
    public void removeLayoutComponent(Component comp) {
    }

    private void setSizes(Container parent) {
        int nComps = parent.getComponentCount();
        Dimension d = null;

        //Reset preferred/minimum width and height.
        preferredWidth = 0;
        preferredHeight = 0;
        minWidth = 0;
        minHeight = 0;

        for (int i = 0; i < nComps; i++) {
            Component c = parent.getComponent(i);
            if (c.isVisible()) {
                d = c.getPreferredSize();

                if (i > 0) {
                    preferredHeight += vgap;
                }
                preferredHeight += d.height;

                minWidth = Math.max(c.getPreferredSize().width,minWidth);
                preferredWidth = minWidth;
                minHeight = preferredHeight;
            }
        }
    }


    /* Required by LayoutManager. */
    public Dimension preferredLayoutSize(Container parent) {
        Dimension dim = new Dimension(0, 0);
        int nComps = parent.getComponentCount();

        setSizes(parent);

        //Always add the container's insets!
        Insets insets = parent.getInsets();
        dim.width = preferredWidth 
                    + insets.left + insets.right;
        dim.height = preferredHeight 
                     + insets.top + insets.bottom;

        sizeUnknown = false;

        return dim;
    }

    /* Required by LayoutManager. */
    public Dimension minimumLayoutSize(Container parent) {
        Dimension dim = new Dimension(0, 0);
        int nComps = parent.getComponentCount();

        //Always add the container's insets!
        Insets insets = parent.getInsets();
        dim.width = minWidth 
                    + insets.left + insets.right;
        dim.height = minHeight 
                     + insets.top + insets.bottom;

        sizeUnknown = false;

        return dim;
    }

    /* Required by LayoutManager. */
    /* 
     * This is called when the panel is first displayed, 
     * and every time its size changes. 
     * Note: You CAN'T assume preferredLayoutSize or 
     * minimumLayoutSize will be called -- in the case 
     * of applets, at least, they probably won't be. 
     */
    public void layoutContainer(Container parent) {
        Insets insets = parent.getInsets();
        int maxWidth = parent.getSize().width
                       - (insets.left + insets.right);
        int maxHeight = parent.getSize().height
                        - (insets.top + insets.bottom);
        int nComps = parent.getComponentCount();
        int previousHeight = 0;
        int x = insets.left, y = insets.top;

        // Go through the components' sizes, if neither 
        // preferredLayoutSize nor minimumLayoutSize has 
        // been called.
        if (sizeUnknown) {
            setSizes(parent);
        }

        for (int i = 0 ; i < nComps ; i++) {
            Component c = parent.getComponent(i);
            if (c.isVisible()) {
                Dimension d = c.getPreferredSize();
                
		// increase x and y, if appropriate
                if (i > 0) { 
                    y += previousHeight + vgap;
                }

                // Set the component's size and position.
		//all component have the same width - maximal
		c.setBounds(x, y, minWidth, d.height);

		//old variant - all components have minimal width
                //c.setBounds(x, y, d.width, d.height);

                previousHeight = d.height;
            }
        }
    }

    /** Returns the string that describes an instance. */    
    public String toString() {
        String str = "";
        return getClass().getName() + "[vgap=" + vgap + str + "]";
    }


    /** The main method of the application. */
    static public void main(String[] args) {

	JFrame mainFrame = new JFrame("Test of VerticalLayout class");
	mainFrame.addWindowListener(
	    new java.awt.event.WindowAdapter() {
		public void windowClosing(java.awt.event.WindowEvent evt) {
		    System.exit(0);
		}
	    }
	);

	mainFrame.getContentPane().setLayout(new BorderLayout());

	JPanel panel = new JPanel();
	panel.setLayout(new VerticalLayout(0));
  	mainFrame.getContentPane().add(panel,BorderLayout.WEST);


	JTextField txt_1 = new JTextField("aaa");
	JTextField txt_2 = new JTextField(8);
	txt_2.setText("=========aaa===========");
	txt_2.setFont(new Font(txt_1.getFont().getFamily(),Font.BOLD,50));


	JPanel panel_1 = new JPanel();
        panel_1.setLayout(new GridLayout(0,1,0,0));

        JLabel  label_1   = new JLabel("Label 1 ",SwingConstants.CENTER);
        JLabel  label_2   = new JLabel("Label 2 ",SwingConstants.CENTER);
        panel_1.add(label_1);
        panel_1.add(label_2);


	JPanel panel_2 = new JPanel();
	panel_2.setLayout( new FlowLayout(FlowLayout.LEFT, 2, 2));
        JLabel  label_3   = new JLabel("Text 3: ",SwingConstants.CENTER);
        JTextField txt_3 = new JTextField(" ===========text field 3====");
	panel_2.add(label_3);
	panel_2.add(txt_3);
	panel_2.setBackground(Color.red);

  	panel.add(txt_1);
  	panel.add(txt_2);
  	panel.add(panel_1);
  	panel.add(panel_2);
      
	mainFrame.pack();
	//mainFrame.setSize(new Dimension(300,430));
	mainFrame.setVisible(true);


    }


}

