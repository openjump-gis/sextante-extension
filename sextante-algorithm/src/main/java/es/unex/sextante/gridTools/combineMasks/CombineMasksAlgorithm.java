

package es.unex.sextante.gridTools.combineMasks;

import java.util.ArrayList;

import es.unex.sextante.additionalInfo.AdditionalInfoMultipleInput;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;


public class CombineMasksAlgorithm
         extends
            GeoAlgorithm {

   public static final String LAYERS = "LAYERS";
   public static final String RESULT = "RESULT";

   protected double           m_dValue;
   protected double           m_dValue2;


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("CombineMasks"));
      setGroup(Sextante.getText("Calculus_tools_for_raster_layer"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addMultipleInput(LAYERS, Sextante.getText("Layers"), AdditionalInfoMultipleInput.DATA_TYPE_RASTER, true);
         addOutputRasterLayer(RESULT, Sextante.getText("Result"), 1);
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int x, y;
      int iNX, iNY;

      final ArrayList list = (ArrayList) m_Parameters.getParameterValueAsObject(LAYERS);
      final IRasterLayer[] grids = new IRasterLayer[list.size()];
      for (int i = 0; i < grids.length; i++) {
         grids[i] = (IRasterLayer) list.get(i);
         grids[i].setWindowExtent(m_AnalysisExtent);
      }


      final IRasterLayer result = getNewRasterLayer(RESULT, this.getName(), IRasterLayer.RASTER_DATA_TYPE_DOUBLE,
               m_AnalysisExtent);

      result.setNoDataValue(0.0);
      result.assignNoData();

      iNX = m_AnalysisExtent.getNX();
      iNY = m_AnalysisExtent.getNY();

      for (y = 0; (y < iNY) && setProgress(y, iNY); y++) {
         for (x = 0; x < iNX; x++) {
            int i;
            for (i = 0; i < grids.length; i++) {
               final double dValue = grids[i].getCellValueAsDouble(x, y);
               if (!grids[i].isNoDataValue(dValue)) {
                  result.setCellValue(x, y, 1);
                  break;
               }
            }
            if (!(i < grids.length)) {
               result.setNoData(x, y);
            }
         }
      }

      return !m_Task.isCanceled();

   }

}
