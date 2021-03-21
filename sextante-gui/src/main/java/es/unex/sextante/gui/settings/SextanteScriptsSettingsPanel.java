package es.unex.sextante.gui.settings;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import es.unex.sextante.core.Sextante;
import es.unex.sextante.gui.algorithm.FileSelectionPanel;
import es.unex.sextante.gui.cmd.ScriptAlgorithmProvider;
import es.unex.sextante.gui.core.SextanteGUI;

public class SextanteScriptsSettingsPanel
         extends
            SettingPanel {

   private FileSelectionPanel jFolderScripts;
   private JLabel             jLabelModels;
   private JButton            jButton;


   @Override
   protected void initGUI() {
      final TableLayout thisLayout = new TableLayout(new double[][] {
               { 3.0, TableLayoutConstants.FILL, TableLayoutConstants.FILL, 3.0 },
               { 3.0, TableLayoutConstants.MINIMUM, 30, TableLayoutConstants.MINIMUM, TableLayoutConstants.FILL } });
      thisLayout.setHGap(5);
      thisLayout.setVGap(5);
      this.setLayout(thisLayout);
      jLabelModels = new JLabel();
      this.add(jLabelModels, "1, 1");
      jLabelModels.setText(Sextante.getText("Scripts_folder"));
      jFolderScripts = new FileSelectionPanel(true, true, (String[]) null, Sextante.getText("Scripts_folder"));
      this.add(jFolderScripts, "2,1");
      final String sFolder = SextanteGUI.getSettingParameterValue(SextanteScriptsSettings.SCRIPTS_FOLDER);
      jFolderScripts.setFilepath(sFolder);
      jButton = new JButton(Sextante.getText("load_scripts"));
      jButton.addActionListener(new ActionListener() {
         public void actionPerformed(final ActionEvent arg0) {
            SextanteGUI.setSettingParameterValue(SextanteScriptsSettings.SCRIPTS_FOLDER, jFolderScripts.getFilepath());
            SextanteGUI.updateAlgorithmProvider(ScriptAlgorithmProvider.class);
            final int iNumAlgs = Sextante.getAlgorithms().get(new ScriptAlgorithmProvider().getName()).size();
            JOptionPane.showMessageDialog(null, Sextante.getText("ScriptsLoaded") + " " + iNumAlgs + ". ",
                     Sextante.getText("Scripts"), JOptionPane.INFORMATION_MESSAGE);
         }
      });
      this.add(jButton, "2,3");

   }


   @Override
   public HashMap<String, String> getValues() {

      final HashMap<String, String> map = new HashMap<String, String>();
      final String path = jFolderScripts.getFilepath();
      if (path != null) {
         map.put(SextanteScriptsSettings.SCRIPTS_FOLDER, path);
      }
      return map;

   }

}
