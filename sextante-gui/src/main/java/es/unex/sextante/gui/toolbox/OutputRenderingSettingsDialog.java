

package es.unex.sextante.gui.toolbox;

import info.clearthought.layout.TableLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;

import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.OutputObjectsSet;
import es.unex.sextante.dataObjects.ILayer;
import es.unex.sextante.gui.core.SextanteGUI;
import es.unex.sextante.outputs.Output;
import es.unex.sextante.outputs.OutputRasterLayer;
import es.unex.sextante.outputs.OutputVectorLayer;


public class OutputRenderingSettingsDialog
         extends
            JDialog {

   private HashMap<String, Object> m_Settings  = new HashMap<String, Object>();
   private final JLabel            jLabel[]    = new JLabel[10];
   private JButton                 jButtonCancel;
   private JButton                 jButtonOK;
   private final JComboBox         jComboBox[] = new JComboBox[10];
   private final GeoAlgorithm      m_Alg;


   public OutputRenderingSettingsDialog(final GeoAlgorithm alg) {

      m_Alg = alg;
      initGUI();
      setLocationRelativeTo(null);

   }


   public HashMap<String, Object> getSettings() {

      return m_Settings;

   }


   private void initGUI() {
      try {
         final TableLayout thisLayout = new TableLayout(new double[][] {
                  { 6.0, TableLayout.FILL, TableLayout.FILL, 10.0, TableLayout.FILL, TableLayout.FILL, 6.0 },
                  { 6.0, TableLayout.MINIMUM, TableLayout.MINIMUM, TableLayout.MINIMUM, TableLayout.MINIMUM, TableLayout.MINIMUM,
                           TableLayout.MINIMUM, TableLayout.MINIMUM, TableLayout.MINIMUM, TableLayout.MINIMUM,
                           TableLayout.MINIMUM, TableLayout.FILL, TableLayout.MINIMUM, 6.0 } });
         thisLayout.setHGap(5);
         thisLayout.setVGap(5);
         getContentPane().setLayout(thisLayout);

         int iRow = 1;
         final OutputObjectsSet outputs = m_Alg.getOutputObjects();
         for (int i = 0; i < outputs.getOutputObjectsCount(); i++) {
            final Output out = outputs.getOutput(i);
            if (out instanceof OutputRasterLayer) {
               jLabel[iRow - 1] = new JLabel(out.getDescription());
               getContentPane().add(jLabel[iRow - 1], "1," + Integer.toString(iRow) + ",2," + Integer.toString(iRow));
               final ComboBoxModel jComboBoxModel = new DefaultComboBoxModel(SextanteGUI.getInputFactory().getRasterLayers());
               jComboBox[iRow - 1] = new JComboBox();
               getContentPane().add(jComboBox[iRow - 1], "4," + Integer.toString(iRow) + ",5," + Integer.toString(iRow));
               jComboBox[iRow - 1].setModel(jComboBoxModel);
               iRow++;
            }
            if (out instanceof OutputVectorLayer) {
               jLabel[iRow - 1] = new JLabel(out.getDescription());
               getContentPane().add(jLabel[iRow - 1], "1," + Integer.toString(iRow) + ",2," + Integer.toString(iRow));
               final int type = ((OutputVectorLayer) out).getShapeType();
               final ComboBoxModel jComboBoxModel = new DefaultComboBoxModel(getVectorLayers(type));
               jComboBox[iRow - 1] = new JComboBox();
               getContentPane().add(jComboBox[iRow - 1], "4," + Integer.toString(iRow) + ",5," + Integer.toString(iRow));
               jComboBox[iRow - 1].setModel(jComboBoxModel);
               iRow++;
            }
         }
         {
            jButtonOK = new JButton();
            getContentPane().add(jButtonOK, "4, 12");
            jButtonOK.setText("OK");
            jButtonOK.addActionListener(new ActionListener() {
               public void actionPerformed(final ActionEvent evt) {
                  createSettings();
                  dispose();
                  setVisible(false);
               }
            });
         }
         {
            jButtonCancel = new JButton();
            getContentPane().add(jButtonCancel, "5, 12");
            jButtonCancel.setText("Cancel");
            jButtonCancel.addActionListener(new ActionListener() {
               public void actionPerformed(final ActionEvent evt) {
                  m_Settings = null;
                  dispose();
                  setVisible(false);
               }
            });
         }
         {
            this.setSize(504, 218);
         }
      }
      catch (final Exception e) {
         e.printStackTrace();
      }
   }


   private Object[] getVectorLayers(final int type) {

      //TODO:use type!!
      if (type != OutputVectorLayer.SHAPE_TYPE_UNDEFINED) {
         return SextanteGUI.getInputFactory().getVectorLayers(AdditionalInfoVectorLayer.SHAPE_TYPE_ANY);
      }
      return SextanteGUI.getInputFactory().getVectorLayers(type);
   }


   protected void createSettings() {

      int iCombo = 0;
      final OutputObjectsSet outputs = m_Alg.getOutputObjects();
      for (int i = 0; i < outputs.getOutputObjectsCount(); i++) {
         final Output out = outputs.getOutput(i);
         if (out instanceof OutputRasterLayer) {
            final Object layer = jComboBox[iCombo].getSelectedItem();
            if (layer != null) {
               m_Settings.put(out.getName(), SextanteGUI.getDataRenderer().getRenderingDataFromLayer((ILayer) layer));
               iCombo++;
            }
         }
         if (out instanceof OutputVectorLayer) {
            final Object layer = jComboBox[iCombo].getSelectedItem();
            if (layer != null) {
               m_Settings.put(out.getName(), SextanteGUI.getDataRenderer().getRenderingDataFromLayer((ILayer) layer));
               iCombo++;
            }
         }
      }

   }

}
