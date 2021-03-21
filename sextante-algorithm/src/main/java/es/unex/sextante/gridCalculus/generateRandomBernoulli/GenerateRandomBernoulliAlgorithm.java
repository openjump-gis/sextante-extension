package es.unex.sextante.gridCalculus.generateRandomBernoulli;

import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;

public class GenerateRandomBernoulliAlgorithm
         extends
            GeoAlgorithm {

   public static final String RESULT = "RESULT";
   public static final String PROB   = "PROB";

   private IRasterLayer       m_Result;


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int x, y;
      int iNX, iNY;
      int iValue;
      double dValue;

      final double dProb = m_Parameters.getParameterValueAsDouble(PROB) / 100.;

      m_Result = getNewRasterLayer(RESULT, Sextante.getText("New_layer"), IRasterLayer.RASTER_DATA_TYPE_INT);

      iNX = m_Result.getWindowGridExtent().getNX();
      iNY = m_Result.getWindowGridExtent().getNY();

      for (y = 0; y < iNY; y++) {
         for (x = 0; x < iNX; x++) {
            dValue = Math.random();
            if (dValue < dProb) {
               iValue = 1;
            }
            else {
               iValue = 0;
            }
            m_Result.setCellValue(x, y, iValue);
         }
      }

      return !m_Task.isCanceled();

   }


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Random_grid__Bernouilli"));
      setGroup(Sextante.getText("Raster_creation_tools"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addNumericalValue(PROB, Sextante.getText("Probability__%"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE, 50, 0, 100);
         addOutputRasterLayer(RESULT, Sextante.getText("Probability"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }

}
