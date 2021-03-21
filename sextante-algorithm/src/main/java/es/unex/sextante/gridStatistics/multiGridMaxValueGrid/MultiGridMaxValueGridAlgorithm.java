package es.unex.sextante.gridStatistics.multiGridMaxValueGrid;

import es.unex.sextante.core.Sextante;
import es.unex.sextante.gridStatistics.base.MultiGridStatsBaseAlgorithm;

public class MultiGridMaxValueGridAlgorithm
         extends
            MultiGridStatsBaseAlgorithm {

   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Maximum_value_layer"));
      setGroup(Sextante.getText("Local_statistics"));
      super.defineCharacteristics();

   }


   @Override
   protected double processValues(final double[] dValues) {

      int i;
      double dValue;

      double dMax = Double.NEGATIVE_INFINITY;
      double layer = NO_DATA;

      for (i = 0; i < dValues.length; i++) {
         dValue = dValues[i];
         if (dValue != NO_DATA) {
            if (dValue > dMax) {
               dMax = dValue;
               layer = i;
            }
         }
      }

      return layer;

   }

}
