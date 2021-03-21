package es.unex.sextante.gridStatistics.multiGridKurtosis;

import es.unex.sextante.core.Sextante;
import es.unex.sextante.gridStatistics.base.MultiGridStatsBaseAlgorithm;

public class MultiGridKurtosisAlgorithm
         extends
            MultiGridStatsBaseAlgorithm {

   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Kurtosis"));
      setGroup(Sextante.getText("Local_statistics"));
      super.defineCharacteristics();

   }


   @Override
   protected double processValues(final double[] dValues) {

      int i;
      int iValidCells = 0;
      double dValue;

      double dMean = 0;
      double dDif, dDif2;
      double dMom2 = 0;
      double dMom4 = 0;

      for (i = 0; i < dValues.length; i++) {
         dValue = dValues[i];
         if (dValue != NO_DATA) {
            dMean += dValue;
            iValidCells++;
         }
      }
      dMean /= iValidCells;
      if (iValidCells > 3) {
         for (i = 0; i < dValues.length; i++) {
            dDif = dValues[i] - dMean;
            dDif2 = dDif * dDif;
            dMom2 += dDif2;
            dMom4 += (dDif2 * dDif2);
         }
         dMom2 /= iValidCells;
         dMom4 /= iValidCells;
         return (dMom4 / Math.pow(dMom2, 2));
      }
      else {
         return NO_DATA;
      }

   }

}
