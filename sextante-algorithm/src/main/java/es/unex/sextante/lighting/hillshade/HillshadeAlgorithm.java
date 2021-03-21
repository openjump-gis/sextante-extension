package es.unex.sextante.lighting.hillshade;

import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;

public class HillshadeAlgorithm
         extends
            GeoAlgorithm {

   public static final String  METHOD                 = "METHOD";
   public static final String  DEM                    = "DEM";
   public static final String  AZIMUTH                = "AZIMUTH";
   public static final String  DECLINATION            = "DECLINATION";
   public static final String  EXAGGERATION           = "EXAGGERATION";
   public static final String  SHADED                 = "SHADED";

   public static final int     METHOD_STANDARD        = 0;
   public static final int     METHOD_STANDARD_MAX_90 = 1;
   public static final int     METHOD_COMBINED        = 2;
   public static final int     METHOD_RAY_TRACE       = 3;

   private static final double DEG_TO_RAD             = Math.PI / 180.;
   private static final double DEG_90_IN_RAD          = Math.PI / 2;
   private static final double DEG_180_IN_RAD         = Math.PI;

   private double              m_dExaggeration;
   private IRasterLayer        m_Hillshade;
   private IRasterLayer        m_Window               = null;


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int iMethod;
      double dAzimuth;
      double dDeclination;

      iMethod = m_Parameters.getParameterValueAsInt(METHOD);
      m_Window = m_Parameters.getParameterValueAsRasterLayer(DEM);
      m_Hillshade = getNewRasterLayer(SHADED, Sextante.getText("Shaded_relief"), IRasterLayer.RASTER_DATA_TYPE_FLOAT);
      dAzimuth = m_Parameters.getParameterValueAsDouble(AZIMUTH) * DEG_TO_RAD;
      dDeclination = m_Parameters.getParameterValueAsDouble(DECLINATION) * DEG_TO_RAD;
      m_dExaggeration = m_Parameters.getParameterValueAsDouble(EXAGGERATION);

      final AnalysisExtent extent = m_Hillshade.getWindowGridExtent();

      m_Window.setWindowExtent(extent);

      switch (iMethod) {
         case METHOD_STANDARD:
            getShading(dAzimuth, dDeclination, false, false);
            break;
         case METHOD_STANDARD_MAX_90:
            getShading(dAzimuth, dDeclination, true, false);
            break;
         case METHOD_COMBINED:
            getShading(dAzimuth, dDeclination, false, true);
            break;
         case METHOD_RAY_TRACE:
            rayTrace(dAzimuth, dDeclination);
            break;
      }

      return !m_Task.isCanceled();
   }


   @Override
   public void defineCharacteristics() {

      final String[] sMethod = { Sextante.getText("Standard"), Sextante.getText("Standard_max_90\u00b0"),
               Sextante.getText("Combined"), "Ray tracing" };

      setUserCanDefineAnalysisExtent(true);
      setGroup(Sextante.getText("Visibility_and_lighting"));
      setName(Sextante.getText("Shaded_relief"));

      try {
         m_Parameters.addInputRasterLayer(DEM, Sextante.getText("Elevation"), true);
         m_Parameters.addSelection(METHOD, Sextante.getText("Method"), sMethod);
         m_Parameters.addNumericalValue(DECLINATION, Sextante.getText("Sun_elevation_angle_[degrees]"), 45,
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE);
         m_Parameters.addNumericalValue(AZIMUTH, Sextante.getText("Sun_azimuthal_angle_[degrees]"), 315,
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE);
         m_Parameters.addNumericalValue(EXAGGERATION, Sextante.getText("Exaggeration"), 1,
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE);
         addOutputRasterLayer(SHADED, Sextante.getText("Shaded_relief"));
      }
      catch (final RepeatedParameterNameException e) {
         e.printStackTrace();
      }

   }


   private void getShading(final double dAzimuth,
                           final double dDeclination,
                           final boolean bDelimit,
                           final boolean bCombine) {

      int iNX, iNY;
      int x, y;
      double dSlope;
      double dAspect;
      double dShading;
      double dSinDec, dCosDec;

      iNX = m_Window.getNX();
      iNY = m_Window.getNY();

      for (y = 0; (y < iNY) && setProgress(y, iNY); y++) {
         for (x = 0; x < iNX; x++) {
            dSlope = m_Window.getSlope(x, y);
            dAspect = m_Window.getAspect(x, y);
            if (!m_Window.isNoDataValue(dSlope) && !m_Window.isNoDataValue(dAspect)) {
               dSinDec = Math.sin(dDeclination);
               dCosDec = Math.cos(dDeclination);
               dSlope = Math.tan(dSlope);
               dShading = DEG_90_IN_RAD - Math.atan(m_dExaggeration * dSlope);
               dShading = Math.acos(Math.sin(dShading) * dSinDec + Math.cos(dShading) * dCosDec * Math.cos(dAspect - dAzimuth));

               if (bDelimit && (dShading > DEG_90_IN_RAD)) {
                  dShading = DEG_90_IN_RAD;
               }

               if (bCombine) {
                  dShading *= dSlope / DEG_90_IN_RAD;
               }

               m_Hillshade.setCellValue(x, y, dShading);
            }
            else {
               m_Hillshade.setNoData(x, y);
            }
         }
      }

   }


   private void rayTrace(double dAzimuth,
                         final double dDeclination) {

      int iNX, iNY;
      int x, y, ix, iy, xStart, yStart, xStep, yStep;
      double dx, dy, dz;

      iNX = m_Window.getNX();
      iNY = m_Window.getNY();

      getShading(dAzimuth, dDeclination, true, false);

      dAzimuth = dAzimuth + DEG_180_IN_RAD;

      if (Math.sin(dAzimuth) > 0.0) {
         xStart = 0;
         xStep = 1;
      }
      else {
         xStart = iNX - 1;
         xStep = -1;
      }

      if (Math.cos(dAzimuth) > 0.0) {
         yStart = 0;
         yStep = 1;
      }
      else {
         yStart = iNY - 1;
         yStep = -1;
      }

      dx = Math.sin(dAzimuth);
      dy = Math.cos(dAzimuth);

      if (Math.abs(dx) > Math.abs(dy)) {
         dy /= Math.abs(dx);
         dx = dx < 0 ? -1 : 1;
      }
      else if (Math.abs(dy) > Math.abs(dx)) {
         dx /= Math.abs(dy);
         dy = dy < 0 ? -1 : 1;
      }
      else {
         dx = dx < 0 ? -1 : 1;
         dy = dy < 0 ? -1 : 1;
      }

      dz = Math.tan(dDeclination) * Math.sqrt(dx * dx + dy * dy) * m_Window.getWindowCellSize();

      for (iy = 0, y = yStart; (iy < iNY) && setProgress(iy, iNY); iy++, y += yStep) {
         for (ix = 0, x = xStart; ix < iNX; ix++, x += xStep) {
            rayTrace_Trace(x, y, dx, dy, dz);
         }

      }
   }


   private void rayTrace_Trace(int x,
                               int y,
                               final double dx,
                               final double dy,
                               final double dz) {

      double ix, iy, iz, iz2;

      iz = m_Window.getCellValueAsDouble(x, y);

      if (!m_Window.isNoDataValue(iz)) {
         for (ix = x + 0.5, iy = y + 0.5, iz = m_Window.getCellValueAsDouble(x, y);;) {
            ix += dx;
            iy += dy;
            iz -= dz;

            x = (int) ix;
            y = (int) iy;

            iz2 = m_Window.getCellValueAsDouble(x, y);

            if (m_Window.isNoDataValue(iz) || (iz2 > iz)) {
               break;
            }
            else if (!m_Window.isNoDataValue(iz)) {
               m_Hillshade.setCellValue(x, y, DEG_90_IN_RAD);
            }
         }
      }

   }

}
