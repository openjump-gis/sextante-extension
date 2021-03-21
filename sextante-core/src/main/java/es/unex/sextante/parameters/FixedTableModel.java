

package es.unex.sextante.parameters;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;


/**
 * A table model to be used for a fixed table parameter
 * 
 * @see ParameterFixedTable
 * @author volaya
 * 
 */
public class FixedTableModel
         extends
            AbstractTableModel {

   String[]    m_sColumnNames;
   ArrayList[] m_Data;
   boolean     m_bIsNumberOfRowsFixed;


   /**
    * Creates a new table
    * 
    * @param sColumnNames
    *                names of columns(fields) in table
    * @param iRows
    *                number of rows in table
    * @param bIsNumberOfRowsFixed
    *                true if new rows can be added
    */
   public FixedTableModel(final String[] sColumnNames,
                          final int iRows,
                          final boolean bIsNumberOfRowsFixed) {

      int i, j;

      m_sColumnNames = sColumnNames;
      m_bIsNumberOfRowsFixed = bIsNumberOfRowsFixed;
      m_Data = new ArrayList[sColumnNames.length];

      for (i = 0; i < m_sColumnNames.length; i++) {
         m_Data[i] = new ArrayList();
         for (j = 0; j < iRows; j++) {
            m_Data[i].add("0");
         }
      }

   }


   /**
    * use this method to clone the table.
    * 
    * @param fixedTableModel
    * @return a new instance of fixedTableModel
    */
   public static FixedTableModel newInstance(final FixedTableModel fixedTableModel) {

      final FixedTableModel newFixedTableModel = new FixedTableModel(fixedTableModel.getColumnNames(),
               fixedTableModel.getRowCount(), fixedTableModel.isNumberOfRowsFixed());
      newFixedTableModel.setData(fixedTableModel.getData());

      return newFixedTableModel;

   }


   /**
    * Sets the attributes of the table
    * 
    * @param sColumnNames
    *                names of columns(fields)
    * @param data
    *                table data
    * @param bIsNumberOfRowsFixed
    *                true if new rows can be added
    * @return true if data is consistent
    */
   public boolean setAttributes(final String[] sColumnNames,
                                final ArrayList[] data,
                                final boolean bIsNumberOfRowsFixed) {

      if (data.length == sColumnNames.length) {
         this.setData(data);
         m_sColumnNames = sColumnNames;
         m_bIsNumberOfRowsFixed = bIsNumberOfRowsFixed;
         return true;
      }
      else {
         return false;
      }

   }


   /**
    * Returns the number of columns
    * 
    * @return Number of columns(fields)
    */
   public int getColumnCount() {

      return m_sColumnNames.length;

   }


   /**
    * Returns the number of rows
    * 
    * @return Number of rows
    */
   public int getRowCount() {

      return m_Data[0].size();

   }


   /**
    * Returns the name of a column
    * 
    * @param iCol
    *                the index of the column
    * @return Name of column iCol
    */
   @Override
   public String getColumnName(final int iCol) {

      return m_sColumnNames[iCol];

   }


   /**
    * Returns an array of strings with column names
    * 
    * @return An array of strings with column names
    */
   public String[] getColumnNames() {

      return m_sColumnNames;

   }


   /**
    * Returns the value at a cell
    * 
    * @param iRow
    *                the row
    * @param iCol
    *                the column
    * @return the value at cell [iRow, iCol]
    */
   public Object getValueAt(final int iRow,
                            final int iCol) {

      return m_Data[iCol].get(iRow);

   }


   /**
    * Returns the class of a field
    * 
    * @param iField
    *                the index of the field
    * @return the class of the given field
    */
   @Override
   public Class getColumnClass(final int iField) {

      return getValueAt(0, iField).getClass();

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

      try {
         //final Double d = new Double(Double.parseDouble(value.toString()));
         if (iRow < getRowCount()) {
            m_Data[iCol].set(iRow, value.toString());
            fireTableCellUpdated(iRow, iCol);
         }
      }
      catch (final Exception e) {
      }

   }


   /**
    * Adds a new row to the table, only if possible
    * 
    */
   public void addRow() {

      int i;

      if (!m_bIsNumberOfRowsFixed) {
         for (i = 0; i < m_sColumnNames.length; i++) {
            m_Data[i].add(new Double(0));
         }
      }

      this.fireTableRowsInserted(m_Data[0].size(), m_Data[0].size());

   }


   /**
    * removes row iRow, if number of rows is not fixed
    * 
    * @param iRow
    *                the index of the row to remove
    */
   public void removeRow(final int iRow) {

      int i;
      if (!m_bIsNumberOfRowsFixed) {
         for (i = 0; i < m_sColumnNames.length; i++) {
            m_Data[i].remove(iRow);
         }
      }

      this.fireTableRowsDeleted(iRow, iRow);
   }


   public ArrayList[] getData() {

      return m_Data;

   }


   public boolean setData(final ArrayList[] data) {

      int i, j;

      if (data.length == m_sColumnNames.length) {
         for (i = 0; i < m_Data.length; i++) {
            m_Data[i].clear();
            for (j = 0; j < data[i].size(); j++) {
               //m_Data[i].add(new Double(((Double) data[i].get(j)).doubleValue()));
               m_Data[i].add(data[i].get(j));
            }
         }
         return true;
      }
      else {
         return false;
      }

   }


   /**
    * Returns true if the number of rows in the table is fixed
    * 
    * @return true if the number of rows in the table is fixed
    */
   public boolean isNumberOfRowsFixed() {

      return m_bIsNumberOfRowsFixed;

   }


   /**
    * Sets whether the number of rows can be modified or not
    * 
    * @param bIsNumberOfRowsFixed
    *                indicates whether the number of rows can be modified or not
    */
   public void setIsNumberOfRowsFixed(final boolean bIsNumberOfRowsFixed) {

      m_bIsNumberOfRowsFixed = bIsNumberOfRowsFixed;

   }


   /**
    * Returns the dimensions of the table as a string
    * 
    * @return a String in the form "x by y" with the dimensions of the table
    */
   public String getDimensionsAsString() {

      final StringBuffer s = new StringBuffer();

      s.append(Integer.toString(getRowCount()));
      s.append(" X ");
      s.append(Integer.toString(getColumnCount()));

      return s.toString();

   }


   @Override
   public String toString() {

      int i, j;
      final StringBuffer sb = new StringBuffer("[");

      for (i = 0; i < getRowCount(); i++) {
         sb.append("[");
         for (j = 0; j < getColumnCount(); j++) {
            sb.append(getValueAt(i, j));
            if (j < getColumnCount() - 1) {
               sb.append("|");
            }
         }
         if (i < getRowCount() - 1) {
            sb.append("],");
         }
         else {
            sb.append("]");
         }
      }
      sb.append("]");

      return sb.toString();

   }


   /**
    * Returns a Comma-Separated Values version of the table
    * 
    * @return a Comma-Separated Values version of the table
    */
   public String getAsCSV() {

      int i, j;
      final StringBuffer sb = new StringBuffer("");

      for (i = 0; i < getRowCount(); i++) {
         for (j = 0; j < getColumnCount(); j++) {
            sb.append(getValueAt(i, j));
            if ((i != getRowCount() - 1) || (j != getColumnCount() - 1)) {
               sb.append(",");
            }

         }
      }

      return sb.toString();

   }

}
