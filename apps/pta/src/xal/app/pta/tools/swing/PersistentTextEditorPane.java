/**
 * PersistentTextEditorPane.java
 *
 *  Created	: Nov 20, 2009
 *  Author      : Christopher K. Allen 
 */
package xal.app.pta.tools.swing;

import xal.app.pta.tools.logging.IEventLogger;
import xal.app.pta.tools.logging.NullLogger;

import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * This is an text editor class that maintains a backing store
 * in a consist with the current editor state.  All text editor
 * commands are mirrored in the disk file.  As such, if there
 * is an unexpected system crash the information in the text
 * editor is not lost.  
 * 
 * <p>
 * <b>Ported from XAL on Jul 17, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 *
 * @since  Nov 20, 2009
 * @author Christopher K. Allen
 */
public class PersistentTextEditorPane extends TextEditorPane {

    
    /**
     * Used for catching the editor events and mirroring the
     * insert/delete operations on the attached disk file.
     *
     * @since  Nov 20, 2009
     * @author Christopher K. Allen
     */
    class EditHandler implements DocumentListener {

        
        /*
         * Local Attributes
         */
        
        /** The character set used for encoding byte strings */
        private final Charset                 cset;
        
        
        /*
         * Initialization
         */
        
        /**
         * Create a new <code>EditHandler</code> object using
         * the given character set object to encode and decode
         * uni-code characters to and from disk file.
         *
         * @param cset  character set to use during encoding/decoding
         *
         * @since     Nov 30, 2009
         * @author    Christopher K. Allen
         */
        public EditHandler(Charset cset) {
            this.cset = cset;
        }
        
        
        
        /*
         * DocumentListener Interface
         */
        
        /**
         * Not used.
         * 
         * @since       Nov 20, 2009
         * @author  Christopher K. Allen
         *
         * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
         */
        @Override
        public void changedUpdate(DocumentEvent e) {
            
        }

        /**
         * Catch the editor insert text event.  Writes the inserted text to
         * disk file. Must split the file and move the top portion up in order
         * to insert the next (raw byte) text into the file.
         * 
         * @param evt   structure describing the text insertion event
         * 
         * @since       Nov 20, 2009
         * @author  Christopher K. Allen
         *
         * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
         */
        @Override
        public void insertUpdate(DocumentEvent evt) {
            
            try {
                
                String          sstrEdit = getText();
                ByteBuffer      fbufEdit = this.cset.encode(sstrEdit);
                
                dskStore.seek(0);
                dskStore.write(fbufEdit.array());
                
            } catch (IOException e) {
                getLogger().logError(this.getClass(), "I/O error writing edit to disk file");
                e.printStackTrace();
                
            }
        }

//        /**
//         * Catch the editor insert text event.  Writes the inserted text to
//         * disk file. Must split the file and move the top portion up in order
//         * to insert the next (raw byte) text into the file.
//         * 
//         * @param evt   structure describing the text insertion event
//         * 
//         * @since       Nov 20, 2009
//         * @author  Christopher K. Allen
//         *
//         * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
//         */
//        @Override
//        public void insertUpdate(DocumentEvent evt) {
//            
//            // Extract the defining event parameters
//            int         sptrBelow  = evt.getOffset();
//            int         scntInsert = evt.getLength(); 
//            
//            try {
//                // Get the bottom insertion location within the file
//                String     sstrBelow = getDoc().getText(0, sptrBelow);
//                ByteBuffer fbufConv  = this.cset.encode(sstrBelow);
//                int        fptrBelow = fbufConv.capacity();
//                       
//                
//                // -- Debugging
//                String     fstrEnc   = fbufConv.asCharBuffer().toString();
//                String     sstrDec   = this.cset.decode(fbufConv).toString();
//
//                
//                // Get the new character text in the editor and conversion to raw bytes
//                String     sstrInsert = getDoc().getText(sptrBelow, scntInsert);
//                ByteBuffer fbufInsert = this.cset.encode(sstrInsert);
//                int        fcntInsert = fbufInsert.capacity();
//                
//                // --- Debugging
//                ByteBuffer fbufBelow = ByteBuffer.allocate(fptrBelow);
//                dskStore.seek(0);
//                dskStore.read(fbufBelow.array());
//                
//                String    strFile = this.cset.decode(fbufBelow).toString();
//                
//                
//                // Get the insertion location within the file, and read from there
//                //      to the top - we must move this data up the stack  
//                int        fcntAbove = (int) (dskStore.length() - fptrBelow);
//                ByteBuffer fbufAbove = ByteBuffer.allocate(fcntAbove);
//                
//                dskStore.seek(fptrBelow);
//                dskStore.read(fbufAbove.array());
//                
//                // -- Debugging
//                String  sstrAbove = this.cset.decode(fbufAbove).toString();
//
//                
//                // Now insert the new text (converted to bytes) into the file
//                dskStore.setLength(fptrBelow);
//                dskStore.seek(fptrBelow);
//                dskStore.write( fbufInsert.array() );
//
//                // Move the upper block to the new location
//                int        ptrAbove = fptrBelow + fcntInsert;
//                
//                dskStore.seek(ptrAbove);
//                dskStore.write(fbufAbove.array());
//
//                
////                intLen *= this.encEditor.averageBytesPerChar();
////                intPtr *= this.encEditor.averageBytesPerChar();
//                
//            } catch (BadLocationException e) {
//                getLogger().logError(this.getClass(), "Invalid text location in document: ptr=" 
//                                + sptrBelow + ", len=" + scntInsert
//                                );
//                e.printStackTrace();
//                
//            } catch (IOException e) {
//                getLogger().logError(this.getClass(), "I/O error writing to disk file at location ptr=" + sptrBelow);
//                e.printStackTrace();
//                
//            }
//        }

