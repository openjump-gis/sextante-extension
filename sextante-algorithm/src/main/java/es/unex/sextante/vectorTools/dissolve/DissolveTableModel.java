package es.unex.sextante.vectorTools.dissolve;

import javax.swing.table.AbstractTableModel;

import es.unex.sextante.dataObjects.IVectorLayer;

public class DissolveTableModel
         extends
            AbstractTableModel {

   private final IVectorLayer m_Layer;
   private final Object[][]   m_Data;


   public DissolveTableModel(final IVectorLayer layer) {

      m_Layer = layer;
      m_Data = new Object[layer.getFieldCount()][5];
      for (int i = 0; i < layer.getFieldCount(); i++) {
         m_Data[i][0] = layer.getFieldName(i);
         for (int j = 0; j < 4; j++) {
            m_Data[i][j + 1] = new Boolean(false);
         }
      }

   }


   public int getColumnCount() {

      return 5;

   }


   @Override
   public String getColumnName(final int col) {

      if (col == 0) {
         return "FIELD";
      }
      else {
         return DissolveAlgorithm.FUNCTIONS[col - 1];
      }


   }


   public int getRowCount() {

      return m_Layer.getFieldCount();

   }


   public Object getValueAt(final int row,
                            final int col) {

      return m_Data[row][col];

   }


   @Override
   public boolean isCellEditable(final int row,
                                 final int col) {

      if (col == 0) {
         return false;
      }
      else {
         return true;
      }

   }


   @Override
   public void setValueAt(final Object value,
                          final int row,
                          final int col) {

      m_Data[row][col] = value;
      fireTableCellUpdated(row, col);

   }


   @Override
   public Class getColumnClass(final int columnIndex) {

      if (columnIndex != 0) {
         return Boolean.class;
      }
      else {
         return String.class;
      }

   }


   public String getAsString() {

      final StringBuffer sb = new StringBuffer();
      boolean bFirst = true;
      for (int i = 0; i < m_Data.length; i++) {
         for (int j = 1; j < 5; j++) {
            final Boolean b = (Boolean) m_Data[i][j];
            if (b) {
               if (!bFirst) {
                  sb.append(",");
               }
               else {
                  bFirst = false;
               }
               sb.append(Integer.toString(i));
               sb.append(",");
               sb.append(Integer.toString(j - 1));
            }
         }

      }

      return sb.toString();

   }


}
