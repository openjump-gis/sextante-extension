package es.unex.sextante.gui.modeler.parameters;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import es.unex.sextante.additionalInfo.AdditionalInfoMultipleInput;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.exceptions.NullParameterAdditionalInfoException;
import es.unex.sextante.gui.modeler.ModelerPanel;
import es.unex.sextante.parameters.Parameter;
import es.unex.sextante.parameters.ParameterMultipleInput;

public class MultipleInputPanel
         extends
            ParameterPanel {

   private JLabel    jLabelType;
   private JComboBox jComboBoxType;
   private JCheckBox jCheckBoxMandatory;


   public MultipleInputPanel(final JDialog parent,
                             final ModelerPanel panel) {

      super(parent, panel);

   }


   public MultipleInputPanel(final ModelerPanel panel) {

      super(panel);

   }


   @Override
   protected void initGUI() {

      super.initGUI();

      try {
         {
            final TableLayout thisLayout = new TableLayout(new double[][] { { TableLayoutConstants.FILL, 5.0, TableLayoutConstants.FILL },
                     { TableLayoutConstants.FILL, 20.0, TableLayoutConstants.FILL, 20.0, TableLayoutConstants.FILL } });
            thisLayout.setHGap(5);
            thisLayout.setVGap(5);
            jPanelMiddle.setLayout(thisLayout);
            {
               jLabelType = new JLabel();
               jPanelMiddle.add(jLabelType, "0, 1");
               jLabelType.setText(Sextante.getText("Input_type"));
            }
            {
               final ComboBoxModel jComboBoxTypeModel = new DefaultComboBoxModel(new String[] { Sextante.getText("Raster"),
                        Sextante.getText("Vector_any_type"), Sextante.getText("Vectorial__polygons"),
                        Sextante.getText("Vectorial__lines"), Sextante.getText("Vectorial__points"), Sextante.getText("Table"),
                        Sextante.getText("Band") });
               jComboBoxType = new JComboBox();
               jPanelMiddle.add(jComboBoxType, "2, 1");
               jComboBoxType.setModel(jComboBoxTypeModel);
            }
            {
               jCheckBoxMandatory = new JCheckBox();
               jCheckBoxMandatory.setSelected(true);
               jPanelMiddle.add(jCheckBoxMandatory, "0,3,2,3");
               jCheckBoxMandatory.setText(Sextante.getText("Mandatory_input"));
            }
         }
      }
      catch (final Exception e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   protected boolean prepareParameter() {

      int iType = 0;

      switch (jComboBoxType.getSelectedIndex()) {
         case 0:
            iType = AdditionalInfoMultipleInput.DATA_TYPE_RASTER;
            break;
         case 1:
            iType = AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_ANY;
            break;
         case 2:
            iType = AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_POLYGON;
            break;
         case 3:
            iType = AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_LINE;
            break;
         case 4:
            iType = AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_POINT;
            break;
         case 5:
            iType = AdditionalInfoMultipleInput.DATA_TYPE_TABLE;
            break;
         case 6:
            iType = AdditionalInfoMultipleInput.DATA_TYPE_BAND;
            break;
      }

      final String sDescription = jTextFieldDescription.getText();
      final boolean bMandatory = jCheckBoxMandatory.isSelected();

      if (sDescription.length() != 0) {
         final AdditionalInfoMultipleInput addInfo = new AdditionalInfoMultipleInput(iType, bMandatory);
         m_Parameter = new ParameterMultipleInput();
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
         final AdditionalInfoMultipleInput ai = (AdditionalInfoMultipleInput) param.getParameterAdditionalInfo();
         jCheckBoxMandatory.setSelected(ai.getIsMandatory());
         switch (ai.getDataType()) {
            case AdditionalInfoMultipleInput.DATA_TYPE_RASTER:
               jComboBoxType.setSelectedIndex(0);
               break;
            case AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_ANY:
               jComboBoxType.setSelectedIndex(1);
               break;
            case AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_POLYGON:
               jComboBoxType.setSelectedIndex(2);
               break;

            case AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_POINT:
               jComboBoxType.setSelectedIndex(3);
               break;
            case AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_LINE:
               jComboBoxType.setSelectedIndex(4);
               break;
            case AdditionalInfoMultipleInput.DATA_TYPE_TABLE:
               jComboBoxType.setSelectedIndex(5);
               break;
            case AdditionalInfoMultipleInput.DATA_TYPE_BAND:
               jComboBoxType.setSelectedIndex(6);
               break;
         }

      }
      catch (final NullParameterAdditionalInfoException e) {
         e.printStackTrace();
      }

   }


   @Override
   public String getParameterDescription() {

      return Sextante.getText("Multiple_input");

   }


   @Override
   public boolean parameterCanBeAdded() {

      return true;

   }

}
