package es.unex.sextante.gridCategorical.reclassifyEqualAmplitude;

import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;

public class ReclassifyEqualAmplitudeAlgorithm
         extends
            GeoAlgorithm {

   public static final String CLASSCOUNT = "CLASSCOUNT";
   public static final String RECLASS    = "RECLASS";
   public static final String INPUT      = "INPUT";


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Divide_into_n_classes_of_equal_amplitude"));
      setGroup(Sextante.getText("Reclassify_raster_layers"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputRasterLayer(INPUT, Sextante.getText("Layer"), true);
         m_Parameters.addNumericalValue(CLASSCOUNT, Sextante.getText("Number_of_classes"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_INTEGER, 10, 2, Integer.MAX_VALUE);
         addOutputRasterLayer(RECLASS, Sextante.getText("Reclassify"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int x, y;
      int iNX, iNY;
      int iClass;
      double dValue;
      double dClassAmplitude;

      final IRasterLayer grid = m_Parameters.getParameterValueAsRasterLayer(INPUT);
      final int iClasses = m_Parameters.getParameterValueAsInt(CLASSCOUNT);

      final IRasterLayer result = getNewRasterLayer(RECLASS, grid.getName() + Sextante.getText("[reclassified]"),
               IRasterLayer.RASTER_DATA_TYPE_INT);

      grid.setWindowExtent(result.getWindowGridExtent());

      final double dMax = grid.getMaxValue();
      final double dMin = grid.getMinValue();

      if (dMax != dMin) {
         dClassAmplitude = (dMax - dMin) / iClasses;

         iNX = grid.getNX();
         iNY = grid.getNY();

         for (y = 0; (y < iNY) && setProgress(y, iNY); y++) {
            for (x = 0; x < iNX; x++) {
               dValue = grid.getCellValueAsDouble(x, y);
               if (!grid.isNoDataValue(dValue)) {
                  iClass = (int) Math.floor(Math.min((dValue - dMin) / dClassAmplitude, iClasses - 1));
                  result.setCellValue(x, y, iClass + 1);
               }
               else {
                  result.setNoData(x, y);
               }
            }
         }
      }

      return !m_Task.isCanceled();

   }


}
