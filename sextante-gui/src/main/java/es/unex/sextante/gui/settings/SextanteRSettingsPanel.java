package es.unex.sextante.gui.settings;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import es.unex.sextante.core.Sextante;
import es.unex.sextante.gui.algorithm.FileSelectionPanel;
import es.unex.sextante.gui.core.SextanteGUI;
import es.unex.sextante.gui.r.RAlgorithmProvider;


public class SextanteRSettingsPanel
         extends
            SettingPanel {

   private FileSelectionPanel jRFolder;
   private JButton            jButton;
   private JLabel             jLabelFolder;
   private JCheckBox          jActivateCheckBox;
   private JLabel             jLabelScriptsFolder;
   private FileSelectionPanel jScriptsRFolder;
   private FileSelectionPanel jRScriptsFolder;


   @Override
   protected void initGUI() {

      final TableLayout thisLayout = new TableLayout(new double[][] {
               { 3.0, TableLayoutConstants.FILL, TableLayoutConstants.FILL, 3.0 },
               { 3.0, TableLayoutConstants.MINIMUM, TableLayoutConstants.MINIMUM, 30, TableLayoutConstants.MINIMUM,
                        TableLayoutConstants.FILL, TableLayoutConstants.MINIMUM, 30 } });
      thisLayout.setHGap(5);
      thisLayout.setVGap(5);
      this.setLayout(thisLayout);
      jLabelFolder = new JLabel();
      this.add(jLabelFolder, "1, 1");
      jLabelFolder.setText(Sextante.getText("R_folder"));
      jRFolder = new FileSelectionPanel(true, true, (String[]) null, Sextante.getText("R_folder"));
      this.add(jRFolder, "2,1");
      final String sFolder = SextanteGUI.getSettingParameterValue(SextanteRSettings.R_FOLDER);
      jRFolder.setFilepath(sFolder);
      jLabelScriptsFolder = new JLabel();
      this.add(jLabelScriptsFolder, "1, 2");
      jLabelScriptsFolder.setText(Sextante.getText("Scripts_folder"));
      jRScriptsFolder = new FileSelectionPanel(true, true, (String[]) null, Sextante.getText("Scripts_folder"));
      this.add(jRScriptsFolder, "2,2,");
      final String sScriptsFolder = SextanteGUI.getSettingParameterValue(SextanteRSettings.R_SCRIPTS_FOLDER);
      jRScriptsFolder.setFilepath(sScriptsFolder);

      jButton = new JButton(Sextante.getText("load_scripts"));
      jButton.addActionListener(new ActionListener() {
         public void actionPerformed(final ActionEvent arg0) {
            SextanteGUI.setSettingParameterValue(SextanteRSettings.R_SCRIPTS_FOLDER, jRScriptsFolder.getFilepath());
            SextanteGUI.updateAlgorithmProvider(RAlgorithmProvider.class);
            final int iNumAlgs = Sextante.getAlgorithms().get(new RAlgorithmProvider().getName()).size();
            JOptionPane.showMessageDialog(null, Sextante.getText("ScriptsLoaded") + " " + iNumAlgs + ". ",
                     Sextante.getText("Scripts"), JOptionPane.INFORMATION_MESSAGE);
         }
      });
      this.add(jButton, "2,4");


      jActivateCheckBox = new JCheckBox(Sextante.getText("ActivateProvider"));
      final String sActivate = SextanteGUI.getSettingParameterValue(SextanteRSettings.R_ACTIVATE);
      final boolean bActivate = Boolean.parseBoolean(sActivate);
      jActivateCheckBox.setSelected(bActivate);
      jActivateCheckBox.addActionListener(new ActionListener() {
         public void actionPerformed(final ActionEvent arg0) {
            setCursor(new Cursor(Cursor.WAIT_CURSOR));
            SextanteGUI.setSettingParameterValue(SextanteRSettings.R_ACTIVATE,
                     new Boolean(jActivateCheckBox.isSelected()).toString());
            SextanteGUI.updateAlgorithmProvider(RAlgorithmProvider.class);
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
         }
      });
      this.add(jActivateCheckBox, "1,5");

   }


   @Override
   public HashMap<String, String> getValues() {

      final HashMap<String, String> map = new HashMap<String, String>();
      String path = jRFolder.getFilepath();
      if (path != null) {
         map.put(SextanteRSettings.R_FOLDER, path);
      }
      path = jRScriptsFolder.getFilepath();
      if (path != null) {
         map.put(SextanteRSettings.R_SCRIPTS_FOLDER, path);
      }
      map.put(SextanteRSettings.R_ACTIVATE, new Boolean(jActivateCheckBox.isSelected()).toString());
      return map;

   }

}
