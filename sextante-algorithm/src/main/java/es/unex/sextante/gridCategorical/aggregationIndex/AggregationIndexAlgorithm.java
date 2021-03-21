package es.unex.sextante.gridCategorical.aggregationIndex;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.ITable;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.exceptions.UnsupportedOutputChannelException;

public class AggregationIndexAlgorithm
         extends
            GeoAlgorithm {

   public static final String RESULT       = "RESULT";
   public static final String INPUT        = "INPUT";

   private final int          m_iOffsetX[] = { -1, 0, 0, 1 };
   private final int          m_iOffsetY[] = { 0, -1, 1, 0 };

   int                        m_iNX, m_iNY;
   int                        m_iTotalArea;
   IRasterLayer               m_Window;
   HashMap                    m_Map;
   boolean                    m_IsCellAlreadyVisited[][];


   @Override
   public void defineCharacteristics() {

      this.setName(Sextante.getText("Aggregation_index"));
      setGroup(Sextante.getText("Raster_categories_analysis"));
      setUserCanDefineAnalysisExtent(false);

      try {
         m_Parameters.addInputRasterLayer(INPUT, Sextante.getText("Layer"), true);
         addOutputTable(RESULT, Sextante.getText("Aggregation_index"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      m_Window = m_Parameters.getParameterValueAsRasterLayer(INPUT);

      m_Window.setFullExtent();

      m_iNX = m_Window.getNX();
      m_iNY = m_Window.getNY();

      m_Map = new HashMap();

      if (calculateIndex()) {
         createTable();
         return true;
      }
      else {
         return false;
      }

   }


   private boolean calculateIndex() {

      int i;
      int x, y;
      int iClass, iClass2;
      AggregationInfo info;

      m_iTotalArea = 0;

      for (y = 0; (y < m_iNY) && setProgress(y, m_iNY); y++) {
         for (x = 0; x < m_iNX; x++) {
            iClass = m_Window.getCellValueAsInt(x, y);
            info = (AggregationInfo) m_Map.get(new Integer(iClass));
            if (info != null) {
               m_iTotalArea++;
               info.iArea++;
               for (i = 0; i < 4; i++) {
                  iClass2 = m_Window.getCellValueAsInt(x + m_iOffsetX[i], y + m_iOffsetY[i]);
                  if (iClass2 == iClass) {
                     info.iAggregation++;
                  }
               }
            }
            else {
               info = new AggregationInfo(iClass);
               m_iTotalArea++;
               info.iArea++;
               for (i = 0; i < 4; i++) {
                  iClass2 = m_Window.getCellValueAsInt(x + m_iOffsetX[i], y + m_iOffsetY[i]);
                  if (iClass2 == iClass) {
                     info.iAggregation++;
                  }
               }
               m_Map.put(new Integer(iClass), info);
            }
         }
      }

      return !m_Task.isCanceled();

   }


   private void createTable() throws UnsupportedOutputChannelException {

      int iLargestInt;
      int iRemainder;
      int iMaxEii;
      Object[] values;
      AggregationInfo info;
      final Set set = m_Map.keySet();
      final Iterator iter = set.iterator();

      final String sFields[] = { Sextante.getText("Class"), Sextante.getText("Area_cells"), Sextante.getText("Area[%]"),
               Sextante.getText("Aggregation_index") };
      final Class types[] = { Integer.class, Integer.class, Double.class, Double.class };
      final String sTableName = Sextante.getText("Aggregation_index_[") + m_Window.getName() + "]";

      final ITable table = getNewTable(RESULT, sTableName, types, sFields);
      values = new Object[4];

      while (iter.hasNext()) {
         info = (AggregationInfo) m_Map.get(iter.next());
         values[0] = new Integer(info.iClass);
         values[1] = new Integer(info.iArea);
         values[2] = new Double((double) info.iArea / (double) m_iTotalArea * 100.);
         iLargestInt = (int) Math.floor(Math.sqrt(info.iArea));
         iRemainder = (info.iArea - (iLargestInt * iLargestInt));
         if (iRemainder != 0) {
            if (iRemainder < iLargestInt) {
               iMaxEii = 2 * iLargestInt * (iLargestInt - 1) + 2 * iRemainder - 1;
            }
            else {
               iMaxEii = 2 * iLargestInt * (iLargestInt - 1) + 2 * iRemainder - 2;
            }
         }
         else {
            iMaxEii = 2 * iLargestInt * (iLargestInt - 1);
         }
         values[3] = new Double((double) info.iAggregation / (double) iMaxEii / 2.0);
         table.addRecord(values);
      }

   }

   private class AggregationInfo {

      public int iClass;
      public int iArea;
      public int iAggregation;


      public AggregationInfo(final int i) {

         iClass = i;
         iArea = 0;
         iAggregation = 0;

      }

   }

}
