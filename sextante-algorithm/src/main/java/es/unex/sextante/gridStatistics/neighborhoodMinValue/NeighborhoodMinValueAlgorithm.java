package es.unex.sextante.gridStatistics.neighborhoodMinValue;

import es.unex.sextante.core.Sextante;
import es.unex.sextante.gridStatistics.base.NeighborhoodStatsBaseAlgorithm;

public class NeighborhoodMinValueAlgorithm
         extends
            NeighborhoodStatsBaseAlgorithm {

   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Minimum__neighbourhood"));
      setGroup(Sextante.getText("Focal_statistics"));
      super.defineCharacteristics();

   }


   @Override
   protected double processValues() {

      int i;
      double dValue;

      double dMin = Double.MAX_VALUE;

      for (i = 0; i < m_dValues.length; i++) {
         dValue = m_dValues[i];
         if (dValue != NO_DATA) {
            if (dValue < dMin) {
               dMin = dValue;
            }
         }
      }

      if (dMin == Double.MAX_VALUE) {
         dMin = NO_DATA;
      }

      return dMin;

   }

}
