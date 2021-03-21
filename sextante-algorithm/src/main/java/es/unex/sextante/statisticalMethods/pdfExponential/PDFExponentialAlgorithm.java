package es.unex.sextante.statisticalMethods.pdfExponential;

import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.math.pdf.PDF;

public class PDFExponentialAlgorithm
         extends
            GeoAlgorithm {

   public static final String INPUT       = "INPUT";
   public static final String CDF         = "CDF";
   public static final String MEAN        = "MEAN";
   public static final String STDDEV      = "STDDEV";
   public static final String PROBABILITY = "PROBABILITY";


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Exponential_probability_distribution"));
      setGroup(Sextante.getText("Statistical_methods"));
      setUserCanDefineAnalysisExtent(false);

      try {
         m_Parameters.addInputRasterLayer(INPUT, Sextante.getText("Raster_layer"), true);
         m_Parameters.addNumericalValue(MEAN, Sextante.getText("Mean"), 0, AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE);
         m_Parameters.addNumericalValue(STDDEV, Sextante.getText("Standard_deviation"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE, 1, 0, Double.MAX_VALUE);
         m_Parameters.addBoolean(CDF, Sextante.getText("Accumulated_probability"), false);
         addOutputRasterLayer(PROBABILITY, Sextante.getText("Result"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int x, y;
      int iNX, iNY;
      double dValue;
      double dMean, dStdDev;
      boolean bCDF;

      final IRasterLayer window = m_Parameters.getParameterValueAsRasterLayer(INPUT);
      bCDF = m_Parameters.getParameterValueAsBoolean(CDF);
      dMean = m_Parameters.getParameterValueAsDouble(MEAN);
      dStdDev = m_Parameters.getParameterValueAsDouble(STDDEV);
      window.setFullExtent();
      final AnalysisExtent gridExtent = new AnalysisExtent(window);
      final IRasterLayer result = getNewRasterLayer(PROBABILITY, Sextante.getText("Probability__exponential"),
               IRasterLayer.RASTER_DATA_TYPE_DOUBLE, gridExtent);

      iNX = window.getNX();
      iNY = window.getNY();

      for (y = 0; y < iNY && setProgress(y, iNY); y++) {
         for (x = 0; x < iNX; x++) {
            dValue = window.getCellValueAsDouble(x, y);
            if (!window.isNoDataValue(dValue)) {
               if (bCDF) {
                  result.setCellValue(x, y, PDF.exponentialCDF(dMean, dStdDev, dValue));
               }
               else {
                  result.setCellValue(x, y, PDF.exponential(dMean, dStdDev, dValue));
               }
            }
            else {
               result.setNoData(x, y);
            }
         }
      }

      return !m_Task.isCanceled();

   }

}
