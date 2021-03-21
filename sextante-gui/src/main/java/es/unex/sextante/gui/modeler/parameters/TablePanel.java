package es.unex.sextante.gui.modeler.parameters;

import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

import es.unex.sextante.additionalInfo.AdditionalInfoTable;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.exceptions.NullParameterAdditionalInfoException;
import es.unex.sextante.gui.modeler.ModelerPanel;
import es.unex.sextante.parameters.Parameter;
import es.unex.sextante.parameters.ParameterTable;

public class TablePanel
         extends
            ParameterPanel {

   private JCheckBox jCheckBoxMandatory;


   public TablePanel(final JDialog parent,
                     final ModelerPanel panel) {

      super(parent, panel);

   }


   public TablePanel(final ModelerPanel panel) {

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
         final AdditionalInfoTable addInfo = new AdditionalInfoTable(jCheckBoxMandatory.isSelected());
         m_Parameter = new ParameterTable();
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
         final AdditionalInfoTable ai = (AdditionalInfoTable) param.getParameterAdditionalInfo();
         jCheckBoxMandatory.setSelected(ai.getIsMandatory());
      }
      catch (final NullParameterAdditionalInfoException e) {
         e.printStackTrace();
      }

   }


   @Override
   public String getParameterDescription() {

      return Sextante.getText("Table");

   }


   @Override
   public boolean parameterCanBeAdded() {

      return true;

   }

}
