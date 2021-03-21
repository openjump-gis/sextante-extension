package es.unex.sextante.gridStatistics.neighborhoodKurtosis;

import es.unex.sextante.core.Sextante;
import es.unex.sextante.gridStatistics.base.NeighborhoodStatsBaseAlgorithm;

public class NeighborhoodKurtosisAlgorithm
         extends
            NeighborhoodStatsBaseAlgorithm {

   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Kurtosis__neighbourhood"));
      setGroup(Sextante.getText("Focal_statistics"));
      super.defineCharacteristics();

   }


   @Override
   protected double processValues() {

      int i;
      int iValidCells = 0;
      double dValue;

      double dMean = 0;
      double dDif, dDif2;
      double dMom2 = 0;
      double dMom4 = 0;

      for (i = 0; i < m_dValues.length; i++) {
         dValue = m_dValues[i];
         if (dValue != NO_DATA) {
            dMean += dValue;
            iValidCells++;
         }
      }
      dMean /= iValidCells;
      if (iValidCells > 3) {
         for (i = 0; i < m_dValues.length; i++) {
            dDif = m_dValues[i] - dMean;
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
