package es.unex.sextante.gridStatistics.neighborhood.patternBase;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import es.unex.sextante.gridStatistics.base.NeighborhoodStatsBaseAlgorithm;

public abstract class PatternBaseAlgorithm
         extends
            NeighborhoodStatsBaseAlgorithm {


   protected int getNumberOfClasses() {

      int i;
      double dValue;
      Double value;
      final HashMap map = new HashMap();

      for (i = 0; i < m_dValues.length; i++) {
         dValue = m_dValues[i];
         if (dValue != NO_DATA) {
            value = new Double(dValue);
            if (!map.containsKey(value)) {
               map.put(value, "");
            }
         }
      }

      return map.size();

   }


   protected double getDiversity() {

      int i;
      int iCount = 0;
      int iCells = 0;
      double dProp;
      double dValue;
      double dReturn = 0;
      Integer count;
      Double value;
      final HashMap map = new HashMap();

      for (i = 0; i < m_dValues.length; i++) {
         dValue = m_dValues[i];
         if (dValue != NO_DATA) {
            iCells++;
            value = new Double(dValue);
            count = (Integer) map.get(value);
            if (count != null) {
               count = new Integer(count.intValue() + 1);
            }
            else {
               count = new Integer(1);
            }
            map.put(value, count);
         }
      }

      if (iCells != 0) {
         final Set set = map.keySet();
         final Iterator iter = set.iterator();

         while (iter.hasNext()) {
            value = (Double) iter.next();
            dValue = value.doubleValue();
            count = (Integer) map.get(value);
            iCount = count.intValue();
            dProp = (double) iCount / (double) iCells;
            dReturn += (dProp * Math.log(dProp));
         }

         return dReturn;
      }
      else {
         return NO_DATA;
      }


   }

}
