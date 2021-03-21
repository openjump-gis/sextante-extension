package es.unex.sextante.gui.batch;

import javax.swing.JFileChooser;
import javax.swing.JTable;

import es.unex.sextante.core.Sextante;
import es.unex.sextante.gui.algorithm.GenericFileFilter;
import es.unex.sextante.gui.core.SextanteGUI;

public class RasterFilePanel
         extends
            TextFieldAndButton {

   private final JTable m_Table;


   public RasterFilePanel(final JTable table) {

      m_Table = table;


   }


   @Override
   protected void btnActionPerformed() {

      final JFileChooser fc = new JFileChooser();

      fc.setFileFilter(new GenericFileFilter(SextanteGUI.getInputFactory().getRasterLayerInputExtensions(),
               Sextante.getText("Capas_raster")));
      fc.setMultiSelectionEnabled(true);
      final int returnVal = fc.showOpenDialog(this.getParent().getParent());

      if (returnVal == JFileChooser.APPROVE_OPTION) {
         textField.setText(fc.getSelectedFile().getAbsolutePath());
         m_Table.setValueAt(textField.getText(), m_Table.getSelectedRow(), m_Table.getSelectedColumn());
      }


   }

}
