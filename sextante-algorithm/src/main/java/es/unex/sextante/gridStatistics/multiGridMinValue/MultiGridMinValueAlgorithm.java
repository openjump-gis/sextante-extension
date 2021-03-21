package es.unex.sextante.gridStatistics.multiGridMinValue;

import es.unex.sextante.core.Sextante;
import es.unex.sextante.gridStatistics.base.MultiGridStatsBaseAlgorithm;

public class MultiGridMinValueAlgorithm
         extends
            MultiGridStatsBaseAlgorithm {

   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Minimum"));
      setGroup(Sextante.getText("Local_statistics"));
      super.defineCharacteristics();

   }


   @Override
   protected double processValues(final double[] dValues) {

      int i;
      double dValue;

      double dMin = Double.MAX_VALUE;

      for (i = 0; i < dValues.length; i++) {
         dValue = dValues[i];
         if (dValue != NO_DATA) {
            if (dValue < dMin) {
               dMin = dValue;
            }
         }
      }

      if (dMin == Double.MAX_VALUE) {
         dMin = NO_DATA;
      }

      return dMin;

   }

}
