

package es.unex.sextante.gridTools.closeGapsNN;

import java.util.ArrayList;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.rasterWrappers.GridCell;


public class CloseGapsNNAlgorithm
         extends
            GeoAlgorithm {

   private static final int   NO_DATA = -1;

   public static final String LAYER   = "LAYER";
   public static final String RESULT  = "RESULT";

   int                        m_iNX, m_iNY;
   IRasterLayer               m_Window;
   IRasterLayer               m_Result;
   IRasterLayer               m_Filled;
   ArrayList                  m_CentralPoints, m_AdjPoints;


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Void_filling_[nearest_neighbour]"));
      setGroup(Sextante.getText("Basic_tools_for_raster_layers"));
      setUserCanDefineAnalysisExtent(false);
      setIsDeterminatedProcess(false);

      try {
         m_Parameters.addInputRasterLayer(LAYER, Sextante.getText("Layer"), true);
         addOutputRasterLayer(RESULT, Sextante.getText("Filled_layer"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int x, y;
      double dValue;

      m_CentralPoints = new ArrayList();
      m_AdjPoints = new ArrayList();

      m_Window = m_Parameters.getParameterValueAsRasterLayer(LAYER);
      m_Window.setFullExtent();
      m_Result = getNewRasterLayer(RESULT, m_Window.getName() + Sextante.getText("[filled]"), m_Window.getDataType(),
               m_Window.getWindowGridExtent());
      m_Filled = getTempRasterLayer(IRasterLayer.RASTER_DATA_TYPE_INT, m_Window.getWindowGridExtent());


      m_iNX = m_Window.getNX();
      m_iNY = m_Window.getNY();

      m_Filled.setNoDataValue(NO_DATA);
      m_Filled.assignNoData();

      m_Result.setNoDataValue(NO_DATA);
      m_Result.assignNoData();

      for (y = 0; y < m_iNY; y++) {
         for (x = 0; x < m_iNX; x++) {
            dValue = m_Window.getCellValueAsDouble(x, y);
            if (!m_Window.isNoDataValue(dValue)) {
               m_CentralPoints.add(new GridCell(x, y, dValue));
               m_Filled.setCellValue(x, y, 0.0);
               m_Result.setCellValue(x, y, dValue);
            }
         }
      }

      closeGaps();

      return !m_Task.isCanceled();

   }


   private void closeGaps() {

      int i, j;
      int iPt;
      int x, y, x2, y2;
      double dValue;
      double dAccCost;
      double dPrevAccCost;
      GridCell cell;

      final double dDist[][] = new double[3][3];

      for (i = -1; i < 2; i++) {
         for (j = -1; j < 2; j++) {
            dDist[i + 1][j + 1] = Math.sqrt(i * i + j * j);
         }
      }

      while ((m_CentralPoints.size() != 0) && !m_Task.isCanceled()) {
         for (iPt = 0; iPt < m_CentralPoints.size(); iPt++) {
            cell = (GridCell) m_CentralPoints.get(iPt);
            x = cell.getX();
            y = cell.getY();
            dValue = cell.getValue();
            for (i = -1; i < 2; i++) {
               for (j = -1; j < 2; j++) {
                  x2 = x + i;
                  y2 = y + j;
                  if (m_Window.isInWindow(x2, y2)) {
                     dAccCost = m_Filled.getCellValueAsDouble(x, y);
                     dAccCost += dDist[i + 1][j + 1];
                     dPrevAccCost = m_Filled.getCellValueAsDouble(x2, y2);
                     if (m_Filled.isNoDataValue(dPrevAccCost) || (dPrevAccCost > dAccCost)) {
                        m_Filled.setCellValue(x2, y2, dAccCost);
                        m_Result.setCellValue(x2, y2, dValue);
                        m_AdjPoints.add(new GridCell(x2, y2, dValue));
                     }
                  }
               }
            }
         }

         m_CentralPoints = m_AdjPoints;
         m_AdjPoints = new ArrayList();

         if (m_Task.isCanceled()) {
            return;
         }

      }

   }

}
