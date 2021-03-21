package es.unex.sextante.imageAnalysis.texture.quantization;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.imageAnalysis.texture.base.BaseTextureAnalysisAlgorithm;

public class QuantizationAlgorithm
         extends
            GeoAlgorithm {

   public static final String  GRID    = "GRID";
   public static final String  RESULT  = "RESULT";
   private static final double CLASSES = BaseTextureAnalysisAlgorithm.GRAYSCALE_LEVELS - 1;

   private int                 m_iNX, m_iNY;

   private IRasterLayer        m_Grid  = null;
   private IRasterLayer        m_Result;


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      m_Grid = m_Parameters.getParameterValueAsRasterLayer(GRID);

      m_Result = getNewRasterLayer(RESULT, m_Grid.getName() + Sextante.getText("[4 bits]"), IRasterLayer.RASTER_DATA_TYPE_BYTE);

      m_Result.setNoDataValue(-1.);

      m_Grid.setWindowExtent(getAnalysisExtent());

      m_iNX = m_Grid.getNX();
      m_iNY = m_Grid.getNY();

      return normalize();

   }


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Quantization_[4_bits]"));
      setGroup(Sextante.getText("Image_processing"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputRasterLayer(GRID, Sextante.getText("Image"), true);
         addOutputRasterLayer(RESULT, Sextante.getText("Result"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   private boolean normalize() throws GeoAlgorithmExecutionException {

      int x, y;
      double z;
      double dValue1 = 0, dValue2 = 0;

      dValue1 = m_Grid.getMinValue();
      dValue2 = m_Grid.getMaxValue() - dValue1;

      if (dValue2 == 0) {
         m_Result.assign(1.);
         return true;
      }

      for (y = 0; (y < m_iNY) && setProgress(y, m_iNY); y++) {
         for (x = 0; x < m_iNX; x++) {
            z = m_Grid.getCellValueAsDouble(x, y);
            if (!m_Grid.isNoDataValue(z)) {
               m_Result.setCellValue(x, y, Math.floor((z - dValue1) / dValue2) * CLASSES);
            }
            else {
               m_Result.setNoData(x, y);
            }
         }
      }

      return !m_Task.isCanceled();

   }

}
