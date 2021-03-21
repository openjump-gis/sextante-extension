package es.unex.sextante.gridTools.invertNoData;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;

public class InvertNoDataAlgorithm
         extends
            GeoAlgorithm {

   public static final String RESULT = "RESULT";
   public static final String INPUT  = "INPUT";


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Invert_mask"));
      setGroup(Sextante.getText("Basic_tools_for_raster_layers"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputRasterLayer(INPUT, Sextante.getText("Layer"), true);
         addOutputRasterLayer(RESULT, Sextante.getText("Inverted_mask"));
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
      IRasterLayer input;

      input = m_Parameters.getParameterValueAsRasterLayer(INPUT);

      final IRasterLayer result = getNewRasterLayer(RESULT, Sextante.getText("Inverted_mask"), IRasterLayer.RASTER_DATA_TYPE_BYTE);


      result.setNoDataValue(0.0);

      input.setWindowExtent(result.getWindowGridExtent());

      iNX = result.getWindowGridExtent().getNX();
      iNY = result.getWindowGridExtent().getNY();

      for (y = 0; (y < iNY) && setProgress(y, iNY); y++) {
         for (x = 0; x < iNX; x++) {
            dValue = input.getCellValueAsDouble(x, y);
            if (input.isNoDataValue(dValue)) {
               result.setCellValue(x, y, 1.0);
            }
            else {
               result.setNoData(x, y);
            }
         }
      }

      return !m_Task.isCanceled();

   }


}
