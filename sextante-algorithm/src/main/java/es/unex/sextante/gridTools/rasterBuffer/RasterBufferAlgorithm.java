package es.unex.sextante.gridTools.rasterBuffer;

import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;

public class RasterBufferAlgorithm
         extends
            GeoAlgorithm {

   private static final int   BUFFER                            = 1;
   private static final int   FEATURE                           = 2;

   public static final String INPUT                             = "INPUT";
   public static final String METHOD                            = "METHOD";
   public static final String DIST                              = "DIST";
   public static final String BUFFER_LAYER                      = "BUFFER_LAYER";

   public static final int    FIXED_DISTANCE                    = 0;
   public static final int    USE_CELL_VALUE_AS_BUFFER_DISTANCE = 0;


   @Override
   public void defineCharacteristics() {

      final String[] sMethod = { Sextante.getText("Fixed_distance"), Sextante.getText("Use_cell_value_as_distance") };

      setName(Sextante.getText("Raster_buffer"));
      setGroup(Sextante.getText("Buffers"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputRasterLayer(INPUT, Sextante.getText("Layer"), true);
         m_Parameters.addSelection(METHOD, Sextante.getText("Method"), sMethod);
         m_Parameters.addNumericalValue(DIST, Sextante.getText("Distance"), AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE,
                  100, 0, Double.MAX_VALUE);
         addOutputRasterLayer(BUFFER_LAYER, Sextante.getText("Buffer"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i, j;
      int x, y;
      int x2, y2;
      int iNX, iNY;
      int iBufFixedDist;
      int iBufferType;
      int iBufDist;
      double dBufDist;
      double dDist = 0;
      double dValue = 0;
      IRasterLayer layer;

      iBufferType = m_Parameters.getParameterValueAsInt(METHOD);
      layer = m_Parameters.getParameterValueAsRasterLayer(INPUT);

      final IRasterLayer result = getNewRasterLayer(BUFFER_LAYER, Sextante.getText("Buffer") + "[" + layer.getName() + "]",
               IRasterLayer.RASTER_DATA_TYPE_BYTE);

      layer.setWindowExtent(result.getWindowGridExtent());

      dBufDist = m_Parameters.getParameterValueAsDouble("DIST") / layer.getWindowCellSize();
      iBufFixedDist = (int) (dBufDist + 2.0);

      result.assign(0.0);

      iNX = layer.getNX();
      iNY = layer.getNY();

      for (y = 0; (y < iNY) && setProgress(y, iNY); y++) {
         for (x = 0; x < iNX; x++) {
            dValue = layer.getCellValueAsDouble(x, y);
            if ((dValue != 0) && !layer.isNoDataValue(dValue)) {
               if (iBufferType == 1) {
                  dBufDist = dValue / layer.getWindowCellSize();
                  iBufDist = (int) (dBufDist + 2.0);
               }
               else {
                  iBufDist = iBufFixedDist;
               }
               for (i = -iBufDist; i < iBufDist + 1; i++) {
                  for (j = -iBufDist; j < iBufDist + 1; j++) {
                     x2 = Math.max(Math.min(iNX - 1, x + i), 0);
                     y2 = Math.max(Math.min(iNY - 1, y + j), 0);
                     dDist = getDist(x, y, x2, y2);
                     if (dDist < dBufDist) {
                        dValue = layer.getCellValueAsDouble(x2, y2);
                        if ((dValue != 0) && !layer.isNoDataValue(dValue)) {
                           result.setCellValue(x2, y2, FEATURE);
                        }
                        else {
                           result.setCellValue(x2, y2, BUFFER);
                        }
                     }
                  }
               }
            }
         }

      }

      return !m_Task.isCanceled();


   }


   private double getDist(final int x1,
                          final int y1,
                          final int x2,
                          final int y2) {

      return Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));

   }

}
