package es.unex.sextante.gridAnalysis.accCostCombined;

import java.util.ArrayList;

import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.rasterWrappers.GridCell;

public class AccCostCombinedAlgorithm
         extends
            GeoAlgorithm {

   public static final String  ISOCOST      = "ISOCOST";
   public static final String  ANISOCOST    = "ANISOCOST";
   public static final String  COSTDIR      = "COSTDIR";
   public static final String  KFACTOR      = "KFACTOR";
   public static final String  FEATURES     = "FEATURES";
   public static final String  ACCCOST      = "ACCCOST";
   public static final String  CLOSESTPOINT = "CLOSESTPOINT";

   private static final int    NO_DATA      = -1;
   private static final double ANGLES[][]   = { { 315, 0, 45 }, { 270, 0, 90 }, { 225, 180, 135 } };

   int                         m_iNX, m_iNY;
   double                      m_dK;
   IRasterLayer                m_IsotropicCost;
   IRasterLayer                m_AnisotropicCost;
   IRasterLayer                m_CostDir;
   IRasterLayer                m_Features;
   IRasterLayer                m_AccCost;
   IRasterLayer                m_ClosestPoint;
   ArrayList                   m_CentralPoints, m_AdjPoints;


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Accumulated_cost__combined"));
      setGroup(Sextante.getText("Cost_distances_and_routes"));
      setUserCanDefineAnalysisExtent(true);
      setIsDeterminatedProcess(false);

      try {
         m_Parameters.addInputRasterLayer(ISOCOST, Sextante.getText("Isotropic_cost"), true);
         m_Parameters.addInputRasterLayer(ANISOCOST, Sextante.getText("Anisotropic_cost"), true);
         m_Parameters.addInputRasterLayer(COSTDIR, Sextante.getText("Direction_of_maximum_cost_[degrees]"), true);
         m_Parameters.addInputRasterLayer(FEATURES, Sextante.getText("Origin-destination_points"), true);
         m_Parameters.addNumericalValue(KFACTOR, Sextante.getText("K"), 2, AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE);
         addOutputRasterLayer(ACCCOST, Sextante.getText("Accumulated_cost"));
         addOutputRasterLayer(CLOSESTPOINT, Sextante.getText("Closest_points"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int x, y;
      int iPoint = 1;
      double dValue;

      m_CentralPoints = new ArrayList();
      m_AdjPoints = new ArrayList();

      m_IsotropicCost = m_Parameters.getParameterValueAsRasterLayer(ISOCOST);
      m_AnisotropicCost = m_Parameters.getParameterValueAsRasterLayer(ANISOCOST);
      m_CostDir = m_Parameters.getParameterValueAsRasterLayer(COSTDIR);
      m_Features = m_Parameters.getParameterValueAsRasterLayer(FEATURES);

      m_dK = m_Parameters.getParameterValueAsDouble(KFACTOR);

      m_AccCost = getNewRasterLayer(ACCCOST, Sextante.getText("Accumulated_cost"), IRasterLayer.RASTER_DATA_TYPE_DOUBLE);
      m_ClosestPoint = getNewRasterLayer(CLOSESTPOINT, Sextante.getText("Closest_points"), IRasterLayer.RASTER_DATA_TYPE_INT);

      final AnalysisExtent extent = m_AccCost.getWindowGridExtent();

      m_IsotropicCost.setWindowExtent(extent);
      m_IsotropicCost.setInterpolationMethod(IRasterLayer.INTERPOLATION_BSpline);

      m_AnisotropicCost.setWindowExtent(extent);
      m_AnisotropicCost.setInterpolationMethod(IRasterLayer.INTERPOLATION_BSpline);

      m_CostDir.setWindowExtent(extent);
      m_CostDir.setInterpolationMethod(IRasterLayer.INTERPOLATION_BSpline);

      m_Features.setWindowExtent(extent);
      m_Features.setInterpolationMethod(IRasterLayer.INTERPOLATION_NearestNeighbour);

      m_iNX = m_IsotropicCost.getNX();
      m_iNY = m_IsotropicCost.getNY();

      m_AccCost.setNoDataValue(NO_DATA);
      m_AccCost.assignNoData();

      m_ClosestPoint.setNoDataValue(NO_DATA);
      m_ClosestPoint.assignNoData();

      for (y = 0; y < m_iNY; y++) {
         for (x = 0; x < m_iNX; x++) {
            dValue = m_Features.getCellValueAsDouble(x, y);
            if ((dValue != 0.0) && !m_Features.isNoDataValue(dValue)) {
               m_CentralPoints.add(new GridCell(x, y, iPoint));
               m_AccCost.setCellValue(x, y, 0.0);
               m_ClosestPoint.setCellValue(x, y, iPoint);
               iPoint++;
            }
         }
      }

      calculateCost();

      return !m_Task.isCanceled();


   }


   private void calculateCost() {

      int i, j;
      int iPt;
      int iPoint;
      int x, y, x2, y2;
      double dAccCost;
      double dPrevAccCost;
      GridCell cell;

      while ((m_CentralPoints.size() != 0) && !m_Task.isCanceled()) {
         for (iPt = 0; iPt < m_CentralPoints.size(); iPt++) {
            cell = (GridCell) m_CentralPoints.get(iPt);
            x = cell.getX();
            y = cell.getY();
            iPoint = (int) cell.getValue();
            for (i = -1; i < 2; i++) {
               for (j = -1; j < 2; j++) {
                  x2 = x + i;
                  y2 = y + j;
                  final double dCostInDir = getCostInDir(x, y, i, j);
                  if (dCostInDir != NO_DATA) {
                     dAccCost = m_AccCost.getCellValueAsDouble(x, y);
                     dAccCost += dCostInDir * Math.sqrt(i * i + j * j);
                     dPrevAccCost = m_AccCost.getCellValueAsDouble(x2, y2);
                     if (m_AccCost.isNoDataValue(dPrevAccCost) || (dPrevAccCost > dAccCost)) {
                        m_AccCost.setCellValue(x2, y2, dAccCost);
                        m_ClosestPoint.setCellValue(x2, y2, iPoint);
                        m_AdjPoints.add(new GridCell(x2, y2, iPoint));
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


   private double getCostInDir(final int x,
                               final int y,
                               final int iH,
                               final int iV) {

      final double dAngle = ANGLES[iV + 1][iH + 1];

      final int x2 = x + iH;
      final int y2 = y + iV;

      final double dCostDir1 = m_CostDir.getCellValueAsDouble(x, y);
      final double dCostDir2 = m_CostDir.getCellValueAsDouble(x2, y2);
      double dCost1 = m_AnisotropicCost.getCellValueAsDouble(x, y);
      double dCost2 = m_AnisotropicCost.getCellValueAsDouble(x2, y2);
      final double dIsoCost1 = m_IsotropicCost.getCellValueAsDouble(x, y);
      final double dIsoCost2 = m_IsotropicCost.getCellValueAsDouble(x2, y2);

      if (m_AnisotropicCost.isNoDataValue(dCost1) || m_AnisotropicCost.isNoDataValue(dCost1)
          || m_IsotropicCost.isNoDataValue(dIsoCost1) || m_IsotropicCost.isNoDataValue(dIsoCost2)
          || m_CostDir.isNoDataValue(dCostDir1) || m_CostDir.isNoDataValue(dCostDir1)) {
         return NO_DATA;
      }
      else {
         double dDifAngle1 = Math.abs(dCostDir1 - dAngle);
         double dDifAngle2 = Math.abs(dCostDir2 - dAngle);

         dDifAngle1 = Math.toRadians(dDifAngle1);
         dDifAngle2 = Math.toRadians(dDifAngle1);

         dCost1 = Math.pow(Math.cos(dDifAngle1), m_dK) / 2. * (dCost1) + dIsoCost1;
         dCost2 = Math.pow(Math.cos(dDifAngle2), m_dK) / 2. * (dCost2) + dIsoCost2;

         return dCost1 + dCost2;
      }

   }

}
