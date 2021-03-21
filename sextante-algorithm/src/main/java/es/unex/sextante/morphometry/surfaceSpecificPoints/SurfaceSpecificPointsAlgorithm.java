package es.unex.sextante.morphometry.surfaceSpecificPoints;

import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.exceptions.UnsupportedOutputChannelException;

public class SurfaceSpecificPointsAlgorithm
         extends
            GeoAlgorithm {

   private final static int   m_iOffsetX[] = { 0, 1, 1, 1, 0, -1, -1, -1 };
   private final static int   m_iOffsetY[] = { 1, 1, 0, -1, -1, -1, 0, 1 };

   public static final String METHOD       = "METHOD";
   public static final String DEM          = "DEM";
   public static final String THRESHOLD    = "THRESHOLD";
   public static final String RESULT       = "RESULT";

   IRasterLayer               m_DEM        = null;
   IRasterLayer               m_Result;
   double                     m_dThreshold;
   int                        m_iNX, m_iNY;


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      final int iMethod = m_Parameters.getParameterValueAsInt(METHOD);
      m_DEM = m_Parameters.getParameterValueAsRasterLayer(DEM);
      m_dThreshold = m_Parameters.getParameterValueAsDouble(THRESHOLD);

      m_Result = getNewRasterLayer(RESULT, Sextante.getText("Result"), IRasterLayer.RASTER_DATA_TYPE_INT);

      final AnalysisExtent extent = m_Result.getWindowGridExtent();

      m_DEM.setWindowExtent(extent);

      m_iNX = m_DEM.getNX();
      m_iNY = m_DEM.getNY();

      switch (iMethod) {
         case 0:
            doMarkHighestNB();
            break;
         case 1:
            doOppositeNB();
            break;
         case 2:
            doFlowDirection();
            break;
         case 3:
            doPeuckerDouglas();
            break;
      }

      return !m_Task.isCanceled();

   }


   @Override
   public void defineCharacteristics() {

      final String[] sMethod = { Sextante.getText("Mark_Highest_Neighbour"), Sextante.getText("Opposite_neighbours"),
               Sextante.getText("Flow_direction"), Sextante.getText("Peucker_&_Douglas") };

      setUserCanDefineAnalysisExtent(true);
      setGroup(Sextante.getText("Geomorphometry_and_terrain_analysis"));
      setName(Sextante.getText("Landform_classification"));

      try {
         m_Parameters.addInputRasterLayer(DEM, Sextante.getText("Elevation"), true);
         m_Parameters.addSelection(METHOD, Sextante.getText("Method"), sMethod);
         m_Parameters.addNumericalValue(THRESHOLD, Sextante.getText("Threshold__Peucker_&_Douglas"), 0.01,
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE);
         addOutputRasterLayer(RESULT, Sextante.getText("Result"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   private void doMarkHighestNB() throws UnsupportedOutputChannelException {

      int i, x, y, ix, iy, xlo, ylo, xhi, yhi;
      double lo, hi, z;

      IRasterLayer clo, chi;

      clo = getTempRasterLayer(IRasterLayer.RASTER_DATA_TYPE_BYTE, m_Result.getWindowGridExtent());
      chi = getTempRasterLayer(IRasterLayer.RASTER_DATA_TYPE_BYTE, m_Result.getWindowGridExtent());

      for (y = 0; (y < m_iNY) && setProgress(y, m_iNY); y++) {
         for (x = 0; x < m_iNX; x++) {
            lo = hi = m_DEM.getCellValueAsDouble(x, y);
            xhi = xlo = x;
            yhi = ylo = y;
            for (i = 0; i < 4; i++) {
               ix = x + m_iOffsetX[i];
               iy = y + m_iOffsetY[i];
               z = m_DEM.getCellValueAsDouble(ix, iy);
               if (!m_DEM.isNoDataValue(z)) {
                  if (z > hi) {
                     hi = z;
                     xhi = ix;
                     yhi = iy;
                  }
                  else if (z < lo) {
                     lo = z;
                     xlo = ix;
                     ylo = iy;
                  }
               }
            }
            this.setProgress(y, m_iNY);
            clo.addToCellValue(xlo, ylo, 1);
            chi.addToCellValue(xhi, yhi, 1);
         }
      }

      for (y = 0; (y < m_iNY) && setProgress(y, m_iNY); y++) {
         for (x = 0; x < m_iNX; x++) {
            if (chi.getCellValueAsByte(x, y) == 0) {
               if (clo.getCellValueAsByte(x, y) == 0) {
                  m_Result.setCellValue(x, y, 2);
               }
               else {
                  m_Result.setCellValue(x, y, 1);
               }
            }
            else if (clo.getCellValueAsByte(x, y) == 0) {
               m_Result.setCellValue(x, y, -1);
            }
            else {
               m_Result.setCellValue(x, y, 0);
            }
         }
      }

   }


   private void doOppositeNB() throws UnsupportedOutputChannelException {

      int i, x, y, ix, iy, jx, jy;
      double z, iz, jz;

      IRasterLayer clo, chi;

      clo = getTempRasterLayer(IRasterLayer.RASTER_DATA_TYPE_BYTE, m_Result.getWindowGridExtent());
      chi = getTempRasterLayer(IRasterLayer.RASTER_DATA_TYPE_BYTE, m_Result.getWindowGridExtent());

      for (y = 0; (y < m_iNY) && setProgress(y, m_iNY); y++) {
         for (x = 0; x < m_iNX; x++) {
            z = m_DEM.getCellValueAsDouble(x, y);
            for (i = 0; i < 4; i++) {
               ix = x + m_iOffsetX[i];
               iy = y + m_iOffsetY[i];
               iz = m_DEM.getCellValueAsDouble(ix, iy);
               if (!m_DEM.isNoDataValue(iz)) {
                  jx = x + m_iOffsetX[i + 4];
                  jy = y + m_iOffsetX[i + 4];
                  jz = m_DEM.getCellValueAsDouble(jx, jy);
                  if (!m_DEM.isNoDataValue(jz)) {
                     if ((iz > z) && (jz > z)) {
                        chi.addToCellValue(x, y, 1);
                     }
                     else if ((iz < z) && (jz < z)) {
                        clo.addToCellValue(x, y, 1);
                     }
                  }
               }
            }
         }
      }

      for (y = 0; (y < m_iNY) && setProgress(y, m_iNY); y++) {
         for (x = 0; x < m_iNX; x++) {
            if (chi.getCellValueAsByte(x, y) != 0) {
               if (clo.getCellValueAsByte(x, y) != 0) {
                  m_Result.setCellValue(x, y, 5);
               }
               else {
                  m_Result.setCellValue(x, y, chi.getCellValueAsByte(x, y));
               }
            }
            else if (clo.getCellValueAsByte(x, y) != 0) {
               m_Result.setCellValue(x, y, -clo.getCellValueAsByte(x, y));
            }
            else {
               m_Result.setCellValue(x, y, 0);
            }
         }
      }

   }


   private void doFlowDirection() {

      boolean bLower;
      int x, y, i, ix, iy, xLow = 0, yLow = 0;
      double z, iz, zLow = 0.;

      m_Result.assign(0.0);

      for (y = 0; (y < m_iNY) && setProgress(y, m_iNY); y++) {
         for (x = 0; x < m_iNX; x++) {
            z = m_DEM.getCellValueAsDouble(x, y);
            bLower = false;
            for (i = 0; i < 8; i++) {
               ix = x + m_iOffsetX[i];
               iy = y + m_iOffsetY[i];
               iz = m_DEM.getCellValueAsDouble(ix, iy);
               if (!m_DEM.isNoDataValue(iz)) {
                  if (iz < z) {
                     if (!bLower) {
                        bLower = true;
                        zLow = iz;
                        xLow = ix;
                        yLow = iy;
                     }
                     else if (iz < zLow) {
                        zLow = iz;
                        xLow = ix;
                        yLow = iy;
                     }
                  }
               }
            }

            if (bLower) {
               m_Result.addToCellValue(xLow, yLow, 1);
            }
         }
      }
   }


   private void doPeuckerDouglas() {

      boolean wasPlus;
      int x, y, i, ix, iy, nSgn;
      double d, dPlus, dMinus, z, iz, alt[];

      alt = new double[8];

      for (y = 0; (y < m_iNY) && setProgress(y, m_iNY); y++) {
         for (x = 0; x < m_iNX; x++) {
            z = m_DEM.getCellValueAsDouble(x, y);
            for (i = 0; i < 8; i++) {
               ix = x + m_iOffsetX[i];
               iy = y + m_iOffsetY[i];
               iz = m_DEM.getCellValueAsDouble(ix, iy);
               if (!m_DEM.isNoDataValue(iz)) {
                  alt[i] = iz;
               }
               else {
                  alt[i] = z;
               }
            }

            dPlus = dMinus = 0;
            nSgn = 0;
            wasPlus = (alt[7] - z > 0) ? true : false;

            for (i = 0; i < 8; i++) {
               d = alt[i] - z;
               if (d > 0) {
                  dPlus += d;
                  if (!wasPlus) {
                     nSgn++;
                     wasPlus = true;
                  }
               }
               else if (d < 0) {
                  dMinus -= d;
                  if (wasPlus) {
                     nSgn++;
                     wasPlus = false;
                  }
               }
            }

            i = 0;
            if (dPlus == 0) {
               i = 9;
            }
            else if (dMinus == 0) {
               i = -9;
            }
            else if (nSgn == 4) {
               i = 1;
            }
            else if (nSgn == 2) {
               i = nSgn = 0;

               if (alt[7] > z) {
                  while (alt[i++] > z) {
                     ;
                  }
                  do {
                     nSgn++;
                  }
                  while (alt[i++] < z);
               }
               else {
                  while (alt[i++] < z) {
                     ;
                  }
                  do {
                     nSgn++;
                  }
                  while (alt[i++] > z);
               }

               i = 0;

               if (nSgn == 4) {
                  if (dMinus - dPlus > m_dThreshold) {
                     i = 2;
                  }
                  else if (dPlus - dMinus > m_dThreshold) {
                     i = -2;
                  }
               }
               else { // lines:
                  if (dMinus - dPlus > 0) {
                     i = 7;
                  }
                  else {
                     // Channel
                     i = -7;
                  }
               }
            }

            m_Result.setCellValue(x, y, i);
         }
      }

   }

}
