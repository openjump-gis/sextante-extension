package es.unex.sextante.additionalInfo;

/**
 * Additional info for a parameter representing a fixed table
 * 
 * @author Victor Olaya volaya@unex.es
 */
public class AdditionalInfoFixedTable
         implements
            AdditionalInfo {

   private boolean  m_bIsNumberOfRowsFixed = true;
   private int      m_iRows                = 3;
   private String[] m_sCols                = { "", "", "" };


   /**
    * Constructor
    * 
    * @param sCols
    *                the number of columns
    * @param iRows
    *                the number of rows
    * @param bIsNumberOfRowsFixed
    *                true if the number of rows cannot be modified
    */
   public AdditionalInfoFixedTable(final String[] sCols,
                                   final int iRows,
                                   final boolean bIsNumberOfRowsFixed) {

      m_iRows = iRows;
      m_sCols = sCols;
      m_bIsNumberOfRowsFixed = bIsNumberOfRowsFixed;
   }


   /**
    * Returns the number of columns
    * 
    * @return the number of columns
    */
   public int getColsCount() {

      return m_sCols.length;

   }


   /**
    * Returns an array with column names
    * 
    * @return the names of the columns
    */
   public String[] getCols() {

      return m_sCols;

   }


   /**
    * Sets the name of the columns
    * 
    * @param sCols
    *                an array with column names
    */
   public void setCols(final String[] sCols) {

      m_sCols = sCols;

   }


   /**
    * Returns the number of rows
    * 
    * @return the number of rows
    */
   public int getRowsCount() {

      return m_iRows;

   }


   /**
    * Sets the number of rows
    * 
    * @param iRows
    *                the newn umber of rows
    */
   public void setRows(final int iRows) {

      m_iRows = iRows;

   }


   /**
    * Returns whether the number of rows is fixed or not
    * 
    * @return True if the number of rows cannot be modified
    */
   public boolean isNumberOfRowsFixed() {

      return m_bIsNumberOfRowsFixed;

   }


   /**
    * Sets whether the number of rows is fixed or not
    * 
    * @param bIsNumberOfRowsFixed
    *                True if the number of rows cannot be modified
    */
   public void setIsNumberOfRowsFixed(final boolean bIsNumberOfRowsFixed) {

      m_bIsNumberOfRowsFixed = bIsNumberOfRowsFixed;

   }


   public String getTextDescription() {

      final StringBuffer sb = new StringBuffer();
      if (m_bIsNumberOfRowsFixed) {
         sb.append("Number of rows: fixed(" + Integer.toString(m_iRows) + ")\n");
      }
      else {
         sb.append("Number of rows: variable\n");
      }

      sb.append("Cols: |");
      for (final String element : m_sCols) {
         sb.append(element + "|");
      }
      return sb.toString();

   }

}