        /**
         * Catch the editor delete text event.  Removes the deleted text from the
         * disk file.
         *
         * @since       Nov 20, 2009
         * @author  Christopher K. Allen
         *
         * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
         */
        @Override
        public void removeUpdate(DocumentEvent evt) {

            try {
                String          sstrEdit = getText();
                ByteBuffer      fbufEdit = this.cset.encode(sstrEdit);
                int             fcntFile = fbufEdit.capacity();
                
                dskStore.seek(0);
                dskStore.write(fbufEdit.array());
                dskStore.setLength(fcntFile);
                
            } catch (IOException e) {
                getLogger().logError(this.getClass(), "General I/O exception while performing text delete update");
                
            } catch (IllegalArgumentException e) {
                getLogger().logError(this.getClass(), "Could not allocate memory buffer for file operation");
                
            } catch (NullPointerException e) {
                getLogger().logError(this.getClass(), "Attempted memory transfer from/to empty buffer.");
                
            } catch (IndexOutOfBoundsException e) {
                getLogger().logError(this.getClass(), "Buffer over/under flow during file operation");
                
            }
        }
        
    }
    
//        /**
//         * Catch the editor delete text event.  Removes the deleted text from the
//         * disk file.
//         *
//         * @since       Nov 20, 2009
//         * @author  Christopher K. Allen
//         *
//         * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
//         */
//        @Override
//        public void removeUpdate(DocumentEvent evt) {
//
//            // Extract the defining event parameters
//            int         intPtrLwr = evt.getOffset();
//            int         intLenDel = evt.getLength(); 
//            
//            
//            try {
//                Long    lngLenOld = dskStore.length();
//                
//
//                // Compute the file pointers delineating the deletion area
//                int     fptrLwr = this.cset.encode( getDoc().getText(0, intPtrLwr) ).capacity();
//                int     cntByt  = this.cset.encode( getDoc().getText(intPtrLwr, intLenDel) ).capacity();
//                int     fptrUpr = fptrLwr + cntByt;
//
//                
//                // Allocate the memory buffer than shift the top of the file down
//                byte[]          arrRaw = new byte[cntByt];
//                
//                dskStore.seek(fptrUpr);
//                dskStore.read(arrRaw);
//                
//                dskStore.seek(fptrLwr);
//                dskStore.write(arrRaw);
//
//                
//                // Tell the disk that the file is smaller now
//                Long    lngLenNew = lngLenOld - cntByt;
//                dskStore.setLength(lngLenNew);
//                
//                
//            } catch (IOException e) {
//                getLogger().logError(this.getClass(), "General I/O exception while performing text delete update");
//                e.printStackTrace();
//                
//            } catch (IllegalArgumentException e) {
//                getLogger().logError(this.getClass(), "Could not allocate memory buffer for file operation");
//                e.printStackTrace();
//                
//            } catch (NullPointerException e) {
//                getLogger().logError(this.getClass(), "Attempted memory transfer from/to empty buffer.");
//                e.printStackTrace();
//                
//            } catch (IndexOutOfBoundsException e) {
//                getLogger().logError(this.getClass(), "Buffer over/under flow during file operation");
//                e.printStackTrace();
//                
//            } catch (BadLocationException e) {
//                getLogger().logError(this.getClass(), "Invalid text location in document: ptr=" 
//                                + intPtrLwr + ", len=" + intLenDel
//                                );
//                e.printStackTrace();
//            }
//        }
//        
//    }
    
    
    
    
    /*
     * Global Constants
     */
    
