package es.unex.sextante.gridStatistics.neighborhoodFragmentation;

import java.util.HashMap;

import es.unex.sextante.core.Sextante;
import es.unex.sextante.gridStatistics.neighborhood.patternBase.PatternBaseAlgorithm;

public class NeighborhoodFragmentationAlgorithm
         extends
            PatternBaseAlgorithm {

   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Fragmentation"));
      setGroup(Sextante.getText("Pattern_analysis"));
      super.defineCharacteristics();

   }


   @Override
   protected double processValues() {

      int i;
      int iCells = 0;
      double dValue;
      Double value;
      final HashMap map = new HashMap();

      for (i = 0; i < m_dValues.length; i++) {
         dValue = m_dValues[i];
         if (dValue != NO_DATA) {
            iCells++;
            value = new Double(dValue);
            if (!map.containsKey(value)) {
               map.put(value, "");
            }
         }
      }

      return (((double) map.size() - 1) / ((double) iCells + 1));

   }
}
