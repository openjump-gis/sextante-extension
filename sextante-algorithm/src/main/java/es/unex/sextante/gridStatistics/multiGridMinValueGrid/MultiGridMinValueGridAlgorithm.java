package es.unex.sextante.gridStatistics.multiGridMinValueGrid;

import es.unex.sextante.core.Sextante;
import es.unex.sextante.gridStatistics.base.MultiGridStatsBaseAlgorithm;

public class MultiGridMinValueGridAlgorithm
         extends
            MultiGridStatsBaseAlgorithm {

   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Minimum_value_layer"));
      setGroup(Sextante.getText("Local_statistics"));
      super.defineCharacteristics();

   }


   @Override
   protected double processValues(final double[] dValues) {

      int i;
      double dValue;

      double dMin = Double.MAX_VALUE;
      double layer = NO_DATA;

      for (i = 0; i < dValues.length; i++) {
         dValue = dValues[i];
         if (dValue != NO_DATA) {
            if (dValue > dMin) {
               dMin = dValue;
               layer = i;
            }
         }
      }

      return layer;

   }

}
