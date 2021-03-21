package es.unex.sextante.imageAnalysis.calibrateRegression;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.math.regression.Regression;

public class CalibrateRegressionAlgorithm
         extends
            GeoAlgorithm {

   public static final String CALIBRATED = "CALIBRATED";
   public static final String INPUT      = "INPUT";
   public static final String REF        = "REF";


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Calibrate_an_image__regression"));
      setUserCanDefineAnalysisExtent(false);
      setGroup(Sextante.getText("Image_processing"));

      try {
         m_Parameters.addInputRasterLayer(INPUT, Sextante.getText("Image_to_calibrate"), true);
         m_Parameters.addInputRasterLayer(REF, Sextante.getText("Reference_image"), false);
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
      double dCellValue, dCellValue2;
      final Regression regression = new Regression();
      IRasterLayer input, ref;
      IRasterLayer output;

      input = m_Parameters.getParameterValueAsRasterLayer(INPUT);
      ref = m_Parameters.getParameterValueAsRasterLayer(REF);
      input.setFullExtent();
      ref.setWindowExtent(input.getWindowGridExtent());
      output = getNewRasterLayer(CALIBRATED, Sextante.getText("Calibrated_image"), input.getDataType(),
               input.getWindowGridExtent());
      if ((ref.getDataType() == IRasterLayer.RASTER_DATA_TYPE_INT) || (ref.getDataType() == IRasterLayer.RASTER_DATA_TYPE_DOUBLE)) {
         ref.setInterpolationMethod(IRasterLayer.INTERPOLATION_NearestNeighbour);
      }

      iNX = input.getNX();
      iNY = input.getNY();

      for (y = 0; (y < iNY) && setProgress(y, iNY); y++) {
         for (x = 0; x < iNX; x++) {
            dCellValue = input.getCellValueAsDouble(x, y);
            dCellValue2 = ref.getCellValueAsDouble(x, y);
            if (!input.isNoDataValue(dCellValue) && !ref.isNoDataValue(dCellValue2)) {
               regression.addValue(dCellValue, dCellValue2);
            }
         }

      }

      regression.calculate();

      for (y = 0; (y < iNY) && setProgress(y, iNY); y++) {
         for (x = 0; x < iNX; x++) {
            dCellValue = input.getCellValueAsDouble(x, y);
            if (input.isNoDataValue(dCellValue)) {
               output.setNoData(x, y);
            }
            else {
               output.setCellValue(x, y, regression.getY(dCellValue));
            }
         }

      }

      return !m_Task.isCanceled();


   }

}
