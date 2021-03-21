package es.unex.sextante.gridStatistics.neighborhoodRange;

import es.unex.sextante.core.Sextante;
import es.unex.sextante.gridStatistics.base.NeighborhoodStatsBaseAlgorithm;

public class NeighborhoodRangeAlgorithm
         extends
            NeighborhoodStatsBaseAlgorithm {

   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Range__neighbourhood"));
      setGroup(Sextante.getText("Focal_statistics"));
      super.defineCharacteristics();

   }


   @Override
   protected double processValues() {

      int i;
      double dValue;

      double dMin = Double.MAX_VALUE;
      double dMax = Double.NEGATIVE_INFINITY;

      for (i = 0; i < m_dValues.length; i++) {
         dValue = m_dValues[i];
         if (dValue != NO_DATA) {
            if (dValue < dMin) {
               dMin = dValue;
            }
            if (dValue > dMax) {
               dMax = dValue;
            }
         }
      }

      if ((dMax == Double.NEGATIVE_INFINITY) || (dMin == Double.MAX_VALUE)) {
         return NO_DATA;
      }
      else {
         return dMax - dMin;
      }

   }

}