    /**  Serialization version */
    private static final long serialVersionUID = 1L;
    
    
    
    
    /*
     * Instance Attributes
     */
    
    /** The file name (with path) */
    private final File                  fileStore; 
    
    
    /** The attached file */
    private final RandomAccessFile      dskStore;
    
    /** The editor character set */
    private final Charset               csetEditor;
    
    /** Editor event handler - synchronizes with disk file */
    private final DocumentListener      hndEdtEvts;
    
    /** Event logger to where all exceptions notifications are sent */
    private IEventLogger                lgrEvts;

    
    
    /*
     * Initialization
     */
    
    /**
     * Create a new <code>PersistentTextEditorPane</code> object
     * attached to the backing store identified by the file
     * argument.
     *
     * @param strUrl    URL to the file containing the persistent editor text 
     *
     * @since     Nov 24, 2009
     * @author    Christopher K. Allen
     * 
     * @throws NullPointerException      invalid URL
     * @throws IllegalArgumentException  was not able to open file in read/write mode
     * @throws FileNotFoundException     unusual file open/creation error
     * @throws SecurityException         user does not have read and/or write privileges on file 
     */
    public PersistentTextEditorPane(String strUrl)
        throws NullPointerException, IllegalArgumentException, FileNotFoundException, SecurityException 
    {
        this(new File(strUrl), Charset.defaultCharset() );
    }
    
    /**
     * Create a new <code>PersistentTextEditorPane</code> object
     * attached to the backing store identified by the file
     * argument.
     *
     * @param strUrl    URL to the file containing the persistent editor text 
     * @param strCharset string identifier of the character set to be used by the editor
     *                   (e.g., "UTF-8") 
     *
     * @since     Nov 24, 2009
     * @author    Christopher K. Allen
     * 
     * @throws NullPointerException      invalid URL
     * @throws IllegalCharsetNameException The given character set name is illegal
     * @throws UnsupportedCharsetException No support for the named character set is available 
     * @throws IllegalArgumentException  was not able to open file in read/write mode, or an argument was null
     * @throws FileNotFoundException     unusual file open/creation error
     * @throws SecurityException         user does not have read and/or write privileges on file 
     */
    public PersistentTextEditorPane(String strUrl, String strCharset)
        throws NullPointerException, IllegalCharsetNameException, IllegalArgumentException, 
            FileNotFoundException, SecurityException, 
            UnsupportedCharsetException
    {
        this(new File(strUrl), Charset.forName(strCharset) );
    }
    
