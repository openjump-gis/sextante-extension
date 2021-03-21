package es.unex.sextante.gridStatistics.neighborhoodMaxValue;

import es.unex.sextante.core.Sextante;
import es.unex.sextante.gridStatistics.base.NeighborhoodStatsBaseAlgorithm;

public class NeighborhoodMaxValueAlgorithm
         extends
            NeighborhoodStatsBaseAlgorithm {

   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Maximum__neighbourhood"));
      setGroup(Sextante.getText("Focal_statistics"));
      super.defineCharacteristics();

   }


   @Override
   protected double processValues() {

      int i;
      double dValue;

      double dMax = Double.NEGATIVE_INFINITY;

      for (i = 0; i < m_dValues.length; i++) {
         dValue = m_dValues[i];
         if (dValue != NO_DATA) {
            if (dValue > dMax) {
               dMax = dValue;
            }
         }
      }

      if (dMax == Double.NEGATIVE_INFINITY) {
         dMax = NO_DATA;
      }

      return dMax;

   }

}
