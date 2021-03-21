package es.unex.sextante.gridTools.changeDataType;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;

public class ChangeDataTypeAlgorithm
         extends
            GeoAlgorithm {

   public static final String INPUT  = "INPUT";
   public static final String TYPE   = "TYPE";
   public static final String RESULT = "RESULT";

   public static final int    BYTE   = 0;
   public static final int    SHORT  = 0;
   public static final int    INT    = 0;
   public static final int    FLOAT  = 0;
   public static final int    DOUBLE = 0;


   @Override
   public void defineCharacteristics() {

      final String sTypes[] = { "Byte", "Short", "Int", "Float", "Double" };
      setName(Sextante.getText("Change_data_type"));
      setGroup(Sextante.getText("Basic_tools_for_raster_layers"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputRasterLayer(INPUT, Sextante.getText("Layer"), true);
         m_Parameters.addSelection(TYPE, Sextante.getText("Data_type"), sTypes);
         addOutputRasterLayer(RESULT, Sextante.getText("Result"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int x, y;
      int iNX, iNY;
      int iType, iRasterType;
      double dValue;
      IRasterLayer input;

      input = m_Parameters.getParameterValueAsRasterLayer(INPUT);
      iType = m_Parameters.getParameterValueAsInt(TYPE);

      switch (iType) {
         case 0:
            iRasterType = IRasterLayer.RASTER_DATA_TYPE_BYTE;
            break;
         case 1:
            iRasterType = IRasterLayer.RASTER_DATA_TYPE_DOUBLE;
            break;
         case 2:
            iRasterType = IRasterLayer.RASTER_DATA_TYPE_INT;
            break;
         case 3:
            iRasterType = IRasterLayer.RASTER_DATA_TYPE_FLOAT;
            break;
         case 4:
         default:
            iRasterType = IRasterLayer.RASTER_DATA_TYPE_DOUBLE;
            break;
      }
      final IRasterLayer result = getNewRasterLayer(RESULT, Sextante.getText("Result"), iRasterType);

      input.setWindowExtent(m_AnalysisExtent);

      iNX = result.getWindowGridExtent().getNX();
      iNY = result.getWindowGridExtent().getNY();

      for (y = 0; (y < iNY) && setProgress(y, iNY); y++) {
         for (x = 0; x < iNX; x++) {
            dValue = input.getCellValueAsDouble(x, y);
            if (!input.isNoDataValue(dValue)) {
               result.setCellValue(x, y, dValue);
            }
            else {
               result.setNoData(x, y);
            }
         }
      }

      return !m_Task.isCanceled();

   }


}
