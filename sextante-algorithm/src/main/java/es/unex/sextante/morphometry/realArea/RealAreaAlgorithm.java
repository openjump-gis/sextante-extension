package es.unex.sextante.morphometry.realArea;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;

public class RealAreaAlgorithm
         extends
            GeoAlgorithm {

   public static final String DEM      = "DEM";
   public static final String REALAREA = "REALAREA";

   IRasterLayer               m_RealArea;
   IRasterLayer               m_DEM    = null;
   private double             m_DX2;


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int x, y;
      int iNX, iNY;

      m_DEM = m_Parameters.getParameterValueAsRasterLayer(DEM);
      m_RealArea = getNewRasterLayer(REALAREA, Sextante.getText("Real_area"), IRasterLayer.RASTER_DATA_TYPE_FLOAT);


      final AnalysisExtent extent = m_RealArea.getWindowGridExtent();

      m_DEM.setWindowExtent(extent);

      iNX = m_DEM.getNX();
      iNY = m_DEM.getNY();

      m_DX2 = m_DEM.getWindowCellSize() * m_DEM.getWindowCellSize();

      for (y = 0; (y < iNY) && setProgress(y, iNY); y++) {
         for (x = 0; x < iNX; x++) {
            setRealArea(x, y);
         }
      }

      return !m_Task.isCanceled();
   }


   @Override
   public void defineCharacteristics() {

      setUserCanDefineAnalysisExtent(true);
      setGroup(Sextante.getText("Geomorphometry_and_terrain_analysis"));
      setName(Sextante.getText("Real_area"));

      try {
         m_Parameters.addInputRasterLayer(DEM, Sextante.getText("Elevation"), true);
         addOutputRasterLayer(REALAREA, Sextante.getText("Real_area"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   private void setRealArea(final int x,
                            final int y) {

      final double dSlope = m_DEM.getSlope(x, y);
      double dArea;

      if (!m_DEM.isNoDataValue(dSlope)) {
         dArea = m_DX2 / Math.cos(dSlope);
         m_RealArea.setCellValue(x, y, dArea);
      }
      else {
         m_RealArea.setNoData(x, y);
      }

   }

}
