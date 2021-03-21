package es.unex.sextante.gridStatistics.multiGridSkewness;

import es.unex.sextante.core.Sextante;
import es.unex.sextante.gridStatistics.base.MultiGridStatsBaseAlgorithm;

public class MultiGridSkewnessAlgorithm
         extends
            MultiGridStatsBaseAlgorithm {

   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Skewness"));
      setGroup(Sextante.getText("Local_statistics"));
      super.defineCharacteristics();

   }


   @Override
   protected double processValues(final double[] dValues) {

      int i;
      int iValidCells = 0;
      double dValue;

      double dMean = 0;
      double dDif;
      double dDif2 = 0;
      double dDif3 = 0;

      for (i = 0; i < dValues.length; i++) {
         dValue = dValues[i];
         if (dValue != NO_DATA) {
            dMean += dValue;
            iValidCells++;
         }
      }
      dMean /= iValidCells;
      if (iValidCells > 2) {
         for (i = 0; i < dValues.length; i++) {
            dDif = dValues[i] - dMean;
            dDif2 += (dDif * dDif);
            dDif3 += (dDif * dDif * dDif);
         }
         dDif2 /= iValidCells;
         dDif3 /= iValidCells;
         return (dDif3 / Math.pow(dDif2, 1.5));
      }
      else {
         return NO_DATA;
      }

   }

}
