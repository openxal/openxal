package xal.app.lossviewer.dndcomponents;


import xal.app.lossviewer.views.*;
import java.awt.datatransfer.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

public class CollectionTransferHandler<DetectorType> extends TransferHandler {
    DataFlavor localListFlavor, serialListFlavor;
    String localListType = DataFlavor.javaJVMLocalObjectMimeType +
                                ";class=java.util.List";
    SelectionHandler source = null;
    Collection<DetectorType> selection = null;
    

    public CollectionTransferHandler() {
        try {
            localListFlavor = new DataFlavor(localListType);
        } catch (ClassNotFoundException e) {
            System.out.println(
				"CollectionTransferHandler: unable to create data flavor");
        }
        serialListFlavor = new DataFlavor(Collection.class,
                                              "Collection");
    }

    public boolean importData(JComponent c, Transferable t) {
        SelectionHandler<DetectorType> target = null;
		Collection<DetectorType> alist;
       
        if (!canImport(c, t.getTransferDataFlavors())) {
            return false;
        }
        try {
            target = (SelectionHandler<DetectorType>)c;
            if (hasLocalArrayListFlavor(t.getTransferDataFlavors())) {
                alist = (Collection<DetectorType>)t.getTransferData(localListFlavor);
            }
			else if (hasSerialArrayListFlavor(t.getTransferDataFlavors())) {
                alist = (Collection<DetectorType>)t.getTransferData(serialListFlavor);
            } else {
                return false;
            }
        } catch (UnsupportedFlavorException ufe) {
            System.out.println("importData: unsupported data flavor");
            return false;
        } catch (IOException ioe) {
            System.out.println("importData: I/O exception");
            return false;
        }

        //At this point we use the same code to retrieve the data
        //locally or serially.

        

        //Prevent the user from dropping data back on itself.
        
//        if (source==target) {
//			System.out.println(source);
//            return false;
//        }

        View<DetectorType> rootView = target.getRoot();
        rootView.addDetectors(alist);
        return true;
    }

    protected void exportDone(JComponent c, Transferable data, int action) {
       System.out.println(data+" "+action);
    }

    private boolean hasLocalArrayListFlavor(DataFlavor[] flavors) {
        if (localListFlavor == null) {
            return false;
        }

        for (int i = 0; i < flavors.length; i++) {
            if (flavors[i].equals(localListFlavor)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasSerialArrayListFlavor(DataFlavor[] flavors) {
        if (serialListFlavor == null) {
            return false;
        }

        for (int i = 0; i < flavors.length; i++) {
            if (flavors[i].equals(serialListFlavor)) {
                return true;
            }
        }
        return false;
    }

    public boolean canImport(JComponent c, DataFlavor[] flavors) {
        if (hasLocalArrayListFlavor(flavors))  { return true; }
        if (hasSerialArrayListFlavor(flavors)) { return true; }
        return false;
    }

    protected Transferable createTransferable(JComponent c) {
        if (c instanceof SelectionHandler<?>) {
            source = (SelectionHandler<DetectorType>)c;
            selection = source.getSelection();
            
            if (selection==null||selection.size()==0) {
                return null;
            }

            return new CollectionTransferable(selection);
        }
        return null;
    }

    public int getSourceActions(JComponent c) {
        return COPY_OR_MOVE;
    }

    public class CollectionTransferable implements Transferable {
        Collection data;

        public CollectionTransferable(Collection alist) {
            data = alist;
        }

        public Object getTransferData(DataFlavor flavor)
                                 throws UnsupportedFlavorException {
            if (!isDataFlavorSupported(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return data;
        }

        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[] { localListFlavor,
                                      serialListFlavor };
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            if (localListFlavor.equals(flavor)) {
                return true;
            }
            if (serialListFlavor.equals(flavor)) {
                return true;
            }
            return false;
        }
    }
}
