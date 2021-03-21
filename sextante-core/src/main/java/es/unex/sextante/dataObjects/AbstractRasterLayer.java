

package es.unex.sextante.dataObjects;

import java.util.Arrays;

import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.rasterWrappers.GridWrapper;
import es.unex.sextante.rasterWrappers.GridWrapperInterpolated;
import es.unex.sextante.rasterWrappers.GridWrapperNotInterpolated;


/**
 * A convenience class which implements some of the methods of the IRasterLayer interface. Extending this class is recommended
 * instead of implementing the interface directly
 * 
 * @author volaya
 * 
 */
public abstract class AbstractRasterLayer
         implements
            IRasterLayer {

   private final static int    m_iOffsetX[]            = { 0, 1, 1, 1, 0, -1, -1, -1 };
   private final static int    m_iOffsetY[]            = { 1, 1, 0, -1, -1, -1, 0, 1 };

   private final static double DEG_90_IN_RAD           = Math.PI / 180. * 90.;
   private final static double DEG_180_IN_RAD          = Math.PI;
   private final static double DEG_270_IN_RAD          = Math.PI / 180. * 270.;

   private GridWrapper         m_GridWrapper;

   private double              m_dDist[];

   private double              _2DX;

   private int[][]             m_Histogram;
   private double[]            m_dMax;
   private double[]            m_dMin;
   private double[]            m_dMean;
   private double[]            m_dVariance;
   private boolean             m_bStatisticsCalculated = false;
   private boolean             m_bHistogramCalculated  = false;


   public void setInterpolationMethod(final int iMethod) {

      m_GridWrapper.setInterpolationMethod(iMethod);

   }


   public byte getCellValueAsByte(final int x,
                                  final int y) {

      return m_GridWrapper.getCellValueAsByte(x, y);

   }


   public byte getCellValueAsByte(final int x,
                                  final int y,
                                  final int band) {

      return m_GridWrapper.getCellValueAsByte(x, y, band);

   }


   public short getCellValueAsShort(final int x,
                                    final int y) {

      return m_GridWrapper.getCellValueAsShort(x, y);

   }


   public short getCellValueAsShort(final int x,
                                    final int y,
                                    final int band) {

      return m_GridWrapper.getCellValueAsShort(x, y, band);

   }


   public int getCellValueAsInt(final int x,
                                final int y) {

      return m_GridWrapper.getCellValueAsInt(x, y);

   }


   public int getCellValueAsInt(final int x,
                                final int y,
                                final int band) {

      return m_GridWrapper.getCellValueAsInt(x, y, band);

   }


   public float getCellValueAsFloat(final int x,
                                    final int y) {

      return m_GridWrapper.getCellValueAsFloat(x, y);

   }


   public float getCellValueAsFloat(final int x,
                                    final int y,
                                    final int band) {

      return m_GridWrapper.getCellValueAsFloat(x, y, band);

   }


   public double getCellValueAsDouble(final int x,
                                      final int y) {

      return m_GridWrapper.getCellValueAsDouble(x, y);

   }


   public double getCellValueAsDouble(final int x,
                                      final int y,
                                      final int band) {

      return m_GridWrapper.getCellValueAsDouble(x, y, band);

   }


   public double getValueAt(final double x,
                            final double y) {

      return m_GridWrapper.getValueAt(x, y, 0);

   }


   public double getValueAt(final double x,
                            final double y,
                            final int band) {

      return m_GridWrapper.getValueAt(x, y, band);

   }


   public boolean isNoDataValue(final double dValue) {

      return dValue == getNoDataValue();

   }


   public boolean isInWindow(final int x,
                             final int y) {

      if ((x < 0) || (y < 0)) {
         return false;
      }

      if ((x >= m_GridWrapper.getNX()) || (y >= m_GridWrapper.getNY())) {
         return false;
      }

      return true;

   }


   public int getNX() {

      return m_GridWrapper.getNX();

   }


   public int getNY() {

      return m_GridWrapper.getNY();

   }


   public double getWindowCellSize() {

      return m_GridWrapper.getCellSize();

   }


   public AnalysisExtent getWindowGridExtent() {

      return m_GridWrapper.getGridExtent();

   }


   public void assign(final double dValue) {

      int iBand;
      int x, y;

      for (iBand = 0; iBand < this.getBandsCount(); iBand++) {
         for (y = 0; y < getNY(); y++) {
            for (x = 0; x < getNX(); x++) {
               setCellValue(x, y, iBand, dValue);
            }
         }
      }

   }


   public void assign(final IRasterLayer layer) {

      double dValue;

      layer.setWindowExtent(getWindowGridExtent());

      final int iNX = layer.getNX();
      final int iNY = layer.getNY();

      for (int y = 0; y < iNY; y++) {
         for (int x = 0; x < iNX; x++) {
            dValue = layer.getCellValueAsDouble(x, y);
            setCellValue(x, y, dValue);
         }
      }

      setNoDataValue(layer.getNoDataValue());

   }


   public void add(final IRasterLayer driver) {

      double dValue;

      if (driver.getWindowGridExtent().equals(getWindowGridExtent())) {
         for (int y = 0; y < getWindowGridExtent().getNY(); y++) {
            for (int x = 0; x < getWindowGridExtent().getNX(); x++) {
               dValue = driver.getCellValueAsDouble(x, y) + getCellValueAsDouble(x, y);
               setCellValue(x, y, dValue);
            }
         }
         setNoDataValue(driver.getNoDataValue());
      }

   }


   public void assignNoData() {

      assign(getNoDataValue());

   }


   public void setCellValue(final int x,
                            final int y,
                            final double dValue) {

      setCellValue(x, y, 0, dValue);

   }


   public void setNoData(final int x,
                         final int y) {

      setCellValue(x, y, getNoDataValue());

   }


   public void setNoData(final int x,
                         final int y,
                         final int iBand) {

      setCellValue(x, y, iBand, getNoDataValue());

   }


   public void addToCellValue(final int x,
                              final int y,
                              final int iBand,
                              final double dValue) {

      double dCellValue = getCellValueAsDouble(x, y, iBand);

      if (!isNoDataValue(dCellValue)) {
         dCellValue += dValue;
         setCellValue(x, y, iBand, dCellValue);
      }

   }


   public void addToCellValue(final int x,
                              final int y,
                              final double dValue) {

      addToCellValue(x, y, 0, dValue);

   }


   public void multiply(final double dValue) {

      int iBand;
      int x, y;

      for (iBand = 0; iBand < this.getBandsCount(); iBand++) {
         for (y = 0; y < getNY(); y++) {
            for (x = 0; x < getNX(); x++) {
               final double dVal = getCellValueAsDouble(x, y, iBand);
               setCellValue(x, y, iBand, dValue * dVal);
            }
         }
      }

   }


   public void setWindowExtent(final IRasterLayer layer) {

      final AnalysisExtent layerExtent = new AnalysisExtent(layer);

      setWindowExtent(layerExtent);

   }


   public void setWindowExtent(final AnalysisExtent extent) {


      if (extent.fitsIn(this.getLayerGridExtent())) {
         m_GridWrapper = new GridWrapperNotInterpolated(this, extent);
      }
      else {
         m_GridWrapper = new GridWrapperInterpolated(this, extent);
      }

      setConstants();

      m_bHistogramCalculated = false;
      m_bStatisticsCalculated = false;

   }


   public void setFullExtent() {

      m_GridWrapper = new GridWrapperNotInterpolated(this, getLayerGridExtent());

      setConstants();

      m_bHistogramCalculated = false;
      m_bStatisticsCalculated = false;

   }


   ///////////////////////////////////Statistical stuff//////////////////////

   protected void setConstants() {

      int i;
      final double dCellSize = getWindowCellSize();

      m_dDist = new double[8];

      for (i = 0; i < 8; i++) {
         m_dDist[i] = Math.sqrt(m_iOffsetX[i] * dCellSize * m_iOffsetX[i] * dCellSize + m_iOffsetY[i] * dCellSize * m_iOffsetY[i]
                                * dCellSize);
      }

      _2DX = dCellSize * 2;

   }


   protected void calculateStatistics() {


      int x, y;
      double z;
      int iValues;

      final int iBands = getBandsCount();

      m_dMean = new double[iBands];
      m_dVariance = new double[iBands];
      m_dMin = new double[iBands];
      m_dMax = new double[iBands];

      for (int i = 0; i < this.getBandsCount(); i++) {
         m_dMean[i] = 0.0;
         m_dVariance[i] = 0.0;
      }

      if (m_GridWrapper == null) {
         this.setFullExtent();
      }

      for (int i = 0; i < this.getBandsCount(); i++) {
         iValues = 0;
         for (y = 0; y < getNY(); y++) {
            for (x = 0; x < getNX(); x++) {
               z = getCellValueAsDouble(x, y, i);
               if (!isNoDataValue(z)) {
                  if (iValues == 0) {
                     m_dMin[i] = m_dMax[i] = z;
                  }
                  else if (m_dMin[i] > z) {
                     m_dMin[i] = z;
                  }
                  else if (m_dMax[i] < z) {
                     m_dMax[i] = z;
                  }

                  m_dMean[i] += z;
                  m_dVariance[i] += z * z;
                  iValues++;
               }
            }
         }

         if (iValues > 0) {
            m_dMean[i] /= iValues;
            m_dVariance[i] = m_dVariance[i] / iValues - m_dMean[i] * m_dMean[i];
         }
      }

      m_bStatisticsCalculated = true;

   }


   protected void calculateHistogram() {

      int x, y;
      int iClass;
      double dValue;
      double dRange;

      if (!m_bStatisticsCalculated) {
         calculateStatistics();
      }

      final int iBands = getBandsCount();

      m_Histogram = new int[iBands][256];

      for (int i = 0; i < m_Histogram.length; i++) {
         Arrays.fill(m_Histogram[i], 0);
      }

      for (int i = 0; i < iBands; i++) {
         dRange = m_dMax[i] - m_dMin[i];
         for (y = 0; y < getNY(); y++) {
            for (x = 0; x < getNX(); x++) {
               dValue = getCellValueAsDouble(x, y, i);
               if (!isNoDataValue(dValue)) {
                  iClass = (int) ((dValue - m_dMin[i]) / dRange * 255.);
                  m_Histogram[i][iClass]++;
               }
            }
         }
      }

      m_bHistogramCalculated = true;


   }


   public int[] getHistogram(final int iBand) {

      if (!m_bHistogramCalculated) {
         calculateHistogram();
      }

      return m_Histogram[iBand];

   }


   public int[] getHistogram() {

      return getHistogram(0);

   }


   public int[] getAccumulatedHistogram(final int iBand) {

      final int[] accHistogram = new int[256];

      Arrays.fill(accHistogram, 0);

      if (!m_bHistogramCalculated) {
         calculateHistogram();
      }

      for (int i = 1; i < 256; i++) {
         accHistogram[i] = m_Histogram[iBand][i] + accHistogram[i - 1];
      }

      return accHistogram;

   }


   public int[] getAccumulatedHistogram() {

      return getAccumulatedHistogram(0);

   }


   public double getMinValue(final int iBand) {

      if (!m_bStatisticsCalculated) {
         calculateStatistics();
      }

      return m_dMin[iBand];

   }


   public double getMaxValue(final int iBand) {

      if (!m_bStatisticsCalculated) {
         calculateStatistics();
      }

      return m_dMax[iBand];

   }


   public double getMeanValue(final int iBand) {

      if (!m_bStatisticsCalculated) {
         calculateStatistics();
      }

      return m_dMean[iBand];

   }


   public double getVariance(final int iBand) {

      if (!m_bStatisticsCalculated) {
         calculateStatistics();
      }

      return m_dVariance[iBand];

   }


   public double getMeanValue() {

      return getMeanValue(0);

   }


   public double getMinValue() {

      return getMinValue(0);

   }


   public double getMaxValue() {

      return getMaxValue(0);

   }


   public double getVariance() {

      return getVariance(0);

   }


   //////////////////////////////Additional methods for DEM analysis//////


   protected boolean getSubMatrix3x3(final int x,
                                     final int y,
                                     final double SubMatrix[]) {

      int i;
      int iDir;
      double z, z2;

      z = getCellValueAsDouble(x, y);

      if (isNoDataValue(z)) {
         return false;
      }
      else {
         //SubMatrix[4]	= 0.0;
         for (i = 0; i < 4; i++) {

            iDir = 2 * i;
            z2 = getCellValueAsDouble(x + m_iOffsetX[iDir], y + m_iOffsetY[iDir]);
            if (!isNoDataValue(z2)) {
               SubMatrix[i] = z2 - z;
            }
            else {
               z2 = getCellValueAsDouble(x + m_iOffsetX[(iDir + 4) % 8], y + m_iOffsetY[(iDir + 4) % 8]);
               if (!isNoDataValue(z2)) {
                  SubMatrix[i] = z - z2;
               }
               else {
                  SubMatrix[i] = 0.0;
               }
            }
         }

         return true;
      }

   }


   public double getSlope(final int x,
                          final int y) {

      double zm[], G, H;

      zm = new double[4];

      if (getSubMatrix3x3(x, y, zm)) {
         G = (zm[0] - zm[2]) / _2DX;
         H = (zm[1] - zm[3]) / _2DX;
         return Math.atan(Math.sqrt(G * G + H * H));
      }
      else {
         return getNoDataValue();
      }
   }


   public double getAspect(final int x,
                           final int y) {

      double zm[], G, H, dAspect;

      zm = new double[4];

      if (getSubMatrix3x3(x, y, zm)) {
         G = (zm[0] - zm[2]) / _2DX;
         H = (zm[1] - zm[3]) / _2DX;
         if (G != 0.0) {
            dAspect = DEG_180_IN_RAD + Math.atan2(H, G);
         }
         else {
            dAspect = H > 0.0 ? DEG_270_IN_RAD : (H < 0.0 ? DEG_90_IN_RAD : -1.0);
         }
         return dAspect;
      }
      else {
         return getNoDataValue();
      }
   }


   public double getDistToNeighborInDir(final int iDir) {

      return m_dDist[iDir];

   }


   public static double getUnitDistToNeighborInDir(final int iDir) {

      return ((iDir % 2 != 0) ? Math.sqrt(2.0) : 1.0);

   }


   public int getDirToNextDownslopeCell(final int x,
                                        final int y) {

      return getDirToNextDownslopeCell(x, y, true);

   }


   public int getDirToNextDownslopeCell(final int x,
                                        final int y,
                                        final boolean bForceDirToNoDataCell) {

      int i, iDir;
      double z, z2, dSlope, dMaxSlope;

      z = getCellValueAsDouble(x, y);

      if (isNoDataValue(z)) {
         return -1;
      }

      dMaxSlope = 0.0;
      for (iDir = -1, i = 0; i < 8; i++) {
         z2 = getCellValueAsDouble(x + m_iOffsetX[i], y + m_iOffsetY[i]);
         if (isNoDataValue(z2)) {
            if (bForceDirToNoDataCell) {
               return i;
            }
            /*else {
               return -1;
            }*/
         }
         else {
            dSlope = (z - z2) / getDistToNeighborInDir(i);
            if (dSlope > dMaxSlope) {
               iDir = i;
               dMaxSlope = dSlope;
            }
         }
      }

      return iDir;

   }


   @Override
   public String toString() {

      return this.getName();

   }


   public void setStatisticsHaveToBeCalculated() {

      m_bHistogramCalculated = false;
      m_bStatisticsCalculated = false;

   }

}
