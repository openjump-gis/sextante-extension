package es.unex.sextante.gridStatistics.neighborhoodDiversity;

import es.unex.sextante.core.Sextante;
import es.unex.sextante.gridStatistics.neighborhood.patternBase.PatternBaseAlgorithm;

public class NeighborhoodDiversityAlgorithm
         extends
            PatternBaseAlgorithm {

   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Diversity"));
      setGroup(Sextante.getText("Pattern_analysis"));
      super.defineCharacteristics();

   }


   @Override
   protected double processValues() {

      return getDiversity();

   }

}
