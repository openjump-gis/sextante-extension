package es.unex.sextante.morphometry.convergence;


import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;

public class ConvergenceAlgorithm
         extends
            GeoAlgorithm {

   private final static int    m_iOffsetX[]   = { 0, 1, 1, 1, 0, -1, -1, -1 };
   private final static int    m_iOffsetY[]   = { 1, 1, 0, -1, -1, -1, 0, 1 };

   public static final String  DEM            = "DEM";
   public static final String  METHOD         = "METHOD";
   public static final String  RESULT         = "RESULT";

   private static final double DEG_45_IN_RAD  = Math.toRadians(45);
   private static final double DEG_90_IN_RAD  = Math.toRadians(90);
   private static final double DEG_180_IN_RAD = Math.toRadians(180);
   private static final double DEG_360_IN_RAD = Math.toRadians(360);

   IRasterLayer                m_Convergence;
   IRasterLayer                m_DEM;


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int x, y;
      int iNX, iNY;

      final int iMethod = m_Parameters.getParameterValueAsInt(METHOD);
      m_DEM = m_Parameters.getParameterValueAsRasterLayer(DEM);

      m_Convergence = getNewRasterLayer(RESULT, Sextante.getText("Convergence"), IRasterLayer.RASTER_DATA_TYPE_FLOAT);

      final AnalysisExtent extent = m_Convergence.getWindowGridExtent();

      m_DEM.setWindowExtent(extent);

      iNX = m_DEM.getNX();
      iNY = m_DEM.getNY();

      for (y = 0; (y < iNY) && setProgress(y, iNY); y++) {
         for (x = 0; x < iNX; x++) {
            switch (iMethod) {
               case 0:
                  Do_Aspect(x, y);
                  break;
               case 1:
                  Do_Gradient(x, y);
                  break;
            }
         }
      }


      return !m_Task.isCanceled();
   }


   @Override
   public void defineCharacteristics() {

      final String[] sMethod = { Sextante.getText("Aspect"), Sextante.getText("Gradient") };

      setName(Sextante.getText("Convergence_index"));
      setGroup(Sextante.getText("Geomorphometry_and_terrain_analysis"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputRasterLayer(DEM, Sextante.getText("Elevation"), true);
         m_Parameters.addSelection(METHOD, Sextante.getText("Method"), sMethod);
         addOutputRasterLayer(RESULT, Sextante.getText("Convergence"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   private void Do_Aspect(final int x,
                          final int y) {

      int i, n;
      double dAspect, dIAspect, d, dSum, z, z2;

      z = m_DEM.getCellValueAsDouble(x, y);

      if (m_DEM.isNoDataValue(z)) {
         m_Convergence.setNoData(x, y);
      }
      else {
         for (i = 0, n = 0, dSum = 0.0, dIAspect = -DEG_180_IN_RAD; i < 8; i++, dIAspect += DEG_45_IN_RAD) {
            z2 = m_DEM.getCellValueAsDouble(x + m_iOffsetX[i], y + m_iOffsetY[i]);
            if (!m_DEM.isNoDataValue(z2)) {
               dAspect = m_DEM.getAspect(x + m_iOffsetX[i], y + m_iOffsetY[i]);
               if (!m_DEM.isNoDataValue(dAspect) && (dAspect >= 0)) {
                  d = dAspect - dIAspect;
                  d = d % DEG_360_IN_RAD;
                  if (d < -DEG_180_IN_RAD) {
                     d += DEG_360_IN_RAD;
                  }
                  else if (d > DEG_180_IN_RAD) {
                     d -= DEG_360_IN_RAD;
                  }

                  dSum += Math.abs(d);
                  n++;
               }
            }
         }
         double res = dSum / n;
         res = (res - DEG_90_IN_RAD);
         res = res * 100.0 / DEG_90_IN_RAD;

         m_Convergence.setCellValue(x, y, n > 0 ? (dSum / n - DEG_90_IN_RAD) * 100.0 / DEG_90_IN_RAD : 0.0);
      }
   }


   private void Do_Gradient(final int x,
                            final int y) {

      int i, n;
      double dAspect, dIAspect, dSlope, dISlope, d, dSum, z, z2;

      z = m_DEM.getCellValueAsDouble(x, y);

      if (m_DEM.isNoDataValue(z)) {
         m_Convergence.setNoData(x, y);
      }
      else {
         for (i = 0, n = 0, dSum = 0.0, dIAspect = -DEG_180_IN_RAD; i < 8; i++, dIAspect += DEG_45_IN_RAD) {
            z2 = m_DEM.getCellValueAsDouble(x + m_iOffsetX[i], y + m_iOffsetY[i]);
            if (!m_DEM.isNoDataValue(z2)) {
               dSlope = m_DEM.getSlope(x + m_iOffsetX[i], y + m_iOffsetY[i]);
               dAspect = m_DEM.getAspect(x + m_iOffsetX[i], y + m_iOffsetY[i]);
               if (!m_DEM.isNoDataValue(dAspect) && (dAspect >= 0)) {
                  dISlope = Math.atan((z2 - z) / m_DEM.getDistToNeighborInDir(i));
                  d = Math.acos(Math.sin(dSlope) * Math.sin(dISlope) + Math.cos(dSlope) * Math.cos(dISlope)
                                * Math.cos(dIAspect - dAspect));
                  d = d % DEG_360_IN_RAD;

                  if (d < -DEG_180_IN_RAD) {
                     d += DEG_360_IN_RAD;
                  }
                  else if (d > DEG_180_IN_RAD) {
                     d -= DEG_360_IN_RAD;
                  }

                  dSum += Math.abs(d);
                  n++;

               }
            }
         }
         m_Convergence.setCellValue(x, y, n > 0 ? (dSum / n - DEG_90_IN_RAD) * 100.0 / DEG_90_IN_RAD : 0.0);
      }
   }

}
