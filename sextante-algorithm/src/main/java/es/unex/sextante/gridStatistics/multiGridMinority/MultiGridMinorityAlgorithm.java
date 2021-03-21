package es.unex.sextante.gridStatistics.multiGridMinority;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import es.unex.sextante.core.Sextante;
import es.unex.sextante.gridStatistics.base.MultiGridStatsBaseAlgorithm;

public class MultiGridMinorityAlgorithm
         extends
            MultiGridStatsBaseAlgorithm {

   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Minority"));
      setGroup(Sextante.getText("Local_statistics"));
      super.defineCharacteristics();

   }


   @Override
   protected double processValues(final double[] dValues) {

      int i;
      int iCount;
      int iMinCount = Integer.MAX_VALUE;
      double dValue;
      double dReturn = NO_DATA;
      Integer count;
      Double value;
      final HashMap map = new HashMap();

      for (i = 0; i < dValues.length; i++) {
         dValue = dValues[i];
         if (dValue != NO_DATA) {
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

      final Set set = map.keySet();
      final Iterator iter = set.iterator();

      while (iter.hasNext()) {
         value = (Double) iter.next();
         dValue = value.doubleValue();
         count = (Integer) map.get(value);
         iCount = count.intValue();
         if (iCount < iMinCount) {
            dReturn = dValue;
            iMinCount = iCount;
         }
         else if (iCount == iMinCount) {
            dReturn = NO_DATA;
         }
      }

      return dReturn;

   }

}
