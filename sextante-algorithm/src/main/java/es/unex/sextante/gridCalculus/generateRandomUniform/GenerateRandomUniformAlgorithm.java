package es.unex.sextante.gridCalculus.generateRandomUniform;

import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;

public class GenerateRandomUniformAlgorithm
         extends
            GeoAlgorithm {

   public static final String PROB = "PROB";
   public static final String MAX  = "MAX";
   public static final String MIN  = "MIN";

   private IRasterLayer       m_Result;


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int x, y;
      int iNX, iNY;
      double dValue;

      final double dMin = m_Parameters.getParameterValueAsDouble(MIN);
      final double dMax = m_Parameters.getParameterValueAsDouble(MAX);

      m_Result = getNewRasterLayer(PROB, Sextante.getText("New_layer"), IRasterLayer.RASTER_DATA_TYPE_FLOAT);

      iNX = m_Result.getWindowGridExtent().getNX();
      iNY = m_Result.getWindowGridExtent().getNY();

      for (y = 0; y < iNY; y++) {
         for (x = 0; x < iNX; x++) {
            dValue = Math.random() * (dMax - dMin) + dMin;
            m_Result.setCellValue(x, y, dValue);
         }
      }

      return !m_Task.isCanceled();
   }


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Random_grid__uniform"));
      setGroup(Sextante.getText("Raster_creation_tools"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addNumericalValue(MIN, Sextante.getText("Min_value"), 0,
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE);
         m_Parameters.addNumericalValue(MAX, Sextante.getText("Max_value"), 1,
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE);
         addOutputRasterLayer(PROB, Sextante.getText("Probability"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }

}
