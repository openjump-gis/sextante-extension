package es.unex.sextante.gui.modeler;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.ObjectAndDescription;
import es.unex.sextante.core.OutputObjectsSet;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.gui.core.IGUIFactory;
import es.unex.sextante.gui.core.SextanteGUI;
import es.unex.sextante.modeler.elements.IModelElement;
import es.unex.sextante.modeler.elements.ModelElement3DRasterLayer;
import es.unex.sextante.modeler.elements.ModelElementNumericalValue;
import es.unex.sextante.modeler.elements.ModelElementRasterLayer;
import es.unex.sextante.modeler.elements.ModelElementTable;
import es.unex.sextante.modeler.elements.ModelElementVectorLayer;
import es.unex.sextante.outputs.Output;
import es.unex.sextante.outputs.Output3DRasterLayer;
import es.unex.sextante.outputs.OutputNumericalValue;
import es.unex.sextante.outputs.OutputRasterLayer;
import es.unex.sextante.outputs.OutputTable;
import es.unex.sextante.outputs.OutputVectorLayer;

public class AlgorithmDialog
         extends
            JDialog {

   private final ModelAlgorithm                 m_ModelAlgorithm;
   private final GeoAlgorithm                   m_Algorithm;
   private final String                         m_sAlgorithmName;
   private final String                         m_sAlgorithmDescription;
   private final HashMap                        m_DataObjects;
   private JPanel                               jPanelButtons;
   private JPanel                               jMainPanel;
   private JButton                              jButtonCancel;
   private JButton                              jButtonOK;

   protected GeoAlgorithmModelerParametersPanel jPanelParametersMain = null;
   private int                                  m_iDialogReturn;


   public AlgorithmDialog(final GeoAlgorithm algorithm,
                          final String sName,
                          final String sDescription,
                          final ModelAlgorithm modelAlgorithm,
                          final GeoAlgorithmModelerParametersPanel panel,
                          final HashMap dataObjects,
                          final JDialog parent) {

      super(parent, sDescription, true);
      //setLocationRelativeTo(null);

      m_Algorithm = algorithm;
      m_ModelAlgorithm = modelAlgorithm;
      m_DataObjects = dataObjects;
      m_sAlgorithmName = sName;
      m_sAlgorithmDescription = sDescription;

      jPanelParametersMain = panel;
      jPanelParametersMain.init(this);

      initGUI();
      setLocationRelativeTo(null);

   }


   public AlgorithmDialog(final GeoAlgorithm algorithm,
                          final String sName,
                          final String sDescription,
                          final ModelAlgorithm modelAlgorithm,
                          final GeoAlgorithmModelerParametersPanel panel,
                          final HashMap dataObjects) {

      super(SextanteGUI.getMainFrame(), "", true);
      setLocationRelativeTo(null);

      m_Algorithm = algorithm;
      m_ModelAlgorithm = modelAlgorithm;
      m_DataObjects = dataObjects;
      m_sAlgorithmName = sName;
      m_sAlgorithmDescription = sDescription;

      jPanelParametersMain = panel;
      jPanelParametersMain.init(this);

      initGUI();
      setLocationRelativeTo(null);

   }


   private void initGUI() {

      jMainPanel = new JPanel();

      this.getContentPane().add(jMainPanel);

      final TableLayout thisLayout = new TableLayout(new double[][] {
               { 10.0, TableLayoutConstants.FILL, TableLayoutConstants.FILL, 10.0 }, { 338.0, TableLayoutConstants.FILL } });
      thisLayout.setHGap(5);
      thisLayout.setVGap(5);
      jMainPanel.setLayout(thisLayout);
      jMainPanel.setSize(new java.awt.Dimension(600, 373));
      jMainPanel.setPreferredSize(new java.awt.Dimension(692, 384));
      jMainPanel.setMaximumSize(new java.awt.Dimension(700, 373));
      this.setSize(new java.awt.Dimension(700, 415));
      this.setPreferredSize(new java.awt.Dimension(700, 415));
      {
         jPanelButtons = new JPanel();
         jMainPanel.add(jPanelButtons, "1, 1, 2, 1");
         jMainPanel.add(getJPanelParameters(), "1, 0, 2, 0");
      }
      {
         jButtonOK = new JButton();
         jPanelButtons.add(jButtonOK, "1, 2");
         jButtonOK.setText(Sextante.getText("OK"));
         jButtonOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final java.awt.event.ActionEvent e) {
               if (addAlgorithm()) {
                  m_iDialogReturn = IGUIFactory.OK;
                  cancel();
               }
            }
         });
      }
      {
         jButtonCancel = new JButton();
         jPanelButtons.add(jButtonCancel, "2, 2");
         jButtonCancel.setText(Sextante.getText("Cancel"));
         jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final java.awt.event.ActionEvent e) {
               m_iDialogReturn = IGUIFactory.CANCEL;
               cancel();
            }
         });
      }

   }


   protected boolean addAlgorithm() {

      String sKey;
      String sAssignment;
      String sDescription;
      final HashMap map = new HashMap();
      OutputObjectsSet ooSet;
      if (assignParameters(map)) {
         m_ModelAlgorithm.addAlgorithm(m_Algorithm, m_sAlgorithmName);
         final Set set = map.keySet();
         final Iterator iter = set.iterator();
         while (iter.hasNext()) {
            sKey = (String) iter.next();
            sAssignment = (String) map.get(sKey);
            m_ModelAlgorithm.addInputAsignment(sKey, sAssignment, m_sAlgorithmName);
         }
         ooSet = m_Algorithm.getOutputObjects();
         for (int i = 0; i < ooSet.getOutputObjectsCount(); i++) {
            final Output out = ooSet.getOutput(i);
            if ((out instanceof OutputRasterLayer) || (out instanceof OutputVectorLayer) || (out instanceof OutputTable)
                || (out instanceof Output3DRasterLayer) || (out instanceof OutputNumericalValue)) {
               sKey = out.getName();
               sDescription = out.getDescription();
               sKey += m_sAlgorithmName;
               sDescription = "\"" + sDescription + "\" " + Sextante.getText("from") + " " + m_sAlgorithmDescription;
               m_DataObjects.put(sKey, new ObjectAndDescription(sDescription, getOutputAsModelElement(out)));
            }

         }
         return true;
      }
      else {
         JOptionPane.showMessageDialog(null, Sextante.getText("Invalid_parameters"), Sextante.getText("Warning"),
                  JOptionPane.WARNING_MESSAGE);
         return false;
      }

   }


   private IModelElement getOutputAsModelElement(final Output out) {

      IModelElement element = null;

      if (out instanceof OutputRasterLayer) {
         element = new ModelElementRasterLayer();
         final int iBands = ((OutputRasterLayer) out).getNumberOfBands();
         ((ModelElementRasterLayer) element).setNumberOfBands(iBands);
      }
      else if (out instanceof OutputVectorLayer) {
         element = new ModelElementVectorLayer();
         final int iShapeType = ((OutputVectorLayer) out).getShapeType();
         ((ModelElementVectorLayer) element).setShapeType(iShapeType);
      }
      else if (out instanceof OutputTable) {
         element = new ModelElementTable();
      }
      else if (out instanceof Output3DRasterLayer) {
         element = new ModelElement3DRasterLayer();
      }
      else if (out instanceof OutputNumericalValue) {
         element = new ModelElementNumericalValue();
      }

      return element;

   }


   protected boolean assignParameters(final HashMap map) {

      return getJPanelParameters().assignParameters(map);

   }


   public void cancel() {

      dispose();
      setVisible(false);

   }


   private GeoAlgorithmModelerParametersPanel getJPanelParameters() {

      return jPanelParametersMain;

   }


   public int getDialogReturn() {

      return m_iDialogReturn;

   }


   public GeoAlgorithm getAlgorithm() {

      return m_Algorithm;

   }


   public ModelAlgorithm getModelAlgorithm() {

      return m_ModelAlgorithm;

   }


   public HashMap getDataObjects() {

      return m_DataObjects;

   }


   public String getAlgorithmName() {

      return m_sAlgorithmName;

   }


   public String getAlgorithmDescription() {

      return m_sAlgorithmDescription;

   }


}
