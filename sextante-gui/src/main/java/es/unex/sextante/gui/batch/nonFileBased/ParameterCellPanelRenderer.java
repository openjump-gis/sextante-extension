package es.unex.sextante.gui.batch.nonFileBased;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import es.unex.sextante.additionalInfo.AdditionalInfoFixedTable;
import es.unex.sextante.additionalInfo.AdditionalInfoMultipleInput;
import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.additionalInfo.AdditionalInfoSelection;
import es.unex.sextante.exceptions.NullParameterAdditionalInfoException;
import es.unex.sextante.outputs.Output;
import es.unex.sextante.parameters.ParameterDataObject;
import es.unex.sextante.parameters.ParameterFixedTable;
import es.unex.sextante.parameters.ParameterMultipleInput;
import es.unex.sextante.parameters.ParameterNumericalValue;
import es.unex.sextante.parameters.ParameterPoint;
import es.unex.sextante.parameters.ParameterSelection;

public class ParameterCellPanelRenderer
         extends
            DefaultTableCellRenderer {

   private final Object m_Object;
   private Object       m_Value;


   public ParameterCellPanelRenderer(final Object obj) {

      m_Object = obj;

   }


   @Override
   public Component getTableCellRendererComponent(final JTable table,
                                                  final Object value,
                                                  final boolean isSelected,
                                                  final boolean hasFocus,
                                                  final int rowIndex,
                                                  final int colIndex) {

      super.getTableCellRendererComponent(table, value, isSelected, hasFocus, rowIndex, colIndex);

      m_Value = table.getModel().getValueAt(rowIndex, colIndex);
      this.setText(m_Value.toString());
      if (isValueOK()) {
         this.setForeground(Color.BLACK);
      }
      else {
         this.setForeground(Color.RED);
      }
      return this;
   }


   public boolean isValueOK() {

      if (m_Object instanceof ParameterPoint) {
         return isPointOK();
      }
      else if (m_Object instanceof ParameterDataObject) {
         return true;
      }
      else if (m_Object instanceof ParameterFixedTable) {
         return isFixedTableOK();
      }
      else if (m_Object instanceof ParameterSelection) {
         return isSelectionOK();
      }
      else if (m_Object instanceof ParameterNumericalValue) {
         return isNumericalValueOK();
      }
      else if (m_Object instanceof ParameterMultipleInput) {
         return isMultipleInputOK();
      }
      else if (m_Object instanceof Output) {
         return isOutputOK();
      }

      return true;

   }


   private boolean isOutputOK() {

      return !((String) m_Value).trim().equals("");

   }


   private boolean isMultipleInputOK() {

      AdditionalInfoMultipleInput ai;
      try {
         ai = (AdditionalInfoMultipleInput) ((ParameterMultipleInput) m_Object).getParameterAdditionalInfo();
      }
      catch (final NullParameterAdditionalInfoException e) {
         return false;
      }
      if (m_Value instanceof ArrayList) {
         final ArrayList list = (ArrayList) m_Value;
         final int iCount = list.size();
         return (!ai.getIsMandatory() || (iCount > 0));
      }
      else {
         return !ai.getIsMandatory();
      }

   }


   private boolean isFixedTableOK() {

      boolean bIsNumberOfRowsFixed;
      int iCols, iRows;;
      final StringTokenizer st = new StringTokenizer((String) m_Value, ",");
      AdditionalInfoFixedTable ai;
      final ParameterFixedTable parameter = (ParameterFixedTable) m_Object;
      try {
         ai = (AdditionalInfoFixedTable) parameter.getParameterAdditionalInfo();
         iCols = ai.getColsCount();
         iRows = ai.getRowsCount();
         bIsNumberOfRowsFixed = ai.isNumberOfRowsFixed();

         if (bIsNumberOfRowsFixed) {
            if (st.countTokens() != iCols * iRows) {
               return false;
            }
         }
         else {
            if (st.countTokens() % iCols != 0) {
               return false;
            }
         }

         while (st.hasMoreTokens()) {
            Double.parseDouble(st.nextToken().trim());

         }

         return true;
      }
      catch (final Exception e) {
         return false;
      }

   }


   private boolean isSelectionOK() {

      try {
         final ParameterSelection parameter = (ParameterSelection) m_Object;
         final AdditionalInfoSelection ai = (AdditionalInfoSelection) parameter.getParameterAdditionalInfo();
         final String[] sValues = ai.getValues();
         for (final String element : sValues) {
            if (((String) m_Value).equals(element)) {
               return true;
            }
         }
      }
      catch (final Exception e) {
         return false;
      }

      return false;

   }


   private boolean isNumericalValueOK() {

      try {
         final ParameterNumericalValue parameter = (ParameterNumericalValue) m_Object;
         final AdditionalInfoNumericalValue ai = (AdditionalInfoNumericalValue) parameter.getParameterAdditionalInfo();
         final double dMin = ai.getMinValue();
         final double dMax = ai.getMaxValue();
         final double dVal = Double.parseDouble((String) m_Value);
         if ((dVal < dMin) || (dMax < dVal)) {
            return false;
         }
         return true;
      }
      catch (final Exception e) {
         return false;
      }

   }


   private boolean isPointOK() {

      final String[] s = ((String) m_Value).split(",");

      if (s.length != 2) {
         return false;
      }

      try {
         Double.parseDouble(s[0]);
         Double.parseDouble(s[1]);
      }
      catch (final Exception e) {
         return false;
      }

      return true;

   }


   // The following methods override the defaults for performance reasons
   @Override
   public void validate() {}


   @Override
   public void revalidate() {}


   @Override
   protected void firePropertyChange(final String propertyName,
                                     final Object oldValue,
                                     final Object newValue) {}


   @Override
   public void firePropertyChange(final String propertyName,
                                  final boolean oldValue,
                                  final boolean newValue) {}

}
