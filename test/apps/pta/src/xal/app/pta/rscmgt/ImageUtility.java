/**
 * ImageUtility.java
 *
 *  Created	: Jun 15, 2009
 *  Author      : Christopher K. Allen 
 */
package xal.app.pta.rscmgt;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.net.URL;

import javax.swing.ImageIcon;

/**
 * <h1>ImageUtility</h1>
 * <p>
 * Utility class for processing images and icons represented
 * by AWT 
 * <code>{@link java.awt.Image}</code> and 
 * <code>{@link javax.swing.ImageIcon}</code>
 * objects.
 * </p>
 * 
 * <p>
 * <b>Ported from XAL on Jul 17, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @since  Jun 15, 2009
 * @author Christopher K. Allen
 * 
 * @see java.awt.Image
 * @see javax.swing.ImageIcon
 */
public class ImageUtility {

    
    /*
     * Global Constants
     */
    
    /** 
     * Size of thumb nail images.  Thumb nail images have size
     * 32&times;32.
     */
    public static final int             INT_THUMBNAIL = 32;
    
    
    /** 
     * Size of tool bar images.  Tool bar images have size
     * 24&times;24. 
     */
    public static final int             INT_TOOLBAR = 24;
    
    
    
    
    
    /*
     * Images
     */
    
    /**
     * <p>
     * Resizes the given image using a 
     * <code>Graphics2D</code> object backed by a 
     * <code>BufferedImage</code> object.
     * This method is the main engine for all the 
     * image re-scaling methods in this class.
     * </p>
     * <p>
     * Use this method when the resultant image is smaller
     * than the original.
     * </p>
     * 
     * @param imgSource - source image to scale
     * @param w - desired width
     * @param h - desired height
     * 
     * @return - the new resized image
     * 
     * @since  Jun 15, 2009
     * @author Christopher K. Allen
     *
     */
    public static Image downsizeImage(Image imgSource, int w, int h){
        BufferedImage imgResized = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D    g2dResized = imgResized.createGraphics();
        
//        g2dResized.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2dResized.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
//        g2dResized.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2dResized.drawImage(imgSource, 0, 0, w, h, null);
        g2dResized.dispose();
        
        return imgResized;
    }
    
    /**
     * <p>
     * Resizes the given image using a 
     * <code>Graphics2D</code> object backed by a 
     * <code>BufferedImage</code> object.
     * </p>
     * <p>
     * Use this method when the resultant image is larger
     * than the original.
     * </p>
     * 
     * @param imgSource - source image to scale
     * @param w - desired width
     * @param h - desired height
     * 
     * @return - the new resized image
     * 
     * @since  Jun 15, 2009
     * @author Christopher K. Allen
     *
     */
    public static Image upsizeImage(Image imgSource, int w, int h){
        BufferedImage imgResized = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D    g2dResized = imgResized.createGraphics();
        
        g2dResized.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2dResized.drawImage(imgSource, 0, 0, w, h, null);
        g2dResized.dispose();
        
        return imgResized;
    }
    
    /**
     * Creates and returns a thumb-nail sized copy of the given image.
     *
     * @param imgSource         the source image to copy
     * 
     * @return  a copy of the argument rescaled for thumb nail display 
     * 
     * @since  Jun 15, 2009
     * @author Christopher K. Allen
     * 
     * @see     ImageUtility#INT_THUMBNAIL
     * @see     ImageUtility#downsizeImage(Image, int, int)
     */
    public static Image createThumbnailImage(Image imgSource)        {
        return ImageUtility.downsizeImage(imgSource, INT_THUMBNAIL, INT_THUMBNAIL);
    }
    
    /**
     * Creates and returns a tool-bar sized copy of the given image.
     *
     * @param imgSource         the source image to copy
     * 
     * @return  a copy of the argument rescaled for tool bar display 
     * 
     * @since  Jun 15, 2009
     * @author Christopher K. Allen
     * 
     * @see     ImageUtility#INT_TOOLBAR
     * @see     ImageUtility#downsizeImage(Image, int, int)
     */
    public static Image createToolbarImage(Image imgSource)        {
        return ImageUtility.downsizeImage(imgSource, INT_TOOLBAR, INT_TOOLBAR);
    }
    
    /**
     * Loads the icon at the given URL, then creates and returns 
     * a thumb-nail sized image of the icon.
     *
     * @param urlSource         URL of the icon to be loaded and rescaled
     * 
     * @return  the image at the given URL rescaled for thumb-nail display 
     * 
     * @since  Jun 15, 2009
     * @author Christopher K. Allen
     * 
     * @see     ImageUtility#createThumbnailImage(Image)
     */
    public static Image createThumbnailImage(URL urlSource) {
        ImageIcon icnFull  = new ImageIcon(urlSource);
        Image     imgTBar  = ImageUtility.createThumbnailImage(icnFull.getImage());
        
        return imgTBar;
    }
    
