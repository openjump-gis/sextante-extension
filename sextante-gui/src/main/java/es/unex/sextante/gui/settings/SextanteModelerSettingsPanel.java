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
import es.unex.sextante.gui.core.SextanteGUI;
import es.unex.sextante.gui.modeler.ModelerAlgorithmProvider;

public class SextanteModelerSettingsPanel
         extends
            SettingPanel {

   private FileSelectionPanel jFolderModels;
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
      jLabelModels.setText(Sextante.getText("Models_folder"));
      jFolderModels = new FileSelectionPanel(true, true, (String[]) null, Sextante.getText("Models_folder"));
      this.add(jFolderModels, "2,1");
      final String sFolder = SextanteGUI.getSettingParameterValue(SextanteModelerSettings.MODELS_FOLDER);
      jFolderModels.setFilepath(sFolder);
      jButton = new JButton(Sextante.getText("load_models"));
      jButton.addActionListener(new ActionListener() {
         public void actionPerformed(final ActionEvent arg0) {
            SextanteGUI.setSettingParameterValue(SextanteModelerSettings.MODELS_FOLDER, jFolderModels.getFilepath());
            SextanteGUI.updateAlgorithmProvider(ModelerAlgorithmProvider.class);
            final int iNumAlgs = Sextante.getAlgorithms().get(new ModelerAlgorithmProvider().getName()).size();
            JOptionPane.showMessageDialog(null, Sextante.getText("ModelsLoaded") + " " + iNumAlgs + ". ",
                     Sextante.getText("Models"), JOptionPane.INFORMATION_MESSAGE);
         }
      });
      this.add(jButton, "2,3");

   }


   @Override
   public HashMap<String, String> getValues() {

      final HashMap<String, String> map = new HashMap<String, String>();
      final String path = jFolderModels.getFilepath();
      if (path != null) {
         map.put(SextanteModelerSettings.MODELS_FOLDER, path);
      }
      return map;

   }

}
