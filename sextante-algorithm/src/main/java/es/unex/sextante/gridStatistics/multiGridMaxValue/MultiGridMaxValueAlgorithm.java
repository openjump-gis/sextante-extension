package es.unex.sextante.gridStatistics.multiGridMaxValue;

import es.unex.sextante.core.Sextante;
import es.unex.sextante.gridStatistics.base.MultiGridStatsBaseAlgorithm;

public class MultiGridMaxValueAlgorithm
         extends
            MultiGridStatsBaseAlgorithm {

   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Maximum"));
      setGroup(Sextante.getText("Local_statistics"));
      super.defineCharacteristics();

   }


   @Override
   protected double processValues(final double[] dValues) {

      int i;
      double dValue;

      double dMax = Double.NEGATIVE_INFINITY;

      for (i = 0; i < dValues.length; i++) {
         dValue = dValues[i];
         if (dValue != NO_DATA) {
            if (dValue > dMax) {
               dMax = dValue;
            }
         }
      }

      if (dMax == Double.NEGATIVE_INFINITY) {
         dMax = NO_DATA;
      }

      return dMax;

   }

}
