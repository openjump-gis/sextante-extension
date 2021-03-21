package es.unex.sextante.gui.settings;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.util.HashMap;

import javax.swing.JLabel;

import es.unex.sextante.core.Sextante;
import es.unex.sextante.gui.algorithm.FileSelectionPanel;
import es.unex.sextante.gui.core.SextanteGUI;

public class SextanteFolderSettingsPanel
         extends
            SettingPanel {

   private FileSelectionPanel jFolderResults;
   private JLabel             jLabelResults;


   @Override
   protected void initGUI() {
      final TableLayout thisLayout = new TableLayout(new double[][] {
               { 3.0, TableLayoutConstants.FILL, TableLayoutConstants.FILL, 3.0 },
               { 3.0, TableLayoutConstants.MINIMUM, TableLayoutConstants.MINIMUM, TableLayoutConstants.FILL } });
      thisLayout.setHGap(5);
      thisLayout.setVGap(5);
      this.setLayout(thisLayout);
      {
         jLabelResults = new JLabel();
         this.add(jLabelResults, "1, 1");
         jLabelResults.setText(Sextante.getText("Output_folder"));
         jFolderResults = new FileSelectionPanel(true, true, (String[]) null, Sextante.getText("Output_folder"));
         this.add(jFolderResults, "2,1");
         jFolderResults.setFilepath(SextanteGUI.getOutputFolder());
      }
   }


   @Override
   public HashMap<String, String> getValues() {

      final HashMap<String, String> map = new HashMap<String, String>();
      final String path = jFolderResults.getFilepath();
      if (path != null) {
         map.put(SextanteFolderSettings.RESULTS_FOLDER, path);
      }
      return map;

   }

}
