package es.unex.sextante.gui.additionalResults;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import es.unex.sextante.dataObjects.IDataObject;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IRecord;
import es.unex.sextante.dataObjects.IRecordsetIterator;
import es.unex.sextante.dataObjects.ITable;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.IteratorException;

public class TableTools {

   public static JScrollPane getScrollableTablePanelFromITable(final IDataObject obj) {

      final JScrollPane jScrollPane = new JScrollPane();

      final JTable jTable = new JTable();
      jScrollPane.setViewportView(jTable);
      final TableModel model = getTableModel(obj);
      if (model != null) {
         jTable.setModel(model);
      }
      jTable.setEnabled(false);

      return jScrollPane;


   }


   private static TableModel getTableModel(final IDataObject obj) {

      final DefaultTableModel model = new DefaultTableModel();

      String[] fields;
      int iCount;


      if (obj instanceof ITable) {
         iCount = (int) ((ITable) obj).getRecordCount();
         fields = ((ITable) obj).getFieldNames();

      }
      else if (obj instanceof IVectorLayer) {
         iCount = ((IVectorLayer) obj).getShapesCount();
         fields = ((IVectorLayer) obj).getFieldNames();
      }
      else {
         return null;
      }

      final String[][] data = new String[iCount][fields.length];

      if (obj instanceof ITable) {
         final IRecordsetIterator iter = ((ITable) obj).iterator();
         int i = 0;
         while (iter.hasNext()) {
            IRecord record;
            try {
               record = iter.next();
               for (int j = 0; j < fields.length; j++) {
                  data[i][j] = record.getValue(j).toString();
               }
               i++;
            }
            catch (final IteratorException e) {
               e.printStackTrace();
            }
         }
      }
      else {
         final IFeatureIterator iter = ((IVectorLayer) obj).iterator();
         int i = 0;
         while (iter.hasNext()) {
            IFeature feature;
            try {
               feature = iter.next();
               for (int j = 0; j < fields.length; j++) {
                  data[i][j] = feature.getRecord().getValue(j).toString();
               }
               i++;
            }
            catch (final IteratorException e) {
               e.printStackTrace();
            }
         }
      }

      model.setDataVector(data, fields);

      return model;

   }


}
