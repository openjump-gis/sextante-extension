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

import es.unex.sextante.additionalInfo.AdditionalInfoBand;
import es.unex.sextante.core.ObjectAndDescription;
import es.unex.sextante.core.ParametersSet;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.exceptions.NullParameterAdditionalInfoException;
import es.unex.sextante.gui.modeler.ModelerPanel;
import es.unex.sextante.parameters.Parameter;
import es.unex.sextante.parameters.ParameterBand;
import es.unex.sextante.parameters.ParameterRasterLayer;


public class RasterBandPanel
         extends
            ParameterPanel {

   private JLabel    jLabelType;
   private JComboBox jComboBoxParent;


   public RasterBandPanel(final JDialog parent,
                          final ModelerPanel panel) {

      super(parent, panel);

   }


   public RasterBandPanel(final ModelerPanel panel) {

      super(panel);

   }


   @Override
   protected void initGUI() {

      super.initGUI();

      try {

         final TableLayout thisLayout = new TableLayout(new double[][] {
                  { TableLayoutConstants.FILL, 5.0, TableLayoutConstants.FILL },
                  { TableLayoutConstants.FILL, 25.0, TableLayoutConstants.FILL } });
         thisLayout.setHGap(5);
         thisLayout.setVGap(5);
         jPanelMiddle.setLayout(thisLayout);
         jLabelType = new JLabel();
         jPanelMiddle.add(jLabelType, "0, 1");
         jLabelType.setText(Sextante.getText("Parent_layer"));
         jComboBoxParent = new JComboBox();
         jPanelMiddle.add(jComboBoxParent, "2, 1");

      }
      catch (final Exception e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public void updateOptions() {

      super.updateOptions();

      ArrayList layers;
      final ParametersSet ps = m_ModelerPanel.getAlgorithm().getParameters();

      layers = ps.getParametersOfType(ParameterRasterLayer.class);
      final ObjectAndDescription oad[] = new ObjectAndDescription[layers.size()];
      for (int i = 0; i < oad.length; i++) {
         final ParameterRasterLayer param = (ParameterRasterLayer) layers.get(i);
         oad[i] = new ObjectAndDescription(param.getParameterDescription(), param.getParameterName());
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
         final AdditionalInfoBand addInfo = new AdditionalInfoBand(sName);
         m_Parameter = new ParameterBand();
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
         final AdditionalInfoBand ai = (AdditionalInfoBand) param.getParameterAdditionalInfo();
         final ComboBoxModel model = jComboBoxParent.getModel();
         for (int i = 0; i < model.getSize(); i++) {
            final ObjectAndDescription item = (ObjectAndDescription) model.getElementAt(i);
            final String sName = (String) item.getObject();
            if (sName.equals(ai.getParentParameterName())) {
               jComboBoxParent.setSelectedIndex(i);
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

      return Sextante.getText("Band");

   }


   @Override
   public boolean parameterCanBeAdded() {

      if (m_ModelerPanel.getAlgorithm() != null) {
         return m_ModelerPanel.getAlgorithm().requiresIndividualRasterLayers();
      }
      else {
         return false;
      }

   }


}
