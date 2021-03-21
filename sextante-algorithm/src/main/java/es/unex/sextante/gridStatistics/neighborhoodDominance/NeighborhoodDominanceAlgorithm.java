package es.unex.sextante.gridStatistics.neighborhoodDominance;

import es.unex.sextante.core.Sextante;
import es.unex.sextante.gridStatistics.neighborhood.patternBase.PatternBaseAlgorithm;

public class NeighborhoodDominanceAlgorithm
         extends
            PatternBaseAlgorithm {

   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Dominance"));
      setGroup(Sextante.getText("Pattern_analysis"));
      super.defineCharacteristics();

   }


   @Override
   protected double processValues() {

      return (Math.log(getNumberOfClasses()) - getDiversity());

   }

}
