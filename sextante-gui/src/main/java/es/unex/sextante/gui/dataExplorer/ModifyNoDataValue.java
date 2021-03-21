package es.unex.sextante.gui.dataExplorer;

import javax.swing.JOptionPane;

import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;

public class ModifyNoDataValue
         implements
            Action {

   private final IRasterLayer m_Layer;


   public ModifyNoDataValue(final IRasterLayer layer) {

      m_Layer = layer;

   }


   public boolean execute() {

      final String sValue = (String) JOptionPane.showInputDialog(null, "NoData", "NoData", JOptionPane.PLAIN_MESSAGE, null, null,
               Double.toString(m_Layer.getNoDataValue()));
      if (sValue != null) {
         try {
            final double dValue = Double.parseDouble(sValue);
            m_Layer.setNoDataValue(dValue);
            return true;
         }
         catch (final NumberFormatException e) {}
      }

      return false;

   }


   public String getDescription() {

      return Sextante.getText("Modify");

   }

}
