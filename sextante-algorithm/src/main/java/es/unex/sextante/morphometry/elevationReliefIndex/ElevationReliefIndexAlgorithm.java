package es.unex.sextante.morphometry.elevationReliefIndex;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;

public class ElevationReliefIndexAlgorithm
         extends
            GeoAlgorithm {

   private final static int   m_iOffsetX[] = { 0, 1, 1, 1, 0, -1, -1, -1 };
   private final static int   m_iOffsetY[] = { 1, 1, 0, -1, -1, -1, 0, 1 };

   public static final String DEM          = "DEM";
   public static final String RESULT       = "RESULT";

   IRasterLayer               m_ERIndex;
   IRasterLayer               m_DEM        = null;


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int x, y;
      int iNX, iNY;

      m_DEM = m_Parameters.getParameterValueAsRasterLayer(DEM);

      m_ERIndex = getNewRasterLayer(RESULT, Sextante.getText("Elevation-relief_ratio"), IRasterLayer.RASTER_DATA_TYPE_FLOAT);

      final AnalysisExtent extent = m_ERIndex.getWindowGridExtent();

      m_DEM.setWindowExtent(extent);

      iNX = m_DEM.getNX();
      iNY = m_DEM.getNY();

      for (y = 0; (y < iNY) && setProgress(y, iNY); y++) {
         for (x = 0; x < iNX; x++) {
            calculateERIndex(x, y);
         }

      }

      return !m_Task.isCanceled();

   }


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Elevation-relief_ratio"));
      setGroup(Sextante.getText("Geomorphometry_and_terrain_analysis"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputRasterLayer(DEM, Sextante.getText("Elevation"), true);
         addOutputRasterLayer(RESULT, Sextante.getText("Elevation-relief_ratio"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   private void calculateERIndex(final int x,
                                 final int y) {

      int i;
      int iCells;
      double z, dAvgZ;
      double dMaxZ, dMinZ;
      double dIndex;

      z = m_DEM.getCellValueAsDouble(x, y);

      if (m_DEM.isNoDataValue(z)) {
         m_ERIndex.setNoData(x, y);
      }
      else {
         dMaxZ = dMinZ = z;
         dAvgZ = z;
         iCells = 0;
         for (i = 0; i < 8; i++) {
            z = m_DEM.getCellValueAsDouble(x + m_iOffsetX[i], y + m_iOffsetY[i]);
            if (!m_DEM.isNoDataValue(z)) {
               iCells++;
               dAvgZ += z;
               dMinZ = Math.min(dMinZ, z);
               dMaxZ = Math.max(dMaxZ, z);
            }
         }

         if (iCells == 0) {
            m_ERIndex.setNoData(x, y);
         }
         else {
            if (dMaxZ != dMinZ) {
               dIndex = (dAvgZ - dMinZ) / (dMaxZ - dMinZ);
               m_ERIndex.setCellValue(x, y, dIndex);
            }
            else {
               m_ERIndex.setNoData(x, y);
            }
         }
      }
   }


}
