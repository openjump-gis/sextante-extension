package es.unex.sextante.statisticalMethods.pdfChiSquared;

import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.math.pdf.PDF;

public class PDFChiSquaredAlgorithm
         extends
            GeoAlgorithm {

   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Chi_squared_probability_distribution"));
      setGroup(Sextante.getText("Statistical_methods"));
      setUserCanDefineAnalysisExtent(false);

      try {
         m_Parameters.addInputRasterLayer("INPUT", Sextante.getText("Raster_layer"), true);
         m_Parameters.addNumericalValue("DEGREES", Sextante.getText("Degrees_of_freedom"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_INTEGER, 2, 0, Integer.MAX_VALUE);
         m_Parameters.addBoolean("CDF", Sextante.getText("Accumulated_probability"), false);
         addOutputRasterLayer("PROBABILITY", Sextante.getText("Result"));
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
      int iDegrees;
      boolean bCDF;

      final IRasterLayer window = m_Parameters.getParameterValueAsRasterLayer("INPUT");
      bCDF = m_Parameters.getParameterValueAsBoolean("CDF");
      iDegrees = m_Parameters.getParameterValueAsInt("DEGREES");
      window.setFullExtent();
      final AnalysisExtent gridExtent = new AnalysisExtent(window);
      final IRasterLayer result = getNewRasterLayer("PROBABILITY", Sextante.getText("Probability__chi_squared"),
               IRasterLayer.RASTER_DATA_TYPE_DOUBLE, gridExtent);

      iNX = window.getNX();
      iNY = window.getNY();

      for (y = 0; y < iNY && setProgress(y, iNY); y++) {
         for (x = 0; x < iNX; x++) {
            dValue = window.getCellValueAsDouble(x, y);
            if (!window.isNoDataValue(dValue)) {
               if (bCDF) {
                  result.setCellValue(x, y, PDF.chiSquareCDF(dValue, iDegrees));
               }
               else {
                  result.setCellValue(x, y, PDF.chiSquare(dValue, iDegrees));
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
