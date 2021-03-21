package es.unex.sextante.gridAnalysis.polarToRect;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;

public class PolarToRectAlgorithm
         extends
            GeoAlgorithm {

   public static final String ANGLE    = "ANGLE";
   public static final String DISTANCE = "DISTANCE";
   public static final String X        = "X";
   public static final String Y        = "Y";

   private int                m_iNX, m_iNY;
   private IRasterLayer       m_Distance;
   private IRasterLayer       m_Angle;
   private IRasterLayer       m_X;
   private IRasterLayer       m_Y;


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Polar_to_Cartesian"));
      setGroup(Sextante.getText("Cost_distances_and_routes"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputRasterLayer(ANGLE, Sextante.getText("Angle"), true);
         m_Parameters.addInputRasterLayer(DISTANCE, Sextante.getText("Distance"), true);
         addOutputRasterLayer(X, "X");
         addOutputRasterLayer(Y, "Y");
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

      m_Distance = m_Parameters.getParameterValueAsRasterLayer(DISTANCE);
      m_Angle = m_Parameters.getParameterValueAsRasterLayer(ANGLE);

      m_X = getNewRasterLayer(X, "X", IRasterLayer.RASTER_DATA_TYPE_DOUBLE);
      m_Y = getNewRasterLayer(Y, "Y", IRasterLayer.RASTER_DATA_TYPE_DOUBLE);

      final AnalysisExtent extent = m_X.getWindowGridExtent();

      m_Distance.setWindowExtent(extent);
      m_Angle.setWindowExtent(extent);

      m_iNX = extent.getNX();
      m_iNY = extent.getNY();

      for (y = 0; (y < m_iNY) && setProgress(y, m_iNY); y++) {
         for (x = 0; x < m_iNX; x++) {
            dDist = m_Distance.getCellValueAsDouble(x, y);
            dAngle = m_Angle.getCellValueAsDouble(x, y);
            if (!m_Distance.isNoDataValue(dDist) && !m_Angle.isNoDataValue(dAngle)) {
               dX = Math.cos(dAngle) * dDist;
               dY = Math.sin(dAngle) * dDist;
               m_X.setCellValue(x, y, dX);
               m_Y.setCellValue(x, y, dY);
            }
            else {
               m_X.setNoData(x, y);
               m_Y.setNoData(x, y);
            }
         }
      }

      return !m_Task.isCanceled();

   }


}
