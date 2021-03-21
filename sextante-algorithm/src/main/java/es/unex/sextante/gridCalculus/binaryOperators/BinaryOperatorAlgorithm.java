

package es.unex.sextante.gridCalculus.binaryOperators;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;


public abstract class BinaryOperatorAlgorithm
         extends
            GeoAlgorithm {

   public static final String LAYER  = "LAYER";
   public static final String LAYER2 = "LAYER2";
   public static final String RESULT = "RESULT";

   //protected final double     NO_DATA = -99999.0;

   private IRasterLayer       m_window;
   private IRasterLayer       m_window2;

   protected double           m_dValue;
   protected double           m_dValue2;


   @Override
   public void defineCharacteristics() {

      setGroup(Sextante.getText("Calculus_tools_for_raster_layer"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputRasterLayer(LAYER, Sextante.getText("Layer"), true);
         m_Parameters.addInputRasterLayer(LAYER2, Sextante.getText("Layer") + " 2", true);

         addOutputRasterLayer(RESULT, Sextante.getText("Result"), 1);
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int x, y;
      int iNX, iNY;

      m_window = m_Parameters.getParameterValueAsRasterLayer(LAYER);
      m_window2 = m_Parameters.getParameterValueAsRasterLayer(LAYER2);

      m_window.setWindowExtent(m_AnalysisExtent);
      m_window2.setWindowExtent(m_AnalysisExtent);

      final IRasterLayer result = getNewRasterLayer(RESULT, this.getName(), IRasterLayer.RASTER_DATA_TYPE_DOUBLE,
               m_window.getWindowGridExtent());

      result.setNoDataValue(m_OutputFactory.getDefaultNoDataValue());
      result.assignNoData();

      iNX = m_window.getNX();
      iNY = m_window.getNY();

      for (y = 0; (y < iNY) && setProgress(y, iNY); y++) {
         for (x = 0; x < iNX; x++) {
            m_dValue = m_window.getCellValueAsDouble(x, y);
            m_dValue2 = m_window2.getCellValueAsDouble(x, y);
            if (!m_window.isNoDataValue(m_dValue) && !m_window2.isNoDataValue(m_dValue2)) {
               result.setCellValue(x, y, getProcessedValue());
            }
            else {
               result.setNoData(x, y);
            }
         }
      }

      return !m_Task.isCanceled();

   }


   protected abstract double getProcessedValue();

}
