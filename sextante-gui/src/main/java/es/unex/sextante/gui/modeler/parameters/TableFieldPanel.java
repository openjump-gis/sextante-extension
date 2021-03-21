package es.unex.sextante.gui.modeler.parameters;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.util.ArrayList;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import es.unex.sextante.additionalInfo.AdditionalInfoTableField;
import es.unex.sextante.core.ObjectAndDescription;
import es.unex.sextante.core.ParametersSet;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.exceptions.NullParameterAdditionalInfoException;
import es.unex.sextante.gui.modeler.ModelAlgorithm;
import es.unex.sextante.gui.modeler.ModelerPanel;
import es.unex.sextante.parameters.Parameter;
import es.unex.sextante.parameters.ParameterTable;
import es.unex.sextante.parameters.ParameterTableField;
import es.unex.sextante.parameters.ParameterVectorLayer;

public class TableFieldPanel
         extends
            ParameterPanel {

   private JLabel    jLabelType;
   private JComboBox jComboBoxParent;


   //private JCheckBox jCheckBoxMandatory;


   public TableFieldPanel(final JDialog parent,
                          final ModelerPanel panel) {

      super(parent, panel);

   }


   public TableFieldPanel(final ModelerPanel panel) {

      super(panel);

   }


   @Override
   protected void initGUI() {

      super.initGUI();

      try {
         final TableLayout thisLayout = new TableLayout(new double[][] {
                  { TableLayoutConstants.FILL, 5.0, TableLayoutConstants.FILL },
                  { TableLayoutConstants.FILL, 25.0, TableLayoutConstants.FILL, 25.0, TableLayoutConstants.FILL } });
         thisLayout.setHGap(5);
         thisLayout.setVGap(5);
         jPanelMiddle.setLayout(thisLayout);
         jLabelType = new JLabel();
         jPanelMiddle.add(jLabelType, "0, 1");
         jLabelType.setText(Sextante.getText("Parent_layer_or_table"));
         jComboBoxParent = new JComboBox();
         jPanelMiddle.add(jComboBoxParent, "2, 1");
         /*jCheckBoxMandatory = new JCheckBox(Sextante.getText("Mandatory"));
         jPanelMiddle.add(jCheckBoxMandatory, "0, 3, 2, 3");*/
      }
      catch (final Exception e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public void updateOptions() {

      super.updateOptions();

      int i;

      ArrayList layers, tables;
      final ParametersSet ps = m_ModelerPanel.getAlgorithm().getParameters();

      layers = ps.getParametersOfType(ParameterVectorLayer.class);
      tables = ps.getParametersOfType(ParameterTable.class);
      final ObjectAndDescription oad[] = new ObjectAndDescription[layers.size() + tables.size()];
      for (i = 0; i < layers.size(); i++) {
         final ParameterVectorLayer param = (ParameterVectorLayer) layers.get(i);
         oad[i] = new ObjectAndDescription(param.getParameterDescription() + Sextante.getText("[layer]"),
                  param.getParameterName());
      }
      for (i = 0; i < tables.size(); i++) {
         final ParameterTable param = (ParameterTable) tables.get(i);
         oad[i + layers.size()] = new ObjectAndDescription(param.getParameterDescription() + Sextante.getText("[table]"),
                  param.getParameterName());
      }
      final ComboBoxModel jComboBoxTypeModel = new DefaultComboBoxModel(oad);
      jComboBoxParent.setModel(jComboBoxTypeModel);

   }


   @Override
   protected boolean prepareParameter() {

      String sName;
      final String sDescription = jTextFieldDescription.getText();

      if (sDescription.length() != 0) {
         sName = (String) ((ObjectAndDescription) jComboBoxParent.getSelectedItem()).getObject();
         final AdditionalInfoTableField addInfo = new AdditionalInfoTableField(sName, /*jCheckBoxMandatory.isSelected()*/true);
         m_Parameter = new ParameterTableField();
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
         final AdditionalInfoTableField ai = (AdditionalInfoTableField) param.getParameterAdditionalInfo();
         final ComboBoxModel model = jComboBoxParent.getModel();
         for (int i = 0; i < model.getSize(); i++) {
            final ObjectAndDescription item = (ObjectAndDescription) model.getElementAt(i);
            final String sName = (String) item.getObject();
            if (sName.equals(ai.getParentParameterName())) {
               jComboBoxParent.setSelectedIndex(i);
               //jCheckBoxMandatory.setSelected(ai.getIsMandatory());
               break;
            }
         }

      }
      catch (final NullParameterAdditionalInfoException e) {
         e.printStackTrace();
      }

   }


   @Override
   public String getParameterDescription() {

      return Sextante.getText("Field");

   }


   @Override
   public boolean parameterCanBeAdded() {

      final ModelAlgorithm alg = m_ModelerPanel.getAlgorithm();

      if (alg != null) {
         return alg.requiresIndividualVectorLayers() || alg.requiresTables();
      }
      else {
         return false;
      }


   }

}
