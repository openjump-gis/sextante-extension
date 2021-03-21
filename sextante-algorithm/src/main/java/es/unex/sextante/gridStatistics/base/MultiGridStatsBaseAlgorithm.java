

package es.unex.sextante.gridStatistics.base;

import java.awt.image.DataBuffer;
import java.util.ArrayList;

import es.unex.sextante.additionalInfo.AdditionalInfoMultipleInput;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;


public abstract class MultiGridStatsBaseAlgorithm
         extends
            GeoAlgorithm {

   public static final String INPUT        = "INPUT";
   public static final String FORCE_NODATA = "NODATA";
   public static final String RESULT       = "RESULT";


   protected double           NO_DATA;


   @Override
   public void defineCharacteristics() {

      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addMultipleInput(INPUT, Sextante.getText("Layers"), AdditionalInfoMultipleInput.DATA_TYPE_RASTER, true);
         m_Parameters.addBoolean(FORCE_NODATA, Sextante.getText("Force_no-data_value"), true);
         addOutputRasterLayer(RESULT, this.getName());
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i;
      int x, y;
      int iNX, iNY;
      double dValue;
      IRasterLayer[] window;

      NO_DATA = m_OutputFactory.getDefaultNoDataValue();

      final ArrayList input = m_Parameters.getParameterValueAsArrayList("INPUT");
      final boolean bForceNoData = m_Parameters.getParameterValueAsBoolean("NODATA");

      if (input.size() == 0) {
         return false;
      }

      final IRasterLayer result = getNewRasterLayer(RESULT, this.getName(), IRasterLayer.RASTER_DATA_TYPE_DOUBLE);

      result.setNoDataValue(m_OutputFactory.getDefaultNoDataValue());
      result.assignNoData();

      window = new IRasterLayer[input.size()];
      for (i = 0; i < input.size(); i++) {
         window[i] = (IRasterLayer) input.get(i);
         window[i].setWindowExtent(result.getWindowGridExtent());
         if ((window[i].getDataType() == DataBuffer.TYPE_FLOAT) || (window[i].getDataType() == DataBuffer.TYPE_DOUBLE)) {
            window[i].setInterpolationMethod(IRasterLayer.INTERPOLATION_BSpline);
         }
         else {
            window[i].setInterpolationMethod(IRasterLayer.INTERPOLATION_NearestNeighbour);
         }
      }

      iNX = window[0].getNX();
      iNY = window[0].getNY();

      final double dValues[] = new double[input.size()];

      for (y = 0; (y < iNY) && setProgress(y, iNY); y++) {
         for (x = 0; x < iNX; x++) {
            for (i = 0; i < window.length; i++) {
               dValue = window[i].getCellValueAsDouble(x, y);
               if (!window[i].isNoDataValue(dValue)) {
                  dValues[i] = dValue;
               }
               else {
                  if (bForceNoData) {
                     result.setNoData(x, y);
                     break;
                  }
                  else {
                     dValues[i] = m_OutputFactory.getDefaultNoDataValue();
                  }
               }
            }
            result.setCellValue(x, y, processValues(dValues));
         }

      }

      return !m_Task.isCanceled();


   }


   protected abstract double processValues(double[] dValues);


}
