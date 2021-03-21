package es.unex.sextante.gridTools.mergeGrids;

import java.util.ArrayList;

import es.unex.sextante.additionalInfo.AdditionalInfoMultipleInput;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;

public class MergeGridsAlgorithm
         extends
            GeoAlgorithm {

   public static final String METHOD = "METHOD";
   public static final String INPUT  = "INPUT";
   public static final String RESULT = "RESULT";


   @Override
   public void defineCharacteristics() {


      final String[] sMethod = { Sextante.getText("Nearest_neighbour"), Sextante.getText("Bilinear"),
               Sextante.getText("Inverse_distance"), Sextante.getText("Cubic_spline"), Sextante.getText("B-spline") };

      setName(Sextante.getText("Merge_grids"));
      setGroup(Sextante.getText("Basic_tools_for_raster_layers"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addMultipleInput(INPUT, Sextante.getText("Layers"), AdditionalInfoMultipleInput.DATA_TYPE_RASTER, true);
         m_Parameters.addSelection(METHOD, Sextante.getText("Interpolation_method"), sMethod);
         addOutputRasterLayer(RESULT, Sextante.getText("Merged_grids"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i;
      int x, y;
      int iMethod;
      int iNX, iNY;
      double dValue, dValueSum;
      double dValidValues;
      IRasterLayer[] window;

      iMethod = m_Parameters.getParameterValueAsInt(METHOD);
      final ArrayList input = m_Parameters.getParameterValueAsArrayList(INPUT);

      if (input.size() == 0) {
         return false;
      }

      boolean bAddWarning = false;
      int iBands = ((IRasterLayer) input.get(0)).getBandsCount();
      for (i = 0; i < input.size(); i++) {
         final IRasterLayer layer = ((IRasterLayer) input.get(i));
         if (layer.getBandsCount() != iBands) {
            iBands = Math.max(layer.getBandsCount(), iBands);
            bAddWarning = true;
         }
      }

      if (bAddWarning) {
         Sextante.addWarningToLog(Sextante.getText("Layers_to_be_merged_have_different_number_of_bands"));
      }

      final IRasterLayer result = getNewRasterLayer(RESULT, Sextante.getText("Merged_grids"),
               ((IRasterLayer) input.get(0)).getDataType(), iBands);

      result.assignNoData();

      window = new IRasterLayer[input.size()];
      for (i = 0; i < input.size(); i++) {
         window[i] = ((IRasterLayer) input.get(i));
         window[i].setWindowExtent(result.getWindowGridExtent());
         window[i].setInterpolationMethod(iMethod);
      }

      iNX = window[0].getNX();
      iNY = window[0].getNY();

      for (int iBand = 0; iBand < iBands; iBand++) {
         for (y = 0; (y < iNY) && setProgress(y, iNY); y++) {
            for (x = 0; x < iNX; x++) {
               dValueSum = 0;
               dValidValues = 0;
               for (i = 0; i < window.length; i++) {
                  dValue = window[i].getCellValueAsDouble(x, y, iBand);
                  if (!window[i].isNoDataValue(dValue)) {
                     dValidValues++;
                     dValueSum += dValue;
                  }
               }
               if (dValidValues == 0.0) {
                  result.setNoData(x, y);
               }
               else {
                  result.setCellValue(x, y, iBand, dValueSum / dValidValues);
               }
            }
         }
      }

      return !m_Task.isCanceled();

   }

}
