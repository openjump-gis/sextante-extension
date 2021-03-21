package es.unex.sextante.gridStatistics.neighborhoodVariance;

import es.unex.sextante.core.Sextante;
import es.unex.sextante.gridStatistics.base.NeighborhoodStatsBaseAlgorithm;

public class NeighborhoodVarianceAlgorithm
         extends
            NeighborhoodStatsBaseAlgorithm {

   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Variance__neighbourhood"));
      setGroup(Sextante.getText("Focal_statistics"));
      super.defineCharacteristics();

   }


   @Override
   protected double processValues() {

      int i;
      int iValidCells = 0;
      double dValue;

      double dSum = 0;
      double dVar = 0;

      for (i = 0; i < m_dValues.length; i++) {
         dValue = m_dValues[i];
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
