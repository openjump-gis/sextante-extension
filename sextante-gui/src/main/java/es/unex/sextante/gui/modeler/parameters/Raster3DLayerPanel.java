package es.unex.sextante.gui.modeler.parameters;

import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

import es.unex.sextante.additionalInfo.AdditionalInfo3DRasterLayer;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.exceptions.NullParameterAdditionalInfoException;
import es.unex.sextante.gui.modeler.ModelerPanel;
import es.unex.sextante.parameters.Parameter;
import es.unex.sextante.parameters.Parameter3DRasterLayer;

public class Raster3DLayerPanel
         extends
            ParameterPanel {

   private JCheckBox jCheckBoxMandatory;


   public Raster3DLayerPanel(final JDialog parent,
                             final ModelerPanel panel) {

      super(parent, panel);

   }


   public Raster3DLayerPanel(final ModelerPanel panel) {

      super(panel);

   }


   @Override
   protected void initGUI() {

      super.initGUI();

      try {
         jCheckBoxMandatory = new JCheckBox();
         jCheckBoxMandatory.setSelected(true);
         jPanelMiddle.add(jCheckBoxMandatory);
         jCheckBoxMandatory.setText(Sextante.getText("Mandatory"));
      }
      catch (final Exception e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   protected boolean prepareParameter() {


      final String sDescription = jTextFieldDescription.getText();

      if (sDescription.length() != 0) {
         final AdditionalInfo3DRasterLayer addInfo = new AdditionalInfo3DRasterLayer(jCheckBoxMandatory.isSelected());
         m_Parameter = new Parameter3DRasterLayer();
         m_Parameter.setParameterAdditionalInfo(addInfo);
         m_Parameter.setParameterDescription(jTextFieldDescription.getText());
         return true;
      }
      else {
         JOptionPane.showMessageDialog(null, Sextante.getText("Invalid_description"), Sextante.getText("Warning"),
                  JOptionPane.WARNING_MESSAGE);
         return false;
      }


   }


   @Override
   public void setParameter(final Parameter param) {

      super.setParameter(param);

      try {
         final AdditionalInfo3DRasterLayer ai = (AdditionalInfo3DRasterLayer) param.getParameterAdditionalInfo();
         jCheckBoxMandatory.setSelected(ai.getIsMandatory());
      }
      catch (final NullParameterAdditionalInfoException e) {
         e.printStackTrace();
      }

   }


   @Override
   public String getParameterDescription() {

      return Sextante.getText("3D_Raster_layer");

   }


   @Override
   public boolean parameterCanBeAdded() {

      return true;

   }

}
