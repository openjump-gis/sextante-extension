package es.unex.sextante.imageAnalysis.contrastStretching;

import java.awt.image.DataBuffer;

import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.imageAnalysis.pixelOpsBase.PixelOpsBaseAlgorithm;


public class ContrastStretchingAlgorithm
         extends
            PixelOpsBaseAlgorithm {

   public static final String LOWER = "LOWER";
   public static final String UPPER = "UPPER";

   private int                m_iUpper, m_iLower;
   private int[]              m_Histogram;
   private int                m_iSize;
   private double             m_dUpper, m_dLower;
   private double             m_dRange;


   @Override
   public void defineCharacteristics() {

      super.defineCharacteristics();

      setName(Sextante.getText("Contrast_stretching"));

      try {
         m_Parameters.addNumericalValue(LOWER, Sextante.getText("Lower_percentile"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_INTEGER, 0, 0, 99);
         m_Parameters.addNumericalValue(UPPER, Sextante.getText("Upper_percentile"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_INTEGER, 100, 1, 100);
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   protected void setValues() throws GeoAlgorithmExecutionException {

      super.setValues();

      m_Histogram = m_Image.getAccumulatedHistogram();
      final double dMin = m_dLower = m_Image.getMinValue();
      final double dMax = m_dUpper = m_Image.getMaxValue();
      final double dRange = dMax - dMin;
      m_iLower = m_Parameters.getParameterValueAsInt(LOWER);
      m_iUpper = m_Parameters.getParameterValueAsInt(UPPER);
      if (m_iLower > m_iUpper) {
         final int iSwap = m_iUpper;
         m_iUpper = m_iLower;
         m_iLower = iSwap;
      }
      m_iSize = (int) (Math.pow(2., DataBuffer.getDataTypeSize(m_Image.getDataType())) / 2.) - 1;
      m_iLower = (int) (m_Histogram[m_Histogram.length - 1] * (double) m_iLower / 100.);
      m_iUpper = (int) (m_Histogram[m_Histogram.length - 1] * (double) m_iUpper / 100.);
      for (int i = 0; i < m_Histogram.length - 1; i++) {
         if ((m_Histogram[i] < m_iLower) && (m_Histogram[i + 1] > m_iLower)) {
            m_dLower = dMin + (i + 1) * dRange / m_iSize;
         }
         if ((m_Histogram[i] < m_iUpper) && (m_Histogram[i + 1] > m_iUpper)) {
            m_dUpper = dMin + (i + 1) * dRange / m_iSize;
         }
      }
      m_dRange = m_dUpper - m_dLower;

   }


   @Override
   protected double getValueAt(final int x,
                               final int y) {

      final double dValue = m_Image.getCellValueAsDouble(x, y);
      double dRet = (dValue - m_dLower) / m_dRange * m_iSize;

      if (dRet > m_iSize) {
         dRet = m_iSize;
      }
      if (dRet < 0) {
         dRet = 0;
      }

      return dRet;

   }

}
