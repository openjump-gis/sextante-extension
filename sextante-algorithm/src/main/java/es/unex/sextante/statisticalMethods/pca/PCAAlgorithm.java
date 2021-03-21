

package es.unex.sextante.statisticalMethods.pca;


import java.util.ArrayList;
import java.util.Arrays;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;
import es.unex.sextante.additionalInfo.AdditionalInfoMultipleInput;
import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.outputs.OutputRasterLayer;
import es.unex.sextante.parameters.RasterLayerAndBand;


public class PCAAlgorithm
         extends
            GeoAlgorithm {

   public static final String RESULT = "RESULT";
   public static final String NBANDS = "NBANDS";
   public static final String INPUT  = "INPUT";

   private static double      NODATA = -9999999.;

   private ArrayList          m_RasterLayers;
   private IRasterLayer[]     m_Windows;
   private int                m_iBands[];
   private int                m_iNX, m_iNY;
   private double             m_dMean[];
   private int                m_iNumBands;


   @Override
   public void defineCharacteristics() {

      setUserCanDefineAnalysisExtent(true);
      setGroup(Sextante.getText("Statistical_methods"));
      setName(Sextante.getText("Principal_Components_Analysis"));

      try {
         m_Parameters.addMultipleInput(INPUT, Sextante.getText("Input_bands"), AdditionalInfoMultipleInput.DATA_TYPE_BAND, true);
         m_Parameters.addNumericalValue(NBANDS, Sextante.getText("Bands_number"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_INTEGER, 1, 1, Integer.MAX_VALUE);
         addOutputRasterLayer(RESULT, Sextante.getText("Principal_Components_Analysis"),
                  OutputRasterLayer.NUMBER_OF_BANDS_UNDEFINED);
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i;
      int x, y;
      int iBand;
      int iInputBand;
      double dValue;
      double dPCValue;

      m_RasterLayers = m_Parameters.getParameterValueAsArrayList(INPUT);
      m_iNumBands = m_Parameters.getParameterValueAsInt(NBANDS);

      if ((m_RasterLayers.size() == 0) || (m_iNumBands > m_RasterLayers.size())) {
         throw new GeoAlgorithmExecutionException(Sextante.getText("Error_not_enough_bands_selected"));
      }

      m_iNX = getAnalysisExtent().getNX();
      m_iNY = getAnalysisExtent().getNY();

      final double coVar[][] = getCovarMatrix();
      final Matrix coVarMatrix = new Matrix(coVar);
      final EigenvalueDecomposition eigenvalueDecomp = new EigenvalueDecomposition(coVarMatrix);
      final Matrix eigenvectors = eigenvalueDecomp.getV();
      final double[] eigenvalues = eigenvalueDecomp.getRealEigenvalues();
      final ValueAndOrder[] vao = new ValueAndOrder[eigenvalues.length];
      for (i = 0; i < eigenvalues.length; i++) {
         vao[i] = new ValueAndOrder(eigenvalues[i], i);
      }

      Arrays.sort(vao);
      final IRasterLayer pca = getNewRasterLayer(RESULT, Sextante.getText("Principal_Components_Analysis"),
               IRasterLayer.RASTER_DATA_TYPE_DOUBLE, m_iNumBands);

      for (y = 0; y < m_iNY; y++) {
         for (x = 0; x < m_iNX; x++) {
            for (iBand = 0; (iBand < m_iNumBands) && setProgress(iBand, m_iNumBands); iBand++) {
               dPCValue = 0;
               for (iInputBand = 0; iInputBand < m_RasterLayers.size(); iInputBand++) {
                  dValue = m_Windows[iInputBand].getCellValueAsDouble(x, y, m_iBands[iInputBand]);
                  dPCValue += (eigenvectors.get(iInputBand, vao[iBand].getOrder()) * dValue);
               }
               pca.setCellValue(x, y, iBand, dPCValue);
            }
         }
      }


      return !m_Task.isCanceled();

   }


   private double[][] getCovarMatrix() {

      int i, j;

      final double dCovar[][] = new double[m_RasterLayers.size()][m_RasterLayers.size()];
      m_dMean = new double[m_RasterLayers.size()];

      m_Windows = new IRasterLayer[m_RasterLayers.size()];
      m_iBands = new int[m_RasterLayers.size()];

      for (i = 0; i < m_RasterLayers.size(); i++) {
         final RasterLayerAndBand rab = (RasterLayerAndBand) m_RasterLayers.get(i);
         m_Windows[i] = rab.getRasterLayer();
         m_Windows[i].setWindowExtent(this.getAnalysisExtent());
         m_Windows[i].setInterpolationMethod(IRasterLayer.INTERPOLATION_BSpline);
         m_dMean[i] = m_Windows[i].getMeanValue();
         m_iBands[i] = rab.getBand();
      }


      final int iTotal = (int) (m_RasterLayers.size() * m_RasterLayers.size() / 2.);
      int iCount = 0;
      for (i = 0; (i < m_RasterLayers.size() - 1) && setProgress(iCount, iTotal); i++) {
         dCovar[i][i] = 1.0;
         iCount++;
         for (j = i + 1; j < m_RasterLayers.size(); j++) {
            dCovar[i][j] = dCovar[j][i] = getCovar(i, j);
            iCount++;
         }
      }


      return dCovar;

   }


   private double getCovar(final int i,
                           final int j) {

      int x, y;
      int iValues = 0;
      double dValuei, dValuej;
      double dSum = 0;

      for (y = 0; y < m_iNY; y++) {
         for (x = 0; x < m_iNX; x++) {
            dValuei = m_Windows[i].getCellValueAsDouble(x, y, m_iBands[i]);
            dValuej = m_Windows[j].getCellValueAsDouble(x, y, m_iBands[j]);
            if (!m_Windows[i].isNoDataValue(dValuei) && !m_Windows[j].isNoDataValue(dValuej)) {
               dSum += (dValuei - m_dMean[i]) * (dValuej - m_dMean[j]);
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

      return NODATA;

   }

   private class ValueAndOrder
            implements
               Comparable {

      private final int    m_iOrder;
      private final double m_dValue;


      public ValueAndOrder(final double dValue,
                           final int iOrder) {

         m_dValue = dValue;
         m_iOrder = iOrder;

      }


      public int getOrder() {

         return m_iOrder;

      }


      public double getValue() {

         return m_dValue;

      }


      public int compareTo(final Object vao) throws ClassCastException {

         if (!(vao instanceof ValueAndOrder)) {
            throw new ClassCastException();
         }

         final double dValue = ((ValueAndOrder) vao).getValue();
         final double dDif = this.m_dValue - dValue;

         if (dDif > 0.0) {
            return 1;
         }
         else if (dDif < 0.0) {
            return -1;
         }
         else {
            return 0;
         }

      }
   }
}
