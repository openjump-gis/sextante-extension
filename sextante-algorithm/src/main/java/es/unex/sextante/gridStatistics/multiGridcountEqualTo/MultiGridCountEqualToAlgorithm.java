package es.unex.sextante.gridStatistics.multiGridcountEqualTo;

import es.unex.sextante.core.Sextante;
import es.unex.sextante.gridStatistics.base.MultiGridStatsExtendedBaseAlgorithm;

public class MultiGridCountEqualToAlgorithm
         extends
            MultiGridStatsExtendedBaseAlgorithm {

   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Equal_values_count"));
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
            if (dValue == m_dValue) {
               iCount++;
            }
         }
      }

      return iCount;

   }

}
