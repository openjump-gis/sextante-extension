package es.unex.sextante.gridStatistics.multiGridRange;

import es.unex.sextante.core.Sextante;
import es.unex.sextante.gridStatistics.base.MultiGridStatsBaseAlgorithm;

public class MultiGridRangeAlgorithm
         extends
            MultiGridStatsBaseAlgorithm {

   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Range"));
      setGroup(Sextante.getText("Local_statistics"));
      super.defineCharacteristics();

   }


   @Override
   protected double processValues(final double dValues[]) {

      int i;
      double dValue;

      double dMin = Double.MAX_VALUE;
      double dMax = Double.NEGATIVE_INFINITY;

      for (i = 0; i < dValues.length; i++) {
         dValue = dValues[i];
         if (dValue != NO_DATA) {
            if (dValue < dMin) {
               dMin = dValue;
            }
            if (dValue > dMax) {
               dMax = dValue;
            }
         }
      }

      if ((dMax == Double.NEGATIVE_INFINITY) || (dMin == Double.MAX_VALUE)) {
         return NO_DATA;
      }
      else {
         return dMax - dMin;
      }

   }

}
