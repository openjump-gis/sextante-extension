package es.unex.sextante.gridTools.closeGaps;

import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;


public class CloseGapsAlgorithm
         extends
            GeoAlgorithm {

   public static final String INPUT        = "INPUT";
   public static final String THRESHOLD    = "THRESHOLD";
   public static final String RESULT       = "RESULT";

   private final int          m_iOffsetX[] = { 0, 1, 1, 1, 0, -1, -1, -1 };
   private final int          m_iOffsetY[] = { 1, 1, 0, -1, -1, -1, 0, 1 };

   private double             m_dDistToNeighbour[];
   private IRasterLayer       m_Window;
   private IRasterLayer       m_Result;
   private IRasterLayer       m_TensionKeep, m_TensionTemp;
   private int                m_iNX, m_iNY;
   double                     m_dThreshold;


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Void_filling"));
      setGroup(Sextante.getText("Basic_tools_for_raster_layers"));
      setUserCanDefineAnalysisExtent(false);
      setIsDeterminatedProcess(false);

      try {
         m_Parameters.addInputRasterLayer(INPUT, Sextante.getText("Layer"), true);
         m_Parameters.addNumericalValue(THRESHOLD, Sextante.getText("Tension_threshold"), 0.1,
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE);
         addOutputRasterLayer(RESULT, Sextante.getText("Filled_layer"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      m_dDistToNeighbour = new double[8];
      for (int i = 0; i < m_dDistToNeighbour.length; i++) {
         m_dDistToNeighbour[i] = ((i % 2 != 0) ? Math.sqrt(2.0) : 1.0);
      }

      m_dThreshold = m_Parameters.getParameterValueAsDouble(THRESHOLD);
      m_Window = m_Parameters.getParameterValueAsRasterLayer(INPUT);
      m_Window.setFullExtent();
      m_Result = getNewRasterLayer(RESULT, m_Window.getName() + Sextante.getText("[filled]"), m_Window.getDataType(),
               m_Window.getWindowGridExtent());
      m_TensionTemp = getTempRasterLayer(m_Window.getDataType(), m_Window.getWindowGridExtent());
      m_TensionKeep = getTempRasterLayer(IRasterLayer.RASTER_DATA_TYPE_BYTE, m_Window.getWindowGridExtent());

      m_iNX = m_Window.getNX();
      m_iNY = m_Window.getNY();

      tensionMain();

      return !m_Task.isCanceled();

   }


   private void tensionMain() {

      int iStep, iStart, n;
      double max;

      n = m_iNX > m_iNY ? m_iNX : m_iNY;
      iStep = 0;
      do {
         iStep++;
      }
      while (Math.pow(2.0, iStep + 1) < n);
      iStart = (int) Math.pow(2.0, iStep);

      m_Result.assignNoData();

      for (iStep = iStart; iStep >= 1; iStep /= 2) {
         tensionInit(iStep);
         do {
            max = tensionStep(iStep);
         }
         while (max > m_dThreshold);
         if (m_Task.isCanceled()) {
            return;
         }
      }

   }


   private void tensionInit(final int iStep) {

      int x, y, i, ix, iy, nx, ny, nz;
      double z, z2;

      m_TensionTemp.assignNoData();
      m_TensionKeep.assign(0.0);

      for (y = 0; y < m_iNY; y += iStep) {
         ny = y + iStep < m_iNY ? y + iStep : m_iNY;
         for (x = 0; x < m_iNX; x += iStep) {
            z = m_Window.getCellValueAsDouble(x, y);
            if (!m_Window.isNoDataValue(z)) {
               m_TensionTemp.setCellValue(x, y, z);
               m_TensionKeep.setCellValue(x, y, 1.0);
            }
            else {
               nx = x + iStep < m_iNX ? x + iStep : m_iNX;
               nz = 0;
               z = 0.0;
               for (iy = y; iy < ny; iy++) {
                  for (ix = x; ix < nx; ix++) {
                     z2 = m_Window.getCellValueAsDouble(ix, iy);
                     if (!m_Window.isNoDataValue(z2)) {
                        z += z2;
                        nz++;
                     }
                  }
               }
               if (nz > 0) {
                  m_TensionTemp.setCellValue(x, y, z / nz);
                  m_TensionKeep.setCellValue(x, y, 1.0);
               }
            }
         }
      }

      for (y = 0; y < m_iNY; y += iStep) {
         for (x = 0; x < m_iNX; x += iStep) {
            if (m_TensionKeep.getCellValueAsByte(x, y) == 0.0) {
               z = m_Result.getCellValueAsDouble(x, y);
               if (!m_Result.isNoDataValue(z)) {
                  m_TensionTemp.setCellValue(x, y, z);
               }
               else {
                  nz = 0;
                  z = 0.0;
                  for (i = 0; i < 8; i++) {
                     ix = x + iStep * m_iOffsetX[i];
                     iy = y + iStep * m_iOffsetY[i];
                     z2 = m_Result.getCellValueAsDouble(ix, iy);
                     if (!m_Result.isNoDataValue(z2)) {
                        z += z2;
                        nz++;
                     }
                  }

                  if (nz > 0.0) {
                     m_TensionTemp.setCellValue(x, y, z / nz);
                  }
                  else {
                     m_TensionTemp.setCellValue(x, y, m_Window.getCellValueAsDouble(x, y));
                  }
               }
            }
         }
      }

      m_Result.assign(m_TensionTemp);

   }


   private double tensionStep(final int iStep) {

      int x, y;
      double d, dMax;
      dMax = 0.0;

      for (y = 0; y < m_iNY; y += iStep) {
         for (x = 0; x < m_iNX; x += iStep) {
            if (m_TensionKeep.getCellValueAsByte(x, y) == 0.0) {
               d = tensionChange(x, y, iStep);
               m_TensionTemp.setCellValue(x, y, d);
               d = Math.abs(d - m_Result.getCellValueAsDouble(x, y));
               if (d > dMax) {
                  dMax = d;
               }
            }
         }
      }

      for (y = 0; y < m_iNY; y += iStep) {
         for (x = 0; x < m_iNX; x += iStep) {
            if (m_TensionKeep.getCellValueAsByte(x, y) == 0.0) {
               m_Result.setCellValue(x, y, m_TensionTemp.getCellValueAsDouble(x, y));
            }
         }
      }

      return (dMax);
   }


   private double tensionChange(final int x,
                                final int y,
                                final int iStep) {

      int i, ix, iy;
      double n, d, dz, z;

      for (i = 0, d = 0.0, n = 0.0; i < 8; i++) {
         ix = x + iStep * m_iOffsetX[i];
         iy = y + iStep * m_iOffsetY[i];
         z = m_Result.getCellValueAsDouble(ix, iy);
         if (!m_Result.isNoDataValue(z)) {
            dz = 1.0 / m_dDistToNeighbour[i];
            d += dz * z;
            n += dz;
         }
      }

      if (n > 0.0) {
         d /= n;
         return d;
      }

      return m_Result.getCellValueAsDouble(x, y);

   }

}
