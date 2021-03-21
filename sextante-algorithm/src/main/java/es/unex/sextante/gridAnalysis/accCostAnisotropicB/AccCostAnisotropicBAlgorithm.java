package es.unex.sextante.gridAnalysis.accCostAnisotropicB;

import java.util.ArrayList;
import java.util.Arrays;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.parameters.FixedTableModel;
import es.unex.sextante.rasterWrappers.GridCell;

public class AccCostAnisotropicBAlgorithm
         extends
            GeoAlgorithm {

   public static final String   COST         = "COST";
   public static final String   COSTDIR      = "COSTDIR";
   public static final String   FEATURES     = "FEATURES";
   public static final String   ACCCOST      = "ACCCOST";
   public static final String   CLOSESTPOINT = "CLOSESTPOINT";
   public static final String   FACTORS      = "FACTORS";

   private static final int     NO_DATA      = -1;
   private static final int     DATA         = 1;
   private static final double  ANGLES[][]   = { { 135, 180, 225 }, { 90, 0, 270 }, { 45, 0, 315 } };

   private final static int     m_iOffsetX[] = { 0, 1, 1, 1, 0, -1, -1, -1 };
   private final static int     m_iOffsetY[] = { 1, 1, 0, -1, -1, -1, 0, 1 };
   private final static double  m_dDist[]    = { 1, Math.sqrt(2.), 1, Math.sqrt(2.), 1, Math.sqrt(2.), 1, Math.sqrt(2.) };

   private int                  m_iNX, m_iNY;
   private IRasterLayer         m_Cost;
   private IRasterLayer         m_CostDir;
   private IRasterLayer         m_Features;
   private IRasterLayer         m_AccCost;
   private IRasterLayer         m_ClosestPoint;
   private IRasterLayer         m_Points, m_Points2;
   private ArrayList            m_CentralPoints, m_AdjPoints;
   private DeviationAndFactor[] m_Factors;


   @Override
   public void defineCharacteristics() {

      final String sColumnNames[] = new String[] { Sextante.getText("Difference"), Sextante.getText("Factor") };

      setName(Sextante.getText("Accumulated_cost__anisotropic") + "(B)");
      setGroup(Sextante.getText("Cost_distances_and_routes"));
      setUserCanDefineAnalysisExtent(true);
      setIsDeterminatedProcess(false);

      try {
         m_Parameters.addInputRasterLayer(COST, Sextante.getText("Maximum_unitary_cost"), true);
         m_Parameters.addInputRasterLayer(COSTDIR, Sextante.getText("Direction_of_maximum_cost_[degrees]"), true);
         m_Parameters.addInputRasterLayer(FEATURES, Sextante.getText("Origin-destination_points"), true);
         m_Parameters.addFixedTable(FACTORS, Sextante.getText("Cost_variation_factors"), sColumnNames, 5, false);
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

      m_Cost = m_Parameters.getParameterValueAsRasterLayer(COST);
      m_CostDir = m_Parameters.getParameterValueAsRasterLayer(COSTDIR);
      m_Features = m_Parameters.getParameterValueAsRasterLayer(FEATURES);

      final FixedTableModel factors = (FixedTableModel) m_Parameters.getParameterValueAsObject(FACTORS);

      m_Factors = new DeviationAndFactor[factors.getRowCount()];
      for (int i = 0; i < factors.getRowCount(); i++) {
         final double dDeviation = Double.parseDouble(factors.getValueAt(i, 0).toString());
         final double dFactor = Double.parseDouble(factors.getValueAt(i, 1).toString());
         m_Factors[i] = new DeviationAndFactor(dDeviation, dFactor);
      }

      Arrays.sort(m_Factors);

      m_AccCost = getNewRasterLayer(ACCCOST, Sextante.getText("Accumulated_cost"), IRasterLayer.RASTER_DATA_TYPE_DOUBLE);
      m_ClosestPoint = getNewRasterLayer(CLOSESTPOINT, Sextante.getText("Closest_points"), IRasterLayer.RASTER_DATA_TYPE_INT);

      final AnalysisExtent extent = m_AccCost.getWindowGridExtent();

      m_Cost.setWindowExtent(extent);
      m_Cost.setInterpolationMethod(IRasterLayer.INTERPOLATION_BSpline);

      m_CostDir.setWindowExtent(extent);
      m_CostDir.setInterpolationMethod(IRasterLayer.INTERPOLATION_BSpline);

      m_Features.setWindowExtent(extent);
      m_Features.setInterpolationMethod(IRasterLayer.INTERPOLATION_NearestNeighbour);

      m_iNX = m_Cost.getNX();
      m_iNY = m_Cost.getNY();

      m_Points = getTempRasterLayer(IRasterLayer.RASTER_DATA_TYPE_INT, extent);

      m_Points2 = getTempRasterLayer(IRasterLayer.RASTER_DATA_TYPE_INT, extent);

      m_Points.setNoDataValue(NO_DATA);
      m_Points.assignNoData();

      m_Points2.setNoDataValue(NO_DATA);
      m_Points2.assignNoData();

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

      int i;
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
            if (m_Points.getCellValueAsInt(x, y) == DATA) {
               m_Points.setCellValue(x, y, NO_DATA);
               for (i = 0; i < 8; i++) {
                  x2 = x + m_iOffsetX[i];
                  y2 = y + m_iOffsetY[i];
                  dAccCost = m_AccCost.getCellValueAsDouble(x, y);
                  dPrevAccCost = m_AccCost.getCellValueAsDouble(x2, y2);
                  if ((dPrevAccCost > dAccCost) || m_AccCost.isNoDataValue(dPrevAccCost)) {
                     final double dCostInDir = getCostInDir(x, y, m_iOffsetX[i], m_iOffsetY[i]);
                     if (dCostInDir != NO_DATA) {
                        dAccCost += dCostInDir * m_dDist[i];
                        if (m_AccCost.isNoDataValue(dPrevAccCost) || (dPrevAccCost > dAccCost)) {
                           m_AccCost.setCellValue(x2, y2, dAccCost);
                           m_ClosestPoint.setCellValue(x2, y2, iPoint);
                           if (m_Points2.getCellValueAsInt(x2, y2) == NO_DATA) {
                              m_Points2.setCellValue(x2, y2, DATA);
                              m_AdjPoints.add(new GridCell(x2, y2, iPoint));
                           }
                        }
                     }
                  }
               }
            }
         }

         final IRasterLayer swap = m_Points;
         m_Points = m_Points2;
         m_Points2 = swap;

         m_CentralPoints = m_AdjPoints;
         m_AdjPoints = new ArrayList();

         setProgressText(Integer.toString(m_AdjPoints.size()));

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
      double dCost1 = m_Cost.getCellValueAsDouble(x, y);
      double dCost2 = m_Cost.getCellValueAsDouble(x2, y2);

      if (m_Cost.isNoDataValue(dCost1) || m_Cost.isNoDataValue(dCost1) || m_CostDir.isNoDataValue(dCostDir1)
          || m_CostDir.isNoDataValue(dCostDir1)) {
         return NO_DATA;
      }
      else {
         final double dDifAngle1 = Math.abs(dCostDir1 - dAngle);
         final double dDifAngle2 = Math.abs(dCostDir2 - dAngle);

         dCost1 = getWeigthedCost(dCost1, dDifAngle1);
         dCost2 = getWeigthedCost(dCost2, dDifAngle2);

         return dCost1 + dCost2;
      }

   }


   private double getWeigthedCost(final double cost,
                                  final double difAngle) {

      for (int i = 0; i < m_Factors.length - 1; i++) {
         if ((m_Factors[i].deviation < difAngle) || (m_Factors[i + 1].deviation >= difAngle)) {
            return m_Factors[i].factor * cost / 2.;
         }
      }

      return cost / 2.;

   }

   private class DeviationAndFactor
            implements
               Comparable {

      public double factor;
      public double deviation;


      DeviationAndFactor(final double dDeviation,
                         final double dFactor) {

         deviation = dDeviation;
         factor = dFactor;
      }


      public int compareTo(final Object ob) throws ClassCastException {

         if (!(ob instanceof DeviationAndFactor)) {
            throw new ClassCastException();
         }

         final double dDeviation = ((DeviationAndFactor) ob).deviation;
         final double dDif = this.deviation - dDeviation;

         if (dDif > 0.0) {
            return 1;
         }
         else if (dDif < 0.0) {
            return -1;
         }
         else {
            return 0;
         }

      }
   }

}
