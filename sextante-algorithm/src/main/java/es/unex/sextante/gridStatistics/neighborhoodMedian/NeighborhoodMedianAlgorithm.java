package es.unex.sextante.gridStatistics.neighborhoodMedian;

import java.util.ArrayList;
import java.util.Arrays;

import es.unex.sextante.core.Sextante;
import es.unex.sextante.gridStatistics.base.NeighborhoodStatsBaseAlgorithm;

public class NeighborhoodMedianAlgorithm
         extends
            NeighborhoodStatsBaseAlgorithm {

   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Median__neighbourhood"));
      setGroup(Sextante.getText("Focal_statistics"));
      super.defineCharacteristics();

   }


   @Override
   protected double processValues() {

      int i;
      double dValue;
      final ArrayList validCells = new ArrayList();

      for (i = 0; i < m_dValues.length; i++) {
         dValue = m_dValues[i];
         if (dValue != NO_DATA) {
            validCells.add(new Double(dValue));
         }
      }

      if (validCells.size() > 0) {
         final Object[] array = validCells.toArray();
         Arrays.sort(array);
         return (((Double) array[validCells.size() / 2]).doubleValue());
      }
      else {
         return NO_DATA;
      }

   }

}
