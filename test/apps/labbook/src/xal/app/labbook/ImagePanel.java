//
//  ImagePanel.java
//  xal
//
//  Created by Tom Pelaia on 2/2/07.
//  Copyright 2007 Oak Ridge National Lab. All rights reserved.
//

package xal.app.labbook;

import java.awt.*;
import java.awt.image.*;
import javax.swing.*;


/** panel for displaying an image */
public class ImagePanel extends JPanel {
    /** ID for serializable version */
    private static final long serialVersionUID = 1L;
	/** image icon to display */
	protected ImageIcon _imageIcon;

	
	/** set the new image to display */
	public void setIcon( final ImageIcon imageIcon ) {
		_imageIcon = imageIcon;
	}
	
	
	/** override the paint method to paint the image */
	public void paint( final Graphics graphics ) {		
		final int width = getWidth();
		final int height = getHeight();
		
		graphics.clearRect( 0, 0, width, height );
		
		final ImageIcon icon = _imageIcon;
		if ( icon != null ) {
			final Image image = icon.getImage();
			
			// we want to preserve the aspect ration of the image but scale it down to fit inside the panel
			final int imageWidth = icon.getIconWidth();
			final int imageHeight = icon.getIconHeight();
			final boolean scaleToWidth = height != 0 && imageHeight != 0 ? ((double)width) / height < ((double)imageWidth) / imageHeight : false;
			
			if ( scaleToWidth ) {
				final double scale = imageWidth == 0 ? 1.0 : ((double)width) / imageWidth;
				final int displayHeight = (int) ( imageHeight * scale );
				graphics.drawImage( image, 0, 0, width, displayHeight, this );
			}
			else {
				final double scale = imageHeight == 0 ? 1.0 : ((double)height) / imageHeight;
				final int displayWidth = (int) ( imageWidth * scale );
				graphics.drawImage( image, 0, 0, displayWidth, height, this );
			}
		}
	}
}
