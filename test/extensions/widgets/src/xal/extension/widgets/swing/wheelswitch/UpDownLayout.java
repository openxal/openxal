/*
 * @@COPYRIGHT@@
 */
package xal.extension.widgets.swing.wheelswitch;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;

/**
 * Implementation of <code>java.awt.LayoutManager</code> to be used for
 * <code>UpDownButton</code> layout.
 * 
 * @author <a href="mailto:jernej.kamenik@cosylab.com">Jernej Kamenik</a>
 * @version $id$
 */
public class UpDownLayout implements LayoutManager {
	
	/**
	 * @see LayoutManager#addLayoutComponent(String, Component)
	 */
    @Override
	public void addLayoutComponent(String name, Component comp) {
	}

	/**
	 * @see LayoutManager#removeLayoutComponent(Component)
	 */
    @Override
	public void removeLayoutComponent(Component comp) {}

	/**
	 * @see LayoutManager#preferredLayoutSize(Container)
	 */
    @Override
	public Dimension preferredLayoutSize(Container parent) {
		return parent.getPreferredSize();
	}

	/**
	 * @see LayoutManager#minimumLayoutSize(Container)
	 */
    @Override
	public Dimension minimumLayoutSize(Container parent) {
		return parent.getMinimumSize();
	}

	/**
	 * @see LayoutManager#layoutContainer(Container)
	 */
    @Override
	public void layoutContainer(Container parent) {
		UpDownButton upDownButton = (UpDownButton)parent;
		if (upDownButton==null || upDownButton.getComponentCount()!=2) return;		
		int height = upDownButton.getHeight();
		int width = upDownButton.getWidth();
		
		upDownButton.getComponent(0).setBounds(0,0,width,Math.round(height/2f)+1);			
		upDownButton.getComponent(1).setBounds(0,Math.round(height/2f)-1,width,height/2+1);			
		upDownButton.revalidate();
	}	
}
