

package es.unex.sextante.modeler.elements;


public class ModelElementFixedTable
         implements
            IModelElement {

   private int     m_iRowsCount;
   private int     m_iColsCount;
   private boolean m_bIsNumberOfRowsFixed;


   public void setRowsCount(final int rowsCount) {

      m_iRowsCount = rowsCount;

   }


   public void setColsCount(final int colsCount) {

      m_iColsCount = colsCount;

   }


   public void setIsNumberOfRowsFixed(final boolean isNumberOfRowsFixed) {

      m_bIsNumberOfRowsFixed = isNumberOfRowsFixed;

   }


   public int getColsCount() {

      return m_iColsCount;

   }


   public int getRowsCount() {

      return m_iRowsCount;

   }


}
