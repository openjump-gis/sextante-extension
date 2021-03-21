package es.unex.sextante.gridCategorical.reclassifyConsecutive;

import java.util.HashMap;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;

public class ReclassifyConsecutiveAlgorithm
         extends
            GeoAlgorithm {

   public static final String INPUT   = "INPUT";
   public static final String RECLASS = "RECLASS";


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Reclassify_into_ordered_classes"));
      setGroup(Sextante.getText("Reclassify_raster_layers"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputRasterLayer(INPUT, Sextante.getText("Layer"), true);
         addOutputRasterLayer(RECLASS, Sextante.getText("Reclassify"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int x, y;
      int iClass = 1;
      int iNX, iNY;
      HashMap map;
      Integer ClassID;

      final IRasterLayer window = m_Parameters.getParameterValueAsRasterLayer(INPUT);

      final IRasterLayer result = getNewRasterLayer(RECLASS, window.getName() + Sextante.getText("[reclassified]"),
               IRasterLayer.RASTER_DATA_TYPE_INT);

      window.setWindowExtent(result.getWindowGridExtent());
      window.setInterpolationMethod(IRasterLayer.INTERPOLATION_NearestNeighbour);

      iNX = window.getNX();
      iNY = window.getNY();

      map = new HashMap();

      for (y = 0; (y < iNY) && setProgress(y, iNY); y++) {
         for (x = 0; x < iNX; x++) {
            final double dValue = window.getCellValueAsDouble(x, y);
            if (!window.isNoDataValue(dValue)) {
               ClassID = new Integer(window.getCellValueAsInt(x, y));
               if (map.containsKey(ClassID)) {
                  result.setCellValue(x, y, ((Integer) map.get(ClassID)).intValue());
               }
               else {
                  result.setCellValue(x, y, iClass);
                  map.put(ClassID, new Integer(iClass));
                  iClass++;
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
