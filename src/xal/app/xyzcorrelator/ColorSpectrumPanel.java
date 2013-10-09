//
//  ColorSpectrumPanel.java
//  xal
//
//  Created by Tom Pelaia on 12/11/08.
//  Copyright 2008 Oak Ridge National Lab. All rights reserved.
//

package xal.app.xyzcorrelator;

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JPanel;


/** Color spectrum from red to blue */
class ColorSpectrumPanel extends JPanel {
    /** serialization ID */
    private static final long serialVersionUID = 1L;
	/** Constructor */
	public ColorSpectrumPanel() {}
	
	
	/** generate a color from red to blue corresponding to a color value from 0 (red) to 1 (blue) */
	public static Color getColor( final float colorValue ) {
		final float redness = ( ( 1.0f - colorValue ) * ( 1.0f - colorValue ) );
		final float greeness = ( colorValue < 0.5 ? 4 * colorValue * colorValue : 4 * ( 1.0f - colorValue ) * ( 1.0f - colorValue ) );
		final float blueness = colorValue * colorValue;
		return new Color( redness, greeness, blueness );
	}
	
	
	/** paint the spectrum */
	public void paint( final java.awt.Graphics graphics ) {
		final Dimension size = this.getSize();
		final int width = (int)size.getWidth();
		final int height = (int)size.getHeight();
		for ( int x = 0 ; x < width ; x++ ) {
			final float colorValue = (float)x / width;
			graphics.setColor( getColor( colorValue ) );
			graphics.drawLine( x, 0, x, height );
		}
	}
}
