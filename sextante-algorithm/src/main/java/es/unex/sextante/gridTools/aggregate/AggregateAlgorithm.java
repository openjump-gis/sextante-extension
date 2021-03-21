package es.unex.sextante.gridTools.aggregate;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;

public class AggregateAlgorithm
         extends
            GeoAlgorithm {

   public static final String GRID   = "GRID";
   public static final String METHOD = "METHOD";
   public static final String SIZE   = "SIZE";
   public static final String RESULT = "RESULT";

   public static final int    SUM    = 0;
   public static final int    MAX    = 1;
   public static final int    MIN    = 2;
   public static final int    MODE   = 3;


   @Override
   public void defineCharacteristics() {

      final String[] sMethod = { Sextante.getText("Sum"), Sextante.getText("Maximum"), Sextante.getText("Minimum"),
               Sextante.getText("Moda") };

      setName(Sextante.getText("Aggregate"));
      setGroup(Sextante.getText("Basic_tools_for_raster_layers"));
      setUserCanDefineAnalysisExtent(false);

      try {
         m_Parameters.addInputRasterLayer(GRID, Sextante.getText("Layer"), true);
         m_Parameters.addSelection(METHOD, Sextante.getText("M\u00e9todo"), sMethod);
         m_Parameters.addNumericalValue(SIZE, Sextante.getText("Aggregation_factor"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_INTEGER, 2, 2, Integer.MAX_VALUE);
         addOutputRasterLayer(RESULT, Sextante.getText("Aggregated_layer"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int x, y;
      int x2, y2;
      int i, j;
      int iNX, iNY;
      int iSize;
      int iMethod;
      double dMin, dMax;
      double dSum;
      double dValue;
      double dCellSize;
      Integer count;
      Double value;
      final HashMap map = new HashMap();
      AnalysisExtent resultExtent;
      IRasterLayer window;

      iSize = m_Parameters.getParameterValueAsInt(SIZE);
      iMethod = m_Parameters.getParameterValueAsInt(METHOD);
      window = m_Parameters.getParameterValueAsRasterLayer(GRID);

      window.setFullExtent();
      final AnalysisExtent ge = window.getLayerGridExtent();

      iNX = (int) Math.floor((window.getNX() / iSize));
      iNY = (int) Math.floor((window.getNY() / iSize));
      dCellSize = window.getLayerCellSize() * iSize;

      resultExtent = new AnalysisExtent();
      resultExtent.setCellSize(dCellSize);
      resultExtent.setXRange(ge.getXMin(), ge.getXMin() + iNX * dCellSize, true);
      resultExtent.setYRange(ge.getYMin(), ge.getYMin() + iNY * dCellSize, true);


      final String sName = window.getName() + Sextante.getText("[aggregated]");

      final IRasterLayer result = getNewRasterLayer(RESULT, sName, window.getDataType(), resultExtent);

      for (y = 0, y2 = 0; (y2 < iNY) && setProgress(y, iNY); y += iSize, y2++) {
         for (x = 0, x2 = 0; x2 < iNX; x += iSize, x2++) {
            dMax = dMin = window.getCellValueAsDouble(x, y);
            dSum = 0;
            for (i = 0; i < iSize; i++) {
               for (j = 0; j < iSize; j++) {
                  dValue = window.getCellValueAsDouble(x + i, y + j);
                  if (!window.isNoDataValue(dValue)) {
                     if (dValue > dMax) {
                        dMax = dValue;
                     }
                     if (dValue < dMin) {
                        dMin = dValue;
                     }
                     dSum += dValue;
                     value = new Double(dValue);
                     count = (Integer) map.get(new Double(dValue));
                     if (count != null) {
                        count = new Integer(count.intValue() + 1);
                     }
                     else {
                        count = new Integer(1);
                     }
                     map.put(new Double(dValue), count);

                  }
               }
            }


            int iCount;
            int iMaxCount = 0;
            double dMode = window.getNoDataValue();

            final Set set = map.keySet();
            final Iterator iter = set.iterator();

            while (iter.hasNext()) {
               value = (Double) iter.next();
               dValue = value.doubleValue();
               count = (Integer) map.get(value);
               if (count == null) {
                  count = new Integer(1);
               }
               iCount = count.intValue();
               if (iCount > iMaxCount) {
                  dMode = dValue;
                  iMaxCount = iCount;
               }
               map.put(value, new Integer(iCount + 1));
            }
            switch (iMethod) {
               case SUM:
                  result.setCellValue(x2, y2, dSum);
                  break;
               case MIN:
                  result.setCellValue(x2, y2, dMin);
                  break;
               case MAX:
                  result.setCellValue(x2, y2, dMax);
                  break;
               case MODE:
                  result.setCellValue(x, y, dMode);
               default:
                  break;
            }
         }
      }

      return !m_Task.isCanceled();


   }

}
