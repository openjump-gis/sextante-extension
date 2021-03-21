package es.unex.sextante.gridCalculus.generateRandomNormal;

import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;

public class GenerateRandomNormalAlgorithm
         extends
            GeoAlgorithm {

   public static final String PROB   = "PROB";
   public static final String STDDEV = "STDDEV";
   public static final String MEAN   = "MEAN";

   private IRasterLayer       m_Result;
   private double             m_dMean, m_dStdDev;


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int x, y;
      int iNX, iNY;
      double dValue;

      m_dMean = m_Parameters.getParameterValueAsDouble(MEAN);
      m_dStdDev = m_Parameters.getParameterValueAsDouble(STDDEV);

      m_Result = getNewRasterLayer(PROB, Sextante.getText("New_layer"), IRasterLayer.RASTER_DATA_TYPE_DOUBLE);

      iNX = m_Result.getWindowGridExtent().getNX();
      iNY = m_Result.getWindowGridExtent().getNY();

      for (y = 0; y < iNY; y++) {
         for (x = 0; x < iNX; x++) {
            dValue = getValue();
            m_Result.setCellValue(x, y, dValue);
         }
      }

      return !m_Task.isCanceled();
   }


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Random_grid__normal"));
      setGroup(Sextante.getText("Raster_creation_tools"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addNumericalValue(MEAN, Sextante.getText("Mean"), 0, AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE);
         m_Parameters.addNumericalValue(STDDEV, Sextante.getText("Standard_deviation"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE, 1, 0, Double.MAX_VALUE);
         addOutputRasterLayer(PROB, Sextante.getText("Probability"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   double getValue() {


      double x1, x2, w, y1;

      do {
         x1 = 2.0 * Math.random() - 1.0;
         x2 = 2.0 * Math.random() - 1.0;

         w = x1 * x1 + x2 * x2;
      }
      while (w >= 1.0);

      w = Math.sqrt((-2.0 * Math.log(w)) / w);

      y1 = x1 * w;

      return (m_dMean + m_dStdDev * y1);

   }

}
