package es.unex.sextante.gridAnalysis.rectToPolar;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;

public class RectToPolarAlgorithm
         extends
            GeoAlgorithm {

   public static final String ANGLE = "ANGLE";
   public static final String DIST  = "DIST";
   public static final String X     = "X";
   public static final String Y     = "Y";

   int                        m_iNX, m_iNY;
   IRasterLayer               m_Distance;
   IRasterLayer               m_Angle;
   IRasterLayer               m_X;
   IRasterLayer               m_Y;


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Cartesian_to_polar"));
      setGroup(Sextante.getText("Cost_distances_and_routes"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputRasterLayer(X, Sextante.getText("X"), true);
         m_Parameters.addInputRasterLayer(Y, Sextante.getText("Y"), true);
         addOutputRasterLayer(ANGLE, Sextante.getText("Angle"));
         addOutputRasterLayer(DIST, Sextante.getText("Distance"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int x, y;
      double dAngle, dDist;
      double dX, dY;

      m_X = m_Parameters.getParameterValueAsRasterLayer(X);
      m_Y = m_Parameters.getParameterValueAsRasterLayer(Y);

      m_Distance = getNewRasterLayer(DIST, Sextante.getText("Distance"), IRasterLayer.RASTER_DATA_TYPE_DOUBLE);
      m_Angle = getNewRasterLayer(ANGLE, Sextante.getText("Angle"), IRasterLayer.RASTER_DATA_TYPE_DOUBLE);

      final AnalysisExtent extent = m_Distance.getWindowGridExtent();

      m_X.setWindowExtent(extent);
      m_Y.setWindowExtent(extent);

      m_iNX = extent.getNX();
      m_iNY = extent.getNY();

      for (y = 0; (y < m_iNY) && setProgress(y, m_iNY); y++) {
         for (x = 0; x < m_iNX; x++) {
            dX = m_X.getCellValueAsDouble(x, y);
            dY = m_Y.getCellValueAsDouble(x, y);
            if (!m_Y.isNoDataValue(dY) && !m_X.isNoDataValue(dX)) {
               dDist = Math.sqrt(dX * dX + dY * dY);
               dAngle = Math.atan(dY / dX);
               if (dX * dY > 0) {
                  if ((dY < 0) && (dX < 0)) {
                     dAngle += Math.PI;
                  }
               }
               else {
                  if (dY < 0) {
                     dAngle = 2 * Math.PI - dAngle;
                  }
                  else {
                     dAngle = Math.PI - dAngle;
                  }
               }
               m_Distance.setCellValue(x, y, dDist);
               m_Angle.setCellValue(x, y, dAngle);
            }
            else {
               m_Distance.setNoData(x, y);
               m_Angle.setNoData(x, y);
            }
         }
      }

      return !m_Task.isCanceled();

   }

}
