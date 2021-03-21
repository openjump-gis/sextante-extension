package es.unex.sextante.gridTools.gridCompletion;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;

public class GridCompletionAlgorithm
         extends
            GeoAlgorithm {

   public static final String INPUT      = "INPUT";
   public static final String ADDITIONAL = "ADDITIONAL";
   public static final String METHOD     = "METHOD";
   public static final String RESULT     = "RESULT";


   @Override
   public void defineCharacteristics() {

      final String[] sMethod = { Sextante.getText("Nearest_neighbour"), Sextante.getText("Bilinear"),
               Sextante.getText("Inverse_distance"), Sextante.getText("Cubic_spline"), Sextante.getText("B-spline") };

      setName(Sextante.getText("Complete_grid"));
      setGroup(Sextante.getText("Basic_tools_for_raster_layers"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputRasterLayer(INPUT, Sextante.getText("Base_layer"), true);
         m_Parameters.addInputRasterLayer(ADDITIONAL, Sextante.getText("Additional_layer"), true);
         m_Parameters.addSelection(METHOD, Sextante.getText("Interpolation_method"), sMethod);
         addOutputRasterLayer(RESULT, Sextante.getText("Filled_layer"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int x, y;
      int iMethod;
      int iNX, iNY;
      double dValue = 0;
      IRasterLayer window, window2;

      iMethod = m_Parameters.getParameterValueAsInt(METHOD);
      window = m_Parameters.getParameterValueAsRasterLayer(INPUT);
      window2 = m_Parameters.getParameterValueAsRasterLayer(ADDITIONAL);
      final IRasterLayer result = getNewRasterLayer(RESULT, window.getName() + Sextante.getText("[completed]"),
               window.getDataType());

      window.setWindowExtent(result.getWindowGridExtent());
      window.setInterpolationMethod(iMethod);
      window2.setWindowExtent(result.getWindowGridExtent());
      window.setInterpolationMethod(iMethod);

      iNX = window.getNX();
      iNY = window.getNY();

      for (y = 0; (y < iNY) && setProgress(y, iNY); y++) {
         for (x = 0; x < iNX; x++) {
            dValue = window.getCellValueAsDouble(x, y);
            if (window.isNoDataValue(dValue)) {
               dValue = window2.getCellValueAsDouble(x, y);
               if (window2.isNoDataValue(dValue)) {
                  result.setNoData(x, y);
               }
               else {
                  result.setCellValue(x, y, dValue);
               }
            }
            else {
               result.setCellValue(x, y, dValue);
            }
         }

      }

      return !m_Task.isCanceled();

   }


}
