package es.unex.sextante.gui.modeler.parameters;

import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

import es.unex.sextante.additionalInfo.AdditionalInfoBoolean;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.exceptions.NullParameterAdditionalInfoException;
import es.unex.sextante.gui.modeler.ModelerPanel;
import es.unex.sextante.parameters.Parameter;
import es.unex.sextante.parameters.ParameterBoolean;

public class BooleanPanel
         extends
            ParameterPanel {

   private JCheckBox jCheckBoxDefault;


   public BooleanPanel(final ModelerPanel panel) {

      super(panel);

   }


   public BooleanPanel(final JDialog parent,
                       final ModelerPanel panel) {

      super(parent, panel);

   }


   @Override
   protected void initGUI() {

      super.initGUI();

      try {
         jCheckBoxDefault = new JCheckBox();
         jCheckBoxDefault.setSelected(true);
         jPanelMiddle.add(jCheckBoxDefault);
         jCheckBoxDefault.setText(Sextante.getText("Default_value"));
      }
      catch (final Exception e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public String getParameterDescription() {

      return Sextante.getText("Boolean_value");

   }


   @Override
   protected boolean prepareParameter() {


      final String sDescription = jTextFieldDescription.getText();

      if (sDescription.length() != 0) {
         m_Parameter = new ParameterBoolean();
         m_Parameter.setParameterDescription(sDescription);
         final AdditionalInfoBoolean ai = new AdditionalInfoBoolean(jCheckBoxDefault.isSelected());
         m_Parameter.setParameterAdditionalInfo(ai);
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
         final AdditionalInfoBoolean ai = (AdditionalInfoBoolean) param.getParameterAdditionalInfo();
         jCheckBoxDefault.setSelected(ai.getDefaultValue());
      }
      catch (final NullParameterAdditionalInfoException e) {
         e.printStackTrace();
      }

   }


   @Override
   public boolean parameterCanBeAdded() {

      return true;

   }

}
