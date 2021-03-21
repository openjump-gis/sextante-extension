

package es.unex.sextante.gui.settings;

import info.clearthought.layout.TableLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import es.unex.sextante.core.Sextante;
import es.unex.sextante.gui.core.SextanteGUI;
import es.unex.sextante.gui.exceptions.WrongSettingValuesException;
import es.unex.sextante.gui.toolbox.AlgorithmGroupConfiguration;
import es.unex.sextante.gui.toolbox.AlgorithmGroupsOrganizer;


public class SextanteGeneralSettingsPanel
         extends
            SettingPanel {


   private JCheckBox  jCheckBoxChangeNames;
   private JLabel     jLabelNoDataValue;
   private JTextField jTextFieldNoData;
   private JCheckBox  jCheckBoxUseInternalNames;
   private JButton    jButtonConfigureGroups;
   private JCheckBox  jCheckBoxShowMostRecent;


   @Override
   protected void initGUI() {

      final TableLayout thisLayout = new TableLayout(new double[][] {
               { 5.0, TableLayout.FILL, TableLayout.FILL, 5.0 },
               { 7.0, TableLayout.MINIMUM, 5.0, TableLayout.MINIMUM, 6.0, TableLayout.MINIMUM, 50, TableLayout.MINIMUM, 50,
                        TableLayout.MINIMUM, TableLayout.FILL } });
      thisLayout.setHGap(5);
      thisLayout.setVGap(5);
      this.setLayout(thisLayout);
      {
         final boolean bUseInternalNames = new Boolean(
                  SextanteGUI.getSettingParameterValue(SextanteGeneralSettings.USE_INTERNAL_NAMES)).booleanValue();
         final boolean bModiFyResultsNames = new Boolean(
                  SextanteGUI.getSettingParameterValue(SextanteGeneralSettings.MODIFY_NAMES)).booleanValue();
         final boolean bShowMostRecent = new Boolean(
                  SextanteGUI.getSettingParameterValue(SextanteGeneralSettings.SHOW_MOST_RECENT)).booleanValue();
         jCheckBoxChangeNames = new JCheckBox();
         jCheckBoxChangeNames.setText(Sextante.getText("Modify_output_names"));
         jCheckBoxChangeNames.setSelected(bModiFyResultsNames);
         this.add(jCheckBoxChangeNames, "1, 1, 2, 1");
         jCheckBoxUseInternalNames = new JCheckBox();
         jCheckBoxUseInternalNames.setText(Sextante.getText("Use_internal_names_for_outputs"));
         jCheckBoxUseInternalNames.setSelected(bUseInternalNames);
         this.add(jCheckBoxUseInternalNames, "1, 3, 2, 3");
         jLabelNoDataValue = new JLabel();
         jLabelNoDataValue.setText(Sextante.getText("Default_no_data_value"));
         this.add(jLabelNoDataValue, "1, 5");
         jTextFieldNoData = new JTextField();
         final String sNoDataValue = Double.toString(SextanteGUI.getOutputFactory().getDefaultNoDataValue());
         jTextFieldNoData.setText(sNoDataValue);
         this.add(jTextFieldNoData, "2, 5");
         jButtonConfigureGroups = new JButton(Sextante.getText("ConfigureAlgGroups"));
         jButtonConfigureGroups.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent arg0) {
               configureGroups();
            }
         });
         this.add(jButtonConfigureGroups, "1, 9, 2, 9");
         jCheckBoxShowMostRecent = new JCheckBox(Sextante.getText("ShowMostRecent"));
         jCheckBoxShowMostRecent.setSelected(bShowMostRecent);
         this.add(jCheckBoxShowMostRecent, "1, 7, 2, 7");
      }

   }


   protected void configureGroups() {

      final AlgorithmGroupsConfigurationDialog dialog = new AlgorithmGroupsConfigurationDialog();
      dialog.setVisible(true);

      final HashMap<String, AlgorithmGroupConfiguration> map = dialog.getGrouppingsMap();
      if (map != null) {
         AlgorithmGroupsOrganizer.setConfiguration(map);
         AlgorithmGroupsOrganizer.saveSettings();
      }

   }


   @Override
   public HashMap<String, String> getValues() throws WrongSettingValuesException {

      final HashMap<String, String> map = new HashMap<String, String>();
      map.put(SextanteGeneralSettings.MODIFY_NAMES, new Boolean(jCheckBoxChangeNames.isSelected()).toString());
      map.put(SextanteGeneralSettings.SHOW_MOST_RECENT, new Boolean(jCheckBoxShowMostRecent.isSelected()).toString());
      map.put(SextanteGeneralSettings.USE_INTERNAL_NAMES, new Boolean(jCheckBoxUseInternalNames.isSelected()).toString());
      try {
         final double dValue = Double.parseDouble(jTextFieldNoData.getText());
         SextanteGUI.getOutputFactory().setDefaultNoDataValue(dValue);
      }
      catch (final Exception e) {
         throw new WrongSettingValuesException();
      }
      map.put(SextanteGeneralSettings.DEFAULT_NO_DATA_VALUE, jTextFieldNoData.getText());

      return map;

   }

}
