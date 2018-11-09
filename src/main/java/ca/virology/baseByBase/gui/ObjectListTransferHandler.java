package ca.virology.baseByBase.gui;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.List;

public class ObjectListTransferHandler extends TransferHandler {
    DataFlavor localObjectListFlavor;
    String localObjectType = DataFlavor.javaJVMLocalObjectMimeType + ";class=java.lang.Object";
    JList source = null;
    JList target = null;

    int[] indices = null;
    int addIndex = -1; //Location where items were added
    int addCount = 0;  //Number of items added


    public ObjectListTransferHandler() {
        try {
            localObjectListFlavor = new DataFlavor(localObjectType);
        } catch (ClassNotFoundException e) {
            System.out.println(
                    "ReportingListTransferHandler: unable to create data flavor");
        }
        //TODO: serialObjectListFlavor, stringFlavor?
        //serialArrayListFlavor = new DataFlavor(ArrayList.class, "ArrayList");
    }

    @Override
    public boolean importData(JComponent c, Transferable t) {
        //Object[] data = null;

        if (!canImport(c, t.getTransferDataFlavors())) {
            return false;
        }
        try {
            if (hasLocalObjectListFlavor(t.getTransferDataFlavors())) {
                return importIntoList((JList) c, (Object[]) t.getTransferData(localObjectListFlavor));
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

    }

    private boolean importIntoList(JList c, Object[] data) {
        target = c;
        //We'll drop at the current selected index.
        int index = target.getSelectedIndex();

        //Prevent the user from dropping data back on itself.
        //For example, if the user is moving items #4,#5,#6 and #7 and
        //attempts to insert the items after item #5, this would
        //be problematic when removing the original items.
        //This is interpreted as dropping the same data on itself
        //and has no effect.
        if (source.equals(target)) {
            System.out.println((indices[0]) + "<=" + index + "<=" + (indices[indices.length - 1]));
            if (indices != null && index >= indices[0] && index <= indices[indices.length - 1]) {
                indices = null;
                return true;
            }
        }

        DefaultListModel listModel = (DefaultListModel) target.getModel();

        int max = listModel.getSize();
        //System.out.printf("index = %d  max = %d%n", index, max);
        if (index < 0) {
            index = max;
        } else {
            index++;
            if (index > max) {
                index = max;
            }
        }
        addIndex = index;
        addCount = data.length;
        for (Object obj : data) {
            listModel.add(index++, obj);
        }
        return true;
    }

    private boolean hasLocalObjectListFlavor(DataFlavor[] flavors) {
        if (localObjectListFlavor == null) {
            return false;
        }

        for (DataFlavor flavor : flavors) {
            if (flavor.equals(localObjectListFlavor)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canImport(JComponent c, DataFlavor[] flavors) {
        return hasLocalObjectListFlavor(flavors);
    }

    @Override
    public int getSourceActions(JComponent c) {
        return COPY_OR_MOVE;
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        //caity - getSelectedValues is deprecated -> switch to getSelectedValuesList
//       if (c instanceof JList) {
//            source = (JList) c;
//            indices = source.getSelectedIndices();
//            Object[] values = source.getSelectedValues();
//            if (values == null || values.length == 0) {
//                return null;
//            }
//            return new ObjectListTransferable(values);
//        }
//        return null;
        if (c instanceof JList) {
            source = (JList) c;
            indices = source.getSelectedIndices();
            List<Object> l = source.getSelectedValuesList();
            Object[] values = l.toArray();
            if (values == null || values.length == 0) {
                return null;
            }
            return new ObjectListTransferable(values);
        }
        return null;
    }

    @Override
    protected void exportDone(JComponent c, Transferable transferableData, int action) {

        //DefaultListModel model = (DefaultListModel)source.getModel();
        if ((action == TransferHandler.MOVE) && (indices != null)) {
            JList source = (JList) c;
            DefaultListModel model = (DefaultListModel) source.getModel();

            if (source.equals(target)) {
                //If we are moving items around in the same list, we
                //need to adjust the indices accordingly since those
                //after the insertion point have moved.
                if (addCount > 0) {
                    for (int i = 0; i < indices.length; i++) {
                        if (indices[i] > addIndex &&
                                indices[i] + addCount < model.getSize()) {
                            indices[i] += addCount;
                        }
                    }
                }
            }
            for (int i = indices.length - 1; i >= 0; i--) {
                model.remove(indices[i]);
            }

        }
        source = null;
        target = null;
        indices = null;
        addIndex = -1;
        addCount = 0;

    }

    public class ObjectListTransferable implements Transferable {
        Object[] data;

        public ObjectListTransferable(Object[] data) {
            this.data = data;
        }

        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (localObjectListFlavor.equals(flavor)) {
                return data;
            } else {
                throw new UnsupportedFlavorException(flavor);
            }
        }

        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{localObjectListFlavor};
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return localObjectListFlavor.equals(flavor);
        }

    }
}