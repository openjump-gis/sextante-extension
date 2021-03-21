package es.unex.sextante.gui.batch;

import java.awt.Frame;
import java.util.ArrayList;

import javax.swing.JTable;

import es.unex.sextante.core.ObjectAndDescription;
import es.unex.sextante.gui.algorithm.MultipleInputSelectionDialog;

public class RasterBandPanel
         extends
            TextFieldAndButton {

   private static final int             MAX_BANDS = 250;
   private final ObjectAndDescription[] m_Values;
   private final JTable                 m_Table;


   public RasterBandPanel(final JTable table) {

      m_Table = table;
      m_Values = new ObjectAndDescription[MAX_BANDS];

      for (int i = 0; i < m_Values.length; i++) {
         m_Values[i] = new ObjectAndDescription(Integer.toString(i + 1), new Integer(i));
      }

   }


   @Override
   protected void btnActionPerformed() {

      int iCount;
      final StringBuffer sb = new StringBuffer();

      final Frame window = new Frame();

      final ArrayList selectedIndices = getSelectedIndices();
      final MultipleInputSelectionDialog dialog = new MultipleInputSelectionDialog(window, m_Values, selectedIndices);

      dialog.pack();
      dialog.setVisible(true);

      iCount = selectedIndices.size();
      for (int i = 0; i < iCount; i++) {
         final int iValue = ((Integer) selectedIndices.get(i)).intValue() + 1;
         sb.append(Integer.toString(iValue));
         if (i < iCount - 1) {
            sb.append(",");
         }
      }

      textField.setText(sb.toString());
      m_Table.setValueAt(textField.getText(), m_Table.getSelectedRow(), m_Table.getSelectedColumn());

   }


   private ArrayList getSelectedIndices() {

      try {
         final ArrayList list = new ArrayList();
         final String sTokens[] = textField.getText().split(",");
         for (final String element : sTokens) {
            list.add(new Integer(element) - 1);
         }
         return list;
      }
      catch (final Exception e) {
         return new ArrayList();
      }
   }

}
