/*
 * URLUtil.java
 *
 * Created on June 19, 2002, 9:02 AM
 */

package xal.tools;

import java.net.URL;
import java.io.File;

/**
 * URLUtil is a convenience class of static methods that allow the user to 
 * convert between file path and URL specifications.
 *
 * @author  tap
 */
public class URLUtil {

    /** Creates new URLUtil */
    protected URLUtil() {}
    

    /** Convert a file to a URL specification */
    static public String urlSpecForFile(File file) throws FilePathException {
        try {
            return file.toURI().toURL().toString();
        }
        catch(Exception excpt) {
            throw new FilePathException( file.getPath() );
        }
    }
    
    
    /** Convert a file path to a URL specification */
    static public String urlSpecForFilePath( final String filePath ) throws FilePathException {
        try {
            final File mainFile = new File( filePath );			
			return mainFile.toURI().toURL().toString();
        }
        catch( Exception exception ) {
            throw new FilePathException( filePath );
        }
    }
    
    
    /** Convert a URL specification to a file path */
    static public String filePathForUrlSpec( final String urlSpec ) throws UrlSpecException {
		try {
			return filePathForUrl( new URL( urlSpec ) );
		}
		catch( Exception exception ) {
			throw new UrlSpecException( urlSpec );
		}
    }
    
    
    /** Convert a URL to a file path */
    static public String filePathForUrl( final URL url ) {
        try {
            return new File( url.toURI() ).getAbsolutePath();
        }
        catch( Exception exception ) {
			exception.printStackTrace();
			return null;
        }
    }
    
    
    
    /** 
     * Exception for bad file path specification.
     */
    static public class FilePathException extends RuntimeException {
        /** serialization ID */
        private static final long serialVersionUID = 1L;
        
        private String filePath;
        
        public FilePathException(String newFilePath) {
            filePath = newFilePath;
        }
        
        public String filePath() {
            return filePath;
        }
        
        public String toString() {
            return "Invalid file path specification: " + filePath;
        }
    }

    
    
    /** 
     * Exception for bad file path specification.
     */
    static public class UrlSpecException extends RuntimeException {
        /** serialization ID */
        private static final long serialVersionUID = 1L;
        
        private String urlSpec;
        
        public UrlSpecException(String newUrlPath) {
            urlSpec = newUrlPath;
        }
        
        public String urlSpec() {
            return urlSpec;
        }
        
        public String toString() {
            return "Invalid URL path specification: " + urlSpec;
        }
    }
}
