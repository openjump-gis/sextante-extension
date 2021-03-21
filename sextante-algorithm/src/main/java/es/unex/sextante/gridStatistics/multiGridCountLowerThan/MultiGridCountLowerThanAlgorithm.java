package es.unex.sextante.gridStatistics.multiGridCountLowerThan;

import es.unex.sextante.core.Sextante;
import es.unex.sextante.gridStatistics.base.MultiGridStatsExtendedBaseAlgorithm;

public class MultiGridCountLowerThanAlgorithm
         extends
            MultiGridStatsExtendedBaseAlgorithm {

   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Lower_values_count"));
      setGroup(Sextante.getText("Local_statistics"));
      super.defineCharacteristics();

   }


   @Override
   protected double processValues(final double[] dValues) {

      int i;
      double dValue;

      int iCount = 0;

      for (i = 0; i < dValues.length; i++) {
         dValue = dValues[i];
         if (dValue != NO_DATA) {
            if (dValue < m_dValue) {
               iCount++;
            }
         }
      }

      return iCount;

   }

}
