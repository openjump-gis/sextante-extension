package es.unex.sextante.gridAnalysis.sumOfCostFromAllPoints;

import java.util.ArrayList;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.rasterWrappers.GridCell;

public class SumOfCostFromAllPointsAlgorithm
         extends
            GeoAlgorithm {

   private static final int   NO_DATA  = -1;

   public static final String COST     = "COST";
   public static final String FEATURES = "FEATURES";
   public static final String ACCCOST  = "ACCCOST";

   int                        m_iNX, m_iNY;
   IRasterLayer               m_Cost;
   IRasterLayer               m_Features;
   IRasterLayer               m_AccCost, m_TotalCost;
   ArrayList                  m_CentralPoints, m_AdjPoints;


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Sum_of_cost_to_all_points"));
      setGroup(Sextante.getText("Cost_distances_and_routes"));
      setUserCanDefineAnalysisExtent(true);
      setIsDeterminatedProcess(false);

      try {
         m_Parameters.addInputRasterLayer(COST, Sextante.getText("Unitary_cost"), true);
         m_Parameters.addInputRasterLayer(FEATURES, Sextante.getText("Origin-destination_points"), true);
         addOutputRasterLayer(ACCCOST, Sextante.getText("Accumulated_cost"));
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

      m_Cost = m_Parameters.getParameterValueAsRasterLayer(COST);
      m_Features = m_Parameters.getParameterValueAsRasterLayer(FEATURES);

      m_TotalCost = getNewRasterLayer(ACCCOST, Sextante.getText("Accumulated_cost"), IRasterLayer.RASTER_DATA_TYPE_DOUBLE);

      final AnalysisExtent extent = m_TotalCost.getWindowGridExtent();
      m_AccCost = getTempRasterLayer(IRasterLayer.RASTER_DATA_TYPE_DOUBLE, extent);

      m_Cost.setWindowExtent(extent);
      m_Cost.setInterpolationMethod(IRasterLayer.INTERPOLATION_BSpline);

      m_Features.setWindowExtent(extent);
      m_Features.setInterpolationMethod(IRasterLayer.INTERPOLATION_NearestNeighbour);

      m_iNX = m_Cost.getNX();
      m_iNY = m_Cost.getNY();

      m_TotalCost.setNoDataValue(NO_DATA);
      m_TotalCost.assign(0.0);

      for (y = 0; (y < m_iNY) && setProgress(y, m_iNY); y++) {
         for (x = 0; x < m_iNX; x++) {
            dValue = m_Features.getCellValueAsDouble(x, y);
            if ((dValue != 0.0) && !m_Features.isNoDataValue(dValue)) {
               m_CentralPoints.clear();
               m_CentralPoints.add(new GridCell(x, y, 0));
               m_AccCost.setCellValue(x, y, 0.0);
               m_AccCost.setNoDataValue(NO_DATA);
               m_AccCost.assignNoData();
               calculateCost();
               if (m_Task.isCanceled()) {
                  return false;
               }
               m_AccCost.multiply(dValue);
               m_TotalCost.add(m_AccCost);
            }
         }
      }

      return !m_Task.isCanceled();

   }


   private void calculateCost() {

      int i, j;
      int iPt;
      int x, y, x2, y2;
      double dAccCost;
      double dCost1, dCost2;
      double dPrevAccCost;
      GridCell cell;

      final double dDist[][] = new double[3][3];

      for (i = -1; i < 2; i++) {
         for (j = -1; j < 2; j++) {
            dDist[i + 1][j + 1] = Math.sqrt(i * i + j * j);
         }
      }

      while (m_CentralPoints.size() != 0) {
         for (iPt = 0; iPt < m_CentralPoints.size(); iPt++) {
            cell = (GridCell) m_CentralPoints.get(iPt);
            x = cell.getX();
            y = cell.getY();
            dCost1 = m_Cost.getCellValueAsDouble(x, y);
            for (i = -1; i < 2; i++) {
               for (j = -1; j < 2; j++) {
                  x2 = x + i;
                  y2 = y + j;
                  dCost2 = m_Cost.getCellValueAsDouble(x2, y2);
                  if (!m_Cost.isNoDataValue(dCost1) && !m_Cost.isNoDataValue(dCost2)) {
                     dAccCost = 0;
                     dAccCost = m_AccCost.getCellValueAsDouble(x, y);
                     dAccCost += ((dCost1 + dCost2) / 2.0 * dDist[i + 1][j + 1]);
                     dPrevAccCost = m_AccCost.getCellValueAsDouble(x2, y2);
                     if (m_AccCost.isNoDataValue(dPrevAccCost) || (dPrevAccCost > dAccCost)) {
                        m_AccCost.setCellValue(x2, y2, dAccCost);
                        m_AdjPoints.add(new GridCell(x2, y2, 0));
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
