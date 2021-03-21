package es.unex.sextante.gridStatistics.multiGridMeanValue;

import es.unex.sextante.core.Sextante;
import es.unex.sextante.gridStatistics.base.MultiGridStatsBaseAlgorithm;

public class MultiGridMeanValueAlgorithm
         extends
            MultiGridStatsBaseAlgorithm {

   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Mean"));
      setGroup(Sextante.getText("Local_statistics"));
      super.defineCharacteristics();

   }


   @Override
   protected double processValues(final double[] dValues) {

      int i;
      double dValue;
      int iValidCells = 0;

      double dMean = 0;

      for (i = 0; i < dValues.length; i++) {
         dValue = dValues[i];
         if (dValue != NO_DATA) {
            dMean += dValue;
            iValidCells++;
         }
      }

      if (iValidCells > 0) {
         return (dMean / iValidCells);
      }
      else {
         return NO_DATA;
      }

   }

}
