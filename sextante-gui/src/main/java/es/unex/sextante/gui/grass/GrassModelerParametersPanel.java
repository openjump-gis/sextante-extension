package es.unex.sextante.gui.grass;

import info.clearthought.layout.TableLayoutConstants;

import java.util.HashMap;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import es.unex.sextante.core.OutputObjectsSet;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.gui.modeler.DefaultModelerParametersPanel;

public class GrassModelerParametersPanel
         extends
            DefaultModelerParametersPanel {

   private JComboBox jComboRestrictGeometryType;


   @Override
   protected void addOutputObjects(final JPanel pane) {

      super.addOutputObjects(pane);

      addTitleLabel(pane, Sextante.getText("grass_restrict_geometry_ouput"), m_iCurrentRow, false);
      m_iCurrentRow++;
      final String[] options = new String[] { Sextante.getText("Points"), Sextante.getText("Lines"), Sextante.getText("Polygons") };
      jComboRestrictGeometryType = new JComboBox(options);
      pane.add(jComboRestrictGeometryType, getStringTableCoords(2, m_iCurrentRow));

   }


   @Override
   public boolean assignParameters(final HashMap map) {

      final boolean ret = super.assignParameters(map);
      if (ret) {
         m_GlobalAlgorithm.setGeometryTypeRestriction(jComboRestrictGeometryType.getSelectedIndex());
         return true;
      }
      else {
         return false;
      }

   }


   @Override
   protected double[][] getTableLayoutMatrix() {

      int i;
      int iRows = 0;

      final double iSizeColumns[] = { 10, TableLayoutConstants.FILL, 360, 10 };

      iRows += m_Algorithm.getNumberOfParameters();

      if (m_Algorithm.requiresRasterLayers() || m_Algorithm.requiresMultipleRasterLayers()
          || m_Algorithm.requiresMultipleRasterBands()) {
         iRows++;
      }
      if (m_Algorithm.requiresVectorLayers() || m_Algorithm.requiresMultipleVectorLayers()) {
         iRows++;
      }
      if (m_Algorithm.requiresTables() || m_Algorithm.requiresMultipleTables()) {
         iRows++;
      }
      if (m_Algorithm.requires3DRasterLayers()) {
         iRows++;
      }
      if (m_Algorithm.requiresNonDataObjects()) {
         iRows++;
      }

      final OutputObjectsSet ooSet = m_Algorithm.getOutputObjects();
      int iOutput = ooSet.getOutputObjectsCount();

      final int iVectorLayers = ooSet.getVectorLayersCount();
      if (iVectorLayers != 0) {
         iRows += 2;
         iOutput += 2;
      }

      if (iOutput != 0) {
         iRows += (iOutput + 1);
      }


      final double iSizeRows[] = new double[iRows];
      for (i = 0; i < iRows - iOutput; i++) {
         iSizeRows[i] = CELL_HEIGHT;
      }
      for (i = iRows - iOutput; i < iRows; i++) {
         iSizeRows[i] = CELL_HEIGHT * 2.5;
      }

      final double iSize[][] = new double[2][];
      iSize[0] = iSizeColumns;
      iSize[1] = iSizeRows;

      return iSize;

   }


}
