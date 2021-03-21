package es.unex.sextante.gridStatistics.multiGridVariance;

import es.unex.sextante.core.Sextante;
import es.unex.sextante.gridStatistics.base.MultiGridStatsBaseAlgorithm;

public class MultiGridVarianceAlgorithm
         extends
            MultiGridStatsBaseAlgorithm {

   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Variance"));
      setGroup(Sextante.getText("Local_statistics"));
      super.defineCharacteristics();

   }


   @Override
   protected double processValues(final double[] dValues) {

      int i;
      int iValidCells = 0;
      double dValue;

      double dSum = 0;
      double dVar = 0;

      for (i = 0; i < dValues.length; i++) {
         dValue = dValues[i];
         if (dValue != NO_DATA) {
            dSum += dValue;
            dVar += (dValue * dValue);
            iValidCells++;
         }
      }

      if (iValidCells > 0) {
         final double dMean = dSum / iValidCells;
         return (dVar / iValidCells - dMean * dMean);
      }
      else {
         return NO_DATA;
      }

   }

}
