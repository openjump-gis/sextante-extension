package es.unex.sextante.gridAnalysis.owa;

import java.util.ArrayList;
import java.util.Arrays;

import es.unex.sextante.additionalInfo.AdditionalInfoMultipleInput;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.parameters.FixedTableModel;
import es.unex.sextante.rasterWrappers.GridWrapper;

public class OWAAlgorithm
         extends
            GeoAlgorithm {

   public static final String INPUT   = "INPUT";
   public static final String WEIGHTS = "WEIGHTS";
   public static final String RESULT  = "RESULT";


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Ordered_Weighted_Averaging__OWA"));
      setGroup(Sextante.getText("Raster_layer_analysis"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addMultipleInput(INPUT, Sextante.getText("Layers"), AdditionalInfoMultipleInput.DATA_TYPE_RASTER, true);
         m_Parameters.addFixedTable(WEIGHTS, Sextante.getText("Weights"), new String[] { Sextante.getText("Pesos") }, 3, false);
         addOutputRasterLayer(RESULT, Sextante.getText("Result"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i;
      int x, y;
      double dResult;
      double dValues[];
      double dWeights[];
      boolean bNoDataValue;
      IRasterLayer windows[];

      final ArrayList layers = m_Parameters.getParameterValueAsArrayList(INPUT);
      final FixedTableModel weights = (FixedTableModel) m_Parameters.getParameterValueAsObject(WEIGHTS);

      if ((layers.size() == 0) || (weights.getRowCount() == 0) || (weights.getRowCount() < layers.size())) {
         return false;
      }

      final IRasterLayer result = getNewRasterLayer(RESULT, Sextante.getText("OWA"), IRasterLayer.RASTER_DATA_TYPE_DOUBLE);

      dWeights = new double[layers.size()];
      windows = new IRasterLayer[layers.size()];

      for (i = 0; i < layers.size(); i++) {
         windows[i] = (IRasterLayer) layers.get(i);
         windows[i].setWindowExtent(result.getWindowGridExtent());
         windows[i].setInterpolationMethod(GridWrapper.INTERPOLATION_BSpline);
         try {
            dWeights[i] = Double.parseDouble(weights.getValueAt(i, 0).toString());
         }
         catch (final NumberFormatException e) {
            throw new GeoAlgorithmExecutionException(Sextante.getText("OWA_Invalid_weight"));
         }
      }

      final int iNX = result.getWindowGridExtent().getNX();
      final int iNY = result.getWindowGridExtent().getNY();

      dValues = new double[layers.size()];

      for (y = 0; (y < iNY) && setProgress(y, iNY); y++) {
         for (x = 0; x < iNX; x++) {
            bNoDataValue = false;
            for (i = 0; i < dValues.length; i++) {
               dValues[i] = windows[i].getCellValueAsDouble(x, y);
               if (windows[i].isNoDataValue(dValues[i])) {
                  bNoDataValue = true;
                  break;
               }
            }
            if (bNoDataValue) {
               result.setNoData(x, y);
            }
            else {
               Arrays.sort(dValues);
               dResult = 0;
               for (i = 0; i < dValues.length; i++) {
                  dResult += (dValues[i] * dWeights[i]);
               }
               result.setCellValue(x, y, dResult);
            }
         }
      }

      return !m_Task.isCanceled();

   }

}
