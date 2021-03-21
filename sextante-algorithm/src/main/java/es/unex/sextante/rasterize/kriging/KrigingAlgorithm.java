package es.unex.sextante.rasterize.kriging;

import java.awt.geom.Point2D;
import java.util.Arrays;

import Jama.Matrix;
import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.closestpts.Point3D;
import es.unex.sextante.closestpts.PtAndDistance;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.rasterWrappers.GridCell;
import es.unex.sextante.rasterize.interpolationBase.BaseInterpolationAlgorithm;

public class KrigingAlgorithm
         extends
            BaseInterpolationAlgorithm {

   public static final String SILL            = "SILL";
   public static final String RANGE           = "RANGE";
   public static final String NUGGET          = "NUGGET";
   public static final String MODEL           = "MODEL";
   public static final String MINPOINTS       = "MINPOINTS";
   public static final String MAXPOINTS       = "MAXPOINTS";
   public static final String VARIANCE        = "VARIANCE";
   public static final String CROSSVALIDATION = "CROSSVALIDATION";

   private double             m_dNugget;
   private double             m_dScale;
   private double             m_dRange;
   private double             m_dGammas[];
   private int                m_iMinPoints;
   private int                m_iMaxPoints;
   private int                m_iModel;
   private boolean            m_bCreateVarianceLayer;
   private double             m_dWeights[][];
   private Matrix             m_Matrix;
   private IRasterLayer       m_Variance;


   @Override
   public void defineCharacteristics() {

      super.defineCharacteristics();

      setGroup(Sextante.getText("Rasterization_and_interpolation"));
      setName(Sextante.getText("Kriging"));

      final String sModels[] = { Sextante.getText("Spherical"), Sextante.getText("Exponential"), Sextante.getText("Gaussian") };

      try {
         m_Parameters.addNumericalValue(MINPOINTS, Sextante.getText("Mino_number_of_points"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_INTEGER, 4, 1, Integer.MAX_VALUE);
         m_Parameters.addNumericalValue(MAXPOINTS, Sextante.getText("Max_number_of_points"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_INTEGER, 25, 1, Integer.MAX_VALUE);
         m_Parameters.addSelection(MODEL, Sextante.getText("Model"), sModels);
         m_Parameters.addNumericalValue(NUGGET, Sextante.getText("Nugget"), AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE,
                  0, 0, Double.MAX_VALUE);
         m_Parameters.addNumericalValue(SILL, Sextante.getText("Sill"), AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE, 10,
                  0, Double.MAX_VALUE);
         m_Parameters.addNumericalValue(RANGE, Sextante.getText("Range"), AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE,
                  100, 0, Double.MAX_VALUE);
         addOutputTable(CROSSVALIDATION, Sextante.getText("Cross_validation"));
         addOutputRasterLayer("VARIANCE", Sextante.getText("Variances"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   protected void setValues() throws GeoAlgorithmExecutionException {

      super.setValues();

      m_iMaxPoints = m_Parameters.getParameterValueAsInt(MAXPOINTS);
      m_iMinPoints = m_Parameters.getParameterValueAsInt(MINPOINTS);
      m_iModel = m_Parameters.getParameterValueAsInt(MODEL);
      m_dNugget = m_Parameters.getParameterValueAsDouble(NUGGET);
      m_dScale = m_Parameters.getParameterValueAsDouble(SILL) - m_dNugget;
      m_dRange = m_Parameters.getParameterValueAsDouble(RANGE);
      m_bCreateVarianceLayer = true; //m_Parameters.getParameterValueAsBoolean("VARIANCE");
      m_dWeights = new double[m_iMaxPoints + 1][m_iMaxPoints + 1];
      m_Matrix = new Matrix(m_dWeights);
      m_dGammas = new double[m_iMaxPoints + 1];
      if (m_bCreateVarianceLayer) {
         m_Variance = getNewRasterLayer(VARIANCE, m_Layer.getName() + Sextante.getText("[variances]"),
                  IRasterLayer.RASTER_DATA_TYPE_DOUBLE);
      }


   }


   @Override
   protected double getValueAt(final int x,
                               final int y) {

      final Point2D pt = m_AnalysisExtent.getWorldCoordsFromGridCoords(new GridCell(x, y, 0));
      final PtAndDistance[] nearestPoints = m_SearchEngine.getClosestPoints(pt.getX(), pt.getY(), m_dDistance);

      Arrays.sort(nearestPoints);

      final int n = Math.min(nearestPoints.length, m_iMaxPoints);
      m_NearestPoints = new PtAndDistance[n];
      System.arraycopy(nearestPoints, 0, m_NearestPoints, 0, n);

      final double dValue = interpolate(pt.getX(), pt.getY());

      return dValue;

   }


   @Override
   protected double getValueAt(final double x,
                               final double y) {

      try {
         final PtAndDistance[] nearestPoints = m_SearchEngine.getClosestPoints(x, y, m_dDistance);
         Arrays.sort(nearestPoints);
         final int n = Math.min(nearestPoints.length, m_iMaxPoints) - 1;
         m_NearestPoints = new PtAndDistance[n];
         System.arraycopy(nearestPoints, 1, m_NearestPoints, 0, n);

         return interpolate(x, y);
      }
      catch (final Exception e) {
         return NO_DATA;
      }

   }


   @Override
   protected double interpolate(final double x,
                                final double y) {

      int i, j, nPoints;
      double dLambda, dValue, dVariance;
      Point3D pt;

      if ((nPoints = getWeights(x, y)) >= m_iMinPoints) {
         for (i = 0; i < nPoints; i++) {
            m_dGammas[i] = getWeight(m_NearestPoints[i].getDist());
         }

         m_dGammas[nPoints] = 1.0;

         for (i = 0, dValue = 0.0, dVariance = 0.0; i < nPoints; i++) {
            pt = m_NearestPoints[i].getPt();
            for (j = 0, dLambda = 0.0; j <= nPoints; j++) {
               dLambda += m_dWeights[i][j] * m_dGammas[j];
            }

            dValue += dLambda * pt.getZ();

            if (m_bCreateVarianceLayer) {
               dVariance += dLambda * m_dGammas[i];
            }
         }

         if (m_bCreateVarianceLayer) {
            final GridCell cell = m_AnalysisExtent.getGridCoordsFromWorldCoords(x, y);
            m_Variance.setCellValue(cell.getX(), cell.getY(), dVariance);
         }

         return dValue;
      }

      if (m_bCreateVarianceLayer) {
         final GridCell cell = m_AnalysisExtent.getGridCoordsFromWorldCoords(x, y);
         m_Variance.setNoData(cell.getX(), cell.getY());
      }

      return NO_DATA;
   }


   private double getWeight(double d) {

      if (d == 0.0) {
         d = 0.0001;
      }

      switch (m_iModel) {
         case 0: // Spherical Model
            if (d >= m_dRange) {
               d = m_dNugget + m_dScale;
            }
            else {
               d = m_dNugget + m_dScale * (3 * d / (2 * m_dRange) - d * d * d / (2 * m_dRange * m_dRange * m_dRange));
            }
            break;

         case 1: // Exponential Model
            d = m_dNugget + m_dScale * (1 - Math.exp(-3 * d / m_dRange));
            break;

         case 2: // Gaussian Model
            d = 1 - Math.exp(-3 * d / (m_dRange * m_dRange));
            d = m_dNugget + m_dScale * d * d;
            break;
      }

      return (d);
   }


   private int getWeights(final double x,
                          final double y) {

      int i, j, n;
      double dx, dy;
      Point3D pt, pt2;

      m_dWeights = m_Matrix.getArray();

      if ((n = Math.min(m_NearestPoints.length, m_iMaxPoints)) >= m_iMinPoints) {
         //n = Math.min(n, m_iMaxPoints);
         for (i = 0; i < n; i++) {
            pt = m_NearestPoints[i].getPt();
            m_dWeights[i][i] = 0.0;
            m_dWeights[i][n] = m_dWeights[n][i] = 1.0;
            for (j = i + 1; j < n; j++) {
               pt2 = m_NearestPoints[j].getPt();
               dx = pt.getX() - pt2.getX();
               dy = pt.getY() - pt2.getY();
               m_dWeights[i][j] = m_dWeights[j][i] = getWeight(Math.sqrt(dx * dx + dy * dy));
            }
         }

         m_dWeights[n][n] = 0.0;

         final Matrix subMatrix = m_Matrix.getMatrix(0, n, 0, n);

         try {
            final Matrix inverse = subMatrix.inverse();
            m_Matrix.setMatrix(0, n, 0, n, inverse);
         }
         catch (final RuntimeException e) {
            return 0;
         }

         //m_dWeights = inverse.getArray();

      }

      return n;


   }


}
