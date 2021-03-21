package es.unex.sextante.gui.modeler.parameters;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import es.unex.sextante.additionalInfo.AdditionalInfoString;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.exceptions.NullParameterAdditionalInfoException;
import es.unex.sextante.gui.modeler.ModelerPanel;
import es.unex.sextante.parameters.Parameter;
import es.unex.sextante.parameters.ParameterString;

public class StringPanel
         extends
            ParameterPanel {

   private JTextField jTextFieldDefault;
   private JLabel     jLabelDefault;


   public StringPanel(final JDialog parent,
                      final ModelerPanel panel) {

      super(parent, panel);

   }


   public StringPanel(final ModelerPanel panel) {

      super(panel);

   }


   @Override
   protected void initGUI() {

      super.initGUI();

      try {
         {
            final TableLayout thisLayout = new TableLayout(new double[][] { { TableLayoutConstants.FILL, 5.0, TableLayoutConstants.FILL },
                     { TableLayoutConstants.FILL, 20, TableLayoutConstants.FILL } });
            thisLayout.setHGap(5);
            thisLayout.setVGap(5);
            jPanelMiddle.setLayout(thisLayout);
            {
               jTextFieldDefault = new JTextField();
               jPanelMiddle.add(jTextFieldDefault, "2, 1");
            }
            {
               jLabelDefault = new JLabel();
               jPanelMiddle.add(jLabelDefault, "0, 1");
               jLabelDefault.setText(Sextante.getText("Default_value"));
            }

         }
      }
      catch (final Exception e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public String getParameterDescription() {

      return Sextante.getText("String");

   }


   @Override
   protected boolean prepareParameter() {


      final String sDescription = jTextFieldDescription.getText();
      final String sDefault = jTextFieldDefault.getText();

      if (sDescription.length() != 0) {
         final AdditionalInfoString ai = new AdditionalInfoString();
         ai.setDefaultString(sDefault);
         m_Parameter = new ParameterString();
         m_Parameter.setParameterDescription(sDescription);
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
         final AdditionalInfoString ai = (AdditionalInfoString) param.getParameterAdditionalInfo();
         jTextFieldDefault.setText(ai.getDefaultString());
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
