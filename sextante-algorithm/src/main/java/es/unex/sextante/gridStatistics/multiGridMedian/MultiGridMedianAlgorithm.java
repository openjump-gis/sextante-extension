package es.unex.sextante.gridStatistics.multiGridMedian;

import java.util.ArrayList;
import java.util.Arrays;

import es.unex.sextante.core.Sextante;
import es.unex.sextante.gridStatistics.base.MultiGridStatsBaseAlgorithm;

public class MultiGridMedianAlgorithm
         extends
            MultiGridStatsBaseAlgorithm {

   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Median"));
      setGroup(Sextante.getText("Local_statistics"));
      super.defineCharacteristics();

   }


   @Override
   protected double processValues(final double[] dValues) {

      int i;
      double dValue;
      final ArrayList validCells = new ArrayList();

      for (i = 0; i < dValues.length; i++) {
         dValue = dValues[i];
         if (dValue != NO_DATA) {
            validCells.add(new Double(dValue));
         }
      }

      if (validCells.size() > 0) {
         final Object[] array = validCells.toArray();
         Arrays.sort(array);
         return (((Double) array[validCells.size() / 2]).doubleValue());
      }
      else {
         return NO_DATA;
      }


   }

}
