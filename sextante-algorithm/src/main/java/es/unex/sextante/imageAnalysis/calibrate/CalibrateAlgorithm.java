package es.unex.sextante.imageAnalysis.calibrate;

import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;

public class CalibrateAlgorithm
         extends
            GeoAlgorithm {

   private static final String INPUT      = "INPUT";
   private static final String METHOD     = "METHOD";
   private static final String STDDEV     = "STDDEV";
   private static final String MEAN       = "MEAN";
   private static final String GAIN       = "GAIN";
   private static final String OFFSET     = "OFFSET";
   private static final String CALIBRATED = "CALIBRATED";


   @Override
   public void defineCharacteristics() {

      final String[] sMethod = { Sextante.getText("Average_and_standard_deviation"), Sextante.getText("Gain_and_offset") };

      setName(Sextante.getText("Calibrate_an_image"));
      setGroup(Sextante.getText("Image_processing"));
      setUserCanDefineAnalysisExtent(false);

      try {
         m_Parameters.addInputRasterLayer(INPUT, Sextante.getText("Image_to_calibrate"), true);
         m_Parameters.addSelection(METHOD, Sextante.getText("Method"), sMethod);
         m_Parameters.addNumericalValue(STDDEV, Sextante.getText("Resulting_standard_deviation"), 1,
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE);
         m_Parameters.addNumericalValue(MEAN, Sextante.getText("Resulting_average"), 0,
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE);
         m_Parameters.addNumericalValue(GAIN, Sextante.getText("Gain"), 1, AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE);
         m_Parameters.addNumericalValue(OFFSET, Sextante.getText("Offset"), 0,
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE);
         addOutputRasterLayer(CALIBRATED, Sextante.getText("Calibrated_image"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int x, y;
      int iNX, iNY;
      double dCellValue;
      double dOutput;
      double dMeanIn, dStdDevIn;
      IRasterLayer input;
      IRasterLayer output;

      input = m_Parameters.getParameterValueAsRasterLayer(INPUT);
      final double dMean = m_Parameters.getParameterValueAsDouble(MEAN);
      final double dStdDev = m_Parameters.getParameterValueAsDouble(STDDEV);
      final double dGain = m_Parameters.getParameterValueAsDouble(GAIN);
      final double dOffset = m_Parameters.getParameterValueAsDouble(OFFSET);
      final int iMethod = m_Parameters.getParameterValueAsInt(METHOD);

      input.setFullExtent();

      output = getNewRasterLayer(CALIBRATED, Sextante.getText("Calibrated_image"), input.getDataType(),
               input.getWindowGridExtent());

      iNX = input.getNX();
      iNY = input.getNY();

      if (iMethod == 0) { //mean and stddev
         dMeanIn = input.getMeanValue();
         dStdDevIn = Math.sqrt(input.getVariance());
         for (y = 0; (y < iNY) && setProgress(y, iNY); y++) {
            for (x = 0; x < iNX; x++) {
               dCellValue = input.getCellValueAsDouble(x, y);
               if (input.isNoDataValue(dCellValue)) {
                  output.setNoData(x, y);
               }
               else {
                  dOutput = (((dCellValue - dMeanIn) / dStdDevIn) * dStdDev) + dMean;
                  output.setCellValue(x, y, dOutput);
               }
            }

         }
      }
      else { // gain and offset
         for (y = 0; (y < iNY) && setProgress(y, iNY); y++) {
            for (x = 0; x < iNX; x++) {
               dCellValue = input.getCellValueAsDouble(x, y);
               if (input.isNoDataValue(dCellValue)) {
                  output.setNoData(x, y);
               }
               else {
                  output.setCellValue(x, y, dGain * dCellValue + dOffset);
               }
            }

         }
      }

      return !m_Task.isCanceled();

   }

}
