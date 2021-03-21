package es.unex.sextante.gui.modeler.parameters;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import es.unex.sextante.core.Sextante;
import es.unex.sextante.gui.modeler.ModelerPanel;
import es.unex.sextante.parameters.ParameterPoint;

public class PointPanel
         extends
            ParameterPanel {

   public PointPanel(final JDialog parent,
                     final ModelerPanel panel) {

      super(parent, panel);

   }


   public PointPanel(final ModelerPanel panel) {

      super(panel);

   }


   @Override
   public String getParameterDescription() {

      return Sextante.getText("Coordinate");

   }


   @Override
   protected boolean prepareParameter() {


      final String sDescription = jTextFieldDescription.getText();

      if (sDescription.length() != 0) {
         m_Parameter = new ParameterPoint();
         m_Parameter.setParameterDescription(sDescription);
         return true;
      }
      else {
         JOptionPane.showMessageDialog(null, Sextante.getText("Invalid_description"), Sextante.getText("Warning"),
                  JOptionPane.WARNING_MESSAGE);
         return false;
      }


   }


   @Override
   public boolean parameterCanBeAdded() {

      return true;

   }

}
