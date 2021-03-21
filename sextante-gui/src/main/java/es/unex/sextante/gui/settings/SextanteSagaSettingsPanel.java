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

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.gui.algorithm.FileSelectionPanel;
import es.unex.sextante.gui.core.SextanteGUI;
import es.unex.sextante.gui.saga.SagaAlgorithmProvider;
import es.unex.sextante.gui.saga.SagaExecutionException;
import es.unex.sextante.gui.saga.SagaUtils;


public class SextanteSagaSettingsPanel
         extends
            SettingPanel {

   private FileSelectionPanel jSagaFolder;
   private JButton            jButton;
   private JLabel             jLabelFolder;
   private JCheckBox          jActivateCheckBox;


   @Override
   protected void initGUI() {

      final boolean bCanConfigureSaga = new Boolean(SextanteGUI.getSettingParameterValue(SextanteSagaSettings.CAN_CONFIGURE_SAGA)).booleanValue();

      final TableLayout thisLayout = new TableLayout(new double[][] {
               { 3.0, TableLayoutConstants.FILL, TableLayoutConstants.FILL, 3.0 },
               { 3.0, TableLayoutConstants.MINIMUM, 30, TableLayoutConstants.MINIMUM, TableLayoutConstants.FILL,
                        TableLayoutConstants.MINIMUM, 30 } });
      thisLayout.setHGap(5);
      thisLayout.setVGap(5);
      this.setLayout(thisLayout);
      jLabelFolder = new JLabel();
      this.add(jLabelFolder, "1, 1");
      jLabelFolder.setText(Sextante.getText("Saga_folder"));
      //jLabelFolder.setVisible(bCanConfigureSaga);
      jSagaFolder = new FileSelectionPanel(true, true, (String[]) null, Sextante.getText("Saga_folder"));
      //jSagaFolder.setVisible(bCanConfigureSaga);
      this.add(jSagaFolder, "2,1");
      final String sFolder = SextanteGUI.getSettingParameterValue(SextanteSagaSettings.SAGA_FOLDER);
      jSagaFolder.setFilepath(sFolder);
      //      jButton = new JButton(Sextante.getText("set_up_saga"));
      //      jButton.addActionListener(new ActionListener() {
      //         public void actionPerformed(final ActionEvent arg0) {
      //            SextanteGUI.setSettingParameterValue(SextanteSagaSettings.SAGA_FOLDER, jSagaFolder.getFilepath());
      //            setupSaga();
      //         }
      //      });
      //this.add(jButton, "2,3");
      //jButton.setVisible(bCanConfigureSaga);
      jActivateCheckBox = new JCheckBox(Sextante.getText("ActivateProvider"));
      final String sActivate = SextanteGUI.getSettingParameterValue(SextanteSagaSettings.SAGA_ACTIVATE);
      final boolean bActivate = Boolean.parseBoolean(sActivate);
      jActivateCheckBox.setSelected(bActivate);
      jActivateCheckBox.addActionListener(new ActionListener() {
         public void actionPerformed(final ActionEvent arg0) {
            setCursor(new Cursor(Cursor.WAIT_CURSOR));
            SextanteGUI.setSettingParameterValue(SextanteSagaSettings.SAGA_ACTIVATE,
                     new Boolean(jActivateCheckBox.isSelected()).toString());
            SextanteGUI.updateAlgorithmProvider(SagaAlgorithmProvider.class);
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
         }
      });
      this.add(jActivateCheckBox, "1,5");

   }


   protected void setupSaga() {

      try {
         this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
         SagaUtils.installSaga();
         this.setCursor(Cursor.getDefaultCursor());
         SextanteGUI.updateAlgorithmProvider(SagaAlgorithmProvider.class);
         final HashMap<String, GeoAlgorithm> algs = Sextante.getAlgorithms().get("SAGA");
         int iNumAlgs = 0;
         if (algs != null) {
            iNumAlgs = algs.size();
         }
         JOptionPane.showMessageDialog(null, Sextante.getText("SagaAlgorithmsLoaded") + " " + iNumAlgs + ". ",
                  Sextante.getText("SAGA"), JOptionPane.INFORMATION_MESSAGE);
      }
      catch (final SagaExecutionException e) {
         e.printStackTrace();
         JOptionPane.showMessageDialog(null, Sextante.getText("ErrorInstallingSaga"), Sextante.getText("SAGA"),
                  JOptionPane.ERROR_MESSAGE);
      }

   }


   @Override
   public HashMap<String, String> getValues() {

      final HashMap<String, String> map = new HashMap<String, String>();
      final String path = jSagaFolder.getFilepath();
      if (path != null) {
         map.put(SextanteSagaSettings.SAGA_FOLDER, path);
      }
      map.put(SextanteSagaSettings.SAGA_ACTIVATE, new Boolean(jActivateCheckBox.isSelected()).toString());
      return map;

   }

}
