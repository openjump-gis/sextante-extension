

package es.unex.sextante.gridStatistics.base;

import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;


;

public abstract class NeighborhoodStatsBaseAlgorithm
         extends
            GeoAlgorithm {

   private static final String LAYER        = "LAYER";
   private static final String RADIUS       = "RADIUS";
   private static final String FORCE_NODATA = "NODATA";
   private static final String RESULT       = "RESULT";

   protected double            NO_DATA;

   private int                 m_iRadius;
   private boolean             m_bForceNoData;
   private boolean             m_bIsValidCell[][];
   private IRasterLayer        m_window;
   protected double            m_dValues[];


   @Override
   public void defineCharacteristics() {

      setUserCanDefineAnalysisExtent(false);

      try {
         m_Parameters.addInputRasterLayer(LAYER, Sextante.getText("Layer"), true);
         m_Parameters.addNumericalValue(RADIUS, Sextante.getText("Radius"), AdditionalInfoNumericalValue.NUMERICAL_VALUE_INTEGER,
                  1, 1, 20);
         m_Parameters.addBoolean(FORCE_NODATA, Sextante.getText("Force_no-data_value"), true);
         addOutputRasterLayer(RESULT, this.getName());
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int x, y;
      int iNX, iNY;
      int iValidCells = 0;

      NO_DATA = m_OutputFactory.getDefaultNoDataValue();

      m_iRadius = m_Parameters.getParameterValueAsInt(RADIUS);
      m_window = m_Parameters.getParameterValueAsRasterLayer(LAYER);
      m_bForceNoData = m_Parameters.getParameterValueAsBoolean(FORCE_NODATA);

      m_window.setFullExtent();

      final IRasterLayer result = getNewRasterLayer(RESULT, this.getName(), IRasterLayer.RASTER_DATA_TYPE_DOUBLE,
               m_window.getWindowGridExtent());

      result.setNoDataValue(NO_DATA);
      result.assignNoData();

      iNX = m_window.getNX();
      iNY = m_window.getNY();

      m_bIsValidCell = new boolean[2 * m_iRadius + 1][2 * m_iRadius + 1];

      for (y = -m_iRadius; y < m_iRadius + 1; y++) {
         for (x = -m_iRadius; x < m_iRadius + 1; x++) {
            final double dDist = Math.sqrt(x * x + y * y);
            if (dDist <= m_iRadius) {
               m_bIsValidCell[x + m_iRadius][y + m_iRadius] = true;
               iValidCells++;
            }
            else {
               m_bIsValidCell[x + m_iRadius][y + m_iRadius] = false;
            }
         }
      }

      m_dValues = new double[iValidCells];

      for (y = 0; (y < iNY) && setProgress(y, iNY); y++) {
         for (x = 0; x < iNX; x++) {
            if (setNeighborhoodValues(x, y)) {
               result.setCellValue(x, y, processValues());
            }
            else {
               result.setNoData(x, y);
            }
         }
      }

      return !m_Task.isCanceled();

   }


   private boolean setNeighborhoodValues(final int iX,
                                         final int iY) {

      int x, y;
      int iCell = 0;
      double dValue;

      for (y = -m_iRadius; y < m_iRadius + 1; y++) {
         for (x = -m_iRadius; x < m_iRadius + 1; x++) {
            if (m_bIsValidCell[x + m_iRadius][y + m_iRadius]) {
               dValue = m_window.getCellValueAsDouble(iX + x, iY + y);
               if (!m_window.isNoDataValue(dValue)) {
                  m_dValues[iCell] = dValue;
               }
               else {
                  if (m_bForceNoData) {
                     return false;
                  }
                  else {
                     m_dValues[iCell] = NO_DATA;
                  }
               }
               iCell++;
            }
         }
      }

      return true;

   }


   protected abstract double processValues();


}
