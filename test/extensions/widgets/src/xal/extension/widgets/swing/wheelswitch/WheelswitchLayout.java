/*
 * @@COPYRIGHT@@
 */
package xal.extension.widgets.swing.wheelswitch;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;

import xal.extension.widgets.swing.Wheelswitch;

/**
 * Implementation of <code>java.awt.LayoutManager</code> to be used for
 * the layout of the <code>Wheelswitch</code> component.
 * 
 * @author <a href="mailto:jernej.kamenik@cosylab.com">Jernej Kamenik</a>
 * @version $id$
 */
public class WheelswitchLayout implements LayoutManager {
  
	/**
	 * Constructor for WheelswitchLayout
	 */
	public WheelswitchLayout() {
		super();
	}
	
	/**
	 * Empty implementation.
	 * 
	 * @see LayoutManager#addLayoutComponent(String, Component)
	 */
    @Override
	public void addLayoutComponent(String name, Component comp) {
	}

	/**
	 * Empty implementation.
	 * 
	 * @see LayoutManager#removeLayoutComponent(Component)
	 */
    @Override
	public void removeLayoutComponent(Component comp) {
	}

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
		Wheelswitch wswitch = (Wheelswitch)parent;
		Component comp;
		int numberOfComponents = wswitch.getComponentCount();
		if (numberOfComponents<2) return;
		
		int preferredWidth = wswitch.getPreferredSize().width;
		int height = wswitch.getHeight();
		int width = wswitch.getWidth();
//  mkadunc	
//	int digitInset = Math.max(height/10,1);
  	int digitInset = 0;
		int compWidth;
		int compHeight;

        if (width==0 || height==0) return;

		double widthRatio = 1.*width/preferredWidth;

		if (wswitch.isEditable()) {
			numberOfComponents-=1;
			comp = wswitch.getComponent(numberOfComponents);
			compHeight = height;
			if (comp.getPreferredSize().height<1)  {
				compWidth = comp.getPreferredSize().width;
			} else  {
				compWidth = height*comp.getPreferredSize().width/comp.getPreferredSize().height;
			}
			int digitWidth=(int)(wswitch.getComponent(0).getPreferredSize().width*widthRatio);
			if (compWidth>digitWidth) {
				compWidth=digitWidth;
				digitInset=(height-2*compWidth)/2;
				compHeight=height-2*digitInset;
			} 
			width -= compWidth;
			comp.setBounds(width,digitInset,compWidth,compHeight);
			widthRatio = 1.*width/(preferredWidth-comp.getPreferredSize().width);
		} else  {
			int digitWidth=(int)(wswitch.getComponent(0).getPreferredSize().width*widthRatio);
			if (height>2*digitWidth) {
				digitInset=(height-2*digitWidth)/2;
			} 
		}

		int x = 0;
		int y;
		for (int i=0; i<numberOfComponents; i++) {
			comp = wswitch.getComponent(i);
			if (comp instanceof Digit) {
				y = digitInset;
				compHeight = height-2*digitInset;
			} else {
				y = height/2;
				compHeight = 0;
			}
			compWidth = (int)(comp.getPreferredSize().width*widthRatio);
			comp.setBounds(x,y,compWidth,compHeight);
			x += compWidth;
		}
	}
}
