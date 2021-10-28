package es.unex.sextante.openjump.gui;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.gui.core.DefaultGUIFactory;

public class OpenJUMPGUIFactory
         extends
            DefaultGUIFactory {

   @Override
   public void showBatchProcessingDialog(final GeoAlgorithm alg,
                                         final JDialog parent) {

      JOptionPane.showMessageDialog(parent, "Batch processing not yet implemented");

   }


}
