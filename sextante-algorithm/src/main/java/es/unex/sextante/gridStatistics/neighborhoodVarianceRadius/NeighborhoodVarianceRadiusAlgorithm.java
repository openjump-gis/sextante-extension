

package es.unex.sextante.gridStatistics.neighborhoodVarianceRadius;

import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;


public class NeighborhoodVarianceRadiusAlgorithm
         extends
            GeoAlgorithm {

   public static final String LAYER           = "LAYER";
   public static final String MAXRADIUS       = "MAXRADIUS";
   public static final String STDDEV          = "STDDEV";
   public static final String UNITS           = "UNITS";
   public static final String RESULT          = "RESULT";

   public static final int    UNITS_CELLS     = 0;
   public static final int    UNITS_MAP_UNITS = 1;

   protected double           NO_DATA;

   private int                m_iNX, m_iNY;
   private int                m_iMaxRadius;
   private int                m_Check[][];
   private double             m_dStopVariance;
   private boolean            m_bWriteGridsize;
   private IRasterLayer       m_Window;
   private IRasterLayer       m_InputQ;


   @Override
   public void defineCharacteristics() {

      final String sMethod[] = { Sextante.getText("Cells"), Sextante.getText("Map_units") };
      setUserCanDefineAnalysisExtent(false);

      setName(Sextante.getText("Radius_of_variance"));
      setGroup(Sextante.getText("Geostatistics"));

      try {
         m_Parameters.addInputRasterLayer(LAYER, Sextante.getText("Layer"), true);
         m_Parameters.addNumericalValue(MAXRADIUS, Sextante.getText("Maximum_radius__cells"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_INTEGER, 20, 2, Integer.MAX_VALUE);
         m_Parameters.addNumericalValue(STDDEV, Sextante.getText("Standard_deviation"), 1.0,
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE);
         m_Parameters.addSelection(UNITS, Sextante.getText("Units"), sMethod);
         addOutputRasterLayer(RESULT, this.getName());
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int x, y;

      NO_DATA = m_OutputFactory.getDefaultNoDataValue();

      m_dStopVariance = m_Parameters.getParameterValueAsDouble(STDDEV);
      m_dStopVariance *= m_dStopVariance;
      m_iMaxRadius = m_Parameters.getParameterValueAsInt(MAXRADIUS);
      m_bWriteGridsize = (m_Parameters.getParameterValueAsInt(UNITS) == 1);
      m_Window = m_Parameters.getParameterValueAsRasterLayer(LAYER);

      m_Window.setFullExtent();

      final IRasterLayer result = getNewRasterLayer(RESULT, this.getName(), IRasterLayer.RASTER_DATA_TYPE_DOUBLE,
               m_Window.getWindowGridExtent());

      m_InputQ = getTempRasterLayer(IRasterLayer.RASTER_DATA_TYPE_DOUBLE, m_Window.getWindowGridExtent());

      result.setNoDataValue(NO_DATA);
      result.assignNoData();

      m_iNX = m_Window.getNX();
      m_iNY = m_Window.getNY();

      initialize();

      for (y = 0; (y < m_iNY) && setProgress(y, m_iNY); y++) {
         for (x = 0; x < m_iNX; x++) {
            result.setCellValue(x, y, getRadius(x, y));
         }

      }

      return !m_Task.isCanceled();

   }


   private void initialize() {

      int x, y;
      double d;

      for (y = 0; y < m_iNY; y++) {
         for (x = 0; x < m_iNX; x++) {
            d = m_Window.getCellValueAsDouble(x, y);
            m_InputQ.setCellValue(x, y, d * d);
         }
      }

      m_Check = new int[m_iMaxRadius + 1][m_iMaxRadius + 1];

      for (y = 0; y <= m_iMaxRadius; y++) {
         for (x = 0; x <= m_iMaxRadius; x++) {
            m_Check[y][x] = (int) Math.sqrt((x + 0.5) * (x + 0.5) + (y + 0.5) * (y + 0.5));
         }
      }
   }


   private double getRadius(final int xPoint,
                            final int yPoint) {

      final double sqrt2 = 1.0 / Math.sqrt(2.0);

      int x, y, dx, dy, sRadius, Radius = 0, nValues = 0;

      double ArithMean, Variance, Sum = 0.0, SumQ = 0.0;

      do {
         sRadius = (int) (sqrt2 * Radius - 4.0);
         if (sRadius < 0) {
            sRadius = 0;
         }

         for (dy = sRadius; dy <= Radius; dy++) {
            for (dx = sRadius; dx <= Radius; dx++) {
               if (m_Check[dy][dx] == Radius) {
                  y = yPoint - dy;
                  if (y >= 0) {
                     x = xPoint - dx;
                     if (x >= 0) {
                        Sum += m_Window.getCellValueAsDouble(x, y);
                        SumQ += m_InputQ.getCellValueAsDouble(x, y);
                        nValues++;
                     }

                     x = xPoint + dx;
                     if (x < m_iNX) {
                        Sum += m_Window.getCellValueAsDouble(x, y);
                        SumQ += m_InputQ.getCellValueAsDouble(x, y);
                        nValues++;
                     }
                  }

                  y = yPoint + dy;
                  if (y < m_iNY) {
                     x = xPoint - dx;
                     if (x >= 0) {
                        Sum += m_Window.getCellValueAsDouble(x, y);
                        SumQ += m_InputQ.getCellValueAsDouble(x, y);
                        nValues++;
                     }

                     x = xPoint + dx;
                     if (x < m_iNX) {
                        Sum += m_Window.getCellValueAsDouble(x, y);
                        SumQ += m_InputQ.getCellValueAsDouble(x, y);
                        nValues++;
                     }
                  }
               }
            }
         }

         if (nValues != 0) {
            ArithMean = Sum / nValues;
            Variance = SumQ / nValues - ArithMean * ArithMean;
         }
         else {
            Variance = 0;
         }

         Radius++;
      }
      while ((Variance < m_dStopVariance) && (Radius <= m_iMaxRadius));

      return (m_bWriteGridsize ? Radius : Radius * m_Window.getWindowCellSize());

   }


}