    /**
     * Create a new <code>PersistentTextEditorPane</code> object
     * attached to the backing store identified by the file
     * argument.
     *
     * @param fileStore  handle of the file containing the persistent editor text 
     * @param strCharset string identifier of the character set to be used by the editor
     *                   (e.g., "UTF-8") 
     *
     * @since     Nov 24, 2009
     * @author    Christopher K. Allen
     * 
     * @throws NullPointerException      invalid URL
     * @throws IllegalCharsetNameException The given character set name is illegal
     * @throws UnsupportedCharsetException No support for the named character set is available 
     * @throws IllegalArgumentException  was not able to open file in read/write mode, or an argument was null
     * @throws FileNotFoundException     unusual file open/creation error
     * @throws SecurityException         user does not have read and/or write privileges on file 
     */
    public PersistentTextEditorPane(File fileStore, String strCharset)
        throws NullPointerException, IllegalCharsetNameException, IllegalArgumentException, 
            FileNotFoundException, SecurityException, 
            UnsupportedCharsetException
    {
        this(fileStore, Charset.forName(strCharset) );
    }
    
    /**
     * Create a new <code>PersistentTextEditorPane</code> object
     * attached to the backing store identified by the file
     * argument.
     *
     * @param fileStore  file containing the persistent editor text 
     * @param csetEditor character set to be used by the editor 
     *
     * @since     Nov 20, 2009
     * @author    Christopher K. Allen
     * 
     * @throws IllegalArgumentException  was not able to open file in read/write mode
     * @throws FileNotFoundException     unusual file open/creation error
     * @throws SecurityException         user does not have read and/or write privileges on file 
     */
    public PersistentTextEditorPane(File fileStore, Charset csetEditor) 
        throws IllegalArgumentException, FileNotFoundException, SecurityException 
    {
        super();
        
        this.fileStore  = fileStore;
        this.csetEditor = csetEditor;
        this.dskStore   = new RandomAccessFile(this.fileStore, "rw");
        this.hndEdtEvts = new EditHandler(this.csetEditor);
        this.lgrEvts    = new NullLogger();

        this.initEditor();

        // If all went well, add the edit handler to catch the edit events.
        this.getDocument().addDocumentListener(this.hndEdtEvts);
    }

    
    /**
     * Returns the descriptor of the file where used
     * as the backing store.
     *
     * @return  backing store file descriptor
     * 
     * @since  Nov 25, 2009
     * @author Christopher K. Allen
     */
    public File getBackingStoreDescriptor() {
        return this.fileStore;
    }
    
    
    /**
     * Returns the text currently 
     * saved to disk.   
     *
     * @return  text of the saved bug report
     * 
     * @since  Nov 20, 2009
     * @author Christopher K. Allen
     */
    public String       getDiskText() {
        
        String          strTxt = null;
        
        try {
            Long            lngLen = this.dskStore.length();
            
            ByteBuffer      bufRaw = ByteBuffer.allocate(lngLen.intValue());
            
            this.dskStore.seek(0);
            this.dskStore.read(bufRaw.array());
            
            strTxt = this.csetEditor.decode(bufRaw).toString();
            
        } catch (IllegalArgumentException e) {
            this.getLogger().logError(this.getClass(), "Bad file buffer length.");
            
        } catch (EOFException e) {
            this.getLogger().logError(this.getClass(), "End of file during disk read.");

        } catch (IOException e) {
            this.getLogger().logError(this.getClass(), "General I/O exception during disk read.");
            
        }
        
        return strTxt;
    }

    
    /*
     * Operations
     */
    
    /**
     * Sets the event logger where all exception
     * notifications are sent.
     *
     * @param lgrEvts   new event logger
     * 
     * @since  May 7, 2010
     * @author Christopher K. Allen
     */
    public void setLogger(IEventLogger lgrEvts) {
        this.lgrEvts = lgrEvts;
    }
    
    /**
     * Explicit closure of the backing disk storage file.
     *
     * 
     * @since  Nov 24, 2009
     * @author Christopher K. Allen
     */
    public void closeBackingStore() {
        
        try {
            this.dskStore.close();
            
        } catch (IOException e) {
            this.getLogger().logError(this.getClass(), "General I/O exception while closing back store of editor");
            e.printStackTrace();
            
        }
    }
    
    
    
    
    /*
     * Support Methods
     */
    
