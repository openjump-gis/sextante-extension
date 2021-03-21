package es.unex.sextante.lighting.visualExposure;

import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.RepeatedParameterNameException;

public class VisualExposureAlgorithm
         extends
            GeoAlgorithm {

   public static final String DEM        = "DEM";
   public static final String FEATURES   = "FEATURES";
   public static final String WEIGHTS    = "WEIGHTS";
   public static final String METHOD     = "METHOD";
   public static final String RADIUS     = "RADIUS";
   public static final String RESULT     = "RESULT";

   private int                m_iNX, m_iNY;
   private int                m_iMethod;
   private int                m_iRadius;
   private int                m_iRadius2;

   private IRasterLayer       m_Exposure;
   private IRasterLayer       m_DEM      = null;
   private IRasterLayer       m_Features = null;
   private IRasterLayer       m_Weights  = null;


   @Override
   public boolean processAlgorithm() {

      int x, y;
      double dValue;

      try {
         m_iMethod = m_Parameters.getParameterValueAsInt(METHOD);
         m_DEM = m_Parameters.getParameterValueAsRasterLayer(DEM);
         m_Weights = m_Parameters.getParameterValueAsRasterLayer(WEIGHTS);
         m_Features = m_Parameters.getParameterValueAsRasterLayer(FEATURES);
         m_Exposure = getNewRasterLayer(RESULT, Sextante.getText("Exposure"), IRasterLayer.RASTER_DATA_TYPE_INT);

         //GridExtent extent = m_Exposure.getGridExtent();

         m_DEM.setWindowExtent(getAnalysisExtent());
         m_Weights.setWindowExtent(getAnalysisExtent());
         m_Features.setWindowExtent(getAnalysisExtent());
         m_Features.setInterpolationMethod(IRasterLayer.INTERPOLATION_NearestNeighbour);

         m_iRadius = (int) Math.ceil(m_Parameters.getParameterValueAsDouble("RADIUS") / m_DEM.getWindowCellSize());
         m_iRadius2 = (int) Math.ceil(Math.pow(m_Parameters.getParameterValueAsDouble("RADIUS"), 2.0)
                                      / Math.pow(m_DEM.getWindowCellSize(), 2.0));

         m_Exposure.assign(0.0);

         m_iNX = m_DEM.getNX();
         m_iNY = m_DEM.getNY();

         for (y = 0; (y < m_iNY) && setProgress(y, m_iNY); y++) {
            for (x = 0; x < m_iNX; x++) {
               dValue = m_Features.getCellValueAsDouble(x, y);
               if (!m_Features.isNoDataValue(dValue) && (dValue != 0)) {
                  if (m_iMethod == 0) {
                     Irradiate(x, y);
                  }
                  else {
                     Collect(x, y);
                  }
               }
            }

         }

         m_Exposure.setNoDataValue(0.0);

      }
      catch (final Exception e) {
         Sextante.addErrorToLog(e);
         return false;
      }

      return !m_Task.isCanceled();
   }


   @Override
   public void defineCharacteristics() {

      final String[] sMethod = { Sextante.getText("Irradiate"), Sextante.getText("Colect_values") };

      setUserCanDefineAnalysisExtent(true);
      setGroup(Sextante.getText("Visibility_and_lighting"));
      setName(Sextante.getText("Visual_exposure"));

      try {
         m_Parameters.addInputRasterLayer(DEM, Sextante.getText("Elevation"), true);
         m_Parameters.addInputRasterLayer(FEATURES, Sextante.getText("Elements"), true);
         m_Parameters.addInputRasterLayer(WEIGHTS, Sextante.getText("Weight"), true);
         m_Parameters.addSelection(METHOD, Sextante.getText("Method"), sMethod);
         m_Parameters.addNumericalValue(RADIUS, Sextante.getText("Radius"), 1000,
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE);
         addOutputRasterLayer(RESULT, Sextante.getText("Visual_exposure"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   private void Irradiate(final int x,
                          final int y) {

      final int i, j;
      int x2, y2;
      int dx, dy;
      int iXMin, iXMax, iYMin, iYMax;
      double dz, z, z2;
      final double dWeight = m_Weights.getCellValueAsDouble(x, y);

      z = m_DEM.getCellValueAsDouble(x, y) + m_Features.getCellValueAsDouble(x, y);

      iXMin = Math.max(0, x - m_iRadius);
      iXMax = Math.min(m_iNX, x + m_iRadius) + 1;
      iYMin = Math.max(0, y - m_iRadius);
      iYMax = Math.max(m_iNY, y + m_iRadius) + 1;

      for (y2 = iYMin; y2 < iYMax; y2++) {
         for (x2 = iXMin; x2 < iXMax; x2++) {
            dx = x - x2;
            dy = y - y2;
            if ((dx * dx + dy * dy) < m_iRadius2) {
               z2 = m_DEM.getCellValueAsDouble(x2, y2);
               if (!m_DEM.isNoDataValue(z2)) {
                  dz = z - z2;
                  if (tracePoint(x2, y2, dx, dy, dz)) {
                     m_Exposure.addToCellValue(x2, y2, dWeight);
                  }
               }
            }
         }
      }

   }


   private void Collect(final int x,
                        final int y) {

      final int i, j;
      int x2, y2;
      int dx, dy;
      int iXMin, iXMax, iYMin, iYMax;
      double dz, z, z2;

      z = m_DEM.getCellValueAsDouble(x, y) + m_Features.getCellValueAsDouble(x, y);

      iXMin = Math.max(0, x - m_iRadius);
      iXMax = Math.min(m_iNX, x + m_iRadius) + 1;
      iYMin = Math.max(0, y - m_iRadius);
      iYMax = Math.max(m_iNY, y + m_iRadius) + 1;

      for (y2 = iYMin; y2 < iYMax; y2++) {
         for (x2 = iXMin; x2 < iXMax; x2++) {
            dx = x - x2;
            dy = y - y2;
            if ((dx * dx + dy * dy) < m_iRadius2) {
               z2 = m_DEM.getCellValueAsDouble(x2, y2);
               if (!m_DEM.isNoDataValue(z2)) {
                  dz = z - z2;
                  if (tracePoint(x2, y2, dx, dy, dz)) {
                     m_Exposure.addToCellValue(x, y, m_Weights.getCellValueAsDouble(x2, y2));
                  }
               }
            }
         }
      }

   }


   boolean tracePoint(int x,
                      int y,
                      double dx,
                      double dy,
                      double dz) {

      double ix, iy, iz, id, d, dist, zmax;

      d = Math.abs(dx) > Math.abs(dy) ? Math.abs(dx) : Math.abs(dy);

      zmax = m_DEM.getMaxValue();

      if (d > 0) {
         dist = Math.sqrt(dx * dx + dy * dy);

         dx /= d;
         dy /= d;
         dz /= d;

         d = dist / d;

         id = 0.0;
         ix = x + 0.5;
         iy = y + 0.5;
         iz = m_DEM.getCellValueAsDouble(x, y);

         while (id < dist) {
            id += d;

            ix += dx;
            iy += dy;
            iz += dz;

            x = (int) ix;
            y = (int) iy;

            if (!m_DEM.getWindowGridExtent().containsCell(x, y)) {
               return true;
            }
            else if (iz < m_DEM.getCellValueAsDouble(x, y)) {
               return false;
            }
            else if (iz > zmax) {
               return true;
            }
         }
      }

      return (true);
   }

}
