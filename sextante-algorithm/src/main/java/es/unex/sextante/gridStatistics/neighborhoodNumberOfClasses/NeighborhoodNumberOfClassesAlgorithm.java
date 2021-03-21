package es.unex.sextante.gridStatistics.neighborhoodNumberOfClasses;

import es.unex.sextante.core.Sextante;
import es.unex.sextante.gridStatistics.neighborhood.patternBase.PatternBaseAlgorithm;

public class NeighborhoodNumberOfClassesAlgorithm
         extends
            PatternBaseAlgorithm {

   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Number_of_different_classes"));
      setGroup(Sextante.getText("Pattern_analysis"));
      super.defineCharacteristics();

   }


   @Override
   protected double processValues() {

      return getNumberOfClasses();

   }

}
