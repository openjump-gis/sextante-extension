package es.unex.sextante.lighting.visibility;

import java.awt.geom.Point2D;

import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.rasterWrappers.GridCell;

public class VisibilityAlgorithm
         extends
            GeoAlgorithm {

   public static final String  DEM           = "DEM";
   public static final String  POINT         = "POINT";
   public static final String  METHOD        = "METHOD";
   public static final String  HEIGHT        = "HEIGHT";
   public static final String  HEIGHTOBS     = "HEIGHTOBS";
   public static final String  RADIUS        = "RADIUS";
   public static final String  RESULT        = "RESULT";

   private static final double DEG_90_IN_RAD = Math.toRadians(90);

   private int                 m_iNX, m_iNY;
   private IRasterLayer        m_DEM         = null;
   private IRasterLayer        m_Visibility;
   private GridCell            m_Point;
   private double              m_dHeight, m_dHeightObs;
   private int                 m_iMethod;
   private int                 m_iRadius, m_iRadius2;


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int iDataType;

      m_iMethod = m_Parameters.getParameterValueAsInt("METHOD");
      if (m_iMethod == 0) {
         iDataType = IRasterLayer.RASTER_DATA_TYPE_INT;
      }
      else {
         iDataType = IRasterLayer.RASTER_DATA_TYPE_FLOAT;
      }

      m_DEM = m_Parameters.getParameterValueAsRasterLayer(DEM);
      final Point2D pt = m_Parameters.getParameterValueAsPoint(POINT);
      m_dHeight = m_Parameters.getParameterValueAsDouble(HEIGHT);
      m_dHeightObs = m_Parameters.getParameterValueAsDouble(HEIGHTOBS);

      m_DEM.setWindowExtent(getAnalysisExtent());
      m_Visibility = getNewRasterLayer(RESULT, Sextante.getText("Visibility"), iDataType);

      m_iRadius = (int) (m_Parameters.getParameterValueAsInt(RADIUS) / m_DEM.getWindowCellSize());
      m_iRadius2 = m_iRadius * m_iRadius;

      m_iNX = m_DEM.getNX();
      m_iNY = m_DEM.getNY();

      m_Point = m_DEM.getWindowGridExtent().getGridCoordsFromWorldCoords(pt);

      calculateVisibility(m_Point.getX(), m_Point.getY());

      return !m_Task.isCanceled();
   }


   @Override
   public void defineCharacteristics() {

      final String sMethod[] = { Sextante.getText("Visibility"), Sextante.getText("Lighting"), Sextante.getText("Distance"),
               Sextante.getText("Height") };

      setName(Sextante.getText("Visibility"));
      setGroup(Sextante.getText("Visibility_and_lighting"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputRasterLayer(DEM, Sextante.getText("Elevation"), true);
         m_Parameters.addSelection(METHOD, Sextante.getText("Method"), sMethod);
         m_Parameters.addPoint(POINT, Sextante.getText("Coordinates_of_emitter-receiver"));
         m_Parameters.addNumericalValue(HEIGHT, Sextante.getText("Height_of_emitter-receiver"), 10,
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE);
         m_Parameters.addNumericalValue(HEIGHTOBS, Sextante.getText("Height_of_mobile_receiver-emitter"), 0,
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE);
         m_Parameters.addNumericalValue(RADIUS, Sextante.getText("Radius"), 0,
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE);
         addOutputRasterLayer(RESULT, Sextante.getText("Visibility"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   private void calculateVisibility(final int x_Pos,
                                    final int y_Pos) {

      int x, y;
      int dx, dy;
      int iXMin, iYMin;
      int iXMax, iYMax;
      int iDist;
      double z;
      double z_Pos, aziDTM, decDTM, aziSrc, decSrc, d, dz;
      final double Exaggeration = 1.0;

      z_Pos = m_DEM.getCellValueAsDouble(x_Pos, y_Pos);
      if (m_DEM.isNoDataValue(z_Pos)) {
         return;
      }

      z_Pos += m_dHeight;

      if (m_iRadius > 0) {
         iXMin = Math.max(0, x_Pos - m_iRadius);
         iYMin = Math.max(0, y_Pos - m_iRadius);
         iXMax = Math.min(m_iNX, x_Pos + m_iRadius);
         iYMax = Math.min(m_iNY, y_Pos + m_iRadius);
      }
      else {
         iXMin = 0;
         iXMax = m_iNX;
         iYMin = 0;
         iYMax = m_iNY;
      }

      for (y = iYMin; (y < iYMax) && setProgress(y, m_iNY); y++) {
         for (x = iXMin; x < iXMax; x++) {
            dx = x_Pos - x;
            dy = y_Pos - y;
            iDist = dx * dx + dy * dy;
            if ((iDist < m_iRadius2) || (m_iRadius2 <= 0)) {
               z = m_DEM.getCellValueAsDouble(x, y) + m_dHeightObs;
               if (m_DEM.isNoDataValue(z)) {
                  m_Visibility.setNoData(x, y);
               }
               else {
                  dz = z_Pos - z;
                  if (tracePoint(x, y, dx, dy, dz)) {
                     switch (m_iMethod) {
                        case 0: // Visibility
                           m_Visibility.setCellValue(x, y, 1);
                           break;
                        case 1: // Shade
                           decDTM = m_DEM.getSlope(x, y);
                           aziDTM = m_DEM.getAspect(x, y);
                           decDTM = DEG_90_IN_RAD - Math.atan(Exaggeration * Math.tan(decDTM));

                           decSrc = Math.atan2(dz, Math.sqrt(dx * dx + dy * dy));
                           aziSrc = Math.atan2(dx, dy);

                           d = Math.acos(Math.sin(decDTM) * Math.sin(decSrc) + Math.cos(decDTM) * Math.cos(decSrc)
                                         * Math.cos(aziDTM - aziSrc));

                           m_Visibility.setCellValue(x, y, d < DEG_90_IN_RAD ? d : DEG_90_IN_RAD);
                           break;
                        case 2: // Distance
                           m_Visibility.setCellValue(x, y, m_DEM.getWindowCellSize() * Math.sqrt(dx * dx + dy * dy));
                           break;
                        case 3: // Size
                           if ((d = m_DEM.getWindowCellSize() * Math.sqrt(dx * dx + dy * dy)) > 0.0) {
                              m_Visibility.setCellValue(x, y, Math.atan2(m_dHeight, d));
                           }
                           else {
                              m_Visibility.setNoData(x, y);
                           }
                           break;
                     }
                  }

                  else {
                     switch (m_iMethod) {
                        case 0: // Visibility
                           m_Visibility.setCellValue(x, y, 0);
                           break;
                        case 1: // Shade
                           m_Visibility.setCellValue(x, y, DEG_90_IN_RAD);
                           break;
                        case 2: // Distance
                        case 3: // Size
                           m_Visibility.setNoData(x, y);
                           break;
                     }
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
