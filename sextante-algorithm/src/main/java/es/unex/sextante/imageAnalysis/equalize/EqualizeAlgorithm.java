package es.unex.sextante.imageAnalysis.equalize;

import java.awt.image.DataBuffer;

import es.unex.sextante.core.Sextante;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.imageAnalysis.pixelOpsBase.PixelOpsBaseAlgorithm;

public class EqualizeAlgorithm
         extends
            PixelOpsBaseAlgorithm {

   private int[]  m_Histogram;
   private double m_dRange;
   private double m_dMin;
   private int    m_iCells;
   private int    m_iSize;


   @Override
   public void defineCharacteristics() {

      super.defineCharacteristics();

      setName(Sextante.getText("Equalize"));

   }


   @Override
   protected double getValueAt(final int x,
                               final int y) {

      final double dValue = m_Image.getCellValueAsDouble(x, y);
      final int iClass = (int) (((dValue - m_dMin) / m_dRange) * m_iSize);

      return (double) m_Histogram[iClass] / (double) m_iCells * m_iSize;

   }


   @Override
   protected void setValues() throws GeoAlgorithmExecutionException {

      super.setValues();
      m_Histogram = m_Image.getAccumulatedHistogram();
      m_iCells = m_Histogram[m_Histogram.length - 1];
      m_dMin = m_Image.getMinValue();
      m_dRange = m_Image.getMaxValue() - m_dMin;
      m_iSize = (int) Math.pow(2., DataBuffer.getDataTypeSize(m_Image.getDataType())) - 1;

   }


}