    /**
     * Loads the icon at the given URL, then creates and returns 
     * a tool-bar sized image of the icon.
     *
     * @param urlSource         URL of the icon to be loaded and rescaled
     * 
     * @return  the image at the given URL rescaled for tool bar display 
     * 
     * @since  Jun 15, 2009
     * @author Christopher K. Allen
     * 
     * @see     ImageUtility#createToolbarImage(Image)
     */
    public static Image createToolbarImage(URL urlSource) {
        ImageIcon icnFull  = new ImageIcon(urlSource);
        Image     imgTBar  = ImageUtility.createToolbarImage(icnFull.getImage());
        
        return imgTBar;
    }
    
    

    /*
     * Icons
     */
    
    /**
     * Convenience method resizes the given icon using a
     * call to <code>{@link #downsizeImage(Image, int, int)}</code>.
     * This method is the main engine for the other
     * icon methods in the utility class.
     * 
     * @param icnSource - source icon to scale
     * @param w - desired width
     * @param h - desired height
     * 
     * @return - the new resized icon
     * 
     * @see     ImageUtility#downsizeImage(Image, int, int)
     */
    public static ImageIcon resizeIcon(ImageIcon icnSource, int w, int h){
        Image imgResized = ImageUtility.downsizeImage(icnSource.getImage(), w, h);
        ImageIcon icnResized = new ImageIcon( imgResized );
        
        return icnResized;
    }
    
    /**
     * Creates and returns a thumb-nail sized copy of the given icon.
     *
     * @param icnSource         the source icon to copy
     * 
     * @return  a copy of the argument rescaled for thumb nail display 
     * 
     * @since  Jun 15, 2009
     * @author Christopher K. Allen
     * 
     * @see     ImageUtility#INT_THUMBNAIL
     * @see     ImageUtility#resizeIcon(ImageIcon, int, int)
     */
    public static ImageIcon createThumbnailIcon(ImageIcon icnSource)        {
        return ImageUtility.resizeIcon(icnSource, INT_THUMBNAIL, INT_THUMBNAIL);
    }
    
    /**
     * Creates and returns a tool-bar sized copy of the given icon.
     *
     * @param icnSource         the source icon to copy
     * 
     * @return  a copy of the argument rescaled for tool bar display 
     * 
     * @since  Jun 15, 2009
     * @author Christopher K. Allen
     * 
     * @see     ImageUtility#INT_TOOLBAR
     * @see     ImageUtility#resizeIcon(ImageIcon, int, int)
     */
    public static ImageIcon createToolbarIcon(ImageIcon icnSource)        {
        return ImageUtility.resizeIcon(icnSource, INT_TOOLBAR, INT_TOOLBAR);
    }

    /**
     * Loads the icon at the given URL, then creates and returns 
     * a tool-bar sized copy of the icon.
     *
     * @param urlSource         URL of the icon to be loaded and rescaled
     * 
     * @return  the image at the given URL rescaled for thumb nail display 
     * 
     * @since  Jun 15, 2009
     * @author Christopher K. Allen
     * 
     * @see     ImageUtility#createThumbnailIcon(ImageIcon)
     */
    public static ImageIcon createThumbnailIcon(URL urlSource) {
        ImageIcon icnFull  = new ImageIcon(urlSource);
        ImageIcon icnTBar  = ImageUtility.createThumbnailIcon(icnFull);
        
        return icnTBar;
    }
    
    
    /**
     * Loads the icon at the given URL, then creates and returns 
     * a tool-bar sized copy of the icon.
     *
     * @param urlSource         URL of the icon to be loaded and rescaled
     * 
     * @return  the image at the given URL rescaled for tool bar display 
     * 
     * @since  Jun 15, 2009
     * @author Christopher K. Allen
     * 
     * @see     ImageUtility#createToolbarIcon(ImageIcon)
     */
    public static ImageIcon createToolbarIcon(URL urlSource) {
        ImageIcon icnFull  = new ImageIcon(urlSource);
        ImageIcon icnTBar  = ImageUtility.createToolbarIcon(icnFull);
        
        return icnTBar;
    }
    
    
    
    
    /*
     * Support Methods
     */
    
    
    /**
     * Prevent instantiation of any 
     * <code>ImageUtility</code> objects.
     *
     *
     * @since     Jun 15, 2009
     * @author    Christopher K. Allen
     */
    private ImageUtility() {};
}
