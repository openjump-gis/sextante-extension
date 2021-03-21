package es.unex.sextante.gridStatistics.neighborhoodCountGreaterThan;

import es.unex.sextante.core.Sextante;
import es.unex.sextante.gridStatistics.base.NeighborhoodStatsExtendedBaseAlgorithm;

public class NeighborhoodCountGreaterThanAlgorithm
         extends
            NeighborhoodStatsExtendedBaseAlgorithm {

   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Larger_values_count__neighbourhood"));
      setGroup(Sextante.getText("Focal_statistics"));
      super.defineCharacteristics();

   }


   @Override
   protected double processValues() {

      int i;
      double dValue;

      int iCount = 0;

      for (i = 0; i < m_dValues.length; i++) {
         dValue = m_dValues[i];
         if (dValue != NO_DATA) {
            if (dValue > m_dValue) {
               iCount++;
            }
         }
      }

      return iCount;

   }

}
