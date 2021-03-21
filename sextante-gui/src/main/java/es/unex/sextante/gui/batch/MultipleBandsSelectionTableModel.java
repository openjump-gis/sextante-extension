package es.unex.sextante.gui.batch;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import es.unex.sextante.core.Sextante;

/**
 * A table model to be used at a {@link MultipleBandSelectionDialog}
 * 
 * @author volaya
 * 
 */
public class MultipleBandsSelectionTableModel
         extends
            AbstractTableModel {

   private final String[]    m_sColumnNames;
   private final ArrayList[] m_Data;

   public static final int   INIT_ROWS = 1;


   public MultipleBandsSelectionTableModel() {

      super();

      int i, j;

      m_sColumnNames = new String[] { Sextante.getText("Layer"), Sextante.getText("Band") };
      m_Data = new ArrayList[2];


      for (i = 0; i < m_sColumnNames.length; i++) {
         m_Data[i] = new ArrayList();
         for (j = 0; j < INIT_ROWS; j++) {
            m_Data[i].add("");
         }
      }

   }


   public int getColumnCount() {

      return m_sColumnNames.length;

   }


   public int getRowCount() {

      return m_Data[0].size();

   }


   @Override
   public String getColumnName(final int iCol) {

      return m_sColumnNames[iCol];

   }


   /**
    * Returns the names of the columns
    * 
    * @return the names of the columns
    */
   public String[] getColumnNames() {

      return m_sColumnNames;

   }


   public Object getValueAt(final int iRow,
                            final int iCol) {

      return m_Data[iCol].get(iRow);

   }


   @Override
   public Class getColumnClass(final int c) {

      return getValueAt(0, c).getClass();

   }


   @Override
   public boolean isCellEditable(final int iRow,
                                 final int iCol) {

      return true;

   }


   @Override
   public void setValueAt(final Object value,
                          final int iRow,
                          final int iCol) {

      m_Data[iCol].set(iRow, value);
      fireTableCellUpdated(iRow, iCol);

   }


   /**
    * Adds a new row to the table
    * 
    */
   public void addRow() {

      int i;

      for (i = 0; i < m_sColumnNames.length; i++) {
         m_Data[i].add("");
      }

      this.fireTableRowsInserted(m_Data[0].size(), m_Data[0].size());

   }


   /**
    * Removes a row from the table
    * 
    * @param iRow
    *                the zero-based index of the row to remove
    */
   public void removeRow(final int iRow) {

      int i;
      for (i = 0; i < m_sColumnNames.length; i++) {
         m_Data[i].remove(iRow);
      }

      this.fireTableRowsDeleted(iRow, iRow);

   }


   /**
    * Returns the selected bands as an array with two ArrayList, one of them containing the layers and another one containing the
    * bands
    * 
    * @return the selected bands
    */
   public ArrayList[] getData() {

      return m_Data;

   }


}
