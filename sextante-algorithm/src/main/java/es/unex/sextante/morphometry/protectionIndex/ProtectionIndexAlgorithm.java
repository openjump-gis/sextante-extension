package es.unex.sextante.morphometry.protectionIndex;

import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;

public class ProtectionIndexAlgorithm
         extends
            GeoAlgorithm {

   private final static int   m_iOffsetX[] = { 0, 1, 1, 1, 0, -1, -1, -1 };
   private final static int   m_iOffsetY[] = { 1, 1, 0, -1, -1, -1, 0, 1 };

   public static final String DEM          = "DEM";
   public static final String RADIUS       = "RADIUS";
   public static final String RESULT       = "RESULT";

   double                     m_dRadius;
   IRasterLayer               m_ProtectionIndex;
   IRasterLayer               m_Window     = null;


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int x, y;
      int iNX, iNY;

      m_Window = m_Parameters.getParameterValueAsRasterLayer(DEM);
      m_dRadius = m_Parameters.getParameterValueAsDouble(RADIUS);
      m_ProtectionIndex = getNewRasterLayer(RESULT, Sextante.getText("Protection_index"), IRasterLayer.RASTER_DATA_TYPE_FLOAT);

      final AnalysisExtent extent = m_ProtectionIndex.getWindowGridExtent();

      m_Window.setWindowExtent(extent);

      iNX = m_Window.getNX();
      iNY = m_Window.getNY();

      for (y = 0; (y < iNY) && setProgress(y, iNY); y++) {
         for (x = 0; x < iNX; x++) {
            setProtectionIndex(x, y);
         }
      }

      return !m_Task.isCanceled();

   }


   @Override
   public void defineCharacteristics() {

      setUserCanDefineAnalysisExtent(true);
      setGroup(Sextante.getText("Geomorphometry_and_terrain_analysis"));
      setName(Sextante.getText("Protection_index"));

      try {
         m_Parameters.addInputRasterLayer(DEM, Sextante.getText("Elevation"), true);
         m_Parameters.addNumericalValue(RADIUS, Sextante.getText("Radius"), AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE,
                  1000, 0, Double.MAX_VALUE);
         addOutputRasterLayer(RESULT, Sextante.getText("Protection_index"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   private void setProtectionIndex(final int x,
                                   final int y) {

      int i, j;
      double z, z2;
      double dDifHeight;
      double dDist;
      double dAngle;
      double dProtectionIndex = 0;
      double dMaxAngle;

      z = m_Window.getCellValueAsDouble(x, y);

      for (i = 0; i < 8; i++) {
         j = 1;
         dMaxAngle = 0;
         dDist = m_Window.getDistToNeighborInDir(i) * j;
         while (dDist < m_dRadius) {
            z2 = m_Window.getCellValueAsDouble(x + m_iOffsetX[i] * j, y + m_iOffsetY[i] * j);
            if (m_Window.isNoDataValue(z2)) {
               m_ProtectionIndex.setNoData(x, y);
               return;
            }
            dDifHeight = z2 - z;
            dAngle = Math.atan(dDifHeight / dDist);
            if (dMaxAngle < dAngle) {
               dMaxAngle = dAngle;
            }
            j++;
            dDist = m_Window.getDistToNeighborInDir(i) * j;
         }
         dProtectionIndex += dMaxAngle;
      }

      m_ProtectionIndex.setCellValue(x, y, dProtectionIndex / 8.);

   }

}