    /**
     * Initialized the bug reporting system.  Reads the 
     * previous bug reports into the current display.
     * 
     * @since  Nov 19, 2009
     * @author Christopher K. Allen
     * @throws FileNotFoundException 
     * 
     * @throws NullPointerException     invalid URL
     * @throws IllegalArgumentException was not able to open file in read/write mode
     * @throws FileNotFoundException    unusual file open/creation error
     * @throws SecurityException        user does not have read and/or write privileges on file 
     * @throws EOFException             if this file reaches the end before reading all the bytes.
     * @throws IOException              general file reading exception
     */
    private void initEditor() 
        throws NullPointerException, IllegalArgumentException, FileNotFoundException, SecurityException 
    {
        
        try {
            Long        szFile = this.dskStore.length();

            // Allocate a memory buffer then check it
            ByteBuffer  bufRaw = ByteBuffer.allocate( szFile.intValue() );
            if ( !(bufRaw.hasArray()) )
                throw new BufferOverflowException();

            
            // Read disk file into buffer, convert to characters, then create a string
            this.dskStore.readFully(bufRaw.array());
            
            CharBuffer  bufChar = this.csetEditor.decode(bufRaw);
            String      strText = bufChar.toString();
            
            
            // Set the current editor text to the character string contents
            this.setText(strText);
            
            

        } catch (IllegalArgumentException e) {
            String              strMsg = "The buffer capacity given as a negative integer";
            
            this.getLogger().logError(this.getClass(), strMsg);
//            this.getMainWindow().displayError(strErrHdr, strMsg, e);
//            e.printStackTrace();
            
        } catch (BufferOverflowException e) {
            String              strMsg = "Could not allocation raw file buffer.";
            
            this.getLogger().logError(this.getClass(), strMsg);
//            this.getMainWindow().displayError(strErrHdr, strMsg, e);
//            e.printStackTrace();

        } catch (EOFException e) {
            String              strMsg = "Could not read the file into the byte buffer.";
            
            this.getLogger().logError(this.getClass(), strMsg);
//            this.getMainWindow().displayError(strErrHdr, strMsg, e);
//            e.printStackTrace();

        } catch (IOException e) {
            String              strMsg = "General I/O exception on reading editor file.";
            
            this.getLogger().logError(this.getClass(), strMsg);
//            this.getMainWindow().displayError(strErrHdr, strMsg, e);
//            e.printStackTrace();

        }
    }
    
    /**
     * This is a temporary kluge during refactoring.
     *
     * @return  returns the <code>MainApplication</code> global event logger
     * 
     * @since  May 7, 2010
     * @author Christopher K. Allen
     */
    private IEventLogger getLogger() {
        return this.lgrEvts;
    }
    
    

//    /**
//     * Shuts down the bug reporting system.  Saves all the debug information
//     * to file.
//     *
//     * 
//     * @since  Nov 20, 2009
//     * @author Christopher K. Allen
//     */
//    private void saveBugReport() {
//        // Log that we are closing saving the bug report
//        this.logInfo(this.getClass(), "Application saving bug report " + Calendar.getInstance().getTime().toString() );
//        
//        // Get the logging buffer
//        String  strBugRpt = this.getMainWindow().getBugReportView().getText();
//
//        // Create the logging output file and write out log buffer
//        String                strBugRptFile = AppProperties.APP.BUGRPT.getValue().asString();
//        try {
//            OutputStream          osBugRpt  = new FileOutputStream(strBugRptFile);
//            OutputStreamWriter    wtrBugRpt = new OutputStreamWriter(osBugRpt);
//
//            wtrBugRpt.write(strBugRpt.toString());
//            wtrBugRpt.close();
//
//        } catch (SecurityException e) {
//            this.logError(this.getClass(), "Sercurity Violation: Unable to open application bug report file " + strBugRptFile );
//            e.printStackTrace();
//
//        } catch (FileNotFoundException e) {
//            this.logError(this.getClass(), "Bad Location: Unable to open application bug report file " + strBugRptFile );
//            e.printStackTrace();
//            
//        } catch (IOException e) {
//            this.logError(this.getClass(), "Write Error: Unable to write to application bug report file " + strBugRptFile );
//            e.printStackTrace();
//        }
//        
//        
//    }
    


}
