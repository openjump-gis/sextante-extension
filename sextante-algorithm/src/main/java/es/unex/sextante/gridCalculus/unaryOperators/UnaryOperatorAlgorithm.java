

package es.unex.sextante.gridCalculus.unaryOperators;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;


public abstract class UnaryOperatorAlgorithm
         extends
            GeoAlgorithm {

   public static final String LAYER  = "LAYER";
   public static final String RESULT = "RESULT";

   //protected final double     NO_DATA = -99999.0;

   private IRasterLayer       m_window;

   protected double           m_dValue;


   @Override
   public void defineCharacteristics() {

      setGroup(Sextante.getText("Calculus_tools_for_raster_layer"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputRasterLayer(LAYER, Sextante.getText("Layer"), true);
         addOutputRasterLayer(RESULT, Sextante.getText("Result"));
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

      m_window.setWindowExtent(m_AnalysisExtent);

      final IRasterLayer result = getNewRasterLayer(RESULT, this.getName(), IRasterLayer.RASTER_DATA_TYPE_DOUBLE,
               m_window.getWindowGridExtent());

      result.setNoDataValue(m_OutputFactory.getDefaultNoDataValue());
      result.assignNoData();

      iNX = m_window.getNX();
      iNY = m_window.getNY();

      for (y = 0; (y < iNY) && setProgress(y, iNY); y++) {
         for (x = 0; x < iNX; x++) {
            m_dValue = m_window.getCellValueAsDouble(x, y);
            if (!m_window.isNoDataValue(m_dValue)) {
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
