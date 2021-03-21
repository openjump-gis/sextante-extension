

package es.unex.sextante.gridAnalysis.predictiveModels;


import java.util.ArrayList;

import Jama.Matrix;

import org.locationtech.jts.geom.Coordinate;

import es.unex.sextante.additionalInfo.AdditionalInfoMultipleInput;
import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.dataObjects.vectorFilters.BoundingBoxFilter;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.rasterWrappers.GridWrapper;


public class PredictiveModelsAlgorithm
         extends
            GeoAlgorithm {

   public static final String  INPUT       = "INPUT";
   public static final String  POINTS      = "POINTS";
   public static final String  METHOD      = "METHOD";
   public static final String  CUTOFF      = "CUTOFF";
   public static final String  RESULT      = "RESULT";

   private static final double NODATA      = -99999999999999.;
   private static final int    DISTTOAVG   = 0;
   private static final int    MAHALANOBIS = 1;
   private static final int    BIOCLIM     = 2;

   private int                 m_iNX, m_iNY;
   private int                 m_iMethod;
   private double              m_dCutoff;
   private double              m_dMean[];
   private double              m_dMin[];
   private double              m_dMax[];
   private double              m_dStdDev[];
   private ArrayList           m_RasterLayers;
   private IVectorLayer        m_Points;
   private IRasterLayer        m_Windows[];
   private IRasterLayer        m_Result;
   private Matrix              m_Inverse;


   @Override
   public void defineCharacteristics() {

      final String sOptions[] = { Sextante.getText("Distance_to_mean_value"), Sextante.getText("Mahalanobis_distance"),
               Sextante.getText("BIOCLIM") };

      setUserCanDefineAnalysisExtent(true);
      setGroup(Sextante.getText("Raster_layer_analysis"));
      setName(Sextante.getText("Predictive_models"));

      try {
         m_Parameters.addMultipleInput(INPUT, Sextante.getText("Predictors"), AdditionalInfoMultipleInput.DATA_TYPE_RASTER, true);
         m_Parameters.addInputVectorLayer(POINTS, Sextante.getText("Presence_points"),
                  AdditionalInfoVectorLayer.SHAPE_TYPE_POINT, true);
         m_Parameters.addSelection(METHOD, Sextante.getText("Method"), sOptions);
         m_Parameters.addNumericalValue(CUTOFF, Sextante.getText("Cutoff__BIOCLIM"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE, 0.1, 0, Double.MAX_VALUE);
         addOutputRasterLayer(RESULT, Sextante.getText("Suitability"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i;
      int iCount;

      m_RasterLayers = m_Parameters.getParameterValueAsArrayList(INPUT);
      m_Points = m_Parameters.getParameterValueAsVectorLayer(POINTS);
      m_iMethod = m_Parameters.getParameterValueAsInt(METHOD);
      m_dCutoff = m_Parameters.getParameterValueAsDouble(CUTOFF);

      if (!m_bIsAutoExtent) {
         m_Points.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
      }

      iCount = m_Points.getShapesCount();

      if ((m_RasterLayers.size() == 0) || (iCount < 3)) {
         throw new GeoAlgorithmExecutionException(Sextante.getText("Numero_insuficiente_de_puntos"));
      }

      m_Result = getNewRasterLayer(RESULT, Sextante.getText("Suitability"), IRasterLayer.RASTER_DATA_TYPE_DOUBLE);

      m_Windows = new IRasterLayer[m_RasterLayers.size()];

      for (i = 0; i < m_RasterLayers.size(); i++) {
         m_Windows[i] = (IRasterLayer) m_RasterLayers.get(i);
         m_Windows[i].setWindowExtent(m_Result.getWindowGridExtent());
         m_Windows[i].setInterpolationMethod(GridWrapper.INTERPOLATION_BSpline);
      }

      m_iNX = m_Result.getWindowGridExtent().getNX();
      m_iNY = m_Result.getWindowGridExtent().getNY();

      calculateStatisticalValues();

      calculateSuitability();

      return !m_Task.isCanceled();


   }


   private void calculateSuitability() {

      switch (m_iMethod) {
         case DISTTOAVG:
            calculateDistanceToAverage();
            break;
         case MAHALANOBIS:
            calculateMahalanobisDistance();
            break;
         case BIOCLIM:
            calculateBioclim();
            break;
      }

   }


   private void calculateBioclim() {

      int i;
      int x, y;
      double dValue;
      double dSuitability;
      boolean bNoDataValue;

      setProgressText(Sextante.getText("Calculating_distances"));
      for (y = 0; (y < m_iNY) && setProgress(y, m_iNY); y++) {
         for (x = 0; x < m_iNX; x++) {
            bNoDataValue = false;
            dSuitability = 1;
            for (i = 0; i < m_Windows.length; i++) {
               dValue = m_Windows[i].getCellValueAsDouble(x, y);
               if (m_Windows[i].isNoDataValue(dValue)) {
                  bNoDataValue = true;
                  break;
               }
               else {
                  if (Math.abs(dValue - m_dMean[i]) > m_dCutoff * m_dStdDev[i]) {
                     if ((dValue > m_dMax[i]) && (dValue < m_dMin[i])) {
                        dSuitability = 0.5;
                     }
                     else {
                        dSuitability = 0;
                        break;
                     }
                  }
               }
            }
            if (bNoDataValue) {
               m_Result.setNoData(x, y);
            }
            else {
               m_Result.setCellValue(x, y, dSuitability);
            }
         }
      }

   }


   private void calculateDistanceToAverage() {

      int i;
      int x, y;
      double dValue;
      double dDist;
      boolean bNoDataValue;
      final double dNormalizeFactor = Math.sqrt(m_Windows.length);

      setProgressText(Sextante.getText("Calculating_distances"));
      for (y = 0; (y < m_iNY) && setProgress(y, m_iNY); y++) {
         for (x = 0; x < m_iNX; x++) {
            bNoDataValue = false;
            dDist = 0;
            for (i = 0; i < m_Windows.length; i++) {
               dValue = m_Windows[i].getCellValueAsDouble(x, y);
               if (m_Windows[i].isNoDataValue(dValue)) {
                  bNoDataValue = true;
                  break;
               }
               else {
                  dDist += Math.pow((dValue - m_dMean[i]) / (m_dMax[i] - m_dMin[i]), 2.);
               }
            }
            if (bNoDataValue) {
               m_Result.setNoData(x, y);
            }
            else {
               m_Result.setCellValue(x, y, dDist / dNormalizeFactor);
            }
         }
      }

   }


   private void calculateMahalanobisDistance() {

      int i;
      int x, y;
      double dValue;
      double dDist;
      boolean bNoDataValue;
      final Matrix values = new Matrix(1, m_Windows.length);

      setProgressText(Sextante.getText("Calculating_distances"));
      for (y = 0; (y < m_iNY) && setProgress(y, m_iNY); y++) {
         for (x = 0; x < m_iNX; x++) {
            bNoDataValue = false;
            for (i = 0; i < m_Windows.length; i++) {
               dValue = m_Windows[i].getCellValueAsDouble(x, y);
               if (m_Windows[i].isNoDataValue(dValue)) {
                  bNoDataValue = true;
                  break;
               }
               else {
                  values.set(0, i, dValue - m_dMean[i]);
               }
            }
            if (bNoDataValue) {
               m_Result.setNoData(x, y);
            }
            else {
               final Matrix temp = values.times(m_Inverse);
               dDist = temp.times(values.transpose()).get(0, 0);
               m_Result.setCellValue(x, y, dDist);
            }
         }
      }

   }


   private void calculateStatisticalValues() throws GeoAlgorithmExecutionException {

      int i, j;
      int iCount;
      int iRow, iCol;
      int iValues[];
      double dValue;
      double dCov;
      final double covariances[][] = new double[m_Windows.length][m_Windows.length];
      double values[][];
      Coordinate pt;

      try {
         iCount = m_Points.getShapesCount();
         values = new double[m_Windows.length][iCount];
         iValues = new int[m_Windows.length];
         m_dMean = new double[m_Windows.length];
         m_dMin = new double[m_Windows.length];
         m_dMax = new double[m_Windows.length];
         m_dStdDev = new double[m_Windows.length];
         for (i = 0; i < m_Windows.length; i++) {
            m_dMin[i] = Double.MAX_VALUE;
            m_dMax[i] = Double.NEGATIVE_INFINITY;
         }
         setProgressText(Sextante.getText("Calculating_statistical_values"));
         final IFeatureIterator iter = m_Points.iterator();
         i = 0;
         while (iter.hasNext() && setProgress(i, iCount)) {
            final IFeature feature = iter.next();
            pt = feature.getGeometry().getCoordinate();
            for (j = 0; j < m_Windows.length; j++) {
               dValue = m_Windows[j].getValueAt(pt.x, pt.y);
               if (!m_Windows[j].isNoDataValue(dValue)) {
                  values[j][i] = dValue;
                  m_dMean[j] += dValue;
                  m_dMin[j] = Math.min(m_dMin[j], dValue);
                  m_dMax[j] = Math.max(m_dMax[j], dValue);
                  iValues[j]++;
               }
               else {
                  values[j][i] = NODATA;
               }
            }
            i++;
         }
         iter.close();

         if (m_Task.isCanceled()) {
            return;
         }

         for (i = 0; i < m_Windows.length; i++) {
            if (iValues[i] != 0) {
               m_dMean[i] /= iValues[i];
            }
            else {
               throw new GeoAlgorithmExecutionException(Sextante.getText("Error_calculando_valores_estadisticos"));
            }
         }

         if (m_iMethod == MAHALANOBIS) {
            for (iRow = 0; iRow < m_Windows.length; iRow++) {
               for (iCol = 0; iCol < iRow + 1; iCol++) {
                  dCov = calculateCovariance(values[iCol], m_dMean[iCol], values[iRow], m_dMean[iRow]);
                  if (dCov != NODATA) {
                     covariances[iRow][iCol] = dCov;
                     covariances[iCol][iRow] = dCov;
                  }
                  else {
                     throw new GeoAlgorithmExecutionException(Sextante.getText("Error_calculando_valores_estadisticos"));
                  }
               }
            }

            final Matrix C = new Matrix(covariances);
            m_Inverse = C.inverse();
         }
         else if (m_iMethod == BIOCLIM) {
            for (i = 0; i < m_Windows.length; i++) {
               m_dStdDev[i] = calculateCovariance(values[i], m_dMean[i], values[i], m_dMean[i]);
            }
         }


      }
      catch (final Exception e) {
         throw new GeoAlgorithmExecutionException(Sextante.getText("Error_calculando_valores_estadisticos"));
      }
   }


   private double calculateCovariance(final double[] xx,
                                      final double x,
                                      final double[] yy,
                                      final double y) {

      double dSum = 0;
      int iValues = 0;

      for (int i = 0; i < yy.length; i++) {
         if ((xx[i] != NODATA) && (yy[i] != NODATA)) {
            dSum += (xx[i] - x) * (yy[i] - y);
            iValues++;
         }
      }
      if (iValues > 1) {
         return dSum / (iValues - 1);
      }
      else {
         return NODATA;
      }

   }

}
