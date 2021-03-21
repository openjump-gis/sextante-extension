package es.unex.sextante.gridCalculus.generateTerrain;

import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;

public class GenerateTerrainAlgorithm
         extends
            GeoAlgorithm {

   public static final String RADIUS     = "RADIUS";
   public static final String ITERATIONS = "ITERATIONS";
   public static final String DEM        = "DEM";

   private int                m_iNX, m_iNY;
   private int                m_iRadius, m_iRadius2;

   private IRasterLayer       m_DEM;


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i;

      final int iIterations = m_Parameters.getParameterValueAsInt(ITERATIONS);
      m_iRadius = m_Parameters.getParameterValueAsInt(RADIUS);
      m_iRadius2 = (int) Math.pow(m_iRadius, 2.0);

      m_DEM = getNewRasterLayer(DEM, Sextante.getText("Elevation"), IRasterLayer.RASTER_DATA_TYPE_DOUBLE);

      m_iNX = m_DEM.getWindowGridExtent().getNX();
      m_iNY = m_DEM.getWindowGridExtent().getNY();

      m_DEM.assign(0.0);

      for (i = 0; i < iIterations; i++) {
         addBump();
         setProgress(i, iIterations);
      }

      return !m_Task.isCanceled();
   }


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Random_DEM"));
      setGroup(Sextante.getText("Raster_creation_tools"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addNumericalValue(RADIUS, Sextante.getText("Radius_[cells]"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_INTEGER, 100, 0, Integer.MAX_VALUE);
         m_Parameters.addNumericalValue(ITERATIONS, Sextante.getText("Iterations"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_INTEGER, 50, 0, Integer.MAX_VALUE);
         addOutputRasterLayer(DEM, Sextante.getText("Elevation"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   private void addBump() {

      int i, j;
      int x, y, x2, y2;
      int iDist;
      double dOffset;

      x = (int) (-m_iRadius + (Math.random() * (m_iNX + 2 * m_iRadius)));
      y = (int) (-m_iRadius + (Math.random() * (m_iNY + 2 * m_iRadius)));

      for (i = -m_iRadius; i < m_iRadius; i++) {
         for (j = -m_iRadius; j < m_iRadius; j++) {
            x2 = x + i;
            y2 = y + j;
            if (m_DEM.getWindowGridExtent().containsCell(x2, y2)) {
               iDist = ((i) * (i) + (j) * (j));
               if (iDist <= m_iRadius2) {
                  dOffset = Math.sqrt(m_iRadius2 - iDist);
                  m_DEM.addToCellValue(x2, y2, dOffset);
               }
            }
         }
      }

   }

}
