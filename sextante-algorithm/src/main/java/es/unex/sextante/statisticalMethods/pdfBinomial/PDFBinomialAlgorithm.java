package es.unex.sextante.statisticalMethods.pdfBinomial;

import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.math.pdf.PDF;

public class PDFBinomialAlgorithm
         extends
            GeoAlgorithm {

   public static final String INPUT       = "INPUT";
   public static final String P           = "P";
   public static final String N           = "N";
   public static final String CDF         = "CDF";
   public static final String PROBABILITY = "PROBABILITY";


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Bernouilli_probability_distribution"));
      setGroup(Sextante.getText("Statistical_methods"));
      setUserCanDefineAnalysisExtent(false);

      try {
         m_Parameters.addInputRasterLayer(INPUT, Sextante.getText("Raster_layer"), true);
         m_Parameters.addNumericalValue(P, Sextante.getText("Success_probability"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE, 0.5, 0, 1);
         m_Parameters.addNumericalValue(N, Sextante.getText("Number_of_trials"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_INTEGER, 10, 1, Integer.MAX_VALUE);
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
      int iN;
      int iNX, iNY;
      double dValue;
      double dProb;
      boolean bCDF;

      final IRasterLayer layer = m_Parameters.getParameterValueAsRasterLayer(INPUT);
      bCDF = m_Parameters.getParameterValueAsBoolean(CDF);
      dProb = m_Parameters.getParameterValueAsDouble(P);
      iN = m_Parameters.getParameterValueAsInt(N);
      layer.setFullExtent();
      final AnalysisExtent gridExtent = new AnalysisExtent(layer);
      final IRasterLayer result = getNewRasterLayer(PROBABILITY, Sextante.getText("Probability__binomial"),
               IRasterLayer.RASTER_DATA_TYPE_DOUBLE, gridExtent);

      iNX = layer.getNX();
      iNY = layer.getNY();

      for (y = 0; y < iNY && setProgress(y, iNY); y++) {
         for (x = 0; x < iNX; x++) {
            dValue = layer.getCellValueAsDouble(x, y);
            if (!layer.isNoDataValue(dValue)) {
               if (bCDF) {
                  result.setCellValue(x, y, PDF.binomialCDF(dProb, iN, (int) dValue));
               }
               else {
                  result.setCellValue(x, y, PDF.binomial(dProb, iN, (int) dValue));
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
