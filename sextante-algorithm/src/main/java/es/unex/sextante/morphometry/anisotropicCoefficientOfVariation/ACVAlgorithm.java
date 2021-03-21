

package es.unex.sextante.morphometry.anisotropicCoefficientOfVariation;

import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.exceptions.UnsupportedOutputChannelException;


public class ACVAlgorithm
         extends
            GeoAlgorithm {

   private double             NO_DATA;

   public static final String DEM     = "DEM";
   public static final String RESULT  = "RESULT";

   IRasterLayer               m_ACV;
   IRasterLayer               m_DEM;
   IRasterLayer               m_DX, m_DY, m_DUp, m_DDown;
   int                        m_iNX, m_iNY;

   int                        m_iSize = 5;


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int x, y;

      NO_DATA = m_OutputFactory.getDefaultNoDataValue();

      m_DEM = m_Parameters.getParameterValueAsRasterLayer(DEM);
      m_ACV = getNewRasterLayer(RESULT, Sextante.getText("Anisotropic_coefficient_of_variation"),
               IRasterLayer.RASTER_DATA_TYPE_FLOAT);

      final AnalysisExtent extent = m_ACV.getWindowGridExtent();

      m_DX = getTempRasterLayer(IRasterLayer.RASTER_DATA_TYPE_FLOAT, extent);
      m_DY = getTempRasterLayer(IRasterLayer.RASTER_DATA_TYPE_FLOAT, extent);
      m_DDown = getTempRasterLayer(IRasterLayer.RASTER_DATA_TYPE_FLOAT, extent);
      m_DUp = getTempRasterLayer(IRasterLayer.RASTER_DATA_TYPE_FLOAT, extent);

      m_DEM.setWindowExtent(extent);

      m_iNX = m_DEM.getNX();
      m_iNY = m_DEM.getNY();

      m_ACV.setNoDataValue(NO_DATA);

      calculateDerivatives();

      setProgressText(Sextante.getText("Calculating_coefficient") + "...");

      for (y = 0; (y < m_iNY) && setProgress(y, m_iNY); y++) {
         for (x = 0; x < m_iNX; x++) {
            m_ACV.setCellValue(x, y, getACV(x, y));
         }
      }

      return !m_Task.isCanceled();

   }


   @Override
   public void defineCharacteristics() {

      setUserCanDefineAnalysisExtent(true);
      setGroup(Sextante.getText("Geomorphometry_and_terrain_analysis"));
      setName(Sextante.getText("Anisotropic_coefficient_of_variation"));

      try {
         m_Parameters.addInputRasterLayer(DEM, Sextante.getText("Elevation"), true);
         addOutputRasterLayer(RESULT, Sextante.getText("Anisotropic_coefficient_of_variation"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   private void calculateDerivatives() throws UnsupportedOutputChannelException {

      int x, y;

      setProgressText(Sextante.getText("Calculating_derivatives") + "...");

      for (y = 0; (y < m_iNY) && setProgress(y, m_iNY); y++) {
         for (x = 0; x < m_iNX; x++) {
            m_DX.setCellValue(x, y, getDFDX(x, y));
            m_DY.setCellValue(x, y, getDFDY(x, y));
            m_DDown.setCellValue(x, y, getDFDDown(x, y));
            m_DUp.setCellValue(x, y, getDFDUp(x, y));
         }
      }
      smooth(m_DX);
      smooth(m_DY);
      smooth(m_DDown);
      smooth(m_DUp);

   }


   private void smooth(IRasterLayer driver) throws UnsupportedOutputChannelException {

      int i, j;
      int x, y;
      int iValidCells;
      double dSum;
      double dValue;
      final IRasterLayer temp = getTempRasterLayer(IRasterLayer.RASTER_DATA_TYPE_FLOAT, m_DEM.getWindowGridExtent());

      for (y = 1; y < m_iNY - 1; y++) {
         for (x = 1; x < m_iNX - 1; x++) {
            dSum = 0;
            iValidCells = 0;
            for (i = -1; i < 2; i++) {
               for (j = -1; j < 2; j++) {
                  dValue = driver.getCellValueAsDouble(x + i, y + j);
                  if (dValue != NO_DATA) {
                     dSum += dValue;
                     iValidCells++;
                  }
               }
            }
            if (iValidCells != 0) {
               temp.setCellValue(x, y, dSum / iValidCells);
            }
            else {
               temp.setNoData(x, y);
            }
         }
      }

      driver = temp;

      System.gc();

   }


   private double getACV(final int x,
                         final int y) {

      final double dX = m_DX.getCellValueAsDouble(x, y);
      final double dY = m_DY.getCellValueAsDouble(x, y);
      final double dUp = m_DUp.getCellValueAsDouble(x, y);
      final double dDown = m_DDown.getCellValueAsDouble(x, y);

      if ((dX == NO_DATA) || (dY == NO_DATA) || (dUp == NO_DATA) || (dDown == NO_DATA)) {
         return NO_DATA;
      }

      final double dAvg = (dX + dY + dUp + dDown) / 4.;
      double dDif = 0;
      double dZ;

      if ((dZ = m_DX.getCellValueAsDouble(x, y - 1)) != NO_DATA) {
         dDif += Math.pow(dAvg - dZ, 2.);
      }
      else {
         return NO_DATA;
      }

      if ((dZ = m_DX.getCellValueAsDouble(x, y + 1)) != NO_DATA) {
         dDif += Math.pow(dAvg - dZ, 2.);
      }
      else {
         return NO_DATA;
      }

      if ((dZ = m_DY.getCellValueAsDouble(x - 1, y)) != NO_DATA) {
         dDif += Math.pow(dAvg - dZ, 2.);
      }
      else {
         return NO_DATA;
      }

      if ((dZ = m_DY.getCellValueAsDouble(x + 1, y)) != NO_DATA) {
         dDif += Math.pow(dAvg - dZ, 2.);
      }
      else {
         return NO_DATA;
      }

      if ((dZ = m_DDown.getCellValueAsDouble(x - 1, y + 1)) != NO_DATA) {
         dDif += Math.pow(dAvg - dZ, 2.);
      }
      else {
         return NO_DATA;
      }
      if ((dZ = m_DDown.getCellValueAsDouble(x + 1, y - 1)) != NO_DATA) {
         dDif += Math.pow(dAvg - dZ, 2.);
      }
      else {
         return NO_DATA;
      }

      if ((dZ = m_DUp.getCellValueAsDouble(x - 1, y - 1)) != NO_DATA) {
         dDif += Math.pow(dAvg - dZ, 2.);
      }
      else {
         return NO_DATA;
      }

      if ((dZ = m_DUp.getCellValueAsDouble(x + 1, y + 1)) != NO_DATA) {
         dDif += Math.pow(dAvg - dZ, 2.);
      }
      else {
         return NO_DATA;
      }

      return Math.log(1 + Math.sqrt(dDif / 8.) / dAvg);

   }


   private double getDFDX(final int x,
                          final int y) {

      double dValue;
      final double dValues[] = new double[4];

      dValue = m_DEM.getCellValueAsDouble(x - 2, y);
      if (m_DEM.isNoDataValue(dValue)) {
         return NO_DATA;
      }
      dValues[0] = dValue;
      dValue = m_DEM.getCellValueAsDouble(x - 1, y);
      if (m_DEM.isNoDataValue(dValue)) {
         return NO_DATA;
      }
      dValues[1] = dValue;
      dValue = m_DEM.getCellValueAsDouble(x + 1, y);
      if (m_DEM.isNoDataValue(dValue)) {
         return NO_DATA;
      }
      dValues[2] = dValue;
      dValue = m_DEM.getCellValueAsDouble(x + 2, y);
      if (m_DEM.isNoDataValue(dValue)) {
         return NO_DATA;
      }
      dValues[3] = dValue;

      return Math.abs(dValues[0] + dValues[1] * -8 + dValues[2] * 8 + dValues[3] * -1);

   }


   private double getDFDY(final int x,
                          final int y) {

      double dValue;
      final double dValues[] = new double[4];

      dValue = m_DEM.getCellValueAsDouble(x, y - 2);
      if (m_DEM.isNoDataValue(dValue)) {
         return NO_DATA;
      }
      dValues[0] = dValue;
      dValue = m_DEM.getCellValueAsDouble(x, y - 1);
      if (m_DEM.isNoDataValue(dValue)) {
         return NO_DATA;
      }
      dValues[1] = dValue;
      dValue = m_DEM.getCellValueAsDouble(x, y + 1);
      if (m_DEM.isNoDataValue(dValue)) {
         return NO_DATA;
      }
      dValues[2] = dValue;
      dValue = m_DEM.getCellValueAsDouble(x, y + 2);
      if (m_DEM.isNoDataValue(dValue)) {
         return NO_DATA;
      }
      dValues[3] = dValue;

      return Math.abs(dValues[0] + dValues[1] * -8 + dValues[2] * 8 + dValues[3] * -1);

   }


   private double getDFDDown(final int x,
                             final int y) {

      double dValue;
      final double dValues[] = new double[4];

      dValue = m_DEM.getCellValueAsDouble(x - 2, y + 2);
      if (m_DEM.isNoDataValue(dValue)) {
         return NO_DATA;
      }
      dValues[0] = dValue;
      dValue = m_DEM.getCellValueAsDouble(x - 1, y + 1);
      if (m_DEM.isNoDataValue(dValue)) {
         return NO_DATA;
      }
      dValues[1] = dValue;
      dValue = m_DEM.getCellValueAsDouble(x + 1, y - 1);
      if (m_DEM.isNoDataValue(dValue)) {
         return NO_DATA;
      }
      dValues[2] = dValue;
      dValue = m_DEM.getCellValueAsDouble(x + 2, y - 2);
      if (m_DEM.isNoDataValue(dValue)) {
         return NO_DATA;
      }
      dValues[3] = dValue;

      return Math.abs(dValues[0] + dValues[1] * -8 + dValues[2] * 8 + dValues[3] * -1);

   }


   private double getDFDUp(final int x,
                           final int y) {

      double dValue;
      final double dValues[] = new double[4];

      dValue = m_DEM.getCellValueAsDouble(x - 2, y - 2);
      if (m_DEM.isNoDataValue(dValue)) {
         return NO_DATA;
      }
      dValues[0] = dValue;
      dValue = m_DEM.getCellValueAsDouble(x - 1, y - 1);
      if (m_DEM.isNoDataValue(dValue)) {
         return NO_DATA;
      }
      dValues[1] = dValue;
      dValue = m_DEM.getCellValueAsDouble(x + 1, y + 1);
      if (m_DEM.isNoDataValue(dValue)) {
         return NO_DATA;
      }
      dValues[2] = dValue;
      dValue = m_DEM.getCellValueAsDouble(x + 2, y + 2);
      if (m_DEM.isNoDataValue(dValue)) {
         return NO_DATA;
      }
      dValues[3] = dValue;

      return Math.abs(dValues[0] + dValues[1] * -8 + dValues[2] * 8 + dValues[3] * -1);

   }

}
